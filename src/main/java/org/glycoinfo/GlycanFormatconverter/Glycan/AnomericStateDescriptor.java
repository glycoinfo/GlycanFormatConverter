package org.glycoinfo.GlycanFormatconverter.Glycan;

public enum AnomericStateDescriptor {
	
	ALPHA('a', "alpha"),//\u03B1
	BETA('b', "beta"),//\u03B2
	OPEN('o', ""),
	UNKNOWN_STATE('x', "?"),
	UNKNOWN('?', "?");
	
	private char a_cNotation;
	private String a_sIUPACNotaiton;
	
	public String getIUPACAnomericState() {
		return this.a_sIUPACNotaiton;
	}
	
	public char getAnomericState() {
		return this.a_cNotation;
	}
	
	AnomericStateDescriptor(char _a_cNotation, String _a_sIUPACNotation) {
		this.a_cNotation = _a_cNotation;
		this.a_sIUPACNotaiton = _a_sIUPACNotation;
	}
	
	public static AnomericStateDescriptor forAnomericState (char _a_sNotation){
		AnomericStateDescriptor[] enum_array = AnomericStateDescriptor.values();
		
		for(AnomericStateDescriptor enum_str : enum_array) {
			if(_a_sNotation == enum_str.a_cNotation) return enum_str;
		}
		
		return null;
	}
}
