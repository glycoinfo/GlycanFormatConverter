package org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph;

import org.eurocarbdb.MolecularFramework.sugar.*;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;
import org.glycoinfo.WURCSFramework.wurcs.graph.DirectionDescriptor;
import org.glycoinfo.WURCSFramework.wurcs.graph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcs.graph.Modification;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSEdge;

import java.util.ArrayList;
import java.util.LinkedList;

public class GlycoEdgeToWURCSEdge {

	private GlycoEdge m_oParentLinkage;
	private GlycoEdge m_oChildLinkage;
	private Monosaccharide m_oParentMS;
	private Monosaccharide m_oChildMS;
	private Substituent m_oSubst;

	private LinkedList<WURCSEdge> m_aParentEdges = new LinkedList<WURCSEdge>();
	private LinkedList<WURCSEdge> m_aChildEdges  = new LinkedList<WURCSEdge>();
	private Modification m_oModification;
	private int m_iMAPPositionParentSide = 0;
	private int m_iMAPPositionChildSide = 0;

	public void setLinkage(GlycoEdge a_oEdge) {
		this.m_oParentLinkage = a_oEdge;
		this.m_oChildLinkage = a_oEdge;
	}

	public Monosaccharide getParent() {
		return this.m_oParentMS;
	}

	public Monosaccharide getChild() {
		return this.m_oChildMS;
	}

	public Substituent getSubstituent() {
		return this.m_oSubst;
	}

	public LinkedList<WURCSEdge> getParentEdges() {
		return this.m_aParentEdges;
	}

	public LinkedList<WURCSEdge> getChildEdges() {
		return this.m_aChildEdges;
	}

	public Modification getModification() {
		return this.m_oModification;
	}

	public int getMAPPositionParentSide() {
		return this.m_iMAPPositionParentSide;
	}

	public void start(GlycoEdge a_oEdge) throws WURCSExchangeException {
		this.setLinkage(a_oEdge);

		GlycoNode t_oParentNode = GlycoEdgeAnalyzer.getParentNode(a_oEdge);
		GlycoNode t_oChildNode  = GlycoEdgeAnalyzer.getChildNode(a_oEdge);

		if ( t_oParentNode instanceof Substituent && t_oChildNode instanceof Substituent )
			throw new WURCSExchangeException("Substituent must not link to Substituent.");
		if ( t_oChildNode instanceof Substituent && t_oChildNode.getChildEdges().size() > 1 )
			throw new WURCSExchangeException("Substituent having two or more children is NOT handled in the system.");

		// Set parent and child
		this.setParent(t_oParentNode);
		this.setChild(t_oChildNode);

		// Make modification and position information
		this.makeModificaiton();

		this.setWURCSEdge(true);

		if ( this.m_oChildMS == null ) return;

		this.setWURCSEdge(false);
	}

	protected void setWURCSEdge(boolean a_bIsParent) {
		if ( a_bIsParent ) {
			this.m_aParentEdges = this.makeWURCSEdges(this.m_oParentLinkage, this.m_iMAPPositionParentSide, a_bIsParent);
		} else {
			this.m_aChildEdges = this.makeWURCSEdges(this.m_oChildLinkage, this.m_iMAPPositionChildSide, a_bIsParent);
		}
	}

	protected LinkedList<WURCSEdge> makeWURCSEdges(GlycoEdge a_oEdge, int a_iMAPPosition, boolean a_bIsParent) {
		LinkedList<WURCSEdge> t_oEdges = new LinkedList<WURCSEdge>();
		Linkage t_oParentLinkage = a_oEdge.getGlycosidicLinkages().get(0);
		if ( a_oEdge.getGlycosidicLinkages().size() == 1 ) {
			int t_iMAPPos = (a_bIsParent)? this.m_iMAPPositionParentSide : this.m_iMAPPositionChildSide;
			ArrayList<Integer> t_aPositions = (a_bIsParent)? t_oParentLinkage.getParentLinkages() : t_oParentLinkage.getChildLinkages();
			WURCSEdge t_oEdge = this.makeWURCSEdge(t_aPositions, t_iMAPPos);
			t_oEdges.add(t_oEdge);
			return t_oEdges;
		}

		// For divalent substituent on a monosaccharide
		ArrayList<Integer> t_aParentPositions = a_oEdge.getGlycosidicLinkages().get(0).getParentLinkages();
		ArrayList<Integer> t_aChildPositions  = a_oEdge.getGlycosidicLinkages().get(1).getParentLinkages();
		WURCSEdge t_oParentEdge = this.makeWURCSEdge(t_aParentPositions, this.m_iMAPPositionParentSide);
		WURCSEdge t_oChildEdge  = this.makeWURCSEdge(t_aChildPositions,  this.m_iMAPPositionChildSide);
		t_oEdges.add(t_oParentEdge);
		t_oEdges.add(t_oChildEdge);

		return t_oEdges;
	}

	protected WURCSEdge makeWURCSEdge(ArrayList<Integer> a_aPositions, int a_iMAPPosition) {
		WURCSEdge t_oEdge = new WURCSEdge();
		for ( Integer t_iPos : a_aPositions ) {
			LinkagePosition t_oLinkPos = new LinkagePosition(t_iPos, DirectionDescriptor.N, a_iMAPPosition);
			if ( a_iMAPPosition != 0 )
				t_oLinkPos = new LinkagePosition(t_iPos, DirectionDescriptor.N, false, a_iMAPPosition, false );

			t_oEdge.addLinkage(t_oLinkPos);
		}
		return t_oEdge;
	}

	protected void setParent(GlycoNode a_oNode) {
		GlycoNode t_oParentNode = a_oNode;

		if ( t_oParentNode instanceof Substituent ) {
			this.m_oSubst = (Substituent)t_oParentNode;
			this.m_oParentLinkage = this.m_oSubst.getParentEdge();
			t_oParentNode = GlycoEdgeAnalyzer.getParentNode(this.m_oParentLinkage);
		}
		this.m_oParentMS = (Monosaccharide)t_oParentNode;
	}

	protected void setChild(GlycoNode a_oNode) {
		GlycoNode t_oChildNode  = a_oNode;

		if ( t_oChildNode instanceof Monosaccharide )
			this.m_oChildMS = (Monosaccharide)t_oChildNode;

		if ( !(t_oChildNode instanceof Substituent) ) return;

		this.m_oSubst = (Substituent)t_oChildNode;

		if ( t_oChildNode.getChildEdges().isEmpty() ) return;

		this.m_oChildLinkage = t_oChildNode.getChildEdges().get(0);
		t_oChildNode = GlycoEdgeAnalyzer.getChildNode( this.m_oChildLinkage );
		this.m_oChildMS = (Monosaccharide)t_oChildNode;
	}

	protected void makeModificaiton() throws WURCSExchangeException {
		// Make modification and position information
		Modification t_oMod = new Modification("");
		if ( this.m_oSubst != null ) {
			SubstituentToModification t_oSubstToMod = new SubstituentToModification();

			// Set parent edge for root of subgraph
			t_oSubstToMod.setParentEdge( this.m_oParentLinkage );

			// Set child edge for leaf of subgraph
			if ( this.m_oChildLinkage != this.m_oParentLinkage )
				t_oSubstToMod.setChildEdge( this.m_oChildLinkage );

			t_oSubstToMod.start(this.m_oSubst);
			String t_strMAP = t_oSubstToMod.getMAPCode();
			t_oMod = new Modification(t_strMAP);
			this.m_iMAPPositionParentSide = t_oSubstToMod.getMAPPositionParentSide();
			this.m_iMAPPositionChildSide  = t_oSubstToMod.getMAPPositionChildSide();
		}
		this.m_oModification = t_oMod;
	}

}
