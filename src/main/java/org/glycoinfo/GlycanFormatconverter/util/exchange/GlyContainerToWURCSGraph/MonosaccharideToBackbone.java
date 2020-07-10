package org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph;

import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.WURCSFramework.util.array.mass.AtomicPropatiesOld;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;

import java.util.ArrayList;
import java.util.LinkedList;

public class MonosaccharideToBackbone {

	private Backbone backbone;
	
	private int anomericPos = 0;
	private char anomericSymbol = 'x';
	private char configuration = 'X';
	
	private Monosaccharide mono;
	private boolean isRootOfSubgraph = false;
	
	private LinkedList<Modification> unknownPosCoreMod = new LinkedList<>();
	
	public Backbone getBackbone() {
		return this.backbone;
	}
	
	public LinkedList<Modification> getCoreModification() {
		return this.unknownPosCoreMod;
	}
	
	public void setRootOfSubgraph () {
		this.isRootOfSubgraph = true;
	}
	
	private boolean hasParent() {
		if (this.isRootOfSubgraph) return true;
		
		if (this.mono.getParentNode() != null) return true;
		
		for (Edge edge : mono.getChildEdges()) {
			for (Linkage lin : edge.getGlycosidicLinkages()) {
				for (Integer linkPos : lin.getParentLinkages()) {
					int ringStart = mono.getRingStart();
					if(ringStart != -1 && ringStart == linkPos)	{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void start (Node _node) throws WURCSExchangeException, GlycanException {
		mono = (Monosaccharide) _node;
		this.anomericPos = mono.getRingStart();
		this.anomericSymbol = mono.getAnomer().getAnomericState();
		
		if (this.anomericPos == Monosaccharide.OPEN_CHAIN) {
			this.anomericSymbol = 'o';
		}
		
		int numOfAtom = mono.getSuperClass().getSize();
		if (numOfAtom == 0) {
			this.backbone = new BackboneUnknown_TBD(this.anomericSymbol);
			return;
		}
		
		MonosaccharideAnalyzer monoAnalyzer = new MonosaccharideAnalyzer();
		monoAnalyzer.analyze(_node);
		
		this.anomericPos = monoAnalyzer.getAnomericPosition();
		this.anomericSymbol = monoAnalyzer.getAnomericSymbol();
		this.configuration = monoAnalyzer.getConfiguration();
		String skeletonCode = monoAnalyzer.getSkeletonCode();
		
		for (String map : monoAnalyzer.getCoreModificationUnknownPosiiton()) {
			this.unknownPosCoreMod.add(new Modification(map));
		}
		
		if (this.anomericPos == Monosaccharide.OPEN_CHAIN) {
			//if (this.anomericSymbol == 'x' || this.anomericSymbol == 'o') {
			if (!this.isAldehyde()) {
				if (skeletonCode.startsWith("o") && skeletonCode.contains("O")) {
					skeletonCode = skeletonCode.replaceFirst("o", "u");
				} else {
					skeletonCode = skeletonCode.replaceAll("o", "u");
					skeletonCode = skeletonCode.replaceAll("O", "U");					
				}
				this.anomericPos = Monosaccharide.OPEN_CHAIN;
			}
			/*
			else {
				if (skeletonCode.contains("o")) {
					this.anomericPos = skeletonCode.indexOf("o") + 1;
					skeletonCode = skeletonCode.replaceFirst("o", "a");
				} else if (skeletonCode.contains("O")) {
					this.anomericPos = skeletonCode.indexOf("O") + 1;
					skeletonCode = skeletonCode.replaceFirst("O", "a");
				}
			}
			 */
		}

		StringBuilder skeletonCode_b = new StringBuilder(skeletonCode);
		if (mono.getParentEdge() != null) {
			this.replaceCarbonDescriptorByEdge(skeletonCode_b, mono.getParentEdge(), true);
		}
		for (Edge childEdge : mono.getChildEdges()) {
			this.replaceCarbonDescriptorByEdge(skeletonCode_b, childEdge, false);
		}
		
		Backbone_TBD backbone = new Backbone_TBD();
		backbone.setAnomericPosition(this.anomericPos);
		backbone.setAnomericSymbol(this.anomericSymbol);
		for (int i = 0; i < numOfAtom; i++) {
			char carbonDescriptor = skeletonCode_b.charAt(i);
			CarbonDescriptor_TBD cdT = CarbonDescriptor_TBD.forCharacter(carbonDescriptor, (i == 0 || i == numOfAtom-1));
			BackboneCarbon bc = new BackboneCarbon(backbone, cdT);
			backbone.addBackboneCarbon(bc);
		}
		this.backbone = backbone;
	}
	
	private void replaceCarbonDescriptorByEdge (StringBuilder skeletonCode_b, Edge _edge, boolean _isParentSide) throws WURCSExchangeException {
		Node node = (_isParentSide) ? _edge.getParent() : _edge.getChild();
		Substituent sub = (node instanceof Substituent) ? (Substituent) node : null;
		boolean isSwapChirality = false;

		if (_edge.getSubstituent() != null && _edge.getSubstituent() instanceof GlycanRepeatModification) return;

		for (Linkage lin : _edge.getGlycosidicLinkages()) {
			ArrayList<Integer> positions = (_isParentSide) ? lin.getChildLinkages() : lin.getParentLinkages();
			if (positions.size() > 1) continue;
			
			isSwapChirality = (this.compareConnectAtom(lin, sub, _isParentSide) < 0);
			Linkage parentLinkage = lin;

			int pos = positions.get(0);

			if (pos == -1) continue;
			char carbonDescriptor = skeletonCode_b.charAt(pos-1);
			char newCarbonDescriptor = carbonDescriptor;
			
			LinkageType type0 = (_isParentSide) ? lin.getChildLinkageType() : parentLinkage.getParentLinkageType();
			LinkageType type1 = (_isParentSide) ? parentLinkage.getParentLinkageType() : lin.getChildLinkageType();
			if (type0 == LinkageType.H_LOSE) {
				newCarbonDescriptor = this.replaceCarbonDescriptorByHydrogenLose(carbonDescriptor, isSwapChirality);
			} else if (type0 == LinkageType.DEOXY && type1 == LinkageType.H_AT_OH) {
				newCarbonDescriptor = (carbonDescriptor == 'c') ? 'x' :
									  (carbonDescriptor == 'C') ? 'X' : newCarbonDescriptor;
			}
			
			skeletonCode_b.replace(pos-1, pos, newCarbonDescriptor+"");
		}
		
		if (!_isParentSide ) return;
		if (this.anomericPos == Monosaccharide.OPEN_CHAIN
		  || this.anomericPos == Monosaccharide.UNKNOWN_RING ) return;

		if (this.anomericSymbol != 'a' && this.anomericSymbol != 'b') return;

		char anomericCD = skeletonCode_b.charAt(this.anomericPos-1);
		if (anomericCD != 'x' && anomericCD != 'X') return;

		char anomStereo = (this.anomericSymbol == 'a')? '1' : '2';

		if (this.configuration == 'L') {
			anomStereo = (anomStereo == '1')? '2' : '1';
		}
		if (isSwapChirality) {
			anomStereo = (anomStereo == '1')? '2' : '1';
		}
		if (anomericCD == 'X') {
			anomStereo = (anomStereo == '1')? '5' : '6';
		}
		
		skeletonCode_b.replace(this.anomericPos-1, this.anomericPos, anomStereo+"");
	}
	
	private char replaceCarbonDescriptorByHydrogenLose (char _carbonDescriptor, boolean _isSwapChirality) {
		char newCarbonDescriptor = (_carbonDescriptor == '1') ? '5' :
								   (_carbonDescriptor == '2') ? '6' :
								   (_carbonDescriptor == '3') ? '7' :
								   (_carbonDescriptor == '4') ? '8' :
								   (_carbonDescriptor == 'x') ? 'X' :
								   (_carbonDescriptor == 'c') ? 'C' :
								   (_carbonDescriptor == 'm') ? 'h' :
								   (_carbonDescriptor == 'h') ? 'c' : _carbonDescriptor;
		
		if (_isSwapChirality) {
			newCarbonDescriptor = (newCarbonDescriptor == '5') ? '6' :
								  (newCarbonDescriptor == '6') ? '5' :
								  (newCarbonDescriptor == '7') ? '8' :
								  (newCarbonDescriptor == '8') ? '7' : newCarbonDescriptor;
		}
		return newCarbonDescriptor;
	}
	
	private int compareConnectAtom (Linkage _lin, Substituent _sub, boolean _isParentSide) throws WURCSExchangeException {
		LinkageType parentType = (_isParentSide) ? _lin.getChildLinkageType() : _lin.getParentLinkageType();
		LinkageType childType = (_isParentSide) ? _lin.getParentLinkageType() : _lin.getChildLinkageType();
		if (parentType == LinkageType.H_AT_OH || childType == LinkageType.H_AT_OH) return 0;
		
		if (_sub == null) return 0;
		return this.compareConnectAtomOfSubstituent(_sub, _isParentSide);
	}
	
	private int compareConnectAtomOfSubstituent (Substituent _sub, boolean _substituentIsParent) throws WURCSExchangeException {
		SubstituentToModification subst2mod = new SubstituentToModification();
		subst2mod.start(_sub);
		String connAtom = (_substituentIsParent) ? subst2mod.getTailAtom() : subst2mod.getHeadAtom();

		int numberOfFirstAtom = AtomicPropatiesOld.forSymbol(connAtom).getAtomicNumber();
		int comp = 16 - AtomicPropatiesOld.forSymbol(connAtom).getAtomicNumber();
		if (numberOfFirstAtom < 16) return 1;
		return -1;
	}

	private boolean isAldehyde () {
		boolean ret = false;
		for (GlyCoModification gMod : this.mono.getModifications()) {
			if (gMod.getPositionOne() != 1) continue;
			if (gMod.getModificationTemplate().equals(ModificationTemplate.ALDEHYDE)) {
				ret = true;
				break;
			}
		}
		return ret;
	}
}
