package cs.crypto;

import java.math.BigInteger;

/**
 * Created by edavis on 10/22/16.
 */
public interface RSACipher {
    BigInteger encrypt(BigInteger m, BigInteger e, BigInteger n);
    BigInteger encrypt(BigInteger msg, BigInteger[] k);
    BigInteger[] encrypt(BigInteger[] m, BigInteger e, BigInteger n);
    BigInteger decrypt(BigInteger c);
    BigInteger[] decrypt(BigInteger[] c);
}
