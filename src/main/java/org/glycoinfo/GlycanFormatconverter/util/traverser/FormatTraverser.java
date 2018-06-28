package org.glycoinfo.GlycanFormatconverter.util.traverser;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanGraph;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.util.visitor.ContainerVisitor;
import org.glycoinfo.GlycanFormatconverter.util.visitor.VisitorException;

public abstract class FormatTraverser {

	public static final int ENTER = 0;
	public static final int LEAVE = 1;
	public static final int RETURN = 2;
	
	protected ContainerVisitor visitor = null;
	protected int state = 0;
	
	public FormatTraverser (ContainerVisitor _visitor) throws VisitorException {
		if (_visitor == null) {
			throw new VisitorException("Null visitor given to traverser.");
		}
		visitor = _visitor;
	}
	
	public abstract void traverse (Node _node) throws VisitorException;
	public abstract void traverse (Edge _edge) throws VisitorException;
	
	public abstract void traverseGraph (GlycanGraph _glycanGraph) throws VisitorException; 
	
	public int getState () {
		return this.state;
	}
}
