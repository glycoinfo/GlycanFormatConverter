package org.glycoinfo.GlycanFormatconverter.io;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;

import java.util.ArrayList;

/**
 * Created by e15d5605 on 2017/10/12.
 */
public interface ExporterInterface {

    void start (GlyContainer _glyCo) throws Exception;
    void makeMonosaccharideNotation(Node _node) throws Exception;
    void makeLinkageNotation (Node _node);
    void makeSubstituentNotation (GlycanUndefinedUnit _und) throws Exception;
    void makeFragmentsAnchor (GlyContainer _glyCo) throws Exception;
    void makeLinkageNotationFragmentSide (Node _node);
    String makeSimpleLinkageNotation (ArrayList<Edge> _edges);
    String makeComposition (GlyContainer _glyCo);
    String makeSequence (ArrayList<Node> _nodes) throws Exception;
    String makeFragmentsSequence (ArrayList<GlycanUndefinedUnit> _fragments) throws Exception;
    String makeAcceptorPosition (Edge _edge);
    String makeDonorPosition (Edge _edge);
    boolean isFragmentsRoot (GlyContainer _glyco, Node _node) throws Exception;
}
