package cs.crypto;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by edavis on 11/26/16.
 */
public class Bytes {
    private static Logger _log = Logger.getLogger(Bytes.class.getName());

    public static byte[] toBytes(String string) {
        //Base64.encode
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(string);

        return bytes;
    }

    public static byte[] toBytes(Serializable object) {
        return toBytes(object, false);
    }

    public static byte[] compress(byte[] inBytes) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzOut = new GZIPOutputStream(bos);
        gzOut.write(inBytes, 0, inBytes.length);
        gzOut.finish();
        byte[] outBytes = bos.toByteArray();
        bos.close();

        return outBytes;
    }

    public static byte[] decompress(byte[] inBytes) throws ClassNotFoundException, IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(inBytes);
        GZIPInputStream gzIn = new GZIPInputStream(bis);
        ObjectInputStream in = new ObjectInputStream(gzIn);
        byte[] outBytes = (byte[]) in.readObject();
        in.close();

        return outBytes;
    }

    public static byte[] toBytes(Serializable object, boolean compressed) {
        byte[] bytes = new byte[0];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;

        try {
            if (compressed) {
                GZIPOutputStream gzOut = new GZIPOutputStream(bos);
                out = new ObjectOutputStream(gzOut);
            } else {
                out = new ObjectOutputStream(bos);
            }

            out.writeObject(object);
            out.flush();
            bytes = bos.toByteArray();
            bos.close();
        } catch (IOException ex) {
            throw new RequestException(ex);
        } finally {
            return bytes;
        }
    }

    public static Object fromBytes(byte[] bytes) {
        return fromBytes(bytes, false);
    }

    public static Object fromBytes(byte[] bytes, boolean compressed) {
        Object object = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in;

        try {
            if (compressed) {
                GZIPInputStream gzIn = new GZIPInputStream(bis);
                in = new ObjectInputStream(gzIn);
            } else {
                in = new ObjectInputStream(bis);
            }

            object = in.readObject();
            in.close();
        } catch (IOException ex) {      // Log I/O errors and continue...
            _log.warn(ex.toString());
        } finally {
            return object;
        }
    }

    public static Object fromString(String string) {
        return fromBytes(toBytes(string));
    }

    public static String toString(byte[] bytes) {
        Base64.Encoder encoder = Base64.getEncoder();
        String string = encoder.encodeToString(bytes);

        return string;
    }

    public static boolean equals(byte[] b1, byte[] b2) {
        boolean equal = (b1.length == b2.length);
        if (equal) {
            for (int i = 0; i < b1.length && equal; i++) {
                equal = (b1[i] == b2[i]);
            }
        }

        return equal;
    }
}
