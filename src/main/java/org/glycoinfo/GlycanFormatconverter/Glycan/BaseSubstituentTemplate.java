package org.glycoinfo.GlycanFormatconverter.Glycan;

/**
 * Created by e15d5605 on 2019/03/07.
 */
public enum BaseSubstituentTemplate implements SubstituentInterface {

    ETHYR				("*CC", "ethyl", "Et"),
    CMETHYL				("*C", "methyl", "CMe"),
    OMETHYL				("*OC", "methyl", "Me"),
    NMETHYL             ("*NC", "n-methyl", "NMe"),
    OACETYL				("*OCC/3=O", "acetyl", "Ac"),
    NACETYL             ("*NCC/3=O", "n-acetyl", "NAc"),
    OGLYCOLYL			("*OCCO/3=O", "glycolyl", "Gc"),
    NGLYCOLYL			("*NCCO/3=O", "n-glycolyl", "NGc"),
    OSULFATE			("*OSO/3=O/3=O", "sulfate", "S"),
    NSULFATE            ("*NSO/3=O/3=O", "n-sulfate", "NS"),
    CFORMYL             ("*C=O", "formyl", "CFo"),
    OFORMYL				("*OC=O", "formyl", "Fo"),
    NFORMYL             ("*NC=O", "n-formyl", "NFo"),
    OAMIDINO			("*OCN/3=N", "amidino", "Am"),
    NAMIDINO            ("*NCN/3=N", "n-amidino", "NAm"),
    OSUCCINATE			("*OCCCCO/6=O/3=O", "succinate", "Suc"),
    NSUCCINATE          ("*NCCCCO/6=O/3=O", "n-succinate", "NSuc"),
    ODIMETHYL			("*OC/2C", "dimethyl", "DiMe"),
    NDIMETHYL			("*NC/2C", "n-dimethyl", "NDiMe"),
    OPHOSPHATE			("*OPO/3O/3=O", "phosphate", "P"),
    PHOSPHOCHOLINE		("*OP^XOCCNC/7C/7C/3O/3=O", "phospho-choline", "PCho"),
    ETHANOL				("*OCCO", "ethanol", "EtOH"),
    ETHANOLAMINE        ("*NCCO", "ethanolamine", "Etn"),
    DIPHOSPHOETHANOLAMINE("*OP^XOP^XOCCN/5O/5=O/3O/3=O", "diphospho-ethanolamine", "PPEtn"),
    PHOSPHOETHANOLAMINE	("*OP^XOCCN/3O/3=O", "phospho-ethanolamine", "PEtn"),
    PYROPHOSPHATE		("*OP^XOPO/5O/5=O/3O/3=O", "pyrophosphate", "PyrP"),
    TRIPHOSPHATE		("*OP^XOP^XOPO/7O/7=O/5O/5=O/3O/3=O", "triphosphate","Tri-P"),
    HYDROXYMETHYL		("*CO", "hydroxymethyl", "MeOH"),
    THIO				("*S", "thio", "SH"),
    AMINE				("*N", "amino", "N"),
    FLUORO				("*F", "fluoro", "F"),
    CHLORO				("*Cl", "chloro", "Cl"),
    BROMO				("*Br", "bromo", "Br"),
    IODO				("*I", "iodo", "I"),
    S_CARBOXYETHYL		("*OC^SCO/4=O/3C", "(s)-carboxyethyl", "(S)CE"),
    R_CARBOXYETHYL		("*OC^RCO/4=O/3C", "(r)-carboxyethyl", "(R)CE"),
    X_CARBOXYETHYL		("*OC^XCO/4=O/3C", "(x)-carboxyethyl", "(X)CE"),
    S_LACTATE			("*OCC^SC/4O/3=O", "(s)-lactate", "(S)Lac"),
    R_LACTATE			("*OCC^RC/4O/3=O", "(r)-lactate", "(R)Lac"),
    X_LACTATE			("*OCC^XC/4O/3=O", "(x)-lactate", "(X)Lac"),

    ACYL                ("*OCR/3=O", "acyl", "acyl");
    //NACYL               ("*NCR/3=O", "n-acyl", "");
    //NITRATE             ("", "nitrate", "Ni"),
    //IMINO               ("", "imino", "Im"),
    //NMETHYLCARBAMOYL    ("", "n-methyl-carbamoyl", "");

    private final String map;
    private final String gct;
    private final String iupac;

    BaseSubstituentTemplate(String _map, String _gct, String _iupac) {
        this.map = _map;
        this.gct = _gct;
        this.iupac = _iupac;
    }

    @Override
    public String getMAP() {
        return this.map;
    }

    @Override
    public String getglycoCTnotation() {
        return this.gct;
    }

    @Override
    public String getIUPACnotation() {
        return this.iupac;
    }

    public static BaseSubstituentTemplate forMAP (String _map) {
        for(BaseSubstituentTemplate bst : BaseSubstituentTemplate.values()) {
            if(bst.map.equals(_map)) return bst;
        }
        return null;
    }

    public static BaseSubstituentTemplate forIUPACNotation (String _iupac) {
        for(BaseSubstituentTemplate bst : BaseSubstituentTemplate.values()) {
            if(bst.iupac.equals(_iupac)) return bst;
        }
        return null;
    }

    public static BaseSubstituentTemplate forGlycoCTNotation (String _ct) {
        for(BaseSubstituentTemplate bst : BaseSubstituentTemplate.values()) {
            if(bst.gct.equals(_ct)) return bst;
        }
        return null;
    }

    public static BaseSubstituentTemplate forGlycoCTNotationWithIgnore (String _ct) {
        for (BaseSubstituentTemplate ind : BaseSubstituentTemplate.values()) {
            if (ind.gct.equalsIgnoreCase(_ct)) return ind;
        }
        return null;
    }

    public static BaseSubstituentTemplate forIUPACNotationWithIgnore (String _iupac) {
        for (BaseSubstituentTemplate ind : BaseSubstituentTemplate.values()) {
            if (ind.iupac.equalsIgnoreCase(_iupac)) return ind;
        }
        return null;
    }
}
