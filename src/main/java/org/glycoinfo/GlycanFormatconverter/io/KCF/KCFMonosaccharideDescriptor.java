package org.glycoinfo.GlycanFormatconverter.io.KCF;

/**
 * Created by e15d5605 on 2017/12/25.
 */
public enum KCFMonosaccharideDescriptor {

    /* Triose (three-carbon sugar) */
    GRO ("Gro"),

    /* Tetrose (four-carbon sugar) */
    ERY ("Ery"),
    THO ("Tho"),
    THR ("Thr"),

    /* Pentose (five-carbon sugar) */
    RIB ("Rib"),
    ARA ("Ara"),
    XYL ("Xyl"),
    LYX ("Lyx"),

    /* HEXOSE (six-carbon sugar) */
    GLC ("Glc"),
    GAL ("Gal"),
    MAN ("Man"),
    ALL ("All"),
    ALT ("Alt"),
    GUL ("Gul"),
    IDO ("Ido"),
    TAL ("Tal"),

    /* Heptose (seven-carbon sugar) */
    //HEP ("Hep", "?", "?", ""),
    //LGROMANHEP ("Lgro-manHep", "L,D", "p", ""),
    //DGROMANHEP ("gro-manHep", "D,D", "p", ""),

    /* Deoxysugar (hexose or pentose without a hydroxyl group at the 6-position or the 3-position) */
    FUC ("Fuc"),
    RHA ("Rha"),
    QUI ("Qui"),

    /* Di-deoxysugar (hexose without hydroxyl groups at the 6-position and another position) */
    OLI ("Oli"),
    TYV ("Tyv"),
    ASC ("Asc"),
    ABE ("Abe"),
    PAR ("Par"),
    DIG ("Dig"),
    COL ("Col"),

    /* Sialic acid */
    SIA ("Sia"),
    NEU ("Neu"),

    /* Ketose */
    PSI ("Psi"),
    FRU ("Fru"),
    SOR ("Sor"),
    TAG ("Tag"),
    XUL ("Xul"),
    SED ("Sed"),

    /* Others */
    API ("Api"),
    BAC ("Bac"),
    THE ("The"),
    ACO ("Aco"),
    CYM ("Cym"),
    MUR ("Mur"),
    DHA ("Dha"),
    KDO ("Kdo"),
    KDN ("Kdn");

    String codes;

    public String getCode () {
        return codes;
    }

    KCFMonosaccharideDescriptor (String _codes) {
        this.codes = _codes;
    }

    public static KCFMonosaccharideDescriptor forThreeCodeWithIgnore (String _threeCode) {
        for (KCFMonosaccharideDescriptor value : KCFMonosaccharideDescriptor.values()) {
            if (value.codes.equalsIgnoreCase(_threeCode)) return value;
        }

        return null;
    }

}
