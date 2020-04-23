package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONRepeatParser {

    private HashMap<String, Node> nodeIndex;

    public GCJSONRepeatParser (HashMap<String, Node> _nodeIndex) {
        this.nodeIndex = _nodeIndex;
    }

    public GlyContainer start (JSONObject _repeat, JSONObject _bridge, GlyContainer _glyCo) throws GlycanException {

        for (String id: _repeat.keySet()) {
            JSONObject repObj = _repeat.getJSONObject(id);
            JSONObject bridgeObj = extractBridgeBlock(repObj, _bridge);

            GlycanRepeatModification repMod = new GlycanRepeatModification(extractBridge(repObj, _bridge));
            repMod.setFirstPosition(new Linkage());
            repMod.setSecondPosition(new Linkage());

            if (bridgeObj != null) {
                //repMod.getFirstPosition().setParentLinkages(parsePosition(repObj.getJSONObject("Acceptor").getJSONArray("Position")));
                repMod.getFirstPosition().setParentLinkageType(parseLinkageType(repObj.getJSONObject("Acceptor").get("LinkageType")));
                //repMod.getFirstPosition().setChildLinkages(parsePosition(null));
                repMod.getFirstPosition().setChildLinkageType(parseLinkageType(bridgeObj.getJSONObject("Donor").get("LinkageType")));


                //repMod.getSecondPosition().setParentLinkages(parsePosition(repObj.getJSONObject("Donor").getJSONArray("Position")));
                repMod.getSecondPosition().setParentLinkageType(parseLinkageType(bridgeObj.getJSONObject("Acceptor").get("LinkageType")));
                //repMod.getSecondPosition().setChildLinkages(parsePosition(null));
                repMod.getSecondPosition().setChildLinkageType(parseLinkageType(repObj.getJSONObject("Donor").get("LinkageType")));

                if (repMod.getFirstPosition().getParentLinkageType().equals(LinkageType.H_AT_OH)) {
                    repMod.setHeadAtom("O");
                }
                if (repMod.getSecondPosition().getChildLinkageType().equals(LinkageType.H_AT_OH)) {
                    repMod.setTailAtom("O");
                }
            }

            repMod.setMinRepeatCount((Integer) repObj.get("Min"));
            repMod.setMaxRepeatCount((Integer) repObj.get("Max"));


            String start = (String) repObj.getJSONObject("Donor").get("Node");
            String end = (String) repObj.getJSONObject("Acceptor").get("Node");
            Node endNode = this.nodeIndex.get(end);
            Node startNode = this.nodeIndex.get(start);

            Linkage repPos = new Linkage();
            repPos.setChildLinkages(parsePosition(repObj.getJSONObject("Donor").getJSONArray("Position")));
            repPos.setChildLinkageType(parseLinkageType(repObj.getJSONObject("Donor").get("LinkageType")));
            repPos.setParentLinkages(parsePosition(repObj.getJSONObject("Acceptor").getJSONArray("Position")));
            repPos.setParentLinkageType(parseLinkageType(repObj.getJSONObject("Acceptor").get("LinkageType")));
            repPos.setProbabilityUpper(parseProbability(repObj.getJSONObject("Probability").get("High")));
            repPos.setProbabilityLower(parseProbability(repObj.getJSONObject("Probability").get("Low")));

            Edge repEdge = new Edge();
            repEdge.addGlycosidicLinkage(repPos);
            repEdge.setSubstituent(repMod);
            repMod.addParentEdge(repEdge);

            _glyCo.addNode(endNode, repEdge, startNode);
       }

        return _glyCo;
    }

    private JSONObject extractBridgeBlock (JSONObject _edge, JSONObject _bridge) {
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

    private SubstituentInterface extractBridge (JSONObject _repeat, JSONObject _bridge) {
        JSONObject bridge = extractBridgeBlock(_repeat, _bridge);

        if (bridge != null) {
            return BaseCrossLinkedTemplate.forIUPACNotationWithIgnore((String) bridge.get("Notation"));
        } else {
            return null;
        }
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
