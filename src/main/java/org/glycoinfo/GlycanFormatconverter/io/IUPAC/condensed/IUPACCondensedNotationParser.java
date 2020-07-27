package org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.MonosaccharideUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.BaseStereoIndex;
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

    public Node parseMonosaccharide (String _notation) throws GlycanException, GlyCoImporterException{
        if (_notation.matches("\\[[\\d?]\\).+") || _notation.matches(".*\\([ab?]\\d-][\\dn-]+.*"))
            throw new GlyCoImporterException("Repeating structure is not support !");
        if (_notation.matches("[\\d?]\\).+"))
            throw new GlyCoImporterException("Cyclic structure is not support !");
        if (_notation.contains("$"))
            throw new GlyCoImporterException("Glycan fragments is not support !");

        String temp = this.trimSymbols(_notation);
        String ringSize = "";
        Monosaccharide mono = new Monosaccharide();
        ArrayList<String> subNotations = new ArrayList<>();
        ArrayList<String> modifications = new ArrayList<>();
        LinkedList<String> configurations = new LinkedList<>();

        //parse independent substituent
        if (this.isSubstituent(temp)) {
            SubstituentIUPACNotationAnalyzer subAna = new SubstituentIUPACNotationAnalyzer();
            subAna.start(temp);
            return subAna.getSubstituents().get(0);
        }

        //
        String linkage = this.extractLinkage(temp);
        temp = temp.replace(linkage, "");

        // parse aldehyde or ketone
        Matcher matModiAlde = Pattern.compile("(aldehyde|keto)-").matcher(temp);
        if (matModiAlde.find()) {
            String modification = matModiAlde.group(1);
            modifications.add(modification);
            temp = temp.replace(matModiAlde.group(0), "");
        }

        //parse unhydro
        Matcher matSubUnhydro = Pattern.compile("([\\d,?]+)-(Anhydro)-").matcher(temp);
        if (matSubUnhydro.find()) {
            String position = matSubUnhydro.group(1);
            String notation = matSubUnhydro.group(2);
            subNotations.add(position + notation);
            temp = temp.replace(matSubUnhydro.group(0), "");
        }

        //parse deoxy
        Matcher matDeoxy = Pattern.compile("([\\d,?]+)-(.*)?(deoxy)-").matcher(temp);
        if (matDeoxy.find()) {
            String position = matDeoxy.group(1);
            String notation = matDeoxy.group(3);
            for (String pos : position.split(",")) {
                modifications.add(pos + notation.substring(0, 1));
            }
            temp = temp.replace(matDeoxy.group(0), "");
        }

        // parse modification at tail
        Matcher matModiSuf = Pattern.compile("(-(onic|aric|uronic))").matcher(temp);
        if (matModiSuf.find()) {
            if (matModiSuf.group(2) != null) {
                String acidic = matModiSuf.group(2);
                if (acidic.equals("aric")) {
                    modifications.add("1A");
                    modifications.add("6A");
                }
                if (acidic.equals("onic")) {
                    modifications.add("1A");
                }
                if (acidic.equals("ulonic")) {
                    modifications.add("6A");
                }
                temp = temp.replace(matModiSuf.group(1), "");
            }
        }

        //parse alditol
        Matcher matAldi = Pattern.compile("-ol").matcher(temp);
        if (matAldi.find()) {
            modifications.add("1h");
            temp = temp.replace(matAldi.group(0), "");
        }

        // parse ulo
        Matcher matUlo = Pattern.compile("(([\\d,?]+)+(.*)?(ulo))").matcher(temp);
        if (matUlo.find()) {
            //TODO : uloの表記を修飾と統一するべきかを検討する必要がある
            /*
            String position = matUlo.group(2);
            String notation = matUlo.group(4);
            for (String pos : position.split(",")) {
                modifications.add(pos + notation);
            }
             */

            SubstituentIUPACNotationAnalyzer subAnalyze = new SubstituentIUPACNotationAnalyzer();
            modifications.addAll(subAnalyze.resolveSubstituents(matUlo.group(0), true));
            temp = temp.replace(matUlo.group(1), "");
        }

        /*
            parse complex monosaccharide such as DDmanHep
            group1 : isomer
            group2 : monosaccharide notation
            group3 : ring size
            group4 : superclass
         */
        Matcher matMono1 = Pattern.compile("([DL?]+)([a-z]{3})+([pf?])?([A-Z][a-z]{2})+").matcher(temp);
        if (matMono1.find()) {
            String isomer = "";
            int partSize = -1;
            int coreSize = -1;
            if (matMono1.group(1) != null) {
                isomer = String.valueOf(matMono1.group(1).charAt(0));
                temp = temp.replace(isomer, "");
                configurations.add(isomer);
            }
            if (matMono1.group(2) != null) {
                String notation = matMono1.group(2);
                BaseStereoIndex baseInd = BaseStereoIndex.forCode(notation.toLowerCase());
                partSize = baseInd.getSize();
                temp = temp.replace(notation, String.valueOf(notation.charAt(0)).toUpperCase() + notation.charAt(1) + notation.charAt(2));
            }
            if (matMono1.group(3) != null) ringSize = matMono1.group(3);
            if (matMono1.group(4) != null) {
                SuperClass superclass = SuperClass.forSuperClassWithIgnore(matMono1.group(4));
                coreSize = superclass.getSize();
                mono.setSuperClass(superclass);
                temp = temp.replace(matMono1.group(4), "");
            }

            //make other side monosaccharide
            if (coreSize - partSize == 1) {
                String notation = BaseStereoIndex.GRO.getNotation();
                mono.addStereo(isomer.equals("?") ? "" : isomer.toLowerCase() + notation);
                configurations.add(isomer.toLowerCase());
            } else {
                throw new GlyCoImporterException("Multiple name of monosaccharide can be assign to this monosaccharide : " + _notation);
            }
        }

        /*
          parse sub side monosaccharide
          group1 : sub side notation
          group2 : isomer
          group3 : sub side monosaccharide notation
         */
        Matcher matSubMono = Pattern.compile("(([DL?])-([a-z]{3})-)").matcher(temp);

        /*
          parse core side monosaccharide
          group1 : isomer
          group2 : native deoxy modification such as 6dTal
          group3 : monosaccharide notation
          group4 : ring size
          group5 : core-substituent (5Gc, 5Ac, NAc, NGc, NA, A, N)
          group6 : peri-substituent
         */
        Matcher matMono = Pattern.compile("([LD?]-?)?([468]?[dei])?([A-Z][a-z]{1,2}C?|KDN|[a-zA-Z]{6})([pf?])?(5[GA]c|N[AG]c|NA|A|N)?([A-Za-z]+)?").matcher(temp);
        String threeLetterCode = "";
        if (matMono.find()) {
            String isomer = "";
            String prefix = "";
            String coreName;

            //extract sub side monosaccharide
            if (matSubMono.find()) {
                if (!matSubMono.group(3).equals(matMono.group(3))) {
                    String subIsomer = matSubMono.group(2);
                    String subNotation = matSubMono.group(3);
                    mono.addStereo(subIsomer.equals("?") ? "" : subIsomer.toLowerCase() + subNotation);
                    temp = temp.replaceFirst(matSubMono.group(1), "");
                    configurations.add(subIsomer);
                }
            }

            //extract isomer
            if (matMono.group(1) != null) {
                isomer = matMono.group(1);
                temp = temp.replace(isomer, "");
                configurations.add(isomer.replaceFirst("-", ""));
            }

            //extract deoxy for trivial name
            if (matMono.group(2) != null) {
                prefix = matMono.group(2);
                temp = temp.replaceFirst(prefix, "");
            }

            // extract trivial name and super class
            if(matMono.group(3) != null) {
                coreName = matMono.group(3);
                ThreeLetterCodeAnalyzer threeCode = new ThreeLetterCodeAnalyzer();
                threeCode.analyzeTrivialName(prefix + coreName, new LinkedList<>());

                if (threeCode.getCoreNotation() != null) {
                    threeLetterCode = threeCode.getCoreNotation();
                } else {
                    threeLetterCode = coreName;
                }

                for (String stereo : threeCode.getStereos()) {
                    isomer = isomer.replace("-", "");
                    mono.addStereo(isomer.equals("?") ? stereo : isomer.toLowerCase() + stereo);
                }

                if (mono.getSuperClass() == null) {
                    mono.setSuperClass(threeCode.getSuperClass());
                }

                subNotations.addAll(threeCode.getSubstituents());
                modifications.addAll(threeCode.getModificaitons());
                temp = temp.replace(coreName, "");
            }

            //extract ring size
            if (matMono.group(4) != null) {
                //check for core-substituent
                if (matMono.group(4).equals("?") && matMono.group(6) == null) {
                    ringSize = matMono.group(4);
                    temp = temp.replaceFirst("\\?", "");
                }
                if (!matMono.group(4).equals("?")) {
                    ringSize = matMono.group(4);
                    temp = temp.replaceFirst(ringSize, "");
                }
            }

            //extract native substituent
            if (matMono.group(5) != null) {
                String sub = matMono.group(5);
                if (sub.matches("NA\\d?")) {
                    subNotations.add("N");
                    modifications.add("6A");
                }
                if (sub.equals("A")) {
                    modifications.add("6A");
                }
                //TODO : シアル酸のときに結合位置を持たない修飾をコアとして取り出してしまっている
                if (sub.matches("N[GA]c") || sub.equals("N") || sub.matches("5[GA]c")) {
                    subNotations.add(sub);
                }
                temp = replaceTemplate(temp, sub);
            }
        }

        //parse linkage positions
        String anomericState = "";
        int anomericPosition = -1;
        if (!linkage.equals("")) {
            for (int i = 0; i < linkage.length(); i++) {
                char item = linkage.charAt(i);
                if (i == 1 && (item == 'a' || item == 'b' || item == '?')) {
                    anomericState = String.valueOf(item);
                }
                if (i == 2 && String.valueOf(item).matches("[\\d]")) {
                    anomericPosition = Integer.parseInt(String.valueOf(item));
                }
                if (i == 1 && item == '-') {
                    anomericState = AnomericStateDescriptor.UNKNOWN.getIUPACAnomericState();
                    anomericPosition = 1;
                }
            }
        }

        // parse peri substituent
        if (this.isSubstituent(temp)) {
            if (ringSize.equals("?") && temp.startsWith(",")) {
                temp = ringSize + temp;
                ringSize = "";
            }
            SubstituentIUPACNotationAnalyzer subAnalyze = new SubstituentIUPACNotationAnalyzer();
            subNotations.addAll(subAnalyze.resolveSubstituents(temp, true));
            modifications.addAll(subAnalyze.resolveSubstituents(temp, false));
            temp = temp.replace(temp, "");
        }

        if (!temp.equals(""))
            throw new GlyCoImporterException(_notation + " could not completely parsed : " + temp);

        MonosaccharideIndex mi = MonosaccharideIndex.forTrivialNameWithIgnore(threeLetterCode);
        MonosaccharideUtility monoUtil = new MonosaccharideUtility();

        //modified anomeric position
        mono.setAnomericPosition(anomericPosition);
        mono.setAnomer(convertAnomericState(mono, anomericState));

        if (mi != null) {
            //modify anomeric position
            mono = assignAnomericPosition(mono, mi);

            //modify ring size
            mono = (Monosaccharide) modifyRingSize(mono, mi, ringSize);

            //modify cofiguration
            configurations.addLast(mi.getFirstConfiguration());
            mono = monoUtil.modifyStereos(mono, configurations);
        }

        //add native substituents
        mono = monoUtil.appendSubstituents(mono, subNotations);

        // make modifications
        mono = monoUtil.appendModifications(mono, modifications);

		// check and modify configuration
        //mono = monoUtil.checkTruelyConfiguration(threeLetterCode, configurations, mono);

        return mono;
    }

    private Monosaccharide assignAnomericPosition (Node _mono, MonosaccharideIndex _mi) {
        if (_mi == null) return (Monosaccharide) _mono;

        /*
         * if monosaccharide is open chane, anomeric position is 0.
         */
        if (((Monosaccharide) _mono).getAnomer().equals(AnomericStateDescriptor.OPEN)) {
            ((Monosaccharide) _mono).setAnomericPosition(Monosaccharide.OPEN_CHAIN);
        } else {
            ((Monosaccharide) _mono).setAnomericPosition(_mi.getAnomerciPosition());
        }

        return (Monosaccharide) _mono;
    }

    private Node modifyRingSize (Node _mono, MonosaccharideIndex _mi, String _ringSize) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _mono;
        int anomericPosition = ((Monosaccharide) _mono).getAnomericPosition();

        // check ring position
        if (mono.getAnomericPosition() == 1) {
            if (_ringSize.equals("p")) {
                if (mono.getSuperClass().getSize() < SuperClass.PEN.getSize()) {
                    throw new GlycanException("Ring position exceeds the number of carbon backbone : 5 -> " + mono.getSuperClass().getSize());
                }
            }
            if (_ringSize.equals("f")) {
                if (mono.getSuperClass().getSize() < SuperClass.TET.getSize()) {
                    throw new GlycanException("Ring position exceeds the number of carbon backbone : 4 -> " + mono.getSuperClass().getSize());
                }
            }
        }
        if (mono.getAnomericPosition() == 2) {
            if (_ringSize.equals("p")) {
                if (mono.getSuperClass().getSize() < SuperClass.HEX.getSize()) {
                    throw new GlycanException("Ring position exceeds the number of carbon backbone : 6 -> " + mono.getSuperClass().getSize());
                }
            }
            if (_ringSize.equals("f")) {
                if (mono.getSuperClass().getSize() < SuperClass.PEN.getSize()) {
                    throw new GlycanException("Ring position exceeds the number of carbon backbone : 5 -> " + mono.getSuperClass().getSize());
                }
            }
        }

        // for open chain
        if (anomericPosition == 0 && _ringSize.equals("")) {
            return assignRingSize("o", mono);
        }

        if (_ringSize.equals("")) {
            return assignRingSize(_mi.getRingSize(), mono);
        } else {
            return assignRingSize(_ringSize, mono);
        }
    }

    private Node assignRingSize (String _ringSize, Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;
        int anomericPosition = ((Monosaccharide) _node).getAnomericPosition();

        switch (_ringSize) {
            case "p":
                if (anomericPosition == 1) {
                    mono.setRing(anomericPosition, 5);
                }
                if (anomericPosition == 2) {
                    mono.setRing(anomericPosition, 6);
                }
                break;
            case "f":
                if (anomericPosition == 1) {
                    mono.setRing(anomericPosition, 4);
                }
                if (anomericPosition == 2) {
                    mono.setRing(anomericPosition, 5);
                }
                break;
            case "o":
                mono.setRing(anomericPosition, Monosaccharide.OPEN_CHAIN);
                break;
            default:
                mono.setRing(anomericPosition, Monosaccharide.UNKNOWN_RING);
                break;
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
            if (unit.contains("a") || unit.contains("b")) {
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

    private String trimSymbols (String _notation) {
        // remove end rep symbol
        if (_notation.matches("\\[[\\d?]\\).+")) {
            _notation = _notation.replaceAll("\\[[\\d?]\\)", "");
        }

        // remove end cyclic symbol
        if (_notation.matches("[\\d?]\\)].+")) {
            _notation = _notation.replaceFirst("[\\d?]\\)", "");
        }

        // remove branches brackets
        _notation = _notation.replaceFirst("\\[", "");

        //TODO : start repの除去
        // remove start rep symbol
        //check start repeating position
        if (_notation.matches(".+][n\\d-]+$")) {
            _notation = _notation.replaceAll("][n\\d-]+", "");
        }

        //check for partially repeating point
        if (_notation.matches(".+\\([ab?][\\d?]-][n\\d-]+:.+")) {
            _notation = _notation.replaceAll("[ab?][\\d?]-][n\\d-]+:", "");
        }

        // remove start cyclic symbol
        if (_notation.matches(".+\\([ab?]?[\\d?]-")) {
            _notation.replaceFirst("\\([ab?][\\d?]-", "");
        }

        //TODO : 分岐開始のブラケットの除去
        _notation = _notation.replaceFirst("]", "");

        if (_notation.startsWith("[") && _notation.matches("\\[[A-Za-z\\[]+.+")) {
            _notation = _notation.replaceFirst("\\[", "");
        }

        //check start repeating position
        if (_notation.matches("^\\[(-[(A-Za-z)]+-)?[\\d?/]+\\).+$")) {
            _notation = _notation.replaceFirst("^\\[(-[(A-Za-z)]+-)?[\\d?/]+\\)", "");
        }

        if (_notation.startsWith("]")) {
            //check repeating position
            _notation = _notation.replaceFirst("]", "");
        }

        //check bisecting
        if (_notation.matches("\\[(([\\d,]+-.?deoxy-)|\\de)?([?DL]-)?[A-Z].+")) {
            _notation = _notation.replaceFirst("\\[", "");
        }

        if (_notation.endsWith(")]")) {
            _notation = _notation.replace("]", "");
        }

        //remove ambiguous anchor
        _notation = _notation.replaceAll("=?[?\\d]\\$,?", "");

        return _notation;
    }

    private String extractLinkage (String _notation) {
        String donor = "[ab?]?[\\d?]";
        String bridge = "\\d?(Tri-)?[(A-Za-z)]+\\d?";
        String acceptor = "[\\d?/]+[ab?]?";

        String edge = donor + "-" + acceptor;
        String root = donor + "-";
        String bridgeEdge = donor + "-" + bridge + "-" + acceptor;

        Matcher matPos = Pattern.compile("\\(" + "(" + root + "[:)]*|" + bridgeEdge + "[:)]*|" + edge + "[:)]*)+$").matcher(_notation);
        if (matPos.find()) {
            return matPos.group(0);
        } else {
            return "";
        }
    }

    private boolean isSubstituent (String _notation) {
        if (isModification(_notation)) return false;
        String simple = "[\\d?]";
        String bridge = "[\\d?](-[\\d?])?,[\\d?](-[\\d?])?";
        String multiple = "([\\d?],[\\d?])+";
        String fuzzy = "([\\d?]/[\\d?])+";
        String regex = "(" + simple + ":?|" + bridge + ":?|" + multiple + ":?|" + fuzzy + ":?)+";
        String notation = "(Tri-)?([(A-Za-z)]+)\\d?";

        return (_notation.matches("^(?![468][de])" + "(" + regex + notation + ")+"));
    }

    private boolean isModification (String _notation) {
        return (_notation.matches("^(\\d,?)+-(.*)?(deoxy|Anhydro|aldehyde)-.*"));
    }

    private String trimHead (String _temp) {
        return _temp.substring(1);
    }

    private String replaceTemplate (String _temp, String _regex) {
        return _temp.replaceFirst(_regex, "");
    }
}
