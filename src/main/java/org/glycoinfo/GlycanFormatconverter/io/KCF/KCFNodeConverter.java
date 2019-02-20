package org.glycoinfo.GlycanFormatconverter.io.KCF;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationParser;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.BaseStereoIndex;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.KCFNotationToIUPACNotation;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;

import java.util.ArrayList;

/**
 * Created by e15d5605 on 2017/12/15.
 */
public class KCFNodeConverter {

    private KCFUtility kcfUtil;

    KCFNodeConverter (KCFUtility _kcfUtil) {
        kcfUtil = _kcfUtil;
    }

    public Node start (String _node) throws GlycanException, GlyCoImporterException, ConverterExchangeException {
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
        }
        
        return node;
    }


    private Node makeSubstituent (String _notation) throws GlycanException {
        String unit = kcfUtil.splitNotation(_notation).get(1);

        /* modify methyl */
        unit = unit.equalsIgnoreCase("Me") ? "O" + unit : unit;
        unit = unit.equals("PP") ? "PyrP" : unit;
        unit = unit.equals("EtN") ? SubstituentTemplate.ETHANOLAMINE.getIUPACnotation() : unit;
        unit = unit.equals("EtnP") ? SubstituentTemplate.PHOSPHOETHANOLAMINE.getIUPACnotation() : unit;
        
        if (!isSubstituent(unit)) return null;

        if (haveChild(_notation)) {
        		if (isPyrophosphate (_notation)) {
        			 return modifyLinkageType(new Substituent(SubstituentTemplate.PYROPHOSPHATE));
        		}
        		if (isPhosphoEthanolamine(_notation)) {
        			 return modifyLinkageType(new Substituent(SubstituentTemplate.PHOSPHOETHANOLAMINE));
        		}
            return modifyLinkageType((Substituent) makeCrossLinkedSubstituent(unit));
        } else {
            return modifyLinkageType((Substituent) makeSimpleSubstituent(unit));
        }
    }

    private boolean isSubstituent (String _notation) {
        CrossLinkedTemplate crossT = CrossLinkedTemplate.forIUPACNotationWithIgnore(_notation);
        SubstituentTemplate subT = SubstituentTemplate.forIUPACNotationWithIgnore(_notation);

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

        /* Check modifications */
        for (GlyCoModification gMod : mono.getModifications()) {
            if (gMod.getPositionOne() == mono.getAnomericPosition()) continue;
            if (gMod.getPositionOne() == mono.getSuperClass().getSize()) continue;

            skeletonCode.replace(gMod.getPositionOne() - 2, gMod.getPositionOne() - 1, "");
        }

        /* define modified base type */
        baseDict = BaseTypeDictionary.forStereoCode(skeletonCode.toString());
        String modifiedStereo = baseDict.getName();

        /**/
        mono.removeStereo(tempStereo);
        mono.addStereo(modifiedStereo);


        return _node;
    }

    private Node modifyLinkageType (Substituent _sub) throws GlycanException {
        SubstituentUtility subUtil = new SubstituentUtility();

        /* define first linkage */
        _sub.setFirstPosition(new Linkage());

        /* define second linkage */
        if (_sub.getSubstituent() instanceof CrossLinkedTemplate) {
            _sub.setSecondPosition(new Linkage());
        }

        return subUtil.modifyLinkageType(_sub);
    }

    private boolean haveChild (String _notation) {
        String currentID = kcfUtil.splitNotation(_notation).get(0);
        String childSideNotation = kcfUtil.extractEdgeByID(currentID, true);
        String parentSideNotation = kcfUtil.extractEdgeByID(currentID, false);


        return (!childSideNotation.equals("") && !parentSideNotation.equals(""));
    }

    private boolean isPyrophosphate (String _notation) {
        String currentID = kcfUtil.splitNotation(_notation).get(0);
        String childSideNotation = kcfUtil.extractEdgeByID(currentID, true);

        if (!childSideNotation.equals("")) {
            String childNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(childSideNotation).get(1));
            String parentNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(childSideNotation).get(2));

            if (childNode.equals("") || parentNode.equals("")) return false;

            SubstituentTemplate parentT =
                    SubstituentTemplate.forIUPACNotationWithIgnore(kcfUtil.splitNotation(parentNode).get(1));
            SubstituentTemplate childT =
                    SubstituentTemplate.forIUPACNotationWithIgnore(kcfUtil.splitNotation(childNode).get(1));

            if (parentT == null || childT == null) return false;
            if (parentT.equals(SubstituentTemplate.PHOSPHATE) && childT.equals(SubstituentTemplate.PHOSPHATE)) return true;
        }

        return false;
    }

    private boolean isPhosphoEthanolamine (String _notation) {
	    	String currentID = kcfUtil.splitNotation(_notation).get(0);
	    	String childNotation = kcfUtil.extractEdgeByID(currentID, true);
	
	    	if (!childNotation.equals("")) {
	    		String childNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(childNotation).get(1));
	    		String parentNode = kcfUtil.getNodeByID(kcfUtil.splitNotation(childNotation).get(2));

	    		if (childNode.equals("") || parentNode.equals("")) return false;

	    		childNode = kcfUtil.splitNotation(childNode).get(1);
	    		parentNode = kcfUtil.splitNotation(parentNode).get(1);
	    		
	    		if (childNode.equals("EtN"))
	    			childNode = SubstituentTemplate.ETHANOLAMINE.getIUPACnotation();
	
	    		SubstituentTemplate parentT =
	    				SubstituentTemplate.forIUPACNotationWithIgnore(parentNode);
	    		SubstituentTemplate childT =
	    				SubstituentTemplate.forIUPACNotationWithIgnore(childNode);
	    		
	    		if (parentT == null || childT == null) return false;
	    		if (parentT.equals(SubstituentTemplate.PHOSPHATE) && childT.equals(SubstituentTemplate.ETHANOLAMINE)) return true;
	    	}
	
	
	    	return false;
    }
    
    private boolean isLinkageSubstituents (String _notation) {
        String currentID = kcfUtil.splitNotation(_notation).get(0);
        String parentSideNotation = kcfUtil.extractEdgeByID(currentID, false);

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

    private Node makeSimpleSubstituent (String _unit) {
        SubstituentTemplate subT = SubstituentTemplate.forIUPACNotation(_unit);

        if (subT == null) return null;

        return new Substituent(subT);
    }

    private Node makeCrossLinkedSubstituent (String _unit) {
        CrossLinkedTemplate crossT = CrossLinkedTemplate.forIUPACNotation(_unit);

        if (crossT == null) return null;
        return new Substituent(crossT);
    }
}
