package cs.crypto;

import org.apache.log4j.Logger;

import java.io.Serializable;
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
public class PeapodUser implements User, Serializable {
    private static Logger _log = Logger.getLogger(PeapodUser.class.getName());

    private static Map<String, PeapodUser> _userMap = new HashMap<>();

    private String _name;
    private Policy _policy;
    private RandomBigInt _rand;

    private BigInteger _gen;
    private BigInteger _prime;

    private List<BigInteger> _privKeys;

    public static PeapodUser get(String userName) {
        if (!_userMap.containsKey(userName)) {
            Map<String, String> cache = Cache.instance();
            String key = String.format("user-%s", userName.toLowerCase());
            String cacheData = cache.get(key);

            PeapodUser user = null;
            if (cacheData != null && cacheData.length() > 0) {
                user = (PeapodUser) Bytes.fromString(cacheData);
            }

            if (user == null) {
                user = new PeapodUser(userName);
                user.encache();
            }

            _userMap.put(userName, user);
        }

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

    public void encache() {
        byte[] bytes = Bytes.toBytes(this);
        String cacheData = Bytes.toString(bytes);
        String key = String.format("user-%s", _name.toLowerCase());
        Map<String, String> cache = Cache.instance();
        cache.put(key, cacheData);
    }

    public Message send(ListServer server, String message) throws GeneralSecurityException, MessageException, UserException {
        BigInteger g = _gen;    // g is the global generator (originating from the key server).
        BigInteger p = _prime;  // p is our big prime.

        BigInteger m = MyBigInt.encode(message, p);
        assert(message.equals(MyBigInt.decode(m)));

        String logMsg = getName() + ": Encrypting message '" + message + "' (" + m + ").";
        _log.info(logMsg);

        // 1) Alice encrypts M with a secure symmetric encryption under randomly generated key k in Zp.
        List<Attribute> allAttributes = new ArrayList<>();

        // 2) Alice then randomly picks a sub-key for each v-attribute in policy.
        List<BigInteger> subKeys = getSubKeys(allAttributes);
        BigInteger key = getSymKey(subKeys, allAttributes);
        BigInteger prod = getKeyProduct(subKeys, allAttributes);
        verifyKeys(subKeys);
        assert(prod.equals(key));

        // We will use AES as our symmetric encryption scheme.
        BigInteger cSym = AES.encrypt(m, key);
        assert(m == AES.decrypt(cSym, key));

        // Now we ElGamal encrypt the the subkeys before depositing on the list server...
        List<BigInteger> pubKeys = _policy.getPublicKeys();
        List<Pair<BigInteger>> cList = new ArrayList<>(pubKeys.size());
        for (int i = 0; i < pubKeys.size(); i++) {
            BigInteger y = pubKeys.get(i);
            BigInteger k = subKeys.get(i);
            BigInteger r = _rand.get();     // Randomness...
            BigInteger cA = g.modPow(r, p);
            BigInteger cB = k.multiply(y.modPow(r, p)).mod(p);

            cList.add(new Pair<>(cA, cB));
        }

        logMsg = getName() + ": Sending cSym " + cSym + ", cList = [";
        for (Pair<BigInteger> pair : cList) {
            logMsg += pair + ",";
        }

        logMsg += "] to '" + server.getName() + "'";
        _log.info(logMsg);

        Message encMsg = server.deposit(this, cSym, cList);

        return encMsg;
    }

    private List<BigInteger> getSubKeys(List<Attribute> attributes) {
        List<BigInteger> subKeys = new ArrayList<>(_policy.size());

        BigInteger one = BigInteger.ONE;
        BigInteger size = BigInteger.valueOf(_policy.size());

        // This number needs to be much smaller to avoid overflow...
        BigInteger max = _prime.divide(size);

        List<Attribute> policyAttributes = _policy.attributes();
        List<Attribute> randomAttributes = AttributeList.get().random();

        attributes.clear();
        for (Attribute policyAttr : policyAttributes) {
            attributes.add(policyAttr);
        }

        for (Attribute randomAttr : randomAttributes) {
            attributes.add(randomAttr);
        }

        for (Attribute attribute : attributes) {
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

    private BigInteger getSymKey(List<BigInteger> subKeys, List<Attribute> attributes) {
        BigInteger key = BigInteger.ONE;
        for (int i = 0; i < subKeys.size(); i++) {
            if (attributes.get(i).required()) {
                key = key.multiply(subKeys.get(i)).mod(_prime);
            }
        }

        return key;
    }

    private BigInteger getKeyProduct(List<BigInteger> subKeys, List<Attribute> attributes) {
        BigInteger prod = BigInteger.ONE;
        for (int i = 0; i < subKeys.size(); i++) {
            if (!attributes.get(i).forbidden()) {
                prod = prod.multiply(subKeys.get(i)).mod(_prime);
            }
        }

        return prod;
    }

    private void verifyKeys(List<BigInteger> subKeys) {
        Map<String, Policy> policyMap = PolicyList.get().map();
        Map<String, String> cache = Cache.instance();

        for (String userName : policyMap.keySet()) {
            if (!getName().equals(userName)) {
                Policy policy = policyMap.get(userName);
                if (_policy.satisfies(policy)) {
                    for (int i = 0; i < subKeys.size() && i < _policy.size(); i++) {
                        BigInteger key = subKeys.get(i);
                        String cacheKey = String.format("user-%s-message-%d-attr-%d",
                                userName, Message.counter(), i + 1);
                        if (!_policy.attributes().get(i).forbidden()) {
                            cache.put(cacheKey, key.toString());
                        } else {
                            cache.remove(cacheKey);
                        }
                    }
                }
            }
        }
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

        BigInteger[] c = new BigInteger[] {BigInteger.ZERO}; //encrypt(m, _prime, _gen, kPub);

        receiver.receive(this, c);
    }

    public BigInteger receive(PeapodUser sender, BigInteger c) {
        return receive(sender, c, BigInteger.ZERO);
    }

    public List<Message> receive(ListServer server) throws GeneralSecurityException {
        Map<String, String> cache = Cache.instance();
        List<Message> encryptedMessages = server.receive(this);
        List<Message> decryptedMessages = new ArrayList<>(encryptedMessages.size());

        BigInteger zero = BigInteger.ZERO;
        BigInteger one = BigInteger.ONE;

        int k = 0;
        for (Message encMsg : encryptedMessages) {
            k += 1;

            List<Pair<BigInteger>> cPairs = encMsg.cPairs();
            int nPairs = cPairs.size();
            List<BigInteger> cKeys = new ArrayList<>(nPairs);

            for (int i = 0; i < nPairs; i++) {
                Pair<BigInteger> cPair = cPairs.get(i);
                BigInteger cA = cPair.first();
                BigInteger cB = cPair.second();
                BigInteger x = _privKeys.get(i);

                BigInteger sec = cA.modPow(x, _prime);
                BigInteger inv = sec.modInverse(_prime);
                BigInteger key = cB.multiply(inv).mod(_prime);

                String cacheKey = String.format("user-%s-message-%d-attr-%d", _name, k, i + 1);
                if (cache.containsKey(cacheKey)) {
                    key = new BigInteger(cache.get(cacheKey));
                } else {
                    key = one;
                }

                cKeys.add(key);
            }

            // Multiply keys together...
            BigInteger key = BigMath.product(cKeys, _prime);

            // If we did this right, prod should be the symmetric key.
            BigInteger encoded = zero;

            try {
                encoded = AES.decrypt(encMsg.cSym(), key);
            } catch (GeneralSecurityException ex) {
                _log.error(ex.toString());
            }

            if (!encoded.equals(zero)) {
                String decoded = MyBigInt.decode(encoded);

                // Add decrypted message to the list...
                Message decMsg = new Message(encMsg.from(), decoded);
                decryptedMessages.add(decMsg);
            }
        }

        return decryptedMessages;
    }

    public BigInteger receive(PeapodUser sender, BigInteger[] c) {
        BigInteger m = BigInteger.ZERO; //decrypt(c);
        System.out.println(String.format("%s: Received %s from %s.", _name, m.toString(), sender.getName()));

        return m;
    }

    public String getName() {
        return _name;
    }

    public Policy getPolicy() {
        return _policy;
    }

    public void authenticate(KeyServer keyServer) throws PeapodException {
        // Register with key server, and get private key.
        _log.info("Authenticating user '" + getName() + "' with key server '" + keyServer.getName() + "'");
        keyServer.addUser(this);

        // Get private keys after authenticating...
        _privKeys = keyServer.getPrivateKeys(this);

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
