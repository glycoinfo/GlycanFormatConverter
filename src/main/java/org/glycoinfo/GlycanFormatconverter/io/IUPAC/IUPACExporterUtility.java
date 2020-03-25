package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanRepeatModification;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class IUPACExporterUtility {

	protected String extractPosition (Edge _edge) {
		return extractPosition(_edge.getGlycosidicLinkages().get(0).getParentLinkages());
	}

	protected String extractPosition (ArrayList<Integer> _positions) {
		StringBuilder ret = new StringBuilder();
		
		for(Iterator<Integer> iterPos = _positions.iterator(); iterPos.hasNext();) {
			Integer elm = iterPos.next();
			ret.append(elm == -1 ? "?" : elm);
			if(iterPos.hasNext()) ret.append("/");
		}
		
		return ret.toString();		
	}
	
	protected boolean isFacingAnom(Edge _edge) {
		if(_edge.getGlycosidicLinkages().size() > 1) return false;
		if(_edge.getGlycosidicLinkages().get(0).getParentLinkages().size() > 1 || 
				_edge.getGlycosidicLinkages().get(0).getChildLinkages().size() > 1) return false;
		if(_edge.getChild() == null || _edge.getParent() == null) return false;

		int childSide = _edge.getGlycosidicLinkages().get(0).getChildLinkages().get(0);
		int parentSide = _edge.getGlycosidicLinkages().get(0).getParentLinkages().get(0);
		int childAnom = ((Monosaccharide) _edge.getChild()).getAnomericPosition();
		int parentAnom = ((Monosaccharide) _edge.getParent()).getAnomericPosition();

		if (childSide == -1 || parentSide == -1 || childAnom == -1 || parentAnom == -1) return false;

		return (childAnom == childSide && parentAnom == parentSide);
	}

	protected boolean isFacingAnoms(ArrayList<Edge> _edges) {
		for(Edge _edge : _edges) {
			if(_edge.getGlycosidicLinkages().isEmpty()) continue;
			if(isFacingAnom(_edge)) return true;
		}
		
		return false;
	}	
	
	protected String makeRepeatingCount(GlycanRepeatModification _repmod) {
		StringBuilder ret = new StringBuilder();
		if(_repmod.getMinRepeatCount() == -1 && _repmod.getMaxRepeatCount() == -1) ret.append("n");
		if(_repmod.getMinRepeatCount() != -1 && _repmod.getMaxRepeatCount() == -1) {
			ret.append(_repmod.getMinRepeatCount());
			ret.append("-");
			ret.append("n");
		}
		if(_repmod.getMinRepeatCount() == -1 && _repmod.getMaxRepeatCount() != -1) {
			ret.append("n");
			ret.append("-");
			ret.append(_repmod.getMaxRepeatCount());
		}
		if(_repmod.getMinRepeatCount() != -1 && _repmod.getMaxRepeatCount() != -1) {
			ret.append(_repmod.getMinRepeatCount());
			ret.append("-");
			ret.append(_repmod.getMaxRepeatCount());
		}
		if(_repmod.getMinRepeatCount() == 0 && _repmod.getMaxRepeatCount() == 0) ret = new StringBuilder();
	
		return ret.toString();
	}

	protected boolean isCyclicLinkage(ArrayList<Edge> _edges) {
		if(_edges.size() < 1) return false;
		
		Node node = null;
		for(Edge edge : _edges) {
			if(edge.getSubstituent() != null && edge.getChild() == null) continue;
			if(node == null) {
				node = edge.getChild();
				continue;
			}
			if(edge.getChild().equals(node)) return true;	
		}
		
		return false;
	}

	public String makeLinkagePosition (ArrayList<Integer> _positions) {
		StringBuilder ret = new StringBuilder();

		for(Iterator<Integer> iterPos = _positions.iterator(); iterPos.hasNext();) {
			Integer elm = iterPos.next();
			ret.append(elm == -1 ? "?" : elm);
			if(iterPos.hasNext()) ret.append("/");
		}

		return ret.toString();
	}

	public String makeProbabilityAnnotation (Edge _parentEdge) {
		String ret = "";
		int probabilityLow = (int) (_parentEdge.getGlycosidicLinkages().get(0).getParentProbabilityLower() * 100);
		int probabilityUp = (int) (_parentEdge.getGlycosidicLinkages().get(0).getParentProbabilityUpper() * 100);

		if (probabilityLow != 100) {
			if (probabilityLow == -100) ret += "?";
			else ret += probabilityLow;
		}

		if ((probabilityLow != probabilityUp) && probabilityUp != 100) {
			ret += ",";
			if (probabilityUp == -100) ret += "?";
			else ret += probabilityUp;
		}

		if (ret.length() != 0) ret +="%";

		return ret;
	}

}
