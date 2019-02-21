package org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;

public class GlycoEdgeAnalyzer {

	public static GlycoEdge getParentEdge(GlycoEdge a_oEdge) {
		GlycoNode t_oParent = a_oEdge.getParent();
		if ( t_oParent == null ) return null;

		GlycoEdge t_oParentLinkage = a_oEdge;
		// Search node for parent node if parent is repeating unit
		while ( t_oParent instanceof SugarUnitRepeat ) {
			t_oParentLinkage = ((SugarUnitRepeat)t_oParent).getRepeatLinkage();
			t_oParent = t_oParentLinkage.getParent();
		}
		return t_oParentLinkage;
	}

	public static GlycoEdge getChildEdge(GlycoEdge a_oEdge) {
		GlycoNode t_oChild = a_oEdge.getChild();
		if ( t_oChild == null ) return null;

		GlycoEdge t_oChildLinkage = a_oEdge;
		// Search node for child node if child is repeating unit or cyclic
		while ( t_oChild instanceof SugarUnitRepeat ) {
			// For repeating unit
			if ( t_oChild instanceof SugarUnitRepeat )
				t_oChildLinkage = ((SugarUnitRepeat)t_oChild).getRepeatLinkage();
				t_oChild = t_oChildLinkage.getChild();
		}
		return t_oChildLinkage;
	}

	public static GlycoNode getParentNode(GlycoEdge a_oEdge) {
		GlycoEdge t_oParentLinkage = GlycoEdgeAnalyzer.getParentEdge(a_oEdge);
		if ( t_oParentLinkage == null ) return null;

		return t_oParentLinkage.getParent();
	}

	public static GlycoNode getChildNode(GlycoEdge a_oEdge) {
		GlycoNode t_oChild = a_oEdge.getChild();
		if ( t_oChild == null ) return null;

		// Search node for child node if child is repeating unit or cyclic
		while ( t_oChild instanceof SugarUnitRepeat || t_oChild instanceof SugarUnitCyclic ) {
			// For cyclic
			if ( t_oChild instanceof SugarUnitCyclic )
				t_oChild = ((SugarUnitCyclic)t_oChild).getCyclicStart();

			// For repeating unit
			if ( t_oChild instanceof SugarUnitRepeat )
				t_oChild = ((SugarUnitRepeat)t_oChild).getRepeatLinkage().getChild();
		}
		return t_oChild;
	}

}
