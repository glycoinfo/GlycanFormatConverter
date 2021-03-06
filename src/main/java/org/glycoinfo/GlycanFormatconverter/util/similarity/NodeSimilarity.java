package org.glycoinfo.GlycanFormatconverter.util.similarity;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.ThreeLetterCodeConverter;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameDictionary;
import org.glycoinfo.GlycanFormatconverter.util.comparater.EdgeComparator;

import java.util.ArrayList;
import java.util.Collections;

public class NodeSimilarity {

	/*
	 * sort recursively all nodes by linkage position from root node.
	 */
	public ArrayList<Node> sortAllNode (Node _root) {
		ArrayList<Node> ret = new ArrayList<>();

		return sortNotations(ret, _root);
	}

	private ArrayList<Node> sortNotations (ArrayList<Node> _notations, Node _node) {
		_notations.add(_node);

		for(Edge childEdge : sortPosition(_node.getChildEdges())) {
			if(childEdge.getChild() == null) continue;
			Substituent sub = (Substituent) childEdge.getSubstituent();
			if(sub != null && sub instanceof GlycanRepeatModification) continue; /* is repeat with cross linked substituent */
			_notations = sortNotations(_notations, childEdge.getChild());
		}

		return _notations;
	}

	/*
	 * sort edges by linkage position from current node.
	 */
	public ArrayList<Edge> sortChildSideEdges (Node _node) {
		ArrayList<Edge> picked = new ArrayList<>();
		for (Edge edge : _node.getChildEdges()) {
			if (edge.isRepeat()) continue;
			if (edge.isCyclic()) continue;
			picked.add(edge);
		}

		return sortPosition(picked);
	}
	
	/*
	 * sort edges of parent linkage position.
	 * If edges are contains cyclic and repeating structure.
	 * simple->repeating->cyclic
	 */
	public ArrayList<Edge> sortParentSideEdges (ArrayList<Edge> _edges) {
		ArrayList<Edge> ret = new ArrayList<>();
		
		// stack the edge of cyclic and repeating
		Edge cyclic = null;
		ArrayList<Edge> repeating = new ArrayList<>();
		ArrayList<Edge> simple = new ArrayList<>();
		for(Edge edge : _edges) {
			if(edge.isRepeat()) repeating.add(edge);
			else if(edge.isCyclic()) cyclic = edge;
			else simple.add(edge);
		}
		
		if(repeating != null) ret.addAll(repeating);
		if(cyclic != null) ret.add(cyclic);
		if (simple != null) ret.addAll(simple);

		if(ret.isEmpty()) return _edges;
		return ret;
	}
	
	/*
	 * Check main.
	 */
	public boolean isMainChaineBranch(Node _node) {
		if (_node.getParentEdges().isEmpty()) return false;
		if (this.isAlternativeLinkedForAcceptor(_node)) return false;

		Edge parentEdge = null;
		for (Edge edge : _node.getParentEdges()) {
			if (edge.isRepeat()) continue;
			parentEdge = edge;
		}
		if (parentEdge == null || parentEdge.getParent() == null) return false;

		if (!parentEdge.isReverseEdge()) return false;
		if (countChildren(parentEdge.getParent()) == 1) return false;

		ArrayList<Edge> childEdges = sortChildSideEdges(parentEdge.getParent());

		if (childEdges.size() == 1) return false;
		if (childEdges.indexOf(parentEdge) != childEdges.size() - 1) return true;

		return false;
	}
	
	public int countChildren(Node _node) {
		int ret = 0;
		int cyclic = 0;
		Edge repeatEdge = null;

		for(Edge childEdge : this.sortEdge(_node)) {
			Substituent sub = (Substituent) childEdge.getSubstituent();
			if(sub != null && childEdge.getChild() == null) continue;
			if(childEdge.isRepeat()) {
				repeatEdge = childEdge;
				continue;
			}
			if(childEdge.isCyclic()) {
				cyclic++; 
				continue;
			}

			/*
			 * for outer node from repeat 
			 * o--[-o
			 *  a4 4
			 */
			/*if(repeatEdge != null && !childEdge.isRepeat() && childEdge.getGlycosidicLinkages().get(0).getParentLinkages().size() == 1) {
				int repPos = repeatEdge.getGlycosidicLinkages().get(0).getParentLinkages().get(0);
				int parentPos = childEdge.getGlycosidicLinkages().get(0).getParentLinkages().get(0);
				if(repPos != parentPos) {
					ret++;
					repeatEdge = null;
				}
			}*/
			
			ret++;
		}

		if(ret > 0) ret = ret + cyclic;
		
		return ret;
	}
	
	/* sort priority edges with repeat */
	private ArrayList<Edge> sortEdge (Node _node) {
		ArrayList<Edge> ret = new ArrayList<>();
		
		if(_node.getChildEdges().isEmpty()) return ret;

		// pick repeat
		for(Edge childEdge : _node.getChildEdges()) {
			if(childEdge.isRepeat()) ret.add(childEdge);
		}

		for(Edge childEdge : _node.getChildEdges()) {
			if(!ret.contains(childEdge)) ret.add(childEdge);
		}

		return ret;
	}
	
	private ArrayList<Edge> sortPosition (ArrayList<Edge> _edges) {
		ArrayList<Edge> ret = new ArrayList<>();

		ArrayList<Edge> copy = new ArrayList<>();
		try {
			Node child = null;
			for(Edge childEdge : _edges) {
				if(childEdge.getChild() != null) {
					// for cyclic linkages
					if(child != null && child.equals(childEdge.getChild())) continue;
					copy.add(childEdge);
				}
				if(child == null && !childEdge.isRepeat()) child = childEdge.getChild();
			}
			
			// sort sticky
			for(Edge childEdge : extractSticky(_edges)) {
				ret.add(childEdge);
				copy.remove(childEdge);
			}

			// sort monosaccharides other than sticky
			copy.sort(new EdgeComparator());
			//comparePositions(copy)) {
			ret.addAll(copy);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}
	
	private ArrayList<Edge> extractSticky(ArrayList<Edge> _edges) {
		ArrayList<Edge> ret = new ArrayList<>();
	
		for(Edge unit : _edges) {
			Node child = unit.getChild();
			if(child == null) continue;
			
			try {
				Node tempChild = ((Monosaccharide) child).copy();
				ThreeLetterCodeConverter threeCon = new ThreeLetterCodeConverter();
				threeCon.start(tempChild);

				if (((Monosaccharide) tempChild).getStereos().isEmpty()) continue;

				if(threeCon.getDictionary() == null && ((Monosaccharide) tempChild).getStereos().get(0).contains("xyl")) {
					ret.add(unit);
				}
				if(threeCon.getDictionary() != null && threeCon.getDictionary().equals(TrivialNameDictionary.FUC)) {
					ret.add(unit);
				}
			} catch (GlycanException e) {
				e.printStackTrace();
			}
		}

		ret.sort(new EdgeComparator());
		return ret;
	}

	private boolean isAlternativeLinkedForAcceptor (Node _node) {
		if (_node instanceof Substituent) return false;
		Monosaccharide mono = (Monosaccharide) _node;

		boolean ret = false;
		Edge temp = null;
		for (Edge acceptor : mono.getParentEdges()) {
			if (acceptor.getSubstituent() != null) {
				if (acceptor.getSubstituent() instanceof GlycanRepeatModification) continue;
				if (acceptor.getSubstituent() instanceof Substituent) continue;
			}
			if (acceptor.getParent() == null) continue;
			if (temp == null) {
				temp = acceptor;
			} else {
				ret = (!temp.getParent().equals(acceptor.getParent()));
			}
		}

		return ret;
	}
}
