package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

public enum SNFGNodeDescriptor {

    HEX    ("Hexose", "Hex", "axxxxh-1x_1-5", "oxxxxh", "uxxxxh"),
    GLC    ("D-Glucose", "Glc", "a2122h-1x_1-5", "o2122h", "u2122h"),
    MAN    ("D-Mannose", "Man", "a1122h-1x_1-5", "o1122h", "u1122h"),
    GAL    ("D-Galactose", "Gal", "a2112h-1x_1-5", "o2112h", "u2112h"),
    GUL    ("D-Gulose", "Gul", "a2212h-1x_1-5", "o2212h", "u2212h"),
    ALT    ("D-Altrose", "Alt", "a1222h-1x_1-5", "o1222h", "u1222h"),
    ALL    ("L-Allose", "All", "a1111h-1x_1-5", "o1111h", "u1111h"),
    TAL    ("D-Talose", "Tal", "a1112h-1x_1-5", "o1112h", "u1112h"),
    IDO    ("L-Idose", "Ido", "a2121h-1x_1-5", "o2121h", "u2121h"),

    HEXNAC ("N-Acetyl-hexosamine", "HexNAc", "axxxxh-1x_1-5_2*NCC/3=O", "oxxxxh_2*NCC/3=O", "uxxxxh_2*NCC/3=O"),
    GLCNAC ("N-Acetyl-D-glucosamine", "GlcNAc", "a2122h-1x_1-5_2*NCC/3=O", "o2122h_2*NCC/3=O", "u2122h_2*NCC/3=O"),
    MANNAC ("N-Acetyl-D-mannosamine", "ManNAc", "a1122h-1x_1-5_2*NCC/3=O", "o1122h_2*NCC/3=O", "u1122h_2*NCC/3=O"),
    GALNAC ("N-Acetyl-D-galactosamine", "GalNAc", "a2112h-1x_1-5_2*NCC/3=O", "o2112h_2*NCC/3=O", "u2112h_2*NCC/3=O"),
    GULNAC ("N-Acetyl-D-gulosamine", "GulNAc", "a2212h-1x_1-5_2*NCC/3=O", "o2212h_2*NCC/3=O", "u2212h_2*NCC/3=O"),
    ALTNAC ("N-Acetyl-D-altrosamine", "AltNAc", "a1222h-1x_1-5_2*NCC/3=O", "o1222h_2*NCC/3=O", "u1222h_2*NCC/3=O"),
    ALLNAC ("N-Acetyl-L-allosamine", "AllNAc", "a1111h-1x_1-5_2*NCC/3=O", "o1111h_2*NCC/3=O", "u1111h_2*NCC/3=O"),
    TALNAC ("N-Acetyl-D-talosamine", "TalNAc", "a1112h-1x_1-5_2*NCC/3=O", "o1112h_2*NCC/3=O", "u1112h_2*NCC/3=O"),
    IDONAC ("N-Acetyl-L-idosamine", "IdoNAc", "a2121h-1x_1-5_2*NCC/3=O", "o2121h_2*NCC/3=O", "u2121h_2*NCC/3=O"),

    HEXN   ("Hexosamine", "HexN", "axxxxh-1x_1-5_2*N", "oxxxxh_2*N", "uxxxxh_2*N"),
    GLCN   ("D-Glucosamine", "GlcN", "a2122h-1x_1-5_2*N", "o2122h_2*N", "u2122h_2*N"),
    MANN   ("D-Mannosamine", "ManN", "a1122h-1x_1-5_2*N", "o1122h_2*N", "u1122h_2*N"),
    GALN   ("D-Galactosamine", "GalN", "a2112h-1x_1-5_2*N", "o2112h_2*N", "u2112h_2*N"),
    GULN   ("D-Gulosamine", "GulN", "a2212h-1x_1-5_2*N", "o2212h_2*N", "u2212h_2*N"),
    ALTN   ("D-Altrosamine", "AltN", "a1222h-1x_1-5_2*N", "o1222h_2*N", "u1222h_2*N"),
    ALLN   ("L-Allosamine", "AllN", "a1111h-1x_1-5_2*N", "o1111h_2*N", "u1111h_2*N"),
    TALN   ("D-Talosamine", "TalN", "a1112h-1x_1-5_2*N", "o1112h_2*N", "u1112h_2*N"),
    IDON   ("L-Idosamine", "IdoN", "a2121h-1x_1-5_2*N", "o2121h_2*N", "u2121h_2*N"),

    HEXA   ("Hexuronate", "HexA", "axxxxA-1x_1-5", "oxxxxA", "uxxxxA"),
    GLCA   ("D-Glucuronic acid", "GlcA", "a2122A-1x_1-5", "o2122A", "u2122A"),
    MANA   ("D-Mannuronic acid", "ManA", "a1122A-1x_1-5", "o1122A", "u1122A"),
    GALA   ("D-Galacturonic acid", "GalA", "a2112A-1x_1-5", "o2112A", "u2112A"),
    GULA   ("D-Guluonic acid", "GulA", "a2212A-1x_1-5", "o2212A", "u2212A"),
    ALTA   ("D-Altruronic acid", "AltA", "a1222A-1x_1-5", "o1222A", "u1222A"),
    ALLA   ("L-Alluronic acid", "AllA", "a1111A-1x_1-5", "o1111A", "u1111A"),
    TALA   ("D-Taluronic acid", "TalA", "a1112A-1x_1-5", "o1112A", "u1112A"),
    IDOA   ("L-Iduronic acid", "IdoA", "a2121A-1x_1-5", "o2121A", "u2121A"),

    DHEX   ("Deoxyhexose", "dHex", "axxxxm-1x_1-5", "oxxxxm", "uxxxxm"),
    QUI    ("D-Quinovose", "Qui", "a2122m-1x_1-5", "o2122m", "u2122m"),
    RHA    ("L-Rhamnose", "Rha", "a2211m-1x_1-5", "o2211m", "u2211m"),
    SIXDGUL("6-Deoxy-D-gulose", "6dGul", "a2212m-1x_1-5", "o2212m", "u2212m"),
    SIXDALT("6-Deoxy-L-altrose", "6dAlt", "a2111m-1x_1-5", "o2111m", "u2111m"),
    SIXDTAL("6-Deoxy-D-talose", "6dTal", "a1112m-1x_1-5", "o1112m", "u1112m"),
    FUC    ("L-Fucose", "Fuc", "a1221m-1x_1-5", "o1221m", "u1221m"),

    DHEXNAC     ("DeoxyhexNAc", "dHexNAc", "axxxxm-1x_1-5_2*NCC/3=O", "oxxxxm_2*NCC/3=O", "uxxxxm_2*NCC/3=O"),
    QUINAC      ("N-Acetyl-D-quinovosamine", "QuiNAc", "a2122m-1x_1-5_2*NCC/3=O", "o2122m_2*NCC/3=O", "u2122m_2*NCC/3=O"),
    RHANAC      ("N-Acetyl-L-rhamnosamine", "RhaNAc", "a2211m-1x_1-5_2*NCC/3=O", "o2211m_2*NCC/3=O", "u2211m_2*NCC/3=O"),
    SIXDALTNAC  ("N-Acetyl-6-deoxy-L-altrosamine", "6dAltNAc", "a2111m-1x_1-5_2*NCC/3=O", "o2111m_2*NCC/3=O", "u2111m_2*NCC/3=O"),
    SIXDTALNAC  ("N-Acetyl-6-deoxy-D-talosamine", "6dTalNAc", "a1112m-1x_1-5_2*NCC/3=O", "o1112m_2*NCC/3=O", "u1112m_2*NCC/3=O"),
    FUCNAC      ("N-Acetyl-L-fucosamine", "FucNAc", "a1221m-1x_1-5_2*NCC/3=O", "o1221m_2*NCC/3=O", "u1221m_2*NCC/3=O"),

    DDHEXNAC    ("Di-dexyhexose", "ddHex", "adxxxm-1x_1-5", "odxxxm", "udxxxm"),
    OLI         ("Olivose", "Oli", "ad122m-1x_1-5", "od122m", "ud122m"),
    TYV         ("Tyvelose", "Tyv", "a1d22m-1x_1-5", "o1d22m", "u1d22m"),
    ABE         ("Abequose", "Abe", "a2d12m-1x_1-5", "o2d12m", "u2d12m"),
    PAR         ("Pratose", "Par", "a2d22m-1x_1-5", "o2d22m", "u2d22m"),
    DIG         ("D-Digitoxose", "Dig", "ad222m-1x_1-5", "od222m", "ud222m"),
    COL         ("Colitose", "Col", "a1d21m-1x_1-5", "o1d21m", "u1d21m"),

    PEN         ("Pentose", "Pen", "axxxh-1x_1-5", "oxxxh", "uxxxh"),
    ARA         ("L-Arabinose", "Ara", "a211h-1x_1-5", "o211h", "u211h"),
    LYX         ("D-Lyxose", "Lyx", "a112h-1x_1-5", "o112h", "u112h"),
    XYL         ("D-Xylose", "Xyl", "a212h-1x_1-5", "o212h", "u212h"),
    RIB         ("D-Ribose", "Rib", "a222h-1x_1-5", "o222h", "u222h"),

    NULO        ("Deoxynonulosonate", "NulO", "Aadxxxxxh-2x_2-6", "AOdxxxxxh", "AUdxxxxxh"),
    KDN         ("3-Deoxy-D-glycero-D-galacto-nonulosonic Acid", "Kdn", "Aad21122h-2x_2-6", "AOd21122h", "AUd21122h"),
    NEU5AC      ("N-Acetylneuraminic acid", "Neu5Ac", "Aad21122h-2x_2-6_5*NCC/3=O", "AOd21122h_5*NCC/3=O", "AUd21122h_5*NCC/3=O"),
    NEU5GC      ("N-Glycolylneuraminic acid", "Neu5Gc", "Aad21122h-2x_2-6_5*NCCO/3=O", "AOd21122h_5*NCCO/3=O", "AUd21122h_5*NCCO/3=O"),
    Neu         ("Neuraminic acid", "Neu", "Aad21122h-2x_2-6_5*N", "AOd21122h_5*N", "AUd21122h_5*N"),

    DDNULO      ("Di-deoxynonulosonate", "ddNulO", "Aadxxxxxm-2x_2-6_5*N_7*N", "AOdxxxxxm_5*N_7*N", "AUdxxxxxm_5*N_7*N"),
    PSE         ("Pseudaminic acid", "Pse", "Aad22111m-2x_2-6_5*N_7*N", "AOd22111m_5*N_7*N", "AUd22111m_5*N_7*N"),
    LEG         ("Legionaminic acid", "Leg", "Aad21122m-2x_2-6_5*N_7*N", "AOd21122m_5*N_7*N", "AUd21122m_5*N_7*N"),
    ACI         ("Acinetaminic acid", "Aci", "Aad21111m-2x_2-6_5*N_7*N", "AOd21111m_5*N_7*N", "AUd21111m_5*N_7*N"),
    FOURELEG    ("4-Epilegionaminic acid", "4eLeg", "Aad11122m-2x_2-6_5*N_7*N", "AOd11122m_5*N_7*N", "AUd11122m_5*N_7*N"),

    BAC         ("Bacillosamine", "Bac", "a2122m-1x_1-5_2*N_4*N", "o2122m_2*N_4*N", "u2122m_2*N_4*N"),
    LDMANHEP    ("L-glycero-D-manno-Heptose", "LDmanHep", "a11221h-1x_1-5", "o11221h", "u11221h"),
    KDO         ("3-Deoxy-D-manno-octulosonic acid", "Kdo", "Aad1122h-2x_2-6", "AOd1122h", "AUd1122h"),
    DHA         ("3-Deoxy-D-lyxo-heptulosaric acid", "Dha", "Aad112A-2x_2-6", "AOd112A", "AUd112A"),
    DDMANHEP    ("D-glycero-D-manno-Heptose", "DDmanHep", "a11222h-1x_1-5", "o11222h", "u11222h"),
    MURNAC      ("N-Acetylmuramic acid", "MurNAc", "a2122h-1x_1-5_2*NCC/3=O_3*OC^RCO/4=O/3C", "o2122h_2*NCC/3=O_3*OC^RCO/4=O/3C", "u2122h_2*NCC/3=O_3*OC^RCO/4=O/3C"),
    MURNGC      ("N-Glycolylmuramic acid", "MurNGc", "a2122h-1x_1-5_2*NCCO/3=O_3*OC^RCO/4=O/3C", "o2122h_2*NCCO/3=O_3*OC^RCO/4=O/3C", "u2122h_2*NCCO/3=O_3*OC^RCO/4=O/3C"),
    MUR         ("Muramic acid", "Mur", "a2122h-1x_1-5_2*N_3*OC^RCO/4=O/3C", "o2122h_2*N_3*OC^RCO/4=O/3C", "u2122h_2*N_3*OC^RCO/4=O/3C"),

    API         ("L-Apiose", "Api", "a15h-1x_1-4_3*CO", "o15h_3*CO", "u15h_3*CO"),
    FRU         ("D-Fructose", "Fru", "ha122h-2x_2-6", "hO122h", "hU122h"),
    TAG         ("D-Tagatose", "Tag", "ha112h-2x_2-6", "hO112h", "hU112h"),
    SOR         ("L-Sorbose", "Sor", "ha121h-2x_2-6", "hO121h", "hU121h"),
    PSI         ("D-Psicose", "Psi", "ha222h-2x_2-6", "hO222h", "hU222h");

    SNFGNodeDescriptor (String _commonName, String _abbreviation, String _ringForm, String _openChain, String _composition) {
        this.commonName = _commonName;
        this.abbreviation = _abbreviation;
        this.ringForm = _ringForm;
        this.openChain = _openChain;
        this.composition = _composition;
    }

    private final String commonName;
    private final String abbreviation;
    private final String ringForm;
    private final String openChain;
    private final String composition;

    public static SNFGNodeDescriptor forAbbreviation (String _abbreviation) {
        for (SNFGNodeDescriptor value : SNFGNodeDescriptor.values()) {
            if (value.abbreviation.equals(_abbreviation)) return value;
        }
        return null;
    }

    public static SNFGNodeDescriptor forRingForm (String _ringForm) {
        for (SNFGNodeDescriptor value : SNFGNodeDescriptor.values()) {
            if (value.ringForm.equals(_ringForm)) return value;
        }
        return null;
    }

    public static SNFGNodeDescriptor forOpenChain (String _openChain) {
        for (SNFGNodeDescriptor value : SNFGNodeDescriptor.values()) {
            if (value.openChain.equals(_openChain)) return value;
        }
        return null;
    }

    public static SNFGNodeDescriptor forComposition (String _composition) {
        for (SNFGNodeDescriptor value : SNFGNodeDescriptor.values()) {
            if (value.composition.equals(_composition)) return value;
        }
        return null;
    }
}
