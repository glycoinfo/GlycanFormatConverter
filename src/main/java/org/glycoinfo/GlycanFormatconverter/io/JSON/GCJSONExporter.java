package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.util.MonosaccharideUtility;
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

    public String start(GlyContainer _glyco, boolean _isVisualize) throws GlycanException, ConverterExchangeException {
        //TODO : 単糖のソート処理の実装
        nodeIndex = makeTreeMap(_glyco);
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
                String trivialName = makeTrivialNotation(mono);
                monosaccharide.accumulate("Notation", trivialName);
                monoUtil.modifiedSubstituents(trivialName, mono);
            }

            // extract modifications
            monosaccharide.put("Modifications", this.extractModifications(mono));

            // extract substituent
            monosaccharide.put("Substituents", extractSubstituents(mono));

            // Extract edges
            for (Object obj : extractEdge(mono)) {
                edge.accumulate("e" + edge.length(), obj);
            }

            // Extract repeat
            for (Object obj : extractRepeat(mono)) {
                repeat.accumulate("r" + repeat.length(), obj);
            }

            // extract cross-linked substituent
            JSONObject bridge = extractBridge(mono, false);
            if (bridge.length() != 0) {
                bridges.accumulate("b" + bridges.keySet().size(), bridge);
            }

            monosaccharides.accumulate("m" + String.valueOf(key), monosaccharide);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Monosaccharides", monosaccharides);

        //
        jsonObject.put("Edges", edge);

        //
        jsonObject.put("Bridge", bridges);

        // Extract composition
        jsonObject.put("Composition", this.extractComposition(_glyco, monosaccharides));

        //extract repeating notation
        jsonObject.put("Repeat", repeat);

        // Extract monosaccharide fragments
        jsonObject.accumulate("Fragments", extractFragment(_glyco));

        // Assign aglycone
        jsonObject.put("Aglycone", this.extractAglycone(_glyco.getAglycone()));

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
                repUnit = extractLinkage(lin, false);
            }

            repUnit.accumulate("Min", repMod.getMinRepeatCount());
            repUnit.accumulate("Max", repMod.getMaxRepeatCount());
            repUnit.accumulate("Bridge", extractBridge(_mono, true));
            repUnit.accumulate("Start", "m" + flip.get(parent.getChild()));
            repUnit.accumulate("End", "m" + flip.get(parent.getParent()));

            ret.put(repUnit);
        }

        return ret;
    }

    private JSONObject extractFragment (GlyContainer _glyCo) throws GlycanException {
        JSONObject ret = new JSONObject();
        BidiMap flip = makeFlipMap();

        for (GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
            if (und.isComposition()) continue;

            JSONObject unit = new JSONObject();
            Node root = und.getRootNodes().get(0);

            JSONArray acceptors = new JSONArray();
            for (Node acceptor : und.getParents()) {
                if (flip.get(acceptor) == null) continue;
                acceptors.put("m" + flip.get(acceptor));
            }
            unit.put("Acceptor", acceptors);

            JSONObject pos = new JSONObject();
            if (!und.isComposition()) {
                for (Linkage linkage : und.getConnection().getGlycosidicLinkages()) {
                    pos = extractLinkage(linkage, false);
                }
            }
            unit.accumulate("Edge", pos);

            // Substituent notation
            if (root instanceof Substituent) {
                unit.accumulate("Donor", ((Substituent) root).getNameWithIUPAC());
            } else {
                unit.accumulate("Donor", "m" + flip.get(root));
            }

            ret.accumulate("f" + _glyCo.getUndefinedUnit().indexOf(und), unit);
        }

        return ret;
    }

    private JSONArray extractSubstituents(Monosaccharide _mono) {
        JSONArray ret = new JSONArray();

        for (Edge childEdge : _mono.getChildEdges()) {
            Substituent sub = (Substituent) childEdge.getSubstituent();

            if (sub == null) continue;
            if (sub instanceof GlycanRepeatModification) continue;
            if (sub.getSubstituent() instanceof CrossLinkedTemplate && childEdge.getChild() != null) continue;

            JSONObject unit = new JSONObject();
            //Extract position
            unit = extractLinkage(sub.getFirstPosition(), false);
            //unit.put("Position", extractLinkage(sub.getFirstPosition(), false));

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

    private JSONObject extractProbability (Linkage _lin, boolean _isDonor) {
        JSONObject prob = new JSONObject();

        //is donor side
        if (_isDonor) {
            prob.accumulate("Low", _lin.getParentProbabilityLower());
            prob.accumulate("High", _lin.getParentProbabilityUpper());
        }

        //is acceptor side
        if (!_isDonor) {
            prob.accumulate("Low", _lin.getParentProbabilityLower());
            prob.accumulate("High", _lin.getParentProbabilityUpper());
        }

        return prob;
    }

    private JSONObject extractLinkage(Linkage _lin, boolean _isDonor) {
        JSONObject ret = new JSONObject();

        if (_lin == null) return ret;

        /* extract position */
        JSONObject pos = new JSONObject();
        pos.accumulate("Acceptor", _lin.getParentLinkages());
        pos.accumulate("Donor", _lin.getChildLinkages());

        // extract probability annotation */
        ret.accumulate("Probability", extractProbability(_lin, _isDonor));

        /* extract linkage type */
        JSONObject type = new JSONObject();
        type.accumulate("Acceptor", _lin.getParentLinkageType());
        type.accumulate("Donor", _lin.getChildLinkageType());

        /**/
        ret.accumulate("Position", pos);
        ret.accumulate("LinkageType", type);

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
                edge = extractLinkage(lin, false);
            }

            edge.accumulate("Donor", "m" + flip.get(acceptor.getChild()));
            edge.accumulate("Acceptor", "m" + flip.get(acceptor.getParent()));

            ret.put(edge);
        }

        return ret;
    }

    private JSONObject extractBridge (Node _node, boolean _isRepeat) {
        JSONObject unit = new JSONObject();
        BidiMap flip = makeFlipMap();

        for (Edge parentEdge : _node.getParentEdges()) {
            Substituent sub = (Substituent) parentEdge.getSubstituent();
            if (sub == null) continue;
            if (!(sub.getSubstituent() instanceof  CrossLinkedTemplate)) continue;
            if (sub instanceof GlycanRepeatModification && !_isRepeat) continue;

            unit.accumulate("Notation", sub.getNameWithIUPAC());

            // define node
            //JSONObject node = new JSONObject();
            unit.accumulate("Acceptor", "m" + flip.get(_node.getParentNode()));
            unit.accumulate("Donor", "m" + flip.get(_node));
            //unit.accumulate("Node", node);

            // define linkage position
            JSONObject pos = new JSONObject();
            if (this.haveSubstituentDirection(sub)) {
                pos.accumulate("Donor", sub.getFirstPosition().getChildLinkages().get(0));
                pos.accumulate("Acceptor", sub.getSecondPosition().getChildLinkages().get(0));
                unit.accumulate("Pos", pos);
            }

            // define probability annotation
            //JSONObject prob = new JSONObject();
            //unit.accumulate("Probabiity", prob);

            // define linkage type  \
            if (sub.getFirstPosition() != null && sub.getSecondPosition() != null) {
                JSONObject linType = new JSONObject();
                linType.accumulate("Acceptor", sub.getFirstPosition().getParentLinkageType());
                linType.accumulate("Donor", sub.getSecondPosition().getChildLinkageType());
                unit.accumulate("LinkageType", linType);
            }
        }

        return unit;
    }

    private TreeMap<Integer, Node> makeTreeMap(GlyContainer _glyco) throws GlycanException {
        TreeMap<Integer, Node> ret = new TreeMap<>();
        NodeSimilarity nodeSim = new NodeSimilarity();
        ArrayList<Node> sorted = new ArrayList<>();

        /* sort core notations */
        if (_glyco.getAllNodes().size() == _glyco.getUndefinedUnit().size()) {
            for (GlycanUndefinedUnit und : _glyco.getUndefinedUnit()) {
                sorted.addAll(und.getNodes());
            }
        } else {
            sorted = nodeSim.sortAllNode(_glyco.getRootNodes().get(0));
            /* sort fragments */
            for (GlycanUndefinedUnit und : _glyco.getUndefinedUnit()) {
                Node fragRoot = und.getRootNodes().get(0);
                if (fragRoot instanceof Substituent) continue;
                sorted.addAll(nodeSim.sortAllNode(fragRoot));
            }

        }

        for (Node node : sorted) {
            ret.put(sorted.indexOf(node), node);
        }

        return ret;
    }

    private ArrayList<String> extractTrivialName(LinkedList<String> _stereo) {
        ArrayList<String> ret = new ArrayList<>();
        for (String unit : _stereo) {
            unit = (unit.indexOf("-") != -1) ? unit.substring(unit.indexOf("-") + 1, unit.length()) : unit;
            BaseTypeDictionary baseType = BaseTypeDictionary.forName(unit);

            String name = baseType.getCoreName();
            ret.add(name);
        }

        return ret;
    }

    private String makeTrivialNotation (Node _node) throws ConverterExchangeException, GlycanException {
        IUPACNotationConverter iupacConv = new IUPACNotationConverter();
        iupacConv.makeTrivialName(_node);

        String ret = iupacConv.getCoreCode();

        //iupacConv.getThreeLetterCode() + iupacConv.getSubConv().getCoreSubstituentNotaiton();

        return ret;
    }

    private ArrayList<String> extractConfiguration (LinkedList<String> _stereo) {
        ArrayList<String> ret = new ArrayList<>();
        for (String unit : _stereo) {
            unit = (unit.indexOf("-") != -1) ? unit.substring(unit.indexOf("-") + 1, unit.length()) : unit;
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

        if (ringStart == -1 && ringEnd == -1) return "";
        if (ringStart == 1) {
            if (ringEnd == 4) return "f";
            if (ringEnd == 5) return "p";
        }
        if (ringStart == 2) {
            if (ringEnd == 5) return "f";
            if (ringEnd == 6) return "p";
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
        boolean isUnsaturate = false;
        JSONArray pos = null;

        for (GlyCoModification gMod : mono.getModifications()) {
            if (mono.getSuperClass().getSize() == gMod.getPositionOne() &&
                    gMod.getModificationTemplate().equals(ModificationTemplate.HYDROXYL)) continue;
            JSONObject modUnit = new JSONObject();

            if (isUnsaturation(gMod)) {
                if (isUnsaturate) {
                    pos.put(gMod.getPositionOne());
                    isUnsaturate = false;
                } else {
                    pos = new JSONArray();
                    pos.put(gMod.getPositionOne());
                    isUnsaturate = true;
                    continue;
                }
            } else {
                pos = new JSONArray();
                pos.put(gMod.getPositionOne());
            }

            if (pos != null) modUnit.put("Position", pos);
            modUnit.accumulate("Notation", gMod.getModificationTemplate().getIUPACnotation());
            ret.put(modUnit);
        }

        return ret;
    }

    private JSONObject extractComposition (GlyContainer _glyCo, JSONObject _monoObj) throws GlycanException {
        JSONObject ret = new JSONObject();

        if (!_glyCo.isComposition()) return ret;

        LinkedHashMap<String, ArrayList<String>> count = new LinkedHashMap<>();

        for (Integer key : nodeIndex.keySet()) {
            String jsonKey = "m" + String.valueOf(key);
            String json = _monoObj.get(jsonKey).toString();

            if (!count.containsKey(json.toString())) {
                ArrayList<String> tmp = new ArrayList<>();
                tmp.add(jsonKey);
                count.put(json.toString(), tmp);
            } else {
                count.get(json.toString()).add(jsonKey);
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

    private boolean isUnsaturation (GlyCoModification _gMod) {
        if (_gMod.getModificationTemplate().equals(ModificationTemplate.UNSATURATION_EL) ||
                _gMod.getModificationTemplate().equals(ModificationTemplate.UNSATURATION_EU) ||
                _gMod.getModificationTemplate().equals(ModificationTemplate.UNSATURATION_FL) ||
                _gMod.getModificationTemplate().equals(ModificationTemplate.UNSATURATION_FU) ||
                _gMod.getModificationTemplate().equals(ModificationTemplate.UNSATURATION_ZL) ||
                _gMod.getModificationTemplate().equals(ModificationTemplate.UNSATURATION_ZU)) return true;

        return false;
    }
}