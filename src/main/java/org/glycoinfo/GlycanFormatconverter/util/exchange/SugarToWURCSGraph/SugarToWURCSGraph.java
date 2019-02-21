package org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph;

import java.util.HashMap;
import java.util.LinkedList;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;
import org.glycoinfo.WURCSFramework.util.graph.WURCSGraphNormalizer;
import org.glycoinfo.WURCSFramework.wurcs.graph.Backbone;
import org.glycoinfo.WURCSFramework.wurcs.graph.BackboneUnknown;
import org.glycoinfo.WURCSFramework.wurcs.graph.DirectionDescriptor;
import org.glycoinfo.WURCSFramework.wurcs.graph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcs.graph.Modification;
import org.glycoinfo.WURCSFramework.wurcs.graph.ModificationAlternative;
import org.glycoinfo.WURCSFramework.wurcs.graph.ModificationRepeat;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

public class SugarToWURCSGraph {

	private WURCSGraph m_oGraph;

	private HashMap<Monosaccharide, Backbone> m_hashMonosaccharideToBackbone = new HashMap<Monosaccharide, Backbone>();

	public WURCSGraph getGraph() {
		return this.m_oGraph;
	}

	public void start(Sugar a_objSugar) throws WURCSExchangeException {
		// Analyze sugar
		GlycoVisitorAnalyzeSugarForWURCS t_oGraphAnal = new GlycoVisitorAnalyzeSugarForWURCS();
		try {
			t_oGraphAnal.start(a_objSugar);
		} catch (GlycoVisitorException e) {
			throw new WURCSExchangeException(e.getErrorMessage());
		}

		this.m_oGraph = new WURCSGraph();

		// For monosaccharides
		for ( Monosaccharide t_oMS : t_oGraphAnal.getMonosaccharides() ) {
			boolean t_bIsRootOfSubgraph = t_oGraphAnal.getRootOfSubgraphMSs().contains(t_oMS);
			this.analyzeMonosaccharide(t_oMS, t_bIsRootOfSubgraph);
		}

		// For linkages
		for ( GlycoEdge t_oGlycoEdge : t_oGraphAnal.getLinkages() ) {
			GlycoEdgeToWURCSEdge t_oEdgeAnal = new GlycoEdgeToWURCSEdge();
			t_oEdgeAnal.start(t_oGlycoEdge);

			Modification t_oMod = t_oEdgeAnal.getModification();
			// For repeating linkage
			if ( t_oGraphAnal.getRepeatingUnitByEdge(t_oGlycoEdge) != null ) {
				ModificationRepeat t_oRepMod = new ModificationRepeat( t_oMod.getMAPCode() );
				SugarUnitRepeat t_oRep = t_oGraphAnal.getRepeatingUnitByEdge(t_oGlycoEdge);
				t_oRepMod.setMinRepeatCount(t_oRep.getMinRepeatCount());
				t_oRepMod.setMaxRepeatCount(t_oRep.getMaxRepeatCount());
				t_oMod = t_oRepMod;
			}

			// For parent side
			Backbone t_oParentB = this.m_hashMonosaccharideToBackbone.get(t_oEdgeAnal.getParent());
			this.makeLinkage(t_oParentB, t_oEdgeAnal.getParentEdges(), t_oMod);

			if ( t_oEdgeAnal.getChild() == null ) continue;
			// For child side
			Backbone t_oChildB = this.m_hashMonosaccharideToBackbone.get(t_oEdgeAnal.getChild());
			this.makeLinkage(t_oChildB, t_oEdgeAnal.getChildEdges(), t_oMod);
		}

		// For underdetermined subtree
		for ( UnderdeterminedSubTree t_oSub : t_oGraphAnal.getUnderdetereminedTrees() ) {
			UnderdeterminedSubTreeToWURCSEdge t_oSubToEdge = new UnderdeterminedSubTreeToWURCSEdge();
			t_oSubToEdge.start(t_oSub);

			Modification t_oMod = t_oSubToEdge.getModification();

			// For parent side
			if ( !t_oSubToEdge.isAleternative() ) {
				Backbone t_oParentBackbone = this.m_hashMonosaccharideToBackbone.get( t_oSubToEdge.getParent() );
				this.makeLinkage(t_oParentBackbone, t_oSubToEdge.getParentEdges(), t_oMod);
				if ( t_oSubToEdge.getChild() != null ) {
					Backbone t_oChildB = this.m_hashMonosaccharideToBackbone.get( t_oSubToEdge.getChild() );
					this.makeLinkage(t_oChildB, t_oSubToEdge.getChildEdges(), t_oMod);
				}
				continue;
			}

			if ( t_oSubToEdge.getParentEdges().size() > 1 )
				throw new WURCSExchangeException("UnderdeterminedSubTree must have only one linkage to parents.");

			ModificationAlternative t_oAltMod = new ModificationAlternative( t_oMod.getMAPCode() );
			for ( Monosaccharide t_oParent : t_oSubToEdge.getParents() ) {
				// Copy edge for each parents
				LinkedList<WURCSEdge> t_oParentEdgesCopy = new LinkedList<WURCSEdge>();
				t_oParentEdgesCopy.add( t_oSubToEdge.getParentEdges().get(0).copy() );

				// Do linkage
				Backbone t_oBackbone = this.m_hashMonosaccharideToBackbone.get(t_oParent);
				this.makeLinkage(t_oBackbone, t_oParentEdgesCopy, t_oAltMod);

				t_oAltMod.addLeadInEdge( t_oParentEdgesCopy.getFirst() );
				t_oMod = t_oAltMod;
			}

			// For child side
			if ( t_oSubToEdge.getChild() != null ) {
				Backbone t_oChildB = this.m_hashMonosaccharideToBackbone.get( t_oSubToEdge.getChild() );
				this.makeLinkage(t_oChildB, t_oSubToEdge.getChildEdges(), t_oMod);
			}
		}

		// For composition
		if ( t_oGraphAnal.isComposition() ) {
			int t_nBackbones = this.m_oGraph.getBackbones().size();
			for ( int i=0; i<t_nBackbones-1; i++ ) {
				ModificationAlternative t_oAltMod = new ModificationAlternative("");
				for ( Backbone t_oBackbone : this.m_oGraph.getBackbones() ) {
					// Create a parent edge
					WURCSEdge t_oEdge = new WURCSEdge();
					t_oEdge.addLinkage( new LinkagePosition( -1, DirectionDescriptor.N, 0 ) );
					LinkedList<WURCSEdge> t_oEdges = new LinkedList<WURCSEdge>();
					t_oEdges.add(t_oEdge);
					this.makeLinkage(t_oBackbone, t_oEdges, t_oAltMod);
					t_oAltMod.addLeadInEdge( t_oEdges.getFirst() );

					// Create a child edge
					t_oEdge = new WURCSEdge();
					t_oEdge.addLinkage( new LinkagePosition( -1, DirectionDescriptor.N, 0 ) );
					t_oEdges = new LinkedList<WURCSEdge>();
					t_oEdges.add(t_oEdge);
					this.makeLinkage(t_oBackbone, t_oEdges, t_oAltMod);
					t_oAltMod.addLeadOutEdge( t_oEdges.getFirst() );
				}
			}
		}

		// Normalize graph
		WURCSGraphNormalizer t_oNorm = new WURCSGraphNormalizer();
		try {
			t_oNorm.start(this.m_oGraph);
		} catch (WURCSException e) {
			throw new WURCSExchangeException(e.getErrorMessage());
		}
	}

	private void analyzeMonosaccharide(Monosaccharide a_oMS, boolean a_bIsRootOfSubgraph) throws WURCSExchangeException {
		MonosaccharideToBackbone t_oMS2B = new MonosaccharideToBackbone();
		if ( a_bIsRootOfSubgraph )
			t_oMS2B.setRootOfSubgraph();

		// Convert monosaccharide to backbone
		t_oMS2B.start(a_oMS);
		Backbone t_oBackbone = t_oMS2B.getBackbone();
		this.m_hashMonosaccharideToBackbone.put(a_oMS, t_oBackbone);

		try {
			this.m_oGraph.addBackbone(t_oBackbone);
		} catch (WURCSException e) {
			throw new WURCSExchangeException(e.getErrorMessage());
		}
/*
		// TODO: remove print
		String t_strCoreStructure = t_oBackbone.getSkeletonCode();
		if ( t_oBackbone.getAnomericPosition() != 0 ) {
			t_strCoreStructure += "-"+t_oBackbone.getAnomericPosition()+t_oBackbone.getAnomericSymbol();
		}
		System.out.println(t_strCoreStructure);
*/
		// Make core modification linkage with unknown position
		for ( Modification t_oCoreMod : t_oMS2B.getCoreModification() ) {
			WURCSEdge t_oEdge = new WURCSEdge();
//			t_oEdge.addLinkage( new LinkagePosition( -1, DirectionDescriptor._, 0 ) );
			t_oEdge.addLinkage( new LinkagePosition( -1, DirectionDescriptor.N, 0 ) );
			if ( t_oCoreMod.getMAPCode().lastIndexOf("*") > 0 )
//				t_oEdge.addLinkage( new LinkagePosition( -1, DirectionDescriptor._, 0 ) );
				t_oEdge.addLinkage( new LinkagePosition( -1, DirectionDescriptor.N, 0 ) );
			LinkedList<WURCSEdge> t_oCoreEdges = new LinkedList<WURCSEdge>();
			t_oCoreEdges.add(t_oEdge);
			this.makeLinkage(t_oBackbone, t_oCoreEdges, t_oCoreMod);
		}

		if ( t_oBackbone.getAnomericPosition() == 0 ) return;
		if ( t_oBackbone instanceof BackboneUnknown ) return;

		// Make ring modification and edges
		Modification t_oRingMod = new Modification("");

		WURCSEdge t_oStartEdge = new WURCSEdge();
		WURCSEdge t_oEndEdge = new WURCSEdge();
		if ( a_oMS.getRingStart() != Monosaccharide.UNKNOWN_RING ) {
//			t_oStartEdge.addLinkage( new LinkagePosition( a_oMS.getRingStart(), DirectionDescriptor._, 0 ) );
			t_oStartEdge.addLinkage( new LinkagePosition( a_oMS.getRingStart(), DirectionDescriptor.N, 0 ) );
//			t_oEndEdge.addLinkage( new LinkagePosition( a_oMS.getRingEnd(), DirectionDescriptor._, 0 ) );
			t_oEndEdge.addLinkage( new LinkagePosition( a_oMS.getRingEnd(), DirectionDescriptor.N, 0 ) );
		} else if ( t_oBackbone.getAnomericPosition() != Monosaccharide.UNKNOWN_RING ) {
//			t_oStartEdge.addLinkage( new LinkagePosition( t_oBackbone.getAnomericPosition(), DirectionDescriptor._, 0 ) );
			t_oStartEdge.addLinkage( new LinkagePosition( t_oBackbone.getAnomericPosition(), DirectionDescriptor.N, 0 ) );
//			t_oEndEdge.addLinkage( new LinkagePosition( Monosaccharide.UNKNOWN_RING, DirectionDescriptor._, 0 ) );
			t_oEndEdge.addLinkage( new LinkagePosition( Monosaccharide.UNKNOWN_RING, DirectionDescriptor.N, 0 ) );
		}
		LinkedList<WURCSEdge> t_oEdges = new LinkedList<WURCSEdge>();
		t_oEdges.add(t_oStartEdge);
		t_oEdges.add(t_oEndEdge);
		this.makeLinkage(t_oBackbone, t_oEdges, t_oRingMod);
	}

	private void makeLinkage(Backbone a_oBackbone, LinkedList<WURCSEdge> a_oEdges, Modification a_oMod) throws WURCSExchangeException {
		try {
			for ( WURCSEdge t_oEdge : a_oEdges )
				this.m_oGraph.addResidues(a_oBackbone, t_oEdge, a_oMod);
		} catch (WURCSException e) {
			throw new WURCSExchangeException(e.getErrorMessage());
		}
	}

}
