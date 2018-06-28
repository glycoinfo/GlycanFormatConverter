package org.glycoinfo.GlycanFormatconverter.util.comparater;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.Linkage;

import java.util.Comparator;

/**
 * Created by e15d5605 on 2017/10/11.
 */
public class EdgeComparator implements Comparator<Edge> {


    @Override
    public int compare(Edge o1, Edge o2) {
        Linkage lin1 = o1.getGlycosidicLinkages().get(0);
        Linkage lin2 = o2.getGlycosidicLinkages().get(0);

        int pos1 = lin1.getParentLinkages().get(0);
        int pos2 = lin2.getParentLinkages().get(0);

        if (pos2 > pos1) {
            return 1;
        } else return -1;
    }
}
