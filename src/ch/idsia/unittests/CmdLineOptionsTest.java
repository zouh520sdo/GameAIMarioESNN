package ch.idsia.unittests;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.utils.ParameterContainer;
import junit.framework.TestCase;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Aug 28, 2010
 * Time: 8:36:02 PM
 * Package: ch.idsia.unittests
 */

public class CmdLineOptionsTest extends TestCase
{
CmdLineOptions cmdLineOptions;

@BeforeTest
public void setUp()
{
    cmdLineOptions = new CmdLineOptions();
}

@AfterTest
public void tearDown()
{
}

@Test
public void testTotalNumberOfOptions() throws Exception
{
    assertEquals(48, cmdLineOptions.getTotalNumberOfOptions());
}

@Test
public void testAllOptionsHaveDefaults()
{
    assertEquals(ParameterContainer.getTotalNumberOfOptions(), ParameterContainer.getNumberOfAllowedOptions());
}

@Test
public void testSetArgs() throws Exception
{
    String args = "-ag ch.idsia.agents.controllers.human.HumanKeyboardAgent" +
            " -amico off" +
            " -echo off" +
            " -ewf on" +
            " -grc 1.0" +
            " -grm 1.0" +
            " -gv off" +
            " -gvc off" +
            " -i off" +
            " -ld 0" +
            " -ll 320" +
            " -ls 0" +
            " -lt 0" +
            " -fps 24" +
            " -mm 2" +
            " -pw off" +
            " -pr off" +
            " -rfh 19" +
            " -rfw 19" +
            " -srf off" +
            " -tl 200" +
            " -tc off" +
            " -vaot off" +
            " -vlx 0" +
            " -vly 0" +
            " -vis on" +
            " -vw 320" +
            " -vh 240" +
            " -zs 1" +
            " -ze 0" +
            " -lh 15" +
            " -lde off" +
            " -lc on" +
            " -lhs on" +
            " -ltb on" +
            " -lco on" +
            " -lb on" +
            " -lg on" +
            " -lhb off" +
            " -le 1111111111" +
            " -lf off";
    cmdLineOptions.setArgs(args);
    // TODO: test all occurences
}

@Test
public void testSetLevelEnemies()
{
    cmdLineOptions.setArgs("-le 1111111111");
    // TODO: test various conditions
}

@Test
public void testSetMarioInvulnerable() throws Exception
{
    cmdLineOptions.setMarioInvulnerable(true);
    assertEquals(cmdLineOptions.isMarioInvulnerable(), true);
    cmdLineOptions.setArgs("-i off");
    assertEquals(cmdLineOptions.isMarioInvulnerable(), false);
}

@Test
public void testDefaultAgent()
{
    assertNotNull(cmdLineOptions.getAgent());
    assertEquals("ch.idsia.agents.controllers.human.HumanKeyboardAgent", cmdLineOptions.getAgentFullLoadName());
    assertEquals("HumanKeyboardAgent", cmdLineOptions.getAgent().getName());
}

@Test
public void testStop()
{
    this.cmdLineOptions.setArgs("-stop on");
    assertEquals(true, this.cmdLineOptions.isStopGamePlay().booleanValue());
    assertEquals(GlobalOptions.isGameplayStopped, this.cmdLineOptions.isStopGamePlay().booleanValue());
}


}
