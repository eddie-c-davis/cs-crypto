package cs.crypto;

import java.util.HashMap;
import java.util.Map;
import java.math.BigInteger;

/**
 * Created by edavis on 10/2/16.
 */
public class ShankBabyGiantBigInt {
    private BigInteger h;
    private BigInteger g;
    private BigInteger p;

    private boolean useSAM;

    public ShankBabyGiantBigInt(BigInteger h, BigInteger g, BigInteger p) {
        this(h, g, p, false);
    }

    public ShankBabyGiantBigInt(BigInteger h, BigInteger g, BigInteger p, boolean sam) {
        this.h = h;
        this.g = g;
        this.p = p;
        useSAM = sam;
    }

    public BigInteger run() {
        BigInteger x = BigInteger.ZERO;
        BigInteger bI = BigInteger.ZERO;
        BigInteger gI = BigInteger.ZERO;

        Map<BigInteger, BigInteger> bSteps = new HashMap<>();
        Map<BigInteger, BigInteger> gSteps = new HashMap<>();

        Long m = (long) Math.ceil(Math.sqrt(p.doubleValue()));

        // Calculate baby steps and giant steps...
        boolean converged = false;
        for (long i = 0; i < m && !converged; i++) {
            BigInteger bigI = BigInteger.valueOf(i);

            // Compute baby step...
            //BigInteger exp = g.pow((int) i);
            //BigInteger exp = MyBigInt.squareAndMultiply(g, i);

            BigInteger exp;
            if (useSAM) {
                exp = MyBigInt.squareAndMultiply(g, i);
            } else {
                exp = g.pow((int) i);
            }

            BigInteger bStep = (h.multiply(exp)).mod(p);
            bSteps.put(bStep, bigI);
            bI = bigI;

            // Compute giant step...
            exp = BigInteger.valueOf((i + 1) * m);

            //BigInteger gStep = g.modPow(exp, p);
            //BigInteger gStep = MyBigInt.squareAndMultiply(g, exp, p);

            BigInteger gStep;
            if (useSAM) {
                gStep = MyBigInt.squareAndMultiply(g, exp, p);
            } else {
                gStep = g.modPow(exp, p);
            }

            gSteps.put(gStep, bigI);
            gI = bigI;

            // Check for convergence...
            converged = (gSteps.containsKey(bStep));
            if (converged) {
                gI = gSteps.get(bStep);
            }
        }

        // If steps did NOT converge during the primary loop, check all baby steps...
        if (!converged) {
            for (BigInteger bStep : bSteps.keySet()) {
                converged = gSteps.containsKey(bStep);
                if (converged) {
                    bI = bSteps.get(bStep);
                    gI = gSteps.get(bStep);
                    break;
                }
            }
        }

        // Find the inverse (g^-m) a la extended euclidean...
        if (converged) {
            BigInteger r = bI;
            BigInteger mq = (gI.add(BigInteger.ONE)).multiply(BigInteger.valueOf(m));
            x = mq.subtract(r);
        }

        return x;
    }
}
