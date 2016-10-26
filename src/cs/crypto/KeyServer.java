package cs.crypto;

/**
 * Created by edavis on 10/17/16.
 */
public class KeyServer extends ElgamalEntity {

    public KeyServer() {
        this(DEFAULT_BITLEN);
    }

    public KeyServer(int bitLen) {
        super("KeyServer", bitLen);
    }


}
