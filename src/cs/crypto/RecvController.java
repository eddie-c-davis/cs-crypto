package cs.crypto;

/**
 * Created by edavis on 11/26/16.
 */
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RecvController {
    private static Logger _log = Logger.getLogger(RecvController.class.getName());

    @RequestMapping("/recv")
    public String recvRequest(@RequestParam(value="user", required=true, defaultValue="") String user,
                              HttpServletRequest request) {
        String result = "";
        Request req = new RecvRequest(user);

        _log.info(String.format("Received Recv request from '%s', on %s.", user, request.getRemoteAddr()));

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
