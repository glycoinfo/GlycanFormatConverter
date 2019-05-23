package org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph;

import java.util.ArrayList;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanUndefinedUnit;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;
import org.glycoinfo.WURCSFramework.wurcs.graph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSEdge;

public class FragmentsToWURCSEdge extends EdgeToWURCSEdge {

	private ArrayList<Monosaccharide> parents = new ArrayList<Monosaccharide>();
	
	public Monosaccharide getParent() {
		return this.parents.get(0);
	}
	
	public ArrayList<Monosaccharide> getParents() {
		return this.parents;
	}

	public boolean isAlternative() {
		return (parents.size() > 1);
	}
	
	public void start (GlycanUndefinedUnit _und) throws WURCSException {
		if (_und.isComposition()) return;

		Node root;
		
		try {
			root = _und.getRootNodes().get(0);
		} catch (GlycanException e) {
			throw new WURCSExchangeException(e.getMessage());
		}
		
		for (Node parent : _und.getParents()) {
			if (parent instanceof Monosaccharide) {
				this.parents.add((Monosaccharide) parent);
				continue;
			}
			if (parent instanceof Substituent)
				throw new WURCSExchangeException("Substituent cannot connect to substituent.");
			throw new WURCSExchangeException("Substituent cannot be parent of underdetermined subtree.");
		}
		
		if (root instanceof Substituent && root.getChildEdges().size() > 1) {
			throw new WURCSExchangeException("Substituent having two or more children is NOT handled in the system.");
		}

		this.setLinkage(_und.getConnection());
		this.setChild(_und.getConnection());
		
		this.makeModification();
		this.setWURCSEdge(true);
	
		try {
			for (WURCSEdge wedge : this.getParentEdges()) {
				for (LinkagePosition lp : wedge.getLinkages()) {
					lp.setProbabilityLower(_und.getProbabilityLow() / 100);
					lp.setProbabilityUpper(_und.getProbabilityHigh() / 100);
					lp.setProbabilityPosition(LinkagePosition.MODIFICATIONSIDE);
				}
			}
		} catch (WURCSException e) {
			throw new WURCSExchangeException(e.getMessage());
		}
		
		/* for ambiguous substituent */
		if (root instanceof Substituent && root.getChildEdges().isEmpty()) return;
		
		this.setWURCSEdge(false);
	}
}
