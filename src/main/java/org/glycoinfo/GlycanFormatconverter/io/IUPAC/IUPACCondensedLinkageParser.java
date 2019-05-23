package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.apache.xerces.impl.xpath.regex.Match;
import org.glycoinfo.GlycanFormatconverter.Glycan.*;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2018/08/22.
 */
public class IUPACCondensedLinkageParser {

    private HashMap<Node, String> nodeIndex;
    private GlyContainer glyCo;
    private IUPACStacker stacker;

    public IUPACCondensedLinkageParser (GlyContainer _glyCo, HashMap<Node, String> _nodeIndex, IUPACStacker _stacker) {
        this.nodeIndex = _nodeIndex;
        this.glyCo = _glyCo;
        this.stacker = _stacker;
    }

    public GlyContainer start () throws GlycanException {
        GlycanUndefinedUnit undef = new GlycanUndefinedUnit();

        for (Node node : stacker.getNodes()) {
            //Make glycosidic linkage with simple bond
            if (node instanceof Substituent) {
                parseSubstituent(node);
                continue;
            } else {
                parseLinkage(node);
            }

            String notation = nodeIndex.get(node);

            //Make glycosidic linkage with fragments
            //if(isRootOfFramgnets(notation) || stacker.isComposition()) {
            //    undef = this.makeUndefinedUnit(node, notation);
            //}

            //Make compositions
            //if (stacker.isComposition()) {
            //    undef = this.makeUndefinedUnit(node, notation);
            //    glyCo.addGlycanUndefinedUnit(undef);
            //    continue;
            //}

            //Define single monosaccharide
            if (!glyCo.containsNode(node)) {
                if (undef != null && !undef.containsNode(node)) glyCo.addNode(node);
                if (undef == null) glyCo.addNode(node);
            }
        }

        return glyCo;
    }

    private void parseSubstituent (Node _node) throws GlycanException {
        Edge edge = new Edge();
        Node acceptor = stacker.getParent(_node);

        edge.setSubstituent(_node);
        edge.setParent(acceptor);
        acceptor.addChildEdge(edge);
        _node.addParentEdge(edge);
        edge.addGlycosidicLinkage(((Substituent) _node).getFirstPosition());

        //glyCo.addNodeWithSubstituent(acceptor, edge, (Substituent) _node);
    }

    private void parseLinkage (Node _node) throws GlycanException {
        String notation = nodeIndex.get(_node);
        Node acceptor = stacker.getParent(_node);

        //TODO : parse repeating (cyclic) position
        //parse repeating information

        parseSimpleLinkage(_node, acceptor, notation);

        //parse cyclic information
    }

    private void parseSimpleLinkage (Node _donor, Node _acceptor, String _notation) throws GlycanException {
        //extract linkage
        String linkNotation = extractLinkage(_notation);

        if (linkNotation.equals("") || linkNotation.equals("-")) return;

        for (String unit : linkNotation.split(":")) {
            //group1 : anomeric position
            //group2 : linkage position of acceptor
            Matcher matPos = Pattern.compile("([\\d?])-(.+)").matcher(unit);
            if (matPos.find()) {

                Edge edge = new Edge();
                Linkage linkage = new Linkage();

                //Add donor side position
                linkage.addChildLinkage(charToInt(matPos.group(1)));

                //Add acceptor side position
                for (String pos : matPos.group(2).split("/")) {
                    linkage.addParentLinkage(charToInt(pos));
                }

                //Define linkage types

                edge.addGlycosidicLinkage(linkage);

                glyCo.addNode(_acceptor, edge, _donor);
            }
        }
    }

    private String extractLinkage (String _notation) {
        String ret = "";
        ret = _notation.replaceAll("\\(", "").replaceAll("\\)", "");

        Matcher matPos = Pattern.compile(".+[ab?](.+)").matcher(ret);

        if (matPos.find()) {
            return matPos.group(1);
        }

        return "";
    }

    private int charToInt (String _position) {
        if (_position.equals("?")) return -1;
        return Integer.parseInt(_position);
    }

}
