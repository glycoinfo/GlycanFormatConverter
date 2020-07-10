package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONModificationParser {

    public GCJSONModificationParser () {

    }

    public ArrayList<Edge> parseSubstituents (JSONArray _subs) throws GlycanException {
        ArrayList<Edge> ret = new ArrayList<>();
        for (Object key : _subs) {
            ret.add(parseSubstituent((JSONObject) key));
        }

        return ret;
    }

    private Edge parseSubstituent (JSONObject _sub) throws GlycanException {
        Edge subEdge = new Edge();
        SubstituentInterface subFace = null;
        Linkage positionOne = new Linkage();

        for (String key : _sub.keySet()) {
            switch (key) {
                case "Status" :
                    break;
                case "Acceptor" :
                    JSONObject acceptor = (JSONObject) _sub.get(key);
                    positionOne.setParentLinkages(JSONParamAnalyzer.parsePosition(acceptor.getJSONArray("Position")));
                    positionOne.setParentLinkageType(JSONParamAnalyzer.parseLinkageType(acceptor.get("LinkageType")));
                    break;
                case "Donor" :
                    JSONObject donor = (JSONObject) _sub.get(key);
                    positionOne.setChildLinkages(JSONParamAnalyzer.parsePosition(donor.getJSONArray("Position")));
                    positionOne.setChildLinkageType(JSONParamAnalyzer.parseLinkageType(donor.get("LinkageType")));
                    break;
                case "Probability" :
                    JSONObject probability = (JSONObject) _sub.get(key);
                    positionOne.setProbabilityUpper(JSONParamAnalyzer.parseProbability(_sub.getJSONObject("Probability").get("High")));
                    positionOne.setProbabilityLower(JSONParamAnalyzer.parseProbability(_sub.getJSONObject("Probability").get("Low")));
                    break;
                case "Notation" :
                    subFace = JSONParamAnalyzer.parseSubstituentTemplate(_sub.getString(key));
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

        HashMap<Integer, ArrayList<JSONObject>> mods = this.extractUnsaturationWithDeoxy(_mod);
        for (Integer key : mods.keySet()) {
            JSONObject modObj = mods.get(key).get(0);
            ModificationTemplate modTemp;
            if (this.hasUnsaturationWithDeoxy(mods.get(key))) {
                modTemp = parseUnsaturation(mods.get(key));
            } else {
                modTemp = modifyUnsaturation(parseModificationTemplate((String) modObj.get("Notation")));
            }
            GlyCoModification gMod = new GlyCoModification(modTemp, key);
            ret.add(gMod);
        }

        return ret;
    }

    private HashMap<Integer, ArrayList<JSONObject>> extractUnsaturationWithDeoxy(JSONArray _mod) {
        HashMap<Integer, ArrayList<JSONObject>> ret = new HashMap<>();

        for (Object item : _mod) {
            JSONObject modObj = (JSONObject) item;
            Integer pos = (Integer) modObj.get("Position");
            if (!ret.containsKey(pos)) {
                ArrayList<JSONObject> items = new ArrayList<>();
                items.add(modObj);
                ret.put(pos, items);
            } else {
                ret.get(pos).add(modObj);
            }
        }

        return ret;
    }

    private boolean hasUnsaturationWithDeoxy (ArrayList<JSONObject> _mods) {
        int result = 0;
        for (JSONObject item : _mods) {
            if (this.parseModificationTemplate((String) item.get("Notation")).equals(ModificationTemplate.UNSATURATION_EL)) {
                result++;
            }
            if (this.parseModificationTemplate((String) item.get("Notation")).equals(ModificationTemplate.UNSATURATION_FL)) {
                result++;
            }
            if (this.parseModificationTemplate((String) item.get("Notation")).equals(ModificationTemplate.UNSATURATION_ZL)) {
                result++;
            }
            if (this.parseModificationTemplate((String) item.get("Notation")).equals(ModificationTemplate.DEOXY)) {
                result++;
            }
        }

        return (result == 2);
    }

    private ModificationTemplate parseModificationTemplate (String _notation) {
        for (ModificationTemplate value : ModificationTemplate.values()) {
            if (_notation.equals(value.getIUPACnotation())) return value;
        }
        return null;
    }

    private ModificationTemplate parseUnsaturation (ArrayList<JSONObject> _mods) {
        ModificationTemplate ret = null;

        for (JSONObject item : _mods) {
            if (item.get("Notation").equals("(E)en")) {
                ret = ModificationTemplate.UNSATURATION_EL;
            }
            if (item.get("Notation").equals("(F)en")) {
                ret = ModificationTemplate.UNSATURATION_FL;
            }
            if (item.get("Notation").equals("(Z)en")) {
                ret = ModificationTemplate.UNSATURATION_ZL;
            }
        }

        return ret;
    }

    private ModificationTemplate modifyUnsaturation (ModificationTemplate _modTemp) {
        if (_modTemp == null) return null;

        if (_modTemp.equals(ModificationTemplate.UNSATURATION_FL)) {
            return ModificationTemplate.UNSATURATION_FU;
        }
        if (_modTemp.equals(ModificationTemplate.UNSATURATION_ZL)) {
            return ModificationTemplate.UNSATURATION_ZU;
        }
        if (_modTemp.equals(ModificationTemplate.UNSATURATION_EL)) {
            return ModificationTemplate.UNSATURATION_EU;
        }

        return _modTemp;
    }
}
