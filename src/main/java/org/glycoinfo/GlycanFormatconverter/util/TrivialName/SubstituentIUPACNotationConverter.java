package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.glycoinfo.GlycanFormatconverter.Glycan.CrossLinkedTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanRepeatModification;
import org.glycoinfo.GlycanFormatconverter.Glycan.Linkage;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyCoModification;
import org.glycoinfo.GlycanFormatconverter.Glycan.ModificationTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.GlycanFormatconverter.Glycan.SubstituentInterface;
import org.glycoinfo.GlycanFormatconverter.Glycan.SubstituentTemplate;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;

public class SubstituentIUPACNotationConverter extends SubstituentUtility{

	private StringBuilder prefixSubs;
	private StringBuilder surfixSubs;
	private StringBuilder surfixCore;
	private HashMap<String, String> mapSubs;
	
	public String getCoreSubstituentNotaiton () {
		return surfixCore.toString();
	}
	
	public String getSubstituentNotation() {
		return surfixSubs.toString();
	}
	
	public String getPrefixSubstituent() {
		return prefixSubs.toString();
	}
	
	public void start(String _code, Node _current) throws GlycanException {
		init();
		
		StringBuilder nativeSub = new StringBuilder();
		
		for (Edge child : _current.getChildEdges()) {
			if (child.getSubstituent() == null) continue;
			
			Substituent sub = (Substituent) child.getSubstituent();
			SubstituentInterface subface = sub.getSubstituent();

			if (subface == null || sub instanceof GlycanRepeatModification || child.getChild() != null) continue;

			/* check N-sulfate of hexose */
			if (haveNativeSubstituentWithNsulfate(_code, sub, _current)) {
				nativeSub.append(SubstituentTemplate.AMINE.getIUPACnotation());
				sub = makeConvertedSubstituent(sub);
			}

			if (haveNativeSubstituentInNeu(_code, sub, _current)) {
				nativeSub.append(makePosition(sub.getFirstPosition(), haveSecondPos(sub)) + subface.getIUPACnotation());
			} else if (haveNativeSubstituent(_code, sub, _current)) {
				nativeSub.append(subface.getIUPACnotation());
			} else if (haveAnhydroxyl(subface)) {
				extractAnhydroxylSubstituent(sub);
			} else {
				extractSubstituentWithPosition(sub);
			}
		}
		
		/**/
		if (prefixSubs.length() != 0) {
			prefixSubs.append("-Anhydro-");
		}
		
		surfixCore.append(nativeSub);

		/* extract unsaturated state */
		extractUnsaturatedState(_current);
		
		/**/
		StringBuilder coreSub = new StringBuilder();
		for (String unit : concatSubstituents()) {
			coreSub.append(unit);
		}
		surfixSubs.append(coreSub);
	}
	
	protected Substituent makeConvertedSubstituent(Substituent _sub) throws GlycanException {
		Linkage firstPos = _sub.getFirstPosition();
		SubstituentTemplate subT = this.convertNTypeToOType(_sub.getSubstituent());
		Substituent ret = null;
		
		if (subT == null) {
		} else {
			SubstituentInterface temp = convertNTypeToOType(_sub.getSubstituent());
			ret = new Substituent(temp);
			ret.setFirstPosition(firstPos);
		}
		
		return ret;
	}
	
	private String makePosition (Linkage _linkage, boolean _haveSecond) {
		if (_linkage == null) return "";
		
		StringBuilder ret = new StringBuilder();

		/* Append linkage position */
		for (Iterator<Integer> iterPos = _linkage.getParentLinkages().iterator(); iterPos.hasNext();) {
			Integer pos = iterPos.next();
			ret.append(pos == -1 ? "?" : pos);
			if (iterPos.hasNext()) ret.append("/");
		}		

		if (_haveSecond) {
			if (_linkage.getChildLinkages().contains(0))
				ret.append("-" + 1);
			else {
				ret.append("-" + _linkage.getChildLinkages().get(0));
			}
		}

		/* Append probability annotation */
		if (_linkage.getParentProbabilityLower() != 1.0D/* && _linkage.getParentProbabilityLower() != 1.0*/) {
			ret.append("(");
			if (_linkage.getParentProbabilityLower() == -1.0) {
				ret.append("?%");
			} else {
				ret.append( ((int) (_linkage.getParentProbabilityLower() * 100)) + "%");
			}
		}

		if (_linkage.getParentProbabilityUpper() != 1.0D/* && _linkage.getParentProbabilityUpper() != 1.0*/) {
			if ((_linkage.getParentProbabilityUpper() != _linkage.getParentProbabilityLower())) {
				if (ret.length() != 0) ret.append(",");
				if (_linkage.getParentProbabilityUpper() == -1.0) {
					ret.append("?%");
				} else {
					ret.append(((int) (_linkage.getParentProbabilityUpper() * 100)) + "%");
				}
			}
			ret.append(")");
		}

		if (ret.indexOf("(") != -1 && ret.indexOf(")") == -1) ret.append(")");

		return ret.toString();
	}
	
	private void extractUnsaturatedState(Node _node) {
		Monosaccharide mono = (Monosaccharide) _node;
		for (GlyCoModification mod : mono.getModifications()) {
			ModificationTemplate modT = mod.getModificationTemplate();
			if (!mapSubs.containsKey(modT.getIUPACnotation())) {
				if (isUnsaturate(modT)) mapSubs.put(modT.getIUPACnotation(), String.valueOf(mod.getPositionOne()));

				if (modT.equals(ModificationTemplate.UNKNOWN)) {
					mapSubs.put(modT.getIUPACnotation(), "?");
				}
			}
		}
	}

	private boolean isUnsaturate(ModificationTemplate _modT) {
		if (_modT.equals(ModificationTemplate.UNSATURATION_EL) ||
				_modT.equals(ModificationTemplate.UNSATURATION_FL) || 
				_modT.equals(ModificationTemplate.UNSATURATION_ZL) ||
				_modT.equals(ModificationTemplate.UNSATURATION_EU) ||
				_modT.equals(ModificationTemplate.UNSATURATION_FU) ||
				_modT.equals(ModificationTemplate.UNSATURATION_ZU)) return true;
		
		return false;
	}
	
	private ArrayList<String> concatSubstituents() {
		ArrayList<String> ret = new ArrayList<String>();
		
		for (String key : mapSubs.keySet()) {
			int numOfsub = 0;
			String notation = mapSubs.get(key);
			if (notation.indexOf(",") != -1 && notation.indexOf("-") == -1) {
				numOfsub = notation.split(",").length;
			}
			if (notation.indexOf(":") != -1) {
				numOfsub = notation.split(":").length;
			}
			
			String temp = mapSubs.get(key).replaceAll("_", ",") + key;
			if (numOfsub > 1) temp = temp + numOfsub;
			
			ret.add(temp);
		}
				
		Collections.sort(ret);
		
		return ret;
	}
	
	private void extractSubstituentWithPosition (Substituent _sub) {
		StringBuilder sbPos = new StringBuilder();
		Substituent sub = _sub;
		boolean haveSecond = haveSecondPos(sub);

		if (sub.getFirstPosition() == null) return;

		/* append first position */
		sbPos.append(makePosition(sub.getFirstPosition(), haveSecond));

		/* append second position */
		if (sub.getSecondPosition() != null) {
			if (sub.getSecondPosition().getChildLinkages().isEmpty())
				sbPos.append("_" + makePosition(sub.getSecondPosition(), haveSecond));
			else
				sbPos.append("," + makePosition(sub.getSecondPosition(), haveSecond));
		}

		if (mapSubs.containsKey(sub.getNameWithIUPAC())) {
			StringBuilder temp = new StringBuilder(mapSubs.get(sub.getNameWithIUPAC()));
			if (comparePosition(sbPos.toString(), temp.toString())) {
				if (sub.getFirstPosition().getParentLinkages().size() > 1) temp.append(":" + sbPos);
				else {
					if (temp.indexOf(":") != -1 || temp.indexOf("-") != -1 || sbPos.indexOf(":") != -1 || sbPos.indexOf("-") != -1) temp.append(":" + sbPos);
					else temp.append("," + sbPos);
				}
		
				sbPos = temp;
			} else {
				if (sub.getFirstPosition().getParentLinkages().size() > 1) sbPos.append(":" + sbPos);
				else {
					if (temp.indexOf(":") != -1 || temp.indexOf("-") != -1 || sbPos.indexOf(":") != -1 || sbPos.indexOf("-") != -1) sbPos.append(":" + temp);
					else sbPos.append("," + temp);
				}
			}
		}
		
		mapSubs.put(sub.getSubstituent().getIUPACnotation(), sbPos.toString());
	}
	
	private void extractAnhydroxylSubstituent(Substituent _sub) {
		if (prefixSubs.length() != 0) prefixSubs.append(":");
		prefixSubs.append(_sub.getFirstPosition().getParentLinkages().get(0));
		prefixSubs.append(",");
		prefixSubs.append(_sub.getSecondPosition().getParentLinkages().get(0));
	}
	
	private boolean comparePosition(String onePos, String twoPos) {
		String one = onePos.substring(0, 1);
		String two = twoPos.substring(0, 1);
		int intone = one.equals("?") ? -1 : Integer.parseInt(one);
		int inttwo = two.equals("?") ? -1 : Integer.parseInt(two);

		if (intone > inttwo) return true;		
		return false;
	}
	
	private boolean haveAnhydroxyl (SubstituentInterface _subface) {
		if (!(_subface instanceof CrossLinkedTemplate)) return false;		
		return ((CrossLinkedTemplate) _subface).equals(CrossLinkedTemplate.ANHYDROXYL);
	}
	
	private boolean haveNativeSubstituent(String _code, Substituent _sub, Node _current) {
		if (_sub.getSubstituent() instanceof CrossLinkedTemplate) return false;

		SubstituentTemplate subT = (SubstituentTemplate) _sub.getSubstituent();
		
		if (!isHexoseWithNativeSubstituent(_code)) return false;
		if (_sub.getFirstPosition().getParentLinkages().size() > 1 ||
				_sub.getSecondPosition() != null) return false;

		Integer firstPosition = _sub.getFirstPosition().getParentLinkages().get(0);
		
		if (firstPosition == 2 && !isAcidicTail(_current)) {
			/* for N-acetyl hexosamine */
			if (subT.equals(SubstituentTemplate.N_ACETYL)) return true;
			
			/* for hexosamine */
			if (subT.equals(SubstituentTemplate.AMINO) && !is6DeoxyHexose(_code)) return true;
			if (subT.equals(SubstituentTemplate.AMINE) && !is6DeoxyHexose(_code)) return true;
			
			/* for hexosamine from NS */
			//if(subT.equals(SubstituentTemplate.N_SULFATE) && !is6DeoxyHexose(_code)) return true;
		}
		
		return false;
	}
	
	private boolean haveNativeSubstituentInNeu(String _code, Substituent _sub, Node _current) {
		if (_sub.getSubstituent() instanceof CrossLinkedTemplate) return false;

		SubstituentTemplate subT = (SubstituentTemplate) _sub.getSubstituent();
		
		if (!isHexoseWithNativeSubstituent(_code)) return false;
		if (_sub.getFirstPosition().getParentLinkages().size() > 1 ||
				_sub.getSecondPosition() != null) return false;
		
		Integer firstPosition = _sub.getFirstPosition().getParentLinkages().get(0);
	
		/* for neugc or neuac */
		if (firstPosition == 5 && isNeuraminicAcid(_code)) {
			if (subT.equals(SubstituentTemplate.ACETYL)) return true;
			if (subT.equals(SubstituentTemplate.GLYCOLYL)) return true;
		}
		
		return false;
	}
	
	private boolean isNeuraminicAcid (String _code) {
		TrivialNameDictionary dict = TrivialNameDictionary.forThreeLetterCode(_code);
		if (dict == null) return false;
		if (dict.equals(TrivialNameDictionary.NEU)) return true;
		
		return false;
	}
	
	protected boolean haveNativeSubstituentWithNsulfate(String _code, Substituent _sub, Node _current) {
		if (_sub.getSubstituent() instanceof CrossLinkedTemplate) return false;
		
		SubstituentTemplate subT = (SubstituentTemplate) _sub.getSubstituent();
		
		if (_sub.getFirstPosition().getParentLinkages().size() > 1) return false;

		Integer firstPosition = _sub.getFirstPosition().getParentLinkages().get(0);
		
		if (firstPosition == 2 && !isAcidicTail(_current)) {
			/* for hexosamine from NS */
			if (subT.equals(SubstituentTemplate.N_SULFATE) && !is6DeoxyHexose(_code)) return true;
		}
		
		return false;
	}
	
	protected boolean isHexoseWithNativeSubstituent(String _code) {
		HexoseDescriptor enumHex = HexoseDescriptor.forTrivialName(_code);
		
		return (enumHex != null);
	}
	
	protected boolean is6DeoxyHexose(String _code) {
		HexoseDescriptor enumHex = HexoseDescriptor.forTrivialName(_code);
		
		if (enumHex == null) return false;
		
		if (enumHex.equals(HexoseDescriptor.FUC)) return true;
		if (enumHex.equals(HexoseDescriptor.RHA)) return true;
		if (enumHex.equals(HexoseDescriptor.QUI)) return true;
		
		return false;
	}
	
	private boolean isAcidicTail (Node _node) {
		Monosaccharide mono = (Monosaccharide) _node;
	
		boolean tailAcid = false;
		ModificationTemplate acid = ModificationTemplate.URONICACID;//.ALDONICACID;
		
		for (GlyCoModification mod : mono.getModifications()) {
			ModificationTemplate modT = mod.getModificationTemplate();
			tailAcid = (mod.getPositionOne() == mono.getSuperClass().getSize() && modT.equals(acid));
		}
		
		return tailAcid;
	}	

	private boolean haveSecondPos (Substituent _sub) {
		if (_sub.getSecondPosition() != null) return true;
		return false;
	}

	private void init() {
		prefixSubs = new StringBuilder();
		surfixCore = new StringBuilder();
		surfixSubs = new StringBuilder();
		mapSubs = new HashMap<String, String>();
	}
}
