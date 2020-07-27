package org.glycoinfo.GlycanFormatconverter.util.validator;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;

public interface TextValidator {

    void checkForRepeat(Edge _edge) throws GlycanException;

    void checkForUnknownLinkages (Edge _edge) throws GlycanException;

    void checkForProbability (Edge _edge) throws GlycanException;

    void checkForBridgeSubstituent (Edge _edge) throws GlycanException;

    void checkForMonosaccharide (Node _node) throws GlycanException;

    void checkForGeneric (Node _node) throws GlycanException;

    void checkForStereos (Node _node) throws GlycanException;

    void checkForAnomericity (Node _node) throws GlycanException;

    void checkForIsomer (Node _node) throws GlycanException;

    void checkForRingSize (Node _node) throws GlycanException;

    void checkForSubstituents (Edge _edge) throws GlycanException;

    void checkForModifications (Node _node) throws GlycanException;

    boolean hasTrivialName (Node _node) throws GlycanException;
}
