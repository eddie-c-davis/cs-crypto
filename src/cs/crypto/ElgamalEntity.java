package cs.crypto;

import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

/**
 * Created by edavis on 10/4/16.
 */
public class ElgamalEntity implements ElgamalCipher {
    protected static final int DEFAULT_BITLEN = 128;
    protected static final int DEFAULT_CERTAINTY = 90;

    protected boolean _useSAM = false;
    protected boolean _initialized = false;

    protected String _name;
    protected int _bitLen;
    protected PrimeGenerator _pGen;

    protected BigInteger _gen;
    protected BigInteger _prime;
    protected BigInteger _kPr;
    protected BigInteger _kPub;

    public ElgamalEntity() {
        this("");
    }

    public ElgamalEntity(int bitLen) {
        this("", bitLen);
    }

    public ElgamalEntity(String name) {
        this(name, DEFAULT_BITLEN);
    }

    public ElgamalEntity(String name, int bitLen) {
        _name = name;
        _bitLen = bitLen;
        _pGen = new PrimeGenerator(_bitLen, DEFAULT_CERTAINTY);
    }

    public ElgamalEntity(String name, BigInteger g, BigInteger h, BigInteger p) {
        _name = name;
        _gen = g;
        _prime = p;
        _bitLen =  p.bitLength();
        _kPr = h;
    }

    public void init() {
        if (_prime == null) {
            _prime = _pGen.getP();
        }
        if (_gen == null) {
            _gen = _pGen.getG();
        }
        if (_kPr == null) {
            _kPr = _pGen.getH();
        }

        _kPub = _gen.modPow(_kPr, _prime);

        _initialized = true;
    }

    public BigInteger[] encrypt(BigInteger m, BigInteger p, BigInteger g, BigInteger k) {
        BigInteger one = BigInteger.ONE;
        BigInteger pm1 = p.subtract(one);
        BigInteger r = (new RandomBigInt(one, pm1)).get();

        BigInteger c1;
        BigInteger secKey;

        if (_useSAM) {
            c1 = MyBigInt.squareAndMultiply(g, r, p);
            secKey = MyBigInt.squareAndMultiply(k, r, p);
        } else {
            c1 = g.modPow(r, p);
            secKey = k.modPow(r, p);
        }

        BigInteger c2 = m.multiply(secKey).mod(_prime);

        return new BigInteger[] {c1, c2};
    }

    public BigInteger decrypt(BigInteger[] c) {
        BigInteger c1 = c[0];
        BigInteger c2 = c[1];

        BigInteger secKey; // = c1.modPow(_kPr, _prime);
        if (_useSAM) {
            secKey = MyBigInt.squareAndMultiply(c1, _kPr, _prime);
        } else {
            secKey = c1.modPow(_kPr, _prime);
        }

        BigInteger secInv = secKey.modInverse(_prime);
        BigInteger m = c2.multiply(secInv).mod(_prime);

        return m;
    }

    protected BigInteger privateKey() {
        return _kPr;
    }

    public BigInteger publicKey() {
        return _kPub;
    }

    public boolean initialized() {
        return _initialized;
    }

    public String getName() {
        return _name;
    }

    public BigInteger generator() {
        return _gen;
    }

    public BigInteger prime() {
        return _prime;
    }

    public void setSAM(boolean sam) {
        _useSAM = sam;
    }
}
