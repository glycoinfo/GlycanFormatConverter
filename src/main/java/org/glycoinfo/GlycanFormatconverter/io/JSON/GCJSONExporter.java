package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.util.similarity.NodeSimilarity;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Created by e15d5605 on 2017/09/19.
 */
public class GCJSONExporter {

    private TreeMap<Integer, Node> nodeIndex;

    public String start(GlyContainer _glyco, boolean _isVisualize) throws GlycanException, ConverterExchangeException {
        //TODO : 単糖のソート処理の実装
        nodeIndex = makeTreeMap(_glyco);

        JSONObject monosaccharides = new JSONObject();
        for (Integer key : nodeIndex.keySet()) {
            Monosaccharide mono = (Monosaccharide) nodeIndex.get(key);

            /* parse monosaccharide */
            JSONObject monosaccharide = new JSONObject();
            monosaccharide.accumulate("AnomericSymbol", mono.getAnomer());
            monosaccharide.accumulate("AnomericPosition", mono.getAnomericPosition());
            monosaccharide.accumulate("TrivialName", extractTrivialName(mono.getStereos()));
            monosaccharide.accumulate("SuperClass", mono.getSuperClass());
            monosaccharide.accumulate("RingStart", mono.getRingStart());
            monosaccharide.accumulate("RingEnd", mono.getRingEnd());

            /* define trivial notation */
            if (_isVisualize) {
                monosaccharide.accumulate("TrivialNotation", makeTrivialNotation(mono));
            }

            /* extract modifications */
            JSONArray modifications = new JSONArray();
            for (GlyCoModification gMod : mono.getModifications()) {
                if (mono.getSuperClass().getSize() == gMod.getPositionOne() &&
                        gMod.getModificationTemplate().equals(ModificationTemplate.HYDROXYL)) continue;
                JSONObject modUnit = new JSONObject();
                modUnit.accumulate("Notation", gMod.getModificationTemplate());
                modUnit.accumulate("PositionOne", gMod.getPositionOne());
                modUnit.accumulate("PositionTwo", gMod.getPositionTwo());
                modifications.put(modUnit);
            }
            monosaccharide.put("Modifications", modifications);

            /* extract substituent */
            monosaccharide.put("Substituents", extractSubstituents(mono));

            /* extract edges */
            monosaccharide.put("Edge", extractEdge(mono, _glyco.getUndefinedUnit()));

            monosaccharides.accumulate(String.valueOf(key), monosaccharide);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Monosaccharides", monosaccharides);

        /* extract substituent fragments */
        jsonObject.put("SubFragments", extractSubstituentFragments(_glyco.getUndefinedUnit()));

        return jsonObject.toString();
    }

    private JSONObject extractSubstituentFragments (ArrayList<GlycanUndefinedUnit> _fragments) throws GlycanException {
        JSONObject ret = new JSONObject();
        for (GlycanUndefinedUnit undefUnit : _fragments) {
            if (undefUnit.getNodes().get(0) instanceof Substituent) {
                JSONObject unit = new JSONObject();

                /**/
                for (Linkage linkage : undefUnit.getConnection().getGlycosidicLinkages()) {
                    unit = extractLinkage(linkage);
                }

                /**/
                Substituent frag = (Substituent) undefUnit.getRootNodes().get(0);
                unit.accumulate("Notation", frag.getSubstituent());

                /**/
                JSONArray parents = new JSONArray();
                for (Node parent : undefUnit.getParents()) {
                    parents.put(getIndex(parent));
                }
                unit.put("ParentNodeID", parents);

                ret.accumulate(String.valueOf(_fragments.indexOf(undefUnit)), unit);
            }
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
            unit.accumulate("Notation", sub.getSubstituent());

            /* extract position one */
            unit.accumulate("PositionOne", extractLinkage(sub.getFirstPosition()));

            /* extract position two */
            unit.accumulate("PositionTwo", extractLinkage(sub.getSecondPosition()));

            ret.put(unit);
        }

        return ret;
    }

    private JSONObject extractLinkage(Linkage _lin) {
        JSONObject ret = new JSONObject();

        if (_lin == null) return ret;

        /* extract position */
        JSONObject pos = new JSONObject();
        pos.accumulate("ParentSide", _lin.getParentLinkages());
        pos.accumulate("ChildSide", _lin.getChildLinkages());

        /* extract probability annotation */
        JSONObject prob = new JSONObject();

        JSONObject parentSideProb = new JSONObject();
        parentSideProb.accumulate("Low", _lin.getParentProbabilityLower());
        parentSideProb.accumulate("High", _lin.getParentProbabilityUpper());
        prob.accumulate("ParentSide", parentSideProb);

        JSONObject childSideProb = new JSONObject();
        childSideProb.accumulate("Low", _lin.getChildProbabilityLower());
        childSideProb.accumulate("High", _lin.getChildProbabilityUpper());
        prob.accumulate("ChildSide", childSideProb);

        /* extract linkage type */
        JSONObject type = new JSONObject();
        type.accumulate("ParentSide", _lin.getParentLinkageType());
        type.accumulate("ChildSide", _lin.getChildLinkageType());

        /**/
        ret.accumulate("Position", pos);
        ret.accumulate("Probability", prob);
        ret.accumulate("LinkageType", type);

        return ret;
    }

    private JSONObject extractEdge(Monosaccharide _mono, ArrayList<GlycanUndefinedUnit> _fragments) throws GlycanException {
        JSONObject parentObj = new JSONObject();

        /* extract parent side edge */
        JSONArray repeat = new JSONArray();
        JSONArray simples = new JSONArray();

        for (Edge parent : _mono.getParentEdges()) {
            Substituent sub = (Substituent) parent.getSubstituent();

            /* extract repeating status */
            if (sub instanceof GlycanRepeatModification) {
                GlycanRepeatModification repMod = (GlycanRepeatModification) sub;

                JSONObject repUnit = new JSONObject();
                for (Linkage lin : parent.getGlycosidicLinkages()) {
                    repUnit = extractLinkage(lin);
                }

                repUnit.accumulate("Min", repMod.getMinRepeatCount());
                repUnit.accumulate("Max", repMod.getMaxRepeatCount());
                repUnit.accumulate("Bridge", extractBridge(repMod));//repMod.getSubstituent() == null ? "" : repMod.getSubstituent());
                repUnit.accumulate("ParentNodeID", getIndex(parent.getParent()));

                repeat.put(repUnit);

                continue;
            }

            /* extract standard parent side linkage */
            if (parent.getParent() != null) {
                JSONObject simple = new JSONObject();
                for (Linkage linkage : parent.getGlycosidicLinkages()) {
                    simple = extractLinkage(linkage);
                }

                /* extract cross linked substituent */
                simple.accumulate("Bridge", extractBridge(sub));

                simple.put("ParentNodeID", getIndex(parent.getParent()));
                simples.put(simple);
            }
        }

        parentObj.put("Repeat", repeat);
        parentObj.put("Parent", simples);
        parentObj.put("Fragment", extractFragment(_mono, _fragments));

        return parentObj;
    }

    private JSONObject extractFragment (Node _node, ArrayList<GlycanUndefinedUnit> _fragments) {
        JSONObject ret = new JSONObject();

        GlycanUndefinedUnit unit = null;
        for (GlycanUndefinedUnit undef : _fragments) {
            if (undef.getNodes().contains(_node)) unit = undef;
        }

        if (unit == null) return ret;

        if (!unit.isComposition()) {
            for (Linkage linkage : unit.getConnection().getGlycosidicLinkages()) {
                ret = extractLinkage(linkage);
            }
        }

        JSONArray parents = new JSONArray();
        for (Node parent : unit.getParents()) {
            parents.put(getIndex(parent));
        }
        ret.put("ParentNodeID", parents);
        ret.put("AnchorID", _fragments.indexOf(unit));

        return ret;
    }

    private JSONObject extractBridge (Substituent _sub) {
        JSONObject unit = new JSONObject();

        if (_sub == null) return unit;
        if (_sub.getSubstituent() == null) return unit;

        unit.accumulate("Notation", _sub.getSubstituent());

        /* extract position one */
        unit.accumulate("PositionOne", extractLinkage(_sub.getFirstPosition()));

        /* extract position two */
        unit.accumulate("PositionTwo", extractLinkage(_sub.getSecondPosition()));

        return unit;
    }

    private Integer getIndex(Node _node) {
        Integer ret = -1;
        for (Integer ind : nodeIndex.keySet()) {
            if (nodeIndex.get(ind).equals(_node)) {
                ret = ind;
            }
        }
        return ret;
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

    private ArrayList<BaseTypeDictionary> extractTrivialName(LinkedList<String> _stereo) {
        ArrayList<BaseTypeDictionary> ret = new ArrayList<>();
        for (String unit : _stereo) {
            unit = (unit.indexOf("-") != -1) ? unit.substring(unit.indexOf("-") + 1, unit.length()) : unit;
            BaseTypeDictionary baseType = BaseTypeDictionary.forName(unit);
            ret.add(baseType);
        }

        return ret;
    }

    private String makeTrivialNotation (Node _node) throws ConverterExchangeException, GlycanException {
        IUPACNotationConverter iupacConv = new IUPACNotationConverter();
        iupacConv.makeTrivialName(_node);

        return iupacConv.getThreeLetterCode();
    }
}