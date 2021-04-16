package org.glycoinfo.GlycanFormatconverter.Glycan;

public enum CrossLinkedTemplate implements SubstituentInterface {

	/**modificaiton bridge*/
	ANHYDROXYL			("*o", "anhydro", "Anhydro"),
	S_PYRUVATE			("*1OC^SO*2/3CO/6=O/3C", "(s)-pyruvate", "(S)Py"),
	R_PYRUVATE			("*1OC^RO*2/3CO/6=O/3C", "(r)-pyruvate", "(R)Py"),
	X_PYRUVATE			("*OC^XO*/3CO/6=O/3C", "pyruvate", "Py"),
	X_PYRUVATE_deoxy	("*1OC^X*2/3CO/5=O/3C", "pyruvate", "Py"), // 2018/09/13 added by Masaaki Matsubara
	AMINO				("*N*", "amino", "N"),
	ETHANOLAMINE		("*NCC*", "ethanolamine", ""),
	IMINO				("*=N*", "imino", ""),
	PHOSPHATE			("*OPO*/3O/3=O", "phosphate", "P"),
	PYROPHOSPHATE_U		("*OP^XOP^X*/5O/5=O/3O/3=O", "pyrophosphate", "PyrP"),
	P_bridge_un			("*1OP^X*2/3O/3=O", "phosphate", "P"),
	SULFATE				("*OSO*/3=O/3=O", "sulfate", "S"),
	N_SULFATE			("*NS*/3=O/3=O", "n_sulfate", "NS"),
	SUCCINATE			("*OCCCCO*/6=O/3=O", "succinate", "Suc"),
	TRIPHOSPHATE		("*OP^XOP^XOP^X*/7O/7=O/5O/5=O/3O/3=O", "triphosphate", "Tri-P"),
	PHOSPHOETHANOLAMINE	("*1NCCOP^XO*2/6O/6=O", "phospho-ethanolamine", "PEtn"),
	DIPHOSPHOETHANOLAMINE("*NCCOP^XOP^X*/8O/8=O/6O/6=O",	"diphospho-ethanolamine", "PPEtn"),
	PYROPHOSPHATE		("*OPOPO*/5O/5=O/3O/3=O", "pyrophosphate", "PyrP");
	
	private final String map;
	private final String notationGlycoCT;
	private final String notationIUPAC;
	
	@Override
		public String getMAP () {
		return this.map;
	}
		
	@Override
	public String getglycoCTnotation() {
		return this.notationGlycoCT;
	}
	
	@Override
	public String getIUPACnotation () {
		return this.notationIUPAC;
	}
	
	CrossLinkedTemplate(String _map, String _ct, String _iupac) {
		this.map = _map;
		this.notationGlycoCT = _ct;
		this.notationIUPAC = _iupac;
	}
	
	public static CrossLinkedTemplate forMAP (String _map) {
		for(CrossLinkedTemplate temp : CrossLinkedTemplate.values()) {
			if(temp.map.equals(_map)) return temp;
		}
		
		return null;
	}
	
	public static CrossLinkedTemplate forIUPACNotation (String _iupac) {
		for(CrossLinkedTemplate temp : CrossLinkedTemplate.values()) {
			if(temp.notationIUPAC.equals(_iupac)) return temp;
		}
		
		return null;
	}

	public static CrossLinkedTemplate forIUPACNotationWithIgnore (String _iupac) {
		for (CrossLinkedTemplate temp : CrossLinkedTemplate.values()) {
			if (temp.notationIUPAC.equalsIgnoreCase(_iupac)) return temp;
		}

		return null;
	}
}