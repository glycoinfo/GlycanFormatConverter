package org.glycoinfo.GlycanFormatconverter.util;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2019/03/05.
 */
public class GlyContainerOptimizer {

    public GlyContainer start (GlyContainer _glyCo) throws GlycanException {

        for (Node node : _glyCo.getNodes()) {
            // Optimize monoasccharide
            this.optimizeMonosaccharide((Monosaccharide) node);

            // Merge duplicated substituents
            this.optimizeDuplicateSubstituent((Monosaccharide) node);

            // Optimize linkage type of substituent
            this.optimizeSubstituent((Monosaccharide) node);

            // Optimize glycosidic linkage
            this.optimizeGlycoSidicLinkage((Monosaccharide) node);

            //checkStatus(node);
        }

        // Optimize glycan fragments
        for (GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
            optimizeUndefinedUnit(und);
        }

        return _glyCo;
    }

    //TODO: What should this method do ?
    public void optimizeMonosaccharide (Monosaccharide _mono) {
        //TODO : optimize anomeric state
    }

    public void optimizeDuplicateSubstituent (Monosaccharide _mono) throws GlycanException {
        HashMap<Integer, ArrayList<Edge>> subMap = extractDupeSubstituents(_mono);

        for (Integer key : subMap.keySet()) {
            ArrayList<Edge> edges = subMap.get(key);
            if (edges.size() < 2) continue;
            if (edges.size() > 2)
                throw new GlycanException("This node have multiple substituents for same position.");

            Substituent sub1 = (Substituent) edges.get(0).getSubstituent();
            Substituent sub2 = (Substituent) edges.get(1).getSubstituent();

            if (sub1.getSubstituent() == null)
                throw new GlycanException("Substituent could not defined.");
            if (sub2.getSubstituent() == null)
                throw new GlycanException("Substituent could not defined.");

            if (sub1.getSubstituent().equals(BaseSubstituentTemplate.AMINE)) {
                String oldNotation = sub2.getSubstituent().getIUPACnotation();
                BaseSubstituentTemplate bsubT = BaseSubstituentTemplate.forIUPACNotationWithIgnore("N" + oldNotation);
                sub2.setTemplate(bsubT);

                _mono.removeChildEdge(edges.get(0));
            }
            if (sub2.getSubstituent().equals(BaseSubstituentTemplate.AMINE)) {
                String oldNotation = sub2.getSubstituent().getIUPACnotation();
                BaseSubstituentTemplate bsubT = BaseSubstituentTemplate.forIUPACNotationWithIgnore("N" + oldNotation);
                sub1.setTemplate(bsubT);

                _mono.removeChildEdge(edges.get(1));
            }
        }

        return;
    }

    private HashMap<Integer, ArrayList<Edge>> extractDupeSubstituents (Node _node) {
        HashMap<Integer, ArrayList<Edge>> subMap = new HashMap<>();
        for (Edge childEdge : _node.getChildEdges()) {
            if (childEdge.getSubstituent() == null) continue;
            Substituent sub = (Substituent) childEdge.getSubstituent();

            if (sub instanceof GlycanRepeatModification) continue;
            if (sub.getSubstituent() instanceof CrossLinkedTemplate) continue;
            if (sub.getFirstPosition().getParentLinkages().size() > 1) continue;
            if (Double.compare(sub.getFirstPosition().getParentProbabilityLower(), 1.0D) != 0) continue;

            int position = sub.getFirstPosition().getParentLinkages().get(0);

            if (subMap.containsKey(position)) {
                ArrayList<Edge> subs = subMap.get(position);
                subs.add(childEdge);
                subMap.put(position, subs);
            } else {
                ArrayList<Edge> subs = new ArrayList<>();
                subs.add(childEdge);
                subMap.put(position, subs);
            }
        }

        return subMap;
    }

    public void optimizeSubstituent (Monosaccharide _mono) throws GlycanException {
        // Optimize linkage type
        for (Edge edge : _mono.getChildEdges()) {
            if (edge.getSubstituent() == null) continue;
            if (edge.getChild() != null) continue;

            Substituent sub = (Substituent) edge.getSubstituent();

            if (sub instanceof GlycanRepeatModification) continue;

            if (sub.getFirstPosition() != null && sub.getSecondPosition() == null) {
                // Simple substituent
                Linkage lin = edge.getGlycosidicLinkages().get(0);
                lin.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                lin.setParentLinkageType(LinkageType.DEOXY);

                sub.getFirstPosition().setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                sub.getFirstPosition().setParentLinkageType(LinkageType.DEOXY);

                // O-type substituent
                //if (SubstituentUtility.isOLinkedSubstituent(sub.getSubstituent())) {
                if (sub.getHeadAtom().equals("O")) {
                    lin.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                    lin.setParentLinkageType(LinkageType.H_AT_OH);

                    sub.getFirstPosition().setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                    sub.getFirstPosition().setParentLinkageType(LinkageType.H_AT_OH);
                }

                // With H_LOSE
                if (this.withH_LOSE(_mono, sub)) {
                    lin.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                    lin.setParentLinkageType(LinkageType.H_LOSE);

                    sub.getFirstPosition().setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                    sub.getFirstPosition().setParentLinkageType(LinkageType.H_LOSE);
                }
            }

            // Cyclic substituent
            if (sub.getFirstPosition() != null && sub.getSecondPosition() != null) {
                Linkage first = sub.getFirstPosition();
                Linkage second = sub.getSecondPosition();

                first.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                first.setParentLinkageType(LinkageType.H_AT_OH);

                second.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                second.setParentLinkageType(LinkageType.H_AT_OH);

                edge.getGlycosidicLinkages().get(0).setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                edge.getGlycosidicLinkages().get(0).setParentLinkageType(LinkageType.H_AT_OH);
            }
        }
    }

    public void optimizeGlycoSidicLinkage (Monosaccharide _mono) throws GlycanException {
        for (Edge edge : _mono.getChildEdges()) {
            if (edge.getChild() == null) continue;

            Substituent sub = (Substituent) edge.getSubstituent();

            // Optimize linkage type between glycosidic linkages
            if (edge.getChild() != null && sub == null) {
                for (Linkage lin : edge.getGlycosidicLinkages()) {
                    lin.setChildLinkageType(LinkageType.DEOXY);
                    lin.setParentLinkageType(LinkageType.H_AT_OH);
                }
            }

            if (sub == null) continue;

            // Optimize linkage of repeating unit
            if (isRepeating(edge)) {
                GlycanRepeatModification repMod = (GlycanRepeatModification) sub;

                for (Linkage lin : edge.getGlycosidicLinkages()) {
                    lin.setParentLinkageType(LinkageType.H_AT_OH);

                    if (repMod.getSubstituent() != null) {
                        lin.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                    } else {
                        lin.setChildLinkageType(LinkageType.DEOXY);
                    }
                }
            }

            // Optimize linkage type between cross-linked substituent
            // First linkage : donor side
            // Second linkage : acceptor side
            if (!isRepeating(edge) && isCrossLinkedSubstituent(edge)) {
                Linkage first = sub.getFirstPosition();
                Linkage second = sub.getSecondPosition();

                first.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
                first.setParentLinkageType(LinkageType.H_AT_OH);

                second.setChildLinkageType(LinkageType.H_AT_OH);
                second.setParentLinkageType(LinkageType.NONMONOSACCHARIDE);

                // Optimize linkage type between monosaccharides
                for (Linkage lin : edge.getGlycosidicLinkages()) {
                    lin.setChildLinkageType(LinkageType.H_AT_OH);
                    lin.setParentLinkageType(LinkageType.H_AT_OH);
                }
            }
        }

        return;
    }

    public void optimizeUndefinedUnit (GlycanUndefinedUnit _und) throws GlycanException {
        Edge connect = _und.getConnection();

        // Optimize acceptor side linkage
        // connection equal acceptor side edge of root node
        if (connect != null) {
            for (Linkage lin : connect.getGlycosidicLinkages()) {
                lin.setChildLinkageType(LinkageType.DEOXY);
                lin.setParentLinkageType(LinkageType.H_AT_OH);
            }
        }

        for (Node node : _und.getRootNodes()) {
            // Optimize donor side linkage type
            if (node instanceof Monosaccharide) {
                this.optimizeSubstituent((Monosaccharide) node);
                this.optimizeGlycoSidicLinkage((Monosaccharide) node);
            }
        }
    }

    public boolean withH_LOSE (Monosaccharide _mono, Substituent _sub) {
        if (_sub.getHeadAtom().equals("C")) return true;
        //if (_mono.getModifications().isEmpty()) return false;

        Linkage lin = _sub.getFirstPosition();
        int pos = lin.getParentLinkages().get(0);
        boolean ret = false;

        for (GlyCoModification gMod : _mono.getModifications()) {
            boolean isHLOSE = (gMod.getModificationTemplate().equals(ModificationTemplate.HLOSE_5) ||
                    gMod.getModificationTemplate().equals(ModificationTemplate.HLOSE_6) ||
                    gMod.getModificationTemplate().equals(ModificationTemplate.HLOSE_7) ||
                    gMod.getModificationTemplate().equals(ModificationTemplate.HLOSE_8) ||
                    gMod.getModificationTemplate().equals(ModificationTemplate.HLOSE_X));
            if (gMod.getPositionOne() == pos && isHLOSE) ret = true;
        }

        return ret;
    }

    public boolean isCrossLinkedSubstituent (Edge _edge) {
        if (_edge.getSubstituent() == null) return false;
        Substituent sub = (Substituent) _edge.getSubstituent();

        if (sub.getSubstituent() instanceof CrossLinkedTemplate) return true;
        return false;
    }

    public boolean isRepeating (Edge _edge) {
        if (_edge.getSubstituent() == null) return false;
        Substituent sub = (Substituent) _edge.getSubstituent();

        return (sub instanceof GlycanRepeatModification);
    }

    private void checkStatus (Node _node) {
        if (_node instanceof Monosaccharide) {
            Monosaccharide mono = (Monosaccharide) _node;
            for (Edge edge : mono.getChildEdges()) {
                Substituent sub = (Substituent) edge.getSubstituent();
                if (sub != null) {
                    if (sub.getFirstPosition() != null) {
                        System.out.println(sub.getFirstPosition().getParentLinkages() + " " + sub.getFirstPosition().getChildLinkages() + " " + sub.getFirstPosition().getParentLinkageType() + " " + sub.getFirstPosition().getChildLinkageType());
                    }
                    if (sub.getSecondPosition() != null) {
                        System.out.println(sub.getSecondPosition().getParentLinkages() + " " + sub.getSecondPosition().getChildLinkages() + " " + sub.getSecondPosition().getParentLinkageType() + " " + sub.getSecondPosition().getChildLinkageType());
                    }
                    System.out.println(sub.getSubstituent());
                }
            }
        }


    }
}