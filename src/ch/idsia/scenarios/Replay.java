package ch.idsia.scenarios;

import ch.idsia.benchmark.tasks.ReplayTask;
import ch.idsia.tools.CmdLineOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Oct 9, 2010
 * Time: 8:00:13 PM
 * Package: ch.idsia.scenarios
 */
public class Replay
{
public static void main(String[] args)
{
    final CmdLineOptions cmdLineOptions = new CmdLineOptions(args);
    final ReplayTask replayTask = new ReplayTask(cmdLineOptions);
    replayTask.reset(cmdLineOptions);
    replayTask.startReplay();
    System.out.println(replayTask.getEnvironment().getEvaluationInfoAsString());

    System.exit(0);
}
}
