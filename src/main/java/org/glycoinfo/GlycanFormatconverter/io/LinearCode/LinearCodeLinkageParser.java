package org.glycoinfo.GlycanFormatconverter.io.LinearCode;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2017/09/01.
 */
public class LinearCodeLinkageParser {

    private LinkedHashMap<LinearCodeStacker, Node> lc2node;
    private HashMap<LinearCodeStacker, LinearCodeStacker> family;

    public LinearCodeLinkageParser (LinkedHashMap<LinearCodeStacker, Node> _lc2node,
                             HashMap<LinearCodeStacker, LinearCodeStacker> _family) {
        lc2node = _lc2node;
        family = _family;
    }

    public GlyContainer start (ArrayList<LinearCodeStacker> _nodeList, GlyContainer _glyco) throws GlycanException {
        for (LinearCodeStacker lcStacker : _nodeList) {
            GlycanUndefinedUnit antennae = null;
            Node node = lc2node.get(lcStacker);
            _glyco = parseLinkage(lcStacker, _glyco);

            if (isRootOfFramgnets(lcStacker.getBaseUnit())) {
                antennae = new GlycanUndefinedUnit();
                antennae.addNode(node);
                antennae.setConnection(node.getParentEdge());
                for(Node parent : parseFragmentParents(lcStacker.getBaseUnit())) {
                    antennae.addParentNode(parent);
                }
            }

            if (!_glyco.containsNode(node)) {
                if (antennae != null && !antennae.containsNode(node)) _glyco.addNode(node);
                if (antennae == null) _glyco.addNode(node);
            }

            /* set fragments */
            if(antennae != null) _glyco.addGlycanUndefinedUnit(antennae);
        }

        return _glyco;
    }

    private GlyContainer parseLinkage (LinearCodeStacker _lcStacker, GlyContainer _glyco) throws GlycanException {
        if (_lcStacker.getBaseUnit().endsWith("}")) {
            _glyco = parseRepeating(_lcStacker, _glyco);
        }

        _glyco = parseSimpleLinkage(_lcStacker, _glyco);

        return _glyco;
    }

    private GlyContainer parseRepeating (LinearCodeStacker _lcStacker, GlyContainer _glyco) throws GlycanException {
        Node startNode = lc2node.get(_lcStacker);
        ArrayList<LinearCodeStacker> lcs = getEndRepeatingNode(_lcStacker);

        for (LinearCodeStacker endRep : lcs) {
            String startPos = String.valueOf(((Monosaccharide) startNode).getAnomericPosition());
            String endPos = "?";
            int count = -1;

            Node endNode = lc2node.get(endRep);

            String startRepPos = extractMultipleRepStart(_lcStacker).get(lcs.indexOf(endRep) + 1);

            Matcher matStartRep = Pattern.compile("(\\d|\\?)}+$").matcher(startRepPos);
            Matcher matEndRep = Pattern.compile("^\\{([n\\d+]).+").matcher(endRep.getBaseUnit());

            Edge repeatEdge = new Edge();
            Linkage lin = new Linkage();
            //extract parent side linkage
            if (matStartRep.find()) {
                endPos = (matStartRep.group(1) != null) ? matStartRep.group(1) : "?";
            }

            if (matEndRep.find()) {
                String sc = (matEndRep.group(1) != null) ? matEndRep.group(1) : "n";
                count = (sc.equals("n")) ? -1 : Integer.parseInt(sc);
            }

            lin.setChildLinkages(makeLinkage(startPos));
            lin.setParentLinkages(makeLinkage(endPos));
            repeatEdge.addGlycosidicLinkage(lin);

            GlycanRepeatModification repMod = new GlycanRepeatModification(null);

            /* define repeating count */
            repMod.setMaxRepeatCount(count);
            repMod.setMinRepeatCount(count);

            repeatEdge.setSubstituent(repMod);

            repMod.addParentEdge(repeatEdge);

            _glyco.addNode(endNode, repeatEdge, startNode);
        }

        return _glyco;
    }

    private GlyContainer parseSimpleLinkage (LinearCodeStacker _lcStacker, GlyContainer _glyco) throws GlycanException {
        Node parent = lc2node.get(family.get(_lcStacker));
        Monosaccharide current = (Monosaccharide) lc2node.get(_lcStacker);

        if (_lcStacker.getParentLinkage() != null) {
            Edge parentEdge = new Edge();
            Linkage lin = new Linkage();
            lin.setChildLinkages(makeLinkage(String.valueOf(current.getAnomericPosition())));
            lin.setParentLinkages(makeLinkage(_lcStacker.getParentLinkage()));

            parentEdge.addGlycosidicLinkage(lin);

            if (parent != null) _glyco.addNode(parent, parentEdge, current);
            if (parent == null && isRootOfFramgnets(_lcStacker.getBaseUnit())) {
                current.addParentEdge(parentEdge);
                parentEdge.setChild(current);
            }
        }

        return _glyco;
    }

    private ArrayList<LinearCodeStacker> getEndRepeatingNode (LinearCodeStacker _lcStacker) {
        int size = new ArrayList<>(lc2node.keySet()).indexOf(_lcStacker) + 1;

        ArrayList<LinearCodeStacker> lcStackers = new ArrayList<>(lc2node.keySet());

        List<LinearCodeStacker> subNodes = lcStackers.subList(0, size);

        Collections.reverse(subNodes);

        ArrayList<LinearCodeStacker> ret = countRepeats(subNodes);

        if (ret.isEmpty()) {
            ret = countRepeats(lcStackers.subList(lcStackers.indexOf(_lcStacker), lcStackers.size()));
        }

        return ret;
    }

    private ArrayList<LinearCodeStacker> countRepeats (Collection<LinearCodeStacker> _nodes) {
        int numOfstart;
        ArrayList<LinearCodeStacker> retNodes = new ArrayList<>();

		/* extract current repeating unit */
        LinearCodeStacker start = new ArrayList<>(_nodes).get(0);
        TreeMap<Integer, String> repPos = extractMultipleRepStart(start);

        for (Integer unit : repPos.keySet()) {
            numOfstart = unit;

            for (LinearCodeStacker lcStacker : _nodes) {
                String notation = lcStacker.getBaseUnit();

                if (!isStartRep(notation) && !isEndRep(notation) && !isEndRepNonBondingSite(notation)) continue;
                if (isStartRep(notation) && !lcStacker.equals(start)) {
                    numOfstart+= extractMultipleRepStart(lcStacker).size();
                }

                if (isEndRepNonBondingSite(notation)) {
                    numOfstart--;
                    if (numOfstart == 0) retNodes.add(lcStacker);
                } else {
                    Matcher matEndRep = Pattern.compile("\\{[n|\\d+].+").matcher(notation);

                    while (matEndRep.find()) {
                    //String repStatus = matEndRep.group(0);
                    if (isEndRep(notation)) {
                        if (numOfstart != 0) numOfstart--;
                        if (numOfstart == 0) {
                            retNodes.add(lcStacker);
                            break;
                        }
                    }

                    notation = notation.replaceFirst("\\{[n|\\d+]", "");
                    matEndRep = Pattern.compile("\\{[n|\\d+]").matcher(notation);
                    }
                }

                if (numOfstart == 0) break;
            }
        }

        return retNodes;
    }

    private TreeMap<Integer, String> extractMultipleRepStart (LinearCodeStacker _node) {
        String repStart = _node.getBaseUnit();
        TreeMap<Integer, String> repPos = new TreeMap<>();
        int key = 1;

        String startRep = repStart.substring(repStart.indexOf("}") - 1);
        for (String pos : startRep.split("}")) {
            //if (isStartRep(pos)) {
                repPos.put(key, pos + "}");
                key++;
            //}
        }

        return repPos;
    }

    private boolean isStartRep (String _notation) {
        return (_notation.matches(".*}$"));
    }

    private boolean isEndRep (String _notation) {
        return (_notation.matches("^\\{[n|\\d]+.*"));
    }

    private boolean isEndRepNonBondingSite (String _notation) {
        return (_notation.matches(".+-.+-$"));
    }

    private boolean isRootOfFramgnets (String _notation) {
        return (_notation.lastIndexOf("%|") != -1);
    }

    private ArrayList<Node> parseFragmentParents (String _fragment) {
        ArrayList<Node> ret = new ArrayList<>();
        String anchor = _fragment.substring(_fragment.indexOf("=") + 1, _fragment.length() - 1);

        for(LinearCodeStacker lcStacker : lc2node.keySet()) {
            if (isRootOfFramgnets(lcStacker.getBaseUnit())) break;
            if (anchor.equals("%")) {
                ret.add(lc2node.get(lcStacker));
            } else {
                if (lcStacker.getBaseUnit().contains(anchor) && !isRootOfFramgnets(lcStacker.getBaseUnit())) {
                    ret.add(lc2node.get(lcStacker));
                }
            }
        }

        return ret;
    }

    private LinkedList<Integer> makeLinkage (String _position) {
        _position = _position.equals("?") ? "-1" : _position;
        LinkedList<Integer> ret = new LinkedList<>();
        for (String unit : _position.split("/")) {
            ret.add(Integer.parseInt(unit));
        }

        return ret;
    }
}
