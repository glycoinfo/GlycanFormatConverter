package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.MonosaccharideUtility;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.SubstituentIUPACNotationAnalyzer;
import org.glycoinfo.GlycanFormatconverter.util.analyzer.ThreeLetterCodeAnalyzer;

public class IUPACNotationParser {

	public Node parseMonosaccharide (String _iupacNotation) throws GlycanException, GlyCoImporterException{
		/* remove */
		String temp = this.trim(_iupacNotation);
		
		String linkagePos = "";
		String anomericState = "";
		Monosaccharide mono = new Monosaccharide();
		ArrayList<String> subNotation = new ArrayList<String>();
		ArrayList<String> modifications = new ArrayList<String>();
		LinkedList<String> configurations = new LinkedList<String>();
		LinkedList<String> trivialName = new LinkedList<String>();

		SubstituentIUPACNotationAnalyzer subAna = new SubstituentIUPACNotationAnalyzer();
		MonosaccharideUtility monoUtil = new MonosaccharideUtility();

		/* extract linkage positions */
		if(temp.indexOf("-(") != -1 ) {
			linkagePos = temp.substring(temp.indexOf("-(") + 1, temp.length());
			temp = temp.replace("-" + linkagePos, "");
		}
		if (temp.indexOf(")-") != -1 ) {
			temp = temp.substring(temp.indexOf(")-") + 2);
		}
		
		/* parse fragment by substituent */
		//group 1 : anchor
		//group 2 : notation
		//group 3 : fragments ID
		Matcher matSub = Pattern.compile("^([?\\d])+([(\\w)]+)+=(\\d\\$)").matcher(_iupacNotation);
		if (matSub.find()) {
			subAna.start(matSub.group(1) + matSub.group(2));
			Substituent sub = subAna.getSubstituents().get(0);
			Edge parentEdge = new Edge();
			parentEdge.addGlycosidicLinkage(sub.getFirstPosition());
			parentEdge.setSubstituent(sub);
			sub.addParentEdge(parentEdge);
			return sub;
		}
		
		/* extract anhydro and deoxy */
		//group 1 : anhydro (3,9-dideoxy-L-gro-α-L-manNon2ulop5N7NFo-onic-)
		//group 2 : deoxy block (3,9-dideoxy-L-gro-α-L-manNon2ulop5N7NFo-onic-)
		//group 5 : anomeric state
		//group 6 : anhydro
		//group 7 : deoxy (β-3,4-Anhydro-3,4,7-trideoxy-L-lyxHepp2,6N2-)
		//group 8 : configuration
		Matcher matMod = Pattern.compile("([\\d,:]*-Anhydro-)?([\\d,?]*-\\w*deoxy-)?([LD?]-\\w{3}-)?((aldehyde|\\?|\u03B1|\u03B2)-)?([\\d,:]*-Anhydro-)?([\\d,?]*-\\w*deoxy-)?([DL?])?").matcher(temp);
		
		if(matMod.find()) {
			/* extract anomeric state */
			if(matMod.group(5) != null && matMod.group(8) != null) {
				if (!matMod.group(5).equals("aldehyde")) anomericState = matMod.group(5);
				else modifications.add(matMod.group(5));

				String regex = matMod.group(4).replace("?", "\\?");
				temp = temp.replaceFirst(regex, "");
			}
			
			/* extract anhydro */
			if(matMod.group(1) != null || matMod.group(6) != null) {
				String anhydro = (matMod.group(1) != null) ? matMod.group(1) : matMod.group(6);
				subNotation.addAll(monoUtil.resolveNotation(trimTail(anhydro)));
				temp = temp.replace(anhydro, "");
			}
			/* extract modifications */
			if(matMod.group(2) != null || matMod.group(7) != null) {
				String deoxy = "";
				if (matMod.group(2) != null) deoxy = matMod.group(2);
				if (matMod.group(7) != null) deoxy = matMod.group(7);
				modifications.addAll(monoUtil.resolveNotation(trimTail(deoxy)));
				temp = temp.replace(deoxy, "");
			}
		}

		/* extract configuration and trivial name */
		for(String unit : temp.split("-")) {
			if(unit.equals("D") || unit.equals("L") || unit.equals("?")) {
				configurations.addLast(unit);
				if (unit.equals("?")) unit = unit.replace(unit, "\\" + unit);
				temp = temp.replaceFirst(unit + "-", "");
			}
			if(unit.equals("ol") || unit.equals("onic") || unit.equals("aric") || unit.equals("uronic")) {
				modifications.add(unit);
				temp = temp.replace("-" + unit, "");
			}
		}

		if (temp.matches("[a-z]{3}-.+")) {//temp.indexOf("gro-") != -1) {
			trivialName.add(temp.substring(0, temp.indexOf("-")));
			trivialName.add(temp.substring(temp.indexOf("-") + 1, temp.length()));
		} else {
			trivialName.add(temp);
		}

		if(modifications.contains("ol")) {
			anomericState = "";
		}

		String coreNotation = trivialName.getLast();
		String threeLetterCode = "";

		//group 1 : trivial name ([A-Z]{1}[a-z]{2}\\d?[A-Z]{1}[a-z]{1,2} : Neu5Ac, [a-z]{2,3}[A-Z]{1}[a-z]{2} : lyxHex, araHex,  [A-Z]{1}[a-z]{2} : Glc)
		//group 2 : ulosonic
		Matcher matCore = Pattern.compile("(Sugar|Ko|[a-z]{2,3}[A-Z]{1}[a-z]{2}|6?d?i?[A-Z]{1}[a-z]{2})+((\\dulo)+)?").matcher(coreNotation);
		if(matCore.find()) {

			/* extract trivial name and super class */
			if(matCore.group(1) != null) {
				ThreeLetterCodeAnalyzer threeCode = new ThreeLetterCodeAnalyzer();
				threeCode.analyzeTrivialName(matCore.group(1), trivialName);

				if (threeCode.getCoreNotation() != null) {
					threeLetterCode = threeCode.getCoreNotation();
				} else {
					threeLetterCode = matCore.group(1);
				}

				mono.setStereos(threeCode.getStereos());
				mono.setSuperClass(threeCode.getSuperClass());
				subNotation.addAll(threeCode.getSubstituents());

				modifications.addAll(threeCode.getModificaitons());
				coreNotation = coreNotation.replace(matCore.group(1), "");
			}
			/* extract ulosonic */
			if(matCore.group(2) != null) {
				String ulosonic = matCore.group(2);
				while (ulosonic.length() != 0) {
					String unit = ulosonic.substring(0, 4);
					modifications.add(unit);
					ulosonic = ulosonic.replaceFirst(unit, "");
				}
				coreNotation = coreNotation.replace(matCore.group(2), "");
			}
		}

		/* make anomeric state */
		mono.setAnomer(convertAnomericState(mono, anomericState));

		/* define anomeric position */
		mono.setAnomericPosition(extractAnomericPosition(mono, linkagePos));
		
		/* extract ring size and substituents */
		//group 1 : ring size
		//group 2 : substituents
		Matcher matTail = Pattern.compile("([pf?])?([\\d,\\w/:(%)\\-\\?]+)?").matcher(coreNotation);
		if (matTail.find() && (matTail.group(1) != null || matTail.group(2) != null)) {
			boolean isRingSize = false;

			if (matTail.group(2) == null) isRingSize = true;
			if (matTail.group(2) != null) {
				if (coreNotation.length() > 1 && String.valueOf(coreNotation.charAt(1)).matches("[\\dNA\\\\?]"))
					isRingSize = true;
			}
			if (matTail.group(1) == null) isRingSize = false;
			
			/* extract ring size */
			if (isRingSize) {
				mono = monoUtil.makeRingSize(mono, matTail.group(1), threeLetterCode, modifications);
			}
			
			/* extract substituents */
			if(matTail.group(2) != null) {
				String subNotations = "";
				if (!isRingSize && matTail.group(1) != null) {
					subNotations += matTail.group(1);
				}
				subNotations += matTail.group(2);

				if (subNotations.startsWith("A")) {
					String acid = String.valueOf(mono.getSuperClass().getSize()) + subNotations.charAt(0);
					modifications.add(acid);
					subNotations = subNotations.replaceFirst(String.valueOf(subNotations.charAt(0)), "");
				}

				if (subNotations != null) {
					subNotation.addAll(subAna.resolveSubstituents(subNotations, true));
					modifications.addAll(subAna.resolveSubstituents(subNotations, false));
				}
			}
		}
		
		/* make modifications */
		mono = monoUtil.appendModifications(mono, modifications);

		/* append substituents */
		mono = monoUtil.appendSubstituents(mono, subNotation);
		
		/* append configuration */
		mono = monoUtil.modifyStereos(mono, configurations);
		
		/* check and modify configuration */
		mono = monoUtil.checkTruelyConfiguration(threeLetterCode, configurations, mono);
		
		return mono;
	}

	private int extractAnomericPosition (Monosaccharide _mono, String _linkage) {
		if(_linkage.equals("")) {
			if (_mono.getAnomericPosition() != 0) return _mono.getAnomericPosition();
			return Monosaccharide.OPEN_CHAIN;
		}
		int childPos = Monosaccharide.UNKNOWN_RING;
		AnomericStateDescriptor anomer = _mono.getAnomer();

		boolean isAnomeric = false;
		
		for (String unit : _linkage.split(":")) {
			if (unit.matches("\\(.+")) unit = this.trimHead(unit);
			if (unit.indexOf("\u2192") != -1 || unit.indexOf("\u2194") != -1) {
				childPos = this.charToInt(unit.charAt(0));
				isAnomeric = true;
			}
			if (isAnomeric) break;
		}

		if (anomer.equals(AnomericStateDescriptor.OPEN)) return Monosaccharide.OPEN_CHAIN;
		if (_mono.getAnomericPosition() != 0 && childPos == -1) childPos = _mono.getAnomericPosition();

		return childPos;
	}

	private AnomericStateDescriptor convertAnomericState (Monosaccharide _mono, String _anomeric) {
		switch (_anomeric) {
			case "?" :
				if (_mono.getAnomericPosition() == -1) return AnomericStateDescriptor.UNKNOWN;
				else return AnomericStateDescriptor.UNKNOWN_STATE;

			case "" :
				return AnomericStateDescriptor.OPEN;

			case "\u03B1" :
				return AnomericStateDescriptor.ALPHA;

			case "\u03B2" :
				return AnomericStateDescriptor.BETA;
		}

		return null;
	}

	private int charToInt (char _char) {
		if (_char == '?') return -1;
		return (Integer.parseInt(String.valueOf(_char)));
	}
	
	private String trim (String _notation) {
		String ret = _notation;	
		
		/* remove anchor*/
		if (_notation.matches(".+(=\\d+\\$,?)+")) ret = _notation.replaceAll("(=\\d+\\$,?)", "");
		if (_notation.matches(".*([\\?\\d+]\\$\\|?)+.+")) ret = ret.replaceFirst("([\\?\\d+]\\$\\|?)+", "");
		
		ret = (ret.indexOf(")-") != -1) ? ret.substring(ret.indexOf(")-") + 1, ret.length()) : ret;
		ret = (ret.startsWith("[")) ? ret.replaceAll("\\[", "") : ret;
		ret = (ret.startsWith("]-")) ? ret.replaceAll("]-", "") : ret;
		ret = (ret.startsWith("-")) ? ret.replaceFirst("-", "") : ret;
		ret = (ret.endsWith("]")) ? ret.replaceFirst("\\]", "") : ret;
		
		return ret;
	}

	private String trimTail (String _temp) {
		return _temp.substring(0, _temp.length() - 1);
	}
	
	private String trimHead (String _temp) {
		return _temp.substring(1, _temp.length());
	}
}