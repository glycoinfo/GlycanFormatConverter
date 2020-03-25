package org.glycoinfo.GlycanFormatconverter.Glycan;

import java.util.ArrayList;
import java.util.Iterator;

public interface GlycanGraph {

	ArrayList<Node> getRootNodes() throws GlycanException;
	
	Iterator<Node> getNodeIterator();

	boolean isConnected() throws GlycanException;

	boolean removeNode(Node _node) throws GlycanException;

	ArrayList<Node> getNodes();

	ArrayList<Edge> getEdges();

	void addEdge(Node _parent, Node _child, Edge _linkage) throws GlycanException;
	
	void addNode(Node _node) throws GlycanException;

	void addNode(Node _parent, Edge _linkage, Node _child) throws GlycanException;
	
	void addNodeWithSubstituent(Node _parent, Edge _linkage, Substituent _child) throws GlycanException;
	
	boolean containsNode(Node _node);
	
	boolean isParent(Node _parent, Node _current);
	
	void removeEdge(Edge _edge) throws GlycanException;

	boolean isComposition ();
}
