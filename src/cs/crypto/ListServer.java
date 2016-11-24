package cs.crypto;

import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by edavis on 10/16/16.
 */
public class ListServer extends ElgamalEntity {
    private static final int DEFAULT_CAPACITY = 100;
    private static Logger _log = Logger.getLogger(KeyServer.class.getName());

    private boolean _registered = false;

    private KeyServer _keyServer;

    private List<Message> _messages = new ArrayList<>(DEFAULT_CAPACITY);
    private Map<String, User> _subscribers = new HashMap<>();
    private List<BigInteger> _transKeys = new ArrayList<>();

    public ListServer() {
        this(DEFAULT_BITLEN);
    }

    public ListServer(int bitLen) {
        super("ListServer", bitLen);
    }

    public void receive(User sender, BigInteger cSym, List<Pair<BigInteger>> cList) throws UserException {
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

    public void register(KeyServer keyServer) throws ServerException {
        _log.info("Registering list server '" + getName() + "' with key server '" + keyServer.getName() + "'.");
        _registered = true;

        // register with the key server...
        _keyServer = keyServer;
        _keyServer.register(this);

        // Also fetch the key server's generator and big prime.
        _gen = keyServer.generator();
        _prime = keyServer.prime();

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

    public BigInteger publicKey() {
        return _kPub;
    }

    public boolean registered() {
        return _registered;
    }
}
