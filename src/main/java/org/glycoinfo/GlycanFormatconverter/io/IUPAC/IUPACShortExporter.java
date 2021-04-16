package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.ExporterInterface;
import org.glycoinfo.GlycanFormatconverter.util.similarity.NodeSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class IUPACShortExporter extends IUPACExporterUtility implements ExporterInterface{

	private final StringBuilder shortIUPAC = new StringBuilder();
	private final HashMap<Node, String> notationIndex = new HashMap<>();
	private final NodeSimilarity gu = new NodeSimilarity();

	public String getIUPACShort() {
		return shortIUPAC.toString();
	}
	
	public void start (GlyContainer _glyCo) throws GlycanException {
		for( Node node : _glyCo.getAllNodes()) {
			// make core notations
			makeMonosaccharideNotation(node);
			// append linkage position to core notation
			makeLinkageNotation(node);
		}
		
		// for fragments of substituent
		for (GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
			for (Node node : und.getRootNodes()) {
				if (node instanceof Substituent) {
					makeSubstituentNotation(und);
				}
			}
		}
		
		makeFragmentsAnchor(_glyCo);

		if (_glyCo.isComposition()) {
			shortIUPAC.insert(0, makeComposition(_glyCo));
			return;
		}
		
		// sort core node
		ArrayList<Node> sortedList = gu.sortAllNode(_glyCo.getRootNodes().get(0));
		shortIUPAC.insert(0, makeSequence(sortedList));
		
		// sort fragments
		shortIUPAC.insert(0, makeFragmentsSequence(_glyCo.getUndefinedUnit()));
	}

	@Override
	public String makeComposition(GlyContainer _glyCo) {
		return "";
		
		/*
		StringBuilder ret = new StringBuilder();
		
		ArrayList<String> nodeList = new ArrayList<String>();
		for (Iterator<GlycanUndefinedUnit> iterUnd = _glyCo.getUndefinedUnit().iterator(); iterUnd.hasNext();) {
			nodeList.add(notationIndex.get(iterUnd.next().getNodes().get(0)));
		}
		
		LinkedHashMap<String, Integer> block = new LinkedHashMap<String, Integer>();
		for (String notation : nodeList) {
			if (block.containsKey(notation)) {
				block.put(notation, block.get(notation)+1);
			} else {
				block.put(notation, 1);
			}
		}
		
		for (Iterator<String> iterKey = block.keySet().iterator(); iterKey.hasNext();) {
			String notation = iterKey.next();
			ret.append("{" + notation + "}" + block.get(notation));
			if (iterKey.hasNext()) ret.append(",");
		}
		
		return ret.toString();
		*/
	}

	public String makeFragmentsSequence (ArrayList<GlycanUndefinedUnit> _fragments) throws GlycanException {
		StringBuilder ret = new StringBuilder();
		for(GlycanUndefinedUnit und : _fragments) {
			for(Node antennae : und.getRootNodes()) {
				ArrayList<Node> sortedFragments = gu.sortAllNode(antennae);
				shortIUPAC.insert(0, makeSequence(sortedFragments) + ",");
			}
		}
		return ret.toString();
	}

	@Override
	public boolean isFragmentsRoot(GlyContainer _glyco, Node _node) throws Exception {
		return false;
	}

	public void makeFragmentsAnchor(GlyContainer _glyCo) throws GlycanException {
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

	@Override
	public void makeLinkageNotationFragmentSide(Node _node) {

	}

	@Override
	public String makeSimpleLinkageNotation (ArrayList<Edge> _edges) {
		StringBuilder linkagePos = new StringBuilder();
		for(Iterator<Edge> iterParent = gu.sortParentSideEdges(_edges).iterator(); iterParent.hasNext();) {
			Edge parentEdge = iterParent.next();
			Substituent sub = (Substituent) parentEdge.getSubstituent();

			// append anomeric position
			linkagePos.append(makeDonorPosition(parentEdge));

			// append start repeating position
			if(sub != null && sub instanceof GlycanRepeatModification && !parentEdge.isCyclic()) {
				linkagePos.append("]");
				linkagePos.append(makeRepeatingCount((GlycanRepeatModification) parentEdge.getSubstituent()));
			}

			// append probability annotation
			linkagePos.append(makeProbabilityAnnotation(parentEdge));

			// append parent linkage position
			//if(sub != null && !(sub instanceof GlycanRepeatModification)) {
			if (!parentEdge.isRepeat() && !parentEdge.isCyclic()) {
				linkagePos.append(makeAcceptorPosition(parentEdge));
			}

			// append a separator for dual linkage position
			if(iterParent.hasNext()) linkagePos.append(":");
		}

		return linkagePos.toString();
	}

	public String makeSequence (ArrayList<Node> _nodes) {
		int branch = 0;
		StringBuilder encode = new StringBuilder();
		
		for(Node skey : _nodes) {
			StringBuilder notation = new StringBuilder(notationIndex.get(skey));
			
			if(gu.isMainChaineBranch(skey)) {
				notation.append(")");
				branch++;
			}
			if(gu.countChildren(skey) == 0 && branch > 0) {
				notation.insert(0, "(");
				branch--;
			}
			encode.insert(0, notation);
		}
		
		return encode.toString();
	}
	
	public void makeSubstituentNotation(GlycanUndefinedUnit _und) {
		Node sub = _und.getNodes().get(0);
		if(!(sub instanceof Substituent)) return;
		
		if (!notationIndex.containsKey(sub)) {
			String subNotation = ((Substituent) sub).getSubstituent().getIUPACnotation();
			subNotation = this.extractPosition(_und.getConnection().getGlycosidicLinkages().get(0).getParentLinkages()) + subNotation;
			notationIndex.put(sub, subNotation);
		}
	}
	
	public void makeMonosaccharideNotation(Node _node) throws GlycanException {
		IUPACNotationConverter monoIUPAC = new IUPACNotationConverter();

		Node copy = _node.copy();
		monoIUPAC.makeTrivialName(copy);

		if(!notationIndex.containsKey(_node)) notationIndex.put(_node, monoIUPAC.getCoreCode());
	}
	
	public void makeLinkageNotation (Node _node) {
		StringBuilder notation = new StringBuilder(notationIndex.get(_node));
		Monosaccharide mono = (Monosaccharide) _node;

		// append simple linkage notation
		if(!mono.getParentEdges().isEmpty()) {
			notation.append(this.makeSimpleLinkageNotation(_node.getParentEdges()));
		}

		// for root node
		if(mono.getParentEdges().isEmpty() && !mono.getAnomer().equals(AnomericStateDescriptor.OPEN) && !isFacingAnoms(mono.getChildEdges())) {
			char anomericState = mono.getAnomer().getAnomericState();
			notation.append(anomericState == 'x' ? '?' : anomericState == 'o' ? '?' : anomericState);
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
					endReppos.append(sub.getNameWithIUPAC());
				}
				endReppos.append(makeAcceptorPosition(edge));
				notation.insert(0, endReppos);
			}
		}
		
		this.notationIndex.put(mono, notation.toString());
	}
	
	public String makeAcceptorPosition (Edge _edge) {
		if(_edge.getGlycosidicLinkages().size() > 1) return "";
		StringBuilder ret = new StringBuilder(extractPosition(_edge.getGlycosidicLinkages().get(0).getParentLinkages()));
		if(isFacingAnom(_edge)) {
			Monosaccharide parent = (Monosaccharide) _edge.getParent();
			char parentAnom = parent.getAnomer().getAnomericState();
			ret.append(parentAnom == 'x' ? '?' : parentAnom);
		}

		return ret.toString();
	}
	
	public String makeDonorPosition (Edge _edge) {
		StringBuilder ret = new StringBuilder();
		// append anomeric state
		Node child = _edge.getChild();
		if(child != null) {
			char childAnom = ((Monosaccharide) child).getAnomer().getAnomericState();
			int childAnomPos = ((Monosaccharide) child).getAnomericPosition();
			int childPos = _edge.getGlycosidicLinkages().get(0).getChildLinkages().get(0);
		
			if(childPos == childAnomPos)
				ret.append(childAnom == 'x' ? '?' : childAnom == 'o' ? '?' : childAnom);
			else
				ret.append(childPos == -1 ? "?" : childPos);
		}
		
		/* append child position (anomeric carbon)*/
		//if(_parentEdge.getSubstituent() != null && child != null)
		//	ret.append(extractPosition(_parentEdge.getGlycosidicLinkages().get(0).getChildLinkages()));
		
		// append cross linked substituent
		if(_edge.getSubstituent() != null && !(_edge.getSubstituent() instanceof GlycanRepeatModification)) {
			ret.append(((Substituent) _edge.getSubstituent()).getNameWithIUPAC());
		}
		
		return ret.toString();
	}
}
