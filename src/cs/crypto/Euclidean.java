package cs.crypto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edavis on 10/12/16.
 */
public class Euclidean {
    private BigInteger _r0;
    private BigInteger _r1;

    private BigInteger _r;
    private BigInteger _s;
    private BigInteger _t;

    public Euclidean(BigInteger a, BigInteger b) {
        if (a.compareTo(b) > 0) {
            _r0 = a;
            _r1 = b;
        } else {
            _r0 = b;
            _r1 = a;
        }

        _r = _s = _t = BigInteger.ZERO;
    }

    private void run() {
        int initCap = _r0.bitLength() / 2;
        BigInteger one = BigInteger.ONE;
        BigInteger zero = BigInteger.ZERO;

        List<BigInteger> rList = new ArrayList<>(initCap);
        rList.add(_r0);
        rList.add(_r1);

        List<BigInteger> sList = new ArrayList<>(initCap);
        sList.add(one);
        sList.add(zero);

        List<BigInteger> tList = new ArrayList<>(initCap);
        tList.add(zero);
        tList.add(one);

        List<BigInteger> qList = new ArrayList<>(initCap);
        qList.add(zero);
        qList.add(zero);

        //r = [r0, r1]
        //s = [1, 0]
        //t = [0, 1]
        //q = [0, 0]

        int i = 1, j = 0, k = 0;
        BigInteger r_i = one;
        while (true) {
            i += 1;
            j = i - 1;
            k = i - 2;

            BigInteger r_im2 = rList.get(k);
            BigInteger r_im1 = rList.get(j);

            r_i = r_im2.mod(r_im1);
            if (r_i.equals(zero)) {
                break;
            }

            BigInteger q_im1 = (r_im2.subtract(r_i)).divide(r_im1);

            BigInteger s_i = (sList.get(k).subtract(q_im1)).multiply(sList.get(j));
            BigInteger t_i = (tList.get(k).subtract(q_im1)).multiply(tList.get(j));

            qList.add(q_im1);
            rList.add(r_i);
            sList.add(s_i);
            tList.add(t_i);
        }

        _r = rList.get(j);
        _s = sList.get(j);
        _t = tList.get(j);

        //return (r[im1], s[im1], t[im1])
    }

    public BigInteger getR() {
        if (_r.equals(BigInteger.ZERO)) {
            run();
        }

        return _r;
    }

    public BigInteger getS() {
        if (_s.equals(BigInteger.ZERO)) {
            run();
        }

        return _s;
    }

    public BigInteger getT() {
        if (_t.equals(BigInteger.ZERO)) {
            run();
        }

        return _t;
    }
}
