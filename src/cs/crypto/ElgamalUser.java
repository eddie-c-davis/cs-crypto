package cs.crypto;

import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

/**
 * Created by edavis on 10/4/16.
 */
public class ElgamalUser extends ElgamalEntity implements User {
    private RandomBigInt _rand;

    private Policy _policy;

    public ElgamalUser() {
        super("");
    }

    public ElgamalUser(int bitLen) {
        super("", bitLen);
    }

    public ElgamalUser(String name) {
        super(name, DEFAULT_BITLEN);
    }

    public ElgamalUser(String name, int bitLen) {
        super(name, bitLen);
    }

    public ElgamalUser(String name, BigInteger g, BigInteger h, BigInteger p) {
        super(name, g, h, p);
    }

    public void send(ListServer server, String message) throws MessageException, UserException {
        BigInteger x = _kPr;    // x is x_u, the user's private key.
        BigInteger g = _gen;    // g is the global generator (originating from the key server).
        BigInteger p = _prime;  // p is our big prime.
        BigInteger y = g.modPow(x, p);
        BigInteger r = _rand.get();

        //byte[] mBytes = DatatypeConverter.parseBase64Binary(message);
        //BigInteger m = (new BigInteger(1, message.getBytes())).mod(_prime);
        BigInteger m = MyBigInt.encode(message, p);

        BigInteger cA = g.modPow(r, p);
        BigInteger cB = m.multiply(y.modPow(r, p)).mod(p);

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

    public BigInteger receive(ListServer server, BigInteger cA, BigInteger cB) {

        BigInteger m = cB.divide(cA.modPow(_kPr, _prime));

        // TODO: Determine how to turn m back into a string...

        return m;
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

    public Policy getPolicy() {
        return _policy;
    }

    public void authenticate(KeyServer keyServer) {
        // Register with key server, and get private key.
        _kPr = keyServer.addUser(this);

        // Also fetch the key server's generator and big prime.
        _gen = keyServer.generator();
        _prime = keyServer.prime();

        // Initialize randomizer...
        BigInteger one = BigInteger.ONE;
        BigInteger pm1 = _prime.subtract(one);
        _rand = new RandomBigInt(one, pm1);

        // Fetch this user's policy
        _policy = PolicyList.get().map().get(getName());
    }
}
