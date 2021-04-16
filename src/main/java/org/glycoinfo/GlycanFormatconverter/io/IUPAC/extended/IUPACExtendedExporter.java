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

	private final StringBuilder extended = new StringBuilder();
	private final HashMap<Node, String> notationIndex = new HashMap<>();
	private final NodeSimilarity gu = new NodeSimilarity();

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
			// append linkage notation
			if (this.isFragmentsRoot(_glyCo, node)) {
				// for root of fragments
				makeLinkageNotationFragmentSide(node);
			} else {
				// for other sides
				makeLinkageNotation(node);
			}
		}

		// for fragments of substituent
		for (GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
			for (Node node : und.getRootNodes()) {
				if (node instanceof Substituent) {
					makeSubstituentNotation(und);
				}
			}
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

		// append simple linkage notation
		if(!mono.getParentEdges().isEmpty()) {
			notation.append(this.makeSimpleLinkageNotation(_node.getParentEdges()));
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

				endReppos.append(makeAcceptorPosition(edge));
				endReppos.append(")-");
				notation.insert(0, endReppos);
			}
		}
		this.notationIndex.put(mono, notation.toString());
	}

	public String makeSimpleLinkageNotation (ArrayList<Edge> _edges) {
		StringBuilder linkagePos = new StringBuilder("-(");
		//ArrayList<Edge> edges = mono.getParentEdges();
		for (Iterator<Edge> iterParent = gu.sortParentSideEdges(_edges).iterator(); iterParent.hasNext(); ) {
			Edge parentEdge = iterParent.next();

			if (parentEdge.isCyclic() && linkagePos.charAt(linkagePos.length() - 1) == ':') linkagePos.append("(");

			// append anomeric position
			linkagePos.append(makeDonorPosition(parentEdge));

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
			if (!(parentEdge.getSubstituent() instanceof GlycanRepeatModification)) {
				linkagePos.append(makeAcceptorPosition(parentEdge));
				if (!iterParent.hasNext()) linkagePos.append(")");
			}
			// append a separator for dual linkage position
			if (iterParent.hasNext()) {
				linkagePos.append(":");
			}
		}

		return linkagePos.toString();
	}

	public void makeLinkageNotationFragmentSide (Node _node) {
		if (_node.getParentEdges().isEmpty()) return;
		StringBuilder notation = new StringBuilder(notationIndex.get(_node));
		Monosaccharide mono = (Monosaccharide) _node;
		ArrayList<Edge> edges = new ArrayList<>();
		edges.add(mono.getParentEdge());

		notation.append(this.makeSimpleLinkageNotation(edges));
		this.notationIndex.put(mono, notation.toString());
	}

	public String makeAcceptorPosition (Edge _edge) {
		if(_edge.getGlycosidicLinkages().size() > 1) return "";
		return extractPosition(_edge.getGlycosidicLinkages().get(0).getParentLinkages());
	}
	
	public String makeDonorPosition (Edge _edge) {
		StringBuilder ret = new StringBuilder();
		ret.append(extractPosition(_edge.getGlycosidicLinkages().get(0).getChildLinkages()));

		// check facing monosaccharides
		if(isFacingAnom(_edge)) ret.append("<");
	
		// append cross linked substituent
		if(_edge.getSubstituent() != null && !(_edge.getSubstituent() instanceof GlycanRepeatModification)) {
			Substituent sub = (Substituent) _edge.getSubstituent();
			ret.append("-");
			ret.append(sub.getFirstPosition() == null ? "" : extractPosition(sub.getFirstPosition().getChildLinkages()));
			ret.append(sub.getNameWithIUPAC());
			ret.append(sub.getSecondPosition() == null ? "" : extractPosition(sub.getSecondPosition().getChildLinkages()));
			ret.append("-");
			ret.append(">");
		} else {
			ret.append("-");
			ret.append(">");
		}

		return ret.toString();
	}

	public boolean isFragmentsRoot (GlyContainer _glyco, Node _node) throws GlycanException {
		boolean ret = false;
		for (GlycanUndefinedUnit und : _glyco.getUndefinedUnit()) {
			if (und.getRootNodes().get(0).equals(_node)) {
				ret = true;
				break;
			}
		}
		return ret;
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
