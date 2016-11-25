package cs.crypto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.List;

/**
 * Created by edavis on 11/24/16.
 */
public class BigMath {
    public static BigDecimal root(final int n, final BigDecimal x) {
        BigDecimal s = x;
        if (n > 1 && x.compareTo(BigDecimal.ZERO) > 0) {
            /* start the computation from a double precision estimate */
            s = new BigDecimal(Math.pow(x.doubleValue(), 1.0 / n));

            final BigDecimal nth = new BigDecimal(n);
            final BigDecimal xhighpr = scalePrec(x, 2);
            MathContext mc = new MathContext(2 + x.precision());

            /* Relative accuracy of the result is machine eps. */
            final double eps = x.ulp().doubleValue() / (2 * n * x.doubleValue());

            double diff = eps;
            while (diff >= eps) {
                BigDecimal c = xhighpr.divide(s.pow(n - 1), mc);
                c = s.subtract(c);
                MathContext locmc = new MathContext(c.precision());
                c = c.divide(nth, locmc);
                s = s.subtract(c);
                diff = Math.abs(c.doubleValue() / s.doubleValue());
            }

            s = s.round(new MathContext(errPrec(eps))) ;
        }

        return s;
    }

    private static BigDecimal scalePrec(final BigDecimal x, int d) {
        return x.setScale(d + x.scale());
    }

    private static int errPrec(double xerr) {
        return 1+(int)(Math.log10(Math.abs(0.5/xerr)));
    }

    public static BigInteger sum(List<BigInteger> numbers) {
        BigInteger sum = BigInteger.ZERO;
        for (BigInteger bigI : numbers) {
            sum = sum.add(bigI);
        }

        return sum;
    }

    public static BigInteger product(List<BigInteger> numbers) {
        BigInteger prod = BigInteger.ONE;
        for (BigInteger bigI : numbers) {
            prod = prod.multiply(bigI);
        }

        return prod;
    }
}
