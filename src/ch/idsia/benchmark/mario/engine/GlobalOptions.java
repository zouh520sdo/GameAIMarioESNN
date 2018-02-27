package ch.idsia.benchmark.mario.engine;

import ch.idsia.tools.GameViewer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class
        GlobalOptions
{
public static final int primaryVerionUID = 0;
public static final int minorVerionUID = 1;
public static final int minorSubVerionID = 9;

public static boolean areLabels = false;
public static boolean isCameraCenteredOnMario = false;
public static Integer FPS = 24;
public static int MaxFPS = 100;
public static boolean isPauseWorld = false;

public static boolean isVisualization = true;
public static boolean isGameplayStopped = false;
public static boolean isFly = false;

private static GameViewer GameViewer = null;
//    public static boolean isTimer = true;

public static boolean isPowerRestoration;

// required for rendering grid in ch/idsia/benchmark/mario/engine/sprites/Sprite.java
public static int receptiveFieldWidth = -1;
public static int receptiveFieldHeight = -1;

private static MarioVisualComponent marioVisualComponent;
public static int VISUAL_COMPONENT_WIDTH = 320;
public static int VISUAL_COMPONENT_HEIGHT = 240;

public static boolean isShowReceptiveField = false;
public static boolean isScale2x = false;

public static int getPrimaryVersionUID()
{
    return primaryVerionUID;
}

public static int getMinorVersionUID()
{
    return minorVerionUID;
}

public static int getMinorSubVerionID()
{
    return minorSubVerionID;
}

public static String getBenchmarkName()
{
    return "[~ Mario AI Benchmark ~" + GlobalOptions.getVersionUID() + "]";
}

public static String getVersionUID()
{
    return " " + getPrimaryVersionUID() + "." + getMinorVersionUID() + "." + getMinorSubVerionID();
}

public static void registerMarioVisualComponent(MarioVisualComponent mc)
{
    marioVisualComponent = mc;
}

public static void registerGameViewer(GameViewer gv)
{
    GameViewer = gv;
}

public static void AdjustMarioVisualComponentFPS()
{
    if (marioVisualComponent != null)
        marioVisualComponent.adjustFPS();
}

public static void gameViewerTick()
{
    if (GameViewer != null)
        GameViewer.tick();
}

public static String getDateTime(Long d)
{
    final DateFormat dateFormat = (d == null) ? new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:ms") :
            new SimpleDateFormat("HH:mm:ss:ms");
    if (d != null)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    final Date date = (d == null) ? new Date() : new Date(d);
    return dateFormat.format(date);
}

final static private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

public static String getTimeStamp()
{
    return dateFormat.format(new Date());
}

public static void changeScale2x()
{
    isScale2x = !isScale2x;
    marioVisualComponent.width *= isScale2x ? 2 : 0.5;
    marioVisualComponent.height *= isScale2x ? 2 : 0.5;
    marioVisualComponent.changeScale2x();
}
}
