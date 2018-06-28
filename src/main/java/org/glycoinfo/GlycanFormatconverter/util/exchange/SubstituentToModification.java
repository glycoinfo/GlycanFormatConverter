package org.glycoinfo.GlycanFormatconverter.util.exchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.Linkage;
import org.glycoinfo.GlycanFormatconverter.Glycan.LinkageType;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.WURCSFramework.util.exchange.SubstituentTypeToMAP;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;

public class SubstituentToModification {

	private Substituent substituent;
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
		this.substituent = _sub;
		
		if (_sub.getSubstituent() == null) return;
		
		this.subTypeToMap = SubstituentTypeToMAP.forName(_sub.getSubstituent().getglycoCTnotation());
		
		this.headAtom = this.subTypeToMap.getHeadAtom();
		this.tailAtom = this.subTypeToMap.getTailAtom();
		
		ArrayList<LinkageType> linkageTypes = new ArrayList<LinkageType>();
		if (this.substituent.getParentEdge() != null) {
			this.parentEdge = this.substituent.getParentEdge();
		}
		if (this.parentEdge == null) {
			throw new WURCSExchangeException("Substituent must have parent linkage.");
		}
		if (!this.substituent.getChildEdges().isEmpty()) {
			this.childEdge = this.substituent.getChildEdges().get(0);
		}

		if (this.substituent.getFirstPosition() != null) {
			linkageTypes.add(this.substituent.getFirstPosition().getParentLinkageType());
		}
		if (this.substituent.getSecondPosition() != null) {
			linkageTypes.add(this.substituent.getSecondPosition().getParentLinkageType());
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
			//this.parentSidePosition = substituent.getSecondPosition().getChildLinkages().get(0);
			//this.childSidePosition  = substituent.getFirstPosition().getChildLinkages().get(0);
			if (isSwap) {
				parentSidePosition = 2;
				childSidePosition = 1;
				//this.parentSidePosition = substituent.getFirstPosition().getChildLinkages().get(0);
				//this.childSidePosition  = substituent.getSecondPosition().getChildLinkages().get(0);
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
		
		ArrayList<Integer> nums = new ArrayList<Integer>();
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
		for (Iterator<Integer> iterNum = nums.iterator(); iterNum.hasNext();) {
			Integer num1 = iterNum.next();
			if (num1 <= posO) continue;
			Integer num2 = num1 + 1;
			newMAP = newMAP.replaceAll(num1.toString(), num2.toString());
		}
		return newMAP;
	}
	
	private String addOxygenToHead (String _map) {
		if (_map.startsWith("NCCOP")) return _map;
		
		ArrayList<Integer> nums = new ArrayList<Integer>();
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
		for (Iterator<Integer> iterNum = nums.iterator(); iterNum.hasNext();) {
			Integer num1 = iterNum.next();
			Integer num2 = num1 + 1;
			newMAP = newMAP.replaceAll(num1.toString(), num2.toString());
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
