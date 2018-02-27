package ch.idsia.benchmark.mario.engine.sprites;

import ch.idsia.benchmark.mario.engine.Art;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.level.Level;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.CmdLineOptions;

public final class Mario extends Sprite
{
private final int FractionalPowerUpTime = 0;
public static final String[] MODES = new String[]{"small", "Large", "FIRE"};

//        fire = (mode == MODE.MODE_FIRE);
public static final int KEY_LEFT = 0;
public static final int KEY_RIGHT = 1;
public static final int KEY_DOWN = 2;
public static final int KEY_JUMP = 3;
public static final int KEY_SPEED = 4;
public static final int KEY_UP = 5;

public static final int STATUS_RUNNING = 2;
public static final int STATUS_WIN = 1;
public static final int STATUS_DEAD = 0;

private static float marioGravity;

public static boolean large = false;
public static boolean fire = false;
public static int coins = 0;
public static int hiddenBlocksFound = 0;
public static int collisionsWithCreatures = 0;
public static int mushroomsDevoured;
public static int flowersDevoured;

private static boolean isTrace;

private static boolean isMarioInvulnerable;

private int status = STATUS_RUNNING;
// for racoon when carrying the shell
private int prevWPic;
private int prevxPicO;
private int prevyPicO;
private int prevHPic;

private boolean isRacoon;
private float yaa = 1;

public static void resetStatic(CmdLineOptions cmdLineOptions)
{
    large = cmdLineOptions.getMarioMode() > 0;
    fire = cmdLineOptions.getMarioMode() == 2;
    coins = 0;
    hiddenBlocksFound = 0;
    mushroomsDevoured = 0;
    flowersDevoured = 0;

    isMarioInvulnerable = cmdLineOptions.isMarioInvulnerable();
    marioGravity = cmdLineOptions.getMarioGravity();

    isTrace = cmdLineOptions.isTrace();
}

public int getMode()
{
    return ((large) ? 1 : 0) + ((fire) ? 1 : 0);
}

//    private static float GROUND_INERTIA = 0.89f;
//    private static float AIR_INERTIA = 0.89f;

public boolean[] keys = new boolean[Environment.numberOfButtons];
public boolean[] cheatKeys;
private float runTime;
boolean wasOnGround = false;
boolean onGround = false;
private boolean mayJump = false;
private boolean ducking = false;
private boolean sliding = false;
private int jumpTime = 0;
private float xJumpSpeed;
private float yJumpSpeed;

private boolean canShoot = false;

int width = 4;
int height = 24;

public LevelScene levelScene;
public int facing;
private int powerUpTime = 0; // exclude pause for rendering changes

public int xDeathPos, yDeathPos;

public int deathTime = 0;
public int winTime = 0;
private int invulnerableTime = 0;

public Sprite carried = null;
//    private static Mario instance;

public Mario(LevelScene levelScene)
{
    kind = KIND_MARIO;
//        Mario.instance = this;
    this.levelScene = levelScene;
    x = levelScene.getMarioInitialPos().x;
    y = levelScene.getMarioInitialPos().y;
    mapX = (int) (x / 16);
    mapY = (int) (y / 16);

    facing = 1;
    setMode(Mario.large, Mario.fire);

    final float jumpPower = 7;
    // TODO: -jp <float>, default 7;
    yaa = marioGravity * 3;
    jT = jumpPower / (marioGravity);
}

private float jT;
private boolean lastLarge;
private boolean lastFire;
private boolean newLarge;
private boolean newFire;

private void blink(boolean on)
{
    Mario.large = on ? newLarge : lastLarge;
    Mario.fire = on ? newFire : lastFire;

//        System.out.println("on = " + on);
    if (large)
    {
        sheet = Art.mario;
        if (fire)
            sheet = Art.fireMario;

        xPicO = 16;
        yPicO = 31;
        wPic = hPic = 32;
    } else
    {
        sheet = Art.smallMario;

        xPicO = 8;
        yPicO = 15;
        wPic = hPic = 16;
    }
    savePrevState();
    calcPic();
}

void setMode(boolean large, boolean fire)
{
//        System.out.println("large = " + large);
    if (fire) large = true;
    if (!large) fire = false;

    lastLarge = Mario.large;
    lastFire = Mario.fire;

    Mario.large = large;
    Mario.fire = fire;

    newLarge = Mario.large;
    newFire = Mario.fire;

    blink(true);
}

public void setRacoon(boolean isRacoon)
{
//        if (true)
//        return;
    this.isRacoon = isRacoon;
//        this.setMode(isRacoon, false);
//        System.out.println("isRacoon = " + isRacoon);
//        System.out.println("Art.racoonmario.length = " + Art.racoonmario.length);
//        System.out.println("Art.racoonmario[0].length = " + Art.racoonmario[0].length);
    if (isRacoon)
    {
        savePrevState();

        xPicO = 16;
        yPicO = 31;
        wPic = hPic = 32;
        this.sheet = Art.racoonmario;
    } else
    {

        this.sheet = prevSheet;
        this.xPicO = this.prevxPicO;
        this.yPicO = this.prevyPicO;
        wPic = prevWPic;
        hPic = prevHPic;
//            blink(false);
    }
}

private void savePrevState()
{
    this.prevSheet = this.sheet;
    prevWPic = wPic;
    prevHPic = hPic;
    this.prevxPicO = xPicO;
    this.prevyPicO = yPicO;
}

public void move()
{
    if (GlobalOptions.isFly)
    {
        xa = ya = 0;
        ya = keys[KEY_DOWN] ? 10 : ya;
        ya = keys[KEY_UP] ? -10 : ya;
        xa = keys[KEY_RIGHT] ? 10 : xa;
        xa = keys[KEY_LEFT] ? -10 : xa;
    }

    if (mapY > -1 && isTrace)
        ++levelScene.level.marioTrace[this.mapX][this.mapY];

    if (winTime > 0)
    {
        winTime++;

        xa = 0;
        ya = 0;
        return;
    }

    if (deathTime > 0)
    {
        deathTime++;
        if (deathTime < 11)
        {
            xa = 0;
            ya = 0;
        } else if (deathTime == 11)
        {
            ya = -15;
        } else
        {
            ya += 2;
        }
        x += xa;
        y += ya;
        return;
    }

    if (powerUpTime != 0)
    {
        if (powerUpTime > 0)
        {
            powerUpTime--;
            blink(((powerUpTime / 3) & 1) == 0);
        } else
        {
            powerUpTime++;
            blink(((-powerUpTime / 3) & 1) == 0);
        }

        if (powerUpTime == 0) levelScene.paused = false;

        calcPic();
        return;
    }

    if (invulnerableTime > 0) invulnerableTime--;
    visible = ((invulnerableTime / 2) & 1) == 0;

    wasOnGround = onGround;
    float sideWaysSpeed = keys[KEY_SPEED] ? 1.2f : 0.6f;

    //        float sideWaysSpeed = onGround ? 2.5f : 1.2f;

    if (onGround)
    {
        ducking = keys[KEY_DOWN] && large;
    }

    if (xa > 2)
    {
        facing = 1;
    }
    if (xa < -2)
    {
        facing = -1;
    }

    if (keys[KEY_JUMP] || (jumpTime < 0 && !onGround && !sliding))
    {
        if (jumpTime < 0)
        {
            xa = xJumpSpeed;
            ya = -jumpTime * yJumpSpeed;
            jumpTime++;
        } else if (onGround && mayJump)
        {
            xJumpSpeed = 0;
            yJumpSpeed = -1.9f;
            jumpTime = (int) jT;
            ya = jumpTime * yJumpSpeed;
            onGround = false;
            sliding = false;
        } else if (sliding && mayJump)
        {
            xJumpSpeed = -facing * 6.0f;
            yJumpSpeed = -2.0f;
            jumpTime = -6;
            xa = xJumpSpeed;
            ya = -jumpTime * yJumpSpeed;
            onGround = false;
            sliding = false;
            facing = -facing;
        } else if (jumpTime > 0)
        {
            xa += xJumpSpeed;
            ya = jumpTime * yJumpSpeed;
            jumpTime--;
        }
    } else
    {
        jumpTime = 0;
    }

    if (keys[KEY_LEFT] && !ducking)
    {
        if (facing == 1) sliding = false;
        xa -= sideWaysSpeed;
        if (jumpTime >= 0) facing = -1;
    }

    if (keys[KEY_RIGHT] && !ducking)
    {
        if (facing == -1) sliding = false;
        xa += sideWaysSpeed;
        if (jumpTime >= 0) facing = 1;
    }

    if ((!keys[KEY_LEFT] && !keys[KEY_RIGHT]) || ducking || ya < 0 || onGround)
    {
        sliding = false;
    }

    if (keys[KEY_SPEED] && canShoot && Mario.fire && levelScene.fireballsOnScreen < 2)
    {
        levelScene.addSprite(new Fireball(levelScene, x + facing * 6, y - 20, facing));
    }
    // Cheats:
    if (GlobalOptions.isPowerRestoration && keys[KEY_SPEED] && (!Mario.large || !Mario.fire))
        setMode(true, true);
//        if (cheatKeys[KEY_LIFE_UP])
//            this.lives++;

    levelScene.paused = GlobalOptions.isPauseWorld;
//        if (keys[KEY_DUMP_CURRENT_WORLD])
//            try {
//                System.out.println("DUMP:");
////                levelScene.getObservationStrings(System.out);
//                //levelScene.level.save(System.out);
//                System.out.println("DUMPED:");
//            } catch (IOException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
    canShoot = !keys[KEY_SPEED];

    mayJump = (onGround || sliding) && !keys[KEY_JUMP];

    xFlipPic = facing == -1;

    runTime += (Math.abs(xa)) + 5;
    if (Math.abs(xa) < 0.5f)
    {
        runTime = 0;
        xa = 0;
    }

    calcPic();

    if (sliding)
    {
        for (int i = 0; i < 1; i++)
        {
            levelScene.addSprite(new Sparkle((int) (x + Math.random() * 4 - 2) + facing * 8, (int) (y + Math.random() * 4) - 24, (float) (Math.random() * 2 - 1), (float) Math.random() * 1, 0, 1, 5));
        }
        ya *= 0.5f;
    }

    onGround = false;
    move(xa, 0);
    move(0, ya);

    if (y > levelScene.level.height * 16 + 16)
        die("Gap");

    if (x < 0)
    {
        x = 0;
        xa = 0;
    }

    if (x > levelScene.level.xExit * 16 - 8 &&
            x < levelScene.level.xExit * 16 + 2 * 16 &&
            y < levelScene.level.yExit * 16)
    {
        x = levelScene.level.xExit * 16;
        win();
    }

    if (x > levelScene.level.length * 16)
    {
        x = levelScene.level.length * 16;
        xa = 0;
    }

    ya *= 0.85f;
    if (onGround)
    {
        xa *= GROUND_INERTIA;
    } else
    {
        xa *= AIR_INERTIA;
    }


    if (!onGround)
    {
//        ya += 3;
        ya += yaa;
    }

    if (carried != null)
    {
        carried.x = x + facing * 8; //TODO : move to cellSize_2 = cellSize/2;
        carried.y = y - 2;
        if (!keys[KEY_SPEED])
        {
            carried.release(this);
            carried = null;
            setRacoon(false);
//                System.out.println("carried = " + carried);
        }
//            System.out.println("sideWaysSpeed = " + sideWaysSpeed);
    }
}

private void calcPic()
{
    int runFrame;

    if (large || isRacoon)
    {
        runFrame = ((int) (runTime / 20)) % 4;
        if (runFrame == 3) runFrame = 1;
        if (carried == null && Math.abs(xa) > 10) runFrame += 3;
        if (carried != null) runFrame += 10;
        if (!onGround)
        {
            if (carried != null) runFrame = 12;
            else if (Math.abs(xa) > 10) runFrame = 7;
            else runFrame = 6;
        }
    } else
    {
        runFrame = ((int) (runTime / 20)) % 2;
        if (carried == null && Math.abs(xa) > 10) runFrame += 2;
        if (carried != null) runFrame += 8;
        if (!onGround)
        {
            if (carried != null) runFrame = 9;
            else if (Math.abs(xa) > 10) runFrame = 5;
            else runFrame = 4;
        }
    }

    if (onGround && ((facing == -1 && xa > 0) || (facing == 1 && xa < 0)))
    {
        if (xa > 1 || xa < -1) runFrame = large ? 9 : 7;

        if (xa > 3 || xa < -3)
        {
            for (int i = 0; i < 3; i++)
            {
                levelScene.addSprite(new Sparkle((int) (x + Math.random() * 8 - 4), (int) (y + Math.random() * 4), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
            }
        }
    }

    if (large)
    {
        if (ducking) runFrame = 14;
        height = ducking ? 12 : 24;
    } else
    {
        height = 12;
    }

    xPic = runFrame;
}

private boolean move(float xa, float ya)
{
    while (xa > 8)
    {
        if (!move(8, 0)) return false;
        xa -= 8;
    }
    while (xa < -8)
    {
        if (!move(-8, 0)) return false;
        xa += 8;
    }
    while (ya > 8)
    {
        if (!move(0, 8)) return false;
        ya -= 8;
    }
    while (ya < -8)
    {
        if (!move(0, -8)) return false;
        ya += 8;
    }

    boolean collide = false;
    if (ya > 0)
    {
        if (isBlocking(x + xa - width, y + ya, xa, 0)) collide = true;
        else if (isBlocking(x + xa + width, y + ya, xa, 0)) collide = true;
        else if (isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
        else if (isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;
    }
    if (ya < 0)
    {
        if (isBlocking(x + xa, y + ya - height, xa, ya)) collide = true;
        else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
        else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
    }
    if (xa > 0)
    {
        sliding = true;
        if (isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
        else sliding = false;
        if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
        else sliding = false;
        if (isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;
        else sliding = false;
    }
    if (xa < 0)
    {
        sliding = true;
        if (isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
        else sliding = false;
        if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
        else sliding = false;
        if (isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;
        else sliding = false;
    }

    if (collide)
    {
        if (xa < 0)
        {
            x = (int) ((x - width) / 16) * 16 + width;
            this.xa = 0;
        }
        if (xa > 0)
        {
            x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
            this.xa = 0;
        }
        if (ya < 0)
        {
            y = (int) ((y - height) / 16) * 16 + height;
            jumpTime = 0;
            this.ya = 0;
        }
        if (ya > 0)
        {
            y = (int) ((y - 1) / 16 + 1) * 16 - 1;
            onGround = true;
        }
        return false;
    } else
    {
        x += xa;
        y += ya;
        return true;
    }
}

private boolean isBlocking(float _x, float _y, float xa, float ya)
{
    int x = (int) (_x / 16);
    int y = (int) (_y / 16);
    if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) return false;

    boolean blocking = levelScene.level.isBlocking(x, y, xa, ya);

    byte block = levelScene.level.getBlock(x, y);

    if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
    {
        Mario.gainCoin();
        levelScene.level.setBlock(x, y, (byte) 0);
        for (int xx = 0; xx < 2; xx++)
            for (int yy = 0; yy < 2; yy++)
                levelScene.addSprite(new Sparkle(x * 16 + xx * 8 + (int) (Math.random() * 8), y * 16 + yy * 8 + (int) (Math.random() * 8), 0, 0, 0, 2, 5));
    }

    if (blocking && ya < 0)
    {
        levelScene.bump(x, y, large);
    }

    return blocking;
}

public void stomp(Enemy enemy)
{
    if (deathTime > 0 || levelScene.paused) return;

    float targetY = enemy.y - enemy.height / 2;
    move(0, targetY - y);
    mapY = (int) y / 16;

    xJumpSpeed = 0;
    yJumpSpeed = -1.9f;
    jumpTime = (int) jT + 1;
    ya = jumpTime * yJumpSpeed;
    onGround = false;
    sliding = false;
    invulnerableTime = 1;
}

public void stomp(Shell shell)
{
    if (deathTime > 0 || levelScene.paused) return;

    if (keys[KEY_SPEED] && shell.facing == 0)
    {
        carried = shell;
        shell.carried = true;
        setRacoon(true);
    } else
    {
        float targetY = shell.y - shell.height / 2;
        move(0, targetY - y);
        mapY = (int) y / 16;

        xJumpSpeed = 0;
        yJumpSpeed = -1.9f;
        jumpTime = (int) jT + 1;
        ya = jumpTime * yJumpSpeed;
        onGround = false;
        sliding = false;
        invulnerableTime = 1;
    }
}

public void getHurt(final int spriteKind)
{
    if (deathTime > 0 || levelScene.paused || isMarioInvulnerable) return;

    if (invulnerableTime > 0) return;

    ++collisionsWithCreatures;
    if (large)
    {
        levelScene.paused = true;
        powerUpTime = -3 * FractionalPowerUpTime;
        if (fire)
        {
            levelScene.mario.setMode(true, false);
        } else
        {
            levelScene.mario.setMode(false, false);
        }
        invulnerableTime = 32;
    } else
    {
        die("Collision with a creature " + spriteKind); // TODO: substitue by named creature kind
    }
}

public void win()
{
    xDeathPos = (int) x;
    yDeathPos = (int) y;
    levelScene.paused = true;
    winTime = 1;
    status = Mario.STATUS_WIN;
}

public void die(String reasonOfDeath)
{
    xDeathPos = (int) x;
    yDeathPos = (int) y;
    levelScene.paused = true;
    deathTime = 25;
    status = Mario.STATUS_DEAD;
    // TODO: [M] refactor reasons of death to enum {COLLISION, GAP, TIMEOUT}
    levelScene.addMemoMessage("Reason of death: " + reasonOfDeath);
}

public void getFlower()
{
    if (deathTime > 0 || levelScene.paused) return;

    if (!fire)
    {
        levelScene.paused = true;
        powerUpTime = 3 * FractionalPowerUpTime;
        levelScene.mario.setMode(true, true);
    } else
    {
        Mario.gainCoin();
    }
    ++flowersDevoured;
}

public void getMushroom()
{
    if (deathTime > 0 || levelScene.paused) return;

    if (!large)
    {
        levelScene.paused = true;
        powerUpTime = 3 * FractionalPowerUpTime;
        levelScene.mario.setMode(true, false);
    } else
    {
        Mario.gainCoin();
    }
    ++mushroomsDevoured;
}

public void kick(Shell shell)
{
//        if (deathTime > 0 || levelScene.paused) return;

    if (keys[KEY_SPEED])
    {
        carried = shell;
        shell.carried = true;
        setRacoon(true);
        System.out.println("shell = " + shell);
    } else
    {
        invulnerableTime = 1;
    }
}

public void stomp(BulletBill bill)
{
    if (deathTime > 0 || levelScene.paused) return;

    float targetY = bill.y - bill.height / 2;
    move(0, targetY - y);
    mapY = (int) y / 16;

    xJumpSpeed = 0;
    yJumpSpeed = -1.9f;
    jumpTime = (int) jT + 1;
    ya = jumpTime * yJumpSpeed;
    onGround = false;
    sliding = false;
    invulnerableTime = 1;
}

public static void gainCoin()
{
    coins++;
//        if (coins % 100 == 0)
//            get1Up();
}

public static void gainHiddenBlock()
{
    ++hiddenBlocksFound;
}

public int getStatus()
{
    return status;
}

public boolean isOnGround()
{
    return onGround;
}

public boolean mayJump()
{
    return mayJump;
}

public boolean isCanShoot()
{
    return canShoot;
}

}

//    public byte getKeyMask()
//    {
//        int mask = 0;
//        for (int i = 0; i < 7; i++)
//        {
//            if (keys[i]) mask |= (1 << i);
//        }
//        return (byte) mask;
//    }

//    public void setKeys(byte mask)
//    {
//        for (int i = 0; i < 7; i++)
//        {
//            keys[i] = (mask & (1 << i)) > 0;
//        }
//    }
//
//    public static void get1Up()
//    {
//        lives++;
//    }
