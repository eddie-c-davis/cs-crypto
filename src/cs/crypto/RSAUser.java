package cs.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
//import javax.xml.bind.DatatypeConverter;

/**
 * Created by edavis on 10/4/16.
 */
public class RSAUser implements User {
    private static final int DEFAULT_BITLENGH = 128;

    private boolean _useSAM = false;

    private int _bitLen = DEFAULT_BITLENGH;

    private String _name = "";
    private String _msg = "";

    private PrimeGenerator _pGen;

    private BigInteger _p;
    private BigInteger _q;
    private BigInteger _n;
    private BigInteger _e;
    private BigInteger _d;

    private Random _rand;

    public RSAUser() {
        this("");
    }

    public RSAUser(int bitLen) {
        this("", bitLen);
    }

    public RSAUser(String name) {
        this(name, DEFAULT_BITLENGH);
    }

    public RSAUser(String name, int bitLen) {
        _name = name;
        _bitLen = bitLen;
        _p = BigInteger.ZERO;
        _q = BigInteger.ZERO;
        _n = BigInteger.ZERO;
        _e = BigInteger.ZERO;
        _d = BigInteger.ZERO;
    }

    public RSAUser(String name, long p, long q) {
        this(name, p, q, 0);
    }

    public RSAUser(String name, long p, long q, long e) {
        _name = name;
        _p = BigInteger.valueOf(p);
        _q = BigInteger.valueOf(q);
        _e = BigInteger.valueOf(e);

        if (_p.bitLength() > 0) {
            _bitLen = _p.bitLength();
        }
    }

    public void init() {
        BigInteger zero = BigInteger.ZERO;
        if (_p.equals(zero) || _q.equals(zero)) {
            _pGen = new PrimeGenerator(_bitLen, 100);
            _p = _pGen.getP();
            _q = _pGen.getQ();
        }

        _n = _p.multiply(_q);

        BigInteger one = BigInteger.ONE;
        BigInteger phi = (_p.subtract(one)).multiply(_q.subtract(one));

        if (_e.equals(zero)) {
            // Choose e such that gcd(e, phi) == 1
            RandomBigInt randInt = new RandomBigInt(one, phi.subtract(one), getRand());
            _e = randInt.get();

            BigInteger gcd = _e.gcd(phi);
            while (!gcd.equals(one)) {
                _e = randInt.get();
                gcd = _e.gcd(phi);
            }
        }

        _d = _e.modInverse(phi);        // Compute d...
    }

    public void send(ListServer server, String message) {
        // TODO: Implement maybe?
    }

    public List<Attribute> getAttributes() {
        // TODO: Implement maybe?
        return null; //_attributes;
    }

    public void send(User receiver, String message) {
        send((RSAUser) receiver, message);
    }

    public void send(User receiver, long m) {
        send((RSAUser) receiver, m);
    }

    public void send(User receiver, BigInteger m) {
        send((RSAUser) receiver, m);
    }

    public BigInteger receive(User sender, BigInteger c) {
        return receive((RSAUser) sender, c);
    }

    public BigInteger receive(User sender, BigInteger c1, BigInteger c2) {
        return receive((RSAUser) sender, c1, c2);
    }

    public void send(RSAUser receiver, String message) {
        _msg = message;
        byte[] mBytes = _msg.getBytes();
        BigInteger m = new BigInteger(mBytes);      // BigInteger(1, mBytes);

        send(receiver, m);
    }

    public void send(RSAUser receiver, long m) {
        send(receiver, BigInteger.valueOf(m));
    }

    public void send(RSAUser receiver, BigInteger m) {
        // Get public key from receiver...
        BigInteger[] pubKey = receiver.publicKey();
        BigInteger n = pubKey[0];
        BigInteger e = pubKey[1];

        System.out.println(String.format("%s: Sending %s to %s.", _name, m.toString(), receiver.getName()));

        BigInteger c;
        if (_useSAM) {
            c = MyBigInt.squareAndMultiply(m, e, n);
        } else {
            c = m.modPow(e, n);
        }

        receiver.receive(this, c);
    }

    public BigInteger receive(RSAUser sender, BigInteger c1, BigInteger c2) {
        return receive(sender, c1.add(c2));
    }

    public BigInteger receive(RSAUser sender, BigInteger c) {
        BigInteger m;
        if (_useSAM) {
            m = MyBigInt.squareAndMultiply(c, _d, _n);
        } else {
            m = c.modPow(_d, _n);
        }

        // We are good up to here, but mPrime.toByteArray does not return same bytes as String.getBytes...
        byte[] mBytes = m.toByteArray();
        _msg = new String(mBytes);
        //String message = DatatypeConverter.printBase64Binary(mBytes);

        System.out.println(String.format("%s: received %s from %s.", _name, m.toString(), sender.getName()));

        return m;
    }

    public BigInteger[] publicKey() {
        return new BigInteger[] {_n, _e};
    }

    public BigInteger getE() { return _e; }
    public BigInteger getN() { return _n; }

    public void setSAM(boolean sam) {
        _useSAM = sam;
    }

    private BigInteger privateKey() {
        return getD();
    }

    private BigInteger getP() {
        return _p;
    }
    private BigInteger getQ() {
        return _q;
    }
    private BigInteger getD() {
        return _d;
    }

    public String getName() {
        return _name;
    }

    public boolean satisfies(Policy policy) {
        // TODO: Implement based on attributes in policy..
        return false;
    }

    public Random getRand() {
        if (_rand == null) {
            if (_pGen != null) {
                _rand = _pGen.getRand();
            } else {
                _rand = new SecureRandom();
            }
        }

        return _rand;
    }
}
