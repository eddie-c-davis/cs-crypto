package cs.crypto;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by edavis on 11/26/16.
 */
public class RecvRequest implements Request {
    private static Logger _log = Logger.getLogger(SendRequest.class.getName());

    private PeapodUser _user;

    public RecvRequest(String userName) {
        _user = PeapodUser.get(userName);
    }

    public String run() throws RequestException {
        String json = "";

        try {
            KeyServer keyServer = KeyServer.instance();
            _user.authenticate(keyServer);

            ListServer listServer = ListServer.get();
            listServer.subscribe(_user);

            List<Message> messages = _user.receive(listServer);

            // Ensure latest version of user is encached...
            _user.encache();

            JSONObject obj = new JSONObject();
            obj.put("receiver", _user.getName());
            obj.put("listserver", listServer.getName());
            obj.put("keyserver", keyServer.getName());

            String msgStr = "[";
            for (Message message : messages) {
                msgStr = String.format("%s%s,", msgStr, message.toJSON());
            }

            msgStr = String.format("%s]", msgStr);
            obj.put("messages", msgStr);

            json = obj.toString();
        } catch (Exception ex) {
            throw new RequestException(ex.getMessage(), ex);
        } finally {
            return json;
        }
    }
}
