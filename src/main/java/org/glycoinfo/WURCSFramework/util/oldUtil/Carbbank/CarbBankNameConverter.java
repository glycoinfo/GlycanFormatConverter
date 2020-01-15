package org.glycoinfo.WURCSFramework.util.oldUtil.Carbbank;

import org.glycoinfo.GlycanFormatconverter.Glycan.AnomericStateDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;
import org.glycoinfo.WURCSFramework.util.oldUtil.SubstituentConverter;
import org.glycoinfo.WURCSFramework.util.oldUtil.TrivialNameConverter;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainer;
import org.glycoinfo.WURCSFramework.util.residuecontainer.SuperClass;

import java.util.Iterator;
import java.util.LinkedList;

public class CarbBankNameConverter {

	private SubstituentConverter a_objSC = new SubstituentConverter();
	
	public String makeCommonName(ResidueContainer a_objRC) throws WURCSFormatException, ConverterExchangeException {
		StringBuilder ret = (a_objRC.getCommonName().size() > 1) ? 
				new StringBuilder(this.convertMultipleMotifMonosaccharide(a_objRC, true)) : new StringBuilder(a_objRC.getCommonName().getFirst());
		
		if(a_objRC.isMotif()) {
			SuperClass a_enumSuperClass = SuperClass.getSize(a_objRC.getBackBoneSize());
			if(ret.indexOf("-") == -1) 
				ret = new StringBuilder(ret.toString().toLowerCase());
			ret.append(a_enumSuperClass.getSuperClass());
		}
		if(a_objRC.isAcidicSugar()) ret.append("A");
		
		if(!a_objRC.isAcidicSugar() && !a_objRC.getNativeSubstituent().isEmpty()) {
			String t = this.a_objSC.convertSubstituent(a_objRC.getNativeSubstituent().get(0));
			if(t.startsWith("5") && (t.contains("NAc") || t.contains("NGc"))) {
				t = t.replace("N", "");
			}
			
			if(t.equals("N*S")) t = "N";
			ret.append(t);
		}
		
		return ret.toString();
	}
		
	public String makeIUPACExtendedNotation(ResidueContainer a_objRC)
			throws WURCSFormatException, ConverterExchangeException, TrivialNameException {
		/** convert the monosaccharide name official name to trival name*/
		StringBuilder str_standard = new StringBuilder(makeSugarNotation(a_objRC));
		
		/**define CarbBank notation*/
		
		/**convert trival name (d-gro-dgalNon -> Neu)*/
		str_standard = new StringBuilder(TrivialNameConverter.convertTrivalName(a_objRC, str_standard.toString()));
		
		/**append ring size*/
		if(a_objRC.getRingSize() != ' ') str_standard.append(a_objRC.getRingSize());
		
		/**append acidic status*/
		if(a_objRC.isAcidicSugar()) str_standard.append("A");
		
		/**append D/L configuration*/
		if(a_objRC.getDLconfiguration().size() > 0 && 
			(str_standard.indexOf("?-") == -1 && str_standard.indexOf("D-") == -1 && str_standard.indexOf("L-") == -1)) 
			str_standard.insert(0, a_objRC.getDLconfiguration().getFirst() + "-");
		
		/**append substituent*/
		str_standard.append(this.a_objSC.appendSubstituent(a_objRC));
		
		/**append anomer state*/
		if(str_standard.indexOf("gro") != -1 && a_objRC.getBackBoneSize() > 6) 
			str_standard.insert(str_standard.indexOf("gro") + 4, convertAnomerStateCarb(a_objRC));
		else 
			str_standard.insert(0, convertAnomerStateCarb(a_objRC));
		
		/**append various modification (onic, alditol, aldehyde)*/
		if(a_objRC.isAldehydo()) str_standard.insert(0, "aldehyde-");
		if(a_objRC.isONIC()) str_standard.append("-onic");
		if(a_objRC.isARIC()) str_standard.append("-aric");
		if(a_objRC.isAlditol()) str_standard.append("-ol");
		
		/** append probability annotation */
		str_standard.insert(0, this.makeProbabilityAnnotation(a_objRC));
		
		return str_standard.toString();
	}
		
	private String makeSugarNotation(ResidueContainer a_objRC)
			throws WURCSFormatException, ConverterExchangeException, TrivialNameException {
		StringBuilder ret = new StringBuilder();
		LinkedList<String> lst_coreName = a_objRC.getCommonName();
		String str_deoxy = this.a_objSC.makeDeoxyPosition(a_objRC);		
		
		for (Iterator<String> i = lst_coreName.iterator(); i.hasNext(); ) {
			String str_coreName = i.next();
			
			ret.append(a_objRC.getDLconfiguration().get(lst_coreName.indexOf(str_coreName)));
			ret.append("-");
			
			if(a_objRC.isMotif()) {
				String str_prefix = str_coreName.substring(0, 1);
				str_coreName = str_coreName.replaceFirst(str_prefix, str_prefix.toLowerCase());
			}
			
			ret.append(str_coreName);

			if(i.hasNext()) ret.append("-");
		}
		
		if(a_objRC.isMotif()) {
			char char_tmp = ret.toString().charAt(0);
			if(!ret.toString().startsWith("L-") && !ret.toString().startsWith("D-"))
				ret = ret.replace(0, 1, String.valueOf(char_tmp).toLowerCase());

			/** append super class*/
			ret.append(a_objRC.getSuperClass());	
		}

		/**append ketose*/
		ret.append(this.a_objSC.appendUlonic(a_objRC));
			
		/** append deoxy position as pos-deoxy-*/
		if(!ret.toString().contains(str_deoxy)) ret.insert(0, str_deoxy);
		
		return ret.toString();
	}
	
	public String createIUPACondensedNotation (ResidueContainer a_oRC) throws WURCSFormatException, ConverterExchangeException, TrivialNameException {
		/** convert the monosaccharide name official name to trival name*/
		StringBuilder str_standard = new StringBuilder(this.createBaseTypeName(a_oRC));
		String a_sDeoxy = this.a_objSC.makeDeoxyPosition(a_oRC);
		
		if(str_standard.toString().contains("NAc")) 
			str_standard.replace(str_standard.indexOf("NAc"), str_standard.indexOf("NAc") + 3, "");
		if(str_standard.toString().contains("5Gc")) 
			str_standard.replace(str_standard.indexOf("5Gc"), str_standard.indexOf("5Gc") + 3, "");
		if(str_standard.toString().contains("5Ac")) 
			str_standard.replace(str_standard.indexOf("5Ac"), str_standard.indexOf("5Ac") + 3, "");
		if(str_standard.toString().endsWith("N"))
			str_standard.replace(str_standard.indexOf("N"), str_standard.indexOf("N") + 1, "");
		
		/** append ulonic */
		if(a_oRC.getModification().contains("2*ulo") && a_oRC.getCommonName().size() > 1 && str_standard.indexOf("2ulo") == -1) 
			str_standard.append(this.a_objSC.appendUlonic(a_oRC));
	
		/** append deoxy position as pos-deoxy-*/
		if(!str_standard.toString().contains(a_sDeoxy)) str_standard.insert(0, a_sDeoxy);
		
		/**append substituent*/
		str_standard.append(this.a_objSC.appendSubstituent(a_oRC));
		
		/**append various modification (onic, alditol, aldehyde)*/
		if(a_oRC.isAldehydo()) str_standard.insert(0, "aldehyde-");
		if(a_oRC.isONIC()) str_standard.append("-onic");
		if(a_oRC.isARIC()) str_standard.append("-aric");
		if(a_oRC.isAlditol()) str_standard.append("-ol");
		
		/** append probability annotation */
		str_standard.insert(0, this.makeProbabilityAnnotation(a_oRC));
		
		return str_standard.toString();
	}
	
	public String createBaseTypeName (ResidueContainer a_oRC) throws ConverterExchangeException {
		StringBuilder ret = (a_oRC.getCommonName().size() > 1) ? 
				new StringBuilder(this.convertMultipleMotifMonosaccharide(a_oRC, false)) : new StringBuilder(a_oRC.getCommonName().getFirst());
		
		if(a_oRC.isMotif()) {
			SuperClass a_enumSuperClass = SuperClass.getSize(a_oRC.getBackBoneSize());
			if(ret.indexOf("-") == -1) 
				ret = new StringBuilder(ret.toString().toLowerCase());
			ret.append(a_enumSuperClass.getSuperClass());
		}
		if(a_oRC.isAcidicSugar()) ret.append("A");
		
		if(!a_oRC.isAcidicSugar() && !a_oRC.getNativeSubstituent().isEmpty()) {
			String t = this.a_objSC.convertSubstituent(a_oRC.getNativeSubstituent().get(0));
			
			if(t.equals("N*S")) t = "N";
			ret.append(t);
		}
		
		return ret.toString();
	}
	
	private String convertMultipleMotifMonosaccharide(ResidueContainer a_oResidueC, boolean a_bIsShowDL) {
		String a_sMultipleName = "";
		for(Iterator<String> i = a_oResidueC.getCommonName().iterator(); i.hasNext();) {
			String a_sTrivialName = i.next();
			
			if(a_bIsShowDL) {
				String a_sDLConfiguration = a_oResidueC.getDLconfiguration().get(a_oResidueC.getCommonName().indexOf(a_sTrivialName));	
				a_sMultipleName += a_sDLConfiguration + "-";
			}
			a_sMultipleName += a_sTrivialName.toLowerCase();
			
			if(i.hasNext()) a_sMultipleName += "-";
		}
		
		return a_sMultipleName;
	}
	
	private String convertAnomerStateCarb(ResidueContainer a_objRC) {
		AnomericStateDescriptor a_enumAnom = AnomericStateDescriptor.forAnomericState(a_objRC.getAnomerSymbol());
		
		String ret = "";
		if(a_enumAnom != null && !a_enumAnom.getIUPACAnomericState().equals("")) {
			ret = a_enumAnom.getIUPACAnomericState() + "-";
		}
		
		return ret;
	}
	
	private String makeProbabilityAnnotation (ResidueContainer a_oRC) {
		StringBuilder a_sProbability = new StringBuilder("");
    	int a_iHigh = (int) (a_oRC.getLinkage().getProbabilityHigh() * 100);
    	int a_iLow = (int) (a_oRC.getLinkage().getProbabilityLow() * 100);
 
    	if((a_iHigh != 100 && a_iLow != 100) && (a_iHigh == a_iLow)) {
    		a_sProbability.append("(" + ((a_iHigh == -100) ? "?" : a_iHigh) + "%" + ")");
    		return a_sProbability.toString();
    	}
    	
    	if(a_iLow != 100) {
    		a_sProbability.append("(");
    		a_sProbability.append((a_iLow == -100) ? "?" : a_iLow);
    	}
    	if(a_iHigh != 100 || a_iLow < 100) {
    		if(a_sProbability.length() != 0) {
    			a_sProbability.append(",");
    			a_sProbability.append((a_iHigh == -100) ? "?" : a_iHigh);
    		}else {
    			a_sProbability.append("(");
    			a_sProbability.append((a_iHigh == -100) ? "?" : a_iHigh);
    		}
    	}
    	if(a_sProbability.length() > 0) a_sProbability.append("%)");
    	
    	return a_sProbability.toString();
	}
}