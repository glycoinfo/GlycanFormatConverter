package org.glycoinfo.GlycanFormatconverter.util.validator.importer;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;

public class IUPACExtendedImporterValidator {

    public void validateExtended (GlyContainer _glyCo) throws GlyCoImporterException {
        for (Node node: _glyCo.getAllNodes()) {
            // check deoxy-neuraminic acid
            this.checkKetal(node);
        }
    }

    public void checkKetal (Node _node) throws GlyCoImporterException {
        Monosaccharide mono = (Monosaccharide) _node;
        int count = 0;
        /*
        if (mono.getSuperClass().equals(SuperClass.NON)) {
            count++;
        }
        if (mono.getStereos().isEmpty()) {
            count++;
        }
         */
        if (mono.getAnomer().equals(AnomericStateDescriptor.OPEN)) {
            count++;
        }
        if (mono.getRingStart() == -1 && mono.getRingEnd() == -1) {
            count++;
        }
        if (mono.getAnomericPosition() == 0) {
            count++;
        }

        // check contains ketone
        for (GlyCoModification gMod : mono.getModifications()) {
            if (gMod.getPositionOne() != 2) continue;
            if (!gMod.getModificationTemplate().equals(ModificationTemplate.KETONE_U)) continue;
            count++;
        }

        if (count == 4) {
            throw new GlyCoImporterException("IUPAC-Extended format can not handle ketose with open chain or unknown anomeric state.");
        }
    }
}
