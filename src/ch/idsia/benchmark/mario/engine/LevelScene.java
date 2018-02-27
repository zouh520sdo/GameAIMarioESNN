package ch.idsia.benchmark.mario.engine;

import ch.idsia.benchmark.mario.engine.level.Level;
import ch.idsia.benchmark.mario.engine.level.LevelGenerator;
import ch.idsia.benchmark.mario.engine.level.SpriteTemplate;
import ch.idsia.benchmark.mario.engine.sprites.*;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.CmdLineOptions;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class LevelScene implements SpriteContext, Serializable
{
public static final boolean[] defaultKeys = new boolean[Environment.numberOfButtons];
public static final String[] keysStr = {"<<L ", "R>> ", "\\\\//", "JUMP", " RUN", "^UP^"};

public static final int cellSize = 16;

final public List<Sprite> sprites = new ArrayList<Sprite>();
final private List<Sprite> spritesToAdd = new ArrayList<Sprite>();
final private List<Sprite> spritesToRemove = new ArrayList<Sprite>();

public Level level;
public Mario mario;
public float xCam, yCam, xCamO, yCamO;

public int tick;

public boolean paused = false;
public int startTime = 0;
private int timeLeft;
private int width;
private int height;

private static float creaturesGravity;

public boolean visualization = false;

private static final int CANNON_MUZZLE = -82;
private static final int CANNON_TRUNK = -80;
private static final int COIN_ANIM = Sprite.KIND_COIN_ANIM;  //1
private static final int BREAKABLE_BRICK = -20;
private static final int UNBREAKABLE_BRICK = -22; //a rock with animated question mark
private static final int BRICK = -24;           //a rock with animated question mark
private static final int FLOWER_POT = -90;
private static final int BORDER_CANNOT_PASS_THROUGH = -60;
private static final int BORDER_HILL = -62;
// TODO : resolve this FLOWER_POT_OR_CANNON = -85;
private static final int FLOWER_POT_OR_CANNON = -85;

private int receptiveFieldHeight = -1; // to be setup via CmdLineOptions
private int receptiveFieldWidth = -1; // to be setup via CmdLineOptions
private int prevRFH = -1;
private int prevRFW = -1;
private int[] serializedLevelScene;   // memory is allocated in reset
private int[] serializedEnemies;      // memory is allocated in reset
private int[] serializedMergedObservation; // memory is allocated in reset

private byte[][] levelSceneZ;     // memory is allocated in reset
private byte[][] enemiesZ;      // memory is allocated in reset
private byte[][] mergedZZ;      // memory is allocated in reset


final private List<Float> enemiesFloatsList = new ArrayList<Float>();
final private float[] marioFloatPos = new float[2];
final private int[] marioState = new int[12];
private int numberOfHiddenCoinsGained = 0;

public String memo = "";
private Point marioInitialPos;
private static final long serialVersionUID = -715653399093887130L;

//    public int getTimeLimit() {  return timeLimit; }

public void setTimeLimit(int timeLimit)
{ this.timeLimit = timeLimit; }

private int timeLimit = 200;

private long levelSeed;
private int levelType;
private int levelDifficulty;
private int levelLength;
private int levelHeight;
public static int killedCreaturesTotal;
public static int killedCreaturesByFireBall;
public static int killedCreaturesByStomp;
public static int killedCreaturesByShell;

//    private int[] args; //passed to reset method. ATTENTION: not cloned.

public LevelScene()
{
    try
    {
//            System.out.println("Java::LevelScene: loading tiles.dat...");
//            System.out.println("LS: System.getProperty(\"user.dir()\") = " + System.getProperty("user.dir"));
        Level.loadBehaviors(new DataInputStream(LevelScene.class.getResourceAsStream("resources/tiles.dat")));
    }
    catch (IOException e)
    {
        System.err.println("[MarioAI ERROR] : error loading file resources/tiles.dat ; ensure this file exists in ch/idsia/benchmark/mario/engine ");
        e.printStackTrace();
        System.exit(0);
    }
}

//TODO: Move to createLevel, enable -ls to accept filePaths for *.lvl files, i.e. allow to load levels with -ls option

private String mapElToStr(int el)
{
    String s = "";
    if (el == 0 || el == 1)
        s = "##";
    s += (el == mario.kind) ? "#M.#" : el;
    while (s.length() < 4)
        s += "#";
    return s + " ";
}

private String enemyToStr(int el)
{
    String s = "";
    if (el == 0)
        s = "";
    s += (el == mario.kind) ? "-m" : el;
    while (s.length() < 2)
        s += "#";
    return s + " ";
}

private byte ZLevelMapElementGeneralization(byte el, int ZLevel)
{
    if (el == 0)
        return 0;
    switch (ZLevel)
    {
        case (0):
            switch (el)
            {
                case 16:  // brick, simple, without any surprise.
                case 17:  // brick with a hidden coin
                case 18:  // brick with a hidden friendly flower
                    return BREAKABLE_BRICK;
                case 21:       // question brick, contains coin
                case 22:       // question brick, contains flower/mushroom
                    return UNBREAKABLE_BRICK; // question brick, contains something
                case 34:
                    return COIN_ANIM;
                case 4:
                    return BORDER_CANNOT_PASS_THROUGH;
                case 14:
                    return CANNON_MUZZLE;
                case 30:
                case 46:
                    return CANNON_TRUNK;
                case 10:
                case 11:
                case 26:
                case 27:
                    return FLOWER_POT;
                case 1:
                    return 0; //hidden block
                case -124:
                case -123:
                case -122:
                case -74:
                    return BORDER_HILL;
                case -108:
                case -107:
                case -106:
                    return 0; //background of the hill. empty space
            }
            return el;
        case (1):
            switch (el)
            {
                case 16:  // brick, simple, without any surprise.
                case 17:  // brick with a hidden coin
                case 18:  // brick with a hidden flower
                case 21:       // question brick, contains coin
                case 22:       // question brick, contains flower/mushroom
                    return BRICK; // any brick
                case 1:   // hidden block
                case (-111):
                case (-108):
                case (-107):
                case (-106):
                case (15): // Sparcle, irrelevant
                    return 0;
                case (34):
                    return COIN_ANIM;
                case (-128):
                case (-127):
                case (-126):
                case (-125):
                case (-120):
                case (-119):
                case (-118):
                case (-117):
                case (-116):
                case (-115):
                case (-114):
                case (-113):
                case (-112):
                case (-110):
                case (-109):
                case (-104):
                case (-103):
                case (-102):
                case (-101):
                case (-100):
                case (-99):
                case (-98):
                case (-97):
                case (-96):
                case (-95):
                case (-94):
                case (-93):
                case (-69):
                case (-65):
                case (-88):
                case (-87):
                case (-86):
                case (-85):
                case (-84):
                case (-83):
                case (-82):
                case (-81):
                case (-77):
                case (4):  // kicked hidden brick
                case (9):
                    return BORDER_CANNOT_PASS_THROUGH;   // border, cannot pass through, can stand on
//                    case(9):
//                        return -12; // hard formation border. Pay attention!
                case (-124):
                case (-123):
                case (-122):
                case (-76):
                case (-74):
                    return BORDER_HILL; // half-border, can jump through from bottom and can stand on
                case (10):
                case (11):
                case (26):
                case (27): //flower pot
                case (14):
                case (30):
                case (46): // canon
                    return FLOWER_POT_OR_CANNON;  // angry flower pot or cannon
            }
            System.err.println("ZLevelMapElementGeneralization: Unknown value el = " + el + " Possible Level tiles bug; " +
                    "Please, inform sergey@idsia.ch or julian@togelius.com. Thanks!");
            return el;
        case (2):
            switch (el)
            {
                //cancel out half-borders, that could be passed through
                case (0):
                case (-108):
                case (-107):
                case (-106):
                case 1:   //hidden block
                case (15): // Sparcle, irrelevant
                    return 0;
                case (34): // coins
                    return COIN_ANIM;
                case 16:  // brick, simple, without any surprise.
                case 17:  // brick with a hidden coin
                case 18:  // brick with a hidden flower
                case 21:       // question brick, contains coin
                case 22:       // question brick, contains flower/mushroom
                    //here bricks are any objects cannot jump through and can stand on
                case 4: //kicked hidden block
                case 9:
                case (10):
                case (11):
                case (26):
                case (27): //flower pot
                case (14):
                case (30):
                case (46): // canon
                    return BORDER_CANNOT_PASS_THROUGH; // question brick, contains something
            }
            return 1;  // everything else is "something", so it is 1
    }
    System.err.println("Unkown ZLevel Z" + ZLevel);
    return el; //TODO: Throw unknown ZLevel exception
}


private byte ZLevelEnemyGeneralization(byte el, int ZLevel)
{
    switch (ZLevel)
    {
        case (0):
            switch (el)
            {
                // cancel irrelevant sprite codes
                case (Sprite.KIND_COIN_ANIM):
                case (Sprite.KIND_PARTICLE):
                case (Sprite.KIND_SPARCLE):
                case (Sprite.KIND_MARIO):
                    return Sprite.KIND_NONE;
            }
            return el;   // all the rest should go as is
        case (1):
            switch (el)
            {
                case (Sprite.KIND_COIN_ANIM):
                case (Sprite.KIND_PARTICLE):
                case (Sprite.KIND_SPARCLE):
                case (Sprite.KIND_MARIO):
                    return Sprite.KIND_NONE;
                case (Sprite.KIND_FIRE_FLOWER):
                    return Sprite.KIND_FIRE_FLOWER;
                case (Sprite.KIND_MUSHROOM):
                    return Sprite.KIND_MUSHROOM;
                case (Sprite.KIND_FIREBALL):
                    return Sprite.KIND_FIREBALL;
                case (Sprite.KIND_BULLET_BILL):
                case (Sprite.KIND_GOOMBA):
                case (Sprite.KIND_GOOMBA_WINGED):
                case (Sprite.KIND_GREEN_KOOPA):
                case (Sprite.KIND_GREEN_KOOPA_WINGED):
                case (Sprite.KIND_RED_KOOPA):
                case (Sprite.KIND_RED_KOOPA_WINGED):
                case (Sprite.KIND_SHELL):
                    return Sprite.KIND_GOOMBA;
                case (Sprite.KIND_SPIKY):
                case (Sprite.KIND_ENEMY_FLOWER):
                case (Sprite.KIND_SPIKY_WINGED):
                    return Sprite.KIND_SPIKY;
            }
            System.err.println("Z1 UNKOWN el = " + el);
            return el;
        case (2):
            switch (el)
            {
                case (Sprite.KIND_COIN_ANIM):
                case (Sprite.KIND_PARTICLE):
                case (Sprite.KIND_SPARCLE):
                case (Sprite.KIND_FIREBALL):
                case (Sprite.KIND_MARIO):
                case (Sprite.KIND_FIRE_FLOWER):
                case (Sprite.KIND_MUSHROOM):
                    return Sprite.KIND_NONE;
                case (Sprite.KIND_BULLET_BILL):
                case (Sprite.KIND_GOOMBA):
                case (Sprite.KIND_GOOMBA_WINGED):
                case (Sprite.KIND_GREEN_KOOPA):
                case (Sprite.KIND_GREEN_KOOPA_WINGED):
                case (Sprite.KIND_RED_KOOPA):
                case (Sprite.KIND_RED_KOOPA_WINGED):
                case (Sprite.KIND_SHELL):
                case (Sprite.KIND_SPIKY):
                case (Sprite.KIND_ENEMY_FLOWER):
                case (Sprite.KIND_SPIKY_WINGED):
                    return 1;
            }
            System.err.println("ERROR: Z2 UNKNOWNN el = " + el);
            return 1;
    }
    return el; //TODO: Throw unknown ZLevel exception
}

public byte[][] getLevelSceneObservationZ(int ZLevel)
{
    for (int y = mario.mapY - receptiveFieldHeight / 2, obsX = 0; y <= mario.mapY + receptiveFieldHeight / 2; y++, obsX++)
    {
        for (int x = mario.mapX - receptiveFieldWidth / 2, obsY = 0; x <= mario.mapX + receptiveFieldWidth / 2; x++, obsY++)
        {
            if (x >= 0 && x < level.xExit && y >= 0 && y < level.height)
            {
                levelSceneZ[obsX][obsY] = ZLevelMapElementGeneralization(level.map[x][y], ZLevel);
            } else
            {
                levelSceneZ[obsX][obsY] = 0;
            }

        }
    }
    return levelSceneZ;
}

public byte[][] getEnemiesObservationZ(int ZLevel)
{
    for (int w = 0; w < enemiesZ.length; w++)
        for (int h = 0; h < enemiesZ[0].length; h++)
            enemiesZ[w][h] = 0;
    for (Sprite sprite : sprites)
    {
        if (sprite.kind == mario.kind)
            continue;
        if (sprite.mapX >= 0 &&
                sprite.mapX >= mario.mapX - receptiveFieldWidth / 2 &&
                sprite.mapX <= mario.mapX + receptiveFieldWidth / 2 &&
                sprite.mapY >= 0 &&
                sprite.mapY >= mario.mapY - receptiveFieldHeight / 2 &&
                sprite.mapY <= mario.mapY + receptiveFieldHeight / 2)
        {
            int obsX = sprite.mapY - mario.mapY + receptiveFieldHeight / 2;
            int obsY = sprite.mapX - mario.mapX + receptiveFieldWidth / 2;
            enemiesZ[obsX][obsY] = ZLevelEnemyGeneralization(sprite.kind, ZLevel);
        }
    }
    return enemiesZ;
}

public float[] getEnemiesFloatPos()
{
    enemiesFloatsList.clear();
    for (Sprite sprite : sprites)
    {
        // TODO: add unit tests for getEnemiesFloatPos involving all kinds of creatures
        switch (sprite.kind)
        {
            case Sprite.KIND_GOOMBA:
            case Sprite.KIND_BULLET_BILL:
            case Sprite.KIND_ENEMY_FLOWER:
            case Sprite.KIND_GOOMBA_WINGED:
            case Sprite.KIND_GREEN_KOOPA:
            case Sprite.KIND_GREEN_KOOPA_WINGED:
            case Sprite.KIND_RED_KOOPA:
            case Sprite.KIND_RED_KOOPA_WINGED:
            case Sprite.KIND_SPIKY:
            case Sprite.KIND_SPIKY_WINGED:
            case Sprite.KIND_SHELL:
            {
                enemiesFloatsList.add((float) sprite.kind);
                enemiesFloatsList.add(sprite.x - mario.x);
                enemiesFloatsList.add(sprite.y - mario.y);
            }
        }
    }

    float[] enemiesFloatsPosArray = new float[enemiesFloatsList.size()];

    int i = 0;
    for (Float F : enemiesFloatsList)
        enemiesFloatsPosArray[i++] = F;

    return enemiesFloatsPosArray;
}

public byte[][] getMergedObservationZZ(int ZLevelScene, int ZLevelEnemies)
{
//    int MarioXInMap = (int) mario.x / cellSize;
//    int MarioYInMap = (int) mario.y / cellSize;

//    if (MarioXInMap != (int) mario.x / cellSize ||MarioYInMap != (int) mario.y / cellSize )
//        throw new Error("WRONG mario x or y pos");
    for (int y = mario.mapY - receptiveFieldHeight / 2, obsX = 0; y <= mario.mapY + receptiveFieldHeight / 2; y++, obsX++)
    {
        for (int x = mario.mapX - receptiveFieldWidth / 2, obsY = 0; x <= mario.mapX + receptiveFieldWidth / 2; x++, obsY++)
        {
            if (x >= 0 && x < level.xExit && y >= 0 && y < level.height)
            {
                mergedZZ[obsX][obsY] = ZLevelMapElementGeneralization(level.map[x][y], ZLevelScene);
            } else
                mergedZZ[obsX][obsY] = 0;
//                if (x == MarioXInMap && y == MarioYInMap)
//                    mergedZZ[obsX][obsY] = mario.kind;
        }
    }
//        for (int w = 0; w < mergedZZ.length; w++)
//            for (int h = 0; h < mergedZZ[0].length; h++)
//                mergedZZ[w][h] = -1;
    for (Sprite sprite : sprites)
    {
        if (sprite.kind == mario.kind)
            continue;
        if (sprite.mapX >= 0 &&
                sprite.mapX > mario.mapX - receptiveFieldWidth / 2 &&
                sprite.mapX < mario.mapX + receptiveFieldWidth / 2 &&
                sprite.mapY >= 0 &&
                sprite.mapY > mario.mapY - receptiveFieldHeight / 2 &&
                sprite.mapY < mario.mapY + receptiveFieldHeight / 2)
        {
            int obsX = sprite.mapY - mario.mapY + receptiveFieldHeight / 2;
            int obsY = sprite.mapX - mario.mapX + receptiveFieldWidth / 2;
            // quick fix TODO: handle this in more general way.
            // TODO: substitue '14' by explicit statement
            if (mergedZZ[obsX][obsY] != 14)
            {
                byte tmp = ZLevelEnemyGeneralization(sprite.kind, ZLevelEnemies);
                if (tmp != Sprite.KIND_NONE)
                    mergedZZ[obsX][obsY] = tmp;
            }
        }
    }

    return mergedZZ;
}

public List<String> getObservationStrings(boolean Enemies, boolean LevelMap,
                                          boolean mergedObservationFlag,
                                          int ZLevelScene, int ZLevelEnemies)
{
    List<String> ret = new ArrayList<String>();
    if (level != null && mario != null)
    {
        ret.add("Total levelScene length = " + level.length);
        ret.add("Total levelScene height = " + level.height);
        ret.add("Physical Mario Position (x,y): (" + mario.x + "," + mario.y + ")");
        ret.add("Mario Observation (Receptive Field)   Width: " + receptiveFieldWidth + " Height: " + receptiveFieldHeight);
        ret.add("X Exit Position: " + level.xExit);
        int MarioXInMap = (int) mario.x / cellSize;
        int MarioYInMap = (int) mario.y / cellSize;
        ret.add("Calibrated Mario Position (x,y): (" + MarioXInMap + "," + MarioYInMap + ")\n");

        byte[][] levelScene = getLevelSceneObservationZ(ZLevelScene);
        if (LevelMap)
        {
            ret.add("~ZLevel: Z" + ZLevelScene + " map:\n");
            for (int x = 0; x < levelScene.length; ++x)
            {
                String tmpData = "";
                for (int y = 0; y < levelScene[0].length; ++y)
                    tmpData += mapElToStr(levelScene[x][y]);
                ret.add(tmpData);
            }
        }

        byte[][] enemiesObservation = null;
        if (Enemies || mergedObservationFlag)
            enemiesObservation = getEnemiesObservationZ(ZLevelEnemies);

        if (Enemies)
        {
            ret.add("~ZLevel: Z" + ZLevelScene + " Enemies Observation:\n");
            for (int x = 0; x < enemiesObservation.length; x++)
            {
                String tmpData = "";
                for (int y = 0; y < enemiesObservation[0].length; y++)
                {
//                        if (x >=0 && x <= level.xExit)
                    tmpData += enemyToStr(enemiesObservation[x][y]);
                }
                ret.add(tmpData);
            }
        }

        if (mergedObservationFlag)
        {
            byte[][] mergedObs = getMergedObservationZZ(ZLevelScene, ZLevelEnemies);
            ret.add("~ZLevelScene: Z" + ZLevelScene + " ZLevelEnemies: Z" + ZLevelEnemies + " ; Merged observation /* Mario ~> #M.# */");
            for (int x = 0; x < levelScene.length; ++x)
            {
                String tmpData = "";
                for (int y = 0; y < levelScene[0].length; ++y)
                    tmpData += mapElToStr(mergedObs[x][y]);
                ret.add(tmpData);
            }
        }
    } else
        ret.add("~[MarioAI ERROR] level : " + level + " mario : " + mario);
    return ret;
}

public int fireballsOnScreen = 0;

List<Shell> shellsToCheck = new ArrayList<Shell>();

public void checkShellCollide(Shell shell)
{
    shellsToCheck.add(shell);
}

List<Fireball> fireballsToCheck = new ArrayList<Fireball>();

public void checkFireballCollide(Fireball fireball)
{
    fireballsToCheck.add(fireball);
}

public void tick()
{
    if (GlobalOptions.isGameplayStopped)
        return;

    timeLeft--;
    if (timeLeft == 0)
        mario.die("Time out!");
    xCamO = xCam;
    yCamO = yCam;

    if (startTime > 0)
    {
        startTime++;
    }

    float targetXCam = mario.x - 160;

    xCam = targetXCam;

    if (xCam < 0) xCam = 0;
    if (xCam > level.length * cellSize - GlobalOptions.VISUAL_COMPONENT_WIDTH)
        xCam = level.length * cellSize - GlobalOptions.VISUAL_COMPONENT_WIDTH;

    // TODO: reincarnate recordings
    /*      if (recorder != null)
    {
    recorder.addTick(mario.getKeyMask());
    }

    if (replayer!=null)
    {
    mario.setKeys(replayer.nextTick());
    }*/

    fireballsOnScreen = 0;

    for (Sprite sprite : sprites)
    {
        if (sprite != mario)
        {
            float xd = sprite.x - xCam;
            float yd = sprite.y - yCam;
            if (xd < -64 || xd > GlobalOptions.VISUAL_COMPONENT_WIDTH + 64 || yd < -64 || yd > GlobalOptions.VISUAL_COMPONENT_HEIGHT + 64)
            {
                removeSprite(sprite);
            } else
            {
                if (sprite instanceof Fireball)
                    fireballsOnScreen++;
            }
        }
    }

    if (paused)
    {
        for (Sprite sprite : sprites)
        {
            if (sprite == mario)
            {
                sprite.tick();
            } else
            {
                sprite.tickNoMove();
            }
        }
    } else
    {
        tick++;
        level.tick();

//            boolean hasShotCannon = false;
//            int xCannon = 0;

        for (int x = (int) xCam / cellSize - 1; x <= (int) (xCam + this.width) / cellSize + 1; x++)
            for (int y = (int) yCam / cellSize - 1; y <= (int) (yCam + this.height) / cellSize + 1; y++)
            {
                int dir = 0;

                if (x * cellSize + 8 > mario.x + cellSize) dir = -1;
                if (x * cellSize + 8 < mario.x - cellSize) dir = 1;

                SpriteTemplate st = level.getSpriteTemplate(x, y);

                if (st != null)
                {
//                        if (st.getType() == Sprite.KIND_SPIKY)
//                        {
//                            System.out.println("here");
//                        }

                    if (st.lastVisibleTick != tick - 1)
                    {
                        if (st.sprite == null || !sprites.contains(st.sprite))
                            st.spawn(this, x, y, dir);
                    }

                    st.lastVisibleTick = tick;
                }

                if (dir != 0)
                {
                    byte b = level.getBlock(x, y);
                    if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0)
                    {
                        if ((b % cellSize) / 4 == 3 && b / cellSize == 0)
                        {
                            if ((tick - x * 2) % 100 == 0)
                            {
//                                    xCannon = x;
                                for (int i = 0; i < 8; i++)
                                {
                                    addSprite(new Sparkle(x * cellSize + 8, y * cellSize + (int) (Math.random() * cellSize), (float) Math.random() * dir, 0, 0, 1, 5));
                                }
                                addSprite(new BulletBill(this, x * cellSize + 8 + dir * 8, y * cellSize + 15, dir));

//                                    hasShotCannon = true;
                            }
                        }
                    }
                }
            }

        for (Sprite sprite : sprites)
            sprite.tick();

        for (Sprite sprite : sprites)
            sprite.collideCheck();

        for (Shell shell : shellsToCheck)
        {
            for (Sprite sprite : sprites)
            {
                if (sprite != shell && !shell.dead)
                {
                    if (sprite.shellCollideCheck(shell))
                    {
                        if (mario.carried == shell && !shell.dead)
                        {
                            mario.carried = null;
                            mario.setRacoon(false);
                            //System.out.println("sprite = " + sprite);
                            shell.die();
                            ++this.killedCreaturesTotal;
                        }
                    }
                }
            }
        }
        shellsToCheck.clear();

        for (Fireball fireball : fireballsToCheck)
            for (Sprite sprite : sprites)
                if (sprite != fireball && !fireball.dead)
                    if (sprite.fireballCollideCheck(fireball))
                        fireball.die();
        fireballsToCheck.clear();
    }

    sprites.addAll(0, spritesToAdd);
    sprites.removeAll(spritesToRemove);
    spritesToAdd.clear();
    spritesToRemove.clear();
}

public void addSprite(Sprite sprite)
{
    spritesToAdd.add(sprite);
    sprite.tick();
}

public void removeSprite(Sprite sprite)
{
    spritesToRemove.add(sprite);
}

public void bump(int x, int y, boolean canBreakBricks)
{
    byte block = level.getBlock(x, y);

    if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0)
    {
        if (block == 1)
            Mario.gainHiddenBlock();
        bumpInto(x, y - 1);
        level.setBlock(x, y, (byte) 4);
        level.setBlockData(x, y, (byte) 4);

        if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
        {
            if (!Mario.large)
            {
                addSprite(new Mushroom(this, x * cellSize + 8, y * cellSize + 8));
                ++level.counters.mushrooms;
            } else
            {
                addSprite(new FireFlower(this, x * cellSize + 8, y * cellSize + 8));
                ++level.counters.flowers;
            }
        } else
        {
            Mario.gainCoin();
            addSprite(new CoinAnim(x, y));
        }
    }

    if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
    {
        bumpInto(x, y - 1);
        if (canBreakBricks)
        {
            level.setBlock(x, y, (byte) 0);
            for (int xx = 0; xx < 2; xx++)
                for (int yy = 0; yy < 2; yy++)
                    addSprite(new Particle(x * cellSize + xx * 8 + 4, y * cellSize + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));
        } else
        {
            level.setBlockData(x, y, (byte) 4);
        }
    }
}

public void bumpInto(int x, int y)
{
    byte block = level.getBlock(x, y);
    if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
    {
        Mario.gainCoin();
        level.setBlock(x, y, (byte) 0);
        addSprite(new CoinAnim(x, y + 1));
    }

    for (Sprite sprite : sprites)
    {
        sprite.bumpCheck(x, y);
    }
}

public int getTimeSpent() { return startTime / 15; }

public int getTimeLeft() { return timeLeft / 15; }

public int getKillsTotal()
{
    return mario.levelScene.killedCreaturesTotal;
}

public int getKillsByFire()
{
    return mario.levelScene.killedCreaturesByFireBall;
}

public int getKillsByStomp()
{
    return mario.levelScene.killedCreaturesByStomp;
}

public int getKillsByShell()
{
    return mario.levelScene.killedCreaturesByShell;
}

public int[] getMarioState()
{
    marioState[0] = this.getMarioStatus();
    marioState[1] = this.getMarioMode();
    marioState[2] = this.isMarioOnGround() ? 1 : 0;
    marioState[3] = this.isMarioAbleToJump() ? 1 : 0;
    marioState[4] = this.isMarioAbleToShoot() ? 1 : 0;
    marioState[5] = this.isMarioCarrying() ? 1 : 0;
    marioState[6] = this.getKillsTotal();
    marioState[7] = this.getKillsByFire();
    marioState[8] = this.getKillsByStomp();
    marioState[9] = this.getKillsByStomp();
    marioState[10] = this.getKillsByShell();
    marioState[11] = this.getTimeLeft();
    return marioState;
}

public void performAction(boolean[] action)
{
    // might look ugly , but arrayCopy is not necessary here:
    this.mario.keys = action;
}

public boolean isLevelFinished()
{
    return mario.getStatus() != Mario.STATUS_RUNNING;
}

public boolean isMarioAbleToShoot()
{
    return mario.isCanShoot();
}

public int getMarioStatus()
{
    return mario.getStatus();
}

public float[] getSerializedFullObservationZZ(int ZLevelScene, int ZLevelEnemies)
{
    // TODO:SK, serialize all data to a sole double[]
    assert false;
    return new float[0];
}

public int[] getSerializedLevelSceneObservationZ(int ZLevelScene)
{
    // serialization into arrays of primitive types to speed up the data transfer.
    byte[][] levelScene = this.getLevelSceneObservationZ(ZLevelScene);
    for (int i = 0; i < serializedLevelScene.length; ++i)
    {
        final int i1 = i / receptiveFieldWidth;
        final int i2 = i % receptiveFieldWidth;
        serializedLevelScene[i] = (int) levelScene[i1][i2];
    }
    return serializedLevelScene;
}

public int[] getSerializedEnemiesObservationZ(int ZLevelEnemies)
{
    // serialization into arrays of primitive types to speed up the data transfer.
    byte[][] enemies = this.getEnemiesObservationZ(ZLevelEnemies);
    for (int i = 0; i < serializedEnemies.length; ++i)
        serializedEnemies[i] = (int) enemies[i / receptiveFieldWidth][i % receptiveFieldWidth];
    return serializedEnemies;
}

public int[] getSerializedMergedObservationZZ(int ZLevelScene, int ZLevelEnemies)
{
    // serialization into arrays of primitive types to speed up the data transfer.
    byte[][] merged = this.getMergedObservationZZ(ZLevelScene, ZLevelEnemies);
    for (int i = 0; i < serializedMergedObservation.length; ++i)
        serializedMergedObservation[i] = (int) merged[i / receptiveFieldWidth][i % receptiveFieldWidth];
    return serializedMergedObservation;
}

/**
 * first and second elements of the array are x and y Mario coordinates correspondingly
 *
 * @return an array of size 2*(number of creatures on screen) including mario
 */
public float[] getCreaturesFloatPos()
{
    float[] enemies = this.getEnemiesFloatPos();
    float ret[] = new float[enemies.length + 2];
    System.arraycopy(this.getMarioFloatPos(), 0, ret, 0, 2);
    System.arraycopy(enemies, 0, ret, 2, enemies.length);
    return ret;
}

public boolean isMarioOnGround()
{ return mario.isOnGround(); }

public boolean isMarioAbleToJump()
{ return mario.mayJump(); }


public void resetDefault()
{
    // TODO: set values to defaults
    reset(CmdLineOptions.getDefaultOptions());
}

public void reset(CmdLineOptions cmdLineOptions)
{
//        System.out.println("\nLevelScene RESET!");
//        this.gameViewer = setUpOptions[0] == 1;
//        System.out.println("this.mario.isMarioInvulnerable = " + this.mario.isMarioInvulnerable);
//    this.levelDifficulty = cmdLineOptions.getLevelDifficulty();
//        System.out.println("this.levelDifficulty = " + this.levelDifficulty);
//    this.levelLength = cmdLineOptions.getLevelLength();
//        System.out.println("this.levelLength = " + this.levelLength);
//    this.levelSeed = cmdLineOptions.getLevelRandSeed();
//        System.out.println("levelSeed = " + levelSeed);
//    this.levelType = cmdLineOptions.getLevelType();
//        System.out.println("levelType = " + levelType);


    GlobalOptions.FPS = cmdLineOptions.getFPS();
//        System.out.println("GlobalOptions.FPS = " + GlobalOptions.FPS);
    GlobalOptions.isPowerRestoration = cmdLineOptions.isPowerRestoration();
//        System.out.println("GlobalOptions.isPowerRestoration = " + GlobalOptions.isPowerRestoration);
    GlobalOptions.isPauseWorld = cmdLineOptions.isPauseWorld();
//        System.out.println("GlobalOptions = " + GlobalOptions.isPauseWorld);
//        GlobalOptions.isTimer = cmdLineOptions.isTimer();
//        System.out.println("GlobalOptions.isTimer = " + GlobalOptions.isTimer);
//        isToolsConfigurator = setUpOptions[11] == 1;
    this.setTimeLimit(cmdLineOptions.getTimeLimit());
//        System.out.println("this.getTimeLimit() = " + this.getTimeLimit());
//        this.isViewAlwaysOnTop() ? 1 : 0, setUpOptions[13]
    this.visualization = cmdLineOptions.isVisualization();
//        System.out.println("visualization = " + visualization);

    receptiveFieldWidth = cmdLineOptions.getReceptiveFieldWidth();
    receptiveFieldHeight = cmdLineOptions.getReceptiveFieldHeight();
    killedCreaturesTotal = 0;
    killedCreaturesByFireBall = 0;
    killedCreaturesByStomp = 0;
    killedCreaturesByShell = 0;

    if (receptiveFieldHeight != this.prevRFH || receptiveFieldWidth != this.prevRFW)
    {
        serializedLevelScene = new int[receptiveFieldHeight * receptiveFieldWidth];
        serializedEnemies = new int[receptiveFieldHeight * receptiveFieldWidth];
        serializedMergedObservation = new int[receptiveFieldHeight * receptiveFieldWidth];

        levelSceneZ = new byte[receptiveFieldHeight][receptiveFieldWidth];
        enemiesZ = new byte[receptiveFieldHeight][receptiveFieldWidth];
        mergedZZ = new byte[receptiveFieldHeight][receptiveFieldWidth];
        this.prevRFH = this.receptiveFieldHeight;
        this.prevRFW = this.receptiveFieldWidth;
    }

    marioInitialPos = cmdLineOptions.getMarioInitialPos();

    //open replayer file, read level, close file
    String replayFileName = cmdLineOptions.getReplayFileName();
    if (!replayFileName.equals(""))
    {
        try
        {
            //TODO: fix it! replay is opened twice: here and in ReplayTask). Reduce to a single open.
            Replayer replayer = new Replayer(replayFileName);
            replayer.openFile("level.lvl");
            level = (Level) replayer.readObject();
//            replayer.closeFile();
            replayer.closeZip();
        } catch (IOException e)
        {
            //TODO: describe this exceptions
            e.printStackTrace();
        } catch (Exception e)
        {
            //TODO
            e.printStackTrace();
        }
    } else
        level = LevelGenerator.createLevel(cmdLineOptions);

    String fileName = cmdLineOptions.getLevelFileName();
    if (!fileName.equals(""))
    {
        try
        {
            System.out.println("fileName = " + fileName);
            Level.save(level, new ObjectOutputStream(new FileOutputStream(fileName)));
        } catch (IOException e)
        {
            System.err.println("[Mario AI WARNING] : Cannot write to file " + fileName);
            e.printStackTrace();
        }
    }
    this.levelSeed = level.randomSeed;
    this.levelLength = level.length;
    this.levelHeight = level.height;
    this.levelType = level.type;
    this.levelDifficulty = level.difficulty;

    paused = false;
    Sprite.spriteContext = this;
    sprites.clear();
    this.width = GlobalOptions.VISUAL_COMPONENT_WIDTH;
    this.height = GlobalOptions.VISUAL_COMPONENT_HEIGHT;

    Sprite.setCreaturesGravity(cmdLineOptions.getCreaturesGravity());
    Mario.resetStatic(cmdLineOptions);
    mario = new Mario(this);

    sprites.add(mario);
    startTime = 1;
    timeLeft = timeLimit * 15;

    tick = 0;
}

public float[] getMarioFloatPos()
{
    marioFloatPos[0] = this.mario.x;
    marioFloatPos[1] = this.mario.y;
    return marioFloatPos;
}

public int getMarioMode()
{ return mario.getMode(); }

public boolean isMarioCarrying()
{ return mario.carried != null; }

public int getLevelDifficulty()
{ return levelDifficulty; }

public long getLevelSeed()
{ return levelSeed; }

public int getLevelLength()
{ return levelLength; }

public int getLevelHeight()
{ return levelHeight; }

public int getLevelType()
{ return levelType; }

public int getReceptiveFieldWidth()
{
    return receptiveFieldWidth;
}

public int getReceptiveFieldHeight()
{
    return receptiveFieldHeight;
}

public void addMemoMessage(final String memoMessage)
{
    memo += memoMessage;
}

public Point getMarioInitialPos() {return marioInitialPos;}
}

//    public void update(boolean[] action)
//    {
//        System.arraycopy(action, 0, mario.keys, 0, 6);
//    }
