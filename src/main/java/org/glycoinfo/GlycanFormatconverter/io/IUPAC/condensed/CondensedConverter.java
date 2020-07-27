package org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;

/**
 * Created by e15d5605 on 2017/11/15.
 *
 * notation for iupac condensed form
 * without anomeric state, configuration and ring size
 * 3-deoxy-gro-galNon2ulo5NAc-onic
 * ManNAc
 * */
public class CondensedConverter extends IUPACNotationConverter {

    public String start (Node _node) throws GlycanException, TrivialNameException {
        Node copy = _node.copy();

        makeTrivialName(copy);

        return makeCondensedNotation(copy);
    }

    private String makeCondensedNotation (Node _copy) throws TrivialNameException {
        Monosaccharide mono = (Monosaccharide) _copy;
        String threeLetter = getThreeLetterCode();
        String configuration = "?";

        if (!mono.getStereos().isEmpty())
            configuration = makeConfiguration(mono.getStereos().getFirst()).toUpperCase();

        StringBuilder ret = new StringBuilder(threeLetter);

        // append configuration
        MonosaccharideIndex monoInd = MonosaccharideIndex.forTrivialNameWithIgnore(threeLetter);
        if (monoInd != null) {
            if (!monoInd.getFirstConfiguration().endsWith(configuration)) {
                ret.insert(0, configuration + "-");
            }
        }
        if (monoInd == null && mono.getStereos().size() != 2) {
            ret.insert(0, configuration + "-");
        }

        // append deoxy
        String deoxyNotation = makeDeoxyPosition(mono);
        if(!ret.toString().contains(deoxyNotation)) ret.insert(0, deoxyNotation);

        // append anhydro
        ret.insert(0, getSubConv().getPrefixSubstituent());

        // append ulonic notation
        ret.append(extractUlonic(mono));

        // make ring size
        String ringSize = defineRingSize(_copy);
        if (monoInd != null) {
            if (!monoInd.getRingSize().equals(ringSize)) ret.append(ringSize);
        } else {
            ret.append(ringSize);
        }

        // append core substituent
        ret.append(getSubConv().getCoreSubstituentNotaiton());

        // make acidic tail
        String acidicStatus = makeAcidicStatus(_copy);
        if (acidicStatus.equals("A")) {
            ret.append(acidicStatus);
        }

        // make substituent notation
        ret.append(getSubConv().getSubstituentNotation());

        // append onic
        if (!acidicStatus.equals("A") && !containUlonicAcid(getThreeLetterCode())) ret.append(acidicStatus);

        // make modification with head
        if (isAlditol(_copy)) ret.append("-ol");
        if (isAldose(_copy)) ret.insert(0, "aldehyde-");
        if (isKetose(_copy)) ret.insert(0, "keto-");

        return ret.toString();
    }
}
