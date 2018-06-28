package org.glycoinfo.WURCSFramework.util.exchange;

import java.util.ArrayList;
import java.util.LinkedList;

public enum TrivialNameDescriptor {

	API("Api", "Ery", "", "", "3*OMeOH", ""),
	RUL("Rul", "Ery", "", "2*ulo", "", "Ery2ulo"),
	XUL("Xul", "Thr", "", "2*ulo", "", "Thr2ulo"),
	FRU("Fru", "Ara", "", "2*ulo", "", "Ara2ulo"),
	TAG("Tag", "Lyx", "", "2*ulo", "", "Lyx2ulo"),
	SOR("Sor", "Xyl", "", "2*ulo", "", "Xyl2ulo"),
	PSI("Psi", "Rib", "", "2*ulo", "", "Rib2ulo"),
	SED("Sed", "Alt", "", "2*ulo", "", "Alt2ulo"),
	KDO("Kdo", "Man", "?", "1*a_2*ulo_3*d", "", "3-deoxy-?-manOct2ulo"),
	DKDO("Kdo", "Man", "D", "1*a_2*ulo_3*d", "", "3-deoxy-D-manOct2ulo"),
	LKDO("Kdo", "Man", "L", "1*a_2*ulo_3*d", "", "3-deoxy-L-manOct2ulo"),
	KDN("Kdn", "Gro_Gal", "D_?", "1*a_2*ulo_3*d", "", "3-deoxy-D-gro-?-galNon2ulo"),
	DKDN("Kdn", "Gro_Gal", "D_D", "1*a_2*ulo_3*d", "", "3-deoxy-D-gro-D-galNon2ulo"),
	LKDN("Kdn", "Gro_Gal", "D_L", "1*a_2*ulo_3*d", "", "3-deoxy-D-gro-L-galNon2ulo"),
	LEG("Leg", "Gro_Gal", "D_?", "1*a_2*ulo_3*d_9*m", "5*N_7*N", "3,9-dideoxy-D-gro-?-galNon2ulo"),
	DLEG("Leg", "Gro_Gal", "D_D", "1*a_2*ulo_3*d_9*m", "5*N_7*N", "3,9-dideoxy-D-gro-D-galNon2ulo"),
	LLEG("Leg", "Gro_Gal", "D_L", "1*a_2*ulo_3*d_9*m", "5*N_7*N", "3,9-dideoxy-D-gro-L-galNon2ulo"),
	DQUI("Qui", "Glc", "", "6*m", "", "6-deoxy-D-Glc"),
	LQUI("Qui", "Glc", "", "6*m", "", "6-deoxy-L-Glc"),
	QUI("Qui", "Glc", "", "6*m", "", "6-deoxy-?-Glc"),
	DRHA("Rha", "Man", "", "6*m", "", "6-deoxy-D-Man"),
	LRHA("Rha", "Man", "", "6*m", "", "6-deoxy-L-Man"),
	RHA("Rha", "Man", "", "6*m", "", "6-deoxy-?-Man"),
	DFUC("Fuc", "Gal", "", "6*m", "", "6-deoxy-D-Gal"),
	LFUC("Fuc", "Gal", "", "6*m", "", "6-deoxy-L-Gal"),
	FUC("Fuc", "Gal", "", "6*m", "", "6-deoxy-?-Gal"),
	DTAL("dTal", "Tal", "", "6*m", "", "6-deoxy-Tal"),
	DALT("dAlt", "Alt", "", "6*m", "", "6-deoxy-Alt"),
	
	BOI("Boi", "Xyl", "", "2*d_6*m", "", "2,6-dideoxy-?-xylHex"),
	DBOI("Boi", "Xyl", "D", "2*d_6*m", "", "2,6-dideoxy-D-xylHex"),
	LBOI("Boi", "Xyl", "L", "2*d_6*m", "", "2,6-dideoxy-L-xylHex"),
	OLI("Oli", "Ara", "", "2*d_6*m", "", "2,6-dideoxy-?-araHex"),
	DOLI("Oli", "Ara", "D", "2*d_6*m", "", "2,6-dideoxy-D-araHex"),
	LOLI("Oli", "Ara", "L", "2*d_6*m", "", "2,6-dideoxy-L-araHex"),
	RHO("Rho", "Thr", "", "2*d_3*d_6*m", "", "2,3,6-trideoxy-?-thrHex"),
	DRHO("Rho", "Thr", "D", "2*d_3*d_6*m", "", "2,3,6-trideoxy-D-thrHex"),
	LRHO("Rho", "Thr", "L", "2*d_3*d_6*m", "", "2,3,6-trideoxy-L-thrHex"),
	AMI("Ami", "Ery", "", "2*d_3*d_6*m", "", "2,3,6-trideoxy-?-eryHex"),
	DAMI("Ami", "Ery", "D", "2*d_3*d_6*m", "", "2,3,6-trideoxy-D-eryHex"),
	LAMI("Ami", "Ery", "L", "2*d_3*d_6*m", "", "2,3,6-trideoxy-L-eryHex"),
	DIG("Dig", "Rib", "D", "2*d_6*m", "", "2,6-dideoxy-D-ribHex"),
	ABE("Abe", "Xyl", "D", "3*d_6*m", "", "3,6-dideoxy-D-xylHex"),
	COL("Col", "Xyl", "L", "3*d_6*m", "", "3,6-dideoxy-L-xylHex"),
	TYV("Tyv", "Ara", "D", "3*d_6*m", "", "3,6-dideoxy-D-araHex"),
	ASC("Asc", "Ara", "L", "3*d_6*m", "", "3,6-dideoxy-L-araHex"),
	DPAR("Par", "Rib", "D", "3*d_6*m", "", "3,6-dideoxy-D-ribHex"),
	
	NEU("Neu", "Gro_Gal", "D_?", "1*a_2*ulo_3*d", "5*N", "3-deoxy-D-gro-?-galNon2ulo"),
	DNEU("Neu", "Gro_Gal", "D_D", "1*a_2*ulo_3*d", "5*N", "3-deoxy-D-gro-D-galNon2ulo"),
	LNEU("Neu", "Gro_Gal", "D_L", "1*a_2*ulo_3*d", "5*N", "3-deoxy-D-gro-L-galNon2ulo"),
	
	NEUGC("NeuGc", "Gro_Gal", "D_?", "1*a_2*ulo_3*d", "5*NGc", ""),
	DNEUGC("NeuGc", "Gro_Gal", "D_D", "1*a_2*ulo_3*d", "5*NGc", ""),
	LNEUGC("NeuGc", "Gro_Gal", "D_L", "1*a_2*ulo_3*d", "5*NGc", ""),
	
	NEUAC("NeuAc", "Gro_Gal", "D_?", "1*a_2*ulo_3*d", "5*NAc", ""),
	DNEUAC("NeuAc", "Gro_Gal", "D_D", "1*a_2*ulo_3*d", "5*NAc", ""),
	LNEUAC("NeuAc", "Gro_Gal", "D_L", "1*a_2*ulo_3*d", "5*NAc", ""),
	
	NEU5GC("Neu5Gc", "Gro_Gal", "", "1*a_2*ulo_3*d", "5*NGc", ""),
	NEU5AC("Neu5Ac", "Gro_Gal", "", "1*a_2*ulo_3*d", "5*NAc", ""),
	MURX("Mur", "Glc", "", "", "2*N_3*(X)Lac", ""),
	MURR("Mur", "Glc", "", "", "2*N_3*(R)Lac", ""),
	MURS("Mur", "Glc", "", "", "2*N_3*(S)Lac", ""),
	MURNAC("MurNAc", "Glc", "", "", "2*NAc_3*(R)Lac", ""),
	MURNGC("MurNGc", "Glc", "", "", "2*NGc_3*(R)Lac", ""),
	BAC("Bac", "Glc", "", "6*d", "2*N_4*N", ""),
	CYM("Cym", "Rib", "L", "2*d_6*d", "3*Me", "2,6-dideoxy-L-ribHex"),
	OLE("Ole", "Ara", "L", "2*d_6*d", "3*Me", "2,6-dideoxy-L-araHex"),
	GALN("GalN", "Gal", "", "", "2*N", ""),
	GLCN("GlcN", "Glc", "", "", "2*N", ""),
	MANN("ManN", "Man", "", "", "2*N", ""),
	ALLN("AllN", "All", "", "", "2*N", ""),
	ALTN("AltN", "Alt", "", "", "2*N", ""),
	IDON("IdoN", "Ido", "", "", "2*N", ""),
	GULN("GulN", "Gul", "", "", "2*N", ""),
	HEXN("HexN", "Hex", "", "", "2*N", ""),
	TALN("TalN", "Tal", "", "", "2*N", ""),
	//FUCN("FucN", "Gal", "", "6*d", "2*N", ""),
	//RHAN("RhaN", "Man", "", "6*d", "2*N", ""),
	//QUIN("QuiN", "Glc", "", "6*d", "2*N", ""),
	GALA("GalA", "Gal", "", "6*a", "", ""),
	GLCA("GlcA", "Glc", "", "6*a", "", ""),
	MANA("ManA", "Man", "", "6*a", "", ""),
	ALLA("AllA", "All", "", "6*a", "", ""),
	ALTA("AltA", "Alt", "", "6*a", "", ""),
	IDOA("IdoA", "Ido", "", "6*a", "", ""),
	GULA("GulA", "Gul", "", "6*a", "", ""),
	HEXA("HexA", "Hex", "", "6*a", "", ""),
	TALA("TalA", "Tal", "", "6*a", "", ""),
	GALNAC("GalNAc", "Gal", "", "", "2*NAc", ""),
	GLCNAC("GlcNAc", "Glc", "", "", "2*NAc", ""),
	MANNAC("ManNAc", "Man", "", "", "2*NAc", ""),
	ALLNAC("AllNAc", "All", "", "", "2*NAc", ""),
	ALTNAC("AltNAc", "Alt", "", "", "2*NAc", ""),
	IDONAC("IdoNAc", "Ido", "", "", "2*NAc", ""),
	GULNAC("GulNAc", "Gul", "", "", "2*NAc", ""),
	HEXNAC("HexNAc", "Hex", "", "", "2*NAc", ""),
	TALNAC("TalNAc", "Tal", "", "", "2*NAc", ""),
	FUCNAC("FucNAc", "Gal", "", "6*d", "2*NAc", ""),
	RHANAC("RhaNAc", "Man", "", "6*d", "2*NAc", ""),
	QUINAC("QuiNAc", "Glc", "", "6*d", "2*NAc", ""),
	NONULOSONATE("Non", "", "", "1*a_2*ulo_3*d_9*m", "5*N_7*N", "Nonulosonate");
	//DGRODMAN("D-gro-D-manHep", "Gro_Man", "D_D", "", "", ""),
	//LGRODMAN("L-gro-D-manHep", "Gro_Man", "L_D", "", "", ""),
	//GRODMAN("?-gro-D-manHep", "Gro_Man", "_D", "", "", "");
	
	private String a_sTrivialName;
	private String a_sBasetype;
	private String a_sConfiguration;
	private String a_sModString;
	private String a_sSubstituent;
	private String a_sIUPAC;

	public String getTrivalName() {
		return this.a_sTrivialName;
	}
	
	public LinkedList<String> getBasetype() {
		LinkedList<String> a_aBasetypes = new LinkedList<String>();
		for(String a_sBasetype : this.a_sBasetype.split("_")) 
			a_aBasetypes.add(a_sBasetype);

		return a_aBasetypes;
	}
	
	public LinkedList<String> getConfiguration() {
		LinkedList<String> a_aConfigurations = new LinkedList<String>();
		for(String _a_sConfiguration : this.a_sConfiguration.split("_")) {
			if(_a_sConfiguration.equals("")) continue;
			a_aConfigurations.add(_a_sConfiguration);
		}
		
		return a_aConfigurations;
	}
	
	public LinkedList<String> getBasetypeWithConfiguration() {
		LinkedList<String> a_aBaseWithConfig = new LinkedList<String>();
		String[] a_aBasetypes;
		String[] a_aConfigurations;
		
		if(this.a_sBasetype.contains("_")) {
			a_aBasetypes = this.a_sBasetype.split("_");
			a_aConfigurations = this.a_sConfiguration.split("_");
			
			for(int i = 0; i < a_aBasetypes.length; i++) {
				a_aBaseWithConfig.add((a_aConfigurations[i].equals("?") ? "x" : a_aConfigurations[i]) + a_aBasetypes[i]);
			}
		}else {
			a_aBaseWithConfig.add(this.a_sConfiguration + this.a_sBasetype);			
		}
		
		return a_aBaseWithConfig;
	}
	
	public ArrayList<String> getModifications() {
		ArrayList<String> a_aModifications = new ArrayList<String>();

		if(this.a_sModString.isEmpty()) return a_aModifications;
		
		for(String a_sModificaiton : this.a_sModString.split("_")) {
			a_aModifications.add(a_sModificaiton);
		}
		
		return a_aModifications;
	}
	
	public ArrayList<String> getSubstituent() {
		ArrayList<String> a_aSubstituents = new ArrayList<String>();
	
		if(this.a_sSubstituent.isEmpty()) return a_aSubstituents;
		
		for(String a_sSubstituent : this.a_sSubstituent.split("_"))
			a_aSubstituents.add(a_sSubstituent);
		
		return a_aSubstituents;
	}
	
	public String getIUPAC() {
		return this.a_sIUPAC;
	}
	
	private TrivialNameDescriptor (String _a_sTrivialName, String _a_sBasetype, String _a_sModPos, String _a_sModString, String _a_sSubstituent, String _a_sIUPAC) {
		this.a_sTrivialName = _a_sTrivialName;
		this.a_sBasetype = _a_sBasetype;
		this.a_sConfiguration = _a_sModPos;
		this.a_sModString = _a_sModString;
		this.a_sSubstituent = _a_sSubstituent;
		this.a_sIUPAC = _a_sIUPAC;
	}
	
	public static TrivialNameDescriptor forTrivialName(String _a_sTrivialName) {
		TrivialNameDescriptor[] enumArray = TrivialNameDescriptor.values();
		
		for(TrivialNameDescriptor enumStr : enumArray) {
			if(_a_sTrivialName.equals(enumStr.a_sTrivialName)) return enumStr;
		}
		return null;
	}
	
	public static TrivialNameDescriptor forTrivialName(String _a_sTrivialName, String _a_sConfiguration) { 
		TrivialNameDescriptor[] enumArray = TrivialNameDescriptor.values();

		for(TrivialNameDescriptor enumStr : enumArray) {
			if(_a_sTrivialName.equals(enumStr.a_sTrivialName) && 
					_a_sConfiguration.equals(enumStr.getConfiguration().getLast())) return enumStr;
		}
		return null;
	}
	
	public static TrivialNameDescriptor forIUPAC(String _a_sIUPAC) {
		TrivialNameDescriptor[] enumArray = TrivialNameDescriptor.values();
		
		for(TrivialNameDescriptor enumStr : enumArray) {
			if(_a_sIUPAC.equals(enumStr.a_sIUPAC)) return enumStr;
		}
		return null;
	}
}