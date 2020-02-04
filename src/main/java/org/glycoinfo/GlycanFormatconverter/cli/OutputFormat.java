package org.glycoinfo.GlycanFormatconverter.cli;

public enum OutputFormat {

    IUPAC_SHORT,
    IUPAC_CONDENSED,
    IUPAC_EXTENDED,
    GLYCOCT,
    WURCS,
    GLYCANWEB;

    public static OutputFormat forOutputFormat (String _output) throws Exception {
        switch (_output.toLowerCase()) {
            case "iupac-short":
                return IUPAC_SHORT;
            case "iupac-condensed":
                return IUPAC_CONDENSED;
            case "iupac-extended":
                return IUPAC_EXTENDED;
            case "glycoct":
                return GLYCOCT;
            case "wurcs":
                return WURCS;
            case "glycanweb":
                return GLYCANWEB;
            default:
                throw new Exception("This format is unknown format : " + _output);
        }
    }
}
