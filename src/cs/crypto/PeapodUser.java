package cs.crypto;

import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cs.crypto.AES.encrypt;

/**
 * Created by edavis on 11/22/16.
 */
public class PeapodUser implements User {
    private static Logger _log = Logger.getLogger(PeapodUser.class.getName());

    private static Map<String, PeapodUser> _userMap = new HashMap<>();

    private String _name;
    private Policy _policy;
    private RandomBigInt _rand;

    private BigInteger _gen;
    private BigInteger _prime;

    public static PeapodUser get(String userName) {
        if (!_userMap.containsKey(userName)) {
            _userMap.put(userName, new PeapodUser(userName));
        }

        // TODO: Pull users from RedisCache as serialized objects...

        return _userMap.get(userName);
    }

    public PeapodUser() {
        this("");
    }

    public PeapodUser(String name) {
        // Set name...
        _name = name;

        // Fetch this user's policy
        _policy = PolicyList.get().map().get(getName());
    }

    public void send(ListServer server, String message) throws GeneralSecurityException, MessageException, UserException {
        BigInteger g = _gen;    // g is the global generator (originating from the key server).
        BigInteger p = _prime;  // p is our big prime.

        // Convert message to
        //byte[] mBytes = DatatypeConverter.parseBase64Binary(message);
        //BigInteger m = (new BigInteger(1, message.getBytes())).mod(_prime);
        BigInteger m = MyBigInt.encode(message, p);

        String logMsg = getName() + ": Encrypting message '" + message + "' (" + m + ").";
        _log.info(logMsg);

        // 1) Alice encrypts M with a secure symmetric encryption under randomly generated key k in Zp.

        // 2) Alice then randomly picks a sub-key for each v-attribute in policy.
        List<BigInteger> subKeys = getSubKeys();
        BigInteger key = getSymKey(subKeys);

        BigInteger prod = getKeyProduct(subKeys);
        assert(prod.equals(key));

        // We will use AES as our symmetric encryption scheme.
        BigInteger cSym = AES.encrypt(m, key);
        assert(m == AES.decrypt(cSym, key));

        // Now we ElGamal encrypt the the subkeys before depositing on the list server...
        List<BigInteger> pubKeys = getPubKeys();
        List<Pair<BigInteger>> cList = new ArrayList<>(pubKeys.size());
        for (BigInteger y : pubKeys) {
            BigInteger r = _rand.get();     // Randomness...
            BigInteger cA = g.modPow(r, p);
            BigInteger cB = m.multiply(y.modPow(r, p)).mod(p);

            cList.add(new Pair<>(cA, cB));
        }

        logMsg = getName() + ": Sending cSym " + cSym + ", cList = [";
        for (Pair<BigInteger> pair : cList) {
            logMsg += pair + ",";
        }

        logMsg += "] to '" + server.getName() + "'";
        _log.info(logMsg);

        server.deposit(this, cSym, cList);
    }

    private List<BigInteger> getPubKeys() {
        List<BigInteger> pubKeys = new ArrayList<>(_policy.size());
        for (Attribute attribute : _policy.attributes()) {
            pubKeys.add(attribute.publicKey());
        }

        return pubKeys;
    }

    private List<BigInteger> getSubKeys() {
        List<BigInteger> subKeys = new ArrayList<>(_policy.size());

        BigInteger one = BigInteger.ONE;
        BigInteger size = BigInteger.valueOf(_policy.size());

        // This number needs to be much smaller to avoid overflow...
        BigInteger max = _prime.divide(size);

        for (Attribute attribute : _policy.attributes()) {
            BigInteger subKey;
            if (attribute.required()) {
                // Randomly generate a sub-key < k
                subKey = (new RandomBigInt(one, max)).get();
            } else if (attribute.forbidden()) {
                // Generate a random number r_i in Z_p
                subKey = _rand.get();
            } else {    // attribute.irrelevant()
                subKey = one;
            }

            subKeys.add(subKey);
        }

        return subKeys;
    }

    private BigInteger getSymKey(List<BigInteger> subKeys) {
        List<Attribute> attributes = _policy.attributes();

        BigInteger key = BigInteger.ONE;
        for (int i = 0; i < subKeys.size(); i++) {
            if (attributes.get(i).required()) {
                key = key.multiply(subKeys.get(i)).mod(_prime);
            }
        }

        return key;
    }

    private BigInteger getKeyProduct(List<BigInteger> subKeys) {
        List<Attribute> attributes = _policy.attributes();

        BigInteger prod = BigInteger.ONE;
        for (int i = 0; i < subKeys.size(); i++) {
            if (!attributes.get(i).forbidden()) {
                prod = prod.multiply(subKeys.get(i)).mod(_prime);
            }
        }

        return prod;
    }

    public void send(User receiver, String message) {
        send((PeapodUser) receiver, message);
    }

    public void send(User receiver, long m) {
        send((PeapodUser) receiver, m);
    }

    public void send(User receiver, BigInteger m) {
        send((PeapodUser) receiver, m);
    }

    public BigInteger receive(User sender, BigInteger c) {
        return receive((PeapodUser) sender, c);
    }

    public BigInteger receive(User sender, BigInteger c1, BigInteger c2) {
        return receive((PeapodUser) sender, c1, c2);
    }

    public void send(PeapodUser receiver, String message) {
        byte[] mBytes = message.getBytes();
        //byte[] mBytes = DatatypeConverter.parseBase64Binary(message);
        BigInteger m = BigInteger.ZERO; //(new BigInteger(1, mBytes)).mod(receiver.prime());

        send(receiver, m);
    }

    public void send(PeapodUser receiver, long m) {
        send(receiver, BigInteger.valueOf(m));
    }

    public void send(PeapodUser receiver, BigInteger m) {
        System.out.println(String.format("%s: Sending %s to %s.", _name, m.toString(), receiver.getName()));

//        _prime = receiver.prime();
//        _gen = receiver.generator();
//        BigInteger kPub = receiver.publicKey();
//
        BigInteger[] c = new BigInteger[] {BigInteger.ZERO}; //encrypt(m, _prime, _gen, kPub);

        receiver.receive(this, c);
    }

    public BigInteger receive(PeapodUser sender, BigInteger c) {
        return receive(sender, c, BigInteger.ZERO);
    }

    public BigInteger receive(ListServer server) { //}, BigInteger cA, BigInteger cB) {
        BigInteger m = BigInteger.ZERO; //cB.divide(cA.modPow(_kPr, _prime));

        List<Message> messages = server.receive(this);
        for (Message message : messages) {
            // TODO: Attempt to decrypt message...

        }


        return m;
    }

    public BigInteger decrypt(BigInteger[] c) {
        BigInteger c1 = c[0];
        BigInteger c2 = c[1];

//        BigInteger secKey; // = c1.modPow(_kPr, _prime);
//        if (_useSAM) {
//            secKey = MyBigInt.squareAndMultiply(c1, _kPr, _prime);
//        } else {
//            secKey = c1.modPow(_kPr, _prime);
//        }
//
//        BigInteger secInv = secKey.modInverse(_prime);
        BigInteger m = BigInteger.ZERO; //c2.multiply(secInv).mod(_prime);

        return m;
    }

    public BigInteger receive(PeapodUser sender, BigInteger[] c) {
        BigInteger m = decrypt(c);

        // We are good up to here, but mPrime.toByteArray does not return same bytes as String.getBytes...
        //byte[] mBytes = mPrime.toByteArray();
        //String message = new String(mBytes);
        //String message = DatatypeConverter.printBase64Binary(mBytes);

        System.out.println(String.format("%s: Received %s from %s.", _name, m.toString(), sender.getName()));

        return m;
    }

    public String getName() {
        return _name;
    }

    public Policy getPolicy() {
        return _policy;
    }

    public void authenticate(KeyServer keyServer) throws ServerException {
        // Register with key server, and get private key.
        //_kPr = keyServer.addUser(this);
        _log.info("Authenticating user '" + getName() + "' with key server '" + keyServer.getName() + "'");
        keyServer.addUser(this);

        // Also fetch the key server's generator and big prime.
        _gen = keyServer.generator();
        _prime = keyServer.prime();

        // Initialize randomizer...
        BigInteger one = BigInteger.ONE;
        BigInteger pm1 = _prime.subtract(one);
        _rand = new RandomBigInt(one, pm1);

        // Get policy attribute public keys
        _policy.setPublicKeys(keyServer.getPublicKeys());
    }
}
