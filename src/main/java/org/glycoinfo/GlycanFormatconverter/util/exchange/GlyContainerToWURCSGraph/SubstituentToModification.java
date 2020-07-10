package org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.LinkageType;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph.SubstituentTypeToMAP;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;

import java.util.ArrayList;
import java.util.Collections;

public class SubstituentToModification {

	private Edge parentEdge = null;
	private Edge childEdge = null;
	private LinkageType parentType;
	private LinkageType childType;

	private int parentSidePosition = 0;
	private int childSidePosition = 0;
	private String headAtom = "";
	private String tailAtom = "";
	private String mapCode = "";

	private SubstituentTypeToMAP subTypeToMap;

	public String getMAPCode() {
		return this.mapCode;
	}

	public int getParentSidePosition () {
		return this.parentSidePosition;
	}

	public int getChildSidePosition () {
		return this.childSidePosition;
	}

	public String getHeadAtom () {
		return this.headAtom;
	}

	public String getTailAtom () {
		return this.tailAtom;
	}

	public void setParentEdge (Edge _edge) {
		this.parentEdge = _edge;
	}

	public void setChildEdge (Edge _edge) {
		this.childEdge = _edge;
	}

	public void start (Substituent _sub) throws WURCSExchangeException {

		if (_sub.getSubstituent() == null) return;

		this.subTypeToMap = SubstituentTypeToMAP.forName(_sub.getSubstituent().getglycoCTnotation());

		this.headAtom = this.subTypeToMap.getHeadAtom();
		this.tailAtom = this.subTypeToMap.getTailAtom();

		ArrayList<LinkageType> linkageTypes = new ArrayList<>();
		if (_sub.getParentEdge() != null) {
			this.parentEdge = _sub.getParentEdge();
		}
		if (this.parentEdge == null) {
			throw new WURCSExchangeException("Substituent must have parent linkage.");
		}
		if (!_sub.getChildEdges().isEmpty()) {
			this.childEdge = _sub.getChildEdges().get(0);
		}

		if (_sub.getFirstPosition() != null) {
			linkageTypes.add(_sub.getFirstPosition().getParentLinkageType());
		}
		if (_sub.getSecondPosition() != null) {
			linkageTypes.add(_sub.getSecondPosition().getChildLinkageType());
		}

		if (linkageTypes.isEmpty()) {
			throw new WURCSExchangeException("Substituent having no linkage is NOT handled in this system.");
		}
		if (linkageTypes.size() > 2) {
			throw new WURCSExchangeException("Substituent having three or more linkage is NOT handled in this system.");
		}

		this.parentType = linkageTypes.get(0);
		this.childType = (linkageTypes.size() == 2) ? linkageTypes.get(1) : null;

		if (this.parentType == LinkageType.UNKNOWN) {
			this.parentType = LinkageType.H_AT_OH;
		}

		String mapDouble = this.subTypeToMap.getMAPDouble();
		if (mapDouble != null && mapDouble.equals("")) return;

		this.mapCode = (this.childType == null) ? this.getMAPCodeSingle() : this.getMAPCodeDouble();
	}

	public String getMAPCodeSingle () {
		String map = this.subTypeToMap.getMAPSingle();

		if (this.parentType == LinkageType.H_AT_OH) {
			this.headAtom = "O";
			map = this.addOxygenToHead(map);
		}
		return "*" + map;
	}

	public String getMAPCodeDouble() {
		String map = this.subTypeToMap.getMAPDouble();
		Boolean isSwap = this.subTypeToMap.isSwapCarbonPositions();
		boolean hasOrder = false;
		if (isSwap == null && this.parentType != this.childType) {
			if (this.parentType == LinkageType.H_AT_OH) {
				isSwap = false;
			} else if (this.childType == LinkageType.H_AT_OH) {
				isSwap = true;
			}
		}

		if (isSwap != null) {
			parentSidePosition = 1;
			childSidePosition = 2;
			if (isSwap) {
				parentSidePosition = 2;
				childSidePosition = 1;
			}
			hasOrder = true;
		} else {
			isSwap = false;
		}

		// Add oxygen
		if (this.parentType == LinkageType.H_AT_OH ) {
			this.headAtom = "O";
			map = (isSwap)? this.addOxygenToTail(map) : this.addOxygenToHead(map);
		}

		if ( this.childType == LinkageType.H_AT_OH ) {
			this.tailAtom = "O";
			map = (isSwap)? this.addOxygenToHead(map) : this.addOxygenToTail(map);
		}

		// Add index to MAP star if it has priority order
		if (hasOrder)
			map = this.addMAPStarIndex(map);

		map = "*" + map;

		map = map.replace("*OP^XO*", "*OPO*");
		map = map.replace("*P^X*", "*P*");

		return map;
	}

	private String addOxygenToTail (String _map) {
		StringBuilder sb = new StringBuilder(_map);
		int pos = _map.lastIndexOf("*");
		sb.insert(pos, 'O');
		_map = sb.toString();

		int posO = 1;
		for (int i=0; i < pos; i++) {
			char c = _map.charAt(i);
			if (c == '^' || c == '/') {
				i++;
				continue;
			} else if (c == '=' || c == '#') {
				continue;
			} else if (c == '*') {
				break;
			}
			posO++;
		}

		ArrayList<Integer> nums = new ArrayList<>();
		String num = "";
		for (int i = 0; i < _map.length(); i++) {
			char c = _map.charAt(i);
			if (Character.isDigit(c)) {
				num += c;
				continue;
			}
			if (num.equals("")) continue;
			if (nums.contains(Integer.parseInt(num))) continue;
			nums.add(Integer.parseInt(num));
			num = "";
		}
		Collections.sort(nums);
		Collections.reverse(nums);

		String newMAP = _map;
		for (Integer num1 : nums) {
			if (num1 <= posO) continue;
			int num2 = num1 + 1;
			newMAP = newMAP.replaceAll(num1.toString(), Integer.toString(num2));
		}
		return newMAP;
	}

	private String addOxygenToHead (String _map) {
		if (_map.startsWith("NCCOP")) return _map;

		ArrayList<Integer> nums = new ArrayList<>();
		String num = "";
		for (int i = 0; i < _map.length(); i++) {
			char c = _map.charAt(i);
			if (Character.isDigit(c)) {
				num += c;
				continue;
			}
			if (num.equals("")) continue;
			if (nums.contains(Integer.parseInt(num))) continue;
			nums.add(Integer.parseInt(num));
			num = "";
		}
		Collections.sort(nums);
		Collections.reverse(nums);

		String newMAP = _map;
		for (Integer num1 : nums) {
			int num2 = num1 + 1;
			newMAP = newMAP.replaceAll(num1.toString(), Integer.toString(num2));
		}
		return "O" + newMAP;
	}

	private String addMAPStarIndex (String _map) {
		StringBuilder sb = new StringBuilder(_map);
		int pos2 = _map.indexOf("*");
		sb.insert(pos2 + 1, '2');
		sb.insert(0, '1');
		return sb.toString();
	}
}
