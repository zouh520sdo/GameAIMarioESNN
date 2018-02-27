package ch.idsia.benchmark.tasks;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.ReplayAgent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.tools.CmdLineOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Oct 9, 2010
 * Time: 7:17:49 PM
 * Package: ch.idsia.benchmark.tasks
 */
public class ReplayTask implements Task
{
protected final static Environment environment = MarioEnvironment.getInstance();
private Agent agent;
protected CmdLineOptions options;
private String name = getClass().getSimpleName();

public ReplayTask(CmdLineOptions cmdLineOptions)
{
    setOptions(cmdLineOptions);
}

public boolean startReplay()
{
    while (!environment.isLevelFinished())
    {
        environment.tick();
        if (!GlobalOptions.isGameplayStopped)
        {
            boolean[] action = agent.getAction();
            environment.performAction(action);
        }
    }
    ((ReplayAgent) agent).closeReplayer();
    environment.getEvaluationInfo().setTaskName(name);
    return true;
}

public float[] evaluate(final Agent controller)
{
    return new float[0];
}

public void setOptions(final CmdLineOptions options)
{
    this.options = options;
}

public CmdLineOptions getOptions()
{
    return null;
}

public void doEpisodes(final int amount, final boolean verbose)
{}

public boolean isFinished()
{
    return false;
}

public void reset(CmdLineOptions cmdLineOptions)
{
    options = cmdLineOptions;

    String replayFileName = options.getReplayFileName();

    // TODO: possible to remove it?
    if (!replayFileName.equals(""))
        cmdLineOptions.setParameterValue("-ag", "ch.idsia.agents.controllers.ReplayAgent"); //todo:fix it

    environment.reset(cmdLineOptions);
    agent = options.getAgent();

    if (!replayFileName.equals(""))
        ((ReplayAgent) agent).setRepFile(replayFileName);

    agent.reset();
}

public void reset()
{}

public String getName()
{
    return name;
}

public Environment getEnvironment()
{
    return environment;
}
}
