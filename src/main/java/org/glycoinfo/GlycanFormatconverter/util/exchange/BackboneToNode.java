package org.glycoinfo.GlycanFormatconverter.util.exchange;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.comparater.GlyCoModificationComparater;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.array.WURCSImporter;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;
import org.glycoinfo.WURCSFramework.util.subsumption.MSStateDeterminationUtility;
import org.glycoinfo.WURCSFramework.util.subsumption.WURCSSubsumptionConverter;
import org.glycoinfo.WURCSFramework.wurcs.array.MS;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by e15d5605 on 2017/07/19.
 */
public class BackboneToNode {

    public Node start(Backbone _backbone) throws GlycanException, WURCSFormatException, ConverterExchangeException {
        Monosaccharide ret = new Monosaccharide();
        String skeletonCode = _backbone.getSkeletonCode();

		/* set superclass */
        ret.setSuperClass(SuperClass.forSize(_backbone.getLength()));

		/* extract anomeric state */
        char anomericstate = _backbone.getAnomericSymbol();
        AnomericStateDescriptor enumAnom = AnomericStateDescriptor.forAnomericState(checkAnomericState(skeletonCode, anomericstate));
        ret.setAnomer(enumAnom);

		/* extract anomeric position */
        int anomericposition = checkAnomericPosition(_backbone);
        ret.setAnomericPosition(anomericposition);

		/* extract stereo */
        ret.setStereos(extractStereo(skeletonCode));
        
		/* extract ring position */
        for(WURCSEdge we : _backbone.getChildEdges()) {
            Modification mod = we.getModification();
            if(!mod.isRing()) continue;
            for(LinkagePosition lp : we.getLinkages()) {
                if(_backbone.getAnomericPosition() == lp.getBackbonePosition()) ret.setRingStart(lp.getBackbonePosition());
                else ret.setRingEnd(lp.getBackbonePosition());
            }
        }

		/* extract modification */
        ArrayList<GlyCoModification> mods = extractModification(ret, skeletonCode);

		/* extract substituent */
        ArrayList<Modification> tempMods = new ArrayList<>();
        for(WURCSEdge we : _backbone.getChildEdges()) {
            Modification mod = we.getModification();
            if (mod.isRing() || mod.isGlycosidic() || mod instanceof ModificationRepeat) continue;
 
            /* extract sinple substituent */
            if (mod.getParentEdges().size() == 1 && !mod.getMAPCode().equals("*")) {
                ret = appendSubstituent(ret, ModificationToSubstituent(mod));
            }
            if (mod.getParentEdges().size() == 1 && mod.getMAPCode().equals("*")) {
                ModificationTemplate modT = ModificationTemplate.forCarbon(mod.getMAPCode().charAt(0));
                GlyCoModification gmod = new GlyCoModification(modT, 0);
                mods.add(gmod);
            }

            /* extract cross linked substituent */
            if (mod.getParentEdges().size() == 2 && !mod.getMAPCode().equals("") && !mod.getMAPCode().equals("*")) {
                if (tempMods.contains(mod)) continue;
                ret = appendSubstituent(ret, ModificationToCrossLinkedSubstituent(mod));
                tempMods.add(mod);
            }

            /* extract anhydroxyl group */
            if (mod.getParentEdges().size() == 2 && mod.getMAPCode().equals("")) {
                if (tempMods.contains(mod)) continue;
                ret = appendSubstituent(ret, ModificationToCrossLinkedSubstituent(mod));
                tempMods.add(mod);
            }
                        
            if (mod.getParentEdges().size() > 2) {
            	throw new GlycanException(mod.getMAPCode() + " have more than two anchors.");
            }
        }
        
        /* sort modifications */
        Collections.sort(mods, new GlyCoModificationComparater());

        ret.setModification(mods);

        return ret;
    }

    private ArrayList<GlyCoModification> extractModification (Monosaccharide _mono, String _sc) throws GlycanException {
        ArrayList<GlyCoModification> ret = new ArrayList<>();

        for (int i = 0; i < _sc.length(); i++) {
            char carbon = _sc.charAt(i);
            ModificationTemplate modT = ModificationTemplate.forCarbon(carbon);

            if (i != 0) {
                if (carbon == 'o') modT = ModificationTemplate.KETONE;
                if (carbon == 'O' || carbon == 'a') modT = ModificationTemplate.KETONE_U;
            }
            if (i == (_mono.getSuperClass().getSize() -1 ) && carbon == 'A') {
                modT = ModificationTemplate.URONICACID;
            }

            if (modT == null) continue;

            GlyCoModification mod = new GlyCoModification(modT,i + 1);

            ret.add(mod);

            if (i != 1 && i+1 != _mono.getSuperClass().getSize() &&
                    (modT.equals(ModificationTemplate.KETONE) || modT.equals(ModificationTemplate.KETONE_U))) {
                boolean haveMod = false;
                for (GlyCoModification gMod : ret) {
                    if (gMod.getPositionOne() == 1) haveMod = true;
                }
                if (!haveMod) {
                    mod = new GlyCoModification(modT,1);
                    ret.add(mod);
                }
            }
        }

        return ret;
    }

    /*private Monosaccharide extractModification (Monosaccharide _mono, String _skeletonCode) throws GlycanException {
        for (int i = 0; i < _skeletonCode.length(); i++) {
            char carbon = _skeletonCode.charAt(i);
            ModificationTemplate modT = ModificationTemplate.forCarbon(carbon);
            GlyCoModification mod;

            if (i > 0) {
                if (carbon == 'o') modT = ModificationTemplate.KETONE;
                if (carbon == 'O' || carbon == 'a') modT = ModificationTemplate.KETONE_U;
            }
            if (i == (_mono.getSuperClass().getSize() - 1) && carbon == 'A') {
                modT = ModificationTemplate.URONICACID;
            }

            if (modT != null) {
                mod = new GlyCoModification(modT, i + 1);
                _mono.addModification(mod);

                if (i != 1 && i + 1 != _mono.getSuperClass().getSize() &&
                        (modT.equals(ModificationTemplate.KETONE) || modT.equals(ModificationTemplate.KETONE_U))) {
                    mod = new GlyCoModification(modT, 1);
                    _mono.addModification(mod);
                }
            }
        }

        return _mono;
    }*/

    private Substituent ModificationToSubstituent(Modification _mod) throws ConverterExchangeException, GlycanException {
        SubstituentInterface subT = SubstituentTemplate.forMAP(_mod.getMAPCode());
        Linkage lin = new Linkage();

        if (subT == null) throw new ConverterExchangeException(_mod.getMAPCode() + " could not found!");
        if (subT.getIUPACnotation().equals("")) throw new ConverterExchangeException(_mod.getMAPCode() + " could not support!");

        for(WURCSEdge we : _mod.getParentEdges()) {
            for(LinkagePosition lp : we.getLinkages()) {
                lin.addParentLinkage(lp.getBackbonePosition());
                lin.addChildLinkage(lp.getModificationPosition());
                lin.setProbabilityUpper(lp.getProbabilityUpper());
                lin.setProbabilityLower(lp.getProbabilityLower());
            }
        }

        SubstituentUtility subUtil = new SubstituentUtility();
        return subUtil.modifyLinkageType(new Substituent(subT, lin));
    }

    private Substituent ModificationToCrossLinkedSubstituent(Modification _mod) throws ConverterExchangeException, GlycanException {
        SubstituentInterface subT = CrossLinkedTemplate.forMAP(_mod.getMAPCode().equals("") ? "*o" : _mod.getMAPCode());
        Linkage first = new Linkage();
        Linkage second = new Linkage();

        if (subT == null) throw new ConverterExchangeException(_mod.getMAPCode() + " could not found!");
        if (subT.getIUPACnotation().equals("")) throw new ConverterExchangeException(_mod.getMAPCode() + " could not support!");

        for(WURCSEdge we : _mod.getParentEdges()) {
            if((_mod.getParentEdges().size() - 1) != _mod.getParentEdges().indexOf(we)) {
                for(LinkagePosition lp : we.getLinkages()) {
                    first.addParentLinkage(lp.getBackbonePosition());
                    first.addChildLinkage(lp.getModificationPosition());
                }
            }else {
                for(LinkagePosition lp : we.getLinkages()) {
                    second.addParentLinkage(lp.getBackbonePosition());
                    second.addChildLinkage(lp.getModificationPosition());
                }
            }
        }

        SubstituentUtility subUtil = new SubstituentUtility();
        return subUtil.modifyLinkageType(new Substituent(subT, first, second));
    }

    private LinkedList<String> extractStereo(String _skeletonCode) throws WURCSFormatException, ConverterExchangeException {
        MS ms = new WURCSImporter().extractMS(_skeletonCode);
        MSStateDeterminationUtility msUtility = new MSStateDeterminationUtility();
        WURCSSubsumptionConverter wsc = new WURCSSubsumptionConverter();
        LinkedList<String> stereos = msUtility.extractStereo(ms);

        /* retry define stereo */
        if(stereos.isEmpty()) stereos = checkStereos(ms, wsc);

        return stereos;
    }

    private LinkedList<String> checkStereos(MS _ms, WURCSSubsumptionConverter _wsc) throws ConverterExchangeException {
        LinkedList<String> a_aStereo = new LinkedList<String>();
        String a_sSkeletonCode = _ms.getSkeletonCode();
        
        if (a_sSkeletonCode.equals("<Q>")) {
            a_aStereo.addLast("Sugar");
        }
        
        /* check uncertain groups */
        if (haveUncertainGroups(_ms)) return a_aStereo;

        SuperClass enumSuperClass = SuperClass.forSize(_ms.getSkeletonCode().length());
        MSStateDeterminationUtility msUtil = new MSStateDeterminationUtility();
        
        if(a_sSkeletonCode.contains("1") && a_sSkeletonCode.contains("2")) {
            for(String s :
                    msUtil.extractStereo(_wsc.convertConfigurationUnknownToAbsolutes(_ms).getFirst())) {
                if(a_sSkeletonCode.endsWith("xh") && s.contains("gro")) {
                    a_aStereo.addLast(checkDLconfiguration(s));
                    continue;
                }a_aStereo.addLast(s);
            }
        }

        if(a_sSkeletonCode.contains("3") || a_sSkeletonCode.contains("4")) {
            for (String ms : msUtil.extractStereo(_wsc.convertConfigurationRelativeToD(_ms))) {
                if (ms.contains("gro")) {
                    if (a_sSkeletonCode.endsWith("xh")) a_aStereo.addLast(checkDLconfiguration(ms));
                    else a_aStereo.addLast(ms);
                } else a_aStereo.addLast(checkDLconfiguration(ms));
            }
            //a_aStereo = _msUtility.extractStereo(_wsc.convertConfigurationRelativeToD(_ms));
        }

        if(a_aStereo.isEmpty()) {
            if(enumSuperClass.equals(SuperClass.TET))
                throw new ConverterExchangeException(_ms.getSkeletonCode() + " could not handled");
            else if(enumSuperClass.equals(SuperClass.TRI)) {
            	_ms = _wsc.convertCarbonylGroupToHydroxyl(_ms) != null ? 
            			_wsc.convertCarbonylGroupToHydroxyl(_ms) : _ms;
            	_ms =  _wsc.convertConfigurationUnknownToAbsolutes(_ms) != null ? 
            			_wsc.convertConfigurationUnknownToAbsolutes(_ms).getFirst() : _ms;
            	
            	LinkedList<String> stereos = msUtil.extractStereo(_ms);
            	if (stereos.isEmpty())
            		throw new ConverterExchangeException(_ms.getSkeletonCode() + " could not handled");
                a_aStereo.add(checkDLconfiguration(stereos.getFirst()));
            }
        }
        
        return a_aStereo;
    }

    private boolean haveUncertainGroups (MS _ms) {
    	boolean ret = false;
    	
    	for (int i = 0; i < _ms.getSkeletonCode().length(); i++) {
    		if (i == 0 || i == _ms.getSkeletonCode().length() - 1) continue;
    		CarbonDescriptor cd = 
    				CarbonDescriptor.forCharacter(_ms.getSkeletonCode().charAt(i), false);
  
    		if (cd.equals(CarbonDescriptor.SS3_STEREO_X)) {
    			ret = true;
    		} else {
	    		ret = false;
	    		break;
    		}
    	}
    	
    	return ret;
    }
    
    private char checkAnomericState (String _skeletonCode, char _anomericstate) {
        if (_anomericstate == 'o') {
            if (_skeletonCode.indexOf("o") == 0 || _skeletonCode.indexOf("O") == 1) return 'o';
            if (_skeletonCode.indexOf("u") == 0 || _skeletonCode.indexOf("U") == 1) return '?';
        }
        return _anomericstate;
    }

    private String checkDLconfiguration (String a_sStereo) {
        if((a_sStereo.startsWith("l") || a_sStereo.startsWith("d")))
            a_sStereo = a_sStereo.replaceFirst("[ld]", "");
        return a_sStereo;
    }

    private Monosaccharide appendSubstituent (Node _node, Node _substituent) throws GlycanException {
        Edge first = new Edge();
        first.setSubstituent(_substituent);
        first.setParent(_node);
        _node.addChildEdge(first);
        _substituent.addParentEdge(first);

        return (Monosaccharide) _node;
    }

    private int checkAnomericPosition (Backbone _backbone) {
        int anomericPosition = _backbone.getAnomericPosition();
        String skeletonCode = _backbone.getSkeletonCode();

        if (skeletonCode.indexOf("u") == 0 || skeletonCode.indexOf("U") == 1) {
            anomericPosition = -1;
        }

        return anomericPosition;
    }
}
