package org.glycoinfo.GlycanFormatconverter.io.GlycoCT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.eurocarbdb.MolecularFramework.sugar.Anomer;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.SubstituentType;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserValdidation;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class GlycoVisitorValidationForWURCS  implements GlycoVisitor
{
	private ArrayList<String> m_aErrorList = new ArrayList<String>();
	private ArrayList<String> m_aWarningList = new ArrayList<String>();
//	private GlycoGraph m_objGlycoGraph = null;
//	private ArrayList<GlycoEdge> m_aEdge = new ArrayList<GlycoEdge>();
//	private GlycoVisitorNodeType m_visNodeType = new GlycoVisitorNodeType();

	public ArrayList<String> getErrors() {
		return this.m_aErrorList;
	}

	public ArrayList<String> getWarnings() {
		return this.m_aWarningList;
	}

	public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
		return new GlycoTraverserValdidation(a_objVisitor);
	}

	@Override
	public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {

		int t_numC = a_objMonosaccharid.getSuperclass().getCAtomCount();

		int t_iRingStart = a_objMonosaccharid.getRingStart();
		boolean t_bUnknownRingSize = ( t_iRingStart == Monosaccharide.UNKNOWN_RING );
		boolean t_bOpenChain       = ( t_iRingStart == Monosaccharide.OPEN_CHAIN );

		Anomer t_oAnom = a_objMonosaccharid.getAnomer();
		// Open chain check
		if ( ( t_oAnom == Anomer.OpenChain ) != t_bOpenChain )
			this.m_aWarningList.add("Anomeric symbol and ring size are not match. :" +a_objMonosaccharid.getGlycoCTName());

		LinkedList<Integer> t_aAnomPositions = new LinkedList<Integer>();

		// Modify base skeletoncode by core modifications
		// if terminal carbon is modified, replace terminal skeletoncode
		// if non-terminal carbon is modified, insert modification code into skeletoncode
		boolean t_bAldose = true;
		int t_nTermMod    = 0;
		boolean t_bDeoxyC1 = false;
		ArrayList<Modification> t_aEnMods = new ArrayList<Modification>();
//		StringBuilder sb = new StringBuilder(skeleton);
		for(Modification modif : a_objMonosaccharid.getModification() ) {
			String modtype = modif.getName();

			if(modif.hasPositionTwo()) { // Modification has two likage position
				int pos1 = modif.getPositionOne();
				int pos2 = modif.getPositionTwo();
//				System.out.println( modif.getPositionOne() +","+ modif.getPositionTwo() +":"+ modif.getName());
				if ( modtype.equals("en") || modtype.equals("enx") ) { // double bond carbons
					// After processing modification with single position,
					// to process double bond carbons for skeletoncharacter code
					t_aEnMods.add(modif);
					if ( pos1 != 1 && pos1 != t_numC ) t_nTermMod++;
					if ( pos2 != 1 && pos2 != t_numC ) t_nTermMod++;
				}
				continue;
			}

			// Modification with single position
//			System.out.println( modif.getPositionOne() +":"+  modif.getName());
			int pos = modif.getPositionOne();
			boolean t_bAtTerm = (pos == 1 || pos == t_numC);

			// For alditol
			if ( modif.getModificationType() == ModificationType.ALDI ) {
//			if ( modtype.equals("aldi") ){
				if ( pos != 1 )
					this.m_aErrorList.add("Modification \"aldi\" must be set to the C1. :" + a_objMonosaccharid.getGlycoCTName());
				t_bAldose = false;
				continue;
			}

			// For carbonyl acid
			if ( modif.getModificationType() == ModificationType.ACID ) {
//			if ( modtype.equals("a") ) {
				// At non-terminal
				if ( !t_bAtTerm ) {
					this.m_aErrorList.add("Can not do carboxylation to non-terminal carbon. :" + a_objMonosaccharid.getGlycoCTName());
				}
				// At terminal
				if ( pos == 1 ) t_bAldose = false;
				if ( pos == t_iRingStart )
					this.m_aErrorList.add("Carboxylic acid contained in the ring. It must be use a substituent \"lactone\". :" + a_objMonosaccharid.getGlycoCTName());
				continue;
			}

			// Count non-terminal modification
			if ( !t_bAtTerm ) t_nTermMod++;

			// For deoxy
			if ( modif.getModificationType() == ModificationType.DEOXY ) {
//			if ( modtype.equals("d") ) {
				if ( pos == 1 ) t_bDeoxyC1 = true;
				continue;
			}

			// For ketose modification
			if ( modif.getModificationType() == ModificationType.KETO ) {
//			if ( modtype.equals("keto") ) {
				t_bAldose = false;
				t_aAnomPositions.add(pos);
				continue;
			}

		}

		// For aldose
		if ( t_bAldose ) {
			t_aAnomPositions.addFirst(1);
			// If deoxy at aldehyde
			if ( t_bDeoxyC1 )
				this.m_aErrorList.add("Can not be deoxy to aldehyde. :" + a_objMonosaccharid.getGlycoCTName());
		}
		// Error if glycosidic linkage position is carbonyl carbon (aldehyde or ketone)
		if ( t_iRingStart == Monosaccharide.OPEN_CHAIN && a_objMonosaccharid.getParentEdge() != null) {
			int t_iPos = a_objMonosaccharid.getParentEdge().getGlycosidicLinkages().get(0).getChildLinkages().get(0);
			if ( !t_aAnomPositions.isEmpty() && t_iPos == t_aAnomPositions.getFirst() )
				this.m_aErrorList.add("Glycosidic linkage must not be aldehyde or ketone position. :" + a_objMonosaccharid.getGlycoCTName());
		}
		// If no anomeric position, it must be open chain structure
		if ( t_aAnomPositions.size() == 0 ) {
			if ( !t_bOpenChain && t_bUnknownRingSize )
				this.m_aWarningList.add("There is no anomeric position in the monosaccharide. It should be open chain. :" + a_objMonosaccharid.getGlycoCTName());

			t_bOpenChain       = true;
			t_bUnknownRingSize = false;
		}

		// Ring start is not anomeric position
		if ( ( !t_bUnknownRingSize && !t_bOpenChain ) && !t_aAnomPositions.contains(t_iRingStart) ) {
			this.m_aErrorList.add("Ring start must be anomeric position. :" + a_objMonosaccharid.getGlycoCTName());
		}

	}

	@Override
	public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException {

		// Traverse repeating unit
		GlycoTraverser t_objTraverser = this.getTraverser(this);
		t_objTraverser.traverseGraph(a_objRepeat);

		// Traverse subtree
		for (Iterator<UnderdeterminedSubTree> t_iterSubTree = a_objRepeat.getUndeterminedSubTrees().iterator(); t_iterSubTree.hasNext();) {
			UnderdeterminedSubTree t_objTree = t_iterSubTree.next();
//			this.m_objGlycoGraph = t_objTree;
			t_objTraverser.traverseGraph(t_objTree);
		}

	}

	@Override
	public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
		if ( a_objSubstituent.getSubstituentType().equals( SubstituentType.R_PYRUVATE )
		  || a_objSubstituent.getSubstituentType().equals( SubstituentType.S_PYRUVATE ) )
			this.m_aErrorList.add("Conversion of \"(r)/(s)-pyruvate\" is not handled now.");

	}

	@Override
	public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void visit(SugarUnitAlternative a_objAlternative)
			throws GlycoVisitorException {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void visit(UnvalidatedGlycoNode a_objUnvalidated)
			throws GlycoVisitorException {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void start(Sugar a_objSugar) throws GlycoVisitorException {
		this.clear();
		try {
//			this.m_objGlycoGraph = a_objSugar;
			if ( a_objSugar.getRootNodes().size() != 1 ) {
//				this.m_aErrorList.add("Sugar has more than one root residue.");
			}
			GlycoTraverser t_objTraverser = this.getTraverser(this);
			t_objTraverser.traverseGraph(a_objSugar);
/*
			GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
			for (Iterator<GlycoNode> t_iterRoot = a_objSugar.getRootNodes().iterator(); t_iterRoot.hasNext();)
			{
				GlycoNode t_objNode = t_iterRoot.next();
				if ( t_visType.isSubstituent(t_objNode) ) {
					this.m_aErrorList.add("A substituent can not be the root node auf an sugar.");
				}
			}

			for (Iterator<UnderdeterminedSubTree> t_iterSubTree = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterSubTree.hasNext();) {
				UnderdeterminedSubTree t_objTree = t_iterSubTree.next();
				this.m_objGlycoGraph = t_objTree;
				if ( t_objTree.getRootNodes().size() != 1 )
				{
					this.m_aErrorList.add("UnderdeterminedSubTree has more than one root residue.");
				}
				t_objTraverser.traverseGraph(t_objTree);
//				this.testUnderdeterminded(t_objTree,a_objSugar);
				if ( t_objTree.getProbabilityLower() < 100.0 )
				{
					this.m_aErrorList.add("Sugar can not have a statistical distribution.");
				}
				if ( t_objTree.getParents().size() < 2 )
				{
					this.m_aErrorList.add("Each uncertain terminal block needs at least 2 parent residues.");
				}
			}
*/
		} catch (GlycoconjugateException e) {
			throw new GlycoVisitorException(e.getMessage(),e);
		}

	}

	@Override
	public void clear() {
		this.m_aErrorList = new ArrayList<String>();
		this.m_aWarningList = new ArrayList<String>();

	}

}
