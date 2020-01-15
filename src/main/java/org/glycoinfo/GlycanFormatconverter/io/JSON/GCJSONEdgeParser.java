package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONEdgeParser {

    private HashMap<String, Node> nodeIndex;
    private GCJSONLinkageParser gclinParser;

    public GCJSONEdgeParser (HashMap<String, Node> _nodeIndex) {
        nodeIndex = _nodeIndex;
        gclinParser = new GCJSONLinkageParser();
    }

    public GlyContainer parseEdge (JSONObject _monosaccharides) throws GlycanException {
        GlyContainer ret = new GlyContainer();

        for (String key : _monosaccharides.keySet()) {
            JSONObject edge = _monosaccharides.getJSONObject(key).getJSONObject("Edge");
            Node currentNode = nodeIndex.get(key);

            if (_monosaccharides.length() == 1) {
                ret.addNode(currentNode);
                break;
            }
            ret = extractEdge(edge, ret, currentNode);
        }

        return ret;
    }

    public GlyContainer extractEdge (JSONObject _edge, GlyContainer _glyCo, Node _current) throws GlycanException {
        for (String key : _edge.keySet()) {
            switch (key) {
                case "Parent" :
                    GCJSONModificationParser gcModParser = new GCJSONModificationParser();

                    for (Object obj : _edge.getJSONArray(key)) {
                        Edge edge = new Edge();
                        Linkage lin = gclinParser.parsePosition((JSONObject) obj);
                        edge.addGlycosidicLinkage(lin);

                        Substituent bridge = gcModParser.parseBridge(((JSONObject) obj).getJSONObject("Bridge"));
                        edge.setSubstituent(bridge);
                        if (bridge != null) bridge.addParentEdge(edge);

                        Node parent = nodeIndex.get(extractParentID((JSONObject) obj));
                        _glyCo.addEdge(parent, _current, edge);
                    }
                    break;

                case "Repeat" :
                    for (Object obj : _edge.getJSONArray(key)) {
                        GCJSONRepeatParser gcRepParser =
                                new GCJSONRepeatParser(_glyCo, _current, nodeIndex.get(extractParentID((JSONObject) obj)));
                        _glyCo = gcRepParser.makeRepeatingEdge(obj);
                    }

                    break;

                case "Fragment" :
                    if (_edge.getJSONObject(key).length() == 0) break;
                    GCJSONFragmentsParser gcFragParser = new GCJSONFragmentsParser(nodeIndex);
                    GlycanUndefinedUnit und = gcFragParser.parseFragments(_edge.getJSONObject(key), _current);
                    _glyCo.addGlycanUndefinedUnit(und);
                    break;
            }
        }

        return _glyCo;
    }

    private String extractParentID (JSONObject _edgeUnit) {
        return _edgeUnit.get("ParentNodeID").toString();
    }
}
