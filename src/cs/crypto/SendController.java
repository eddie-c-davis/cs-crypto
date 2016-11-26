package cs.crypto;

/**
 * Created by edavis on 11/25/16.
 */
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SendController {
    private static Logger _log = Logger.getLogger(SendController.class.getName());

    @RequestMapping("/send")
    public String sendRequest(@RequestParam(value="user", required=true, defaultValue="") String user,
                                 @RequestParam(value="body", required=true, defaultValue="") String body,
                                 HttpServletRequest request) {
        String result = "";
        Request req = new SendRequest(user, body);

        _log.info(String.format("Received Send request from '%s', on %s.", user, request.getRemoteAddr()));

        try {
            result = req.run();
        } catch (Exception ex) {
            result = (new RequestException(ex).toJSON());
        } finally {
            return result;
        }
    }

    @ExceptionHandler({RequestException.class})
    public String requestError(RequestException ex) {
        return ex.toJSON();
    }
}
