package org.glycoinfo.GlycanFormatconverter.util.similarity;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoExporterException;
import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeNodeConverter;
import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeSUDictionary;
import org.glycoinfo.GlycanFormatconverter.util.comparater.LCNodeComparater;
import org.glycoinfo.GlycanFormatconverter.util.comparater.NodeDescendingComparator;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Created by e15d5605 on 2017/10/06.
 */
public class LinearCodeNodeSimilarity extends NodeSimilarity {

    public ArrayList<Node> sortAllNode (Node _root) {
        ArrayList<Node> ret = new ArrayList<>();

        ret.add(_root);

        try {
            ret = sortSUNotations(ret, _root);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public ArrayList<Node> sortCurrentChildren (Node _node) throws GlyCoExporterException, GlycanException, ConverterExchangeException {
        return sortSUBranch(_node);
    }

    public ArrayList<Edge> sortParentSideEdges (ArrayList<Edge> _edges) {
        ArrayList<Edge> ret = new ArrayList<>();

        Edge cyclic = null;
        ArrayList<Edge> repeating = new ArrayList<>();
        ArrayList<Edge> simple = new ArrayList<>();
        for (Edge edge : _edges) {
            if (edge.isRepeat()) repeating.add(edge);
            else if (edge.isCyclic()) cyclic = edge;
            else simple.add(edge);
        }

        ret.addAll(simple);
        ret.addAll(repeating);
        if (cyclic != null) ret.add(cyclic);

        if (ret.isEmpty()) return _edges;
        return ret;
    }

    private ArrayList<Node> sortSUNotations (ArrayList<Node> _notations, Node _node) throws GlyCoExporterException, GlycanException, ConverterExchangeException {
        if (_node.getChildNodes().isEmpty() || countChildren(_node) == 0) return _notations;

        if (countChildren(_node) > 1) {
            for (Node child : sortSUBranch(_node)) {
                _notations.add(child);
                _notations = sortSUNotations(_notations, child);
            }
        } else {
            _notations.add(_node.getChildNodes().get(0));
            _notations = sortSUNotations(_notations, _node.getChildNodes().get(0));
        }

        return _notations;
    }

    private ArrayList<Node> sortSUBranch (Node _node) throws GlyCoExporterException, ConverterExchangeException, GlycanException {
        ArrayList<Node> children = new ArrayList<>();

        for (Edge childEdge : _node.getChildEdges()) {
            Substituent sub = (Substituent) childEdge.getSubstituent();
            if (sub != null && sub instanceof GlycanRepeatModification) continue;
            if (childEdge.getChild() == null) continue;
            children.add(childEdge.getChild());
        }

        Collections.sort(children, new LCNodeComparater());

        return sortSUBranchByPosition(children);
    }

    private ArrayList<Node> sortSUBranchByPosition (ArrayList<Node> _notations) throws GlyCoExporterException, GlycanException, ConverterExchangeException {
        ArrayList<Node> subNotations = new ArrayList<>();
        TreeMap<Integer, Node> mapNodes = new TreeMap<>();

        for (int i = 0; i < _notations.size(); i++) {
            mapNodes.put(i, _notations.get(i));

            if ((i+1) > (_notations.size() - 1)) {
                if (!subNotations.isEmpty()) mapNodes = sortMap(mapNodes, i, subNotations);
                break;
            }

            LinearCodeNodeConverter lcConv = new LinearCodeNodeConverter();
            LinearCodeSUDictionary lcDict1 = lcConv.start(_notations.get(i));
            LinearCodeSUDictionary lcDict2 = lcConv.start(_notations.get(i+1));
            if (lcDict1.equals(lcDict2)) {
                if (!subNotations.contains(_notations.get(i))) subNotations.add(_notations.get(i));
                if (!subNotations.contains(_notations.get(i+1))) subNotations.add(_notations.get(i+1));
            }

            if (!lcDict1.equals(lcDict2) && !subNotations.isEmpty()) {
                mapNodes = sortMap(mapNodes, i, subNotations);
                subNotations.clear();
            }
        }

        /**/
        ArrayList<Node> ret = new ArrayList<>();
        for (Iterator<Node> iterNode = mapNodes.values().iterator(); iterNode.hasNext();) {
            ret.add(iterNode.next());
        }

        return ret;
    }

    private TreeMap<Integer, Node> sortMap (TreeMap<Integer, Node> _map, int _index, ArrayList<Node> _subGraph) {
        Collections.sort(_subGraph, new NodeDescendingComparator());

        for (Node node : _subGraph) {
            _map.put(_index - _subGraph.size() + 1 + _subGraph.indexOf(node), node);
        }

        return _map;
    }

    public boolean isSubBranch (Node _node) throws GlyCoExporterException, ConverterExchangeException, GlycanException {
        if (_node.getParentEdges().isEmpty()) return false;

        for (Edge parentEdge : _node.getParentEdges()) {
            if (parentEdge.isRepeat() || parentEdge.isCyclic()) continue;
            if (parentEdge.getParent() == null) break;

            ArrayList<Node> children = sortCurrentChildren(parentEdge.getParent());
            if (children.size() < 2) continue;
            if (children.indexOf(_node) != (children.size() - 1)) {
                return true;
            }
        }
        return false;
    }
}
