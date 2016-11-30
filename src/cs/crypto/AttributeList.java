package cs.crypto;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Created by edavis on 11/10/16.
 */
public class AttributeList {
    private static AttributeList _inst = null;

    private List<Attribute> _attributes;
    private List<Attribute> _randomList;

    public static AttributeList get() {
        if (_inst == null) {
            _inst = new AttributeList();
        }

        return _inst;
    }

    private AttributeList() {
        _attributes = new ArrayList<>();
        _randomList = new ArrayList<>();
    }

    public List<Attribute> list() {
        if (_attributes.size() < 1) {
            read();
        }

        return _attributes;
    }

    public List<Attribute> random() {
        // Select a random number of random attributes
        Random rand = new Random();
        int randCount = rand.nextInt(_randomList.size());
        if (randCount < 1) {
            randCount = 1;
        }

        List<Attribute> subList = new ArrayList<>(randCount);
        for (int i = 0; i < randCount; i++) {
            // Select a random attribute at random...
            int randIndex = rand.nextInt(_randomList.size());
            subList.add(_randomList.get(randIndex));
        }

        return subList;
    }

    public void read() {
        // Add an equal number of random attributes...

        Properties props = PropReader.read("attributes.properties");
        int nAttribs = Integer.parseInt(props.getProperty("count"));
        for (int i = 1; i <= nAttribs; i++) {
            String key = "attr" + i;
            String attr = props.getProperty(key);
            _attributes.add(new Attribute(attr));
            _randomList.add(new RandomAttribute());
        }
    }
}
