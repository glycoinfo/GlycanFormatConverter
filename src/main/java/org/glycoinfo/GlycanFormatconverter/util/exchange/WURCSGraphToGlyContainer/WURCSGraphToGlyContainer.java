package org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;

import java.util.*;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.GlyContainerOptimizer;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph.GlyContainerEdgeAnalyzer;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;
import org.glycoinfo.WURCSFramework.util.graph.comparator.WURCSEdgeComparatorSimple;
import org.glycoinfo.WURCSFramework.util.graph.visitor.WURCSVisitorCollectSequence;
import org.glycoinfo.WURCSFramework.util.graph.visitor.WURCSVisitorException;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;

public class WURCSGraphToGlyContainer {

	private HashMap<WURCSComponent, Node> backbone2node;
	private GlyContainer glyCo;
	private ArrayList<ModificationAlternative> antennae;
	private ArrayList<ModificationAlternative> undefinedLinkages; // 09/24/2018 Masaaki added
	private ArrayList<ModificationAlternative> undefinedSubstituents; // 09/25/2018 Masaaki added
	private Backbone root;

	public WURCSGraphToGlyContainer () {
		this.backbone2node = new HashMap<>();
		this.glyCo = new GlyContainer();
		this.antennae = new ArrayList<>();
		this.undefinedLinkages = new ArrayList<>(); // 09/24/2018 Masaaki added
		this.undefinedSubstituents = new ArrayList<>(); // 09/25/2018 Masaaki added
		this.root = null;
	}
	
	public GlyContainer getGlycan() {
		return this.glyCo;
	}
	
	public void start (WURCSGraph _graph) throws WURCSException, GlycanException {
		// sort nodes
		LinkedList<Backbone> sortedNodes = sortNodes(_graph);
		
		// set root
		Backbone root = sortedNodes.getFirst();
		this.root = sortedNodes.getFirst();

		// convert node
		BackboneToNode b2n = new BackboneToNode();
		for (Backbone bb : sortedNodes) {
			backbone2node.put(bb, b2n.start(bb));
		}

		// extract glycan fragments root
		ModAltToUndUnit gfParser = new ModAltToUndUnit(glyCo, backbone2node);
		gfParser.start(sortedNodes);
		this.antennae = gfParser.getAntennae();
		this.undefinedLinkages = gfParser.getUndefinedLinkages();
		this.undefinedSubstituents = gfParser.getUndefinedSubstituents();
		this.glyCo = gfParser.getGlycan();

		// extract compositions
		if (isCompositions(_graph)) {
			for (Backbone bb : _graph.getBackbones()) {
				compositionToUndefinedUnit(bb);
			}
			/* 2018/09/24 Masaaki: Set number of undefined linkages */
			this.glyCo.setNumberOfUndefinedLinkages(undefinedLinkages.size());
			for (ModificationAlternative modAlt : this.undefinedSubstituents) {
				compositionToUndefinedUnitForSubstituent(modAlt);
			}
		} else {
			if (glyCo.getNodes().isEmpty()) glyCo.addNode(backbone2node.get(root));

			// convert linkage
			for (Backbone bb : sortedNodes) {
				if (glyCo.containsAntennae(backbone2node.get(bb))) continue;
				WURCSEdgeToLinkage(bb);
			}
			//
			GlyContainerEdgeAnalyzer gcEdgeAnalyzer = new GlyContainerEdgeAnalyzer(glyCo);
			gcEdgeAnalyzer.start(backbone2node, root);
		}

		//Optimize GlyContainer
		GlyContainerOptimizer gop = new GlyContainerOptimizer();
		glyCo = gop.start(glyCo);
	}

	private void WURCSEdgeToLinkage(Backbone _backbone) throws GlycanException, ConverterExchangeException, WURCSFormatException {
		for (WURCSEdge cEdge : _backbone.getChildEdges()) {
			Modification mod = cEdge.getModification();

			if (mod.isRing()) continue;
			if (isSubstituentEdge(cEdge)) continue;

			// define simple linkage
			if (!(mod instanceof ModificationRepeat)) {
				//extractSimpleLinkage(_backbone, cEdge, mod);
				defineSimpleLinkage(_backbone, mod);
			}

			// define repeating unit
			if (mod instanceof ModificationRepeat) {
				extractRpeatingUnit(_backbone, mod);
			}

			// define cyclic unit
			if (isCyclicNodeByEdge(cEdge)) {
				extractCyclicUnit(_backbone, cEdge, mod);
			}
		}
	}

	private void defineSimpleLinkage (Backbone _acceptor, Modification _mod) throws WURCSFormatException, GlycanException {
		ArrayList<Backbone> donors = new ArrayList<>();
		ArrayList<Backbone> acceptors = new ArrayList<>();
		LinkedList<LinkagePosition> donorPos = null;
		LinkedList<LinkagePosition> acceptorPos = null;

		Substituent sub = SubstituentUtility.MAPToSubstituent(_mod);

		if (_mod.getChildEdges().isEmpty() && _mod.getParentEdges().size() > 1) {
			// donor <--> acceptor
			Backbone donor = null;

			// extract monosaccharide of acceptor side.
			for (WURCSEdge acceptorEdge : _mod.getParentEdges()) {
				Modification mod = acceptorEdge.getModification();
				if (!mod.isGlycosidic()) continue;
				if (_acceptor.equals(acceptorEdge.getBackbone())) continue;

				acceptors.add(_acceptor);
				donors.add(acceptorEdge.getBackbone());
				donor = acceptorEdge.getBackbone();
				acceptorPos = acceptorEdge.getLinkages();
			}

			// extract monosaccharide of donor side.
			for (WURCSEdge donorEdge : donor.getChildEdges()) {
				Modification mod = donorEdge.getModification();
				if (!mod.isGlycosidic()) continue;
				if (mod.getParentEdges().size() < 2) continue;

				for (WURCSEdge acceptorEdge : donorEdge.getNextComponent().getParentEdges()) {
					if (donor.equals(acceptorEdge.getBackbone())) continue;
					acceptors.add(donorEdge.getBackbone());
					donors.add(acceptorEdge.getBackbone());
					donorPos = acceptorEdge.getLinkages();
				}
			}
		} else {

			for (WURCSEdge donorEdge : _mod.getChildEdges()) {
				Modification mod = donorEdge.getModification();
				if (!mod.isGlycosidic()) continue;

				donors.add(donorEdge.getBackbone());
				donorPos = donorEdge.getLinkages();
			}

			for (WURCSEdge acceptorEdge : _mod.getParentEdges()) {
				Modification mod = acceptorEdge.getModification();
				if (!mod.isGlycosidic()) continue;

				acceptors.add(acceptorEdge.getBackbone());
				acceptorPos = acceptorEdge.getLinkages();
			}
		}

		//Check linkage for fragments
		if (glyCo.containsAntennae(backbone2node.get(donors.get(0)))) return;

		if (sub != null) {
			sub.setFirstPosition(new Linkage());
			sub.setSecondPosition(new Linkage());

			for (LinkagePosition lp : donorPos) {
				if (lp.getModificationPosition() != 0) {
					sub.getSecondPosition().addChildLinkage(lp.getModificationPosition());
				}
			}

			for (LinkagePosition lp : acceptorPos) {
				if (lp.getModificationPosition() != 0) {
					sub.getFirstPosition().addChildLinkage(lp.getModificationPosition());
				}
			}
		}

		Node acceptorNode = backbone2node.get(acceptors.get(0));
		Node donorNode = backbone2node.get(donors.get(0));
		Edge edge = null;

		//if (!donorNode.getParentEdges().isEmpty()) return;

		for (Backbone donor : donors) {
			if (backbone2node.get(donor).getParentEdges().isEmpty()) {
				int index = donors.indexOf(donor);
				acceptorNode = backbone2node.get(acceptors.get(index));
				donorNode = backbone2node.get(donor);
			}

			for (Edge familyEdge : glyCo.getEdges()) {
				if (familyEdge.getSubstituent() != null && familyEdge.getChild() == null) continue;
				if (familyEdge.getParent().equals(acceptorNode) && familyEdge.getChild().equals(donorNode)) return;
				if (familyEdge.getParent().equals(donorNode) && familyEdge.getChild().equals(acceptorNode)) return;
			}
		}

		edge = this.WURCSEdgeToEdge(acceptorPos, donorPos);
		edge.setSubstituent(sub);

		glyCo.addNode(acceptorNode, edge, donorNode);

		return;
	}

	private void extractSimpleLinkage (Backbone _backbone, WURCSEdge _c, Modification _mod) throws GlycanException, WURCSFormatException {
		Backbone ccBackbone = null;
		Backbone cpBackbone = null;
		LinkedList<LinkagePosition> donor = null;
		LinkedList<LinkagePosition> acceptor = null;

		Substituent sub = SubstituentUtility.MAPToSubstituent(_mod);

		// extract child side child
		for (WURCSEdge cc : _c.getNextComponent().getChildEdges()) {
			if (_backbone.equals(cc.getBackbone())) continue;
			ccBackbone = cc.getBackbone();
			acceptor = cc.getLinkages();
		}

		// extract parent side child
		for (WURCSEdge cp : _c.getNextComponent().getParentEdges()) {
			if (_backbone.equals(cp.getBackbone())) continue;
			cpBackbone = cp.getBackbone();
			donor = cp.getLinkages();
		}

		if (ccBackbone == null && cpBackbone == null) return;

		if (cpBackbone != null) {
			if (isCyclicNode(_backbone) || isCyclicNode(cpBackbone)) return;
			if (haveChild(backbone2node.get(_backbone), backbone2node.get(cpBackbone), sub)) return;
			if (antennae.contains(_backbone)) return;
			if (isAntennaeAnchor(cpBackbone)) return;
			acceptor = _c.getLinkages();
		}
		if (ccBackbone != null) {
			if (haveChild(backbone2node.get(ccBackbone), backbone2node.get(_backbone), sub)) return;
			donor = _c.getLinkages();
		}

		if (isFlipFlop(_backbone)) {
			Backbone tmp = _backbone;
			LinkedList<LinkagePosition> temp = acceptor;
			if (cpBackbone != null) {
				_backbone = cpBackbone;
				cpBackbone = tmp;
				acceptor = donor;
				donor = temp;
			}
			if (ccBackbone != null) {
				_backbone = ccBackbone;
				ccBackbone = tmp;
				acceptor = donor;
				donor = temp;
			}
		}

		Edge edge = null;
		if (ccBackbone != null) {
			edge = WURCSEdgeToEdge(donor, acceptor);
		}
		if (cpBackbone != null) {
			edge = WURCSEdgeToEdge(acceptor, donor);
		}

		if (sub != null) {
			sub.setFirstPosition(new Linkage());
			sub.setSecondPosition(new Linkage());
		}

		for (LinkagePosition lp : donor) {
			if (lp.getModificationPosition() != 0) {
				sub.getSecondPosition().addChildLinkage(lp.getModificationPosition());
			}
		}

		for (LinkagePosition lp : acceptor) {
			if (lp.getModificationPosition() != 0) {
				sub.getFirstPosition().addChildLinkage(lp.getModificationPosition());
			}
		}

		edge.setSubstituent(sub);

		if (glyCo.containsAntennae(backbone2node.get(_backbone))) {
			GlycanUndefinedUnit und = glyCo.getUndefinedUnitWithIndex(backbone2node.get(_backbone));
			und.addNode(backbone2node.get(_backbone), edge, backbone2node.get(ccBackbone));
		} else {
			if (ccBackbone != null && !isDefinedLinkage(_backbone, edge, ccBackbone)) { //&& !containNodes(_backbone, ccBackbone)) {
				glyCo.addNode(backbone2node.get(_backbone), edge, backbone2node.get(ccBackbone));
			}

			if (cpBackbone != null && !isDefinedLinkage(_backbone, edge, cpBackbone)) {// && !containNodes(_backbone, cpBackbone)) {
				glyCo.addNode(backbone2node.get(_backbone), edge, backbone2node.get(cpBackbone));
			}
		}
		//open(_backbone, cpBackbone, ccBackbone, acceptor, donor, sub);
	}

	private void extractRpeatingUnit (Backbone _backbone, Modification _mod) throws GlycanException, WURCSFormatException {
		Node start;
		Node end;
		Node current = backbone2node.get(_backbone);

		// define repeating linkage position
		LinkedList<LinkagePosition> donor = _mod.getParentEdges().getLast().getLinkages();
		LinkedList<LinkagePosition> acceptor = _mod.getParentEdges().getFirst().getLinkages();

		// define start rep
		end = backbone2node.get(_mod.getParentEdges().getLast().getBackbone());

		// define end rep
		start = backbone2node.get(_mod.getParentEdges().getFirst().getBackbone());

		if (!current.equals(end)) return;

		if ( !this.isStandardRepeatEdgeOrder((ModificationRepeat)_mod) ) { // 09/21/2018 Masaaki added
			LinkedList<LinkagePosition> linkTemp = donor;
			donor = acceptor;
			acceptor = linkTemp;

			Node temp = end;
			end = start;
			start = temp;
		}

		Edge parentEdge = WURCSEdgeToEdge(donor, acceptor);

		parentEdge.setSubstituent(makeSubstituentWithRepeat(_mod));

		for (Edge edge : end.getChildEdges()) {
			if (start.equals(edge.getChild())) return;
		}

		glyCo.addNode(end, parentEdge, start);
	}

	/**
	 * Return true if the order of WURCSEdges on the given ModificationRepeat is standard (Last is end, First is start).
	 * @author Masaaki Matsubara
	 * @param _rep ModificationRepeat having start and end WURCSEdges
	 * @return true if the order is standard (no need to make it reverse)
	 * @throws GlycanException When the given ModificationRepeat has number of edges except for two.
	 */
	private boolean isStandardRepeatEdgeOrder(ModificationRepeat _rep) throws GlycanException {
		if ( _rep.getParentEdges().size() != 2 )
			throw new GlycanException("Illegal repeat connections.");

		WURCSEdge t_edgeFirst = _rep.getParentEdges().getFirst();
		WURCSEdge t_edgeLast  = _rep.getParentEdges().getLast();

		Backbone t_bbFirst = t_edgeFirst.getBackbone();
		Backbone t_bbLast  = t_edgeLast.getBackbone();
		// Check tree structure
		if ( !t_bbFirst.equals(t_bbLast) ) {
			boolean t_bFirstIsParent = this.checkParentToChild(t_bbFirst, t_bbLast);
			boolean t_bLastIsParent  = this.checkParentToChild(t_bbLast, t_bbFirst);
			if ( t_bFirstIsParent != t_bLastIsParent )
				return t_bFirstIsParent;
		}

		// Check anomeric position
		Boolean t_bFirstIsAnom = this.isAnomericEdge(t_edgeFirst);
		Boolean t_bLastIsAnom  = this.isAnomericEdge(t_edgeLast);
		if ( t_bFirstIsAnom != t_bLastIsAnom ) {
			// true (annomer edge) > null (no annomer edge) > false (not annomer edge)
			if ( t_bFirstIsAnom != null && t_bFirstIsAnom )
				return true;
			if ( t_bLastIsAnom != null && t_bLastIsAnom )
				return false;
			if ( t_bFirstIsAnom == null )
				return true;
			if ( t_bLastIsAnom == null )
				return false;
		}

		// Compare WURCSEdges
		WURCSEdgeComparatorSimple t_comp = new WURCSEdgeComparatorSimple();
		int t_iComp = t_comp.compare(t_edgeFirst, t_edgeLast);
		if ( t_iComp != 0 )
			return ( t_iComp < 0 );

		return true;
	}

	/**
	 * Check the first given Backbone has the second given Backbone as a child.
	 * The relationship is searched recursively.
	 * @author Masaaki Matsubara
	 * @param _bb1 Backbone to be checked as parent
	 * @param _bb2 Backbone to be checked as child
	 * @return true if _bb1 is parent of _bb2
	 */
	private boolean checkParentToChild(Backbone _bb1, Backbone _bb2) {
		LinkedList<Backbone> t_lChildrenQueue = new LinkedList<>();
		// Search children of first backbone recursively
		t_lChildrenQueue.add(_bb1);
		while (!t_lChildrenQueue.isEmpty()) {
			Backbone t_bb = t_lChildrenQueue.removeFirst();
			for ( WURCSEdge t_edgeChild : t_bb.getChildEdges() ) {
				Modification t_mod = t_edgeChild.getModification();
				if ( t_mod instanceof RepeatInterface )
					continue;
				for ( WURCSEdge t_edgeChildChild : t_mod.getChildEdges() ) {
					Backbone t_bbChild = t_edgeChildChild.getBackbone();
					// Return true if second backbone is matched to first one's child
					if ( _bb2.equals(t_bbChild) )
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return true if the given WURCSEdge is assumed to anomeric linkage.
	 * @author Masaaki Matsubara
	 * @param _oEdge WURCSEdge to be judged whether annomeric linkage or not
	 * @return true if the given WURCSEdge is assumed to anomeric linkage (null if no anomer position)
	 */
	private Boolean isAnomericEdge(WURCSEdge _oEdge) {
		if ( _oEdge.getLinkages().size() > 1 ) return false;
		int t_iAnomPos = _oEdge.getBackbone().getAnomericPosition();
		if ( t_iAnomPos == 0 || t_iAnomPos == -1 ) return null;
		if ( _oEdge.getLinkages().getFirst().getBackbonePosition() != t_iAnomPos ) return false;
		return true;
	}

	private void extractCyclicUnit(Backbone _backbone, WURCSEdge _cEdge, Modification _mod) throws GlycanException, WURCSFormatException {
		if (!_cEdge.getNextComponent().getChildEdges().isEmpty()) return;
		if (_mod instanceof ModificationRepeat) return;

		Node start = backbone2node.get(_backbone);
		Node end = null;

		Edge cyclicEdge = new Edge();
		Linkage lin = new Linkage();

		// extract end cyclic node
		for (WURCSEdge edge : _backbone.getChildEdges()) {
			if (edge.getModification().isRing() || edge.getModification() instanceof ModificationRepeat) continue;
			if (!edge.getModification().getMAPCode().equals("")) break;

			for (WURCSEdge cp : edge.getNextComponent().getParentEdges()) {
				if (!_backbone.equals(cp.getBackbone())) {
					end = backbone2node.get(cp.getBackbone());
				}
			}

			if (end == null) continue;

			for (WURCSEdge cc : edge.getNextComponent().getChildEdges()) {
				if (end.equals(backbone2node.get(cc.getBackbone()))) {
					end = null;
					break;
				}
			}
		}

		if (end == null) return;

		for (WURCSEdge cpEdge : _cEdge.getNextComponent().getParentEdges()) {
			if (_cEdge.getNextComponent().getParentEdges().indexOf(cpEdge) == 0) {
				lin.addChildLinkage(cpEdge.getLinkages().getFirst().getBackbonePosition());
			}
			if (_cEdge.getNextComponent().getParentEdges().indexOf(cpEdge) == 1) {
				lin.addParentLinkage(cpEdge.getLinkages().getFirst().getBackbonePosition());
			}
		}

		if (lin.getChildLinkages().isEmpty() || lin.getParentLinkages().isEmpty()) return;

		cyclicEdge.addGlycosidicLinkage(lin);

		cyclicEdge.setSubstituent(makeSubstituentWithRepeat(_mod));

		if (end != null && !end.equals(start)) {
			glyCo.addNode(end, cyclicEdge, start);
		}
	}

	/**
	 * Create and add a GlycanUndefinedUnit for substituent.
	 * @author Masaaki Matsubara
	 * @param _modAlt
	 * @throws GlycanException
	 */
	private void compositionToUndefinedUnitForSubstituent (ModificationAlternative _modAlt) throws GlycanException, WURCSFormatException {
		// Populate Substituent
		Substituent sub = SubstituentUtility.MAPToSubstituent(_modAlt);
		sub.setFirstPosition(new Linkage());
		sub.setSecondPosition(new Linkage());

		// Add undefined linkage to substituent
		sub.getFirstPosition().addParentLinkage(-1);
		sub.getFirstPosition().addChildLinkage(1);
		if ( sub.getSubstituent() instanceof BaseCrossLinkedTemplate ) {
			sub.getSecondPosition().addParentLinkage(-1);
			sub.getSecondPosition().addChildLinkage(1);
		}

		// Add Substituent to an Edge
		Edge edge = new Edge();
		edge.setSubstituent(sub);
		sub.addParentEdge(edge);

		GlycanUndefinedUnit t_und = new GlycanUndefinedUnit();
		t_und.setConnection(edge);
		glyCo.addGlycanUndefinedUnitForSubstituent(t_und);
	}

	private void compositionToUndefinedUnit (Backbone _backbone) throws GlycanException {
		Node current = backbone2node.get(_backbone);
		GlycanUndefinedUnit und = new GlycanUndefinedUnit();
		und.addNode(current);

		glyCo.addGlycanUndefinedUnit(und);
	}

	private Edge WURCSEdgeToEdge (LinkedList<LinkagePosition> _donor, LinkedList<LinkagePosition> _acceptor) throws GlycanException {
		Edge edge = new Edge();
		Linkage lin = new Linkage();
		for (LinkagePosition lp : _acceptor) {
			lin.addChildLinkage(lp.getBackbonePosition());
		}
		for (LinkagePosition lp : _donor) {
			lin.addParentLinkage(lp.getBackbonePosition());
		}

		lin.setProbabilityLower(_donor.getFirst().getProbabilityLower());
		lin.setProbabilityUpper(_donor.getFirst().getProbabilityUpper());
		lin.setChildProbabilityLower(_acceptor.getFirst().getProbabilityLower());
		lin.setChildProbabilityUpper(_acceptor.getFirst().getProbabilityUpper());

		edge.addGlycosidicLinkage(lin);
		return edge;
	}

	private Substituent makeSubstituentWithRepeat (Modification _mod) throws GlycanException, WURCSFormatException {
	    MAPAnalyzer mapAnalyze = new MAPAnalyzer();
		mapAnalyze.start(_mod.getMAPCode());
		BaseCrossLinkedTemplate bcT = mapAnalyze.getCrossTemplate();

		GlycanRepeatModification ret = new GlycanRepeatModification(bcT);

		ret.setHeadAtom(mapAnalyze.getHeadAtom());
		ret.setTailAtom(mapAnalyze.getTailAtom());

		ModificationRepeat repMod = (_mod instanceof  ModificationRepeat) ? (ModificationRepeat) _mod : null;

		ret.setMinRepeatCount(repMod == null ? 0 : repMod.getMinRepeatCount());
		ret.setMaxRepeatCount(repMod == null ? 0 : repMod.getMaxRepeatCount());

		ret.setFirstPosition(new Linkage());
		ret.setSecondPosition(new Linkage());

		return ret;
	}

	private LinkedList<Backbone> sortNodes (WURCSGraph _graph) throws WURCSVisitorException {
		LinkedList<Backbone> ret = new LinkedList<Backbone>();
		WURCSVisitorCollectSequence w = new WURCSVisitorCollectSequence();
		w.start(_graph);

		for (WURCSComponent wc : w.getNodes()) {
			if (wc instanceof Backbone && !ret.contains(wc)) {
				ret.add(((Backbone) wc));
			}
		}

		return ret;
	}

	private boolean isCyclicNode (Backbone _backbone) {
		if (!root.equals(_backbone)) return false;

		boolean isCyclic = false;
		Backbone end = null;

		//if (!_backbone.isRoot()) return isCyclic;

		for (WURCSEdge edge : _backbone.getChildEdges()) {
			if (edge.getModification().isRing()) continue;
			if (isSubstituentEdge(edge) || edge.getModification() instanceof ModificationRepeat) break;
			if (edge.getModification().isGlycosidic() && !edge.getModification().getMAPCode().equals("")) continue;

			for (WURCSEdge cp : edge.getNextComponent().getParentEdges()) {
				if (cp.getNextComponent() instanceof ModificationAlternative) continue;
				if (!_backbone.equals(cp.getBackbone()) && !cp.getBackbone().getParentEdges().isEmpty()) {
					end = cp.getBackbone();
				}
			}

			for (WURCSEdge cc : edge.getNextComponent().getChildEdges()) {
				if (cc.getNextComponent() instanceof ModificationAlternative) continue;
			}

			if (end == null) continue;

			for (WURCSEdge cc : edge.getNextComponent().getChildEdges()) {
				if (end.equals(cc.getBackbone())) {
					end = null;
					break;
				}
			}
		}

		isCyclic = (end != null);
		return isCyclic;
	}

	private boolean isCyclicNodeByEdge (WURCSEdge _edge) {
		if (_edge.getModification() instanceof ModificationRepeat) return false;
		if (isSubstituentEdge(_edge)) return false;
		if (_edge.getModification().isRing()) return false;
		if (_edge.getModification().isGlycosidic() && !_edge.getModification().getMAPCode().equals("")) return false;

		return (isCyclicNode(_edge.getBackbone()));
	}

	private boolean isSubstituentEdge (WURCSEdge _wedge) {
		Modification mod = _wedge.getModification();
		if (mod.isGlycosidic() || mod.isRing() || mod instanceof ModificationRepeat) return false;

		if (!_wedge.getModification().getMAPCode().equals("")) {
			return (_wedge.getNextComponent().getChildEdges().isEmpty() && !_wedge.getNextComponent().getParentEdges().isEmpty());
		} else {
			return (_wedge.getNextComponent().getChildEdges().isEmpty() && _wedge.getNextComponent().getParentEdges().size() == 2);
		}
	}

	private boolean haveChild (Node _parent, Node _child, Node _sub) {
		if (_parent.getParentEdges().isEmpty()) return false;
		boolean ret = false;

		for (Edge edge : _parent.getParentEdges()) {
			if (edge.getParent() == null || edge.getChild() == null) continue;
			if (edge.getSubstituent() == null && _sub != null) continue;
			if (edge.getSubstituent() != null && _sub == null) continue;
			if (edge.getParent().equals(_child) && edge.getChild().equals(_parent)) {
				ret = true;
			}

		}

		return ret;
	}

	private boolean isAntennaeAnchor (Backbone _backbone) {
		boolean ret = false;

		for (ModificationAlternative modAlt : antennae) {
			for (WURCSEdge inEdge : modAlt.getLeadInEdges()) {
				for (WURCSEdge parentEdge : inEdge.getNextComponent().getParentEdges()) {
					if (parentEdge.getBackbone().equals(_backbone)) {
						ret = true;
						break;
					}
				}

				if (ret) break;
			}

			if (ret) break;
		}

		return ret;
	}

	private boolean isFlipFlop (Backbone _parent) {
		if (!_parent.isRoot() || root.equals(_parent) || antennae.contains(_parent)) return false;
		return (backbone2node.get(_parent).getParentEdges().isEmpty());
	}

	private boolean isCompositions (WURCSGraph _graph) {
		int count = 0;

		if (_graph.getBackbones().size() == 1) return false;
		for (Backbone bb : _graph.getBackbones()) {
			if (bb.isRoot()) count++;
		}

		return (_graph.getBackbones().size() == count);
	}

	private boolean isDefinedLinkage (Backbone _acceptor, Edge _edge, Backbone _donor) {
		Node donor = backbone2node.get(_donor);
		Node acceptor = backbone2node.get(_acceptor);

		if (donor == null || acceptor == null) return false;

		boolean isDefined = false;

		int currentDonorPos = -1;
		int currentAcceptorPos = -1;

		for (Linkage lin : _edge.getGlycosidicLinkages()) {
			if (lin.getChildLinkages().size() > 1 || lin.getParentLinkages().size() > 1) continue;
			currentDonorPos = lin.getChildLinkages().get(0);
			currentAcceptorPos = lin.getParentLinkages().get(0);
		}

		for (Edge edge : acceptor.getChildEdges()) {
			if (edge.getChild() == null || edge.getParent() == null) continue;
			if (edge.getChild().equals(donor) && edge.getParent().equals(acceptor)) {
				for (Linkage lin : edge.getGlycosidicLinkages()) {
					if (lin.getChildLinkages().size() > 1 || lin.getParentLinkages().size() > 1) continue;
					if (lin.getChildLinkages().indexOf(currentDonorPos) != -1 &&
							lin.getParentLinkages().indexOf(currentAcceptorPos) != -1) {
						isDefined = true;
					}
				}
			}
		}

		return isDefined;
	}

	/**
	 * for debug
	 * */
	private void open (Backbone _backbone, Backbone _cpBackbone, Backbone _ccBackbone, LinkedList<LinkagePosition> _acceptor, LinkedList<LinkagePosition> _donor, Substituent _sub) {
		if (_sub != null) {
			System.out.println(_sub.getSubstituent());
		}

		if (_ccBackbone != null) {
			System.out.println("cc : " + _ccBackbone + "<-" + _backbone);
			System.out.println(_ccBackbone.getSkeletonCode() + " " + _backbone.getSkeletonCode());
			System.out.println("cc : " + backbone2node.get(_ccBackbone) + "<-" + backbone2node.get(_backbone));
		}
		if (_cpBackbone != null) {
			System.out.println("cp : " + _cpBackbone + "<=" + _backbone);
			System.out.println(_cpBackbone.getSkeletonCode() + " " + _backbone.getSkeletonCode());
			System.out.println("cp : " + backbone2node.get(_cpBackbone) + "<-" + backbone2node.get(_backbone));
		}

		if (_acceptor != null) {
			for (LinkagePosition lp : _acceptor) {
				System.out.println("acceptor : " + lp.getBackbonePosition() + " " +lp.getDirection() + " "  +lp.getProbabilityPosition());
			}
		}
		if (_donor != null) {
			for (LinkagePosition lp : _donor) {
				System.out.println("donor : "  +lp.getBackbonePosition() + " "  +lp.getDirection() + " " + lp.getProbabilityPosition());
			}
		}
		System.out.println("");
	}

}