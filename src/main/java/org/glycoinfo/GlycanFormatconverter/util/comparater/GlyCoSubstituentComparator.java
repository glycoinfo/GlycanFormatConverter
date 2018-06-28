package org.glycoinfo.GlycanFormatconverter.util.comparater;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;

import java.util.Comparator;

/**
 * Created by e15d5605 on 2017/10/12.
 */
public class GlyCoSubstituentComparator implements Comparator<Edge> {

    @Override
    public int compare(Edge o1, Edge o2) {
        Substituent sub1 = (Substituent) o1.getSubstituent();
        Substituent sub2 = (Substituent) o2.getSubstituent();

        int pos1 = sub1.getFirstPosition().getParentLinkages().get(0);
        int pos2 = sub2.getFirstPosition().getParentLinkages().get(0);

        if (pos1 > pos2) {
            return 1;
        } else if (pos1 == pos2) {
            return 0;
        } else {
            return -1;
        }
   }
}
