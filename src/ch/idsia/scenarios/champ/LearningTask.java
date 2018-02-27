package ch.idsia.scenarios.champ;

import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.Task;
import ch.idsia.tools.CmdLineOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Jul 8, 2010
 * Time: 5:39:55 PM
 * Package: ch.idsia.scenarios.champ
 */
public class LearningTask extends BasicTask implements Task
{
    public LearningTask(CmdLineOptions cmdLineOptions)
    {
        super(cmdLineOptions);
    }

    public void reset(CmdLineOptions cmdLineOptions)
    {
        options = cmdLineOptions;
        environment.reset(cmdLineOptions);

    }

}
