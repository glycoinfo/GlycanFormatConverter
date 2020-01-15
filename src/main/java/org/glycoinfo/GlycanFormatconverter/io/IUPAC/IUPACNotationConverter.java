package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.*;

import java.util.ArrayList;
import java.util.Iterator;

public class IUPACNotationConverter {

	private StringBuilder threeLetterCode = new StringBuilder();
	private StringBuilder coreCode = new StringBuilder();
	private SubstituentIUPACNotationConverter subConv = new SubstituentIUPACNotationConverter();

	public SubstituentIUPACNotationConverter getSubConv () {
		return subConv;
	}

	public String getCoreCode () {
		return this.coreCode.toString();
	}

	/** 
	 * notation for iupac short form (monosaccharide three letter code) 
	 * ManNAc
	 * */
	public String getThreeLetterCode() {
		return this.threeLetterCode.toString();
	}


	public void makeTrivialName (Node _node) throws GlycanException {
		StringBuilder trivialName;

		Monosaccharide mono = (Monosaccharide) _node;

		/* convert to tivial name (D-gro-D-galNon -> D-Neu) */
		ThreeLetterCodeConverter threeCon = new ThreeLetterCodeConverter();
		threeCon.start(_node);

		if(threeCon.getThreeLetterCode().equals("")) {
			if (mono.getStereos().isEmpty()) {
				trivialName = new StringBuilder(mono.getSuperClass().getSuperClass());
			} else {
				trivialName = new StringBuilder(makeStandardName(mono));
			}
		} else {
			trivialName = new StringBuilder(makeTrivialName(mono, threeCon));
		}

		subConv.start(trivialName.toString(), mono);

		/* define non additional notation */
		threeLetterCode = new StringBuilder(trivialName);

		/* */
		trivialName.append(subConv.getCoreSubstituentNotaiton());
		coreCode.append(trivialName);

		/* make acidic tail */
		if(makeAcidicStatus(mono).equals("A")) {
			coreCode.append("A");
		}
	}

	private String makeStandardName (Node _node) {
		Monosaccharide mono = (Monosaccharide) _node;

		StringBuilder ret = new StringBuilder();

		for(Iterator<String> iterStereo = mono.getStereos().iterator(); iterStereo.hasNext();) {
			String stereo = iterStereo.next();

			String configuration = makeConfiguration(stereo);
			String threeLetter = trimThreeLetterPrefix(stereo, configuration);

			if (mono.getStereos().size() != 1) {
				ret.append(configuration.equals("x") ? "?" : configuration.toUpperCase());
				ret.append("-");
			}

			if(isFuzzyMotif(_node)) {
				String prefix = threeLetter.substring(0, 1);
				threeLetter = threeLetter.replaceFirst(prefix, prefix.toLowerCase());
			}

			ret.append(threeLetter);

			if(iterStereo.hasNext()) ret.append("-");
		}

		/* append super class */
		if (isFuzzyMotif(_node)) ret = appendSuperClass(_node, ret);

		return ret.toString();
	}

	private String makeTrivialName (Node _node, ThreeLetterCodeConverter _threeCon) {
		Monosaccharide mono = (Monosaccharide) _node;
		String trivial = _threeCon.getThreeLetterCode();

		StringBuilder ret = new StringBuilder(trivial);

		/**/
		if(mono.getSuperClass().getSize() > _threeCon.getSize()) {
			ret = appendSuperClass(_node, ret);
		}

		return ret.toString();
	}

	public String makeConfiguration (String _stereo) {
		String configuration = "?";

		_stereo = trimThreeLetterPrefix(_stereo);

		BaseTypeDictionary baseDict = BaseTypeDictionary.forName(_stereo.toLowerCase());
		configuration = baseDict.getConfiguration();

		return configuration;
	}

	private StringBuilder appendSuperClass (Node _node, StringBuilder _temp) {
		if(isFuzzyMotif(_node)) {
			char prefix = _temp.toString().charAt(0);
			if(_temp.indexOf("-") == -1)
				_temp = _temp.replace(0, 1, String.valueOf(prefix).toLowerCase());
			
			/* append super class*/
			_temp.append(((Monosaccharide) _node).getSuperClass().getSuperClass());
		}

		return _temp;
	}
	
	private String trimThreeLetterPrefix(String _letter, String _configuration) {
		if(_configuration.equals("?")) {
			if(_letter.startsWith("d/l-")) _letter = _letter.replaceFirst("d/l-", "");
			if(_letter.startsWith("l/d-")) _letter = _letter.replaceFirst("l/d-", "");
		}else {
			_letter = _letter.replaceFirst(_configuration, "");
		}

		StringBuilder ret = new StringBuilder(_letter);
		ret = ret.replace(0, 1, ret.substring(0, 1).toUpperCase());
		return ret.toString();
	}

	private String trimThreeLetterPrefix (String _letter) {
		if (_letter.startsWith("d/l-")) _letter = _letter.replaceFirst("d/l-", "");
		if (_letter.startsWith("l/d-")) _letter = _letter.replaceFirst("l/d-", "");

		if (_letter.length() == 3) {
			StringBuilder ret = new StringBuilder(_letter);
			ret = ret.replace(0, 1, ret.substring(0, 1).toUpperCase());
			return ret.toString();
		}

		return _letter;
	}
	
	public String extractUlonic(Monosaccharide _mono) {
		String ret = "";

		for(GlyCoModification mod : _mono.getModifications()) {
			ModificationTemplate modT = mod.getModificationTemplate();
			if(modT.equals(ModificationTemplate.KETONE_U)) {
				ret += mod.getPositionOne() + modT.getIUPACnotation();
			}
			if(modT.equals(ModificationTemplate.ULOSONIC)) {
				ret += mod.getPositionOne() + modT.getIUPACnotation();
			}
			if (modT.equals(ModificationTemplate.KETONE)) {
				ret += mod.getPositionOne() + modT.getIUPACnotation();
			}
		}
		
		return ret;
	}
	
	public String extractDLconfiguration(String _stereo) {
		if(_stereo.length() == 4) {
			return _stereo.substring(0, 1);
		}
		if (_stereo.startsWith("d/l-") || _stereo.startsWith("l/d-")) {
			return "?";
		}
		/*if(_stereo.length() == 3 || _stereo.length() > 4) {
			return "?";
		}*/

		return "?";
	}
	
	private boolean isFuzzyMotif(Node _node) {
		Monosaccharide mono = (Monosaccharide) _node;
		SuperClass superclass = mono.getSuperClass();
		
		if(superclass == null) return false;
		if(mono.getStereos().size() > 1) return true;
		//if (mono.getStereos().getFirst().contains(superclass.getSuperClass().toLowerCase())) return false;
		if(mono.getStereos().contains(superclass.getSuperClass().toLowerCase())) return false;

		for(GlyCoModification mod : mono.getModifications()) {
			ModificationTemplate modT = mod.getModificationTemplate();
			if(modT == null) continue;

			if (superclass.getSize() == mod.getPositionOne()) return false;

			if(modT.equals(ModificationTemplate.DEOXY)) return true;
			if(modT.equals(ModificationTemplate.METHYL)) return true;
			if(modT.equals(ModificationTemplate.ALDEHYDE) && mod.getPositionOne() != 1) return true;
			if(modT.equals(ModificationTemplate.ULOSONIC) && mod.getPositionOne() != 2) return true;
			if(modT.equals(ModificationTemplate.KETONE_U)) return true;//&& mod.getPositionOne() != 2) return true;
			if(modT.equals(ModificationTemplate.UNSATURATION_EL)) return true;
			if(modT.equals(ModificationTemplate.UNSATURATION_EU)) return true;
			if(modT.equals(ModificationTemplate.UNSATURATION_FL)) return true;
			if(modT.equals(ModificationTemplate.UNSATURATION_FU)) return true;
			if(modT.equals(ModificationTemplate.UNSATURATION_ZL)) return true;
			if(modT.equals(ModificationTemplate.UNSATURATION_ZU)) return true;
		}
				
		return false;
	}
	
	public String makeDeoxyPosition(Monosaccharide _mono) throws TrivialNameException {
		if(_mono.getModifications().isEmpty()) return "";
		
		ArrayList<GlyCoModification> deoxys = new ArrayList<GlyCoModification>();
		
		for(GlyCoModification mod : _mono.getModifications()) {
			ModificationTemplate modT = mod.getModificationTemplate();
			if(modT.equals(ModificationTemplate.DEOXY)) deoxys.add(mod);
			if(modT.equals(ModificationTemplate.METHYL)) deoxys.add(mod);
			if(modT.equals(ModificationTemplate.UNSATURATION_EL)) deoxys.add(mod);
			if(modT.equals(ModificationTemplate.UNSATURATION_FL)) deoxys.add(mod);
			if(modT.equals(ModificationTemplate.UNSATURATION_ZL)) deoxys.add(mod);
		}
		
		/* define prefix */
		StringBuilder deoxy = new StringBuilder();
		PrefixDescriptor a_enumPrefix = PrefixDescriptor.forNumber(deoxys.size());
		
		for (Iterator<GlyCoModification> i = deoxys.iterator(); i.hasNext(); ) {
			deoxy.append(i.next().getPositionOne());
			if(i.hasNext()) deoxy.append(",");
			else deoxy.append("-" + a_enumPrefix.getPrefix() + "deoxy-");
		}
	
		return deoxy.toString();
	}
	
	public boolean isAlditol(Node _node) {
		Monosaccharide mono = (Monosaccharide) _node;
		
		if(mono.getAnomericPosition() != 0) return false;
		//if(mono.getSuperClass().getSize() > 6) return false;
		
		boolean ret = false;
		for(GlyCoModification mod : mono.getModifications()) {
			if(mod.getPositionOne() == 1 &&
					mod.getModificationTemplate().equals(ModificationTemplate.HYDROXYL)) ret = true;
			if(mod.getPositionOne() == 2 &&
					mod.getModificationTemplate().equals(ModificationTemplate.KETONE_U)) ret = false;
		}		
		
		return ret;
	}
	
	public boolean isAldehyde(Node _node) {
		Monosaccharide mono = (Monosaccharide) _node;
		boolean aldehyde = false;
		
		if(mono.getAnomericPosition() != 0) return false;
		
		for(GlyCoModification mod : mono.getModifications()) {
			ModificationTemplate modT = mod.getModificationTemplate();
			if(modT.equals(ModificationTemplate.ALDEHYDE) && mod.getPositionOne() == 1) {
				aldehyde = true;
			}
		}
		
		return aldehyde;
	}

	public String makeAcidicStatus(Node _node) {
		Monosaccharide mono = (Monosaccharide) _node;
		
		boolean headAcid = false;
		boolean tailAcid = false;
		ModificationTemplate acid = ModificationTemplate.ALDONICACID;
		SuperClass superclass = mono.getSuperClass();
		
		for(GlyCoModification mod : mono.getModifications()) {
			ModificationTemplate modT = mod.getModificationTemplate();
			if(mod.getPositionOne() == 1 && modT.equals(ModificationTemplate.ALDONICACID)) headAcid = true;
			if (mod.getPositionOne() == superclass.getSize() && modT.equals(ModificationTemplate.URONICACID)) tailAcid = true;
			//if(mod.getPositionOne() == superclass.getSize() && modT.equals(acid)) tailAcid = true;
		}

		if (headAcid && tailAcid) return "-aric";
		if (headAcid && !tailAcid) return "-onic";
		if(!headAcid && tailAcid) {
			if(superclass.equals(SuperClass.HEX)) return "A";
			else return "-uronic";
		}
		
		return "";
	}
	
	public boolean containUlonicAcid(String _code) {
		TrivialNameDictionary dict = TrivialNameDictionary.forThreeLetterCode(_code);
		ModifiedMonosaccharideDescriptor mdict = ModifiedMonosaccharideDescriptor.forTrivialName(_code);
		
		if(dict != null) {
			if(dict.equals(TrivialNameDictionary.KO)) return true;
			if(dict.equals(TrivialNameDictionary.NEU)) return true;
			if(dict.equals(TrivialNameDictionary.LEG)) return true;
			if(dict.equals(TrivialNameDictionary.KDO)) return true;
			if(dict.equals(TrivialNameDictionary.KDN)) return true;
		}
		if(mdict != null) {
			if(mdict.equals(ModifiedMonosaccharideDescriptor.NEU5AC)) return true;
			if(mdict.equals(ModifiedMonosaccharideDescriptor.NEU5GC)) return true;		
		}	
		
		return false;
	}
	
	public String defineRingSize(Node _node) {
		Monosaccharide mono = (Monosaccharide) _node;
		int start = mono.getRingStart();
		int end = mono.getRingEnd();
		
		if(start == 0 && end == 0) return "";
		if(start == -1 && end == -1) return "";
		if(start != -1 && end == -1) return "?";
		if((start + end == 5) || (start == 2 && end == 5)) return "f";
		if((start + end == 6) || (start == 2 && end == 6)) return "p";
		
		return "?";
	}
}