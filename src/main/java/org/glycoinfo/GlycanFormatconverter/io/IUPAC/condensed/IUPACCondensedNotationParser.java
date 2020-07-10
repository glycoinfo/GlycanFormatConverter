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
        if (_notation.matches("\\[[\\d?]\\)"))
            throw new GlyCoImporterException("Repeating structure is not support !");
        if (_notation.matches("[\\d?]\\)") || _notation.matches("\\([ab?][\\d?]-"))
            throw new GlyCoImporterException("Cyclic structure is not support !");
        //if (_notation.matches(".+=[\\d?]\\$,$"))
        //    throw new GlyCoImporterException("Glycan fragments is not support !");

        String temp = this.trimSymbols(_notation);
        String ringSize = "";
        Monosaccharide mono = new Monosaccharide();
        ArrayList<String> subNotation = new ArrayList<>();
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

        // parse modification at head
        Matcher matModi = Pattern.compile("(([\\d,?]+)+-(.*)?)?(aldehyde|deoxy|Anhydro)-").matcher(temp);
        if (matModi.find()) {
            String modification = matModi.group(0);
            String position = matModi.group(2);
            String prefix = matModi.group(3);
            String notation = matModi.group(4);

            if (notation.equals("deoxy")) {
                for (String pos : position.split(",")) {
                    modifications.add(pos + "d");
                }
            }
            if (notation.equals("Anhydro")) {
                for (String pos : position.split(",")) {
                    subNotation.add(pos + "Anhydro");
                }
            }
            if (notation.equals("aldehyde")) {
                modifications.add("aldehyde");
            }
            temp = temp.replace(modification, "");
        }

        // parse modification at tail
        Matcher matModiSuf = Pattern.compile("(-(onic|aric))").matcher(temp);
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

        // parse ulo
        Matcher matUlo = Pattern.compile("(([\\d,?]+)+(.*)?(ulo))").matcher(temp);
        if (matUlo.find()) {
            String position = matUlo.group(2);
            String notation = matUlo.group(4);

            for (String pos : position.split(",")) {
                modifications.add(pos + notation);
            }
            temp = temp.replace(matUlo.group(1), "");
        }

        /*
            parse complex monosaccharide such as DDmanHep
            group1 : configuration
            group2 : monosaccharide notation
            group3 : ring size
            group4 : superclass
         */
        Matcher matMono1 = Pattern.compile("([DL?]+)([a-z]{3})+([pf?])?([A-Z][a-z]{2})+").matcher(temp);
        if (matMono1.find()) {
            String configuration = "";
            int partSize = -1;
            int coreSize = -1;
            if (matMono1.group(1) != null) {
                configuration = String.valueOf(matMono1.group(1).charAt(0));
                temp = temp.replace(configuration, "");
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
                mono.addStereo(configuration.equals("?") ? "" : configuration.toLowerCase() + notation);
            } else {
                throw new GlyCoImporterException("Multiple name of monosaccharide can be assign to this monosaccharide : " + _notation);
            }
        }

        /*
          group1 : configuration
          group2 : native deoxy modification such as 6dTal
          group3 : monosaccharide notation
          group4 : ring size
          group5 : native substituent (5Gc, 5Ac, NAc, NGc, NA, A, N)
         */
        Matcher matMono = Pattern.compile("([LD?]-?)?([468]?[dei])?([A-Z][a-z]{1,2}C?|KDN)([pf?])?(5[GA]c|N[AG]c|NA|A|N)?").matcher(temp);
        String threeLetterCode = "";
        if (matMono.find()) {
            String configuration = "";
            String prefix = "";
            String coreName;

            //extract configuration
            if (matMono.group(1) != null) {
                configuration = matMono.group(1);
                temp = temp.replace(configuration, "");
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
                    mono.addStereo(configuration.equals("?") ? "" : configuration.toLowerCase() + stereo);
                }

                if (mono.getSuperClass() == null) {
                    mono.setSuperClass(threeCode.getSuperClass());
                }

                subNotation.addAll(threeCode.getSubstituents());
                modifications.addAll(threeCode.getModificaitons());
                temp = temp.replace(coreName, "");
            }

            //extract ring size
            if (matMono.group(4) != null) {
                ringSize = matMono.group(4);
                temp = temp.replace(ringSize, "");
            }

            //extract native substituent
            if (matMono.group(5) != null) {
                String sub = matMono.group(5);
                if (sub.matches("NA\\d?")) {
                    subNotation.add("N");
                    modifications.add("6A");
                }
                if (sub.equals("A")) {
                    modifications.add("6A");
                }
                if (sub.matches("N[GA]c") || sub.equals("N") || sub.matches("5[GA]c")) {
                    subNotation.add(sub);
                }

                temp = replaceTemplate(temp, sub);
            }
        }

        //parse linked substituent
        if (this.isSubstituent(temp)) {
            SubstituentIUPACNotationAnalyzer subAnalyze = new SubstituentIUPACNotationAnalyzer();
            subNotation.addAll(subAnalyze.resolveSubstituents(temp, true));
            temp = temp.replace(temp, "");
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
                if (i == 0 && item == '-') {
                    anomericState = AnomericStateDescriptor.UNKNOWN.getIUPACAnomericState();
                    anomericPosition = 1;
                }
            }
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
            mono = assignRingPosition(mono, mi, ringSize);

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

    private Monosaccharide assignRingPosition (Node _mono, MonosaccharideIndex _mi, String _ringSize) throws GlycanException {
        if (_mi == null) return (Monosaccharide) _mono;

        Monosaccharide mono = (Monosaccharide) _mono;
        int anomericPosition = ((Monosaccharide) _mono).getAnomericPosition();

        if (anomericPosition != -1) {
            if (((Monosaccharide) _mono).getAnomer().equals(AnomericStateDescriptor.UNKNOWN_STATE) && _ringSize.equals("?")) {
                mono.setRing(anomericPosition, Monosaccharide.UNKNOWN_RING);
                return (Monosaccharide) _mono;
            }
            if (_mi.getRingSize().equals("p") || _ringSize.equals("p")) {
                if (anomericPosition == 1) {
                    mono.setRing(anomericPosition, 5);
                }
                if (anomericPosition == 2) {
                    mono.setRing(anomericPosition, 6);
                }
            }
            if (_mi.getRingSize().equals("f") || _ringSize.equals("f")) {
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
        //check start repeating position
        if (_notation.matches(".+][n\\d-]+$")) {
            _notation = _notation.replaceAll("][n\\d-]+", "");
        }

        // modify open chain
        if (_notation.matches("^.+(-ol)(\\([\\d?]-)$")) {
            _notation = _notation.replaceFirst("(\\([\\d?]-)$", "");
        }

        if (_notation.endsWith(")]")) {
            _notation = _notation.replace("]", "");
        }

        //check cyclic at the right side
        if (_notation.matches("^[\\d?]\\).+$")) {
            _notation = _notation.replaceFirst("^[\\d?]\\)", "");
        }

        //remove ambiguous anchor
        _notation = _notation.replaceAll("=?[?\\d]\\$,?", "");

        return _notation;
    }

    private String extractLinkage (String _notation) {
        StringBuilder ret = new StringBuilder();
        boolean isLinkage = false;

        if (_notation.endsWith("-") && !_notation.matches(".*[ab?][\\d?]?-$")) {
            return "-";
        }

        for (int i = _notation.length() - 1; i != 0; i--) {
            char item = _notation.charAt(i);
            ret.insert(0, item);
            if (item == '-') isLinkage = true;

            if (isLinkage) {
                if (item == '(') break;
                //if (item == 'a' | item == 'b') break;
                //if (item == '?' && _notation.charAt(i - 1) != '?') break;
            }
        }

        //TODO: 結合を含まない単糖に対してどう対応するか、エラーとして扱うか
        if (ret.toString().matches("\\([ab?]?[\\d?]-([(A-Za-z)]+-)?([\\d?/]+)?\\)?")) return ret.toString();
        else return "";
    }

    private boolean isSubstituent (String _notation) {
        if (isModification(_notation)) return false;
        return (_notation.matches("^(?![468][de])\\d.+"));
    }

    private boolean isModification (String _notation) {
        return (_notation.matches("^(\\d,?)+-(.*)?(deoxy|Anhydro)-.*"));
    }

    private String trimHead (String _temp) {
        return _temp.substring(1);
    }

    private String replaceTemplate (String _temp, String _regex) {
        return _temp.replace(_regex, "");
    }
}
