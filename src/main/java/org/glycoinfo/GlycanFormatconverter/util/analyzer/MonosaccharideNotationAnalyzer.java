package org.glycoinfo.GlycanFormatconverter.util.analyzer;

import org.glycoinfo.GlycanFormatconverter.Glycan.CrossLinkedTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.SubstituentTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.SuperClass;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;

/**
 * Created by e15d5605 on 2017/08/24.
 */
public class MonosaccharideNotationAnalyzer {

    //TODO : SubstituentとModificationを判別する処理の実装

    public static boolean start (String _notation) throws ConverterExchangeException {
        MonosaccharideIndex modIndex = null;
        SuperClass superclass = null;

        for (String unit : _notation.split("-")) {
            if (unit.length() < 3) continue;
            int point = 3;

            while (point <= unit.length()) {
                String twoLetter = unit.substring(point-3, point-1);
                modIndex = MonosaccharideIndex.forTrivialNameWithIgnore(twoLetter);
                if (modIndex != null) break;

                String threeLetter = unit.substring(point-3, point);
                modIndex = MonosaccharideIndex.forTrivialNameWithIgnore(threeLetter);

                superclass = SuperClass.forSuperClassWithIgnore(threeLetter);

                if (modIndex == null && superclass == null) point++;
                else break;
            }

            if (modIndex != null || superclass != null) break;
        }

        if (modIndex != null || superclass != null) return true;

        return false;
    }
}
