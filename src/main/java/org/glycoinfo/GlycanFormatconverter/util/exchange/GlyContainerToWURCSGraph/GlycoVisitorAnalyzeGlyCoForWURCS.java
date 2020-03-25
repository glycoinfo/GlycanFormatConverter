package org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.traverser.ContainerTraverserBranch;
import org.glycoinfo.GlycanFormatconverter.util.traverser.FormatTraverser;
import org.glycoinfo.GlycanFormatconverter.util.visitor.ContainerVisitor;
import org.glycoinfo.GlycanFormatconverter.util.visitor.VisitorException;

import java.util.ArrayList;
import java.util.HashMap;

public class GlycoVisitorAnalyzeGlyCoForWURCS implements ContainerVisitor {

	private ArrayList<Edge> substituentLinkages;
	private ArrayList<Edge> linkages;
	private HashMap<Edge, Substituent> linkageToRepeat;
	
	private GlycanGraph graph;
	private GlycanGraph fragment;
	
	private ArrayList<Node> nodes;
	private ArrayList<Node> subGraph;
	
	private ArrayList<Substituent> repeats;
	private ArrayList<GlycanUndefinedUnit> fragments;

	public GlycoVisitorAnalyzeGlyCoForWURCS () {
		this.linkages = new ArrayList<>();
		this.linkageToRepeat = new HashMap<>();
		this.substituentLinkages = new ArrayList<>();

		this.nodes = new ArrayList<>();
		this.subGraph = new ArrayList<>();

		this.repeats = new ArrayList<>();
		this.fragments = new ArrayList<>();
	}

	public ArrayList<Node> getMonosaccharides() {
		return this.nodes;
	}
	
	public ArrayList<Node> getRootOfSubgraphs() {
		return this.subGraph;
	}
	
	public ArrayList<Edge> getLinkages() {
		ArrayList<Edge> ret = new ArrayList<>();
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
		if(!_glyCo.getUndefinedUnit().isEmpty()) {
			fragments.addAll(_glyCo.getUndefinedUnit());
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
}
