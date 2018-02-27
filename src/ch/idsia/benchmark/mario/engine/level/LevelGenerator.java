package ch.idsia.benchmark.mario.engine.level;

import ch.idsia.benchmark.mario.engine.sprites.Sprite;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.RandomCreatureGenerator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;

/**
 * This class is simple to use. Just call <b>createLevel</b> method with params:
 * <ul>
 * CmdLineOptions args, that contains: ... TODO
 * <p/>
 * <li>length -- length of the level in cells. One cell is 16 pixels long</li>
 * <li>height -- height of the level in cells. One cell is 16 pixels long </li>
 * <li>seed -- use this param to make a globalRandom level.
 * On different machines with the same seed param there will be one level</li>
 * <li>levelDifficulty -- use this param to change difficult of the level.
 * On different machines with the same seed param there will be one level</li>
 * <li>levelType -- levelType of the level. One of Overground, Underground, Castle.</li>
 * </ul>
 *
 * @see #TYPE_OVERGROUND
 * @see #TYPE_UNDERGROUND
 * @see #TYPE_CASTLE
 */

public class LevelGenerator
{
public static final int TYPE_OVERGROUND = 0;
public static final int TYPE_UNDERGROUND = 1;
public static final int TYPE_CASTLE = 2;

public static final int DEFAULT_FLOOR = -1;

public static final int LevelLengthMinThreshold = 50; // minimal length of the level. used in ToolsConfigurator
private static boolean isFlatLevel;

private static int length;
private static int height;
private static Level level;

private static Random globalRandom = new Random(0);
private static RandomCreatureGenerator creaturesRandom = new RandomCreatureGenerator(0, "", 0);
public static Random dxRnd = new Random(0); //used in addEnemy to compute dx

private static final int ODDS_STRAIGHT = 0;
private static final int ODDS_HILL_STRAIGHT = 1;
private static final int ODDS_TUBES = 2;
private static final int ODDS_GAPS = 3;
private static final int ODDS_CANNONS = 4;
private static final int ODDS_DEAD_ENDS = 5;
private static int[] odds = new int[6];
private static int totalOdds;
private static int levelDifficulty;
private static int levelType;
private static int levelSeed;

private static final int ANY_HEIGHT = -1;
private static final int INFINITE_FLOOR_HEIGHT = Integer.MAX_VALUE;

//Level customization counters
static Level.objCounters counters = new Level.objCounters();

private LevelGenerator() {}

private static void loadLevel(String filePath)
{    try
    {
        if (filePath.equals("")) //This must never happen
        {
            System.err.println("[MarioAI ERROR] : file path to the level is empty");
            System.exit(-3);
        }

        level = Level.load(new ObjectInputStream(new FileInputStream(filePath)));
    } catch (IOException e)
    {
        System.err.println("[MarioAI EXCEPTION] : failed while trying to load " + filePath);
        System.exit(-3); //TODO: move all error codes to constants
    } catch (ClassNotFoundException e)
    {
        System.err.println("[MarioAI EXCEPTION] : class not found in " + filePath);
        System.exit(-3);
    }
}

public static Level createLevel(CmdLineOptions args)
{
    try
    {
        levelSeed = args.getLevelRandSeed();
    } catch (Exception e)
    {
        loadLevel(args.getParameterValue("-ls"));
        return level;
    }
    length = args.getLevelLength();
    height = args.getLevelHeight();
    if (height < 15)
    {
        System.err.println("[Mario AI WARNING] : Level height changed to minimal allowed value 15");
        height = 15;
    }
    isFlatLevel = args.isFlatLevel();
    counters.totalHillStraight = args.getHillStraightCount() ? Integer.MAX_VALUE : 0;
    counters.totalCannons = args.getCannonsCount() ? Integer.MAX_VALUE : 0;
    counters.totalGaps = args.getGapsCount() ? Integer.MAX_VALUE : 0;
    counters.totalDeadEnds = args.getDeadEndsCount() ? Integer.MAX_VALUE : 0;
    counters.totalBlocks = args.getBlocksCount() ? Integer.MAX_VALUE : 0;
    counters.totalHiddenBlocks = args.getHiddenBlocksCount() ? Integer.MAX_VALUE : 0;
    counters.totalCoins = args.getCoinsCount() ? Integer.MAX_VALUE : 0;
    counters.totalTubes = args.getTubesCount() ? Integer.MAX_VALUE : 0;

    levelType = args.getLevelType();
    levelDifficulty = args.getLevelDifficulty();
    odds[ODDS_STRAIGHT] = 20;
    odds[ODDS_HILL_STRAIGHT] = 1;
    odds[ODDS_TUBES] = 2 + 1 * levelDifficulty;
    odds[ODDS_GAPS] = 3 * levelDifficulty;
    odds[ODDS_CANNONS] = -10 + 5 * levelDifficulty;
    odds[ODDS_DEAD_ENDS] = 2 + 2 * levelDifficulty;

    if (levelType != LevelGenerator.TYPE_OVERGROUND)
        odds[ODDS_HILL_STRAIGHT] = 0; //no hill straight in TYPE_OVERGROUND level

    totalOdds = 0;
    for (int i = 0; i < odds.length; i++)
    {
        if (odds[i] < 0) odds[i] = 0;
        totalOdds += odds[i];
        odds[i] = totalOdds - odds[i];
    }

    level = new Level(length, height);
//    levelSeed = args.getLevelRandSeed();// + levelType; // TODO: ensure the difference of underground, castle
    globalRandom.setSeed(levelSeed);
    creaturesRandom.setSeed(levelSeed, args.getEnemies(), levelDifficulty);
    dxRnd.setSeed(levelSeed);

    int currentLength = 0; //total level currentLength so far

    //by default mario supposed to start on a straight surface
    int floor = DEFAULT_FLOOR;
    if (isFlatLevel)
        floor = height - 1 - globalRandom.nextInt(12);

    currentLength += buildStraight(0, level.length, true, floor, INFINITE_FLOOR_HEIGHT);
    while (currentLength < level.length - 10)
    {
//        System.out.println("level.currentLength - currentLength = " + (level.currentLength - currentLength));
        currentLength += buildZone(currentLength, level.length - currentLength, ANY_HEIGHT, floor, INFINITE_FLOOR_HEIGHT);
    }

    if (!isFlatLevel)  //NOT flat level
        floor = height - 1 - globalRandom.nextInt(12); //floor of the exit line

    //coordinates of the exit
    level.xExit = level.length;
    level.yExit = floor;
    level.randomSeed = levelSeed;
    level.type = levelType;
    level.difficulty = levelDifficulty;

    //level zone where exit is located
    for (int x = currentLength; x < level.length; x++)
    {
        for (int y = 0; y < height; y++)
        {
            if (y >= floor)
            {
                level.setBlock(x, y, (byte) (1 + 9 * 16));
            }
        }
    }

    //if underground or castle then build ceiling
    if (levelType == LevelGenerator.TYPE_CASTLE || levelType == LevelGenerator.TYPE_UNDERGROUND)
    {
        int ceiling = 0;
        int run = 0;
        for (int x = 0; x < level.length; x++)
        {
            if (run-- <= 0 && x > 4)
            {
                ceiling = globalRandom.nextInt(4);
                run = globalRandom.nextInt(4) + 4;
            }
            for (int y = 0; y < level.height; y++)
            {
                if ((x > 4 && y <= ceiling) || x < 1)
                {
                    level.setBlock(x, 0, (byte) (1 + 9 * 16));
                }
            }
        }
    }

    fixWalls();

    level.counters = counters;

    return level;
}

private static int buildZone(int x, int maxLength, int maxHeight, int floor, int floorHeight)
{
//    System.out.println("buildZone maxLength = " + maxLength);
    int t = globalRandom.nextInt(totalOdds);
    int type = 0;
    //calculate what will be built
    for (int i = 0; i < odds.length; i++)
    {
        if (odds[i] <= t)
        {
            type = i;
        }
    }

    int length = 0;

    switch (type)
    {
        case ODDS_STRAIGHT:
            length = buildStraight(x, maxLength, false, floor, floorHeight);
            break;
        case ODDS_HILL_STRAIGHT:
            if (floor == DEFAULT_FLOOR && counters.hillStraightCount < counters.totalHillStraight)
            {
                counters.hillStraightCount++;
//                length = buildHillStraight(x, maxLength, floor, false);
                length = buildHill(x, true, maxLength, floor, false);
            } else
                length = 0;
            break;
        case ODDS_TUBES:
            if (counters.tubesCount < counters.totalTubes)
                length = buildTubes(x, maxLength, maxHeight, floor, floorHeight);
            else
                length = 0;
            break;
        case ODDS_GAPS:
            if ((floor > 2 || floor == ANY_HEIGHT) && (counters.gapsCount < counters.totalGaps))
            {
                counters.gapsCount++;
                length = buildGap(x, maxLength, maxHeight, floor, floorHeight);
            } else
                length = 0;
            break;
        case ODDS_CANNONS:
            if (counters.cannonsCount < counters.totalCannons)
                length = buildCannons(x, maxLength, maxHeight, floor, floorHeight);
            else
                length = 0;
            break;
        case ODDS_DEAD_ENDS:
        {
            if (floor == DEFAULT_FLOOR && counters.deadEndsCount < counters.totalDeadEnds) //if method was not called from buildDeadEnds
            {
                counters.deadEndsCount++;
                length = buildDeadEnds(x, maxLength);
            }
        }
    }

    int crCount = 0;
//    for (int y = level.height - 3; y > levelDifficulty + 1; --y)
//    {
//        addEnemy(x, y);
//        ++crCount;
//    }
    for (int yy = level.height; yy > 0; yy--)
        if (level.getBlock(x, yy) == 0 &&
                creaturesRandom.nextInt(levelDifficulty + 1) + 1 > (levelDifficulty + 1) / 2 &&
                crCount < levelDifficulty + 1 &&
                level.getSpriteTemplate(x, yy) == null)
        {
            addEnemy(x, yy);
            ++crCount;
        }

    return length;
}

public static void addEnemy(int x, int y)
{
    if (!creaturesRandom.canAdd())
        return;

    int dx = (int) dxRnd.nextGaussian();
    int creatureKind = creaturesRandom.nextCreature();
    if (creatureKind != Sprite.KIND_UNDEF)
    {
        if (level.setSpriteTemplate(x + dx, y, new SpriteTemplate(creatureKind)))
            ++counters.creatures;
        else
            creaturesRandom.increaseLastCreature();
    }
}

//x0 - first block to start from
//maxLength - maximal length of the zone

private static int buildDeadEnds(int x0, int maxLength)
{
    //first of all build pre dead end zone
    int floor = height - 2 - globalRandom.nextInt(2);  //floor of pre dead end zone
    int length = 0; // total zone length
    int preDeadEndLength = 7 + globalRandom.nextInt(10);
    int rHeight = floor - 1; //rest height

    length += buildStraight(x0, preDeadEndLength, true, floor, INFINITE_FLOOR_HEIGHT);//buildZone( x0, x0+preDeadEndLength, floor ); //build pre dead end zone
    buildBlocks(x0, x0 + preDeadEndLength, floor, true, 0, 0, true, true);

    //correct direction
    //true - top, false = bottom
    globalRandom.nextInt();
    int k = globalRandom.nextInt(5);//(globalRandom.nextInt() % (this.levelDifficulty+1));
    boolean direction = globalRandom.nextInt(k + 1) != 1;

    int separatorY = 3 + globalRandom.nextInt(rHeight - 7); //Y coordinate of the top line of the separator
    //Y coordinate of the bottom line of the separator is determined as separatorY + separatorHeight
    int separatorHeight = 2 + globalRandom.nextInt(2);

    int nx = x0 + length;
    int depth = globalRandom.nextInt(levelDifficulty + 1) + 2 * (1 + levelDifficulty);
    if (depth + length > maxLength)
    {
//        depth = maxLength
        while (depth + length > maxLength - 1)
        {
            depth--;
        }
    }

    int tLength = 0;
    int bSpace = floor - (separatorY + separatorHeight);
    if (bSpace < 4)
    {
        while (bSpace < 4)
        {
            separatorY -= 1;
            bSpace = floor - (separatorY + separatorHeight);
        }
    }

    int wallWidth = 2 + globalRandom.nextInt(3);

    while (tLength < depth) //top part
    {
        tLength += buildZone(nx + tLength, depth - tLength, separatorY - 1, separatorY, separatorHeight);
    }
    tLength = 0;
    while (tLength < depth) //bottom part
    {
        tLength += buildZone(nx + tLength, depth - tLength, bSpace, floor, INFINITE_FLOOR_HEIGHT);
    }

    for (int x = nx; x < nx + depth; x++)
    {
        for (int y = 0; y < height; y++)
        {
            if (x - nx >= depth - wallWidth)
            {
                if (direction == globalRandom.nextBoolean()) //wall on the top
                {
                    if (y <= separatorY)// + separatorHeight )
                    {
                        level.setBlock(x, y, (byte) (1 + 9 * 16));
                    }
                } else
                {
                    if (y >= separatorY)
                    {
                        level.setBlock(x, y, (byte) (1 + 9 * 16));
                    }
                }
            }
        }
    }

    return length + tLength;
}

private static int buildGap(int xo, int maxLength, int maxHeight, int vfloor, int floorHeight)
{
    int gs = globalRandom.nextInt(5) + 2; //GapStairs
    int gl = globalRandom.nextInt(levelDifficulty) + levelDifficulty > 7 ? 10 : 3;//globalRandom.nextInt(2) + 2; //GapLength
    int length = gs * 2 + gl;

    if (length > maxLength)
        length = maxLength;

    boolean hasStairs = globalRandom.nextInt(3) == 0;
    if (isFlatLevel || (maxHeight <= 5 && maxHeight != ANY_HEIGHT))
    {
        hasStairs = false;
    }

    int floor = vfloor;
    if (vfloor == DEFAULT_FLOOR && !isFlatLevel)
    {
        floor = height - 1 - globalRandom.nextInt(4);
    } else //code in this block is a magic. don't change it
    {
        floor++;
//        globalRandom.nextInt();
        if (floor > 1)
        {
            floor -= 1;
        }
    }

    if (floorHeight == INFINITE_FLOOR_HEIGHT)
    {
        floorHeight = height - floor;
    }

//
//    if (gs > 3 && creaturesRandom.nextInt(35) > levelDifficulty + 1 && !hasStairs)
//    {
////        addEnemiesLine(xo, xo + gs - 1, floor - 1);
////        addEnemiesLine(xo + length - gs, xo + length - 1, floor - 1);
//    }

    for (int x = xo; x < xo + length; x++)
    {
        if (x < xo + gs || x > xo + length - gs - 1)
        {
            for (int y = 0; y < height; y++)
            {
                if (y >= floor && y <= floor + floorHeight)
                    level.setBlock(x, y, (byte) (1 + 9 * 16));
                else if (hasStairs)
                {
                    if (x < xo + gs)
                    {
                        if (y >= floor - (x - xo) + 1 && y <= floor + floorHeight)
                            level.setBlock(x, y, (byte) (9 + 0 * 16));
                    } else if (y >= floor - ((xo + length) - x) + 2 && y <= floor + floorHeight)
                        level.setBlock(x, y, (byte) (9 + 0 * 16));
                }
            }
        }
    }
    if (gl > 8)
    {
        buildHill(xo + gs + globalRandom.nextInt(Math.abs((gl - 4)) / 2 + 1), false, 3, floor, true);
    }

    return length;
}

private static int buildCannons(int xo, int maxLength, int maxHeight, int vfloor, int floorHeight)
{
    int maxCannonHeight = 0;
    int length = globalRandom.nextInt(10) + 2;
    if (length > maxLength) length = maxLength;

    int floor = vfloor;
    if (vfloor == DEFAULT_FLOOR)
    {
        floor = height - 1 - globalRandom.nextInt(4);
    } else
    {
        globalRandom.nextInt();
    }

    if (floorHeight == INFINITE_FLOOR_HEIGHT)
    {
        floorHeight = height - floor;
    }

    int oldXCannon = -1;

    int xCannon = xo + 1 + globalRandom.nextInt(4);
    for (int x = xo; x < xo + length; x++)
    {
        if (x > xCannon)
        {
            xCannon += 2 + globalRandom.nextInt(4);
            counters.cannonsCount++;
        }
        if (xCannon == xo + length - 1)
        {
            xCannon += 10;
        }

        int cannonHeight = floor - globalRandom.nextInt(3) - 1; //cannon height is a Y coordinate of top part of the cannon
        if (maxHeight != ANY_HEIGHT)
        {
            //maxHeight -= 2;
            if (floor - cannonHeight >= maxHeight)
            {
                if (maxHeight > 4)
                {
                    maxHeight = 4;
                }
                while (floor - cannonHeight > maxHeight)
                {
                    cannonHeight++;
                }
            }
            if (cannonHeight > maxCannonHeight)
                maxCannonHeight = cannonHeight;
        }

        for (int y = 0; y < height; y++)
        {
            if (y >= floor && y <= floor + floorHeight)
            {
                level.setBlock(x, y, (byte) (1 + 9 * 16));
            } else if (counters.cannonsCount <= counters.totalCannons)
            {
                if (x == xCannon && y >= cannonHeight && y <= floor)// + floorHeight)
                {
                    if (y == cannonHeight)
                    {
                        if (oldXCannon != -1 && creaturesRandom.nextInt(35) > levelDifficulty + 1)
                        {
//                            addEnemiesLine(oldXCannon + 1, xCannon - 1, floor - 1);
                        }
                        oldXCannon = x;
                        level.setBlock(x, y, (byte) (14 + 0 * 16));   // cannon barrel
                    } else if (y == cannonHeight + 1)
                    {
                        level.setBlock(x, y, (byte) (14 + 1 * 16));   // base for cannon barrel
                    } else
                    {
                        level.setBlock(x, y, (byte) (14 + 2 * 16));   // cannon pole
                    }
                }
            }
        }
    }

    if (globalRandom.nextBoolean())
        buildBlocks(xo, xo + length, floor - maxCannonHeight - 2, false, 0, 0, false, false);

    return length;
}

private static int buildHill(int x0, boolean withStraight, int maxLength, int vfloor, boolean isInGap)
{
    int length = globalRandom.nextInt(10) + 10;
    if (length > maxLength)
    {
        length = maxLength;
    }

    int floor = vfloor;
    if (vfloor == DEFAULT_FLOOR)
    {
        floor = height - 1 - globalRandom.nextInt(4);
    }

    if (withStraight)
    {
        for (int x = x0; x < x0 + length; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (y >= floor)
                {
                    level.setBlock(x, y, (byte) (1 + 9 * 16));
                }
            }
        }
    }

    boolean canBuild = true;

    int top = floor;
    if (isInGap)
        floor = level.height;
    while (canBuild)
    {
        top -= isFlatLevel ? 0 : (globalRandom.nextInt(2) + 2);
        if (top < 0)
            canBuild = false;
        else
        {
            int l = globalRandom.nextInt(length / 2) + 1;
            int xx0 = globalRandom.nextInt(l + 1) + x0;

            if (globalRandom.nextInt(4) == 0)
            {
                decorate(xx0 - 1, xx0 + l + 1, top);
                canBuild = false;
            }
            for (int x = xx0; x < xx0 + l; x++)
            {
                for (int y = top; y < floor; y++)
                {
                    int xx = 5;
                    if (x == xx0) xx = 4;
                    if (x == xx0 + l - 1) xx = 6;
                    int yy = 9;
                    if (y == top) yy = 8;

                    if (level.getBlock(x, y) == 0)
                    {
                        level.setBlock(x, y, (byte) (xx + yy * 16));
                    } else
                    {
                        if (level.getBlock(x, y) == (byte) (4 + 8 * 16))
                            level.setBlock(x, y, (byte) (4 + 11 * 16));
                        if (level.getBlock(x, y) == (byte) (6 + 8 * 16))
                            level.setBlock(x, y, (byte) (6 + 11 * 16));
                    }
                }
            }
            addEnemy(xx0, top - 1);
        }
    }

    return length;
}

private static int buildTubes(int xo, int maxLength, int maxHeight, int vfloor, int floorHeight)
{
    int maxTubeHeight = 0;
    int length = globalRandom.nextInt(10) + 5;
    if (length > maxLength) length = maxLength;

    int floor = vfloor;
    if (vfloor == DEFAULT_FLOOR)
    {
        floor = height - 1 - globalRandom.nextInt(4);
    } else
    {
        globalRandom.nextInt();
    }
    int xTube = xo + 1 + globalRandom.nextInt(4);

    int tubeHeight = floor - globalRandom.nextInt(3) - 1;

    if (maxHeight != ANY_HEIGHT)
    {
        //maxHeight -= 2;
        if (floor - tubeHeight > maxHeight)
        {
            if (maxHeight > 4)
            {
                maxHeight = 4;
            }
            while (floor - tubeHeight > maxHeight)
            {
                tubeHeight++;
            }
        }
    }

    if (floorHeight == INFINITE_FLOOR_HEIGHT)
    {
        floorHeight = height - floor;
    }

    int oldXTube = -1;

    for (int x = xo; x < xo + length; x++)
    {
        if (x > xTube + 1)
        {
            xTube += 3 + globalRandom.nextInt(4);
            tubeHeight = floor - globalRandom.nextInt(2) - 2;
            if (maxHeight != ANY_HEIGHT)
            {
                while (floor - tubeHeight > maxHeight - 1)
                {
                    tubeHeight++;
                }
            }

            if (tubeHeight > maxTubeHeight)
                maxTubeHeight = tubeHeight;
        }
        if (xTube >= xo + length - 2)
        {
            xTube += 10;
        }

        if (x == xTube && globalRandom.nextInt(11) < levelDifficulty + 1 && creaturesRandom.isCreatureEnabled("f"))
        {
            level.setSpriteTemplate(x, tubeHeight, new SpriteTemplate(Sprite.KIND_ENEMY_FLOWER));
            ++counters.creatures;
        }

        for (int y = 0; y < floor + floorHeight; y++)
        {
            if (y >= floor && y <= floor + floorHeight)
                level.setBlock(x, y, (byte) (1 + 9 * 16));
            else
            {
                if ((x == xTube || x == xTube + 1) && y >= tubeHeight)
                {

                    int xPic = 10 + x - xTube;
                    if (y == tubeHeight)
                    {
                        level.setBlock(x, y, (byte) (xPic + 0 * 16));
                        if (x == xTube)
                        {
                            if (oldXTube != -1 && creaturesRandom.nextInt(35) > levelDifficulty + 1)
                            {
//                                addEnemiesLine(oldXTube + 2, xTube - 1, floor - 1);
                            }
                            oldXTube = x;
                            counters.tubesCount++;
                        }
                    } else
                    {
                        level.setBlock(x, y, (byte) (xPic + 1 * 16));
                    }
                }
            }
        }
    }

    if (globalRandom.nextBoolean())
        buildBlocks(xo, xo + length, floor - maxTubeHeight - 2, false, 0, 0, false, false);

    return length;
}

// parameter safe should be set to true iff length of the Straight > 10.
// minimal length = 2
//floorHeight - height of the floor. used for building of the top part of the dead end separator

private static int buildStraight(int xo, int maxLength, boolean safe, int vfloor, int floorHeight)
{
    int length;
    if (floorHeight != INFINITE_FLOOR_HEIGHT)
    {
        length = maxLength;
    } else
    {
        length = globalRandom.nextInt(8) + 2;//globalRandom.nextInt(50)+1) + 2;
        if (safe) length = 10 + globalRandom.nextInt(5);
        if (length > maxLength) length = maxLength;
    }


    int floor = vfloor;
    if (vfloor == DEFAULT_FLOOR)
    {
        floor = height - 1 - globalRandom.nextInt(4);
    } else
    {
        globalRandom.nextInt();
    }

    int y1 = height;
    if (floorHeight != INFINITE_FLOOR_HEIGHT)
    {
        y1 = floor + floorHeight;
    }

    for (int x = xo; x < xo + length; x++)
        for (int y = floor; y < y1; y++)
            if (y >= floor)
                level.setBlock(x, y, (byte) (1 + 9 * 16));

    if (!safe)
    {
        if (length > 5)
        {
            decorate(xo, xo + length, floor);
        }
    }

    return length;
}

private static boolean canBuildBlocks(int x0, int floor, boolean isHB)
{
    if ((counters.blocksCount >= counters.totalBlocks && !isHB))
    {
        return false;
    }

    boolean res = true;

//    if (floor < 1)
//    {
//        return false;
//    }

//    for (int y = 0; y < 1; y++)
//    {
//        if (level.getBlock(x0, floor - y) != 0)
//        {
//            res = false;
//            break;
//        }
//    }

    return res;
}

private static void buildBlocks(int x0, int x1, int floor, boolean pHB, int pS, int pE, boolean onlyHB, boolean isDistance)
{
    if (counters.blocksCount > counters.totalBlocks)
    {
        return;
    }
    int s = pS; //Start
    int e = pE; //End
    boolean hb = pHB;

    if (onlyHB)
        hb = onlyHB;

    --floor;
    while (floor > 0) //minimal distance between the bricks line and floor is 4
    {
        if ((x1 - 1 - e) - (x0 + 1 + s) > 0) //minimal number of bricks in the line is positive value
        {
            for (int x = x0 + s; x < x1 - e; x++)
            {
                if (hb && counters.totalHiddenBlocks != 0) //if hidden blocks to be built
                {
                    boolean isBlock = globalRandom.nextInt(2) == 1;
                    if (isBlock && canBuildBlocks(x, floor - 4, true))
                    {
                        level.setBlock(x, floor - 4, (byte) (1)); //a hidden block with a coin
                        counters.hiddenBlocks++;
                        ++counters.coinsCount;
                    }
                } else
                {
                    boolean canDeco = false; //can add enemy line and coins
                    //decorate( x0, x1, floor, true );
                    if (x != x0 + 1 && x != x1 - 2 && globalRandom.nextInt(3) == 0)
                    {
                        if (canBuildBlocks(x, floor - 4, false))
                        {
                            counters.blocksCount++;
                            if ((globalRandom.nextInt(4) == 0))
                            {
                                if (level.getBlock(x, floor) == 0)
                                    level.setBlock(x, floor, (byte) (4 + 2 + 1 * 16)); //a brick with animated question symbol with power up. when broken becomes a rock
                            } else
                            {
                                if (level.getBlock(x, floor) == 0)
                                {
                                    level.setBlock(x, floor, (byte) (4 + 1 + 1 * 16)); //a brick with animated question symbol with coin. when broken becomes a rock
                                    ++counters.coinsCount;
                                }
                            }
                            canDeco = true;
                        }
                    } else if (globalRandom.nextInt(4) == 0)
                    {
                        if (canBuildBlocks(x, floor - 4, false))
                        {
                            counters.blocksCount++;
                            if (globalRandom.nextInt(4) == 0)
                            {
                                if (level.getBlock(x, floor) == 0)
                                    level.setBlock(x, floor, (byte) (2 + 1 * 16)); //a brick with a power up. when broken becomes a rock
                            } else
                            {
                                if (level.getBlock(x, floor) == 0)
                                {
                                    level.setBlock(x, floor, (byte) (1 + 1 * 16)); //a brick with a coin. when broken becomes a rock
                                    ++counters.coinsCount;
                                }
                            }
                            canDeco = true;
                        }
                    } else if (globalRandom.nextInt(2) == 1 && canBuildBlocks(x, floor - 4, false))
                    {
                        if (level.getBlock(x, floor) == 0)
                        {
                            counters.blocksCount++; //TODO: move it in to the Level.setBlock
                            level.setBlock(x, floor, (byte) (0 + 1 * 16)); //a break brick
                            canDeco = true;
                        }
                    }
                    if (canDeco)
                    {
                        //if (creaturesRandom.nextInt(35) > levelDifficulty + 1) addEnemiesLine(x0 + 1, x1 - 1, floor - 1);
                        buildCoins(x0, x1, floor, s, e);
                    }
                }
            }
            if (onlyHB)
            {
                hb = true;
            } else
            {
                hb = globalRandom.nextInt(4) == 0;//globalRandom.nextInt(3) == globalRandom.nextInt(3);
            }
        }

//        if (creaturesRandom.nextInt(35) > levelDifficulty + 1)
//            addEnemiesLine(x0 + 1, x1 - 1, floor - 1);

        floor -= isDistance ? 4 : globalRandom.nextInt(6) + 3;
        s = globalRandom.nextInt(4);
        e = globalRandom.nextInt(4);
    }
    globalRandom.nextBoolean();
}

private static void buildCoins(int x0, int x1, int floor, int s, int e)
{
    if (floor - 2 < 0) return;

    if ((x1 - 1 - e) - (x0 + 1 + s) > 1)
    {
        for (int x = x0 + 1 + s; x < x1 - 1 - e; x++)
        {
            if (counters.coinsCount >= counters.totalCoins)
            {
                break;
            }
            if (level.getBlock(x, floor - 2) == 0) //if cell (x, floor-2) is empty
            {
                counters.coinsCount++;
                level.setBlock(x, floor - 2, (byte) (2 + 2 * 16)); //coin
            }
        }
    }
}

private static void decorate(int x0, int x1, int floor)
{
    if (floor < 1) return;

    int s = globalRandom.nextInt(4);
    int e = globalRandom.nextInt(4);
    boolean hb = ((globalRandom.nextInt(levelDifficulty + 1) % (levelDifficulty + 1))) > 0.5;

//    if (!hb)
//    {
//        addEnemiesLine(x0 + 1, x1 - 1, floor - 1);
//    }

    if (floor - 2 > 0 && !hb)
    {
        buildCoins(x0, x1, floor, s, e);
    }

    buildBlocks(x0, x1, floor, hb, s, e, false, false);
}

private static void fixWalls()
{
    boolean[][] blockMap = new boolean[length + 1][height + 1];
    for (int x = 0; x < length + 1; x++)
    {
        for (int y = 0; y < height + 1; y++)
        {
            int blocks = 0;
            for (int xx = x - 1; xx < x + 1; xx++)
            {
                for (int yy = y - 1; yy < y + 1; yy++)
                {
                    if (level.getBlockCapped(xx, yy) == (byte) (1 + 9 * 16)) blocks++;
                }
            }
            blockMap[x][y] = blocks == 4;
        }
    }
    blockify(level, blockMap, length + 1, height + 1);
}

private static void blockify(Level level, boolean[][] blocks, int width, int height)
{
    int to = 0;
    if (levelType == LevelGenerator.TYPE_CASTLE)
        to = 4 * 2;
    else if (levelType == LevelGenerator.TYPE_UNDERGROUND)
        to = 4 * 3;

    boolean[][] b = new boolean[2][2];
    for (int x = 0; x < width; x++)
    {
        for (int y = 0; y < height; y++)
        {
            for (int xx = x; xx <= x + 1; xx++)
            {
                for (int yy = y; yy <= y + 1; yy++)
                {
                    int _xx = xx;
                    int _yy = yy;
                    if (_xx < 0) _xx = 0;
                    if (_yy < 0) _yy = 0;
                    if (_xx > width - 1) _xx = width - 1;
                    if (_yy > height - 1) _yy = height - 1;
                    b[xx - x][yy - y] = blocks[_xx][_yy];
                }
            }

            if (b[0][0] == b[1][0] && b[0][1] == b[1][1])
            {
                if (b[0][0] == b[0][1])
                {
                    if (b[0][0])
                    {
                        level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                    } else
                    {
                        // KEEP OLD BLOCK!
                    }
                } else
                {
                    if (b[0][0])
                    {
                        level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
                    } else
                    {
                        level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
                    }
                }
            } else if (b[0][0] == b[0][1] && b[1][0] == b[1][1])
            {
                if (b[0][0])
                {
                    level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
                } else
                {
                    level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
                }
            } else if (b[0][0] == b[1][1] && b[0][1] == b[1][0])
            {
                level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
            } else if (b[0][0] == b[1][0])
            {
                if (b[0][0])
                {
                    if (b[0][1])
                    {
                        level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
                    } else
                    {
                        level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
                    }
                } else
                {
                    if (b[0][1])
                    {
                        level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
                    } else
                    {
                        level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
                    }
                }
            } else if (b[0][1] == b[1][1])
            {
                if (b[0][1])
                {
                    if (b[0][0])
                    {
                        level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
                    } else
                    {
                        level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
                    }
                } else
                {
                    if (b[0][0])
                    {
                        level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
                    } else
                    {
                        level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
                    }
                }
            } else
            {
                level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
            }
        }
    }
}

//private static int buildHillStraight(int xo, int maxLength, int vfloor, final boolean isInGap)
//{
////    System.out.println("xo = " + xo);
//    int length = globalRandom.nextInt(10) + 10;
//    if (length > maxLength)
//    {
//        length = maxLength;
//    }
///*        if( maxLength < 10 )
//        {
//            return 0;
//        }
//*/
//    int floor = vfloor;
//    if (vfloor == DEFAULT_FLOOR)
//    {
//        floor = height - 1 - globalRandom.nextInt(4);
//    }
////    if (isInGap)
////        floor = level.height;
//
//    if (!isInGap)
//    {
//        for (int x = xo; x < xo + length; x++)
//        {
//            for (int y = 0; y < height; y++)
//            {
//                if (y >= floor)
//                {
//                    level.setBlock(x, y, (byte) (1 + 9 * 16));
//                }
//            }
//        }
//    }
//
////    addEnemiesLine(xo + 1, xo + length - 1, floor - 1);
//
////    int h = level.height;
////    if (isFlatLevel)
////        floor = level.height;
////    int h = isInGap ? level.height : floor;
//    int h = floor;
//
//    boolean keepGoing = true;
//
//    boolean[] occupied = new boolean[length];
//    while (keepGoing)
//    {
//        if (isFlatLevel)
//            h = vfloor;
//        else
//            h = h - 2 - globalRandom.nextInt(3);
//
//        if (h <= 0)
//        {
//            keepGoing = false;
//        } else
//        {
//            int l = globalRandom.nextInt(maxLength) + 1;
//            //int xxo = globalRandom.nextInt(length - l - 2 + 1) + xo + 1;
//            int xxo = globalRandom.nextInt(l + 1) + xo;
//
//            if (false)//occupied[xxo - xo] || occupied[xxo - xo + l] || occupied[xxo - xo - 1] || occupied[xxo - xo + l])
//            {
//                keepGoing = false;
//            } else
//            {
////                occupied[xxo - xo] = true;
////                occupied[xxo - xo + l] = true;
////                addEnemiesLine(xxo, xxo + l, h - 1);
//                if (globalRandom.nextInt(4) == 0)
//                {
//                    decorate(xxo - 1, xxo + l + 1, h);
//                    keepGoing = false;
//                }
//                for (int x = xxo; x < xxo + l; x++)
//                {
//                    for (int y = h; y < floor; y++)
//                    {
//                        int xx = 5;
//                        if (x == xxo) xx = 4;
//                        if (x == xxo + l - 1) xx = 6;
//                        int yy = 9;
//                        if (y == h) yy = 8;
//
//                        if (level.getBlock(x, y) == 0)
//                        {
//                            level.setBlock(x, y, (byte) (xx + yy * 16));
//                        } else
//                        {
//                            if (level.getBlock(x, y) == (byte) (4 + 8 * 16))
//                                level.setBlock(x, y, (byte) (4 + 11 * 16));
//                            if (level.getBlock(x, y) == (byte) (6 + 8 * 16))
//                                level.setBlock(x, y, (byte) (6 + 11 * 16));
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    return length;
//}

//private static boolean canAddEnemyLine(int x0, int x1, int y)
//{
//    if (!creaturesMaskParser.canAdd())
//    {
//        return false;
//    }
//    boolean res = true;
//    for (int x = x0; x < x1; x++)
//    {
//        for (int yy = y; yy > y + 1; yy++)
//        {
//            if (level.getBlock(x, yy) != 0)
//            {
//                res = false;
//                break;
//            }
//        }
//    }
//
//    return res;
//}
//
//private static void addEnemiesLine(int x0, int x1, int y)
//{
//    if (x0 > 0)
//        return;
//
//    if (!canAddEnemyLine(x0, x1, y))
//        return;
//
//    for (int x = x0; x < x1; x++)
//    {
//        if (creaturesRandom.nextInt(25) < levelDifficulty + 1)
//        {
//            if (creaturesMaskParser.isComplete())
//            { //Difficulty of creatures on the level depends on the levelDifficulty of the level
//                int type = creaturesRandom.nextInt(4);
//                if (levelDifficulty < 1)
//                {
//                    type = CreaturesMaskParser.GOOMBA;
//                } else if (levelDifficulty < 3)
//                {
//                    int type1 = creaturesRandom.nextInt(3);
//                    int type2 = creaturesRandom.nextInt(3) + 3;
//                    type = creaturesRandom.nextInt(2) == 1 ? type1 : type2;
//                }
//                type = creaturesMaskParser.getNativeType(type);
//                level.setSpriteTemplate(x, y, new SpriteTemplate(type));
//                ++counters.creatures;
//            } else
//            {
//                boolean enabled = false;
//                int crType;// = creaturesRandom.nextInt(4);
//                if (levelDifficulty < 3)
//                {
//                    creaturesRandom.nextInt(3);
//                }
//                do
//                {
//                    crType = creaturesRandom.nextInt(8);
//                    if (creaturesMaskParser.isEnabled(crType))
//                    {
//                        enabled = true;
//                    }
//                }
//                while (!enabled);
//
//                int t = creaturesMaskParser.getNativeType(crType);
//                level.setSpriteTemplate(x, y, new SpriteTemplate(t));
//                ++counters.creatures;
//            }
//        }
//    }
//}
}

/*
    first component of sum : position on  Y axis
    second component of sum : position  on X axis
    starting at 0
    *16 because size of the picture is 16x16 pixels
    0+9*16 -- left side of the ground
    1+9*16 -- upper side of ground; common block telling "it's smth (ground) here". Is processed further.
    2+9*16 -- right side of the earth
    3+9*16 -- peice of the earth
    9+0*16 -- block of a ladder
    14+0*16 -- cannon barrel
    14+1*16 -- base for cannon barrel
    14+2*16 -- cannon pole
    4+8*16 -- left piece of a hill of ground
    4+11*16 -- left piece of a hill of ground as well
    6+8*16 --  right upper peice of a hill
    6+11*16 -- right upper peice of a hill on earth
    2+2*16 --  animated coin
    4+2+1*16 -- a rock with animated question symbol with power up
    4+1+1*16 -- a rock with animated question symbol with coin
    2+1*16 -- brick with power up. when broken becomes a rock
    1+1*16 -- brick with power coin. when broken becomes a rock
    0+1*16 -- break brick
    1+10*16 -- earth, bottom piece
    1+8*16 --  earth, upper piece
    3+10*16 -- piece of earth
    3+11*16 -- piece of earth
    2+8*16 -- right part of earth
    0+8*16 -- left upper part of earth
    3+8*16 -- piece of earth
    2+10*16 -- right bottomp iece of earth
    0+10*16 -- left bottom piece of earth
*/