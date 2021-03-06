package org.glycoinfo.GlycanFormatconverter.util;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.ModifiedMonosaccharideDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.IUPACSubstituentNotationAnalyzer;
import org.glycoinfo.GlycanFormatconverter.util.comparater.GlyCoModificationComparater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2017/08/30.
 */
public class MonosaccharideUtility {

    public ArrayList<String> resolveNotation (String _temp) {
        ArrayList<String> ret = new ArrayList<>();

        Matcher matMod = Pattern.compile("([\\d,?:]+)-?(\\D+)").matcher(_temp);
        if(!matMod.find()) return ret;

        String positions = matMod.group(1) != null ? matMod.group(1) : "";
        String notation = matMod.group(2) != null ? matMod.group(2) : "";

        BaseCrossLinkedTemplate crossT = BaseCrossLinkedTemplate.forIUPACNotation(notation);

        if(notation.contains("deoxy")) {
            for(String pos : positions.split(",")) {
                if(notation.contains("deoxy")) ret.add(pos+"d");
                else ret.add(pos+notation);
            }
        }
        if(crossT != null && crossT.equals(BaseCrossLinkedTemplate.ANHYDRO)) ret.add(positions + notation);

        return ret;
    }

    public Monosaccharide appendSubstituents (Node _node,  ArrayList<String> _substituents) throws GlycanException {
        // make substituents for GlyContainer
        IUPACSubstituentNotationAnalyzer subAna = new IUPACSubstituentNotationAnalyzer();
        subAna.start((Monosaccharide) _node, _substituents);

        for (Substituent sub : subAna.getSubstituents()) {
            Edge first = new Edge();
            first.addGlycosidicLinkage(sub.getFirstPosition());
            first.setSubstituent(sub);
            first.setParent(_node);
            _node.addChildEdge(first);
            sub.addParentEdge(first);
            if (sub.getSecondPosition() != null) {
                first.addGlycosidicLinkage(sub.getSecondPosition());
            }
        }

        return (Monosaccharide) _node;
    }

    public Monosaccharide makeRingSize (Monosaccharide _mono, String _ringSize, String _code, ArrayList<String> _modifications) throws GlycanException {
        int pos = _mono.getAnomericPosition();
        boolean haveKetose = this.haveKetoneAtAnomer(_mono, _modifications);
        
		// Modify anomeric position
        if (pos == 0) {
            MonosaccharideIndex monoIndex = MonosaccharideIndex.forTrivialNameWithIgnore(_code);
            if (monoIndex != null) {
                pos = (haveKetose) ? extractAnomeriKetone(_modifications) : monoIndex.getAnomerciPosition();
                _mono.setAnomericPosition(pos);
            }
            if (monoIndex == null && _mono.getSuperClass() != null) {
                pos = (haveKetose) ? extractAnomeriKetone(_modifications) : 1;
            }
        }

        // if anomeric position is undefined
        if (!_ringSize.equals("?") && pos == -1) {
            MonosaccharideIndex mi = MonosaccharideIndex.forTrivialNameWithIgnore(_code);
            if (mi != null) {
                pos = mi.getAnomerciPosition();
                _mono.setAnomericPosition(pos);
            }
        }
        _mono.setRingStart(pos);

        // check ring position
        if (_mono.getAnomericPosition() == 1) {
            if (_ringSize.equals("p")) {
                if (_mono.getSuperClass().getSize() < SuperClass.PEN.getSize()) {
                    throw new GlycanException("Ring position exceeds the number of carbon backbone : 5 -> " + _mono.getSuperClass().getSize());
                }
            }
            if (_ringSize.equals("f")) {
                if (_mono.getSuperClass().getSize() < SuperClass.TET.getSize()) {
                    throw new GlycanException("Ring position exceeds the number of carbon backbone : 4 -> " + _mono.getSuperClass().getSize());
                }
            }
        }
        if (_mono.getAnomericPosition() == 2) {
            if (_ringSize.equals("p")) {
                if (_mono.getSuperClass().getSize() < SuperClass.HEX.getSize()) {
                    throw new GlycanException("Ring position exceeds the number of carbon backbone : 6 -> " + _mono.getSuperClass().getSize());
                }
            }
            if (_ringSize.equals("f")) {
                if (_mono.getSuperClass().getSize() < SuperClass.PEN.getSize()) {
                    throw new GlycanException("Ring position exceeds the number of carbon backbone : 5 -> " + _mono.getSuperClass().getSize());
                }
            }
        }

        if(_ringSize.equals("p")) {
            if(pos == 1) _mono.setRingEnd(5);
            if(pos == 2) _mono.setRingEnd(6);
        }
        if(_ringSize.equals("f")) {
            if(pos == 1) _mono.setRingEnd(4);
            if(pos == 2) _mono.setRingEnd(5);
        }
        if(_ringSize.equals("?")) {
            _mono.setRingEnd(-1);
        }

        return _mono;
    }

    private boolean haveKetoneAtAnomer(Monosaccharide _mono, ArrayList<String> _modifications) {
        int anomericPos = _mono.getAnomericPosition();
        
        if (anomericPos == 0) {
        		String ketone = "";
        		for (String mod : _modifications) {
        			if (ketone.equals("") && mod.contains("ulo")) ketone = mod;
        		}
        		if (ketone.equals("1ulo") || ketone.equals("2ulo")) return true;
        }
        
        if (_modifications.contains("2ulo") && anomericPos == 2) return true;
        if (_modifications.contains("3ulo") && anomericPos == 3) return true;
        return false;
    }

    private int extractAnomeriKetone (ArrayList<String> _modifications) {
        int ret = -1;
        for (String mod : _modifications) {
            if (mod.contains("ulo")) {
                ret = Integer.parseInt(mod.substring(0, 1));
                break;
            }
        }

        return ret;
    }

    public Monosaccharide appendModifications (Monosaccharide _mono, ArrayList<String> _modifications) throws GlycanException {
        HashMap<Integer, ModificationTemplate> hashMod = new HashMap<>();
        
        for( String unit : _modifications) {
            // parse single notation
            switch (unit) {
                case "ol" :
                    hashMod.put(1, ModificationTemplate.HYDROXYL);
                    break;

                case "onic" :
                    hashMod.put(1, ModificationTemplate.ALDONICACID);
                    break;

                case "aric" :
                    hashMod.put(1, ModificationTemplate.ALDONICACID);
                    hashMod.put(_mono.getSuperClass().getSize(), ModificationTemplate.URONICACID);
                    break;

                case "uronic" :
                    hashMod.put(_mono.getSuperClass().getSize(), ModificationTemplate.URONICACID);
                    break;

                case "aldehyde" :
                    hashMod.put(1, ModificationTemplate.ALDEHYDE);
                    break;

                case "keto" :
                    hashMod.put(2, ModificationTemplate.ULOSONIC);
                    break;
            }

            // parse notation with position
            Matcher matMod = Pattern.compile("(\\d+)+(\\([XEZ]\\)\\w+|\\w+|[5678])+").matcher(unit);
            if (!matMod.find()) continue;

            int pos = Integer.parseInt(matMod.group(1).equals("?") ? "-1" : matMod.group(1));
            String notation = matMod.group(2);

            switch (notation) {
                case "ulo" :
                    notation = "U";
                    break;

                case "(X)en" :
                    if (hashMod.get(pos) != null && this.isDeoxy(pos, _mono, hashMod)) {
                        hashMod.put(pos, ModificationTemplate.UNSATURATION_FL);
                    } else {
                        hashMod.put(pos, ModificationTemplate.UNSATURATION_FU);
                    }

                    if (hashMod.get(pos+1) != null && this.isDeoxy(pos+1, _mono, hashMod)) {
                        hashMod.put(pos + 1, ModificationTemplate.UNSATURATION_FL);
                    } else {
                        hashMod.put(pos + 1, ModificationTemplate.UNSATURATION_FU);
                    }
                    break;

                case "(E)en" :
                    if (hashMod.get(pos) != null && this.isDeoxy(pos, _mono, hashMod)) {
                        hashMod.put(pos, ModificationTemplate.UNSATURATION_EL);
                    } else {
                        hashMod.put(pos, ModificationTemplate.UNSATURATION_EU);
                    }

                    if (hashMod.get(pos+1) != null && this.isDeoxy(pos+1, _mono, hashMod)) {
                        hashMod.put(pos + 1, ModificationTemplate.UNSATURATION_EL);
                    } else {
                        hashMod.put(pos + 1, ModificationTemplate.UNSATURATION_EU);
                    }
                    break;

                case "(Z)en" :
                    if (hashMod.get(pos) != null && this.isDeoxy(pos, _mono, hashMod)) {
                        hashMod.put(pos, ModificationTemplate.UNSATURATION_ZL);
                    } else {
                        hashMod.put(pos, ModificationTemplate.UNSATURATION_ZU);
                    }

                    if (hashMod.get(pos+1) != null && this.isDeoxy(pos+1, _mono, hashMod)) {
                        hashMod.put(pos + 1, ModificationTemplate.UNSATURATION_ZL);
                    } else {
                        hashMod.put(pos + 1, ModificationTemplate.UNSATURATION_ZU);
                    }
                    break;
                case "5" :
                    hashMod.put(pos, ModificationTemplate.HLOSE_5);
                    break;
                case "6" :
                    hashMod.put(pos, ModificationTemplate.HLOSE_6);
                    break;
                case "7" :
                    hashMod.put(pos, ModificationTemplate.HLOSE_7);
                    break;
                case "8" :
                    hashMod.put(pos, ModificationTemplate.HLOSE_8);
                    break;
            }

            // modify anomeric modification
            ModificationTemplate modT = analyzeSingleMod(_mono, pos, notation);

            if (modT != null && !hashMod.containsKey(pos)) hashMod.put(pos, modT);
        }

        for(Integer key : hashMod.keySet()) {
        		ModificationTemplate modT = hashMod.get(key);
            if(modT.equals(ModificationTemplate.ULOSONIC)) modT = ModificationTemplate.KETONE_U;
            GlyCoModification mod = new GlyCoModification(modT, key);

            _mono.addModification(mod);
        }

        _mono.getModifications().sort(new GlyCoModificationComparater());

        return _mono;
    }

    private ModificationTemplate analyzeSingleMod (Monosaccharide _mono, int _position, String _notation) {
        // modify anomeric modification
        ModificationTemplate modT = ModificationTemplate.forCarbon(_notation.charAt(0));
        if (_notation.length() != 1 || modT == null) return null;

        if (_position == _mono.getSuperClass().getSize() && modT.equals(ModificationTemplate.ALDONICACID)) {
            modT = ModificationTemplate.URONICACID;
        }
        if (modT.equals(ModificationTemplate.ULOSONIC))
            modT = ModificationTemplate.KETONE_U;
        if (_mono.getSuperClass().getSize() == _position && modT.equals(ModificationTemplate.KETONE_U)) {
            modT = ModificationTemplate.KETONE;
        }
        if (modT.equals(ModificationTemplate.DEOXY) && (_position == 1 || _position == _mono.getSuperClass().getSize())) {
            modT = ModificationTemplate.METHYL;
        }

        return modT;
    }

    public Monosaccharide modifyStereos (Monosaccharide _mono, LinkedList<String> _configurations) throws GlycanException {
        String firstConfig = _configurations.isEmpty() ? "" : _configurations.getFirst().equals("?") ? "" : _configurations.getFirst();
        String secondConfig = _configurations.isEmpty() ? "" : _configurations.getLast().equals("?") ? "" : _configurations.getLast();

        LinkedList<String> stereos = new LinkedList<>();
        
        for(String stereo : _mono.getStereos()) {
            if (stereo.equals("Sugar") || stereo.length() == 4) {
                stereos.addLast(stereo);
                continue;
            }
            if(_mono.getStereos().indexOf(stereo) == 0) {
                stereos.addLast((firstConfig + stereo).toLowerCase());
            }
            if(_mono.getStereos().indexOf(stereo) == 1) {
                stereos.addLast((secondConfig + stereo).toLowerCase());
            }
        }

        _mono.setStereos(stereos);

        return _mono;
    }

    public Monosaccharide checkTruelyConfiguration (String _code, LinkedList<String> _configurations, Monosaccharide _mono) throws GlycanException {
        if (!_configurations.isEmpty()) return _mono;
        
        MonosaccharideIndex index = MonosaccharideIndex.forTrivialName(_code);
       
        if (index == null) return _mono;
   
        LinkedList<String> modStereo = new LinkedList<>();
        for (String stereo : _mono.getStereos()) {
        	modStereo.add(index.getFirstConfiguration().toLowerCase() + stereo);
        }
        
        _mono.setStereos(modStereo);
        
       // String stereo = _mono.getStereos().getFirst();
       // _mono.getStereos().remove(stereo);
       // stereo = index.getFirstConfiguration().toLowerCase() + stereo;
        		//stereo.replaceFirst(String.valueOf(stereo.charAt(0)), index.getFirstConfiguration().toLowerCase());
        
        //_mono.getStereos().addFirst(stereo);

        return _mono;
    }

    private HashMap<Integer, ModificationTemplate> chekcUnsaturateStatus
            (Monosaccharide _mono, Integer _pos, HashMap<Integer, ModificationTemplate> _hashMod) {
        if (_pos == -1) return _hashMod;

        boolean isTerminal = (_pos == 1 || _mono.getSuperClass().getSize() == _pos);
        ModificationTemplate modT1 = (_hashMod.containsKey(_pos)) ? _hashMod.get(_pos) : null;
        ModificationTemplate modT2 = (_hashMod.containsKey(_pos + 1)) ? _hashMod.get(_pos + 1) : null;
        boolean isDeoxy1 = (modT1 != null && modT1.equals(ModificationTemplate.DEOXY));
        boolean isDeoxy2 = (modT2 != null && modT2.equals(ModificationTemplate.DEOXY));

        if (isTerminal && _pos == _mono.getRingEnd()) {
			/*if (carbonDescriptor1 == 'c') {
				this.posToChar.put(pos1, 'N');
				this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'n' : 'N');
				return true;
			}*/
            if (modT1.equals(ModificationTemplate.HYDROXYL)) {
                _hashMod.put(_pos, ModificationTemplate.UNSATURATION_ZL);
                _hashMod.put(_pos + 1, (isDeoxy2) ? ModificationTemplate.UNSATURATION_ZL : ModificationTemplate.UNSATURATION_ZU);
            }
            return _hashMod;
        }
        if (isTerminal && (_pos + 1) == _mono.getRingEnd()) {
			/*if (carbonDescriptor1 == 'm') {
				this.posToChar.put(pos1, 'n');
				this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'n' : 'N');
				return true;
			}*/
            if (modT1.equals(ModificationTemplate.HYDROXYL)) {
                _hashMod.put(_pos, ModificationTemplate.UNSATURATION_FL);
                _hashMod.put(_pos + 1, (isDeoxy2) ? ModificationTemplate.UNSATURATION_FL : ModificationTemplate.UNSATURATION_FU);
            }
            return _hashMod;
        }
        if (!isTerminal && _pos == _mono.getRingEnd()) {
        		_hashMod.put(_pos, ModificationTemplate.UNSATURATION_FU);
            _hashMod.put(_pos + 1, (isDeoxy2) ? ModificationTemplate.UNSATURATION_FL : ModificationTemplate.UNSATURATION_FU);
            return _hashMod;
        }
        if (isTerminal && _pos == _mono.getRingEnd()) {
            _hashMod.put(_pos, ModificationTemplate.UNSATURATION_ZU);
            _hashMod.put(_pos + 1, (isDeoxy2) ? ModificationTemplate.UNSATURATION_ZL : ModificationTemplate.UNSATURATION_ZU);
            return _hashMod;
        }
        if (!isTerminal && (_pos + 1) == _mono.getRingEnd()) {
            _hashMod.put(_pos, (isDeoxy1) ? ModificationTemplate.UNSATURATION_EL : ModificationTemplate.UNSATURATION_EU);
            _hashMod.put(_pos + 1, ModificationTemplate.UNSATURATION_EU);
            return _hashMod;
        }
        if (_pos > _mono.getRingStart() && (_pos + 1) < _mono.getRingEnd()) {
            _hashMod.put(_pos, (isDeoxy1) ? ModificationTemplate.UNSATURATION_ZL : ModificationTemplate.UNSATURATION_ZU);
            _hashMod.put(_pos + 1, (isDeoxy2) ? ModificationTemplate.UNSATURATION_ZL : ModificationTemplate.UNSATURATION_ZU);
            return _hashMod;
        }
        
        //is terminal Methyl
        //if (modT1 != null && modT1.equals(ModificationTemplate.METHYL)) {
        //		_hashMod.put(_pos, null); //n
        //		_hashMod.put(_pos + 1, (isDeoxy2) ? null : null); //n : N
 	//		return _hashMod;
      //  }
        
        if (modT1 != null && modT1.equals(ModificationTemplate.HYDROXYL)) {
            _hashMod.put(_pos, ModificationTemplate.HYDROXYL);
            _hashMod.put(_pos + 1, (isDeoxy2) ? ModificationTemplate.UNSATURATION_FL : ModificationTemplate.UNSATURATION_FU);
            return _hashMod;
        }
        
        _hashMod.put(_pos, (isDeoxy1) ? ModificationTemplate.UNSATURATION_FL : ModificationTemplate.UNSATURATION_FU);
        _hashMod.put(_pos + 1, (isDeoxy2) ? ModificationTemplate.UNSATURATION_FL : ModificationTemplate.UNSATURATION_FU);
        return _hashMod;
    }

    // for GC-JSON (WURCS-JSON) utility
    public Monosaccharide modifiedSubstituents (String _trivialName, Node _node) {
        Monosaccharide mono = (Monosaccharide) _node;
        ModifiedMonosaccharideDescriptor modMonoDesc = ModifiedMonosaccharideDescriptor.forTrivialName(_trivialName);

        if (modMonoDesc != null) {
            if (isHexosamine(modMonoDesc)) {
                this.modifyNsubstituent(_node);
            }

            for (String sub : modMonoDesc.getSubstituents().split("_")) {
                removeSubstituents(sub, _node);
            }
            for (String mod : modMonoDesc.getModifications().split("_")) {
                remomveModifications(mod, _node);
            }

            return mono;
        }

        if (_trivialName.endsWith("A")) {
            remomveModifications("6*A", _node);
        }

        return mono;
    }

    // for GC-JSON (WURCS-JSON) utility
    private void modifyNsubstituent (Node _node) {
        Monosaccharide mono = (Monosaccharide) _node;

        for (Edge edge : mono.getChildEdges()) {
            if (edge.getSubstituent() == null) continue;
            if (edge.getSubstituent() != null && edge.getChild() != null) continue;
        }
    }

    // for GC-JSON (WURCS-JSON) utility
    private void removeSubstituents (String _notation, Node _node) {
        if (_notation.equals("")) return;

        String[] posNot = _notation.split("\\*");

        for (Edge edge : _node.getChildEdges()) {
            if (edge.getSubstituent() == null) continue;
            if (edge.getSubstituent() != null && edge.getChild() != null) continue;

            //Substituent sub = (Substituent) edge.getSubstituent();
            //TODO: convert O-link to N-link

            //SubstituentTemplate convSub = subUtil.convertOTypeToNType(sub.getSubstituent());

            //if (subTemp.equals(convSub) && sub.getFirstPosition().getParentLinkages().contains(Integer.parseInt(posNot[0]))) {
            //    _node.removeChildEdge(edge);
            //}
        }
    }

    // for WURCS-JSON (GC-JSON) utility
    private void remomveModifications (String _mod, Node _node) {
        if (_mod.equals("")) return;

        String[] posNot = _mod.split("\\*");
        ModificationTemplate modTemp = ModificationTemplate.forCarbon(posNot[1].charAt(0));
        if (modTemp.equals(ModificationTemplate.ALDONICACID) && !posNot[0].equals("1")) {
            modTemp = ModificationTemplate.URONICACID;
        }
        Monosaccharide mono = (Monosaccharide) _node;

        for (GlyCoModification gMod : mono.getModifications()) {
            if (gMod.getModificationTemplate().equals(modTemp)) mono.removeModification(gMod);
        }
    }

    private boolean isHexosamine (ModifiedMonosaccharideDescriptor _modMonoDesc) {
        return (_modMonoDesc.equals(ModifiedMonosaccharideDescriptor.ALLN) ||
                _modMonoDesc.equals(ModifiedMonosaccharideDescriptor.ALTN) ||
                _modMonoDesc.equals(ModifiedMonosaccharideDescriptor.GALN) ||
                _modMonoDesc.equals(ModifiedMonosaccharideDescriptor.GLCN) ||
                _modMonoDesc.equals(ModifiedMonosaccharideDescriptor.IDON) ||
                _modMonoDesc.equals(ModifiedMonosaccharideDescriptor.MANN) ||
                _modMonoDesc.equals(ModifiedMonosaccharideDescriptor.HEXN) ||
                _modMonoDesc.equals(ModifiedMonosaccharideDescriptor.TALN) ||
                _modMonoDesc.equals(ModifiedMonosaccharideDescriptor.GULN)
        );
    }

    private boolean isDeoxy (Integer _pos, Node _node, HashMap<Integer, ModificationTemplate> _modMap) {
        Monosaccharide node = (Monosaccharide) _node;
        if (_pos == 1 && _modMap.get(_pos).equals(ModificationTemplate.METHYL)) {
            return true;
        }
        if (_pos == node.getSuperClass().getSize() && _modMap.get(_pos).equals(ModificationTemplate.METHYL)) {
            return true;
        }
        if (_pos != node.getSuperClass().getSize() && _modMap.get(_pos).equals(ModificationTemplate.DEOXY)) {
            return true;
        }
        return false;
    }
}