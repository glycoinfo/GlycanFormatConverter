package org.glycoinfo.GlycanFormatconverter.util.analyzer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glycoinfo.GlycanFormatconverter.Glycan.SuperClass;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.BaseStereoIndex;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameDictionary;

public class ThreeLetterCodeAnalyzer {

	private ArrayList<String> substituents;
	private ArrayList<String> modifications;
	private LinkedList<String> stereos;
	private SuperClass superclass;
	private String coreNotation;

	public ThreeLetterCodeAnalyzer () {
		this.substituents = new ArrayList<>();
		this.modifications = new ArrayList<>();
		this.stereos = new LinkedList<>();
	}

	public ArrayList<String> getSubstituents () {
		return this.substituents;
	}
	
	public ArrayList<String> getModificaitons () {
		return this.modifications;
	}
	
	public LinkedList<String> getStereos () {
		return this.stereos;
	}
	
	public SuperClass getSuperClass () {
		return this.superclass;
	}

	public String getCoreNotation() {
		if ( coreNotation == null ) return coreNotation;
		String prefix = String.valueOf(coreNotation.charAt(0));
		coreNotation = coreNotation.replaceFirst(prefix, prefix.toUpperCase());
		return this.coreNotation;
	}

	public void analyzeTrivialName (String _trivialName, LinkedList<String> _templates) throws GlyCoImporterException {
		//group 1 : deoxy notation
		//group 2 : three letter code
		//group 3 : super class
		Matcher matTriv = Pattern.compile("(\\dd)?([a-z]{2,3})+([A-Z][a-z]{1,2})+").matcher(_trivialName);
		BaseStereoIndex bsi;
		String stereo = "";

		if(matTriv.find()) {
			/* analyze trivial name */
			String sugarName = "";
			if (matTriv.group(1) != null) sugarName += matTriv + matTriv.group(1);
			if (matTriv.group(2) != null) {
				sugarName += matTriv.group(2);
				coreNotation = matTriv.group(2);
			}

			TrivialNameDictionary trivDict = TrivialNameDictionary.forThreeLetterCode(sugarName);

			if(trivDict != null) {
				stereo = trivDict.getStereos();
				extractModifications(trivDict.getModificationNotation());
				extractSubstituents(trivDict.getSubstituentNotation());
			} else {
				bsi = BaseStereoIndex.forCode(sugarName);
				if(bsi != null) stereo = matTriv.group(2);
			}

			superclass = SuperClass.forSuperClass(matTriv.group(3));
			
			if (!stereo.contains("_") && _templates.size() == 2) {
				stereos.addFirst(_templates.getFirst().toLowerCase());
				stereos.addLast(stereo.toLowerCase());
			} else extractStereos(stereo);
		} else {
			TrivialNameDictionary trivDict = TrivialNameDictionary.forThreeLetterCode(_trivialName);

			if(trivDict != null) {
				stereo = trivDict.getStereos();
				superclass = SuperClass.forSize(trivDict.getSize());
				extractModifications(trivDict.getModificationNotation());
				extractSubstituents(trivDict.getSubstituentNotation());
			} else {
				stereo = _trivialName;

				if (!_trivialName.equals("Sugar")) {
					bsi = BaseStereoIndex.forCode(_trivialName);

					if (bsi == null) superclass = SuperClass.forSuperClass(_trivialName);
					else superclass = SuperClass.forSize(bsi.getSize());
				} else superclass = SuperClass.SUG;
			}

			if (!isSuperClass(_trivialName)) extractStereos(stereo);
		}
		
		if(superclass == null) throw new GlyCoImporterException("Monosaccharide size could not defined.");
		//if(stereos.isEmpty()) throw new GlyCoImporterException("Stereo could not correctly defined.");
	}

	private boolean isSuperClass (String _threeLetterCode) {
		SuperClass superclass = SuperClass.forSuperClassWithIgnore(_threeLetterCode);
		return (superclass != null);
	}

	private void extractStereos (String _temp) {
		for(String unit : _temp.split("_")) {
			stereos.add(unit.toLowerCase());
		}
	}
	
	private void extractSubstituents (String _temp) {
		if(_temp.equals("")) return;
		for(String unit : _temp.split("_")) {
			String[] parts = unit.split("\\*");
			substituents.add(parts[0] + parts[1]);
		}
	}
	
	private void extractModifications (String _temp) {
		if(_temp.equals("")) return;
		for(String unit : _temp.split("_")) {
			String[] parts = unit.split("\\*");
			if (parts[1].equals("6")) continue;
			modifications.add(parts[0] + parts[1]);
		}
	}
}
