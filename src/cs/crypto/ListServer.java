package cs.crypto;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by edavis on 10/16/16.
 */
public class ListServer extends ElgamalEntity {
    private static final int DEFAULT_CAPACITY = 100;

    private KeyServer _keyServer;

    private List<String> _messages = new ArrayList<>(DEFAULT_CAPACITY);
    private Map<String, User> _subscribers = new HashMap<>();
    private Map<String, BigInteger> _transKeys = new HashMap<>();

    public ListServer() {
        this(DEFAULT_BITLEN);
    }

    public ListServer(int bitLen) {
        super("ListServer", bitLen);
    }

    public void receive(User sender, BigInteger cA, BigInteger cB) throws UserException {
        String senderName = sender.getName();
        if (_transKeys.containsKey(senderName)) {
            BigInteger s = _transKeys.get(senderName);
            BigInteger cPrimeA = cA;
            BigInteger cPrimeB = cB.multiply((cA.modPow(s, _prime)));

            // Now send  cA'' and cB'' to the respective subscribers...
            for (Map.Entry<String, User> entry : _subscribers.entrySet()) {
                String subName = entry.getKey();
                if (!subName.equals(senderName)) {              // Do not send message back to sender...
                    User subscriber = entry.getValue();
                    BigInteger s_i = _transKeys.get(subName);

                    // Calculate cA'' and cB'''
                    BigInteger cDblPrA = cPrimeA;
                    BigInteger cDblPrB = cPrimeB.divide(cPrimeA.modPow(s_i, _prime));

                    // TODO: Add attribute matching code here!!!
                    subscriber.receive(this, cDblPrA, cDblPrB);
                }
            }
        } else {
            throw new UserException("ListServer cannot receive from unsubscribed user '" + senderName + "'.");
        }
    }

    public void register(KeyServer keyServer) {
        // register with the key server...
        _keyServer = keyServer;
        _keyServer.register(this);

        // Also fetch the key server's generator and big prime.
        _gen = keyServer.generator();
        _prime = keyServer.prime();
    }

    public void unregister(KeyServer keyServer) throws ServerException {
        if (_keyServer == keyServer) {
            _keyServer = null;
            _keyServer.unregister(this);
        } else {
            throw new ServerException("Cannot unregister non-matching key server.");
        }
    }

    public void subscribe(User user) throws UserException {
        String userName = user.getName();
        if (!_subscribers.containsKey(userName)) {
            // Get transformation secret key from key server...
            BigInteger s_i = _keyServer.getTransKey(this, user);
            _subscribers.put(userName, user);
            _transKeys.put(userName, s_i);
        } else {
            throw new UserException("User '" + userName + "' already subscribed.");
        }
    }

    public void unsubscribe(User user) throws UserException {
        String userName = user.getName();
        if (_subscribers.containsKey(userName)) {
            _subscribers.remove(userName);
        } else {
            throw new UserException("User '" + userName + "' not subscribed.");
        }
    }

    public BigInteger publicKey() {
        return _kPub;
    }
}
