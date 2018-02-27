package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Aug 10, 2009
 * Time: 6:41:42 PM
 */

public class TimingAgent extends BasicMarioAIAgent implements Agent
{
    private Agent agent;
    private long timeTaken = 0;
    private int actionsPerformed = 0;

    public TimingAgent(Agent agent)
    {
        super("TimingAgent");
        this.agent = agent;
    }

    public boolean[] getAction()
    {
        long start = System.currentTimeMillis();
        boolean[] action = agent.getAction();
        timeTaken += (System.currentTimeMillis() - start);
        actionsPerformed++;
        return action;
    }

    public void reset()
    {
        agent.reset();
    }

    public String getName()
    {
        return agent.getName();
    }

    public void setName(String name)
    {
        agent.setName(name);
    }

    public double averageTimeTaken()
    {
        double average = ((double) timeTaken) / actionsPerformed;
        timeTaken = 0;
        actionsPerformed = 0;
        return average;
    }
}
