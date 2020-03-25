package org.glycoinfo.GlycanFormatconverter.util.visitor;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.traverser.FormatTraverser;

public interface ContainerVisitor {
	void visit (Monosaccharide _monosaccharide) throws VisitorException;
	void visit (Substituent _substituent) throws VisitorException;
	void visit (Edge _edge) throws VisitorException;
	//public void visit (GlycanRepeatModification _repMod) throws VisitorException;
	void visit (GlyCoModification _modification) throws VisitorException;
	
	void start (GlyContainer _glyCo) throws VisitorException;
	
	FormatTraverser getTraverser (ContainerVisitor _visitor) throws VisitorException;
}
