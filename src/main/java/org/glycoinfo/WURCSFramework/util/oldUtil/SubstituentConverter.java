package org.glycoinfo.WURCSFramework.util.oldUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.glycoinfo.GlycanFormatconverter.util.TrivialName.HexoseDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.PrefixDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainer;

public class SubstituentConverter {

	public String appendSubstituent(ResidueContainer a_oRC) throws ConverterExchangeException {
		ArrayList<String> a_aMODs = new ArrayList<String>();		
		String ret = "";

		/**Check core modification*/
		for(String a_strNS : copy(a_oRC.getNativeSubstituent()) ) {
			if(!a_oRC.isAcidicSugar() && isChangeCompositeWithSubstituent(a_oRC)) {
				ret += convertSubstituent(a_strNS);
			}else {
				a_oRC.addSubstituent(a_strNS);
				a_oRC.getNativeSubstituent().remove(a_strNS);
			}
		}
		
		/**Check periferal modification*/
		for(String a_sMAP : a_oRC.getSubstituent()) {
			if(a_sMAP.equals("?*")) continue;
			if(a_sMAP.equals("-1*")) {
				throw new ConverterExchangeException(a_sMAP + " can not handled");
			}
			
			String[] s = a_sMAP.split("\\*");
			String a_sIUPACSubstituent = convertIUPACnotation(a_sMAP.substring(a_sMAP.indexOf("*")));
			String a_key = getKey(a_aMODs, a_sIUPACSubstituent);

			if(a_key.equals("")) a_aMODs.add(s[0] + "_" + a_sIUPACSubstituent);
			else {
				String[] tmp = a_key.split("_");

				if(s[0].equals("?") || 
						(Integer.parseInt(String.valueOf(s[0].charAt(0))) > 
						Integer.parseInt(String.valueOf(a_key.charAt(0))))) {
					a_aMODs.set(a_aMODs.indexOf(a_key), tmp[0] + "," + s[0] + "_" + a_sIUPACSubstituent);						
				}else
					a_aMODs.set(a_aMODs.indexOf(a_key), s[0] + "," + tmp[0] + "_" + a_sIUPACSubstituent);
			}
		}

		Collections.sort(a_aMODs);
		
		for(String a_item : a_aMODs) {
			String[] a_key = a_item.split("_");
			String str_dupeSub = checkSameSubstituent(a_key[0]);
			
			ret += a_key[0] + a_key[1] + str_dupeSub;
		}
		
		return ret;
	}
	
	private String convertIUPACnotation(String a_sMAP) throws ConverterExchangeException {
		String ret = "";
		SubstituentTemplate enum_sub = SubstituentTemplate.forMAP(a_sMAP);
		
		if(enum_sub == null || enum_sub.getIUPACnotation().equals(""))
			throw new ConverterExchangeException(
				"This substituent (or modification) can not convert correctly : " + a_sMAP);
		//if(enum_sub.isBridge())
		//	throw new ConverterExchangeException("Bridge is not handled : " + a_sMAP);

		if(enum_sub.isSubstituent()) ret = enum_sub.getIUPACnotation();
		if(enum_sub.equals(SubstituentTemplate.UNSATURATION)) ret = enum_sub.getIUPACnotation();
		if(enum_sub.isBridge()) ret = enum_sub.getIUPACnotation();
		
		return ret;
	}
	
	public String convertSubstituent(String a_sMAP) throws ConverterExchangeException {
		String[] str_MAP = a_sMAP.split("\\*");
		String ret = SubstituentTemplate.forMAP("*" + str_MAP[1]).getIUPACnotation();
		
		if(str_MAP[0].equals("5") && !ret.equals("N")) ret = str_MAP[0] + ret;
		
		return ret;
	}
	
	private String checkSameSubstituent(String a_sPosition) {
		int a_iCount = a_sPosition.split(",").length;
		
		if(a_iCount < 2) return "";
		else return String.valueOf(a_iCount);
	}
	
	private String getKey(ArrayList<String> a_aMODs, String a_sIUPACSubstituent) {
		String ret = "";
		
		for(String key : a_aMODs) {
			if(key.substring(key.indexOf("_") + 1).equals(a_sIUPACSubstituent)) {
				ret = key;
				break;
			}
		}
		
		return ret;
	}
	
	public String makeDeoxyPosition(ResidueContainer a_oRC) throws ConverterExchangeException, TrivialNameException {
		if(a_oRC.getModification().isEmpty()) return "";
			
		LinkedList<String> a_aDeoxys = new LinkedList<String>();
		
		StringBuilder a_sbAnhydro = new StringBuilder();
		
		for(String unit : this.copy(a_oRC.getModification())) {
			String[] s = unit.split("\\*");
			SubstituentTemplate enum_sub = SubstituentTemplate.forMAP("*" + s[1]);
			if(enum_sub.equals(SubstituentTemplate.DEOXY)) a_aDeoxys.add(s[0]);
			if(enum_sub.equals(SubstituentTemplate.DEOXYTAIL)) a_aDeoxys.add(s[0]);
			if(enum_sub.equals(SubstituentTemplate.UNSATURATION)) {
				a_oRC.addSubstituent(unit);
				a_oRC.getModification().remove(unit);
			}
			if(enum_sub.equals(SubstituentTemplate.ANHYDROXYL)) {
				a_sbAnhydro.append(s[0] + "-" + enum_sub.getIUPACnotation() + "-");
				a_oRC.getModification().remove(unit);
			}
		}
		
		/** define prefix */
		StringBuilder a_sDeoxy = new StringBuilder();		
		PrefixDescriptor a_enumPrefix = PrefixDescriptor.forNumber(a_aDeoxys.size());
		
		for (Iterator<String> i = a_aDeoxys.iterator(); i.hasNext(); ) {
			a_sDeoxy.append(i.next());
			if(i.hasNext()) a_sDeoxy.append(",");
			else a_sDeoxy.append("-" + a_enumPrefix.getPrefix() + "deoxy-");
		}
	
		a_sDeoxy.insert(0, a_sbAnhydro);
		
		return a_sDeoxy.toString();
	}
	
	public String appendUlonic(ResidueContainer a_objRC) throws ConverterExchangeException {
		String ret = "";
		for(String s: a_objRC.getModification()) {
			String[] ss = s.split("\\*");
			SubstituentTemplate enum_mod = SubstituentTemplate.forMAP("*" + ss[1]);
			if(enum_mod.getGlycoCTnotation().equals("keto")) ret += ss[0] + ss[1];
		}
		
		return ret;
	}
	
	private boolean isChangeCompositeWithSubstituent(ResidueContainer a_oRC) {
		if(a_oRC.getCommonName().size() == 2) return false;
		
		HexoseDescriptor enum_HexDesc = 
				HexoseDescriptor.forTrivialName(a_oRC.getCommonName().getFirst(), a_oRC.getBackBoneSize());
		
		if(enum_HexDesc != null) return true;
		return false;
	}
	
	
	public <T> List<T> copy(Collection<T> c) {
		return new ArrayList<T>(c);
	}
}
