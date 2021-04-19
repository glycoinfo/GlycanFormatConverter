package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.CoreSubstituentMonosaccharide;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class IUPACSubstituentNotationConverter {

	private final StringBuilder prefixSubs;
	private final StringBuilder surfixSubs;
	private final StringBuilder surfixCore;
	private final HashMap<String, String> mapSubs = new HashMap<>();

	public IUPACSubstituentNotationConverter() {
		prefixSubs = new StringBuilder();
		surfixCore = new StringBuilder();
		surfixSubs = new StringBuilder();
	}

	public HashMap<String, String> getMapSubs () {
		return this.mapSubs;
	}

	public String getCoreSubstituentNotaiton () {
		return surfixCore.toString();
	}
	
	public String getSubstituentNotation() {
		return surfixSubs.toString();
	}
	
	public String getPrefixSubstituent() {
		return prefixSubs.toString();
	}
	
	public void start(String _code, Node _node) throws GlycanException {
		StringBuilder nativeSub = new StringBuilder();

		for (Edge child : _node.getChildEdges()) {
			if (child.getSubstituent() == null) continue;
			
			Substituent sub = (Substituent) child.getSubstituent();

			if (sub.getSubstituent() == null || sub instanceof GlycanRepeatModification || child.getChild() != null) continue;

			// extract only N (Amino group) part from N-linked substituent
			if (haveNativeSubstituentWithNsulfate(_code, sub, _node)) {
				nativeSub.append(BaseSubstituentTemplate.AMINE.getIUPACnotation());

				//make plane substituent
				SubstituentUtility.changePlaneTemplate(sub);
			}

			if (haveNativeSubstituentInNeu(_code, sub)) {
				nativeSub.append(makePosition(sub.getFirstPosition(), haveSecondPos(sub)));
				nativeSub.append(sub.getSubstituent().getIUPACnotation());
			} else if (haveNativeSubstituent(_code, sub, _node)) {
				nativeSub.append(sub.getSubstituent().getIUPACnotation());
			} else if (haveAnhydro(sub.getSubstituent())) {
				extractAnhydroxylSubstituent(sub);
			} else {
				extractSubstituentWithPosition(sub);
			}
		}
		
		//
		if (prefixSubs.length() != 0) {
			prefixSubs.append("-Anhydro-");
		}
		
		surfixCore.append(nativeSub);

		// extract unsaturated state
		extractUnsaturatedState(_node);

		//
		StringBuilder coreSub = new StringBuilder();
		for (String unit : concatSubstituents()) {
			coreSub.append(unit);
		}

		surfixSubs.append(coreSub);
	}

	private String makePosition (Linkage _linkage, boolean _haveSecond) {
		if (_linkage == null) return "";
		
		StringBuilder ret = new StringBuilder();

		// Append linkage position
		for (Iterator<Integer> iterPos = _linkage.getParentLinkages().iterator(); iterPos.hasNext();) {
			Integer pos = iterPos.next();
			ret.append(pos == -1 ? "?" : pos);
			if (iterPos.hasNext()) ret.append("/");
		}		

		if (_haveSecond) {
			if (_linkage.getChildLinkages().contains(0))
				ret.append("-" + 1);
			else {
				ret.append("-").append(_linkage.getChildLinkages().get(0));
			}
		}

		// Append probability annotation
		if (_linkage.getParentProbabilityLower() != 1.0D) {
			ret.append("(");
			if (_linkage.getParentProbabilityLower() == -1.0) {
				ret.append("?%");
			} else {
				ret.append((int) (_linkage.getParentProbabilityLower() * 100)).append("%");
			}
		}

		if (_linkage.getParentProbabilityUpper() != 1.0D) {
			if ((_linkage.getParentProbabilityUpper() != _linkage.getParentProbabilityLower())) {
				if (ret.length() != 0) ret.append(",");
				if (_linkage.getParentProbabilityUpper() == -1.0) {
					ret.append("?%");
				} else {
					ret.append((int) (_linkage.getParentProbabilityUpper() * 100)).append("%");
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
			}
		}
	}

	private boolean isUnsaturate(ModificationTemplate _modT) {
		return _modT.equals(ModificationTemplate.UNSATURATION_EL) ||
				_modT.equals(ModificationTemplate.UNSATURATION_FL) ||
				_modT.equals(ModificationTemplate.UNSATURATION_ZL) ||
				_modT.equals(ModificationTemplate.UNSATURATION_EU) ||
				_modT.equals(ModificationTemplate.UNSATURATION_FU) ||
				_modT.equals(ModificationTemplate.UNSATURATION_ZU);
	}
	
	private ArrayList<String> concatSubstituents() {
		ArrayList<String> ret = new ArrayList<>();
		
		for (String key : mapSubs.keySet()) {
			int numOfsub = 0;
			String notation = mapSubs.get(key);
			if (notation.contains(",") && !notation.contains("-")) {
				numOfsub = notation.split(",").length;
			}
			if (notation.contains(":")) {
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
		boolean haveSecond = haveSecondPos(_sub);

		if (_sub.getFirstPosition() == null) return;

		String subNotation = SubstituentUtility.optimizeSubstituentNotationWithLinkageType(_sub);

		// append first position
		sbPos.append(makePosition(_sub.getFirstPosition(), haveSecond));

		// append second position
		if (_sub.getSecondPosition() != null) {
			if (_sub.getSecondPosition().getChildLinkages().isEmpty())
				sbPos.append("_").append(makePosition(_sub.getSecondPosition(), haveSecond));
			else
				sbPos.append(",").append(makePosition(_sub.getSecondPosition(), haveSecond));
		}

		if (mapSubs.containsKey(subNotation)) {
			StringBuilder temp = new StringBuilder(mapSubs.get(subNotation));
			if (comparePosition(sbPos.toString(), temp.toString())) {
				if (_sub.getFirstPosition().getParentLinkages().size() > 1) temp.append(":").append(sbPos);
				else {
					if (temp.indexOf(":") != -1 || temp.indexOf("-") != -1 || sbPos.indexOf(":") != -1 || sbPos.indexOf("-") != -1) temp.append(":").append(sbPos);
					else temp.append(",").append(sbPos);
				}
		
				sbPos = temp;
			} else {
				if (_sub.getFirstPosition().getParentLinkages().size() > 1) sbPos.append(":").append(sbPos);
				else {
					if (temp.indexOf(":") != -1 || temp.indexOf("-") != -1 || sbPos.indexOf(":") != -1 || sbPos.indexOf("-") != -1) sbPos.append(":").append(temp);
					else sbPos.append(",").append(temp);
				}
			}
		}

		mapSubs.put(subNotation, sbPos.toString());
	}
	
	private void extractAnhydroxylSubstituent(Substituent _sub) {
		if (prefixSubs.length() != 0) prefixSubs.append(":");
		prefixSubs.append(_sub.getFirstPosition().getParentLinkages().get(0));
		prefixSubs.append(",");
		prefixSubs.append(_sub.getSecondPosition().getParentLinkages().get(0));
	}
	
	private boolean comparePosition(String onePos, String twoPos) {
		String one = onePos.startsWith("-") ? onePos.substring(0, 2) : onePos.substring(0, 1);
		String two = twoPos.startsWith("-") ? twoPos.substring(0, 2) : twoPos.substring(0, 1);
		int intone = one.equals("?") ? -1 : Integer.parseInt(one);
		int inttwo = two.equals("?") ? -1 : Integer.parseInt(two);

		return intone > inttwo;
	}
	
	private boolean haveAnhydro (SubstituentInterface _subface) {
		if (!(_subface instanceof BaseCrossLinkedTemplate)) return false;
		return (_subface).equals(BaseCrossLinkedTemplate.ANHYDRO);
	}
	
	private boolean haveNativeSubstituent(String _code, Substituent _sub, Node _node) {
		if (_sub.getSubstituent() instanceof BaseCrossLinkedTemplate) return false;

		BaseSubstituentTemplate subT = (BaseSubstituentTemplate) _sub.getSubstituent();

		if (checkNotHavingNativeSubstituent(_code)) return false;
		if (_sub.getFirstPosition().getParentLinkages().size() > 1 || _sub.getSecondPosition() != null) return false;

		Integer firstPosition = _sub.getFirstPosition().getParentLinkages().get(0);

		if (firstPosition != 2) return false;
		if (isAcidicTail(_node)) return false;

		// for N-acetyl hexosamine
		if (subT.equals(BaseSubstituentTemplate.NACETYL)) return true;

		//if (is6DeoxyHexose(_code)) return false;

		// for hexosamine
		return (_sub.getHeadAtom().equals("N"));
	}
	
	private boolean haveNativeSubstituentInNeu(String _code, Substituent _sub) {
		if (_sub.getSubstituent() instanceof BaseCrossLinkedTemplate) return false;
		if (_sub.getFirstPosition().getParentLinkages().size() > 1 || _sub.getSecondPosition() != null) return false;
		if (!isNeuraminicAcid(_code)) return false;
		if (checkNotHavingNativeSubstituent(_code)) return false;

		Integer firstPosition = _sub.getFirstPosition().getParentLinkages().get(0);

		if (firstPosition != 5) return false;

		BaseSubstituentTemplate subT = (BaseSubstituentTemplate) _sub.getSubstituent();

		// for neugc or neuac
		return (subT.equals(BaseSubstituentTemplate.OACETYL) || subT.equals(BaseSubstituentTemplate.OGLYCOLYL));
	}
	
	private boolean isNeuraminicAcid (String _code) {
		TrivialNameDictionary dict = TrivialNameDictionary.forThreeLetterCode(_code);
		if (dict == null) return false;
		return dict.equals(TrivialNameDictionary.NEU);
	}
	
	protected boolean haveNativeSubstituentWithNsulfate(String _code, Substituent _sub, Node _current) {
		if (_sub.getSubstituent() instanceof BaseCrossLinkedTemplate) return false;
		if (_sub.getFirstPosition().getParentLinkages().size() > 1) return false;

		if (checkNotHavingNativeSubstituent(_code)) return false;

		Integer firstPosition = _sub.getFirstPosition().getParentLinkages().get(0);

		if (firstPosition != 2) return false;
		if (is6DeoxyHexose(_code) || isAcidicTail(_current)) return false;

		if (_sub.getSubstituent().equals(BaseSubstituentTemplate.NACETYL)) return false;

		return (SubstituentUtility.isNLinkedSubstituent(_sub));
	}
	
	protected boolean checkNotHavingNativeSubstituent(String _code) {
		CoreSubstituentMonosaccharide enumHex = CoreSubstituentMonosaccharide.forTrivialName(_code);
		return enumHex == null;
	}
	
	protected boolean is6DeoxyHexose(String _code) {
		CoreSubstituentMonosaccharide enumHex = CoreSubstituentMonosaccharide.forTrivialName(_code);
		
		if (enumHex == null) return false;
		return (enumHex.equals(CoreSubstituentMonosaccharide.FUC) ||
				enumHex.equals(CoreSubstituentMonosaccharide.RHA) ||
				enumHex.equals(CoreSubstituentMonosaccharide.QUI));
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
		return _sub.getSecondPosition() != null;
	}
}
