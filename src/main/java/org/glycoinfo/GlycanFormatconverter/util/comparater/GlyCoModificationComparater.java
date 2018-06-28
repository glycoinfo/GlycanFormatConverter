package org.glycoinfo.GlycanFormatconverter.util.comparater;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyCoModification;

import java.util.Comparator;

/**
 * Created by e15d5605 on 2017/06/22.
 */
public class GlyCoModificationComparater implements Comparator<GlyCoModification> {
    @Override
    public int compare(GlyCoModification o1, GlyCoModification o2) {
        int position1 = o1.getPositionOne();
        int position2 = o2.getPositionOne();

        if (position1 > position2) {
            return 1;
        } else if (position1 == position2) {
            return 0;
        } else {
            return -1;
        }
    }
}
