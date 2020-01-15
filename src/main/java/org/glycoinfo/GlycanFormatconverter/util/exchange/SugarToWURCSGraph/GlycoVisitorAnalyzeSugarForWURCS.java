package org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph;

import org.eurocarbdb.MolecularFramework.io.GlycoCT.GlycoCTTraverser;
import org.eurocarbdb.MolecularFramework.sugar.*;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class for analyzing sugar and collecting elements which be required for WURCSGraph
 * @author MasaakiMatsubara
 *
 */
public class GlycoVisitorAnalyzeSugarForWURCS implements GlycoVisitor {

	private ArrayList<GlycoEdge> m_aModificationLinakges;
	private ArrayList<GlycoEdge> m_aGlycosidicLinakges;
	private HashMap<GlycoEdge, SugarUnitRepeat> m_hashEdgeToRepeatUnit;

	private GlycoGraph m_oGraph;
	private GlycoGraph m_oParentGraph;

	private ArrayList<Monosaccharide> m_aMSs = new ArrayList<Monosaccharide>();
	private ArrayList<Monosaccharide> m_aRootOfSubgraph = new ArrayList<Monosaccharide>();

	private ArrayList<SugarUnitRepeat> m_aRepeats;
	private ArrayList<NonMonosaccharide> m_aNonMS;
	private ArrayList<SugarUnitAlternative> m_aAlternativeUnits;
	private ArrayList<UnderdeterminedSubTree> m_aUnderdeterminedTrees;

	private boolean m_bIsComposition = false;

	public GlycoVisitorAnalyzeSugarForWURCS () {
		this.m_aModificationLinakges = new ArrayList<>();
		this.m_aGlycosidicLinakges   = new ArrayList<>();
		this.m_hashEdgeToRepeatUnit = new HashMap<>();

		this.m_aRepeats              = new ArrayList<>();
		this.m_aNonMS                = new ArrayList<>();
		this.m_aAlternativeUnits     = new ArrayList<>();
		this.m_aUnderdeterminedTrees = new ArrayList<>();
	}

	public ArrayList<Monosaccharide> getMonosaccharides() {
		return this.m_aMSs;
	}

	public ArrayList<Monosaccharide> getRootOfSubgraphMSs() {
		return this.m_aRootOfSubgraph;
	}

	public ArrayList<GlycoEdge> getLinkages() {
		ArrayList<GlycoEdge> t_oEdges = new ArrayList<GlycoEdge>();
		t_oEdges.addAll(this.m_aGlycosidicLinakges);
		t_oEdges.addAll(this.m_aModificationLinakges);
		return t_oEdges;
	}

	public SugarUnitRepeat getRepeatingUnitByEdge(GlycoEdge t_oEdge) {
		if ( !this.m_hashEdgeToRepeatUnit.containsKey(t_oEdge) )
			return null;
		return this.m_hashEdgeToRepeatUnit.get(t_oEdge);
	}

	public ArrayList<SugarUnitRepeat> getRepeatingUnits() {
		return this.m_aRepeats;
	}

	public ArrayList<SugarUnitAlternative> getAlternativeUnits() {
		return this.m_aAlternativeUnits;
	}

	public ArrayList<UnderdeterminedSubTree> getUnderdetereminedTrees() {
		return this.m_aUnderdeterminedTrees;
	}

	public boolean isComposition() {
		return this.m_bIsComposition;
	}

	@Override
	public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {
		this.m_aMSs.add(a_objMonosaccharid);
		// Check subgraph
		if ( !this.m_oGraph.equals(this.m_oParentGraph) )
			this.m_aRootOfSubgraph.add(a_objMonosaccharid);

	}

	@Override
	public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
		this.m_aNonMS.add(a_objResidue);
	}

	@Override
	public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException {
		this.m_aRepeats.add(a_objRepeat);


		for ( UnderdeterminedSubTree t_oSubtree : a_objRepeat.getUndeterminedSubTrees() )
			this.m_aUnderdeterminedTrees.add(t_oSubtree);

		// Store parent graph
		GlycoGraph t_oOldParentGraph = this.m_oParentGraph;
		this.m_oParentGraph = a_objRepeat;

		// Traverse repeating unit
		GlycoTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverseGraph(a_objRepeat);

		// Reset parent graph
		this.m_oParentGraph = t_oOldParentGraph;

		GlycoEdge t_objRepEdge = a_objRepeat.getRepeatLinkage();
		// Collect map between repeating edge and repeating unit
		if ( t_objRepEdge != null ) {
			this.m_hashEdgeToRepeatUnit.put(t_objRepEdge, a_objRepeat);
			this.m_aGlycosidicLinakges.add(t_objRepEdge);
		}
	}

	@Override
	public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
		// Do nothing
	}

	@Override
	public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
		// Do nothing
	}

	@Override
	public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
		this.m_aAlternativeUnits.add(a_objAlternative);
	}

	@Override
	public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException {
		throw new GlycoVisitorException("UnvalidatedGlycoNode is NOT handled in the system.");
	}

	@Override
	public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException {
		GlycoNode t_oChild = a_objLinkage.getChild();
		// For parent edge of substituent
		if ( t_oChild instanceof Substituent ) {
			if ( t_oChild.getChildNodes().size() > 0 ) return;

			// Ignore substituent which is tail of repeating unit
			if ( this.m_oParentGraph instanceof SugarUnitRepeat ) {
				GlycoEdge t_oRepEdge = ((SugarUnitRepeat)this.m_oParentGraph).getRepeatLinkage();
				if ( t_oChild.equals( t_oRepEdge.getParent() ) )
					return;
			}

			this.m_aModificationLinakges.add(a_objLinkage);
			return;
		}

		// Check parent of parent substituent
		GlycoNode t_oParent = a_objLinkage.getParent();
		if ( t_oParent instanceof Substituent && t_oParent.getParentEdge() == null ) {
			// Do nothing if in the subgraph
			if ( this.m_oParentGraph instanceof SugarUnitRepeat ) return;
			if ( this.m_oParentGraph instanceof UnderdeterminedSubTree ) return;
			throw new GlycoVisitorException("Substituent can not be root node.");
		}

		this.m_aGlycosidicLinakges.add(a_objLinkage);
	}

	@Override
	public void start(Sugar a_objSugar) throws GlycoVisitorException {
//		this.clear();
		try {
			Sugar cloneSugar = a_objSugar.copy();
//			if ( cloneSugar.getRootNodes().size() > 1)
//				throw new GlycoVisitorException( "Cannot encode sugar with multiple root nodes." );
			// Concurrent addition-information
			if (cloneSugar.getUndeterminedSubTrees().size()>0)
			{
				for (UnderdeterminedSubTree t_oSubtree : cloneSugar.getUndeterminedSubTrees())
				{
					this.m_aUnderdeterminedTrees.add(t_oSubtree);
				}
			}

			this.m_oGraph = cloneSugar;
			this.m_oParentGraph = cloneSugar;
			GlycoTraverser t_objTraverser = this.getTraverser(this);
			t_objTraverser.traverseGraph(cloneSugar);

			if ( this.m_aGlycosidicLinakges.isEmpty() )
				this.m_bIsComposition = true;

			for ( UnderdeterminedSubTree t_oSubtree : this.m_aUnderdeterminedTrees ) {
				this.m_oParentGraph = t_oSubtree;
				t_objTraverser.traverseGraph(t_oSubtree);
			}
		} catch (GlycoconjugateException e) {
			throw new GlycoVisitorException( e.getMessage() );
		}
	}

	@Override
	public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
		return new GlycoCTTraverser(a_objVisitor);
	}

	@Override
	public void clear() {
	}

	public void start(GlycoNode a_objNode) throws GlycoVisitorException
	{
		GlycoTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverse(a_objNode);

	}

}
