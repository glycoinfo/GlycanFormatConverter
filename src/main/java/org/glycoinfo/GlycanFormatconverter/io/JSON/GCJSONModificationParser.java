package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONModificationParser {

    private GCJSONLinkageParser gclinParser;

    public GCJSONModificationParser () {
        gclinParser = new GCJSONLinkageParser();
    }

    public Substituent parseBridge (JSONObject _bridge) throws GlycanException {
        Linkage lin1 = null;
        Linkage lin2 = null;
        SubstituentInterface subInter = null;
        SubstituentUtility subUtil = new SubstituentUtility();

        for (String key : _bridge.keySet()) {
            switch (key) {
                case "PositionOne" :
                    lin1 = gclinParser.parsePosition(_bridge.getJSONObject(key));
                    break;

                case "PositionTwo" :
                    lin2 = gclinParser.parsePosition(_bridge.getJSONObject(key));
                    break;

                case "Notation" :
                    subInter = parseCrossLinkedTemplate(_bridge.getString(key));
                    break;
            }
        }

        if (subInter == null) return null;
        if (lin1 == null) lin1 = new Linkage();
        if (lin2 == null) lin2 = new Linkage();

        Substituent ret = new Substituent(subInter, lin1, lin2);
        return ret;//subUtil.modifyLinkageType(ret);
    }

    public Edge parseSubstituent (JSONObject _sub) throws GlycanException {
        Edge subEdge = new Edge();
        SubstituentInterface subInter = null;
        SubstituentUtility subUtil = new SubstituentUtility();
        Linkage positionOne = null;
        Linkage positionTwo = null;

        for (String key : _sub.keySet()) {
            switch (key) {
                case "PositionOne" :
                    positionOne = gclinParser.parsePosition(_sub.getJSONObject(key));
                    break;

                case "PositionTwo" :
                    positionTwo = gclinParser.parsePosition(_sub.getJSONObject(key));
                    break;

                case "Notation" :
                    if (positionTwo != null) {
                        subInter = parseCrossLinkedTemplate(_sub.getString(key));
                    } else {
                        subInter = parseSubstituentTemplate(_sub.getString(key));
                    }
                    break;
            }
        }

        Substituent sub = new Substituent(subInter, positionOne, positionTwo);//subUtil.modifyLinkageType(new Substituent(subInter, positionOne, positionTwo));
        subEdge.setSubstituent(sub);
        subEdge.addGlycosidicLinkage(positionOne);
        if (positionTwo != null) {
            subEdge.addGlycosidicLinkage(positionTwo);
        }

        return subEdge;
    }

    public ArrayList<GlyCoModification> parseModifications (JSONArray _mod) throws GlycanException {
        ArrayList<GlyCoModification> ret = new ArrayList<>();

        for (Object unit : _mod) {
            JSONObject mod = (JSONObject) unit;
            ModificationTemplate modTemp = null;
            int position = -1;

            for (String key : mod.keySet()) {
                switch (key) {
                    case "PositionOne" :
                        position = mod.getInt(key);
                        break;

                    case "Notation" :
                        modTemp = parseModificationTemplate(mod.getString(key));
                        break;
                }
            }

            if (modTemp == null) continue;

            GlyCoModification gMod = new GlyCoModification(modTemp, position);
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

    public SubstituentTemplate parseSubstituentTemplate (String _notation) {
        for (SubstituentTemplate value : SubstituentTemplate.values()) {
            if (_notation.equals(value.toString())) return value;
        }
        return null;
    }

    public ModificationTemplate parseModificationTemplate (String _notation) {
        for (ModificationTemplate value : ModificationTemplate.values()) {
            if (_notation.equals(value.toString())) return value;
        }
        return null;
    }

    public CrossLinkedTemplate parseCrossLinkedTemplate (String _notation) {
        for (CrossLinkedTemplate value : CrossLinkedTemplate.values()) {
            if (_notation.equals(value.toString())) return value;
        }
        return null;
    }
}
