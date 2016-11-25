package cs.crypto;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by edavis on 11/24/16.
 */
public class Message {
    private String _from;
    private String _body;

    private BigInteger _cSym;
    private List<Pair<BigInteger>> _cPairs;

    public Message(String from, String body) {
       set(from, body);
    }

    public Message() {
        set("", "");
    }

    public Message(BigInteger cSym, List<Pair<BigInteger>> cPairs) {
        this("", cSym, cPairs);
    }

    public Message(String from, BigInteger cSym, List<Pair<BigInteger>> cPairs) {
        _from = from;
        _cSym = cSym;
        _cPairs = cPairs;
    }

    public void set(String from, String body) {
        _from = from;
        _body = body;
    }

    public String from() {
        return _from;
    }

    public String body() {
        return _body;
    }

    public BigInteger cSym() {
        return _cSym;
    }

    public List<Pair<BigInteger>> cPairs() {
        return _cPairs;
    }
}
