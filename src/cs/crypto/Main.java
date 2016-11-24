package cs.crypto;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            peapodTest();
        } catch (Exception ex) {
            print(ex.toString());
            ex.printStackTrace(System.out);
        }

        if (args.length == 1 && args[0].indexOf('h') >= 0) {
            System.out.println("usage 1: rsa <x> <p> <q> <e> [sam]");
            System.out.println("usage 2: elgamal <x> <g> <h> <p> [sam]");
        } else {
            long t_i = System.nanoTime();

            char encSys = args[0].charAt(0);
            if (encSys == 'R' || encSys == 'r') {
                rsaTest(args);            // RSA test
            } else {
                elgamalTest(args);        // Elgamal test
            }

            long t_r = (System.nanoTime() - t_i) / 1000000L;
            System.out.println(String.format("Runtime: %d ms", t_r));
        }
    }

    private static void peapodTest() throws GeneralSecurityException, PeapodException {
        // Create a couple users...
        User alice = new PeapodUser("Alice");
        User bob = new PeapodUser("Bob");

        // Initialize key server (generate K).
        KeyServer keyServer = new KeyServer();
        print("Initializing key server...");
        keyServer.init();

        // Authenticate users with KeyServer...
        print("Authenticating Alice with key server...");
        alice.authenticate(keyServer);
        print("Authenticating Bob with key server...");
        bob.authenticate(keyServer);

        // Attribute test...
        //boolean satisfied = alice.getPolicy().satisfies(bob.getPolicy());

        // Register list server with the key server...
        ListServer listServer = new ListServer();
        print("Registering list server with key server...");
        listServer.register(keyServer);

        // Subscribe users to the list server...
        print("Subscribing Alice to list server...");
        listServer.subscribe(alice);
        print("Subscribing Bob to list server...");
        listServer.subscribe(bob);

        // Now try sending a message to the server...
        print("Alice sending message to list server...");
        alice.send(listServer, "Roommate needed...");

        print("Bob checking list server for messages...");
        bob.receive(listServer);
    }

    private static void elgamalTest(String[] args) {
        ElgamalUser bob;
        ElgamalUser alice;

        BigInteger x = MyBigInt.parse(args[1]);

        if (args.length > 4) {
            BigInteger g = MyBigInt.parse(args[2]);
            BigInteger h = MyBigInt.parse(args[3]);
            BigInteger p = MyBigInt.parse(args[4]);

            bob = new ElgamalUser("Bob", g, h, p);
            alice = new ElgamalUser("Alice", g, h, p);

            boolean useSAM = (args.length > 5 && args[5].length() > 0);

            bob.setSAM(useSAM);
            alice.setSAM(useSAM);
        } else {
            int bitLen = 1024;
            if (args.length > 2) {
                bitLen = Integer.parseInt(args[2]);
            }

            bob = new ElgamalUser("Bob", bitLen);
            alice = new ElgamalUser("Alice", bitLen);
        }

        bob.init();
        alice.send(bob, x);
    }

    private static void rsaTest(String[] args) {
        // Parameters from book...
        // p = E0DFD2C2A288ACEBC705EFAB30E4447541A8C5A47A37185C5A9CB98389CE4DE19199AA3069B404FD98C801568CB9170EB712BF10B4955CE9C9DC8CE6855C6123h
        // q = EBE0FCF21866FD9A9F0D72F7994875A8D92E67AEE4B515136B2A778A8048B149828AEA30BD0BA34B977982A3D42168F594CA99F3981DDABFAB2369F229640115h
        // e = 40B028E1E4CCF07537643101FF72444A0BE1D7682F1EDB553E3AB4F6DD8293CA1945DB12D796AE9244D60565C2EB692A89B8881D58D278562ED60066DD8211E67315CF89857167206120405B08B54D10D4EC4ED4253C75FA74098FE3F7FB751FF5121353C554391E114C85B56A9725E9BD5685D6C9C7EED8EE442366353DC39h

        BigInteger x = MyBigInt.parse(args[1]);
        BigInteger p = MyBigInt.parse(args[2]);
        BigInteger q = MyBigInt.parse(args[3]);
        BigInteger e = MyBigInt.parse(args[4]);

        boolean useSAM = (args.length > 5 && args[5].length() > 0);

        RSAUser bob = new RSAUser("Bob", p, q, e);
        bob.setSAM(useSAM);
        bob.init();

        RSAUser alice = new RSAUser("Alice");
        alice.setSAM(useSAM);
        alice.send(bob, x);
    }

    private static void print(String str) {
        System.out.println(str);
    }
}
