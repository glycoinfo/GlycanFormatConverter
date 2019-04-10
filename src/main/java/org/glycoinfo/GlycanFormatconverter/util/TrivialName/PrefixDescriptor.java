package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

public enum PrefixDescriptor {

	UNKNOWN("", 0),
	MONO("", 1),
	DI("di", 2),
	TRI("tri", 3),
	TETRA("tetra", 4),
	PENTA("penta", 5),
	HEXA("hexa", 6),
	HEPTA("hepta", 7),
	OCTA("octa", 8),
	NONA("nona", 9),
	DECA("deca", 10);
	
	private String a_sPrefix;
	private int a_iNumber;
	
	PrefixDescriptor(String _a_sPrefix, int _a_iNumber) {
		this.a_sPrefix = _a_sPrefix;
		this.a_iNumber = _a_iNumber;
	}
	
	public String getPrefix() {
		return this.a_sPrefix;
	}
	
	public static PrefixDescriptor forPrefix(String _a_sPrefix) throws TrivialNameException {
		PrefixDescriptor[] enum_array = PrefixDescriptor.values();
		
		for(PrefixDescriptor enum_str : enum_array) {
			if(_a_sPrefix.equals(enum_str.a_sPrefix)) return enum_str;
		}
		
		throw new TrivialNameException(_a_sPrefix + " is not found");
	}
	
	public static PrefixDescriptor forNumber(int _a_iNumber) throws TrivialNameException {
		PrefixDescriptor[] enum_array = PrefixDescriptor.values();
		
		for(PrefixDescriptor enum_str : enum_array) {
			if(_a_iNumber == (enum_str.a_iNumber)) return enum_str;
		}
		
		throw new TrivialNameException (_a_iNumber + " is illegal paramertor");
	}
}
