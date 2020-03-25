package org.glycoinfo.GlycanFormatconverter.Glycan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GlyContainer implements GlycanGraph {

	private ArrayList<Node> nodes = new ArrayList<>();
	private ArrayList<GlycanUndefinedUnit> antennae = new ArrayList<>();
	private Node aglycone;

	private ArrayList<GlycanUndefinedUnit> undefinedSubstituent = new ArrayList<>(); // 2018/09/25 Masaaki added
	private int nUndefinedLinkage = 0; // 2018/09/24 Masaaki added

	public void setNumberOfUndefinedLinkages(int _nUndefinedLinkages) {
		this.nUndefinedLinkage = _nUndefinedLinkages;
	}

	public int getNumberOfUndefinedLinkages() {
		return this.nUndefinedLinkage;
	}

	public ArrayList<Node> getRootNodes() throws GlycanException {
		ArrayList<Node> ret = new ArrayList<>();
		Node root;

		for(Node unit : getNodes()) {
			root = unit;
			ArrayList<Edge> parentEdges = root.getParentEdges();
			boolean isRootRepeat = false;

			if (parentEdges.isEmpty()) {
				ret.add(root);
			}

			for (Edge parentEdge : parentEdges) {
				if (parentEdge.isReverseEdge()) {
					isRootRepeat = false;
					break;
				}
				if (parentEdge.isRepeat()) isRootRepeat = true;
				if (parentEdge.isCyclic()) ret.add(root);
			}

			if (isRootRepeat && ret.isEmpty()) ret.add(root);
		}

		if(ret.size() == 1) return ret;

		throw new GlycanException ("Node seems not to have at least one root residue");
	}
	
	public Iterator<Node> getNodeIterator() {
		return this.nodes.iterator();
	}
	
	public boolean isConnected() throws GlycanException {
		ArrayList<Node> roots = this.getRootNodes();
		return roots.size() <= 1;
	}
	
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
		
		return this.nodes.remove(_node);
	}
	
	public ArrayList<Node> getAllNodes () {
		ArrayList<Node> ret = new ArrayList<>(getNodes());
		for(GlycanUndefinedUnit und : this.antennae) {
			for(Node root : und.getNodes()) {
				if (!(root instanceof Monosaccharide)) continue;
				if(!ret.contains(root)) ret.add(root);
			}
		}
		return ret;
	}
	
	public ArrayList<Node> getNodes() {
		ArrayList<Node> ret = new ArrayList<>();
		Iterator<Node> iterNode = getNodeIterator();

		while(iterNode.hasNext()) {
			Node node = iterNode.next();
			ret.add(node);
		}

		return ret;
	}

	@Override
	public ArrayList<Edge> getEdges() {
		ArrayList<Edge> ret = new ArrayList<>();
		Iterator<Node> iterNode = getNodeIterator();

		while(iterNode.hasNext()) {
			Node node = iterNode.next();
			ret.addAll(node.getChildEdges());
		}
		return ret;
	}

	public void addNode(Node _node) throws GlycanException {
		if(_node == null) throw new GlycanException ("Invalid residue.");
		if(!this.nodes.contains(_node)) {
			this.nodes.add(_node);
		}
	}
	
	public void addNode(Node _parent, Edge _linkage, Node _child) throws GlycanException {
		if(_parent == null || _child == null) throw new GlycanException ("Invalid residue");
		if(_linkage == null) throw new GlycanException ("Invalid linkage");
		
		if(!containsNode(_parent) && !containsAntennae(_parent)) {
			this.addNode(_parent);
		}
		if(!containsNode(_child) && !containsAntennae(_child)) {
			this.addNode(_child);
		}
		
		_child.addParentEdge(_linkage);
		_parent.addChildEdge(_linkage);
		_linkage.setChild(_child);
		_linkage.setParent(_parent);
	}
	
	public void addNodeWithSubstituent(Node _parent, Edge _linkage, Substituent _child) throws GlycanException {
		if(_parent == null || _child == null) throw new GlycanException ("Invalid residue");
		if(_linkage == null) throw new GlycanException ("Invalid residue");
		
		if(!containsNode(_parent) && !containsAntennae(_parent)) {
			this.addNode(_parent);
		}
		if(!containsNode(_parent))
			throw new GlycanException ("Critical error imposible to add residue.");
		
		_parent.addChildEdge(_linkage);
		_linkage.setSubstituent(_child);
	}
	
	public void addEdge(Node _parent, Node _child, Edge _linkage) throws GlycanException {
		this.addNode(_parent, _linkage, _child);
	}

	public boolean containsNode(Node _node) {
		return this.nodes.contains(_node);
	}
	
	public boolean containsAntennae(Node _node) {
		boolean ret = false;
		for(GlycanUndefinedUnit und : antennae) {
			if(und.getNodes().contains(_node)) {
				ret = true;
				break;
			}
		}

		return ret;
	}
	
	public boolean isParent(Node _parent, Node _current) {
		Node parent = _current.getParentNode();
		if(parent == null) return false;
		if(parent == _parent) return true;
		return this.isParent(_parent, parent);
	}
	
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
		if(this.containsCyclicBelow(_edge))
			throw new GlycanException("");
		
		child.removeParentEdge(_edge);
		parent.removeChildEdge(_edge);
	}
	
	private boolean containsCyclicBelow(Edge _edge) throws GlycanException {
		if(_edge == null) 
			throw new GlycanException("Edge is null");
		
		if(_edge.getParent() == null) return false;
		if(_edge.getSubstituent() == null) return false;
		Substituent sub = (Substituent) _edge.getSubstituent();
		
		if(!(sub instanceof GlycanRepeatModification)) return false;
		if(((GlycanRepeatModification) sub).getMaxRepeatCount() == 1 &&
				((GlycanRepeatModification) sub).getMinRepeatCount() == 1) return true;

		return false;
	}
	
	public void setUndefinedUnit(ArrayList<GlycanUndefinedUnit> _und) throws GlycanException {
		if(_und == null) {
			throw new GlycanException ("null is not a validate set of antennae");
		}
		this.antennae.clear();
		for (GlycanUndefinedUnit glycanUndefinedUnit : _und) {
			this.addGlycanUndefinedUnit(glycanUndefinedUnit);
		}
	}
	
	public ArrayList<GlycanUndefinedUnit> getUndefinedUnit() {
		return this.antennae;
	}

	public GlycanUndefinedUnit getUndefinedUnitWithIndex (Node _node) {
		GlycanUndefinedUnit ret = null;
		for (GlycanUndefinedUnit und : this.antennae) {
			if (und.containsNode(_node)) ret = und;
		}
		return ret;
	}

	public boolean addGlycanUndefinedUnit(GlycanUndefinedUnit _und, Node _parent) throws GlycanException {
		if(!this.antennae.contains(_parent)) {
			throw new GlycanException ("Parent is not part of the glycan");
		}
		if(!this.antennae.contains(_und)) {
			throw new GlycanException ("Undefined unit is not part of the glycan");
		}
		return _und.addParentNode(_parent);
	}
	
	public boolean addGlycanUndefinedUnit(GlycanUndefinedUnit _und) throws GlycanException {
		if(_und == null) {
			throw new GlycanException ("Null is not validate for undefined unit");
		}
		if(!this.antennae.contains(_und)) {
			return this.antennae.add(_und);
		}
		return false;
	}

	/**
	 * @author Masaaki Matsubara
	 * @return
	 */
	public ArrayList<GlycanUndefinedUnit> getUndefinedUnitsForSubstituent() {
		return this.undefinedSubstituent;
	}

	/**
	 *
	 * @author Masaaki Matsubara
	 * @param _und
	 * @param _parent
	 * @return
	 * @throws GlycanException
	 */
	public boolean addGlycanUndefinedUnitForSubstituent(GlycanUndefinedUnit _und, Node _parent) throws GlycanException {
		if(!this.containsNode(_parent)) {
			throw new GlycanException ("Parent is not part of the glycan");
		}
		if(!this.antennae.contains(_und)) {
			throw new GlycanException ("Undefined unit is not part of the glycan");
		}
		return _und.addParentNode(_parent);
	}

	/**
	 *
	 * @author Masaaki Matsubara
	 * @param _und
	 * @return
	 * @throws GlycanException
	 */
	public boolean addGlycanUndefinedUnitForSubstituent(GlycanUndefinedUnit _und) throws GlycanException {
		if(_und == null) {
			throw new GlycanException ("Null is not validate for undefined unit");
		}
		if(!this.undefinedSubstituent.contains(_und)) {
			return this.undefinedSubstituent.add(_und);
		}
		return false;
	}

	public boolean isComposition () {
		return (getAllNodes().size() == getUndefinedUnit().size());
	}

	public void setAglycone (Node _aglycone) {
		this.aglycone = _aglycone;
	}

	public Node getAglycone () {
		return this.aglycone;
	}

	public GlyContainer copy () throws GlycanException {
		GlyContainer copy = new GlyContainer();

		HashMap<Node, Node> copyIndex = new HashMap<>();

		if (this.getAglycone() != null) {
			copy.setAglycone(this.getAglycone().copy());
		}

		if (getNodes().size() == 1 && getNodes().get(0).getParentEdges().isEmpty()) {
			Node current = getNodes().get(0);
			Node copyCurrent = current.copy();
			
			copy.addNode(copyCurrent);
			
			return copy;
		}
		
		for (Node node : getNodes()) {
			for (Edge parentEdge : node.getParentEdges()) {
				Node copyParent;
				Node copyChild;

				if (parentEdge.getChild() == null || parentEdge.getParent() == null) continue;

				if (!copyIndex.containsKey(parentEdge.getChild())) {
					copyChild = parentEdge.getChild().copy();
					copyIndex.put(parentEdge.getChild(), copyChild);
				}

				if (!copyIndex.containsKey(parentEdge.getParent())) {
					copyParent = parentEdge.getParent().copy();
					copyIndex.put(parentEdge.getParent(), copyParent);
				}
			}
		}

		/* make copy of fragments */
		for (GlycanUndefinedUnit und : getUndefinedUnit()) {
			GlycanUndefinedUnit copyUnd = und.copy();
			copy.addGlycanUndefinedUnit(copyUnd);

			ArrayList<Node> acceptorNodes = new ArrayList<>();
			for (Node acceptor : und.getParents()) {
				acceptorNodes.add(copyIndex.get(acceptor));
			}
			copyUnd.setParentNodes(acceptorNodes);
		}

		// make copy of core
		for (Node node : getNodes()) {
			for (Edge parentEdge : node.getParentEdges()) {				
				Edge copyEdge = parentEdge.copy();
				Node copyParent;
				Node copyChild;
				Node copySub;

				if (containsAntennae(node)) {
				    continue;
                }

				if (copyIndex.containsKey(parentEdge.getChild())) {
					copyChild = copyIndex.get(parentEdge.getChild());
				} else {
					copyChild = parentEdge.getChild().copy();
				}

				if (copyIndex.containsKey(parentEdge.getParent())) {
					copyParent = copyIndex.get(parentEdge.getParent());
				} else {
					copyParent = parentEdge.getParent().copy();
				}

				// copy of simple cross linked substituent
				if (parentEdge.getChild() != null && parentEdge.getSubstituent() != null) {
					copySub = parentEdge.getSubstituent().copy();
					copyEdge.setSubstituent(copySub);
				}

				copy.addNode(copyParent, copyEdge, copyChild);
			}
		}

		return copy;
	}
}
