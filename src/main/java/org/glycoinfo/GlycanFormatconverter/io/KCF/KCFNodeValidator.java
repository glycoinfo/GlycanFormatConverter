package org.glycoinfo.GlycanFormatconverter.io.KCF;

import org.glycoinfo.GlycanFormatconverter.Glycan.AnomericStateDescriptor;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;

public class KCFNodeValidator {

    public KCFNodeValidator () {}

    public void start (Node _node) throws GlyCoImporterException {
        if (!(_node instanceof Monosaccharide)) return;
        Monosaccharide mono = (Monosaccharide) _node;

        // check anomeric state and position
        if (mono.getAnomer().equals(AnomericStateDescriptor.OPEN)) {
            if (mono.getAnomericPosition() != -1) {
                throw new GlyCoImporterException("The structural information of the monosaccharide in unclear: ring-opening or ring-closure is not clear.");
            }
        }
    }
}
