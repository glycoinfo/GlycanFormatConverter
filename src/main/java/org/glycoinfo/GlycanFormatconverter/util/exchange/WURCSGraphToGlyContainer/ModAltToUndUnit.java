package org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
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
    }

    private void parseFragments (Backbone _backbone) throws GlycanException {
        if (_backbone.getChildEdges().isEmpty()) return;

        // Extract root of glycan fragments
        for (WURCSEdge cEdge : _backbone.getChildEdges()) {
            if (!(cEdge.getNextComponent() instanceof ModificationAlternative)) continue;

			ModificationAlternative modAlt = (ModificationAlternative) cEdge.getNextComponent();

			/* 2018/09/25 Masaaki added */
            this.extractUndefinedLinkages(modAlt);
            this.extractUndefinedSubstituents(modAlt);
			/**/

            if (!this.undefinedLinkages.contains(modAlt) &&
                    !this.undefinedSubstituents.contains(modAlt) &&
                    !this.antennae.contains(modAlt)) this.antennae.add(modAlt);
        }
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

        //define monosaccharide compositons
        this.convertCompositon();

        //define monosaccharide fragment(s)
        this.convertMonosacchrideFragment();

        //define substituent fragment(s)
        this.convertSubstituentFragment();

        if ((glyCo.getUndefinedUnit().size() + glyCo.getUndefinedUnitsForSubstituent().size()) !=
                (this.undefinedSubstituents.size() + this.undefinedLinkages.size() + this.antennae.size())) {
            throw new GlycanException ("Parse fragment did not correctly performed.");
        }
    }

    private void convertCompositon () throws GlycanException {
        if (!this.getUndefinedLinkages().isEmpty()) {
            throw new GlycanException("Monosaccharide composition with linkages can not support.");
        }

        for (ModificationAlternative modComp : this.undefinedLinkages) {
            GlycanUndefinedUnit und = new GlycanUndefinedUnit();

            Backbone currentBackbone = null;

            //extract donor side WURCSEdge
            WURCSEdge donorSideEdge = extractDonorSideWURCSEdge(modComp);

            for (WURCSEdge inEdge : modComp.getLeadInEdges()) {
                if (currentBackbone == null) {
                    currentBackbone = inEdge.getBackbone();
                    und.addNode(this.backbone2Node.get(inEdge.getBackbone()));
                }

                Node compNode = this.backbone2Node.get(currentBackbone);
                Edge edgeForOtherSide = this.parseLinkagePosition(inEdge, donorSideEdge, compNode);
                und.addConnection(edgeForOtherSide);
                und.setConnection(edgeForOtherSide);
                und.addParentNode(this.backbone2Node.get(inEdge.getBackbone()));
                compNode.addParentEdge(edgeForOtherSide);
            }
            glyCo.addGlycanUndefinedUnit(und);
        }
    }

    private void convertMonosacchrideFragment () throws GlycanException {
        for (ModificationAlternative modAltNode : this.antennae) {
            GlycanUndefinedUnit und = new GlycanUndefinedUnit();

            //define fragment node
            Node fragRoot = this.backbone2Node.get(this.extractWURCSEdge(modAltNode).getBackbone());

            //extract donor side WURCSEdge
            WURCSEdge donorSideEdge = extractDonorSideWURCSEdge(modAltNode);

            for (WURCSEdge inEdge : modAltNode.getLeadInEdges()) {
                //extract position for acceptor and donor side
                Edge edgeForAcceptor = this.parseLinkagePosition(inEdge, donorSideEdge, fragRoot);
                fragRoot.addParentEdge(edgeForAcceptor);

                und.addConnection(edgeForAcceptor);
                und.setConnection(edgeForAcceptor);
                und.addParentNode(this.backbone2Node.get(inEdge.getBackbone()));
            }

            und.addNode(fragRoot);

            glyCo.addGlycanUndefinedUnit(und);
        }
    }

    private void convertSubstituentFragment () throws GlycanException, WURCSFormatException {
        for (ModificationAlternative modAltSub : this.undefinedSubstituents) {
            GlycanUndefinedUnit und = new GlycanUndefinedUnit();

            //define fragment node
            Substituent fragRoot = SubstituentUtility.MAPToSubstituent(modAltSub);

            //extract donor side WURCSEdge
            WURCSEdge donorSideEdge = extractDonorSideWURCSEdge(modAltSub);

            for (WURCSEdge inEdge : modAltSub.getLeadInEdges()) {
                //extract position for acceptor and donor side
                Edge edgeForAcceptor = this.parseLinkagePosition(inEdge, donorSideEdge, fragRoot);
                fragRoot.addParentEdge(edgeForAcceptor);

                und.addConnection(edgeForAcceptor);
                und.setConnection(edgeForAcceptor);
                und.addParentNode(this.backbone2Node.get(inEdge.getBackbone()));
            }

            fragRoot.setFirstPosition(und.getConnection().getGlycosidicLinkages().get(0));
            //fragRoot.setSecondPosition(new Linkage());
            und.addNode(fragRoot);

            glyCo.addGlycanUndefinedUnit(und);
        }
    }

    private Edge parseLinkagePosition (WURCSEdge _inEdge, WURCSEdge _donorEdge, Node _fragRoot) throws GlycanException {
        Edge ret = new Edge();

        Linkage linkage = new Linkage();
        ArrayList<Integer> acceptorPos = new ArrayList<>();
        ArrayList<Integer> donorPos = new ArrayList<>();

        for (LinkagePosition lp : _inEdge.getLinkages()) {
            acceptorPos.add(lp.getBackbonePosition());

            if (_fragRoot instanceof Substituent) {
                donorPos.add(lp.getModificationPosition());
            } else {
                donorPos.add(_donorEdge.getLinkages().getFirst().getBackbonePosition());
            }

            linkage.setParentLinkages(acceptorPos);
            linkage.setChildLinkages(donorPos);

            linkage.setProbabilityLower(1.0d);
            linkage.setProbabilityUpper(1.0d);
        }

        ret.addGlycosidicLinkage(linkage);
        if (_fragRoot instanceof Substituent) {
            ret.setSubstituent(_fragRoot);
        } else {
            ret.setChild(_fragRoot);
        }
        ret.setParent(this.backbone2Node.get(_inEdge.getBackbone()));

        return ret;
    }

    private WURCSEdge extractWURCSEdge (ModificationAlternative _modAlt) {
        WURCSEdge ret = null;
        for (WURCSEdge edge : _modAlt.getEdges()) {
            if (_modAlt.getLeadInEdges().contains(edge)) continue;
            ret = edge;
        }
        return ret;
    }

    private ArrayList<WURCSEdge> extractWURCSEdges (ModificationAlternative _modAlt) {
        ArrayList<WURCSEdge> ret = new ArrayList<>();
        for (WURCSEdge edge : _modAlt.getEdges()) {
            if (_modAlt.getLeadInEdges().contains(edge)) continue;
            ret.add(edge);
        }

        return ret;
    }

    private WURCSEdge extractDonorSideWURCSEdge (ModificationAlternative _modAlt) {
        WURCSEdge ret = null;
        for (WURCSEdge wurcsEdge : _modAlt.getEdges()) {
            if (_modAlt.getLeadInEdges().contains(wurcsEdge)) continue;
            ret = wurcsEdge;
        }
        return ret;
    }
}
