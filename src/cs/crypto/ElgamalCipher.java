package cs.crypto;

import java.math.BigInteger;

/**
 * Created by edavis on 10/22/16.
 */
public interface ElgamalCipher {
    void init();
    BigInteger[] encrypt(BigInteger m, BigInteger p, BigInteger g, BigInteger k);
    BigInteger decrypt(BigInteger[] c);
}
