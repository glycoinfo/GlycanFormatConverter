package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

/**
 * Created by e15d5605 on 2017/06/15.
 */
public enum MonosaccharideIndex {

    /* Referenced from AppendixB SNFG */
    /* Trivial name, configurations, anomeric position */
    /* references : http://www.genome.jp/kegg/catalog/codes2.html */

    /* Triose */
    GRO("Gro", "D", "p", 1),

    /* Tetrose */
    ERY("Ery", "D", "f", 1),
    THR("Thr", "D", "f", 1),
    THO("Tho", "D", "f", 1),

    /* Pentose */
    ARA("Ara", "L", "p", 1),
    LYX("Lyx", "L", "p", 1),
    RIB("Rib", "D", "p", 1),
    XYL("Xyl", "D", "p", 1),

    /* Hexose */

    /* Heptose */
    MANHEP("manHep", "D", "", 1),
    HEP("Hep", "?", "?", 1),

    /* Octose */

    GLC("Glc", "D", "p", 1),
    GAL("Gal", "D", "p", 1),
    MAN("Man", "D", "p", 1),
    IDO("Ido", "D", "p", 1),
    TAL("Tal", "D", "p", 1),
    GUL("Gul", "D", "p", 1),
    ALT("Alt", "L", "p", 1),
    ALL("All", "D", "p", 1),
    HEX("Hex", "D", "p", 1),

    /* Deoxy hexoses */
    FUC("Fuc", "L", "p", 1),
    RHA("Rha", "L", "p", 1),
    QUI("Qui", "D", "p", 1),
    OLI("Oli", "D", "p", 1),
    TYV("Tyv", "D", "p", 1),
    ABE("Abe", "D", "p", 1),
    PAR("Par", "D", "p", 1),
    DIG("Dig", "D", "p", 1),
    COL("Col", "L", "p", 1),
    ASC("Asc", "L", "p", 1),
    BOI("Boi", "D", "p", 1),
    AMI("Ami", "D", "p", 1),
    RHO("Rho", "D", "p", 1),
    SDTAL("6dTal", "D", "p", 1),
    SDAlt("6dAlt", "D", "p", 1),

    /* Ketoses */
    FRU("Fru", "D", "p", 2),
    TAG("Tag", "D", "p", 2),
    SOR("Sor", "L", "p", 2),
    PSI("Psi", "D", "p", 2),
    ERU("Eru", "D", "p", 2),
    RUL("Rul", "D", "p", 2),
    XUL("Xul", "D", "p", 2),
    SED("Sed", "D", "p", 2),

    /* Keto-ulosonic acids */
    KO("Ko", "D", "p", 2),
    KDO("Kdo", "D", "p", 2),
    KDN("Kdn", "D", "p", 2),
    NEU("Neu", "D", "p", 2),
    LEG("Leg", "D", "p", 2),
    PSE("Pse", "L", "p", 2),

    /* Amino sugars */
    BAC("Bac", "D", "p", 1),
    MUR("Mur", "D", "p", 1),

    /* Others */
    API("Api", "L", "p", 1),
    CYM("Cym", "D", "p", 1),
    OLE("Ole", "D", "p", 1),
    THE("The", "D", "p", 1),
    Aco("Aco", "D", "p", 1);

    private String trivialName;
    private String configuration1;
    private String ringSize;
    private int anomerciPosition;

    public String getTrivialName () { return this.trivialName; }

    public String getFirstConfiguration() {
        return this.configuration1;
    }

    public String getRingSize () { return this.ringSize;}

    public int getAnomerciPosition () {
        return this.anomerciPosition;
    }

    MonosaccharideIndex(String _trivialName, String _configuration, String _ringSize, int _anomericPos) {
        this.trivialName = _trivialName;
        this.configuration1 = _configuration;
        this.ringSize = _ringSize;
        this.anomerciPosition = _anomericPos;
    }

    public static MonosaccharideIndex forTrivialName (String _trivialName) {
        for (MonosaccharideIndex ind : MonosaccharideIndex.values()) {
            if (ind.trivialName.equals(_trivialName)) return ind;
        }

        return null;
    }

    public static MonosaccharideIndex forTrivialNameWithIgnore (String _trivialName) {
        for (MonosaccharideIndex ind : MonosaccharideIndex.values()) {
            if (ind.trivialName.equalsIgnoreCase(_trivialName)) return ind;
        }

        return null;
    }
}
