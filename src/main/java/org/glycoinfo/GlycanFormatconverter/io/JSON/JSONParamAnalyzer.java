package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.BaseCrossLinkedTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.BaseSubstituentTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.LinkageType;
import org.glycoinfo.GlycanFormatconverter.Glycan.SubstituentInterface;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONParamAnalyzer {

    protected static JSONObject extractBridgeBlock (JSONObject _edge, JSONObject _bridge) {
        String start = (String) _edge.getJSONObject("Acceptor").get("Node");
        String end = (String) _edge.getJSONObject("Donor").get("Node");

        for (String key : _bridge.keySet()) {
            JSONObject bridge = _bridge.getJSONObject(key);
            String acceptor = (String) bridge.getJSONObject("Acceptor").get("Node");
            String donor = (String) bridge.getJSONObject("Donor").get("Node");
            if (acceptor.equals(start) && donor.equals(end)) {
                return bridge;
            }
        }

        return null;
    }

    protected static SubstituentInterface extractBridge (JSONObject _repeat, JSONObject _bridge) {
        JSONObject bridge = extractBridgeBlock(_repeat, _bridge);

        if (bridge != null) {
            return BaseCrossLinkedTemplate.forIUPACNotationWithIgnore((String) bridge.get("Notation"));
        } else {
            return null;
        }
    }

    protected static double parseProbability (Object _prob) {
        double ret = 1.0;

        if (_prob instanceof Integer) {
            ret = (int) _prob;
        }
        if (_prob instanceof Double) {
            ret = (double) _prob;
        }

        return ret;
    }

    protected static LinkageType parseLinkageType (Object _type) {
        String type = _type.toString();

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

    protected static ArrayList<Integer> parsePosition (JSONArray _position) {
        ArrayList<Integer> ret = new ArrayList<>();
        if (_position == null) {
            ret.add(-1);
            return ret;
        }

        for (Object pos: _position) {
            ret.add((Integer) pos);
        }
        return ret;
    }

    protected static BaseSubstituentTemplate parseSubstituentTemplate (String _notation) {
        for (BaseSubstituentTemplate value : BaseSubstituentTemplate.values()) {
            if (_notation.equals(value.getIUPACnotation())) return value;
        }
        return null;
    }

}
