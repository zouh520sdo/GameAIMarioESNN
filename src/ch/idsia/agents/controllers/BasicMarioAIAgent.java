package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:30:41 AM
 * Package: ch.idsia.agents.controllers;
 */
public class BasicMarioAIAgent implements Agent
{
protected boolean action[] = new boolean[Environment.numberOfButtons];
protected String name = "Instance_of_BasicAIAgent._Change_this_name";

/*final*/
protected byte[][] levelScene;
/*final */
protected byte[][] enemies;
protected byte[][] mergedObservation;

protected float[] marioFloatPos = null;
protected float[] enemiesFloatPos = null;

protected int[] marioCenter = null;

protected int[] marioState = null;

protected int marioStatus;
protected int marioMode;
protected boolean isMarioOnGround;
protected boolean isMarioAbleToJump;
protected boolean isMarioAbleToShoot;
protected boolean isMarioCarrying;
protected int getKillsTotal;
protected int getKillsByFire;
protected int getKillsByStomp;
protected int getKillsByShell;

protected int receptiveFieldWidth;
protected int receptiveFieldHeight;

// values of these variables could be changed during the Agent-Environment interaction.
// Use them to get more detailed or less detailed description of the level.
// for information see documentation for the benchmark <link: marioai.org/marioaibenchmark/zLevels
int zLevelScene = 1;
int zLevelEnemies = 0;


public BasicMarioAIAgent(String s)
{
    setName(s);
}


public boolean[] getAction()
{
    return new boolean[Environment.numberOfButtons];

}

public void integrateObservation(Environment environment)
{
    levelScene = environment.getLevelSceneObservationZ(zLevelScene);
    enemies = environment.getEnemiesObservationZ(zLevelEnemies);
    mergedObservation = environment.getMergedObservationZZ(1, 0);

    this.marioFloatPos = environment.getMarioFloatPos();
    this.enemiesFloatPos = environment.getEnemiesFloatPos();
    this.marioState = environment.getMarioState();

    this.marioCenter = environment.getMarioReceptiveFieldCenter();
    receptiveFieldWidth = environment.getReceptiveFieldWidth();
    receptiveFieldHeight = environment.getReceptiveFieldHeight();

    // It also possible to use direct methods from Environment interface.
    //
    marioStatus = marioState[0];
    marioMode = marioState[1];
    isMarioOnGround = marioState[2] == 1;
    isMarioAbleToJump = marioState[3] == 1;
    isMarioAbleToShoot = marioState[4] == 1;
    isMarioCarrying = marioState[5] == 1;
    getKillsTotal = marioState[6];
    getKillsByFire = marioState[7];
    getKillsByStomp = marioState[8];
    getKillsByShell = marioState[9];
}

public void giveIntermediateReward(float intermediateReward)
{

}

public void reset()
{
    action = new boolean[Environment.numberOfButtons];// Empty action
}

@Deprecated
public boolean[] getAction(Environment observation)
{
    return new boolean[Environment.numberOfButtons]; // Empty action
}

public String getName() { return name; }

public void setName(String Name) { this.name = Name; }

public int getReceptiveFieldCellValue(int x, int y)
{
    if (x < 0 || x >= levelScene.length || y < 0 || y >= levelScene[0].length)
        return 0;

    return levelScene[x][y];
}

//    public void integrateObservation(int[] serializedLevelSceneObservationZ, int[] serializedEnemiesObservationZ, float[] marioFloatPos, float[] enemiesFloatPos, int[] marioState)
//    {
//        int k = 0;
//        for (int i = 0; i < levelScene.length; ++i)
//        {
//            for (int j = 0; j < levelScene[0].length; ++j)
//            {
//                levelScene[i][j] = (byte)serializedLevelSceneObservationZ[k];
//                enemies[i][j] = (byte)serializedEnemiesObservationZ[k++];
//                mergedObservation[i][j] = levelScene[i][j];
//                // Simulating merged observation!
//                if (enemies[i][j] != 0)
//                {
//                    mergedObservation[i][j] = enemies[i][j];
//                }
////                System.out.print(observation[i][j] + "\t");
//            }
////            System.out.println();
//        }
//        this.marioFloatPos = marioFloatPos;
//        this.enemiesFloatPos = enemiesFloatPos;
//        this.marioState = marioState;
//
//        marioStatus = marioState[0];
//        marioMode = marioState[1];
//        isMarioOnGround = marioState[2] == 1 ;
//        isMarioAbleToJump = marioState[3] == 1;
//        isMarioAbleToShoot = marioState[4] == 1;
//        isMarioCarrying = marioState[5] == 1;
//        getKillsTotal = marioState[6];
//        getKillsByFire = marioState[7];
//        getKillsByStomp = marioState[8];
//        getKillsByShell = marioState[9];
//    }
}
