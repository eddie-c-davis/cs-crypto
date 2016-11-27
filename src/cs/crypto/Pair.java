package cs.crypto;

import java.io.Serializable;

/**
 * Created by edavis on 11/24/16.
 */
public class Pair<T> implements Serializable {
    private T _first; //first member of pair
    private T _second; //second member of pair

    public Pair(T first, T second) {
        set(first, second);
    }

    public void set(T first, T second) {
        _first = first;
        _second = second;
    }

    public T first() {
        return _first;
    }

    public T second() {
        return _second;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)", _first.toString(), _second.toString());
    }
}
