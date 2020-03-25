package org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;

import java.util.LinkedList;
import java.util.TreeMap;

public class MonosaccharideAnalyzer {
	
	private Monosaccharide mono;
	
	private boolean isAldose;
	
	private int anomericPos;
	private char anomericSymbol;
	private int numOfAtom;
	private char configuration;
	private String skeletonCode;
	
	private LinkedList<Integer> anomericPositions;
	private TreeMap<Integer, Character> posToChar;
	private LinkedList<String> unknownPosMap;

	public MonosaccharideAnalyzer() {
		this.mono = null;
		this.anomericPos = 0;
		this.numOfAtom = 0;
		this.anomericSymbol = 'x';
		this.configuration = 'X';
		this.skeletonCode = "";
		this.isAldose = true;

		this.posToChar = new TreeMap<>();
		this.unknownPosMap = new LinkedList<>();
		this.anomericPositions = new LinkedList<>();
	}

	public int getAnomericPosition() {
		return this.anomericPos;
	}
	
	public char getAnomericSymbol() {
		return this.anomericSymbol;
	}
	
	public int getNumberOfCarbons() {
		return this.numOfAtom;
	}
	
	public char getConfiguration () {
		return this.configuration;
	}
	
	public String getSkeletonCode() {
		return this.skeletonCode;
	}
	
	public LinkedList<String> getCoreModificationUnknownPosiiton() {
		return this.unknownPosMap;
	}
	
	public boolean isAldose() {
		return this.isAldose;
	}
	
	public void analyze (Node _node) throws WURCSExchangeException, GlycanException {
		this.mono = (Monosaccharide) _node;
		this.anomericPos = mono.getRingStart();
		this.anomericSymbol = mono.getAnomer().getAnomericState();

		if (this.anomericSymbol == 'o' || this.anomericPos == Monosaccharide.OPEN_CHAIN) {
			this.anomericSymbol = 'o';
			this.anomericPos = Monosaccharide.OPEN_CHAIN;
		}

		/*
		if (this.anomericPos == Monosaccharide.UNKNOWN_RING && this.anomericSymbol == '?') {
			this.anomericSymbol = 'o';
		}
		 */

		this.numOfAtom = mono.getSuperClass().getSize();
		this.posToChar.put(1, 'h');
		this.posToChar.put(this.numOfAtom, 'h');

		LinkedList<GlyCoModification> mods = new LinkedList<>();
		for (GlyCoModification mod : this.mono.getModifications()) {
			if (mod.hasPositionTwo()) {
				if (this.isDoublebond(mod)) mods.add(mod);
				continue;
			}
			this.ModificationToCarbonDescriptor(mod);
		}

		// Analyze ketose
		if (!this.anomericPositions.isEmpty() && this.anomericPositions.getFirst() != 1) {
			this.isAldose = false;
		}
		
		// Analyze head carbon
		if (this.isAldose) {
			this.posToChar.put(1, 'o');
			this.anomericPositions.addFirst(1);
		}

		//
		if (this.anomericPositions.isEmpty()) {
			this.anomericPos = Monosaccharide.OPEN_CHAIN;
			this.anomericSymbol = 'o';
		}

		if (this.anomericSymbol == '?' && this.anomericPos != Monosaccharide.UNKNOWN_RING && mono.getRingEnd() != Monosaccharide.UNKNOWN_RING) {
			this.anomericSymbol = 'x';
		}

		// Analyze anomeric position
		if (this.anomericPos != Monosaccharide.OPEN_CHAIN && this.anomericPos != Monosaccharide.UNKNOWN_RING) {
			if (!this.posToChar.containsKey(this.anomericPos)) {
				throw new WURCSExchangeException("Illegal structure is found at ring start position.");
			}
			char carbonDescriptor = this.posToChar.get(this.anomericPos);
			if (carbonDescriptor == 'o' || carbonDescriptor == 'O') {
				this.posToChar.put(this.anomericPos, 'a');
			}
		}

		// Analyzer core modifications contained double bond
		for (GlyCoModification glyCoMod : mods) {
			//if (!this.replaceCarbonDescirptorByUnsaturate(glyCoMod)) {
			//	throw new WURCSExchangeException("There is an error in the modification \"en\" or \"enx\".");
			//}
		}

		// Count non terminal stereo
		int brankPosition = 0;
		for (int i = 2; i < this.numOfAtom; i++) {
			if (this.posToChar.containsKey(i)) continue;
			brankPosition++;
		}

		// Encode stereo code
		String stereo = this.convertBaseTypesToSkeletonCode(mono.getStereos());
		/*if (stereo.contains("x")) {
			stereo = trimModificationPositions(mono, stereo);
		}*/
		if (!stereo.equals("") && stereo.length() != brankPosition) {
			throw new WURCSExchangeException("There is the excess or shortage of the stereo information.");
		}

		int j = 0;
		for (int i = 2; i < this.numOfAtom; i++) {
			if (this.posToChar.containsKey(i)) continue;
			char carbonDescriptor = (stereo.equals("")) ? 'x' : stereo.charAt(j);
			this.posToChar.put(i, carbonDescriptor);
			j++;
		}

		// Set SkeletonCode
		for (int i = 0; i < this.numOfAtom; i++) {
			this.skeletonCode += this.posToChar.get(i + 1);
		}
	}
	
	private String convertBaseTypesToSkeletonCode (LinkedList<String> _stereos) throws GlycanException {
		String stereoCode = "";

		LinkedList<String> configurations = new LinkedList<>();
		for (String stereo : _stereos) {
			BaseTypeDictionary baseDict = BaseTypeDictionary.forName(stereo);
			if (baseDict == null) throw new GlycanException(stereo + " could not found!");

			String code = baseDict.getStereoCode();

			if (code.endsWith("1")) configurations.add("L");
			if (code.endsWith("2")) configurations.add("D");
			stereoCode = code + stereoCode;
		}
		
		String configuration = "X";
		if (configurations.size() > 0) configuration = configurations.getLast();
		this.configuration = configuration.charAt(0);

		return stereoCode;
	}
	
	private void ModificationToCarbonDescriptor (GlyCoModification _glyCoMod) throws WURCSExchangeException {
		int pos = _glyCoMod.getPositionOne();
		boolean isTerminal = (pos == 1 || pos == this.numOfAtom);
		char carbonDescriptor = this.ModificationTempalteToCarbonDescriptor(_glyCoMod.getModificationTemplate());

		if (carbonDescriptor == 'd' && isTerminal) {
			carbonDescriptor ='m';
		}
		if (carbonDescriptor == 'O') {
			this.anomericPositions.add(pos);
			if (isTerminal) carbonDescriptor = 'o';
		}
		if (pos == 1) {
			this.isAldose = false;
		}
		if (pos == 0) {
			this.unknownPosMap.add("*");
		}
		
		if (carbonDescriptor == 'h' && !isTerminal) {//pos != 1) {
			throw new WURCSExchangeException("Modification \"aldi\" is must set to first carbon.");
		}
		if (carbonDescriptor == 'A' && !isTerminal) {
			throw new WURCSExchangeException("Can not do carboxylation to non-terminal carbon.");
		}
		if (carbonDescriptor == ' ') {
			throw new WURCSExchangeException("Unknown modification is found.");
		}

		this.posToChar.put(pos, carbonDescriptor);
	}
	
	/*private boolean replaceCarbonDescirptorByUnsaturate (GlyCoModification _glyCoMod) {
		int pos1 = _glyCoMod.getPositionOne();
		int pos2 = _glyCoMod.getPositionTwo();

		if (pos1 == 0 && pos2 == 0) {
			this.unknownPosMap.add("**");
		}
		if (pos2 == this.numOfAtom) {
			int pos = pos2;
			pos2 = pos1;
			pos1 = pos;
		}
		boolean atTerminal = (pos1 == 1 || pos1 == this.numOfAtom);
		char carbonDescriptor1 = (this.posToChar.containsKey(pos1)) ? this.posToChar.get(pos1) : ' ';
		char carbonDescriptor2 = (this.posToChar.containsKey(pos2)) ? this.posToChar.get(pos2) : ' ';
		
		if (carbonDescriptor1 == 'o' || carbonDescriptor1 == 'A' || carbonDescriptor2 == 'O' || carbonDescriptor2 == 'C') return false;

		if (atTerminal && pos1 == this.mono.getRingEnd()) {
			if (carbonDescriptor1 == 'c') {
				this.posToChar.put(pos1, 'N');
				this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'n' : 'N');
				return true;
			}
			if (carbonDescriptor1 == 'h') {
				this.posToChar.put(pos1, 'z');
				this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'z' : 'Z');
				return true;
			}
			return false;
		}
		
		if (atTerminal && pos2 == this.mono.getRingEnd()) {
			if (carbonDescriptor1 == 'm') {
				this.posToChar.put(pos1, 'n');
				this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'n' : 'N');
				return true;
			}
			if (carbonDescriptor1 == 'h') {
				this.posToChar.put(pos1, 'f');
				this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'f' : 'F');
				return true;
			}
			return false;
		}
		if (!atTerminal && pos1 == this.mono.getRingEnd()) {
			this.posToChar.put(pos1, 'F');
			this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'f' : 'F');
			return true;
		}

		if (atTerminal && pos1 == this.mono.getRingEnd()) {
			this.posToChar.put(pos1, 'z');
			this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'z' : 'Z');
			return true;
		}
		if (!atTerminal && pos2 == this.mono.getRingEnd()) {
			this.posToChar.put(pos1, (carbonDescriptor2 == 'd') ? 'e' : 'E');
			this.posToChar.put(pos2, 'E');
			return true;
		}
		
		if (pos1 > this.mono.getRingStart() && pos2 < this.mono.getRingEnd()) {
			this.posToChar.put(pos1, (carbonDescriptor1 == 'd') ? 'z' : 'Z');
			this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'z' : 'Z');
			return true;
		}
		
		if (carbonDescriptor1 == 'm') {
			this.posToChar.put(pos1, 'n');
			this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'n' : 'N');
			return true;
		}
		
		if (carbonDescriptor1 == 'h') {
			this.posToChar.put(pos1, 'h');
			this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'f' : 'F');
			return true;
		}

		this.posToChar.put(pos1, (carbonDescriptor1 == 'd') ? 'f' : 'F');
		this.posToChar.put(pos2, (carbonDescriptor2 == 'd') ? 'f' : 'F');
		return true;
	}*/
	
	private boolean isDoublebond (GlyCoModification _glyCoMod) {
		ModificationTemplate modT = _glyCoMod.getModificationTemplate();
		if (modT.equals(ModificationTemplate.UNSATURATION_EL)) return true;
		if (modT.equals(ModificationTemplate.UNSATURATION_FL)) return true;
		if (modT.equals(ModificationTemplate.UNSATURATION_ZL)) return true;
		if (modT.equals(ModificationTemplate.UNSATURATION_EU)) return true;
		if (modT.equals(ModificationTemplate.UNSATURATION_FU)) return true;
		if (modT.equals(ModificationTemplate.UNSATURATION_ZU)) return true;
		return false;
	}

	private char ModificationTempalteToCarbonDescriptor (ModificationTemplate _modT) {
		if (_modT.equals(ModificationTemplate.KETONE_U)) return 'O';
		return _modT.getCarbon();
	}
}
