package org.glycoinfo.GlycanFormatconverter.io.IUPAC.extended;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStacker;
import org.glycoinfo.GlycanFormatconverter.util.SubstituentUtility;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//TODO : 結合位置のパース処理を統合する

public class IUPACLinkageParser extends SubstituentUtility {

	private final HashMap<Node, String> nodeIndex;
	private final GlyContainer glyCo;
	private final IUPACStacker stacker;
	
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

		/*
		if (stacker.isFragment()) {
			Node root = stacker.getRoot();
			GlycanUndefinedUnit und = glyCo.getUndefinedUnitWithIndex(root);
			und.setConnection(root.getParentEdge());
		}
		 */
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
		ArrayList<Node> ret = new ArrayList<>();
		String anchor = _fragment.substring(_fragment.indexOf("=") + 1, _fragment.length() - 1);

		for(Node node : nodeIndex.keySet()) {
			String notation = nodeIndex.get(node);
			if(notation.equals(_fragment)) continue;

			if (notation.contains("=") && notation.startsWith("?$")) {
				String target;

				// for composition
				if (notation.endsWith(",")) target = notation.substring(notation.indexOf("=")+1, notation.length()-1);
				else target = notation.substring(notation.indexOf("=")+1);

				if (anchor.equals(target)) ret.add(node);
			}
			if ((notation.contains("|") || notation.contains("$")) &&!notation.contains("=")) {
				// resolve multiple anchor
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
		Matcher matStartRep = Pattern.compile("\\(([\\d?/]+)[\u2192\\-]](([n\\d]+)-?([n\\d]+)?)(-.+|:.+)?").matcher(notation);

		/*
		 * ?P=1$,
		 * {Position}{Notation}={Number}$,
		 * group 1 : linkage position
		 */
		Matcher matSubFrag = Pattern.compile("([\\d?]).+=\\d+\\$,").matcher(notation);

		// parse start repetition
		if (matStartRep.find()) {
			parseRepeating(_node, parent, matStartRep.group(5));
		} else if (matSubFrag.find()) {
			if (_node instanceof Substituent) {
				parseFragmentsLinkage(_node, matSubFrag.group(1));
			}
			if (_node instanceof Monosaccharide) {
				parseSimpleLinkage(_node, parent, notation);
			}
		} else {
			parseSimpleLinkage(_node, parent, notation);
		}

		// parse cyclic
		if(isEndCyclic(notation)) {
			parseCyclic(_node, getIndex(nodeIndex.size() - 1));
		}

		if (parent == null &&
				(!glyCo.containsNode(_node) && !glyCo.containsAntennae(_node))) {
			glyCo.addNode(_node);
		}
	}
	
	private void parseSimpleLinkage (Node _node, Node _parent, String _notation) throws GlycanException{
		String linkage = extractLinkageNotation(_notation);
		int count = 0;

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

			Matcher matLin = Pattern.compile("([\\d?])+([\u002D\u2190](\\d)?([(a-zA-Z)]+)+(\\d)?)?[\u002D\u2192\u2194]?(([\\d?]+),?([\\d?]+)?%)?([\\d?/]+)?").matcher(unit);

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

					// HEAD is parent, Tail is child
					if (matLin.group(5) != null) {
						bridge.getSecondPosition().addChildLinkage(Integer.parseInt(matLin.group(5)));
					}
					if (matLin.group(3) != null) {
						bridge.getFirstPosition().addChildLinkage(Integer.parseInt(matLin.group(3)));
					}

					parentEdge.setSubstituent(bridge);
					bridge.addParentEdge(parentEdge);
				}

				// extract probability annotation
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
					//Assign anomeric state for acceptor, only anomeric-anomeric linkages
					_parent = this.modifyMonosaccharideState(_parent, matLin.group(0), matLin.group(9));
					if (!stacker.isFragment()) glyCo.addNode(_parent, parentEdge, _node);
					else {
						GlycanUndefinedUnit und = glyCo.getUndefinedUnitWithIndex(stacker.getRoot());
						und.addNode(_parent, parentEdge, _node);
					}
				}

				// for root of antennae
				if(matLin.group(9) != null && _parent == null) {
					_node.addParentEdge(parentEdge);
					parentEdge.setChild(_node);
					GlycanUndefinedUnit und = glyCo.getUndefinedUnitWithIndex(_node);
					parentEdge.setParent(und.getParents().get(count));
					und.setConnection(parentEdge);
					und.addConnection(parentEdge);
				}
			}
			count++;
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
			Matcher matStartRep = Pattern.compile("([\\d?/]+)[\u2192\\-]](([n\\d]+)-?([n\\d]+)?)").matcher(startPos);
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
				String min;
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

					SubstituentInterface subface;

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
		String end = nodeIndex.get(_node).replaceFirst("([]\\-]+)", "");
		String startPos = start.substring(start.length() - 2, start.length() - 1);
		
		Linkage lin = new Linkage();		
		lin.addChildLinkage(startPos.equals("?") ? -1 : Integer.parseInt(startPos));

		String endPos = String.valueOf(end.charAt(0));
		lin.setParentLinkages(makeLinkageList(endPos));

		Edge cyclicEdge = new Edge();
		cyclicEdge.addGlycosidicLinkage(lin);
		
		GlycanRepeatModification repMod = new GlycanRepeatModification(null);
		repMod.setMaxRepeatCount(1);
		repMod.setMinRepeatCount(1);
		cyclicEdge.setSubstituent(repMod);
		
		glyCo.addNode(_node, cyclicEdge, _startCyclic);
	}

	private void parseFragmentsLinkage (Node _node, String _linkagePosition) throws GlycanException {
		if (_node instanceof Monosaccharide) return;
		GlycanUndefinedUnit und = glyCo.getUndefinedUnitWithIndex(_node);
		for (Node coreNode : und.getParents()) {
			Edge acceptor = new Edge();
			Linkage lin = new Linkage();

			lin.setParentLinkages(this.makeLinkageList(_linkagePosition));
			lin.setChildLinkages(this.makeLinkageList("1"));
			acceptor.setSubstituent(_node);
			acceptor.addGlycosidicLinkage(lin);
			acceptor.setParent(coreNode);
			und.addConnection(acceptor);
			_node.addParentEdge(acceptor);
		}
		und.setConnection(und.getConnections().get(0));
	}

	private String extractLinkageNotation (String _linkage) {
		if (!_linkage.contains("-(")) return "";
		_linkage = _linkage.substring(_linkage.indexOf("-(") + 1);
		
		if(_linkage.matches("^\\(.+")) _linkage = _linkage.substring(1);
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
		String regex = "\\[(\u002D[\\w()]+\u002D)?[\\d?/]+\\)";
		ArrayList<Node> retNodes = new ArrayList<>();

		// extract current repeating unit
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
		for (String pos : repStart.substring(repStart.indexOf("-(") + 2).split(":")) {
			if (isStartRep(pos)) {
				repPosMap.put(key, pos);
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
		_notation = _notation.replaceFirst("([]\\-]+)", "");
		return (_notation.matches("^[\\d?]\\).+"));
	}
	
	private boolean isStartRep (String _notation) {
		return (_notation.matches(".*\\(*[\\d?]+[\u2192\u002D]][\\w\\d].*$"));
	}
	
	private boolean isEndRep (String _notation) {
		return (_notation.matches("^.*\\[(\u002D\\w\u002D)?[\\d?/]+\\).+"));
	}

	private boolean isRootOfFramgnets (String _notation) {
		if (_notation.lastIndexOf("$,") != -1) return true;
		else return _notation.lastIndexOf("$") == _notation.length() - 1;
	}

	private LinkedList<Integer> makeLinkageList (String _pos) {
		LinkedList<Integer> ret = new LinkedList<>();
		
		for(String pos : _pos.split("/")) {
			if(pos.equals("?")) ret.addLast(-1);
			else ret.addLast(Integer.parseInt(pos));
		}
		
		return ret;
	}

	private Node modifyMonosaccharideState (Node _acceptor, String _linkage, String _acceptorPos) throws GlycanException {
		if (_acceptor instanceof Substituent) return _acceptor;
		if (!_linkage.matches(".+\u2194.+")) return _acceptor;

		Monosaccharide acceptor = (Monosaccharide) _acceptor;
		String acceptorNotation = nodeIndex.get(_acceptor);

		// remove linkage notation
		if (acceptorNotation.contains("-(")) {
			acceptorNotation = acceptorNotation.substring(0, acceptorNotation.indexOf("-("));
		}

		// parse ring size
		Matcher matMono = Pattern.compile(".+([pf?]).*").matcher(acceptorNotation);
		String ringSize = "";
		if (matMono.find()) {
			ringSize = matMono.group(1);
		}

		// assign anomeric position
		if (_acceptorPos.equals("1") || _acceptorPos.equals("2")) {
			acceptor.setAnomericPosition(Integer.parseInt(_acceptorPos));
		} else {
			acceptor.setAnomericPosition(Monosaccharide.UNKNOWN_RING);
		}

		if (ringSize.equals("")) {
			String stereo = acceptor.getStereos().getFirst();
			stereo = stereo.length() == 4 ? stereo.substring(1) : stereo;
			MonosaccharideIndex mi = MonosaccharideIndex.forTrivialNameWithIgnore(stereo);
			ringSize = mi.getRingSize();
		}

		if (ringSize.equals("p")) {
			if (acceptor.getAnomericPosition() == 1) {
				acceptor.setRing(1, 5);
			}
			if (acceptor.getAnomericPosition() == 2) {
				acceptor.setRing(2, 6);
			}
		} else if (ringSize.equals("f")) {
			if (acceptor.getAnomericPosition() == 1) {
				acceptor.setRing(1, 4);
			}
			if (acceptor.getAnomericPosition() == 2) {
				acceptor.setRing(2, 5);
			}
		} else {
			acceptor.setRing(acceptor.getAnomericPosition(), Monosaccharide.UNKNOWN_RING);
		}

		return _acceptor;
	}
}
