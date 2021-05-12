package org.glycoinfo.GlycanFormatconverter.util;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by e15d5605 on 2019/03/05.
 */
public class GlyContainerOptimizer {

    public GlyContainer start (GlyContainer _glyCo) throws GlycanException {
        for (Node node : _glyCo.getAllNodes()) {
            // Optimize monoasccharide
            //this.optimizeMonosaccharide((Monosaccharide) node);

            // Merge duplicated substituents
            this.optimizeDuplicateSubstituent((Monosaccharide) node);

            // Optimize state of un saturation
            //this.optimizeUnsaturation((Monosaccharide) node);

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
                BaseSubstituentTemplate bsubT = this.convertOtoNsubstituent(sub2);
                sub2.setTemplate(bsubT);
                sub2.setHeadAtom("N");

                _mono.removeChildEdge(edges.get(0));
            }
            if (sub2.getSubstituent().equals(BaseSubstituentTemplate.AMINE)) {
                BaseSubstituentTemplate bsubT = this.convertOtoNsubstituent(sub1);
                sub1.setTemplate(bsubT);
                sub2.setHeadAtom("N");

                _mono.removeChildEdge(edges.get(1));
            }
        }
    }

    private HashMap<Integer, ArrayList<Edge>> extractDupeSubstituents (Node _node) {
        HashMap<Integer, ArrayList<Edge>> subMap = new HashMap<>();
        for (Edge childEdge : _node.getChildEdges()) {
            if (childEdge.getSubstituent() == null) continue;
            Substituent sub = (Substituent) childEdge.getSubstituent();

            if (sub instanceof GlycanRepeatModification) continue;
            if (sub.getSubstituent() instanceof BaseCrossLinkedTemplate) continue;
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

            // Optimize linkage atoms
            this.optimizeSubstituentAtoms(sub);

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
            if (this.isCrossLinkedSubstituent(edge)) {
                this.optimizeCrossLinkedSubstituent(edge, sub);
            }
        }
    }

    public void optimizeSubstituentFragmentLinkageType (Substituent sub, Edge _coreSide) throws GlycanException {
        // Optimize linkage atoms
        this.optimizeSubstituentAtoms(sub);

        if (sub.getFirstPosition() != null && sub.getSecondPosition() == null) {
            // Simple substituent
            Linkage lin = _coreSide.getGlycosidicLinkages().get(0);
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
        }
    }

    public void optimizeCrossLinkedSubstituent (Edge _edge, Substituent _sub) throws GlycanException {
        // Optimize linkage type between cross-linked substituent
        //if (!isRepeating(_edge) && isCrossLinkedSubstituent(_edge)) {
        Linkage first = _sub.getFirstPosition();
        Linkage second = _sub.getSecondPosition();

        first.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
        second.setParentLinkageType(LinkageType.NONMONOSACCHARIDE);

        if (_edge.getChild() != null) {
            // HEAD is parent, Tail is child
            // First is parent, second is child
            // (head/first)*NCCOP^X*(tail/second)/6O/6=O
            // 1 is H_AT_OH 2 is DEOXY
            if (second.getChildLinkages().contains(1) || second.getChildLinkages().isEmpty()) {
                second.setChildLinkageType(LinkageType.H_AT_OH);
            } else {
                second.setChildLinkageType(LinkageType.DEOXY);
            }

            if (first.getChildLinkages().contains(1) || first.getChildLinkages().isEmpty()) {
                first.setParentLinkageType(LinkageType.H_AT_OH);
            } else {
                first.setParentLinkageType(LinkageType.DEOXY);
            }

            // Check phospho-ethanol amine
            if (_sub.getSubstituent().equals(BaseCrossLinkedTemplate.PHOSPHOETHANOLAMINE)) {
                first.setParentLinkageType(LinkageType.H_AT_OH);
                second.setChildLinkageType(LinkageType.H_AT_OH);
            }

            if (_sub.getSubstituent().equals(BaseCrossLinkedTemplate.AMINO)) {
                first.setParentLinkageType(LinkageType.DEOXY);
                second.setChildLinkageType(LinkageType.DEOXY);
            }
        } else {
            if (_sub.getSubstituent().equals(BaseCrossLinkedTemplate.AMINO)) {
                if (!_sub.getHeadAtom().equals("O")) {
                    first.setParentLinkageType(LinkageType.DEOXY);
                } else {
                    first.setParentLinkageType(LinkageType.H_AT_OH);
                }
                if (!_sub.getTailAtom().equals("O")) {
                    second.setChildLinkageType(LinkageType.DEOXY);
                } else {
                    second.setChildLinkageType(LinkageType.H_AT_OH);
                }
            } else {
                first.setParentLinkageType(LinkageType.H_AT_OH);
                second.setChildLinkageType(LinkageType.H_AT_OH);
            }
        }

        // Optimize linkage type between monosaccharides
        for (Linkage lin : _edge.getGlycosidicLinkages()) {
            lin.setChildLinkageType(first.getParentLinkageType());
            lin.setParentLinkageType(second.getChildLinkageType());
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

            //Optimize head and tail atoms
            this.optimizeSubstituentAtoms(sub);

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
            if (this.isCrossLinkedSubstituent(edge)) {
                this.optimizeCrossLinkedSubstituent(edge, sub);
            }
        }
    }

    public void optimizeUndefinedUnit (GlycanUndefinedUnit _und) throws GlycanException {
        Edge connect = _und.getConnection();
        Node root = _und.getRootNodes().get(0);

        // Optimize acceptor side linkage
        // connection equal acceptor side edge of root node
        if (connect != null) {
            if (root instanceof Monosaccharide) {
                for (Linkage lin : connect.getGlycosidicLinkages()) {
                    lin.setChildLinkageType(LinkageType.DEOXY);
                    lin.setParentLinkageType(LinkageType.H_AT_OH);
                }
            }
            if (root instanceof Substituent) {
                this.optimizeSubstituentFragmentLinkageType((Substituent) root, connect);
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

    public void optimizeSubstituentAtoms (Substituent _sub) {
        SubstituentInterface subFace = _sub.getSubstituent();

        if (subFace == null) return;
        if (_sub.getHeadAtom() == null) _sub.setHeadAtom("");
        if (_sub.getTailAtom() == null) _sub.setTailAtom("");

        if (subFace instanceof BaseSubstituentTemplate) {
            BaseSubstituentTemplate baseSub = (BaseSubstituentTemplate) subFace;

            if (baseSub.getMAP().startsWith("*O")) {
                _sub.setHeadAtom("O");
            }
            if (baseSub.getMAP().startsWith("*N")) {
                _sub.setHeadAtom("N");
            }
            if (baseSub.equals(BaseSubstituentTemplate.CFORMYL) || baseSub.equals(BaseSubstituentTemplate.CMETHYL)) {
                _sub.setHeadAtom("C");
            }
        }

        if (subFace instanceof BaseCrossLinkedTemplate) {
            BaseCrossLinkedTemplate baseCross = (BaseCrossLinkedTemplate) subFace;
            if (baseCross.getMAP().startsWith("*O")) {
                _sub.setHeadAtom("O");
            }
            if (baseCross.getMAP().startsWith("*N")) {
                _sub.setHeadAtom("N");
            }
        }

        //System.out.println(_sub.getSubstituent() + " " + _sub.getHeadAtom());
    }

    public boolean withH_LOSE (Monosaccharide _mono, Substituent _sub) {
        if (_sub.getHeadAtom().equals("O")) return false;
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

        if (sub.getSubstituent() == null) return false;

        if (sub.getSubstituent().equals(BaseCrossLinkedTemplate.ANHYDRO)) return false;

        return (sub.getSubstituent() instanceof BaseCrossLinkedTemplate);
   }

    public boolean isRepeating (Edge _edge) {
        if (_edge.getSubstituent() == null) return false;
        Substituent sub = (Substituent) _edge.getSubstituent();

        return (sub instanceof GlycanRepeatModification);
    }

    private void optimizeUnsaturation (Monosaccharide _mono) throws GlycanException {
        HashMap<Integer, List<GlyCoModification>> modList = new HashMap<>();
        for (GlyCoModification gMod : _mono.getModifications()) {
            if (!modList.containsKey(gMod.getPositionOne())) {
                List<GlyCoModification> list = new ArrayList<>();
                list.add(gMod);
                modList.put(gMod.getPositionOne(), list);
            } else {
                modList.get(gMod.getPositionOne()).add(gMod);
            }
        }

        for (Integer key :modList.keySet()) {
            List<GlyCoModification> list = modList.get(key);
            if (list.size() != 1) continue;
            if (list.get(0).getModificationTemplate().equals(ModificationTemplate.UNSATURATION_FL)) {
                _mono.removeModification(list.get(0));
                GlyCoModification gMod = new GlyCoModification(ModificationTemplate.UNSATURATION_FU, key);
                _mono.addModification(gMod);
            }
            if (list.get(0).getModificationTemplate().equals(ModificationTemplate.UNSATURATION_EL)) {
                _mono.removeModification(list.get(0));
                GlyCoModification gMod = new GlyCoModification(ModificationTemplate.UNSATURATION_EU, key);
                _mono.addModification(gMod);
            }
            if (list.get(0).getModificationTemplate().equals(ModificationTemplate.UNSATURATION_ZL)) {
                _mono.removeModification(list.get(0));
                GlyCoModification gMod = new GlyCoModification(ModificationTemplate.UNSATURATION_ZU, key);
                _mono.addModification(gMod);
            }
        }
    }

    private boolean hasUnsaturationWithDeoxy (List<GlyCoModification> _gModList) {
        boolean haveDeoxy = false;
        boolean haveUnsaturation = false;
        for (GlyCoModification gMod : _gModList) {
            if (gMod.getModificationTemplate().equals(ModificationTemplate.DEOXY)) {
                haveDeoxy = true;
            }
            if (gMod.getModificationTemplate().equals(ModificationTemplate.UNSATURATION_FU) ||
                    gMod.getModificationTemplate().equals(ModificationTemplate.UNSATURATION_ZU) ||
                    gMod.getModificationTemplate().equals(ModificationTemplate.UNSATURATION_EU)) {
                haveUnsaturation = true;
            }
        }
        return (haveDeoxy && haveUnsaturation);
    }

    private BaseSubstituentTemplate convertOtoNsubstituent (Substituent _sub) throws GlycanException {
        String oldNotation = _sub.getSubstituent().getIUPACnotation();

        if (_sub.getSubstituent().equals(BaseSubstituentTemplate.ACYL)) {
            throw new GlycanException("GlycanFormatConverter can not support N linked acyl group.");
        }

        if (_sub.getSubstituent().equals(BaseSubstituentTemplate.ETHANOL)) {
            return BaseSubstituentTemplate.ETHANOLAMINE;
        } else {
            return BaseSubstituentTemplate.forIUPACNotationWithIgnore("N" + oldNotation);
        }
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

                    System.out.println("head atom : " + sub.getHeadAtom() + "/ tail atom : " + sub.getTailAtom());
                }
            }
        }
    }
}
