package ch.idsia.unittests;

import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.CmdLineOptions;
import junit.framework.TestCase;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Aug 28, 2010
 * Time: 8:39:30 PM
 * Package: ch.idsia.unittests
 */
public final class MarioAIBenchmarkTest extends TestCase
{
@BeforeTest
public void setUp()
{
}

@AfterTest
public void tearDown()
{
}

@Test
public void testRandomPersistence()
{
    Random r1 = new Random(42);
    int seq[] = new int[]{2, 0, 2, 0, 1, 3, 1, 2, 2, 0, 3, 1, 1, 1, 1, 2, 1, 3, 3, 3, 3, 0, 1, 1, 2, 3, 1, 3, 0, 0, 2, 1, 0, 1, 3, 1, 0, 0, 2, 1, 3, 2};
    for (int i = 0; i < 42; ++i)
    {
        assertEquals(r1.nextInt(4), seq[i]);
    }
}


@Test
public void testAgentLoadAndGetAgentName()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -lc 0 -ag ch.idsia.agents.controllers.ForwardJumpingAgent");
    assertNotNull(cmdLineOptions.getAgent());
    assertEquals(cmdLineOptions.getAgent().getName(), "ForwardJumpingAgent");
    assertEquals(cmdLineOptions.getAgentFullLoadName(), "ch.idsia.agents.controllers.ForwardJumpingAgent");
}

@Test
public void testAgentLoadHumanKeyboardAgent()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -lc 0");
    assertNotNull(cmdLineOptions.getAgent());
    assertEquals(cmdLineOptions.getAgent().getName(), "HumanKeyboardAgent");
    assertEquals(cmdLineOptions.getAgentFullLoadName(), "ch.idsia.agents.controllers.human.HumanKeyboardAgent");
}


@Test
public void testForwardJumpingAgentFitnessWithoutCreatures()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -le 0 -ag ch.idsia.agents.controllers.ForwardJumpingAgent -echo on");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
    assertEquals("Fitness", 5227.0, basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(), 0.1);
}

@Test
public void testForwardJumpingAgentFitnessWithDefaultCreatures()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -ag ch.idsia.agents.controllers.ForwardJumpingAgent -echo on");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
    assertEquals("Fitness", 5873.3193359375, basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(), 0.1);
}

@Test
public void testReceptiveField_1x2()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis on -fps 75 -ag ch.idsia.agents.controllers.ForwardAgent -echo on -rfw 1 -rfh 2 -srf on");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
    assertEquals(6852.63671875, basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(), 0.1);
}

@Test
public void testReceptiveField_3x1()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -ag ch.idsia.agents.controllers.ForwardAgent -echo on -rfw 3 -rfh 1 -srf on");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
    assertEquals(6852.63671875, basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(), 0.1);
}

@Test
public void testReceptiveField_1x1()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -ag ch.idsia.agents.controllers.ForwardAgent -echo on -rfw 1 -rfh 1 -srf on");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
    assertEquals(6852.63671875, basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(), 0.1);
}

@Test
public void testForwardAgentFitnessWithDefaultCreatures()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -ag ch.idsia.agents.controllers.ForwardAgent -echo on");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
    assertEquals(7162.177734375, basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(), 0.1);
}

@Test
public void testForwardAgentFitnessWithDefaultCreaturesVisual()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis on -fps 100 -ag ch.idsia.agents.controllers.ForwardAgent -echo on");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
    assertEquals(7162.177734375, basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(), 0.1);
}

@Test
public void testForwardAgentCoinsCollected()
{
    //TODo: testForwardAgentCoinsCollected
}

@Test
public void testForwardAgentFitnessWithoutCreatures()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -le 0 -ag ch.idsia.agents.controllers.ForwardAgent -echo on");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    System.out.println(basicTask.getEnvironment().getEvaluationInfoAsString());
    assertEquals("Fitness", 5435.0, basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(), 0.1);
}

@Test
public void testForwardJumpingAgentCoinsCollected()
{
    //TODo: testForwardJumpingAgentCoinsCollected
}

/**
 * In this test benchmark in launched and stopped. Press "SPACE" key to resume the game.
 * You can press "SPACE" to stop and resume the gameplay.
 */
@Test
public void testStopGameplay()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis on -rfw 9 -rfh 5 -stop on -ll 100 -ag ch.idsia.agents.controllers.ForwardAgent -echo on");
    assertEquals(true, cmdLineOptions.isStopGamePlay().booleanValue());
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
}

@Test
public void testScaredShooty_G10()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -rfw 5 -rfh 5 -ag ch.idsia.agents.controllers.ScaredShooty -lf on -ltb off -lg off -lb off -i on -le g:10");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    assertEquals(10, basicTask.getEnvironment().getEvaluationInfo().killsByFire);
}

@Test
public void testScaredShooty_G10RK5()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -rfw 5 -rfh 5 -ag ch.idsia.agents.controllers.ScaredShooty -lf on -ltb off -lg off -lb off -i on -le g:10,rk:5");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    assertEquals(13, basicTask.getEnvironment().getEvaluationInfo().killsByFire);
}

@Test
public void testScaredShooty_GW10G10()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -rfw 5 -rfh 5 -ag ch.idsia.agents.controllers.ScaredShooty -lf on -ltb off -lg off -lb off -i on -le g:10,gw:10");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    assertEquals(9, basicTask.getEnvironment().getEvaluationInfo().killsByFire);
}

@Test
public void testScaredShooty_G6S3()
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions("-vis off -rfw 5 -rfh 5 -ag ch.idsia.agents.controllers.ScaredShooty -lf on -ltb off -lg off -lb off -i on -le g:6,s:3");
    final BasicTask basicTask = new BasicTask(cmdLineOptions);
    basicTask.reset(cmdLineOptions);
    basicTask.runOneEpisode();
    assertEquals(6, basicTask.getEnvironment().getEvaluationInfo().killsByFire);
}

}