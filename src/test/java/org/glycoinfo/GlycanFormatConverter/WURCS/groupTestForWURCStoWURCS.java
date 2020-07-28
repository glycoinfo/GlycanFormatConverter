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

    private String filePath = "src/test/resources/";
    private String outPath = "src/test/resources/result/";

    @Test
    public void wrongCase1 () {
        String inputWURCS = "WURCS=2.0/7,16,15/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1b_1-5][a1221m-1a_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-2-5-2-5-4-2-5-2-5-6-7-7/a4-b1_a6-n1_b4-c1_c3-d1_c6-i1_d2-e1_d4-g1_e4-f1_g4-h1_i2-j1_i6-l1_j4-k1_l4-m1_p2-a3|b3|c3|d3|e3|f3|g3|h3|i3|j3|k3|l3|m3|n3}_o2-a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?}";
        String optWURCS = this.modifyArray(inputWURCS);
        Assert.assertEquals(optWURCS, toWURCS(optWURCS));
    }

    @Test
    public void wrongCase2 () {
        String inputWURCS = "WURCS=2.0/3,3,2/[a2122h-1x_1-5][ha122h-2x_2-5][u211h]/1-2-3/a1-b2_b?-c?*OPO*/3O/3=O";
        String optWURCS = this.modifyArray(inputWURCS);
        Assert.assertEquals(optWURCS, toWURCS(optWURCS));
    }

    /*
    WURCS=2.0/5,12,11/[a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2122h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-1-2-3-3-1-4-1-4-1-4-5/a4-b1_b4-c1_c3-d1_c6-e1_f4-g1_h4-i1_j4-k1_f1-d2|e2}_h1-d2|e2}_j1-d2|e2}_l2-g3|g6|i3|i6|k3|k6}
    WURCS=2.0/7,19,18/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a1221m-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-4-5-2-5-6-7-6-2-5-6-7-6-2-6-7/a4-b1_a6-f1_b4-c1_c3-d1_c6-e1_g3-h1_g4-i1_i3-j2_i4-k1_l3-m1_l4-n1_n3-o2_n4-p1_q4-r1_r3-s2_q1-a2|b2|c2|d2|e2|f2|r2|s2}_g1-a?|b?|c?|d?|e?|f?|r?|s?}_l1-a?|b?|c?|d?|e?|f?|r?|s?}
    WURCS=2.0/7,20,19/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a1221m-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-4-2-5-6-7-6-2-5-6-6-2-6-6-2-6-6/a4-b1_b4-c1_c3-d1_c6-e1_f3-g1_f4-h1_h3-i2_h4-j1_k3-l1_k4-m1_m4-n1_o4-p1_p4-q1_r4-s1_s4-t1_f1-a?|b?|c?|d?|e?|s?|t?}_k1-a?|b?|c?|d?|e?|s?|t?}_o1-a?|b?|c?|d?|e?|s?|t?}_r1-a?|b?|c?|d?|e?|s?|t?}
     */
    @Test
    public void errorCase1 () {
        String inputWURCS = "WURCS=2.0/7,19,18/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a1221m-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-4-5-2-5-6-7-6-2-5-6-7-6-2-6-7/a4-b1_a6-f1_b4-c1_c3-d1_c6-e1_g3-h1_g4-i1_i3-j2_i4-k1_l3-m1_l4-n1_n3-o2_n4-p1_q4-r1_r3-s2_q1-a2|b2|c2|d2|e2|f2|r2|s2}_g1-a?|b?|c?|d?|e?|f?|r?|s?}_l1-a?|b?|c?|d?|e?|f?|r?|s?}";
        Assert.assertEquals(inputWURCS, toWURCS(this.modifyArray(inputWURCS)));
    }

    @Test
    public void errorCase2 () {
        String inputWURCS = "WURCS=2.0/3,3,2/[a261m-1a_1-4_3*C=O][a1211h-1a_1-5_2*NC][a222h-1b_1-4_1*N]/1-2-3/a2-b1_b3-c5*OPO*/3O/3=O";
        Assert.assertEquals(inputWURCS, toWURCS(this.modifyArray(inputWURCS)));
    }

    @Test
    public void errorCase3 () {
        String inputWURCS = "WURCS=2.0/2,11,10/[a222h-1b_1-4_1*N][a222h-1b_1-4_1*N_3*OPO/3O/3=O]/1-2-1-1-1-1-1-1-1-1-1/a3-b5*OPO*/3O/3=O_a5-c3*OPO*/3O/3=O_c5-h3*OPO*/3O/3=O_d3-f5*OPO*/3O/3=O_e3-j5*OPO*/3O/3=O_e5-k3*OPO*/3O/3=O_f3-k5*OPO*/3O/3=O_g3-i5*OPO*/3O/3=O_g5-j3*OPO*/3O/3=O_h5-i3*OPO*/3O/3=O";
        Assert.assertEquals(inputWURCS, toWURCS(this.modifyArray(inputWURCS)));
    }

    @Test
    public void errorCase4 () {
        String inputWURCS = "WURCS=2.0/3,8,8/[h2h][a2122h-1a_1-5_2*NCC/3=O][a2122h-1a_1-5]/1-1-2-1-3-1-3-1/a1-b3*OPO*/3O/3=O_a3-d1*OPO*/3O/3=O_b1-h3*OPO*/3O/3=O_b2-c1_d2-e1_d3-f1*OPO*/3O/3=O_f2-g1_f3-h1*OPO*/3O/3=O~n";
        Assert.assertEquals(inputWURCS, toWURCS(this.modifyArray(inputWURCS)));
    }

    @Test
    public void fragments () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_fragments.tsv");
        fileHandler.writeFile(outPath + "WURCS_fragments", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void fragments2 () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_fragments2.tsv");
        fileHandler.writeFile(outPath + "WURCS_fragments2", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void bridge () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_bridgeMod.tsv");
        fileHandler.writeFile(outPath + "WURCS_bridgeMod", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void bridge2 () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_bridgeMod2.tsv");
        fileHandler.writeFile(outPath + "WURCS_bridgeMod2", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void repeats () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_repeats.tsv");
        fileHandler.writeFile(outPath + "WURCS_repeats", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void wrongCase1_repeats () {
        String inputWURCS = "WURCS=2.0/2,2,2/[a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5_4-6*OC^XO*/3CO/6=O/3C_2*NCC/3=O]/1-2/a4-b1_a1-b3~n";
        Assert.assertEquals(inputWURCS, toWURCS(this.modifyArray(inputWURCS)));
    }

    @Test
    public void wrongCase2_repeats () {
        String inputWURCS = "WURCS=2.0/2,3,3/[a2122h-1x_1-?_3-6][a2122h-1a_1-5]/1-2-2/a4-b1_b4-c1_a1-c4~n";
        Assert.assertEquals(inputWURCS, toWURCS(this.modifyArray(inputWURCS)));
    }

    @Test
    public void wrongCase3_repeats () {
        String inputWURCS = "WURCS=2.0/1,3,4/[ha122h-2b_2-5]/1-1-1/a1-b4_a4-c1_b1-c4_a1-a4~n";
        Assert.assertEquals(inputWURCS, toWURCS(this.modifyArray(inputWURCS)));
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
}
