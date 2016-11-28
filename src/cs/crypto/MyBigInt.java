package cs.crypto;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by edavis on 10/10/16.
 */
public class MyBigInt {
    private BigInteger _inst;

    public MyBigInt() {
        _inst = BigInteger.ZERO;
    }

    public MyBigInt(String valStr) {
        _inst = new BigInteger(valStr);
    }

    public MyBigInt(int lilInt) {
        _inst = BigInteger.valueOf(lilInt);
    }

    public MyBigInt(BigInteger bigInt) {
        _inst = bigInt;
    }

    public BigInteger toBigInt() {
        return _inst;
    }

    public String toString() {
        return _inst.toString();
    }

    public String toString(int radix) {
        return _inst.toString(radix);
    }

    public double doubleValue() {
        return _inst.doubleValue();
    }

    public MyBigInt pow(int exp) {
        return pow(BigInteger.valueOf(exp));
    }

    public MyBigInt pow(MyBigInt exp) {
        return pow(exp.toBigInt());
    }

    public MyBigInt pow(BigInteger exp) {
        return modPow(exp, BigInteger.ONE);
    }

    public MyBigInt modPow(MyBigInt exp, MyBigInt mod) {
        return modPow(exp.toBigInt(), mod.toBigInt());
    }

    public MyBigInt modPow(BigInteger exp, BigInteger mod) {
        BigInteger res = squareAndMultiply(_inst, exp, mod);

        return new MyBigInt(res);
    }

    public MyBigInt add(MyBigInt other) {
        return new MyBigInt(_inst.add(other.toBigInt()));
    }

    public MyBigInt subtract(MyBigInt other) {
        return new MyBigInt(_inst.subtract(other.toBigInt()));
    }

    public MyBigInt mod(MyBigInt other) {
        return new MyBigInt(_inst.mod(other.toBigInt()));
    }

    public MyBigInt multiply(MyBigInt other) {
        return new MyBigInt(_inst.multiply(other.toBigInt()));
    }

    public static MyBigInt valueOf(long val) {
        return new MyBigInt(BigInteger.valueOf(val));
    }

    public static BigInteger squareAndMultiply(BigInteger base, long exp) {
        return squareAndMultiply(base, BigInteger.valueOf(exp));
    }

    public static BigInteger squareAndMultiply(BigInteger base, BigInteger exp) {
        return squareAndMultiply(base, exp, BigInteger.ONE);
    }

    public static BigInteger squareAndMultiply(BigInteger base, BigInteger exp, BigInteger mod) {
        // Initialize...
        BigInteger res = base;

        // Start at MSB...
        String bits = exp.toString(2);     // Convert exponent to bit string...
        for (int i = 1; i < bits.length(); i++) {
            // Square...
            res = res.multiply(res);

            if (bits.charAt(i) == '1') {
                res = res.multiply(base);
            }

            if (mod.compareTo(BigInteger.ONE) > 0 && res.compareTo(mod) >= 0) {
                res = res.mod(mod);
            }
        }

        return res;
    }

    public static BigInteger parse(String input) {
        BigInteger bigI = BigInteger.ZERO;

        if (input.length() > 0) {
            int base = 10;
            char lastChar = input.charAt(input.length() - 1);

            if (lastChar == 'b' || lastChar == 'h' || lastChar == 'o') {
                input = input.substring(0, input.length() - 1);

                switch (lastChar) {
                    case 'b':
                        base = 2;
                        break;
                    case 'h':
                        input = input.toLowerCase();
                        base = 16;
                        break;
                    case 'o':
                        base = 8;
                        break;
                    default:
                        base = 10;
                        break;
                }
            }

            bigI = new BigInteger(input, base);
        }

        return bigI;
    }

    public static BigInteger encode(String message, BigInteger prime) throws MessageException {
        return encode(message, prime, false);
    }

    public static BigInteger encode(String message, BigInteger prime, boolean compressed) throws MessageException {
        byte[] mBytes = message.getBytes();
        //byte[] mBytes = Bytes.toBytes(message);

        BigInteger m = (new BigInteger(1, mBytes)).mod(prime);
        if (compressed) {
            mBytes = Bytes.toBytes(m, true);
            m = (new BigInteger(1, mBytes)).mod(prime);
//            try {
//                mBytes = Bytes.compress(mBytes);
//            } catch (IOException ioe) {
//                throw new MessageException(ioe.toString());
//            }
        }

        if (m.compareTo(prime) > 0) {
            throw new MessageException("Message exceeds prime size.");
        }

        return m;
    }

    public static String decode(BigInteger msgBigInt) {
        return decode(msgBigInt, false);
    }

    public static String decode(BigInteger msgBigInt, boolean compressed) {
        //String message = Bytes.toString(msgBigInt.toByteArray());
        byte[] bytes = msgBigInt.toByteArray();
        if (compressed) {
            msgBigInt = (BigInteger) Bytes.fromBytes(bytes, compressed);
            bytes = msgBigInt.toByteArray();
        }

        return new String(bytes);
    }

    public static final MyBigInt ZERO = new MyBigInt(BigInteger.ZERO);
    public static final MyBigInt ONE = new MyBigInt(BigInteger.ONE);
    public static final MyBigInt TEN = new MyBigInt(BigInteger.TEN);
}
