package org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStacker;
import org.glycoinfo.GlycanFormatconverter.util.GlyContainerOptimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by e15d5605 on 2017/07/31.
 */
public class IUPACCondensedImporter {

    public GlyContainer start (String _iupac) throws GlyCoImporterException, GlycanException {
        GlyContainer glyCo = new GlyContainer();
        LinkedHashMap<Node, String> nodeIndex = new LinkedHashMap<>();
        List<String> notations = new ArrayList<>();

        _iupac = _iupac.replaceAll("[\\xc2\\xa0]", "");
        _iupac = _iupac.replaceAll(" ", "");
        _iupac = _iupac.trim();

        //_iupac = _iupac.replaceAll("\\(", "");
        //_iupac = _iupac.replaceAll("\\)", "");

        //if (_iupac.contains("[")) {
        //    _iupac = this.replaceBlockBrakcets(_iupac);
        //}

        /**/
        for (String unit : _iupac.split("\\$,")) {
            if (unit.matches(".+=[\\d?]")) unit += "$,";
            notations.add(unit);
        }
        Collections.reverse(notations);

		/**/
        for (String subst : notations) {
            IUPACStacker stacker = new IUPACStacker();

            //TODO: parse monosaccharide compositions

            stacker.setNotations(parseNotation(subst));

            if (subst.endsWith("$,")) stacker.setFragment();

			//generate monosaccharide
            for (String unit : stacker.getNotations()) {
                if (stacker.isComposition()) {
                    for (int i = stacker.getNumOfNode(); i != 0; i--) {
                        Node node = makeNode(unit);
                        nodeIndex.put(node, unit);
                        stacker.addNode(node);
                    }
                } else {
                    Node node = makeNode(unit);
                    nodeIndex.put(node, unit);
                    stacker.addNode(node);
                }
            }

			// define family in each nodes
            parseChildren(stacker, nodeIndex);

			// define linkages
            IUPACCondensedLinkageParser icLP = new IUPACCondensedLinkageParser(glyCo, nodeIndex, stacker);
            glyCo = icLP.start();
        }

        //
        GlyContainerOptimizer gcOpt = new GlyContainerOptimizer();
        return gcOpt.start(glyCo);
    }

    private Node makeNode (String _notation) throws GlyCoImporterException, GlycanException {
        IUPACCondensedNotationParser iupacNP = new IUPACCondensedNotationParser();
        return iupacNP.parseMonosaccharide(_notation);
    }

    private ArrayList<String> parseNotation (String _iupac) {
        ArrayList<String> ret = new ArrayList<>();
        boolean isLinkage = false;
        boolean isSub = false;
        StringBuilder node = new StringBuilder();

        for (int i = 0; i < _iupac.length(); i++) {
            char item = _iupac.charAt(i);
            node.append(item);

            //End
            if (i == _iupac.length() - 1) {
                ret.add(node.toString());
                break;
            }

            //Parse child of substituent
            if (!isLinkage && node.length() == 0 && String.valueOf(item).matches("[\\d?]")) {
                isSub = true;
            }

            //Add substituent to list
            if (isSub && item == ')') {
                ret.add(node.toString());
                node = new StringBuilder();
                isSub = false;
                isLinkage = false;

                continue;
            }

            //parse anomeric state
            char linkage = _iupac.charAt(i+1);
            if ((item == 'a' || item == 'b' || item == '?') &&
                    String.valueOf(linkage).matches("[\\d?-]")) {
                isLinkage = true;
            }

            if (isLinkage && _iupac.charAt(i) == ')') {
                if (_iupac.charAt(i+1) == '=') {
                    isLinkage = false;
                    continue;
                }
                ret.add(node.toString());
                isLinkage = false;
                node = new StringBuilder();
            }

            //add monosaccharide notation to list.
            char nextItem = _iupac.charAt(i+1);
            if (isLinkage && String.valueOf(nextItem).matches("[A-Z(]")) {
                ret.add(node.toString());
                isLinkage = false;
                node = new StringBuilder();
            }
        }

        return ret;
    }

    private String replaceBlockBrakcets (String _iupac) {
        StringBuilder ret = new StringBuilder(_iupac);

        for (int i = 0; i < _iupac.length(); i++) {
            char item = _iupac.charAt(i);
            if (item == '[') {
                if (_iupac.charAt(i+2) != ')') ret.replace(i, i+1, "(");
            }

            if (item == ']') {
                if (_iupac.charAt(i-1) != '-') ret.replace(i, i+1, ")");
            }
        }

        return ret.toString();
    }

    private void parseChildren(IUPACStacker _stacker, LinkedHashMap<Node, String> _index) {
        ArrayList<Node> nodes = new ArrayList<>(_stacker.getNodes());

        Collections.reverse(nodes);

        for (Node node : nodes) {
            if (haveChild(node, _index)) {
                int childIndex = nodes.indexOf(node) + 1;
                Node child = nodes.get(childIndex);
                _stacker.addFamily(child, node);
            }

            if (isStartOfBranch(node, _index)) {
                int childIndex = nodes.indexOf(node) + 1;
                Node child = nodes.get(childIndex);
                _stacker.addFamily(child, node);

                for (Node cNode : pickChildren(nodes, node, _index)) {
                    _stacker.addFamily(cNode, node);
                }
            }
        }
    }

    private ArrayList<Node> pickChildren (ArrayList<Node> _nodes, Node _branch, LinkedHashMap<Node, String> _index) {
        ArrayList<Node> children = new ArrayList<>();
        int count = 0;
        boolean isChild = false;

        if (isStartOfBranch(_branch, _index)) count = -1;

        for (Node node : _nodes.subList(_nodes.indexOf(_branch) + 1, _nodes.size())) {
            if (isChild) {
                children.add(node);
            }

            if (count == 0 && !isBisecting(node, _index)) {
                if (isStartOfBranch(node, _index)) break;
                if (isEndOfBranch(node, _index)) break;
                if (haveChild(node, _index)) break;
            }

            if (isStartOfBranch(node, _index)) count--;
            if (isEndOfBranch(node, _index)) count++;
            if (isBisecting(node, _index)) count--;

            if (count == 0) {
                if (isBisecting(node, _index)) isChild = true;
                if (isEndOfBranch(node, _index)) isChild = true;
                continue;
            }

            isChild = false;
        }

        return children;
    }

    private boolean isBisecting (Node _node, LinkedHashMap<Node, String> _index) {
        int currentIndex = new ArrayList<>(_index.keySet()).indexOf(_node);

        if (currentIndex == 0)
            return false;
        if (!_index.get(_node).endsWith("]"))
            return false;

        Node next = new ArrayList<>(_index.keySet()).get(currentIndex + 1);
        return (_index.get(next).startsWith("["));
    }

    private boolean haveChild (Node _node, LinkedHashMap<Node, String> _index) {
        int currentIndex = new ArrayList<>(_index.keySet()).indexOf(_node);

        if (currentIndex == 0) //is leaf end
            return false;
        if (_index.get(_node).matches(".+(=[\\d?]\\$,)$")) //is root of fragment
            return false;
        return !_index.get(_node).startsWith("[");
    }

    private boolean isStartOfBranch (Node _node, LinkedHashMap<Node, String> _index) {
        int currentIndex = new ArrayList<>(_index.keySet()).indexOf(_node);

        if (currentIndex == 0)
            return false;
        if (_index.get(_node).startsWith("["))
            return false;

        return _index.get(_node).startsWith("]");
    }

    private boolean isEndOfBranch (Node _node, LinkedHashMap<Node, String> _index) {
        return (_index.get(_node).startsWith("["));
    }
}
