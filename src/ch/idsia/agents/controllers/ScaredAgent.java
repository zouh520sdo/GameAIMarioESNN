package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
//import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: May 9, 2009
 * Time: 9:46:59 AM
 * Package: ch.idsia.controllers.agents
 */
public class ScaredAgent extends BasicMarioAIAgent implements Agent
{
public ScaredAgent()
{
    super("ScaredAgent");
}

int trueJumpCounter = 0;
//    int trueSpeedCounter = 0;

public boolean[] getAction()
{
    if (/*levelScene[11][13] != 0 ||*/ levelScene[this.receptiveFieldWidth / 2][this.receptiveFieldHeight / 2 + 1] != 0 ||
            /* levelScene[12][13] == 0 ||*/ levelScene[this.receptiveFieldWidth / 2 / 2 + 1][this.receptiveFieldHeight / 2 + 1] == 0)
    {
        if (isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP]))
        {
            action[Mario.KEY_JUMP] = true;
        }
        ++trueJumpCounter;
    } else
    {
        action[Mario.KEY_JUMP] = false;
        trueJumpCounter = 0;
    }

    if (trueJumpCounter > 46)
    {
        trueJumpCounter = 0;
        action[Mario.KEY_JUMP] = false;
    }

    return action;  //To change body of implemented methods use File | Settings | File Templates.
}

public void reset()
{
    action[Mario.KEY_RIGHT] = true;
    action[Mario.KEY_SPEED] = false;
}
}
