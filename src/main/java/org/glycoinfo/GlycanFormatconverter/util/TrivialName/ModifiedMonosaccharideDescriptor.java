package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

public enum ModifiedMonosaccharideDescriptor {

	GALN("GalN", "gal", "", "2*N"),
	GLCN("GlcN", "glc", "", "2*N"),
	MANN("ManN", "man", "", "2*N"),
	ALLN("AllN", "all", "", "2*N"),
	ALTN("AltN", "alt", "", "2*N"),
	IDON("IdoN", "ido", "", "2*N"),
	GULN("GulN", "gul", "", "2*N"),
	HEXN("HexN", "hex", "", "2*N"),
	TALN("TalN", "tal", "", "2*N"),
	GALA("GalA", "gal", "6*A", ""),
	GLCA("GlcA", "glc", "6*A", ""),
	MANA("ManA", "man", "6*A", ""),
	ALLA("AllA", "all", "6*A", ""),
	ALTA("AltA", "alt", "6*A", ""),
	IDOA("IdoA", "ido", "6*A", ""),
	GULA("GulA", "gul", "6*A", ""),
	HEXA("HexA", "hex", "6*A", ""),
	TALA("TalA", "tal", "6*A", ""),
	GALNAC("GalNAc", "gal", "", "2*NAc"),
	GLCNAC("GlcNAc", "glc", "", "2*NAc"),
	MANNAC("ManNAc", "man", "", "2*NAc"),
	ALLNAC("AllNAc", "all", "", "2*NAc"),
	ALTNAC("AltNAc", "alt", "", "2*NAc"),
	IDONAC("IdoNAc", "ido", "", "2*NAc"),
	GULNAC("GulNAc", "gul", "", "2*NAc"),
	HEXNAC("HexNAc", "hex", "", "2*NAc"),
	TALNAC("TalNAc", "tal", "", "2*NAc"),
	FUCNAC("FucNAc", "gal", "6*m", "2*NAc"),
	RHANAC("RhaNAc", "man", "6*m", "2*NAc"),
	QUINAC("QuiNAc", "glc", "6*m", "2*NAc"),
	DEOXYALTNAC("6dAltNAc", "alt", "6*m", "2*NAc"),
	DEOXYTALNAC("6dTalNAc", "tal", "6*m", "2*NAc"),
	DEOXYHEXNAC("dHexNAc", "hex", "6*m", "2*NAc"),
	//MURNAC_XLAC("MurNAc", "glc", "", "2*NAc_3*(X)Lac"),
	MURNAC_SLAC("iMurNAc", "glc", "", "2*NAc_3*(S)Lac"),
	MURNAC_RLAC("MurNAc", "glc", "", "2*NAc_3*(R)Lac"),
	//MURNGC_XLAC("MurNGc", "glc", "", "2*NGc_3*(X)Lac"),
	MURNGC_SLAC("iMurNGc", "glc", "", "2*NGc_3*(S)Lac"),
	MURNGC_RLAC("MurNGc", "glc", "", "2*NGc_3*(R)Lac"),
	//MURNGC_XLAC("MurNGc", "glc", "", "2*NGc_3*(X)Lac"),
	NEUGC("NeuGc", "dgro_dgal", "1*A_2*O_3*d", "5*NGc"), //always d configuration
	NEUAC("NeuAc", "dgro_dgal", "1*A_2*O_3*d", "5*NAc"), //always d configuration
	NEU5GC("Neu5Gc", "dgro_dgal", "1*A_2*O_3*d", "5*NGc"), //always d configuraiton
	NEU5AC("Neu5Ac", "dgro_dgal", "1*A_2*O_3*d", "5*NAc"), //always d configuration
	NON("Non", "non", "1*A_2*O_3*d", ""),
	DIDEOXYNON("ddNon", "non", "1*A_2*O_3*d_9*m", "5*N_7*N");

	private String trivialName;
	private String stereos;
	private String modifications;
	private String substituents;
	
	public String getStereos () {
		return this.stereos;
	}
	
	public String getModifications () {
		return this.modifications;
	}
	
	public String getSubstituents () {
		return this.substituents;
	}
	
	ModifiedMonosaccharideDescriptor (String _trival, String _stereos, String _modifications, String _substituents) {
		this.trivialName = _trival;
		this.stereos = _stereos;
		this.modifications = _modifications;
		this.substituents = _substituents;
	}
	
	public static ModifiedMonosaccharideDescriptor forTrivialName (String _trivial) {
		for (ModifiedMonosaccharideDescriptor mod : ModifiedMonosaccharideDescriptor.values()) {
			if(mod.trivialName.equalsIgnoreCase(_trivial)) return mod;
		}
		
		return null;
	}
}
