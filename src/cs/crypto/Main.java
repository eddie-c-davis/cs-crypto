package cs.crypto;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        //bigIntTest();       // MyBigInt Test
        //elgamalTest();    // Elgamal test...

        // Test Case: g=5 h = 3668993056 p = 9048610007 x = 948603
        if (args.length == 1 && args[0].indexOf('h') >= 0) {
            System.out.println("usage: shank <g> <h> <p> [sam]");
        } else {
            long t_i = System.nanoTime();

            if (args.length < 1) {
                shankRandom();
            } else if (args.length > 2) {
                //shankLong(args);
                shankBigInt(args);      // ~90 ms is the time to beat....
            }

            long t_r = (System.nanoTime() - t_i) / 1000000L;
            System.out.println(String.format(" (%d msec)", t_r));
        }
    }

    private static void shankLong(String[] args) {
        long g = Long.parseUnsignedLong(args[0]);       //long g = 3L, 2L;
        long h = Long.parseUnsignedLong(args[1]);       //long h = 6L, 3L;
        long p = Long.parseUnsignedLong(args[2]);       //long p = 31L, 29L;

        long x = (new ShankBabyGiantLong(h, g, p)).run();

        System.out.print(String.format("x = %d", x));
    }

    private static void shankBigInt(String[] args) {
        BigInteger g = new BigInteger(args[0]);         //long h = 6L, 3L;
        BigInteger h = new BigInteger(args[1]);         //long g = 3L, 2L;
        BigInteger p = new BigInteger(args[2]);         //long p = 31L, 29L;

        boolean useSAM = (args.length > 3 && args[3].length() > 0);

        BigInteger x = (new ShankBabyGiantBigInt(h, g, p, useSAM)).run();

        System.out.print("x = " + x.toString());
    }

    private static void shankRandom() {
        // Generate a large prime...
        PrimeGenerator pGen = new PrimeGenerator(40, 100);
        BigInteger g = pGen.getG();
        BigInteger p = pGen.getP();
        BigInteger h = pGen.getH();

        BigInteger x = (new ShankBabyGiantBigInt(h, g, p)).run();

        System.out.print("x = " + x.toString());
    }

    private static void bigIntTest() {
        BigInteger prime = new BigInteger("9048610007");
        //BigInteger biExp = BigInteger.valueOf(exp);
        BigInteger base = new BigInteger("3668993056");
        //int exp = 948603;
        BigInteger exp = BigInteger.valueOf(948603);

        MyBigInt myBase = new MyBigInt(base);
        MyBigInt myExp = new MyBigInt(exp);
        MyBigInt myPrime = new MyBigInt(prime);

        long t_ib = System.nanoTime();
        BigInteger res = base.modPow(exp, prime);
        long t_rb = System.nanoTime() - t_ib;

        long t_im = System.nanoTime();
        MyBigInt myRes = myBase.modPow(myExp, myPrime);
        long t_rm = System.nanoTime() - t_im;

        //long dt = (t_rb - t_rm) / 1000000L;
        double speedup = (double) t_rb / (double) t_rm;
        System.out.format("SquareAndMultiply method yields %f speedup!%n", speedup);
    }

    private static void elgamalTest() {
        ElgamalUser bob = new ElgamalUser("Bob");
        bob.init();

        ElgamalUser alice = new ElgamalUser("Alice");
        alice.send(bob, "Hello!");
    }
}
