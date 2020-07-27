package org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;

import org.glycoinfo.GlycanFormatconverter.util.comparater.MAPMASSComparator;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.array.WURCSImporter;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;
import org.glycoinfo.WURCSFramework.util.property.AtomicProperties;
import org.glycoinfo.WURCSFramework.util.subsumption.MSStateDeterminationUtility;
import org.glycoinfo.WURCSFramework.util.subsumption.WURCSSubsumptionConverter;
import org.glycoinfo.WURCSFramework.wurcs.array.MS;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by e15d5605 on 2019/03/04.
 */
public class SkeletonCodeToStereo {

    public SkeletonCodeToStereo() {
    }

    public LinkedList<String> start (Backbone _backbone) throws WURCSFormatException, ConverterExchangeException {
        HashMap<Integer, ArrayList<Modification>> modMap = new HashMap<>();
        StringBuilder sc = new StringBuilder(_backbone.getSkeletonCode());

        // check SkeletonCode (to validator ?)
        if (containUndefinedCarbonDescriptor(_backbone)) {
            throw new ConverterExchangeException("This SkeletonCode partially contains the wild card (x or X).");
        }

        // extract branch substituent position
        if (!_backbone.hasUnknownLength()) {
        //if (!(_backbone instanceof BackboneUnknown_TBD)) {
            extractSubstituentOnBranchPosition(_backbone, modMap);
        }

        // check whether there is substituent to the branches
        if (!this.haveSubstituent(modMap)) {
            throw new ConverterExchangeException("This SkeletonCode contains H_LOSE in backbone without substituent.");
        }

        // compare substituent(s) on the branching position
        for (Integer pos : modMap.keySet()) {
            char newCD = this.modifyCarbonDescriptor(sc.charAt(pos-1), modMap.get(pos));
            sc.replace(pos-1, pos, String.valueOf(newCD));
        }

        //
        MS ms = new WURCSImporter().extractMS(sc.toString());
        MSStateDeterminationUtility msUtility = new MSStateDeterminationUtility();
        WURCSSubsumptionConverter wsc = new WURCSSubsumptionConverter();
        LinkedList<String> stereos = msUtility.extractStereo(ms);

        // retry define stereo
        if(stereos.isEmpty()) stereos = checkStereos(ms);

        // modify fuzzy configuration such as d/l-
        stereos = this.modifyFuzzyConfiguration(stereos);

        return stereos;
    }

    private LinkedList<String> checkStereos(MS _ms) throws ConverterExchangeException {
        LinkedList<String> a_aStereo = new LinkedList<>();
        String a_sSkeletonCode = _ms.getSkeletonCode();

        if (a_sSkeletonCode.equals("<Q>")) {
            a_aStereo.addLast("Sugar");
        }

        /* check uncertain groups */
        if (haveUncertainGroups(_ms)) return a_aStereo;

        if(a_aStereo.isEmpty()) {
            throw new ConverterExchangeException(_ms.getSkeletonCode() + " could not handled");
        }

        return a_aStereo;
    }

    private boolean haveUncertainGroups (MS _ms) {
        boolean ret = false;

        for (int i = 0; i < _ms.getSkeletonCode().length(); i++) {
            if (i == 0 || i == _ms.getSkeletonCode().length() - 1) continue;
            CarbonDescriptor cd =
                    CarbonDescriptor.forCharacter(_ms.getSkeletonCode().charAt(i), false);

            //if (cd.equals(CarbonDescriptor.SS3_STEREO_X)) {
            if (cd.equals(CarbonDescriptor.SS3_STEREO_X_L)) {
                ret = true;
            } else {
                ret = false;
                break;
            }
        }

        return ret;
    }

    private LinkedList<String> modifyFuzzyConfiguration (LinkedList<String> _stereos) {
        LinkedList<String> ret = new LinkedList<>();
        for (String stereo : _stereos) {
            if (!stereo.startsWith("d/l-")) {
                ret.add(stereo);
                continue;
            }
            ret.add(stereo.replaceFirst("d/l-", ""));
        }
        return ret;
    }

    private String checkDLconfiguration (String _stereo) {
        if((_stereo.startsWith("l") || _stereo.startsWith("d")))
            _stereo = _stereo.replaceFirst("[ld]", "");
        return _stereo;
    }

    private boolean containUndefinedCarbonDescriptor (Backbone _backbone) {
        boolean isNormal = false;
        boolean isWild = false;
        for (BackboneCarbon bc : _backbone.getBackboneCarbons()) {
            char cd = bc.getDescriptor().getChar();
            if (cd == '1' || cd == '2' || cd == '3' || cd == '4' || cd == '5' || cd == '6' || cd == '7' || cd == '8') {
                isNormal = true;
            }
            if ((cd == 'x' || cd == 'X')) {
                isWild = true;
            }

            if (isNormal && isWild) break;
        }

        return (isNormal && isWild);
    }

    private HashMap<Integer, ArrayList<Modification>>  extractBranchingPoints
            (Backbone _backbone, HashMap<Integer, ArrayList<Modification>> _modMap) {
        String skeletonCode = _backbone.getSkeletonCode();
        for (int i = 0; i < skeletonCode.length(); i++) {
            char carbon = skeletonCode.charAt(i);
            if ( carbon == '5' || carbon == '6' || carbon == '7' || carbon == '8' || carbon == 'X' )
                _modMap.put(i+1, new ArrayList<>());
        //        ret.add(i+1);
        }
        return _modMap;
    }

    private HashMap<Integer, ArrayList<Modification>> extractSubstituentOnBranchPosition
            (Backbone _backbone, HashMap<Integer, ArrayList<Modification>> _modMap) {
        _modMap = this.extractBranchingPoints(_backbone, _modMap);
        String skeletonCode = _backbone.getSkeletonCode();

        for (WURCSEdge we : _backbone.getChildEdges()) {
            Modification mod = we.getModification();
            if (mod.isGlycosidic() || mod.isRing() || (mod instanceof ModificationRepeat)) continue;
            if (mod.getParentEdges().size() != 1) continue;

            for (LinkagePosition lp : we.getLinkages()) {
                if (lp.getBackbonePosition() == -1) continue;
                char cd = skeletonCode.charAt(lp.getBackbonePosition() - 1);
                if (!this.isChiralCarbonDescriptor(cd)) continue;

                if (_modMap.containsKey(lp.getBackbonePosition())) {
                    _modMap.get(lp.getBackbonePosition()).add(mod);
                }
            }
        }

        return _modMap;
    }

    private boolean haveSubstituent (HashMap<Integer, ArrayList<Modification>> _modMap) {
        boolean ret = true;

        for (Integer pos : _modMap.keySet()) {
            if (_modMap.get(pos).isEmpty()) ret = false;
        }

        return ret;
    }

    //TODO : 検証が必要
    private char modifyCarbonDescriptor (char _carbonDescriptor, ArrayList<Modification> _mods) {
        boolean isSwap = (this.compareSubstituentGroups(_mods) < 0);

        char ret = this.replaceCarbonDescriptorByStereo(_carbonDescriptor, isSwap);

        return ret;
    }

    private char replaceCarbonDescriptorByStereo (char _carbonDescriptor, boolean _isSwap) {
        char replacedCD =
                (_carbonDescriptor == '5') ? '1' :
                (_carbonDescriptor == '6') ? '2' :
                (_carbonDescriptor == '7') ? '3' :
                (_carbonDescriptor == '8') ? '4' :
                (_carbonDescriptor == 'X') ? 'x' :
                (_carbonDescriptor == 'C') ? 'c' :
                (_carbonDescriptor == 'h') ? 'm' :
                (_carbonDescriptor == 'c') ? 'h' : _carbonDescriptor;

        if (_isSwap) {
            replacedCD =
                (replacedCD == '1') ? '2' :
                (replacedCD == '2') ? '1' :
                (replacedCD == '3') ? '4' :
                (replacedCD == '4') ? '3' : replacedCD;
        }

        return replacedCD;
    }

    private int compareSubstituentGroups (ArrayList<Modification> _mods) {
        // Compare several substituents and select one with the highest priority
        Collections.sort(_mods, new MAPMASSComparator());

        // extract highest priority MAP of substituent
        char atom = _mods.get(_mods.size()-1).getMAPCode().charAt(1);

        int atomMass = AtomicProperties.forSymbol(String.valueOf(atom)).getMassNumber();

        if (atomMass < 16) return 1;
        return -1;
    }

    private boolean isChiralCarbonDescriptor (char _carbonDescriptor) {
        return (_carbonDescriptor == '5' || _carbonDescriptor == '6' ||
                _carbonDescriptor == '7' || _carbonDescriptor == '8' || _carbonDescriptor == 'X');
    }
}
