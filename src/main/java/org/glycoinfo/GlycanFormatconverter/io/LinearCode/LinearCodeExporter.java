package org.glycoinfo.GlycanFormatconverter.io.LinearCode;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.ExporterInterface;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoExporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExporterUtility;
import org.glycoinfo.GlycanFormatconverter.util.similarity.LinearCodeNodeSimilarity;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/10/04.
 */
public class LinearCodeExporter extends IUPACExporterUtility implements ExporterInterface {

    private final StringBuilder linearCode = new StringBuilder();
    private final LinearCodeNodeSimilarity lcSim = new LinearCodeNodeSimilarity();
    private final HashMap<Node, String> LCMap = new HashMap<>();

    public String getLinearCode () {
        return this.linearCode.toString();
    }

    public void start (GlyContainer _glyCo) throws GlyCoExporterException, GlycanException, ConverterExchangeException {
        for (Node node : _glyCo.getAllNodes()) {
            makeMonosaccharideNotation(node);
            makeLinkageNotation(node);
        }

        // for fragments of substituent
        for (GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
            for (Node node : und.getRootNodes()) {
                if (node instanceof Substituent) {
                    makeSubstituentNotation(und);
                }
            }
        }

        // append fragments anchor
        makeFragmentsAnchor(_glyCo);

        // sort core nodes
        this.linearCode.insert(0, makeSequence(lcSim.sortAllNode(_glyCo.getRootNodes().get(0))));

        // sort fragments
        String[] fragments = makeFragmentsSequence(_glyCo.getUndefinedUnit()).split("\\+");
        if (fragments.length > 0) {
            if (fragments[0].length() != 0) {
                this.linearCode.insert(0, fragments[0]);
            }
            if (fragments.length > 1) {
                this.linearCode.append(fragments[1]);
            }
        }
    }

    public String makeFragmentsSequence (ArrayList<GlycanUndefinedUnit> _fragments) throws GlycanException, GlyCoExporterException, ConverterExchangeException {
        StringBuilder ret = new StringBuilder();

        StringBuilder unknownLink = new StringBuilder();

        for(GlycanUndefinedUnit und : _fragments) {
            for(Node antennae : und.getRootNodes()) {
                ArrayList<Node> sortedFragments = lcSim.sortAllNode(antennae);
                String fragment = makeSequence(sortedFragments);

                if (fragment.contains("%")) {
                    ret.insert(0, fragment + "|");
                } else {
                    if (unknownLink.length() != 0) unknownLink.append(",");
                    fragment = fragment.replaceFirst("[ab?][\\d?]$","");
                    unknownLink.append(fragment);
                }
            }
        }

        if (unknownLink.length() != 0) {
            unknownLink.insert(0, "&");
            unknownLink.append("&");
        }

        ret.append("+");
        ret.append(unknownLink);
        return ret.toString();
    }

    @Override
    public String makeAcceptorPosition(Edge _edge) {
        return null;
    }

    @Override
    public String makeDonorPosition(Edge _edge) {
        return null;
    }

    @Override
    public boolean isFragmentsRoot(GlyContainer _glyco, Node _node) {
        return false;
    }

    public String makeSequence(ArrayList<Node> _nodes) throws GlyCoExporterException, GlycanException, ConverterExchangeException {
        int branch = 0;
        StringBuilder encode = new StringBuilder();

        for(Node skey : _nodes) {
            StringBuilder notation = new StringBuilder(LCMap.get(skey));
            if(lcSim.isSubBranch(skey)) {
                notation.append(")");
                branch++;
            }

            int numOfChildren = lcSim.countChildren(skey);
            if(numOfChildren == 0 && branch > 0) {
                notation.insert(0, "(");
                branch--;
            }
            encode.insert(0, notation);
        }

        return encode.toString();
    }

    public void makeMonosaccharideNotation(Node _node) throws GlyCoExporterException, ConverterExchangeException, GlycanException {
        LinearCodeNodeConverter lcConv = new LinearCodeNodeConverter();
        LCMap.put(_node, lcConv.makeLCNotation(_node));
    }

    public void makeLinkageNotation(Node _node) {

        for (Edge parentEdge : lcSim.sortParentSideEdges(_node.getParentEdges())) {
            if (parentEdge.isRepeat()) {
                // append a symbol of repeating for start node
                LCMap.put(_node, LCMap.get(_node) + "}");
            } else if (parentEdge.isCyclic()) {

            } else {
                for (Linkage lin : parentEdge.getGlycosidicLinkages()) {
                    LCMap.put(_node, LCMap.get(_node) + makeLinkagePosition(lin.getParentLinkages()));
                }
            }
        }

        // append a symbol of repeating for end node
        for (Edge childEdge : _node.getChildEdges()) {
            Substituent sub = (Substituent) childEdge.getSubstituent();
            if (sub != null && sub instanceof GlycanRepeatModification) {
                GlycanRepeatModification repMod = (GlycanRepeatModification) sub;
                LCMap.put(_node, "{" + makeRepeatingCount(repMod) + LCMap.get(_node));
            }
        }
    }

    public void makeFragmentsAnchor(GlyContainer _glyCo) throws GlycanException {
        for(GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {

            if (extractPosition(und.getRootNodes().get(0).getParentEdge()).equals("?")) {

            } else {
                int index = _glyCo.getUndefinedUnit().indexOf(und) + 1;
                for(Node antennae : und.getRootNodes()) {
                    String notation = LCMap.get(antennae);
                    LCMap.put(antennae, notation + "=" + index + "%");
                }

                for(Node parent : und.getParents()) {
                    String notation = LCMap.get(parent);
                    LCMap.put(parent, index + "%" + (index > 1 ? "/" : "") + notation);
                }
            }
        }
    }

    @Override
    public void makeLinkageNotationFragmentSide(Node _node) {

    }

    @Override
    public String makeSimpleLinkageNotation(ArrayList<Edge> _edges) {
        return null;
    }

    @Override
    public String makeComposition(GlyContainer _glyCo) {
        return null;
    }

    public void makeSubstituentNotation(GlycanUndefinedUnit _und) {
        Node sub = _und.getNodes().get(0);
        if(!(sub instanceof Substituent)) return;

        if (!LCMap.containsKey(sub)) {
            String subNotation = ((Substituent) sub).getSubstituent().getIUPACnotation();
            subNotation = this.extractPosition(_und.getConnection().getGlycosidicLinkages().get(0).getParentLinkages()) + subNotation;
            LCMap.put(sub, subNotation);
        }
    }
}
