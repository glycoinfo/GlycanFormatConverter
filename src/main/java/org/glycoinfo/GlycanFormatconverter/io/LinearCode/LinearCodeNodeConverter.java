package org.glycoinfo.GlycanFormatconverter.io.LinearCode;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoExporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.util.comparater.GlyCoSubstituentComparator;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by e15d5605 on 2017/10/06.
 */
public class LinearCodeNodeConverter {

    public LinearCodeSUDictionary start(Node _node) throws GlyCoExporterException, ConverterExchangeException, GlycanException {
        IUPACNotationConverter iupacConv = new IUPACNotationConverter();
        //iupacConv.start(_node);
        iupacConv.makeTrivialName(_node);

        LinearCodeSUDictionary lcDict = LinearCodeSUDictionary.forTrivialName(iupacConv.getThreeLetterCode());

        if (lcDict == null) throw new GlyCoExporterException("This glycan contins unsupported monosaccharide.");

        return lcDict;
    }

    public String makeLCNotation (Node _node) throws GlyCoExporterException, GlycanException, ConverterExchangeException {
        String lcUnit = "";

        LinearCodeSUDictionary lcDict = start(_node);

        Monosaccharide mono = (Monosaccharide) _node;

        lcUnit += lcDict.getLinearCode();

        /* extract configuration*/
        BaseTypeDictionary baseDict = BaseTypeDictionary.forName(mono.getStereos().getLast());

        /* extract ring size */
        if (baseDict.getConfiguration().equals("d")) {
            if (convertRingSize(mono.getRingStart(), mono.getRingEnd()) == 'f') lcUnit += "^";
        }
        if (baseDict.getConfiguration().equals("l")) {
            if (convertRingSize(mono.getRingStart(), mono.getRingEnd()) == 'p') lcUnit += "'";
            if (convertRingSize(mono.getRingStart(), mono.getRingEnd()) == 'f') lcUnit += "~";
        }

        /* append substituent notation */
        HashMap<String, String> mapSub = makeSubstituentNotation(_node, lcDict);
        lcUnit += mapSub.get("outside").equals("[]") ? "" : mapSub.get("outside");

        /* append anomeric symbol */
        lcUnit += mono.getAnomer().equals(AnomericStateDescriptor.UNKNOWN_STATE) ?
                AnomericStateDescriptor.UNKNOWN.getAnomericState() : mono.getAnomer().getAnomericState();

        /* append anomeric substituent */
        //TODO : 修飾に限らずanomeric modification も考慮しなければならない
        //TODO : 架橋の場合はポジション抜きのノーてションのみ
        lcUnit += mapSub.get(mono.getAnomer().toString()).equals("[]") ?
                "" : mapSub.get(mono.getAnomer().toString());

        return lcUnit;
    }

    private char convertRingSize (int _start, int _end) {
        if (_start == -1) return '?';

        if (_start == 1) {
            if (_end == 4) return 'f';
            if (_end == 5) return 'p';
        }
        if (_start == 2) {
            if (_end == 5) return 'f';
            if (_end == 6) return 'p';
        }

        return '?';
    }

    public HashMap<String, String> makeSubstituentNotation (Node _node, LinearCodeSUDictionary _lcDict) throws GlyCoExporterException {
        HashMap<String, String> ret = new HashMap<>();
        StringBuilder anomSub = new StringBuilder("[");
        StringBuilder coreSub = new StringBuilder("[");
        Monosaccharide mono = (Monosaccharide) _node;

        ArrayList<Edge> subs = new ArrayList<>();
        for (Edge childEdge : _node.getChildEdges()) {
            Substituent sub = (Substituent) childEdge.getSubstituent();
            if (sub == null) continue;
            if (sub instanceof GlycanRepeatModification) continue;
            if (sub.getSubstituent() instanceof CrossLinkedTemplate) continue;
            subs.add(childEdge);
        }

        Collections.sort(subs, new GlyCoSubstituentComparator());

        for (Edge subEdge : subs) {
            Substituent sub = (Substituent) subEdge.getSubstituent();
            LinearCodeSubstituentDictionary lcSubDict =
                    LinearCodeSubstituentDictionary.forIUPACNotation(sub.getNameWithIUPAC());

            if (isNativeSubstituent(_lcDict, sub)) continue;

            if (lcSubDict == null)
                throw new GlyCoExporterException(sub.getNameWithIUPAC() + " could not support !");

            if (mono.getAnomericPosition() ==
                    Integer.parseInt(extractPosition(sub.getFirstPosition().getParentLinkages()))) {
                anomSub.append(lcSubDict.getLinearCodeNotation());
            } else {
                coreSub.append(extractPosition(sub.getFirstPosition().getParentLinkages()));
                coreSub.append(lcSubDict.getLinearCodeNotation());
            }
        }

        anomSub.append("]");
        coreSub.append("]");

        ret.put(mono.getAnomer().toString(), anomSub.toString());
        ret.put("outside", coreSub.toString());

        return ret;
    }

    private String extractPosition (ArrayList<Integer> _positions) {
        StringBuilder ret = new StringBuilder();

        for(Iterator<Integer> iterPos = _positions.iterator(); iterPos.hasNext();) {
            Integer elm = iterPos.next();
            ret.append(elm == -1 ? "?" : elm);
            if(iterPos.hasNext()) ret.append("/");
        }

        return ret.toString();
    }

    private boolean isNativeSubstituent (LinearCodeSUDictionary _lcDict, Substituent _sub) {
        if (_lcDict.equals(LinearCodeSUDictionary.GALNAC) || _lcDict.equals(LinearCodeSUDictionary.GLCNAC)) {
            if (!extractPosition(_sub.getFirstPosition().getParentLinkages()).equals("2")) return false;
            if (_sub.getSubstituent().equals(SubstituentTemplate.N_ACETYL)) return true;
        }

        if (_lcDict.equals(LinearCodeSUDictionary.NEUAC)) {
            if (!extractPosition(_sub.getFirstPosition().getParentLinkages()).equals("5")) return false;
            if (_sub.getSubstituent().equals(SubstituentTemplate.N_ACETYL)) return true;
        }

        return false;
    }
}
