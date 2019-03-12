package org.glycoinfo.GlycanFormatconverter.Glycan;

/**
 * Created by e15d5605 on 2019/03/07.
 */
public enum BaseSubstituentTemplate implements SubstituentInterface {

    ETHYR				("*CC", "ethyl", "Et"),
    METHYL				("*C", "methyl", "Me"),
    ACETYL				("*CC/2=O", "acetyl", "Ac"),
    NACETYL             ("*NCC/3=O", "n-acetyl", "NAc"),
    GLYCOLYL			("*CCO/2=O", "glycolyl", "Gc"),
    NGLYCOLYL			("*NCCO/3=O", "n-glycolyl", "NGc"),
    ETHANOL				("*CCO", "ethanolamine", "EtOH"),
    SULFATE				("*SO/2=O/2=O", "sulfate", "S"),
    NSULFATE            ("*NSO/3=O/3=O", "n-sulfate", "NS"),
    FORMYL				("*C=O", "formyl", "Fo"),
    NFORMYL             ("*NC=O", "n-formyl", "NFo"),
    AMIDINO				("*CN/2=N", "amidino", "Am"),
    NAMIDINO            ("*NCN/3=N", "n-amidino", "NAm"),
    SUCCINATE			("*CCCCO/5=O/2=O", "succinate", "Suc"),
    NSUCCINATE          ("*NCCCCO/6=O/3=O", "n-succinate", "NSuc"),
    DIMETHYL			("*C/1C", "dimethyl", "DiMe"),
    PHOSPHATE			("*PO/2O/2=O", "phosphate", "P"),
    PHOSPHOCHOLINE		("*P^XOCCNC/6C/6C/2O/2=O", "phospho-choline", "PCho"),
    DIPHOSPHOETHANOLAMINE("*P^XOP^XOCCN/4O/4=O/2O/2=O", "diphospho-ethanolamine", "PPEtn"),
    PHOSPHOETHANOLAMINE	("*P^XOCCN/2O/2=O", "phospho-ethanolamine", "PEtn"),
    AMINE				("*N", "amino", "N"),
    PYROPHOSPHATE		("*P^XOPO/4O/4=O/2O/2=O", "pyrophosphate", "PyrP"),
    TRIPHOSPHATE		("*P^XOP^XOPO/6O/6=O/4O/4=O/2O/2=O", "triphosphate","Tri-P"),
    HYDROXYMETHYL		("*CO", "hydroxymethyl", "MeOH"),
    FLUOLO				("*F", "fluoro", "F"),
    IODO				("*I", "iodo", "I"),
    THIO				("*S", "thio", "SH"),
    BROMO				("*Br", "bromo", "Br"),
    CHLORO				("*Cl", "chloro", "Cl"),
    X_PYRUVATE			("*C^XO*/2CO/5=O/2C", "pyruvate", "Py"),
    S_PYRUVATE			("*C^SO*/2CO/5=O/2C", "pyruvate", "(S)Py"),
    R_PYRUVATE			("*C^RO*/2CO/5=O/2C", "pyruvate", "(R)Py"),
    S_CARBOXYETHYL		("*C^SCO/3=O/2C", "(s)-carboxyethyl", "(S)CE"),
    R_CARBOXYETHYL		("*C^RCO/3=O/2C", "(r)-carboxyethyl", "(R)CE"),
    X_CARBOXYETHYL		("*C^XCO/3=O/2C", "(x)-carboxyethyl", "(X)CE"),
    S_LACTATE			("*CC^SC/3O/2=O", "(s)-lactate", "(S)Lac"),
    R_LACTATE			("*CC^RC/3O/2=O", "(r)-lactate", "(R)Lac"),
    X_LACTATE			("*CC^XC/3O/2=O", "(x)-lactate", "(X)Lac"),
    UNKNOWN				("*", "epoxy", "?");

    private String map;
    private String gct;
    private String iupac;

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
