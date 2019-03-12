package org.glycoinfo.GlycanFormatconverter.Glycan;

/**
 * Created by e15d5605 on 2019/03/11.
 */
public enum BaseCrossLinkedTemplate implements SubstituentInterface {

    // For double
    ANHYDROXYL			("*o", "anhydro", "Anhydro"),
    PYRUVATE			("C^X*/2CO/4=O/2C", "pyruvate",	"Py"),
    R_PYRUVATE			("C^R*/2CO/4=O/2C", "(r)-pyruvate",	"(R)Py"),
    S_PYRUVATE			("C^R*/2CO/4=O/2C", "(s)-pyruvate",	"(S)Py"),

    // For both of single or double
    AMINO				("N*", "amino",	"N"),
    ETHANOLAMINE		("NCC*", "ethanolamine", ""),
    IMINO				("=N*", "imino", ""),
    SULFATE				("S*/2=O/2=O", "sulfate", "S"),
    N_SULFATE			("NS*/3=O/3=O", "n-sulfate", "NSuc"),
    SUCCINATE			("CCCC*/5=O/2=O", "succinate", "Suc"),
    PHOSPHATE			("P^X*/2O/2=O", "phosphate", "P"),
    PYROPHOSPHATE		("P^XOP^X*/4O/4=O/2O/2=O", "pyrophosphate",	"PyrP"),
    TRIPHOSPHATE		("P^XOP^XOP^X*/6O/6=O/4O/4=O/2O/2=O", "triphosphate", "Tri-P"),
    PHOSPHO_ETHANOLAMINE	("NCCOP^X*/6O/6=O", "phospho-ethanolamine",	"PEtn"),
    DIPHOSPHO_ETHANOLAMINE	("NCCOP^XOP^X*/8O/8=O/6O/6=O", "diphospho-ethanolamine", "PPEtn");

    private String map;
    private String gct;
    private String iupac;

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
