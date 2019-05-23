package org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.graph.comparator.WURCSEdgeComparator;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by e15d5605 on 2019/03/05.
 */
public class ModAltToUndUnit {

    private ArrayList<ModificationAlternative> antennae;
    private ArrayList<ModificationAlternative> undefinedLinkages;
    private ArrayList<ModificationAlternative> undefinedSubstituents;

    private GlyContainer glyCo;
    private HashMap<WURCSComponent, Node> backbone2Node;

    public ModAltToUndUnit(GlyContainer _glyco, HashMap<WURCSComponent, Node> _backbone2Node) {
        this.antennae = new ArrayList<>();
        this.undefinedLinkages = new ArrayList<>();
        this.undefinedSubstituents = new ArrayList<>();

        this.glyCo = _glyco;
        this.backbone2Node = _backbone2Node;
    }

    public GlyContainer getGlycan () { return this.glyCo; }

    public ArrayList<ModificationAlternative> getAntennae () {
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

        this.backboneToUndefinedUnit();

        return;
    }

    private void parseFragments (Backbone _backbone) throws GlycanException, WURCSFormatException {
        if (_backbone.getChildEdges().isEmpty()) return;

        // Extract root of glycan fragments
        for (WURCSEdge cEdge : _backbone.getChildEdges()) {
            if (!(cEdge.getNextComponent() instanceof ModificationAlternative)) continue;

			ModificationAlternative modAlt = (ModificationAlternative) cEdge.getNextComponent();

			/* 2018/09/25 Masaaki added */
            this.extractUndefinedLinkages(modAlt);
            this.extractUndefinedSubstituents(modAlt);
			/**/

			if (!this.antennae.contains(modAlt)) this.antennae.add(modAlt);
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

    private void backboneToUndefinedUnit () throws GlycanException, WURCSFormatException {
        for (ModificationAlternative modAlt : this.antennae) {
            GlycanUndefinedUnit und = new GlycanUndefinedUnit();

            for (WURCSEdge inEdge : modAlt.getLeadInEdges()) {
                Node fragRoot;

                // extract root node
                if (!modAlt.getMAPCode().equals("")) {
                    fragRoot = SubstituentUtility.MAPToSubstituent(modAlt);
                    und.addNode(fragRoot);
                } else {
                    fragRoot = this.backbone2Node.get(this.extractFragmentRoot(inEdge));
                    und.addNode(fragRoot);
                }

                // extract core side node(s)
                for (WURCSEdge acceptorWE : inEdge.getNextComponent().getParentEdges()) {
                    Node coreNode = this.backbone2Node.get(acceptorWE.getBackbone());
                    und.addParentNode(coreNode);
                }

                // define linkage
                Edge acceptorEdge = new Edge();
                Linkage linkage = new Linkage();
                ArrayList<Integer> acceptorPos = new ArrayList<>();
                ArrayList<Integer> donorPos = new ArrayList<>();

                if (modAlt.getMAPCode().equals("")) {
                    Backbone fragBB = this.extractFragmentRoot(inEdge);
                    fragRoot = backbone2Node.get(fragBB);
                    fragRoot.addParentEdge(acceptorEdge);

                    // donor (fragment root)
                    acceptorEdge.setChild(fragRoot);

                    // position
                    donorPos.add(fragBB.getAnomericPosition());
                } else {
                    fragRoot = und.getNodes().get(0);
                    fragRoot.addParentEdge(acceptorEdge);

                    acceptorEdge.setSubstituent(fragRoot);

                    donorPos.add(0);
                }

                for (LinkagePosition lp : inEdge.getLinkages()) {
                    acceptorPos.add(lp.getBackbonePosition());
                }

                linkage.setParentLinkages(acceptorPos);
                linkage.setChildLinkages(donorPos);
                acceptorEdge.addGlycosidicLinkage(linkage);
                und.setConnection(acceptorEdge);

                break;
            }

            glyCo.addGlycanUndefinedUnit(und);
        }

        if (glyCo.getUndefinedUnit().size() != antennae.size())
            throw new GlycanException ("Parse fragment did not correctly performed.");

        return;
    }

    private Backbone extractFragmentRoot (WURCSEdge _inEdge) {
        if (!_inEdge.getNextComponent().getChildEdges().isEmpty()) {
            return _inEdge.getNextComponent().getChildEdges().get(0).getBackbone();
        }

        for (WURCSEdge acceptorEdge : _inEdge.getNextComponent().getParentEdges()) {
            if (!acceptorEdge.getBackbone().isRoot()) continue;
            int numGlycosidic = 0;

            for (WURCSEdge ce : acceptorEdge.getBackbone().getChildEdges()) {
                if (ce.getNextComponent() instanceof ModificationAlternative) continue;
                if (ce.getNextComponent() instanceof Modification) {
                    Modification mod = (Modification) ce.getNextComponent();
                    if (mod.isRing()) continue;
                    if (!mod.getMAPCode().equals("")) continue;
                    if (mod.isGlycosidic()) numGlycosidic++;
                }
            }
            if (numGlycosidic == 0) return acceptorEdge.getBackbone();
        }

        return null;
    }
}
