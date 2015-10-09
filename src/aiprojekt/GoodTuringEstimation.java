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
 * @author Anton Jansson
 *
 */
public class GoodTuringEstimation {	
	private final Map<Integer, Integer> frequencyOfFrequencies = new TreeMap<>();

	private int total = 0;
	
	//The parameters in the log-linear fitting
	private double a;
	private double b;
	
	private boolean useSmoothing = false;
	
	/**
	 * Creates a new Good-Turing smoothing
	 */
	public GoodTuringEstimation() {
		
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
			
			//Calculate Z_r
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
			
			//Do a least square fitting to log(Z_r)=a+b*log(r)
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
//			
//			try (BufferedWriter writer = new BufferedWriter(new FileWriter("goodturing.m"))) {
//				StringBuilder xBuilder = new StringBuilder();
//				StringBuilder yBuilder = new StringBuilder();
//				
//				xBuilder.append("r = [");
//				yBuilder.append("Nr = [");
//				
//				for (int r = 0; r < rs[rs.length - 1]; r++) {
//					if (r != 0) {
//						xBuilder.append(" ");
//						yBuilder.append(" ");
//					}
//					
//					xBuilder.append(r);
//					yBuilder.append(this.estimate(r));
//				}
//				
//				xBuilder.append("];");
//				yBuilder.append("];");
//				
//				writer.append(xBuilder.toString() + "\n");
//				writer.append(yBuilder.toString() + "\n");
//				writer.append("plot(r, Nr)");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			//If b > -1, the smoothing won't work.
			if (this.b > -1) {
				this.useSmoothing = false;
			}
		} else {
			this.a = 0.0;
			this.b = 0.0;
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
				return this.calculateSmoothedCount(count + 1) / this.total;
			} else {
				return ((count + 1) * this.calculateSmoothedCount(count + 1)) / this.calculateSmoothedCount(count);
			}
		} else {
			//Maybe do something better
			return count;
		}
	}
}
