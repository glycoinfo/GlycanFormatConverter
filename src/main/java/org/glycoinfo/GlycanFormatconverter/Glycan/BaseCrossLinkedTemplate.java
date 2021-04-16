package org.glycoinfo.GlycanFormatconverter.Glycan;

/**
 * Created by e15d5605 on 2019/03/11.
 */
public enum BaseCrossLinkedTemplate implements SubstituentInterface {

    // For double
    ANHYDRO			    ("*O*", "anhydro", "Anhydro"),

    X_PYRUVATE			("*OC^XO*/3CO/6=O/3C", "pyruvate", "Py"),
    S_PYRUVATE			("*OC^SO*/3CO/6=O/3C", "pyruvate", "(S)Py"),
    R_PYRUVATE			("*OC^RO*/3CO/6=O/3C", "pyruvate", "(R)Py"),

    X_DEOXYPYRUVATE	    ("*1OC^X*2/3CO/5=O/3C", "pyruvate", "Py"),
    R_DEOXYPYRUVATE		("*1OC^RO*2/3CO/6=O/3C", "(r)-pyruvate",	"(R)Py"),
    S_DEOXYPYRUVATE		("*1OC^SO*2/3CO/6=O/3C", "(s)-pyruvate",	"(S)Py"),

    // For both of single or double
    THIO                ("*S*", "thio", "SH"),
    AMINO				("*N*", "amino",	"N"),
    ETHANOLAMINE		("*NCC*", "ethanolamine", ""),
    IMINO				("*=N*", "imino", ""),
    OSULFATE			("*OSO*/3=O/3=O", "sulfate", "S"),
    NSULFATE			("*NS*/3=O/3=O", "n-sulfate", "NS"),
    SUCCINATE			("*OCCCCO*/6=O/3=O", "succinate", "Suc"),
    PHOSPHATE			("*OPO*/3O/3=O", "phosphate", "P"),
    PYROPHOSPHATE		("*OPOPO*/5O/5=O/3O/3=O", "pyrophosphate",	"PyrP"),
    TRIPHOSPHATE		("*OP^XOP^XOP^X*/7O/7=O/5O/5=O/3O/3=O", "triphosphate", "Tri-P"),
    PHOSPHOETHANOLAMINE	("*1NCCOP^XO*2/6O/6=O", "phospho-ethanolamine",	"PEtn"),
    DIPHOSPHOETHANOLAMINE	("*NCCOP^XOP^X*/8O/8=O/6O/6=O", "diphospho-ethanolamine", "PPEtn");

    private final String map;
    private final String gct;
    private final String iupac;

    @Override
    public String getMAP () {
        return this.map;
    }

    @Override
    public String getglycoCTnotation() {
        return this.gct;
    }

    @Override
    public String getIUPACnotation () {
        return this.iupac;
    }

    BaseCrossLinkedTemplate (String _map, String _gct, String _iupac) {
        this.map = _map;
        this.gct = _gct;
        this.iupac = _iupac;
    }

    public static BaseCrossLinkedTemplate forMAP (String _map) {
        for(BaseCrossLinkedTemplate temp : BaseCrossLinkedTemplate.values()) {
            if(temp.map.equals(_map)) return temp;
        }

        return null;
    }

    public static BaseCrossLinkedTemplate forIUPACNotation (String _iupac) {
        for(BaseCrossLinkedTemplate temp : BaseCrossLinkedTemplate.values()) {
            if(temp.iupac.equals(_iupac)) return temp;
        }

        return null;
    }

    public static BaseCrossLinkedTemplate forIUPACNotationWithIgnore (String _iupac) {
        for (BaseCrossLinkedTemplate temp : BaseCrossLinkedTemplate.values()) {
            if (temp.iupac.equalsIgnoreCase(_iupac)) return temp;
        }

        return null;
    }
}
