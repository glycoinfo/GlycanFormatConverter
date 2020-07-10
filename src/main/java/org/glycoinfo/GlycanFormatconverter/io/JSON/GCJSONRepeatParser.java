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
            JSONObject bridgeObj = JSONParamAnalyzer.extractBridgeBlock(repObj, _bridge);

            GlycanRepeatModification repMod = new GlycanRepeatModification(JSONParamAnalyzer.extractBridge(repObj, _bridge));
            repMod.setFirstPosition(new Linkage());
            repMod.setSecondPosition(new Linkage());

            if (bridgeObj != null) {
                repMod.getFirstPosition().setParentLinkageType(JSONParamAnalyzer.parseLinkageType(repObj.getJSONObject("Acceptor").get("LinkageType")));
                repMod.getFirstPosition().setChildLinkageType(JSONParamAnalyzer.parseLinkageType(bridgeObj.getJSONObject("Donor").get("LinkageType")));

                repMod.getSecondPosition().setParentLinkageType(JSONParamAnalyzer.parseLinkageType(bridgeObj.getJSONObject("Acceptor").get("LinkageType")));
                repMod.getSecondPosition().setChildLinkageType(JSONParamAnalyzer.parseLinkageType(repObj.getJSONObject("Donor").get("LinkageType")));

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
            repPos.setChildLinkages(JSONParamAnalyzer.parsePosition(repObj.getJSONObject("Donor").getJSONArray("Position")));
            repPos.setChildLinkageType(JSONParamAnalyzer.parseLinkageType(repObj.getJSONObject("Donor").get("LinkageType")));
            repPos.setParentLinkages(JSONParamAnalyzer.parsePosition(repObj.getJSONObject("Acceptor").getJSONArray("Position")));
            repPos.setParentLinkageType(JSONParamAnalyzer.parseLinkageType(repObj.getJSONObject("Acceptor").get("LinkageType")));
            repPos.setProbabilityUpper(JSONParamAnalyzer.parseProbability(repObj.getJSONObject("Probability").get("High")));
            repPos.setProbabilityLower(JSONParamAnalyzer.parseProbability(repObj.getJSONObject("Probability").get("Low")));

            Edge repEdge = new Edge();
            repEdge.addGlycosidicLinkage(repPos);
            repEdge.setSubstituent(repMod);
            repMod.addParentEdge(repEdge);

            _glyCo.addNode(endNode, repEdge, startNode);
       }

        return _glyCo;
    }
}
