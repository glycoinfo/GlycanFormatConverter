package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONRepeatParser {

    private GCJSONLinkageParser gclinParser;
    private GlyContainer glyCo;
    private Node current;
    private Node parent;

    public GCJSONRepeatParser (GlyContainer _glyCo, Node _current, Node _parent) {
        glyCo = _glyCo;
        current = _current;
        parent = _parent;
        gclinParser = new GCJSONLinkageParser();
    }

    public GlyContainer makeRepeatingEdge (Object _repeat) throws GlycanException {
        Edge edge = new Edge();
        Linkage lin = gclinParser.parsePosition((JSONObject) _repeat);
        edge.addGlycosidicLinkage(lin);

        Substituent repMod = parseRepeat((JSONObject) _repeat);
        edge.setSubstituent(repMod);
        repMod.addParentEdge(edge);

        glyCo.addEdge(parent, current, edge);

        return glyCo;
    }

    public Substituent parseRepeatingBridge (JSONObject _rep) throws GlycanException {
        Linkage lin1 = null;
        Linkage lin2 = null;
        SubstituentInterface subInter = null;
        SubstituentUtility subUtil = new SubstituentUtility();

        for (String key : _rep.keySet()) {
            switch (key) {
                case "PositionOne" :
                    lin1 = gclinParser.parsePosition(_rep.getJSONObject(key));
                    break;

                case "PositionTwo" :
                    lin2 = gclinParser.parsePosition(_rep.getJSONObject(key));
                    break;

                case "Notation" :
                    subInter = parseCrossLinkedTemplate(_rep.getString(key));
                    break;
            }
        }

        if (lin1 == null) lin1 = new Linkage();
        if (lin2 == null) lin2 = new Linkage();
        GlycanRepeatModification ret = new GlycanRepeatModification(subInter);
        ret.setFirstPosition(lin1);
        ret.setSecondPosition(lin2);

        return ret;//subUtil.modifyLinkageType(ret);
    }

    public Substituent parseRepeat (JSONObject _rep) throws GlycanException {
        int max = -1;
        int min = -1;
        GlycanRepeatModification retMod = null;

        for (String key : _rep.keySet()) {
            switch (key) {
                case "Max" :
                    max = _rep.getInt(key);
                    break;

                case "Min" :
                    min = _rep.getInt(key);
                    break;

                case "Bridge" :
                    retMod = (GlycanRepeatModification) parseRepeatingBridge(_rep.getJSONObject(key));
                    break;
            }
        }

        retMod.setMinRepeatCount(min);
        retMod.setMaxRepeatCount(max);

        return retMod;
    }

    public CrossLinkedTemplate parseCrossLinkedTemplate (String _notation) {
        for (CrossLinkedTemplate value : CrossLinkedTemplate.values()) {
            if (_notation.equals(value.toString())) return value;
        }
        return null;
    }

}
