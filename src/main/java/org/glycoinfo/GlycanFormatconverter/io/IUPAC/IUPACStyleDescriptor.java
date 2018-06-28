package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public enum IUPACStyleDescriptor {

    SHORT("Short"),
    CONDENSED("Condensed"),
    EXTENDED("Extended"),
    GREEK("Greek"),
    GLYCANWEB("GlycanWeb");

    private String style;

    IUPACStyleDescriptor (String _style) {
        this.style = _style;
    }

    public static IUPACStyleDescriptor forStyle (String _style) {
        for (IUPACStyleDescriptor value : IUPACStyleDescriptor.values()) {
            if (_style.equals(value.style)) return value;
        }

        return null;
    }
}
