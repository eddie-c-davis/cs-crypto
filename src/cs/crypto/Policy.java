package cs.crypto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edavis on 10/16/16.
 */
public class Policy {
    private List<Attribute> _attributes = new ArrayList<>();

    private String _name;
    private String _key;

    public Policy(String name) {
        set(name, "");
    }

    public Policy(String name, String attribs) {
        set(name, attribs);
    }

    public void set(String name, String attribs) {
        _name = name;
        _key = attribs;

        List<Attribute> attrList = AttributeList.get().list();
        assert(attrList.size() == _key.length());

        for (int i = 0; i < _key.length(); i++) {
            Attribute other = attrList.get(i);
            Attribute attribute = new Attribute(other.getName(), _key.charAt(i));
            _attributes.add(attribute);
        }
    }

    public List<Attribute> attributes() {
        return _attributes;
    }

    public List<Attribute> getRequired() {
        return getMatching(Attribute.REQUIRED);
    }

    public List<Attribute> getForbidden() {
        return getMatching(Attribute.FORBIDDEN);
    }

    public List<Attribute> getIrrelvant() {
        return getMatching(Attribute.IRRELEVANT);
    }

    public List<Attribute> getMatching(char value) {
        List<Attribute> matches = new ArrayList<>(_attributes.size());
        for (Attribute attribute : _attributes) {
            if (attribute.match(value)) {
                matches.add(attribute);
            }
        }

        return matches;
    }

    public boolean satisfies(Policy other) {
        // Does this policy satisfy my policy?
        boolean satisfied = true;
        List<Attribute> thisAttribs = this.attributes();
        List<Attribute> otherAttribs = other.attributes();

        for (int i = 0; i < thisAttribs.size() && satisfied; i++) {
            Attribute thisAttrib = thisAttribs.get(i);
            Attribute otherAttrib = otherAttribs.get(i);
            satisfied &= thisAttrib.match(otherAttrib);
        }

        return satisfied;
    }

    public int size() {
        return _attributes.size();
    }

    @Override
    public String toString() {
        String str = String.format("%s: <", _name);
        for (Attribute attribute : _attributes) {
            str = String.format("%s%c,", str, attribute.getValue());
        }

        str = String.format("%s>", str);

        return str;
    }
}
