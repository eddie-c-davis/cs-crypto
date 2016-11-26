package cs.crypto;

import org.json.JSONObject;

/**
 * Created by edavis on 11/25/16.
 */
public class RequestException extends Exception {
    public RequestException() {
        super();
    }

    public RequestException(String message) {
        super(message);
    }

    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestException(Throwable cause) {
        super(cause);
    }

    public String toJSON() {
        JSONObject json = new JSONObject();
        json.put("message", this.getMessage());
        // TODO: Finish this up...
        //json.put("trace", Errors.stackTraceToString(ex));
        //json.put("time", Dates.getTimestamp());

        return json.toString();
    }
}
