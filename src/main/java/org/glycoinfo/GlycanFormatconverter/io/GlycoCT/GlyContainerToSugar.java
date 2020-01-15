package org.glycoinfo.GlycanFormatconverter.io.GlycoCT;

import org.eurocarbdb.MolecularFramework.sugar.*;
import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.Linkage;
import org.glycoinfo.GlycanFormatconverter.Glycan.LinkageType;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.GlycanFormatconverter.Glycan.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Class for exchanging GlycoContainer to Sugar.
 * Method .start() must be used to start.
 * @author Masaaki Matsubara
 *
 */
public class GlyContainerToSugar {

	private Sugar sugar;
	private Map<Node, GlycoNode> m_mapNodeToGlycoNode;
	private Map<SugarUnitRepeat, Edge> m_mapSugarUnitRepeatToEdge;

	private void init () {
		this.sugar = new Sugar();
		this.m_mapNodeToGlycoNode = new HashMap<>();
		this.m_mapSugarUnitRepeatToEdge = new HashMap<>();
	}

	public Sugar getConvertedSugar() {
		return this.sugar;
	}

	/**
	 * Start to create Sugar object from the given GlyContainer
	 * @param _glyCo GlyConainer to be converted
	 * @throws GlycoconjugateException
	 * @throws GlycanException
	 */
	public void start(GlyContainer _glyCo) throws GlycoconjugateException, GlycanException {
		init();

		// For undetermined units
		Map<GlycanUndefinedUnit, List<Node>> t_mapUndToNodes = new HashMap<>();
		Map<GlycanUndefinedUnit, List<Edge>> t_mapUndToEdges = new HashMap<>();
		int t_nNoParent = 0;
		for ( GlycanUndefinedUnit t_und : _glyCo.getUndefinedUnit() ) {
			// Throw exception if the unit has null parent connection
			if ( t_und.getConnection() == null )
				t_nNoParent++;
//				throw new GlycoconjugateException("UndefinedUnit without connection to parent node is not be handled.");
			for ( Node t_nodeRoot : t_und.getRootNodes() ) {
				if ( t_nodeRoot instanceof Substituent ) {
					GlycoNode t_sub = convertSubstituent((Substituent)t_nodeRoot);
					this.m_mapNodeToGlycoNode.put(t_nodeRoot, t_sub);
				}

				List<Edge> t_edges = new ArrayList<>();
				List<Node> t_nodes = new ArrayList<>();
				t_nodes.add(t_nodeRoot);
				LinkedList<Node> t_nodesQueue = new LinkedList<>();
				t_nodesQueue.add(t_nodeRoot);
				while ( !t_nodesQueue.isEmpty() ) {
					Node t_nodeParent = t_nodesQueue.removeFirst();
					for ( Edge t_edge : t_nodeParent.getChildEdges() ) {
						t_edges.add(t_edge);
						if ( t_edge.getChild() == null )
							continue;
						t_nodesQueue.add(t_edge.getChild());
						t_nodes.add(t_edge.getChild());
					}
				}
				t_mapUndToNodes.put(t_und, t_nodes);
				t_mapUndToEdges.put(t_und, t_edges);
			}
		}
		

		List<Edge> t_lCommonEdges = new ArrayList<>();
		List<Edge> t_lRepeatEdges = new ArrayList<>();
		List<Edge> t_lCyclicEdges = new ArrayList<>();
		for( Node _node : _glyCo.getAllNodes()) {
			// Map node to monosaccharide
			if ( _node instanceof Monosaccharide ) {
				Monosaccharide t_ms = (Monosaccharide)_node;
				// TODO: To be considered how handle the "unknown" status
				if ( t_ms.getAnomer() == AnomericStateDescriptor.UNKNOWN )
					throw new GlycanException("The monosaccharide which contains \"u\" or \"U\" can not be converted to GlycoCT");

				// Convert monosaccharide
				GlycoNode t_msConv = this.convertMonosaccharide(t_ms);
				this.m_mapNodeToGlycoNode.put( _node, t_msConv );
				// For MS without linkage
				if ( _node.getChildEdges().isEmpty() && _node.getParentEdges().isEmpty() ) {
					this.sugar.addNode(t_msConv);
					continue;
				}
			}


			// For edges
//			if ( _node.getParentEdges().size() > 1 )
//				throw new GlycoconjugateException("Multiple parent edges can not be handled.");
			if ( _node.getChildEdges().isEmpty() )
				continue;

			// For und sub tree
			GlycanUndefinedUnit t_undHost = null;
			for ( GlycanUndefinedUnit t_und : _glyCo.getUndefinedUnit() ) {
				if ( !t_mapUndToNodes.get(t_und).contains(_node) )
					continue;
				t_undHost = t_und;
				break;
			}

			// Collect edges and substituents
			for ( Edge t_childEdge : _node.getChildEdges() ) {
				if ( t_childEdge.getSubstituent() != null ) {
					// Map node to substituent
					Substituent t_subst = (Substituent)t_childEdge.getSubstituent();
					if ( t_subst.getSubstituent() != null
					  && !this.m_mapNodeToGlycoNode.containsKey(t_subst) )
						this.m_mapNodeToGlycoNode.put( t_subst, convertSubstituent(t_subst) );
				}

				// For und sub tree
				if ( t_undHost != null ) {
//					t_mapUndToEdges.get(t_undHost).add(t_childEdge);
					continue;
				}

				// For repeat edges
				if ( t_childEdge.isRepeat() ) {
					t_lRepeatEdges.add(t_childEdge);
					continue;
				}
				// For cyclic edges
				if ( t_childEdge.isCyclic() ) {
					t_lCyclicEdges.add(t_childEdge);
					continue;
				}

				// For normal edges
				t_lCommonEdges.add(t_childEdge);
			}
		}

		// For compositions
		if ( _glyCo.isComposition() ) {
			if ( _glyCo.getNumberOfUndefinedLinkages() != _glyCo.getAllNodes().size() - 1 )
				throw new GlycoconjugateException("It is not handled that the glycan with undefined linkages which number is not the same as the number of monosaccharide minus one.");

			// For all monosaccharides
			for ( GlycanUndefinedUnit t_und : _glyCo.getUndefinedUnit() ) {
				for ( Edge t_edge : t_mapUndToEdges.get(t_und) )
					this.addEdgeToGlycoGraph(t_edge, this.sugar);
				if ( t_mapUndToEdges.get(t_und).isEmpty() )
					for ( Node t_node : t_mapUndToNodes.get(t_und) )
						this.sugar.addNode( this.m_mapNodeToGlycoNode.get(t_node) );
			}

			// For undefined substituents
			for ( GlycanUndefinedUnit t_und : _glyCo.getUndefinedUnitsForSubstituent() ) {
				Substituent t_subst = (Substituent)t_und.getConnection().getSubstituent();
				UnderdeterminedSubTree t_undSub = new UnderdeterminedSubTree();
				t_undSub.addNode( convertSubstituent(t_subst) );
				t_undSub.setConnection( this.convertSubstituentEdge(t_subst, true) );

				this.sugar.addUndeterminedSubTree(t_undSub);
				for ( GlycoNode t_node : this.sugar.getNodes() ) {
					if ( t_node instanceof org.eurocarbdb.MolecularFramework.sugar.Substituent )
						continue;
					this.sugar.addUndeterminedSubTreeParent(t_undSub, t_node);
				}
			}
			return;
		}

		// For repeat edges
		RepeatingUnitExtractor t_repExtractor = new RepeatingUnitExtractor();
		t_repExtractor.start(t_lRepeatEdges);
		// Start from the most inside repeat units
		for ( Edge t_edgeRep : t_repExtractor.getSortedRepeatEdges() ) {
			// Create a SugarUnitRepeat
			SugarUnitRepeat t_unitRep = new SugarUnitRepeat();
			// Map Edge and SugarUnitRepeat
			this.m_mapSugarUnitRepeatToEdge.put(t_unitRep, t_edgeRep);
			// Set repeat count
			GlycanRepeatModification t_modRep = (GlycanRepeatModification)t_edgeRep.getSubstituent();
			t_unitRep.setMaxRepeatCount( t_modRep.getMaxRepeatCount() );
			t_unitRep.setMinRepeatCount( t_modRep.getMinRepeatCount() );

			// Add nodes to repeating unit
			for ( Edge t_edgeNested : t_repExtractor.getNestedEdges(t_edgeRep) ) {
				this.addEdgeToGlycoGraph(t_edgeNested, t_unitRep);
				// Remove nested edge from common
				t_lCommonEdges.remove(t_edgeNested);
			}
			// If no edge in the repeating unit
			if ( t_repExtractor.getNestedEdges(t_edgeRep).isEmpty() )
				t_unitRep.addNode( this.m_mapNodeToGlycoNode.get(t_edgeRep.getParent()) );

			// Create repeat linkage
			this.addEdgeToGlycoGraph(t_edgeRep, t_unitRep);

			// Map start and end nodes of repeating unit to SugarUnitRepeat
			this.m_mapNodeToGlycoNode.put(t_edgeRep.getParent(), t_unitRep);
			this.m_mapNodeToGlycoNode.put(t_edgeRep.getChild(), t_unitRep);

			// Add to Sugar if no linkages toward outside of the repeat unit
			if ( RepeatingUnitExtractor.getHeadEdge(t_edgeRep) == null
			  && RepeatingUnitExtractor.getTailEdge(t_edgeRep) == null )
				this.sugar.addNode(t_unitRep);
		}

		// For common edges
		for ( Edge _edge : t_lCommonEdges )
			this.addEdgeToGlycoGraph(_edge, this.sugar);

		// For cyclic edges
		for ( Edge _edgeCyclic : t_lCyclicEdges )
			this.addEdgeToGlycoGraph(_edgeCyclic, this.sugar);

		// For undefined units
		for ( GlycanUndefinedUnit t_und : _glyCo.getUndefinedUnit() ) {
			UnderdeterminedSubTree t_subTree = new UnderdeterminedSubTree();
			// Cut small errors in double value
			BigDecimal t_bdProbLower = new BigDecimal( t_und.getProbabilityLow() );
			if ( !t_bdProbLower.toPlainString().equals("-1") )
				t_bdProbLower = t_bdProbLower.setScale(2, RoundingMode.HALF_UP);
			BigDecimal t_bdProbUpper = new BigDecimal( t_und.getProbabilityHigh() );
			if ( !t_bdProbUpper.toPlainString().equals("-1") )
				t_bdProbUpper = t_bdProbUpper.setScale(2, RoundingMode.HALF_UP);
			t_subTree.setProbability( t_bdProbLower.doubleValue(), t_bdProbUpper.doubleValue() );
			GlycoEdge t_connection = this.convertGlycosidicEdge( t_und.getConnection() );
			if ( t_und.getRootNodes().get(0) instanceof Substituent ) {
				Substituent t_sub = (Substituent)t_und.getRootNodes().get(0);
				t_connection = this.convertSubstituentEdge(t_sub, true);
			}
			t_subTree.setConnection(t_connection);

			// Add nodes
			if ( t_mapUndToEdges.containsKey(t_und) && !t_mapUndToEdges.get(t_und).isEmpty() )
				for ( Edge t_edge : t_mapUndToEdges.get(t_und) )
					this.addEdgeToGlycoGraph(t_edge, t_subTree);
			else
				for ( Node t_node : t_mapUndToNodes.get(t_und) )
					t_subTree.addNode( this.m_mapNodeToGlycoNode.get(t_node) );

			// Add sub tree to sugar
			this.sugar.addUndeterminedSubTree(t_subTree);
			// Add parents of sub tree
			for ( Node t_nodeParent : t_und.getParents() ) {
				GlycoNode t_parent = this.m_mapNodeToGlycoNode.get(t_nodeParent);
				this.sugar.addUndeterminedSubTreeParent(t_subTree, t_parent);
			}
		}
	}

	/**
	 * Convert Monosaccharide to Monosaccharide of the MolecularFramework
	 * @param _ms Monosaccharide to be converted
	 * @return Monosaccharide of the MolecularFramework converted from the Monosaccharide
	 * @throws GlycoconjugateException
	 */
	private org.eurocarbdb.MolecularFramework.sugar.Monosaccharide convertMonosaccharide(Monosaccharide _ms) throws GlycoconjugateException {

		// For Superclass
		SuperClass enumSClass = _ms.getSuperClass();
		Superclass t_enumSuperclass = Superclass.forCAtomCount(enumSClass.getSize());

		// For Anomer
		AnomericStateDescriptor enumAnom = _ms.getAnomer();
		// Change anomer "?" to "x"
		if ( enumAnom == AnomericStateDescriptor.UNKNOWN )
			enumAnom = AnomericStateDescriptor.UNKNOWN_STATE;
		if ( enumSClass == SuperClass.SUG && enumAnom == AnomericStateDescriptor.OPEN )
			enumAnom = AnomericStateDescriptor.UNKNOWN_STATE;
		Anomer t_enumAnomer = Anomer.forSymbol(enumAnom.getAnomericState());

		org.eurocarbdb.MolecularFramework.sugar.Monosaccharide sugarMS
			= new org.eurocarbdb.MolecularFramework.sugar.Monosaccharide(t_enumAnomer, t_enumSuperclass);

		// For BaseType
		for ( String t_strStereo : _ms.getStereos() ) {
			// Skip if unknown sugar
			if ( t_enumSuperclass == Superclass.SUG )
				break;
			String t_stereo = t_strStereo;
			if ( t_stereo.length() == 3 )
				t_stereo = "x"+t_stereo;
			if ( t_stereo.contains("d/l-") )
				t_stereo = t_stereo.replace("d/l-", "x");
			BaseType t_bType = BaseType.forName(t_stereo);
			sugarMS.addBaseType(t_bType);
		}
		// For uncertain stereos
		if ( sugarMS.getBaseType().isEmpty() ) {
			if ( sugarMS.getSuperclass() == Superclass.TRI )
				sugarMS.addBaseType(BaseType.XGRO);
		}

		// For Ring
		int t_iStart = _ms.getRingStart();
		int t_iEnd   = _ms.getRingEnd();
		if ( _ms.getAnomer() == AnomericStateDescriptor.OPEN && t_iStart == -1) {
			t_iStart = 0;
			t_iEnd = 0;
		}
		if ( t_iStart != -1 && t_iEnd == -1 ) {
			t_iStart = -1;
		}
		if ( !_ms.getStereos().isEmpty() && _ms.getStereos().get(0).equals("Sugar") ) {
			t_iStart = -1;
			t_iEnd = -1;
		}
		sugarMS.setRing(t_iStart, t_iEnd);

		// For Modification
		List<GlyCoModification> t_lMods = new ArrayList<>();
		boolean t_bHasModification1 = false;
		boolean t_bHasAldi1 = false;
		boolean t_bIsAldose = false;
		boolean t_bIsKetose = false;
		boolean t_bIsKetoAldose = false;
		for ( GlyCoModification t_mod: _ms.getModifications() ) {
			// Skip unsatulated bond modification
			if ( t_mod.getModificationTemplate().getGlycoCTnotation().equals("en") )
				continue;
			if ( t_mod.getPositionOne() == 1 )
				t_bHasModification1 = true;
			// Ignore the modification hydroxyl
			if ( t_mod.getModificationTemplate() == ModificationTemplate.HYDROXYL ) {
				if ( enumAnom.getAnomericState() != 'o' )
					continue;
				if ( t_mod.getPositionOne() != 1 )
					continue;
				t_bHasAldi1 = true;
				continue;
			}
			if ( t_mod.getModificationTemplate() == ModificationTemplate.KETONE_U 
			  || t_mod.getModificationTemplate() == ModificationTemplate.KETONE ) {
				if ( _ms.getAnomericPosition() == 1 && t_mod.getPositionOne() != 1 )
					t_bIsKetoAldose = true;
				if ( t_mod.getPositionOne() == 1 ) {
					t_bIsAldose = true;
					continue;
				}
				t_bIsKetose = true;
			}
			if ( t_mod.getModificationTemplate() == ModificationTemplate.ALDEHYDE ) {
				if ( t_mod.getPositionOne() == 1 )
					t_bIsAldose = true;
				continue;
			}
			t_lMods.add(t_mod);
		}
		// Add invisible keto for anomer
		if ( !t_bHasModification1 && (_ms.getAnomericPosition() == 1 || _ms.getAnomericPosition() == -1) ) {
			t_bIsAldose = true;
		}
		if ( t_bIsAldose && t_bIsKetose )
			t_bIsKetoAldose = true;
//		if ( t_bIsKetoAldose && t_iAnomPos < 2 )
		if ( t_bIsKetoAldose )
			sugarMS.addModification( new Modification( "keto", 1 ) );
		// Add aldi if no aldehyde or ketone
		if ( t_bIsAldose || t_bIsKetose )
			t_bHasAldi1 = false;
		if ( t_bHasAldi1 )
			sugarMS.addModification( new Modification( "aldi", 1 ) );

		for ( GlyCoModification t_mod: t_lMods ) {
			Modification t_modSugar = new Modification(
						t_mod.getModificationTemplate().getGlycoCTnotation(),
						t_mod.getPositionOne(),
						t_mod.getPositionTwo()
					);
			sugarMS.addModification(t_modSugar);
		}
		// For unsaturated bond modification
		int t_iPrev = 0;
		for ( int i=1; i<sugarMS.getSuperclass().getCAtomCount(); i++ ) {
			if ( i <= t_iPrev )
				continue;
			ModificationTemplate t_modTempFirst = null;
			ModificationTemplate t_modTempSecond = null;
			for ( GlyCoModification t_mod: _ms.getModifications() ) {
				if ( !t_mod.getModificationTemplate().getGlycoCTnotation().equals("en") )
					continue;
				if (t_mod.getPositionOne() == i )
					t_modTempFirst = t_mod.getModificationTemplate();
				if (t_mod.getPositionOne() == i+1)
					t_modTempSecond = t_mod.getModificationTemplate();
			}
			if ( t_modTempFirst == null || t_modTempSecond == null )
				continue;
			t_iPrev = i+1;
			// Determine "en" or "enx"
			boolean t_bHasStereo = false;
			boolean t_bIsEN = false;
			switch(t_modTempFirst) {
			case UNSATURATION_EL :
			case UNSATURATION_ZL :
				t_bHasStereo = true;
			case UNSATURATION_FL :
				t_bIsEN = true;
				Modification t_mod = new Modification("d", i);
				if ( i == 1 )
					t_mod = new Modification("aldi", i);
				sugarMS.addModification( t_mod );
				break;
			case UNSATURATION_EU :
			case UNSATURATION_ZU :
				t_bHasStereo = true;
				break;
			default:
				// Do nothing
				break;
			}
			switch(t_modTempSecond) {
			case UNSATURATION_EL :
			case UNSATURATION_ZL :
				t_bHasStereo = true;
			case UNSATURATION_FL :
				t_bIsEN = true;
				if ( i != sugarMS.getSuperclass().getCAtomCount()-1 )
					sugarMS.addModification( new Modification("d", i+1) );
				break;
			case UNSATURATION_EU :
			case UNSATURATION_ZU :
				t_bHasStereo = true;
				break;
			default:
				// Do nothing
				break;
			}
			if ( t_bHasStereo && (t_iStart == 0 || t_iStart == -1 ) )
				throw new GlycoconjugateException("Double bond stereo will be missed.");
			Modification t_mod = new Modification( "enx", i, i+1 );
			if ( t_bIsEN )
				t_mod = new Modification( "en", i, i+1 );
			sugarMS.addModification(t_mod);
		}

		return sugarMS;
	}

	/**
	 * Convert Substituent to Substituent of the MolecularFramework
	 * @param _subst Substituent to be converted
	 * @return Substituent of MolecularFramework converted from the given Substituent
	 * @throws GlycoconjugateException
	 */
	private org.eurocarbdb.MolecularFramework.sugar.Substituent convertSubstituent(Substituent _subst) throws GlycoconjugateException {
		// Construct Substituent with SubstituentType
		return new org.eurocarbdb.MolecularFramework.sugar.Substituent(
					SubstituentType.forName(_subst.getSubstituent().getglycoCTnotation())
				);
	}

	/**
	 * Add edges to the given GlycoGraph.
	 * The given Edge is converted to GlycoEdge and the parent and child GlycoNode are gotten from the map of Node to GlycoNode.
	 * If the edge has Substituent, the edge information in the Substituent is used for creating the GlycoEdge.
	 * And if the edge has child node, another edge is created and add to the given GlycoGraph.
	 * When the given linkages have probability, UnderdeterminedSubTree is created and added to Sugar with parent node.
	 * In that case, the given linkages are converted to GlycoEdge for a connection of theUnderdeterminedSubTree.
	 * @param _edge Edge to be converted to GlycoEdge
	 * @param _graph GlycoGraph for storing the edge information
	 * @throws GlycoconjugateException
	 * @throws GlycanException
	 */
	private void addEdgeToGlycoGraph(Edge _edge, GlycoGraph _graph) throws GlycoconjugateException, GlycanException {

		// For edges connecting repeating unit nodes
		for ( Linkage t_link : _edge.getGlycosidicLinkages() ) {
			if ( this.m_mapNodeToGlycoNode.get(_edge.getChild()) instanceof SugarUnitRepeat )
				t_link.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
			if ( this.m_mapNodeToGlycoNode.get(_edge.getParent()) instanceof SugarUnitRepeat )
				t_link.setParentLinkageType(LinkageType.NONMONOSACCHARIDE);
		}

		// For edge without substituent
		if ( _edge.getSubstituent() == null || ((Substituent)_edge.getSubstituent()).getSubstituent() == null ) {
			this.addGlycosidicLinkageToGlycoGraph(_edge, _graph);
			return;
		}

		// For cyclic edges
		if ( _edge.isCyclic() ) {
			GlycoNode t_parent = this.m_mapNodeToGlycoNode.get(_edge.getParent());
			GlycoNode t_child  = this.m_mapNodeToGlycoNode.get(_edge.getChild());
			GlycoEdge t_glycoEdge = this.convertGlycosidicEdge(_edge);
			((Sugar) _graph).addCyclic(t_parent, t_glycoEdge, t_child);
			return;
		}

		// For substituent
		Substituent t_subst = (Substituent)_edge.getSubstituent();
		GlycoEdge t_glycoEdge = this.convertSubstituentEdge(t_subst, true);
		if ( t_glycoEdge.getGlycosidicLinkages().isEmpty() ) {
			for ( Linkage t_link : _edge.getGlycosidicLinkages() ) {
				Linkage t_link0 = this.normalizeLinkageTypeForSubstituent(t_subst, t_link, true);
				t_glycoEdge.addGlycosidicLinkage( convertLinkage(t_link0) );
			}
		}
		GlycoNode t_child  = this.m_mapNodeToGlycoNode.get(t_subst);
		Node t_parentNode = t_subst.getParentNode();
		if ( t_parentNode == null )
			t_parentNode = _edge.getParent();
		GlycoNode t_parent = this.m_mapNodeToGlycoNode.get(t_parentNode);

		// For child edge of a repeating unit
		// Ignore duplicated substituent in the repeating unit
		boolean t_bSkipSubst = false;
		if ( t_parent instanceof SugarUnitRepeat ) {
			Edge t_edgeRepeat = this.m_mapSugarUnitRepeatToEdge.get((SugarUnitRepeat)t_parent);
			Substituent t_substRepeat = (Substituent)t_edgeRepeat.getSubstituent();
			
			if ( t_substRepeat != null && t_substRepeat.getSubstituent() != null
			  && t_subst.getSubstituent().getglycoCTnotation().equals(t_substRepeat.getSubstituent().getglycoCTnotation()) ) {
				t_bSkipSubst = true;
			}
		}

		// For edge with probability
		GlycoGraph t_graph = _graph;
		if ( this.hasProbability(_edge) ) 
			t_graph = this.getUndeterminedSubTreeWithProbability(_graph, _edge.getGlycosidicLinkages().get(0), t_parent, t_glycoEdge, t_child);
		else if ( this.hasProbability(t_subst) )
			t_graph = this.getUndeterminedSubTreeWithProbability(_graph, t_subst.getFirstPosition(), t_parent, t_glycoEdge, t_child);
		if ( !t_graph.equals(_graph) )
			t_bSkipSubst = true;

		// Skip addition of the substituent linkage
		// when underdetermined sub tree is generated
		// or parent repeating unit has substituent in the repeat edge
		if ( !t_bSkipSubst )
			t_graph.addNode(t_parent, t_glycoEdge, t_child);

		if ( _edge.getChild() == null )
			return;

		// Create another edge if the edge bridged with the susbtituent
		t_glycoEdge = this.convertSubstituentEdge(t_subst, false);
		if ( t_glycoEdge.getGlycosidicLinkages().isEmpty() ) {
			for ( Linkage t_link : _edge.getGlycosidicLinkages() ) {
				Linkage t_link0 = this.normalizeLinkageTypeForSubstituent(t_subst, t_link, false);
				t_glycoEdge.addGlycosidicLinkage( convertLinkage(t_link0) );
			}
		}
		t_child  = this.m_mapNodeToGlycoNode.get(_edge.getChild());
		if ( !t_bSkipSubst )
			t_parent = this.m_mapNodeToGlycoNode.get(t_subst);
		if ( _edge.isRepeat() ) {
			((SugarUnitRepeat)t_graph).setRepeatLinkage(t_glycoEdge, t_parent, t_child);
			return;
		}
		t_graph.addNode(t_parent, t_glycoEdge, t_child);
	}

	private void addGlycosidicLinkageToGlycoGraph(Edge _edge, GlycoGraph _graph) throws GlycoconjugateException, GlycanException {
		if ( _edge.getGlycosidicLinkages().isEmpty() )
			throw new GlycoconjugateException("Edge with no linkage can not be handled.");

		GlycoNode t_parent = this.m_mapNodeToGlycoNode.get(_edge.getParent());
		GlycoNode t_child  = this.m_mapNodeToGlycoNode.get(_edge.getChild());
		GlycoEdge t_glycoEdge = this.convertGlycosidicEdge(_edge);

		// For edge with probability
		if ( this.hasProbability(_edge) ) {
			this.getUndeterminedSubTreeWithProbability(_graph, _edge.getGlycosidicLinkages().get(0), t_parent, t_glycoEdge, t_child);
			return;
		}

		// Add residues with linkage
		if ( _edge.isRepeat() ) {
			((SugarUnitRepeat)_graph).setRepeatLinkage(t_glycoEdge, t_parent, t_child);
			return;
		}
		_graph.addNode(t_parent, t_glycoEdge, t_child);
		return;
	}

	/**
	 * Return UnderdeteminedSubTree object of the MolecularFramework
	 * created by the given Linkage, GlycoEdge.
	 * The object is set to the given GlycoGraph.
	 * @param _parentGraph GlycoGpaph for storing the UnderdeterminedSubTree
	 * @param _link Linkage having probabilities
	 * @param _parent GlycoNode, the parent node of the UnderdeterminedSubTree
	 * @param _connection GlycoEdge, the connection of the UnderdeterminedSubTree
	 * @param _child GlycoNode, the root node of the UnderdeterminedSubTree
	 * @return UnderdeterminedSubTree having information of the given objects
	 * @throws GlycoconjugateException
	 */
	private UnderdeterminedSubTree getUndeterminedSubTreeWithProbability(GlycoGraph _parentGraph, Linkage _link, GlycoNode _parent, GlycoEdge _connection, GlycoNode _child) throws GlycoconjugateException {
		BigDecimal t_bdProbLower = new BigDecimal( _link.getParentProbabilityLower() );
		if ( !t_bdProbLower.toPlainString().equals("-1") )
			t_bdProbLower = t_bdProbLower.multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
		BigDecimal t_bdProbUpper = new BigDecimal( _link.getParentProbabilityUpper() );
		if ( !t_bdProbUpper.toPlainString().equals("-1") )
			t_bdProbUpper = t_bdProbUpper.multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
//		double t_dProbLow  = _link.getParentProbabilityLower();
//		if ( t_dProbLow != -1 ) t_dProbLow *= 100;
//		double t_dProbHigh = _link.getParentProbabilityUpper();
//		if ( t_dProbHigh != -1 ) t_dProbHigh *= 100;
		UnderdeterminedSubTree t_subTree = new UnderdeterminedSubTree();
		t_subTree.setProbability(t_bdProbLower.doubleValue(), t_bdProbUpper.doubleValue());
		t_subTree.setConnection( _connection);

		if ( !_parentGraph.containsNode(_parent) )
			_parentGraph.addNode(_parent);
		if ( _parentGraph instanceof Sugar ) {
			((Sugar)_parentGraph).addUndeterminedSubTree(t_subTree);
			((Sugar)_parentGraph).addUndeterminedSubTreeParent(t_subTree, _parent);
		}
		if ( _parentGraph instanceof SugarUnitRepeat ) {
			((SugarUnitRepeat)_parentGraph).addUndeterminedSubTree(t_subTree);
			((SugarUnitRepeat)_parentGraph).addUndeterminedSubTreeParent(t_subTree, _parent);
		}
		t_subTree.addNode(_child);
		return t_subTree;
	}

	/**
	 * Return true if the given Edge has linkage(s) with probability
	 * @param _edge Substituent which may have linkage with probability
	 * @return true if the given Edge has linkage(s) with probability
	 */
	private boolean hasProbability(Edge _edge) {
		for ( Linkage t_link : _edge.getGlycosidicLinkages() ) {
			if ( t_link.getChildProbabilityLower() != 1.0 )
				return true;
			if ( t_link.getParentProbabilityLower() != 1.0 )
				return true;
		}
		return false;
	}

	/**
	 * Return true if the given Substituent has linkage(s) with probability
	 * @param _subst Substituent which may have linkage with probability
	 * @return true if the given Substituent has linkage(s) with probability
	 */
	private boolean hasProbability(Substituent _subst) {
		if ( _subst.getFirstPosition() == null )
			return false;
		if ( _subst.getFirstPosition().getChildProbabilityLower() != 1.0 )
			return true;
		if ( _subst.getFirstPosition().getParentProbabilityLower() != 1.0 )
			return true;
		if ( _subst.getSecondPosition() == null )
			return false;
		if ( _subst.getSecondPosition().getChildProbabilityLower() != 1.0 )
			return true;
		if ( _subst.getSecondPosition().getParentProbabilityLower() != 1.0 )
			return true;
		return false;
	}

	/**
	 * Convert the given Edge to GlycoEdge.
	 * @param _edge Edge to be converted
	 * @return GlycoEdge converted from the given Edge
	 * @throws GlycoconjugateException
	 * @throws GlycanException
	 */
	private GlycoEdge convertGlycosidicEdge(Edge _edge) throws GlycoconjugateException, GlycanException {
		GlycoEdge t_glycoEdge = new GlycoEdge();
		for ( Linkage t_link : _edge.getGlycosidicLinkages() ) {

			Linkage t_link0 = this.normalizeLinkageTypeForGlycosidicLinkage(t_link);

			t_glycoEdge.addGlycosidicLinkage( convertLinkage(t_link0) );
		}
		return t_glycoEdge;
	}

	/**
	 * Convert Edge containing in the given Substituent to GlycoEdge.
	 * @param _subst Substituent having Linkages
	 * @param _isChild Whether or not the Substituent is child
	 * @return GlycoEdge converted from Linkages of Substituent
	 * @throws GlycanException
	 * @throws GlycoconjugateException
	 */
	private GlycoEdge convertSubstituentEdge(Substituent _subst, boolean _isChild) throws GlycanException, GlycoconjugateException {
		GlycoEdge t_glycoEdge = new GlycoEdge();

		if ( _subst.getFirstPosition() == null)
			return t_glycoEdge;

		// For first position
		Linkage t_link = _subst.getFirstPosition();
		if ( t_link.getParentLinkages().isEmpty() || t_link.getChildLinkages().isEmpty() )
			return t_glycoEdge;
		Linkage t_linkNew = this.normalizeLinkageTypeForSubstituent(_subst, t_link, _isChild);
		t_glycoEdge.addGlycosidicLinkage( this.convertLinkage(t_linkNew) );

		if ( _subst.getSecondPosition() == null )
			return t_glycoEdge;

		t_link = _subst.getSecondPosition();
		if ( t_link.getParentLinkages().isEmpty() || t_link.getChildLinkages().isEmpty() )
			return t_glycoEdge;

		// For second position
		t_linkNew = this.normalizeLinkageTypeForSubstituent(_subst, t_link, _isChild);
		// Reset parent linkage type
		t_linkNew.setParentLinkageType( this.getLinkageTypeForSubstituent(_subst, false) );
		t_glycoEdge.addGlycosidicLinkage( this.convertLinkage(t_linkNew) );

		// Modify type for andhydro edges
		if ( _subst.getSubstituent().getglycoCTnotation().equals("anhydro") ) {
			t_glycoEdge.getGlycosidicLinkages().get(0).setParentLinkageType(
					org.eurocarbdb.MolecularFramework.sugar.LinkageType.DEOXY
				);
			t_glycoEdge.getGlycosidicLinkages().get(1).setParentLinkageType(
					org.eurocarbdb.MolecularFramework.sugar.LinkageType.H_AT_OH
				);
		}

		return t_glycoEdge;
	}

	/**
	 * Normalize the given linkage type on the given substituent. 
	 * @param _subst Substituent for the linkage
	 * @param _link Linkage to be normalized
	 * @param _isChild Whether or not the linkage is child side
	 * @return Linkage with normalized information
	 * @throws GlycanException
	 */
	private Linkage normalizeLinkageTypeForSubstituent(Substituent _subst, Linkage _link, boolean _isChild) throws GlycanException {
		Linkage t_link = new Linkage();
		LinkageType t_ltParent = _link.getParentLinkageType();
		LinkageType t_ltChild  = _link.getChildLinkageType();
		
		List<Integer> t_lParentPos = new ArrayList<>();
		for ( Integer t_pos : _link.getParentLinkages() )
			t_lParentPos.add(t_pos);
		List<Integer> t_lChildPos = new ArrayList<>();
		for ( Integer t_pos : _link.getChildLinkages() )
			t_lChildPos.add(t_pos);
		if (_isChild) {
			if ( t_ltParent == LinkageType.UNVALIDATED )
				t_ltParent = this.getLinkageTypeForSubstituent(_subst, _isChild);
			if ( t_ltChild  != LinkageType.NONMONOSACCHARIDE )
				t_ltChild = LinkageType.NONMONOSACCHARIDE;
			if ( t_lChildPos.size() == 1 )
				t_lChildPos.set(0, 1);
		} else {
			if ( t_ltChild  == LinkageType.UNVALIDATED )
				t_ltChild = this.getLinkageTypeForSubstituent(_subst, _isChild);
			if ( t_ltParent != LinkageType.NONMONOSACCHARIDE )
				t_ltParent = LinkageType.NONMONOSACCHARIDE;
			if ( t_lParentPos.size() == 1 )
				t_lParentPos.set(0, 1);
		}

		t_link.setParentLinkageType(t_ltParent);
		t_link.setChildLinkageType(t_ltChild);
		t_link.setParentLinkages(t_lParentPos);
		t_link.setChildLinkages(t_lChildPos);
		return t_link;
	}

	/**
	 * Get a LinkageType for the given Substituent.
	 * @param _subst Substituent for using the choice of LinkageType
	 * @param _isChild Whether or not the linkage is child
	 * @return LinkageType suitable for the given Substituent
	 */
	private LinkageType getLinkageTypeForSubstituent(Substituent _subst, boolean _isChild) {
		// For andydro
		if ( _subst.getSubstituent() == CrossLinkedTemplate.ANHYDROXYL )
			return ( _isChild )? LinkageType.H_AT_OH : LinkageType.DEOXY;
		if ( _subst.getSubstituent() == CrossLinkedTemplate.PHOSPHOETHANOLAMINE )
			return ( _isChild )? LinkageType.H_AT_OH : LinkageType.H_AT_OH;

		// Find oxygen next of the linkage point in the MAP

		// Remove numbers from MAP
		String t_strMAP = _subst.getSubstituent().getMAP().replaceAll("\\d", "");

		char t_cLinkage = ( _isChild )? t_strMAP.charAt(1) : t_strMAP.charAt( t_strMAP.indexOf("*", 1) - 1 );

		return (t_cLinkage == 'O')? LinkageType.H_AT_OH : LinkageType.DEOXY;
	}

	/**
	 * Normalize the given Linkage for glycosidic linkage.
	 * @param _link Linkage to be normalized
	 * @return Linkage normalized from the given Linkage
	 * @throws GlycanException
	 */
	private Linkage normalizeLinkageTypeForGlycosidicLinkage(Linkage _link) throws GlycanException {
		Linkage t_link = new Linkage();
		LinkageType t_ltParent = _link.getParentLinkageType();
		LinkageType t_ltChild = _link.getChildLinkageType();

		if ( t_ltParent == LinkageType.UNVALIDATED )
			t_ltParent = LinkageType.H_AT_OH;
		if ( t_ltChild == LinkageType.UNVALIDATED )
			t_ltChild = LinkageType.DEOXY;

		t_link.setParentLinkageType(t_ltParent);
		t_link.setChildLinkageType(t_ltChild);
		t_link.setParentLinkages(_link.getParentLinkages());
		t_link.setChildLinkages(_link.getChildLinkages());

		return t_link;
	}

	/**
	 * Convert the given Linkage to Linkage of the MolecularFramework
	 * @param _link Linkage to be converted
	 * @return Linkage of the MolecularFramework
	 * @throws GlycoconjugateException
	 */
	private org.eurocarbdb.MolecularFramework.sugar.Linkage convertLinkage(Linkage _link) throws GlycoconjugateException {
		org.eurocarbdb.MolecularFramework.sugar.Linkage t_sugarLink
			= new org.eurocarbdb.MolecularFramework.sugar.Linkage();

		LinkageType t_ltParent = _link.getParentLinkageType();
		LinkageType t_ltChild  = _link.getChildLinkageType();
		t_sugarLink.setParentLinkageType(
			org.eurocarbdb.MolecularFramework.sugar.LinkageType.forName( t_ltParent.getSymbol() )
		);
		t_sugarLink.setParentLinkages( _link.getParentLinkages() );
		t_sugarLink.setChildLinkageType(
			org.eurocarbdb.MolecularFramework.sugar.LinkageType.forName( t_ltChild.getSymbol() )
		);
		t_sugarLink.setChildLinkages( _link.getChildLinkages() );

		return t_sugarLink;
	}
}