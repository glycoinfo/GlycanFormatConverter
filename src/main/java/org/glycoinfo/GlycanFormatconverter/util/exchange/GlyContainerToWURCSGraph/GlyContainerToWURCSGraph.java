package org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.util.visitor.VisitorException;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;
import org.glycoinfo.WURCSFramework.util.graph.WURCSGraphNormalizer;
import org.glycoinfo.WURCSFramework.util.graph.visitor.WURCSVisitorCollectSequence;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;

public class GlyContainerToWURCSGraph {

	private WURCSGraph graph;
	private HashMap<Node, Backbone> node2Backbone = new HashMap<Node, Backbone>();

	public WURCSGraph getGraph () {
		return this.graph;
	}

	public void start (GlyContainer _glyCo) throws WURCSException, GlycanException {
		GlycoVisitorAnalyzeGlyCoForWURCS graphAnalyzer = new GlycoVisitorAnalyzeGlyCoForWURCS();

		try {
			graphAnalyzer.start(_glyCo);
		} catch (VisitorException e) {
			throw new WURCSExchangeException(e.getMessage());
		}

		graph = new WURCSGraph();

		/* convert monosaccharide */
		for(Node node : graphAnalyzer.getMonosaccharides()) {
			boolean isRootOfSubGraph = graphAnalyzer.getRootOfSubgraphs().contains(node);
			this.analyzerMonosaccharide(node, isRootOfSubGraph);
		}

		/* convert linkage */
		for (Edge edge : graphAnalyzer.getLinkages()) {
			EdgeToWURCSEdge edgeAnalyzer = new EdgeToWURCSEdge();
			edgeAnalyzer.start(edge);

			Modification mod = edgeAnalyzer.getModification();

			/* for repeating unit */
			if (graphAnalyzer.getRepeatingUnitByEdge(edge) != null) {
				ModificationRepeat repMod = new ModificationRepeat(mod.getMAPCode());
				GlycanRepeatModification gRepMod = graphAnalyzer.getRepeatingUnitByEdge(edge);
				repMod.setMinRepeatCount(gRepMod.getMinRepeatCount());
				repMod.setMaxRepeatCount(gRepMod.getMaxRepeatCount());
				mod = repMod;
			}

			Backbone parent = node2Backbone.get(edgeAnalyzer.getParent());
			Backbone child = node2Backbone.get(edgeAnalyzer.getChild());

			/* parent side */
			this.makeLinkage(parent, edgeAnalyzer.getParentEdges(), mod);

			/* child side */
			if (edgeAnalyzer.getChild() == null) continue;
			this.makeLinkage(child, edgeAnalyzer.getChildEdges(), mod);
		}

		/* for ambiguous structure */
		for (GlycanUndefinedUnit und : graphAnalyzer.getFragments()) {
			FragmentsToWURCSEdge frag2Edge = new FragmentsToWURCSEdge();
			frag2Edge.start(und);

			Modification mod = frag2Edge.getModification();
			if (frag2Edge.isAlternative()) {
				if (frag2Edge.getParentEdges().size() > 1) {
					throw new WURCSExchangeException("UnderdeterminedSubTree must have only one linkage to parents.");
				}

				ModificationAlternative alt = new ModificationAlternative(mod.getMAPCode());

				for (Monosaccharide parent : frag2Edge.getParents()) {
					LinkedList<WURCSEdge> parentEdges = new LinkedList<>();
					parentEdges.add(frag2Edge.getParentEdges().get(0).copy());

					Backbone backbone = node2Backbone.get(parent);
					makeLinkage(backbone, parentEdges, alt);

					alt.addLeadInEdge(parentEdges.getFirst());
				}

				mod = alt;
			} else {
				Backbone backbone = node2Backbone.get(frag2Edge.getChild());
				makeLinkage(backbone, frag2Edge.getChildEdges(), mod);
			}

			Backbone child = node2Backbone.get(frag2Edge.getChild());
			makeLinkage(child, frag2Edge.getChildEdges(), mod);
		}

		/* normalize */
		WURCSGraphNormalizer norm = new WURCSGraphNormalizer();
		norm.start(graph);
	}

	private void analyzerMonosaccharide (Node _node, boolean _isRootOfSubgraoh) throws WURCSExchangeException, GlycanException {
		Monosaccharide mono = (Monosaccharide) _node;
		MonosaccharideToBackbone mono2bb = new MonosaccharideToBackbone();
		if (_isRootOfSubgraoh) mono2bb.setRootOfSubgraph();

		mono2bb.start(_node);
		Backbone backbone = mono2bb.getBackbone();
		this.node2Backbone.put(mono, backbone);

		try {
			this.graph.addBackbone(backbone);
		} catch (WURCSException e) {
			throw new WURCSExchangeException(e.getErrorMessage());
		}

		for (Modification mod : mono2bb.getCoreModification()) {
			WURCSEdge wedge = new WURCSEdge();
			wedge.addLinkage(new LinkagePosition(-1, DirectionDescriptor.L, 0));
			if (mod.getMAPCode().lastIndexOf("*") > 0) {
				wedge.addLinkage(new LinkagePosition(-1, DirectionDescriptor.L, 0));
			}
			LinkedList<WURCSEdge> coreWEdges = new LinkedList<WURCSEdge>();
			coreWEdges.add(wedge);
			this.makeLinkage(backbone, coreWEdges, mod);
		}

		if (backbone.getAnomericPosition() == 0) return;
		if (backbone instanceof BackboneUnknown) return; 

		Modification ring = new Modification("");

		WURCSEdge start = new WURCSEdge();
		WURCSEdge end = new WURCSEdge();
		if (mono.getRingStart() != Monosaccharide.UNKNOWN_RING) {
			start.addLinkage(new LinkagePosition(mono.getRingStart(), DirectionDescriptor.L, 0));
			end.addLinkage(new LinkagePosition(mono.getRingEnd(), DirectionDescriptor.L, 0));
		} else if (backbone.getAnomericPosition() != Monosaccharide.UNKNOWN_RING) { 
			start.addLinkage(new LinkagePosition(backbone.getAnomericPosition(), DirectionDescriptor.L, 0));
			end.addLinkage(new LinkagePosition(Monosaccharide.UNKNOWN_RING, DirectionDescriptor.L, 0));
		} 

		LinkedList<WURCSEdge> edges = new LinkedList<WURCSEdge>();
		edges.add(start);
		edges.add(end);
		this.makeLinkage(backbone, edges, ring);
	}

	private void makeLinkage (Backbone _backbone, LinkedList<WURCSEdge> _edges, Modification _mod) throws WURCSExchangeException {
		try {
			for (WURCSEdge _edge : _edges) {
				this.graph.addResidues(_backbone, _edge, _mod);
			}
		} catch (WURCSException e) {
			throw new WURCSExchangeException(e.getErrorMessage());
		}
	}

	private boolean isFacingAnom (Edge _edge) {
		ArrayList<Integer> parentPos = null;
		ArrayList<Integer> childPos = null;
		Monosaccharide parent = (Monosaccharide) _edge.getParent();
		Monosaccharide child = (Monosaccharide) _edge.getChild();

		for (Linkage lin : _edge.getGlycosidicLinkages()) {
			parentPos = lin.getParentLinkages();
			childPos = lin.getChildLinkages();
		}

		if (parentPos.size() > 1 || childPos.size() > 1) return false;
		if (parentPos.isEmpty() || childPos.isEmpty()) return false;
		if (parent.getAnomericPosition() == -1 || child.getAnomericPosition() == -1) return false;
		return (parentPos.contains(parent.getAnomericPosition()) && childPos.contains(child.getAnomericPosition()));
	}
}
