package org.glycoinfo.GlycanFormatconverter.io.LinearCode;

/**
 * Created by e15d5605 on 2017/08/28.
 */
public enum LinearCodeSUDictionary {

//    GRO     ("Gro", "", "Gro", "GR", 99),
    GLC     ("Glc", "", "Glc", "G", 1), //D-Glcp
    GAL     ("Gal", "", "Gal", "A", 2), //D-Galp
    GLCNAC  ("Glc", "2NAc", "GlcNAc", "GN", 3), //D-GlcpNAc
    GALNAC  ("Gal", "2NAc", "GalNAc", "AN", 4), //D-GalpNAc
    MAN     ("Man", "", "Man", "M", 5), //D-Manp
    NEUAC   ("Neu", "5Ac", "Neu5Ac", "NN", 6), //D-Neu5Acp
    NEU     ("Neu", "", "Neu", "N", 7), //D-Neup
    KDN     ("Kdn", "", "Kdn", "K", 8),
    KDO     ("Kdo", "", "Kdo", "W", 9),
    GALA    ("Gal", "6A", "GalA", "L", 10),
    IDO     ("Ido", "", "Ido", "I", 11),
    RHA     ("Rha", "", "Rha", "H", 12),
    FUC     ("Fuc", "", "Fuc", "F", 13),
    XYL     ("Xyl", "", "Xyl", "X", 14),
    Rib     ("Rib", "", "Rib", "B", 15),
    ARA     ("Ara", "", "Ara", "R", 16),
    GLCA    ("Glc", "6A", "GlcA", "U", 17),
    ALL     ("All", "", "ALl", "O", 18),
    API     ("Api", "", "Api", "P", 19),
    FRU     ("Fru", "", "Fru", "E", 20),
	NEUGC   ("Neu", "5Gc", "Neu5Gc", "NJ", 21),//D-Neu5Gcp
    HEX     ("Hex", "", "", "Z", 97),
    HEXNAC  ("Hex", "2NAc", "HexNAc", "ZN", 98);
//    SUGAR   ("Sugar", "", "Sugar", "*", 99);
//    MANNAC  ("Man", "2NAc", "ManNAc", "MN", 99),
//    IDONAC  ("Ido", "2NAc", "IdoNAc", "IN", 99),
//    FUCNAC  ("Fuc", "2NAc", "FucNAc", "FN", 99),
//    ALLNAC  ("All", "2NAc", "AllNAc", "ON", 99),
//    RHANAC  ("Rha", "2NAc", "RhaNAc", "HN", 99),

    private String iupacThreeLetter;
    private String nativeSubstituents;
    private String trivialName;
    private String linearCode;
    private int hierarchy;

    public String getIupacThreeLetter () {
        return this.iupacThreeLetter;
    }

    public String getNativeSubstituents () {
        return this.nativeSubstituents;
    }

    public String getLinearCode () {
        return this.linearCode;
    }

    public int getHierarchy() { return this.hierarchy; }

    LinearCodeSUDictionary(String _iupacThreeLetter, String _nativeSubstituents, String _trivialName, String _linearCode, int _hierarchy) {
        this.iupacThreeLetter = _iupacThreeLetter;
        this.nativeSubstituents = _nativeSubstituents;
        this.trivialName = _trivialName;
        this.linearCode = _linearCode;
        this.hierarchy = _hierarchy;
    }

    public static LinearCodeSUDictionary forLinearCode (String _linearCode) {
        for (LinearCodeSUDictionary ind : LinearCodeSUDictionary.values()) {
            if (ind.linearCode.equals(_linearCode)) return ind;
        }

        return null;
    }

    public static LinearCodeSUDictionary forTrivialName (String _trivialName) {
        for (LinearCodeSUDictionary ind : LinearCodeSUDictionary.values()) {
            if (ind.trivialName.equals(_trivialName)) return ind;
        }

        return null;
    }
}
