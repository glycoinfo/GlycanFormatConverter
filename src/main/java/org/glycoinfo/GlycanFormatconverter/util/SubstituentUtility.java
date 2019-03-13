package org.glycoinfo.GlycanFormatconverter.util;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.MAPAnalyzer;
import org.glycoinfo.WURCSFramework.wurcs.graph.Modification;

public class SubstituentUtility {

	public static boolean isNLinkedSubstituent(Substituent _sub) {
		if (_sub.getSubstituent() instanceof CrossLinkedTemplate) return false;

		BaseSubstituentTemplate bsubT = (BaseSubstituentTemplate) _sub.getSubstituent();

		if (bsubT.getIUPACnotation().equals("N")) return false;

		return (bsubT.getIUPACnotation().startsWith("N"));
	}

	public static boolean isOLinkedSubstituent (Substituent _sub) {
		SubstituentInterface subFace = (SubstituentInterface) _sub;
		if (subFace instanceof CrossLinkedTemplate) {
			return isOLinkedSubstituent((CrossLinkedTemplate) subFace);
		}
		if (subFace instanceof BaseSubstituentTemplate) {
			return (_sub.getHeadAtom().equals("O"));
		}

		return false;
	}

	public static boolean isOLinkedSubstituent (CrossLinkedTemplate _crossT) {
		if (_crossT.equals(CrossLinkedTemplate.PHOSPHOETHANOLAMINE)) return true;
		return (_crossT.getMAP().startsWith("*O") ||  _crossT.getMAP().startsWith("*1O"));
	}

	public static Substituent MAPToSubstituent(Modification _mod) throws GlycanException {
		if(_mod.getMAPCode().equals("")) return null;

		return new Substituent(MAPToInterface(_mod.getMAPCode()));
	}

	public static SubstituentInterface MAPToInterface (String _map) throws GlycanException {
		if(_map.equals("")) return null;
		SubstituentInterface ret = null;

		MAPAnalyzer mapAnalyze = new MAPAnalyzer();
		mapAnalyze.start(_map);

		if(mapAnalyze.getSingleTemplate() != null) {
			ret = mapAnalyze.getSingleTemplate();
		}
		if(CrossLinkedTemplate.forMAP(_map) != null) {
			ret = CrossLinkedTemplate.forMAP(_map);
		}

		if(ret == null) throw new GlycanException(_map +" could not found !");
		return ret;
	}

	public static String optimizeSubstituentNotationWithLinkageType (Substituent _sub) {
		String ret = _sub.getNameWithIUPAC();

		if (_sub.getFirstPosition() == null) return ret;

		// Optimize substituent notation using H_LOSE
		if (_sub.getFirstPosition().getParentLinkageType().equals(LinkageType.H_LOSE)) {
			if (ret.startsWith("O")) ret = ret.replaceFirst("O", "C");
			if (!ret.startsWith("O") && !ret.startsWith("C")) ret = "C" + ret;
			//System.out.println("H_LOSE" + " " + ret);
		}

		// Optimize substituent notation using DEOXY
		if (_sub.getFirstPosition().getParentLinkageType().equals(LinkageType.DEOXY)) {
			if (ret.startsWith("O")) ret = ret.replaceFirst("O", "");
			if (ret.startsWith("C")) ret = ret.replaceFirst("C", "");
			//System.out.println("DEOXY " + ret);
		}

		// Optimize substituent notation using H_AT_OH
		if (_sub.getFirstPosition().getParentLinkageType().equals(LinkageType.H_AT_OH)) {
			if (ret.startsWith("C")) ret = ret.replaceFirst("C", "O");
			if (!ret.startsWith("C") && !ret.startsWith("O")) ret = "O" + ret;
			//System.out.println("H_AT_OH " + ret);
		}

		return ret;
	}

	public static String optimizeSubstituentNotationWithN_linkage (Substituent _sub) {
		String ret = _sub.getNameWithIUPAC();

		// Optimize substituent notation using N-linked
		if (SubstituentUtility.isNLinkedSubstituent(_sub)) {
			ret = ret.replaceFirst("N", "");
		}
		return ret;
	}

	public static Substituent changePlaneTemplate (Substituent _sub) {
		String plane = optimizeSubstituentNotationWithN_linkage(_sub);
		BaseSubstituentTemplate bsubT = BaseSubstituentTemplate.forIUPACNotationWithIgnore(plane);
		_sub.setTemplate(bsubT);

		return _sub;
	}
}
