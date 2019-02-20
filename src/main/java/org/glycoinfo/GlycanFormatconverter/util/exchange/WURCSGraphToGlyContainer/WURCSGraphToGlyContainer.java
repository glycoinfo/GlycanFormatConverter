package org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.GlyContainerEdgeAnalyzer;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;
import org.glycoinfo.WURCSFramework.util.graph.visitor.WURCSVisitorCollectSequence;
import org.glycoinfo.WURCSFramework.util.graph.visitor.WURCSVisitorException;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;

public class WURCSGraphToGlyContainer {

	private HashMap<WURCSComponent, Node> backbone2node;
	private GlyContainer glyCo;
	private ArrayList<Backbone> antennae;
	private GlycanUndefinedUnit und;
	private Backbone root;

	public WURCSGraphToGlyContainer () {
		this.backbone2node = new HashMap<>();
		this.glyCo = new GlyContainer();
		this.antennae = new ArrayList<>();
		this.und = null;
		this.root = null;
	}
	
	public GlyContainer getGlycan() {
		return this.glyCo;
	}
	
	public void start (WURCSGraph _graph) throws WURCSException, GlycanException {

		/* sort nodes */
		LinkedList<Backbone> sortedNodes = sortNodes(_graph);
		
		/* set root */
		root = sortedNodes.getFirst();
		
		/* convert node */
		WURCSGraphToGlyContainer.BackboneToNode b2n = new WURCSGraphToGlyContainer.BackboneToNode();
		for (Backbone bb : sortedNodes) {
			backbone2node.put(bb, b2n.start(bb));
			extractAntennae(bb);
		}

		/* extract compositions */
		if (isCompositions(_graph)) {
			for (Backbone bb : _graph.getBackbones()) {
				compositionToUndefinedUnit(bb);
			}
		} else {
			if (glyCo.getNodes().isEmpty()) glyCo.addNode(backbone2node.get(root));

			/* convert linkage */
			for (Backbone bb : sortedNodes) {
				WURCSEdgeToLinkage(bb);
			}
			/**/
			GlyContainerEdgeAnalyzer gcEdgeAnalyzer = new GlyContainerEdgeAnalyzer(glyCo);
			gcEdgeAnalyzer.start(backbone2node, root);
		}		
	}

	private void WURCSEdgeToLinkage(Backbone _backbone) throws GlycanException, ConverterExchangeException {
		if (und != null) und = null;

		/* define parent linkage */
		for (Backbone antennae : antennae) {
			if (antennae.equals(_backbone)) backboneToUndefinedUnit(_backbone);
		}
		
		for (WURCSEdge cEdge : _backbone.getChildEdges()) {
			Modification mod = cEdge.getModification();

			if (mod.isRing()) continue;
			if (isSubstituentEdge(cEdge)) continue;
			
			/* define simple linkage */
			if (!(mod instanceof ModificationRepeat)) {
				extractSimpleLinkage(_backbone, cEdge, mod);
			}

			/* define repeating unit */
			if (mod instanceof ModificationRepeat) {
				extractRpeatingUnit(_backbone, mod);
			}
			
			/* define cyclic unit */
			if (isCyclicNodeByEdge(cEdge)) {
				extractCyclicUnit(_backbone, cEdge, mod);
			}
		}
	}

	private void extractSimpleLinkage (Backbone _backbone, WURCSEdge _c, Modification _mod) throws GlycanException {
		Backbone ccBackbone = null;
		Backbone cpBackbone = null;
		LinkedList<LinkagePosition> donor = null;
		LinkedList<LinkagePosition> acceptor = null;
		Substituent sub = MAPToSubstituent(_mod);

		/* extract child side child */
		for (WURCSEdge cc : _c.getNextComponent().getChildEdges()) {
			if (_backbone.equals(cc.getBackbone())) continue;
			ccBackbone = cc.getBackbone();
			acceptor = cc.getLinkages();
		}

		/* extract parent side child */
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
		
		if (isFlipFlop(_backbone, ccBackbone != null ? ccBackbone : cpBackbone != null ? cpBackbone : null)) {
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
			SubstituentUtility subUtil = new SubstituentUtility();
			sub = subUtil.modifyLinkageType(sub);
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
		
		if (ccBackbone != null && !isDefinedLinkage(_backbone, edge, ccBackbone)) { //&& !containNodes(_backbone, ccBackbone)) {
			glyCo.addNode(backbone2node.get(_backbone), edge, backbone2node.get(ccBackbone));
		}

		if (cpBackbone != null && !isDefinedLinkage(_backbone, edge, cpBackbone)) {// && !containNodes(_backbone, cpBackbone)) {
			glyCo.addNode(backbone2node.get(_backbone), edge, backbone2node.get(cpBackbone));
		}

		//open(_backbone, cpBackbone, ccBackbone, acceptor, donor, sub);
	}

	private void extractRpeatingUnit (Backbone _backbone, Modification _mod) throws GlycanException {
		Node start;
		Node end;
		Node current = backbone2node.get(_backbone);

		/* define repeating linkage position */
		LinkedList<LinkagePosition> donor = _mod.getParentEdges().getLast().getLinkages();
		LinkedList<LinkagePosition> acceptor = _mod.getParentEdges().getFirst().getLinkages();
		
		/* define start rep */
		end = backbone2node.get(_mod.getParentEdges().getLast().getBackbone());

		/* define end rep */
		start = backbone2node.get(_mod.getParentEdges().getFirst().getBackbone());

		if (!current.equals(end) /*&& !start.equals(end)*/) return;
		
		/*if (_backbone.getParentEdges().isEmpty() && current.equals(end)) {
			LinkedList<LinkagePosition> linkTemp = donor;
			donor = acceptor;
			acceptor = linkTemp;

			Node temp = end;
			end = start;
			start = temp;
		}*/

		Edge parentEdge = WURCSEdgeToEdge(donor, acceptor);

		parentEdge.setSubstituent(makeSubstituentWithRepeat(_mod));

		for (Edge edge : end.getChildEdges()) {
			if (start.equals(edge.getChild())) return;
		}

		glyCo.addNode(end, parentEdge, start);
	}

	private void extractCyclicUnit(Backbone _backbone, WURCSEdge _cEdge, Modification _mod) throws GlycanException {
		if (!_cEdge.getNextComponent().getChildEdges().isEmpty()) return;
		if (_mod instanceof ModificationRepeat) return;

		Node start = backbone2node.get(_backbone);
		Node end = null;

		Edge cyclicEdge = new Edge();
		Linkage lin = new Linkage();

		/* extract end cyclic node */
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

	private void compositionToUndefinedUnit (Backbone _backbone) throws GlycanException {
		Node current = backbone2node.get(_backbone);
		und = new GlycanUndefinedUnit();
		und.addNode(current);

		glyCo.addGlycanUndefinedUnit(und);
	}

	private void backboneToUndefinedUnit(Backbone _backbone) throws GlycanException {
		Node current = backbone2node.get(_backbone);
		GlycanUndefinedUnit fragment = new GlycanUndefinedUnit();

		/* add parent node in core structure */
		LinkedList<LinkagePosition> acceptor = null;
		LinkedList<LinkagePosition> donor = null;

		ArrayList<WURCSEdge> fragments = new ArrayList<>();

		for (WURCSEdge cEdge : _backbone.getChildEdges()) {
			if (!(cEdge.getNextComponent() instanceof ModificationAlternative)) continue;
			fragments.add(cEdge);
		}

		for (Iterator<WURCSEdge> iterWG = fragments.iterator(); iterWG.hasNext();) {

			WURCSEdge modAlt = iterWG.next();

			for (WURCSEdge cpEdge : modAlt.getNextComponent().getParentEdges()) {
				if (_backbone.equals(cpEdge.getBackbone())) {
					if (cpEdge.getModification().getMAPCode().equals("")) continue;
					if (cpEdge.getModification().getParentEdges().size() < 2) continue;
				}

				if (!cpEdge.getModification().getMAPCode().equals("")) {
					current = new Substituent(MAPToInterface(cpEdge.getModification().getMAPCode()));
					donor = cpEdge.getLinkages();
					acceptor = donor;
				}

				if (donor == null) donor = cpEdge.getLinkages();

				fragment.addParentNode( backbone2node.get(cpEdge.getBackbone()));
			}

			if (acceptor == null && !isSubstituentEdge(modAlt)) acceptor = modAlt.getLinkages();

			if (iterWG.hasNext()) fragment = new GlycanUndefinedUnit();
		}

		Edge parentEdge = WURCSEdgeToEdge(donor, acceptor);
		parentEdge.setChild(current);

		current.addParentEdge(parentEdge);
		fragment.setConnection(parentEdge);
		fragment.addNode(current);

		glyCo.addGlycanUndefinedUnit(fragment);
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

	private Substituent makeSubstituentWithRepeat (Modification _mod) throws GlycanException {
		CrossLinkedTemplate crossTemp = (CrossLinkedTemplate) MAPToInterface(_mod.getMAPCode());

		GlycanRepeatModification ret = new GlycanRepeatModification(crossTemp);

		ModificationRepeat repMod = (_mod instanceof  ModificationRepeat) ? (ModificationRepeat) _mod : null;

		ret.setMinRepeatCount(repMod == null ? 0 : repMod.getMinRepeatCount());
		ret.setMaxRepeatCount(repMod == null ? 0 : repMod.getMaxRepeatCount());

		SubstituentUtility subUtil = new SubstituentUtility();
		ret = (GlycanRepeatModification) subUtil.modifyLinkageType(ret);

		return ret;
	}

	private Substituent MAPToSubstituent(Modification _mod) throws GlycanException {
		if(_mod.getMAPCode().equals("")) return null;

		return new Substituent(MAPToInterface(_mod.getMAPCode()));
	}

	private SubstituentInterface MAPToInterface (String _map) throws GlycanException {
		if(_map.equals("")) return null;
		SubstituentInterface ret = null;
		if(SubstituentTemplate.forMAP(_map) != null) {
			ret = SubstituentTemplate.forMAP(_map);
		}
		if(CrossLinkedTemplate.forMAP(_map) != null) {
			ret = CrossLinkedTemplate.forMAP(_map);
		}

		if(ret == null) throw new GlycanException(_map +" could not found !");
		return ret;
	}

	private LinkedList<Backbone> sortNodes (WURCSGraph _graph) throws WURCSVisitorException {
		LinkedList<Backbone> ret = new LinkedList<Backbone>();
		WURCSVisitorCollectSequence w = new WURCSVisitorCollectSequence();
		w.start(_graph);

		for (WURCSComponent wc : w.getNodes()) {
			if (wc instanceof Backbone) ret.add(((Backbone) wc));
		}

		return ret;
	}

	private void extractAntennae (Backbone _backbone) throws GlycanException {
		if (!_backbone.isRoot()) return;

		/* Extract root of glycan fragments */
		for (WURCSEdge cEdge : _backbone.getChildEdges()) {

			if (!(cEdge.getNextComponent() instanceof ModificationAlternative)) continue;

			for (WURCSEdge cpEdge : cEdge.getNextComponent().getParentEdges()) {
				if (!cpEdge.getBackbone().isRoot()) continue;
				if (isCrossLinkedSubstituent(cpEdge.getModification())) continue;
				if (root.equals(cpEdge.getBackbone())) {
					if (!cpEdge.getModification().getMAPCode().equals("") && (cEdge.getNextComponent().getParentEdges().size()) > 1) {
						antennae.add(cpEdge.getBackbone());
					}
				} else {
					if (cEdge.getNextComponent().getParentEdges().size() - 2 > 0 && !antennae.contains(cpEdge.getBackbone())) {
						antennae.add(cpEdge.getBackbone());
					}
				}
			}
		}
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

		for (Backbone bb : antennae) {
			for (WURCSEdge edge : bb.getChildEdges()) {
				if (edge.getModification().isRing()) continue;
				for (WURCSEdge pp : edge.getNextComponent().getParentEdges()) {
					if (_backbone.equals(pp.getBackbone())) {
						ret = true;
						break;
					}
				}
			}
		}

		return ret;
	}

	private boolean isCrossLinkedSubstituent (Modification _mod) throws GlycanException {
		if (_mod.getMAPCode().equals("")) return false;
		return (MAPToInterface(_mod.getMAPCode()) instanceof CrossLinkedTemplate);
	}

	private boolean isFlipFlop (Backbone _parent, Backbone _child) {
		if (!_parent.isRoot() || root.equals(_parent) || antennae.contains(_parent)) return false;
		return (backbone2node.get(_parent).getParentEdges().isEmpty());
		//return (!backbone2node.get(_child).getParentEdges().isEmpty() && backbone2node.get(_parent).getParentEdges().isEmpty());
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