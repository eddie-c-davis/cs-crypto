package cs.crypto;

import java.math.BigInteger;
import javax.xml.bind.DatatypeConverter;

/**
 * Created by edavis on 10/4/16.
 */
public class ElgamalUser {
    private String _name;
    private int _bitLen;
    private PrimeGenerator _pGen;

    private BigInteger _gen;
    private BigInteger _prime;
    private BigInteger _kPr;
    private BigInteger _kPub;

    public ElgamalUser() {
        this("");
    }

    public ElgamalUser(int bitLen) {
        this("", bitLen);
    }

    public ElgamalUser(String name) {
        this(name, 20);
    }

    public ElgamalUser(String name, int bitLen) {
        _name = name;
        _bitLen = bitLen;
        _pGen = new PrimeGenerator(_bitLen, 100);
    }

    public void init() {
        _prime = _pGen.getP();
        _gen = _pGen.getG();
        _kPr = _pGen.getH();
        _kPub = _gen.modPow(_kPr, _prime);
    }

    public void send(ElgamalUser receiver, String message) {
        _prime = receiver.prime();
        _gen = receiver.generator();
        BigInteger pubKey = receiver.publicKey();

        BigInteger one = BigInteger.ONE;
        BigInteger pm1 = _prime.subtract(one);
        BigInteger r = (new RandomBigInt(one, pm1)).get();

        BigInteger c1 = _gen.modPow(r, _prime);
        BigInteger secKey = pubKey.modPow(r, _prime);

        byte[] mBytes = message.getBytes();
        //byte[] mBytes = DatatypeConverter.parseBase64Binary(message);
        BigInteger mPrime = (new BigInteger(1, mBytes)).mod(_prime);
        BigInteger c2 = mPrime.multiply(secKey).mod(_prime);

        receiver.receive(c1, c2);
    }

    public void receive(BigInteger c1, BigInteger c2) {
        BigInteger secKey = c1.modPow(_kPr, _prime);
        BigInteger secInv = secKey.modInverse(_prime);

        BigInteger mPrime = c2.multiply(secInv).mod(_prime);

        // We are good up to here, but mPrime.toByteArray does not return same bytes as String.getBytes...
        byte[] mBytes = mPrime.toByteArray();
        String message = new String(mBytes);
        //String message = DatatypeConverter.printBase64Binary(mBytes);
        int stop = 1;
    }

    private BigInteger privateKey() {
        return _kPr;
    }

    public BigInteger publicKey() {
        return _kPub;
    }

    public BigInteger generator() {
        return _gen;
    }

    public BigInteger prime() {
        return _prime;
    }
}
