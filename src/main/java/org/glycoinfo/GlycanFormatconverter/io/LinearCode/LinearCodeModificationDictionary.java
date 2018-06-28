package org.glycoinfo.GlycanFormatconverter.io.LinearCode;

/**
 * Created by e15d5605 on 2017/08/30.
 */
public enum LinearCodeModificationDictionary {

    DEOXY       ("DO", "deoxy"),
    ULOSONATE   ("UO", "ulo"),
    ALDONICACID ("O", "onic");

    private String linearCodeNotation;
    private String iupacNotation;

    public String getIupacNotation () {
        return this.iupacNotation;
    }

    LinearCodeModificationDictionary (String _linearCode, String _iupac) {
        this.linearCodeNotation = _linearCode;
        this.iupacNotation = _iupac;
    }

    public static LinearCodeModificationDictionary forLCnotation (String _linearCode) {
        for (LinearCodeModificationDictionary ind : LinearCodeModificationDictionary.values()) {
            if (ind.linearCodeNotation.equals(_linearCode)) return ind;
        }

        return null;
    }
}
