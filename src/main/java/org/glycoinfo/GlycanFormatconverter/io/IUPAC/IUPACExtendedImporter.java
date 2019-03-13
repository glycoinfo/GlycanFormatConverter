package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.GlyContainerOptimizer;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;

public class IUPACExtendedImporter {

	public GlyContainer start(String _iupac) throws GlycanException, GlyCoImporterException, ConverterExchangeException {
		GlyContainer glyCo = new GlyContainer();
		
		LinkedHashMap<Node, String> nodeIndex = new LinkedHashMap<Node, String>();
		
		List<String> notations = new ArrayList<String>();

		// separate glycan fragents
		if (_iupac.indexOf("$,") != -1) {
			for (String unit : _iupac.split("\\$,")) {
				if (unit.matches(".+=[\\d?]+")) unit += "$,";
				notations.add(unit);
			}
			Collections.reverse(notations);
		}

		// separate monosaccharide composition
		if (_iupac.matches(".*}\\d+")) {
			notations = parseCompositionUnits(_iupac);
		}
		
		if (notations.isEmpty()) notations.add(_iupac);
		
		/**/
		for (String subst : notations) {
			IUPACStacker stacker = new IUPACStacker();
			
			// check monosaccharide composition
			Matcher matComp = Pattern.compile("^\\{.+}(\\d+)+$").matcher(subst);
			if (matComp.find()) {
				String count = matComp.group(1);
				subst = subst.replaceFirst("\\{", "");
				subst = subst.substring(0, subst.indexOf("}" + count));
				stacker.setNumOfNode(Integer.parseInt(count));
			}
			
			stacker.setNotations(parseNotation(subst));
			
			// generate moonsaccharide
			for (String unit : stacker.getNotations()) {
				if (stacker.isComposition()) {
					for (int i = stacker.getNumOfNode(); i != 0; i--) {
						Node node = makeNode(unit);
						nodeIndex.put(node, unit);
						stacker.addNode(node);
					}					 
				} else {
					Node node = makeNode(unit);
					nodeIndex.put(node, unit);
					stacker.addNode(node);
				}
			}

			// define family in each nodes
			parseChildren(stacker, nodeIndex);

			// define linkages
			IUPACLinkageParser iupacLP = new IUPACLinkageParser(glyCo, nodeIndex, stacker);
			iupacLP.start();

			glyCo = iupacLP.getGlyCo();
		}

		//
		GlyContainerOptimizer gcOpt = new GlyContainerOptimizer();
		gcOpt.start(glyCo);

		return glyCo;
	}

	private Node makeNode (String _notation) throws GlycanException, GlyCoImporterException {
		IUPACNotationParser iupacNP = new IUPACNotationParser();
		Node ret = iupacNP.parseMonosaccharide(_notation);
		return ret;
	}

	private ArrayList<String> parseNotation(String _iupac) {
		ArrayList<String> ret = new ArrayList<String>();

		String mono = "";
		boolean isLinkage = false;
		boolean isRepeat = false;
		boolean isbisect = false;
		boolean isMultipleParent = false;
		for (int i = 0; i < _iupac.length(); i++) {
			mono += _iupac.charAt(i);

			if (_iupac.charAt(i) == '(') isLinkage = true;

			if (isbisect && _iupac.charAt(i) == ']') {
				ret.add(mono);
				mono = "";
				isbisect = false;
			}

			/* for end of multiple parent */
			if (isMultipleParent && _iupac.charAt(i) == ')') {
				if (_iupac.charAt(i + 1) == ']' && _iupac.charAt(i + 2) != '-') {
					isbisect = true;
					isMultipleParent = false;
					continue;
				}
				ret.add(mono);
				mono = "";
				isMultipleParent = false;
			}

			/* for linkage */
			if (isLinkage && _iupac.charAt(i) == ')') {
				if (_iupac.charAt(i + 1) == '=') {
					isLinkage = false;
					continue;
				}
				if (_iupac.charAt(i + 1) == ']' && _iupac.charAt(i + 2) != '-') {
					isbisect = true;
					isLinkage = false;
					continue;
				}

				if (String.valueOf(_iupac.charAt(i + 1)).matches("[a-zA-Z,]")) continue;
				ret.add(mono);
				mono = "";
				isLinkage = false;
			}
			/* for repeating */
			if (isLinkage && _iupac.charAt(i) == ']') {
				isLinkage = false;
				isRepeat = true;
				continue;
			}
			/* for root */
			if ((_iupac.length() - 1) == i) {
				ret.add(mono);
				break;
			}
			/* for repeating count */
			if (isRepeat) {
				if (String.valueOf(_iupac.charAt(i)).matches("[\\dn]")) {
					if (String.valueOf(_iupac.charAt(i + 1)).matches("\\d")) continue;
					if (_iupac.charAt(i + 1) == '-' && String.valueOf(_iupac.charAt(i + 2)).matches("[\\dn\\(]")) {
						continue;
					}
					if (_iupac.charAt(i - 1) == '(' && _iupac.charAt(i + 1) == '\u2192') continue;
					isRepeat = false;
				}

				if (_iupac.charAt(i + 1) == ':') {
					isMultipleParent = true;
					continue;
				}

				if (!isRepeat) {
					ret.add(mono);
					mono = "";
				}

			}
		}

		return ret;
	}

	private void parseChildren(IUPACStacker _stacker, LinkedHashMap<Node, String> _index) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.addAll(_stacker.getNodes());

		Collections.reverse(nodes);

		for (Node node : nodes) {
			String current = _index.get(node);
			if (haveChild(current)) {
				int childIndex = nodes.indexOf(node) + 1;
				Node child = nodes.get(childIndex);
				_stacker.addFamily(child, node);
			}

			if (isStartOfBranch(current)) {
				int childIndex = nodes.indexOf(node) + 1;
				Node child = nodes.get(childIndex);
				_stacker.addFamily(child, node);

				for (Node cNode : pickChildren(nodes, node, _index)) {
					_stacker.addFamily(cNode, node);
				}
			}
		}
	}

	private ArrayList<String> parseCompositionUnits (String _iupac) {
		ArrayList<String> ret = new ArrayList<>();
		String notation = "";
		boolean isEndBracket = false;
		
		for (int i = 0; i < _iupac.length(); i++) {
			if (isEndBracket && _iupac.charAt(i) == ',') {
				ret.add(notation);
				notation = "";
				isEndBracket = false;
				continue;
			}
			
			notation = notation + _iupac.charAt(i);
			
			if ((i+1) == _iupac.length()) ret.add(notation);
			if (i > 0 && _iupac.charAt(i) == '}') isEndBracket = true;
		}
		
		return ret;
	}
	
	private ArrayList<Node> pickChildren (ArrayList<Node> _nodes, Node _branch, LinkedHashMap<Node, String> _index) {
		ArrayList<Node> children = new ArrayList<Node>();
		int count = 0;
		boolean isChild = false;

		if (isStartOfBranch(_index.get(_branch))) count = -1;

		for (Node node : _nodes.subList(_nodes.indexOf(_branch) + 1, _nodes.size())) {
			String notation = _index.get(node);
			if (isChild) {
				children.add(node);
			}

			if (count == 0 && !isBisecting(notation)) {
				if (isStartOfBranch(notation)) break;
				if (isEndOfBranch(notation)) break;
				if (haveChild(notation)) break;
			}

			if (isStartOfBranch(notation)) count--;
			if (isEndOfBranch(notation)) count++;
			if (isBisecting(notation)) count--;

			if (count == 0) {
				if (isBisecting(notation)) isChild = true;
				if (isEndOfBranch(notation)) isChild = true;
				continue;
			}

			isChild = false;
		}

		return children;
	}

	private boolean isBisecting (String _notation) {
		return (_notation.endsWith("]"));
	}

	private boolean haveChild (String _notation) {
		return (_notation.startsWith("-"));
	}

	private boolean isStartOfBranch (String _notation) {
		return (_notation.startsWith("]-"));
	}

	private boolean isEndOfBranch (String _notation) {
		//if(isBisecting(_notation)) return false;
		if (_notation.matches("\\[\\d\\).+")) return false;
		return (_notation.startsWith("["));
	}
}