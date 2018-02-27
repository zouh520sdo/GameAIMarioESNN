package gameai.mariocontroller;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import gameai.neuralnetwork.NeuralNetwork;
import java.util.Random;

public class MarioESNNController extends BasicMarioAIAgent implements Agent {

	public NeuralNetwork NN;
	public int NNInputLength;
	public double sigma;
	protected double[] input;
	public String startingWeightsFile;
	
	Random r;
	public MarioESNNController() {
	    super("ESNNController");
	    r = new Random(System.currentTimeMillis());
	    sigma = r.nextDouble() * 2;
	    startingWeightsFile = null;
	    reset();
	}
	
	public MarioESNNController(MarioESNNController other) {
	    super("ESNNController");
	    r = new Random(System.currentTimeMillis());
	    sigma = other.sigma;
	    NNInputLength = other.NNInputLength;
	    NN = new NeuralNetwork(NNInputLength, Environment.numberOfButtons);
	    NN.loadWeights(other.NN.getWeights());
	    input = new double[NNInputLength];
	    reset();
	}
	
	@Override
	public void reset() {
		action = new boolean[Environment.numberOfButtons];
	}
	
	@Override
	public boolean[] getAction() {
		
		// wait for environment info got assigned in BasicMarioAgent
		if (NN == null) {
		    NNInputLength = levelScene.length * levelScene[0].length +
					enemies.length * enemies[0].length +
					marioState.length + Environment.numberOfButtons;
		    input = new double[NNInputLength];
		    NN = new NeuralNetwork(NNInputLength, Environment.numberOfButtons);
		    
		    if (startingWeightsFile == null) {
		    	NN.randomWeights();
		    }
		    else {
		    	NN.loadWeights(startingWeightsFile);
		    }
		    NN.writeWeightsToFile("test1.txt");
		}
		
		updateInput();
		action = NN.getButtons(input);
		return action;
	}
	
	// Update input
	protected void updateInput() {
		int index = 0;
		
		// Level scene info
		for (int i=0; i<levelScene.length; i++) {
			for (int j=0; j<levelScene[i].length; j++) {
				input[index] = levelScene[i][j];
				index++;
			}
		}
		
		// Enemies info
		for (int i=0; i<enemies.length; i++) {
			for (int j=0; j<enemies[i].length; j++) {
				input[index] = enemies[i][j];
				index++;
			}
		}
		
		// Mario state
		for (int i=0; i<marioState.length; i++) {
			input[index] = marioState[i];
			index++;
		}
		
		// Last buttons 
		for (int i=0; i<action.length; i++) {
			if (action[i]) {
				input[index] = 1;
			}
			else {
				input[index] = 0;
			}
		}
	}
}
