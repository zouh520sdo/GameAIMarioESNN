package ch.idsia.tools;

import ch.idsia.benchmark.mario.engine.sprites.Sprite;

import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Sep 17, 2010
 * Time: 1:38:10 AM
 * Package: ch.idsia.tools
 */
public class RandomCreatureGenerator extends Random
{
private HashSet<String> allowedCreatures = new HashSet<String>();
private boolean creaturesEnabled = true;
private boolean kindByDifficulty = true;
private int difficulty = 0;
private final String[] kinds = {"g", "gw", "gk", "gkw", "rk", "rkw", "s", "sw"};
private String lastDecreased;

private final int INFINITE = -1;

private int GOOMBA = 0;
private int GOOMBA_WINGED = 1;
private int RED_KOOPA = 2;
private int RED_KOOPA_WINGED = 3;
private int GREEN_KOOPA = 4;
private int GREEN_KOOPA_WINGED = 5;
private int SPIKY = 6;
private int SPIKY_WINGED = 7;

private int[] counters = new int[9];


public RandomCreatureGenerator(long seed, String creatures, int difficulty)
{
    super(seed);
    this.setSeed(seed, creatures, difficulty);
}

public void setSeed(long seed, String creatures, int difficulty)
{
    this.setSeed(seed);
    this.difficulty = difficulty;

    if (creatures.equals("off"))
    {
        creaturesEnabled = false;
    } else
    {
        creaturesEnabled = true;

        if (!creatures.equals(""))
        {
            kindByDifficulty = false;
            Pattern pattern = Pattern.compile("[gsfr]k?w?(:\\d+)?");
            Matcher matcher = pattern.matcher(creatures);

            while (matcher.find())
            {
                String group = matcher.group();
                String[] subgroups = group.split(":");
                allowedCreatures.add(subgroups[0]);
                int index = getCreatureIndex(subgroups[0]);
                if (subgroups.length == 2)
                {
                    int count = Integer.valueOf (subgroups[1]);
                    counters[index] = count;
                } else
                {
                    counters[index] = Integer.MAX_VALUE;
                }
                //TODO: handle gw:0 situation
                //TODO: creatures type depends on difficulty, but number of creatures can be specified for each kind of creature.
            }
        } else
        {
            kindByDifficulty = true;
        }
    }
//    System.out.println(allowedCreatures.toString());
}

public boolean canAdd()
{
    return creaturesEnabled;
}

private int getCreatureType(String type)
{
    int kind = Sprite.KIND_UNDEF;
    try
    {
        switch (type.charAt(0))
        {
            case 'g':
                kind = Sprite.KIND_GOOMBA;
                if (type.charAt(1) == 'w')
                {
                    kind = Sprite.KIND_GOOMBA_WINGED;
                } else if (type.charAt(1) == 'k')
                {
                    kind = Sprite.KIND_GREEN_KOOPA;
                    if (type.charAt(2) == 'w')
                        kind = Sprite.KIND_GREEN_KOOPA_WINGED;
                }
                break;
            case 'r':
                kind = Sprite.KIND_RED_KOOPA;
                if (type.charAt(2) == 'w')
                    kind = Sprite.KIND_RED_KOOPA_WINGED;
                break;
            case 's':
                kind = Sprite.KIND_SPIKY;
                if (type.charAt(1) == 'w')
                    kind = Sprite.KIND_SPIKY_WINGED;
                break;
        }
    } catch (Exception e)
    {
        return kind;
    }

    return kind;
}

private int getCreatureIndex(String c)
{
    if (c.equals("g")) return GOOMBA;
    if (c.equals("gw")) return GOOMBA_WINGED;
    if (c.equals("gk")) return GREEN_KOOPA;
    if (c.equals("gkw")) return GREEN_KOOPA_WINGED;
    if (c.equals("rk")) return RED_KOOPA;
    if (c.equals("rkw")) return RED_KOOPA_WINGED;
    if (c.equals("s")) return SPIKY;
    if (c.equals("sw")) return SPIKY_WINGED;

    throw new ArrayStoreException("Unknown kind of the creature: " + c);
}

private void decreaseCreatureCounter(String c)
{
    lastDecreased = c;

    int left = --counters[getCreatureIndex(c)];

    if (left == 0)
        allowedCreatures.remove(c);
}

public int nextCreature()
{
    int kind = Sprite.KIND_UNDEF;

    if (allowedCreatures.size() == 0 && !kindByDifficulty)
        return kind;

    if (kindByDifficulty)
    {
        kind = getCreatureType (kinds[this.nextInt(8)]);
        if (difficulty < 1)
        {
            kind = this.getCreatureType("g");
        } else if (difficulty < 3)
        {
            String type = kinds[this.nextInt(6)];
            kind = this.getCreatureType(type);
        }
    } else
    {
        Object[] localKinds = allowedCreatures.toArray();
        String c = (String)localKinds[this.nextInt(localKinds.length)];
        kind = this.getCreatureType(c);
        decreaseCreatureCounter(c);
    }

    return kind;
}

public void increaseLastCreature()
{
    if (!kindByDifficulty)
    {
        int index = getCreatureIndex(lastDecreased);
        if (counters[index] == 0)
        {
            allowedCreatures.add(lastDecreased);
        }
        ++counters[index];
    }
}

public boolean isCreatureEnabled(String creature)
{
    return creature.equals("f") || allowedCreatures.contains(creature);
}
}
