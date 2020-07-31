package org.glycoinfo.GlycanFormatconverter.io.IUPAC.extended;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.ExporterInterface;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExporterUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.GlycanFormatconverter.util.similarity.NodeSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class IUPACExtendedExporter extends IUPACExporterUtility implements ExporterInterface{

	private final StringBuilder extended;
	private final HashMap<Node, String> notationIndex;
	private final NodeSimilarity gu;

	public IUPACExtendedExporter () {
		this.extended = new StringBuilder();
		this.notationIndex = new HashMap<>();
		this.gu = new NodeSimilarity();
	}

	public String getIUPACExtended () {
		return this.extended.toString();
	}
	
	public String toGreek() {
		String ret = this.extended.toString();
		ret = ret.replaceAll(AnomericStateDescriptor.ALPHA.getIUPACAnomericState(), "\u03B1");
		ret = ret.replaceAll(AnomericStateDescriptor.BETA.getIUPACAnomericState(), "\u03B2");

		ret = ret.replaceAll("<->", "\u2194");
		ret = ret.replaceAll("->", "\u2192");

		return ret;
	}
	
	public void start (GlyContainer _glyCo) throws GlycanException, TrivialNameException {
		for( Node node : _glyCo.getAllNodes()) {
			// make core notations
			makeMonosaccharideNotation(node);
			// append linkage position to core notation
			makeLinkageNotation(node);
		}

		// for fragments of substituent
		for (GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
			if (und.getNodes().get(0) instanceof Substituent)
				makeSubstituentNotation(und);
		}

		// append fragments anchor
		makeFragmentsAnchor(_glyCo);

		// sort compositions
		if (_glyCo.isComposition()) {
			extended.insert(0, makeComposition(_glyCo));
			return;
		}

		// sort core node
		ArrayList<Node> sortedList = gu.sortAllNode(_glyCo.getRootNodes().get(0));
		extended.insert(0, makeSequence(sortedList));

		// sort fragments
		extended.insert(0, makeFragmentsSequence(_glyCo.getUndefinedUnit()));
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
			ret.append("{").append(notation).append("}").append(block.get(notation));
			if (iterKey.hasNext()) ret.append(",");
		}
		
		return ret.toString();
	}

	public String makeFragmentsSequence (ArrayList<GlycanUndefinedUnit> _fragments) throws GlycanException {
		StringBuilder ret = new StringBuilder();

		for(GlycanUndefinedUnit und : _fragments) {
			for(Node antennae : und.getRootNodes()) {
				ArrayList<Node> sortedFragments = gu.sortAllNode(antennae);
				ret.insert(0, makeSequence(sortedFragments) + ",");
			}
		}

		return ret.toString();
	}
	
	public String makeSequence (ArrayList<Node> _nodes) {
		int branch = 0;
		StringBuilder encode = new StringBuilder();

		for (Node node : _nodes) {
			StringBuilder notation = new StringBuilder(notationIndex.get(node));
			if (gu.isMainChaineBranch(node)) {
				notation.append("]");
				branch++;
			}

			int numOfChildren = gu.countChildren(node);
			if (numOfChildren > 0) notation.insert(0, "-");
			if (numOfChildren == 0 && branch > 0) {
				notation.insert(0, "[");
				branch--;
			}
			encode.insert(0, notation);
		}

		return encode.toString();
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

	public void makeSubstituentNotation (GlycanUndefinedUnit _und) {
		Node sub = _und.getNodes().get(0);
		if(!(sub instanceof Substituent)) return;
		
		if (!notationIndex.containsKey(sub)) {
			String subNotation = ((Substituent) sub).getSubstituent().getIUPACnotation();
			subNotation = "O" + subNotation;
			subNotation = this.extractPosition(_und.getConnection().getGlycosidicLinkages().get(0).getParentLinkages()) + subNotation;
			notationIndex.put(sub, subNotation);
		}
	}
	
	public void makeMonosaccharideNotation (Node _node) throws GlycanException, TrivialNameException {
		if (!(_node instanceof Monosaccharide)) return;
		ExtendedConverter extConv = new ExtendedConverter();
		if (!notationIndex.containsKey(_node)) notationIndex.put(_node, extConv.start(_node));
	}
	
	public void makeLinkageNotation (Node _node) {
		StringBuilder notation = new StringBuilder(notationIndex.get(_node));
		Monosaccharide mono = (Monosaccharide) _node;

		if(!mono.getParentEdges().isEmpty()) {
			StringBuilder linkagePos = new StringBuilder("-(");
			ArrayList<Edge> edges = mono.getParentEdges();
			for(Iterator<Edge> iterParent = gu.sortParentSideEdges(edges).iterator(); iterParent.hasNext();) {
				Edge parentEdge = iterParent.next();

				if (parentEdge.isCyclic() && linkagePos.charAt(linkagePos.length() - 1) == ':') linkagePos.append("(");

				// append anomeric position
				linkagePos.append(makeChildSidePosition(parentEdge));

				// append start repeating position
				if (parentEdge.isRepeat()) {
					linkagePos.append("]");
					linkagePos.append(makeRepeatingCount((GlycanRepeatModification) parentEdge.getSubstituent()));
				}
				/*if(parentEdge.getSubstituent() != null && parentEdge.getSubstituent() instanceof GlycanRepeatModification) {
					if(!parentEdge.isCyclic()) {
						linkagePos.append("]");
						linkagePos.append(makeRepeatingCount((GlycanRepeatModification) parentEdge.getSubstituent()));
					}
				}*/

				// append probability annotation
				linkagePos.append(makeProbabilityAnnotation(parentEdge));

				// append parent linkage position
				if(!(parentEdge.getSubstituent() instanceof GlycanRepeatModification)) {
					linkagePos.append(makeParentSidePosition(parentEdge));
					if(!iterParent.hasNext()) linkagePos.append(")");
				}
				// append a separator for dual linkage position
				if(iterParent.hasNext()) {
					linkagePos.append(":");
				}
			}

			notation.append(linkagePos);
		}

		// for root node
		if(mono.getParentEdges().isEmpty() && mono.getAnomericPosition() != Monosaccharide.OPEN_CHAIN && !isFacingAnoms(mono.getChildEdges())) {
			notation.append("-(");
			notation.append(mono.getAnomericPosition() == -1 ? "?" : mono.getAnomericPosition());
			notation.append("->");
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
					endReppos.append("-");
				}

				endReppos.append(makeParentSidePosition(edge));
				endReppos.append(")-");
				notation.insert(0, endReppos);
			}
		}
		this.notationIndex.put(mono, notation.toString());
	}
	
	private String makeParentSidePosition (Edge _parentEdge) {
		if(_parentEdge.getGlycosidicLinkages().size() > 1) return "";	
		return extractPosition(_parentEdge.getGlycosidicLinkages().get(0).getParentLinkages());
	}
	
	private String makeChildSidePosition (Edge _parentEdge) {
		StringBuilder ret = new StringBuilder();
		ret.append(extractPosition(_parentEdge.getGlycosidicLinkages().get(0).getChildLinkages()));

		// check facing monosaccharides
		if(isFacingAnom(_parentEdge)) ret.append("<");	
	
		// append cross linked substituent
		if(_parentEdge.getSubstituent() != null && !(_parentEdge.getSubstituent() instanceof GlycanRepeatModification)) {
			Substituent sub = (Substituent) _parentEdge.getSubstituent();
			ret.append("-");
			ret.append(sub.getFirstPosition() == null ? "" : extractPosition(sub.getFirstPosition().getChildLinkages()));
			ret.append(sub.getNameWithIUPAC());
			ret.append(sub.getSecondPosition() == null ? "" : extractPosition(sub.getSecondPosition().getChildLinkages()));
			ret.append("-");
			ret.append(">");
			//if (isLinkageByAnomericPosition(_parentEdge)) ret.append(">");
		} else {
			ret.append("-");
			ret.append(">");
			//if (isLinkageByAnomericPosition(_parentEdge)) ret.append(">");
		}

		return ret.toString();
	}

	private boolean isLinkageByAnomericPosition (Edge _parentEdge) {
		if (_parentEdge.getChild() == null) return false;
		Monosaccharide child = (Monosaccharide) _parentEdge.getChild();
		ArrayList<Integer> childPos = _parentEdge.getGlycosidicLinkages().get(0).getChildLinkages();

		if (child.getAnomericPosition() == -1) return false;
		if (child.getAnomer().equals(AnomericStateDescriptor.UNKNOWN)) return true;
		if (child.getAnomer().equals(AnomericStateDescriptor.OPEN)) return true;
		return (childPos.contains(child.getAnomericPosition()));
	}
}
