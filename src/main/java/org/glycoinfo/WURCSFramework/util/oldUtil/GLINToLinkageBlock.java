package org.glycoinfo.WURCSFramework.util.oldUtil;

import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.util.WURCSDataConverter;
import org.glycoinfo.WURCSFramework.util.residuecontainer.LinkageBlock;
import org.glycoinfo.WURCSFramework.util.residuecontainer.RepeatingBlock;
import org.glycoinfo.WURCSFramework.wurcs.array.GLIP;
import org.glycoinfo.WURCSFramework.wurcs.array.GLIPs;
import org.glycoinfo.WURCSFramework.wurcs.array.LIN;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.GLIN;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.GRES;

public class GLINToLinkageBlock {
	
	/**
	 * Define a linkage information and repeating structure in this node and child position.
	 * Parent position is extract from acceptor,
	 * Child position is extract from donor.
	 * @param a_oGRES
	 * @return
	 * @throws ConverterExchangeException 
	 */
	public LinkageBlock extractGLIN(GRES a_oGRES, LinkedList<LIN> a_aLINs) throws ConverterExchangeException {
		LinkageBlock ret = new LinkageBlock();
		boolean isRoot = this.isRoot(a_oGRES);
		
		/** Acceptor-node-Donor */
		analyzeAcceptorGLIN(a_oGRES, ret);
		analyzeDonorGLIN(a_oGRES.getDonorGLINs(), ret, isRoot, a_aLINs);
		
		return ret;
	}
	
	/**
	 * Analyze GLIN in donor
	 * Donor is described as parent linkage for current residue
	 * <-current"<-" 
	 * @param a_aDGLINs
	 * @param a_oLB
	 * @param a_bIsRoot
	 * @throws ConverterExchangeException 
	 */
	private LinkageBlock analyzeDonorGLIN (LinkedList<GLIN> a_aDGLINs, LinkageBlock a_oLB, boolean a_bIsRoot, LinkedList<LIN> a_aLINs) throws ConverterExchangeException {
		/** cGLIN means the direction of child linkage*/	
		for(GLIN a_oDGLIN : a_aDGLINs) {
			a_oLB.setDonorID(a_oDGLIN.getID());
			
			if(!a_oDGLIN.isRepeat()) {
				/** set parent node ID */
				for(GRES parent : a_oDGLIN.getAcceptor()) {
					String parentIndex = WURCSDataConverter.convertRESIDToIndex(parent.getID());
					if(!a_oLB.getAntenna().contains(parentIndex)) a_oLB.addParent(parentIndex);
				}
				
				/** set cross-linked substituent */
				if(!a_oDGLIN.getMAP().equals(""))
					a_oLB.setBridgeMAP(a_oDGLIN.getMAP());

				a_oLB.addChildAcceptor(a_oDGLIN.getAcceptorPositions());
				a_oLB.addChildDonor(a_oDGLIN.getDonorPositions());

				if(a_bIsRoot && a_oDGLIN.getMAP().equals("")) {
					a_oLB.getParent().remove();
					a_oLB.addRepeatingBlock("cyclic_start", extractStartRep(a_oDGLIN, true));
				}
			}else 
				a_oLB.addRepeatingBlock("start", extractStartRep(a_oDGLIN, false));

			/** extract probability annotation */
			this.extractProbabilityAnnotation(a_oDGLIN, a_aLINs, a_oLB);
		}
		
		return a_oLB;
	}
	
	/**
	 * Analyze GLIN in acceptor
	 * Acceptor is described as child linkage for current residue
	 * "<-"current<-
	 * This linkage is extracted end repeating information, child residue index
	 * 
	 * @param a_oGRES
	 * @param a_oLB
	 * @throws ConverterExchangeException 
	 */
	private LinkageBlock analyzeAcceptorGLIN (GRES a_oGRES, LinkageBlock a_oLB) throws ConverterExchangeException {

		for(GLIN a_oAGLIN : a_oGRES.getAcceptorGLINs()) {
			a_oLB.setAcceptorID(a_oAGLIN.getID());
	
			/** if getAcceptor size is higher than 2, this GRES is a root of antenna*/
			if(a_oAGLIN.getAcceptor().size() > 1) {
				for(GRES g :  a_oAGLIN.getDonor()) 
					a_oLB.addAntennaRoot(WURCSDataConverter.convertRESIDToIndex(g.getID()));
			}
			
			/** If ambiguous substituent in the glycan*/
			if(!a_oAGLIN.getMAP().equals("") && a_oAGLIN.getDonorPositions().size() == 0)
				a_oLB.addAntennaeMAP(a_oAGLIN.getMAP());
			
			/** extract normal linkage*/
			if(!a_oAGLIN.isRepeat()) {
				/** define child node of current monosaccharide */
				for(GRES a_oChildGRES : a_oAGLIN.getDonor()) {
					String index = WURCSDataConverter.convertRESIDToIndex(a_oChildGRES.getID());
					if(checkBackChild(a_oGRES, a_oChildGRES.getID())) {
						/** for reverse straight structure*/
						if(a_oGRES.getID() - a_oChildGRES.getID() == 1) a_oLB.isReverse(true);

						/** for ambiguous structure */
						if(a_oAGLIN.getDonor().size() > 1) {
							a_oLB.addParent(index);
						}
					}
					
					if(!a_oLB.getAntenna().contains(index) && !this.isCyclicEnd(a_oLB, index)) {
						a_oLB.addChild(index);
					}
				}
			}
			
			/** extract repeating information */
			if(a_oAGLIN.isRepeat()) {
				a_oLB.addRepeatingBlock("end", extractEndRep(a_oAGLIN, false));
				
				if(!a_oAGLIN.getAcceptorPositions().isEmpty()) {
					if(!a_oGRES.getAcceptorGLINs().get(0).getAcceptorPositions().get(0).equals(a_oAGLIN.getAcceptorPositions().get(0))) {
						a_oLB.addChild(WURCSDataConverter.convertRESIDToIndex(a_oGRES.getID()) + "_end");
						a_oLB.getRepeatingBlock().get("end").isNonRedEnd(true);
					}else {
						a_oLB.getRepeatingBlock().get("end").isNonRedEnd(false);
					}				
				}
			}
		}

		return a_oLB;
	}
	
	private RepeatingBlock extractStartRep(GLIN a_oDGLIN, boolean a_bIsCyclic) throws ConverterExchangeException {
		RepeatingBlock a_oRB = new RepeatingBlock();
		
		a_oRB.setChildAcceptor(a_oDGLIN.getAcceptorPositions());
		a_oRB.setChildDonor(a_oDGLIN.getDonorPositions());
		a_oRB.setOppositdeNode(WURCSDataConverter.convertRESIDToIndex(a_oDGLIN.getAcceptor().getFirst().getID()));
		a_oRB.isCyclic(a_bIsCyclic);
		if(!a_oDGLIN.getMAP().equals("")) a_oRB.setBridge(a_oDGLIN.getMAP());

		return a_oRB;
	}
	
	private RepeatingBlock extractEndRep(GLIN a_oAGLIN, boolean a_bIsCyclic) throws ConverterExchangeException {
		RepeatingBlock a_objRB = new RepeatingBlock();
		
		a_objRB.addParentAcceptor(a_oAGLIN.getAcceptorPositions());
		a_objRB.addParentDonor(a_oAGLIN.getDonorPositions());
		a_objRB.setOppositdeNode(WURCSDataConverter.convertRESIDToIndex(a_oAGLIN.getDonor().getFirst().getID()));
		a_objRB.isCyclic(a_bIsCyclic);
		if(!a_oAGLIN.getMAP().equals("")) a_objRB.setBridge(a_oAGLIN.getMAP());

		a_objRB.setMax(a_oAGLIN.getRepeatCountMax());
		a_objRB.setMin(a_oAGLIN.getRepeatCountMin());
		
		return a_objRB;
	}
	
	private boolean checkBackChild(GRES a_oGRES, int a_iChildID) {
		if(a_oGRES.getID() - a_iChildID > 0) return true;  			
		return false;
	}
	
	private boolean isCyclicEnd(LinkageBlock a_oLB, String a_sChild) {
		if(!a_oLB.getRepeatingBlock().containsKey("cyclic_end")) return false;
		if(a_oLB.getRepeatingBlock().get("cyclic_end").getOppositeNode().equals(a_sChild)) return true;
		return false;
	}
	
	private String convertBridgeNotation (String a_strBMAP) throws ConverterExchangeException {
		if(a_strBMAP.equals("")) return "";
		
		SubstituentTemplate enum_sub = SubstituentTemplate.forMAP(a_strBMAP);
		String ret = enum_sub.getIUPACnotation();
		
		return ret;
	}
	
	private boolean isRoot (GRES a_oGRES) {		
		/** Acceptor-node-Donor */
		LinkedList<GLIN> lst_Donor = a_oGRES.getDonorGLINs();
		LinkedList<GLIN> lst_Acceptor = a_oGRES.getAcceptorGLINs();

		if(lst_Donor.isEmpty() && a_oGRES.getAcceptorGLINs().isEmpty())
			return true;
		if(lst_Donor.isEmpty())
			return true;

		if(lst_Acceptor.isEmpty()) return false;
		
	//	System.out.println(a_oGRES.getID() + " " + a_oGRES.getDonorGLINs().get(0).getAcceptor().size() + " " + a_oGRES.getDonorGLINs().get(0).getDonor().size());
	//	System.out.println(a_oGRES.getID() + " " + a_oGRES.getAcceptorGLINs().get(0).getAcceptor().size() + " " + a_oGRES.getAcceptorGLINs().get(0).getDonor().size());
		
		if(a_oGRES.getDonorGLINs().size() == 1 && a_oGRES.getDonorGLINs().get(0).isRepeat())
			return true;
		
		for(GLIN a_Donor : lst_Donor) {
			if(a_Donor.isRepeat()) continue;
			GLIN a_oAcceptor = lst_Acceptor.get(0);
			
			if(a_oAcceptor.getDonor().isEmpty()) return false;
			
			if(a_oAcceptor.getAcceptor().get(0).getID() == 1 && a_Donor.getDonor().get(0).getID() == 1) return true;
			
			if(a_oAcceptor.getDonor().get(0).getID() - a_oGRES.getID() > 1) {
				if(a_oAcceptor.getAcceptor().size() > 1) return false;
				return true;
			}
			if(a_Donor.getAcceptor().get(0).getID() - a_oGRES.getID() > 1) {
				return true;
			}
		}
		
		return false;
	}
	
	private void extractProbabilityAnnotation (GLIN a_oDGLIN, LinkedList<LIN> a_aLINs, LinkageBlock a_oLB) {
		if(a_oDGLIN.getAcceptor().size() > 1) return;

		for(LIN a_oWLIN : a_aLINs) {
			for(GLIPs a_aGLIPs : a_oWLIN.getListOfGLIPs()) {
				for(GLIP a_oGLIP : a_aGLIPs.getGLIPs()) {
					if(a_oGLIP.getModificationProbabilityLower() == 1.0 && a_oGLIP.getModificationProbabilityUpper() == 1.0) continue;
					if((WURCSDataConverter.convertRESIDToIndex(a_oDGLIN.getAcceptor().getFirst().getID()).equals(a_oGLIP.getRESIndex())) && 
							(a_oDGLIN.getAcceptorPositions().contains(a_oGLIP.getBackbonePosition()))) {
						a_oLB.setProbabilityLow(a_oGLIP.getModificationProbabilityLower());
						a_oLB.setProbabilityHigh(a_oGLIP.getModificationProbabilityUpper());
					}
				}
			}
		}
			
		return;
	}
}
