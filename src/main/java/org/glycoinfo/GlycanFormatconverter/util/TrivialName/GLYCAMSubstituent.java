package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

/**
 * References :
 * https://github.com/GLYCAM-Web/gmml/blob/stable/src/Glycan/monosaccharide.cc line 1613 void Glycan::Monosaccharide::UpdateComplexSugarChemicalCode()
 * N, NAc, NGc, NS, NP, NMe, Ac, Gc, S, P, Me, A
 */
public enum GLYCAMSubstituent {

    //
    N ("N"),
    NAc ("NAc"),
    NGc ("NGc"),
    NS ("NS"),
    NP ("NP"),
    NMe ("Nme"),
    Ac ("Ac"),
    Gc ("Gc"),
    S ("S"),
    P ("P"),
    Me ("Me"),
    A ("A");

    private final String glycanNotation;

    GLYCAMSubstituent (String _notation) {
        this.glycanNotation = _notation;
    }

    public String getGlycanNotation () {
        return this.glycanNotation;
    }

    public static GLYCAMSubstituent forNotation (String _notation) {
        for (GLYCAMSubstituent value : GLYCAMSubstituent.values()) {
            if (value.getGlycanNotation().equals(_notation)) {
                return value;
            }
        }

        return null;
    }
}
