package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Linkage;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyCoModification;
import org.glycoinfo.GlycanFormatconverter.Glycan.ModificationTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.GlycanFormatconverter.Glycan.SubstituentInterface;
import org.glycoinfo.GlycanFormatconverter.Glycan.SubstituentTemplate;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameDictionary;

public class ThreeLetterCodeConverter extends SubstituentUtility {

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

		/* extract trivial name list */
		String stereo = makeNotation(_node, false);
				
		/* check modification and substituent */
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
		
		/* check configuration for di-deoxy hexose */
		trivial = checkDideoxyHexose(_node, trivial);
		
		/* remove (or modified) substituents and modifications */
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
		
		/** extract modifications */
		ArrayList<GlyCoModification> mods = extractModifications(_dict.getModificationNotation());
		
		/** extract substituents*/
		ArrayList<Substituent> subs = extractSubstituents(_dict.getSubstituentNotation());
		
		/** compare substituents */
		int subPoint = 0;
		for(Edge childEdge : _node.getChildEdges()) {
			if(childEdge.getSubstituent() == null) continue;
			//ignore cross linked substituent 
			if(childEdge.getSubstituent() != null && childEdge.getChild() != null) continue;
			Substituent sub = (Substituent) childEdge.getSubstituent();
			//ignore wrong substituent
			if(sub.getFirstPosition() == null || sub.getSecondPosition() != null) continue;
			//ignore ambiguous linkage position
			if(sub.getFirstPosition().getParentLinkages().size() > 1) continue;
			//
			if (Double.compare(sub.getFirstPosition().getChildProbabilityLower(), 1.0) != 0 ||
					Double.compare(sub.getFirstPosition().getChildProbabilityUpper(), 1.0) != 0 ||
					Double.compare(sub.getFirstPosition().getParentProbabilityLower(), 1.0) != 0 ||
					Double.compare(sub.getFirstPosition().getParentProbabilityUpper(), 1.0) != 0) continue;

			for(Substituent tempSub : subs) {
				if(!sub.getFirstPosition().getParentLinkages().contains(tempSub.getFirstPosition().getParentLinkages().get(0))) continue;
				if(isNLinkedSubstituent(sub.getSubstituent()) && tempSub.getSubstituent().equals(SubstituentTemplate.AMINE)) {
					subPoint++;
					continue;
				}
				if(tempSub.getSubstituent().equals(sub.getSubstituent())) subPoint++;
			}
		}
		
		/** compare modificaitons */
		int modPoint = 0;
		for(GlyCoModification tempMod : mods) {
			if(((Monosaccharide) _node).hasModification(tempMod, tempMod.getPositionOne())) modPoint++;
		}
		
		if(subs.size() == subPoint && mods.size() == modPoint) return true;
		return false;
	}
	
	private void modifySubstituentAndModification (Node _node, TrivialNameDictionary _dict) throws GlycanException{
		Monosaccharide mono = ((Monosaccharide) _node);
		
		/** extract modifications */
		ArrayList<GlyCoModification> mods = extractModifications(_dict.getModificationNotation());
		
		/** extract substituents*/
		ArrayList<Substituent> subs = extractSubstituents(_dict.getSubstituentNotation());
		
		/** modify substituent */
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
				if(isNLinkedSubstituent(sub.getSubstituent())) {
					sub.setTemplate(convertNTypeToOType(sub.getSubstituent()));
					break;
				}
				if(tempSub.getSubstituent().equals(sub.getSubstituent())) {
					sub.setTemplate(null);
					break;
				}
			}
		}
		
		/** remove modification */
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
			
			SubstituentInterface subT = SubstituentTemplate.forIUPACNotation(split_unit[1]);
			if(subT != null) ret.add(new Substituent(subT, lin));
		}
		
		return ret;
	}
	
	private ArrayList<GlyCoModification> extractModifications (String _item) throws GlycanException {
		ArrayList<GlyCoModification> ret = new ArrayList<GlyCoModification>();
		if(_item.equals("")) return ret;
		
		for(String unit : _item.split("_")) {
			if(unit.equals("")) continue;
			String[] split_unit = unit.split("\\*");
			ModificationTemplate modT = ModificationTemplate.forCarbon(split_unit[1].charAt(0));
			if(modT != null) {
				if(modT.equals(ModificationTemplate.ULOSONIC)) modT = ModificationTemplate.KETONE_U;
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
}