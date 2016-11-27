package cs.crypto;

import org.apache.log4j.Logger;
import org.json.JSONObject;

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
            KeyServer keyServer = KeyServer.instance();
            _user.authenticate(keyServer);

            ListServer listServer = ListServer.get();
            listServer.subscribe(_user);

            Message encrypted = _user.send(listServer, _message.body());

            JSONObject obj = new JSONObject();
            obj.put("sender", _user.getName());
            obj.put("body", _message.body());
            obj.put("listserver", listServer.getName());
            obj.put("keyserver", keyServer.getName());
            obj.put("encrypted", encrypted.toJSON());
            json = obj.toString();
        } catch (Exception ex) {
            throw new RequestException(ex.getMessage(), ex);
        } finally {
            return json;
        }
    }
}
