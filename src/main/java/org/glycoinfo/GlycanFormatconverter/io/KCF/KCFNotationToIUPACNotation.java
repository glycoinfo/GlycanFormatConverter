package org.glycoinfo.GlycanFormatconverter.io.KCF;

import org.eurocarbdb.resourcesdb.template.MonosaccharideDictionary;
import org.glycoinfo.GlycanFormatconverter.Glycan.BaseSubstituentTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.SuperClass;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.BaseStereoIndex;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.HexoseDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.PrefixDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.MonosaccharideNotationAnalyzer;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.SubstituentIUPACNotationAnalyzer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2017/08/01.
 */

public class KCFNotationToIUPACNotation {

    public String start(String _input) throws GlyCoImporterException, GlycanException, TrivialNameException {
        KCFNodeStateStacker kcfStacker = new KCFNodeStateStacker();

        String ringSize = "";
        boolean haveMod = false;

        KCFMonosaccharideDescriptor firstUnit = null;
        KCFMonosaccharideDescriptor secondUnit = null;

        ArrayList<String> subs = new ArrayList<>();
        ArrayList<String> mods = new ArrayList<>();

        // check monosaccharide notation
        if (MonosaccharideNotationAnalyzer.start(_input) == false)
            throw new GlyCoImporterException(_input + " is not found!");

        // extract prefix annotation
        Matcher prefixSub = Pattern.compile("(\\d)-([CO])-([Ff]ormyl|[Mm]ethyl)").matcher(_input);
        if (prefixSub.find()) {
            String atomType = "";
            if (prefixSub.group(2) != null && prefixSub.group(2).equals("C")) {
                if (prefixSub.group(2).equals("C")) {
                    atomType = "C";
                }
                if (prefixSub.group(2).equals("O")) {
                    atomType = "O";
                }
            }

            if (prefixSub.group(3).equalsIgnoreCase("formyl")) {
                BaseSubstituentTemplate subT = BaseSubstituentTemplate.FORMYL;
                subs.add(prefixSub.group(1) + atomType + subT.getIUPACnotation());
                _input = _input.replace(prefixSub.group(), "");
            }
            if (prefixSub.group(3).equalsIgnoreCase("methyl")) {
                BaseSubstituentTemplate subT = BaseSubstituentTemplate.METHYL;
                subs.add(prefixSub.group(1) + atomType + subT.getIUPACnotation());
                _input = _input.replace(prefixSub.group(), "");
            }
        }

        Matcher matAnhydro = Pattern.compile("-?(\\d,\\d-Anhydro)-").matcher(_input);
        if (matAnhydro.find()) {
            mods.add(matAnhydro.group(1));
            _input = _input.replace(matAnhydro.group(), "");
        }

        // extract unsaturation
        Matcher unsat = Pattern.compile("-?(\\d)-?(enx|en)-").matcher(_input);
        if (unsat.find()) {
            haveMod = true;
            subs.add(unsat.group(1) + "(X)" + unsat.group(2));
            _input = _input.replace(unsat.group(), "");
        }

        // extract deoxy notation
        Matcher matDeoxy = Pattern.compile("([\\d,]+)+(d|-deoxy-)+").matcher(_input);
        if (matDeoxy.find()) {
            haveMod = true;
            mods.add(analyzeDeoxy(matDeoxy.group(1)));
            _input = _input.replace(matDeoxy.group(), "");
        }

        // extract ulosonation
        Matcher matUlo = Pattern.compile("-?([\\d,]+)-?(.*ulo)").matcher(_input);
        if (matUlo.find()) {
            haveMod = true;
            for (String unit : matUlo.group(1).split(",")) {
                kcfStacker.setUlosonic(unit + "ulo");
            }
            _input = _input.replace(matUlo.group(), "");
        }

        // extract head modification
        Matcher matHead = Pattern.compile("(-(ol|onic|aric|uronic))").matcher(_input);
        if (matHead.find()) {
            kcfStacker.setTailStatus(matHead.group(1));
            //tailStatus = matHead.group(1);
            _input = _input.replace(matHead.group(), "");
        }

        // extract monosaccharide notation
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
                if (count == 1) kcfStacker.setFisrtConfig(config);// firstConfig = config;
                else kcfStacker.setSecondConfig(config);
            }
        }

        // extract supar class
        for (SuperClass values : SuperClass.values()) {
            if (_input.contains(values.getSuperClass())) {
                ringSize = extractRingSize(_input, values.getSuperClass());
                String config = extractConfiguration(_input, values.getSuperClass());

                kcfStacker.setSuperClass(values);

                if (firstUnit == null && secondUnit == null) {
                    kcfStacker.setFisrtConfig(config);
                }

                _input = _input.replace(config + values.getSuperClass() + ringSize, "");
            }
        }

        // define a name of anonymous monosaccharide
        if (kcfStacker.getFisrtConfig().length() == 2) {
            // modify configurations
            kcfStacker.setSecondConfig(String.valueOf(kcfStacker.getFisrtConfig().charAt(1)));
            kcfStacker.setFisrtConfig(String.valueOf(kcfStacker.getFisrtConfig().charAt(0)));

            // modify unit type
            secondUnit = firstUnit;

            // define anonymous monosaccharide
            String anonymousCode = secondUnit.getCode().toLowerCase();
            int anonymousCarbonSize = kcfStacker.getSuperClass().getSize();
            BaseStereoIndex baseInd = BaseStereoIndex.forCode(anonymousCode);
            anonymousCarbonSize = anonymousCarbonSize - baseInd.getSize();

            if (anonymousCarbonSize == 1) {
                secondUnit = KCFMonosaccharideDescriptor.GRO;
            } else {
                throw new TrivialNameException ("This anonymous monosaccharide size has more than 1 carbon backbone.");
            }

        }

        // parse substituents
        SubstituentIUPACNotationAnalyzer subAna = new SubstituentIUPACNotationAnalyzer();
        subs.addAll(subAna.resolveSubstituents(trimHyphen(_input), true));

        // modified substituent notations
        //subs = modifySubstituentNotation(subs);

        kcfStacker.setRingSize(ringSize);
        kcfStacker.setFisrtUnit(firstUnit);
        kcfStacker.setSecondUnit(secondUnit);
        kcfStacker.setSubstituents(subs);
        kcfStacker.setModifications(mods);

        return remodelMonosaccharideNotation(kcfStacker).toString();
    }

    private StringBuilder remodelMonosaccharideNotation (KCFNodeStateStacker _kcfStacker) throws GlyCoImporterException {
        StringBuilder notation = new StringBuilder();

        SuperClass superClass = _kcfStacker.getSuperClass();
        String firstConfig = _kcfStacker.getFisrtConfig();
        String secondConfig = _kcfStacker.getSecondConfig();
        KCFMonosaccharideDescriptor firstUnit = _kcfStacker.getFirstUnit();
        KCFMonosaccharideDescriptor secondUnit = _kcfStacker.getSecondUnit();

        // append prefix substituents
        notation = appendPrefixAnnotations(notation, _kcfStacker.getModifications());

        // append core notation
        if (firstUnit != null) {
            notation = appendCoreNotation(notation, modifyConfiguration(firstConfig, firstUnit.getCode()), firstUnit, (superClass != null));
        }
        if (firstUnit == null && secondUnit == null) {
            notation.append(((firstConfig.equals("D/L") || firstConfig.equals("")) ? "?" : firstConfig) + "-");
        }

        // anomeric status
        if (secondUnit != null) {
            notation.append("-?-");
            if (secondConfig.equals("")) secondConfig = modifyConfiguration(firstConfig, secondUnit.getCode());
            notation = appendCoreNotation(notation, secondConfig, secondUnit, true);
        }

        // append super class
        if (superClass != null) notation.append(superClass.getSuperClass());

        // append ulosonic status
        notation.append(_kcfStacker.getUlosonic());

        // append ring size
        String ringSize = _kcfStacker.getRingSize();
        if (secondUnit != null) {
            //if (!haveMod) ringSize = "?";
            //else
            ringSize = modifyRingSize(ringSize, secondConfig, secondUnit);
        }
        if (firstUnit != null && secondUnit == null) {
            ringSize = modifyRingSize(ringSize, firstConfig, firstUnit);
            //if (!haveDeoxy(mods) && haveUnsaturation(subs)) ringSize = "?";
            //if (haveDeoxy(mods)) {
            //   if (!haveMultipleDeoxy(mods, superClass)) ringSize = "?";
            //   if (firstUnit.equals(KCFMonosaccharideDescriptor.ARA)) ringSize = "p";
            //}
        }
        if (firstUnit == null && secondUnit == null && superClass != null) {
            ringSize = "p";

            //if (!firstConfig.equals("") && !haveDeoxy(mods)) ringSize = "?";
        }

        notation.append(ringSize);

        // append substituents
        String code = "";
        if (firstUnit != null && secondUnit == null) code = firstUnit.getCode();
        if (secondUnit != null) code = secondUnit.getCode();
        notation = appendAcidicStatus(notation, _kcfStacker.getSubstituents(), code);

        // append tail modification
        notation.append(_kcfStacker.getTailStatus());

        return notation;
    }

    //TODO : NAcA -> 2NAc, NFoA -> 2NFo,
    private StringBuilder appendAcidicStatus (StringBuilder _sb, ArrayList<String> _subs, String _code) throws GlyCoImporterException {
        String nativeSub = "";

        HexoseDescriptor hexDesc = HexoseDescriptor.forTrivialName(_code);

        // append acidic status
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

        // modify core substituents
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

        // sort modifications
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
        if (_subNotation.equals("")) return "";
        if (_subNotation.matches("N?diMe")) _subNotation = _subNotation.replaceFirst("di", "Di");

        BaseSubstituentTemplate subT = BaseSubstituentTemplate.forIUPACNotationWithIgnore(_subNotation);

        if (subT == null) {
            subT = BaseSubstituentTemplate.forGlycoCTNotationWithIgnore(_subNotation);
        }
        if (subT == null && _subNotation.startsWith("N")) {
            _subNotation = _subNotation.replaceFirst("N", "N-");
            subT = BaseSubstituentTemplate.forGlycoCTNotationWithIgnore(_subNotation);
        }
        if (subT == null) {
            if (_subNotation.equals("Me")) {
                subT = BaseSubstituentTemplate.METHYL;
            }
            if (_subNotation.contains("Pyr") || _subNotation.contains("pyr")) {
                if (_subNotation.startsWith("(R")) subT = BaseSubstituentTemplate.R_PYRUVATE;
                if (_subNotation.startsWith("(S")) subT = BaseSubstituentTemplate.S_PYRUVATE;
                if (_subNotation.startsWith("Pyr") || _subNotation.startsWith("pyr")) subT = BaseSubstituentTemplate.X_PYRUVATE; //TODO :pyrも
            }
            if (_subNotation.equals("Formyl")) subT = BaseSubstituentTemplate.FORMYL;
        }

        if (subT == null) return this.appendHeadAtom(_subNotation);

        /*

          if (matSub.group(2).startsWith("C") || matSub.group(2).startsWith("O") || matSub.group(2).startsWith("N") ||
                        matSub.group(2).startsWith("(") || matSub.group(2).equals("A") || matSub.group(2).equals("N")) {
                    ret.add(sub);
                    continue;
                }

         */

        return this.appendHeadAtom(subT.getIUPACnotation());
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

        //Api, Ery, Tho, Thr 
        if (_kcfDesc.equals(KCFMonosaccharideDescriptor.THR) ||
                _kcfDesc.equals(KCFMonosaccharideDescriptor.ERY) ||
                _kcfDesc.equals(KCFMonosaccharideDescriptor.API) ||
                _kcfDesc.equals(KCFMonosaccharideDescriptor.THO)) {
            return "f";
        }

        return "p";
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

    private String analyzeDeoxy (String _position) throws TrivialNameException {
        PrefixDescriptor preDesc = PrefixDescriptor.forNumber(_position.split(",").length);
        return (_position + "-" + preDesc.getPrefix() + "deoxy");
    }

    private String trimHyphen (String _notation) {
        _notation = _notation.replaceAll("-", "");

        return _notation;
    }

    private String appendHeadAtom (String _subNode) {
        if (_subNode.startsWith("C") || _subNode.startsWith("O") || _subNode.startsWith("N") ||
                _subNode.startsWith("(") || _subNode.equals("A") || _subNode.equals("N")) {
            return _subNode;
        }
        return "O" + _subNode;
    }
}
