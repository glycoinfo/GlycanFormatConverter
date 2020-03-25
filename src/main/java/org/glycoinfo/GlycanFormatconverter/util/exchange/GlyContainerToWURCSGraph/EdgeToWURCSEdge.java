package org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;
import org.glycoinfo.WURCSFramework.wurcs.graph.DirectionDescriptor;
import org.glycoinfo.WURCSFramework.wurcs.graph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcs.graph.Modification;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSEdge;

import java.util.ArrayList;
import java.util.LinkedList;

public class EdgeToWURCSEdge {

	private Edge parentEdge;
	private Edge childEdge;
	private Monosaccharide parent;
	private Monosaccharide child;
	private Substituent sub;
	
	private LinkedList<WURCSEdge> parentEdges = new LinkedList<>();
	private LinkedList<WURCSEdge> childEdges = new LinkedList<>();
	private Modification mod;
	private int parentSidePos = 0;
	private int childSidePos = 0;

	private double probabilityLow = 1.0D;
	private double probabilityUpper = 1.0D;

	public void setLinkage (Edge _edge) {
		this.parentEdge = _edge;
		this.childEdge = _edge;
	}
	
	public Monosaccharide getParent () {
		return this.parent;
	}
	
	public Monosaccharide getChild () {
		return this.child;
	}
	
	public Substituent getSubstituent () {
		return this.sub;
	}
	
	public LinkedList<WURCSEdge> getParentEdges () {
		return this.parentEdges;
	}

	public LinkedList<WURCSEdge> getChildEdges () {
		return this.childEdges;
	}
	
	public Modification getModification () {
		return this.mod;
	}
	
	/*
	public int getMAPPositionParentSide () {
		return this.parentSidePos;
	}
	 */

	public void start (Edge _edge) throws WURCSException {
		this.setLinkage(_edge);

		this.setParent(_edge);
		this.setChild(_edge);
		
		this.makeModification();
		
		this.setWURCSEdge(true);
		
		if (this.child == null) return;

		init();

		this.setWURCSEdge(false);
	}
	
	protected void setWURCSEdge (boolean _isParent) throws WURCSException {
		if (_isParent) {
			this.parentEdges = this.makeWURCSEdges(parentEdge, true);
		} else {
			this.childEdges = this.makeWURCSEdges(childEdge, false);
		}
	}
	
	protected LinkedList<WURCSEdge> makeWURCSEdges (Edge _edge, boolean _isParent) throws WURCSException {
		LinkedList<WURCSEdge> wedges = new LinkedList<>();
		
		Linkage parentLinkage = _edge.getGlycosidicLinkages().get(0);

		if (_isParent) {
			if (this.probabilityLow == 1.0D) this.probabilityLow = parentLinkage.getParentProbabilityLower();
			if (this.probabilityUpper == 1.0D) this.probabilityUpper = parentLinkage.getParentProbabilityUpper();
		}

		if (_edge.getGlycosidicLinkages().size() == 1) {
			int mapPos = (_isParent) ? this.parentSidePos : this.childSidePos;
			ArrayList<Integer> positions = (_isParent) ?
					parentLinkage.getParentLinkages() : parentLinkage.getChildLinkages();
			WURCSEdge wedge = this.makeWURCSEdge(positions, mapPos);
			wedges.add(wedge);
			return wedges;
		}
		
		ArrayList<Integer> parentPositions = _edge.getGlycosidicLinkages().get(0).getParentLinkages();
		ArrayList<Integer> childPositions = _edge.getGlycosidicLinkages().get(1).getParentLinkages();
		WURCSEdge parentWEdge = this.makeWURCSEdge(parentPositions, this.parentSidePos);
		WURCSEdge childWEdge = this.makeWURCSEdge(childPositions, this.childSidePos);
		wedges.add(parentWEdge);
		wedges.add(childWEdge);
		
		return wedges;
	}
	
	protected WURCSEdge makeWURCSEdge (ArrayList<Integer> _positions, int _mapPosition) throws WURCSException {
		WURCSEdge wedge = new WURCSEdge();
		for (Integer pos : _positions) {
			LinkagePosition linkPos = new LinkagePosition(pos, DirectionDescriptor.L, _mapPosition);
			if (_mapPosition != 0) {
				linkPos = new LinkagePosition(pos, DirectionDescriptor.N, false, _mapPosition, false);
			}

			/* set probability annotation */
			if (probabilityLow != 1.0D) {
				linkPos.setProbabilityLower(probabilityLow);
			}
			if (probabilityUpper != 1.0D)
				linkPos.setProbabilityUpper(probabilityUpper);

			if (probabilityUpper != 1.0D || probabilityLow != 1.0D) {
				linkPos.setProbabilityPosition(LinkagePosition.MODIFICATIONSIDE);
			} else {
				linkPos.setProbabilityPosition(LinkagePosition.BACKBONESIDE);
			}
			wedge.addLinkage(linkPos);
		}
		return wedge;
	}
	
	protected void setParent (Edge _edge) {
		Node parent = _edge.getParent();

		this.parentEdge = _edge;
		
		if(parent instanceof Monosaccharide) {
			this.parent = (Monosaccharide) parent;
		}
	}
	
	protected void setChild (Edge _edge) {
		Node child = _edge.getChild();
		Node substituent = _edge.getSubstituent();

		this.childEdge = _edge;
		
		if (child != null) {
			this.child = (Monosaccharide) child;
		}
		if (substituent == null) return;
		if (!(substituent instanceof Substituent)) return; 
		this.sub = (Substituent) _edge.getSubstituent();
	}
	
	protected void makeModification () throws WURCSExchangeException {
		Modification mod = new Modification("");

		if (this.sub != null) {
			SubstituentToModification subst2mod = new SubstituentToModification();

			if (!(sub instanceof GlycanRepeatModification) && !(sub.getSubstituent() instanceof BaseCrossLinkedTemplate)) {
				probabilityLow = sub.getFirstPosition().getParentProbabilityLower();
				probabilityUpper = sub.getFirstPosition().getParentProbabilityUpper();
			}
			subst2mod.setParentEdge(this.parentEdge);
			
			/*
			if (this.childEdge != this.parentEdge) {
				subst2mod.setChildEdge(this.childEdge);
			}
			 */

			subst2mod.start(this.sub);
			String map = subst2mod.getMAPCode();

			mod = new Modification(map);
			this.parentSidePos = subst2mod.getParentSidePosition();
			this.childSidePos = subst2mod.getChildSidePosition();
		}
		this.mod = mod;
	}

	private void init () {
		this.probabilityLow = 1.0D;
		this.probabilityUpper = 1.0D;
	}
}
