package cs.crypto;

import java.util.Map;

/**
 * Created by edavis on 11/28/16.
 */
public class Cache {
    public static Map<String, String> instance() {
        Map<String, String> cache;
        if (RedisCache.isRunning()) {
            cache = RedisCache.instance();
        } else {
            cache = FileCache.instance();
        }

        return cache;
    }
}
