package ch.idsia.benchmark.mario.engine.level;

import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Enemy;
import ch.idsia.benchmark.mario.engine.sprites.FlowerEnemy;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;

import java.io.Serializable;

public class SpriteTemplate implements Serializable
{
public int lastVisibleTick = -1;
public Sprite sprite;
public boolean isDead = false;
private boolean winged;

public int getType()
{
    return type;
}

private int type;

public SpriteTemplate(int type)
{
    this.type = type;
    switch (type)
    {
        case Sprite.KIND_GOOMBA:
            this.winged = false;
            break;
        case Sprite.KIND_GREEN_KOOPA:
            this.winged = false;
            break;
        case Sprite.KIND_RED_KOOPA:
            this.winged = false;
            break;
        case Sprite.KIND_SPIKY:
            this.winged = false;
            break;
        case Sprite.KIND_GOOMBA_WINGED:
            this.winged = true;
            break;
        case Sprite.KIND_GREEN_KOOPA_WINGED:
            this.winged = true;
            break;
        case Sprite.KIND_RED_KOOPA_WINGED:
            this.winged = true;
            break;
        case Sprite.KIND_SPIKY_WINGED:
            this.winged = true;
            break;
        case Sprite.KIND_ENEMY_FLOWER:
            this.winged = false;
            break;
        case Sprite.KIND_BULLET_BILL:
            this.winged = false;
            break;
    }
}

public void spawn(LevelScene world, int x, int y, int dir)
{
    if (isDead) return;

    if (type == Sprite.KIND_ENEMY_FLOWER)
    {
        sprite = new FlowerEnemy(world, x * 16 + 15, y * 16 + 24, x, y);
    } else
    {
//            sprite = new Enemy(levelScene, x*16+8, y*16+15, dir, type, winged);
        sprite = new Enemy(world, x * 16 + 8, y * 16 + 15, dir, type, winged, x, y);
    }
    sprite.spriteTemplate = this;
    world.addSprite(sprite);
}
}