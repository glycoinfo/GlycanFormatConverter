package org.glycoinfo.GlycanFormatconverter.io.KCF;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.extended.IUPACNotationParser;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.BaseStereoIndex;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.MonosaccharideNotationAnalyzer;

import java.util.ArrayList;

/**
 * Created by e15d5605 on 2017/12/15.
 */
public class KCFNodeConverter {

    private KCFUtility kcfUtil;

    KCFNodeConverter (KCFUtility _kcfUtil) {
        kcfUtil = _kcfUtil;
    }

    public Node start (String _node) throws GlycanException, GlyCoImporterException, TrivialNameException {
        ArrayList<String> units = kcfUtil.splitNotation(_node);
        String notation = units.get(1);

        IUPACAglyconDescriptor currentAglycon = IUPACAglyconDescriptor.forNotation(notation);

        if (units.get(0).equals("1") && currentAglycon != null) return null;
        if (units.get(0).equals("2") && currentAglycon != null) {
            String parentNotation = kcfUtil.splitNotation(kcfUtil.getNodeByID("1")).get(1);
            IUPACAglyconDescriptor parentAglycon = IUPACAglyconDescriptor.forNotation(parentNotation);
            if (parentAglycon == null && !currentAglycon.equals(IUPACAglyconDescriptor.PHOSPHATE)) return null;
            if (parentAglycon != null) return null;
        }
        if (units.get(1).equals("*")) return null;
        if (isLinkageSubstituents(_node)) return null;

        Node node = makeSubstituent(_node);

        if (node == null) {
            KCFNotationToIUPACNotation kcf2iupac = new KCFNotationToIUPACNotation();
            IUPACNotationParser inp = new IUPACNotationParser();
            node = inp.parseMonosaccharide(kcf2iupac.start(notation));

            modifyMonosaccharide(node);
            modifyHeadAtom(node);
        }

        return node;
    }

    private Node makeSubstituent (String _notation) throws GlycanException {
        String unit = this.modifyNotation(kcfUtil.splitNotation(_notation).get(1));

        if (!isSubstituent(unit)) return null;

        Substituent ret = null;

        if (isBridge(_notation)) {
            return this.makeLinkedSubstituent(_notation);
        } else if (isBranches(_notation)) {
            return this.makeLinkedSubstituent(_notation);
        } else {
            return this.makeSubstituentNotation(BaseSubstituentTemplate.forIUPACNotationWithIgnore(unit));
        }
    }

    private Node makeLinkedSubstituent (String _notation) throws GlycanException {
        String unit = this.modifyNotation(kcfUtil.splitNotation(_notation).get(1));

        if (isPyrophosphate (_notation)) {
            if (isCrossLinkedSubstituent(_notation))
                return this.makeSubstituentNotation(BaseCrossLinkedTemplate.PYROPHOSPHATE);
            else
                return this.makeSubstituentNotation(BaseSubstituentTemplate.PYROPHOSPHATE);
        }
        if (isPhosphoEthanolamine(_notation)) {
            if (isCrossLinkedSubstituent(_notation))
                return this.makeSubstituentNotation(BaseCrossLinkedTemplate.PHOSPHO_ETHANOLAMINE);
            else
                return this.makeSubstituentNotation(BaseSubstituentTemplate.PHOSPHOETHANOLAMINE);
        }
        if (unit.equals("P")) {
            if (isCrossLinkedSubstituent(_notation))
                return this.makeSubstituentNotation(BaseCrossLinkedTemplate.PHOSPHATE);
            else
                return this.makeSubstituentNotation(BaseSubstituentTemplate.PHOSPHATE);
        }

        return null;
    }

    private boolean isSubstituent (String _notation) {
        BaseCrossLinkedTemplate crossT = BaseCrossLinkedTemplate.forIUPACNotationWithIgnore(_notation);
        BaseSubstituentTemplate subT = BaseSubstituentTemplate.forIUPACNotationWithIgnore(_notation);

        return (crossT != null || subT != null);
    }

    private Node modifyMonosaccharide (Node _node) throws GlycanException {

        Monosaccharide mono = (Monosaccharide) _node;

        if (mono.getStereos().size() > 1 || mono.getStereos().isEmpty()) return _node;

        BaseTypeDictionary baseDict = BaseTypeDictionary.forName(mono.getStereos().getFirst());
        String tempStereo = mono.getStereos().getFirst();
        StringBuilder skeletonCode = new StringBuilder(baseDict.getStereoCode());

        BaseStereoIndex bsi = BaseStereoIndex.forCode(baseDict.getCoreName());

        if (bsi.getSize() != mono.getSuperClass().getSize()) return _node;

        // Check modifications
        for (GlyCoModification gMod : mono.getModifications()) {
            if (gMod.getPositionOne() == mono.getAnomericPosition()) continue;
            if (gMod.getPositionOne() == mono.getSuperClass().getSize()) continue;

            skeletonCode.replace(gMod.getPositionOne() - 2, gMod.getPositionOne() - 1, "");
        }

        // define modified base type
        baseDict = BaseTypeDictionary.forStereoCode(skeletonCode.toString());
        String modifiedStereo = baseDict.getName();

        //
        mono.removeStereo(tempStereo);
        mono.addStereo(modifiedStereo);

        return _node;
    }

    private boolean isBridge(String _notation) {
        String currentID = kcfUtil.splitNotation(_notation).get(0);
        //String donorEdge = kcfUtil.extractEdgeByID(currentID, false);
        String donorEdge = kcfUtil.extractDonorEdgeByID(currentID);
        String acceptorEdge = kcfUtil.extractEdgeByID(currentID, true);

        return (!donorEdge.equals("") && !acceptorEdge.equals(""));
    }

    private boolean isBranches (String _notation) {
        String currentID = kcfUtil.splitNotation(_notation).get(0);
        ArrayList<String> acceptorEdge = kcfUtil.extractAcceptorEdgeByID(currentID);

        return (acceptorEdge.size() > 1);
    }

    private boolean isCrossLinkedSubstituent (String _notation) {
        String linkage = kcfUtil.getEdgeByID(kcfUtil.splitNotation(_notation).get(0), true);

        String id = kcfUtil.extractID(kcfUtil.splitNotation(linkage).get(1));

        //donor side
        String donorNode = kcfUtil.getNodeByID(id);

        //acceptor side
        String acceptorEdge = kcfUtil.getEdgeByID(kcfUtil.splitNotation(linkage).get(2), false);

        if (acceptorEdge.equals("")) return false;

        String acceptorNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(acceptorEdge).get(2));

        if (donorNode.equals("")) return false;

        if (!acceptorNode.equals("")) {
            if (!(MonosaccharideNotationAnalyzer.start(acceptorNode))) return false;
        }

        return (MonosaccharideNotationAnalyzer.start(kcfUtil.splitNotation(donorNode).get(1)));
    }

    private boolean isPyrophosphate (String _notation) {
        if (kcfUtil.splitNotation(_notation).get(1).equals("PP")) return true;

        String currentID = kcfUtil.splitNotation(_notation).get(0);
        String childSideNotation = kcfUtil.extractEdgeByID(currentID, true);

        if (!childSideNotation.equals("")) {
            String childNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(childSideNotation).get(1));
            String parentNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(childSideNotation).get(2));

            if (childNode.equals("") || parentNode.equals("")) return false;

            BaseSubstituentTemplate parentT =
                    BaseSubstituentTemplate.forIUPACNotationWithIgnore(kcfUtil.splitNotation(parentNode).get(1));
            BaseSubstituentTemplate childT =
                    BaseSubstituentTemplate.forIUPACNotationWithIgnore(kcfUtil.splitNotation(childNode).get(1));

            if (parentT == null || childT == null) return false;
            if (parentT.equals(BaseSubstituentTemplate.PHOSPHATE) && childT.equals(BaseSubstituentTemplate.PHOSPHATE)) return true;
        }

        return false;
    }

    private boolean isPhosphoEthanolamine (String _notation) {
        String currentID = kcfUtil.splitNotation(_notation).get(0);

        //check for donor side
        if (isPhosphoEthanolamineForDonorSide(currentID)) return true;

        //check for acceptor side
        if (isPhosphoEthanolamineForAcceptorSide(currentID)) return true;

        return false;
    }

    private boolean isPhosphoEthanolamineForDonorSide (String _currentID) {
        ArrayList<String> donorEdges = kcfUtil.extractAcceptorEdgeByID(_currentID);

        boolean ret = false;
        for (String edge : donorEdges) {
            String donorNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(edge).get(1));
            String acceptorNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(edge).get(2));
            if (donorNode.equals("") || acceptorNode.equals("")) continue;

            if (kcfUtil.splitNotation(donorNode).get(1).equals("EtN") &&
                    kcfUtil.splitNotation(acceptorNode).get(1).equals("P")) {
                ret = true;
                break;
            }
        }

        return ret;
    }

    private boolean isPhosphoEthanolamineForAcceptorSide (String _currentID) {
        String acceptorEdge = kcfUtil.getEdgeByID(_currentID, false);

        if (acceptorEdge.equals("")) return false;

        String donorNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(acceptorEdge).get(1));
        String acceptorNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(acceptorEdge).get(2));

        if (donorNode.equals("") || acceptorNode.equals("")) return false;

        donorNode = kcfUtil.splitNotation(donorNode).get(1);
        acceptorNode = kcfUtil.splitNotation(acceptorNode).get(1);

        return ((donorNode.equals("P") && acceptorNode.equals("EtN")) ||
                (donorNode.equals("EtN") && acceptorNode.equals("P")));
    }
    
    private boolean isLinkageSubstituents (String _notation) {
        String currentID = kcfUtil.splitNotation(_notation).get(0);
        String parentSideNotation = kcfUtil.extractDonorEdgeByID(currentID);//extractEdgeByID(currentID, false);

        if (parentSideNotation.equals("")) return false;

        String childID = kcfUtil.splitNotation(parentSideNotation).get(1);
        String parentID = kcfUtil.splitNotation(parentSideNotation).get(2);

        if (childID.contains(":") || parentID.contains(":")) return false;

        //Phospho-ethanolamine
        if (kcfUtil.splitNotation(kcfUtil.getNodeByID(childID)).get(1).equals("EtN") &&
        			kcfUtil.splitNotation(kcfUtil.getNodeByID(parentID)).get(1).equals("P")) return true;

        //Di-phosphate
        if (kcfUtil.splitNotation(kcfUtil.getNodeByID(childID)).get(1).equals("P") &&
                kcfUtil.splitNotation(kcfUtil.getNodeByID(parentID)).get(1).equals("P")) return true;
        //if (isSubstituent(kcfUtil.splitNotation(kcfUtil.getNodeByID(childID)).get(1)) &&
        //        isSubstituent(kcfUtil.splitNotation(kcfUtil.getNodeByID(parentID)).get(1))) return true;

        return false;
    }

    private Node makeSubstituentNotation (SubstituentInterface _subInf) throws GlycanException {
        Substituent ret = new Substituent(_subInf);

        // define first linkage
        ret.setFirstPosition(new Linkage());

        // define second linkage
        if (ret.getSubstituent() instanceof BaseCrossLinkedTemplate) {
            ret.setSecondPosition(new Linkage());
        }

        if (!SubstituentUtility.isNLinkedSubstituent(ret)) {
            ret.setHeadAtom("O");
        }

        return ret;
    }

    private String modifyNotation (String _notation) {
        // modify methyl
        //unit = unit.equalsIgnoreCase("Me") ? "O" + unit : unit;
        if (_notation.equals("PP")) _notation = "PyrP";
        if (_notation.equals("EtN")) _notation = BaseSubstituentTemplate.ETHANOLAMINE.getIUPACnotation();
        if (_notation.equals("EtnP")) _notation = BaseSubstituentTemplate.PHOSPHOETHANOLAMINE.getIUPACnotation();

        return _notation;
    }

    private void modifyHeadAtom (Node _node) {
        for (Edge donorEdge : _node.getChildEdges()) {
            if (donorEdge.getSubstituent() == null) continue;
            Substituent sub = (Substituent) donorEdge.getSubstituent();

            if (sub instanceof GlycanRepeatModification) continue;
        }

        return;
    }
}
