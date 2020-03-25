package org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStacker;

import java.util.ArrayList;
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
        //define sub-graph, compositions
        for (Node node : stacker.getNodes()) {
            String notation = nodeIndex.get(node);
            if (!isRootOfFramgnets(notation) && !stacker.isComposition()) continue;
            glyCo.addGlycanUndefinedUnit(makeUndefinedUnit(node, notation));
        }

        //Make glycosidic linkage with simple bond
        for (Node node : stacker.getNodes()) {
            if (node instanceof Substituent) {
                parseSubstituent(node);
            } else {
                parseLinkage(node);
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

        //parse repeating information
        //if () {
        //    parseRepeating(_node, acceptor, "");
        //} else {
            parseSimpleLinkage(_node, acceptor, notation);
        //}

        //parse cyclic information
        /*
        if (isEndCyclic(notation)) {
            parseCyclic(_node, getIndex(nodeIndex.size() - 1));
        }
         */

        if (acceptor == null && !glyCo.containsNode(_node)) {
            glyCo.addNode(_node);
        }
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

                if (_acceptor != null) {
                    if (!stacker.isFragment()) glyCo.addNode(_acceptor, edge, _donor);
                }
                if (_acceptor == null && matPos.group(2) != null) {
                    _donor.addParentEdge(edge);
                    edge.setChild(_donor);
                    GlycanUndefinedUnit und = glyCo.getUndefinedUnitWithIndex(_donor);
                    und.setConnection(edge);
                }
            }
        }
    }

    private String extractLinkage (String _notation) {
        //String ret = _notation.replaceAll("\\(", "").replaceAll("\\)", "");
        String ret = "";
        Matcher matPos = Pattern.compile(".+\\([ab?](.+)\\).?").matcher(_notation);

        if (matPos.find()) return matPos.group(1);
        return ret;
    }

    private int charToInt (String _position) {
        if (_position.equals("?")) return -1;
        return Integer.parseInt(_position);
    }

    private boolean isRootOfFramgnets (String _notation) {
        if (_notation.lastIndexOf("$,") != -1) return true;
        else return _notation.lastIndexOf("$") == _notation.length() - 1;
    }

    private GlycanUndefinedUnit makeUndefinedUnit (Node _node, String _notation) throws GlycanException {
        GlycanUndefinedUnit ret = new GlycanUndefinedUnit();
        ret.addNode(_node);
        for(Node parent : parseFragmentParents(_notation)) {
            ret.addParentNode(parent);
        }

        return ret;
    }

    public ArrayList<Node> parseFragmentParents (String _fragment) {
        ArrayList<Node> ret = new ArrayList<>();
        String anchor = _fragment.substring(_fragment.indexOf("=") + 1, _fragment.length() - 1);

        for(Node node : nodeIndex.keySet()) {
            String notation = nodeIndex.get(node);
            if(notation.equals(_fragment)) continue;

            if (notation.contains("=") && notation.startsWith("?$")) {
                String target;

                // for composition
                if (notation.endsWith(",")) target = notation.substring(notation.indexOf("=")+1, notation.length()-1);
                else target = notation.substring(notation.indexOf("=")+1);

                if (anchor.equals(target)) ret.add(node);
            }
            if ((notation.contains("|") || notation.contains("$")) &&!notation.contains("=")) {
                // resolve multiple anchor
                String temp = "";
                for (int i = 0; i < notation.length(); i++) {
                    char unit = notation.charAt(i);

                    if (String.valueOf(unit).matches("\\d")) temp += unit;
                    if (unit == '$') {
                        temp += unit;
                        if (anchor.endsWith(temp)) ret.add(node);
                        temp = "";
                    }
                }
            }
        }

        return ret;
    }

}
