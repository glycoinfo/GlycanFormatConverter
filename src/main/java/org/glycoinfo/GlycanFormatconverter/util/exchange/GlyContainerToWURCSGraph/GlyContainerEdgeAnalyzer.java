package org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.WURCSFramework.wurcs.graph.Backbone;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by e15d5605 on 2017/07/20.
 */
public class GlyContainerEdgeAnalyzer {

    private GlyContainer glyCo;

    public GlyContainerEdgeAnalyzer (GlyContainer _glyCo) {
        glyCo = _glyCo;
    }

    public void start (HashMap<WURCSComponent, Node> _backbone2Node, Backbone _root) throws GlycanException {
    	Backbone root = _root;
    	LinkedList<Backbone> subRoots = new LinkedList<>();

    	for (WURCSComponent wc : _backbone2Node.keySet()) {
    		Backbone bb = (Backbone) wc;
    		if (root.equals(bb) || !bb.isRoot()) continue;
    		if (_backbone2Node.get(bb).getParentEdges().isEmpty()) {
    			if (this.countMonosaccharideChildren(_backbone2Node.get(bb)) > 1) subRoots.addFirst(bb);
    			else subRoots.addLast(bb);
    		}
    	}

    	if (subRoots.isEmpty()) return;  

    	for (Backbone subRoot : subRoots) {
    		for (Edge edge : extractSubGraph(_backbone2Node.get(subRoot), new ArrayList<Edge>())) {
    			Edge reverseEdge = makeReverseEdge(edge);
    			glyCo.addNode(edge.getChild(), reverseEdge, edge.getParent());
    			if (edge.getChild().getParentEdges().contains(edge))
    				edge.getChild().removeParentEdge(edge);
    			if (edge.getParent().getChildEdges().contains(edge))
    				edge.getParent().removeChildEdge(edge);
    		}
    	}
    }

    private ArrayList<Edge> extractSubGraph (Node _node, ArrayList<Edge> _picked) {
    	if (_node.getChildEdges().isEmpty() && _node.getParentEdges().size() > 1) return _picked;
        for (Edge childEdge : _node.getChildEdges()) {
        	if (childEdge.getChild() == null && childEdge.getSubstituent() != null) continue;  	
        	if (!_picked.contains(childEdge)) _picked.add(childEdge);
            _picked = extractSubGraph(childEdge.getChild(), _picked);
        }

        return _picked;
    }

    private Edge makeReverseEdge (Edge _edge) throws GlycanException {
        Edge ret = new Edge ();

        Substituent sub = (Substituent) _edge.getSubstituent();

        ret.setSubstituent(sub);

        Linkage flipedLinkage = new Linkage();
        for (Linkage lin : _edge.getGlycosidicLinkages()) {
            /* set linkage position */
            flipedLinkage.setChildLinkages(lin.getParentLinkages());
            flipedLinkage.setParentLinkages(lin.getChildLinkages());

            /* set probability */
            flipedLinkage.setChildProbabilityUpper(lin.getParentProbabilityUpper());
            flipedLinkage.setChildProbabilityLower(lin.getParentProbabilityLower());
            flipedLinkage.setProbabilityUpper(lin.getChildProbabilityUpper());
            flipedLinkage.setProbabilityLower(lin.getChildProbabilityLower());

            /* set LinkageType */
            flipedLinkage.setChildLinkageType(lin.getParentLinkageType());
            flipedLinkage.setParentLinkageType(lin.getChildLinkageType());
        }

        ret.addGlycosidicLinkage(flipedLinkage);

        return ret;
    }
    
    private int countMonosaccharideChildren (Node _node) {
    	int ret = 0;
    	for (Edge edge : _node.getChildEdges()) {
    		if (edge.getSubstituent() != null && edge.getChild() == null) continue;
    		ret++;
    	}
    	
    	return ret;
    }
}
