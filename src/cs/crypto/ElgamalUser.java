package cs.crypto;

import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

/**
 * Created by edavis on 10/4/16.
 */
public class ElgamalUser extends ElgamalEntity implements User {

    private List<Attribute> _attributes;

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

    public List<Attribute> getAttributes() {
        return _attributes;
    }

    public boolean satisfies(Policy policy) {
        // TODO: Implement based on attributes in policy..
        return false;
    }
}
