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

            lin.setParentLinkages(JSONParamAnalyzer.parsePosition(edgeObj.getJSONObject("Acceptor").getJSONArray("Position")));
            lin.setParentLinkageType(JSONParamAnalyzer.parseLinkageType(edgeObj.getJSONObject("Acceptor").get("LinkageType")));

            lin.setChildLinkages(JSONParamAnalyzer.parsePosition(edgeObj.getJSONObject("Donor").getJSONArray("Position")));
            lin.setChildLinkageType(JSONParamAnalyzer.parseLinkageType(edgeObj.getJSONObject("Donor").get("LinkageType")));

            lin.setProbabilityUpper(JSONParamAnalyzer.parseProbability(edgeObj.getJSONObject("Probability").get("High")));
            lin.setProbabilityLower(JSONParamAnalyzer.parseProbability(edgeObj.getJSONObject("Probability").get("Low")));

            edge.addGlycosidicLinkage(lin);

            // extract bridge
            SubstituentInterface subFace = JSONParamAnalyzer.extractBridge(edgeObj, _bridge);
            if (subFace != null) {
                JSONObject bridgeObj = JSONParamAnalyzer.extractBridgeBlock(edgeObj, _bridge);

                Substituent bridge = new Substituent(subFace);
                bridge.setFirstPosition(new Linkage());
                bridge.setSecondPosition(new Linkage());

                bridge.getFirstPosition().setParentLinkages(JSONParamAnalyzer.parsePosition(edgeObj.getJSONObject("Acceptor").getJSONArray("Position")));
                bridge.getFirstPosition().setParentLinkageType(JSONParamAnalyzer.parseLinkageType(edgeObj.getJSONObject("Acceptor").get("LinkageType")));
                bridge.getFirstPosition().setChildLinkages(new ArrayList<>(1));
                bridge.getFirstPosition().setChildLinkageType(JSONParamAnalyzer.parseLinkageType(bridgeObj.getJSONObject("Donor").get("LinkageType")));

                bridge.getSecondPosition().setParentLinkages(JSONParamAnalyzer.parsePosition(edgeObj.getJSONObject("Donor").getJSONArray("Position")));
                bridge.getSecondPosition().setParentLinkageType(JSONParamAnalyzer.parseLinkageType(bridgeObj.getJSONObject("Acceptor").get("LinkageType")));
                bridge.getSecondPosition().setChildLinkages(new ArrayList<>(1));
                bridge.getSecondPosition().setChildLinkageType(JSONParamAnalyzer.parseLinkageType(edgeObj.getJSONObject("Donor").get("LinkageType")));

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
}
