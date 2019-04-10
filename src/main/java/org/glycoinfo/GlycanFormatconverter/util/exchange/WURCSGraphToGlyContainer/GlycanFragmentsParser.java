package org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;

import org.glycoinfo.GlycanFormatconverter.Glycan.BaseCrossLinkedTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.graph.comparator.WURCSEdgeComparator;
import org.glycoinfo.WURCSFramework.wurcs.graph.Backbone;
import org.glycoinfo.WURCSFramework.wurcs.graph.Modification;
import org.glycoinfo.WURCSFramework.wurcs.graph.ModificationAlternative;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by e15d5605 on 2019/03/05.
 */
public class GlycanFragmentsParser {

    private Backbone root;
    private ArrayList<Backbone> antennae;
    private ArrayList<ModificationAlternative> undefinedLinkages;
    private ArrayList<ModificationAlternative> undefinedSubstituents;


    public GlycanFragmentsParser (Backbone _root) {
        this.root = _root;
        this.antennae = new ArrayList<>();
        this.undefinedLinkages = new ArrayList<>();
        this.undefinedSubstituents = new ArrayList<>();
    }

    public ArrayList<Backbone> getAntennae () {
        return this.antennae;
    }

    public ArrayList<ModificationAlternative> getUndefinedLinkages () {
        return this.undefinedLinkages;
    }

    public ArrayList<ModificationAlternative> getUndefinedSubstituents () {
        return this.undefinedSubstituents;
    }

    /**
     * Analyze the Backbone localized to the root of glycan fragments and store it in the array.
     * @param _backbones
     * @throws GlycanException
     */
    public void start (LinkedList<Backbone> _backbones) throws GlycanException, WURCSFormatException {
        for (Backbone bb : _backbones) {
            this.parseFragments(bb);
        }

        return;
    }

    private void parseFragments (Backbone _backbone) throws GlycanException, WURCSFormatException {
        if (!_backbone.isRoot()) return;

        // Extract root of glycan fragments
        for (WURCSEdge cEdge : _backbone.getChildEdges()) {

            if (!(cEdge.getNextComponent() instanceof ModificationAlternative)) continue;

			/* 2018/09/25 Masaaki added */
            this.extractUndefinedLinkages((ModificationAlternative)cEdge.getNextComponent());
            this.extractUndefinedSubstituents((ModificationAlternative)cEdge.getNextComponent());
			/**/

            for (WURCSEdge cpEdge : cEdge.getNextComponent().getParentEdges()) {
                if (!cpEdge.getBackbone().isRoot()) continue;
                if (isCrossLinkedSubstituent(cpEdge.getModification())) continue;
                if (root.equals(cpEdge.getBackbone())) {
                    if (!cpEdge.getModification().getMAPCode().equals("") && (cEdge.getNextComponent().getParentEdges().size()) > 1) {
                        antennae.add(cpEdge.getBackbone());
                    }
                } else {
                    if (cEdge.getNextComponent().getParentEdges().size() - 2 > 0 && !antennae.contains(cpEdge.getBackbone())) {
                        antennae.add(cpEdge.getBackbone());
                    }
                }
            }
        }

        return;
    }

    private boolean isCrossLinkedSubstituent (Modification _mod) throws GlycanException, WURCSFormatException {
        if (_mod.getMAPCode().equals("")) return false;
        return (SubstituentUtility.MAPToInterface(_mod.getMAPCode()) instanceof BaseCrossLinkedTemplate);
        //return (SubstituentUtility.MAPToInterface(_mod.getMAPCode()) instanceof CrossLinkedTemplate);
    }

    /**
     * Extract undefined linkages like following:
     * <p> a?|b?|c?}-{a?|b?|c? </p>
     * This must have the same linkages between lead in and out linkages and no substituent.
     * The linkages must be connected to all of the monosaccharides in the glycan.
     * @author Masaaki Matsubara
     * @param t_modAlt
     * @throws GlycanException undefined linkage with substituent cannnot be handled.
     */
    private void extractUndefinedLinkages(ModificationAlternative t_modAlt) throws GlycanException {
        if ( this.undefinedLinkages.contains(t_modAlt) )
            return;
        if ( t_modAlt.getLeadInEdges().size() != t_modAlt.getLeadOutEdges().size())
            return;
		/* Compare lead in and lead out linkages on the ModificationAlternative  */
        LinkedList<WURCSEdge> t_lInEdges  = t_modAlt.getLeadInEdges();
        LinkedList<WURCSEdge> t_lOutEdges = t_modAlt.getLeadOutEdges();
        WURCSEdgeComparator t_compEdges = new WURCSEdgeComparator();
        Collections.sort(t_lInEdges,  t_compEdges);
        Collections.sort(t_lOutEdges, t_compEdges);
        int nEdges = t_lInEdges.size();
        for ( int i=0; i<nEdges; i++) {
            WURCSEdge t_edgeIn  = t_lInEdges.get(i);
            WURCSEdge t_edgeOut = t_lOutEdges.get(i);
            int t_iComp = t_compEdges.compare(t_edgeIn, t_edgeOut);
            if ( t_iComp != 0 )
                return;
        }
        if ( !t_modAlt.canOmitMAP() )
            throw new GlycanException("Undefined linkage with substituent cannot be handled.");
        this.undefinedLinkages.add(t_modAlt);
    }

    /**
     * Extract undefined linkages like following:
     * <p> a?|b?|c?}*OCC/3=O </p>
     * This must have lead in linkages, substituent and no lead out linkages.
     * The linkages must be connected to all of the monosaccharides in the glycan.
     * @author Masaaki Matsubara
     * @param t_modAlt
     * @throws GlycanException
     */
    private void extractUndefinedSubstituents(ModificationAlternative t_modAlt) throws GlycanException {
        if ( this.undefinedSubstituents.contains(t_modAlt) )
            return;
        if ( t_modAlt.getLeadInEdges().isEmpty() || !t_modAlt.getLeadOutEdges().isEmpty() )
            return;
        if ( t_modAlt.getMAPCode().isEmpty() )
            return;
        this.undefinedSubstituents.add(t_modAlt);
    }
}
