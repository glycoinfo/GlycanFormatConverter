package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONEdgeParser {

    private HashMap<String, Node> nodeIndex;

    public GCJSONEdgeParser (HashMap<String, Node> _nodeIndex) {
        nodeIndex = _nodeIndex;
    }

    public GlyContainer start (JSONObject _edges, JSONObject _bridge) throws GlycanException {
        GlyContainer ret = new GlyContainer();

        for (String key : _edges.keySet()) {
            JSONObject edgeObj = _edges.getJSONObject(key);
            Edge edge = new Edge();
            Node donor = nodeIndex.get(edgeObj.getJSONObject("Donor").get("Node"));
            Node acceptor = nodeIndex.get(edgeObj.getJSONObject("Acceptor").get("Node"));

            Linkage lin = new Linkage();

            lin.setParentLinkages(parsePosition(edgeObj.getJSONObject("Acceptor").getJSONArray("Position")));
            lin.setParentLinkageType(parseLinkageType(edgeObj.getJSONObject("Acceptor").get("LinkageType")));

            lin.setChildLinkages(parsePosition(edgeObj.getJSONObject("Donor").getJSONArray("Position")));
            lin.setChildLinkageType(parseLinkageType(edgeObj.getJSONObject("Donor").get("LinkageType")));

            lin.setProbabilityUpper(parseProbability(edgeObj.getJSONObject("Probability").get("High")));
            lin.setProbabilityLower(parseProbability(edgeObj.getJSONObject("Probability").get("Low")));

            edge.addGlycosidicLinkage(lin);

            // extract bridge
            SubstituentInterface subFace = this.extractBridge(edgeObj, _bridge);
            if (subFace != null) {
                JSONObject bridgeObj = extractBridgeBlock(edgeObj, _bridge);

                Substituent bridge = new Substituent(subFace);
                bridge.setFirstPosition(new Linkage());
                bridge.setSecondPosition(new Linkage());

                bridge.getFirstPosition().setParentLinkages(parsePosition(edgeObj.getJSONObject("Acceptor").getJSONArray("Position")));
                bridge.getFirstPosition().setParentLinkageType(parseLinkageType(edgeObj.getJSONObject("Acceptor").get("LinkageType")));
                bridge.getFirstPosition().setChildLinkages(new ArrayList<>(1));
                bridge.getFirstPosition().setChildLinkageType(parseLinkageType(bridgeObj.getJSONObject("Donor").get("LinkageType")));

                bridge.getSecondPosition().setParentLinkages(parsePosition(edgeObj.getJSONObject("Donor").getJSONArray("Position")));
                bridge.getSecondPosition().setParentLinkageType(parseLinkageType(bridgeObj.getJSONObject("Acceptor").get("LinkageType")));
                bridge.getSecondPosition().setChildLinkages(new ArrayList<>(1));
                bridge.getSecondPosition().setChildLinkageType(parseLinkageType(edgeObj.getJSONObject("Donor").get("LinkageType")));

                if (bridge.getFirstPosition().getParentLinkageType().equals(LinkageType.H_AT_OH)) {
                    bridge.setHeadAtom("O");
                }
                if (bridge.getSecondPosition().getChildLinkageType().equals(LinkageType.H_AT_OH)) {
                    bridge.setTailAtom("O");
                }

                edge.setSubstituent(bridge);
                bridge.addParentEdge(edge);
            }

            //acceptor, edge, donor
            ret.addNode(acceptor, edge, donor);
        }

        return ret;
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

    private ArrayList<Integer> parsePosition (JSONArray _position) {
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
}
