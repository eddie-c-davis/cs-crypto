package cs.crypto;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by edavis on 11/10/16.
 */
public class AttributeList {
    private static AttributeList _inst = null;

    private List<Attribute> _attributes;

    public static AttributeList get() {
        if (_inst == null) {
            _inst = new AttributeList();
        }

        return _inst;
    }

    private AttributeList() {
        _attributes = new ArrayList<>();
    }

    public List<Attribute> list() {
        if (_attributes.size() < 1) {
            read();
        }

        return _attributes;
    }

    public void read() {
        Properties props = PropReader.read("attributes.properties");
        int nAttribs = Integer.parseInt(props.getProperty("count"));
        for (int i = 1; i <= nAttribs; i++) {
            String key = "attr" + i;
            String attr = props.getProperty(key);
            _attributes.add(new Attribute(attr));
        }
    }
}
