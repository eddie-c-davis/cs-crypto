package cs.crypto;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by edavis on 11/23/16.
 */
public class AES {
    private static final int KEY_SIZE = 16;

    private static final String ENCODING = "UTF-8";
    private static final String CIPHER = "AES/ECB/PKCS5Padding";
    private static final String HASH = "SHA-1";

    private static SecretKeySpec _secretKey;
    private static byte[] _keyBytes;

    private static void setKey(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        setKey(key.getBytes(ENCODING));
    }

    private static void setKey(BigInteger key) throws NoSuchAlgorithmException {
        setKey(key.toByteArray());
    }

    private static void setKey(byte[] key) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance(HASH);
        _keyBytes = sha.digest(key);
        _keyBytes = Arrays.copyOf(_keyBytes, KEY_SIZE);
        _secretKey = new SecretKeySpec(key, CIPHER.substring(0, 3));
    }

    public static String encrypt(String message, String key) throws GeneralSecurityException, UnsupportedEncodingException {
        byte[] bytes = encrypt(message.getBytes(ENCODING), key.getBytes(ENCODING));

        return new String(bytes);
    }

    public static BigInteger encrypt(BigInteger message, BigInteger key) throws GeneralSecurityException {
        byte[] bytes = encrypt(message.toByteArray(), key.toByteArray());

        return new BigInteger(bytes);
    }

    public static byte[] encrypt(byte[] message, byte[] key)  throws GeneralSecurityException {
        setKey(key);

        return runCipher(message, Cipher.ENCRYPT_MODE);
        //return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
    }

    public static String decrypt(String encrypted, String key) throws GeneralSecurityException, UnsupportedEncodingException {
        byte[] bytes = decrypt(encrypted.getBytes(ENCODING), key.getBytes(ENCODING));

        return new String(bytes);
    }

    public static BigInteger decrypt(BigInteger encrypted, BigInteger key) throws GeneralSecurityException {
        byte[] bytes = decrypt(encrypted.toByteArray(), key.toByteArray());

        return new BigInteger(bytes);
    }

    public static byte[] decrypt(byte[] encrypted, byte[] key)  throws GeneralSecurityException {
        setKey(key);

        return runCipher(encrypted, Cipher.DECRYPT_MODE);
        //return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
    }

    private static byte[] runCipher(byte[] data, int mode) throws GeneralSecurityException  {
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(mode, _secretKey);

        return cipher.doFinal(data);
    }
}
