package aiprojekt;

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
	
	//The parameters in the log-linear fitting
	private double a;
	private double b;
	
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
	}
	
	/**
	 * Returns the Good-Turing estimation of the given count
	 * @param count The count
	 */
	public double estimate(int count) {
		double smooted1 = Math.pow(10, this.a * Math.log10(count + 1) + b);
		double smooted2 = Math.pow(10, this.a * Math.log10(count) + b);
		return (count + 1) * smooted1 / smooted2;
	}
}
