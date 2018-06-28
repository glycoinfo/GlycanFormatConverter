package org.glycoinfo.GlycanFormatconverter.io.KCF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2017/08/23.
 */
public class KCFUtility {

    private ArrayList<String> nodes = new ArrayList<String>();
    private ArrayList<String> edges = new ArrayList<String>();
    private ArrayList<String> notationList = new ArrayList<String>();

    public ArrayList<String> getNotationList () {
        return notationList;
    }

    public ArrayList<String> getNodes () {
        return nodes;
    }

    public ArrayList<String> getEdges () {
        return edges;
    }

    public void start (String _kcf) {
        String status = "";
        String countKey = "";

        notationList = new ArrayList<String>(Arrays.asList(_kcf.split("\n")));

        for (String unit : notationList) {

            unit = unit.trim();

            if (unit.equals("///")) break;

            Matcher matNode = Pattern.compile("^([A-Z]+)+\\s+(\\d+)+$").matcher(unit);
            if (matNode.find()) {
                status = matNode.group(1);
                countKey = matNode.group(2);
                continue;
            }

            if (status.equals("NODE")) {
                nodes.add(unit);
            }

            if (status.equals("EDGE")) {
                edges.add(unit);
            }

            if (unit.startsWith(countKey)) {
                countKey = "";
                status = "";
            }
        }
    }

    public String getEdgeByID (String _id, boolean _isParent) {
        String ret = "";
        for (String s : edges) {
            String notation = (_isParent) ? splitNotation(s).get(2) : splitNotation(s).get(1);
            if (extractID(notation).equals(_id)) ret = s;
        }

        return ret;
    }

    public String getNodeByID (String _id) {
        String ret = "";
        for (String s : nodes) {
            if (splitNotation(s).get(0).equals(_id)) ret = s;
        }

        return ret;
    }

    public String getLinkagePositionByNodeID(String _id, boolean _isParent) {
        String ret = "";
        for (String s : edges) {
            String status = (_isParent) ? splitNotation(s).get(2) : splitNotation(s).get(1);
            if (_id.equals(extractID(status))) ret = status;
        }

        return extractLinkagePosition(ret);
    }

    public ArrayList<String> splitNotation (String _node) {
        ArrayList<String> ret = new ArrayList<String>();
        for (String s : _node.split("\\s")) {
            if (s.equals("")) continue;
            ret.add(s);
        }

        return ret;
    }

    public String extractID (String _linkage) {
        String[] units = _linkage.split(":");

        return units[0];
    }

    public String extractEdgeByID (String _currentID, boolean _isParent) {
        String ret = "";
        boolean isEdge = false;
        for (String unit : edges) {
            String childID = (_isParent) ? extractID(splitNotation(unit).get(2)) : extractID(splitNotation(unit).get(1));
            if (childID.equals(_currentID)) {
                ret = unit;
                break;
            }
        }
        return ret;
    }

    public ArrayList<String> extractEdgesByID (String _currentID, boolean _isParent) {
        ArrayList<String> ret = new ArrayList<>();

        for (String unit : edges) {
            String childID = (_isParent) ? extractID(splitNotation(unit).get(2)) : extractID(splitNotation(unit).get(1));
            if (childID.equals(_currentID)) {
                ret.add(unit);
            }
        }
        return ret;
    }

    public String extractLinkagePosition (String _linkage) {
        if (!_linkage.contains(":")) return null;
        String[] units = _linkage.split(":");

        String ret = units[1];

        if (ret.matches("^[abAB].+")) ret = ret.replaceAll("[abAB]", "");

        return ret;
    }

    public String extractAnomerixState (String _linkage) {
        if (!_linkage.contains(":")) return null;
        String[] units = _linkage.split(":");

        String ret = units[1];

        return ret;
//        if (ret.startsWith("a") || ret.startsWith("b")) return ret.substring(0, 1);
//        else return "?";
    }
}
