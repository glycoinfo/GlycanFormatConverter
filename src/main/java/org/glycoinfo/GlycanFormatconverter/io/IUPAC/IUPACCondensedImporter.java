package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by e15d5605 on 2017/07/31.
 */
public class IUPACCondensedImporter {

    private GlyContainer glyCo = new GlyContainer();

    public GlyContainer getGlyContainer () {
        return glyCo;
    }

    public void start (String _iupac) throws GlyCoImporterException, GlycanException {
        System.out.println(_iupac);

        LinkedHashMap<Node, String> nodeIndex = new LinkedHashMap<Node, String>();

        /**/
        List<String> notations = new ArrayList<String>();
        for (String unit : _iupac.split("\\$,")) {
            if (unit.matches(".+=[\\d\\?]")) unit += "$,";
            notations.add(unit);
        }
        Collections.reverse(notations);

		/**/
        for (String subst : notations) {
            IUPACStacker stacker = new IUPACStacker();
            stacker.setNotations(parseNotation(subst));

            System.out.println(stacker.getNotations());

			/* generate moonsaccharide */
            IUPACCondensedNotationParser iupacNP = new IUPACCondensedNotationParser();
            for (String unit : stacker.getNotations()) {
                //Node node = iupacNP.start(unit);
                Node node = iupacNP.parseMonosaccharide(unit);
                nodeIndex.put(node, unit);
                stacker.addNode(node);
            }

			/* define family in each nodes */
            parseChildren(stacker, nodeIndex);

			/* define linkages */
            IUPACLinkageParser iupacLP = new IUPACLinkageParser(glyCo, nodeIndex, stacker);
            iupacLP.start();

            glyCo = iupacLP.getGlyCo();
        }
    }

    private ArrayList<String> parseNotation(String _iupac) {
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

			/* for end of multiple parent */
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

			/* for linkage */
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
                ret.add(mono);
                mono = "";
                isLinkage = false;
            }
			/* for repeating */
            if (isLinkage && _iupac.charAt(i) == ']') {
                isLinkage = false;
                isRepeat = true;
                continue;
            }
			/* for root */
            if ((_iupac.length() - 1) == i) {
                ret.add(mono);
                break;
            }
			/* for repeating count */
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

    private void parseChildren(IUPACStacker _stacker, LinkedHashMap<Node, String> _index) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        nodes.addAll(_stacker.getNodes());

        Collections.reverse(nodes);

        for (Node node : nodes) {
            String current = _index.get(node);
            if (haveChild(current)) {
                int childIndex = nodes.indexOf(node) + 1;
                Node child = nodes.get(childIndex);
                _stacker.addFamily(child, node);
            }

            if (isStartOfBranch(current)) {
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
        ArrayList<Node> children = new ArrayList<Node>();
        int count = 0;
        boolean isChild = false;

        if (isStartOfBranch(_index.get(_branch))) count = -1;

        for (Node node : _nodes.subList(_nodes.indexOf(_branch) + 1, _nodes.size())) {
            String notation = _index.get(node);
            if (isChild) {
                children.add(node);
            }

            if (count == 0 && !isBisecting(notation)) {
                if (isStartOfBranch(notation)) break;
                if (isEndOfBranch(notation)) break;
                if (haveChild(notation)) break;
            }

            if (isStartOfBranch(notation)) count--;
            if (isEndOfBranch(notation)) count++;
            if (isBisecting(notation)) count--;

            if (count == 0) {
                if (isBisecting(notation)) isChild = true;
                if (isEndOfBranch(notation)) isChild = true;
                continue;
            }

            isChild = false;
        }

        return children;
    }

    private boolean isBisecting (String _notation) {
        return (_notation.endsWith("]"));
    }

    private boolean haveChild (String _notation) {
        return (_notation.startsWith("-"));
    }

    private boolean isStartOfBranch (String _notation) {
        return (_notation.startsWith("]-"));
    }

    private boolean isEndOfBranch (String _notation) {
        //if(isBisecting(_notation)) return false;
        if (_notation.matches("\\[\\d\\).+")) return false;
        return (_notation.startsWith("["));
    }
}
