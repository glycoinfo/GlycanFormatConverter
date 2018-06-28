package org.glycoinfo.GlycanFormatconverter.Glycan;

import java.util.ArrayList;
import java.util.Iterator;

import org.glycoinfo.GlycanFormatconverter.util.visitor.Visitable;

public abstract class Node implements Visitable {
	protected ArrayList<Edge> parentLinkages = new ArrayList<Edge>();
	protected ArrayList<Edge> childLinkages = new ArrayList<Edge>();
	
	public abstract Node copy() throws GlycanException;
	
	 public void setChildEdge(ArrayList<Edge> children) throws GlycanException {
		if (children == null) {
			throw new GlycanException ("Children are Null");
		}
		this.childLinkages.clear();
		for(Iterator<Edge> iterEdge = children.iterator(); iterEdge.hasNext();) {
			this.addChildEdge(iterEdge.next());
		}
	}
	
	//public ArrayList<Edge> getChildEdges() {
	// 	return this.childLinkages;
	//}

	public ArrayList<Edge> getChildEdges() {
	 	ArrayList<Edge> ret = new ArrayList<>();
	 	Iterator<Edge> iterEdge = childLinkages.iterator();

	 	while (iterEdge.hasNext()) {
	 		Edge edge = iterEdge.next();
	 		ret.add(edge);
		}

		return ret;
	}
	
	public boolean addParentEdge(Edge _edge) {
		return this.parentLinkages.add(_edge);
	}
	
	public void setParentEdge(ArrayList<Edge> _edges) {
		this.parentLinkages = _edges;
	}
	
	public Edge getParentEdge() {
		if(parentLinkages.isEmpty()) return null;
		return this.parentLinkages.get(0);
	}
	
	//public ArrayList<Edge> getParentEdges() {
	//	return this.parentLinkages;
	//}

	public ArrayList<Edge> getParentEdges() {
	 	ArrayList<Edge> ret = new ArrayList<>();
	 	Iterator<Edge> iterEdge = this.parentLinkages.iterator();

	 	while (iterEdge.hasNext()) {
	 		Edge edge = iterEdge.next();
	 		ret.add(edge);
		}

		return ret;
	}

	public ArrayList<Node> getChildNodes() {
		ArrayList<Node> ret = new ArrayList<Node>();
		for (Iterator<Edge> linkages = this.childLinkages.iterator(); linkages.hasNext();) {
			Node n_ret = linkages.next().getChild();
			if(n_ret == null) continue;
			if(!ret.contains(n_ret)) {
				ret.add(n_ret);
			}
		}
		return ret;
	}
	
	public Node getParentNode() {
		if(this.parentLinkages.isEmpty()) return null;
		return this.parentLinkages.get(0).getParent();
	}

	public ArrayList<Node> getParents () {
		ArrayList<Node> ret = new ArrayList<Node>();
		for (Edge edge : parentLinkages) {
			ret.add(edge.getParent());
		}

		return ret;
	}

	public boolean addChildEdge(Edge _linkSubstructure) throws GlycanException {
		if (_linkSubstructure == null) {
			throw new GlycanException ("Substructure is Null");
		}
		if(!this.childLinkages.contains(_linkSubstructure)) {
			return this.childLinkages.add(_linkSubstructure);
		}
		return false;
	}
	
	public boolean removeParentEdge(Edge _edge) throws GlycanException { 
		if(!this.parentLinkages.contains(_edge)) {
			throw new GlycanException ("This parent edge can not remove");
		}
		return this.parentLinkages.remove(_edge);
		//this.parentLinkage = null;
		//return true;
	}
	
	public boolean removeChildEdge (Edge _linkage) throws GlycanException {
		if(_linkage == null) {
			throw new GlycanException ("This child edge can not remove");
		}
		if(!this.childLinkages.contains(_linkage)) {
			return false;
		}
		return this.childLinkages.remove(_linkage);
	}
	
	public void removeAllEdges() {
		this.parentLinkages.clear();
		this.childLinkages.clear();
	}
}
