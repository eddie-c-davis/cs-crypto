package test;

import cs.crypto.RecvRequest;

/**
 * Created by edavis on 11/27/16.
 */
public class RecvTest {
    public static void main(String[] args) {
        String user = args[0];
        //String body = args[1];

        TestRunner runner = new TestRunner(new RecvRequest(user));
        runner.run();
    }
}
