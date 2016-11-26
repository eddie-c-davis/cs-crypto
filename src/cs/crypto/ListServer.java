package cs.crypto;

import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by edavis on 10/16/16.
 */
public class ListServer extends ElgamalEntity {
    private static final int DEFAULT_CAPACITY = 100;
    private static final String DEFAULT_NAME = "ListSever";

    private static Logger _log = Logger.getLogger(KeyServer.class.getName());
    private static Map<String, ListServer> _serverMap = new HashMap<>();

    private boolean _registered = false;
    private RandomBigInt _rand;
    private KeyServer _keyServer;

    private List<Message> _messages = new ArrayList<>(DEFAULT_CAPACITY);
    private Map<String, User> _subscribers = new HashMap<>();
    private List<BigInteger> _transKeys = new ArrayList<>();

    public static ListServer get() {
        return get(DEFAULT_NAME);
    }

    public static ListServer get(String serverName) {
        if (!_serverMap.containsKey(serverName)) {
            _serverMap.put(serverName, new ListServer(serverName));
        }

        // TODO: Pull list servers from RedisCache as serialized objects...

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

    public void deposit(User sender, BigInteger cSym, List<Pair<BigInteger>> cList) throws UserException {
        String senderName = sender.getName();
        if (_subscribers.containsKey(senderName)) {

            String logMsg = getName() + ": Received cSym " + cSym + ", cList = [";
            for (Pair<BigInteger> pair : cList) {
                logMsg += pair + ",";
            }

            logMsg += "] from user '" + senderName + "'.";
            _log.info(logMsg);

            int nKeys = cList.size();
            List<Pair<BigInteger>> cPrime = new ArrayList<>(nKeys);

            for (int i = 0; i < nKeys; i++) {
                BigInteger s = _transKeys.get(i);
                Pair<BigInteger> c = cList.get(i);
                BigInteger cA = c.first();
                BigInteger cB = c.second();

                BigInteger cPrA = cA;
                BigInteger cPrB = cB.multiply((cA.modPow(s, _prime)));
                cPrime.add(new Pair<>(cPrA, cPrB));
            }

            logMsg = getName() + ": Saving cSym " + cSym + ", cPrime = [";
            for (Pair<BigInteger> pair : cPrime) {
                logMsg += pair + ",";
            }

            logMsg += "] from user '" + senderName + "'.";
            _log.info(logMsg);

            Message msg = new Message(senderName, cSym, cPrime);
            _messages.add(msg);
        } else {
            throw new UserException("ListServer cannot receive from unsubscribed user '" + senderName + "'.");
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
            for (int i = 0; i < nPairs; i++) {
                if (!attributes.get(i).missing()) {
                    cSubset.add(cPairs.get(i));
                }
            }

            nPairs = cSubset.size();
            List<BigInteger> bFactors = getBlindingFactors(nPairs);

            // OKay, blinding factors still have issues.
            // TODO: Re=encrypt subset with blinding factors...

            // TODO: Encrypt with transformation keys.
            //                BigInteger s_i = BigInteger.ZERO; //_transKeys.get(subName);
//
//                // Calculate cA'' and cB'''
//                BigInteger cDblPrA = cPrimeA;
//                BigInteger cDblPrB = cPrimeB.divide(cPrimeA.modPow(s_i, _prime));

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
        BigInteger primeP1 = _prime.add(one);

        BigDecimal root = BigMath.root(nFactors, new BigDecimal(primeP1));
        BigInteger max = root.toBigInteger();
        RandomBigInt rand = new RandomBigInt(one, max);

        List<BigInteger> bFactors = new ArrayList<>(nFactors);

        BigInteger prod = one;
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
        BigInteger rem = primeP1.mod(prod);
        bFactors.add(bf);
        prod = prod.multiply(bf);

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
}
