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

        // check for carbon descriptor
        this.checkForCarbonDescriptor(_backbone);

		// set superclass
        if (_backbone.hasUnknownLength()) {
            ret.setSuperClass(SuperClass.SUG);
        } else {
            ret.setSuperClass(SuperClass.forSize(_backbone.getLength()));
        }

		// extract anomeric state
        AnomericStateDescriptor enumAnom = this.parseAnomericState(_backbone);
        ret.setAnomer(enumAnom);

		// extract anomeric position
        int anomericposition = _backbone.getAnomericPosition();
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

            if (carbon == 'o') {
                if (i == 0 || i == skeletonCode.length() - 1) {
                    modT = ModificationTemplate.ALDEHYDE;
                } else {
                    modT = ModificationTemplate.KETONE;
                }
            }

            if (i != 0) {
                //20210810, changed
                if (carbon == 'O') modT = ModificationTemplate.ULOSONIC;
                if (carbon == 'a') modT = ModificationTemplate.ULOSONIC;
                if (carbon == 'U') modT = ModificationTemplate.KETONE_U;
                // end of change
            }
            if (i == (_mono.getSuperClass().getSize() -1 ) && carbon == 'A') {
                modT = ModificationTemplate.URONICACID;
            }

            if (modT == null) continue;

            GlyCoModification mod = new GlyCoModification(modT,i + 1);

            ret.add(mod);

            if (modT.equals(ModificationTemplate.KETONE) || modT.equals(ModificationTemplate.KETONE_U)) {
                if (i + 1 == _mono.getSuperClass().getSize()) continue;
                boolean haveMod = false;
                for (GlyCoModification gMod : ret) {
                    if (gMod.getPositionOne() == 1) {
                        haveMod = true;
                        break;
                    }
                }
                if (!haveMod) {
                    mod = new GlyCoModification(modT,1);
                    ret.add(mod);
                }
            }
            /*
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
             */
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
        ret.sort(new GlyCoModificationComparater());

        return ret;
    }

    private void extractSubstituent (Backbone _backbone, Monosaccharide _mono) throws GlycanException, ConverterExchangeException, WURCSFormatException {
        ArrayList<Modification> tempMods = new ArrayList<>();
        for(WURCSEdge we : _backbone.getChildEdges()) {
            Modification mod = we.getModification();
            if (mod.isRing()) continue;
            if (mod.isGlycosidic()) continue;
            if (mod instanceof ModificationRepeat) continue;
            if (mod.getMAPCode().equals("*")) continue;

            // extract simple substituent
            if (mod.getParentEdges().size() == 1) {
                _mono = appendSubstituent(_mono, ModificationToSubstituent(_backbone, mod));
            }

            // extract cyclic substituent and anhydro
            if (mod.getParentEdges().size() == 2) {
                if (tempMods.contains(mod)) continue;
                _mono = appendSubstituent(_mono, ModificationToCyclicSubstituent(mod));
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

    }

  private Substituent ModificationToSubstituent(Backbone _backbone, Modification _mod) throws ConverterExchangeException, WURCSFormatException {
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

        //if (_backbone instanceof UnknownBackbone) ret;
        if (_backbone.hasUnknownLength()) return ret;

        // When linkage type is H_LOSE, assign character of 'C' to head-atom
        if (!ret.getHeadAtom().equals("O") && (lin.getParentLinkages().size() == 1 && !lin.getParentLinkages().contains(-1))) {
            int pos = lin.getParentLinkages().get(0);
            if (_backbone.getBackboneCarbons().get(pos-1).getDescriptor().equals(CarbonDescriptor.SS3_CHIRAL_X_U) ||
                    _backbone.getBackboneCarbons().get(pos-1).getDescriptor().equals(CarbonDescriptor.SS3_CHIRAL_R_U) ||
                    _backbone.getBackboneCarbons().get(pos-1).getDescriptor().equals(CarbonDescriptor.SS3_CHIRAL_r_U) ||
                    _backbone.getBackboneCarbons().get(pos-1).getDescriptor().equals(CarbonDescriptor.SS3_CHIRAL_S_U) ||
                    _backbone.getBackboneCarbons().get(pos-1).getDescriptor().equals(CarbonDescriptor.SS3_CHIRAL_s_U)) {
                ret.setHeadAtom("C");
            }
        }

        return ret;
    }

    private Substituent ModificationToCyclicSubstituent (Modification _mod) throws ConverterExchangeException, WURCSFormatException {
        MAPAnalyzer mapAnalyze = new MAPAnalyzer();
        mapAnalyze.start(_mod.getMAPCode().equals("") ? "*O*" : _mod.getMAPCode());

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

    private AnomericStateDescriptor parseAnomericState (Backbone _backbone) {
        String skeletonCode = _backbone.getSkeletonCode();
        char anomericState = _backbone.getAnomericSymbol();

        if (_backbone.getAnomericPosition() == 0) {
            if (skeletonCode.indexOf("o") == 0 || skeletonCode.indexOf("O") == 1) return AnomericStateDescriptor.OPEN;
            if (skeletonCode.indexOf("u") == 0 || skeletonCode.indexOf("U") == 1) return AnomericStateDescriptor.OPEN;
            if (skeletonCode.indexOf("h") == 0) return AnomericStateDescriptor.OPEN;
        }

        AnomericStateDescriptor anomDesc = AnomericStateDescriptor.forAnomericState(anomericState);
        if (anomDesc == null) return AnomericStateDescriptor.UNKNOWN_STATE;
        else return anomDesc;
    }

    private Monosaccharide appendSubstituent (Node _node, Node _substituent) throws GlycanException {
        Edge subEdge = new Edge();
        subEdge.setSubstituent(_substituent);
        subEdge.setParent(_node);
        _node.addChildEdge(subEdge);
        _substituent.addParentEdge(subEdge);

        //Define linkage position for substituent
        //parse 1st linkage
        Substituent sub = (Substituent) _substituent;
        if (sub.getFirstPosition() != null) {
            Linkage linkage = new Linkage();
            linkage.setChildLinkages(sub.getFirstPosition().getChildLinkages());
            linkage.setParentLinkages(sub.getFirstPosition().getParentLinkages());
            subEdge.addGlycosidicLinkage(linkage);
        }

        //parse 2nd linkage
        if (sub.getSecondPosition() != null) {
            Linkage linkage = new Linkage();
            linkage.setChildLinkages(sub.getSecondPosition().getChildLinkages());
            linkage.setParentLinkages(sub.getSecondPosition().getParentLinkages());
            subEdge.addGlycosidicLinkage(linkage);
        }
        /*
        Linkage linkage = new Linkage();
        for (Integer pos : ((Substituent) _substituent).getFirstPosition().getChildLinkages()) {
            linkage.addChildLinkage(pos);
        }
        for (Integer pos : ((Substituent) _substituent).getFirstPosition().getParentLinkages()) {
            linkage.addParentLinkage(pos);
        }
        first.addGlycosidicLinkage(linkage);
         */

        return (Monosaccharide) _node;
    }

    private void extractRingPosition (Backbone _backbone, Monosaccharide _mono) throws GlycanException {
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
            if (_mono.getAnomer().equals(AnomericStateDescriptor.OPEN)) {
                _mono.setRing(0, 0);
            } else {
                if (!_backbone.hasUnknownLength()) {
                    throw new GlycanException("GlycanFormatConverter can not handle a monosaccharide having anomer without ring position.");
                }
            }
        } else {
            Collections.sort(ring);
            int end = ring.get(0);
            if (ring.size() > 1) {
                throw new GlycanException("GlycanFormatConverter can not handle multiple ring structure.");
            }
            if (start == 1 && (end != 4 && end != 5 && end != -1)) {
                throw new GlycanException("GlycanFormatConverter can not handle this ring end : " + end);
            }
            if (start == 2 && (end != 5 && end != 6 && end != -1)) {
                throw new GlycanException("GlycanFormatConverter can not handle this ring end : " + end);
            }
            _mono.setRing(start, ring.get(0));
        }

    }

    private void checkForCarbonDescriptor (Backbone _backbone) throws WURCSFormatException {
        for (BackboneCarbon bc : _backbone.getBackboneCarbons()) {
            if (bc.getDescriptor().equals(CarbonDescriptor.SZ3_ACETAL_L)) {
                throw new WURCSFormatException("GlycanFormatConverter can not handle acetal : " + bc.getDescriptor().getChar());
            }
            if (bc.getDescriptor().equals(CarbonDescriptor.SZ3_ACETAL_U)) {
                throw new WURCSFormatException("GlycanFormatConverter can not handle acetal : " + bc.getDescriptor().getChar());
            }
            if (bc.getDescriptor().equals(CarbonDescriptor.SS3_ACETAL)) {
                throw new WURCSFormatException("GlycanFormatConverter can not handle acetal : " + bc.getDescriptor().getChar());
            }
        }
    }
}
