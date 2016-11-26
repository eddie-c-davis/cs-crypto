package cs.crypto;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Date;

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
        StringWriter sw = new StringWriter();
        this.printStackTrace(new PrintWriter(sw));

        JSONObject json = new JSONObject();
        json.put("message", this.getMessage());
        json.put("trace", sw.toString());
        json.put("time", (new Timestamp((new Date()).getTime())).toString());

        return json.toString();
    }
}
