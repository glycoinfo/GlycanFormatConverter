package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONFragmentsParser {

    private HashMap<String, Node> nodeIndex;

    public GCJSONFragmentsParser (HashMap<String, Node> _nodeIndex) {
        nodeIndex = _nodeIndex;
    }

    public GlyContainer start (JSONObject _fragment, GlyContainer _glyCo) throws GlycanException {

        for (String id : _fragment.keySet()) {
            GlycanUndefinedUnit und = new GlycanUndefinedUnit();
            JSONObject frgObj = _fragment.getJSONObject(id);

            Node root = null;

            for (Object acceptor : frgObj.getJSONObject("Acceptor").getJSONArray("Node")) {
                Edge edge = new Edge();
                Linkage lin = new Linkage();

                // extract acceptor side
                und.addParentNode(nodeIndex.get(acceptor));

                // extract linkage position
                lin.setParentLinkages(JSONParamAnalyzer.parsePosition(frgObj.getJSONObject("Acceptor").getJSONArray("Position")));
                lin.setChildLinkages(JSONParamAnalyzer.parsePosition(frgObj.getJSONObject("Donor").getJSONArray("Position")));

                // extract probability
                lin.setProbabilityLower(JSONParamAnalyzer.parseProbability(frgObj.getJSONObject("Probability").get("Low")));
                lin.setProbabilityUpper(JSONParamAnalyzer.parseProbability(frgObj.getJSONObject("Probability").get("High")));

                // extract LinkageType
                lin.setParentLinkageType(JSONParamAnalyzer.parseLinkageType(frgObj.getJSONObject("Acceptor").get("LinkageType")));
                lin.setChildLinkageType(JSONParamAnalyzer.parseLinkageType(frgObj.getJSONObject("Donor").get("LinkageType")));

                // extract donor side
                if (isSubstituentFragment(frgObj)) {
                    root = new Substituent(JSONParamAnalyzer.parseSubstituentTemplate((String) frgObj.getJSONObject("Donor").get("Notation")), lin);
                    if (lin.getParentLinkageType().equals(LinkageType.H_AT_OH)) {
                        ((Substituent) root).setHeadAtom("O");
                    }
                    if (lin.getParentLinkageType().equals(LinkageType.DEOXY)) {
                        ((Substituent) root).setHeadAtom("C");
                    }
                    edge.setSubstituent(root);
                } else {
                    root = nodeIndex.get(frgObj.getJSONObject("Donor").get("Node"));
                    edge.setChild(root);
                }

                edge.addGlycosidicLinkage(lin);
                edge.setParent(nodeIndex.get(acceptor));
                und.addConnection(edge);
            }

            root.setParentEdge(und.getConnections());
            und.addNode(root);
            und.setConnection(und.getConnections().get(0));

            _glyCo.addGlycanUndefinedUnit(und);
        }

        return _glyCo;
    }

    private boolean isSubstituentFragment (JSONObject _frgObj) {
        return (_frgObj.getJSONObject("Donor").has("Notation"));
    }
}
