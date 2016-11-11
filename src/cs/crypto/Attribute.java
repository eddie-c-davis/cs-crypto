package cs.crypto;

/**
 * Created by edavis on 10/16/16.
 */
public class Attribute {
    private static final char REQUIRED = 'v';
    private static final char FORBIDDEN = 'x';
    private static final char IRRELEVANT = '*';

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
}
