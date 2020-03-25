package org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.ExporterInterface;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExporterUtility;
import org.glycoinfo.GlycanFormatconverter.util.similarity.NodeSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class IUPACCondensedExporter extends IUPACExporterUtility implements ExporterInterface {

	private StringBuilder condensed = new StringBuilder();
	private HashMap<Node, String> notationIndex = new HashMap<>();
	private NodeSimilarity gu = new NodeSimilarity();
	private boolean isGlycanWeb;

	public IUPACCondensedExporter (boolean _isGlycanWeb) {
		this.isGlycanWeb = _isGlycanWeb;
	}

	public String getIUPACCondensed() {
		return condensed.toString();
	}

	public void start (GlyContainer _glyCo) throws GlycanException {
		for( Node _node : _glyCo.getAllNodes()) {
			// make core notations
			makeMonosaccharideNotation(_node);
			// append linkage position to core notation
			makeLinkageNotation(_node);
		}
		
		// for fragments of substituent
		for (GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
			if (und.getNodes().get(0) instanceof Substituent)
				makeSubstituentNotation(und);
		}

		makeFragmentsAnchor(_glyCo);

		if (_glyCo.isComposition()) {
			condensed.insert(0, makeComposition(_glyCo));
			return;
		}

		// sort core node
		ArrayList<Node> sortedList = gu.sortAllNode(_glyCo.getRootNodes().get(0));
		condensed.insert(0, makeSequence(sortedList));
		
		// sort fragments
		condensed.insert(0, makeFragmentsSequence(_glyCo.getUndefinedUnit()));
	}

	@Override
	public String makeComposition(GlyContainer _glyCo) {
		StringBuilder ret = new StringBuilder();
		
		ArrayList<String> nodeList = new ArrayList<>();
		for (GlycanUndefinedUnit glycanUndefinedUnit : _glyCo.getUndefinedUnit()) {
			nodeList.add(notationIndex.get(glycanUndefinedUnit.getNodes().get(0)));
		}
		
		LinkedHashMap<String, Integer> block = new LinkedHashMap<>();
		for (String notation : nodeList) {
			if (block.containsKey(notation)) {
				block.put(notation, block.get(notation)+1);
			} else {
				block.put(notation, 1);
			}
		}
		
		for (Iterator<String> iterKey = block.keySet().iterator(); iterKey.hasNext();) {
			String notation = iterKey.next();
			ret.append("{")
				.append(notation)
				.append("}")
				.append(block.get(notation));
			if (iterKey.hasNext()) ret.append(",");
		}
		
		return ret.toString();
	}

	public String makeFragmentsSequence (ArrayList<GlycanUndefinedUnit> _fragments) throws GlycanException {
		for(GlycanUndefinedUnit und : _fragments) {
			for(Node antennae : und.getRootNodes()) {
				ArrayList<Node> sortedFragments = gu.sortAllNode(antennae);
				condensed.insert(0, makeSequence(sortedFragments) + ",");
			}
		}
		return "";
	}

	public void makeFragmentsAnchor (GlyContainer _glyCo) throws GlycanException {
		if (_glyCo.isComposition()) return;
		
		for(GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
			int index = _glyCo.getUndefinedUnit().indexOf(und) + 1;
			for(Node antennae : und.getRootNodes()) {
				String notation = notationIndex.get(antennae);
				notation += ("=" + index + "$");
				notationIndex.put(antennae, notation);
			}
			
			for(Node parent : und.getParents()) {
				String notation = notationIndex.get(parent);
				notationIndex.put(parent, index + "$" + (index > 1 ? "|" : "") + notation);
			}
		}
	}
	
	public String makeSequence (ArrayList<Node> _nodes) {
		int branch = 0;
		StringBuilder encode = new StringBuilder();
		
		for(Node skey : _nodes) {
			StringBuilder notation = new StringBuilder(notationIndex.get(skey));
			
			if(gu.isMainChaineBranch(skey)) {
				notation.append("]");
				branch++;
			}
			if(gu.countChildren(skey) == 0 && branch > 0) {
				notation.insert(0, "[");
				branch--;
			}
			encode.insert(0, notation);
		}
		
		return encode.toString();
	}
	
	public void makeSubstituentNotation (GlycanUndefinedUnit _und) {
		Node sub = _und.getNodes().get(0);
		if(!(sub instanceof Substituent)) return;
		
		if (!notationIndex.containsKey(sub)) {
			String subNotation = ((Substituent) sub).getSubstituent().getIUPACnotation();
			subNotation = this.extractPosition(_und.getConnection().getGlycosidicLinkages().get(0).getParentLinkages()) + subNotation;
			notationIndex.put(sub, subNotation);
		}
	}
	
	public void makeMonosaccharideNotation (Node _node) throws GlycanException {
		CondensedConverter condConv = new CondensedConverter();
		if (!notationIndex.containsKey(_node)) notationIndex.put(_node, condConv.start(_node, isGlycanWeb));
	}
	
	public void makeLinkageNotation (Node _node) {
		StringBuilder notation = new StringBuilder(notationIndex.get(_node));
		Monosaccharide mono = (Monosaccharide) _node;

		if(!mono.getParentEdges().isEmpty()) {
			StringBuilder linkagePos = new StringBuilder("(");
			for(Iterator<Edge> iterParent = gu.sortParentSideEdges(mono.getParentEdges()).iterator(); iterParent.hasNext();) {
				Edge parentEdge = iterParent.next();
				Substituent sub = (Substituent) parentEdge.getSubstituent();

				if (parentEdge.isCyclic() && linkagePos.charAt(linkagePos.length() - 1) == ':') linkagePos.append("(");

				// append anomeric position
				linkagePos.append(makeDonorPosition(parentEdge));

				// append start repeating position
				if (sub != null) {
					if (sub instanceof GlycanRepeatModification && !parentEdge.isCyclic()) {
						linkagePos.append("]");
						linkagePos.append(makeRepeatingCount((GlycanRepeatModification) parentEdge.getSubstituent()));
					}
				}

				// append probability annotation
				linkagePos.append(makeProbabilityAnnotation(parentEdge));

				// append parent linkage position
				if (!parentEdge.isRepeat() && !parentEdge.isCyclic()) {
					linkagePos.append(makeAcceptorPosition(parentEdge));
					if(!iterParent.hasNext()) linkagePos.append(")");
				}

				// append a separator for dual linkage position
				if(iterParent.hasNext()) {
					linkagePos.append(":");
				}
			}

			if (this.isGlycanWeb) {
				linkagePos = linkagePos.replace(0, 1, "")
						.replace(linkagePos.length() - 1, linkagePos.length(), "");
			}

			notation.append(linkagePos);
		}
			
		// for root node
		if(mono.getParentEdges().isEmpty() && !mono.getAnomer().equals(AnomericStateDescriptor.OPEN) && !isFacingAnoms(mono.getChildEdges())) {
			if (!this.isGlycanWeb) notation.append("(");
			char parentAnom = mono.getAnomer().getAnomericState();
			notation.append(parentAnom == 'x' ? '?' : parentAnom == 'o' ? '?' : parentAnom);
			
			if (mono.getAnomericPosition() == Monosaccharide.OPEN_CHAIN || 
					mono.getAnomericPosition() == Monosaccharide.UNKNOWN_RING) {
				notation.append("?");
			} else notation.append(mono.getAnomericPosition());

			notation.append("-");
		}
		
		// append end repeating position
		for(Edge edge : gu.sortParentSideEdges(mono.getChildEdges())) {
			Substituent sub = (Substituent) edge.getSubstituent();
			if(sub == null) continue;
			if(sub instanceof GlycanRepeatModification) {
				StringBuilder endReppos = new StringBuilder();
				
				if(!edge.isCyclic()) endReppos.append("[");
				
				// append cross linked substituent
				if(sub.getSubstituent() != null) {
					endReppos.append("-");
					endReppos.append(sub.getFirstPosition() == null ? "" : extractPosition(sub.getFirstPosition().getChildLinkages()));
					endReppos.append(sub.getNameWithIUPAC());
					endReppos.append(sub.getSecondPosition() == null ? "" : extractPosition(sub.getSecondPosition().getChildLinkages()));
					//endReppos.append(sub.getNameWithIUPAC());
					endReppos.append("-");
				}
				endReppos.append(makeAcceptorPosition(edge));
				endReppos.append(")");
				notation.insert(0, endReppos);
			}
		}
		
		this.notationIndex.put(mono, notation.toString());
	}
	
	private String makeAcceptorPosition(Edge _parentEdge) {
		if(_parentEdge.getGlycosidicLinkages().size() > 1) return "";	
		StringBuilder ret = new StringBuilder(extractPosition(_parentEdge.getGlycosidicLinkages().get(0).getParentLinkages()));
		if(isFacingAnom(_parentEdge)) {
			Monosaccharide parent = (Monosaccharide) _parentEdge.getParent();
			char parentAnom = parent.getAnomer().getAnomericState();
			ret.append(parentAnom == 'x' ? '?' : parentAnom);
		}
		return ret.toString();
	}
	
	private String makeDonorPosition(Edge _parentEdge) {
		StringBuilder ret = new StringBuilder();

		// append anomeric state
		Node child = _parentEdge.getChild();
		if(child != null) {
			int anomerPos = ((Monosaccharide) child).getAnomericPosition();
			int childPos = _parentEdge.getGlycosidicLinkages().get(0).getChildLinkages().get(0);
			if(!((Monosaccharide) child).getAnomer().equals(AnomericStateDescriptor.OPEN) && anomerPos == childPos) {
				char parentAnom = ((Monosaccharide) _parentEdge.getChild()).getAnomer().getAnomericState();
				ret.append(parentAnom == 'x' ? '?' : parentAnom);
			}
		} else {
			ret.append("?");
		}
		// append child position (anomeric carbon)
		ret.append(extractPosition(_parentEdge.getGlycosidicLinkages().get(0).getChildLinkages()));
	
		// append cross linked substituent
		if(_parentEdge.getSubstituent() != null && !(_parentEdge.getSubstituent() instanceof GlycanRepeatModification)) {
			Substituent sub = (Substituent) _parentEdge.getSubstituent();
			ret.append("-");
			ret.append(sub.getFirstPosition() == null ? "" : extractPosition(sub.getFirstPosition().getChildLinkages()));
			ret.append(sub.getNameWithIUPAC());
			ret.append(sub.getSecondPosition() == null ? "" : extractPosition(sub.getSecondPosition().getChildLinkages()));
		}
		
		ret.append("-");

		return ret.toString();
	}
}