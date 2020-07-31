package org.glycoinfo.GlycanFormatconverter.util.validator;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.SNFGNodeDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.ThreeLetterCodeConverter;

import java.util.ArrayList;

public class IUPACCondensedValidator implements TextValidator {

    public void validateCondensed (GlyContainer _glyCo) throws GlycanException {
        if (_glyCo.isComposition()) {
            throw new GlycanException("IUPAC-Condensed format can not support monosaccharide compositions.");
        }
        if (!_glyCo.getUndefinedUnit().isEmpty()) {
            throw new GlycanException("IUPAC-Condensed format can not support glycan fragments.");
        }

        for (Edge edge : _glyCo.getEdges()) {
            //check for repeat and cyclic
            this.checkForRepeat(edge);

            //check for unknown linkages and probability
            this.checkForUnknownLinkages(edge);
            this.checkForProbability(edge);
            this.checkForLinkagePositions(edge);

            //check for substituents
            this.checkForSubstituents(edge);

            //check for bridged substituent
            this.checkForBridgeSubstituent(edge);
        }

        //check for glycan having multiple root node
        if (!_glyCo.isComposition()) {
            this.checkForRoot(_glyCo.getAllNodes());
        }

        for (Node node : _glyCo.getAllNodes()) {
            //check for unknown monosaccharide
            this.checkForMonosaccharide(node);

            //check for stereo(s)
            this.checkForGeneric(node);
            this.checkForStereos(node);

            //check for anomeric state
            this.checkForAnomericity(node);

            //check for anomeric position
            this.checkForAnomericPosition(node);

            //check for anomeric position
            this.checkForAnomericPosition(node);

            //check for isomer
            this.checkForIsomer(node);

            //check for ring size
            this.checkForRingSize(node);

            //check for modifications
            this.checkForModifications(node);
        }
    }

    @Override
    public void checkForRoot(ArrayList<Node> _nodes) throws GlycanException {
        int count = 0;
        for (Node node : _nodes) {
            if (!node.getParentEdges().isEmpty()) continue;
            count++;
        }

        if (count > 1) {
            throw new GlycanException("IUPAC-Condensed format can not handle multiple root glycan.");
        }
    }

    @Override
    public void checkForRepeat(Edge _edge) throws GlycanException {
        if (_edge.getSubstituent() == null) return;
        Substituent repMod = (Substituent) _edge.getSubstituent();
        if (!(repMod instanceof GlycanRepeatModification)) return;

        if (((GlycanRepeatModification) repMod).getMaxRepeatCount() == 1 &&
        ((GlycanRepeatModification) repMod).getMinRepeatCount() == 1) {
            throw new GlycanException("IUPAC-Condensed format can not handle cyclic structure.");
        } else {
            throw new GlycanException("IUPAC-Condensed format can not handle repeating unit.");
        }
    }

    @Override
    public void checkForUnknownLinkages(Edge _edge) throws GlycanException {

    }

    @Override
    public void checkForLinkagePositions(Edge _edge) throws GlycanException {
        if (_edge.getChild() == null) return;

        Monosaccharide donor = (Monosaccharide) _edge.getChild();
        if (donor.getAnomericPosition() == Monosaccharide.OPEN_CHAIN &&
        donor.getAnomer().equals(AnomericStateDescriptor.OPEN)) return;

        for (Linkage lin : _edge.getGlycosidicLinkages()) {
            if (lin.getChildLinkages().contains(-1)) continue;
            if (!lin.getChildLinkages().contains(donor.getAnomericPosition())) {
                throw new GlycanException("IUPAC-Condensed format can not handle a glycosidic linkage other than anomeric position.");
            }
        }
    }

    @Override
    public void checkForProbability(Edge _edge) throws GlycanException {
        for (Linkage linkage : _edge.getGlycosidicLinkages()) {
            if (linkage.getChildProbabilityLower() != 1.0D) {
                throw new GlycanException("IUPAC-Condensed format can not handle probability annotation.");
            }
            if (linkage.getChildProbabilityUpper() != 1.0D) {
                throw new GlycanException("IUPAC-Condensed format can not handle probability annotation.");
            }
        }
    }

    @Override
    public void checkForBridgeSubstituent(Edge _edge) throws GlycanException {

    }

    @Override
    public void checkForMonosaccharide(Node _node) throws GlycanException {
        IUPACNotationConverter notationConv = new IUPACNotationConverter();
        notationConv.makeTrivialName(_node.copy());
        String code = notationConv.getThreeLetterCode();

        SNFGNodeDescriptor snfgDesc = SNFGNodeDescriptor.forAbbreviation(code);
        if (snfgDesc == null) {
            throw new GlycanException("IUPAC-Condensed format can not handle this monosaccharide : " + code);
        }
    }

    @Override
    public void checkForGeneric(Node _node) throws GlycanException {

    }

    @Override
    public void checkForStereos(Node _node) throws GlycanException {

    }

    @Override
    public void checkForAnomericity(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;
        if (mono.getAnomericPosition() == -1) return;

        if (mono.getAnomericPosition() == 3) {
            throw new GlycanException("IUPAC-Condensed format can not handle an anomeric position : " + mono.getAnomericPosition());
        }

        ThreeLetterCodeConverter threeConv = new ThreeLetterCodeConverter();
        threeConv.start(_node.copy());
        String trivialName = threeConv.getThreeLetterCode();
        if (trivialName.equals("")) return;

        MonosaccharideIndex mi = MonosaccharideIndex.forTrivialNameWithIgnore(trivialName);
        if (mi == null) return;

        if (mi.getAnomerciPosition() == 2 && mono.getAnomericPosition() == 1) {
            throw new GlycanException("The anomeric position of this monosaccharide differs from the stem type.");
        }
        if (mi.getAnomerciPosition() == 1 && mono.getAnomericPosition() == 2) {
            throw new GlycanException("The anomeric position of this monosaccharide differs from the stem type.");
        }
    }

    @Override
    public void checkForAnomericPosition(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;
        if (mono.getAnomericPosition() == -1) return;

        if (mono.getAnomericPosition() == 3) {
            throw new GlycanException("IUPAC-Condensed format can not handle an anomeric position : " + mono.getAnomericPosition());
        }

        ThreeLetterCodeConverter threeConv = new ThreeLetterCodeConverter();
        threeConv.start(_node.copy());
        String trivialName = threeConv.getThreeLetterCode();
        if (trivialName.equals("")) return;

        MonosaccharideIndex mi = MonosaccharideIndex.forTrivialNameWithIgnore(trivialName);
        if (mi == null) return;

        if (mi.getAnomerciPosition() == 2 && mono.getAnomericPosition() == 1) {
            throw new GlycanException("The anomeric position of this monosaccharide differs from the stem type.");
        }
        if (mi.getAnomerciPosition() == 1 && mono.getAnomericPosition() == 2) {
            throw new GlycanException("The anomeric position of this monosaccharide differs from the stem type.");
        }
    }

    @Override
    public void checkForIsomer(Node _node) throws GlycanException {

    }

    @Override
    public void checkForRingSize(Node _node) throws GlycanException {
    }

    @Override
    public void checkForSubstituents(Edge _edge) throws GlycanException {

    }

    @Override
    public void checkForModifications(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;

        if (!mono.getAnomer().equals(AnomericStateDescriptor.OPEN) &&
                mono.getAnomericPosition() != Monosaccharide.OPEN_CHAIN) return;

        //check for unknown keto-monosaccharide
        for (GlyCoModification gMod : mono.getModifications()) {
            if (gMod.getPositionOne() == 2 &&
                    gMod.getModificationTemplate().equals(ModificationTemplate.KETONE_U)) {
                throw new GlycanException("IUPAC-Condensed format can not handle ketose with unknown anomer state.");
            }
        }

        //check for unknown aldose
        boolean isUnknownAldose = false;
        for (GlyCoModification gMod : mono.getModifications()) {
            if (gMod.getPositionOne() == 1 &&
                    gMod.getModificationTemplate().equals(ModificationTemplate.ALDEHYDE)) {
                isUnknownAldose = true;
            }
        }
        if (isUnknownAldose) {
            throw new GlycanException("IUPAC-Condensed format can not handle unknown aldose.");
        }
    }

    @Override
    public boolean hasTrivialName(Node _node) throws GlycanException {
        //Check if this modification in involved in the definition of trivial name
        ThreeLetterCodeConverter threeConv = new ThreeLetterCodeConverter();
        threeConv.start(_node.copy());

        return (!threeConv.getThreeLetterCode().equals(""));
    }
}
