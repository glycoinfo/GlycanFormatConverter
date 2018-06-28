package org.glycoinfo.GlycanFormatconverter.util.exchange;

import java.util.ArrayList;
import java.util.HashMap;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyCoModification;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanGraph;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanRepeatModification;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanUndefinedUnit;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.GlycanFormatconverter.util.traverser.ContainerTraverserBranch;
import org.glycoinfo.GlycanFormatconverter.util.traverser.FormatTraverser;
import org.glycoinfo.GlycanFormatconverter.util.visitor.ContainerVisitor;
import org.glycoinfo.GlycanFormatconverter.util.visitor.VisitorException;

public class GlycoVisitorAnalyzeGlyCoForWURCS implements ContainerVisitor {

	private ArrayList<Edge> substituentLinkages = new ArrayList<Edge>();
	private ArrayList<Edge> linkages = new ArrayList<Edge>();
	private HashMap<Edge, Substituent> linkageToRepeat = new HashMap<Edge, Substituent>();
	
	private GlycanGraph graph;
	private GlycanGraph fragment;
	
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private ArrayList<Node> subGraph = new ArrayList<Node>();
	
	private ArrayList<Substituent> repeats = new ArrayList<Substituent>();
	private ArrayList<GlycanUndefinedUnit> fragments = new ArrayList<GlycanUndefinedUnit>();
	
	public ArrayList<Node> getMonosaccharides() {
		return this.nodes;
	}
	
	public ArrayList<Node> getRootOfSubgraphs() {
		return this.subGraph;
	}
	
	public ArrayList<Edge> getLinkages() {
		ArrayList<Edge> ret = new ArrayList<Edge>();
		ret.addAll(linkages);
		ret.addAll(substituentLinkages);
		return ret;
	}
 	
	public GlycanRepeatModification getRepeatingUnitByEdge(Edge _edge) {
		return (GlycanRepeatModification) this.linkageToRepeat.get(_edge);
	}
	
	public ArrayList<Substituent> getRepeatingUnits() {
		return this.repeats;
	}
	
	public ArrayList<GlycanUndefinedUnit> getFragments() {
		return this.fragments;
	}
	
	@Override
	public void visit(Monosaccharide _monosaccharide) throws VisitorException {
		if (!nodes.contains(_monosaccharide)) nodes.add(_monosaccharide);
		if(!graph.equals(fragment) && !fragment.isComposition()) subGraph.add(_monosaccharide);
	}

	@Override
	public void visit(Substituent _substituent) throws VisitorException {
	} 

	@Override
	public void visit(Edge _edge) throws VisitorException {
		Node child = _edge.getChild();
		if(_edge.getSubstituent() != null) {
			Substituent sub = (Substituent) _edge.getSubstituent();
			
			if(sub instanceof GlycanRepeatModification && !_edge.isCyclic()) {
				this.linkageToRepeat.put(_edge, sub);
			} else {
				if(!substituentLinkages.contains(_edge))
				substituentLinkages.add(_edge);
				return;
			}
		}

		if (!linkages.contains(_edge)) linkages.add(_edge);
	}

	@Override
	public void visit(GlyCoModification _modification) throws VisitorException {
	}
	
	@Override
	public void start(GlyContainer _glyCo) throws VisitorException {
		this.clear();
		
		if(!_glyCo.getUndefinedUnit().isEmpty()) {
			for(GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
				fragments.add(und);
			}
		}

		this.graph = _glyCo;
		this.fragment = _glyCo;
		FormatTraverser traverser = getTraverser(this);
		traverser.traverseGraph(_glyCo);

		for(GlycanUndefinedUnit und : fragments) {
			this.fragment = und;
			traverser.traverseGraph(fragment);
		}
	}

	@Override
	public FormatTraverser getTraverser(ContainerVisitor _visitor) throws VisitorException {
		return new ContainerTraverserBranch(_visitor);
	}
	
	@Override
	public void clear() {
		this.linkages = new ArrayList<Edge>();
		this.linkageToRepeat = new HashMap<Edge, Substituent>();
		
		this.nodes = new ArrayList<Node>();
		this.subGraph = new ArrayList<Node>();
		
		this.repeats = new ArrayList<Substituent>();
		this.fragments = new ArrayList<GlycanUndefinedUnit>();
	}


}
