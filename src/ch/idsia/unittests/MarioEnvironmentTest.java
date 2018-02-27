package ch.idsia.unittests;

import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.ReplayTask;
import ch.idsia.tools.CmdLineOptions;
import junit.framework.TestCase;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Aug 28, 2010
 * Time: 8:43:51 PM
 * Package: ch.idsia.unittests
 */
public class MarioEnvironmentTest extends TestCase
{
private boolean INCLUDE_VISUAL_TESTS = true;

@BeforeTest
public void setUp()
{
}

@AfterTest
public void tearDown()
{
}

@Test
public void testGetInstance() throws Exception
{
    assertNotNull(MarioEnvironment.getInstance());
}

@Test
public void testResetDefault() throws Exception
{
}

@Test
public void testReset() throws Exception
{
}

@Test
public void testTick() throws Exception
{
}

@Test
public void testGetMarioFloatPos() throws Exception
{
}

@Test
public void testGetMarioMode() throws Exception
{
}

@Test
public void testGetEnemiesFloatPos() throws Exception
{
}

@Test
public void testIsMarioOnGround() throws Exception
{
}

@Test
public void testIsMarioAbleToJump() throws Exception
{
}

@Test
public void testIsMarioCarrying() throws Exception
{
}

@Test
public void testIsMarioAbleToShoot() throws Exception
{
}

@Test
public void testGetObservationWidth() throws Exception
{
}

@Test
public void testGetObservationHeight() throws Exception
{
}

@Test
public void testGetMergedObservationZZ() throws Exception
{
}

@Test
public void testGetLevelSceneObservationZ() throws Exception
{
}

@Test
public void testGetEnemiesObservationZ() throws Exception
{
}

@Test
public void testGetKillsTotal() throws Exception
{
}

@Test
public void testGetKillsByFire() throws Exception
{
}

@Test
public void testGetKillsByStomp() throws Exception
{
}

@Test
public void testGetKillsByShell() throws Exception
{
}

@Test
public void testGetMarioStatus() throws Exception
{
}

@Test
public void testGetSerializedFullObservationZZ() throws Exception
{
}

@Test
public void testGetSerializedLevelSceneObservationZ() throws Exception
{
}

@Test
public void testGetSerializedEnemiesObservationZ() throws Exception
{
}

@Test
public void testGetSerializedMergedObservationZZ() throws Exception
{
}

@Test
public void testGetCreaturesFloatPos() throws Exception
{
}

@Test
public void testGetMarioState() throws Exception
{
}

@Test
public void testPerformAction() throws Exception
{
}

@Test
public void testIsLevelFinished() throws Exception
{
}

@Test
public void testGetEvaluationInfoAsFloats() throws Exception
{
}

@Test
public void testGetEvaluationInfoAsString() throws Exception
{
}

@Test
public void testGetEvaluationInfo() throws Exception
{
}

@Test
public void testSetAgent() throws Exception
{
}

@Test
public void testGetIntermediateReward() throws Exception
{

}

@Test
public void testMarioCenterPos() throws Exception
{
    CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -rfw 5 -rfh 7");
    MarioEnvironment env = MarioEnvironment.getInstance();

    assertNotNull(env);

    env.reset(cmdLineOptions);

    int[] pos = env.getMarioReceptiveFieldCenter();
    assertEquals(2, pos[0]);
    assertEquals(3, pos[1]);
}

@Test
public void testMarioReceptiveFieldSizeW5H7_vis() throws Exception
{
    if (INCLUDE_VISUAL_TESTS)
    {
        final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis on -rfw 5 -rfh 7 -le 0 -srf on -gv on");
        final BasicTask basicTask = new BasicTask(cmdLineOptions);
        basicTask.reset(cmdLineOptions);
        basicTask.runOneEpisode();
        assertEquals(cmdLineOptions.getReceptiveFieldHeight().intValue(), 7);
        assertEquals(cmdLineOptions.getReceptiveFieldWidth().intValue(), 5);
    }
}

/**
 * @throws Exception
 */
@Test
public void testMarioReceptiveFieldSizeW8H6_vis() throws Exception
{
    if (INCLUDE_VISUAL_TESTS)
    {
        final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis on -rfw 8 -rfh 6 -le 0 -srf on");
        final BasicTask basicTask = new BasicTask(cmdLineOptions);
        basicTask.reset(cmdLineOptions);
        basicTask.runOneEpisode();
        assertEquals(7, cmdLineOptions.getReceptiveFieldHeight().intValue());
        assertEquals(9, cmdLineOptions.getReceptiveFieldWidth().intValue());
        int[] pos = basicTask.getEnvironment().getMarioReceptiveFieldCenter();
        assertEquals(4, pos[0]);
        assertEquals(3, pos[1]);

    }
}

@Test
public void testRecordingFitness()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -ag ch.idsia.agents.controllers.ForwardJumpingAgent -rec recorderTest.zip");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    float originalFitness = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness();
    System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());

    final CmdLineOptions options = new CmdLineOptions("-rep recorderTest.zip");
    final ReplayTask replayTask = new ReplayTask(options);
    replayTask.reset(options);
    replayTask.startReplay();
    System.out.println(replayTask.getEnvironment().getEvaluationInfoAsString());
    float replayFitness = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness();
    assertEquals(originalFitness, replayFitness);
}

@Test
public void testRecordingTrace()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -ag ch.idsia.agents.controllers.ForwardJumpingAgent -rec recorderTest.zip");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();

    float originalFitness = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness();
    int[][] firstTrace = basicTask.getEnvironment().getEvaluationInfo().marioTrace;
    System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());

    final CmdLineOptions options = new CmdLineOptions("-rep recorderTest.zip");
    final ReplayTask replayTask = new ReplayTask(options);
    replayTask.reset(options);
    replayTask.startReplay();

    float replayFitness = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness();
    int[][] secondTrace = basicTask.getEnvironment().getEvaluationInfo().marioTrace;
    System.out.println(replayTask.getEnvironment().getEvaluationInfoAsString());
    
    for (int j = 0; j < firstTrace[0].length; ++j)
        for (int i = 0; i < firstTrace.length; ++i)
            assertEquals(firstTrace[i][j], secondTrace[i][j]);
}
}