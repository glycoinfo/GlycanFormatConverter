package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by e15d5605 on 2017/09/19.
 */
public class GCJSONImporter {

    public GlyContainer start (String _json) throws GlycanException {
        JSONObject glycan = new JSONObject(_json);

        JSONObject monosaccharides = glycan.getJSONObject("Monosaccharides");

        /* Monosaccharide to Node */
        HashMap<String, Node> nodeIndex = openMonosaccharides(monosaccharides);

        /* Parse Edges */
        GCJSONEdgeParser gcEdgeParser = new GCJSONEdgeParser(nodeIndex);
        GlyContainer ret = gcEdgeParser.parseEdge(monosaccharides);

        /* Parse ambiguous substituents */
        GCJSONFragmentsParser gcFragParser = new GCJSONFragmentsParser(nodeIndex);
        for (GlycanUndefinedUnit und : gcFragParser.parseSubFragments(glycan.getJSONObject("SubFragments"))) {
            ret.addGlycanUndefinedUnit(und);
        }

        return ret;
    }

    public HashMap<String, Node> openMonosaccharides (JSONObject _monosaccharides) throws GlycanException {
        HashMap<String, Node> ret = new HashMap<>();

        for (String unit : _monosaccharides.keySet()) {
            JSONObject monosaccharide = _monosaccharides.getJSONObject(unit);
            ret.put(unit, parseMonosaccharide(monosaccharide));
        }

        return ret;
    }

    public Node parseMonosaccharide (JSONObject _monosaccharide) throws GlycanException {
        Monosaccharide ret = new Monosaccharide();
        GCJSONModificationParser gcModParser = new GCJSONModificationParser();

        for (String key : _monosaccharide.keySet()) {
            switch (key) {

                case "AnomericPosition" :
                    ret.setAnomericPosition(_monosaccharide.getInt(key));
                    break;

                case "AnomericSymbol" :
                    ret.setAnomer(parseAnomericState(_monosaccharide.getString(key)));
                    break;

                case "Modifications" :
                    ret.setModification(gcModParser.parseModifications(_monosaccharide.getJSONArray(key)));
                    break;

                case "RingEnd" :
                    ret.setRingEnd(_monosaccharide.getInt(key));
                    break;

                case "RingStart" :
                    ret.setRingStart(_monosaccharide.getInt(key));
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

                case "TrivialName" :
                    ret.setStereos(parseTrivialNames(_monosaccharide.getJSONArray(key)));
                    break;
            }
        }

        return ret;
    }

    private AnomericStateDescriptor parseAnomericState (String _anomState) {
        for (AnomericStateDescriptor value : AnomericStateDescriptor.values()) {
            if (_anomState.equals(value.toString())) return value;
        }
        return null;
    }

    private SuperClass parseSuperClass (String _key) {
        for (SuperClass value : SuperClass.values()) {
            if (_key.equals(value.toString())) return value;
        }
        return null;
    }

    private LinkedList<String> parseTrivialNames (JSONArray _trivialName) {
        LinkedList<String> ret = new LinkedList<>();
        for (Object stereo : _trivialName) {
           ret.add(((String) stereo).toLowerCase());
        }
        return ret;
    }
}
