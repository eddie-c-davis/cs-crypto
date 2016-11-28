package cs.crypto;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edavis on 11/28/16.
 */
public class Files
{
    private static String DEFAULT_ENCODING = "utf-8";

    public static boolean exists(String path)
    {
        return new File(path).exists();
    }

    public static boolean delete(String path)
    {
        File file = new File(path);
        boolean deleted = (file.exists() && file.canWrite());
        if (deleted)
        {
            deleted = file.delete();
        }

        return deleted;
    }

    public static long length(String path)
    {
        long len = 0L;

        File file = new File(path);
        if (file.exists())
        {
            len = file.length();
        }

        return len;
    }

    public static long lastModified(String path)
    {
        long lastModTime = 0L;

        File file = new File(path);
        if (file.exists())
        {
            lastModTime = file.lastModified();
        }

        return lastModTime;
    }

    public static String getName(String path)
    {
        String name = "";

        int index = path.lastIndexOf('/');
        if (index >= 0)
        {
            name = path.substring(index + 1);
        }

        return name;
    }

    public static String getNameWithoutExt(String path) {
        return getPathWithoutExt(getName(path));
    }

    public static String getPathWithoutExt(String path) {
        int index = path.lastIndexOf('.');
        if (index >= 0)
        {
            path = path.substring(0, index);
        }

        return path;
    }

    public static String getDir(String path)
    {
        String dir = path;

        int index = path.lastIndexOf('/');
        if (index >= 0)
        {
            dir = path.substring(0, index);
        }

        return dir;
    }

    public static List<String> list(String path) throws IOException {
        List<String> results;

        File dir = new File(path);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            results = new ArrayList<>(files.length);

            for (File file : files) {
                results.add(file.getPath());
            }
        } else {
            results = new ArrayList<>();
        }

        return results;
    }

    public static List<String> read(String path) throws IOException
    {
        return java.nio.file.Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
    }

    public static void write(String path, String text) throws IOException
    {
        write(path, text, DEFAULT_ENCODING);
    }

    public static void write(String path, String text, String encoding) throws IOException
    {
        String dir = getDir(path);
        File file = new File(dir);
        if (!file.exists())
        {
            file.mkdirs();
        }

        FileOutputStream fos = new FileOutputStream(path);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, encoding));
        writer.write(text);
        writer.close();
    }
}
