package org.glycoinfo.GlycanFormatconverter.Glycan;

public enum LinkageType {

	//http://www.monosaccharidedb.org/notation.action?topic=subst
	H_AT_OH('o'),
	DEOXY('d'),
	H_LOSE('h'),
	R_CONFIG('r'),
	S_CONFIG('s'),
	UNKNOWN('x'),
	UNVALIDATED('u'),
	NONMONOSACCHARIDE('n');
	
	private char symbol;
	
	LinkageType(char _symbol) {
		this.symbol = _symbol;
	}
	
	public char getSymbol () {
		return this.symbol;
	}
	
	public static LinkageType forType (char _symbol) {
		for(LinkageType type : LinkageType.values()) {
			if(type.symbol == _symbol) return type;
		}
		
		return null;
	}
}
