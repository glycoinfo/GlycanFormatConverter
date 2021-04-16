package org.glycoinfo.GlycanFormatConverter.WURCS;

import org.eurocarbdb.MolecularFramework.io.glycam.SugarExporterGlycam;
import org.eurocarbdb.MolecularFramework.io.glycam.SugarImporterGlycam;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.glycoinfo.GlycanFormatConverter.util.fileHandler;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.GlycoCT.GlyContainerToSugar;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class groupTestForWURCStoGLYCAM {

    private final String filePath = "src/test/resources/";
    private final String outPath = "src/test/resources/result/";
    private String error = "";

    @Test
    public void fragments () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_fragments.tsv");
        fileHandler.writeFile(outPath + "WURCS_fragmentsGLYCAM", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void bridge () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_bridgeMod.tsv");
        fileHandler.writeFile(outPath + "WURCS_bridgeModGLYCAM", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void simple () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_simple.tsv");
        fileHandler.writeFile(outPath + "WURCS_simpleGLYCAM", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void glycamVSglycam_MF () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_simple.tsv");
        fileHandler.writeFile(outPath + "WURCS_simpleGLYCAMvsGLYCAM", this.makeCompareMAP(fileData), ".tsv");
    }

    // utils

    private String toIUPACCondensed (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toIUPAC(IUPACStyleDescriptor.CONDENSED);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private String toIUPACExtended (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toIUPAC(IUPACStyleDescriptor.GREEK);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private String toGLYCAM (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toIUPAC(IUPACStyleDescriptor.GLYCANWEB);
        } catch (Exception e) {
            e.printStackTrace();
            this.error += e.getMessage() + " ";
            return "";
        }
    }

    private String toWURCS (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toWURCS();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            this.error += e.getMessage() + " ";
            return "";
        }
    }

    private String toGLYCAM_MF_glycam (String _glycam) {
        try {
            if (_glycam.equals("")) {
                throw new Exception("This sequence is empty.");
            }
            SugarImporterGlycam importGLYCAM = new SugarImporterGlycam();
            Sugar sugar = importGLYCAM.parse(_glycam);
            SugarExporterGlycam sugar2glycam = new SugarExporterGlycam();

            return sugar2glycam.export(sugar);
        } catch (Exception e) {
            e.printStackTrace();
            this.error += e.getMessage();
            return "";
        }
    }

    //TODO : バグってて使えない
    private String toGLYCAM_MF_wurcs (String _wurcs) {
        try {
            //to GlyContainer
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);

            //to sugar
            GlyContainerToSugar gc2sugar = new GlyContainerToSugar();
            gc2sugar.start(gc);
            Sugar sugar = gc2sugar.getConvertedSugar();

            SugarExporterGlycam sugar2glycam = new SugarExporterGlycam();
            return sugar2glycam.export(sugar);
        } catch (Exception e) {
            e.printStackTrace();
            this.error = e.getMessage();
            return "";
        }
    }

    private String modifyArray (String _wurcs) {
        try {
            WURCSFactory wf = new WURCSFactory(_wurcs);
            return wf.getWURCS();
        } catch (Exception e) {
            this.error += e.getMessage() + " ";
            return "";
        }
    }

    private HashMap<String, ArrayList<String>> makeMap (String _fileData ) {
        HashMap<String, ArrayList<String>> resultMap = new HashMap<>();
        int count = 0;
        for (String item : _fileData.split("\\n")) {
            if (item.startsWith("?")) continue;
            String[] columns = item.split("\\t");
            //String result = toGLYCAM(columns[1]);
            ArrayList<String> values = new ArrayList<>();

            //optimize test
            String optWURCS = modifyArray(columns[1]);
            String result2 = toGLYCAM(optWURCS);
            String condensed = toIUPACCondensed(optWURCS);

            //values[0] : true/false/null #plane //delete
            //values[1] : true/false/null #optimize //delete
            //values[2] : original WURCS string //delete
            //values[3] : reconverted WURCS string //delete
            //values[4] : optimized WURCS string
            //values[5] : reconverted WURCS string
            //values[6] : IUPAC-condensed
            //values[7] : error message

            //#0
            //values.add(result == null ? null : result.startsWith("WURCS") ? String.valueOf(result.equals(columns[1])) : result);
            //#1
            //values.add(result2 == null ? null : result2.startsWith("WURCS") ? String.valueOf(result2.equals(optWURCS)) : result2);
            //#2
            //values.add(columns[1]);
            //#3
            //values.add(result);
            //#4
            values.add(optWURCS);
            //#5
            values.add(result2);
            //#6
            values.add(condensed);
            //#7
            values.add(this.error);

            if (columns.length == 2) {
                resultMap.put(columns[0], values);
            } else {
                resultMap.put(String.valueOf(count), values);
            }

            count++;
            this.error = "";
        }

        return resultMap;
    }

    private HashMap<String, ArrayList<String>> makeCompareMAP (String _fileData) {
        HashMap<String, ArrayList<String>> resultMap = new HashMap<>();
        int count = 0;
        for (String item : _fileData.split("\\n")) {
            if (item.startsWith("?")) continue;
            String[] columns = item.split("\\t");
            ArrayList<String> values = new ArrayList<>();

            //optimize test
            String optWURCS = modifyArray(columns[1]);
            String glycam = toGLYCAM(optWURCS);
            String glycamMF = this.toGLYCAM_MF_glycam(glycam);

            values.add(optWURCS);
            values.add(glycam);
            values.add(glycamMF);
            values.add(glycam.equals(glycamMF) ? "true" : "false");
            values.add(this.error);

            if (columns.length == 2) {
                resultMap.put(columns[0], values);
            } else {
                resultMap.put(String.valueOf(count), values);
                count++;
            }

            this.error = "";
        }

        return resultMap;
    }
}
