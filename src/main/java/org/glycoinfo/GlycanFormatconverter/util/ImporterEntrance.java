package org.glycoinfo.GlycanFormatconverter.util;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACCondensedImporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.io.JSON.GCJSONImporter;
import org.glycoinfo.GlycanFormatconverter.io.KCF.KCFImporter;
import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeImporter;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.json.JSONObject;

/**
 * Created by e15d5605 on 2017/10/23.
 */
public class ImporterEntrance {

    private String input;

    public ImporterEntrance (String _input) {
        this.input = _input;
    }

    public GlyContainer start (String _input) throws GlyCoImporterException, WURCSException, GlycanException {

        GlyContainer ret = null;

        switch (checkFormat(_input)) {
            case "KCF" :
                KCFImporter kcfImporter = new KCFImporter();
                ret = kcfImporter.start(_input);
                break;

            case "LinearCode" :
                LinearCodeImporter lcImporter = new LinearCodeImporter();
                ret = lcImporter.start(_input);
                break;

            case "WURCS" :
                WURCSFactory wf = new WURCSFactory(_input);
                WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();
                wg2gc.start(wf.getGraph());
                ret = wg2gc.getGlycan();
                break;

            case "Extended" :
                IUPACExtendedImporter ieImporter = new IUPACExtendedImporter();
                ret = ieImporter.start(_input);
                break;

            case "Condensed" :
                IUPACCondensedImporter icImporter = new IUPACCondensedImporter();
                icImporter.start(_input);
                ret = icImporter.getGlyContainer();
                break;

            case "Short" :
                break;

            case "JSON" :
                GCJSONImporter gcImporter = new GCJSONImporter();
                ret = gcImporter.start(_input);
                break;
        }

        if (ret == null)
            throw new GlyCoImporterException("This sequence can not be handled by the GlycanFormatConverter.");

        return ret;
    }

    private String checkFormat (String _inputString) throws GlyCoImporterException {

        if (isKCF(_inputString)) {
            return TextFormatDescriptor.KCF.format;
        }

        if (this.isCondensed(_inputString)) {
            return TextFormatDescriptor.CONDENSED.format;
        }

        if (this.isExtended(_inputString)) {
            return TextFormatDescriptor.EXTENDED.format;
        }

        if (this.isShort(_inputString)) {
            return TextFormatDescriptor.SHORT.format;
        }

        if (this.isGlycoCT(_inputString)) {
            return TextFormatDescriptor.GLYCOCT.format;
        }

        if (this.isJSON(_inputString)) {
            return TextFormatDescriptor.JSON.format;
        }

        if (this.isLinearCode(_inputString)) {
            return TextFormatDescriptor.LC.format;
        }

        if (this.isWURCS(_inputString)) {
            return TextFormatDescriptor.WURCS.format;
        }

        throw new GlyCoImporterException("GlycanFormatConverter could not this string: " + _inputString);
    }

    private boolean isKCF (String _input) {
        return (_input.indexOf("NODE") != -1);
    }

    private boolean isGlycoCT (String _input) {
        return (_input.indexOf("RES") != -1);
    }

    private boolean isCondensed (String _input) {
        return true;
    }

    private boolean isExtended (String _input) {
        return true;
    }

    private boolean isShort (String _input) {
        return true;
    }

    private boolean isJSON (String _input) {
        JSONObject obj = new JSONObject(_input);
        return (obj != null);
    }

    private boolean isLinearCode (String _input) {
        return true;
    }

    private boolean isWURCS (String _input) {
        return (_input.startsWith("WURCS"));
    }
}
