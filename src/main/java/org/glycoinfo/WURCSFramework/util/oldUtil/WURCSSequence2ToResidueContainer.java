package org.glycoinfo.WURCSFramework.util.oldUtil;

import java.util.Collections;
import java.util.LinkedList;

import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.WURCSFramework.util.WURCSDataConverter;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.oldUtil.Carbbank.CarbBankNameConverter;
import org.glycoinfo.WURCSFramework.util.oldUtil.Carbbank.ConverterCarbBankException;
import org.glycoinfo.WURCSFramework.util.residuecontainer.LinkageBlock;
import org.glycoinfo.WURCSFramework.util.residuecontainer.RepeatingBlock;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainer;
import org.glycoinfo.WURCSFramework.util.residuecontainer.RootStatusDescriptor;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.BRIDGE;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.GRES;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.SUBST;

/**
 * Class for output JSON style string from WURCS 
 * @author ShinchiroTsuchiya
 */
public class WURCSSequence2ToResidueContainer {
		
	private CarbBankNameConverter a_objMSNG = new CarbBankNameConverter();
	private GLINToLinkageBlock a_utilLBU= new GLINToLinkageBlock();
	private GRESToResidueData a_objGRD = new GRESToResidueData();
	
	/**
	 * 
	 * @param a_strWURCS
	 * @return
	 * @throws WURCSFormatException 
	 * @throws ConverterCarbBankException 
	 * @throws ConverterExchangeException 
	 * @throws Exception
	 */
	public LinkedList<ResidueContainer> start(String a_strWURCS)
			throws WURCSException, ConverterExchangeException, ConverterCarbBankException, TrivialNameException {
		WURCSFactory a_oWF = new WURCSFactory(a_strWURCS);		
		
		LinkedList<ResidueContainer> a_aRCs = readWURCS(a_oWF);
		
		return a_aRCs;
	}
	
	/**
	 * 
	 * @param a_oWS2
	 * @return
	 * @throws ConverterExchangeException 
	 * @throws WURCSFormatException 
	 * @throws ConverterCarbBankException 
	 */
	private LinkedList<ResidueContainer> readWURCS(WURCSFactory a_oWF)
			throws WURCSFormatException, ConverterExchangeException, ConverterCarbBankException, TrivialNameException {
		LinkedList<ResidueContainer> ret = new LinkedList<ResidueContainer>();
		
		for(GRES a_oGRES : a_oWF.getSequence().getGRESs()) {
			int residueNum = a_oGRES.getID();
			
			/** extract IUPAC sugar name */
			this.a_objGRD.start(a_oGRES);
			ResidueContainer a_oRC = this.a_objGRD.getResidueContainer();
				
			/***/
			a_oRC.setMS(a_oGRES.getMS().getString());
			a_oRC.setNodeID(residueNum);
			a_oRC.setNodeIndex(WURCSDataConverter.convertRESIDToIndex(residueNum));
			
			a_oRC.addLinkage(this.a_utilLBU.extractGLIN(a_oGRES, a_oWF.getArray().getLINs()));
			
			a_oRC.setIUPACExtednedNotation(this.a_objMSNG.makeIUPACExtendedNotation(a_oRC));
			a_oRC.setIUPACCondensedNotation(this.a_objMSNG.createIUPACondensedNotation(a_oRC));
			a_oRC.setSugarName(this.a_objMSNG.makeCommonName(a_oRC));
			
			checkTrueParentChildRelationship(ret, a_oRC);
		
			a_oRC.setRootStatus(analyzeTypeOfRoot(a_oRC));
		
			if(!checkModifications(a_oGRES, a_oRC)) 
				throw new ConverterExchangeException("Substituent could not completely converted");
			
			ret.addLast(a_oRC);
		}
		
		sortResidueContainer(getRoot(ret), ret);
		makeEndCyclicPoint(ret, ret.getFirst());
		
		return ret;
	}
		
	private RootStatusDescriptor analyzeTypeOfRoot(ResidueContainer a_oRC) {
		LinkageBlock a_oLB = a_oRC.getLinkage();
		char a_cAnomSymbol = a_oRC.getAnomerSymbol();
		int a_iAnomPosition = a_oRC.getAnomerPosition();
		String a_sSC = a_oRC.getMS();
		boolean a_bIsRepStart = a_oLB.getRepeatingBlock().containsKey("start");
		boolean a_bIsCyclicStart = a_oLB.getRepeatingBlock().containsKey("cyclic_start");
		
		/** cyclic end can not define root status*/
		if(a_oLB.getRepeatingBlock().containsKey("cyclic_end"))
			return RootStatusDescriptor.NON;
		/** fragment root is need not to define a root status*/
		if(a_oRC.getRootStatusDescriptor() != null &&
				a_oRC.getRootStatusDescriptor().equals(RootStatusDescriptor.FRAGMENT))
			return a_oRC.getRootStatusDescriptor();
		
		/** composition residue */
		if(a_sSC.contains("x") && a_oLB.getAcceptorID() == -1 && a_oLB.getDonorID() == -1)
			return RootStatusDescriptor.COMPOSITION;
		
		if(a_oLB.getDonorID() == -1) {
			/** Aldehyde */
			if(a_sSC.indexOf("o") == a_iAnomPosition - 1 && a_cAnomSymbol == 'o')
				return RootStatusDescriptor.KETOTYPE;
			if(a_sSC.indexOf("O") == a_iAnomPosition - 1 && a_cAnomSymbol == 'o')
				return RootStatusDescriptor.KETOTYPE;
			/** Alditol */
			if(a_sSC.indexOf("h") == a_iAnomPosition - 1 && a_cAnomSymbol == 'o')
				return RootStatusDescriptor.OTYPE;
			/** N-type*/
			if(a_oRC.getSubstituent().contains(a_iAnomPosition + "*N"))
				return RootStatusDescriptor.NTYPE;	
		
			return RootStatusDescriptor.REDEND;
		}
		
		if(a_oLB.getParent().size() == 0 && a_oLB.getBridgeMAP() == null && 
				a_bIsCyclicStart == false) return RootStatusDescriptor.REDEND;
			
			/** define a type of reducing end in repeating structure */
			if(a_bIsRepStart == true && a_bIsCyclicStart == false &&
				a_oLB.getParent().size() == 0) return RootStatusDescriptor.REDEND;
		
		/** cyclic start */
		if(a_oLB.getRepeatingBlock().containsKey("cyclic_start"))
			return RootStatusDescriptor.CYCLICSTART;
		
		return RootStatusDescriptor.NON;
	}
	
	/**
	 * Define root status for first residue.
	 * @param a_aRCs
	 * @param a_oRC
	 * @return
	 */
	private ResidueContainer checkTrueParentChildRelationship 
	(LinkedList<ResidueContainer> a_aRCs, ResidueContainer a_oRC) {
		LinkedList<String> a_aParents = a_oRC.getLinkage().getParent();
		if(a_aParents.size() < 2) return a_oRC;
		
		for(String a_sParent : a_aParents) {
			int a_iID = WURCSDataConverter.convertRESIndexToID(a_sParent);
			if(a_iID > a_oRC.getNodeID()) continue;
			if(a_aRCs.get(a_iID - 1).getLinkage().getChild().contains(a_oRC.getNodeIndex())) 
				continue;
			
			if(a_aRCs.get(a_iID - 1).getLinkage().getParent().contains(a_oRC.getNodeIndex())) {
				LinkageBlock a_oLB = a_aRCs.get(a_iID - 1).getLinkage();
				a_oLB.getParent().remove(a_oLB.getParent().indexOf(a_oRC.getNodeIndex()));
				a_oLB.addAntennaRoot(a_oRC.getNodeIndex());
				if(a_aRCs.get(a_iID - 1).getRootStatusDescriptor().equals(RootStatusDescriptor.FRAGMENT))
					a_aRCs.get(a_iID - 1).setRootStatus(RootStatusDescriptor.NON);
			}
		}
		
		if(a_oRC.getRootStatusDescriptor() == null) {
			a_oRC.setRootStatus(RootStatusDescriptor.FRAGMENT);
		}
		
		return a_oRC;
	}
	
	private LinkedList<ResidueContainer> sortResidueContainer(ResidueContainer a_oRC, 
			LinkedList<ResidueContainer> a_aRCs) {
		if(a_oRC == null) return a_aRCs;
		
		if(a_aRCs.indexOf(a_oRC) == 0) return a_aRCs;
		if(a_aRCs.indexOf(a_oRC) > 0 && a_aRCs.indexOf(a_oRC) + 1 < a_aRCs.size()) {
			a_aRCs.remove(a_oRC);
			a_aRCs.addFirst(a_oRC);
		}

		if(a_aRCs.indexOf(a_oRC) + 1 == a_aRCs.size()) {
			Collections.reverse(a_aRCs);
		}
		
		return a_aRCs;
	}
	
	private ResidueContainer getRoot(LinkedList<ResidueContainer> a_aRCs) {
		ResidueContainer a_oRoot = null;
		
		for(ResidueContainer a_oRC : a_aRCs) {
			if(!a_oRC.getRootStatusDescriptor().equals(RootStatusDescriptor.FRAGMENT) &&
				!a_oRC.getRootStatusDescriptor().equals(RootStatusDescriptor.NON)) {
				a_oRoot = a_oRC;
				break;
			}
		}
		return a_oRoot;
	}
	
	private void makeEndCyclicPoint (LinkedList<ResidueContainer> lst_RC, ResidueContainer a_objCyclicStart) {
		LinkageBlock a_objLB = a_objCyclicStart.getLinkage();		
		if(!a_objLB.getRepeatingBlock().containsKey("cyclic_start")) return;
		
		RepeatingBlock a_objRB = new RepeatingBlock();
		a_objRB.addParentAcceptor(a_objLB.getAcceptors());
		a_objRB.addParentDonor(a_objLB.getDonors());
		a_objRB.setOppositdeNode(a_objCyclicStart.getNodeIndex());
		a_objRB.isCyclic(true);

		String a_id = a_objLB.getRepeatingBlock().get("cyclic_start").getOppositeNode();
		ResidueContainer a_oCyclicEnd = lst_RC.get(WURCSDataConverter.convertRESIndexToID(a_id) - 1);
		LinkageBlock a_cyclicLB = a_oCyclicEnd.getLinkage();
		if(a_cyclicLB.getChild().size() == 1)
			a_cyclicLB.getChild().remove(a_objCyclicStart.getNodeIndex());
		a_cyclicLB.addRepeatingBlock("cyclic_end", a_objRB);

		return;
	}	
	
	private boolean checkModifications (GRES a_oGRES, ResidueContainer a_oRC) {
		int a_iModCount = 0;
		for(SUBST a_oSUBST : a_oGRES.getMS().getSubstituents()) {
			if(a_oSUBST.getMAP().equals("")) continue;
			a_iModCount++;
		}
		for(SUBST a_oSUBST : a_oGRES.getMS().getCoreStructure().getSubstituents()) {
			if(a_oSUBST.getMAP().equals("")) continue;
			a_iModCount++;
		}
		for(BRIDGE a_oBRIDGE : a_oGRES.getMS().getDivalentSubstituents()) {
			if(a_oBRIDGE.getMAP().equals("")) continue;
			a_iModCount++;
		}
		for(BRIDGE a_oBRIDGE : a_oGRES.getMS().getCoreStructure().getDivalentSubstituents()) {
			if(a_oBRIDGE.getMAP().equals("")) continue;
			a_iModCount++;
		}

		TrivialNameDescriptor a_enumTiv = TrivialNameDescriptor.forTrivialName(a_oRC.getSugarName());
		int a_iCCount = 0;
		if(a_enumTiv != null) a_iCCount = a_enumTiv.getSubstituent().size();
		else a_iCCount = a_oRC.getNativeSubstituent().size();
		
		if(a_iModCount > (a_oRC.getSubstituent().size() + a_iCCount)) return false;

		return true;
	}
}