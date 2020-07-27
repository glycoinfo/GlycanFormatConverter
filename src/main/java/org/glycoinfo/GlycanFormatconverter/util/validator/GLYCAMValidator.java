package org.glycoinfo.GlycanFormatconverter.util.validator;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.GLYCAMSubstituent;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.ThreeLetterCodeConverter;

public class GLYCAMValidator implements TextValidator {

    public void validateGLYCAM (GlyContainer _glyCo) throws GlycanException {
        if (_glyCo.isComposition()) {
            throw new GlycanException("GLYCAM format can not support monosaccharide compositions.");
        }
        if (!_glyCo.getUndefinedUnit().isEmpty()) {
            throw new GlycanException("GLYCAM format can not support glycan fragments.");
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

    public void checkForRepeat(Edge _edge) throws GlycanException {
        if (_edge.getSubstituent() == null) return;
        Substituent repMod = (Substituent) _edge.getSubstituent();
        if (!(repMod instanceof GlycanRepeatModification)) return;

        throw new GlycanException("GLYCAM format can not support repeating unit.");
    }

    public void checkForUnknownLinkages(Edge _edge) throws GlycanException {
        for (Linkage linkage : _edge.getGlycosidicLinkages()) {
            if (linkage.getParentLinkages().contains(-1)) {
                throw new GlycanException("GLYCAM format can not support unknown linkage positions.");
            }
            if (linkage.getChildLinkages().contains(-1)) {
                throw new GlycanException("GLYCAM format can not support unknown linkage positions.");
            }
        }
    }

    public void checkForProbability(Edge _edge) throws GlycanException {
        for (Linkage linkage : _edge.getGlycosidicLinkages()) {
            if (linkage.getChildProbabilityLower() != 1.0D) {
                throw new GlycanException("GLYCAM format can not support probability annotation.");
            }
            if (linkage.getChildProbabilityUpper() != 1.0D) {
                throw new GlycanException("GLYCAM format can not support probability annotation.");
            }
        }
    }

    public void checkForBridgeSubstituent(Edge _edge) throws GlycanException {
        if (_edge.getSubstituent() == null) return;
        Substituent bridge = (Substituent) _edge.getSubstituent();
        if (bridge.getSubstituent() instanceof BaseCrossLinkedTemplate) {
            throw new GlycanException("GLYCAM format can not support cross-linked substituent.");
        }
    }

    public void checkForMonosaccharide(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;
        if (mono.getSuperClass().equals(SuperClass.SUG)) {
            throw new GlycanException("GLYCAM format can not handle \"Sugar\".");
        }
    }

    public void checkForGeneric(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;
        if (mono.getStereos().isEmpty()) {
            throw new GlycanException("GLYCAM format can not handle generic type monosaccharide.");
        }
    }

    public void checkForStereos(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;
        if (this.hasTrivialName(_node)) return;

        if (mono.getStereos().size() > 1) {
            throw new GlycanException("GLYCAM format can not handle monosaccharides with multiple stem types.");
        }
    }

    public void checkForAnomericity(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;
        if (mono.getAnomer().equals(AnomericStateDescriptor.OPEN)) {
            throw new GlycanException("GLYCAM format can not handle anomericity other than α or β");
        }
        if (mono.getAnomer().equals(AnomericStateDescriptor.UNKNOWN_STATE)) {
            throw new GlycanException("GLYCAM format can not handle anomericity other than α or β");
        }
        if (mono.getAnomer().equals(AnomericStateDescriptor.UNKNOWN)) {
            throw new GlycanException("GLYCAM format can not handle anomericity other than α or β");
        }
    }

    public void checkForIsomer(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;
        for (String stereo : mono.getStereos()) {
            if (!stereo.startsWith("d") && !stereo.startsWith("l")) {
                throw new GlycanException("GLYCAM format can not handle isomer other than D or L.");
            }
        }
    }

    public void checkForRingSize(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;
        if (mono.getRingStart() == -1 || mono.getRingStart() == 0) {
            throw new GlycanException("GLYCAM format can not handle ring size other than p or f.");
        }
        if (mono.getRingEnd() == -1 || mono.getRingEnd() == 0) {
            throw new GlycanException("GLYCAM format can not handle ring size other than p or f.");
        }
    }

    public void checkForSubstituents(Edge _edge) throws GlycanException {
        if (_edge.getSubstituent() == null) return;
        Substituent sub = (Substituent) _edge.getSubstituent();
        if (sub instanceof GlycanRepeatModification) return;
        if (sub.getSubstituent() instanceof BaseCrossLinkedTemplate) return;

        if (this.hasTrivialName(_edge.getParent())) return;

        //check for linkage position
        if (_edge.getGlycosidicLinkages().size() > 1) {
            throw new GlycanException("GLYCAM format can not handle cyclic substituent");
        }
        for (Linkage linkage : _edge.getGlycosidicLinkages()) {
            if (linkage.getParentLinkages().size() > 1) {
                throw new GlycanException("GLYCAM format can not handle fuzzy linkage.");
            }
            if (linkage.getParentLinkages().contains(-1)) {
                throw new GlycanException("GLYCAM format can not handle unknown linkage.");
            }
        }

        GLYCAMSubstituent enumGLYCAMsub = GLYCAMSubstituent.forNotation(sub.getNameWithIUPAC());
        if (enumGLYCAMsub == null) {
            throw new GlycanException("GLYCAM format can not handle this substituent : " + sub.getSubstituent());
        }
    }

    public void checkForModifications(Node _node) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) _node;

        if (hasTrivialName(_node)) return;

        for (GlyCoModification gMod : mono.getModifications()) {
            GLYCAMSubstituent enumGLYCAMsub = GLYCAMSubstituent.forNotation(String.valueOf(gMod.getModificationTemplate().getCarbon()));
            if (mono.getSuperClass().getSize() == gMod.getPositionOne()) continue;

            if (enumGLYCAMsub == null) {
                throw new GlycanException("GLYCAM format can not handle this modification : " + gMod.getModificationTemplate().getCarbon());
            }
        }
    }

    public boolean hasTrivialName(Node _node) throws GlycanException {
        //Check if this modification in involved in the definition of trivial name
        ThreeLetterCodeConverter threeConv = new ThreeLetterCodeConverter();
        threeConv.start(_node.copy());

        return (!threeConv.getThreeLetterCode().equals(""));
    }
}
