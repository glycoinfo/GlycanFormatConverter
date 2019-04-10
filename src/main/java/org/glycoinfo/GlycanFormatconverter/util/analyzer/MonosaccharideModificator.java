package org.glycoinfo.GlycanFormatconverter.util.analyzer;

import org.glycoinfo.GlycanFormatconverter.Glycan.AnomericStateDescriptor;
import org.glycoinfo.GlycanFormatconverter.Glycan.CrossLinkedTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.ModificationTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.SuperClass;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * Created by e15d5605 on 2017/08/25.
 */
public class MonosaccharideModificator {

    public String start (String _monosaccharide) throws GlyCoImporterException, ConverterExchangeException {
        //if (isUpperCaseNotation(_monosaccharide)) return _monosaccharide;

        TreeMap<Integer, String> unitMap = resolveNotation(_monosaccharide);

        for (Integer key : unitMap.keySet()) {
            String unit = unitMap.get(key);

            if (unit.matches("^\\d.*$")) continue;
            if (checkAnomericSymbol(unit)) continue;
            if (checkConfiguration(unit)) continue;

            /* modify anhydro */
            if (isAnhydro(unit)) {
                unitMap.put(key, modifyAnhydro(unit));
                continue;
            }

            if (isUpperCaseNotation(_monosaccharide)) {
                if (checkModification(unit)) {
                    unitMap.put(key, unit.toLowerCase());
                    continue;
                }

                /* modify monosaccharide */
                String trivialName = "";
                if (MonosaccharideNotationAnalyzer.start(unit))
                    trivialName = modifyTrivialName(unit);

                /* modify substituent */
                trivialName = trivialName + parseSubstituent(trivialName, unit);

                unitMap.put(key, trivialName);
                continue;
            }
        }

        String ret = "";
        for (Iterator<String> i = unitMap.values().iterator(); i.hasNext();) {
            ret = ret + i.next();
            if (i.hasNext()) ret = ret + "-";
        }

        return ret;
    }

    private boolean checkAnomericSymbol (String _notation) {
        if (_notation.length() != 1) return false;
        _notation = _notation.toLowerCase();
        AnomericStateDescriptor anomDict = AnomericStateDescriptor.forAnomericState(_notation.charAt(0));

        if (anomDict != null) return true;

        return false;
    }

    private boolean checkConfiguration (String _notation) {
        //_notation = _notation.toUpperCase();
        if (_notation.length() != 1) return false;
        if (_notation.equals("D") || _notation.equals("L") || _notation.equals("?")) return true;
        return false;
    }

    private boolean checkModification (String _notation) {
        ModificationTemplate modT = ModificationTemplate.forIUPACNotation(_notation.toLowerCase());
        if (modT != null) return true;

        return false;
    }

    private boolean isAnhydro (String _notation) {
        CrossLinkedTemplate crossT = CrossLinkedTemplate.forIUPACNotationWithIgnore(_notation);
        if (crossT == null) return false;
        return (crossT.equals(CrossLinkedTemplate.ANHYDROXYL));
    }

    private String modifyTrivialName (String _notation) {
        int point = 3;
        String twoLetter = "";
        String threeLetter = "";
        MonosaccharideIndex modIndex = null;

        if (point == _notation.length()) modIndex = MonosaccharideIndex.forTrivialNameWithIgnore(_notation);

        while (point != _notation.length()) {
            twoLetter = _notation.substring(point-3, point-1);
            modIndex = MonosaccharideIndex.forTrivialNameWithIgnore(twoLetter);
            if (modIndex != null) {
                threeLetter = "";
                break;
            }

            threeLetter = _notation.substring(point-3, point);
            modIndex = MonosaccharideIndex.forTrivialNameWithIgnore(threeLetter);

            if (modIndex == null) point++;
            else {
                twoLetter = "";
                break;
            }
        }

        String trivialName = modIndex.getTrivialName();
        String superClass = modifySuperClass(_notation);
        String ringSize = "";

        if (superClass == null) {
            ringSize = modifyRingSize(_notation, trivialName.toUpperCase());
            return trivialName + ringSize.toLowerCase();
        } else {
            ringSize = modifyRingSize(_notation, superClass.toUpperCase());

            if (!twoLetter.equals("") || !threeLetter.equals("")) return trivialName.toLowerCase() + superClass + ringSize.toLowerCase();
            else return superClass + ringSize.toLowerCase();
        }
    }

    private String modifySuperClass (String _notation) {
        int point = 3;
        SuperClass superClass = null;

        while (point != _notation.length()) {
            String threeLetter = _notation.substring(point-3, point);
            superClass = SuperClass.forSuperClassWithIgnore(threeLetter);

            if (superClass == null) point++;
            else break;
        }

        if (superClass != null) return superClass.getSuperClass();
        else return null;
    }

    private String modifyRingSize (String _notation, String _nodeName) {
        int point = _notation.indexOf(_nodeName) + _nodeName.length();
        String ringSize = _notation.substring(point, point+1);
        if (!ringSize.equals("P") && !ringSize.equals("F") && !ringSize.equals("?")) return "";
        return ringSize;
    }

    private String parseSubstituent (String _modifiedName, String _notation) throws GlyCoImporterException {
        String ret = _notation.replaceFirst(_modifiedName.toUpperCase(), "");
        if (!_modifiedName.equalsIgnoreCase(ret)) return ret;
        else return "";
    }

    private String modifyAnhydro (String _notation) {
        CrossLinkedTemplate crossT = CrossLinkedTemplate.forIUPACNotationWithIgnore(_notation);
        if (crossT == null) return _notation;
        if (crossT.equals(CrossLinkedTemplate.ANHYDROXYL)) return crossT.getIUPACnotation();
        return _notation;
    }

    private boolean isUpperCaseNotation (String _notation) {
        boolean ret = true;
        for (int i = 0; i < _notation.length(); i++) {
            if (String.valueOf(_notation.charAt(i)).matches("[a-z]")) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    private boolean isLowerCaseNotation (String _notation) {
        boolean ret = true;
        for (int i = 0; i < _notation.length(); i++) {
            if (String.valueOf(_notation.charAt(i)).matches("[A-Z]")) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    private TreeMap<Integer, String> resolveNotation (String _notation) {
        int key = 0;
        TreeMap<Integer, String> ret = new TreeMap<Integer, String>();
        for(String unit : _notation.split("-")) {
            ret.put(key, unit);
            key++;
        }

        return ret;
    }

}
