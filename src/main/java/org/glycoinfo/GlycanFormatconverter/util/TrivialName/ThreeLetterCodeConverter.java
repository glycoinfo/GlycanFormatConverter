package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class ThreeLetterCodeConverter {

	private String threeCodes = "";
	private int size = -1;
	private TrivialNameDictionary trivial;
	
	public String getThreeLetterCode() {
		return threeCodes;
	}
	
	public int getSize() {
		return size;
	}
	
	public TrivialNameDictionary getDictionary() {
		return this.trivial;
	}
	
	public void start(Node _node) throws GlycanException{
		if (((Monosaccharide) _node).getStereos().isEmpty()) return;

		// extract trivial name list
		String stereo = makeNotation(_node, false);

		// check modification and substituent
		TrivialNameDictionary trivial = null;
		for(TrivialNameDictionary dict : TrivialNameDictionary.forStereos(stereo)) {
			if(checkSubstituentAndModifications(_node, dict)) trivial = dict;
		}

		if (trivial == null) {
			stereo = makeNotation(_node, true);
			for(TrivialNameDictionary dict : TrivialNameDictionary.forStereos(stereo)) {
				if(checkSubstituentAndModifications(_node, dict)) trivial = dict;
			}
		}

		// check configuration for di-deoxy hexose
		trivial = checkDideoxyHexose(_node, trivial);
		
		// remove (or modified) substituents and modifications
		if(trivial != null) {
			modifySubstituentAndModification(_node, trivial);
			threeCodes = trivial.getThreeLetterCode();
			size = trivial.getSize();
		}		
		
		this.trivial = trivial;
	}
	
	private String makeNotation (Node _node, boolean _config) {
		String stereo = "";
		
		for(Iterator<String> iterStereo = ((Monosaccharide) _node).getStereos().iterator(); iterStereo.hasNext();) {
			String s = iterStereo.next();
			stereo += (_config) ? s.replaceFirst(trimConfiguration(s), "") : s;
			if(iterStereo.hasNext()) stereo += "_";
		}
		
		return stereo;
	}
	
	private TrivialNameDictionary checkDideoxyHexose(Node _node, TrivialNameDictionary _dict) {
		if(_dict == null) return _dict;
		String configuration = this.trimConfiguration(((Monosaccharide) _node).getStereos().get(0));
		
		if((_dict.equals(TrivialNameDictionary.ABE) || _dict.equals(TrivialNameDictionary.COL)) && configuration.equals("?")) {
			return null;
		}
		if((_dict.equals(TrivialNameDictionary.ASC) || _dict.equals(TrivialNameDictionary.TYV)) && configuration.equals("?")) {
			return null;
		}
		if(_dict.equals(TrivialNameDictionary.ABE) && configuration.equals("l")) {
			return TrivialNameDictionary.COL;
		}
		if(_dict.equals(TrivialNameDictionary.COL) && configuration.equals("d")) {
			return TrivialNameDictionary.ABE;
		}
		if(_dict.equals(TrivialNameDictionary.TYV) && configuration.equals("l")) {
			return TrivialNameDictionary.ASC;
		}
		if(_dict.equals(TrivialNameDictionary.ASC) && configuration.equals("d")) {
			return TrivialNameDictionary.TYV;
		}
		
		return _dict;
	}
	
	private boolean checkSubstituentAndModifications (Node _node, TrivialNameDictionary _dict) throws GlycanException{
		if (((Monosaccharide) _node).getSuperClass() != null) {
			if (_dict.getSize() != ((Monosaccharide) _node).getSuperClass().getSize()) return false;			
		}

		// extract modifications
		ArrayList<GlyCoModification> mods = extractModifications(_dict.getModifications());

		// extract substituents
		ArrayList<Substituent> subs = extractSubstituents(_dict.getSubstituents());

		// compare substituents
		int subPoint = 0;
		for(Edge childEdge : _node.getChildEdges()) {
			if(childEdge.getSubstituent() == null) continue;

			Substituent sub = (Substituent) childEdge.getSubstituent();

			//ignore cross linked substituent
			if (sub.getSubstituent() instanceof BaseCrossLinkedTemplate) continue;
//			if(childEdge.getSubstituent() != null && childEdge.getChild() != null) continue;
			//ignore wrong substituent
			if(sub.getFirstPosition() == null || sub.getSecondPosition() != null) continue;
			//ignore ambiguous linkage position
			if(sub.getFirstPosition().getParentLinkages().size() > 1) continue;
			//
			if (isProbability(sub)) continue;

			for(Substituent tempSub : subs) {
				if(!sub.getFirstPosition().getParentLinkages().contains(tempSub.getFirstPosition().getParentLinkages().get(0))) continue;
				if(SubstituentUtility.isNLinkedSubstituent(sub) && tempSub.getSubstituent().equals(BaseSubstituentTemplate.AMINE)) {
					subPoint++;
					continue;
				}

				if(tempSub.getSubstituent().equals(sub.getSubstituent())) subPoint++;
			}
		}

		// compare modificaitons
		int modPoint = 0;
		for(GlyCoModification tempMod : mods) {
			if(((Monosaccharide) _node).hasModification(tempMod, tempMod.getPositionOne())) modPoint++;
		}

		if(subs.size() == subPoint && mods.size() == modPoint) return true;
		return false;
	}
	
	private void modifySubstituentAndModification (Node _node, TrivialNameDictionary _dict) throws GlycanException{
		Monosaccharide mono = ((Monosaccharide) _node);
		
		// extract modifications
		ArrayList<GlyCoModification> mods = extractModifications(_dict.getModifications());
		
		// extract substituents
		ArrayList<Substituent> subs = extractSubstituents(_dict.getSubstituents());
		
		// modify substituent
		for(Edge childEdge : _node.getChildEdges()) {
			if(childEdge.getSubstituent() == null) continue;
			//ignore cross linked substituent 
			if(childEdge.getSubstituent() != null && childEdge.getChild() != null) continue;

			Substituent sub = (Substituent) childEdge.getSubstituent();
			//ignore wrong substituent
			if(sub.getFirstPosition() == null || sub.getSecondPosition() != null) continue;
			//ignore ambiguous linkage position
			if(sub.getFirstPosition().getParentLinkages().size() > 1) continue;
			
			for(Substituent tempSub : subs) {
				if(!sub.getFirstPosition().getParentLinkages().contains(tempSub.getFirstPosition().getParentLinkages().get(0))) continue;
				if(SubstituentUtility.isNLinkedSubstituent(sub)) {
					SubstituentUtility.changePlaneTemplate(sub);
					break;
				}
				if(tempSub.getSubstituent().equals(sub.getSubstituent())) {
					sub.setTemplate(null);
					break;
				}
			}
		}
		
		// remove modification
		for(GlyCoModification tempMod : mods) {
			mono.removeModification(getModIndex(_node, tempMod));
		}
	}
	
	private GlyCoModification getModIndex(Node _node, GlyCoModification _ind) {
		GlyCoModification ret = null;
		
		for (GlyCoModification mod : ((Monosaccharide) _node).getModifications()) {
			if(mod.getModificationTemplate().equals(_ind.getModificationTemplate()) &&
					mod.getPositionOne() == _ind.getPositionOne()) ret = mod;
		}
		
		return ret;
	}
	
	private ArrayList<Substituent> extractSubstituents (String _item) {
		ArrayList<Substituent> ret = new ArrayList<Substituent>();
		if(_item.equals("")) return ret;

		for(String unit : _item.split("_")) {
			String[] split_unit = unit.split("\\*");
			LinkedList<Integer> pos = new LinkedList<Integer>();
			pos.addLast(Integer.parseInt(String.valueOf(split_unit[0])));
			Linkage lin = new Linkage();
			lin.setParentLinkages(pos);
			
			BaseSubstituentTemplate bsubT = BaseSubstituentTemplate.forIUPACNotation(makePlaneNotation(split_unit[1]));
			if(bsubT != null) ret.add(new Substituent(bsubT, lin));
		}

		return ret;
	}
	
	private ArrayList<GlyCoModification> extractModifications (String _item) throws GlycanException {
		ArrayList<GlyCoModification> ret = new ArrayList<>();
		if(_item.equals("")) return ret;

		for(String unit : _item.split("_")) {
			if(unit.equals("")) continue;
			String[] split_unit = unit.split("\\*");
			ModificationTemplate modT = ModificationTemplate.forCarbon(split_unit[1].charAt(0));

			if(modT != null) {
				if(modT.equals(ModificationTemplate.ULOSONIC)) modT = ModificationTemplate.KETONE_U;
				if (_item.startsWith("1*A") && !split_unit[0].equals("1") && modT.equals(ModificationTemplate.ALDONICACID)) {
					modT = ModificationTemplate.URONICACID;
				}
				ret.add(new GlyCoModification(modT, Integer.parseInt(String.valueOf(split_unit[0]))));
			}
		}
		
		return ret;
	}

	private String trimConfiguration(String _stereo) {
		if(_stereo.length() == 3) return "";
		if(_stereo.length() == 4) return _stereo.substring(0, 1);
		else if(_stereo.indexOf("-") != -1) return _stereo.substring(0, _stereo.indexOf("-") + 1);
		return "";
	}

	private String makePlaneNotation (String _notation) {
		if (_notation.startsWith("O") || _notation.startsWith("C"))
			return (_notation.substring(1, _notation.length()));

		if (_notation.startsWith("(")) {
			String bracket = _notation.substring(0, _notation.indexOf(")")+1);
			String regex = bracket.replace("(", "\\(").replace(")", "\\)");
			_notation = _notation.replaceFirst(regex, "");

			if (_notation.startsWith("O")) {
				_notation = _notation.replaceFirst("O", "");
			}
			if (_notation.startsWith("C") && _notation.length() == 3) {
				_notation = _notation.substring(1, _notation.length());
			}

			_notation = bracket + _notation;
		}

		return _notation;
	}

	private boolean isProbability (Substituent _sub) {
		return (Double.compare(_sub.getFirstPosition().getChildProbabilityLower(), 1.0) != 0 ||
				Double.compare(_sub.getFirstPosition().getChildProbabilityUpper(), 1.0) != 0 ||
				Double.compare(_sub.getFirstPosition().getParentProbabilityLower(), 1.0) != 0 ||
				Double.compare(_sub.getFirstPosition().getParentProbabilityUpper(), 1.0) != 0);
	}
}