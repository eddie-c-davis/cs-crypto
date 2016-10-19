package cs.crypto;

import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

/**
 * Created by edavis on 10/4/16.
 */
public class ElgamalUser implements User {
    private static final int DEFAULT_BITLEN = 128;
    private static final int DEFAULT_CERTAINTY = 90;

    private String _name;
    private int _bitLen;
    private PrimeGenerator _pGen;

    private BigInteger _gen;
    private BigInteger _prime;
    private BigInteger _kPr;
    private BigInteger _kPub;

    private List<Attribute> _attributes;

    public ElgamalUser() {
        this("");
    }

    public ElgamalUser(int bitLen) {
        this("", bitLen);
    }

    public ElgamalUser(String name) {
        this(name, DEFAULT_BITLEN);
    }

    public ElgamalUser(String name, int bitLen) {
        _name = name;
        _bitLen = bitLen;
        _pGen = new PrimeGenerator(_bitLen, DEFAULT_CERTAINTY);
    }

    public void init() {
        _prime = _pGen.getP();
        _gen = _pGen.getG();
        _kPr = _pGen.getH();
        _kPub = _gen.modPow(_kPr, _prime);
    }

    public void send(ListServer server, String message) {
        _prime = server.prime();
        _gen = server.generator();

        BigInteger one = BigInteger.ONE;
        BigInteger pm1 = _prime.subtract(one);
        BigInteger r = (new RandomBigInt(one, pm1)).get();
        BigInteger y = server.publicKey();

        //byte[] mBytes = DatatypeConverter.parseBase64Binary(message);
        BigInteger m = (new BigInteger(1, message.getBytes())).mod(_prime);

        BigInteger cA = _gen.modPow(r, _prime);
        BigInteger cB = m.multiply(y.modPow(r, _prime)).mod(_prime);

        server.receive(this, cA, cB);
    }

    public void send(User receiver, String message) {
        send((ElgamalUser) receiver, message);
    }

    public void send(User receiver, long m) {
        send((ElgamalUser) receiver, m);
    }

    public void send(User receiver, BigInteger m) {
        send((ElgamalUser) receiver, m);
    }

    public BigInteger receive(User sender, BigInteger c) {
        return receive((ElgamalUser) sender, c);
    }

    public BigInteger receive(User sender, BigInteger c1, BigInteger c2) {
        return receive((ElgamalUser) sender, c1, c2);
    }

    public void send(ElgamalUser receiver, String message) {
        byte[] mBytes = message.getBytes();
        //byte[] mBytes = DatatypeConverter.parseBase64Binary(message);
        BigInteger m = (new BigInteger(1, mBytes)).mod(receiver.prime());

        send(receiver, m);
    }

    public void send(ElgamalUser receiver, long m) {
        send(receiver, BigInteger.valueOf(m));
    }

    public void send(ElgamalUser receiver, BigInteger m) {
        _prime = receiver.prime();
        _gen = receiver.generator();
        BigInteger pubKey = receiver.publicKey();

        BigInteger one = BigInteger.ONE;
        BigInteger pm1 = _prime.subtract(one);
        BigInteger r = (new RandomBigInt(one, pm1)).get();

        BigInteger c1 = _gen.modPow(r, _prime);
        BigInteger secKey = pubKey.modPow(r, _prime);

        BigInteger c2 = m.multiply(secKey).mod(_prime);

        receiver.receive(this, c1, c2);
    }

    public BigInteger receive(ElgamalUser sender, BigInteger c) {
        return receive(sender, c, BigInteger.ZERO);
    }

    public BigInteger receive(ElgamalUser sender, BigInteger c1, BigInteger c2) {
        BigInteger secKey = c1.modPow(_kPr, _prime);
        BigInteger secInv = secKey.modInverse(_prime);

        BigInteger mPrime = c2.multiply(secInv).mod(_prime);

        // We are good up to here, but mPrime.toByteArray does not return same bytes as String.getBytes...
        //byte[] mBytes = mPrime.toByteArray();
        //String message = new String(mBytes);
        //String message = DatatypeConverter.printBase64Binary(mBytes);

        return mPrime;
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

    public List<Attribute> getAttributes() {
        return _attributes;
    }

    public boolean satisfies(Policy policy) {
        // TODO: Implement based on attributes in policy..
        return false;
    }
}
