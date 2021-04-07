package org.glycoinfo.GlycanFormatconverter.io.KCF;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.GlyContainerOptimizer;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/07/31.
 */
public class KCFImporter {

    private final KCFUtility kcfUtil;
    private final HashMap<String, Node> nodeIndex;
    private final GlyContainer glyco;

    public KCFImporter () {
        kcfUtil = new KCFUtility();
        nodeIndex = new HashMap<>();
        glyco = new GlyContainer();
    }

    public GlyContainer start (String _kcf) throws GlyCoImporterException, GlycanException, ConverterExchangeException, TrivialNameException {
        String countKey = "";
        boolean isRepeat = false;
        ArrayList<String> repeatNode = new ArrayList<>();

        kcfUtil.start(_kcf);

        KCFNodeConverter kcfNodeConv = new KCFNodeConverter(kcfUtil);

        // define monosaccharide
        for (String unit : kcfUtil.getNodes()) {
            Node node = kcfNodeConv.start(unit);

            if (node != null) {
                nodeIndex.put(kcfUtil.splitNotation(unit).get(0), node);
            }
        }

        // define glycosidic linkage and substituents
        for (String unit : kcfUtil.getEdges()) {
            extractLinkage(unit);
        }

        // define repeating unit
        for (String unit : kcfUtil.getNotationList()) {
            unit = unit.trim();

            if (unit.equals("///")) break;
            if (unit.startsWith("BRACKET")) isRepeat = true;
            if (isRepeat) {
                if (unit.matches("^BRACKET" + "(\\t|\\s)" + ".*$")) {
                    unit = unit.replaceFirst("BRACKET", "");
                    unit = unit.trim();
                    countKey = unit.substring(0, 1);
                }

                repeatNode.add(unit);

                if (unit.matches("^" + countKey + "(\\s|\\t)+" + "(N|n|\\d)+")) {
                    isRepeat = false;
                    countKey = "";
                    extractRepeatation(repeatNode);
                    repeatNode.clear();
                }
            }
        }

        // modify duplicate substituents
        //MonosaccharideUtility monoUtil = new MonosaccharideUtility();
        //for (String unit : nodeIndex.keySet()) {
        //    monoUtil.margeDuplicateSubstituents(nodeIndex.get(unit));
        //}

        // check anomeric status for root node
        modifyRootStatus(glyco.getRootNodes().get(0));

        //
        GlyContainerOptimizer gcOpt = new GlyContainerOptimizer();
        gcOpt.start(glyco);

        // node validator
        /*
        KCFNodeValidator kcfValidator = new KCFNodeValidator();
        for (Node node : glyco.getAllNodes()) {
            kcfValidator.start(node);
        }
         */

        return glyco;
    }

    private void extractLinkage (String _node) throws GlycanException {
        ArrayList<String> units = kcfUtil.splitNotation(_node);

        String pID = kcfUtil.extractID(units.get(2));
        String cID = kcfUtil.extractID(units.get(1));

        Node childNode;
        String childLin;
        Node parentNode;
        String parentLin;
        String anomericState;

        // extract donor and acceptor monosaccharides
        if ((nodeIndex.get(pID) instanceof Substituent &&
                !(((Substituent) nodeIndex.get(pID)).getSubstituent() instanceof BaseCrossLinkedTemplate))) {
            childNode = nodeIndex.get(pID);
            childLin = kcfUtil.extractLinkagePosition(units.get(2));
            parentNode = nodeIndex.get(cID);
            parentLin = kcfUtil.extractLinkagePosition(units.get(1));

            anomericState = kcfUtil.extractAnomerixState(units.get(2));
        } else {
            childNode = nodeIndex.get(cID);
            childLin = kcfUtil.extractLinkagePosition(units.get(1));
            parentNode = nodeIndex.get(pID);
            parentLin = kcfUtil.extractLinkagePosition(units.get(2));

            anomericState = kcfUtil.extractAnomerixState(units.get(1));
        }

        if (childNode == null && parentNode == null) return;

        // donor node is substituent, acceptor node is null
        if ((childNode instanceof Substituent) && parentNode == null) {
            ArrayList<String> acceptorEdge = kcfUtil.extractAcceptorEdgeByID(cID);
            if (acceptorEdge.isEmpty()) return;

            _node = acceptorEdge.get(0);

            units = kcfUtil.splitNotation(_node);
            childLin = kcfUtil.extractLinkagePosition(units.get(1));
            parentLin = kcfUtil.extractLinkagePosition(units.get(2));

            //ArrayList<String> edges = kcfUtil.splitNotation(_node);
            parentNode = nodeIndex.get(kcfUtil.extractID(childLin));
        }

        if (childNode instanceof Substituent && (parentNode instanceof Substituent || parentNode == null)) return;

        Edge edge = new Edge();

        if (parentNode == null) {
            // define anomeric status for child node
            defineAnomericStatus(anomericState, parentLin, childNode);
            glyco.addNode(childNode);
            return;
        }

        // define edge with monosaccharides
        if (childNode instanceof Monosaccharide) {
            String childPos = childLin.length() == 1 ? String.valueOf(childLin.charAt(0)) : String.valueOf(childLin.charAt(1));

            // define anomeric status for child node
            if (parentNode instanceof Substituent) {
                defineAnomericStatus(anomericState.length() == 1 ? "?" : anomericState, parentLin, childNode);
            } else {
                defineAnomericStatus(anomericState, parentLin, childNode);
            }

            if (parentNode instanceof Substituent) return;

            String parentPos = "?";
            if (parentLin != null) {
                // define anomeric status for parent node
                if (kcfUtil.extractAnomerixState(units.get(2)).length() == 2) {
                    defineAnomericStatus(kcfUtil.extractAnomerixState(units.get(2)), null, parentNode);
                }
                parentPos = parentLin.length() == 1 ? String.valueOf(parentLin.charAt(0)) :
                        parentLin.contains("/") ? parentLin : String.valueOf(parentLin.charAt(1));
            }

            // make edge
            Linkage lin = new Linkage();
            lin.setParentLinkages(makeLinkages(parentPos));
            lin.setChildLinkages(makeLinkages(childPos));
            edge.addGlycosidicLinkage(lin);

            glyco.addNode(parentNode, edge, childNode);

            return;
        }

        // define substituent edge with anomer position
        if (this.isSubstituent(childNode) && pID.equals("1")) {
            String childPos = "?";
            String parentPos = "1";

            if (childLin != null) {
                childPos = childLin;
            }
            if (parentLin != null) {
                parentPos = parentLin;
            }

            Linkage linkage = new Linkage();
            linkage.setParentLinkages(makeLinkages(parentPos));
            linkage.setChildLinkages(makeLinkages(childPos));
            edge.addGlycosidicLinkage(linkage);
            edge.setParent(parentNode);
            edge.setSubstituent(childNode);

            Linkage first = new Linkage();
            first.setParentLinkages(makeLinkages(parentPos));
            first.setChildLinkages(makeLinkages(childPos));
            ((Substituent) childNode).setFirstPosition(first);

            glyco.addNodeWithSubstituent(parentNode, edge, (Substituent) childNode);

            return;
        }

        // define edge with substituent
        if (childNode instanceof Substituent) {
            //TODO: 親を持っているかの確認が必要

            // define cross-linked substituent
            if (this.isCrossLinkedSubstituent(childNode)) {
                String bridgeChild = kcfUtil.extractEdgeByID(cID, true);
                if (!bridgeChild.equals("")) {
                    this.makeBridgeSubstituent(cID, bridgeChild, childNode, parentNode, parentLin);
                    return;
                }
            }

            Linkage first = ((Substituent) childNode).getFirstPosition();
            first.setParentLinkages(makeLinkages(parentLin));
            ((Substituent) childNode).setFirstPosition(first);
            edge.addGlycosidicLinkage(first);

            glyco.addNodeWithSubstituent(parentNode, edge, ((Substituent) childNode));
        }
    }

    private void defineAnomericStatus (String _childLin, String _parentLin, Node _node) throws GlycanException {
        if (_childLin == null) return;
        if (_node instanceof Substituent) return;
        
        int pos = _childLin.length() == 1 ? charToint(_childLin.charAt(0)) : charToint(_childLin.charAt(1));
        char anomericSymbol = _childLin.length() == 1 ? 'x' : modifyAnomericSymbol(_childLin.charAt(0));

        if (isOpenStatus(_node)) {
            pos = Monosaccharide.OPEN_CHAIN;
            anomericSymbol = 'o';
        }

        Monosaccharide mono = (Monosaccharide) _node;

        if (_parentLin != null && _parentLin.length() == 2) {
            anomericSymbol = anomericSymbol == 'o' ? 'x' : anomericSymbol;
            pos = _childLin.length() == 1 ? charToint(_childLin.charAt(0)) : charToint(_childLin.charAt(1));
        }
        
        // define anomeric information for child node
        mono.setAnomericPosition(pos);
        AnomericStateDescriptor anomState = AnomericStateDescriptor.forAnomericState(anomericSymbol);
        mono.setAnomer(anomState);
        
        if (mono.getRingStart() != -1 && mono.getRingEnd() != -1) return;

        // modify ring size
        if (pos == 0 || pos == -1) {
            mono.setRing(Monosaccharide.UNKNOWN_RING, Monosaccharide.UNKNOWN_RING);
        }
        if (mono.getAnomericPosition() != mono.getRingStart()) {
            mono.setRingStart(mono.getAnomericPosition());
        }        
    }

    private int charToint (char _char) {
        if (_char == '?') return -1;
        return (Integer.parseInt(String.valueOf(_char)));
    }

    private Collection<Integer> makeLinkages (String _position) {
        ArrayList<Integer> ret = new ArrayList<>();
        if(_position == null) {
            ret.add(-1);
        } else {
            for (String unit : _position.split("/")) {
                ret.add(charToint(unit.charAt(0)));
            }
        }

        return ret;
    }

    private boolean isOpenStatus (Node _node) {
        Monosaccharide mono = (Monosaccharide) _node;
        if (mono.getAnomericPosition() == 2) return false;
        for (GlyCoModification mod : mono.getModifications()) {
        		if (mod.getPositionOne() == 2) {
        			if (mod.getModificationTemplate().equals(ModificationTemplate.KETONE_U)) return false;
        		}
        }
        if (mono.getAnomericPosition() == Monosaccharide.OPEN_CHAIN &&
                mono.getAnomer().equals(AnomericStateDescriptor.OPEN)) return true;
        for (GlyCoModification mod : mono.getModifications()) {
            if (mod.getPositionOne() == 1) {
                if (mod.getModificationTemplate().equals(ModificationTemplate.ALDONICACID)) return true;
                if (mod.getModificationTemplate().equals(ModificationTemplate.HYDROXYL)) return true;
                if (mod.getModificationTemplate().equals(ModificationTemplate.ALDEHYDE)) return true;
            }
        }
        return false;
    }

    private void extractRepeatation(ArrayList<String> _repeatsUnit) throws GlycanException, GlyCoImporterException {
        // extract start
        Node start = extractRepeatation(_repeatsUnit.get(1), null);

        // extract end
        Node end = extractRepeatation(_repeatsUnit.get(0), _repeatsUnit.get(2));

        if (start instanceof Monosaccharide && end instanceof Monosaccharide)
            makeRepeatingUnit(start, end, null);//, _repeatsUnit.get(2));
        if (start instanceof Substituent)
            makeRepeatingUnitWithSubstituent(null, start, end);//, _repeatsUnit.get(2));
        if (end instanceof Substituent)
            makeRepeatingUnitWithSubstituent(start, end, null);//, _repeatsUnit.get(2));
    }

    private Node extractRepeatation (String _repeat, String _count) throws GlyCoImporterException {
        ArrayList<String> repeat = kcfUtil.splitNotation(_repeat);

        BigDecimal bx = new BigDecimal(repeat.get(1)).setScale(1, BigDecimal.ROUND_DOWN);
        BigDecimal b1 = new BigDecimal(repeat.get(2));
        BigDecimal b2 = new BigDecimal(repeat.get(4));
        BigDecimal by = (b1.add(b2)).multiply(new BigDecimal(".5"));
        bx = bx.setScale(1, BigDecimal.ROUND_DOWN);
        by = by.setScale(1, BigDecimal.ROUND_DOWN);

        return extractNodeFromPosition(bx, by, (_count == null));
    }

    private Node extractNodeFromPosition (BigDecimal _x, BigDecimal _y, boolean _isStart) throws GlyCoImporterException {
        String picked = "";

        // extract start side from EDGE
        for (String s : kcfUtil.getEdges()) {
            ArrayList<String> notations = kcfUtil.splitNotation(s);
            String child = kcfUtil.getNodeByID(kcfUtil.extractID(notations.get(1)));
            String parent = kcfUtil.getNodeByID(kcfUtil.extractID(notations.get(2)));

            // child side coordinate
            BigDecimal cx = new BigDecimal(kcfUtil.splitNotation(child).get(2)).setScale(1, BigDecimal.ROUND_DOWN);
            BigDecimal cy = new BigDecimal(kcfUtil.splitNotation(child).get(3)).setScale(1, BigDecimal.ROUND_DOWN);

            if (cx.doubleValue() > _x.doubleValue()) continue;

            // parent side coordinate
            BigDecimal px = new BigDecimal(kcfUtil.splitNotation(parent).get(2)).setScale(1, BigDecimal.ROUND_DOWN);
            BigDecimal py = new BigDecimal(kcfUtil.splitNotation(parent).get(3)).setScale(1, BigDecimal.ROUND_DOWN);

            // middle side coordinate
            BigDecimal mx = (cx.add(px)).multiply(new BigDecimal(".5")).setScale(1, BigDecimal.ROUND_DOWN);
            BigDecimal my = (cy.add(py)).multiply(new BigDecimal(".5")).setScale(1, BigDecimal.ROUND_DOWN);

            if (_x.doubleValue() > cx.doubleValue() && px.doubleValue() > _x.doubleValue()) {
                if (_isStart) {// && kcfUtil.splitNotation(parent).get(1).equals("*")) {
                    picked = child;
                }
                if (!_isStart) {// && kcfUtil.splitNotation(child).get(1).equals("*")) {
                    picked = parent;
                }
                break;
            }

            if (Double.compare(_y.doubleValue(), my.doubleValue()) == 0 ||
                    Double.compare(_y.doubleValue(), cy.doubleValue()) == 0 ||
                    Double.compare(_y.doubleValue(), py.doubleValue()) == 0) {
                if (cx.doubleValue() < _x.doubleValue() && _x.doubleValue() < px.doubleValue()) {
                    if (_isStart) picked = child;
                    else picked = parent;
                    break;
                }
            } else {
                if (isApproximate(mx.divide(_x, 1, BigDecimal.ROUND_HALF_UP), my.divide(_y, 1, BigDecimal.ROUND_HALF_UP))) {
                    if (_isStart) picked = child;
                    else picked = parent;
                    break;
                }
            }
        }

        if (picked.equals("")) throw new GlyCoImporterException("Repeat brackets could not select !");

        return nodeIndex.get(kcfUtil.splitNotation(picked).get(0));
    }

    private boolean isApproximate (BigDecimal _x, BigDecimal _y) {
        return (1.01 > _x.doubleValue() && _x.doubleValue() > 0.89 &&
                1.01 > _y.doubleValue() && _y.doubleValue() > 0.89);
    }

    private void makeRepeatingUnitWithSubstituent (Node _start, Node _cross, Node _end) throws GlycanException {
        // for start side
        if (_start == null) {
            String startSide = kcfUtil.extractEdgeByID(extractNodeID(_cross), true);
            _start = nodeIndex.get(kcfUtil.extractID(kcfUtil.splitNotation(startSide).get(1)));
        }

        // for end side
        if (_end == null) {
            String endSide = kcfUtil.extractDonorEdgeByID(extractNodeID(_cross));//extractEdgeByID(extractNodeID(_cross), false);
            _end = nodeIndex.get(kcfUtil.extractID(kcfUtil.splitNotation(endSide).get(2)));
        }

        makeRepeatingUnit(_start, _end, _cross);
    }

    private void makeRepeatingUnit (Node _start, Node _end, Node _cross) throws GlycanException {
        Edge parentEdge = new Edge();

        String startPos = kcfUtil.getLinkagePositionByNodeID(extractNodeID(_start), false);

        if (startPos == null) {
            startPos = "?";
        } else {
            startPos = startPos.length() > 1 ? String.valueOf(startPos.charAt(1)) : startPos;
        }

        String endPos = "?";

        ArrayList<String> endUnits = kcfUtil.extractEdgesByID(extractNodeID(_end), true);
        if (endUnits.size() > 1) {
            for (String unit : endUnits) {
                ArrayList<String> units = kcfUtil.splitNotation(unit);
                Node parent = nodeIndex.get(kcfUtil.extractID(units.get(2)));
                Node child = nodeIndex.get(kcfUtil.extractID(units.get(1)));

                if (child == null) {
                    endPos = kcfUtil.extractLinkagePosition(units.get(2));
                }
                //if ((parent != null && parent instanceof Substituent) || (child != null && child instanceof Substituent)) {
                //}
            }
        } else endPos = kcfUtil.extractLinkagePosition(kcfUtil.splitNotation(endUnits.get(0)).get(2));

        Linkage lin = new Linkage();
        lin.setChildLinkages(makeLinkages(startPos));
        lin.setParentLinkages(makeLinkages(endPos));
        parentEdge.addGlycosidicLinkage(lin);

        GlycanRepeatModification repMod = new GlycanRepeatModification(null);
        repMod.setFirstPosition(new Linkage());
        repMod.setSecondPosition(new Linkage());

        if (_cross != null) {
            repMod.setTemplate(((Substituent) _cross).getSubstituent());
            //SubstituentUtility subUtil = new SubstituentUtility();
            //repMod = (GlycanRepeatModification) subUtil.modifyLinkageType(repMod);
        }

        repMod.setMinRepeatCount(-1);
        repMod.setMaxRepeatCount(-1);

        parentEdge.setSubstituent(repMod);

        glyco.addNode(_end, parentEdge, _start);
    }

    private String extractNodeID (Node _node) {
        BidiMap bidiMap = new DualHashBidiMap(nodeIndex);
        return (String) bidiMap.getKey(_node);
    }

    private char modifyAnomericSymbol (char _anomericSymbol) {
        if (String.valueOf(_anomericSymbol).matches("[AB]"))
            return String.valueOf(_anomericSymbol).toLowerCase().charAt(0);
        return _anomericSymbol;
    }

    private void modifyRootStatus (Node root) throws GlycanException {
        Monosaccharide mono = (Monosaccharide) root;

        if (mono.getAnomer().equals(AnomericStateDescriptor.OPEN) &&
                mono.getAnomericPosition() != Monosaccharide.OPEN_CHAIN && !isFacingAnoms(root)) {
            mono.setAnomer(AnomericStateDescriptor.UNKNOWN_STATE);
        }

        if (mono.getAnomer().equals(AnomericStateDescriptor.UNKNOWN_STATE) &&
                mono.getRingEnd() == Monosaccharide.UNKNOWN_RING) {
            mono.setRing(Monosaccharide.UNKNOWN_RING, Monosaccharide.UNKNOWN_RING);
        }

        if (isOpenStatus(root)) {
            mono.setAnomer(AnomericStateDescriptor.OPEN);
            mono.setAnomericPosition(Monosaccharide.OPEN_CHAIN);
            mono.setRing(Monosaccharide.UNKNOWN_RING, Monosaccharide.UNKNOWN_RING);
        }
    }

    private boolean isFacingAnoms (Node _node) {
        int anomericPos = ((Monosaccharide) _node).getAnomericPosition();
        for (Edge childEdge : _node.getChildEdges()) {
            if (!(childEdge.getChild() instanceof Monosaccharide)) continue;
            Monosaccharide child = (Monosaccharide) childEdge.getChild();
            if (child == null) continue;
            if (child.getAnomericPosition() == Monosaccharide.UNKNOWN_RING) continue;
            for (Linkage lin : childEdge.getGlycosidicLinkages()) {
                if (lin.getParentLinkages().contains(anomericPos)) continue;

                if (lin.getChildLinkages().size() == 1 &&
                        lin.getChildLinkages().contains(child.getAnomericPosition()) &&
                        lin.getParentLinkages().contains(anomericPos))
                    return true;
            }
        }
        return false;
    }

    private boolean isRepeat (String _notation) {
        if (_notation.equals("")) return false;
        ArrayList<String> units = kcfUtil.splitNotation(_notation);
        String nodeString = kcfUtil.getNodeByID(units.get(1));
        ArrayList<String> nodeItems = kcfUtil.splitNotation(nodeString);

        return (nodeItems.get(1).equals("*"));
    }

    private void makeBridgeSubstituent (String cID, String bridgeChild, Node childNode, Node parentNode, String parentLin) throws GlycanException {
        Edge edge = new Edge();

        String childID = kcfUtil.extractID(kcfUtil.splitNotation(bridgeChild).get(1));
        if (nodeIndex.get(childID) == null) {
            if (!isRepeat(kcfUtil.extractEdgeByID(cID, true))) {
                // check pyrophoshate
                Linkage first = ((Substituent) childNode).getFirstPosition();
                first.setParentLinkages(makeLinkages(parentLin));
                ((Substituent) childNode).setFirstPosition(first);
                edge.addGlycosidicLinkage(first);

                glyco.addNodeWithSubstituent(parentNode, edge, ((Substituent) childNode));
            }
            return;
        }

        Node bridge = childNode;
        childNode = nodeIndex.get(childID);

        //childLin = kcfUtil.extractLinkagePosition(kcfUtil.splitNotation(kcfUtil.extractEdgeByID(childID, false)).get(1));
        String childLin = kcfUtil.extractLinkagePosition(kcfUtil.splitNotation(kcfUtil.extractDonorEdgeByID(childID)).get(1));
        String childPos = childLin.length() == 1 ? String.valueOf(childLin.charAt(0)) : String.valueOf(childLin.charAt(1));

        Linkage first = new Linkage();
        first.setParentLinkages(makeLinkages(parentLin));
        first.setChildLinkages(makeLinkages(childPos));
        edge.addGlycosidicLinkage(first);

        glyco.addNode(parentNode, edge, childNode);
        glyco.addNodeWithSubstituent(parentNode, edge, ((Substituent) bridge));
    }

    private boolean isMonosaccharide (Node _node) {
        return (_node instanceof Monosaccharide);
    }

    private boolean isCrossLinkedSubstituent (Node _node) {
        if (!(_node instanceof Substituent)) return false;
        Substituent sub = (Substituent) _node;
        return (sub.getSubstituent() instanceof BaseCrossLinkedTemplate);
    }

    private boolean isSubstituent (Node _node) {
        if (!(_node instanceof Substituent)) return false;
        Substituent sub = (Substituent) _node;
        return (sub.getSubstituent() instanceof BaseSubstituentTemplate);
    }

}
