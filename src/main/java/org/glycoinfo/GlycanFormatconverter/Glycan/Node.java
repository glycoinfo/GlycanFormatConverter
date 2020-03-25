package org.glycoinfo.GlycanFormatconverter.Glycan;

import org.glycoinfo.GlycanFormatconverter.util.visitor.Visitable;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Node implements Visitable {
	protected ArrayList<Edge> parentLinkages = new ArrayList<>();
	protected ArrayList<Edge> childLinkages = new ArrayList<>();
	
	public abstract Node copy() throws GlycanException;
	
	 public void setChildEdge(ArrayList<Edge> children) throws GlycanException {
		if (children == null) {
			throw new GlycanException ("Children are Null");
		}
		this.childLinkages.clear();
		for (Edge child : children) {
			this.addChildEdge(child);
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
		ArrayList<Node> ret = new ArrayList<>();
		for (Edge childLinkage : this.childLinkages) {
			Node n_ret = childLinkage.getChild();
			if (n_ret == null) continue;
			if (!ret.contains(n_ret)) {
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
		ArrayList<Node> ret = new ArrayList<>();
		for (Edge edge : parentLinkages) {
			ret.add(edge.getParent());
		}

		return ret;
	}

	public void addChildEdge(Edge _linkSubstructure) throws GlycanException {
		if (_linkSubstructure == null) {
			throw new GlycanException ("Substructure is Null");
		}
		if(!this.childLinkages.contains(_linkSubstructure)) {
			this.childLinkages.add(_linkSubstructure);
		}
	}
	
	public void removeParentEdge(Edge _edge) throws GlycanException {
		if(!this.parentLinkages.contains(_edge)) {
			throw new GlycanException ("This parent edge can not remove");
		}
		this.parentLinkages.remove(_edge);
		//this.parentLinkage = null;
		//return true;
	}
	
	public void removeChildEdge (Edge _linkage) throws GlycanException {
		if(_linkage == null) {
			throw new GlycanException ("This child edge can not remove");
		}
		if(!this.childLinkages.contains(_linkage)) {
			return;
		}
		this.childLinkages.remove(_linkage);
	}
	
	public void removeAllEdges() {
		this.parentLinkages.clear();
		this.childLinkages.clear();
	}
}
