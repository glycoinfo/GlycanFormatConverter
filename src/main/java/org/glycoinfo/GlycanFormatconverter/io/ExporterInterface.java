package org.glycoinfo.GlycanFormatconverter.io;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanUndefinedUnit;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;

import java.util.ArrayList;

/**
 * Created by e15d5605 on 2017/10/12.
 */
public interface ExporterInterface {

    void makeMonosaccharideNotation(Node _node) throws Exception;
    void makeLinkageNotation (Node _node);
    void makeSubstituentNotation (GlycanUndefinedUnit _und) throws Exception;
    void makeFragmentsAnchor (GlyContainer _glyCo) throws Exception;
    String makeComposition (GlyContainer _glyCo);
    String makeSequence (ArrayList<Node> _nodes) throws Exception;
    String makeFragmentsSequence (ArrayList<GlycanUndefinedUnit> _fragments) throws Exception;
}
