package org.glycoinfo.GlycanFormatConverter.WURCS;

import org.glycoinfo.GlycanFormatConverter.util.fileHandler;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class groupTestForWURCStoWURCS {

    private final String filePath = "src/test/resources/";
    private final String outPath = "src/test/resources/result/";

    // for error cases
    private final String fileErrorPath = "./Error/";
    private final String outErrorPath = "./Error/";

    @Test
    public void fragments () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_fragments.tsv");
        fileHandler.writeFile(outPath + "WW_fragments", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void fragments2 () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_fragments2.tsv");
        fileHandler.writeFile(outPath + "WW_fragments2", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void bridge () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_bridgeMod.tsv");
        fileHandler.writeFile(outPath + "WW_bridgeMod", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void bridge2 () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_bridgeMod2.tsv");
        fileHandler.writeFile(outPath + "WW_bridgeMod2", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void repeats () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_repeats.tsv");
        fileHandler.writeFile(outPath + "WW_repeats", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void errorCasesFA () {
        String fileData = fileHandler.openTSV(fileErrorPath + "wurcs_roundrobin_failures_aglycon-2021-08-02.tsv");
        fileHandler.writeFile(outErrorPath + "WW_WRFA", this.makeMap2(fileData), ".tsv");
    }

    @Test
    public void errorCasesF () {
        String fileData = fileHandler.openTSV(fileErrorPath + "wurcs_roundrobin_failures-2021-08-02.tsv");
        fileHandler.writeFile(outErrorPath + "WW_WRF", this.makeMap2(fileData), ".tsv");
    }

    // utils

    private String toWURCS (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toWURCS();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private String modifyArray (String _wurcs) {
        try {
            WURCSFactory wf = new WURCSFactory(_wurcs);
            return wf.getWURCS();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private HashMap<String, ArrayList<String>> makeMap (String _fileData ) {
        HashMap<String, ArrayList<String>> resultMap = new HashMap<>();
        for (String item : _fileData.split("\\n")) {
            if (item.startsWith("?")) continue;
            String[] columns = item.split("\\t");
            String result = toWURCS(columns[1]);
            ArrayList<String> values = new ArrayList<>();

            //optimize test
            String optWURCS = this.modifyArray(columns[1]);
            String result2 = this.toWURCS(optWURCS);

            //values[0] : true/false/null #plane
            //values[1] : true/false/null #optimize
            //values[2] : original WURCS string
            //values[3] : reconverted WURCS string
            //values[4] : optimized WURCS string
            //values[5] : reconverted WURCS string

            //#0
            values.add(result == null ? null : result.startsWith("WURCS") ? String.valueOf(result.equals(columns[1])) : result);
            //#1
            values.add(result2 == null ? null : result2.startsWith("WURCS") ? String.valueOf(result2.equals(optWURCS)) : result2);
            //#2
            values.add(columns[1]);
            //#3
            values.add(result);
            //#4
            values.add(optWURCS);
            //#5
            values.add(result2);

            resultMap.put(columns[0], values);
        }

        return resultMap;
    }

    public HashMap<String, ArrayList<String>> makeMap2 (String _fileData) {
        HashMap<String, ArrayList<String>> resultMap = new HashMap<>();
        for (String item : _fileData.split("\\n")) {
            if (item.startsWith("?")) continue;
            String[] columns = item.split("\\t");

            ArrayList<String> values = new ArrayList<>();

            // plane
            //values.add(columns[1]);

            // modified
            values.add(this.modifyArray(columns[1]));

            // reconverted
            values.add(this.toWURCS(columns[1]));

            resultMap.put(columns[0], values);
        }

        System.out.println(resultMap);

        return resultMap;
    }
}
