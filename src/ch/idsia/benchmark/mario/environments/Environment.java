package ch.idsia.benchmark.mario.environments;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationInfo;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 28, 2009
 * Time: 8:51:57 PM
 * Package: .Environments
 */

public interface Environment
{
public static final int numberOfButtons = 6;

public static final int MARIO_KEY_DOWN = Mario.KEY_DOWN;
public static final int MARIO_KEY_JUMP = Mario.KEY_JUMP;
public static final int MARIO_KEY_LEFT = Mario.KEY_LEFT;
public static final int MARIO_KEY_RIGHT = Mario.KEY_RIGHT;
public static final int MARIO_KEY_SPEED = Mario.KEY_SPEED;
public static final int MARIO_STATUS_WIN = Mario.STATUS_WIN;
public static final int MARIO_STATUS_DEAD = Mario.STATUS_DEAD;
public static final int MARIO_STATUS_RUNNING = Mario.STATUS_RUNNING;

// tunable dimensionality:
// default: 21x21 cells [0..20][0..20]
// always centered on the agent

// Chaning ZLevel during the game on-the-fly;
// if your agent recieves too ambiguous observation, it might request for more precise one for the next step

public void resetDefault();

public void reset(String setUpOptions);

public void tick();

public float[] getMarioFloatPos();

public int getMarioMode();

public float[] getEnemiesFloatPos();

public boolean isMarioOnGround();

public boolean isMarioAbleToJump();

public boolean isMarioCarrying();

public boolean isMarioAbleToShoot();

// OBSERVATION

public int getReceptiveFieldWidth();

public int getReceptiveFieldHeight();


public byte[][] getMergedObservationZZ(int ZLevelScene, int ZLevelEnemies);

public byte[][] getLevelSceneObservationZ(int ZLevelScene);

public byte[][] getEnemiesObservationZ(int ZLevelEnemies);

// OBSERVATION FOR AmiCo Agents

public float[] getSerializedFullObservationZZ(int ZLevelScene, int ZLevelEnemies);

/**
 * Serializes the LevelScene observation from 22x22 byte array to a 1x484 byte array
 *
 * @param ZLevelScene -- Zoom Level of the levelScene the caller expects to get
 * @return byte[] with sequenced elements of corresponding getLevelSceneObservationZ output
 */
public int[] getSerializedLevelSceneObservationZ(int ZLevelScene);

/**
 * Serializes the LevelScene observation from 22x22 byte array to a 1x484 byte array
 *
 * @param ZLevelEnemies -- Zoom Level of the enemies observation the caller expects to get
 * @return byte[] with sequenced elements of corresponding <code>getLevelSceneObservationZ</code> output
 */
public int[] getSerializedEnemiesObservationZ(int ZLevelEnemies);

public int[] getSerializedMergedObservationZZ(int ZLevelScene, int ZLevelEnemies);

public float[] getCreaturesFloatPos();

// KILLS

public int getKillsTotal();

public int getKillsByFire();

public int getKillsByStomp();

public int getKillsByShell();

int getMarioStatus();


/**
 * @return int array filled with data about Mario :
 *         marioState[0] = this.getMarioStatus();
 *         marioState[1] = this.getMarioMode();
 *         marioState[2] = this.isMarioOnGround() ? 1 : 0;
 *         marioState[3] = this.isMarioAbleToJump() ? 1 : 0;
 *         marioState[4] = this.isMarioAbleToShoot() ? 1 : 0;
 *         marioState[5] = this.isMarioCarrying() ? 1 : 0;
 *         marioState[6] = this.getKillsTotal();
 *         marioState[7] = this.getKillsByFire();
 *         marioState[8] = this.getKillsByStomp();
 *         marioState[9] = this.getKillsByStomp();
 *         marioState[10] = this.getKillsByShell();
 *         marioState[11] = this.getTimeLeft();
 */
public int[] getMarioState();

void performAction(boolean[] action);

boolean isLevelFinished();

float[] getEvaluationInfoAsFloats();

String getEvaluationInfoAsString();

EvaluationInfo getEvaluationInfo();

void reset(CmdLineOptions cmdLineOptions);

void setAgent(Agent agent);

public float getIntermediateReward();

/**
 * @return pos[0] = ReceptiveFieldWidth/2
 *         pos[1] = ReceptiveFieldHeight/2
 */
public int[] getMarioReceptiveFieldCenter();

public void closeRecorder();
    
// public int getLevelLength(); getLevelType();
}
