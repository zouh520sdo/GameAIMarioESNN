package ch.idsia.benchmark.tasks;

import ch.idsia.agents.Agent;
import ch.idsia.tools.CmdLineOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey at idsia dot ch
 * Date: Apr 4, 2010 Time: 11:33:15 AM
 * Package: ch.idsia.maibe.tasks
 */
public class MushroomTask extends BasicTask implements Task
{
private MarioCustomSystemOfValues sov = new MarioCustomSystemOfValues();

public MushroomTask(CmdLineOptions cmdLineOptions)
{
    super(cmdLineOptions);
    this.options = cmdLineOptions;
}

public float[] evaluate(Agent controller)
{
    float fitness = 0;
    controller.reset();
//        options.setLevelRandSeed(startingSeed++);
//        System.out.println("controller = " + controller);
    options.setAgent(controller);
//        this.setAgent(controller);
    this.reset(options);
    this.runOneEpisode();
    fitness += this.getEnvironment().getEvaluationInfo().computeWeightedFitness(sov);
    return new float[]{fitness};
}

}
