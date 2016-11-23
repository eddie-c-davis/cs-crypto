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

    private Map<String, BigInteger> _pubKeys = new HashMap<>();
    private Map<String, BigInteger> _transKeys = new HashMap<>();

    private List<ListServer> _listServers;
    private List<User> _regUsers;

    public KeyServer() {
        this(DEFAULT_BITLEN);
    }

    public KeyServer(int bitLen) {
        super("KeyServer", bitLen);
        _listServers = new ArrayList<>();
        _regUsers = new ArrayList<>();
    }

    public void init() {
        super.init();
        _rand = new RandomBigInt(BigInteger.ONE, _kPr.subtract(BigInteger.ONE));

        List<Attribute> attrList = AttributeList.get().list();
        for (Attribute attribute : attrList) {
            // Get a private key x_u for this user...
            BigInteger x_u = _rand.get();
            BigInteger s_u = _kPr.subtract(x_u);

            String attrName = attribute.getName();
            attribute.publicKey(x_u);

            _pubKeys.put(attrName, x_u);
            _transKeys.put(attrName, s_u);
        }
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

    public BigInteger getTransKey(ListServer listServer, Attribute attribute) throws AttributeException, ServerException {
        BigInteger s_u = BigInteger.ZERO;

        // Authenticate list server and user...
        String attrName = attribute.getName();
        if (_listServers.contains(listServer)) {
            if (_transKeys.containsKey(attrName)) {
                s_u = _transKeys.get(attrName);
            } else {
                throw new AttributeException("Undefined attribute '" + attrName + "'.");
            }
        } else {
            throw new ServerException("List server '" + listServer.getName() + "' is not registered.");
        }

        return s_u;
    }

    public void addUser(User user) {
        if (!_regUsers.contains(user)) {
            _regUsers.add(user);
        }
    }

    // Make private to override
    public BigInteger publicKey() {
        return _kPub;
    }
}
