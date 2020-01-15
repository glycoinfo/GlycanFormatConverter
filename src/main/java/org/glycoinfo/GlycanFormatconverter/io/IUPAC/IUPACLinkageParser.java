package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//TODO : 結合位置のパース処理を統合する

public class IUPACLinkageParser extends SubstituentUtility {

	private HashMap<Node, String> nodeIndex = new HashMap<Node, String>();
	private GlyContainer glyCo = null;
	private IUPACStacker stacker = new IUPACStacker();
	
	public GlyContainer getGlyCo () {
		return this.glyCo;
	}
	
	public IUPACLinkageParser (GlyContainer _glyCo, LinkedHashMap<Node, String> _nodeIndex, IUPACStacker _stacker) {
		glyCo = _glyCo;
		nodeIndex = _nodeIndex;
		stacker = _stacker;
	}
	
	public void start () throws GlycanException {
		// define sub-graph, compositions
		for (Node node : stacker.getNodes()) {
			String notation = nodeIndex.get(node);
			if (!isRootOfFramgnets(notation) && !stacker.isComposition()) continue;
			glyCo.addGlycanUndefinedUnit(makeUndefinedUnit(node, notation));
		}

		for (Node node : stacker.getNodes()) {
			parseLinkage(node);
		}

		if (stacker.isFragment()) {
			Node root = stacker.getRoot();
			GlycanUndefinedUnit und = glyCo.getUndefinedUnitWithIndex(root);
			und.setConnection(root.getParentEdge());
		}
	}

	public GlycanUndefinedUnit makeUndefinedUnit (Node _node, String _notation) throws GlycanException {
		GlycanUndefinedUnit ret = new GlycanUndefinedUnit();
		ret.addNode(_node);
		for(Node parent : parseFragmentParents(_notation)) {
			ret.addParentNode(parent);
		}

		return ret;
	}
	
	public ArrayList<Node> parseFragmentParents (String _fragment) {
		ArrayList<Node> ret = new ArrayList<Node>();
		String anchor = _fragment.substring(_fragment.indexOf("=") + 1, _fragment.length() - 1);

		for(Node node : nodeIndex.keySet()) {
			String notation = nodeIndex.get(node);
			if(notation.equals(_fragment)) continue;

			if (notation.contains("=") && notation.startsWith("?$")) {
				String target;

				/* for composition */
				if (notation.endsWith(",")) target = notation.substring(notation.indexOf("=")+1, notation.length()-1);
				else target = notation.substring(notation.indexOf("=")+1, notation.length());

				if (anchor.equals(target)) ret.add(node);
			}
			if ((notation.contains("|") || notation.contains("$")) &&!notation.contains("=")) {
				/* resolve multiple anchor */
				String temp = "";
				for (int i = 0; i < notation.length(); i++) {
					char unit = notation.charAt(i);

					if (String.valueOf(unit).matches("\\d")) temp += unit;
					if (unit == '$') {
						temp += unit;
						if (anchor.endsWith(temp)) ret.add(node);
						temp = "";
					}
				}
			}
		}

		return ret;
	}

	public void parseLinkage (Node _node) throws GlycanException {
		String notation = nodeIndex.get(_node);
		Node parent = stacker.getParent(_node);

		/* parse parent linkage position
		* group 1 : start repeating position
		* group 2 : repeating count
		* group 3 : minimum count number
		* group 4 : maximum count number
		* group 5 : start cyclic position or multiple parent position
		* */
		Matcher matStartRep = Pattern.compile("\\(([\\d?/?]+)[\u2192\\-]](([n\\d]+)-?([n\\d]+)?)(-.+|:.+)?").matcher(notation);

		/* parse start repetition */
		if(matStartRep.find()) {
			parseRepeating(_node, parent, matStartRep.group(5));
		} else {
			parseSimpleLinkage(_node, parent, notation);
		}

		/* parse cyclic */
		if(isEndCyclic(notation)) {
			parseCyclic(_node, getIndex(nodeIndex.size() - 1));
		}

		if (parent == null && !glyCo.containsNode(_node)) {
			glyCo.addNode(_node);
		}
	}
	
	private void parseSimpleLinkage (Node _node, Node _parent, String _notation) throws GlycanException{
		String linkage = extractLinkageNotation(_notation);

		for(String unit : linkage.split(":")) {
			/*
			  group(1) : child pos
			  group(3) : child side modification position
			  group(4) : cross-linked substituent
			  group(5) : parent side modification position
			  group(7) : probability low
			  group(8) : probability high
			  group(9) : parent pos
			  */

			Matcher matLin = Pattern.compile("([\\d?])+(\u002D(\\d)?([(a-zA-Z)]+)+(\\d)?)?[\u002D\u2192\u2194]?(([\\d?]+),?([\\d?]+)?%)?([\\d?/]+)?").matcher(unit);

			if(matLin.find()) {
				Edge parentEdge = new Edge();
				Linkage lin = new Linkage();
				lin.setChildLinkages(makeLinkageList(matLin.group(1)));

				// for cross linked substituent
				if(matLin.group(4) != null) {
					SubstituentInterface subface = BaseCrossLinkedTemplate.forIUPACNotation(matLin.group(4));

					Substituent bridge = new Substituent(subface, new Linkage(), new Linkage());

					bridge.getFirstPosition().setParentLinkages(makeLinkageList(matLin.group(1)));
					bridge.getSecondPosition().setParentLinkages(makeLinkageList(matLin.group(9)));

					String headPos = matLin.group(5);
					String tailPos = matLin.group(3);

					// HEAD is parent, Tail is child
					if (headPos != null) {
						bridge.getFirstPosition().addChildLinkage(Integer.parseInt(headPos));
					}
					if (tailPos != null) {
						bridge.getSecondPosition().addChildLinkage(Integer.parseInt(tailPos));
					}

					parentEdge.setSubstituent(bridge);
					bridge.addParentEdge(parentEdge);
				}

				/* extract probability annotation */
				if (matLin.group(7) != null) {
					if (matLin.group(7).equals("?")) lin.setProbabilityLower(-1.0);
					else lin.setProbabilityLower(Double.parseDouble(matLin.group(7)) * .01);
				}
				if (matLin.group(8) != null) {
					if (matLin.group(8).equals("?")) lin.setProbabilityUpper(-1.0);
					else lin.setProbabilityUpper(Double.parseDouble(matLin.group(8)) * .01);
				} else if (matLin.group(8) == null && matLin.group(7) != null){
					if (matLin.group(7).equals("?")) lin.setProbabilityUpper(-1.0);
					else lin.setProbabilityUpper(Double.parseDouble(matLin.group(7)) * .01);
				}

				if(matLin.group(9) != null) lin.setParentLinkages(makeLinkageList(matLin.group(9)));
				parentEdge.addGlycosidicLinkage(lin);

				if(_parent != null) {
					if (!stacker.isFragment()) glyCo.addNode(_parent, parentEdge, _node);
					else {
						GlycanUndefinedUnit und = glyCo.getUndefinedUnitWithIndex(stacker.getRoot());
						und.addNode(_parent, parentEdge, _node);
					}
				}

				/* for root of antennae */
				if(matLin.group(9) != null && _parent == null) {
					_node.addParentEdge(parentEdge);
					parentEdge.setChild(_node);
				}
			}			
		}
	}
	
	private void parseRepeating (Node _node, Node _parent, String _repOutParent) throws GlycanException {
		String childPos = "";
		String parentPos = "";
		String count = "";
		//ArrayList<String> repPos = extractMultipleRepStart(_node);
		TreeMap<Integer, String> repPos = extractMultipleRepStart(_node);
		ArrayList<Node> endNodes = getEndRepeatingNode(_node);

		for (Node endRep : endNodes) {
			String startPos = repPos.get(endNodes.indexOf(endRep) + 1);
			Matcher matStartRep = Pattern.compile("([\\d?/?]+)[\u2192\\-]](([n\\d]+)-?([n\\d]+)?)").matcher(startPos);
			if (matStartRep.find()) {
				childPos = matStartRep.group(1);
				count = matStartRep.group(2);
			}

			/*
			* group 2 : cross linked substituent
			* group 3 : child linkage position
			* */
			Matcher matEndRep = Pattern.compile("\\[(\u002D(\\w)\u002D)?([\\d?/]+)\\)").matcher(nodeIndex.get(endRep));
			childPos = (childPos != null) ? childPos : "?";
			parentPos = "?";

			Edge repeatEdge = new Edge();
			Linkage repeatLin = new Linkage();
			if (matEndRep.find()) {
				if (matEndRep.group(3) != null) parentPos = matEndRep.group(3);

				repeatLin.setChildLinkages(makeLinkageList(childPos));
				repeatLin.setParentLinkages(makeLinkageList(parentPos));
				repeatEdge.addGlycosidicLinkage(repeatLin);

				SubstituentInterface subface = null;

				/* for substituent */
				if (matEndRep.group(2) != null) {
					subface = BaseCrossLinkedTemplate.forIUPACNotation(matEndRep.group(2));
				}

				GlycanRepeatModification repMod = new GlycanRepeatModification(subface);

				repMod.setFirstPosition(new Linkage());
				repMod.setSecondPosition(new Linkage());

				String[] repCount = count.split("-");
				String min = "n";
				String max = "n";
				min = repCount[0];
				if (repCount.length == 2) max = repCount[1];

				repMod.setMaxRepeatCount(max.equals("n") ? -1 : Integer.parseInt(max));
				repMod.setMinRepeatCount(min.equals("n") ? -1 : Integer.parseInt(min));
				repeatEdge.setSubstituent(repMod);

				repMod.addParentEdge(repeatEdge);

				glyCo.addNode(endRep, repeatEdge, _node);
			}

		}

		if (_parent != null) {
			/* child - parent */
			Edge parentEdge = new Edge();
			Linkage lin = new Linkage();

			if (_repOutParent != null) {
				Matcher matParent = Pattern.compile("([\\d?])(\u002D([(\\w)]+))?[\u002D\u2192\u2194]([\\d?/]+)\\)").matcher(_repOutParent);

				if (matParent.find()) {
					childPos = matParent.group(1);
					parentPos = matParent.group(4);

					SubstituentInterface subface = null;

					// for substituent
					if (matParent.group(3) != null) {
						subface = BaseCrossLinkedTemplate.forIUPACNotation(matParent.group(3));

						Substituent bridge = new Substituent(subface, new Linkage(), new Linkage());

						parentEdge.setSubstituent(bridge);
						bridge.addParentEdge(parentEdge);
					}
				}

				//TODO : parse probability annotation
			}

			lin.setChildLinkages(makeLinkageList(childPos));
			lin.setParentLinkages(makeLinkageList(parentPos));
			parentEdge.addGlycosidicLinkage(lin);
			glyCo.addNode(_parent, parentEdge, _node);
		}
	}
	
	private void parseCyclic (Node _node, Node _startCyclic) throws GlycanException {
		String start = nodeIndex.get(_startCyclic);
		String current = nodeIndex.get(_node).replace("]-", "");
		String childPos = start.substring(start.length() - 2, start.length() - 1); //extractLinkageNotation(start).substring(0, 1);
		
		Linkage lin = new Linkage();		
		lin.addChildLinkage(childPos.equals("?") ? -1 : Integer.parseInt(childPos));

		String child = "";
		for (int i =0; i < current.length(); i++) {
			char uni = current.charAt(i);
			if (uni == ')') break;
			if (String.valueOf(uni).matches("\\d")) child = child + uni;
		}
		lin.setParentLinkages(makeLinkageList(child));

		Edge cyclicEdge = new Edge();
		cyclicEdge.addGlycosidicLinkage(lin);
		
		GlycanRepeatModification repMod = new GlycanRepeatModification(null);
		repMod.setMaxRepeatCount(1);
		repMod.setMinRepeatCount(1);
		cyclicEdge.setSubstituent(repMod);
		
		glyCo.addNode(_node, cyclicEdge, _startCyclic);
	}
	
	private String extractLinkageNotation (String _linkage) {
		if (_linkage.indexOf("-(") == -1) return "";
		_linkage = _linkage.substring(_linkage.indexOf("-(") + 1, _linkage.length());
		
		if(_linkage.matches("^\\(.+")) _linkage = _linkage.substring(1, _linkage.length());
		if(_linkage.lastIndexOf(")") != -1) _linkage = _linkage.substring(0, _linkage.lastIndexOf(")"));
	
		return _linkage;
	}
	
	private ArrayList<Node> getEndRepeatingNode (Node _node) {
		int size = new ArrayList<>(nodeIndex.keySet()).indexOf(_node) + 1;

		ArrayList<Node> nodes = stacker.getNodes();

		List<Node> subNodes = nodes.subList(0, size);

		Collections.reverse(subNodes);

		ArrayList<Node> ret = countRepeats(subNodes);

		if (ret.isEmpty()) {
			ret = countRepeats(stacker.getNodes().subList(stacker.getNodes().indexOf(_node), stacker.getNodes().size()));
		}

		return ret;
	}

	private ArrayList<Node> countRepeats (Collection<Node> _nodes) {
		int numOfstart = 0;
		String regex = "\\[(\u002D[\\w()]+\u002D)?[\\d\\?/]+\\)";
		Node ret = null;
		ArrayList<Node> retNodes = new ArrayList<Node>();

		/* extract current repeating unit */
		Node start = new ArrayList<>(_nodes).get(0);
		TreeMap<Integer, String> repPos = extractMultipleRepStart(start);

		for (Integer key : repPos.keySet()) {
			numOfstart = key;

			for (Node node : _nodes) {
				String notation = nodeIndex.get(node);
				if (!isStartRep(notation) && !isEndRep(notation)) continue;
				if (isStartRep(notation) && !node.equals(start)) {
					numOfstart+= extractMultipleRepStart(node).size();
				}

				Matcher matEndRep = Pattern.compile(regex).matcher(notation);

				while (matEndRep.find()) {
					String repStatus = matEndRep.group(0);
					if (isEndRep(repStatus + "-")) {
						if (numOfstart != 0) numOfstart--;
						if (numOfstart == 0) {
							ret = node;
							retNodes.add(node);
							break;
						}
					}
					notation = notation.replaceFirst(regex, "");
					matEndRep = Pattern.compile(regex).matcher(notation);
				}

				if (numOfstart == 0) break;
			}
		}

		return retNodes;
	}

	private TreeMap<Integer, String> extractMultipleRepStart (Node _node) {
		String repStart = nodeIndex.get(_node);
		TreeMap<Integer, String> repPosMap = new TreeMap<>();
		int key = 1;
		for (String pos : repStart.substring(repStart.indexOf("-(") + 2, repStart.length()).split(":")) {
			if (isStartRep(pos)) {
				repPosMap.put(key, pos);//repPos.add(pos);
				key++;
			}
		}

		return repPosMap;
	}

	private Node getStartRepeatingNode (Node _node) {
		Node ret = null;
		
		int start = new ArrayList<> (nodeIndex.keySet()).indexOf(_node);
		
		for(int i = start; i < nodeIndex.size(); i++) {
			if(this.isStartRep(stacker.getNotationByIndex(i))) {
				ret = getIndex(i);
				break;
			}
		}
		return ret;
	}
	
	private Node getIndex (int _ind) {
		return stacker.getNodeByIndex(_ind);
	}
	
	private boolean isEndCyclic (String _notation) {
		if(_notation.indexOf("]-") == 0) _notation = _notation.substring(2, _notation.length());
		if (_notation.indexOf("-") == 0) _notation = _notation.substring(1, _notation.length());
		return (_notation.matches("^\\d\\).+"));
	}
	
	private boolean isStartRep (String _notation) {
		return (_notation.matches(".*\\(*[\\d?]+[\u2192\u002D]][\\w\\d].*$"));
	}
	
	private boolean isEndRep (String _notation) {
		return (_notation.matches("^.*\\[(\u002D\\w\u002D)?[\\d?/]+\\).+"));
	}

	private boolean isRootOfFramgnets (String _notation) {
		if (_notation.lastIndexOf("$,") != -1) return true;
		else if (_notation.lastIndexOf("$") == _notation.length() -1) return true;

		return false;
	}

	private LinkedList<Integer> makeLinkageList (String _pos) {
		LinkedList<Integer> ret = new LinkedList<Integer>();
		
		for(String pos : _pos.split("/")) {
			if(pos.equals("?")) ret.addLast(-1);
			else ret.addLast(Integer.parseInt(pos));
		}
		
		return ret;
	}
}
