package org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStacker;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.WURCSFramework.wurcs.graph.BackboneCarbon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2018/08/22.
 */
public class IUPACCondensedLinkageParser {

    private final HashMap<Node, String> nodeIndex;
    private final GlyContainer glyCo;
    private final IUPACStacker stacker;

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
        if (this.haveStartRepeat(notation)) {
            throw new GlycanException("Repeating unit can not parse in the IUPAC-Condensed importer.");
            //parseRepeating(_node, acceptor, notation);
        } else {
            parseSimpleLinkage(_node, acceptor, notation);
        }

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
            //group2 : cross-linked substituent
            //group3 : linkage position of modification for donor side
            //group4 : substituent notation
            //group5 : linkage position of modification for acceptor side
            //group6 :
            //group7 : linkage position of acceptor
            //group8 : anomeric state for acceptor side
            Matcher matPos = Pattern.compile("([\\d?])-(([\\d?])?([(A-Za-z)]+)([\\d?])?-)?(([\\d?/]+)([ab?])?)").matcher(unit);
            if (matPos.find()) {
                Edge edge = new Edge();
                Linkage linkage = new Linkage();

                //Add donor side position
                linkage.addChildLinkage(charToInt(matPos.group(1).charAt(0)));

                //Add acceptor side position
                for (String pos : matPos.group(7).split("/")) {
                    linkage.addParentLinkage(charToInt(pos.charAt(0)));
                }

                // define cross-linked substituent
                if (matPos.group(2) != null) {
                    SubstituentInterface subface = BaseCrossLinkedTemplate.forIUPACNotation(matPos.group(4));
                    Substituent bridge = new Substituent(subface, new Linkage(), new Linkage());

                    if (matPos.group(1) != null) {
                        bridge.getFirstPosition().addParentLinkage(charToInt(matPos.group(1).charAt(0)));
                    }
                    if (matPos.group(3) != null) {
                        bridge.getFirstPosition().addChildLinkage(charToInt(matPos.group(3).charAt(0)));
                    }
                    if (matPos.group(5) != null) {
                        bridge.getFirstPosition().addChildLinkage(charToInt(matPos.group(5).charAt(0)));
                    }
                    if (matPos.group(7) != null) {
                        bridge.getSecondPosition().addParentLinkage(charToInt(matPos.group(7).charAt(0)));
                    }

                    edge.setSubstituent(bridge);
                    bridge.addParentEdge(edge);
                }

                //Define linkage types
                edge.addGlycosidicLinkage(linkage);

                if (_acceptor != null) {
                    //Assign anomeric state for acceptor, only anomric-anomeric linkages
                    this.modifyStructureState(_acceptor, matPos.group(6));
                    if (!stacker.isFragment()) glyCo.addNode(_acceptor, edge, _donor);
                }
                if (_acceptor == null && matPos.group(7) != null) {
                    _donor.addParentEdge(edge);
                    edge.setChild(_donor);
                    GlycanUndefinedUnit und = glyCo.getUndefinedUnitWithIndex(_donor);
                    und.setConnection(edge);
                }
            }
        }
    }

    private void parseRepeating (Node _donor, Node _acceptor, String _linkage) {

    }

    private String extractLinkage (String _notation) {
        //String ret = _notation.replaceAll("\\(", "").replaceAll("\\)", "");
        String ret = "";
        Matcher matPos = Pattern.compile(".+\\(([ab?]?[\\d?]-(.+))\\).?").matcher(_notation);

        if (matPos.find()) return matPos.group(1);
        return ret;
    }

    private Integer charToInt (char _position) {
        if (_position == '?') return -1;
        return Integer.parseInt(String.valueOf(_position));
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

    //GlcN(a1-4)L-IdoA(a1-4)[4)GlcN(a1-4)L-IdoA(a1-]n:a1-4)GlcN(a1-

    private String extractLinkageNotation (String _notation) {
        Matcher matLin = Pattern.compile("\\(([ab?].+)\\)?").matcher(_notation);
        if (matLin.find()) {
            return matLin.group(1);
        }
        return "";
    }

    private boolean haveStartRepeat (String _notation) {
        for (String item : this.extractLinkageNotation(_notation).split(":")) {
            if (item.matches("[ab?][\\d?]-][\\dn-]+")) {
                return true;
            }
        }
        return false;
    }

    private Node modifyStructureState (Node _acceptor, String _linkage) throws GlycanException {
        if (_acceptor instanceof Substituent) return _acceptor;
        if (_linkage.length() != 2) return _acceptor;

        Monosaccharide acceptor = (Monosaccharide) _acceptor;

        //assign anomeric state
        if (_linkage.charAt(1) == 'a') {
            acceptor.setAnomer(AnomericStateDescriptor.ALPHA);
        } else if (_linkage.charAt(1) == 'b') {
            acceptor.setAnomer(AnomericStateDescriptor.BETA);
        } else {
            acceptor.setAnomer(AnomericStateDescriptor.UNKNOWN_STATE);
        }

        //assign anomeric position
        if (_linkage.charAt(0) == '?') {
            acceptor.setAnomericPosition(Monosaccharide.UNKNOWN_RING);
        } else {
            acceptor.setAnomericPosition(Integer.parseInt(String.valueOf(_linkage.charAt(0))));
        }

        String notation = this.nodeIndex.get(_acceptor);
        Matcher matMono = Pattern.compile("([LD?]-?)?([468]?[dei])?([A-Z][a-z]{1,2}C?|KDN|[a-zA-Z]{6})([pf?])?(5[GA]c|N[AG]c|NA|A|N)?([A-Za-z]+)?").matcher(notation);
        String ringSize = "";
        if (matMono.find()) {
            if (matMono.group(4) != null) {
                //check for core-substituent
                if (matMono.group(4).equals("?") && matMono.group(6) == null) {
                    ringSize = matMono.group(4);
                }
                if (!matMono.group(4).equals("?")) {
                    ringSize = matMono.group(4);
                }
            }
        }

        if (ringSize.equals("")) {
            String stereo = acceptor.getStereos().getFirst();
            stereo = stereo.length() == 4 ? stereo.substring(1) : stereo;
            MonosaccharideIndex mi = MonosaccharideIndex.forTrivialNameWithIgnore(stereo);
            ringSize = mi.getRingSize();
        }

        if (ringSize.equals("p")) {
            if (acceptor.getAnomericPosition() == 1) {
                acceptor.setRing(1, 5);
            }
            if (acceptor.getAnomericPosition() == 2) {
                acceptor.setRing(2, 6);
            }
        } else if (ringSize.equals("f")) {
            if (acceptor.getAnomericPosition() == 1) {
                acceptor.setRing(1, 4);
            }
            if (acceptor.getAnomericPosition() == 2) {
                acceptor.setRing(2, 5);
            }
        } else {
            acceptor.setRing(acceptor.getAnomericPosition(), Monosaccharide.UNKNOWN_RING);
        }

        return _acceptor;
    }
}
