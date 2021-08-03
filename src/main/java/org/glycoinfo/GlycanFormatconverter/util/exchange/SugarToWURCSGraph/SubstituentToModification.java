package org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class SubstituentToModification {

	private Substituent m_oSubstituent;
	private GlycoEdge m_oParentEdge;
	private GlycoEdge m_oChildEdge;
	private LinkageType m_oLinTypeParent;
	private LinkageType m_oLinTypeChild;

	private int m_iMAPPositionParentSide;
	private int m_iMAPPositionChildSide;
	private String m_strHeadAtom;
	private String m_strTailAtom;
	private String m_strMAPCode;

	private SubstituentTypeToMAP m_enumSubstTypeToMAP;

	public SubstituentToModification () {
		m_oParentEdge = null;
		m_oChildEdge = null;

		m_iMAPPositionParentSide = 0;
		m_iMAPPositionChildSide = 0;
		m_strHeadAtom = "";
		m_strTailAtom = "";
		m_strMAPCode = "";
	}

//	public SubstituentToModification(Substituent a_oSubst, LinkageType a_oLinTypeParent, LinkageType a_oLinTypeChild) {
//		this.m_oSubstituent = a_oSubst;
//		this.m_oLinTypeParent = a_oLinTypeParent;
//		this.m_oLinTypeChild  = a_oLinTypeChild;
//	}

	public String getMAPCode() {
		return this.m_strMAPCode;
	}

	public int getMAPPositionParentSide() {
		return this.m_iMAPPositionParentSide;
	}

	public int getMAPPositionChildSide() {
		return this.m_iMAPPositionChildSide;
	}

	public String getHeadAtom() {
		return this.m_strHeadAtom;
	}

	public String getTailAtom() {
		return this.m_strTailAtom;
	}

	public void setParentEdge(GlycoEdge a_oEdge) {
		this.m_oParentEdge = a_oEdge;
	}

	public void setChildEdge(GlycoEdge a_oEdge) {
		this.m_oChildEdge = a_oEdge;
	}

	public void start(Substituent a_oSubst) throws WURCSExchangeException {
		this.m_oSubstituent = a_oSubst;
		this.m_enumSubstTypeToMAP = SubstituentTypeToMAP.forName( this.m_oSubstituent.getSubstituentType().getName() );

		this.m_strHeadAtom = this.m_enumSubstTypeToMAP.getHeadAtom();
		this.m_strTailAtom = this.m_enumSubstTypeToMAP.getTailAtom();

		// Collect linkages around substituent
		ArrayList<LinkageType> t_aLinkageTypes = new ArrayList<LinkageType>();
		// For root of subgraph
		if ( this.m_oSubstituent.getParentEdge() != null )
			this.m_oParentEdge = this.m_oSubstituent.getParentEdge();
		if ( this.m_oParentEdge == null )
			throw new WURCSExchangeException("Substituent must have parent linkage.");
		if ( !this.m_oSubstituent.getChildEdges().isEmpty() )
			this.m_oChildEdge = this.m_oSubstituent.getChildEdges().get(0);

		for ( Linkage t_oLin : this.m_oParentEdge.getGlycosidicLinkages() )
			t_aLinkageTypes.add(t_oLin.getParentLinkageType());

		if ( this.m_oChildEdge != null )
			for ( Linkage t_oLin : this.m_oChildEdge.getGlycosidicLinkages() )
				t_aLinkageTypes.add(t_oLin.getChildLinkageType());

		if ( t_aLinkageTypes.isEmpty() )
			throw new WURCSExchangeException("Substituent having no linkage is NOT handled in this system.");
		if ( t_aLinkageTypes.size() > 2 )
			throw new WURCSExchangeException("Substituent having three or more linkage is NOT handled in this system.");

		this.m_oLinTypeParent = t_aLinkageTypes.get(0);
		this.m_oLinTypeChild  = ( t_aLinkageTypes.size() == 2)? t_aLinkageTypes.get(1) : null ;

		// TODO: Check practice for unknown linkage type
		if ( this.m_oLinTypeParent == LinkageType.UNKNOWN )
			this.m_oLinTypeParent = LinkageType.H_AT_OH;

		// For anhydro substituents ( "anhydro", "epoxy" and "lactone" )
		String t_strMAPDouble = this.m_enumSubstTypeToMAP.getMAPDouble();
		if ( t_strMAPDouble != null && t_strMAPDouble.equals("") ) return;

		this.m_strMAPCode = (this.m_oLinTypeChild == null)? this.getMAPCodeSingle() : this.getMAPCodeDouble();
	}

	public String getMAPCodeSingle() {
		String t_strMAP = this.m_enumSubstTypeToMAP.getMAPSingle();

		// Add oxygen
		if ( this.m_oLinTypeParent == LinkageType.H_AT_OH ) {
			// 20210726 S.TSUCHIYA added
			this.m_strHeadAtom = "O";
			if (this.m_enumSubstTypeToMAP == SubstituentTypeToMAP.NITRATE) {
				this.m_enumSubstTypeToMAP = SubstituentTypeToMAP.O_NITRATE;
				t_strMAP = this.m_enumSubstTypeToMAP.getMAPSingle();
			} else {
				t_strMAP = this.addOxygenToHead(t_strMAP);
			}
			// End of added
			// Original code
			/*
				this.m_strHeadAtom = "O";
				t_strMAP = this.addOxygenToHead(t_strMAP);
			 */
		}

		return "*"+t_strMAP;
	}

	public String getMAPCodeDouble() {
		String t_strMAP = this.m_enumSubstTypeToMAP.getMAPDouble();
		// Set map positions and linkage type
		Boolean t_bIsSwap = this.m_enumSubstTypeToMAP.isSwapCarbonPositions();
		boolean t_bHasOrder = false;
		if ( t_bIsSwap == null && this.m_oLinTypeParent != this.m_oLinTypeChild ) {
			if ( this.m_oLinTypeParent == LinkageType.H_AT_OH ) {
				t_bIsSwap = false;
			} else if ( this.m_oLinTypeChild == LinkageType.H_AT_OH ) {
				t_bIsSwap = true;
			}
		}
		if ( t_bIsSwap != null ) {
			this.m_iMAPPositionParentSide = 1;
			this.m_iMAPPositionChildSide  = 2;
			if ( t_bIsSwap ) {
				this.m_iMAPPositionParentSide = 2;
				this.m_iMAPPositionChildSide  = 1;
			}
			t_bHasOrder = true;
		} else {
			t_bIsSwap = false;
		}

		// Add oxygen
		if ( this.m_oLinTypeParent == LinkageType.H_AT_OH ) {
			this.m_strHeadAtom = "O";
			t_strMAP = (t_bIsSwap)? this.addOxygenToTail(t_strMAP) : this.addOxygenToHead(t_strMAP);
		}

		if ( this.m_oLinTypeChild == LinkageType.H_AT_OH ) {
			this.m_strTailAtom = "O";
			t_strMAP = (t_bIsSwap)? this.addOxygenToHead(t_strMAP) : this.addOxygenToTail(t_strMAP);
		}

		// Add index to MAP star if it has priority order
		if ( t_bHasOrder )
			t_strMAP = this.addMAPStarIndex(t_strMAP);

		t_strMAP = "*"+t_strMAP;

		// Modify chirality for phosphate
		// TODO: check transeration method
		t_strMAP = t_strMAP.replace("*OP^XO*", "*OPO*");
		t_strMAP = t_strMAP.replace("*P^X*", "*P*");

		return t_strMAP;
	}

	private String addOxygenToHead(String a_strMAP) {
		// Ignore for phospho-ethanolamine
		// TODO: Check correct linkage type for phospho-ethanolamine
		if ( a_strMAP.startsWith("NCCOP") ) return a_strMAP;

		// Collect position numbers
		ArrayList<Integer> nums = new ArrayList<Integer>();
		String strnum = "";
		for (int i=0; i < a_strMAP.length(); i++) {
			char ch = a_strMAP.charAt(i);
			if ( Character.isDigit(ch) ) {
				strnum += ch;
				continue;
			}
			if ( strnum.equals("") ) continue;
			if ( nums.contains( Integer.parseInt(strnum) )) continue;
			nums.add( Integer.parseInt(strnum) );
			strnum = "";
		}
		Collections.sort(nums);
		Collections.reverse(nums);
//		System.out.println(nums);

		// Inclement position numbers
		String newMAP = a_strMAP;
		for(Iterator<Integer> it = nums.iterator(); it.hasNext();) {
			Integer num1 = it.next();
			Integer num2 = num1+1;
			newMAP = newMAP.replaceAll(num1.toString(), num2.toString());
		}
		return "O"+newMAP;
	}

	private String addOxygenToTail(String a_strMAP) {
		// Insert "O" to MAP code before last "*"
		StringBuilder sb = new StringBuilder(a_strMAP);
		int t_iInsertPos = a_strMAP.lastIndexOf("*");
		sb.insert(t_iInsertPos, 'O');
		a_strMAP = sb.toString();

		// Count added "O" position
		int t_iPosO = 1;
		for ( int i=0; i < t_iInsertPos; i++) {
			char ch = a_strMAP.charAt(i);
			if ( ch == '^' || ch == '/') {
				i++;
				continue;
			} else if ( ch == '=' || ch == '#' ) {
				continue;
			} else if ( ch == '*' ) {
				break;
			}
			t_iPosO++;
		}
//		System.out.println(t_iPosO);

		// Collect position numbers
		ArrayList<Integer> nums = new ArrayList<Integer>();
		String strnum = "";
		for (int i=0; i < a_strMAP.length(); i++) {
			char ch = a_strMAP.charAt(i);
			if ( Character.isDigit(ch) ) {
				strnum += ch;
				continue;
			}
			if ( strnum.equals("") ) continue;
			if ( nums.contains( Integer.parseInt(strnum) )) continue;
			nums.add( Integer.parseInt(strnum) );
			strnum = "";
		}
		Collections.sort(nums);
		Collections.reverse(nums);
//		System.out.println(nums);

		// inclement position numbers which are greater than added "O" position number
		String newMAP = a_strMAP;
		for(Iterator<Integer> it = nums.iterator(); it.hasNext();) {
			Integer num1 = it.next();
			if (num1 <= t_iPosO) continue;
			Integer num2 = num1+1;
			newMAP = newMAP.replaceAll(num1.toString(), num2.toString());
		}
		return newMAP;
	}

	private String addMAPStarIndex(String a_strMAP) {
		StringBuilder sb = new StringBuilder(a_strMAP);
		int t_iInsertPos2 = a_strMAP.indexOf("*");
		sb.insert(t_iInsertPos2+1, '2');
		sb.insert(0, '1');
		return sb.toString();
	}
}
