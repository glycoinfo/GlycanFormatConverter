package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONFragmentsParser {

    private HashMap<String, Node> nodeIndex;
    private GCJSONLinkageParser gclinParser;

    public GCJSONFragmentsParser (HashMap<String, Node> _nodeIndex) {
        super();
        nodeIndex = _nodeIndex;
        gclinParser = new GCJSONLinkageParser();
    }

    public GlycanUndefinedUnit parseFragments (JSONObject _fragment, Node _root) throws GlycanException {
        GlycanUndefinedUnit ret = new GlycanUndefinedUnit();

        Linkage lin = new Linkage();

        for (String key : _fragment.keySet()) {
            switch (key) {
                case "LinkageType" :
                    lin = gclinParser.extractLinkageType(_fragment.getJSONObject("LinkageType"), lin);
                    break;

                case "Position" :
                    lin = gclinParser.extractPosition(_fragment.getJSONObject("Position"), lin);
                    break;

                case "Probability" :
                    lin = gclinParser.extractProbability(_fragment.getJSONObject("Probability"), lin);
                    break;
            }
        }

        if (!isComposition(_fragment)) {
            Edge edge = new Edge();
            edge.addGlycosidicLinkage(lin);
            edge.setChild(_root);

            _root.addParentEdge(edge);
            ret.setConnection(edge);
        }

        ret.addNode(_root);

        for (Object id : _fragment.getJSONArray("ParentNodeID")) {
            ret.addParentNode(nodeIndex.get(id.toString()));
        }

        return ret;
    }

    public ArrayList<GlycanUndefinedUnit> parseSubFragments (JSONObject _subFragments) throws GlycanException {
        ArrayList<GlycanUndefinedUnit> undes = new ArrayList<>();
        SubstituentUtility subUtil = new SubstituentUtility();

        for (String unit : _subFragments.keySet()) {
            JSONObject subUnit = _subFragments.getJSONObject(unit);

            GlycanUndefinedUnit und = new GlycanUndefinedUnit();

            Linkage lin = new Linkage();

            for (String key : subUnit.keySet()) {
                switch (key) {
                    case "LinkageType" :
                        lin = gclinParser.extractLinkageType(subUnit.getJSONObject("LinkageType"), lin);
                        break;

                    case "Position" :
                        lin = gclinParser.extractPosition(subUnit.getJSONObject("Position"), lin);
                        break;

                    case "Probability" :
                        lin = gclinParser.extractProbability(subUnit.getJSONObject("Probability"), lin);
                        break;
                }
            }

            Substituent root = new Substituent(parseSubstituentTemplate(subUnit.getString("Notation")), lin);
            root = subUtil.modifyLinkageType(root);

            Edge edge = new Edge();
            edge.addGlycosidicLinkage(lin);
            edge.setSubstituent(root);

            root.addParentEdge(edge);
            und.setConnection(edge);
            und.addNode(root);

            for (Object id : subUnit.getJSONArray("ParentNodeID")) {
                und.addParentNode(nodeIndex.get((id).toString()));
            }

            undes.add(und);
        }

        return undes;
    }

    public SubstituentTemplate parseSubstituentTemplate (String _notation) {
        for (SubstituentTemplate value : SubstituentTemplate.values()) {
            if (_notation.equals(value.toString())) return value;
        }
        return null;
    }

    private boolean isComposition (JSONObject _fragments) {
        return (_fragments.length() == 2);
    }
}
