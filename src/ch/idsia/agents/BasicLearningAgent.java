package ch.idsia.agents;

import ch.idsia.agents.learning.SimpleMLPAgent;
import ch.idsia.agents.learning.SmallMLPAgent;
import ch.idsia.benchmark.tasks.ProgressTask;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Jun 9, 2010
 * Time: 7:03:17 PM
 * Package: ch.idsia.agents
 */
public class BasicLearningAgent extends SimpleMLPAgent implements LearningAgent
{
    Agent agent;
    Agent finalAgent;

    public BasicLearningAgent(Agent agent)
    {
        this.agent = agent;
    }

    public BasicLearningAgent()
    {
        this.agent = new SmallMLPAgent();
    }

    public void learn()
    {

    }

    public void giveReward(float r)
    {

    }

    public void newEpisode()
    {

    }

    public Agent getBestAgent()
    {
        return null;
    }

    public void setNumberOfTrials(int num)
    {

    }

    public void setTask(ProgressTask task)
    {

    }

    public void init()
    {

    }
}
