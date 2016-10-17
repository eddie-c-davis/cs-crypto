package cs.crypto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edavis on 10/16/16.
 */
public class ListServer {
    private static final int DEFAULT_CAPACITY = 100;
    private static final int DEFAULT_BITLEN = 256;

    private int _bitLen;

    private PrimeGenerator _pGen;

    private BigInteger _gen;
    private BigInteger _prime;
    private BigInteger _kPr;
    private BigInteger _kPub;
    private BigInteger _K;

    private List<User> _subscribers;
    private List<BigInteger> _decryptKeys;

    public ListServer() {
        this(DEFAULT_BITLEN);
    }

    public ListServer(int bitLen) {
        _bitLen = bitLen;
        _pGen = new PrimeGenerator(_bitLen, 100);
        _subscribers = new ArrayList<>(DEFAULT_CAPACITY);
    }

    public void init() {
        _prime = _pGen.getP();
        _gen = _pGen.getG();
        _kPr = _pGen.getH();
        _kPub = _gen.modPow(_kPr, _prime);
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

    public void subscribe(User user) {
        if (!_subscribers.contains(user)) {
            // Compute s_i for new subscriber (transformation secret key)

            // Compute x_i for new subscripber (decryption private key)

            _subscribers.add(user);
        }
    }

    public void unsubscribe(User user) {
        if (_subscribers.contains(user)) {
            _subscribers.remove(user);
        }
    }

    private BigInteger privateKey() {
        return _kPr;
    }

    public BigInteger publicKey() {
        return _kPub;
    }

    public BigInteger generator() {
        return _gen;
    }

    public BigInteger prime() {
        return _prime;
    }
}
