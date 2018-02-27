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
	public int receptiveWidth = 8;
	public int receptiveHeight = 8;
	public int stateAmount = 11; // < 12
	protected double[] input;
	
	Random r;
	
	/**
	 * Initialize with startingWeightsFile if given
	 * @param startingWeightsFile Weights text file, can be null
	 */
	public MarioESNNController() {
	    super("ESNNController");
	    r = new Random(System.currentTimeMillis());
	    sigma = r.nextDouble() * 2;
	    
		// wait for environment info got assigned in BasicMarioAgent
	    NNInputLength = (2 * receptiveWidth + 1) * (2 * receptiveHeight + 1) +
	    		(2 * receptiveWidth + 1) * (2 * receptiveHeight + 1) +
				stateAmount + Environment.numberOfButtons;
	    input = new double[NNInputLength];
	    NN = new NeuralNetwork(NNInputLength, Environment.numberOfButtons);
	    
    	NN.randomWeights();
	    reset();
	}
	
	public MarioESNNController(String startingWeightsFile) {
	    super("ESNNController");
	    r = new Random(System.currentTimeMillis());
	    sigma = r.nextDouble() * 2;
		
		// wait for environment info got assigned in BasicMarioAgent
	    NNInputLength = (2 * receptiveWidth + 1) * (2 * receptiveHeight + 1) +
	    		(2 * receptiveWidth + 1) * (2 * receptiveHeight + 1) +
				stateAmount + Environment.numberOfButtons;
	    input = new double[NNInputLength];
	    NN = new NeuralNetwork(NNInputLength, Environment.numberOfButtons);
	    
	    if (startingWeightsFile == null) {
	    	NN.randomWeights();
	    }
	    else {
	    	NN.loadWeights(startingWeightsFile);
	    }
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
		updateInput();
		action = NN.getButtons(input);
		return action;
	}
	
	public int getReceptiveEnemyCellValue(int x, int y)
	{
	    if (x < 0 || x >= enemies.length || y < 0 || y >= enemies[0].length)
	        return 0;

	    return enemies[x][y];
	}
	
	public int getMarioStateValue(int x)
	{
	    if (x < 0 || x >= marioState.length)
	        return 0;

	    return marioState[x];
	}
	
	// Update input
	protected void updateInput() {
		int index = 0;
		
		// Level scene info
		for (int i=-receptiveWidth; i<=receptiveWidth; i++) {
			for (int j=-receptiveHeight; j<=receptiveWidth; j++) {
				//System.out.println("index " + index);
				input[index] = getReceptiveFieldCellValue(marioCenter[0]+i, marioCenter[1]+j);
				index++;
			}
		}
		
		// Enemies info
		for (int i=-receptiveWidth; i<=receptiveWidth; i++) {
			for (int j=-receptiveHeight; j<=receptiveWidth; j++) {
				//System.out.println("index " + index);
				input[index] = getReceptiveEnemyCellValue(marioCenter[0]+i, marioCenter[1]+j);
				index++;
			}
		}
		
		// Mario state
		for (int i=0; i<stateAmount; i++) {
			//System.out.println("index " + index);
			input[index] = getMarioStateValue(i);
			index++;
		}
		
		// Last buttons 
		for (int i=0; i<action.length; i++) {
			//System.out.println("index " + index);
			if (action[i]) {
				input[index] = 1;
			}
			else {
				input[index] = 0;
			}
			index++;
		}
	}
}
