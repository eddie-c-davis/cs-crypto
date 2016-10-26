package cs.crypto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edavis on 10/16/16.
 */
public class ListServer extends ElgamalEntity {
    private static final int DEFAULT_CAPACITY = 100;

    private KeyServer _keyServer;

    private List<String> _messages;
    private List<User> _subscribers;
    private List<BigInteger> _transKeys;

    public ListServer() {
        this(DEFAULT_BITLEN);
    }

    public ListServer(int bitLen) {
        super("ListServer", bitLen);
        _subscribers = new ArrayList<>(DEFAULT_CAPACITY);
        _messages = new ArrayList<>(DEFAULT_CAPACITY);
        _decryptKeys = new ArrayList<>(DEFAULT_CAPACITY);
    }

    public void receive(User sender, BigInteger cA, BigInteger cB) {
        // Transformation key s should be K - x (private key).
        // From the paper, it seems that K should be defined the list manager. However, I am not clear on how it is selected.
        BigInteger x = _kPr;
        BigInteger s = x;   // _K.subtract((x)

        BigInteger cPrimeA = cA;
        BigInteger cPrimeB = cB.multiply((cA.modPow(s, _prime)));

        // Now send  cA'' and cB'' to the respective subscribers...
        for (int i = 0; i < _subscribers.size(); i++) {
            User subscriber = _subscribers.get(i);

            // Calculate cA'' and cB'''
        }
    }

    public void register(KeyServer keyServer) {
        _keyServer = keyServer;
        _keyServer.register(this);
    }

    public void unregister(KeyServer keyServer) {
        if (_keyServer == keyServer) {
            _keyServer = null;
            _keyServer.unregister(this);
        }
    }

    public void subscribe(User user) {
        if (!_subscribers.contains(user)) {
            // Get transformation secret key from key server...
            BigInteger s_i = _keyServer.getTransKey(this, user);
            _subscribers.add(user);
            _transKeys.add(user);
        }
    }

    public void unsubscribe(User user) {
        if (_subscribers.contains(user)) {
            _subscribers.remove(user);
        }
    }

    private BigInteger publicKey() {
        return _kPub;
    }
}
