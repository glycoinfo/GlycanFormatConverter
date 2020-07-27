package org.glycoinfo.GlycanFormatconverter.util.validator;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;

public class IUPACValidator {

    public void validateGlycan (GlyContainer _glyCo, IUPACStyleDescriptor _style) throws GlycanException {
        if (_style.equals(IUPACStyleDescriptor.GLYCANWEB)) {
            GLYCAMValidator glycamVali = new GLYCAMValidator();
            glycamVali.validateGLYCAM(_glyCo);
        }

        if (_style.equals(IUPACStyleDescriptor.EXTENDED) || _style.equals(IUPACStyleDescriptor.GREEK)) {
            IUPACExtendedValidator extendedVali = new IUPACExtendedValidator();
            extendedVali.validatedExtended(_glyCo);
        }

        if (_style.equals(IUPACStyleDescriptor.CONDENSED)) {
            IUPACCondensedValidator condensedVali = new IUPACCondensedValidator();
            condensedVali.validateCondensed(_glyCo);
        }

        if (_style.equals(IUPACStyleDescriptor.SHORT)) {
            IUPACShortValidator shortValidator = new IUPACShortValidator();
            shortValidator.validateShort(_glyCo);
        }

        //TODO : validator for LinearCode

        //TODO : validator for KCF
    }
}
