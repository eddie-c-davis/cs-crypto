package cs.crypto;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Base64;

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
        byte[] bytes = new byte[0];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ObjectOutput out = new ObjectOutputStream(bos);
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
        Object object = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        try {
            ObjectInput in = new ObjectInputStream(bis);
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
