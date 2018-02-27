package ch.idsia.benchmark.mario.engine.sprites;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.MarioVisualComponent;
import ch.idsia.benchmark.mario.engine.level.SpriteTemplate;

import java.awt.*;

public class Sprite
{
public static final int KIND_NONE = 0;
public static final int KIND_MARIO = -31;
public static final int KIND_GOOMBA = 80;
public static final int KIND_GOOMBA_WINGED = 95;
public static final int KIND_RED_KOOPA = 82;
public static final int KIND_RED_KOOPA_WINGED = 97;
public static final int KIND_GREEN_KOOPA = 81;
public static final int KIND_GREEN_KOOPA_WINGED = 96;
public static final int KIND_BULLET_BILL = 84;
public static final int KIND_SPIKY = 93;
public static final int KIND_SPIKY_WINGED = 99;
//    public static final int KIND_ENEMY_FLOWER = 11;
public static final int KIND_ENEMY_FLOWER = 91;
public static final int KIND_SHELL = 13;
public static final int KIND_MUSHROOM = 2;
public static final int KIND_FIRE_FLOWER = 3;
public static final int KIND_PARTICLE = 21;
public static final int KIND_SPARCLE = 22;
public static final int KIND_COIN_ANIM = 1;
public static final int KIND_FIREBALL = 25;

public static final int KIND_UNDEF = -42;

public static SpriteContext spriteContext;
public byte kind = KIND_UNDEF;

protected static float GROUND_INERTIA = 0.89f;
protected static float AIR_INERTIA = 0.89f;

public float xOld, yOld, x, y, xa, ya;
public int mapX, mapY;

public int xPic, yPic;
public int wPic = 32;
public int hPic = 32;
public int xPicO, yPicO;
public boolean xFlipPic = false;
public boolean yFlipPic = false;
public Image[][] sheet;
public Image[][] prevSheet;

public boolean visible = true;

public int layer = 1;

public SpriteTemplate spriteTemplate;


public static void setCreaturesGravity(final float creaturesGravity)
{
    Sprite.creaturesGravity = creaturesGravity;
}

protected static float creaturesGravity;

public void move()
{
    x += xa;
    y += ya;
}

public void render(Graphics og)
{
    if (!visible) return;

//        int xPixel = (int)(xOld+(x-xOld)*cameraOffSet)-xPicO;
//        int yPixel = (int)(yOld+(y-yOld)*cameraOffSet)-yPicO;

    int xPixel = (int) x - xPicO;
    int yPixel = (int) y - yPicO;

//        System.out.print("xPic = " + xPic);
//        System.out.print(", yPic = " + yPic);
//        System.out.println(", kind = " + this.kind);

    try
    {
        og.drawImage(sheet[xPic][yPic],
                xPixel + (xFlipPic ? wPic : 0),
                yPixel + (yFlipPic ? hPic : 0),
                xFlipPic ? -wPic : wPic,
                yFlipPic ? -hPic : hPic, null);
    } catch (ArrayIndexOutOfBoundsException ex)
    {
        System.err.println("ok:" + this.kind + ", " + xPic);
    }
    // Labels
    if (GlobalOptions.areLabels)
        og.drawString("" + xPixel + "," + yPixel, xPixel, yPixel);

    // Mario Grid Visualization Enable
    if (GlobalOptions.isShowReceptiveField)
    {
        if (this.kind == KIND_MARIO)
        {
//                og.drawString("M", (int) x, (int) y);
            og.drawString("Matrix View", xPixel - 40, yPixel - 20);
            int width = GlobalOptions.receptiveFieldWidth * 16;
            int height = GlobalOptions.receptiveFieldHeight * 16;

            int rows = GlobalOptions.receptiveFieldHeight;
            int columns = GlobalOptions.receptiveFieldWidth;

            int htOfRow = 16;//height / (columns);
            int k;
            // horizontal lines
            og.setColor(Color.BLACK);
            for (k = -rows / 2 - 1; k <= rows / 2; k++)
                og.drawLine((int) x - width / 2, (int) (y + k * htOfRow), (int) (x + width / 2), (int) (y + k * htOfRow));

//                og.setColor(Color.RED);
            // vertical lines
            int wdOfRow = 16;// length / (rows);
            for (k = -columns / 2 - 1; k < columns / 2 + 1; k++)
                og.drawLine((int) (x + k * wdOfRow + 8), (int) y - height / 2 - 8, (int) (x + k * wdOfRow + 8), (int) (y + height / 2 - 8));
        }
        og.setColor(Color.GREEN);
        MarioVisualComponent.drawString(og, String.valueOf(this.kind), (int) x - 4, (int) y - 8, 2);
    }
}

public final void tick()
{
    xOld = x;
    yOld = y;
    move();
    mapY = (int) (y / 16);
    mapX = (int) (x / 16);
}

public final void tickNoMove()
{
    xOld = x;
    yOld = y;
}

//    public float getX(float alpha)
//    {
//        return (xOld+(x-xOld)*alpha)-xPicO;
//    }
//
//    public float getY(float alpha)
//    {
//        return (yOld+(y-yOld)*alpha)-yPicO;
//    }

public void collideCheck()
{
}

public void bumpCheck(int xTile, int yTile)
{
}

public boolean shellCollideCheck(Shell shell)
{
    return false;
}

public void release(Mario mario)
{
}

public boolean fireballCollideCheck(Fireball fireball)
{
    return false;
}
}