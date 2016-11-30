package cs.crypto;

/**
 * Created by edavis on 11/28/16.
 */
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Created by edavis on 11/24/16.
 */
public class FileCache implements Map<String, String> {
    private static Logger _log = Logger.getLogger(FileCache.class.getName());

    private static FileCache _instance = null;

    private boolean _cacheOnDisk;

    private int _initSize;

    private String _dataPath;
    private String _dataFormat;

    private Dictionary<String, String> _cache;

    public static FileCache instance() {
        if (_instance == null) {
            _instance = new FileCache();
        }

        return _instance;
    }

    private FileCache()
    {
        Properties props = PropReader.read("cache.properties");
        _dataPath = props.getProperty("path");
        _initSize = Integer.parseInt(props.getProperty("size"));
        _cacheOnDisk = Boolean.parseBoolean(props.getProperty("ondisk"));
        _dataFormat = props.getProperty("format");

        _cache = new Hashtable<>(_initSize);
    }

    public void clear()
    {
        _cache = new Hashtable<>(_cache.size());
    }

    public boolean containsKey(Object key)
    {
        return (this.get(key) != null);
    }

    public boolean containsValue(Object value)
    {
        return (_cache.get(value) != null);
    }

    public Set<Entry<String, String>> entrySet()
    {
        Set<Entry<String, String>> set = new HashSet<>(_cache.size());
        for (String key : this.keys())
        {
            set.add(new AbstractMap.SimpleEntry<String, String>(key, get(key)));
        }

        return set;
    }

    public Set<String> keySet()
    {
        return new HashSet<String>(keys());
    }

    public boolean isEmpty()
    {
        return _cache.isEmpty();
    }

    public int size()
    {
        return _cache.size();
    }

    public Collection<String> keys()
    {
        return Collections.list(_cache.keys());
    }

    public Collection<String> values()
    {
        return Collections.list(_cache.elements());
    }

    public String get(Object obj)
    {
        String key = obj.toString();

        String data = _cache.get(key);
        if (data == null && _cacheOnDisk)
        {
            // Memory cache miss... try disk
            try
            {
                String keyFile = getKeyFile(key);
                List<String> lines = Files.read(keyFile);
                if (lines.size() > 0)
                {
                    data = String.join("\n", lines);
                    _cache.put(key, data);
                }

                if (data != null && data.length() < 1) {
                    data = null;
                }

                _log.info(String.format("Cache file '%s' read.", keyFile));
            }
            catch (IOException ioe)
            {
                // Disk cache miss
                data = null;
                _log.error(String.format("Failure reading cache file: '%s'.", ioe.getMessage()));
            }
        }

        return data;
    }

    public String remove(Object obj)
    {
        String key = obj.toString();
        String data = get(key);

        if (data != null)
        {
            _cache.remove(key);

            if (_cacheOnDisk)
            {
                String keyFile = getKeyFile(key);
                Files.delete(keyFile);
                _log.info(String.format("Cache file '%s' deleted.", keyFile));
            }
        }

        return data;
    }

    public String put(String key, String data)
    {
        // Cache in memory
        _cache.put(key, data);

        // Cache on disk
        if (_cacheOnDisk)
        {
            try
            {
                String keyFile = getKeyFile(key);
                Files.write(keyFile, data);
                _log.info(String.format("Cache file '%s' written.", keyFile));
            }
            catch (IOException ioe)
            {
                _log.error(String.format("Failure writing cache file: '%s'.", ioe.getMessage()));
            }
        }

        return data;
    }

    private String getKeyFile(String key)
    {
        return String.format("%s/%s.%s", _dataPath, key.replace(':', '/'), _dataFormat);
    }

    public void putAll(Map<? extends String,? extends String> map)
    {
        Set<? extends String> keySet = map.keySet();
        for (String key : keySet)
        {
            put(key, map.get(key));
        }
    }

    public String save() throws IOException {
        for (String key : this.keySet()) {
            String data = get(key);
            String keyFile = getKeyFile(key);
            Files.write(keyFile, data);
        }

        return _dataPath;
    }
}

