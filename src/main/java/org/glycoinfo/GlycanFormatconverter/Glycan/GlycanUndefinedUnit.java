package org.glycoinfo.GlycanFormatconverter.Glycan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GlycanUndefinedUnit implements GlycanGraph {
	
	private ArrayList<Node> parents;
	private Edge connection = null;
	private ArrayList<Edge> connections; //20200701, added
	private ArrayList<Node> children;
	public static final double UNKNOWN = -1;
	
	private double probabilityLow = 100;
	private double probabilityHigh = 100;
	
	public GlycanUndefinedUnit () {
		this.parents = new ArrayList<>();
		this.children = new ArrayList<>();
		this.connections = new ArrayList<>();
	}
	
	@Override
	public ArrayList<Node> getRootNodes() throws GlycanException {
		ArrayList<Node> ret = new ArrayList<>();
		Node root;

		for(Node unit : getNodes()) {
			root = unit;
			Edge parent = root.getParentEdge();

			if (root instanceof Substituent || isComposition()) {
				ret.add(root);
				continue;
			}

			if (parent == null) continue;
			if (this.parents.contains(parent.getParent())) {
				ret.add(root);
			}
		}

		if(ret.size() == 1) return ret;

		throw new GlycanException ("Node seems not to have at least one root residue");
	}
	
	@Override
	public Iterator<Node> getNodeIterator() {
		return this.children.iterator();
	}
	
	@Override
	public boolean isConnected() throws GlycanException {
		ArrayList<Node> roots = this.getRootNodes();
		return roots.size() <= 1;
	}
	
	@Override
	public boolean removeNode(Node _node) throws GlycanException {
		Edge linkage;
		Node residue;
		if(_node == null) throw new GlycanException ("Invalid residue.");

		linkage = _node.getParentEdge();
		if(linkage != null) {
			residue = linkage.getParent();
			if(residue == null) throw new GlycanException ("A linkage with a null parent exists.");
			residue.removeChildEdge(linkage);
		}

		for (Edge edge : _node.getChildEdges()) {
			linkage = edge;
			residue = linkage.getChild();
			if (residue == null) throw new GlycanException("A linkage with a null child exists.");
			residue.removeParentEdge(linkage);
		}

		return this.children.remove(_node);
	}
	
	@Override
	public ArrayList<Node> getNodes() {
		ArrayList<Node> ret = new ArrayList<>();
		Iterator<Node> iterNode = getNodeIterator();

		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			ret.add(node);
		}
		return ret;
	}

	@Override
	public ArrayList<Edge> getEdges() {
		ArrayList<Edge> ret = new ArrayList<>();
		Iterator<Node> iterNode = getNodeIterator();

		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			ret.addAll(node.getChildEdges());
		}
		return ret;
	}

	@Override
	public void addNode(Node _node) throws GlycanException {
		if(_node == null) throw new GlycanException ("Invalid residue.");
		if(!this.children.contains(_node)) {
			this.children.add(_node);
		}
	}
	
	@Override
	public void addNode(Node _parent, Edge _linkage, Node _child) throws GlycanException {
		if(_parent == null || _child == null) throw new GlycanException ("Invalid residue");
		if(_linkage == null) throw new GlycanException ("Invalid linkage");
		
		if(!containsNode(_parent)) {
			this.addNode(_parent);
		}
		if(!containsNode(_child)) {
			this.addNode(_child);
		}
		
		_child.addParentEdge(_linkage);
		_parent.addChildEdge(_linkage);
		_linkage.setChild(_child);
		_linkage.setParent(_parent);
	}
	
	@Override
	public void addNodeWithSubstituent(Node _parent, Edge _linkage, Substituent _child) throws GlycanException {
		if(_parent == null || _child == null) throw new GlycanException ("Invalid residue");
		if(_linkage == null) throw new GlycanException ("Invalid residue");
		
		if(!containsNode(_parent)) {
			this.addNode(_parent);
		}
		if(!containsNode(_parent))
			throw new GlycanException ("Critical error imposible to add residue.");
		
		_parent.addChildEdge(_linkage);
		_linkage.setSubstituent(_child);
	}
	
	@Override
	public void addEdge(Node _parent, Node _child, Edge _linkage) throws GlycanException {
		this.addNode(_parent, _linkage, _child);
	}

	@Override
	public boolean containsNode(Node _node) {
		return this.children.contains(_node);
	}

	@Override
	public boolean isParent(Node _parent, Node _current) {
		Node parent = _current.getParentNode();
		if(parent == null) return false;
		if(parent == _parent) return true;
		return this.isParent(_parent, parent);
	}

	@Override
	public void removeEdge(Edge _edge) throws GlycanException {
		if(_edge == null) return;
		
		Node child = _edge.getChild();
		Node parent = _edge.getParent();
		
		if(child == null || parent == null) 
			throw new GlycanException ("The edge contains null values");
		if(child.getParentEdge() != _edge)
			throw new GlycanException ("The child attachement is not correct");
		
		ArrayList<Edge> edges = parent.getChildEdges();
		if(!edges.contains(_edge))
			throw new GlycanException ("The parent attachement is not correct");
		
		child.removeParentEdge(_edge);
		parent.removeChildEdge(_edge);
	}

	public void addConnection (Edge _edge) {
		this.connections.add(_edge);
	}

	public ArrayList<Edge> getConnections () {
		return this.connections;
	}

	public void setConnection (Edge _edge) {
		this.connection = _edge;
	}

	public Edge getConnection () {
		return this.connection;
	}
	
	public double getProbabilityHigh () {
		return this.probabilityHigh;
	}
	
	public double getProbabilityLow () {
		return this.probabilityLow;
	}
	
	public void setProbability (double _high, double _low) throws GlycanException {
		if (_low > _high) {
			throw new GlycanException("The lower border of a probability must be smaller or equal than the upper border.");
		}
		this.probabilityHigh = _high;
		this.probabilityLow = _low;
	}
	
	public void setProbability (double _probability) {
		this.probabilityHigh = _probability;
		this.probabilityLow = _probability;
	}
	
	/*****/
	
	protected void setParentNodes(ArrayList<Node> _parents) throws GlycanException {
		if (parents == null) {
			throw new GlycanException ("Parent are Null");
		}
		this.parents.clear();
		for (Node parent : _parents) {
			this.addParentNode(parent);
		}
	}
	
	public boolean addParentNode(Node _parent) {
		if(this.parents.contains(_parent)) return false;
		return this.parents.add(_parent);
	}
	
	public Iterator<Node> getParentIterator() {
		return this.parents.iterator();
	}
	
	public ArrayList<Node> getParents() {
		ArrayList<Node> ret = new ArrayList<>();
		Iterator<Node> iterNode = getParentIterator();

		while(iterNode.hasNext()) {
			Node node = iterNode.next();
			ret.add(node);
		}

		return ret;
	}

	public boolean isComposition () {
		return (connection == null);
	}

	public GlycanUndefinedUnit copy () throws GlycanException {
		GlycanUndefinedUnit und = new GlycanUndefinedUnit();
		HashMap<Node, Node> copyIndex = new HashMap<>();
		HashMap<Edge, Edge> copyEdge = new HashMap<>();

		for (Node node : getNodes()) {
			copyIndex.put(node, node.copy());
			for (Edge acceptorEdge : node.getParentEdges()) {
				Edge edge = acceptorEdge.copy();
				edge.setChild(copyIndex.get(node));
				copyEdge.put(acceptorEdge, edge);
			}
		}

		for (Edge coreSide : this.connections) {
			Edge edge;
			if (!copyEdge.containsKey(coreSide)) {
				edge = copyEdge.get(coreSide);
			} else {
				edge = coreSide.copy();
				copyEdge.put(coreSide, edge);
			}

			if (coreSide.getChild() != null) {
				edge.setChild(copyIndex.get(coreSide.getChild()));
			}
			und.addConnection(edge);
		}
		if (!und.getConnections().isEmpty()) {
			und.connection = und.getConnections().get(0);
		}

		und.setProbability(probabilityHigh, probabilityLow);

		// make copy of fragment linkages
		for (Node node : getNodes()) {
			if (this.parents.contains(node)) continue;

			if (node.getParentEdges().isEmpty()) {
				und.addNode(copyIndex.get(node));
				continue;
			}
			for (Edge parentEdge : node.getParentEdges()) {
				Edge edge = copyEdge.get(parentEdge);
				Node copyParent;
				Node copyChild = null;
				Node copySub;

				if (copyIndex.containsKey(parentEdge.getChild())) {
					copyChild = copyIndex.get(parentEdge.getChild());
				} else {
					if (parentEdge.getChild() != null) {
						copyChild = copyIndex.get(parentEdge.getChild());
					}
					if (parentEdge.getSubstituent() != null) {
						copyChild = copyIndex.get(parentEdge.getSubstituent());
					}
				}

				if (copyIndex.containsKey(parentEdge.getParent())) {
					copyParent = copyIndex.get(parentEdge.getParent());
				} else {
					copyParent = null;
				}

				// copy of simple cross linked substituent
				if (parentEdge.getChild() != null && parentEdge.getSubstituent() != null) {
					copySub = parentEdge.getSubstituent().copy();
					edge.setSubstituent(copySub);
				}

				if (copyParent instanceof Monosaccharide) {
					und.addNode(copyParent, edge, copyChild);
				} else {
					copyChild.addParentEdge(edge);
					und.addNode(copyChild);
				}
			}
		}

		return und;
	}
}
