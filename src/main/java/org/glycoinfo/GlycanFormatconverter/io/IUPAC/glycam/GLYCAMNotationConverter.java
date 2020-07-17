package org.glycoinfo.GlycanFormatconverter.io.IUPAC.glycam;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.Glycan.SuperClass;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class GLYCAMNotationConverter extends IUPACNotationConverter {

    public String start (Node _node) throws GlycanException, TrivialNameException {
        Node copy = _node.copy();
        makeTrivialName(copy);
        return makeGLYCAMNotation(copy);
    }

    private String makeGLYCAMNotation (Node _node) {
        Monosaccharide mono = (Monosaccharide) _node;
        String threeLetter = getThreeLetterCode();
        String configuration = "?";

        if (!mono.getStereos().isEmpty())
            configuration = makeConfiguration(mono.getStereos().getFirst()).toUpperCase();

        StringBuilder ret = new StringBuilder(threeLetter);

        // append configuration
        MonosaccharideIndex monoInd = MonosaccharideIndex.forTrivialNameWithIgnore(threeLetter);
        if (monoInd != null && mono.getStereos().size() == 1) ret.insert(0, configuration);
        SuperClass superclass = SuperClass.forSuperClassWithIgnore(threeLetter);
        if (superclass != null) ret.insert(0, configuration);

        // make ring size
        String ringSize = defineRingSize(_node);
        ret.append(ringSize);

        // append core substituent
        HashMap<String, String> mapSubs = getSubConv().getMapSubs();
        String coreSubstituent = getSubConv().getCoreSubstituentNotaiton();
        if (coreSubstituent.equals("N")) {
            mapSubs.put("N", "2");
        } else {
            ret.append(getSubConv().getCoreSubstituentNotaiton());
        }

        // make acidic tail
        String acidicStatus = makeAcidicStatus(_node);
        if (acidicStatus.equals("A")) {
            mapSubs.put(acidicStatus, "6");
        }

        // make substituent notation
        if (!mapSubs.isEmpty()) {
            ret.append("[");
            ret.append(this.makeSubstituentNotation(mapSubs));
            ret.append("]");
        }

        return ret.toString();
    }

    private String makeSubstituentNotation (HashMap<String, String> _mapSubs) {
        StringBuilder ret = new StringBuilder();
        TreeMap<Integer, String> mapSubs = this.modifySubstituentMap(_mapSubs);
        for (Iterator<Integer> iterPos = mapSubs.keySet().iterator(); iterPos.hasNext();) {
            Integer pos = iterPos.next();
            ret.append(pos);
            ret.append(mapSubs.get(pos));
            if (iterPos.hasNext()) ret.append(",");
        }
        return ret.toString();
    }

    private TreeMap<Integer, String> modifySubstituentMap (HashMap<String, String> _mapSubs) {
        TreeMap<Integer, String> temp = new TreeMap<>();
        for (String key : _mapSubs.keySet()) {
            String positions = _mapSubs.get(key);
            String notation = key;
            if (key.matches("C[A-Za-z]+.*")) {
                notation = key.replaceFirst("C", "");
            }
            if (key.matches("O[A-Za-z]+.*")) {
                notation = key.replaceFirst("O", "");
            }

            for (String position : positions.split(",")) {
                temp.put(Integer.parseInt(position), notation);
            }
        }

        return temp;
    }
}
