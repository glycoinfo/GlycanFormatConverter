package org.glycoinfo.GlycanFormatconverter.io.IUPAC.extended;

import org.glycoinfo.GlycanFormatconverter.Glycan.AnomericStateDescriptor;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;

/**
 * Created by e15d5605 on 2017/11/15.
 */
public class ExtendedConverter extends IUPACNotationConverter {

    public String start (Node _node) throws GlycanException, TrivialNameException {
        Node copy = _node.copy();

        makeTrivialName(copy);

        return makeExtendedNotation(copy);
    }

    public String makeExtendedNotation (Node _copy) throws TrivialNameException {
        Monosaccharide mono = (Monosaccharide) _copy;
        String threeLetter = getThreeLetterCode();
        String configuration = "?";

        if (!mono.getStereos().isEmpty())
            configuration = makeConfiguration(mono.getStereos().getFirst()).toUpperCase();

        StringBuilder ret = new StringBuilder(threeLetter);

		// append configuration
        MonosaccharideIndex monoInd = MonosaccharideIndex.forTrivialNameWithIgnore(threeLetter);
        if (monoInd != null) ret.insert(0, configuration + "-");
        if (monoInd == null && mono.getStereos().size() != 2) ret.insert(0, configuration + "-");

		// append deoxy
        String deoxyNotation = makeDeoxyPosition(mono);
        if(!ret.toString().contains(deoxyNotation)) ret.insert(0, deoxyNotation);

		// append anhydro
        ret.insert(0, getSubConv().getPrefixSubstituent());

		// append ulonic notation
        ret.append(extractUlonic(mono));

		// make ring size
        ret.append(defineRingSize(_copy));

		// append core substituent
        ret.append(getSubConv().getCoreSubstituentNotaiton());

		// make acidic tail
        String acidicStatus = makeAcidicStatus(_copy);
        if(acidicStatus.equals("A")) {
            ret.append(acidicStatus);
        }

		// make substituent notation
        ret.append(getSubConv().getSubstituentNotation());

		// append onic
        if(!acidicStatus.equals("A") && !containUlonicAcid(getThreeLetterCode())) ret.append(acidicStatus);

		// make anomeric state (IUPAC)
        ret = new StringBuilder(defineAnomericState(_copy, ret));

		// make modification with head
        if(isAlditol(_copy)) ret.append("-ol");
        if(isAldehyde(_copy)) ret.insert(0, "aldehyde-");

        return ret.toString();
    }

    private String defineAnomericState(Node _node, StringBuilder sb) {
        Monosaccharide mono = (Monosaccharide) _node;
        StringBuilder ret = new StringBuilder(sb);

        AnomericStateDescriptor enumAnom = mono.getAnomer();

        String anomericState = "";
        if(enumAnom != null && !enumAnom.getIUPACAnomericState().equals("")) {
            anomericState = enumAnom.getIUPACAnomericState() + "-";
        }

        if (mono.getStereos().size() == 2 && haveStereosInTrivial(_node)) {
            String firstComp = mono.getStereos().getFirst();
            firstComp = trimThreeLetterPrefix(firstComp, extractDLconfiguration(firstComp)).toLowerCase();
            ret.insert(ret.indexOf(firstComp) + 4, anomericState);
        } else {
            ret.insert(0, anomericState);
        }

        return ret.toString();
    }

    private boolean haveStereosInTrivial (Node _node) {
        String temp = getThreeLetterCode().toLowerCase();
        boolean ret = true;
        for (String stereo : ((Monosaccharide) _node).getStereos()) {
            stereo = trimThreeLetterPrefix(stereo, extractDLconfiguration(stereo)).toLowerCase();
            if (temp.contains(stereo)) continue;
            else {
                ret = false;
                break;
            }
        }

        return ret;
    }

    private String trimThreeLetterPrefix(String _letter, String _configuration) {
        if(_configuration.equals("?")) {
            if(_letter.startsWith("d/l-")) _letter = _letter.replaceFirst("d/l-", "");
            if(_letter.startsWith("l/d-")) _letter = _letter.replaceFirst("l/d-", "");
        }else {
            _letter = _letter.replaceFirst(_configuration, "");
        }

        StringBuilder ret = new StringBuilder(_letter);
        ret = ret.replace(0, 1, ret.substring(0, 1).toUpperCase());
        return ret.toString();
    }
}
