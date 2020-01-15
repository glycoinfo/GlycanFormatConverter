package org.glycoinfo.WURCSFramework.util.oldUtil;

import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.array.WURCSImporter;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainer;
import org.glycoinfo.WURCSFramework.util.residuecontainer.SuperClass;
import org.glycoinfo.WURCSFramework.util.subsumption.MSStateDeterminationUtility;
import org.glycoinfo.WURCSFramework.util.subsumption.WURCSSubsumptionConverter;
import org.glycoinfo.WURCSFramework.wurcs.array.MOD;
import org.glycoinfo.WURCSFramework.wurcs.array.MS;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.GRES;

import java.util.ArrayList;
import java.util.LinkedList;

public class GRESToResidueData {

	private WURCSSubsumptionConverter a_oWSC = new WURCSSubsumptionConverter();
	private MSStateDeterminationUtility a_oMSSDU = new MSStateDeterminationUtility();
	private SubstituentAnalyzer a_oSubAnalyze = new SubstituentAnalyzer();
	
	private ResidueContainer a_oRC;
	
	public ResidueContainer getResidueContainer() {
		return this.a_oRC;
	}
	
	public void start(GRES a_oGRES) throws WURCSFormatException, ConverterExchangeException {
		int a_iAnomericPosition = a_oGRES.getMS().getCoreStructure().getAnomericPosition();
		char a_cSymbol = a_oGRES.getMS().getCoreStructure().getAnomericSymbol();
		
		String a_sMS = a_oGRES.getMS().getString();
		String a_sSkeletonCode = a_oGRES.getMS().getCoreStructure().getSkeletonCode();
		
		MS a_oMS = new WURCSImporter().extractMS(a_sMS);

		if(a_sMS.contains("<Q>")) {
			this.a_oRC = new ResidueContainer("?", ' ', "sugar");
			this.a_oRC.setAnomerSymbol(a_cSymbol);
			this.a_oRC.setSubstituent(this.a_oSubAnalyze.getSubstituents(a_oRC, a_oGRES, a_oMS));
			return;
		}
		
		SuperClass a_enumSuperClass = SuperClass.getSize(a_oMS.getSkeletonCode().length());
		LinkedList<String> a_aStereos = this.a_oMSSDU.extractStereo(a_oMS);
		
		/** retry */
		if(a_aStereos.isEmpty()) a_aStereos = retryStereo(a_oMS);
		
		ResidueContainer a_oRC = new ResidueContainer();

		/***/
		a_oRC.setBackBoneSize(a_enumSuperClass.getSize());
		
		/** define ring size */
		a_oRC.setRingSize(checkRingSize(a_oMS));
				
		for(String a_sStereo : a_aStereos) {
			/** define d/l configuration*/
			String a_sConfiguration = defineDLcofiguration(a_sStereo);
			
			/** define core name of monosaccharide */
			String a_sCoreName = defineNameOfMonosaccharide(a_sStereo, a_sConfiguration);
			
			a_oRC.addDLconfiguration(a_sConfiguration);
			a_oRC.addCommonName(a_sCoreName);
		}

		/** set anomer information*/
		a_oRC.setAnomerPosition(a_iAnomericPosition);
		a_oRC.setAnomerSymbol(a_cSymbol);
		
		/** define superclass*/
		a_oRC.setSuperClass(a_enumSuperClass.getSuperClass());

		/** check acidic sugar*/
		a_oRC.setAcidicSugar(isAcidicSugar(a_oMS));

		/** extract modification */
		a_oRC.setModification(extractModification(a_oMS, a_oRC));

		/** extract substituent*/
		a_oRC.setSubstituent(this.a_oSubAnalyze.getSubstituents(a_oRC, a_oGRES, a_oMS));
		
		/** check motif structure*/
		a_oRC.setMotif(checkSuperClass(a_sSkeletonCode, a_oRC));
		
		/** check alcohol */	
		a_oRC.setAlditol(isAnomerStatuswithAlcohol(a_oMS));

		/** check aldehyde*/
		a_oRC.setAldehydo(isAnomerStatuswithAldehydo(a_oMS));
		
		this.a_oRC = a_oRC;
	}
	
	private ArrayList<String> extractModification (MS a_oMS, ResidueContainer a_oRC) 
			throws ConverterExchangeException {
		ArrayList<String> a_aModifications = new ArrayList<String>();
		char[] a_aSkeletonCode = a_oMS.getSkeletonCode().toCharArray();
		int a_iAnomericPosition = a_oMS.getAnomericPosition();
		
		for(int i = 0; i < a_aSkeletonCode.length; i++) {
			if(!String.valueOf(a_aSkeletonCode[i]).matches("\\d")) {
				/** extract unsaturation character */
				if(a_aSkeletonCode[i] == 'e' || a_aSkeletonCode[i] == 'f' || a_aSkeletonCode[i] == 'z') {
					a_aModifications.add(i + 1 + "*d");
					if(!a_aModifications.contains(i + "*en")) a_aModifications.add(i + 1 + "*en");
				}
				if((a_aSkeletonCode[i] == 'E' && a_aSkeletonCode[i+1] == 'E') || 
						(a_aSkeletonCode[i] == 'F' && a_aSkeletonCode[i+1] == 'F') ||
						(a_aSkeletonCode[i] == 'Z' && a_aSkeletonCode[i+1] == 'Z')) {
					a_aModifications.add(i + 1 + "*en");
				}
				
				/**extract ulonic position*/
				if(a_iAnomericPosition == 2 && i == 1 && a_aSkeletonCode[i] == 'a') a_aModifications.add(i + 1 + "*ulo");
				if(i == 1 && a_aSkeletonCode[i] == 'U') a_aModifications.add(i + 1 + "*ulo");
				if(a_iAnomericPosition != i + 1 && a_aSkeletonCode[i] == 'O') a_aModifications.add(i + 1 + "*ulo");
				
				/**extract onic pos*/
				if(i == 0 && a_aSkeletonCode[i] == 'A') {
					a_oRC.setONIC(true);
					a_aModifications.add(i + 1 + "*a");
				}
				/**extract ulosaric pos*/
				if(((i + 1) == a_aSkeletonCode.length) && a_aSkeletonCode[i] == 'A' && a_oRC.isAcidicSugar() && a_oRC.isONIC()) {
					a_oRC.setARIC(true);
					a_oRC.setAcidicSugar(false);
					a_aModifications.add(i+ 1 + "*a");
				}
				
				/**extract deoxy position*/
				if(a_aSkeletonCode[i] == 'd' || a_aSkeletonCode[i] == 'm') a_aModifications.add(i + 1 + "*" + a_aSkeletonCode[i]);
			}
		}
		
		return a_aModifications;
	}
	
	private boolean isAnomerStatuswithAlcohol(MS a_oMS) {
		if(a_oMS.getAnomericPosition() == 2) return false;
		if(a_oMS.getAnomericPosition() == 0 && a_oMS.getSkeletonCode().contains("U")) return false;
		
		String a_sSkeltonCode = a_oMS.getSkeletonCode();
		if(a_sSkeltonCode.startsWith("h") /*&& a_objMS.getAnomericPosition() == 1*/) {
			if(this.a_oWSC.convertCarbonylGroupToHydroxyl(a_oMS) == null) return true;
		}
		
		return false;
	}
	
	private boolean isAnomerStatuswithAldehydo(MS a_oMS) {
		int a_iAnomericPosition = a_oMS.getAnomericPosition();
		if(a_oMS.getSkeletonCode().indexOf("o") == a_iAnomericPosition) return true;
		if(a_oMS.getSkeletonCode().indexOf("O") == a_iAnomericPosition) return true;
		
		return false;
	}
	
	private LinkedList<String> retryStereo(MS a_oMS) throws ConverterExchangeException {
		LinkedList<String> a_aStereo = new LinkedList<String>();
		String a_sSkeletonCode = a_oMS.getSkeletonCode();
		SuperClass enum_class = SuperClass.getSize(a_oMS.getSkeletonCode().length());
			
		if(a_sSkeletonCode.contains("1") && a_sSkeletonCode.contains("2")) {
			for(String s : 
				this.a_oMSSDU.extractStereo(this.a_oWSC.convertConfigurationUnknownToAbsolutes(a_oMS).getFirst())) {
				if(a_sSkeletonCode.endsWith("xh") && s.contains("gro")) {
					a_aStereo.addLast(checkDLconfiguration(s));
					continue;
				}a_aStereo.addLast(s);
			}
		}
		
		if(a_sSkeletonCode.contains("3") || a_sSkeletonCode.contains("4")) {
			a_aStereo = this.a_oMSSDU.extractStereo(a_oWSC.convertConfigurationRelativeToD(a_oMS));
		}
		
		if(a_aStereo.isEmpty()) {
			if(enum_class.getSuperClass().equals("Tet"))
				throw new ConverterExchangeException(a_oMS.getSkeletonCode() + " could not handled");
			else if(enum_class.getSuperClass().equals("Tri")) {
				MS a_oGrose = this.a_oWSC.convertConfigurationUnknownToAbsolutes(a_oMS).getFirst();
				a_aStereo.add(checkDLconfiguration(this.a_oMSSDU.extractStereo(a_oGrose).getFirst()));
			}else
				a_aStereo.add(enum_class.getSuperClass());				
		}
		
		return a_aStereo;
	}
	
	private boolean isAcidicSugar(MS a_oMS) {
		int a_iLength = a_oMS.getSkeletonCode().length();
		
		if(a_iLength - 1 == a_oMS.getSkeletonCode().lastIndexOf("A")) return true;		
		return false;
	}
	
	private boolean checkSuperClass(String a_sSkeletonCode, ResidueContainer a_oRC) {
		boolean a_isMotif = false;

		if(SuperClass.getSuperClass(a_oRC.getCommonName().getFirst()) != null)
			return a_isMotif;
		char[] a_SC = a_sSkeletonCode.toCharArray();
		
		for(int i = 0; i < a_SC.length; i++) {
			if(a_oRC.getAnomerPosition() != i && (a_SC[i] == 'o' || a_SC[i] == 'O'))
				a_isMotif = true;
			if(a_SC[i] == 'd'/* || a_SC[i] == 'm'*/) 
				a_isMotif = true;
			if(a_SC[i] == 'e' || a_SC[i] == 'f' || a_SC[i] == 'z' || a_SC[i] == 'E' || a_SC[i] == 'F' || a_SC[i] == 'Z')
				a_isMotif = true;
		}
		
		if(a_sSkeletonCode.length() > 6 && a_oRC.getCommonName().size() > 1) 
			a_isMotif = true;

		return a_isMotif;
	}
	
	private String defineNameOfMonosaccharide(String a_sTrivialName, String a_sConfiguration) {	
		if(a_sConfiguration.equals("?")) {
			if(a_sTrivialName.startsWith("d/l-")) a_sTrivialName = a_sTrivialName.replaceFirst("d/l-", "");
			if(a_sTrivialName.startsWith("l/d-")) a_sTrivialName = a_sTrivialName.replaceFirst("l/d-", "");
		}else
			a_sTrivialName = a_sTrivialName.replaceFirst(a_sConfiguration, "");
		
		String str_prefix = a_sTrivialName.substring(0, 1);
		a_sTrivialName = a_sTrivialName.replaceFirst(str_prefix, str_prefix.toUpperCase());
		
		return a_sTrivialName;
	}
	
	private String defineDLcofiguration(String a_sConfiguration) {
		String ret = "?";
		if(a_sConfiguration.startsWith("l") || a_sConfiguration.startsWith("d")) ret = a_sConfiguration.substring(0, 1);
		if(a_sConfiguration.contains("d/l") || a_sConfiguration.contains("l/d")) ret = "?";
		
		return ret;
	}
	
	private char checkRingSize(MS a_oMS) {
		if(a_oMS.getMODs().size() == 0 || a_oMS.getAnomericPosition() == 0) return ' ';
		
		char a_cRingSize = ' ';
		MOD a_oRing = a_oMS.getMODs().getFirst();
		int a_iStart = a_oRing.getListOfLIPs().getFirst().getLIPs().getFirst().getBackbonePosition();
		int a_iEnd = a_oRing.getListOfLIPs().getLast().getLIPs().getFirst().getBackbonePosition();

		if(a_oMS.getAnomericPosition() == a_iStart) {
			if(a_iEnd == -1) a_cRingSize = '?';
			if(a_iEnd - a_iStart == 3) a_cRingSize = 'f';
			if(a_iEnd - a_iStart == 4) a_cRingSize = 'p';
		}

		return a_cRingSize;
	}
	
	private String checkDLconfiguration(String a_sStereo) {
		if((a_sStereo.startsWith("l") || a_sStereo.startsWith("d"))) 
			a_sStereo = a_sStereo.replaceFirst("[ld]", "");
		return a_sStereo;
	}
}
