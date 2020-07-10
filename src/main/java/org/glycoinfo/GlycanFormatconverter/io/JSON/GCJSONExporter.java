package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed.CondensedConverter;
import org.glycoinfo.GlycanFormatconverter.util.MonosaccharideUtility;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.GlycanFormatconverter.util.similarity.NodeSimilarity;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by e15d5605 on 2017/09/19.
 */
public class GCJSONExporter {

    private TreeMap<Integer, Node> nodeIndex;
    private HashMap<Edge, String> edgeIndex;

    public GCJSONExporter () {
        this.edgeIndex = new HashMap<>();
    }

    public String start(WURCSGraphToGlyContainer _wg2gc) throws GlycanException, ConverterExchangeException {
        boolean _isVisualize = true;
        GlyContainer glyco = _wg2gc.getGlycan();

        nodeIndex = makeTreeMap(glyco, _wg2gc.getSortedList());
        MonosaccharideUtility monoUtil = new MonosaccharideUtility();

        JSONObject monosaccharides = new JSONObject();
        JSONObject bridges = new JSONObject();
        JSONObject repeat = new JSONObject();
        JSONObject edge = new JSONObject();

        for (Integer key : nodeIndex.keySet()) {
            Monosaccharide mono = (Monosaccharide) nodeIndex.get(key);

            // parse monosaccharide
            JSONObject monosaccharide = new JSONObject();
            monosaccharide.accumulate("AnomState", mono.getAnomer().getAnomericState());
            monosaccharide.accumulate("AnomPosition", mono.getAnomericPosition());
            monosaccharide.accumulate("TrivialName", extractTrivialName(mono.getStereos()));
            monosaccharide.accumulate("Configuration", extractConfiguration(mono.getStereos()));
            monosaccharide.accumulate("SuperClass", mono.getSuperClass());
            monosaccharide.accumulate("RingSize", makeRingSymbol(mono));

            // define trivial notation
            if (_isVisualize) {
                Monosaccharide copyMono = mono.copy();
                String trivialName = makeTrivialNotation(copyMono);
                monosaccharide.accumulate("Notation", trivialName);
                monoUtil.modifiedSubstituents(trivialName, copyMono);
            }

            // extract modifications
            monosaccharide.put("Modifications", this.extractModifications(mono));

            // extract substituent
            monosaccharide.put("Substituents", extractSubstituents(mono));

            // Extract edges
            if (!this.isFragmentRoot(glyco, mono)) {
                for (Object obj : extractEdge(mono)) {
                    edge.accumulate("e" + edge.length(), obj);
                }
            }

            // Extract repeat
            for (Object obj : extractRepeat(mono)) {
                repeat.accumulate("r" + repeat.length(), obj);
            }

            // extract cross-linked substituent
            JSONObject bridge = extractBridge(mono);
            if (bridge.length() != 0) {
                bridges.accumulate("b" + bridges.keySet().size(), bridge);
            }

            monosaccharides.accumulate("m" + key, monosaccharide);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Monosaccharides", monosaccharides);

        //
        jsonObject.put("Edges", edge);

        //
        jsonObject.put("Bridge", bridges);

        // Extract composition
        jsonObject.put("Composition", this.extractComposition(glyco, monosaccharides));

        // Extract repeating notation
        jsonObject.put("Repeat", repeat);

        // Extract monosaccharide fragments
        jsonObject.accumulate("Fragments", extractFragment(glyco));

        // Assign aglycone
        jsonObject.put("Aglycone", this.extractAglycone(glyco.getAglycone()));

        // Add original WURCS string
        jsonObject.put("WURCS", "");

        // Add accession number
        jsonObject.put("AN", "");

        return jsonObject.toString();
    }

    private JSONArray extractRepeat (Monosaccharide _mono) {
        JSONArray ret = new JSONArray();

        BidiMap flip = makeFlipMap();

        for (Edge parent : _mono.getParentEdges()) {
            Substituent sub = (Substituent) parent.getSubstituent();
            if (sub == null) continue;
            if (!(sub instanceof GlycanRepeatModification)) continue;

            GlycanRepeatModification repMod = (GlycanRepeatModification) sub;

            JSONObject repUnit = new JSONObject();
            for (Linkage lin : parent.getGlycosidicLinkages()) {
                repUnit = extractLinkage(lin);//, false);
            }

            //Acceptor is end repeat node.
            //Donor is start repeat node.
            repUnit.getJSONObject("Donor").accumulate("Node", "m" + flip.get(parent.getChild()));
            repUnit.getJSONObject("Acceptor").accumulate("Node", "m" + flip.get(parent.getParent()));

            repUnit.accumulate("Min", repMod.getMinRepeatCount());
            repUnit.accumulate("Max", repMod.getMaxRepeatCount());

            ret.put(repUnit);
        }

        return ret;
    }

    private JSONObject extractFragment (GlyContainer _glyCo) throws GlycanException {
        JSONObject ret = new JSONObject();
        BidiMap flip = makeFlipMap();

        for (GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
            if (und.isComposition()) continue;

            Node root = und.getRootNodes().get(0);

            ArrayList<String> acceptors = new ArrayList<>();
            for (Node acceptor : und.getParents()) {
                if (flip.get(acceptor) == null) continue;
                acceptors.add("m" + flip.get(acceptor));
            }

            JSONObject pos = new JSONObject();
            if (!und.isComposition()) {
                for (Linkage linkage : und.getConnection().getGlycosidicLinkages()) {
                    pos = extractLinkage(linkage);
                }
            }

            pos.getJSONObject("Acceptor").accumulate("Node", acceptors);

            // Substituent notation
            if (root instanceof Substituent) {
                pos.getJSONObject("Donor").accumulate("Notation", ((Substituent) root).getNameWithIUPAC());
            } else {
                pos.getJSONObject("Donor").accumulate("Node", "m" + flip.get(root));
            }

            ret.accumulate("f" + _glyCo.getUndefinedUnit().indexOf(und), pos);
        }

        return ret;
    }

    private JSONArray extractSubstituents(Monosaccharide _mono) {
        JSONArray ret = new JSONArray();

        for (Edge childEdge : _mono.getChildEdges()) {
            Substituent sub = (Substituent) childEdge.getSubstituent();

            if (sub == null) continue;
            if (sub instanceof GlycanRepeatModification) continue;
            if (sub.getSubstituent() instanceof BaseCrossLinkedTemplate && childEdge.getChild() != null) continue;

            JSONObject unit;
            //Extract position
            unit = extractLinkage(sub.getFirstPosition());

            unit.accumulate("Notation", sub.getSubstituent().getIUPACnotation());

            //Add state
            if (sub.getSecondPosition() != null && !sub.getSecondPosition().getParentLinkages().isEmpty()) {
                unit.accumulate("Status", "simple");
            }
            if (sub.getSecondPosition() == null) {
                if (sub.getFirstPosition().getParentLinkages().size() > 1) {
                    unit.accumulate("Status", "fuzzy");
                } else {
                    unit.accumulate("Status", "simple");
                }
            }

            ret.put(unit);
        }

        return ret;
    }

    private JSONObject extractProbability (Linkage _lin) {
        JSONObject prob = new JSONObject();
        prob.accumulate("Low", _lin.getParentProbabilityLower());
        prob.accumulate("High", _lin.getParentProbabilityUpper());

        return prob;
    }

    private JSONObject extractLinkage(Linkage _lin) {
        JSONObject ret = new JSONObject();

        if (_lin == null) return ret;

        /*
        "Acceptor" : {
	        node: "m0",
	        Position: [4],
	        LinkageType: "H_AT_OH"
        },
        "Donor" : {
        	node: "m1",
	        Position: [1],
	        LinkageType: "DEOXY"
        }
        */
        JSONObject acceptor = new JSONObject();
        acceptor.accumulate("Position", _lin.getParentLinkages());
        acceptor.accumulate("LinkageType", _lin.getParentLinkageType());

        JSONObject donor = new JSONObject();
        donor.accumulate("Position", _lin.getChildLinkages());
        donor.accumulate("LinkageType", _lin.getChildLinkageType());

        // extract probability annotation
        ret.accumulate("Probability", extractProbability(_lin));

        ret.accumulate("Donor", donor);
        ret.accumulate("Acceptor", acceptor);

        return ret;
    }

    private JSONArray extractEdge (Monosaccharide _mono) {
        JSONArray ret = new JSONArray();
        BidiMap flip = makeFlipMap();

        for (Edge acceptor : _mono.getParentEdges()) {
            Substituent sub = (Substituent) acceptor.getSubstituent();
            if (sub != null && sub instanceof GlycanRepeatModification) continue;
            if (acceptor.getParent() == null) continue;

            JSONObject edge = new JSONObject();
            for (Linkage lin : acceptor.getGlycosidicLinkages()) {
                edge = extractLinkage(lin);//, false);
            }

            edge.getJSONObject("Donor").accumulate("Node", "m" + flip.get(acceptor.getChild()));
            edge.getJSONObject("Acceptor").accumulate("Node", "m" + flip.get(acceptor.getParent()));

            this.edgeIndex.put(acceptor, "e" + this.edgeIndex.size());

            ret.put(edge);
        }

        return ret;
    }

    private JSONObject extractBridge (Node _node) {
        JSONObject unit = new JSONObject();
        BidiMap flip = makeFlipMap();

        for (Edge parentEdge : _node.getParentEdges()) {
            Substituent sub = (Substituent) parentEdge.getSubstituent();
            if (sub == null) continue;
            if (!(sub.getSubstituent() instanceof  BaseCrossLinkedTemplate)) continue;

            unit.accumulate("Notation", sub.getNameWithIUPAC());

            JSONObject acceptor = new JSONObject();
            JSONObject donor = new JSONObject();

            acceptor.accumulate("Node", "m" + flip.get(_node.getParentNode()));
            donor.accumulate("Node", "m" + flip.get(_node));

            // define linkage position
            if (this.haveSubstituentDirection(sub)) {
                donor.accumulate("Position", sub.getFirstPosition().getChildLinkages().get(0));
                acceptor.accumulate("Position", sub.getSecondPosition().getChildLinkages().get(0));
            }

            // define linkage type
            if (sub.getFirstPosition() != null && sub.getSecondPosition() != null) {
                acceptor.accumulate("LinkageType", sub.getFirstPosition().getChildLinkageType());
                donor.accumulate("LinkageType", sub.getSecondPosition().getParentLinkageType());
            }

            unit.accumulate("Donor", donor);
            unit.accumulate("Acceptor", acceptor);
            unit.accumulate("Target", this.edgeIndex.get(parentEdge));
        }

        return unit;
    }

    private TreeMap<Integer, Node> makeTreeMap(GlyContainer _glyco, ArrayList<Node> _sortedNodes) throws GlycanException {
        TreeMap<Integer, Node> ret = new TreeMap<>();
        NodeSimilarity nodeSim = new NodeSimilarity();
        ArrayList<Node> sorted = new ArrayList<>();

        // sort core notations
        if (_glyco.getAllNodes().size() == _glyco.getUndefinedUnit().size()) {
            for (GlycanUndefinedUnit und : _glyco.getUndefinedUnit()) {
                sorted.addAll(und.getNodes());
            }
        } else {
            sorted.addAll(_sortedNodes);//nodeSim.sortAllNode(_glyco.getRootNodes().get(0));
        }

        for (Node node : sorted) {
            ret.put(sorted.indexOf(node), node);
        }

        return ret;
    }

    private ArrayList<String> extractTrivialName(LinkedList<String> _stereo) {
        ArrayList<String> ret = new ArrayList<>();
        for (String unit : _stereo) {
            unit = (unit.contains("-")) ? unit.substring(unit.indexOf("-") + 1) : unit;
            BaseTypeDictionary baseType = BaseTypeDictionary.forName(unit);

            String name = baseType.getCoreName();
            ret.add(name);
        }

        return ret;
    }

    private String makeTrivialNotation (Node _node) throws GlycanException {
        CondensedConverter extConv = new CondensedConverter();
        return extConv.start(_node, false);
    }

    private ArrayList<String> extractConfiguration (LinkedList<String> _stereo) {
        ArrayList<String> ret = new ArrayList<>();
        for (String unit : _stereo) {
            unit = (unit.contains("-")) ? unit.substring(unit.indexOf("-") + 1) : unit;
            BaseTypeDictionary baseType = BaseTypeDictionary.forName(unit);

            String name = baseType.getConfiguration();
            ret.add(name);
        }

        return ret;
    }

    private String makeRingSymbol (Node _node) {
        Monosaccharide mono = (Monosaccharide) _node;
        int ringStart = mono.getRingStart();
        int ringEnd = mono.getRingEnd();

        if (ringStart == 0 && ringEnd == 0) return "o";
        if (ringStart == 1) {
            if (ringEnd == 4) return "f";
            if (ringEnd == 5) return "p";
            if (ringEnd == -1) return "?";
        }
        if (ringStart == 2) {
            if (ringEnd == 5) return "f";
            if (ringEnd == 6) return "p";
            if (ringEnd == -1) return "?";
        }

        return "";
    }

    private boolean haveSubstituentDirection (Node _node) {
        if (!(_node instanceof Substituent)) return false;

        Substituent sub = (Substituent) _node;

        if (sub.getFirstPosition() == null || sub.getSecondPosition() == null) return false;
        if (!sub.getFirstPosition().getChildLinkages().isEmpty() &&
                !sub.getSecondPosition().getChildLinkages().isEmpty()) return true;

        return false;
    }

    private BidiMap makeFlipMap () {
        BidiMap bidi = new DualHashBidiMap(this.nodeIndex);
        BidiMap flip = bidi.inverseBidiMap();

        return flip;
    }

    private JSONArray extractModifications (Node _node) {
        Monosaccharide mono = (Monosaccharide) _node;
        JSONArray ret = new JSONArray();

        for (GlyCoModification gMod : mono.getModifications()) {
            if (mono.getSuperClass().getSize() == gMod.getPositionOne() &&
                    gMod.getModificationTemplate().equals(ModificationTemplate.HYDROXYL)) continue;
            JSONObject modUnit = new JSONObject();

            modUnit.accumulate("Position", gMod.getPositionOne());
            modUnit.accumulate("Notation", gMod.getModificationTemplate().getIUPACnotation());

            if (isDeoxyUnsaturation(gMod.getModificationTemplate())) {
                JSONObject deoxy = new JSONObject();
                deoxy.accumulate("Position", gMod.getPositionOne());
                deoxy.accumulate("Notation", "deoxy");
                ret.put(deoxy);
            }

            ret.put(modUnit);
        }

        return ret;
    }

    private JSONObject extractComposition (GlyContainer _glyCo, JSONObject _monoObj) {
        JSONObject ret = new JSONObject();

        if (!_glyCo.isComposition()) return ret;

        LinkedHashMap<String, ArrayList<String>> count = new LinkedHashMap<>();

        for (Integer key : nodeIndex.keySet()) {
            String jsonKey = "m" + key;
            String json = _monoObj.get(jsonKey).toString();

            if (!count.containsKey(json)) {
                ArrayList<String> tmp = new ArrayList<>();
                tmp.add(jsonKey);
                count.put(json, tmp);
            } else {
                count.get(json).add(jsonKey);
            }
        }

        for (String key : count.keySet()) {
            JSONObject unit = new JSONObject();
            unit.put("Monosaccharide", count.get(key).get(0));
            unit.put("Count", count.get(key).size());
            ret.accumulate("c" + ret.length(), unit);
        }

        return ret;
    }

    private String extractAglycone (Node _node) {
        if (_node == null) return "";
        if (_node instanceof Aglycone) return "";

        Aglycone agly = (Aglycone) _node;
        return agly.getName();
    }

    private boolean isDeoxyUnsaturation (ModificationTemplate _modTemp) {
        return (_modTemp.equals(ModificationTemplate.UNSATURATION_EL) ||
                _modTemp.equals(ModificationTemplate.UNSATURATION_ZL) ||
                _modTemp.equals(ModificationTemplate.UNSATURATION_FL));
    }

    private boolean isFragmentRoot (GlyContainer _glyco, Node _root) {
        boolean ret = false;
        for (GlycanUndefinedUnit und : _glyco.getUndefinedUnit()) {
            Edge connection = und.getConnection();
            if (connection.getChild() != null && connection.getChild().equals(_root)) {
                ret = true;
                break;
            }
            if (connection.getSubstituent() != null && connection.getSubstituent().equals(_root)) {
                ret = true;
                break;
            }
        }

        return ret;
    }
}