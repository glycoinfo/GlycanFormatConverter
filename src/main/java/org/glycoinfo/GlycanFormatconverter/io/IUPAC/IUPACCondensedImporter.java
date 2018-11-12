package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;

import java.util.*;

/**
 * Created by e15d5605 on 2017/07/31.
 */
public class IUPACCondensedImporter {

    private GlyContainer glyCo = new GlyContainer();

    public GlyContainer getGlyContainer () {
        return glyCo;
    }

    public void start (String _iupac) throws GlyCoImporterException, GlycanException {
        _iupac = _iupac.replaceAll("[\\xc2\\xa0]", "");
        _iupac = _iupac.replaceAll(" ", "");
        _iupac = _iupac.trim();

        //Linkage is represented by parentheses.
        if (_iupac.indexOf("[") != -1) {
            _iupac = _iupac.replaceAll("\\(", "").replaceAll("\\)", "");
            _iupac = _iupac.replaceAll("\\[", "(").replaceAll("]", ")");
        }

        LinkedHashMap<Node, String> nodeIndex = new LinkedHashMap<Node, String>();

        /**/
        List<String> notations = new ArrayList<String>();
        for (String unit : _iupac.split("\\$,")) {
            if (unit.matches(".+=[\\d?]")) unit += "$,";
            notations.add(unit);
        }
        Collections.reverse(notations);

		/**/
        for (String subst : notations) {
            IUPACStacker stacker = new IUPACStacker();
            stacker.setNotations(parseNotation(subst));

			//generate moonsaccharide
            IUPACCondensedNotationParser iupacNP = new IUPACCondensedNotationParser();
            for (String unit : stacker.getNotations()) {
                Node node = iupacNP.parseMonosaccharide(unit);

                if (node == null) continue;

                nodeIndex.put(node, unit);
                stacker.addNode(node);
            }

			//define family in each nodes
            parseChildren(stacker, nodeIndex, subst);

			//define linkages
            IUPACCondensedLinkageParser iclp =
                    new IUPACCondensedLinkageParser(glyCo, nodeIndex, stacker);
            glyCo = iclp.start();
        }
    }

    private ArrayList<String> parseNotation (String _iupac) {
        ArrayList<String> ret = new ArrayList<>();
        boolean isLinkage = false;
        boolean isSub = false;
        String node = "";

        for (int i = 0; i < _iupac.length(); i++) {
            char item = _iupac.charAt(i);
            node += item;

            //End
            if (i == _iupac.length() - 1) {
                ret.add(node);
                break;
            }

            //Parse child of substituent
            if (!isLinkage && String.valueOf(item).matches("[\\d?]")) {
                isSub = true;
            }

            //Add substituent to list
            if (isSub && item == ')') {
                ret.add(node);
                node = "";
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

            //add monosaccharide notation to list.
            char nextItem = _iupac.charAt(i+1);
            if (isLinkage && String.valueOf(nextItem).matches("[A-Z(]")) {
                ret.add(node);
                isLinkage = false;
                node = "";
            }
        }

        return ret;
    }

    /*private ArrayList<String> parseNotation(String _iupac) {
        ArrayList<String> ret = new ArrayList<String>();

        String mono = "";
        boolean isLinkage = false;
        boolean isRepeat = false;
        boolean isbisect = false;
        boolean isMultipleParent = false;
        for (int i = 0; i < _iupac.length(); i++) {
            mono += _iupac.charAt(i);

            if (_iupac.charAt(i) == '(') isLinkage = true;

            if (isbisect && _iupac.charAt(i) == ']') {
                ret.add(mono);
                mono = "";
                isbisect = false;
            }

			// for end of multiple parent
            if (isMultipleParent && _iupac.charAt(i) == ')') {
                if (_iupac.charAt(i + 1) == ']' && _iupac.charAt(i + 2) != '-') {
                    isbisect = true;
                    isMultipleParent = false;
                    continue;
                }
                ret.add(mono);
                mono = "";
                isMultipleParent = false;
            }

			// for linkage
            if (isLinkage && _iupac.charAt(i) == ')') {
                if (_iupac.charAt(i + 1) == '=') {
                    isLinkage = false;
                    continue;
                }
                /*if (_iupac.charAt(i + 1) == ']' && _iupac.charAt(i + 2) != '-') {
                    isbisect = true;
                    isLinkage = false;
                    continue;
                }*/

                //if (String.valueOf(_iupac.charAt(i + 1)).matches("[a-zA-Z,]")) continue;
/*                ret.add(mono);
                mono = "";
                isLinkage = false;
            }
			// for repeating
            if (isLinkage && _iupac.charAt(i) == ']') {
                isLinkage = false;
                isRepeat = true;
                continue;
            }
			// for root
            if ((_iupac.length() - 1) == i) {
                ret.add(mono);
                break;
            }
			// for repeating count
            if (isRepeat) {
                if (String.valueOf(_iupac.charAt(i)).matches("[\\dn]")) {
                    if (String.valueOf(_iupac.charAt(i + 1)).matches("\\d")) continue;
                    if (_iupac.charAt(i + 1) == '-' && String.valueOf(_iupac.charAt(i + 2)).matches("[\\dn\\(]")) {
                        continue;
                    }
                    if (_iupac.charAt(i - 1) == '(' && _iupac.charAt(i + 1) == '\u2192') continue;
                    isRepeat = false;
                }

                if (_iupac.charAt(i + 1) == ':') {
                    isMultipleParent = true;
                    continue;
                }

                if (!isRepeat) {
                    ret.add(mono);
                    mono = "";
                }

            }
        }

        return ret;
    }
*/

    private void parseChildren(IUPACStacker _stacker, LinkedHashMap<Node, String> _index, String _subst) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.addAll(_stacker.getNodes());

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

                for (Node cNode : pickChildren(nodes, node, _index, _subst)) {
                    _stacker.addFamily(cNode, node);
                }
            }
        }
    }

    private ArrayList<Node> pickChildren (ArrayList<Node> _nodes, Node _branch, LinkedHashMap<Node, String> _index, String _subst) {
        ArrayList<Node> children = new ArrayList<Node>();
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

            if (isStartOfBranch(node, _index)) {
                count--;
            }
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
        int currentIndex = new ArrayList(_index.keySet()).indexOf(_node);

        if (currentIndex == 0)
            return false;
        if (!_index.get(_node).endsWith(")"))
            return false;

        Node next = (Node) new ArrayList(_index.keySet()).get(currentIndex + 1);
        return (_index.get(next).startsWith("("));
    }

    private boolean haveChild (Node _node, LinkedHashMap<Node, String> _index) {
        int currentIndex = new ArrayList(_index.keySet()).indexOf(_node);

        if (currentIndex == 0)
            return false;
        if (_index.get(_node).startsWith("("))
            return false;
        return true;
    }

    private boolean isStartOfBranch (Node _node, LinkedHashMap<Node, String> _index) {
        int currentIndex = new ArrayList(_index.keySet()).indexOf(_node);

        if (currentIndex == 0)
            return false;
        if (_index.get(_node).startsWith("("))
            return false;

        Node next = (Node) new ArrayList(_index.keySet()).get(currentIndex - 1);
        if (_index.get(next).endsWith(")"))
            return true;

        return false;
    }

    private boolean isEndOfBranch (Node _node, LinkedHashMap<Node, String> _index) {
        return (_index.get(_node).startsWith("("));
        //return (_notation.startsWith("("));
    }
}
