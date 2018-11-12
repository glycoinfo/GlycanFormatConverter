package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.MonosaccharideUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.SubstituentIUPACNotationAnalyzer;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.ThreeLetterCodeAnalyzer;
import org.glycoinfo.WURCSFramework.util.exchange.SubstituentAnalyzer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2017/10/04.
 */
public class IUPACCondensedNotationParser {

    public Node parseMonosaccharide (String _notation) throws GlycanException, GlyCoImporterException{
        String temp = this.trimParentheses(_notation);

        Monosaccharide mono = new Monosaccharide();
        ArrayList<String> subNotation = new ArrayList<String>();
        ArrayList<String> modifications = new ArrayList<String>();
        LinkedList<String> trivialName = new LinkedList<>();
        LinkedList<String> configurations = new LinkedList<>();

        //parse substituent
        if (temp.matches("^\\d.+")) {
            SubstituentIUPACNotationAnalyzer subAna = new SubstituentIUPACNotationAnalyzer();
            subAna.start(temp);
            return subAna.getSubstituents().get(0);
        }

        //extract linkage
        String linkage = this.extractLinkage(temp);
        temp = temp.replace(linkage, "");

        //parse monosaccharide
        Matcher matMono = Pattern.compile("([A-Z][a-z]{2}|KDN)").matcher(temp);
        String threeLetterCode = "";

        if (matMono.find()) {
            String coreName = matMono.group(1);

            // extract trivial name and super class
            if(matMono.group(1) != null) {
                ThreeLetterCodeAnalyzer threeCode = new ThreeLetterCodeAnalyzer();
                threeCode.analyzeTrivialName(coreName, trivialName);

                if (threeCode.getCoreNotation() != null) {
                    threeLetterCode = threeCode.getCoreNotation();
                } else {
                    threeLetterCode = coreName;
                }

                mono.setStereos(threeCode.getStereos());
                mono.setSuperClass(threeCode.getSuperClass());

                subNotation.addAll(threeCode.getSubstituents());
                modifications.addAll(threeCode.getModificaitons());
                temp = temp.replace(coreName, "");
            }
        }

        //parse linkage positions
        String anomericState = "";
        int anomericPosition = -1;
        if (!linkage.equals("")) {
            for (int i = 0; i < linkage.length(); i++) {
                char item = linkage.charAt(i);
                if (i == 0 && (item == 'a' || item == 'b' || item == '?')) {
                    anomericState = String.valueOf(item);
                }
                if (i == 1 && String.valueOf(item).matches("[\\d]")) {
                    anomericPosition = Integer.parseInt(String.valueOf(item));
                }
            }
        }

        //parse native substituent
        if (!temp.equals("")) {
            subNotation.add(temp);
        }

        MonosaccharideIndex mi = MonosaccharideIndex.forTrivialNameWithIgnore(threeLetterCode);
        MonosaccharideUtility monoUtil = new MonosaccharideUtility();

        //modified anomeric position
        mono.setAnomericPosition(anomericPosition);
        mono.setAnomer(convertAnomericState(mono, anomericState));

        if (mi != null) {
            //modify anomeric position
            mono = assignAnomericPosition(mono, mi);

            //modify ring size
            mono = assignRingPosition(mono, mi);

            //modify cofiguration
            configurations.addLast(mi.getFirstConfiguration());
            mono = monoUtil.modifyStereos(mono, configurations);
        }

        //add native substituents
        mono = monoUtil.appendSubstituents(mono, subNotation);

        // make modifications
        mono = monoUtil.appendModifications(mono, modifications);

		// check and modify configuration
        mono = monoUtil.checkTruelyConfiguration(threeLetterCode, configurations, mono);

        return mono;
    }

    private Monosaccharide assignAnomericPosition (Node _mono, MonosaccharideIndex _mi) {
        if (_mi == null) return (Monosaccharide) _mono;

        ((Monosaccharide) _mono).setAnomericPosition(_mi.getAnomerciPosition());

        return (Monosaccharide) _mono;
    }

    private Monosaccharide assignRingPosition (Node _mono, MonosaccharideIndex _mi) throws GlycanException {
        if (_mi == null) return (Monosaccharide) _mono;

        Monosaccharide mono = (Monosaccharide) _mono;
        int anomericPosition = ((Monosaccharide) _mono).getAnomericPosition();

        if (anomericPosition != -1) {
            if (_mi.getRingSize().equals("p")) {
                if (anomericPosition == 1) {
                    mono.setRing(anomericPosition, 5);
                }
                if (anomericPosition == 2) {
                    mono.setRing(anomericPosition, 6);
                }
            }
            if (_mi.getRingSize().equals("f")) {
                if (anomericPosition == 1) {
                    mono.setRing(anomericPosition, 4);
                }
                if (anomericPosition == 2) {
                    mono.setRing(anomericPosition, 5);
                }
            }
        }

        return mono;
    }

    private int extractAnomericPosition (Monosaccharide _mono, String _linkage) {
        if(_linkage.equals("")) {
            if (_mono.getAnomericPosition() != 0) return _mono.getAnomericPosition();
            return Monosaccharide.OPEN_CHAIN;
        }
        int childPos = Monosaccharide.UNKNOWN_RING;
        AnomericStateDescriptor anomer = _mono.getAnomer();

        for (String unit : _linkage.split(":")) {
            if (unit.matches("\\(.+")) unit = this.trimHead(unit);
            if (unit.indexOf("a") != -1 || unit.indexOf("b") != -1) {
                childPos = this.charToInt(unit.charAt(1));
            }
        }

        if (anomer.equals(AnomericStateDescriptor.OPEN)) return Monosaccharide.OPEN_CHAIN;
        if (_mono.getAnomericPosition() != 0 && childPos == -1) childPos = _mono.getAnomericPosition();

        return childPos;
    }

    private AnomericStateDescriptor convertAnomericState (Monosaccharide _mono, String _anomeric) {
        if(_anomeric.equals("?")) {
            if (_mono.getAnomericPosition() == -1) return AnomericStateDescriptor.UNKNOWN;
            else return AnomericStateDescriptor.UNKNOWN_STATE;
        }
        if(_anomeric.equals("")) return AnomericStateDescriptor.OPEN;
        if(_anomeric.equals("a")) return AnomericStateDescriptor.ALPHA;
        if(_anomeric.equals("b")) return AnomericStateDescriptor.BETA;

        return null;
    }

    private int charToInt (char _char) {
        if (_char == '?') return -1;
        return (Integer.parseInt(String.valueOf(_char)));
    }

    private String trimParentheses(String _notation) {
        String ret = _notation;
        ret = ret.replaceAll("\\(","");
        ret = ret.replaceAll("\\)","");
        return ret;
    }

    private String extractLinkage (String _notation) {
        String ret = "";
        boolean isLinkage = false;

        for (int i = 0; i < _notation.length(); i++) {
            char item = _notation.charAt(i);

            if (isLinkage) ret += item;

            if (i == _notation.length() -1) break;

            if ((item == 'a' || item == 'b' || item == '?')) {
                char pos = _notation.charAt(i+1);
                if (String.valueOf(pos).matches("[\\d?-]")) {
                    ret += item;
                    isLinkage = true;
                }
            }
        }

        return ret;
    }

    private String trimHead (String _temp) {
        return _temp.substring(1, _temp.length());
    }
}
