package gameai.mariocontroller;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import gameai.neuralnetwork.NeuralNetwork;
import java.util.Random;

public class MarioESNNController extends BasicMarioAIAgent implements Agent {

	public NeuralNetwork NN;
	public int NNInputLength;
	public double sigma;
	public int receptiveWidth = 2;
	public int receptiveHeight = 2;
	public int stateAmount = 6; // < 12
	protected double[] input;
	
	int lastX = 0;
	int sameXCounter = 0;
	int jumpCounter = 0;
	
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
	    NNInputLength = //(2 * receptiveWidth + 1) * (2 * receptiveHeight + 1) +
	    		//(2 * receptiveWidth + 1) * (2 * receptiveHeight + 1) +
				stateAmount + Environment.numberOfButtons + 6;
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
	    NNInputLength = //(2 * receptiveWidth + 1) * (2 * receptiveHeight + 1) +
	    		//(2 * receptiveWidth + 1) * (2 * receptiveHeight + 1) +
				stateAmount + Environment.numberOfButtons + 6;
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
		//airtime
		//if(!this.isMarioOnGround)
		//	airTime++;
		// stuck solution
	    if((int)marioFloatPos[0] == lastX) {
	    	sameXCounter ++;
	    }else {
	    	sameXCounter = 0;
	    }
	    
	    if(action[Mario.KEY_JUMP] && !this.isMarioOnGround)
	    	jumpCounter++;
	    else
	    	jumpCounter = 0;
	    
		updateInput();
		action = NN.getButtons(input);
		lastX = (int)marioFloatPos[0];
		action[Mario.KEY_DOWN] = false;
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
		/**
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
		**/
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
		
		input[index] = this.WallAhead();
		index++;
		input[index] = this.EnemyAhead();
		index++;
		input[index] = this.Stuck();
		index++;
		input[index] = this.jumpTooLong();
		index++;
		input[index] = this.GapAhead();
		index++;
		input[index] = this.EnemyHeight();
		index++;
	}
	
	
	
	private boolean FacingWall(byte[][] levelScene) {
		boolean result = getReceptiveFieldCellValue(marioCenter[0], marioCenter[1]+1) != 0  
				|| getReceptiveFieldCellValue(marioCenter[0]-1, marioCenter[1]+1) != 0;
		//result = result || (getReceptiveFieldCellValue(marioCenter[0]+1, marioCenter[1]+1) != 0 && isMarioOnGround);
		return result;
		
	}
	
	private int[] ClosestEnemyR(byte[][] enemies) {
		int result[] = {-1, -1, -1, -1};
		for (int x = 0; x < 4; x++) {
			result[2]= 0;
			for (int y = 4; y > -4; y--) {// -4 is the higher y
				if(enemies[marioCenter[0]+y][marioCenter[1]+x] > 0) {
					result[0] = x;
					result[1] = y;
					result[2] += 1;
					result[3] = enemies[marioCenter[0]+y][marioCenter[1]+x];
				}
			}
			if(result[0] == x)
				break;
		}
		
		return result;
	}
	
	private int EnemyAhead() {
		if(ClosestEnemyR(enemies)[0] > 0 && ClosestEnemyR(enemies)[0]<2)
			return 1;
		else return 0;
	}
	
	private int EnemyHeight() {
		return ClosestEnemyR(enemies)[1];
	}
	
	private int WallAhead() {
		if(FacingWall(levelScene))
			return 1;
		else return 0;
	}
	
	private int Stuck() {
	    if(sameXCounter > 15) {
	    	return 1;
	    }
	    else return 0;
	}
	
	public boolean getOnGround() {
		return this.isMarioOnGround;
	}
	
	public boolean FacingWallNotJump() {
		if(WallAhead()==1 && this.getOnGround()) {
			return true;
		}
		return false;
	}
	
	public int jumpTooLong() {
		if(jumpCounter > 10)
			return 1;
		else 
			return 0;
	}
	
	public int getJumpCounter() {
		return this.jumpCounter;
	}
	
	public int GapAhead() {
		//System.out.println("here " + getReceptiveFieldCellValue(levelScene.length-5, marioCenter[1]+1));
		for(int y = 1; y < levelScene.length; y++) {
			if(getReceptiveFieldCellValue(marioCenter[0]+y, marioCenter[1]+1) != 0)
				return 0;
		}
		return 1;
		
	}
}
