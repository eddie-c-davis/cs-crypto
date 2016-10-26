package cs.crypto;

import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

/**
 * Created by edavis on 10/4/16.
 */
public class ElgamalUser implements User, ElgamalCipher {
    private static final int DEFAULT_BITLEN = 128;
    private static final int DEFAULT_CERTAINTY = 90;

    private boolean _useSAM = false;

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

    public ElgamalUser(String name, BigInteger g, BigInteger h, BigInteger p) {
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
        System.out.println(String.format("%s: Sending %s to %s.", _name, m.toString(), receiver.getName()));

        _prime = receiver.prime();
        _gen = receiver.generator();
        BigInteger kPub = receiver.publicKey();

        BigInteger[] c = encrypt(m, _prime, _gen, kPub);

        receiver.receive(this, c);
    }

    public BigInteger receive(ElgamalUser sender, BigInteger c) {
        return receive(sender, c, BigInteger.ZERO);
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

    public BigInteger receive(ElgamalUser sender, BigInteger[] c) {
        BigInteger m = decrypt(c);

        // We are good up to here, but mPrime.toByteArray does not return same bytes as String.getBytes...
        //byte[] mBytes = mPrime.toByteArray();
        //String message = new String(mBytes);
        //String message = DatatypeConverter.printBase64Binary(mBytes);

        System.out.println(String.format("%s: Received %s from %s.", _name, m.toString(), sender.getName()));

        return m;
    }

    private BigInteger privateKey() {
        return _kPr;
    }

    public String getName() {
        return _name;
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

    public void setSAM(boolean sam) {
        _useSAM = sam;
    }
}
