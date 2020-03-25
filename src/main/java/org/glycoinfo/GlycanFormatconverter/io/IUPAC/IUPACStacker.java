package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;

import java.util.ArrayList;
import java.util.HashMap;

public class IUPACStacker {

	private ArrayList<Node> nodes;
	private ArrayList<String> notations;
	private HashMap<Node, Node> family;
	private int numOfNode;
	private boolean isFragment;
	private boolean isComposition;

	public IUPACStacker () {
		this.isFragment = false;
		this.isComposition = false;
		this.nodes = new ArrayList<>();
		this.notations = new ArrayList<>();
		this.family = new HashMap<>();
		this.numOfNode = -1;
	}
	
	public ArrayList<Node> getNodes () {
		return this.nodes;
	}
	
	public ArrayList<String> getNotations () {
		return this.notations;
	}
	
	public int getNumOfNode () {
		return this.numOfNode;
	}
	
	public void setNodes (ArrayList<Node> _nodes) {
		nodes.addAll(_nodes);
	}
	
	public void setNotations (ArrayList<String> _notations) {
		notations.addAll(_notations);
	}

	public void setNumOfNode (int _numOfNode) {
		this.numOfNode = _numOfNode;
	}
	
	public boolean isComposition () {
		return this.isComposition;
	}
	
	public boolean haveChild (Node _child) {
		return (family.containsKey(_child));
	}

	public void addFamily (Node _child, Node _parent) {
		family.put(_child, _parent);
	}

	public Node getParent(Node _child) {return family.get(_child);}

	public ArrayList<Node> getChildren (Node _parent) {
		ArrayList<Node> ret = new ArrayList<Node>();
		for (Node node : family.keySet()) {
			if (family.get(node).equals(_parent)) ret.add(node);
		}

		return ret;
	}

	public int getIndexByNode (Node _node) {
		return nodes.indexOf(_node);
	}
	
	public String getNotationByIndex (int _ind) { 
		return notations.get(_ind);
	}
	
	public Node getNodeByIndex (int _ind) {
		return nodes.get(_ind);
	}

	public boolean addNode (Node _node) throws GlyCoImporterException {
		if (_node == null) 
			throw new GlyCoImporterException("Invalid node.");
		return this.nodes.add(_node);
	}
	
	public boolean addNotation (String _notation) throws GlyCoImporterException { 
		if (_notation.equals(""))
			throw new GlyCoImporterException("Invalid notation.");
		return this.notations.add(_notation);
	}

	public void setFragment () {
		this.isFragment = true;
	}

	public boolean isFragment () {
		return this.isFragment;
	}

	public void setComposition () {
		this.isComposition = true;
	}

	public Node getRoot () {
		Node ret = null;
		for (Node node : this.nodes) {
			if (family.get(node) == null) ret = node;
		}
		return ret;
	}
}
