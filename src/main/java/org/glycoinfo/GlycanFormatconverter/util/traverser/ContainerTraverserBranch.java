package org.glycoinfo.GlycanFormatconverter.util.traverser;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.visitor.ContainerVisitor;
import org.glycoinfo.GlycanFormatconverter.util.visitor.VisitorException;

import java.util.ArrayList;
import java.util.Iterator;

public class ContainerTraverserBranch extends FormatTraverser {

	public ContainerTraverserBranch(ContainerVisitor _visitor) throws VisitorException {
		super(_visitor);
	}

	@Override
	public void traverse(Node _node) throws VisitorException {
		this.state = FormatTraverser.ENTER;
		_node.accept(this.visitor);
		ArrayList<Edge> edges = _node.getChildEdges();
		//TODO : compraterが必要か検討
		for(Edge edge : edges) {
			this.traverse(edge);
		}
	}

	@Override
	public void traverse(Edge _edge) throws VisitorException {
		this.state = FormatTraverser.ENTER;
		_edge.accept(this.visitor);
		ArrayList<Node> children = new ArrayList<Node>();
		if (_edge.getChild() != null) children.add(_edge.getChild());
		if (_edge.getSubstituent() != null) {
			children.add(_edge.getSubstituent());
			if (_edge.getSubstituent() instanceof GlycanRepeatModification) return;
		}
		
		for (Node child : children) {
			this.traverse(child);
		}
	}

	@Override
	public void traverseGraph(GlycanGraph _glycanGraph) throws VisitorException {
		ArrayList<Node> roots;
		try {
			if (_glycanGraph.isComposition()) roots =_glycanGraph.getNodes();
			else roots = _glycanGraph.getRootNodes();
			//TODO : compareterが必要か検討
			Iterator<Node> iterNode = roots.iterator();
			while (iterNode.hasNext()) {
				this.traverse(iterNode.next());
			}
		} catch (GlycanException e) {
			throw new VisitorException (e.getMessage(), e);
		}
	}
}