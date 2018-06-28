package org.glycoinfo.GlycanFormatconverter.Glycan;

public enum BaseTypeDictionary {
   
	DGRO("dgro","2"),
    LGRO("lgro","1"),
    DERY("dery","22"),
    LERY("lery","11"),      
    DRIB("drib","222"),
    LRIB("lrib","111"),     
    DARA("dara","122"),
    LARA("lara","211"),
    DALL("dall","2222"),
    LALL("lall","1111"),
    DALT("dalt","1222"),
    LALT("lalt","2111"),
    DGLC("dglc","2122"),
    LGLC("lglc","1211"),
    DMAN("dman","1122"),
    LMAN("lman","2211"),
    DTHR("dthr","12"),
    LTHR("lthr","21"),
    DXYL("dxyl","212"),
    LXYL("lxyl","121"),
    DLYX("dlyx","112"),
    LLYX("llyx","221"),
    DGUL("dgul","2212"),
    LGUL("lgul","1121"),
    DIDO("dido","1212"),
    LIDO("lido","2121"),
    DGAL("dgal","2112"),
    LGAL("lgal","1221"),
    DTAL("dtal","1112"),
    LTAL("ltal","2221"),

    // unknown configurations
    GRO("gro","x"),
	THR("thr","34"),
	ERY("ery","44"),
	ARA("ara","344"),
	RIB("rib","444"),
	LYX("lyx","334"),
	XYL("xyl","434"),
	ALL("all","4444"),
	ALT("alt","3444"),
	MAN("man","3344"),
	GLC("glc","4344"),
	GUL("gul","4434"),
	IDO("ido","3434"),
	TAL("tal","3334"),
	GAL("gal","4334"),

	// carbon backbone
    TRI("tri", ""),
    TET("tet", ""),
    PEN("pen", ""),
    HEX("hex", ""),
    HEP("hep", ""),
    OCT("oct", ""),
    NON("non", ""),

	SUGAR("Sugar", "");

    private String name;
    private String stereo;
    
    BaseTypeDictionary( String _name, String _stereo) {
        this.name = _name;
        this.stereo = _stereo;
    }
    
    public String getName() {  
        return this.name;  
    }

    public String getCoreName () {
        if (name.length() == 4) {
            return name.substring(1, name.length());
        }else return name;
    }

    public String getStereoCode() {  
        return this.stereo;
    }

    public String getConfiguration () {
        if (name.length() == 4) return name.substring(0, 1);
        return "?";
    }

    public static BaseTypeDictionary forName(String _name) {
        _name = _name.toUpperCase();
        for (BaseTypeDictionary s : BaseTypeDictionary.values()) {
            if (s.name.equalsIgnoreCase(_name)) {
                return s;
            }
        }
        return null;
    }    

    public static BaseTypeDictionary forStereoCode( String _stereo) {
        _stereo = _stereo.toUpperCase();
        for ( BaseTypeDictionary s : BaseTypeDictionary.values()) {
            if (s.stereo.equalsIgnoreCase(_stereo)) {
                return s;
            }
        }
        return null;
    }        
}