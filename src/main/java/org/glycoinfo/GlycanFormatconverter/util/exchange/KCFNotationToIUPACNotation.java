package org.glycoinfo.GlycanFormatconverter.util.exchange;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.KCF.KCFMonosaccharideDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.HexoseDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.PrefixDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.SubstituentIUPACNotationAnalyzer;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.MonosaccharideNotationAnalyzer;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2017/08/01.
 */
public class KCFNotationToIUPACNotation {

    public String start (String _input) throws ConverterExchangeException, GlyCoImporterException, GlycanException {
        String ulosonic = "";
        String ringSize = "";
        String tailStatus = "";

        boolean haveMod = false;

        KCFMonosaccharideDescriptor firstUnit = null;
        String firstConfig = "";
        KCFMonosaccharideDescriptor secondUnit = null;
        String secondConfig = "";

        SuperClass superClass = null;

        ArrayList<String> subs = new ArrayList<>();
        ArrayList<String> mods = new ArrayList<>();

        /* check monosaccharide notation */
        if (MonosaccharideNotationAnalyzer.start(_input) == false)
            throw new GlyCoImporterException(_input + " is not found!");

        /* extract prefix annotation */
        Matcher prefixSub = Pattern.compile("(\\d)-([CO])-([Ff]ormyl|[Mm]ethyl)").matcher(_input);
        if (prefixSub.find()) {
            if (prefixSub.group(3).equalsIgnoreCase("formyl")) {
                SubstituentTemplate subT = SubstituentTemplate.C_FORMYL;
                subs.add(prefixSub.group(1) + subT.getIUPACnotation());
                _input = _input.replace(prefixSub.group(), "");
            }
            if (prefixSub.group(3).equalsIgnoreCase("methyl")) {
                SubstituentTemplate subT = SubstituentTemplate.C_METHYL;
                subs.add(prefixSub.group(1) + subT.getIUPACnotation());
                _input = _input.replace(prefixSub.group(), "");
            }
        }

        Matcher matAnhydro = Pattern.compile("-?(\\d,\\d-Anhydro)-").matcher(_input);
        if (matAnhydro.find()) {
            mods.add(matAnhydro.group(1));
            _input = _input.replace(matAnhydro.group(), "");
        }

        /* extract unsaturation */
        Matcher unsat = Pattern.compile("-?(\\d)-?(enx|en)-").matcher(_input);
        if (unsat.find()) {
            haveMod = true;
            subs.add(unsat.group(1) + unsat.group(2));
            _input = _input.replace(unsat.group(), "");
        }

        /* extract deoxy notation */
        Matcher matDeoxy = Pattern.compile("([\\d,]+)+(d|-deoxy-)+").matcher(_input);
        if (matDeoxy.find()) {
            haveMod = true;
            mods.add(analyzeDeoxy(matDeoxy.group(1)));
            _input = _input.replace(matDeoxy.group(), "");
        }

        /* extract ulosonation */
        Matcher matUlo = Pattern.compile("-?([\\d,]+)-?(.*ulo)").matcher(_input);
        if (matUlo.find()) {
            haveMod = true;
            for (String unit : matUlo.group(1).split(",")) {
                ulosonic = ulosonic + (unit + "ulo");
            }
            _input = _input.replace(matUlo.group(), "");
        }

        /* extract head modification */
        Matcher matHead = Pattern.compile("(-(ol|onic|aric|uronic))").matcher(_input);
        if (matHead.find()) {
            tailStatus = matHead.group(1);
            _input = _input.replace(matHead.group(), "");
        }

        /* extract monosaccharide notation */
        int count = 0;
        for (KCFMonosaccharideDescriptor values : KCFMonosaccharideDescriptor.values()) {
            String code = "";
            String config = "";

            if (_input.contains(values.getCode())) {
                config = extractConfiguration(_input, values.getCode());
                ringSize = extractRingSize(_input, values.getCode());

                code = values.getCode();

                _input = _input.replace(config + values.getCode() + ringSize, "");

                if (values.equals(KCFMonosaccharideDescriptor.THO)) {
                    values = KCFMonosaccharideDescriptor.THR;
                    code = values.getCode();
                }
                count++;
            }

            if (_input.contains(values.getCode().toLowerCase())) {
                config = extractConfiguration(_input, values.getCode().toLowerCase());
                ringSize = extractRingSize(_input, values.getCode().toLowerCase());

                code = values.getCode().toLowerCase();

                _input = _input.replace(config + values.getCode().toLowerCase() + ringSize, "");

                if (values.equals(KCFMonosaccharideDescriptor.THO)) {
                    values = KCFMonosaccharideDescriptor.THR;
                    code = values.getCode().toLowerCase();
                }
                count++;
            }

            if (!code.equals("")) {
                if (count == 1) firstUnit = values;
                else secondUnit = values;
            }
            if (!config.equals("")) {
                if (count == 1) firstConfig = config;
                else secondConfig = config;
            }
        }

        /* extract supar class */
        for (SuperClass values : SuperClass.values()) {
            if (_input.contains(values.getSuperClass())) {
                ringSize = extractRingSize(_input, values.getSuperClass());
                String config = extractConfiguration(_input, values.getSuperClass());

                superClass = values;

                if (firstUnit == null && secondUnit == null) {
                    firstConfig = config;
                }

                _input = _input.replace(config + values.getSuperClass() + ringSize, "");
            }
        }

        /* parse substituents */
        SubstituentIUPACNotationAnalyzer subAna = new SubstituentIUPACNotationAnalyzer();
        subs.addAll(subAna.resolveSubstituents(trimHyphen(_input), true));

        StringBuilder notation = new StringBuilder();

        /* append prefix substituents */
        notation = appendPrefixAnnotations(notation, mods);

        /* append core notation */
        if (firstUnit != null) {
            notation = appendCoreNotation(notation, modifyConfiguration(firstConfig, firstUnit.getCode()), firstUnit, (superClass != null));
        }
        if (firstUnit == null && secondUnit == null) {
            notation.append(((firstConfig.equals("D/L") || firstConfig.equals("")) ? "?" : firstConfig) + "-");
        }

        /* anomeric status */
        if (secondUnit != null) {
            notation.append("-?-");
            if (secondConfig.equals("")) secondConfig = modifyConfiguration(firstConfig, secondUnit.getCode());
            notation = appendCoreNotation(notation, secondConfig, secondUnit, true);
        }

        /* append super class */
        if (superClass != null) notation.append(superClass.getSuperClass());

        /* append ulosonic status */
        notation.append(ulosonic);

        /* append ring size */
        if (secondUnit != null) {
            if (!haveMod) ringSize = "?";
            else ringSize = modifyRingSize(ringSize, secondConfig, secondUnit);
        }
        if (firstUnit != null && secondUnit == null) {
            ringSize = modifyRingSize(ringSize, firstConfig, firstUnit);
            if (!haveDeoxy(mods) && haveUnsaturation(subs)) ringSize = "?";
            if (haveDeoxy(mods)) {
                if (!haveMultipleDeoxy(mods, superClass)) ringSize = "?";
                if (firstUnit.equals(KCFMonosaccharideDescriptor.ARA)) ringSize = "p";
            }
        }
        if (firstUnit == null && secondUnit == null && superClass != null) {
            ringSize = "p";

            if (!firstConfig.equals("") && !haveDeoxy(mods)) ringSize = "?";
        }
        notation.append(ringSize);

        /* append substituents */
        String code = "";
        if (firstUnit != null && secondUnit == null) code = firstUnit.getCode();
        if (secondUnit != null) code = secondUnit.getCode();
        notation = appendAcidicStatus(notation, subs, code);

        /* append tail modification */
        notation.append(tailStatus);

        System.out.println(notation);

        return notation.toString();
    }

    private boolean haveDeoxy (ArrayList<String> _mods) {
        boolean ret = false;
        for (String unit : _mods) {
            if (unit.contains("deoxy")) ret = true;
        }
        return ret;
    }

    private boolean haveUnsaturation (ArrayList<String> _subs) {
        for (String unit : _subs) {
            if (unit.contains("en")) return true;
        }

        return false;
    }

    private boolean haveMultipleDeoxy(ArrayList<String> _mods, SuperClass _superClass) {
        if (_superClass == null) return false;

        int count = 0;

        for (String unit : _mods) {
            if (!unit.contains("deoxy")) continue;
            count = unit.substring(0, unit.indexOf("-")).split(",").length;
        }

        return (count > 1);
    }

    //TODO : NAcA -> 2NAc, NFoA -> 2NFo,
    private StringBuilder appendAcidicStatus (StringBuilder _sb, ArrayList<String> _subs, String _code) throws GlyCoImporterException {
        String nativeSub = "";

        HexoseDescriptor hexDesc = HexoseDescriptor.forTrivialName(_code);

        /* append acidic status */
        for (String unit : _subs) {
            if (unit.matches("\\d.*")) continue;
            nativeSub = unit;
        }

        if (nativeSub.equals("A") || nativeSub.matches("N(Ac|Fo\\w*)?A") || nativeSub.matches("AN(Ac|Fo\\w*)?")) {
            _subs.remove(nativeSub);
            _sb.append("A");
            if (nativeSub.endsWith("A")) {
                nativeSub = nativeSub.substring(0, nativeSub.length() - 1);
            }
            if (nativeSub.startsWith("A") && !nativeSub.equals("A")) {
                nativeSub = nativeSub.substring(1, nativeSub.length());
            }
            if (!nativeSub.equals("A")) _subs.add(nativeSub);
        }

        String modifiedSub = modifySubstituentNotation(nativeSub);

        /* modify core substituents */
        if (hexDesc != null) {
            if (nativeSub.matches ("[GA]c") && hexDesc.equals(HexoseDescriptor.NEU)) {
                _subs.remove(nativeSub);
                _subs.add(5 + modifiedSub);
            }
            if (nativeSub.matches("N[AG]c") && hexDesc.equals(HexoseDescriptor.NEU)) {
                _subs.remove(nativeSub);
                _subs.add(modifiedSub.replaceFirst("N", "5"));
            }
            if (nativeSub.matches("[GA]c") && !hexDesc.equals(HexoseDescriptor.NEU)) {
                _subs.remove(nativeSub);
                _subs.add(2 + modifiedSub);
            }
            if (nativeSub.matches("(?!NAc)N\\w+") && !hexDesc.equals(HexoseDescriptor.NEU)) {
                _subs.remove(nativeSub);

                /*
                SubstituentTemplate subT = SubstituentTemplate.forIUPACNotationWithIgnore(modifiedSub);
                SubstituentUtility subUtil = new SubstituentUtility();

                if (subUtil.isNLinkedSubstituent(subT) &&
                        (!hexDesc.equals(HexoseDescriptor.FUC) && !hexDesc.equals(HexoseDescriptor.QUI) && !hexDesc.equals(HexoseDescriptor.RHA))) {
                    _sb.append("N");
                    _subs.add(2 + subUtil.convertNTypeToOType(subT).getIUPACnotation());

                } else {
                */
                    _subs.add(2 + modifiedSub);
                //}
            }
            if ((nativeSub.equals("N") || nativeSub.equals("NAc")) && !hexDesc.equals(HexoseDescriptor.NEU)) {
                _subs.remove(nativeSub);
                _sb.append(modifiedSub);
            }
        } else {
            if (nativeSub.startsWith("N")) {//nativeSub.equals("NAc") || nativeSub.equals("N")) {
                _subs.remove(nativeSub);
                _subs.add(2 + modifiedSub);
            }
        }

        LinkedHashMap<String, ArrayList<String>> sortedSubs = new LinkedHashMap<>();
        Collections.sort(_subs);
        for (String unit : _subs) {
            Matcher matSub = Pattern.compile("([\\d,]+)+([(\\w]+.*)").matcher(unit);
            if (matSub.find()) {
                String subNode = modifySubstituentNotation(matSub.group(2));
                //TODO : modify substituent notation

                if (!sortedSubs.containsKey(subNode)) {
                    ArrayList<String> positions = new ArrayList<>();
                    positions.add(matSub.group(1));
                    sortedSubs.put(subNode, positions);
                } else {
                    ArrayList<String> positions = sortedSubs.get(subNode);
                    positions.add(matSub.group(1));
                    sortedSubs.put(subNode, positions);
                }
            }
        }

        for (String key : sortedSubs.keySet()) {
            StringBuilder sub = new StringBuilder();
            for (Iterator<String> iterPos = sortedSubs.get(key).iterator(); iterPos.hasNext();) {
                sub.append(iterPos.next());
                if (iterPos.hasNext()) {
                    sub.append(",");
                }
            }

            sub.append(key);

            if (sortedSubs.get(key).size() > 1) sub.append(sortedSubs.get(key).size());

            _sb.append(sub);
        }

        return _sb;
    }

    private StringBuilder appendCoreNotation (StringBuilder _sb, String _configuration, KCFMonosaccharideDescriptor _kcfDesc, boolean _isLowCase) {
        if (_configuration != null && _kcfDesc != null) {
            _sb.append(_configuration);
            _sb.append("-");
            _sb.append(_isLowCase ? _kcfDesc.getCode().toLowerCase() : _kcfDesc.getCode());
        }

        return _sb;
    }

    private StringBuilder appendPrefixAnnotations (StringBuilder _sb, ArrayList<String> _mods) {
        TreeMap<String, String> sorted = new TreeMap<>();

        /* sort modifications */
        for (String unit : _mods) {
            String[] items = unit.split("-");
            sorted.put(items[1], items[0]);
        }

        for (String key : sorted.keySet()) {
            _sb.append(sorted.get(key) + "-" + key + "-");
        }

        return _sb;
    }

    private String modifySubstituentNotation (String _subNotation) throws GlyCoImporterException {
        SubstituentTemplate subT = SubstituentTemplate.forIUPACNotationWithIgnore(_subNotation);

        if (subT == null) {
            subT = SubstituentTemplate.forGlycoCTNotationWithIgnore(_subNotation);
        }
        if (subT == null && _subNotation.startsWith("N")) {
            _subNotation = _subNotation.replaceFirst("N", "N-");
            subT = SubstituentTemplate.forGlycoCTNotationWithIgnore(_subNotation);
        }
        if (subT == null) {
            if (_subNotation.equals("Me")) {
                subT = SubstituentTemplate.METHYL;
            }
            if (_subNotation.contains("Pyr") || _subNotation.contains("pyr")) {
                if (_subNotation.startsWith("(R")) subT = SubstituentTemplate.R_PYRUVATE;
                if (_subNotation.startsWith("(S")) subT = SubstituentTemplate.S_PYRUVATE;
                if (_subNotation.startsWith("Pyr") || _subNotation.startsWith("pyr")) subT = SubstituentTemplate.X_PYRUVATE; //TODO :pyrも
            }
        }

        if (subT == null) return _subNotation;

        /*
        if (subT.equals(SubstituentTemplate.ACETYL) && matNode.group(1) == null) {
            ret = ret + "2" + subT.getIUPACnotation();
        } else {
            ret = ret + ((matNode.group(1) != null) ? matNode.group(1) : "") + subT.getIUPACnotation();
        }
        */

        return subT.getIUPACnotation();
    }

    private String modifyConfiguration (String _configuration, String _notation) {
        if (_configuration.equals("D/L") || _configuration.equals("L/D")) return "?";

        if (_notation.contains("Col") || _notation.contains("Asc")) return "L";

        if (!_configuration.equals("")) return _configuration;

        return "D";

        //MonosaccharideIndex modInd = MonosaccharideIndex.forTrivialNameWithIgnore(_notation);
        //if (modInd != null) return modInd.getFirstConfiguration();

        //return _configuration;
    }

    private String modifyRingSize (String _ringSize, String _config, KCFMonosaccharideDescriptor _kcfDesc) {
        if (!_ringSize.equals("")) return _ringSize;

        if (_kcfDesc.equals(KCFMonosaccharideDescriptor.THR) ||
                /*_kcfDesc.equals(KCFMonosaccharideDescriptor.ERY) || */
                _kcfDesc.equals(KCFMonosaccharideDescriptor.API)) {
            if (!_config.equals("D")) return "?";
            return "f";
        }

        if (_kcfDesc.equals(KCFMonosaccharideDescriptor.RIB)) return "f";
        if (_kcfDesc.equals(KCFMonosaccharideDescriptor.FRU)) return "?";
        if (_kcfDesc.equals(KCFMonosaccharideDescriptor.ARA)) {
            if (_config.equals("L")) return "?";
            else return "p";
        }

        return "p";
        //MonosaccharideIndex modInd = MonosaccharideIndex.forTrivialNameWithIgnore(_notation);
        //if (modInd != null) return modInd.getRingSize();

        //return "?";
    }

    private String extractRingSize (String _kcfNotation, String _notation) {
        String ringSize = "";

        Matcher matRing = Pattern.compile(_notation + "([pf?])" + ".*").matcher(_kcfNotation);
        if (matRing.find()) {
            ringSize = matRing.group(1);
        }

        return ringSize;
    }

    private String extractConfiguration (String _kcfNotation, String _notation) {
        String configuration = "";

        Matcher matConfig = Pattern.compile("([DL/?]+)" + "-?" + _notation + ".*").matcher(_kcfNotation);
        if (matConfig.find()) {
            configuration = matConfig.group(1);
        }

        return configuration;
    }

    private String analyzeDeoxy (String _position) throws ConverterExchangeException {
        PrefixDescriptor preDesc = PrefixDescriptor.forNumber(_position.split(",").length);
        return (_position + "-" + preDesc.getPrefix() + "deoxy");
    }

    private String trimHyphen (String _notation) {
        _notation = _notation.replaceAll("-", "");

        return _notation;
    }
}
