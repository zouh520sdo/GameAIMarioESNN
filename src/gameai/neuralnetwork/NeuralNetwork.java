package gameai.neuralnetwork;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class NeuralNetwork {
	
	/*
	 * Layer 1 (hidden): 
	 * 		number of Mario's status(12) + obstacles + enemies * 12
	 * Layer 2 (hidden): 
	 * 		12 * 12
	 * Layer 3: 
	 * 		12 * button amount (6)
	 * 
	 * {N, 12, 12, O}
	 */
	public Layer[] layers;
	public int N;
	public int O;
	public double[] weights;
	
	public int weightsLength;
	
	Random r;
	
	public NeuralNetwork(int inputAmount, int outputAmount) {
		N = inputAmount;
		O = outputAmount;
		layers = new Layer[3];
		layers[0] = new Layer(N, 12);
		layers[1] = new Layer(12, 12);
		layers[2] = new Layer(12, O);
		weightsLength = N*12+12*12+12*O;
		weights = new double[weightsLength];
		r = new Random(System.currentTimeMillis());
	}
	
	/**
	 * Size of weights must be N*12 + 12*12 + 12*O
	 * @param weights
	 */
	public void loadWeights(double[] weights) {
		this.weights = weights;
		int from = 0;
		for (int i=0; i<layers.length; i++) {
			int to = from + layers[i].row*layers[i].col;
			double[] w = Arrays.copyOfRange(weights, from, to);
			from = to;
			layers[i].loadWeights(w);
		}
	}
	
	/**
	 * Load weights from text file
	 * @param filename text file that stores weights
	 */
	public void loadWeights(String filename) {
		try {
			String line = null;
			FileReader reader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(reader);
			int index = 0;
			while ((line = bufferedReader.readLine()) != null) {
				if (index >= weights.length) break;
				weights[index] = Double.parseDouble(line);
				index++;
			}
			bufferedReader.close();
			
			// Load
			loadWeights(weights);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write weights into text file
	 * @param filename
	 */
	public void writeWeightsToFile(String filename) {
		try {
			FileWriter writer = new FileWriter(filename, false);
			PrintWriter print = new PrintWriter(writer);
			
			for (int i=0; i<weights.length; i++) {
				print.println(weights[i]);
			}
			print.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Random a weights for NN
	 */
	public void randomWeights() {
		for (int i=0; i<weights.length; i++) {
			weights[i] = r.nextGaussian();
		}
		loadWeights(weights);
	}
	
	public double[] getWeights() {
		return this.weights;
	}
	
	/**
	 * 
	 * @param input
	 * @return
	 */
	public double[] calc(double[] input) {
		double[] output = input;
		for (int i=0; i<layers.length; i++) {
			output = layers[i].calc(output, true);
		}
		return output;
	}
	
	/**
	 * Calculate throughout NN and get buttons map
	 * @param input
	 * @return
	 */
	public boolean[] getButtons(double[] input) {
		double[] rawOutput = calc(input);
		boolean[] buttons = new boolean[rawOutput.length];
		
		if (rawOutput[0] == 0 && rawOutput[1] == 0) {
			buttons[0] = false;
			buttons[1] = false;
		}
		else if (rawOutput[1] >= rawOutput[0]) {
			buttons[1] = true;
			buttons[0] = false;
		}
		else {
			buttons[1] = false;
			buttons[0] = true;
		}
		
		for (int i=2; i<buttons.length; i++) {
			
			if (rawOutput[i] == 0) {
				buttons[i] = false;
			}
			else {
				buttons[i] = true;
			}
		}
		return buttons;
	}
}
