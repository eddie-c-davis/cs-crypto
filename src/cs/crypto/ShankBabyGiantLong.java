package cs.crypto;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by edavis on 10/2/16.
 */
public class ShankBabyGiantLong {
    private Long h;
    private Long g;
    private Long p;

    public ShankBabyGiantLong(Long h, Long g, Long p) {
        this.h = h;
        this.g = g;
        this.p = p;
    }

    public long run() {
        Long x = 0L;
        Long bI = 0L;
        Long gI = 0L;

        Map<Long, Long> bSteps = new HashMap<>();
        Map<Long, Long> gSteps = new HashMap<>();

        Long m = (long) Math.ceil(Math.sqrt(p.doubleValue()));

        // Calculate baby steps and giant steps...
        boolean converged = false;
        for (long i = 0; i < m && !converged; i++) {
            // Compute baby step...
            Long g_i = (long) Math.pow(g.doubleValue(), (double) i);
            Long bStep = (h * g_i) % p;
            bSteps.put(bStep, i);
            bI = i;

            // Compute giant step...
            long j = i + 1;
            Long g_j = (long) Math.pow(g.doubleValue(), (double) (j * m));
            Long gStep = g_j % p;
            gSteps.put(gStep, i);
            gI = i;

            // Check for convergence...
            converged = (gSteps.containsKey(bStep));
            if (converged) {
                gI = gSteps.get(bStep);
            }
        }

        // Find the inverse (g^-m) a la extended euclidean...
        if (converged) {
            Long r = bI;
            Long mq = m * (gI + 1);
            x = mq - r;
        }

        return x;
    }
}
