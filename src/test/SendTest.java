package test;

import cs.crypto.SendRequest;

/**
 * Created by edavis on 11/26/16.
 */
public class SendTest {
    public static void main(String[] args) {
        String user = args[0];
        String body = args[1];

        SendRequest req = new SendRequest(user, body);

        try {
            String json = req.run();
            System.out.println(json);
        } catch (Exception ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
    }
}
