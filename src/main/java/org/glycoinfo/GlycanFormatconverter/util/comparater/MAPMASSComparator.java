package org.glycoinfo.GlycanFormatconverter.util.comparater;

import org.glycoinfo.WURCSFramework.util.property.AtomicProperties;
import org.glycoinfo.WURCSFramework.wurcs.graph.Modification;

import java.util.Comparator;

/**
 * Created by e15d5605 on 2019/03/05.
 */
public class MAPMASSComparator implements Comparator<Modification> {

    @Override
    public int compare(Modification o1, Modification o2) {
        char mapHead1 = o1.getMAPCode().charAt(1);
        char mapHead2 = o2.getMAPCode().charAt(1);

        AtomicProperties atomProp1 = AtomicProperties.forSymbol(String.valueOf(mapHead1));
        AtomicProperties atomProp2 = AtomicProperties.forSymbol(String.valueOf(mapHead2));

        if (atomProp1.getMassNumber() > atomProp2.getMassNumber()) {
            return 1;
        } else {
            return -1;
        }
    }
}
