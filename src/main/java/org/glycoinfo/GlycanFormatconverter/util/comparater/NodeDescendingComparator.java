package org.glycoinfo.GlycanFormatconverter.util.comparater;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.Linkage;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;

import java.util.Comparator;

/**
 * Created by e15d5605 on 2017/10/11.
 */
public class NodeDescendingComparator implements Comparator<Node> {

    @Override
    public int compare(Node o1, Node o2) {
        Linkage lin1 = null;
        Linkage lin2 = null;

        for (Edge parentEdge : o1.getParentEdges()) {
            if (parentEdge.isCyclic() || parentEdge.isRepeat()) continue;
            lin1 = parentEdge.getGlycosidicLinkages().get(0);
        }

        for (Edge parentEdge : o2.getParentEdges()) {
            if (parentEdge.isCyclic() || parentEdge.isRepeat()) continue;
            lin2 = parentEdge.getGlycosidicLinkages().get(0);
        }

        if (lin1 == null || lin2 == null) return 0;

        int pos1 = lin1.getParentLinkages().get(0);
        int pos2 = lin2.getParentLinkages().get(0);

        if (pos2 > pos1) {
            return 1;
        } else if (pos1 == pos2) {
            return 0;
        } else {
            return -1;
        }
    }
}
