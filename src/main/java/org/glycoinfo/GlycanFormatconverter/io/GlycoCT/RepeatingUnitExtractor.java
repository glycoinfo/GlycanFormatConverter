package org.glycoinfo.GlycanFormatconverter.io.GlycoCT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.Linkage;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;

/**
 * Class for extracting repeating units from GlyContainer.
 * Method .start() must be used to start.
 * @author Masaaki Matsubara
 *
 */
public class RepeatingUnitExtractor {

	private List<Edge> m_lSortedRepEdges;
	private Map<Edge, List<Edge>> m_mapRepToNestedEdges;
	private Map<Edge, List<Edge>> m_mapNestingToNestedRepEdges;

	public RepeatingUnitExtractor() {
		this.m_lSortedRepEdges = new ArrayList<>();
		this.m_mapRepToNestedEdges = new HashMap<>();
		this.m_mapNestingToNestedRepEdges = new HashMap<>();
	}

	/**
	 * Get repeat Edges sorted from inside to outside of the nesting
	 * @return List of sorted repeat Edges
	 */
	public List<Edge> getSortedRepeatEdges() {
		return this.m_lSortedRepEdges;
	}

	/**
	 * Get list of Edges in a repeating unit indicated by the given Edge
	 * @param _edgeRep Edge for a repeating unit
	 * @return List of Edges in a repeating unit indicated by the given Edge
	 */
	public List<Edge> getNestedEdges(Edge _edgeRep) {
		if ( !this.m_mapRepToNestedEdges.containsKey(_edgeRep) )
			return null;
		return this.m_mapRepToNestedEdges.get(_edgeRep);
	}

	/**
	 * Start extracting nested edges inside of repeating unit indicated by the given Edges
	 * @param _lRepeatEdges List of repeat Edges
	 */
	public void start(List<Edge> _lRepeatEdges) {
		
		List<Edge> t_lRepeatEdges = new ArrayList<>();
		for ( Edge t_edge : _lRepeatEdges ) {
			if ( !t_edge.isRepeat() )
				continue;
			if ( t_lRepeatEdges.contains(t_edge) )
				continue;
			this.extractRepeatUnit(t_edge);
			t_lRepeatEdges.add(t_edge);
		}
		for ( Edge t_edgeRep : t_lRepeatEdges ) {
			System.out.println( "Edge-"+Integer.toHexString(t_edgeRep.hashCode())+": "
				+Integer.toHexString( ((Monosaccharide)t_edgeRep.getParent()).hashCode() ) +"-"
				+Integer.toHexString( ((Monosaccharide)t_edgeRep.getChild()).hashCode() )
			);
			for ( Linkage t_link : t_edgeRep.getGlycosidicLinkages() )
				System.out.println( t_link.getParentLinkages()+" - "+t_link.getChildLinkages() );
			for ( Edge t_edgeNested : this.m_mapRepToNestedEdges.get(t_edgeRep) ) {
				if ( t_edgeNested.getChild() == null )
					continue;
				System.out.println( "Nested edge-"+Integer.toHexString(t_edgeNested.hashCode())+": "
						+Integer.toHexString( ((Monosaccharide)t_edgeNested.getParent()).hashCode() ) +"-"
						+Integer.toHexString( ((Monosaccharide)t_edgeNested.getChild()).hashCode() )
					);
			}
		}

		final Map<Edge, List<Edge>> t_mapNestingToNestedRepEdges = this.m_mapNestingToNestedRepEdges;
		// Sort repeat edges
		Collections.sort(t_lRepeatEdges, new Comparator<Edge>(){

			@Override
			public int compare(Edge o1, Edge o2) {
				// For nested count
				int t_nNesting1 = 0;
				int t_nNesting2 = 0;
				for ( Edge t_edgeRep : t_mapNestingToNestedRepEdges.keySet() ) {
					List<Edge> t_lNestedRepEdges = t_mapNestingToNestedRepEdges.get(t_edgeRep);
					if ( t_lNestedRepEdges.contains(o1) )
						t_nNesting1++;
					if ( t_lNestedRepEdges.contains(o2) )
						t_nNesting2++;
				}
				if ( t_nNesting1 != t_nNesting2 )
					return t_nNesting2 - t_nNesting1;

				// For nesting count
				int t_nNested1 = 0;
				if ( t_mapNestingToNestedRepEdges.containsKey(o1) )
					t_nNested1 = t_mapNestingToNestedRepEdges.get(o1).size();
				int t_nNested2 = 0;
				if ( t_mapNestingToNestedRepEdges.containsKey(o2) )
					t_nNested2 = t_mapNestingToNestedRepEdges.get(o2).size();
				if ( t_nNested1 != t_nNested2 )
					return t_nNested1 - t_nNested2;

				return 0;
			}
			
		});

		this.m_lSortedRepEdges = t_lRepeatEdges;
	}

	private void extractRepeatUnit(Edge _edgeRep) {
		if ( this.m_mapRepToNestedEdges.containsKey(_edgeRep) )
			return;

		Node t_nodeStart = _edgeRep.getChild(); // Start of repeat
		Node t_nodeEnd = _edgeRep.getParent(); // End of repeat
		Edge t_edgeHead = getHeadEdge(_edgeRep);
		Edge t_edgeTail = getTailEdge(_edgeRep);

		// Collect all edges in the repeat unit
		LinkedList<Node> t_lQueueNodes = new LinkedList<>();
		List<Edge> t_lEdgesRepUnit = new ArrayList<>();

		List<Edge> t_lNestedRepEdges = new ArrayList<>();
		t_lQueueNodes.add(t_nodeStart);
		while ( !t_lQueueNodes.isEmpty() ) {
			Node t_end = t_lQueueNodes.removeFirst();
			// Get all repeat edges inside of the repeat unit
			if ( this.isRepeatStart(t_end) )
				t_lNestedRepEdges.addAll( this.getRepeatEdges(t_end, true) );

			// Add child nodes to queue
			for ( Edge t_edge : t_end.getChildEdges() ) {
				if ( t_edge.equals(_edgeRep) )
					continue;
				// No repeat edges
				if ( t_edge.isRepeat() )
					continue;
				// Don't get tail edge
				if ( t_edge.equals(t_edgeTail) )
					continue;
				t_lEdgesRepUnit.add(t_edge);
				if ( t_edge.getChild() != null )
					t_lQueueNodes.add(t_edge.getChild());
			}
		}
		this.m_mapRepToNestedEdges.put(_edgeRep, t_lEdgesRepUnit);
		if ( t_lNestedRepEdges.isEmpty() ) {
			return;
		}

		// For all nested repeat edge
		for ( Edge t_edgeNestedRep : t_lNestedRepEdges ) {
			if ( t_edgeNestedRep.equals(_edgeRep) )
				continue;
			// Associate nested and nesting repeat edges
			if ( !this.m_mapNestingToNestedRepEdges.containsKey(_edgeRep) )
				this.m_mapNestingToNestedRepEdges.put(_edgeRep, new ArrayList<Edge>());
			if ( !this.m_mapNestingToNestedRepEdges.get(_edgeRep).contains(t_edgeNestedRep) )
				this.m_mapNestingToNestedRepEdges.get(_edgeRep).add(t_edgeNestedRep);

			// Remove duplicated edges
			if ( !this.m_mapRepToNestedEdges.containsKey(t_edgeNestedRep) )
				this.extractRepeatUnit(t_edgeNestedRep);
			List<Edge> t_lNestedEdges = this.m_mapRepToNestedEdges.get(t_edgeNestedRep);
			for ( Edge t_edgeNested : t_lNestedEdges )
				if ( t_lEdgesRepUnit.contains(t_edgeNested) )
					t_lEdgesRepUnit.remove(t_edgeNested);
		}
	}

	/**
	 * Get head Edge of a repeating unit indicated by the given Edge.
	 * Return null if no head Edge.
	 * @param _edgeRep Edge indicating a repeating unit
	 * @return Edge of the head of repeating unit (null if no head Edge)
	 */
	public static Edge getHeadEdge(Edge _edgeRep) {
		Node t_nodeStart = _edgeRep.getChild(); // Start of repeat
		List<Linkage> t_lLinksRep = _edgeRep.getGlycosidicLinkages();
		for ( Edge t_edgeParent : t_nodeStart.getParentEdges() ) {
			if ( t_edgeParent.equals(_edgeRep) )
				continue;
			if ( t_edgeParent.isRepeat() )
				continue;
			List<Linkage> t_lLinks = t_edgeParent.getGlycosidicLinkages();
			if ( isSameLinkages(t_lLinksRep, t_lLinks) )
				return t_edgeParent;
		}
		return null;
	}

	/**
	 * Get tail Edge of a repeating unit indicated by the given Edge.
	 * Return null if no tail Edge.
	 * @param _edgeRep Edge indicating a repeating unit
	 * @return Edge of the tail of repeating unit (null if no head Edge)
	 */
	public static Edge getTailEdge(Edge _edgeRep) {
		Node t_nodeEnd = _edgeRep.getParent(); // End of repeat
		List<Linkage> t_lLinksRep = _edgeRep.getGlycosidicLinkages();
		for ( Edge t_edgeChild : t_nodeEnd.getChildEdges() ) {
			if ( t_edgeChild.equals(_edgeRep) )
				continue;
			if ( t_edgeChild.isRepeat() )
				continue;
			List<Linkage> t_lLinks = t_edgeChild.getGlycosidicLinkages();
			if ( isSameLinkages(t_lLinksRep, t_lLinks) )
				return t_edgeChild;
		}
		return null;

	}

	private static boolean isSameLinkages(List<Linkage> _lLinksRep, List<Linkage> _lLinks) {
		if ( _lLinksRep.size() != _lLinks.size() )
			return false;
		int t_nMatchCount = 0;
		for ( Linkage t_link1 : _lLinksRep ) {
			for ( Linkage t_link2 : _lLinks ) {
				if ( isSameLinkage(t_link1, t_link2) )
					t_nMatchCount++;
			}
		}
		if ( _lLinksRep.size() != t_nMatchCount )
			return false;
		return true;
	}

	private static boolean isSameLinkage(Linkage _linkRep, Linkage _link) {
		// Compare child positions
		if ( !isSameLinkagePositions(_linkRep.getChildLinkages(), _link.getChildLinkages()) )
			return false;

		// Compare parent positions
		if ( !isSameLinkagePositions(_linkRep.getParentLinkages(), _link.getParentLinkages()) )
			return false;

		return true;
	}

	private static boolean isSameLinkagePositions(List<Integer> _lLinkPos1, List<Integer> _lLinkPos2) {
		if ( _lLinkPos1 == null && _lLinkPos2 != null )
			return false;
		if ( _lLinkPos1 != null && _lLinkPos2 == null )
			return false;
		if ( _lLinkPos1 != null && _lLinkPos2 != null ) {
			if ( _lLinkPos1.size() != _lLinkPos2.size() )
				return false;
			Collections.sort(_lLinkPos1);
			Collections.sort(_lLinkPos2);
			for ( int i=0; i<_lLinkPos1.size(); i++ ) {
				int t_iPos1 = _lLinkPos1.get(i);
				int t_iPos2 = _lLinkPos2.get(i);
				if ( t_iPos1 != t_iPos2 )
					return false;
			}
		}
		return true;
	}

	private List<Edge> getRepeatEdges(Node _node, boolean _isStart) {
		List<Edge> t_lRepeatEdges = new ArrayList<>();
		if ( _isStart && this.isRepeatStart(_node) )
			for ( Edge t_edgeRep : _node.getParentEdges() ) {
				if ( !t_edgeRep.isRepeat() )
					continue;
				if ( t_lRepeatEdges.contains(t_edgeRep) )
					continue;
				t_lRepeatEdges.add(t_edgeRep);
			}

		if ( !_isStart && this.isRepeatEnd(_node) )
			for ( Edge t_edgeRep : _node.getChildEdges() ) {
				if ( !t_edgeRep.isRepeat() )
					continue;
				if ( t_lRepeatEdges.contains(t_edgeRep) )
					continue;
				t_lRepeatEdges.add(t_edgeRep);
			}

		return t_lRepeatEdges;
	}

	private boolean isRepeatStart(Node _node) {
		for ( Edge t_edge : _node.getParentEdges() )
			if ( t_edge.isRepeat() )
				return true;
		return false;
	}

	private boolean isRepeatEnd(Node _node) {
		for ( Edge t_edge : _node.getChildEdges() )
			if ( t_edge.isRepeat() )
				return true;
		return false;
	}


}
