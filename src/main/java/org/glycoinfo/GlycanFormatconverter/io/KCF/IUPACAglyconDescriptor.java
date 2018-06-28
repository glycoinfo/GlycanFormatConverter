package org.glycoinfo.GlycanFormatconverter.io.KCF;

/**
 * Created by e15d5605 on 2017/07/31.
 */
public enum IUPACAglyconDescriptor {

    LIPIDA("LipidA"),
    R("R"),
    NTYPE("Asn"),
    OTYPE("Ser/Thr"),
    SERINE("Ser"),
    PDOLICHOL("P-Dol"),
    PPDOLICHOL("PP-Dol"),
    PPUND("PP-Und"),
    PHOSPHATE("P"),
    PHOSPHOETANE("PE"),
    CERAMIDE("Cer"),
    MYOINO("myo-Ino"),
    INOACYLP("Ino(acyl)-P"),
    INOP("Ino-P"),
    INO("Ino"),
    SPHNGOLIPID("Sph");

    String notation;

    IUPACAglyconDescriptor(String _notation) {
        notation = _notation;
    }

    public static IUPACAglyconDescriptor forNotation (String _notation) {
        for (IUPACAglyconDescriptor e : IUPACAglyconDescriptor.values()) {
            if (e.notation.equals(_notation)) return e;
        }

        return null;
    }
}
