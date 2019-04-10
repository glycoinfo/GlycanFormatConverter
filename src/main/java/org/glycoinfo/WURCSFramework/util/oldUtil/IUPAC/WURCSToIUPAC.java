package org.glycoinfo.WURCSFramework.util.oldUtil.IUPAC;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.util.oldUtil.WURCSSequence2ToResidueContainer;
import org.glycoinfo.WURCSFramework.util.residuecontainer.LinkageBlock;
import org.glycoinfo.WURCSFramework.util.residuecontainer.RepeatingBlock;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainer;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainerException;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainerUtility;
import org.glycoinfo.WURCSFramework.util.residuecontainer.RootStatusDescriptor;

public class WURCSToIUPAC {

	private StringBuilder a_sExtendedIUPAC;
	private StringBuilder a_sCondensedIUPAC;
	private StringBuilder a_sShortIUPAC;
	
	private StringBuilder a_sExtendedFragments;
	private StringBuilder a_sCondensedFragments;
	private StringBuilder a_sShortFragments;
	
	private final String EXTENDED = IUPACFormatDescriptor.EXTENDED.getFormat();
	private final String CONDENSED = IUPACFormatDescriptor.CONDENSED.getFormat();
	private final String SHORT = IUPACFormatDescriptor.SHORT.getFormat();
	
	private ResidueContainerUtility a_uRCU;
	
	public String getExtendedIUPAC() {
		return this.a_sExtendedIUPAC.toString();
	}
	
	public String getCondensedIUPAC() {
		return this.a_sCondensedIUPAC.toString();
	}
	
	public String getShortIUPAC() {
		return this.a_sShortIUPAC.toString();
	}
	
	public void start(String a_sWURCS) throws Exception {
		this.init();
		
		this.a_uRCU.setResidueList(
			new WURCSSequence2ToResidueContainer().start(a_sWURCS));
		
		/** append monosaccharide */
		addNode(this.a_uRCU.getRoot(), false);
		
		/** append fragments */
		for(ResidueContainer a_oRC : this.a_uRCU.getRootOfFragments()) {
			addNode(a_oRC, true);

			this.a_sCondensedIUPAC.insert(0, this.a_sCondensedFragments + ",");
			this.a_sExtendedIUPAC.insert(0, this.a_sExtendedFragments + ",");
			this.a_sShortIUPAC.insert(0, this.a_sShortFragments + ",");
			
			this.a_sCondensedFragments = new StringBuilder();
			this.a_sExtendedFragments = new StringBuilder();
			this.a_sShortFragments = new StringBuilder();			
		}
		
		
		/** extract antennae of substituent */
		//this.a_uRCU.extractSubstituentOfAntennae();
		
		/***/
		//for(SubstituentTemplate a_enumSub : this.a_uRCU.getSubtituentOfFragments()) {
			
		//}
		
		return;
	}
	
	private void addNode(ResidueContainer a_oRC, boolean a_bIsFragments) 
			throws ConverterIUPACException, ResidueContainerException {
		if(a_oRC == null) throw new ConverterIUPACException("This structure can not handled");
		
		StringBuilder a_sExtended = new StringBuilder();
		StringBuilder a_sCondensed = new StringBuilder();
		StringBuilder a_sShort = new StringBuilder();
		LinkageBlock a_oLB = a_oRC.getLinkage();
		
		/** set monosaccharide name */
		a_sExtended.append(replaceNeuraminicSubstituent(a_oRC.getIUPACExtendedNotation()));
		a_sCondensed.append(a_oRC.getIUPACCondensedNotaiton());
		a_sShort.append(a_oRC.getSugarName());
		
		/** append repeating information */
		a_sExtended = appendRepeatingBlock(a_oRC, a_sExtended, EXTENDED);
		a_sCondensed = appendRepeatingBlock(a_oRC, a_sCondensed, CONDENSED);
		a_sShort = appendRepeatingBlock(a_oRC, a_sShort, SHORT);

		/** append cyclic symbol*/
		a_sExtended =  appendCyclicNotation(a_oRC, a_sExtended, EXTENDED);
		a_sCondensed = appendCyclicNotation(a_oRC, a_sCondensed, CONDENSED);
		a_sShort = appendCyclicNotation(a_oRC, a_sShort, SHORT);
		
		/** append linkage position */
		if(a_sExtended.indexOf("->") == -1) {
			a_sExtended.append(makeLinkageExtended(a_oRC));
		}
		if(a_sShort.indexOf("]") == -1) {
			a_sShort.append(makeLinkageCondensed(a_oRC));
			a_sCondensed.append(makeLinkageShort(a_oRC));
		}
				
		/** check number of child */
		int a_childNum = a_oLB.getChild().size();
		
		if(a_childNum == 0) {
			if(isYoungestChild(a_oRC)) {
				a_sExtended.insert(0, "[");
				a_sCondensed.insert(0, "[");
				a_sShort.insert(0, "(");				
				if(this.a_uRCU.checkIntoRepeating(a_oRC)) {
					a_sExtended = appendEndBracket(a_uRCU.getEndRep(a_oRC), a_sExtended, EXTENDED, false);
					a_sCondensed = appendEndBracket(a_uRCU.getEndRep(a_oRC), a_sCondensed, CONDENSED, false);
					a_sShort = appendEndBracket(a_uRCU.getEndRep(a_oRC), a_sShort, SHORT, false);
				}
				if(this.a_uRCU.isinCyclic()) {
					a_sExtended = appendEndBracket(a_uRCU.getCyclicEnd(), a_sExtended, EXTENDED, true);
					a_sCondensed = appendEndBracket(a_uRCU.getCyclicEnd(), a_sCondensed, CONDENSED, true);
					a_sShort = appendEndBracket(a_uRCU.getCyclicEnd(), a_sShort, SHORT, true);
				}
			}
		}
		
		if(a_childNum == 1) {
			a_sExtended.insert(0, "-");
			addNode(this.a_uRCU.getIndex(this.getSaccharideID(a_oLB)), a_bIsFragments);
		}
		
		if(a_childNum > 1) {
			LinkedList<String> lst_child = this.sortChildIndex(a_oLB.getChild());
			for(int i = 0; i < lst_child.size(); i++) {
				String childID = lst_child.get(i);
				if(childID.contains("end") || isCyclicResidue(this.a_uRCU.getIndex(childID))) continue;

				if(i == lst_child.size() - 1) {
					a_sExtended.insert(0, "]-");
					a_sCondensed.insert(0, "]");
					a_sShort.insert(0, ")");	
				}
				addNode(this.a_uRCU.getIndex(childID), a_bIsFragments);
			}
		}
		
		if(!a_bIsFragments) {
			this.a_sExtendedIUPAC.append(a_sExtended);
			this.a_sCondensedIUPAC.append(a_sCondensed);
			this.a_sShortIUPAC.append(a_sShort);
		}else {		
			this.a_sExtendedFragments.append(a_sExtended);
			this.a_sCondensedFragments.append(a_sCondensed);
			this.a_sShortFragments.append(a_sShort);
		}
		
		return;
	}
	
	private String makeLinkageShort(ResidueContainer a_oRC) 
			throws ResidueContainerException, ConverterIUPACException {
		//if(a_objRC.getAnomerSymbol() == ' ' || a_objRC.getAnomerPosition() == 0) return "";

		StringBuilder a_sShortLinkage = new StringBuilder();
		
		if(!this.isFacingAnomerForChild(a_oRC)) {
			a_sShortLinkage.append("(");

			if(a_oRC.getAnomerSymbol() != ' ' && a_oRC.getAnomerSymbol() != 'x' && a_oRC.getAnomerSymbol() != 'o') 
				a_sShortLinkage.append(a_oRC.getAnomerSymbol());
			if(a_oRC.getAnomerSymbol() == 'x') a_sShortLinkage.append("?");

				
			if(a_oRC.getAnomerPosition() > 0) a_sShortLinkage.append(a_oRC.getAnomerPosition());
			else a_sShortLinkage.append("?");
			
			if(a_sShortLinkage.length() > 0) a_sShortLinkage.append("-");

			/** append cross-linked substituent */
			if(a_oRC.getLinkage().getBridgeMAP() != null)
				a_sShortLinkage.append(a_oRC.getLinkage().getBridgeMAP().getIUPACnotation() + "-");
		}else {
			a_sShortLinkage.append("(" + a_oRC.getAnomerSymbol());
		}
		
		if(a_oRC.getLinkage().getDonors().size() != 0) {
			if(a_oRC.getLinkage().getAcceptors().getFirst() == -1)
				a_sShortLinkage.append("?");
			else
				a_sShortLinkage.append(this.makeAcceptorPosition(a_oRC.getLinkage().getAcceptors()));
			a_sShortLinkage.append(")");
		}		

		return a_sShortLinkage.toString();
	}

	private String makeLinkageCondensed(ResidueContainer a_oRC) 
			throws ResidueContainerException, ConverterIUPACException {
		//if((a_oRC.getAnomerSymbol() == ' ' || a_oRC.getAnomerPosition() == 0) && a_oRC.getLinkage().getBridgeMAP() == null) return "";

		StringBuilder a_sCondensedLinkage = new StringBuilder();
		
		if(a_oRC.getAnomerSymbol() != ' ' && a_oRC.getAnomerSymbol() != 'x' && a_oRC.getAnomerSymbol() != 'o') 
			a_sCondensedLinkage.append(a_oRC.getAnomerSymbol());
		if(a_oRC.getAnomerSymbol() == 'x') a_sCondensedLinkage.append("?");
		
		/** append cross-linked substituent */
		if(a_oRC.getLinkage().getBridgeMAP() != null)
			a_sCondensedLinkage.append("-" + a_oRC.getLinkage().getBridgeMAP().getIUPACnotation() + "-");
			
		if(a_oRC.getLinkage().getDonors().size() != 0) {
			if(a_oRC.getLinkage().getAcceptors().getFirst() == -1)
				a_sCondensedLinkage.append("?");
			else
				a_sCondensedLinkage.append(this.makeAcceptorPosition(a_oRC.getLinkage().getAcceptors()));
		}else if(!this.isFacingAnomerForChild(a_oRC)) a_sCondensedLinkage.append("-");
		
		return a_sCondensedLinkage.toString();
	}
	
	private String makeLinkageExtended(ResidueContainer a_oRC) throws ResidueContainerException {
		StringBuilder a_sExtendedLinkage = new StringBuilder();
		LinkageBlock a_oLB = a_oRC.getLinkage();
		
		/** append linkage position */
		if(a_oLB.getAcceptors().size() > 0) {
			a_sExtendedLinkage.append("-(");
			
			/** append anomeric position */
			if(a_oRC.getAnomerPosition() > 0) 
				a_sExtendedLinkage.append(a_oRC.getAnomerPosition());
			else a_sExtendedLinkage.append("?");

			/** append cross-linked substituent */
			if(a_oLB.getBridgeMAP() != null)
				a_sExtendedLinkage.append("-" + a_oLB.getBridgeMAP().getIUPACnotation());
			
			/** append parent position */
			if(a_oLB.getAcceptors().size() == 1) {
				if(a_oLB.getAcceptors().getLast() == -1) a_sExtendedLinkage.append("->?");
				else {
					if(isFacingAnomerForParent(a_oRC)) a_sExtendedLinkage.append("<");
					a_sExtendedLinkage.append("->");
					a_sExtendedLinkage.append(a_oLB.getAcceptors().getLast());
				}
			}
			if(a_oLB.getAcceptors().size() > 1) {
				a_sExtendedLinkage.append("->");
				a_sExtendedLinkage.append(this.makeAcceptorPosition(a_oLB.getAcceptors()));
			}	
			
			a_sExtendedLinkage.append(")");
		}else if(!this.isFacingAnomerForChild(a_oRC)){
			a_sExtendedLinkage.append("(");
			a_sExtendedLinkage.append(a_oRC.getAnomerPosition() <= 0 ? "?" : a_oRC.getAnomerPosition());
			a_sExtendedLinkage.append("->");
		}
		
		return a_sExtendedLinkage.toString();
	}
	
	private boolean isYoungestChild(ResidueContainer a_oRC) 
		throws ResidueContainerException {
		boolean a_bIsNonRed = a_oRC.getRootStatusDescriptor().equals(RootStatusDescriptor.NON);
		if(!a_bIsNonRed) return false;
		
		String a_sParent = a_oRC.getLinkage().getParent().getLast();
		String a_sChild = a_oRC.getNodeIndex();
		ResidueContainer a_oCurrentRC = this.a_uRCU.getIndex(a_sParent);
		
		while(a_oCurrentRC.getRootStatusDescriptor().equals(RootStatusDescriptor.NON) && 
				this.a_uRCU.checkNumberOfChildren(a_oCurrentRC) == 1) {
			a_sParent = a_oCurrentRC.getLinkage().getParent().getLast();
			a_sChild = a_oCurrentRC.getNodeIndex();
			a_oCurrentRC = this.a_uRCU.getIndex(a_sParent);
			
			if(a_oCurrentRC.getLinkage().getParent().size() == 0) break;
		}
		
		LinkedList<String> lst_child = sortChildIndex(a_oCurrentRC.getLinkage().getChild());
		
		if(this.a_uRCU.checkNumberOfChildren(a_oCurrentRC) <= 1) return false;
		if(lst_child.indexOf(a_sChild) == lst_child.size() - 1) {
			return true;		
		}
		if(a_oCurrentRC.getRootStatusDescriptor().equals(RootStatusDescriptor.NON) && 
				lst_child.indexOf(a_sChild) == 0 && 
				this.a_uRCU.checkNumberOfChildren(this.a_uRCU.getIndex(a_sParent)) > 1) {
			return isYoungestChild(a_oCurrentRC);
		}
		
		return false;
	}
	
	private StringBuilder appendRepeatingBlock
	(ResidueContainer a_oRC, StringBuilder a_sIUPAC, String a_sStyle) 
			throws ResidueContainerException, ConverterIUPACException {
		if(a_oRC.getLinkage().getRepeatingBlock().isEmpty()) 
			return a_sIUPAC;
		
		HashMap<String, RepeatingBlock> a_mPositionToRB = a_oRC.getLinkage().getRepeatingBlock();
		
		if(a_mPositionToRB.containsKey("start")) {
			a_sIUPAC = makeStartRep(a_oRC, a_sIUPAC.toString(), a_sStyle, false);

			/**append repeating count*/
			RepeatingBlock a_oStartRB = a_mPositionToRB.get("start");
			
			if(!a_oStartRB.isCyclic()) {
				String a_sOpposit = a_mPositionToRB.get("start").getOppositeNode();
				a_sIUPAC.append(checkRepeatingNumber(this.a_uRCU.getIndex(a_sOpposit)));			
			}
		}
		if(a_mPositionToRB.containsKey("end") && !a_uRCU.checkIntoRepeating(a_oRC)) {
			a_sIUPAC = makeEndRep(a_mPositionToRB.get("end"), a_sIUPAC.toString(), a_sStyle);
		}
		
		return a_sIUPAC;
	}
	
	private StringBuilder appendEndBracket
	(ResidueContainer a_oRC, StringBuilder a_sIUPAC, String a_sStyle, boolean a_bIsCyclic) {
		if(a_oRC.getLinkage().getRepeatingBlock().isEmpty()) return a_sIUPAC;
		
		HashMap<String, RepeatingBlock> a_RB = a_oRC.getLinkage().getRepeatingBlock();
		
		if(a_RB.containsKey("end") && a_bIsCyclic == false) {
			return makeEndRep(a_RB.get("end"), a_sIUPAC.toString(), a_sStyle);
		}
		if(a_RB.containsKey("cyclic_end") && a_bIsCyclic == true) {
			return makeEndRep(a_RB.get("cyclic_end"), a_sIUPAC.toString(), a_sStyle);
		}
		
		return a_sIUPAC;
	}
	
	private StringBuilder 
	appendCyclicNotation(ResidueContainer a_oRC, StringBuilder a_sIUPAC, String a_sStyle) {
		if(a_oRC.getLinkage().getRepeatingBlock().isEmpty())
			return a_sIUPAC;

		RepeatingBlock a_objcRB = null;		
		
		if(a_oRC.getLinkage().getRepeatingBlock().containsKey("cyclic_start")) {
			a_sIUPAC = makeStartRep(a_oRC, a_sIUPAC.toString(), a_sStyle, true);
		}
		if(a_oRC.getLinkage().getRepeatingBlock().containsKey("cyclic_end") && a_oRC.getLinkage().getChild().size() == 0) {
			a_objcRB = a_oRC.getLinkage().getRepeatingBlock().get("cyclic_end"); 
			a_sIUPAC = makeEndRep(a_objcRB, a_sIUPAC.toString(), a_sStyle);
		}
		return a_sIUPAC;
	}
	
	private StringBuilder makeStartRep (ResidueContainer a_oRC, String a_sIUPAC, String a_sStyle, boolean a_bIsCyclic) {
		StringBuilder ret = new StringBuilder();
		char a_cSymbol = ' ';
		
		ret.append(a_sIUPAC);
		
		if(a_sStyle.equals(EXTENDED)) ret.append("-(");
		if(a_sStyle.equals(SHORT)) ret.append("(");
		if(!a_bIsCyclic && !a_sStyle.equals(EXTENDED)) {
			a_cSymbol = a_oRC.getAnomerSymbol() == ' ' || 
					a_oRC.getAnomerSymbol() == 'x' ||
					a_oRC.getAnomerSymbol() =='o' ? '?' : a_oRC.getAnomerSymbol();
			ret.append(a_cSymbol);
		}

		int a_iAcceptor = a_bIsCyclic ? a_oRC.getLinkage().getDonors().getFirst() : a_oRC.getAnomerPosition();
		ret.append(a_iAcceptor <= 0 ? "?" : a_iAcceptor);

		/** append cross-linked substituent (repeating) */
		if(a_oRC.getLinkage().getRepeatingBlock().containsKey("start")) {
			if(a_oRC.getLinkage().getRepeatingBlock().get("start").getBridge() != null) {
				ret.append("-" + a_oRC.getLinkage().getRepeatingBlock().get("start").getBridge().getIUPACnotation());
			}
		}
		
		if(!a_sStyle.equals(CONDENSED)) ret.append("-");
		if(a_sStyle.equals(EXTENDED)) ret.append(">");
		ret.append("]");
		
		return ret;
	}
	
	private StringBuilder makeEndRep (RepeatingBlock a_oRB, String a_sIUPAC, String a_sStyle) {
		StringBuilder ret = new StringBuilder();
		
		ret.append("[");
		ret.append(a_oRB.getParentAcceptor().getFirst() == -1 ? "?" : a_oRB.getParentAcceptor().getFirst());
		if(!a_sStyle.equals(CONDENSED)) ret.append(")");	
		if(a_sStyle.equals(EXTENDED)) ret.append("-");	
		ret.append(a_sIUPAC);
		
		return ret;
	}
	
	private String checkRepeatingNumber(ResidueContainer a_oEndRES) {
		String ret = "";
		RepeatingBlock a_objERB = a_oEndRES.getLinkage().getRepeatingBlock().get("end");
		int a_iMin = a_objERB.getMin();
		int a_iMax = a_objERB.getMax();
		
		if(a_iMin != -1 && a_iMax != -1) {
			if(a_iMin != a_iMax) {
				ret += a_iMin;
				ret += "-";
				ret += a_iMax;
			}else 
				ret += a_iMin;
		}
		
		if(a_iMin != -1 && a_iMax == -1) ret += a_iMin;
		if(a_iMin == -1 && a_iMax != -1) ret += a_iMax;
		
		if(ret.equals("")) ret +="n";
		
		return ret;
	}

	private boolean isFacingAnomerForParent(ResidueContainer a_oRC) throws ResidueContainerException {
		if(a_oRC.getLinkage().getParent().isEmpty()) return false;
			
		ResidueContainer a_oParent =  this.a_uRCU.getIndex(a_oRC.getLinkage().getParent().getFirst());
		if(a_oRC.getLinkage().getAcceptors().contains(a_oParent.getAnomerPosition()) && 
				a_oRC.getLinkage().getDonors().contains(a_oRC.getAnomerPosition())) {
			return true;
		}
			
		return false;
	}
	
	private boolean isFacingAnomerForChild(ResidueContainer a_oRC) throws ResidueContainerException {
		if(a_oRC.getLinkage().getChild().isEmpty()) return false;
		for(String a_sIndex : a_oRC.getLinkage().getChild()) {
			if(a_sIndex.contains("end")) continue;

			ResidueContainer a_oChild = this.a_uRCU.getIndex(a_sIndex);
			if(a_oChild.getLinkage().getAcceptors().contains(a_oRC.getAnomerPosition()) &&
					a_oChild.getLinkage().getDonors().contains(a_oChild.getAnomerPosition())) {
				return true;
			}	
		}
		
		return false;
	}

	private String getSaccharideID(LinkageBlock a_oLB) {
		String ret = "";
		
		for(String s : a_oLB.getChild()) {
			if(!s.contains("_end")) ret = s;
		}
		
		return ret;
	}
	
	private LinkedList<String> sortChildIndex (LinkedList<String> a_aBeforeChildren) throws ResidueContainerException {
		LinkedList<String> a_aAfterChildren = new LinkedList<String>();
		String a_sFucIndex = "";
		
		for(String a_sIndex : a_aBeforeChildren) {
			if(a_sIndex.contains("end")) {
				a_aAfterChildren.add(a_sIndex);
				continue;
			}
			if(this.a_uRCU.getIndex(a_sIndex).getSugarName().equals("Fuc")) {
				a_sFucIndex = a_sIndex;
				continue;
			}
			if(this.a_uRCU.getIndex(a_sIndex).getLinkage().getRepeatingBlock().containsKey("cyclic_start"))
				continue;
			a_aAfterChildren.add(a_sIndex);			
		}
		
		if(a_sFucIndex != "") a_aAfterChildren.addLast(a_sFucIndex);
		
		return a_aAfterChildren;
	}
	
	private boolean isCyclicResidue (ResidueContainer a_oRC) {
		return a_oRC.getLinkage().getRepeatingBlock().containsKey("cyclic_start");
	}
	
	private String replaceNeuraminicSubstituent(String a_sSugarName) {
		if(a_sSugarName.contains("Neu") && a_sSugarName.contains("5Ac")) 
			a_sSugarName = a_sSugarName.replace("5Ac", "Ac");
		if(a_sSugarName.contains("Neu") && a_sSugarName.contains("5Gc"))
			a_sSugarName = a_sSugarName.replace("5Gc", "Gc");
		
		return a_sSugarName;
	}
	
	private StringBuilder makeAcceptorPosition(LinkedList<Integer> a_aAcceptors) {
		StringBuilder a_sAcceptor = new StringBuilder();
		/** a_objLB.getChildAcceptor() */
		for(Iterator<Integer> i = a_aAcceptors.iterator(); i.hasNext(); ) {
			a_sAcceptor.append(i.next());
			if(i.hasNext()) a_sAcceptor.append("/");
		}
		
		return a_sAcceptor;
	}
	
	private void init() {
		this.a_sExtendedIUPAC = new StringBuilder();
		this.a_sCondensedIUPAC = new StringBuilder();
		this.a_sShortIUPAC = new StringBuilder();
		this.a_sCondensedFragments = new StringBuilder();
		this.a_sExtendedFragments = new StringBuilder();
		this.a_sShortFragments = new StringBuilder();
		this.a_uRCU = new ResidueContainerUtility();
	}
}
