package ch.idsia.agents;

import ch.idsia.agents.learning.MediumSRNAgent;
import ch.idsia.benchmark.tasks.ProgressTask;
import ch.idsia.evolution.ea.ES;

/**
 * Created by IntelliJ IDEA.
 * User: odin
 * Date: Jul 27, 2010
 * Time: 9:40:09 PM
 */


public class SRNESLearningAgent extends MediumSRNAgent implements LearningAgent
{
    private MediumSRNAgent agent;
    Agent bestAgent;
    private static float bestScore = 0;
    private ProgressTask task;
    ES es;
    int populationSize = 100;
    int generations = 5000;
    int numberOfTrials; //common number of trials
    int exhausted; // number of exhausted trials


    public void init()
    {
        es = new ES(task, agent, populationSize);
    }

    public SRNESLearningAgent(MediumSRNAgent agent)
    {
        this.agent = agent;
    }

    public SRNESLearningAgent()
    {
        this.agent = new MediumSRNAgent();
    }

    public void learn()
    {
        this.exhausted++;

        int locBestScore = 0; // local best score

        for (int gen = 0; gen < generations; gen++)
        {
            System.out.println(gen + " generation");
            es.nextGeneration();
            float fitn = es.getBestFitnesses()[0];

            if (fitn > bestScore)
            {
                bestScore = fitn;
                bestAgent = (Agent) es.getBests()[0];
            }
        }
    }

    public void giveReward(float r)
    {
    }

    public void newEpisode()
    {
        task = null;
        agent.reset();
    }

    public void setTask(ProgressTask task)
    {
        this.task = task;
    }

    public void setNumberOfTrials(int num)
    {
        this.numberOfTrials = num;
    }

    public Agent getBestAgent()
    {
        return bestAgent;
    }
}
