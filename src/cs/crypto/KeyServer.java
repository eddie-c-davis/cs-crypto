package cs.crypto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edavis on 10/17/16.
 */
public class KeyServer extends ElgamalEntity {
    private RandomBigInt _rand;

    private Map<String, BigInteger> _userKeys = new HashMap<>();
    private Map<String, BigInteger> _listKeys = new HashMap<>();

    private List<ListServer> _listServers;

    public KeyServer() {
        this(DEFAULT_BITLEN);
    }

    public KeyServer(int bitLen) {
        super("KeyServer", bitLen);
        _listServers = new ArrayList<>();
    }

    public void init() {
        super.init();
        _rand = new RandomBigInt(BigInteger.ONE, _kPr.subtract(BigInteger.ONE));
    }

    public void register(ListServer listServer) {
        if (!_listServers.contains(listServer)) {
            _listServers.add(listServer);
        }
    }

    public void unregister(ListServer listServer) {
        if (_listServers.contains(listServer)) {
            _listServers.remove(listServer);
        }
    }

    public BigInteger getTransKey(ListServer listServer, User user) {
        BigInteger s_u = BigInteger.ZERO;

        // Authenticate list server and user...
        String userName = user.getName();
        if (_listServers.contains(listServer) && _listKeys.containsKey(userName)) {
            s_u = _listKeys.get(userName);
        }

        return s_u;
    }

    public BigInteger addUser(User user) {
        // Get a private key x_u for this user...
        BigInteger x_u = _rand.get();
        BigInteger s_u = _kPr.subtract(x_u);

        String userName = user.getName();
        _userKeys.put(userName, x_u);
        _listKeys.put(userName, s_u);

        return x_u;
    }

    // Make private to override
    public BigInteger publicKey() {
        return _kPub;
    }


}
