package cs.crypto;

import java.util.HashMap;
import java.util.Map;
//import java.math.BigInteger;

/**
 * Created by edavis on 10/2/16.
 */
public class ShankBabyGiantMyBigInt {
    private MyBigInt h;
    private MyBigInt g;
    private MyBigInt p;

    public ShankBabyGiantMyBigInt(MyBigInt h, MyBigInt g, MyBigInt p) {
        this.h = h;
        this.g = g;
        this.p = p;
    }

    public MyBigInt run() {
        MyBigInt x = MyBigInt.ZERO;
        MyBigInt bI = MyBigInt.ZERO;
        MyBigInt gI = MyBigInt.ZERO;

        Map<MyBigInt, MyBigInt> bSteps = new HashMap<>();
        Map<MyBigInt, MyBigInt> gSteps = new HashMap<>();

        Long m = (long) Math.ceil(Math.sqrt(p.doubleValue()));

        // Calculate baby steps and giant steps...
        boolean converged = false;
        for (long i = 0; i < m && !converged; i++) {
            MyBigInt bigI = MyBigInt.valueOf(i);
            //System.out.println(bigI.toString());

            // Compute baby step...
            MyBigInt exp = g.pow((int) i);
            MyBigInt bStep = (h.multiply(exp)).mod(p);
            bSteps.put(bStep, bigI);
            bI = bigI;

            // Compute giant step...
            exp = MyBigInt.valueOf((i + 1) * m);
            MyBigInt gStep = g.modPow(exp, p);
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
            for (MyBigInt bStep : bSteps.keySet()) {
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
            MyBigInt r = bI;
            MyBigInt mq = (gI.add(MyBigInt.ONE)).multiply(MyBigInt.valueOf(m));
            x = mq.subtract(r);
        }

        return x;
    }
}
