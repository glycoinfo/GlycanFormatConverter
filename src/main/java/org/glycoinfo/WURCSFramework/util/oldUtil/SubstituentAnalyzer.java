package org.glycoinfo.WURCSFramework.util.oldUtil;

import java.util.ArrayList;
import java.util.Iterator;

import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainer;
import org.glycoinfo.WURCSFramework.wurcs.array.LIP;
import org.glycoinfo.WURCSFramework.wurcs.array.MOD;
import org.glycoinfo.WURCSFramework.wurcs.array.MS;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.BRIDGE;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.GRES;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.MSPERI;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.SUBST;

public class SubstituentAnalyzer {

	private ArrayList<String> a_aSubs;
	private ResidueContainer a_oRC;

	/**
	 * * Extract modification from current residue
	 * These modification are defined as "modification" in JsonObject
	 * @param a_oRC
	 * @param a_oGRES
	 * @param a_oMS
	 * @return
	 * @throws ConverterExchangeException
	 */
	public ArrayList<String> getSubstituents(ResidueContainer a_oRC, GRES a_oGRES, MS a_oMS) 
			throws ConverterExchangeException {
		this.a_aSubs = new ArrayList<String>();
		this.a_oRC = a_oRC;
		
		/** analyze core modification */
		this.analyzeSUBST(a_oGRES.getMS(), a_oMS);
		
		/** extract BRIDGE substituents */
		this.analyzeBRIDGE(a_oGRES, a_oMS);

		return this.a_aSubs;
	}
	
	private void analyzeSUBST(MSPERI a_oMSPERI, MS a_oMS) {
		for(SUBST a_oSUBST : a_oMSPERI.getCoreStructure().getSubstituents()) {
			int a_iPosition = a_oSUBST.getPositions().getFirst();
			String a_sMAP = a_oSUBST.getMAP();
			if(this.a_oRC.getBackBoneSize() == 9 && a_sMAP.startsWith("*N")) {
				if(a_iPosition == 5 || (a_iPosition == 7 && this.a_oRC.getNativeSubstituent().contains("5*N"))) {
					this.a_oRC.addNativeSubstituent(a_iPosition + "*N");
					if(!a_sMAP.equals("*N")) {
						String a_sNativeSub = a_iPosition + a_sMAP.replaceFirst("N", "O");
						if(a_sNativeSub.contains("OCC") && a_sNativeSub.contains("/3=O"))
							this.a_oRC.addNativeSubstituent(a_sNativeSub);
						else 
							this.a_aSubs.add(a_sNativeSub);
					}
					continue;
				}
			}
			
			if(this.a_oRC.getBackBoneSize() == 6 && a_sMAP.startsWith("*N")) {
				if(a_iPosition == 2) {
					if(!this.a_oRC.isAcidicSugar()) {

						if(a_sMAP.equals("*NCC/3=O") || a_sMAP.equals("*N")) {
							this.a_oRC.addNativeSubstituent(a_iPosition + a_sMAP);
						} else {
							//if(!a_oRC.getModification().contains("6*m")) {
								this.a_oRC.addNativeSubstituent(a_iPosition + "*N");						
								this.a_aSubs.add(a_iPosition + a_sMAP.replaceFirst("N", "O"));
							//}else
							//	this.a_aSubs.add(a_iPosition + a_sMAP);
						}
					}else this.a_aSubs.add(a_iPosition + a_sMAP);
					continue;
				}
				
				if(a_iPosition == 4 && this.a_oRC.getNativeSubstituent().contains("2*N")) {
					//this.a_oRC.addNativeSubstituent(a_pos + "*N");
					this.a_aSubs.add(a_iPosition + "*N");
					if(!a_sMAP.equals("*N")) {
						this.a_aSubs.add(a_iPosition + a_sMAP.replaceFirst("N", "O"));
					}
					continue;
				}
			}
			
			String a_sPosition = String.valueOf(a_iPosition);
			if(a_iPosition == -1) a_sPosition = "?";

			this.a_aSubs.add(a_sPosition + a_sMAP);
		}
		
		/** extract preferable modification*/
		for(SUBST a_oSUBST : a_oMSPERI.getSubstituents()) {
			StringBuilder a_sMAP = new StringBuilder();
			/**append pos*/
			a_sMAP.append(checkSubstituentPosition(a_oMS, a_oSUBST));
			/**append MAP*/
			a_sMAP.append(a_oSUBST.getMAP());
			
			if(!this.a_aSubs.contains(a_sMAP.toString()) || a_sMAP.indexOf("?") == 0)
				this.a_aSubs.add(a_sMAP.toString());
		}
		
		return;
	}
	
	private void analyzeBRIDGE(GRES a_oGRES, MS a_oMS) throws ConverterExchangeException {
		/** extract substituent in MSCORE */
		for(BRIDGE a_oBRIDGE : a_oGRES.getMS().getCoreStructure().getDivalentSubstituents()) {
			if(a_oBRIDGE.getMAP().equals("")) {
				if(a_oBRIDGE.getStartPositions().contains(a_oGRES.getMS().getCoreStructure().getAnomericPosition())) continue;
				else this.a_oRC.addModification(this.makeBridgePosiiton(a_oMS, a_oBRIDGE) + "*o");
			}
			else throw new ConverterExchangeException("Divelent substituent could not converted");
			//this.a_aSubs.add(this.makeBridgePosiiton(a_oMS, a_oBRIDGE) + a_oBRIDGE.getMAP());
		}
		
		/** extract substituent in MSPERI */
		for(BRIDGE a_oBRIDGE : a_oGRES.getMS().getDivalentSubstituents()) {
			if(a_oBRIDGE.getMAP().equals("")) continue;
			else throw new ConverterExchangeException("Divelent substituent could not converted");
			//this.a_aSubs.add(this.makeBridgePosiiton(a_oMS, a_oBRIDGE) + a_oBRIDGE.getMAP());
		}
		
		return;
	}
	
	private String checkSubstituentPosition(MS a_oMS, SUBST a_oSUBST) {
		StringBuilder a_sPosition = new StringBuilder();
		
		for(Iterator<Integer> i = a_oSUBST.getPositions().iterator(); i.hasNext(); ) {
			int a_iPosition = i.next();
			a_sPosition.append(a_iPosition == -1 ? "?" : a_iPosition);
			a_sPosition.append(extractProbabilityPosition(a_oMS, a_oSUBST, a_iPosition));
			if(i.hasNext()) a_sPosition.append("/");
		}
		
		return a_sPosition.toString();
	}
	
	private String makeBridgePosiiton (MS a_oMS, BRIDGE a_oBRIDGE) {
		StringBuilder a_sbPosition = new StringBuilder();
		
		/** start position */
		a_sbPosition.append(a_oBRIDGE.getStartPositions().getFirst() + ",");
		
		/** end position */
		a_sbPosition.append(a_oBRIDGE.getEndPositions().getFirst());
		
		/** linkage type */
		/*for(MOD a_oMOD : a_oMS.getMODs()) {
			if(!a_oBRIDGE.getMAP().equals(a_oMOD.getMAPCode())) continue;
			for(LIPs a_oLIPs :  a_oMOD.getListOfLIPs()) {
				for(LIP a_oLIP : a_oLIPs.getLIPs()) {
					if(a_oLIP.getBackboneDirection() == ' ') continue;
				}
			}
		}*/
		
		return a_sbPosition.toString();
	}
	
	private String extractProbabilityPosition(MS a_oMS, SUBST a_oSUBST, int a_iPosition) {
		StringBuilder a_sProbability = new StringBuilder();
		//StringBuilder str_high = new StringBuilder();
		
		for(MOD a_oMOD : a_oMS.getMODs()) {
			if(a_oMOD.getMAPCode().equals("") || !a_oMOD.getMAPCode().equals(a_oSUBST.getMAP())) 
				continue;
			
			for(LIP a_oLIP : a_oMOD.getListOfLIPs().getFirst().getLIPs()) {
				if(a_oLIP.getBackbonePosition() != a_iPosition) continue;
				if(a_oLIP.getModificationProbabilityLower() == 1.0 && 
					a_oLIP.getModificationProbabilityUpper() == 1.0) continue;
				
				if(a_oLIP.getModificationProbabilityLower() == -1) a_sProbability.append("(%)");
				else {
					a_sProbability.append("(");
					a_sProbability.append((int)(a_oLIP.getModificationProbabilityLower()*100));
					a_sProbability.append("%)");
				}
				/*if(lip.getModificationProbabilityUpper() == -1) str_high.append("(?%)");
				else {
					str_high.append("(");
					str_high.append(String.valueOf(lip.getModificationProbabilityUpper()*100).replace(".0", "%)"));
				}*/
			}
			if(a_sProbability.length() > 0) break;
		}
		
		return a_sProbability.toString();
	}
}
