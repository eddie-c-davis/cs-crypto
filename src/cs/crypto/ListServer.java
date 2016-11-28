package cs.crypto;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by edavis on 10/16/16.
 */
public class ListServer extends ElgamalEntity implements Serializable {
    private static final int DEFAULT_CAPACITY = 100;
    private static final String DEFAULT_NAME = "ListServer";

    private static Logger _log = Logger.getLogger(ListServer.class.getName());
    private static Map<String, ListServer> _serverMap = new HashMap<>();

    private boolean _registered = false;
    private boolean _blindingOn = true;

    private KeyServer _keyServer;
    private RandomBigInt _rand;

    private List<Message> _messages = new ArrayList<>(DEFAULT_CAPACITY);
    private Map<String, User> _subscribers = new HashMap<>();
    private List<BigInteger> _transKeys = new ArrayList<>();

    public static ListServer get() throws ServerException {
        return get(DEFAULT_NAME);
    }

    public static ListServer get(String serverName) throws ServerException {
        if (!_serverMap.containsKey(serverName)) {
            RedisCache cache = RedisCache.instance();
            String key = String.format("listserv-%s", serverName.toLowerCase());
            String cacheData = cache.get(key);

            ListServer listServer = null;
            if (cacheData != null && cacheData.length() > 0) {
                listServer = (ListServer) Bytes.fromString(cacheData);
                listServer.readMessages(cache);
            }

            if (listServer == null) {
                listServer = new ListServer(serverName);
                byte[] bytes = Bytes.toBytes(listServer);
                cacheData = Bytes.toString(bytes);
                cache.put(key, cacheData);
            }

            // Always register with the key server...
            KeyServer keyServer = KeyServer.instance();
            listServer.register(keyServer);

            _serverMap.put(serverName, listServer);
        }

        return _serverMap.get(serverName);
    }

    public ListServer() {
        this(DEFAULT_NAME);
    }

    public ListServer(String name) {
        super(name, DEFAULT_BITLEN);
    }

    public ListServer(String name, int bitLen) {
        super(name, bitLen);
    }

    public void register(KeyServer keyServer) throws ServerException {
        _log.info("Registering list server '" + getName() + "' with key server '" + keyServer.getName() + "'.");
        _registered = true;

        // register with the key server...
        _keyServer = keyServer;
        _keyServer.register(this);

        // Also fetch the key server's generator and big prime.
        _gen = keyServer.generator();
        _prime = keyServer.prime();

        // Initialize randomizer...
        BigInteger one = BigInteger.ONE;
        BigInteger pm1 = _prime.subtract(one);
        _rand = new RandomBigInt(one, pm1);

        // Get transformation secret keys from key server
        _transKeys = keyServer.getTransformationKeys(this);
    }

    public void unregister(KeyServer keyServer) throws ServerException {
        if (_keyServer == keyServer) {
            _log.info("Unregistering list server '" + getName() + "' with key server '" + keyServer.getName() + "'.");
            _keyServer = null;
            _keyServer.unregister(this);
            _registered = false;
        } else {
            throw new ServerException("Cannot unregister non-matching key server.");
        }
    }

    public void subscribe(User user) throws UserException {
        String userName = user.getName();
        if (!_subscribers.containsKey(userName)) {
            _log.info("Subscribing user '" + userName + "' to list server '" + getName() + "'.");
            _subscribers.put(userName, user);
        } else {
            throw new UserException("User '" + userName + "' already subscribed.");
        }
    }

    public void unsubscribe(User user) throws UserException {
        String userName = user.getName();
        if (_subscribers.containsKey(userName)) {
            _log.info("Unsubscribing user '" + userName + "' from list server '" + getName() + "'.");
            _subscribers.remove(userName);
        } else {
            throw new UserException("User '" + userName + "' not subscribed.");
        }
    }

    public Message deposit(User sender, BigInteger cSym, List<Pair<BigInteger>> cList) throws UserException {
        String senderName = sender.getName();
        if (_subscribers.containsKey(senderName)) {

            String logMsg = getName() + ": Received cSym " + cSym + ", cList = [";
            for (Pair<BigInteger> pair : cList) {
                logMsg += pair + ",";
            }

            logMsg += "] from user '" + senderName + "'.";
            _log.info(logMsg);

            List<Pair<BigInteger>> cPrime = transform(cList);

            logMsg = getName() + ": Saving cSym " + cSym + ", cPrime = [";
            for (Pair<BigInteger> pair : cPrime) {
                logMsg += pair + ",";
            }

            logMsg += "] from user '" + senderName + "'.";
            _log.info(logMsg);

            return addMessage(new Message(senderName, cSym, cPrime));
        } else {
            throw new UserException("ListServer cannot receive from unsubscribed user '" + senderName + "'.");
        }
    }

    private List<Pair<BigInteger>> transform(List<Pair<BigInteger>> cPairs) {
        int nPairs = cPairs.size();
        List<Integer> indices = new ArrayList<>(nPairs);
        for (int i = 0; i < cPairs.size(); i++) {
            indices.add(i);
        }

        return transform(cPairs, indices);
    }

    private List<Pair<BigInteger>> transform(List<Pair<BigInteger>> cPairs, List<Integer> indices) {
        int nKeys = cPairs.size();
        List<Pair<BigInteger>> cPrime = new ArrayList<>(nKeys);

        BigInteger p = this.prime();
        for (int i = 0; i < nKeys; i++) {
            //int j = indices.get(i);
            BigInteger s = _transKeys.get(i);
            Pair<BigInteger> c = cPairs.get(i);
            BigInteger cA = c.first();
            BigInteger cB = c.second();

            BigInteger cPrA = cA;
            BigInteger cPrB = cB.multiply((cA.modPow(s, p)));
            cPrime.add(new Pair<>(cPrA, cPrB));
        }

        return cPrime;
    }

    private Message addMessage(Message message) {
        _messages.add(message);

        RedisCache cache = RedisCache.instance();
        String prefix = String.format("listserv-%s-message-", _name.toLowerCase());

        int count = _messages.size();
        message.setCount(count);
        String key = String.format("%scount", prefix);
        cache.put(key, count);

        key = String.format("%s%d", prefix, count);
        byte[] bytes = Bytes.toBytes(message);
        cache.put(key, bytes);

        return message;
    }

    public void readMessages(RedisCache cache) {
        String prefix = String.format("listserv-%s-message-", _name.toLowerCase());
        String key = String.format("%scount", prefix);
        int count = Integer.parseInt(cache.get(key));

        for (int i = 1; i <= count; i++) {
            key = String.format("%s%d", prefix, i);
            String msgStr = cache.get(key);
            if (msgStr != null && msgStr.length() > 0) {
                Message message = (Message) Bytes.fromString(msgStr);
                _messages.add(message);
            }
        }
    }

    public List<Message> receive(User receiver) {
        List<Message> messages = new ArrayList<>();

        Policy policy = receiver.getPolicy();
        List<Attribute> attributes = policy.attributes();

        for (Message message : _messages) {
            List<Pair<BigInteger>> cPairs = message.cPairs();
            int nPairs = cPairs.size();

            List<Pair<BigInteger>> cSubset = new ArrayList<>(nPairs);
            List<Integer> indices = new ArrayList<>(nPairs);

            for (int i = 0; i < nPairs; i++) {
                if (!attributes.get(i).missing()) {
                    cSubset.add(cPairs.get(i));
                    indices.add(i);
                }
//                } else {
//                    cSubset.add(new Pair<>(BigInteger.ONE, BigInteger.ONE));
//                }
            }

            nPairs = cSubset.size();

            if (_blindingOn) {
                List<BigInteger> bFactors = getBlindingFactors(nPairs);

                // TODO: Encrypt blinding factors...
                // Okay, blinding factors still have issues.

                PeapodUser sender = PeapodUser.get(message.from());
                List<BigInteger> pubKeys = sender.getPolicy().getPublicKeys();

                List<Pair<BigInteger>> bfPairs = new ArrayList<>(nPairs);
                for (int i = 0; i < nPairs; i++) {
                    int j = indices.get(i);
                    BigInteger y = pubKeys.get(j);
                    Pair<BigInteger> bfC = encrypt(bFactors.get(i), _prime, _gen, y);
                    bfPairs.add(bfC);
                }

                // TODO: Encrypt with transformation keys.
                bfPairs = transform(bfPairs, indices);

                // TODO: "Homomorphically" multiply binding factors...
                for (int i = 0; i < nPairs; i++) {
                    Pair<BigInteger> bfPair = bfPairs.get(i);
                    Pair<BigInteger> cPair = cSubset.get(i);

                    BigInteger cAProd = bfPair.first().multiply(cPair.first()).mod(_prime);
                    BigInteger cBProd = bfPair.second().multiply(cPair.second()).mod(_prime);

                    Pair<BigInteger> cNew = new Pair(cAProd, cBProd);
                    cSubset.set(i, cNew);
                }
            }

            // TODO: Generate a new message with cSubset, and add to return list.
            Message newMsg = new Message(message.cSym(), cSubset);
            messages.add(newMsg);
        }

        return messages;
    }

    private List<BigInteger> getBlindingFactors(int nFactors) {
        int endIndex = nFactors - 1;

        // Get blinding factors, the product should be 1 mod p [i.e., (p + 1) mod p].
        BigInteger one = BigInteger.ONE;
        BigInteger zero = BigInteger.ZERO;
        BigInteger primeP1 = _prime.add(one);

        BigDecimal root = BigMath.root(nFactors, new BigDecimal(primeP1));
        BigInteger max = root.toBigInteger();
        RandomBigInt rand = new RandomBigInt(one, max);

        List<BigInteger> bFactors = new ArrayList<>(nFactors);

        BigInteger rem = one;
        BigInteger prod = one;

        //while (!rem.equals(zero)) {
            bFactors.clear();
            prod = one;
            BigInteger bf = prod;

            for (int i = 0; i < endIndex; i++) {
                //for (int i = 0; i < nFactors; i++) {
                //bf = (new RandomBigInt(one, max)).get();
                //max = max.divide(bf);
                bf = rand.get();
                prod = prod.multiply(bf);
                bFactors.add(bf);
            }

            bf = primeP1.divide(prod);
            rem = primeP1.mod(prod);
            bFactors.add(bf);
            prod = prod.multiply(bf);
        //}

        // TODO: What to do with remainder?

        // Distribute the error difference among the blinding factors.
        BigInteger diff = primeP1.subtract(prod);
//        BigInteger nF = BigInteger.valueOf(nFactors);
//        BigInteger quot = diff.divide(nF);
//        BigInteger rem = diff.mod(nF);
//
//        for (int i = 0; i < nFactors; i++) {
//            bf = bFactors.get(i).add(quot);
//            bFactors.set(i, bf);
//        }
//
//        if (!rem.equals(BigInteger.ZERO)) {
//            int index = ThreadLocalRandom.current().nextInt(0, endIndex);
//            bf = bFactors.get(index).add(rem);
//            bFactors.set(index, bf);
//        }

        prod = BigMath.product(bFactors);
        BigInteger test = primeP1.mod(_prime);
        assert(prod.equals(primeP1));

        return bFactors;
    }

    public BigInteger publicKey() {
        return _kPub;
    }

    public boolean registered() {
        return _registered;
    }

    public boolean getBlinding() {
        return _blindingOn;
    }

    public void setBlinding(boolean blindingOn) {
        _blindingOn = blindingOn;
    }

    @Override
    public BigInteger generator() {
        if (_gen == null) {
            _gen = _keyServer.generator();
        }

        return _gen;
    }

    @Override
    public BigInteger prime() {
        if (_prime == null) {
            _prime = _keyServer.prime();
        }

        return _prime;
    }
}
