package test;

import cs.crypto.SendRequest;

/**
 * Created by edavis on 11/26/16.
 */
public class SendTest {
    public static void main(String[] args) {
        String user = args[0];
        String body = args[1];

        TestRunner runner = new TestRunner(new SendRequest(user, body));
        runner.run();
    }
}
