package org.glycoinfo.GlycanFormatconverter.util.analyzer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.BaseStereoIndex;

public class SubstituentIUPACNotationAnalyzer extends SubstituentUtility {

	private ArrayList<Substituent> substituents;

	public ArrayList<Substituent> getSubstituents () {
		return this.substituents;
	}

	public void start (Monosaccharide _mono, ArrayList<String> _substituents) throws GlycanException {
		init();

		this.makeSubstituent(_mono, _substituents);
		//this.margeSubstituents();
	}

	public void start (String _substituent) throws GlycanException, GlyCoImporterException {
		init();
		ArrayList<String> temp = resolveSubstituents(_substituent, true);
		makeSubstituent(null, temp);
	}

	public ArrayList<String> resolveSubstituents (String _notations, boolean _isSubstituent) {
		String notation = "";
		ArrayList<String> subs = new ArrayList<>();
		ArrayList<String> mods = new ArrayList<>();

		boolean isLink = false;
		boolean isNotation = false;

		for (int i = 0; i < _notations.length(); i++) {
			char unit = _notations.charAt(i);

			if (isInteger(unit)) isLink = true;
			if (isAlphabet(unit)) isNotation = true;

			notation += unit;

			if (notation.contains("en") || notation.equals("??")) {
				mods.add(notation);
				notation = "";
				isNotation = false;
				isLink = false;
				continue;
			}

			if (i == (_notations.length()-1)) {
				subs.add(notation);
				break;
			}

			if (isLink && isNotation && isInteger(_notations.charAt(i+1))) {
				if (notation.split(",").length == 1) {
					subs.add(notation);
					notation = "";
					isNotation = false;
					isLink = false;
					continue;
				}

				if (notation.split(",").length == toInteger(unit)) {
					subs.add(notation);
					notation = "";
					isNotation = false;
					isLink = false;
					continue;
				}

			}

			if (!isLink && isNotation && isInteger(_notations.charAt(i+1))) {
				subs.add(notation);
				notation = "";
				isNotation = false;
				isLink = false;
				continue;
			}
		}

		return _isSubstituent ? subs : mods;
	}

	private void makeSubstituent (Monosaccharide _mono, ArrayList<String> _substituents) throws GlycanException {

		String regex = "([\\d?:/,(%)-]+(?![RSX]\\)))?([(a-zA-Z?\\-)]+)+(\\d)?([\\d?])?";

		for(String unit : _substituents) {
			/*
			* group 1 : position(low, high)
			* group 2 : notation
			* group 3 : number
			* */
			Matcher matSub = Pattern.compile(regex).matcher(unit);

			if(!matSub.find()) continue;
			
			String positions = matSub.group(1);
			String notation = matSub.group(2);
			String[] probabilitys;
			int number = (matSub.group(3) != null) ? Integer.parseInt(matSub.group(3)) : 1;
			
			SubstituentInterface subT;

			//Native substituent
			if(positions == null) {
				subT = SubstituentTemplate.forIUPACNotation(notation);

				BaseStereoIndex bsi = null;
				if (!_mono.getStereos().isEmpty()) {
					bsi = BaseStereoIndex.forCode(_mono.getStereos().getFirst());
					if(bsi != null && bsi.getSize() == 6) positions = "2";
					if (bsi != null && bsi.getSize() != 6 && _mono.getSuperClass().equals(SuperClass.HEX)) bsi = null;
				}
				if (bsi == null && _mono.getSuperClass().equals(SuperClass.HEX)) {
					positions = "2";
				}

				Linkage firstLink = makeLinkage(positions, "1", 1.0D, 1.0D);
				substituents.add(modifyLinkageType(new Substituent(subT, firstLink)));
				continue;
			} 
			
			for(String position : positions.split(":")) {
				//Dual linkages
				if((positions.indexOf(":") != -1 && position.contains(",")) || (position.contains(",") && number == 1)) {
					subT = CrossLinkedTemplate.forIUPACNotation(notation);
					String[] pos = position.split(",");

					String[] firstPos = extractPos(pos[0]);
					String[] secondPos = extractPos(pos[1]);
					String[] firstProb = trimProbability(pos[0]);
					String[] secondProb = trimProbability(pos[1]);

					Linkage firstLink = makeLinkage(firstPos[0], firstPos[1], extractProbability(firstProb[0]), extractProbability(firstProb[1]));
					Linkage secondLink = makeLinkage(secondPos[0], secondPos[1], extractProbability(secondProb[0]), extractProbability(secondProb[1]));

					substituents.add(modifyLinkageType(new Substituent(subT, firstLink, secondLink)));
					
					continue;
				}

				//fuzzy linkage positions
				if(position.contains("/")) {
					subT = SubstituentTemplate.forIUPACNotation(notation);
					probabilitys = this.trimProbability(position);
					Linkage firstLink = makeLinkage(extractPos(position)[0], "1", extractProbability(probabilitys[0]), extractProbability(probabilitys[1]));
					substituents.add(modifyLinkageType(new Substituent(subT, firstLink)));
					continue;
				}

				//Singele linkage
				for(String multi : position.split(",")) {
					subT = SubstituentTemplate.forIUPACNotation(notation);
					probabilitys = trimProbability(multi);
					Linkage firstLink = makeLinkage(matSub.group(1).equals("?") ? "-1" : extractPos(multi)[0], "1", extractProbability(probabilitys[0]), extractProbability(probabilitys[1]));
					substituents.add(modifyLinkageType(new Substituent(subT, firstLink)));
                }
			}			
		}
	}
	
	/**
	 * _childPos 1 : first linkage
	 * _childPos 2 : second linkage
	 * @param _parentPos
	 * @param _childPos
	 * @throws GlycanException 
	 * @return
	 */
	private Linkage makeLinkage (String _parentPos, String _childPos, double _probabilityLow, double _probabilityHigh) throws GlycanException {
		Linkage ret = new Linkage();

		ret.setProbabilityLower(_probabilityLow == 1.0D ? 1.0D :
										_probabilityLow == -1.0 ? -1.0D : _probabilityLow * .01);
		ret.setProbabilityUpper(_probabilityHigh == 1.0D ? 1.0D :
										_probabilityHigh == -1.0 ? -1.0D : _probabilityHigh * .01);

		ret.addChildLinkage(Integer.parseInt(_childPos));

		for (String parentPos : _parentPos.split("/")) {
			ret.addParentLinkage(Integer.parseInt(parentPos.equals("?") ? "-1" : parentPos));
		}

		return ret;
	}

	public  void margeSubstituents() throws GlycanException {
		boolean isAmino = false;

		for(int ind : new Integer[] {2,4,5,7}) {
			ArrayList<Substituent> pick = this.pickSubstituents(ind);
			if(pick.size() != 2) continue;
			Substituent temp = null;
			for (Substituent unit : pick) {
				SubstituentTemplate subT = (SubstituentTemplate) unit.getSubstituent();
				if (subT.equals(SubstituentTemplate.AMINE)) {
					isAmino = true;
					temp = unit;
					continue;
				}
				if (isAmino) {
					SubstituentInterface subInt = convertOTypeToNType(subT);
					unit.setTemplate(subInt);
					if (subInt instanceof SubstituentTemplate) {
						unit.getFirstPosition().setParentLinkageType(LinkageType.UNVALIDATED);
					}

					substituents.remove(temp);
					temp = null;
					isAmino = false;
				}
			}
		}
	}

	private ArrayList<Substituent> pickSubstituents (int _ind) {
		ArrayList<Substituent> ret = new ArrayList<Substituent>();
		for (Substituent sub : this.substituents) {
			if (sub.getSecondPosition() != null || sub.getFirstPosition().getParentLinkages().size() > 1) continue;
			if (sub.getFirstPosition().getParentProbabilityLower() != 100.0 || sub.getFirstPosition().getParentProbabilityUpper() != 100.0) continue;
			if (_ind == sub.getFirstPosition().getParentLinkages().get(0)) {
				ret.add(sub);
			}
		}

		return ret;
	}

	private double extractProbability (String _pribability) {
		double ret = 1;
		if (_pribability == null) return ret;
		_pribability = _pribability.replace("%", "");
		ret = _pribability.equals("?") ? -1.0 : Double.parseDouble(_pribability.replace("%", ""));
//		ret = (double) temp;
		return ret;
	}

	private String[] trimProbability(String _linkagepos) {
		String[] ret = new String[2];
		ret[0] = "1.0D";
		ret[1] = "1.0D";

		if (_linkagepos.indexOf("(") == -1) return ret;

		String probability = _linkagepos.substring(_linkagepos.indexOf("(") + 1, _linkagepos.indexOf(")"));

		for (String unit : probability.split(",")) {
			if (ret[0] != null) ret[1] = unit.replace("%", "");
			ret[0] = unit.replace("%", "");
		}

		if (ret[1] == null) ret[1] = ret[0];

		return ret;
	}

	private String[] extractPos (String _position) {
		String[] ret = new String[2];
		if (_position == null) return null;
		if (_position.indexOf("(") != -1) _position = _position.substring(0, _position.indexOf("("));

		if (_position.indexOf("-") != -1) {
			ret = _position.split("-");
		} else {
			ret[0] = _position;
			ret[1] = "1";
		}

		return ret;
	}

	private SubstituentTemplate modifyEndSubstituent (Monosaccharide _mono, String _position, SubstituentTemplate _subT) {
		if (_position.equals("?") || !_subT.equals(SubstituentTemplate.AMINE)) return _subT;

		if (_mono.getSuperClass().getSize() == Integer.parseInt(_position) ||
				Integer.parseInt(_position) == 1) return SubstituentTemplate.AMINO;

		return _subT;
	}

	private boolean isInteger (char _int) {
		return String.valueOf(_int).matches("\\d|\\?");
	}

	private boolean isAlphabet (char _alphabet) {
		return String.valueOf(_alphabet).matches("[A-Za-z]");
	}

	private Integer toInteger (char _unit) {
		if (!String.valueOf(_unit).matches("\\d")) return -1;
		return (Integer.parseInt(String.valueOf(_unit)));
	}

	private void init() {
		substituents = new ArrayList<>();
	}
}