package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONModificationParser {

    public GCJSONModificationParser () {

    }

    public Edge parseSubstituent (JSONObject _sub) throws GlycanException {
        Edge subEdge = new Edge();
        SubstituentInterface subFace = null;
        Linkage positionOne = new Linkage();

        for (String key : _sub.keySet()) {
            switch (key) {
                case "Status" :
                    break;
                case "Acceptor" :
                    JSONObject acceptor = (JSONObject) _sub.get(key);
                    positionOne.setParentLinkages(getPositions(acceptor.getJSONArray("Position")));
                    positionOne.setParentLinkageType(parseLinkageType(acceptor.get("LinkageType")));
                    break;
                case "Donor" :
                    JSONObject donor = (JSONObject) _sub.get(key);
                    positionOne.setChildLinkages(getPositions(donor.getJSONArray("Position")));
                    positionOne.setChildLinkageType(parseLinkageType(donor.get("LinkageType")));
                    break;
                case "Probability" :
                    break;
                case "Notation" :
                    subFace = parseSubstituentTemplate(_sub.getString(key));
                    break;
            }
        }

        Substituent sub = new Substituent(subFace, positionOne, null);
        if (sub.getFirstPosition().getParentLinkageType().equals(LinkageType.H_AT_OH)) {
            sub.setHeadAtom("O");
        }

        subEdge.setSubstituent(sub);
        subEdge.addGlycosidicLinkage(positionOne);

        return subEdge;
    }

    public ArrayList<GlyCoModification> parseModifications (JSONArray _mod) throws GlycanException {
        ArrayList<GlyCoModification> ret = new ArrayList<>();

        for (Object item : _mod) {
            JSONObject modObj = (JSONObject) item;
            ModificationTemplate modTemp = parseModificationTemplate((String) modObj.get("Notation"));
            Integer pos = (Integer) modObj.get("Position");

            GlyCoModification gMod = new GlyCoModification(modTemp, pos);
            ret.add(gMod);
        }

        return ret;
    }

    public ArrayList<Edge> parseSubstituents (JSONArray _subs) throws GlycanException {
        ArrayList<Edge> ret = new ArrayList<>();
        for (Object key : _subs) {
            ret.add(parseSubstituent((JSONObject) key));
        }

        return ret;
    }

    public BaseSubstituentTemplate parseSubstituentTemplate (String _notation) {
        for (BaseSubstituentTemplate value : BaseSubstituentTemplate.values()) {
            if (_notation.equals(value.getIUPACnotation())) return value;
        }
        return null;
    }

    public ModificationTemplate parseModificationTemplate (String _notation) {
        for (ModificationTemplate value : ModificationTemplate.values()) {
            if (_notation.equals(value.getIUPACnotation())) return value;
        }
        return null;
    }

    private LinkageType parseLinkageType (Object _type) {
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

    private ArrayList<Integer> getPositions (JSONArray _position) {
        ArrayList<Integer> ret = new ArrayList<>();
        for (Object pos : _position) {
            ret.add((Integer) pos);
        }
        return ret;
    }
}
