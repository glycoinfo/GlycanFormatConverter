package org.glycoinfo.GlycanFormatconverter.util.analyzer;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.BaseStereoIndex;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IUPACSubstituentNotationAnalyzer extends SubstituentUtility {

	private ArrayList<Substituent> substituents;

	public ArrayList<Substituent> getSubstituents () {
		return this.substituents;
	}

	public void start (Monosaccharide _mono, ArrayList<String> _substituents) throws GlycanException {
		this.makeSubstituent(_mono, _substituents);
	}

	public void start (String _substituent) throws GlycanException {
		ArrayList<String> temp = resolveSubstituents(_substituent, true);
		this.makeSubstituent(null, temp);
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
				// single substituent
				if (notation.split(",").length == 1) {
					subs.add(notation);
					notation = "";
					isNotation = false;
					isLink = false;
					continue;
				}

				// multiple substituent
				if (notation.split(",").length == toInteger(unit)) {
					subs.add(notation);
					notation = "";
					isNotation = false;
					isLink = false;
					continue;
				}

				// ring substituent
				if (this.isCyclic(notation) && this.isAlphabet(unit)) {
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
			}
		}

		return _isSubstituent ? subs : mods;
	}

	private void makeSubstituent (Monosaccharide _mono, ArrayList<String> _substituents) throws GlycanException {
		substituents = new ArrayList<>();
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
			String[] probs;
			int number = (matSub.group(3) != null) ? Integer.parseInt(matSub.group(3)) : 1;
			
			SubstituentInterface subT;

			//Native substituent
			if(positions == null) {
				String planeNotation = this.makePlaneNotation(notation);
				subT = BaseSubstituentTemplate.forIUPACNotation(planeNotation);

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
				Substituent sub = new Substituent(subT, firstLink);
				sub.setHeadAtom(this.makeHeadAtomFromNotation(notation, planeNotation));

				substituents.add(sub);
				continue;
			}

			//TODO :辞書の更新が必要
			for(String position : positions.split(":")) {
				//Dual linkages
				if((positions.contains(":") && position.contains(",")) || (position.contains(",") && number == 1)) {
					String planeNotation = this.makePlaneNotation(notation);
					subT = BaseCrossLinkedTemplate.forIUPACNotation(planeNotation);

					// check unsupport substituent
					this.checkSupportSubstituent(subT);

					String[] pos = position.split(",");

					String[] firstPos = extractPos(pos[0]);
					String[] secondPos = extractPos(pos[1]);
					String[] firstProb = trimProbability(pos[0]);
					String[] secondProb = trimProbability(pos[1]);

					Linkage firstLink = makeLinkage(firstPos[0], firstPos[1], extractProbability(firstProb[0]), extractProbability(firstProb[1]));
					Linkage secondLink = makeLinkage(secondPos[0], secondPos[1], extractProbability(secondProb[0]), extractProbability(secondProb[1]));

					//Modify LinkageType and SubstituentTemplate
					Substituent sub = new Substituent(subT, firstLink, secondLink);
					sub.setHeadAtom(this.makeHeadAtomFromNotation(notation, planeNotation));
					//sub.setTailAtom(this.makeHeadAtom(subT));

					// check deoxy substituent with H_LOSE
					this.isDeoxySubstituentWithHLose(_mono, sub, this.makeHeadAtomFromNotation(notation, planeNotation));

					substituents.add(sub);
					continue;
				}

				//fuzzy linkage positions
				if(position.contains("/")) {
					String planeNotation = this.makePlaneNotation(notation);
					subT = BaseSubstituentTemplate.forIUPACNotation(planeNotation);

					probs = this.trimProbability(position);
					String pos = extractPos(position)[0];
					Linkage firstLink = makeLinkage(pos, "1", extractProbability(probs[0]), extractProbability(probs[1]));

					//Modify LinkageType and SubstituentTemplate
					Substituent sub = new Substituent(subT, firstLink);
					sub.setHeadAtom(this.makeHeadAtomFromNotation(notation, planeNotation));

					//this.modifyHeadAtom(_mono, sub, pos);

					// check deoxy substituent with H_LOSE
					this.isDeoxySubstituentWithHLose(_mono, sub, this.makeHeadAtomFromNotation(notation, planeNotation));

					substituents.add(sub);
					continue;
				}

				//Single linkage
				for(String multi : position.split(",")) {
					String planeNotation = this.makePlaneNotation(notation);
					subT = BaseSubstituentTemplate.forIUPACNotation(planeNotation);

					probs = trimProbability(multi);
					String pos = this.extractPos(multi)[0];
					Linkage firstLink = makeLinkage(matSub.group(1).equals("?") ? "-1" : pos, "1", extractProbability(probs[0]), extractProbability(probs[1]));

					Substituent sub = new Substituent(subT, firstLink);
					sub.setHeadAtom(this.makeHeadAtomFromNotation(notation, planeNotation));

					this.modifyHeadAtom(_mono, sub, pos);

					// check deoxy substituent with H_LOSE
					this.isDeoxySubstituentWithHLose(_mono, sub, this.makeHeadAtomFromNotation(notation, planeNotation));

					substituents.add(sub);
				}
			}			
		}
	}

	private Linkage makeLinkage (String _parentPos, String _childPos, double _probabilityLow, double _probabilityHigh) {
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

		if (!_linkagepos.contains("(")) return ret;

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
		if (_position.contains("(")) _position = _position.substring(0, _position.indexOf("("));

		if (_position.contains("-")) {
			ret = _position.split("-");
		} else {
			ret[0] = _position;
			ret[1] = "1";
		}

		return ret;
	}

	private String makeHeadAtomFromNotation (String _notation, String _planeNotaiton) {
		String ret;

		if (_notation.equals("N") && _planeNotaiton.equals("N")) return _planeNotaiton;

		if (_notation.startsWith("(")) {
			String bracket = _notation.substring(0, _notation.indexOf(")") + 1);

			// make regex and remove molecular direction
			String regex = bracket.replace("(", "\\(").replace(")", "\\)");
			_notation = _notation.replaceFirst(regex, "");
			_planeNotaiton = _planeNotaiton.replaceFirst(regex, "");

		}
		ret = _notation.replaceFirst(_planeNotaiton, "");

		return ret;
	}

	private void modifyHeadAtom (Monosaccharide _mono, Substituent _sub, String _position) {
		if (_position.equals("?") || _position.equals("")) return;
		if (_mono == null) return;
		for (GlyCoModification gMod : _mono.getModifications()) {
			if (gMod.getPositionOne() != Integer.parseInt(_position)) continue;
			if (this.isHLOSE(gMod)) {
				_sub.setHeadAtom("C");
			}
		}
	}

	private String makePlaneNotation (String _notation) throws GlycanException {
		if (_notation.equals("N") || _notation.matches("C(l|Fo|Me)")) return _notation;
		if (_notation.startsWith("C")) return _notation.substring(1);
		if (_notation.startsWith("(")) {
			String bracket = _notation.substring(0, _notation.indexOf(")") + 1);
			String regex = bracket.replace("(", "\\(").replace(")", "\\)");
			_notation = _notation.replaceFirst(regex, "");

			if (_notation.equals("CE")) {
				return bracket + _notation;
			}
			if (_notation.startsWith("C")) {
				throw new GlycanException("IUPAC importer can not support " + _notation);
				//_notation = _notation.substring(1);
			}

			return bracket + _notation;
		}

		return _notation;
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

	private boolean isCyclic (String _notation) {
		return (_notation.matches("([\\d?]-[\\d?]),([\\d?]-[\\d?]).+"));
	}

	private void checkSupportSubstituent (SubstituentInterface _subInter) throws GlycanException {
		if (_subInter instanceof BaseSubstituentTemplate) return;
		if (_subInter.equals(BaseCrossLinkedTemplate.X_PYRUVATE) ||
		_subInter.equals(BaseCrossLinkedTemplate.R_PYRUVATE) ||
		_subInter.equals(BaseCrossLinkedTemplate.S_PYRUVATE) ||
		_subInter.equals(BaseCrossLinkedTemplate.X_DEOXYPYRUVATE) ||
		_subInter.equals(BaseCrossLinkedTemplate.R_DEOXYPYRUVATE) ||
		_subInter.equals(BaseCrossLinkedTemplate.S_DEOXYPYRUVATE)) {
			throw new GlycanException("IUPAC importer can not support " + _subInter);
		}
	}

	private void isDeoxySubstituentWithHLose (Monosaccharide _mono, Substituent _sub, String _headAtom) throws GlycanException {
		SubstituentInterface subInter = _sub.getSubstituent();
		boolean ret = false;
		if (_headAtom.equals("N")) return;
		if (_headAtom.equals("") &&
				(!subInter.equals(BaseSubstituentTemplate.CFORMYL) &&
				!subInter.equals(BaseSubstituentTemplate.CMETHYL))) return;

		for (GlyCoModification gMod : _mono.getModifications()) {
			if (!this.isHLOSE(gMod)) {
				ret = true;
				break;
			}
		}

		// is modification is empty
		if (_mono.getModifications().isEmpty()) {
			throw new GlycanException("The (C)-type linkage of substituent requires some kind of H_LOSE modification at linkage position.");
		}
		//
		if (ret) {
			throw new GlycanException("The (C)-type linkage of substituent requires some kind of H_LOSE modification at linkage position.");
		}
	}

	private boolean isHLOSE (GlyCoModification _gMod) {
		return (_gMod.getModificationTemplate().equals(ModificationTemplate.HLOSE_5) ||
				_gMod.getModificationTemplate().equals(ModificationTemplate.HLOSE_6) ||
				_gMod.getModificationTemplate().equals(ModificationTemplate.HLOSE_7) ||
				_gMod.getModificationTemplate().equals(ModificationTemplate.HLOSE_8) ||
				_gMod.getModificationTemplate().equals(ModificationTemplate.HLOSE_X));
	}
}