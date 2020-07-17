package org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph;

import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.*;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;
import org.glycoinfo.WURCSFramework.util.property.AtomicProperties;
import org.glycoinfo.WURCSFramework.wurcs.graph.Modification;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;

import java.util.ArrayList;
import java.util.LinkedList;

public class MonosaccharideToBackbone {

	private Backbone m_oBackbone;

	private int m_iAnomericPosition = 0;
	private char m_cAnomericSymbol = 'x';
	private char m_cConfigurationalSymbol = 'X';

	private Monosaccharide m_oMonosaccharide;
	private boolean m_bRootOfSubgraph = false;

	private LinkedList<Modification> m_aUnknownPosCoreMod = new LinkedList<Modification>();

	public Monosaccharide getMonosaccaride() {
		return this.m_oMonosaccharide;
	}

	public Backbone getBackbone() {
		return this.m_oBackbone;
	}

	public LinkedList<Modification> getCoreModification() {
		return this.m_aUnknownPosCoreMod;
	}

	public void setRootOfSubgraph() {
		this.m_bRootOfSubgraph = true;
	}

	private boolean hasParent() {
		if ( this.m_bRootOfSubgraph ) return true;

		if ( this.m_oMonosaccharide.getParentNode() != null ) return true;

		for ( GlycoEdge t_objChildEdge : this.m_oMonosaccharide.getChildEdges() ) {
			for ( Linkage t_objLink : t_objChildEdge.getGlycosidicLinkages() ) {
				for ( Integer t_iLinkPos : t_objLink.getParentLinkages() ) {
					int t_iRingStart = this.m_oMonosaccharide.getRingStart();
					if ( t_iRingStart != -1 && t_iLinkPos == t_iRingStart ) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void start(Monosaccharide a_oMonosaccharide) throws WURCSExchangeException {
		this.m_oMonosaccharide = a_oMonosaccharide;

		this.m_iAnomericPosition = a_oMonosaccharide.getRingStart();
		this.m_cAnomericSymbol   = a_oMonosaccharide.getAnomer().getSymbol().charAt(0);

		// Correct anomeric symbol
		if ( this.m_iAnomericPosition  == Monosaccharide.OPEN_CHAIN ) {
			this.m_cAnomericSymbol = 'o';
		}

		int t_nCAtom = a_oMonosaccharide.getSuperclass().getCAtomCount();
		// For unknown sugar
		if ( t_nCAtom == 0 ) {
			//20200717, S.TSUCHIYA, changed
			this.m_oBackbone = new Backbone();
			this.m_oBackbone.setAnomericSymbol(this.m_cAnomericSymbol);
			//this.m_oBackbone = new BackboneUnknown_TBD(this.m_cAnomericSymbol);
			return;
		}

		// Analyze monosaccharide
		MonosaccharideAnalyzer t_oMSAnal = new MonosaccharideAnalyzer();
		t_oMSAnal.analyze(a_oMonosaccharide);

		this.m_iAnomericPosition      = t_oMSAnal.getAnomericPosition();
		this.m_cAnomericSymbol        = t_oMSAnal.getAnomericSymbol();
		this.m_cConfigurationalSymbol = t_oMSAnal.getConfigurationalSymbol();
		String t_strSkeletonCode      = t_oMSAnal.getSkeletonCode();

		for ( String t_strMAP : t_oMSAnal.getCoreModificationUnknownPosition() )
			this.m_aUnknownPosCoreMod.add( new Modification(t_strMAP) );

		// For unknown anomeric position
		if ( this.m_iAnomericPosition == Monosaccharide.UNKNOWN_RING ) {
			// Do nothing for the root monosaccharide with unknown anomer and ring size
			// TODO: To be confirmed how to handle the root residue with unknown anomer and ring size
			// If not reducing end
//			if ( !this.hasParent() && this.m_cAnomericSymbol == 'x' ) {
//				t_strSkeletonCode = t_strSkeletonCode.replaceAll("o", "u");
//				t_strSkeletonCode = t_strSkeletonCode.replaceAll("O", "U");
//				this.m_iAnomericPosition = Monosaccharide.OPEN_CHAIN;
//			} else {
				if ( t_strSkeletonCode.contains("o") ) {
					this.m_iAnomericPosition = t_strSkeletonCode.indexOf("o")+1;
					t_strSkeletonCode = t_strSkeletonCode.replaceFirst("o", "a");
				} else if ( t_strSkeletonCode.contains("O") ) {
					this.m_iAnomericPosition = t_strSkeletonCode.indexOf("O")+1;
					t_strSkeletonCode = t_strSkeletonCode.replaceFirst("O", "a");
				}
//			}
		}

		StringBuilder t_sbSkeletonCode = new StringBuilder(t_strSkeletonCode);
		// Replace CarbonDescriptor by substituents
		if ( a_oMonosaccharide.getParentEdge() != null ) {
			this.replaceCarbonDescriptorByEdge(t_sbSkeletonCode, a_oMonosaccharide.getParentEdge(), true);
		}
		for ( GlycoEdge t_oChildEdge : a_oMonosaccharide.getChildEdges() ) {
			this.replaceCarbonDescriptorByEdge(t_sbSkeletonCode, t_oChildEdge, false);
		}

		// Construct backbone
		//20200717, S.TSUCHIYA, changed
		Backbone t_oBackbone = new Backbone();
		t_oBackbone.setAnomericPosition(this.m_iAnomericPosition);
		t_oBackbone.setAnomericSymbol(this.m_cAnomericSymbol);
		for ( int i=0; i<t_nCAtom; i++ ) {
			char t_cCD = t_sbSkeletonCode.charAt(i);
			CarbonDescriptor t_enumCD = CarbonDescriptor.forCharacter(t_cCD, ( i == 0 || i == t_nCAtom-1 ) );
			BackboneCarbon t_oBC = new BackboneCarbon(t_oBackbone, t_enumCD);
			t_oBackbone.addBackboneCarbon(t_oBC);
		}
		this.m_oBackbone = t_oBackbone;
		/*
		Backbone_TBD t_oBackbone = new Backbone_TBD();
		t_oBackbone.setAnomericPosition(this.m_iAnomericPosition);
		t_oBackbone.setAnomericSymbol(this.m_cAnomericSymbol);
		for ( int i=0; i<t_nCAtom; i++ ) {
			char t_cCD = t_sbSkeletonCode.charAt(i);
			CarbonDescriptor_TBD t_enumCD = CarbonDescriptor_TBD.forCharacter(t_cCD, ( i == 0 || i == t_nCAtom-1 ) );
			BackboneCarbon t_oBC = new BackboneCarbon(t_oBackbone, t_enumCD);
			t_oBackbone.addBackboneCarbon(t_oBC);
		}
		this.m_oBackbone = t_oBackbone;
		 */
	}

	private void replaceCarbonDescriptorByEdge(StringBuilder a_sbSC, GlycoEdge a_oEdge, boolean a_bIsParentSideEdge) throws WURCSExchangeException {
		// Check substituent
		GlycoNode t_oNode = (a_bIsParentSideEdge)? a_oEdge.getParent() : a_oEdge.getChild();
		Substituent t_oSubst = ( t_oNode instanceof Substituent )? (Substituent)t_oNode : null;
		boolean t_bSwapChirality = false;
//		if ( t_oNode instanceof Substituent )
//			t_bSwapChirality = ( this.compareConnectAtom(a_oEdge, a_bIsParentSideEdge) < 0 );

		// Get child side information of parent edge
		for ( Linkage t_oLink : a_oEdge.getGlycosidicLinkages() ) {
			// Ignore fuzzy linkage
			ArrayList<Integer> t_aPositions = (a_bIsParentSideEdge)? t_oLink.getChildLinkages() : t_oLink.getParentLinkages();
			if ( t_aPositions.size() > 1 ) continue;

			t_bSwapChirality = ( this.compareConnectAtom(t_oLink, t_oSubst, a_bIsParentSideEdge ) < 0 );
			Linkage t_oParentLink = t_oLink;
			// For child of repeating unit
			if ( a_oEdge.getParent() instanceof SugarUnitRepeat )
				t_oParentLink = ((SugarUnitRepeat)a_oEdge.getParent()).getRepeatLinkage().getGlycosidicLinkages().get(0);

			int t_iPos = t_aPositions.get(0);

			// Ignore unknown position
			if ( t_iPos == -1 ) continue;
			char t_cCD = a_sbSC.charAt(t_iPos-1);
			char t_cNewCD = t_cCD;

			LinkageType t_oType0 = (a_bIsParentSideEdge)? t_oLink.getChildLinkageType() : t_oParentLink.getParentLinkageType();
			LinkageType t_oType1 = (a_bIsParentSideEdge)? t_oParentLink.getParentLinkageType() : t_oLink.getChildLinkageType();
			if ( t_oType0 == LinkageType.H_LOSE ) {
				t_cNewCD = this.replaceCarbonDescriptorByHydrogenLose(t_cCD, t_bSwapChirality);
			} else if ( t_oType0 == LinkageType.DEOXY && t_oType1 != LinkageType.H_AT_OH ) {
				t_cNewCD =	( t_cCD == 'c' )? 'x' :
							( t_cCD == 'C' )? 'X' : t_cNewCD;
			}

			a_sbSC.replace(t_iPos-1, t_iPos, t_cNewCD+"");
		}

		if ( !a_bIsParentSideEdge ) return;
		if ( this.m_iAnomericPosition == Monosaccharide.OPEN_CHAIN
		  || this.m_iAnomericPosition == Monosaccharide.UNKNOWN_RING ) return;

		// For anomeric position
		if ( this.m_cAnomericSymbol != 'a' && this.m_cAnomericSymbol != 'b' ) return;

		// Correct anomeric stereo configuration for anomeric position
		char t_cAnomCD = a_sbSC.charAt(this.m_iAnomericPosition-1);
		if ( t_cAnomCD != 'x' && t_cAnomCD != 'X' ) return;

		char t_cAnomStereo = (this.m_cAnomericSymbol == 'a')? '1' : '2';
		// Swap by D/L
		if ( this.m_cConfigurationalSymbol == 'L' )
			t_cAnomStereo = (t_cAnomStereo == '1')? '2' : '1';
		// Swap by substituent
		if ( t_bSwapChirality )
			t_cAnomStereo = (t_cAnomStereo == '1')? '2' : '1';

		if ( t_cAnomCD == 'X' )
			t_cAnomStereo = (t_cAnomStereo == '1')? '5' : '6';

		a_sbSC.replace(this.m_iAnomericPosition-1, this.m_iAnomericPosition, t_cAnomStereo+"");

	}

	/**
	 * Replace CarbonDescriptor by linkage types
	 * @param a_cCD Target character of CarbonDescriptor
	 * @param a_oType0 LinkageType of the monosaccharide side
	 * @param a_oType1 LinkageType of the partner residue side
	 * @return Replased character of CarbonDescriptor
	 */
	private char replaceCarbonDescriptorByLinkageType(char a_cCD, LinkageType a_oType0, LinkageType a_oType1) {
		char t_cNewCD = a_cCD;

		if ( a_oType0 == LinkageType.DEOXY && a_oType1 != LinkageType.H_AT_OH) {
			t_cNewCD = ( a_cCD == 'c' )? 'x' :
					   ( a_cCD == 'C' )? 'X' : t_cNewCD;
		}
		if ( a_oType0 == LinkageType.H_LOSE ) {
			t_cNewCD = ( a_cCD == '1' )? '5' :
					   ( a_cCD == '2' )? '6' :
					   ( a_cCD == '3' )? '7' :
					   ( a_cCD == '4' )? '8' :
					   ( a_cCD == 'x' )? 'X' :
					   ( a_cCD == 'c' )? 'C' :
					   ( a_cCD == 'm' )? 'h' :
					   ( a_cCD == 'h' )? 'c' : t_cNewCD;
		}
		return t_cNewCD;
	}

	private char replaceCarbonDescriptorByHydrogenLose(char a_cCD, boolean a_bSwapChirality) {
		char t_cNewCD =	( a_cCD == '1' )? '5' :
						( a_cCD == '2' )? '6' :
						( a_cCD == '3' )? '7' :
						( a_cCD == '4' )? '8' :
						( a_cCD == 'x' )? 'X' :
						( a_cCD == 'c' )? 'C' :
						( a_cCD == 'm' )? 'h' :
						( a_cCD == 'h' )? 'c' : a_cCD;
		if ( a_bSwapChirality ) {
			t_cNewCD =	( t_cNewCD == '5' )? '6' :
						( t_cNewCD == '6' )? '5' :
						( t_cNewCD == '7' )? '8' :
						( t_cNewCD == '8' )? '7' : t_cNewCD;
		}

		return t_cNewCD;
	}

	private int compareConnectAtom(Linkage a_oLink, Substituent a_oSubst, boolean a_bIsParentSide) throws WURCSExchangeException {
		// Check linkage type
		LinkageType t_oParentType = (a_bIsParentSide)? a_oLink.getChildLinkageType() : a_oLink.getParentLinkageType();
		LinkageType t_oChildType = (a_bIsParentSide)? a_oLink.getParentLinkageType() : a_oLink.getChildLinkageType();
		if ( t_oParentType == LinkageType.H_AT_OH || t_oChildType == LinkageType.H_AT_OH ) return 0;

		if ( a_oSubst == null ) return 0;
		return this.compareConnectAtomOfSubstituent(a_oSubst, a_bIsParentSide);
	}

	private int compareConnectAtomOfSubstituent(Substituent a_oSubst, boolean a_bSubstituentIsParent) throws WURCSExchangeException {
		SubstituentToModification t_oSubstToMod = new SubstituentToModification();
		t_oSubstToMod.start(a_oSubst);
		String t_strConnAtom = (a_bSubstituentIsParent)? t_oSubstToMod.getTailAtom() : t_oSubstToMod.getHeadAtom();

		int t_iNumberOfFirstAtom = AtomicProperties.forSymbol( t_strConnAtom ).getAtomicNumber();
		int t_iComp = 16 - AtomicProperties.forSymbol( t_strConnAtom ).getAtomicNumber();
		if ( t_iNumberOfFirstAtom < 16 ) return 1;
		return -1;
	}


}
