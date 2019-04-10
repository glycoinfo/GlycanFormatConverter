package org.glycoinfo.WURCSFramework.util.oldUtil;

import java.util.ArrayList;
import java.util.HashMap;

import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainer;

public class TrivialNameConverter {
	
	public static String convertTrivalName(ResidueContainer a_oRC, String a_sSugarName) {
		String tmp = "";	

		if(a_oRC.getDLconfiguration().size() == 1 && 
				(a_sSugarName.startsWith("D-") || a_sSugarName.startsWith("L-") || a_sSugarName.startsWith("?-")))
			a_sSugarName = a_sSugarName.substring(2);
		
		TrivialNameDescriptor a_enumTrivialDescriptor = TrivialNameDescriptor.forIUPAC(a_sSugarName);
		
		/** retry append configuration at a_strSugarName */
		if(a_enumTrivialDescriptor == null) {
			a_enumTrivialDescriptor = TrivialNameDescriptor.forIUPAC(a_oRC.getDLconfiguration().getFirst() + "-" + a_sSugarName);
		}
		if(a_enumTrivialDescriptor == null) {
			for(TrivialNameDescriptor a_enumTrivial : TrivialNameDescriptor.values()) {
				if(a_enumTrivial.getIUPAC().equals("")) continue;
				if(a_sSugarName.contains(a_enumTrivial.getIUPAC())) a_enumTrivialDescriptor = a_enumTrivial;
			}		
		}
		
		if(a_enumTrivialDescriptor == null) {
			tmp = convertTrivialNamefromSub(a_oRC, a_sSugarName);
			margeNativeSubstituent(a_oRC);
			return tmp;
		}

		/** check true Leg */
		if(isLegtose(a_enumTrivialDescriptor) && 
				(!a_oRC.getNativeSubstituent().contains("5*N") && !a_oRC.getNativeSubstituent().contains("7*N"))) {
			margeNativeSubstituent(a_oRC);
			return a_sSugarName;
		}
			
		/** convert ulosonic (ulosonic acid) sugar */
		if(a_oRC.getModification().contains("2*ulo")) {
			tmp = convertDeoxyUlosonic(a_oRC, a_enumTrivialDescriptor);
		}	

		/** convert deoxy monosaccharide*/
		if(!a_oRC.getModification().contains("2*ulo") && a_oRC.getBackBoneSize() == 6) {
			tmp = convertDeoxyHexose(a_oRC, a_enumTrivialDescriptor);
		}

		a_oRC.getCommonName().clear();
		if(tmp.equals("")) {
			a_oRC.addCommonName(a_sSugarName);
			tmp = a_sSugarName;
		} else a_oRC.addCommonName(tmp);
			
		margeSubstituent(a_oRC);
		
		return tmp;
	}
	
	private static String convertTrivialNamefromSub(ResidueContainer a_oRC, String a_sSugarName) {
		String a_sTrivialName = "";
		
		/** convert apiose : composition of ery and have 3*OC*/
		if(a_oRC.getCommonName().contains("Ery") && a_oRC.getSubstituent().contains("3*CO")) {
			a_sTrivialName = "Api";
			a_oRC.getSubstituent().remove("3*CO");
		}

		/** convert GlcNac3*CarboxyEthyl to Mur*/
		if(a_oRC.getCommonName().contains("Glc")) {	
			a_sTrivialName = convertMUR(a_oRC, a_sSugarName);
		}
		
		if(a_sTrivialName.equals("")) {
			a_sTrivialName = a_sSugarName;
		} else {
			a_oRC.getCommonName().clear();
			a_oRC.addCommonName(a_sTrivialName);
		}
		
		return a_sTrivialName;
	}
	
	private static String convertDeoxyUlosonic (ResidueContainer a_oRC, TrivialNameDescriptor a_enumTrivialDescriptor) {
		if(a_enumTrivialDescriptor == null) return "";
		
		/**Neu have 5*N( + CCO/3=O), Kdn do not have any aminoic substituent*/
		if(isKetoDeoxyNonase(a_enumTrivialDescriptor) && a_oRC.getNativeSubstituent().contains("5*N")) {
			if(a_oRC.getDLconfiguration().getLast().equals("D")) a_enumTrivialDescriptor = TrivialNameDescriptor.DNEU;
			if(a_oRC.getDLconfiguration().getLast().equals("L")) a_enumTrivialDescriptor = TrivialNameDescriptor.LNEU;
			if(a_oRC.getDLconfiguration().getLast().equals("?")) a_enumTrivialDescriptor = TrivialNameDescriptor.NEU;
		}
		
		if(a_oRC.getBackBoneSize() == 9) {
			String a_sCoreConfiguration = 
					(a_oRC.getDLconfiguration().isEmpty()) ? "?" : a_oRC.getDLconfiguration().getLast();
			a_oRC.getDLconfiguration().clear();
			a_oRC.addDLconfiguration(a_sCoreConfiguration);
		}
		
		/** remove native substituent */
		for(String a_sMAP : a_enumTrivialDescriptor.getSubstituent())
			a_oRC.getNativeSubstituent().remove(a_sMAP);
		
		/** remove deoxy */
		for(String a_sDeoxy : a_enumTrivialDescriptor.getModifications()) {
			a_oRC.getModification().remove(a_sDeoxy);
		}
		
		a_oRC.setONIC(false);
		a_oRC.setMotif(false);

		return a_enumTrivialDescriptor.getTrivalName();
	}
	
	private static String convertDeoxyHexose (ResidueContainer a_oRC, TrivialNameDescriptor a_enumTrivialDescriptor) {
		if(a_enumTrivialDescriptor == null) return "";
		
		/** convert Quinovose to BAC */
		if(isQuinovose(a_enumTrivialDescriptor) &&
			a_oRC.getNativeSubstituent().contains("2*N") && 
			a_oRC.getSubstituent().contains("4*N")) {
			a_enumTrivialDescriptor = TrivialNameDescriptor.BAC;
		}

		/** remove deoxy */
		for(String a_sDeoxy : a_enumTrivialDescriptor.getModifications()) {
			if(a_oRC.getModification().contains("6*m") && a_sDeoxy.equals("6*d")) 
				a_oRC.getModification().remove("6*m");
			else a_oRC.getModification().remove(a_sDeoxy);
		}

		/** remove native substituent */
		for(String a_sMAP : a_enumTrivialDescriptor.getSubstituent()) {
			a_oRC.getNativeSubstituent().remove(a_sMAP);
			a_oRC.getSubstituent().remove(a_sMAP);	
		}
		
		/***/
		if(isQuinovose(a_enumTrivialDescriptor) || isFucose(a_enumTrivialDescriptor) || isRhamnose(a_enumTrivialDescriptor)) {
			if(a_oRC.getNativeSubstituent().contains("2*N")) {
				a_oRC.getNativeSubstituent().remove("2*N");
				a_oRC.getSubstituent().add("2*N");
			}
		}
		
		a_oRC.setMotif(false);
		
		
		return a_enumTrivialDescriptor.getTrivalName();
	}
	
	private static String convertMUR (ResidueContainer a_oRC, String a_sSugarName) {
		String tmp = "";
		
		if(a_oRC.getSubModList().contains("3*OC^RCO/4=O/3C")) {
			if(!a_oRC.getNativeSubstituent().contains("2*NCC/3=O") && !a_oRC.getNativeSubstituent().contains("2*NCCO/3=O"))	{
				a_oRC.getSubstituent().remove("3*OC^RCO/4=O/3C");
				checkPrefixOxygen(a_oRC, "2*N");
				tmp = "Mur";
			}
		}

		if(a_oRC.getSubModList().contains("3*OC^SCO/4=O/3C")) {
			if(!a_oRC.getNativeSubstituent().contains("2*NCC/3=O") && !a_oRC.getNativeSubstituent().contains("2*NCCO/3=O")) {
				a_oRC.getSubstituent().remove("3*OC^SCO/4=O/3C");
				checkPrefixOxygen(a_oRC, "2*N");
				tmp = "Mur";
			}
		}

		if(a_oRC.getSubModList().contains("3*OC^XCO/4=O/3C")) {
			if(!a_oRC.getNativeSubstituent().contains("2*NCC/3=O") && !a_oRC.getNativeSubstituent().contains("2*NCCO/3=O")) {
				a_oRC.getSubstituent().remove("3*OC^XCO/4=O/3C");
				checkPrefixOxygen(a_oRC, "2*N");
				tmp = "Mur";
			}
		}
		
		if(a_oRC.getSubModList().contains("3*OCC^SC/4O/3=O")) {
			System.out.println(a_oRC.getSubstituent());
			if(a_oRC.getNativeSubstituent().contains("2*NCC/3=O")) {
				tmp = "MurNAc";
				a_oRC.getNativeSubstituent().remove("2*NCC/3=O");
				a_oRC.getSubstituent().remove("3*OCC^SC/4O/3=O");
			}
			if(a_oRC.getNativeSubstituent().contains("2*NCCO/3=O")) {
				tmp = "MurNGc";
				a_oRC.getNativeSubstituent().remove("2*NCCO/3=O");
				a_oRC.getSubstituent().remove("3*OCC^SC/4O/3=O");
			}
		}
		
		if(a_oRC.getSubModList().contains("3*OC^SCO/4=O/3C")) {

			if(a_oRC.getNativeSubstituent().contains("2*NCC/3=O")) {
				tmp = "MurNAc";
				a_oRC.getNativeSubstituent().remove("2*NCC/3=O");
				a_oRC.getSubstituent().remove("3*OC^SCO/4=O/3C");
			}
			if(a_oRC.getNativeSubstituent().contains("2*NCCO/3=O")) {
				tmp = "MurNGc";
				a_oRC.getNativeSubstituent().remove("2*NCCO/3=O");
				a_oRC.getSubstituent().remove("3*OC^SCO/4=O/3C");
			}
		
		}
		
		if(a_oRC.getSubModList().contains("3*OCC^RC/4O/3=O")) {
			if(a_oRC.getNativeSubstituent().contains("2*NCC/3=O")) {
				tmp = "MurNAc";
				a_oRC.getNativeSubstituent().remove("2*NCC/3=O");
				a_oRC.getSubstituent().remove("3*OCC^RC/4O/3=O");
			}
			if(a_oRC.getNativeSubstituent().contains("2*NCCO/3=O")) {
				tmp = "MurNGc";
				a_oRC.getNativeSubstituent().remove("2*NCCO/3=O");
				a_oRC.getSubstituent().remove("3*OCC^RC/4O/3=O");
			}
		}
		
		if(a_oRC.getSubModList().contains("3*OC^RCO/4=O/3C")) {
			if(a_oRC.getNativeSubstituent().contains("2*NCC/3=O")) {
				tmp = "MurNAc";
				a_oRC.getNativeSubstituent().remove("2*NCC/3=O");
				a_oRC.getSubstituent().remove("3*OC^RCO/4=O/3C");
			}
			if(a_oRC.getNativeSubstituent().contains("2*NCCO/3=O")) {
				tmp = "MurNGc";
				a_oRC.getNativeSubstituent().remove("2*NCCO/3=O");
				a_oRC.getSubstituent().remove("3*OC^RCO/4=O/3C");
			}	
		}
		
		if(a_oRC.getSubModList().contains("3*OC^XCO/4=O/3C")) {
			if(a_oRC.getSubModList().contains("2*NCC/3=O")) {
				tmp = "MurNAc";
				a_oRC.getSubstituent().remove("2*NCC/3=O");
				a_oRC.getSubstituent().remove("3*OC^XCO/4=O/3C");
			}
			if(a_oRC.getSubModList().contains("2*NCCO/3=O")) {
				tmp = "MurNGc";
				a_oRC.getSubstituent().remove("2*NCCO/3=O");
				a_oRC.getSubstituent().remove("3*OC^XCO/4=O/3C");
			}
				
		}
		
		return tmp;
	}
	
	private static void checkPrefixOxygen(ResidueContainer a_oRC, String a_sKey) {
		String[] a_sPos = a_sKey.split("\\*");
		for(String a_sMAP : copy(a_oRC.getNativeSubstituent())) {
			if(a_sMAP.equals(a_sKey)) {
				a_oRC.getNativeSubstituent().remove(a_sKey);
				continue;
			}
			if(a_sMAP.startsWith(a_sKey)) {
				String a_sNewMAP = a_sMAP.replace(a_sKey, a_sPos[0] + "*O");
				a_oRC.getNativeSubstituent().remove(a_sMAP);
				a_oRC.getSubstituent().add(a_sNewMAP);
				continue;
			}
		}
		
		return;
	}
	
	private static void margeSubstituent(ResidueContainer a_oRC) {
		HashMap<String, String> a_mMAP = new HashMap<String, String>();
		for(String a_sMAP : copy(a_oRC.getSubstituent())) {
			String[] a_s = a_sMAP.split("\\*");
			
			if(a_s[0].equals("?")) continue;
			if(a_s.length > 2) {
				String a_sSplited = "";
				for(int i = 1; i < a_s.length; i++)
					a_sSplited += "*" + a_s[i];
				
				a_s[1] = a_sSplited.replaceFirst("\\*", "");
			}
			if(!a_mMAP.containsKey(a_s[0])) {
				a_mMAP.put(a_s[0], "*" + a_s[1]);
			}else {
				if(a_s[1].startsWith("*O")) 
					a_mMAP.put(a_s[0], a_s[1].replaceFirst("\\*O", "*" + a_mMAP.get(a_s[1])));
				else {
					a_mMAP.put(a_s[0], a_mMAP.get(a_s[0]).replaceFirst("\\*O", "*" + a_s[1]));
				}
			}
			
			a_oRC.getSubstituent().remove(a_sMAP);
		}
		
		for(String a_sKey : a_mMAP.keySet()) {
			a_oRC.addSubstituent(a_sKey + a_mMAP.get(a_sKey));
		}
		
		return;
	}
	
	private static void margeNativeSubstituent(ResidueContainer a_oRC) {
		HashMap<String, String> a_mMAP = new HashMap<String, String>();
		for(String a_sMAP : copy(a_oRC.getNativeSubstituent())) {
			String[] a_s = a_sMAP.split("\\*");
			if(!a_mMAP.containsKey(a_s[0])) {
				a_mMAP.put(a_s[0], "*" + a_s[1]);
			}else {
				if(a_s[1].startsWith("O")) {
					a_mMAP.put(a_s[0], a_s[1].replaceFirst("O", a_mMAP.get(a_s[0])));
				}else {
					a_mMAP.put(a_s[0], a_mMAP.get(a_s[0]).replaceFirst("O", a_s[1]));
				}
			}
			a_oRC.getNativeSubstituent().remove(a_sMAP);
		}
		
		for(String a_sMAP : copy(a_oRC.getSubstituent())) {
			String[] a_s = a_sMAP.split("\\*");
			if(a_oRC.getBackBoneSize() == 6 && a_s[0].equals("2")) continue; 
			if(a_mMAP.containsKey(a_s[0])) {
				if(a_s[1].startsWith("O")) {
					a_mMAP.put(a_s[0], a_s[1].replaceFirst("O", a_mMAP.get(a_s[0])));
					a_oRC.getSubstituent().remove(a_sMAP);
				}else {
					a_mMAP.put(a_s[0], a_mMAP.get(a_s[0]).replaceFirst("O", a_s[1]));
					a_oRC.getSubstituent().remove(a_sMAP);
				}
			}
		}
		
		for(String a_sKey : a_mMAP.keySet()) {
			//a_oRC.addSubstituent(a_sKey + a_mMAP.get(a_sKey));
			a_oRC.addNativeSubstituent(a_sKey + a_mMAP.get(a_sKey));
		}
		
		return;
	}
	
	private static ArrayList<String> copy(ArrayList<String> a_aSUBs) {
		ArrayList<String> a_aMAPs = new ArrayList<String>();
		
		for(String a_sMAP : a_aSUBs) {
			a_aMAPs.add(a_sMAP);
		}
		
		return a_aMAPs;
	}
	
	private static boolean isLegtose (TrivialNameDescriptor a_enumTrivialDescriptor) {
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.DLEG)) return true;
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.LLEG)) return true;
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.LEG)) return true;

		return false;		
	}
	
	private static boolean isKetoDeoxyNonase (TrivialNameDescriptor a_enumTrivialDescriptor) {
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.DKDN)) return true;
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.LKDN)) return true;
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.KDN)) return true;
		
		return false;
	}
	
	private static boolean isQuinovose (TrivialNameDescriptor a_enumTrivialDescriptor) {
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.DQUI)) return true;
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.LQUI)) return true;
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.QUI)) return true;
		
		return false;
	}
	
	private static boolean isRhamnose (TrivialNameDescriptor a_enumTrivialDescriptor) {
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.DRHA)) return true;
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.LRHA)) return true;
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.RHA)) return true;
		
		return false;
	}
	
	private static boolean isFucose (TrivialNameDescriptor a_enumTrivialDescriptor) {
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.DFUC)) return true;
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.LFUC)) return true;
		if(a_enumTrivialDescriptor.equals(TrivialNameDescriptor.FUC)) return true;
		
		return false;
	}
}