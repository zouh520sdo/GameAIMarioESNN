package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;
//import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey_at_idsia_dot_ch
 * Date: May 9, 2009
 * Time: 1:42:03 PM
 * Package: ch.idsia.agents.controllers
 */

public class ScaredShooty extends BasicMarioAIAgent implements Agent
{
public ScaredShooty()
{
    super("ScaredShooty");
}

int trueJumpCounter = 0;
int trueSpeedCounter = 0;

private boolean isCreature(int c)
{
    switch (c)
    {
        case Sprite.KIND_GOOMBA:
        case Sprite.KIND_RED_KOOPA:
        case Sprite.KIND_RED_KOOPA_WINGED:
        case Sprite.KIND_GREEN_KOOPA_WINGED:
        case Sprite.KIND_GREEN_KOOPA:
            return true;
    }
    return false;
}

public boolean[] getAction()
{
    int x = marioCenter[0];
    int y = marioCenter[1];

    action[Mario.KEY_SPEED] = isCreature(enemies[x][y + 2]) || isCreature(enemies[x][y + 1]);

    return action;
}

public void reset()
{
    action[Mario.KEY_RIGHT] = true;
//    action[Mario.KEY_SPEED] = true;
}
}
