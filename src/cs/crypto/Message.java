package cs.crypto;

import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by edavis on 11/24/16.
 */
public class Message implements Serializable, Jsonizable {
    private static int _counter = 0;

    private int _count;

    private String _from;
    private String _body;

    private BigInteger _cSym = BigInteger.ZERO;
    private List<Pair<BigInteger>> _cPairs;

    public Message(String from, String body) {
        increment();
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
        increment();
    }

    public void set(String from, String body) {
        _from = from;
        _body = body;
    }

    private void increment() {
        if (_count > _counter) {
            _counter = _count;
        }

        _counter += 1;
        _count = _counter;
    }

    public static int counter() {
        return _counter;
    }

    public int count() {
        return _count;
    }

    public String from() {
        return _from;
    }

    public String body() {
        return _body;
    }

    public boolean encrypted() {
        return !_cSym.equals(BigInteger.ZERO);
    }

    public BigInteger cSym() {
        return _cSym;
    }

    public List<Pair<BigInteger>> cPairs() {
        return _cPairs;
    }

    public String toJSON() {
        JSONObject obj = new JSONObject();
        if (encrypted()) {
            obj.put("cSym", _cSym.toString());

            String pairStr = "[";
            for (Pair<BigInteger> pair : _cPairs) {
                pairStr = String.format("%s%s,", pairStr, pair.toString());
            }

            pairStr = String.format("%s]", pairStr);
            obj.put("cPairs", pairStr);
        } else {
            obj.put("from", _from);
            obj.put("body", _body);
        }

        return obj.toString();
    }
}
