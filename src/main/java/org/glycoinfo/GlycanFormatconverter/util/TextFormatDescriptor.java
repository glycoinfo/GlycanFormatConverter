package org.glycoinfo.GlycanFormatconverter.util;

/**
 * Created by e15d5605 on 2018/12/12.
 */
public enum TextFormatDescriptor {

    KCF("KCF", "kcf"),
    GLYCOCT("GlycoCT", "glycoct"),
    LC("LinearCode", "linearcode"),
    EXTENDED("Extended", "extended"),
    CONDENSED("Condensed", "condensed"),
    SHORT("Short", "short"),
    WURCS("WURCS", "wurcs"),
    JSON("JSON", "json");

    String format;
    String descriptor;

    TextFormatDescriptor (String _format, String _descriptor) {
        this.format = _format;
        this.descriptor = _descriptor;
    }

    public static TextFormatDescriptor forFormat (String _format) {
        TextFormatDescriptor ret = null;

        for (TextFormatDescriptor desc : TextFormatDescriptor.values()) {
            if (desc.format.equals(_format)) {
                ret = desc;
            }
        }
        return ret;
    }

    public static TextFormatDescriptor forFormatIgnoreCase (String _format) {
        TextFormatDescriptor ret = null;

        for (TextFormatDescriptor desc : TextFormatDescriptor.values()) {
            if (_format.equalsIgnoreCase(desc.descriptor)) {
                ret = desc;
            }
        }

        return ret;
    }
}
