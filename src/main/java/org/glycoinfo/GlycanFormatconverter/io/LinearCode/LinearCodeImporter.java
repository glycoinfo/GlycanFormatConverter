package org.glycoinfo.GlycanFormatconverter.io.LinearCode;

import org.glycoinfo.GlycanFormatconverter.Glycan.Aglycone;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.GlyContainerOptimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2017/08/23.
 */
public class LinearCodeImporter {

    private LinkedHashMap<LinearCodeStacker, Node> lc2node = new LinkedHashMap<>();

    public GlyContainer start (String _linearCode) throws GlyCoImporterException, GlycanException {
        GlyContainer ret = new GlyContainer();
        HashMap<LinearCodeStacker, LinearCodeStacker> family = new HashMap<>();

        _linearCode = _linearCode.trim();

        ArrayList<String> notations = new ArrayList<>();
        String coreNotation = "";

        //Parse aglycone
        Node aglycone = new Aglycone(extractAglycon(_linearCode));
        ret.setAglycone(aglycone);

        _linearCode = modifyFragments(_linearCode);

        // extract unknown linkage fragments
        if (_linearCode.indexOf("&") != _linearCode.lastIndexOf("&")) {
            String subFragment = _linearCode.substring(_linearCode.indexOf("&"), _linearCode.lastIndexOf("&") + 1);
            coreNotation = _linearCode = _linearCode.replace(subFragment, "");
            subFragment = subFragment.replaceAll("&", "");

            // append fragments
            for (String unit : subFragment.split(",")) {
                if (unit.matches(".*[A-Z]")) {
                    unit = unit + "??=%|";
                }
                if (unit.matches(".*[ab]\\?")) {
                    unit = unit + "=%|";
                }
                if (unit.matches(".*[ab]")) {
                    unit = unit + "?=%|";
                }
                if (unit.matches(".+[ab?]\\d")) {
                    unit = unit + "=%|";
                }
                notations.add(unit);
            }
            Collections.reverse(notations);
        }

        // extract monosaccharide fragments
        for (String unit : _linearCode.split("%\\|")) {
            if (unit.matches(".+=\\d")){
                unit += "%|";
                notations.add(unit);
            } else coreNotation = unit;
        }

        // append core notations
        notations.add(coreNotation);

        Collections.reverse(notations);

        for (String fragment : notations) {
            ArrayList<LinearCodeStacker> nodelist = new ArrayList<>();
            for (String unit : resolveNotation(fragment)) {
                if (unit.equals("*")) continue;
                //Add anomeric state
                //if (!unit.matches(".*[A-Z][ab?][\\d?]")) {
                //    unit = unit + "??";
                //}

                LinearCodeStacker lcStacker = new LinearCodeStacker(unit);
                LinearCodeNodeParser nodeParser = new LinearCodeNodeParser();
                lc2node.put(lcStacker, nodeParser.start(lcStacker));

                //define family
                family = parseChildren(lcStacker, family);
                nodelist.add(lcStacker);
            }

            LinearCodeLinkageParser lcParser = new LinearCodeLinkageParser(lc2node, family);
            ret = lcParser.start(nodelist, ret);
        }

        //
        GlyContainerOptimizer gcOpt = new GlyContainerOptimizer();
        gcOpt.start(ret);

        return ret;
    }

    private HashMap<LinearCodeStacker, LinearCodeStacker>
        parseChildren(LinearCodeStacker _lcParent, HashMap<LinearCodeStacker, LinearCodeStacker> _family) {
        ArrayList<LinearCodeStacker> nodes = new ArrayList<>(lc2node.keySet());

        if (nodes.size() == 1) {
            return _family;
        }

        Collections.reverse(nodes);

        int childIndex = nodes.indexOf(_lcParent) + 1;
        LinearCodeStacker lcChild = nodes.get(childIndex);

        if (isRootOfFramgnets(lcChild.getBaseUnit())) {
            _family.put(lcChild, null);
            return _family;
        }

        if (haveChild(_lcParent.getBaseUnit())) {
            _family.put(lcChild, _lcParent);
        }
        if (isStartOfBranch(_lcParent.getBaseUnit())) {
            _family.put(lcChild, _lcParent);

            for (LinearCodeStacker lc : pickChildren(nodes, _lcParent)) {
                _family.put(lc, _lcParent);
            }
        }

        return _family;
    }

    private ArrayList<LinearCodeStacker> pickChildren (ArrayList<LinearCodeStacker> _lcStacks, LinearCodeStacker _branch) {
        ArrayList<LinearCodeStacker> children = new ArrayList<>();
        int count = 0;
        boolean isChild = false;

        if (isStartOfBranch(_branch.getBaseUnit())) count = -1;

        for (LinearCodeStacker lcStack : _lcStacks.subList(_lcStacks.indexOf(_branch) + 1, _lcStacks.size())) {
            String notation = lcStack.getBaseUnit();
            if (isChild) {
                children.add(lcStack);
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

    private String modifyFragments (String _linearCode) {
        if (_linearCode.contains(",") && _linearCode.contains("&")) return _linearCode;
        if (!_linearCode.contains(",") && !_linearCode.contains("&")) return _linearCode;

        // search fragments point
        boolean isSubstituent = false;
        int fragmentsPos = -1;
        for (int i = 0; i < _linearCode.length(); i++) {
            if (_linearCode.charAt(i) == '[') isSubstituent = true;
            if (_linearCode.charAt(i) == ']') isSubstituent = false;
            if (_linearCode.charAt(i) == ',' && !isSubstituent) fragmentsPos = i;
            if (fragmentsPos != -1) break;
        }

        if (fragmentsPos == -1) return _linearCode;

        StringBuilder ret = new StringBuilder(_linearCode);
        ret.replace(fragmentsPos, fragmentsPos+1, "&");
        ret.append("&");

        return ret.toString();
    }

    private String modifyNotation (String _notation) {
        Matcher matAnchor = Pattern.compile("\\(?(\\d%/?)+\\)?").matcher(_notation);
        if (matAnchor.find()) {
            String temp = matAnchor.group(0);
            if ((temp.contains("(") && !temp.contains(")")) || (!temp.contains("(") && temp.contains(")")))
                return _notation;

            String replace = temp;
            replace = replace.replace("(", "");
            replace = replace.replace(")", "");
            replace = replace.replace("/", "");
            _notation = _notation.replace(temp, replace);
        }
        return _notation;
    }

    private boolean isBisecting (String _notation) {
        return (_notation.endsWith(")"));
    }

    private boolean haveChild (String _notation) { return (_notation.matches("^(\\{[n\\d]+)?([\\d%]+)?[A-Z].*")); }

    private boolean isStartOfBranch (String _notation) {
        return (_notation.startsWith(")"));
    }

    private boolean isEndOfBranch (String _notation) { return (_notation.startsWith("(")); }

    private boolean isMonosaccharide (String _notation) { return (_notation.matches("^.*[A-Z]+.*$")); }

    private boolean isRootOfFramgnets (String _notation) { return (_notation.lastIndexOf("%|") != -1); }

    private ArrayList<String> resolveNotation (String _linearCode) throws GlyCoImporterException {
        ArrayList<String> ret = new ArrayList<>();
        String unit = "";
        boolean isAnomericSymbol = false;
        boolean isBranched = false;

        for (int i = 0; i < _linearCode.length(); i++) {
            char word = _linearCode.charAt(i);

            if (word == ' ')
                throw new GlyCoImporterException("This LinearCode is included wrong space.");
                //throw new GlyCoImporterException("This LinearCode is included wrong notation : " + word);
            if (word == '#' || word == ':' || word == ';')  {
                if (isMonosaccharide(unit)) ret.add(modifyNotation(unit));
                break;
            }

            unit = unit + word;

            if (!isAnomericSymbol) {
                if (word == 'a' || word == 'b' || word == '?' || word == '}') {
                    isAnomericSymbol = true;
                }
            }

            if (_linearCode.length() - 1 > i) {
                if (_linearCode.charAt(i+1) == '-' && isAnomericSymbol) continue;
                if (String.valueOf(_linearCode.charAt(i+1)).matches("[\\d?]") && isAnomericSymbol) continue;
                if (_linearCode.charAt(i+1) == '=' || _linearCode.charAt(i+1) == '}') {
                    isAnomericSymbol = false;
                }
                if (_linearCode.charAt(i+1) == '/') continue;
                if (_linearCode.charAt(i+1) == ')' && _linearCode.charAt(i+2) == '(') {
                    // extract branched notation
                    isBranched = true;
                    continue;
                }
            }
            if ((String.valueOf(word).matches("[\\d?\\-}]") && isAnomericSymbol) || isBranched) {
                ret.add(modifyNotation(unit));
                unit = "";
                isAnomericSymbol = false;
                isBranched = false;
            }

            if (i == (_linearCode.length() - 1) && !unit.equals("")) {
                ret.add(modifyNotation(unit));
            }
        }

        return ret;
    }

    private String extractAglycon (String _linearCode) {
        if (_linearCode.contains(":")) {
            return _linearCode.substring(_linearCode.indexOf(":") + 1);
        }
        if (_linearCode.contains(";")) {
            return _linearCode.substring(_linearCode.indexOf(";") + 1);
        }
        if (_linearCode.contains("#")) {
            return _linearCode.substring(_linearCode.indexOf("#") + 1);
        }

        return "";
    }
}
