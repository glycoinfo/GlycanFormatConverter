package org.glycoinfo.GlycanFormatconverter.util.validator.importer;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;

public class IUPACImporterValidator {

    public void validateImportExtended (GlyContainer _glyCo) throws GlyCoImporterException {
        IUPACExtendedImporterValidator iupacExVali = new IUPACExtendedImporterValidator();
        iupacExVali.validateExtended(_glyCo);
    }

    public void validateImportCondensed (GlyContainer _glyCo) {
        IUPACCondensedImporterValidator iupacCoVali = new IUPACCondensedImporterValidator();
        iupacCoVali.validateCondensed(_glyCo);
    }

    public void validateImportShort (GlyContainer _glyCo) {
        IUPACShortImporterValidator iupacShVali = new IUPACShortImporterValidator();
        iupacShVali.validateShort(_glyCo);
    }
}
