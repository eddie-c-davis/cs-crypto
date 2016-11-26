package cs.crypto;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Created by edavis on 11/26/16.
 */
public class RecvRequest implements Request {
    private static Logger _log = Logger.getLogger(SendRequest.class.getName());

    private User _user;

    public RecvRequest(String userName) {
        _user = PeapodUser.get(userName);
    }

    public String run() throws RequestException {
        String json = "";

        try {
            KeyServer keyServer = KeyServer.get();
            _user.authenticate(keyServer);

            ListServer listServer = ListServer.get();
            listServer.subscribe(_user);

            _user.receive(listServer);

            JSONObject obj = new JSONObject();
            // TODO: Populate object...
            json = obj.toString();
        } catch (Exception ex) {
            throw new RequestException(ex.getMessage(), ex);
        } finally {
            return json;
        }
    }
}
