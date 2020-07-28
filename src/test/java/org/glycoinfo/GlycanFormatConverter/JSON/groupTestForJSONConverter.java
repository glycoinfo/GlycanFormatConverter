package org.glycoinfo.GlycanFormatConverter.JSON;

import org.glycoinfo.GlycanFormatConverter.util.fileHandler;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.JSON.GCJSONExporter;
import org.glycoinfo.GlycanFormatconverter.io.JSON.GCJSONImporter;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class groupTestForJSONConverter {

    private String filePath = "src/test/resources/";
    private String outPath = "src/test/resources/result/";
    private String error = "";

    @Test
    public void fragments () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_fragments.tsv");
        fileHandler.writeFile(outPath + "WURCSJSON_fragments", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void bridge () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_bridgeMod.tsv");
        fileHandler.writeFile(outPath + "WURCSJSON_bridgeMod", this.makeMap(fileData),  ".tsv");
    }

    @Test
    public void repeats () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_repeats.tsv");
        fileHandler.writeFile(outPath + "WURCSJSON_repeats", this.makeMap(fileData),  ".tsv");
    }

    @Test
    public void wrongCase1_bridge () {
        String inputWURCS = "WURCS=2.0/2,8,7/[a1122h-1a_1-5][a2112h-1b_1-5]/1-2-1-2-1-2-1-1/a4-b1_b6-c1*OPO*/3O/3=O_c4-d1_d6-e1*OPO*/3O/3=O_e4-f1_f6n1-g1n2*1OP^X*2/3O/3=O_g2-h1";
        String optWURCS = this.modifyArray(inputWURCS);
        String json = this.toWURCSJSON(optWURCS);
        String wurcs = this.toWURCS(json);
        //Assert.assertEquals(inputWURCS, optWURCS);
        Assert.assertEquals(optWURCS, wurcs);
    }

    @Test
    public void wrongCase2_bridge () {
        String inputWURCS = "WURCS=2.0/3,8,8/[h2h][a2122h-1a_1-5_2*NCC/3=O][a2122h-1a_1-5]/1-1-2-1-3-1-3-1/a1-b3*OPO*/3O/3=O_a3-d1*OPO*/3O/3=O_b1-h3*OPO*/3O/3=O_b2-c1_d2-e1_d3-f1*OPO*/3O/3=O_f2-g1_f3-h1*OPO*/3O/3=O~n";
        String optWURCS = this.modifyArray(inputWURCS);
        String json = this.toWURCSJSON(optWURCS);
        String wurcs = this.toWURCS(json);
        //Assert.assertEquals(inputWURCS, optWURCS);
        Assert.assertEquals(optWURCS, wurcs);
    }

    @Test
    public void errorCase1_bridge () {
        String inputWURCS = "WURCS=2.0/2,2,2/[h222h][a2112h-1a_1-5_4n1-6n2*1OC^RO*2/3CO/6=O/3C]/1-2/a4-b1_a1-a5*OPO*/3O/3=O~n";
        String optWURCS = this.modifyArray(inputWURCS);
        String json = this.toWURCSJSON(optWURCS);
        String wurcs = this.toWURCS(json);
        //Assert.assertEquals(inputWURCS, optWURCS);
        Assert.assertEquals(optWURCS, wurcs);
    }

    @Test
    public void errorCase3_bridge () {
        String inputWURCS = "WURCS=2.0/3,6,5/[o222h][a222h-1b_1-4][a222h-1x_1-4]/1-2-3-2-3-2/a1-b1_b3-e5*OPO*/3O/3=O_c1-d1_c5-f3*OPO*/3O/3=O_e1-f1";
        String optWURCS = this.modifyArray(inputWURCS);
        String json = this.toWURCSJSON(optWURCS);
        String wurcs = this.toWURCS(json);
        //Assert.assertEquals(inputWURCS, optWURCS);
        Assert.assertEquals(optWURCS, wurcs);
    }

    @Test
    public void errorCase1_repeat () {
        String inputWURCS = "WURCS=2.0/2,2,2/[a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5_4-6*OC^XO*/3CO/6=O/3C_2*NCC/3=O]/1-2/a4-b1_a1-b3~n";
        String optWURCS = this.modifyArray(inputWURCS);
        String json = this.toWURCSJSON(optWURCS);
        String wurcs = this.toWURCS(json);
        //Assert.assertEquals(inputWURCS, optWURCS);
        Assert.assertEquals(optWURCS, wurcs);
    }

    @Test
    public void errorCase2_repeat () {
        String inputWURCS = "WURCS=2.0/2,3,3/[a2122h-1x_1-?_3-6][a2122h-1a_1-5]/1-2-2/a4-b1_b4-c1_a1-c4~n";
        String optWURCS = this.modifyArray(inputWURCS);
        String json = this.toWURCSJSON(optWURCS);
        String wurcs = this.toWURCS(json);
        //Assert.assertEquals(inputWURCS, optWURCS);
        Assert.assertEquals(optWURCS, wurcs);
    }



    private String toWURCSJSON (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            wi.start(_wurcs);
            GCJSONExporter jsonExp = new GCJSONExporter();
            return jsonExp.start(wi.getConverter());
        } catch (Exception e) {
            e.printStackTrace();
            this.error += e.getMessage() + " / ";
            return e.getMessage();
        }
    }

    private String toWURCS (String _wurcsjson) {
        try {
            GCJSONImporter jsonImp = new GCJSONImporter();
            GlyContainer gc = jsonImp.start(_wurcsjson);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toWURCS();
        } catch (Exception e) {
            this.error += e.getMessage() + " / ";
            e.printStackTrace();
            return this.error;
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
            this.error = "";

            String[] columns = item.split("\\t");
            String wurcsjson = this.toWURCSJSON(columns[1]);
            String result = this.toWURCS(wurcsjson);
            ArrayList<String> values = new ArrayList<>();

            //optimize test
            String optWURCS = this.modifyArray(columns[1]);
            String wurcsjson2 = this.toWURCSJSON(optWURCS);
            String result2 = this.toWURCS(wurcsjson2);

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
}
