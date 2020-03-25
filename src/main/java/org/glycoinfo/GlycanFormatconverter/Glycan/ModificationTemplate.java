package org.glycoinfo.GlycanFormatconverter.Glycan;

public enum ModificationTemplate {
	
	/**modificaiton
	 * http://www.monosaccharidedb.org/notation.action?topic=basetype
	 * */
	//ANHYDROXYL		("*o", "anhydro", "Anhydro"),
	UNKNOWN 		('*', "", "?"),
	UNSATURATION_EL ('e', "en", "(E)en"),
	UNSATURATION_FL ('f', "en", "(X)en"),
	UNSATURATION_ZL ('z', "en", "(Z)en"),
	UNSATURATION_EU ('E', "en", "(E)en"),
	UNSATURATION_FU ('F', "en", "(X)en"),
	UNSATURATION_ZU ('Z', "en", "(Z)en"),
	DEOXY			('d', "d", "deoxy"),
	METHYL			('m', "d", "d"),
	ALDONICACID		('A', "a", "onic"),
	URONICACID		('A', "a", "uronic"),
	KETONE_U		('U', "keto", "ulo"),
	HYDROXYL		('h', "aldi", "ol"),
	ALDEHYDE		('o', "", "aldehyde"),
	ULOSONIC		('O', "keto", "ulo"),
	KETONE			('o', "keto", "ulo"),
	HLOSE_5 		('5', "h", "dehydro"),
	HLOSE_6			('6', "h", "dehydro"),
	HLOSE_7			('7', "h", "dehydro"),
	HLOSE_8			('8', "h", "dehydro"),
	HLOSE_X			('X', "h", "dehydro");
	//ONIC			('A', "a", "onic");
	
	private char carbon;
	private String glycoCTNotation;
	private String iupacNotation;
	
	public char getCarbon() {
		return this.carbon;
	}
	
	public String getGlycoCTnotation() {
		return this.glycoCTNotation;
	}
	
	public String getIUPACnotation() {
		return this.iupacNotation;
	}
	
	//monosaccharide construct
	ModificationTemplate(char _carbon, String _glycoCTnotation, String _IUPACnotation) {
		this.carbon = _carbon;
		this.glycoCTNotation = _glycoCTnotation;
		this.iupacNotation = _IUPACnotation;
	}
		
	public static ModificationTemplate forCarbon(char _carbon) {
		for(ModificationTemplate s : ModificationTemplate.values()) {
			if(_carbon == s.carbon) return s;
		}
		
		return null;
	}
	
	public static ModificationTemplate forIUPACNotation(String str_canvasName) {
				for(ModificationTemplate s : ModificationTemplate.values())
			if (str_canvasName.equals(s.iupacNotation)) return s;
	
		return null;
	}	
}
