package test;

import cs.crypto.Request;

/**
 * Created by edavis on 11/27/16.
 */
public class TestRunner {
    private Request _req;

    public TestRunner(Request req) {
        _req = req;
    }

    public String run() {
        String output = "";

        try {
            output = _req.run();
            System.out.println(output);
        } catch (Exception ex) {
            output = ex.toString();
            System.out.println(output);
            ex.printStackTrace();
        } finally {
            return output;
        }
    }
}
