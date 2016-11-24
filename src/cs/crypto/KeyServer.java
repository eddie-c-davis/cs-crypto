package cs.crypto;

import com.sun.corba.se.spi.activation.Server;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edavis on 10/17/16.
 */
public class KeyServer extends ElgamalEntity {
    private static Logger _log = Logger.getLogger(KeyServer.class.getName());

    private boolean _authorized = false;

    private RandomBigInt _rand;

    private Map<String, BigInteger> _privKeys = new HashMap<>();
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
        _log.info("Initializing key server '" + getName() + "'");
        super.init();

        _authorized = true;
        _rand = new RandomBigInt(BigInteger.ONE, _kPr.subtract(BigInteger.ONE));

        List<Attribute> attrList = AttributeList.get().list();
        for (Attribute attribute : attrList) {
            String attrName = attribute.getName();

            // Get a private key x_u for this user...
            BigInteger x_u = _rand.get();
            _privKeys.put(attrName, x_u);

            try {
                attribute.privateKey(this, x_u);
            } catch (ServerException se) {
                _log.error("Error setting private key for attribute '" + attribute + "'");
            }


            BigInteger s_u = _kPr.subtract(x_u);
            _transKeys.put(attrName, s_u);

            BigInteger y_u = _gen.modPow(x_u, _prime);
            _pubKeys.put(attrName, y_u);
            attribute.publicKey(y_u);
        }
    }

    public void register(ListServer listServer) throws ServerException {
        if (!_listServers.contains(listServer)) {
            _listServers.add(listServer);
        } else {
            throw new ServerException("List server '" + listServer.getName() + "' already registered with key server.");
        }
    }

    public void unregister(ListServer listServer) throws ServerException {
        if (_listServers.contains(listServer)) {
            _listServers.remove(listServer);
        } else {
            throw new ServerException("List server '" + listServer.getName() + "' not registered with key server.");
        }
    }

    public void addUser(User user) throws ServerException {
        if (!_regUsers.contains(user)) {
            _log.info("Registering user '" + user.getName() + "'");
            _regUsers.add(user);
        } else {
            throw new ServerException("User '" + user.getName() + "' already registered.");
        }
    }

    public boolean authorized() {
        return _authorized;
    }

    public BigInteger publicKey() {
        return _kPub;
    }

    private List<BigInteger> getPrivateKeys() {
        List<Attribute> attrList = AttributeList.get().list();
        List<BigInteger> privKeys = new ArrayList<>(attrList.size());

        for (Attribute attribute : attrList) {
            privKeys.add(_privKeys.get(attribute.getName()));
        }

        return privKeys;
    }

    public List<BigInteger> getPublicKeys() {
        List<Attribute> attrList = AttributeList.get().list();
        List<BigInteger> pubKeys = new ArrayList<>(attrList.size());

        for (Attribute attribute : attrList) {
            pubKeys.add(_pubKeys.get(attribute.getName()));
        }

        return pubKeys;
    }


    public List<BigInteger> getTransformationKeys(ListServer listServer) throws ServerException {
        if (_listServers.contains(listServer)) {
            List<Attribute> attrList = AttributeList.get().list();
            List<BigInteger> transKeys = new ArrayList<>(attrList.size());

            for (Attribute attribute : attrList) {
                transKeys.add(_transKeys.get(attribute.getName()));
            }

            return transKeys;
        } else {
            throw new ServerException("List server '" + listServer.getName() + "' is not registered.");
        }
    }
}
