package org.glycoinfo.GlycanFormatconverter.util.validator;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;

import java.util.ArrayList;
import java.util.LinkedList;

public class IUPACExtendedValidator implements TextValidator {

    public void validatedExtended (GlyContainer _glyCo) throws GlycanException {
        if (_glyCo.isComposition()) {
            //throw new GlycanException("IUPAC-Condensed format can not support monosaccharide compositions.");
        }
        if (!_glyCo.getUndefinedUnit().isEmpty()) {
            //throw new GlycanException("IUPAC-Condensed format can not support glycan fragments.");
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
            throw new GlycanException("IUPAC-Extended format can not handle multiple root glycan.");
        }
    }

    @Override
    public void checkForRepeat(Edge _edge) throws GlycanException {

    }

    @Override
    public void checkForUnknownLinkages(Edge _edge) throws GlycanException {

    }

    @Override
    public void checkForProbability(Edge _edge) throws GlycanException {

    }

    @Override
    public void checkForBridgeSubstituent(Edge _edge) throws GlycanException {

    }

    @Override
    public void checkForMonosaccharide(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;
        if (mono.getSuperClass().equals(SuperClass.SUG)) {
            throw new GlycanException ("IUPAC-Extended format can not handle unknown monosaccharide.");
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
                throw new GlycanException("IUPAC-Extended format can not handle ketose with unknown anomer state.");
            }
        }

        //check for unknown aldose
        boolean isUnknownAldose = false;
        for (GlyCoModification gMod : mono.getModifications()) {
            if (gMod.getPositionOne() == 1 &&
                    (gMod.getModificationTemplate().equals(ModificationTemplate.ALDEHYDE) ||
                            gMod.getModificationTemplate().equals(ModificationTemplate.HYDROXYL))) {
                isUnknownAldose = true;
            }
        }
        if (!isUnknownAldose) {
            throw new GlycanException("IUPAC-Extended format can not handle unknown aldose.");
        }
    }

    @Override
    public boolean hasTrivialName(Node _node) throws GlycanException {
        return false;
    }
}
