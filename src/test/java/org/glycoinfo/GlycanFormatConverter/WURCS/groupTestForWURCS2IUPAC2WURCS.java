package org.glycoinfo.GlycanFormatConverter.WURCS;

import org.glycoinfo.GlycanFormatConverter.util.fileHandler;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.extended.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class groupTestForWURCS2IUPAC2WURCS {

    private final static String filePath = "src/test/resources/";
    private final static String outPath = "src/test/resources/result/";

    @Test
    public void simple () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_simple.tsv");
        fileHandler.writeFile(outPath + "WIW_simple", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void fragments () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_fragments.tsv");
        fileHandler.writeFile(outPath + "WIW_fragments", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void fragments2 () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_fragments2.tsv");
        fileHandler.writeFile(outPath + "WIW_fragments2", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void bridge () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_bridgeMod.tsv");
        fileHandler.writeFile(outPath + "WIW_bridgeMod", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void bridge2 () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_bridgeMod2.tsv");
        fileHandler.writeFile(outPath + "WIW_bridgeMod2", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void repeats () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_repeats.tsv");
        fileHandler.writeFile(outPath + "WIW_repeats", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void glytoucanEntries () {
        String fileData = fileHandler.openTSV(filePath + "20210427-GlyTouCan_Entries_WURCS.tsv");
        fileHandler.writeFile(outPath + "GlyTouCanEntries", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void errorCase () {
        String fileData = fileHandler.openTSV(filePath + "errorCase.tsv");
        fileHandler.writeFile(outPath + "errorCase", this.makeMap(fileData), ".tsv");
    }

    // utils
    private Object inIUPAC (String _iupac) {
        try {
            IUPACExtendedImporter iein = new IUPACExtendedImporter();
            return iein.start(_iupac);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String toIUPAC (Object _gc, IUPACStyleDescriptor _style) {
        try {
            if (_gc == null) {
                throw new Exception("case1: the error happened in the toIUPAC");
            }
            if (_gc instanceof String) {
                throw new Exception((String) _gc);
            }
            ExporterEntrance ee = new ExporterEntrance((GlyContainer) _gc);
            return ee.toIUPAC(_style);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String optimizeWURCS (String _wurcs) {
        try {
            WURCSFactory wf = new WURCSFactory(_wurcs);
            return wf.getWURCS();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private Object inWURCS (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            return wi.start(this.optimizeWURCS(_wurcs));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String toWURCS (Object _gc) {
        try {
            if (_gc == null) {
                throw new Exception("case2: the error happened in the toWURCS");
            }
            if (_gc instanceof String) {
                throw new Exception((String) _gc);
            }
            ExporterEntrance ee = new ExporterEntrance((GlyContainer) _gc);
            return ee.toWURCS();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private HashMap<String, ArrayList<String>> makeMap (String _fileData ) {
        HashMap<String, ArrayList<String>> resultMap = new HashMap<>();
        for (String item : _fileData.split("\\n")) {
            if (item.startsWith("?")) continue;
            String[] columns = item.split("\\t");

            String iupac = "";
            String wurcs = "";

            // modify model wurcs
            String model = this.optimizeWURCS(columns[1]);

            // wurcs to iupac-extended
            iupac = this.toIUPAC(this.inWURCS(model), IUPACStyleDescriptor.GREEK);

            // iupac-extended to wurcs
            wurcs = this.toWURCS(this.inIUPAC(iupac));

            ArrayList<String> values = new ArrayList<>();

            //values[0] : model wurcs
            //values[1] : iupac-extended
            //values[2] : remodel wurcs
            //values[3] : error type

            //#0
            values.add(model);
            //#1
            values.add(iupac);
            //#2
            values.add(wurcs);
            //#3
            values.add(String.valueOf(model.equals(wurcs)));
            //#4
            /*
              0: complete
              1: can not support substituent
              2:
             */

            resultMap.put(columns[0], values);
        }

        return resultMap;
    }
}
