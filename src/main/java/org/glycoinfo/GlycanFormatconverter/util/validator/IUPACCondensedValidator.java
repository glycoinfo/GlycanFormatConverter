package org.glycoinfo.GlycanFormatconverter.util.validator;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.SNFGNodeDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.ThreeLetterCodeConverter;

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

            //check for substituents
            this.checkForSubstituents(edge);

            //check for bridged substituent
            this.checkForBridgeSubstituent(edge);
        }

        for (Node node : _glyCo.getAllNodes()) {
            //check for unknown monosaccharide
            this.checkForMonosaccharide(node);

            //check for stereo(s)
            this.checkForGeneric(node);
            this.checkForStereos(node);

            //check for anomeric state
            this.checkForAnomericity(node);

            //check for isomer
            this.checkForIsomer(node);

            //check for ring size
            this.checkForRingSize(node);

            //check for modifications
            this.checkForModifications(node);
        }
    }

    @Override
    public void checkForRepeat(Edge _edge) throws GlycanException {
        if (_edge.getSubstituent() == null) return;
        Substituent repMod = (Substituent) _edge.getSubstituent();
        if (!(repMod instanceof GlycanRepeatModification)) return;

        throw new GlycanException("IUPAC-Condensed format can not handle repeating unit.");
    }

    @Override
    public void checkForUnknownLinkages(Edge _edge) throws GlycanException {

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
        if (!isUnknownAldose) {
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
