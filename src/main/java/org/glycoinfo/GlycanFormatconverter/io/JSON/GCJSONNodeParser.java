package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class GCJSONNodeParser {

    public HashMap<String, Node> start (JSONObject _monosaccharides) throws GlycanException {
        HashMap<String, Node> ret = new HashMap<>();

        for (String unit : _monosaccharides.keySet()) {
            JSONObject monosaccharide = _monosaccharides.getJSONObject(unit);
            ret.put(unit, parseMonosaccharide(monosaccharide));
        }

        return ret;
    }

    private Node parseMonosaccharide (JSONObject _monosaccharide) throws GlycanException {
        Monosaccharide ret = new Monosaccharide();
        GCJSONModificationParser gcModParser = new GCJSONModificationParser();

        Object[] mapkey = _monosaccharide.keySet().toArray();
        Arrays.sort(mapkey);

        for (Object item : mapkey) {
            String key = (String) item;

            switch (key) {
                case "AnomPosition" :
                    ret.setAnomericPosition((int) _monosaccharide.get(key));
                    break;

                case "AnomState" :
                    String anomState = (String) _monosaccharide.get(key);
                    ret.setAnomer(parseAnomericState(anomState.charAt(0)));
                    break;

                case "Modifications" :
                    ret.setModification(gcModParser.parseModifications(_monosaccharide.getJSONArray(key)));
                    break;

                case "RingSize" :
                    parseRingSize((String) _monosaccharide.get(key), ret);
                    break;

                case "SuperClass" :
                    ret.setSuperClass(parseSuperClass(_monosaccharide.getString(key)));
                    break;

                case "Substituents" :
                    for (Edge edge : gcModParser.parseSubstituents(_monosaccharide.getJSONArray(key))) {
                        edge.setParent(ret);
                        ret.addChildEdge(edge);
                    }
                    break;

                case "Notation" :

                    break;

                case "TrivialName" :
                    ret.setStereos(parseTrivialNames(_monosaccharide.getJSONArray("Configuration"), _monosaccharide.getJSONArray(key)));
                    break;
            }
        }

        return ret;
    }

    private Monosaccharide parseRingSize (String _ringSize, Monosaccharide _node) throws GlycanException {

        if (_ringSize.equals("p")) {
            if (_node.getAnomericPosition() == 1) {
                _node.setRingStart(1);
                _node.setRingEnd(5);
            } else {
                _node.setRingStart(2);
                _node.setRingEnd(6);
            }
        }
        if (_ringSize.equals("f")) {
            if (_node.getAnomericPosition() == 1) {
                _node.setRingStart(1);
                _node.setRingEnd(4);
            } else {
                _node.setRingStart(2);
                _node.setRingEnd(5);
            }
        }
        if (_ringSize.equals("")) {
            _node.setRing(_node.getAnomericPosition(), 0);
        }
        if (_ringSize.equals("?")) {
            _node.setRing(_node.getAnomericPosition(), -1);
        }

        return _node;
    }

    private AnomericStateDescriptor parseAnomericState (char _anomState) {
        for (AnomericStateDescriptor value : AnomericStateDescriptor.values()) {
            if (_anomState == value.getAnomericState()) {
                return value;
            }
        }
        return null;
    }

    private SuperClass parseSuperClass (String _key) {
        for (SuperClass value : SuperClass.values()) {
            if (_key.equals(value.toString())) return value;
        }
        return null;
    }

    private LinkedList<String> parseTrivialNames (JSONArray _configuration, JSONArray _trivialName) {
        LinkedList<String> ret = new LinkedList<>();

        for (int i = 0; i < _trivialName.length(); i++) {
            String c = (String) _configuration.get(i);
            String t = (String) _trivialName.get(i);
            if (c.equals("?")) {
                ret.add(t);
            } else {
                ret.add(c + t);
            }
        }
        return ret;
    }
}
