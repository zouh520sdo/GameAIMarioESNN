package gameai.neuralnetwork;

import java.util.Random;

public class Layer {
	double[][] weights;
	int row;
	int col;
	Random r;
	
	public Layer(int row, int col) {
		weights = new double[row][col];
		this.row = row;
		this.col = col;
		r = new Random(System.currentTimeMillis());
	}
	
	/**
	 * Size of w must be this.row * this.col
	 * @param w
	 */
	public void loadWeights(double[] w) {
		for (int i=0; i<row; i++) {
			for (int j=0; j<col; j++) {
				weights[i][j] = w[i*col + j];
			}
		}
	}
	
	public void randomWeights() {
		for (int i=0; i<row; i++) {
			for (int j=0; j<col; j++) {
				weights[i][j] = r.nextGaussian();
			}
		}
	}
	
	double[] calc(double[] input, boolean isActive) {
		if (input == null) return null;
		double[] output = new double[col];
		for (int j=0; j < col; j++) {
			output[j] = 0;
			for (int i=0; i < row; i++) {
				output[j] += (input[i]*weights[i][j]);
			}
			output[j] = sigmoid(output[j]);
			if (isActive) {
				output[j] = activationFunction(output[j]);
			}
		}
		return output;
	}
	
	public static double activationFunction(double input) {
		return input > 0.5? input : 0;
	}
	
	public static double sigmoid(double x) {
		return 1.0/(1+Math.exp(-x));  //range: 0 .. multiplier
	}
}
