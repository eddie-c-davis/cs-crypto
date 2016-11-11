package cs.crypto;

import java.util.*;

/**
 * Created by edavis on 11/10/16.
 */
public class PolicyList {
    private static PolicyList _inst = null;

    private HashMap<String, Policy> _policies;

    public static PolicyList get() {
        if (_inst == null) {
            _inst = new PolicyList();
        }

        return _inst;
    }

    private PolicyList() {
        _policies = new HashMap<>();
    }

    public HashMap<String, Policy> map() {
        if (_policies.size() < 1) {
            read();
        }

        return _policies;
    }

    public Collection<Policy> list() {
        return map().values();
    }

    public void read() {
        Properties props = PropReader.read("policies.properties");
        Enumeration<?> propNames = props.propertyNames();

        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            String value = props.getProperty(name);
            Policy policy = new Policy(name, value);
            _policies.put(name, policy);
        }
    }
}
