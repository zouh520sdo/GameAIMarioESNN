package ch.idsia.benchmark.mario.engine;

import ch.idsia.benchmark.mario.environments.Environment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstName_at_idsia_dot_ch
 * Date: May 5, 2009
 * Time: 9:34:33 PM
 * Package: ch.idsia.utils
 */
public class Replayer //TODO: auto add .zip extension
{
private ZipFile zf;
private ZipEntry ze;
private BufferedInputStream fis;

public Replayer(String fileName) throws IOException
{
    if (!fileName.endsWith(".zip"))
        fileName += ".zip";

    zf = new ZipFile(fileName);
}

public void openFile(String filename) throws Exception
{
    Enumeration e = zf.entries();
    boolean f = false;

    while (e.hasMoreElements())
    {
        ze = (ZipEntry) e.nextElement();
        if (ze.getName().equals(filename))
        {
            f = true;
            break;
        }
    }
    
    if (!f)
        throw new Exception("[Mario AI EXCEPTION] : File " + filename + " not found in the archive");
}

private void openInputStream() throws IOException
{
    fis = new BufferedInputStream(zf.getInputStream(ze));
}

public boolean[] readAction() throws IOException
{
    if (fis == null)
        openInputStream();

    boolean[] buffer = new boolean[Environment.numberOfButtons];
//    int count = fis.read(buffer, 0, size);
    byte actions = (byte) fis.read();
    for (int i = 0; i < Environment.numberOfButtons; i++)
    {
        if ((actions & ((1 << i))) > 0)
            buffer[i] = true;
        else
            buffer[i] = false;
    }

    if (actions == -1)
        buffer = null;

    return buffer;
}

public Object readObject() throws IOException, ClassNotFoundException
{
    ObjectInputStream ois = new ObjectInputStream(zf.getInputStream(ze));
    Object res = ois.readObject();
//    ois.close();

    return res;
}

public void closeFile() throws IOException
{
    fis.close(); //TODO: never used. fix it
}

public void closeZip() throws IOException
{
    zf.close();
}
}