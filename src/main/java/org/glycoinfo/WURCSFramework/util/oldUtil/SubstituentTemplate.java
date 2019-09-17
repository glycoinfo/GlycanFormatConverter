package org.glycoinfo.WURCSFramework.util.oldUtil;

public enum SubstituentTemplate {
	
	/***/
	HYDROGEN			("*H", "hydrogen", "h", "modification"),
	HYDROXYL			("*OH", "hydroxyl", "o", "modification"),
	ETHYR				("*OCC", "ethyl", "Et", "substituent"),
	METHYL				("*OC", "methyl", "Me", "substituent"), //*
	C_METHYL			("*C", "methyl", "Me", "substituent"),
	N_METHYL			("*NC", "n-methyl", "NMe", "substituent"),
	ACETYL				("*OCC/3=O", "acetyl", "Ac", "substituent"), //*
	dACETYL				("*CC/2=O", "acetyl", "Ac", "substituent"),
	//D_ACETYL			("*CO/3=O", "acetyl", "Ac", "subtituent"),
	N_ACETYL			("*NCC/3=O", "n-acetyl", "NAc", "substituent"), //*
	GLYCOLYL			("*OCCO/3=O", "glycolyl", "Gc", "substituent"), //**
	N_GLYCOLYL			("*NCCO/3=O", "n-glycolyl", "NGc", "substituent"), //**
	ETHANOL				("*OCCO", "ethanolamine", "EtOH", "substituent"),
	ETHANOLAMINE		("*NCCO", "ethanolamine", "NEtOH", "substituent"),
	SULFATE				("*OSO/3=O/3=O", "sulfate", "S", "substituent"), //*
	N_SULFATE			("*NSO/3=O/3=O", "N-sulfate", "NS", "substituent"), //**
	D_FORMYL			("*C=O", "formyl", "Fo", "substituent"),
	FORMYL				("*OC=O", "formyl", "Fo", "substituent"), //**
	N_FORMYL			("*NC=O", "N-formyl", "NFo", "substituent"), //**
	AMIDINO				("*OCN/3=N", "Amidino", "Am", "substituent"), //**
	N_AMIDINO			("*NCN/3=N", "N-amidino", "NAm", "substituent"), //**
	SUCCINATE			("*OCCCCO/6=O/3=O", "succinate", "Suc", "substituent"), //**
	N_SUCCINATE			("*NCCCCO/6=O/3=O", "N-succinate", "NSuc", "substituent"), //**
	DIMETHYL			("*OC/2C", "dimethyl", "DiMe", "substituent"),
	N_DIMETHYL			("*NC/2C", "N-dimethyl", "NDiMe", "substituent"), //**
	PHOSPHATE			("*OPO/3O/3=O", "phosphate", "P", "substituent"), //*
	PHOSPHOCHOLINE		("*OP^XOCCNC/7C/7C/3O/3=O", "phospho-choline", "PCho", "substituent"),
	DIPHOSPHOETHANOLAMINE("*OP^XOP^XOCCN/5O/5=O/3O/3=O", "diphospho-ethanolamine", "PPEtn", "substituent"), //*
	PHOSPHOETHANOLAMINE	("*OP^XOCCN/3O/3=O", "phospho-ethanolamine", "PEtn", "substituent"), //*
	AMINO				("*ON", "amino", "N", "substituent"),
	PYROPHOSPHATE		("*OP^XOPO/5O/5=O/3O/3=O", "pyrophosphate", "PyrP", "substituent"), //*
	TRIPHOSPHATE		("*OP^XOP^XOPO/7O/7=O/5O/5=O/3O/3=O", "triphosphate","Tri-P", "substituent"), //**
	HYDROXYMETHYL		("*CO", "hydroxymethyl", "OMeOH", "substituent"), //**
	FLUOLO				("*F", "fluoro", "F", "substituent"), //**
	IODO				("*I", "iodo", "I", "substituent"), //**
	AMINE				("*N", "amino", "N", "substituent"), //**
	THIO				("*S", "thio", "SH", "substituent"), //**
	BROMO				("*Br", "bromo", "Br", "substituent"), //**
	CHLORO				("*Cl", "chloro", "Cl", "substituent"), //**
	PYRUVATE			("*OC^XO*/3CO/6=O/3C", "pyruvate", "Py", "substituent"),
	S_PYRUVATE			("*OC^SO*/3CO/6=O/3C", "pyruvate", "(S)Py", "substituent"),
	R_PYRUVATE			("*OC^RO*/3CO/6=O/3C", "pyruvate", "(R)Py", "substituent"),
	S_CARBOXYETHYL		("*OC^SCO/4=O/3C", "(s)-carboxyethyl", "(S)Lac", "substituent"),
	R_CARBOXYETHYL		("*OC^RCO/4=O/3C", "(r)-carboxyethyl", "(R)Lac", "substituent"),
	X_CARBOXYETHYL		("*OC^XCO/4=O/3C", "(x)-carboxyethyl", "(X)Lac", "substituent"),
	DS_CARBOXYETHYL		("*C^SCO/3=O/2C", "(s)-carboxyethyl", "", "substituent"),
	DR_CARBOXYETHYL		("*C^RCO/3=O/2C", "(r)-carboxyethyl", "", "substituent"),
	DX_CARBOXYETHYL		("*C^XCO/3=O/2C", "(x)-carboxyethyl", "", "substituent"),
	S_LACTATE			("*OCC^SC/4O/3=O", "(s)-lactate", "(S)Lac", "substituent"), 
	R_LACTATE			("*OCC^RC/4O/3=O", "(r)-lactate", "(R)Lac", "substituent"),
	X_LACTATE			("*OCC^XC/4O/3=O", "(x)-lactate", "(X)Lac", "substituent"),
	UNKNOWN				("*", "epoxy", "?", "substituent"),
		
	/**modificaiton bridge*/
	S_PYRRUVATE			("*1OC^SO*2/3CO/6=O/3C", "(s)-pyruvate", "(S)Py", "bridge"),
	R_PYRRUVATE			("*1OC^RO*2/3CO/6=O/3C", "(r)-pyruvate", "(R)Py", "bridge"),
	X_PYRRUVATE			("*1OC^XO*2/3CO/6=O/3C", "pyruvate", "Py", "bridge"),
	AMINO_bridge		("*N*", "amine", "N", "bridge"),
	ETHANOLAMINE_b		("*NCC*", "ethanolamine", "", "bridge"),
	IMINO_b				("*=N*", "imino", "", "bridge"),
	PHOSPHATE_b			("*OPO*/3O/3=O", "phosphate", "P", "bridge"),
	PYROPHOSPHATE_b		("*OP^XOP^X*/5O/5=O/3O/3=O", "pyrophosphate", "PyrP", "bridge"),
	PYROPHOSPHATE_o		("*OP^XOP^XO*/5O/5=O/3O/3=O", "pyrophosphate", "PyrP", "bridge"),
	P_bridge_un			("*1OP^X*2/3O/3=O", "phosphate", "P", "bridge"),
	SULFATE_b			("*OSO*/3=O/3=O", "sulfate", "S", "bridge"),
	N_SULFATE_b			("*NS*/3=O/3=O", "n_sulfate", "NS", "bridge"),
	SUCCINATE_b			("*OCCCCO*/6=O/3=O", "succinate", "Suc", "bridge"),
	TRIPHOSPHATE_b		("*OP^XOP^XOP^X*/7O/7=O/5O/5=O/3O/3=O", "triphosphate", "Tri-P", "bridge"),
	PHOSPHOETHANOLAMINE_b("*1NCCOP^XO*2/6O/6=O", "phospho-ethanolamine", "PEtn", "bridge"),
	DIPHOSPHOETHANOLAMINE_b("*NCCOP^XOP^X*/8O/8=O/6O/6=O",	"diphospho-ethanolamine", "PPEtn", "bridge"),
	PYROPHOSPHATE_PP	("*OPOPO*/5O/5=O/3O/3=O", "pyrophosphate", "PyrP", "bridge"),
	
	/**modificaiton*/
	ANHYDROXYL			("*o", "anhydro", "Anhydro", "modification"),
	UNSATURATION	("*en", "en", "en", "modificaiton"),
	DEOXY			("*d", "d", "d", "modificaiton"),
	DEOXYTAIL		("*m", "d", "d", "modification"),
	ACID			("*a", "a", "a", "modificaiton"),
	KETO			("*ulo", "keto", "ulo", "modification"),
	ONIC			("*onic", "a", "onic", "modification");

	private String str_MAP;
	private String str_ctName;
	private String str_IUPACnotation;
	private String str_type;
	
	//public void setSugarName(String _str_IUPACnotation) {
	//	this.str_IUPACnotation = _str_IUPACnotation;
	//}
	
	public String getMAP() {
			return this.str_MAP;
	}

	public String getGlycoCTnotation() {
		return this.str_ctName;
	}
	
	public String getIUPACnotation() {
		return this.str_IUPACnotation;
	}
	
	public String getType() {
		return this.str_type;
	}
	
	//monosaccharide construct
	private SubstituentTemplate(String _str_MAP, String _str_glycoCTnotation, 
			String _str_IUPACnotation, String _str_NodeType) {
		this.str_MAP = _str_MAP;
		this.str_ctName = _str_glycoCTnotation;
		this.str_IUPACnotation = _str_IUPACnotation;
		this.str_type = _str_NodeType;
	}
		
	/*
	 * @param : SkeletonCode
	 * @return : Glycan builder base sugar basetype
	 */
	public static SubstituentTemplate forMAP(String str_basetype) throws ConverterExchangeException {
		SubstituentTemplate[] enumArray = SubstituentTemplate.values();

		for(SubstituentTemplate enumStr : enumArray)
			if (str_basetype.equals(enumStr.str_MAP.toString())) return enumStr;

		throw new ConverterExchangeException(str_basetype + " is not found!");
	}
			
	public static SubstituentTemplate forIUPACNotation(String str_canvasName) throws ConverterExchangeException {
		SubstituentTemplate[] enumArray = SubstituentTemplate.values();

		for(SubstituentTemplate enumStr : enumArray)
			if (str_canvasName.equals(enumStr.str_IUPACnotation.toString())) return enumStr;
	
		throw new ConverterExchangeException(str_canvasName + " is not found!");
	}
	
	public boolean isSubstituent() {
		if(this.str_type.equals("substituent")) return true;
		return false;
	}
	
	public boolean isModificaiton() {
		if(this.str_type.equals("modificaiton")) return true;
		return false;
	}
	
	public boolean isBridge() {
		if(this.str_type.equals("bridge")) return true;
		return false;
	}
	
	@Override
	public String toString() {
		return str_MAP;
	}
}
