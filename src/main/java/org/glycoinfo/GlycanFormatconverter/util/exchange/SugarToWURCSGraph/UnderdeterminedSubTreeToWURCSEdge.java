package org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph;

import java.util.ArrayList;

import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;
import org.glycoinfo.WURCSFramework.wurcs.graph.LinkagePosition;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSEdge;

/**
 * Class for converting UnderdeterminedSubTree to WURCSEdge and Modification
 * @author MasaakiMatsubara
 *
 */
public class UnderdeterminedSubTreeToWURCSEdge extends GlycoEdgeToWURCSEdge {

	private ArrayList<Monosaccharide> m_aParents = new ArrayList<Monosaccharide>();

	@Override
	public Monosaccharide getParent() {
		return this.m_aParents.get(0);
	}

	public ArrayList<Monosaccharide> getParents() {
		return this.m_aParents;
	}

	public boolean isAleternative() {
		return (this.m_aParents.size() > 1);
	}

	public void start(UnderdeterminedSubTree a_oSubTree) throws WURCSExchangeException {
		GlycoNode t_oRootNode = null;
		try {
			t_oRootNode = a_oSubTree.getRootNodes().get(0);
		} catch (GlycoconjugateException e) {
			throw new WURCSExchangeException(e.getMessage());
		}

		// Check parent nodes
		for ( GlycoNode t_oParent : a_oSubTree.getParents() ) {
			if ( t_oParent instanceof Monosaccharide ) {
				this.m_aParents.add( (Monosaccharide)t_oParent );
				continue;
			}
			// Throw exception if there are substituents in parents of subgraph
			// Root node of subgraph is substituent
			if ( t_oRootNode instanceof Substituent )
				throw new WURCSExchangeException("Substituent cannot connect to substituent.");
			// Root node of subgraph is not substituent
			throw new WURCSExchangeException("Substituent cannot be parent of underdetermined subtree.");
		}

		// Check root node
		if ( t_oRootNode instanceof Substituent && t_oRootNode.getChildEdges().size() > 1 )
			throw new WURCSExchangeException("Substituent having two or more children is NOT handled in the system.");

		// Set subtree connection
		this.setLinkage( a_oSubTree.getConnection() );

		this.setChild(t_oRootNode);
		this.makeModificaiton();

		this.setWURCSEdge(true);
		try {
			for ( WURCSEdge t_oEdge : this.getParentEdges() ) {
				for ( LinkagePosition t_oLinkPos : t_oEdge.getLinkages() ) {
//					if ( a_oSubTree.getProbabilityLower() == 100.0 ) continue;
					t_oLinkPos.setProbabilityLower( a_oSubTree.getProbabilityLower() / 100 );
					t_oLinkPos.setProbabilityUpper( a_oSubTree.getProbabilityUpper() / 100);
					t_oLinkPos.setProbabilityPosition( LinkagePosition.MODIFICATIONSIDE );
				}
			}
		} catch (WURCSException e) {
			throw new WURCSExchangeException(e.getErrorMessage());
		}

		// Root node is subtituent and not have children
		if ( t_oRootNode instanceof Substituent && t_oRootNode.getChildNodes().isEmpty() ) return;

		this.setWURCSEdge(false);
	}

	public void clear() {
		this.m_aParents = new ArrayList<Monosaccharide>();
	}
}
