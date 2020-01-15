package org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;

import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.comparater.GlyCoModificationComparater;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by e15d5605 on 2017/07/19.
 */
public class BackboneToNode {

    public Node start(Backbone _backbone) throws GlycanException, WURCSFormatException, ConverterExchangeException {
        Monosaccharide ret = new Monosaccharide();
        String skeletonCode = _backbone.getSkeletonCode();

		// set superclass
        ret.setSuperClass(SuperClass.forSize(_backbone.getLength()));

		// extract anomeric state
        char anomericstate = _backbone.getAnomericSymbol();
        AnomericStateDescriptor enumAnom = AnomericStateDescriptor.forAnomericState(checkAnomericState(skeletonCode, anomericstate));
        ret.setAnomer(enumAnom);

		// extract anomeric position
        int anomericposition = checkAnomericPosition(_backbone);
        ret.setAnomericPosition(anomericposition);

		// extract stereo
        SkeletonCodeToStereo sc2s = new SkeletonCodeToStereo();
        ret.setStereos(sc2s.start(_backbone));

		// extract ring position
        this.extractRingPosition(_backbone, ret);

		// extract modification
        ret.setModification(extractModification(ret, _backbone));

        // extract substituent
        this.extractSubstituent(_backbone, ret);

        return ret;
    }

    private ArrayList<GlyCoModification> extractModification (Monosaccharide _mono, Backbone _backbone) throws GlycanException {
        ArrayList<GlyCoModification> ret = new ArrayList<>();
        String skeletonCode = _backbone.getSkeletonCode();

        for (int i = 0; i < skeletonCode.length(); i++) {
            char carbon = skeletonCode.charAt(i);
            ModificationTemplate modT = ModificationTemplate.forCarbon(carbon);

            if (i != 0) {
                if (carbon == 'o') modT = ModificationTemplate.KETONE;
                if (carbon == 'O' || carbon == 'a') modT = ModificationTemplate.KETONE_U;
            }
            if (i == (_mono.getSuperClass().getSize() -1 ) && carbon == 'A') {
                modT = ModificationTemplate.URONICACID;
            }

            if (modT == null) continue;

            GlyCoModification mod = new GlyCoModification(modT,i + 1);

            ret.add(mod);

            if (i != 1 && i+1 != _mono.getSuperClass().getSize() &&
                    (modT.equals(ModificationTemplate.KETONE) || modT.equals(ModificationTemplate.KETONE_U))) {
                boolean haveMod = false;
                for (GlyCoModification gMod : ret) {
                    if (gMod.getPositionOne() == 1) haveMod = true;
                }
                if (!haveMod) {
                    mod = new GlyCoModification(modT,1);
                    ret.add(mod);
                }
            }
        }

        // extract deoxy from Modification
        for(WURCSEdge we : _backbone.getChildEdges()) {
            Modification mod = we.getModification();
            if (mod.isRing() || mod.isGlycosidic() || mod instanceof ModificationRepeat) continue;
            if (mod.getParentEdges().size() != 1 || !mod.getMAPCode().equals("*")) continue;

            // 2018/09/07 Masaaki
            // A MAP "*" means a deoxy, not unknown substituent.
            ModificationTemplate modT = ModificationTemplate.DEOXY;
            GlyCoModification gmod = new GlyCoModification(modT, 0);
            ret.add(gmod);
        }

        // sort modifications by position
        Collections.sort(ret, new GlyCoModificationComparater());

        return ret;
    }

    private Monosaccharide extractSubstituent (Backbone _backbone, Monosaccharide _mono) throws GlycanException, ConverterExchangeException, WURCSFormatException {
        ArrayList<Modification> tempMods = new ArrayList<>();
        for(WURCSEdge we : _backbone.getChildEdges()) {
            Modification mod = we.getModification();
            if (isRingPosition(mod, _mono) || mod.isGlycosidic() || mod instanceof ModificationRepeat) continue;
            if (mod.getMAPCode().equals("*")) continue;

            // extract simple substituent
            if (mod.getParentEdges().size() == 1) {
                _mono = appendSubstituent(_mono, ModificationToSubstituent(_backbone, mod));
            }

            // extract cross-linked substituent and an-hydro
            if (mod.getParentEdges().size() == 2 /*&& !mod.getMAPCode().equals("")*/) {
                if (tempMods.contains(mod)) continue;
                _mono = appendSubstituent(_mono, ModificationToCrossLinkedSubstituent(mod));
                tempMods.add(mod);
            }

/*
            if (mod.getParentEdges().size() == 2 && mod.getMAPCode().equals("")) {
                if (tempMods.contains(mod)) continue;
                _mono = appendSubstituent(_mono, ModificationToCrossLinkedSubstituent(mod));
                tempMods.add(mod);
            }
*/
            if (mod.getParentEdges().size() > 2) {
                // 2018/10/02 Masaaki
                // Multiple linkages on alternative modifications are allowed
                if ( ! (mod instanceof ModificationAlternative) )
                    throw new GlycanException(mod.getMAPCode() + " have more than two anchors.");
            }
        }

        return _mono;
    }

  private Substituent ModificationToSubstituent(Backbone _backbone, Modification _mod) throws ConverterExchangeException, GlycanException, WURCSFormatException {
        MAPAnalyzer mapAnalyze = new MAPAnalyzer();
        mapAnalyze.start(_mod.getMAPCode());

        BaseSubstituentTemplate bsubT = mapAnalyze.getSingleTemplate();

        Linkage lin = new Linkage();

        if (bsubT == null)
            throw new ConverterExchangeException("This substituent could not support: " + _mod.getMAPCode());
        if (bsubT.getIUPACnotation().equals(""))
            throw new ConverterExchangeException("This substituent could not support: " + _mod.getMAPCode());

        for(WURCSEdge we : _mod.getParentEdges()) {
            for(LinkagePosition lp : we.getLinkages()) {
                lin.addParentLinkage(lp.getBackbonePosition());
                lin.addChildLinkage(lp.getModificationPosition());
                lin.setProbabilityUpper(lp.getProbabilityUpper());
                lin.setProbabilityLower(lp.getProbabilityLower());
            }
        }

        Substituent ret = new Substituent(bsubT, lin);
        ret.setHeadAtom(mapAnalyze.getHeadAtom());

        if (_backbone instanceof BackboneUnknown_TBD) return ret;

        // When linkage type is H_LOSE, assign character of 'C' to head-atom
        if (lin.getParentLinkages().size() == 1 && !lin.getParentLinkages().contains(-1)) {
            int pos = lin.getParentLinkages().get(0);
            if (_backbone.getBackboneCarbons().get(pos-1).getDesctriptor().equals(CarbonDescriptor_TBD.SS3_CHIRAL_X_U) ||
                    _backbone.getBackboneCarbons().get(pos-1).getDesctriptor().equals(CarbonDescriptor_TBD.SS3_CHIRAL_R_U) ||
                    _backbone.getBackboneCarbons().get(pos-1).getDesctriptor().equals(CarbonDescriptor_TBD.SS3_CHIRAL_r_U) ||
                    _backbone.getBackboneCarbons().get(pos-1).getDesctriptor().equals(CarbonDescriptor_TBD.SS3_CHIRAL_S_U) ||
                    _backbone.getBackboneCarbons().get(pos-1).getDesctriptor().equals(CarbonDescriptor_TBD.SS3_CHIRAL_s_U)) {
                ret.setHeadAtom("C");
            }
        }

        return ret;
    }

    private Substituent ModificationToCrossLinkedSubstituent(Modification _mod) throws ConverterExchangeException, GlycanException, WURCSFormatException {
        MAPAnalyzer mapAnalyze = new MAPAnalyzer();
        mapAnalyze.start(_mod.getMAPCode().equals("") ? "*o" : _mod.getMAPCode());

        SubstituentInterface subT = mapAnalyze.getCrossTemplate();

        Linkage first = new Linkage();
        Linkage second = new Linkage();

        if (subT == null)
            throw new ConverterExchangeException("This substituent could not support: " + _mod.getMAPCode());
        if (subT.getIUPACnotation().equals(""))
            throw new ConverterExchangeException("This substituent could not support: " + _mod.getMAPCode());

        for(WURCSEdge we : _mod.getParentEdges()) {
            if((_mod.getParentEdges().size() - 1) != _mod.getParentEdges().indexOf(we)) {
                for(LinkagePosition lp : we.getLinkages()) {
                    first.addParentLinkage(lp.getBackbonePosition());
                    first.addChildLinkage(lp.getModificationPosition());
                    first.setProbabilityUpper(lp.getProbabilityUpper()); // 2018/09/18 Masaaki added
                    first.setProbabilityLower(lp.getProbabilityLower()); // 2018/09/18 Masaaki added
                }
            }else {
                for(LinkagePosition lp : we.getLinkages()) {
                    second.addParentLinkage(lp.getBackbonePosition());
                    second.addChildLinkage(lp.getModificationPosition());
                    second.setProbabilityUpper(lp.getProbabilityUpper()); // 2018/09/18 Masaaki added
                    second.setProbabilityLower(lp.getProbabilityLower()); // 2018/09/18 Masaaki added
                }
            }
        }

        Substituent ret = new Substituent(subT, first, second);
        ret.setHeadAtom(mapAnalyze.getHeadAtom());
        ret.setTailAtom(mapAnalyze.getTailAtom());

        return ret;
    }

    //TODO : 修正が必要
    private char checkAnomericState (String _skeletonCode, char _anomericstate) {
        if (_anomericstate == 'o') {
            if (_skeletonCode.indexOf("o") == 0 || _skeletonCode.indexOf("O") == 1) return 'o';
            if (_skeletonCode.indexOf("u") == 0 || _skeletonCode.indexOf("U") == 1) return '?';
        }
        return _anomericstate;
    }

    private Monosaccharide appendSubstituent (Node _node, Node _substituent) throws GlycanException {
        Edge first = new Edge();
        first.setSubstituent(_substituent);
        first.setParent(_node);
        _node.addChildEdge(first);
        _substituent.addParentEdge(first);

        //Define linkage position for substituent
        Linkage linkage = new Linkage();
        for (Integer pos : ((Substituent) _substituent).getFirstPosition().getChildLinkages()) {
            linkage.addChildLinkage(pos);
        }
        for (Integer pos : ((Substituent) _substituent).getFirstPosition().getParentLinkages()) {
            linkage.addParentLinkage(pos);
        }
        first.addGlycosidicLinkage(linkage);

        return (Monosaccharide) _node;
    }

    private int checkAnomericPosition (Backbone _backbone) {
        int anomericPosition = _backbone.getAnomericPosition();
        String skeletonCode = _backbone.getSkeletonCode();

        if (skeletonCode.indexOf("u") == 0 || skeletonCode.indexOf("U") == 1) {
            anomericPosition = -1;
        }

        return anomericPosition;
    }

    private Monosaccharide extractRingPosition (Backbone _backbone, Monosaccharide _mono) throws GlycanException {
        int anomericPos = _backbone.getAnomericPosition();
        ArrayList<Integer> ring = new ArrayList<>();
        int start = -1;

        for (WURCSEdge cwe : _backbone.getChildEdges()) {
            Modification mod = cwe.getModification();
            if (!mod.isRing()) continue;
            for (LinkagePosition lp : cwe.getLinkages()) {
                if (anomericPos == lp.getBackbonePosition()) {
                    start = lp.getBackbonePosition();
                } else {
                    ring.add(lp.getBackbonePosition());
                }
            }
        }

        if (ring.isEmpty()) {
            _mono.setRing(start, -1);
        } else {
            Collections.sort(ring);
            _mono.setRing(start, ring.get(0));
        }

        return _mono;
    }

    private boolean isRingPosition (Modification _mod, Monosaccharide _mono) {
        if (_mod.getParentEdges().size() != 2) return false;
        int start = _mod.getParentEdges().get(0).getLinkages().get(0).getBackbonePosition();
        int end = _mod.getParentEdges().get(1).getLinkages().get(0).getBackbonePosition();
        return (_mono.getRingStart() == start && _mono.getRingEnd() == end);
    }
}
