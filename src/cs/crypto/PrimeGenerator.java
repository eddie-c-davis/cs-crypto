package cs.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import org.bouncycastle.crypto.generators.DHParametersGenerator;
import org.bouncycastle.crypto.params.DHParameters;

/**
 * Created by edavis on 10/3/16.
 */
public class PrimeGenerator {
    private static final int DEFAULT_SIZE = 32;
    private static final int DEFAULT_CERTAINTY = 90;

    private int nBits;

    private BigInteger g;
    private BigInteger h;
    private BigInteger p;
    private BigInteger q;

    private DHParameters dhParams;
    private SecureRandom rand;

    public PrimeGenerator() {
        this(DEFAULT_SIZE);
    }

    public PrimeGenerator(int size) {
        this(size, DEFAULT_CERTAINTY);
    }

    public PrimeGenerator(int size, int certainty) {
        this(size, certainty, new SecureRandom());
    }

    public PrimeGenerator(int size, int certainty, SecureRandom random) {
        DHParametersGenerator dhParamGen = new DHParametersGenerator();
        dhParamGen.init(size, certainty, random);
        rand = random;

        dhParams = dhParamGen.generateParameters();
        g = dhParams.getG();
        p = dhParams.getP();
        q = dhParams.getQ();
        h = BigInteger.ZERO;
        nBits = size;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getH() {
        if (h == BigInteger.ZERO) {
            BigInteger two = BigInteger.ONE.add(BigInteger.ONE);
            BigInteger pm2 = p.subtract(two);
            h = (new RandomBigInt(two, pm2, rand)).get();
        }

        return h;
    }

    public BigInteger getP() {
        return p;
    }
    public BigInteger getQ() {
        return q;
    }

    public Random getRand() { return rand; }
    public int getSize() { return nBits; }
}
