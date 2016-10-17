package cs.crypto;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1 && args[0].indexOf('h') >= 0) {
            System.out.println("usage: rsa <x> <p> <q> [e] [sam]");
        } else {
            long t_i = System.nanoTime();

            //elgamalTest(args);      // Elgamal test
            rsaTest(args);            // RSA test

            long t_r = (System.nanoTime() - t_i) / 1000000L;
            System.out.println(String.format("Runtime: %d ms", t_r));
        }
    }

    private static void elgamalTest(String[] args) {
        ElgamalUser bob = new ElgamalUser("Bob");
        bob.init();

        ElgamalUser alice = new ElgamalUser("Alice");
        alice.send(bob, "Hello!");
    }

    private static void rsaTest(String[] args) {
        long x = 0;
        if (args.length > 0) {
            x = Long.parseLong(args[0]);
        }

        long p = 0;
        if (args.length > 1) {
            p = Long.parseLong(args[1]);
        }

        long q = 0;
        if (args.length > 2) {
            q = Long.parseLong(args[2]);
        }

        long e = 0;
        if (args.length > 3) {
            e = Long.parseLong(args[3]);
        }

        boolean useSAM = (args.length > 4 && args[4].length() > 0);

        RSAUser bob = new RSAUser("Bob", p, q, e);
        bob.setSAM(useSAM);
        bob.init();

        RSAUser alice = new RSAUser("Alice");
        alice.setSAM(useSAM);
        alice.send(bob, x);
    }
}
