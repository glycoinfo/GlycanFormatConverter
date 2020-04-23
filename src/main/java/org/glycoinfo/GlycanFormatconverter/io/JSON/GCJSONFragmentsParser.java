package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONFragmentsParser {

    private HashMap<String, Node> nodeIndex;

    public GCJSONFragmentsParser (HashMap<String, Node> _nodeIndex) {
        nodeIndex = _nodeIndex;
    }

    public GlyContainer start (JSONObject _fragment, GlyContainer _glyCo) throws GlycanException {

        for (String id : _fragment.keySet()) {
            GlycanUndefinedUnit und = new GlycanUndefinedUnit();
            JSONObject frgObj = _fragment.getJSONObject(id);

            Edge edge = new Edge();
            Linkage lin = new Linkage();

            // extract acceptor side
            for (Object mid : frgObj.getJSONObject("Acceptor").getJSONArray("Node")) {
                und.addParentNode(nodeIndex.get(mid));
            }

            // extract linkage position
            lin.setParentLinkages(parsePosition(frgObj.getJSONObject("Acceptor").getJSONArray("Position")));
            lin.setChildLinkages(parsePosition(frgObj.getJSONObject("Donor").getJSONArray("Position")));

            // extract probability
            lin.setProbabilityLower(parseProbability(frgObj.getJSONObject("Probability").get("Low")));
            lin.setProbabilityUpper(parseProbability(frgObj.getJSONObject("Probability").get("High")));

            // extract LinkageType
            lin.setParentLinkageType(parseLinkageType(frgObj.getJSONObject("Acceptor").get("LinkageType")));
            lin.setChildLinkageType(parseLinkageType(frgObj.getJSONObject("Donor").get("LinkageType")));

            // extract donor side
            Node root;
            if (isSubstituentFragment(frgObj)) {
                root = new Substituent(parseSubstituentTemplate((String) frgObj.getJSONObject("Donor").get("Notation")), lin);
                edge.setSubstituent(root);
            } else {
                root = nodeIndex.get(frgObj.getJSONObject("Donor").get("Node"));
                edge.setChild(root);
            }

            if (root == null) throw new GlycanException ("fragment root is not defined.");

            root.addParentEdge(edge);

            und.addNode(root);
            edge.addGlycosidicLinkage(lin);
            und.setConnection(edge);

            _glyCo.addGlycanUndefinedUnit(und);
        }

        return _glyCo;
    }

    private boolean isSubstituentFragment (JSONObject _frgObj) {
        return (_frgObj.getJSONObject("Donor").has("Notation"));
    }

    public BaseSubstituentTemplate parseSubstituentTemplate (String _notation) {
        for (BaseSubstituentTemplate value : BaseSubstituentTemplate.values()) {
            if (_notation.equals(value.getIUPACnotation())) return value;
        }
        return null;
    }

    private double parseProbability (Object _prob) {
        double ret = 1.0;
        if (_prob instanceof Integer) {
            ret = (int) _prob;
        }
        return ret;
    }

    private LinkageType parseLinkageType (Object _type) {
        String type = (String) _type;

        switch (type) {
            case "DEOXY" :
                return LinkageType.DEOXY;
            case "H_AT_OH" :
                return LinkageType.H_AT_OH;
            case "NONMONOSACCHARIDE" :
                return LinkageType.NONMONOSACCHARIDE;
            case "UNVALIDATED" :
                return LinkageType.UNVALIDATED;
            case "H_LOSE" :
                return LinkageType.H_LOSE;
            case "R_CONFIG" :
                return LinkageType.R_CONFIG;
            case "S_CONFIG" :
                return LinkageType.S_CONFIG;
        }

        return LinkageType.UNVALIDATED;
    }

    private ArrayList<Integer> parsePosition (JSONArray _position) {
        ArrayList<Integer> ret = new ArrayList<>();
        for (Object pos: _position) {
            ret.add((Integer) pos);
        }
        return ret;
    }
}
