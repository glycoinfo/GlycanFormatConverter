package org.glycoinfo.GlycanFormatconverter.io.LinearCode;

/**
 * Created by e15d5605 on 2017/08/30.
 */
public enum LinearCodeSubstituentDictionary {

    DN_ACETYL               ("Q", "NAc"),
    N_ACETYL                ("N", "NAc"),
    O_ACETYL                ("T", "Ac"),
    PHOSPHOETHANOL_AMINE    ("PE", "PEth"),
    //????                  ("PN", ""), //2-Aminoethylphosphonate
    INOSITOL                ("IN", ""),
    METHYL                  ("ME", "Me"),
    PHOSPHATE               ("P", "P"),
    PHOSPHOCHOLINE          ("PC", "PCho"),
    PYRUVATE                ("PYR", "Py"),
    SULFATE                 ("S", "S"),
    SULFIDE                 ("SH", "SH"), //thio
    CILIATIN                ("EP", ""),
    FLUORO                  ("FL", "F"),
    CHLORO                  ("CH", "Cl"),
    ANHYDRO                 ("AH", "Anhydro");

    private String linearCodeNotation;
    private String iupacNotation;

    public String getIUPACNotation () {
        return iupacNotation;
    }

    public String getLinearCodeNotation () { return linearCodeNotation; }

    LinearCodeSubstituentDictionary (String _linearCode, String _iupac) {
        this.linearCodeNotation = _linearCode;
        this.iupacNotation = _iupac;
    }

    public static LinearCodeSubstituentDictionary forLinearCode (String _linearCode) {
        for (LinearCodeSubstituentDictionary ind : LinearCodeSubstituentDictionary.values()) {
            if (ind.linearCodeNotation.equals(_linearCode)) return ind;
        }

        return null;
    }

    public static LinearCodeSubstituentDictionary forIUPACNotation (String _iupac) {
        for (LinearCodeSubstituentDictionary ind : LinearCodeSubstituentDictionary.values()) {
            if (ind.iupacNotation.equals(_iupac)) return ind;
        }

        return null;
    }
}
