package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

import java.util.ArrayList;

public enum TrivialNameDictionary {

	//http://www.monosaccharidedb.org/notation.action?topic=trivialname
	
	/** Ketoses */
	ERU("Eru", "gro", "1*h_2*O", "", 4),
	RUL("Rul", "ery", "1*h_2*O", "", 5),
	XUL("Xul", "thr", "1*h_2*O", "", 5),
	FRU("Fru", "ara", "1*h_2*O", "", 6),
	TAG("Tag", "lyx", "1*h_2*O", "", 6),
	SOR("Sor", "xyl", "1*h_2*O", "", 6),
	PSI("Psi", "rib", "1*h_2*O", "", 6),
	SED("Sed", "alt", "1*h_2*O", "", 7),
	
	/** Keto-ulosonic acids */
	KO ("Ko", "dgro_dtal", "1*A_2*O", "", 8), //always d configuration
	KDO("Kdo", "dman", "1*A_2*O_3*d", "", 8), //always d configuration
	KDN("Kdn", "dgro_dgal", "1*A_2*O_3*d", "", 9), //always d configuration
	NEU("Neu", "dgro_dgal", "1*A_2*O_3*d", "5*N", 9), //always d configuration
	LEG("Leg", "dgro_dgal", "1*A_2*O_3*d_9*m", "5*N_7*N", 9), //always d configuration
	PSE("Pse", "lgro_lman", "2*O_3*d_9*m", "5*N_7*N", 9), //always l configuration
	//4ELEG("4eLeg", "dgro_dtal", "1*A_2*O_3*d_9*m", "5*N_7*N", 9), //always d configuration
	//8ELEG("8eLeg", "lgro_dgal", "1*A_2*O_3*d_9*m", "5*N_7*N", 9), //always l configuration
	
	/** Deoxy monosaccharides */
	QUI("Qui", "glc", "6*m", "", 6),
	RHA("Rha", "man", "6*m", "", 6),
	FUC("Fuc", "gal", "6*m", "", 6),	
	BOI("Boi", "xyl", "2*d_6*m", "", 6),
	OLI("Oli", "ara", "2*d_6*m", "", 6),
	DIG("Dig", "drib", "2*d_6*m", "", 6), //only d configuration
	ABE("Abe", "dxyl", "3*d_6*m", "", 6), //only d configuration
	COL("Col", "lxyl", "3*d_6*m", "", 6), //only l configuration
	TYV("Tyv", "dara", "3*d_6*m", "", 6), //only d configuration
	ASC("Asc", "lara", "3*d_6*m", "", 6), //only l configuration
	PAR("Par", "drib", "3*d_6*m", "", 6), //only d configuration
	RHO("Rho", "thr", "2*d_3*d_6*m", "", 6),
	AMI("Ami", "ery", "2*d_3*d_6*m", "", 6),
	DEOXYTAL("6dTal", "tal", "6*m", "", 6),
	DEOXYALT("6dAlt", "alt", "6*m", "", 6),
	
	/** Amino sugars */
	BAC("Bac", "glc", "6*m", "2*N_4*N", 6),
	MUR("Mur", "glc", "", "3*(R)Lac", 6),
	ISOMUR("iMur", "glc", "", "3*(S)Lac", 6),
	//PURC("PurC", "dery", "3*d_4*d", "2*N_6*N", 6), //always d configuration
		
	/** Other */
	API("Api", "ery", "", "3*OMeOH", 4),
	CYM("Cym", "rib", "2*d_6*m", "3*OMe", 6),
	OLE("Ole", "ara", "2*d_6*m", "3*OMe", 6),
	THE("The", "glc", "6*m", "3*OMe", 6),
	ACO("Aco", "man", "6*m", "3*OMe", 6);
	//NON("Non", "", "1*A_2*O_3*d_9*m", "5*N_7*N", 9);
	
	private String threeLetter;
	private String stereos;
	private String modification;
	private String substituent;
	private int size;
	
	public String getStereos() {
		return this.stereos;
	}
	
	public String getModificationNotation() {
		return this.modification;
	}
	
	public String getSubstituentNotation() {
		return this.substituent;
	}
	
	public String getThreeLetterCode() {
		return this.threeLetter;
	}
	
	public int getSize() {
		return this.size;
	}
	
	TrivialNameDictionary(String _code, String _stereos, String _modification, String _substituent, int _size) {
		this.threeLetter = _code;
		this.stereos = _stereos;
		this.modification = _modification;
		this.substituent = _substituent;
		this.size = _size;
	}
	
	public static TrivialNameDictionary forThreeLetterCode (String _code) {
		for(TrivialNameDictionary tiv : TrivialNameDictionary.values()) {
			if(tiv.threeLetter.equalsIgnoreCase(_code)) return tiv;
		}
		
		return null;
	}
	
	public static ArrayList<TrivialNameDictionary> forStereos (String _stereos) {
		ArrayList<TrivialNameDictionary> ret = new ArrayList<TrivialNameDictionary>();
		
		for(TrivialNameDictionary tiv : TrivialNameDictionary.values()) {
			if(tiv.stereos.equalsIgnoreCase(_stereos)) ret.add(tiv);
		}
		
		return ret;
	}
}