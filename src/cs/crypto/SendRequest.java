package cs.crypto;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

/**
 * Created by edavis on 11/25/16.
 */
public class SendRequest implements Request {
    private static Logger _log = Logger.getLogger(SendRequest.class.getName());

    private User _user;
    private Message _message;

    public SendRequest(String userName, String msgBody) {
        _user = PeapodUser.get(userName);
        _message = new Message(userName, msgBody);
    }

    public String run() throws RequestException {
        String json = "";

        try {
            ListServer listServer = ListServer.get();
            _user.send(listServer, _message.body());

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
