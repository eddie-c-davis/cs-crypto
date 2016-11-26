package cs.crypto;

/**
 * Created by edavis on 11/25/16.
 */
public interface Request {
    //public String cacheKey();
    public String run() throws RequestException;
}
