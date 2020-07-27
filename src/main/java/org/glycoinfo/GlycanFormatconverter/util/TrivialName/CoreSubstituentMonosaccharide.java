package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

public enum CoreSubstituentMonosaccharide {
	
	HEX("Hex", 6),
	GLC("Glc", 6),
	GAL("Gal", 6),
	MAN("Man", 6),
	ALL("All", 6),
	ALT("Alt", 6),
	DALT("6dAlt", 6),
	GUL("Gul", 6),
	DGUL("6dGul", 6),
	TAL("Tal", 6),
	DTAL("6dTal", 6),
	IDO("Ido", 6),
	NEU("Neu", 9),
	RHA("Rha", 6),
	FUC("Fuc", 6),
	QUI("Qui", 6),
	MUR("Mur", 6),
	IMUR("iMur", 6);

	private String a_sTrivialName;
	private int a_iBackboneSize;
	
	CoreSubstituentMonosaccharide(String _a_sTrivialName, int _a_iBackboneSize) {
		this.a_sTrivialName = _a_sTrivialName;
		this.a_iBackboneSize = _a_iBackboneSize;
	}
	
	public static CoreSubstituentMonosaccharide forTrivialName(String _a_sTrivialName, int _a_iBackboneSize) {
		CoreSubstituentMonosaccharide[] enum_array = CoreSubstituentMonosaccharide.values();
		
		for(CoreSubstituentMonosaccharide enumStr : enum_array)
			if (_a_sTrivialName.equals(enumStr.a_sTrivialName) && _a_iBackboneSize == enumStr.a_iBackboneSize) return enumStr;
		
		return null;
	}
	
	public static CoreSubstituentMonosaccharide forTrivialName(String _name) {
		for(CoreSubstituentMonosaccharide s : CoreSubstituentMonosaccharide.values()) {
			if(_name.equals(s.a_sTrivialName)) return s;
		}
		
		return null;
	}
}

