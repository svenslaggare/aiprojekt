package aiprojekt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Represents a Good-Turing estimation
 */
public class GoodTuringEstimation {	
	private final Map<Integer, Integer> frequencyOfFrequencies = new TreeMap<>();

	private int total = 0;
	
	//The parameters in the log-linear fitting
	private double a;
	private double b;
	
	private boolean useSmoothing = true;
	private final boolean saveOutput = true;
	
	/**
	 * Creates a new Good-Turing smoothing
	 */
	public GoodTuringEstimation() {
		
	}
	
	/**
	 * Returns the total number of seen words
	 */
	public int getTotal() {
		return this.total;
	}
	
	/**
	 * Returns the constant factor
	 */
	public double getA() {
		return this.a;
	}
	
	/**
	 * Returns the slope
	 */
	public double getB() {
		return this.b;
	}
	
	/**
	 * Sets the total
	 * @param total The total
	 */
	public void setTotal(int total) {
		this.total = total;
	}

	/**
	 * Sets the log-linear parameters (a+b*log(r))
	 * @param a The constant factor
	 * @param b The slope
	 */
	public void setLogLinear(double a, double b) {
		this.a = a;
		this.b = b;
	}
	
	/**
	 * Adds an observation of the given count
	 * @param count The count
	 */
	public void addObservation(int count) {
		int frequency = 0;
		if (this.frequencyOfFrequencies.containsKey(count)) {
			frequency = this.frequencyOfFrequencies.get(count);
		}
		
		this.frequencyOfFrequencies.put(count, frequency + 1);
	}
	
	/**
	 * Calculates the smoothed Good-Turing estimation based on the observations
	 */
	public void fitToData() {
		if (this.frequencyOfFrequencies.size() > 1) {
			//Calculate N
			for (Map.Entry<Integer, Integer> current : this.frequencyOfFrequencies.entrySet()) {
				this.total += current.getKey() * current.getValue();
			}
			
			//Calculate Zr
			Integer[] rs = this.frequencyOfFrequencies.keySet().toArray(new Integer[this.frequencyOfFrequencies.size()]);
			Map<Integer, Double> zR = new TreeMap<>();
					
			for (int i = 0; i < rs.length; i++) {
				int r = rs[i];
				int t = 0;
				int q = 0;
				
				if (i == 0) {
					q = 0;
				} else {
					q = rs[i - 1];
				}
				
				if (i == rs.length - 1) {
					t = 2 * r - q;
				} else {
					t = rs[i + 1];
				}
				
				zR.put(r, this.frequencyOfFrequencies.get(r) / (0.5 * (t - q)));
			}
			
			//Do a least square fitting to log(Zr)=a+b*log(r)
			double[][] matrixA = new double[zR.size()][2];
			double[] logZR = new double[zR.size()];
			
			for (int i = 0; i < rs.length; i++) {
				int r = rs[i];
				matrixA[i][0] = 1;
				matrixA[i][1] = Math.log10(r);
				logZR[i] = Math.log10(zR.get(r));
			}	
			
			RealMatrix A = MatrixUtils.createRealMatrix(matrixA);
			RealMatrix AT = A.transpose();
			RealMatrix lhs = AT.multiply(A);
			RealMatrix rhs = AT.multiply(MatrixUtils.createColumnRealMatrix(logZR));
					
			DecompositionSolver solver = new LUDecomposition(lhs).getSolver();
			RealVector solution = solver.solve(rhs.getColumnVector(0));
			this.a = solution.getEntry(0);
			this.b = solution.getEntry(1);
				
//			System.err.println("a = " + this.a);
//			System.err.println("b = " + this.b);
			
			if (this.saveOutput) {
//				this.saveData(rs);		
				this.saveDiscountingData(rs);
			}
			
			//If b > -1, the smoothing won't work.
			if (this.b > -1) {
				this.useSmoothing = false;
			}
		} else {
			this.a = 0.0;
			this.b = 0.0;
			this.useSmoothing = false;
		}
	}
	
	/**
	 * Saves the data to a MATLAB file
	 * @param rs The sorted counts
	 */
	private void saveData(Integer[] rs) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("goodturing.m"))) {
			StringBuilder xBuilder = new StringBuilder();
			StringBuilder yBuilder = new StringBuilder();
			
			writer.append("hold on\n");
			
			xBuilder.append("r = [");
			yBuilder.append("Nr = [");

//			for (int r = 0; r < rs[rs.length - 1]; r++) {
			for (int r = 0; r < 2000; r++) {
				if (r != 0) {
					xBuilder.append(" ");
					yBuilder.append(" ");
				}
				
				xBuilder.append(r);
				if (r > 0) {
					yBuilder.append(this.estimate(r));
				} else {
					yBuilder.append(this.estimate(r));
				}
			}
			
			xBuilder.append("];");
			yBuilder.append("];");
			
			writer.append(xBuilder.toString() + "\n");
			writer.append(yBuilder.toString() + "\n");
			writer.append("plot(r, Nr)");
			
			writer.append("\n\n");
			
			xBuilder = new StringBuilder();
			yBuilder = new StringBuilder();
			
			xBuilder.append("rE = [");
			yBuilder.append("NrE = [");

			boolean isFirst = true;
			for (int i = 0; i < rs.length; i++) {
				int r = rs[i];
				int j = i + 1;
				
				if (j < rs.length && rs[j] == r + 1) {
					if (!isFirst) {
						xBuilder.append(" ");
						yBuilder.append(" ");
					} else {
						isFirst = false;
					}
					
					xBuilder.append(r);
					yBuilder.append((r + 1) * (double)this.frequencyOfFrequencies.get(r + 1)
							/ this.frequencyOfFrequencies.get(r));
				}
			}
			
			xBuilder.append("];");
			yBuilder.append("];");
			
			writer.append(xBuilder.toString() + "\n");
			writer.append(yBuilder.toString() + "\n");
			writer.append("plot(rE, NrE, '.red')");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the discounting data to file
	 * @param rs The sorted counts
	 */
	private void saveDiscountingData(Integer[] rs) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("discounting.m"))) {
			StringBuilder xBuilder = new StringBuilder();
			StringBuilder yBuilder = new StringBuilder();
			
			writer.append("hold on\n");
			
			xBuilder.append("r = [");
			yBuilder.append("d = [");

			for (int r = 0; r < 100; r++) {
				if (r != 0) {
					xBuilder.append(" ");
					yBuilder.append(" ");
				}
				
				xBuilder.append(r);
				if (r > 0) {
					yBuilder.append(this.estimate(r) / r);
				} else {
					yBuilder.append(this.estimate(r));
				}
			}
			
			xBuilder.append("];");
			yBuilder.append("];");
			
			writer.append(xBuilder.toString() + "\n");
			writer.append(yBuilder.toString() + "\n");
			writer.append("plot(r, d)");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the smoothed count for the given count
	 * @param count The count
	 */
	private double calculateSmoothedCount(int count) {
		return Math.pow(10, this.a + this.b * Math.log10(count));
	}
	
	/**
	 * Returns the Good-Turing estimation of the given count
	 * @param count The count
	 */
	public double estimate(int count) {
		if (this.useSmoothing) {
			if (count == 0) {
				return this.calculateSmoothedCount(1) / this.total;
			} else {
				return ((count + 1) * this.calculateSmoothedCount(count + 1)) / this.calculateSmoothedCount(count);
			}
		} else {
			//Maybe do something better
			return count;
		}
	}
}
