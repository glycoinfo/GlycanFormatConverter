package org.glycoinfo.GlycanFormatconverter.Glycan;

public enum ModificationTemplate {
	
	/**modificaiton
	 * http://www.monosaccharidedb.org/notation.action?topic=basetype
	 * */
	//ANHYDROXYL		("*o", "anhydro", "Anhydro"),
	UNKNOWN 		('*', "", "?"),
	UNSATURATION_EL ('e', "en", "en"),
	UNSATURATION_FL ('f', "en", "en"),
	UNSATURATION_ZL ('z', "en", "en"),
	UNSATURATION_EU ('E', "en", "en"),
	UNSATURATION_FU ('F', "en", "en"),
	UNSATURATION_ZU ('Z', "en", "en"),
	DEOXY			('d', "d", "deoxy"),
	METHYL			('m', "d", "d"),
	ALDONICACID		('A', "a", "onic"),
	URONICACID		('A', "a", "uronic"),
	KETONE_U		('U', "keto", "ulo"),
	HYDROXYL		('h', "aldi", "ol"),
	ALDEHYDE		('o', "", "aldehyde"),
	ULOSONIC		('O', "keto", "ulo"),
	KETONE			('o', "keto", "ulo");
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
	private ModificationTemplate(char _carbon, String _glycoCTnotation, String _IUPACnotation) {
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
			if (str_canvasName.equals(s.iupacNotation.toString())) return s;
	
		return null;
	}	
}
