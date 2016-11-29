package cs.crypto;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;


/**
 * Created by edavis on 11/21/16.
 */
public class RedisCache  implements Map<String, String> {
    public static final int DEFAULT_PORT = 6379;

    private static Logger _log = Logger.getLogger(RedisCache.class.getName());
    private static boolean _redisRunning = false;
    private static RedisCache _cache = null;

    private Jedis _jedis;

    public static RedisCache instance() {
        if (_cache == null) {
            _cache = new RedisCache();
        }

        return _cache;
    }

    public static boolean isRunning() {
        boolean running = _redisRunning;
        try {
            if (!running) {
                String line;
                Process p = Runtime.getRuntime().exec("/bin/ps -ef");
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while (!running && (line = input.readLine()) != null) {
                    running = (line.indexOf("redis-server") >= 0);
                }
                input.close();
                _redisRunning = running;
            }
        } catch (Exception err) {
            _log.error(err.toString());
        } finally {
            return running;
        }
    }

    private RedisCache() {
        Properties props = PropReader.read("redis.properties");
        String host = props.getProperty("host");
        String auth = props.getProperty("auth");

        int port = Integer.parseInt(props.getProperty("port"));
        if (port < 1) {
            port = DEFAULT_PORT;
        }

        _jedis = new Jedis(host, port);

        if (auth.length() > 0) {
            _jedis.auth(auth);
        }
    }

    public void clear() {
        _jedis.flushDB();
    }

    public boolean containsKey(Object key)
    {
        return (_jedis.exists(key.toString()));
    }

    public boolean containsValue(Object value)
    {
        return (_jedis.exists(value.toString()));
    }

    public String get(Object obj) {
        return _jedis.get(obj.toString());
    }

    public byte[] getBytes(Object obj) {
        String str = get(obj);

        return Bytes.toBytes(str);
    }

    public int size()
    {
        return keySet().size();
    }

    public boolean isEmpty() {
        return size() < 1;
    }

    public Set<String> keySet()
    {
        return _jedis.keys("*");
    }

    public Set<Entry<String, String>> entrySet() {
        Set<String> keys = this.keySet();
        Set<Entry<String, String>> set = new HashSet<>(keys.size());
        for (String key : keys) {
            set.add(new AbstractMap.SimpleEntry<String, String>(key, get(key)));
        }

        return set;
    }

    public Collection<String> values() {
        Set<String> keys = keySet();
        List<String> vals = new ArrayList<>(keys.size());
        for (String key : keys) {
            vals.add(get(key));
        }

        return vals;
    }

    public String put(String key, double number) {
        return put(key, String.format("%lf", number));
    }

    public String put(String key, int number) {
        return put(key, String.format("%d", number));
    }

    public String put(String key, byte[] bytes) {
        return put(key, Bytes.toString(bytes));
    }

    public String put(String key, String val) {
        _jedis.set(key, val);

        return val;
    }

    public void putAll(Map<? extends String,? extends String> map) {
        Set<? extends String> keySet = map.keySet();
        for (String key : keySet) {
            put(key, map.get(key));
        }
    }

    public String remove(Object obj) {
        String key = obj.toString();
        String val = "";
        if (_jedis.exists(key)) {
            val = _jedis.get(key);
            _jedis.del(key);
        }

        return val;
    }

    public String save() {
        return _jedis.save();
    }
}
