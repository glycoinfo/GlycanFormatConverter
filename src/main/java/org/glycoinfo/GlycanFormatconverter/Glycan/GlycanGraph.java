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

	boolean addEdge(Node _parent, Node _child, Edge _linkage) throws GlycanException;
	
	boolean addNode(Node _node) throws GlycanException;

	boolean addNode(Node _parent, Edge _linkage, Node _child) throws GlycanException;
	
	boolean addNodeWithSubstituent(Node _parent, Edge _linkage, Substituent _child) throws GlycanException;
	
	boolean containsNode(Node _node);
	
	boolean isParent(Node _parent, Node _current);
	
	boolean removeEdge(Edge _edge) throws GlycanException;

	boolean isComposition ();
}
