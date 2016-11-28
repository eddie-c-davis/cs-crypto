package cs.crypto;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Created by edavis on 10/16/16.
 */
public interface User {
    Message send(ListServer server, String message) throws GeneralSecurityException, MessageException, UserException;

    void send(User receiver, String message);
    void send(User receiver, long m);
    void send(User receiver, BigInteger m);

    List<Message> receive(ListServer server) throws GeneralSecurityException;

    BigInteger receive(User sender, BigInteger c);
    BigInteger receive(User sender, BigInteger c1, BigInteger c2);

    String getName();
    Policy getPolicy();

    //boolean satisfies(Policy policy);
    void authenticate(KeyServer keyServer) throws PeapodException;
}
