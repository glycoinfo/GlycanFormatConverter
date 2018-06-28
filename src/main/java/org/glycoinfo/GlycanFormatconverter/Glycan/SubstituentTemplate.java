package org.glycoinfo.GlycanFormatconverter.Glycan;

public enum SubstituentTemplate implements SubstituentInterface {
	
	ETHYR				("*OCC", "ethyl", "Et"),
	METHYL				("*OC", "methyl", "OMe"),
	C_METHYL			("*C", "methyl", "CMe"),
	N_METHYL			("*NC", "n-methyl", "NMe"),
	ACETYL				("*OCC/3=O", "acetyl", "Ac"),
	dACETYL				("*CC/2=O", "acetyl", "Ac"),
	//D_ACETYL			("*CO/3=O", "acetyl", "Ac", "subtituent"),
	N_ACETYL			("*NCC/3=O", "n-acetyl", "NAc"),
	GLYCOLYL			("*OCCO/3=O", "glycolyl", "Gc"),
	N_GLYCOLYL			("*NCCO/3=O", "n-glycolyl", "NGc"),
	ETHANOL				("*OCCO", "ethanolamine", "EtOH"),
	ETHANOLAMINE		("*NCCO", "ethanolamine", "NEtOH"),
	SULFATE				("*OSO/3=O/3=O", "sulfate", "S"),
	N_SULFATE			("*NSO/3=O/3=O", "n-sulfate", "NS"),
	FORMYL				("*OC=O", "formyl", "OFo"),
	C_FORMYL			("*C=O", "formyl", "CFo"),
	N_FORMYL			("*NC=O", "n-formyl", "NFo"),
	AMIDINO				("*OCN/3=N", "amidino", "Am"),
	N_AMIDINO			("*NCN/3=N", "n-amidino", "NAm"),
	SUCCINATE			("*OCCCCO/6=O/3=O", "succinate", "Suc"),
	N_SUCCINATE			("*NCCCCO/6=O/3=O", "n-succinate", "NSuc"),
	DIMETHYL			("*OC/2C", "dimethyl", "DiMe"),
	N_DIMETHYL			("*NC/2C", "n-dimethyl", "NDiMe"),
	PHOSPHATE			("*OPO/3O/3=O", "phosphate", "P"),
	//PHOSPHOCHOLINE		("*OP^XOCCN/6N/6N/3O/3=O", "phospho-choline", "PCho"),
	PHOSPHOCHOLINE		("*OP^XOCCNC/7C/7C/3O/3=O", "phospho-choline", "PCho"),
	DIPHOSPHOETHANOLAMINE("*OP^XOP^XOCCN/5O/5=O/3O/3=O", "diphospho-ethanolamine", "PPEtn"),
	PHOSPHOETHANOLAMINE	("*OP^XOCCN/3O/3=O", "phospho-ethanolamine", "PEtn"),
	AMINE				("*N", "amino", "N"),
	AMINO				("*ON", "amino", "N"),
	PYROPHOSPHATE		("*OP^XOPO/5O/5=O/3O/3=O", "pyrophosphate", "PyrP"),
	TRIPHOSPHATE		("*OP^XOP^XOPO/7O/7=O/5O/5=O/3O/3=O", "triphosphate","Tri-P"),
	HYDROXYMETHYL		("*CO", "hydroxymethyl", "OMeOH"),
	FLUOLO				("*F", "fluoro", "F"),
	IODO				("*I", "iodo", "I"),
	THIO				("*S", "thio", "SH"),
	BROMO				("*Br", "bromo", "Br"),
	CHLORO				("*Cl", "chloro", "Cl"),
	X_PYRUVATE			("*OC^XO*/3CO/6=O/3C", "pyruvate", "Py"),
	S_PYRUVATE			("*OC^SO*/3CO/6=O/3C", "pyruvate", "(S)Py"),
	R_PYRUVATE			("*OC^RO*/3CO/6=O/3C", "pyruvate", "(R)Py"),
	S_CARBOXYETHYL		("*OC^SCO/4=O/3C", "(s)-carboxyethyl", "(S)CE"),
	R_CARBOXYETHYL		("*OC^RCO/4=O/3C", "(r)-carboxyethyl", "(R)CE"),
	X_CARBOXYETHYL		("*OC^XCO/4=O/3C", "(x)-carboxyethyl", "(X)CE"),
	DS_CARBOXYETHYL		("*C^SCO/3=O/2C", "(s)-carboxyethyl", "(S)DCE"),
	DR_CARBOXYETHYL		("*C^RCO/3=O/2C", "(r)-carboxyethyl", "(R)DCE"),
	DX_CARBOXYETHYL		("*C^XCO/3=O/2C", "(x)-carboxyethyl", "(X)DCE"),
	S_LACTATE			("*OCC^SC/4O/3=O", "(s)-lactate", "(S)Lac"), 
	R_LACTATE			("*OCC^RC/4O/3=O", "(r)-lactate", "(R)Lac"),
	X_LACTATE			("*OCC^XC/4O/3=O", "(x)-lactate", "(X)Lac"),
	UNKNOWN				("*", "epoxy", "?");
	
	private String map;
	private String glycoCTNotation;
	private String iupacNotation;
	
	private SubstituentTemplate (String _map, String _ct, String _iupac) {
		this.map = _map;
		this.glycoCTNotation = _ct;
		this.iupacNotation = _iupac;
	}
	
	@Override
	public String getMAP() {
		return this.map;
	}

	@Override
	public String getglycoCTnotation() {
		return this.glycoCTNotation;
	}

	@Override
	public String getIUPACnotation() {
		return this.iupacNotation;
	}

	public static SubstituentTemplate forMAP (String _map) {
		for(SubstituentTemplate s : SubstituentTemplate.values()) {
			if(s.map.equals(_map)) return s;
		}
		return null;
	}
	
	public static SubstituentTemplate forIUPACNotation (String _iupac) { 
		for(SubstituentTemplate s : SubstituentTemplate.values()) { 
			if(s.iupacNotation.equals(_iupac)) return s;
		}
		return null;
	}
	
	public static SubstituentTemplate forGlycoCTNotation (String _ct) {
		for(SubstituentTemplate s : SubstituentTemplate.values()) {
			if(s.glycoCTNotation.equals(_ct)) return s;
		}
		return null;
	}

	public static SubstituentTemplate forGlycoCTNotationWithIgnore (String _ct) {
		for (SubstituentTemplate ind : SubstituentTemplate.values()) {
			if (ind.glycoCTNotation.equalsIgnoreCase(_ct)) return ind;
		}
		return null;
	}

	public static SubstituentTemplate forIUPACNotationWithIgnore (String _iupac) {
		for (SubstituentTemplate ind : SubstituentTemplate.values()) {
			if (ind.iupacNotation.equalsIgnoreCase(_iupac)) return ind;
		}
		return null;
	}
	
}
