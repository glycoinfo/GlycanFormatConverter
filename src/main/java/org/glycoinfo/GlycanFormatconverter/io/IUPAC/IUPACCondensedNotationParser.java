package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.MonosaccharideUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.SubstituentIUPACNotationAnalyzer;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.ThreeLetterCodeAnalyzer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2017/10/04.
 */
public class IUPACCondensedNotationParser {

    public Node parseMonosaccharide (String _iupacNotation) throws GlycanException, GlyCoImporterException{
        String temp = this.trim(_iupacNotation);

        String linkagePos = "";
        String anomericState = "";
        Monosaccharide mono = new Monosaccharide();
        ArrayList<String> subNotation = new ArrayList<String>();
        ArrayList<String> modifications = new ArrayList<String>();
        LinkedList<String> configurations = new LinkedList<String>();
        LinkedList<String> trivialName = new LinkedList<String>();

        SubstituentIUPACNotationAnalyzer subAna = new SubstituentIUPACNotationAnalyzer();
        MonosaccharideUtility monoUtil = new MonosaccharideUtility();

		/* extract linkage positions */

        if(temp.indexOf("(") != -1) {
            linkagePos = trimLinkage(_iupacNotation);
            temp = temp.replace(linkagePos, "");
            anomericState = String.valueOf(linkagePos.charAt(1));
        }
        if (temp.indexOf(")-") != -1 ) {
            temp = temp.substring(temp.indexOf(")-") + 2);
        }

		/* parse fragment by substituent */
        //group 1 : anchor
        //group 2 : notation
        //group 3 : fragments ID
        Matcher matSub = Pattern.compile("^([\\?\\d])+([\\(\\w\\)]+)+=(\\d\\$)").matcher(temp);
        if (matSub.find()) {
            subAna.start(matSub.group(1) + matSub.group(2));
            Substituent sub = subAna.getSubstituents().get(0);
            Edge parentEdge = new Edge();
            parentEdge.addGlycosidicLinkage(sub.getFirstPosition());
            parentEdge.setSubstituent(sub);
            sub.addParentEdge(parentEdge);
            return sub;
        }

		/* extract anhydro and deoxy */
        //group 1 : ambiguous anchor
        //group 2 : anhydro (3,9-dideoxy-L-gro-α-L-manNon2ulop5N7NFo-onic-)
        //group 3 : deoxy block (3,9-dideoxy-L-gro-α-L-manNon2ulop5N7NFo-onic-)
        //group 6 : anomeric state
        //group 7 : anhydro
        //group 8 : deoxy (β-3,4-Anhydro-3,4,7-trideoxy-L-lyxHepp2,6N2-)
        //group 9 : configuration
        Matcher matMod = Pattern.compile("(\\d+\\$+[\\$\\d\\|]*)?([\\d,:]*-Anhydro-)?([\\d,?]*-\\w*deoxy-)?([LD\\?]-gro-)?((aldehyde|\\?|\u03B1|\u03B2)-)?([\\d,:]*-Anhydro-)?([\\d,?]*-\\w*deoxy-)?([DL\\?])?").matcher(temp);

        if(matMod.find()) {
			/* remove anchor */
            if(matMod.group(1) != null) {
                temp = temp.replace(matMod.group(1), "");
            }

			/* extract anomeric state */
            if(matMod.group(6) != null && matMod.group(9) != null) {
                if (!matMod.group(6).equals("aldehyde")) anomericState = matMod.group(6);
                else modifications.add(matMod.group(6));

                String regex = matMod.group(5).replace("?", "\\?");
                temp = temp.replaceFirst(regex, "");
            }

			/* extract anhydro */
            if(matMod.group(2) != null || matMod.group(7) != null) {
                String anhydro = (matMod.group(2) != null) ? matMod.group(2) : matMod.group(7);
                subNotation.addAll(monoUtil.resolveNotation(trimTail(anhydro)));
                temp = temp.replace(anhydro, "");
            }
			/* extract modifications */
            if(matMod.group(3) != null || matMod.group(8) != null) {
                String deoxy = "";
                if (matMod.group(3) != null) deoxy = matMod.group(3);
                if (matMod.group(8) != null) deoxy = matMod.group(8);
                modifications.addAll(monoUtil.resolveNotation(trimTail(deoxy)));
                temp = temp.replace(deoxy, "");
            }
        }

		/* extract configuration and trivial name */
        for(String unit : temp.split("-")) {
            if(unit.equals("D") || unit.equals("L") || unit.equals("?")) {
                configurations.addLast(unit);
                if (unit.equals("?")) unit = unit.replace(unit, "\\" + unit);
                temp = temp.replaceFirst(unit + "-", "");
            }
            if(unit.equals("ol") || unit.equals("onic") || unit.equals("aric") || unit.equals("uronic")) {
                modifications.add(unit);
                temp = temp.replace("-" + unit, "");
            }
        }

        if (temp.matches("[a-z]{3}-.+")) {//temp.indexOf("gro-") != -1) {
            trivialName.add(temp.substring(0, temp.indexOf("-")));
            trivialName.add(temp.substring(temp.indexOf("-") + 1, temp.length()));
        } else {
            trivialName.add(temp);
        }

        if(modifications.contains("ol")) {
            anomericState = "";
        }

        String coreNotation = trivialName.getLast();
        String threeLetterCode = "";

        //group 1 : trivial name ([A-Z]{1}[a-z]{2}\\d?[A-Z]{1}[a-z]{1,2} : Neu5Ac, [a-z]{2,3}[A-Z]{1}[a-z]{2} : lyxHex, araHex,  [A-Z]{1}[a-z]{2} : Glc)
        //group 2 : ulosonic
        Matcher matCore = Pattern.compile("(Sugar|Ko|[a-z]{2,3}[A-Z]{1}[a-z]{2}|6?d?[A-Z]{1}[a-z]{2})+((\\dulo)+)?").matcher(coreNotation);
        if(matCore.find()) {
			/* extract trivial name and super class */
            if(matCore.group(1) != null) {
                ThreeLetterCodeAnalyzer threeCode = new ThreeLetterCodeAnalyzer();
                threeCode.analyzeTrivialName(matCore.group(1), trivialName);

                if (threeCode.getCoreNotation() != null) {
                    threeLetterCode = threeCode.getCoreNotation();
                } else {
                    threeLetterCode = matCore.group(1);
                }

                mono.setStereos(threeCode.getStereos());
                mono.setSuperClass(threeCode.getSuperClass());

                subNotation.addAll(threeCode.getSubstituents());
                modifications.addAll(threeCode.getModificaitons());
                coreNotation = coreNotation.replace(matCore.group(1), "");
            }
			/* extract ulosonic */
            if(matCore.group(2) != null) {
                String ulosonic = matCore.group(2);
                while (ulosonic.length() != 0) {
                    String unit = ulosonic.substring(0, 4);
                    modifications.add(unit);
                    ulosonic = ulosonic.replaceFirst(unit, "");
                }
                coreNotation = coreNotation.replace(matCore.group(2), "");
            }
        }

		/* extract ring size and substituents */
        //group 1 : ring size
        //group 2 : substituents
        Matcher matTail = Pattern.compile("([p|f|?])?([\\d\\w\\?,/:\\(\\)%-]+)?").matcher(coreNotation);
        if (matTail.find() && (matTail.group(1) != null || matTail.group(2) != null)) {
            boolean isRingSize = false;

            if (matTail.group(2) == null) isRingSize = true;
            if (matTail.group(2) != null) {
                if (coreNotation.length() > 1 && String.valueOf(coreNotation.charAt(1)).matches("[\\dNA\\\\?]"))
                    isRingSize = true;
            }
            //if (matTail.group(1) == null) isRingSize = false;

			/* extract ring size */
            //String ringSize = matTail.group(1) == null ? MonosaccharideIndex.forTrivialNameWithIgnore(threeLetterCode) : "";
            if (matTail.group(1) == null) {
                MonosaccharideIndex monoInd = MonosaccharideIndex.forTrivialNameWithIgnore(threeLetterCode);
           //     ringSize = monoInd == null ? monoInd : monoInd.
            }
            mono = monoUtil.makeRingSize(mono, matTail.group(1), threeLetterCode, modifications);

			/* extract substituents */
            if(matTail.group(2) != null) {
                String subNotations = "";
                if (!isRingSize && matTail.group(1) != null) {
                    subNotations += matTail.group(1);
                }
                subNotations += matTail.group(2);

                if (subNotations.startsWith("A")) {
                    modifications.add("6" + subNotations.charAt(0));
                    subNotations = subNotations.replaceFirst(String.valueOf(subNotations.charAt(0)), "");
                }

                if (subNotations != null) {
                    subNotation.addAll(subAna.resolveSubstituents(subNotations, true));
                    modifications.addAll(subAna.resolveSubstituents(subNotations, false));
                }
            }
        }

		/* make modifications */
        mono = monoUtil.appendModifications(mono, modifications);

		/* make anomeric state */
        mono.setAnomer(convertAnomericState(mono, anomericState));

		/* define anomeric position */
        mono.setAnomericPosition(extractAnomericPosition(mono, linkagePos));

		/* append substituents */
        mono = monoUtil.appendSubstituents(mono, subNotation);

		/* append configuration */
        mono = monoUtil.modifyStereos(mono, configurations);

		/* check and modify configuration */
        mono = monoUtil.checkTruelyConfiguration(threeLetterCode, configurations, mono);

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

    private String trim (String _notation) {
        String ret = _notation;
        ret = (ret.indexOf(")-") != -1) ? ret.substring(ret.indexOf(")-") + 1, ret.length()) : ret;
        ret = (ret.startsWith("[")) ? ret.replaceAll("\\[", "") : ret;
        ret = (ret.startsWith("]")) ? ret.replaceAll("]", "") : ret;
        ret = (ret.startsWith("-")) ? ret.replaceFirst("-", "") : ret;
        ret = (ret.endsWith("]")) ? ret.replaceFirst("]", "") : ret;

        return ret;
    }

    private String trimTail (String _temp) {
        return _temp.substring(0, _temp.length() - 1);
    }

    private String trimHead (String _temp) {
        return _temp.substring(1, _temp.length());
    }

    private String trimLinkage (String _notation) {
        String ret = "";

        boolean isLinkage = false;
        for (int i = 0; i < _notation.length(); i++) {
            if (_notation.charAt(i) == '(' &&
                    (_notation.charAt(i+1) == '?' || _notation.charAt(i+1) == 'a' || _notation.charAt(i+1) == 'b')) isLinkage = true;
            if (!isLinkage) continue;
            else {
                ret += _notation.charAt(i);
                if (_notation.charAt(i) == ')') break;
            }
        }

        return ret;
    }

}
