package cs.crypto;

import java.util.Random;

/**
 * Created by edavis on 11/30/16.
 */
public class RandomAttribute extends Attribute {
    private static final int MAX_NAME_LEN = 24;

    public RandomAttribute() {
        set(getRandomName(), IRRELEVANT);
    }

    private String getRandomName() {
        int minChar = (int) 'A';
        int maxChar = (int) 'z';
        int charRange = maxChar - minChar;

        Random gen = new Random();
        int randLength = gen.nextInt(MAX_NAME_LEN);
        if (randLength < 1) {
            randLength = 1;
        }

        StringBuilder sb = new StringBuilder(randLength);
        for (int i = 0; i < randLength; i++) {
            int nextChar = minChar + gen.nextInt(charRange);
            if (nextChar > maxChar) {
                nextChar = maxChar;
            }

            sb.append((char) nextChar);
        }

        return sb.toString();
    }
}

