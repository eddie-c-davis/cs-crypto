package cs.crypto;

import java.math.BigInteger;

/**
 * Created by edavis on 10/16/16.
 */
public class Attribute extends ElgamalEntity {
    public static final char REQUIRED = 'v';
    public static final char FORBIDDEN = 'x';
    public static final char IRRELEVANT = '*';

    private String _name;
    private char _value;

    public Attribute(String name, boolean val) {
        set(name, val ? REQUIRED : FORBIDDEN);
    }

    public Attribute(String name, char val) {
        set(name, val);
    }

    public Attribute(String name) {
        set(name, IRRELEVANT);
    }

    public Attribute() {
        set("", IRRELEVANT);
    }

    public void set(String name, char val) {
        _name = name;
        _value = val;
    }

    public String getName() {
        return _name;
    }

    public char getValue() {
        return _value;
    }

    public boolean match(Attribute other) {
        char thisVal = _value;
        char otherVal = other.getValue();

        return (thisVal == IRRELEVANT || thisVal == otherVal);
    }

    public boolean match(char other) {
        return _value == other;
    }

    public boolean required() {
        return _value == REQUIRED;
    }

    public boolean forbidden() {
        return _value == FORBIDDEN;
    }

    public boolean irrelevant() {
        return _value == IRRELEVANT;
    }

    public void publicKey(BigInteger kPub) {
        _kPub = kPub;
    }

    @Override
    public String toString() {
        return String.format("%s(%c)", _name, _value);
    }
}
