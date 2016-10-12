package cs.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by edavis on 10/4/16.
 */
public class RandomBigInt {
    BigInteger _begin;
    BigInteger _end;
    Random _rand;

    public RandomBigInt(BigInteger end) {
        this(BigInteger.ZERO, end);
    }

    public RandomBigInt(BigInteger begin, BigInteger end) {
        this(begin, end, new SecureRandom());
    }

    public RandomBigInt(BigInteger begin, BigInteger end, Random rand) {
        _begin = begin;
        _end = end;
        _rand = rand;
    }

    public BigInteger get() {
        BigInteger result = BigInteger.ZERO;
        int bitLen = _end.bitLength();

        do {
            result = new BigInteger(bitLen, _rand);
        } while (result.compareTo(_begin) < 0 || result.compareTo(_end) > 0);

        return result;
    }
}
