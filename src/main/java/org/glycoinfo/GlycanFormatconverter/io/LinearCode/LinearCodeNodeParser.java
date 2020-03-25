package org.glycoinfo.GlycanFormatconverter.io.LinearCode;

import org.glycoinfo.GlycanFormatconverter.Glycan.AnomericStateDescriptor;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
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
 * Created by e15d5605 on 2017/09/04.
 */
public class LinearCodeNodeParser {

    public Monosaccharide start (LinearCodeStacker _lcStacker) throws GlycanException, GlyCoImporterException {
        Monosaccharide mono = new Monosaccharide();
        MonosaccharideUtility monoUtil = new MonosaccharideUtility();
        ThreeLetterCodeAnalyzer threeAnalyzer = new ThreeLetterCodeAnalyzer();

        ArrayList<String> substituents = new ArrayList<>();
        ArrayList<String> modifications = new ArrayList<>();

        if (_lcStacker.getLinearCodeSU() != null) {
            String lcSUcode = _lcStacker.getLinearCodeSU();

            LinearCodeSUDictionary lcDict = LinearCodeSUDictionary.forLinearCode(lcSUcode);
            if (lcDict == null) throw new GlyCoImporterException(lcSUcode + " is wrong LinearCode SU notation!");

            // analyze trivial name
            threeAnalyzer.analyzeTrivialName(lcDict.getIupacThreeLetter(), null);

            mono.setStereos(threeAnalyzer.getStereos());
            mono.setSuperClass(threeAnalyzer.getSuperClass());

            // define anomeric position
            MonosaccharideIndex monoIndex = MonosaccharideIndex.forTrivialName(lcDict.getIupacThreeLetter());
            //if (_lcStacker.getAnomericStatus() == null) mono.setAnomericPosition(Monosaccharide.UNKNOWN_RING);
            //else
            if (monoIndex != null) mono.setAnomericPosition(monoIndex.getAnomerciPosition());
            //if (lcDict.equals(LinearCodeSUDictionary.SUGAR)) {
            //    mono.setAnomericPosition(Monosaccharide.UNKNOWN_RING);
            //}

            /* extract native modifications and substituents */
            if (lcDict.getNativeSubstituents().contains("Ac") || lcDict.getNativeSubstituents().contains("Gc"))
                substituents.add(lcDict.getNativeSubstituents());
            if (lcDict.getNativeSubstituents().equals("6A"))
                modifications.add(lcDict.getNativeSubstituents());
            substituents.addAll(threeAnalyzer.getSubstituents());
            modifications.addAll(threeAnalyzer.getModificaitons());
        }

        // define anomeric symbol
        AnomericStateDescriptor anomDec = AnomericStateDescriptor.UNKNOWN_STATE;
        if (_lcStacker.getAnomericStatus() != null) {
        		char anomState = _lcStacker.getAnomericStatus().charAt(0);
        		anomState = anomState == '?' ? 'x' : anomState;
            anomDec = AnomericStateDescriptor.forAnomericState(anomState);
        }
        mono.setAnomer(anomDec);

        // modify configuration and ring size
        LinkedList<String> configurations = new LinkedList<>();
        if (_lcStacker.getAnnotation() != null) {
            if (_lcStacker.getAnnotation().equals("'")) {
                //L, p
                configurations.add("L");
                mono = monoUtil.makeRingSize(mono, "p", "", threeAnalyzer.getModificaitons());
                mono = monoUtil.modifyStereos(mono, configurations);
            }
            if (_lcStacker.getAnnotation().equals("~")) {
                //L, f
                configurations.add("L");
                mono = monoUtil.makeRingSize(mono, "f", "", threeAnalyzer.getModificaitons());
                mono = monoUtil.modifyStereos(mono, configurations);
            }
            if (_lcStacker.getAnnotation().equals("^")) {
                //D, f
                configurations.add("D");
                mono = monoUtil.makeRingSize(mono, "f", "", threeAnalyzer.getModificaitons());
                mono = monoUtil.modifyStereos(mono, configurations);
            }
        } else {
            if (_lcStacker.getLinearCodeSU().equals("F")) {
	            	//for fucose
            		configurations.add("L");
            } else {
                //D, p (without fucose)            	
            		configurations.add("D");
            }
            
            mono = monoUtil.makeRingSize(mono, "p", "", threeAnalyzer.getModificaitons());
            mono = monoUtil.modifyStereos(mono, configurations);
        }
        
        // append substituents
        if (_lcStacker.getSubstituent() != null) {
            SubstituentIUPACNotationAnalyzer subAna = new SubstituentIUPACNotationAnalyzer();

            // convert LC notation to IUPAC notation
            substituents.addAll(exchangeLC2IUPACSubs(subAna.resolveSubstituents(_lcStacker.getSubstituent(), true)));
        }
        
        mono = monoUtil.appendSubstituents(mono, substituents);
        mono = monoUtil.appendModifications(mono, modifications);

        return mono;
    }

    private ArrayList<String> exchangeLC2IUPACSubs (ArrayList<String> _lcSubs) throws GlyCoImporterException {
        ArrayList<String> ret = new ArrayList<>();
        for (String unit : _lcSubs) {
            Matcher matSub = Pattern.compile("([\\d,]+)+([A-Z]+)+").matcher(unit);
            String iupac = "";
            if (matSub.find()) {
                if (matSub.group(1) != null) {
                    iupac = iupac + matSub.group(1);
                }
                if (matSub.group(2) != null) {
                    LinearCodeSubstituentDictionary lcSubdict =
                            LinearCodeSubstituentDictionary.forLinearCode(matSub.group(2));
                    if (lcSubdict == null) throw new GlyCoImporterException(unit + " is not found!");
                    iupac = iupac + lcSubdict.getIUPACNotation();
                }
            }

            ret.add(modifySubstituent(iupac));
        }

        return ret;
    }

    private String modifySubstituent (String _notation) {
        if (_notation.matches("^\\d.+")) return _notation;

        _notation = _notation.replaceAll(",", "");

        return _notation;
    }
}
