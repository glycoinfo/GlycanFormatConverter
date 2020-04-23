package org.glycoinfo.GlycanFormatConverter.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed.CondensedConverter;
import org.glycoinfo.GlycanFormatconverter.io.JSON.GCJSONExporter;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

public class wurcsjsonexporter {

    @Test
    public void simple () {
        String wurcs = "WURCS=2.0/4,5,4/[a2112h-1a_1-5_2*NCC/3=O][a2112h-1b_1-5][a1221m-1a_1-5][a2122h-1b_1-5_2*NCC/3=O]/1-2-3-1-4/a3-b1_a6-e1_b2-c1_b3-d1";
        System.out.println(exportJSON(wurcs));
        System.out.println(toIUPAC(wurcs));
    }

    @Test
    public void repeat () {
        //G00056UI
        String wurcs = "WURCS=2.0/2,4,4/[a2112h-1b_1-5_2*NCC/3=O_6*OSO/3=O/3=O][a2122A-1b_1-5]/1-2-1-2/a3-b1_b4-c1_c3-d1_a1-d4~n";
        System.out.println(exportJSON(wurcs));
        System.out.println(toIUPAC(wurcs));
    }

    @Test
    public void bridge () {
        //G00321OQ
        String wurcs = "WURCS=2.0/2,2,1/[a1122h-1x_1-5][a2122h-1a_1-5_2*NCC/3=O]/1-2/a6n1-b1n2*OPO*/3O/3=O";
        System.out.println(exportJSON(wurcs));
        System.out.println(toIUPAC(wurcs));
    }

    @Test
    public void bridgeSecondLinkage () {
        //G14394SO
        String wurcs = "WURCS=2.0/4,4,3/[hxh][a2122h-1x_1-5][a1221m-1a_1-5][a2112h-1b_1-5]/1-2-3-4/a3n2-b1n1*1NCCOP^XO*2/6O/6=O_b3-c1_b4-d1";
        System.out.println(exportJSON(wurcs));
        System.out.println(toIUPAC(wurcs));
    }

    @Test
    public void bridgeRepeat () {
        //G00810OX
        String wurcs = "WURCS=2.0/2,2,2/[a2122h-1a_1-5_2*NCC/3=O][a1122h-1a_1-5_2*NCC/3=O]/1-2/a3-b1_a1-b6*OPO*/3O/3=O~n";
        System.out.println(exportJSON(wurcs));
        System.out.println(toIUPAC(wurcs));
    }

    @Test
    public void fragment () {
        //G00264HB
        String wurcs = "WURCS=2.0/8,18,17/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a1221m-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCCO/3=O][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-2-5-6-7-2-4-2-5-6-8-2-5-6-8/a4-b1_b4-c1_c3-d1_c4-i1_c6-j1_d2-e1_e3-f1_e4-g1_j2-k1_k3-l1_k4-m1_o3-p1_o4-q1_h2-g3|g6_n2-m3|m6_r2-q3|q6_o1-d4|d6|j4|j6}";
        System.out.println(exportJSON(wurcs));
        System.out.println(toIUPAC(wurcs));
    }

    private String toIUPAC (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toIUPAC(IUPACStyleDescriptor.GREEK);
        } catch (Exception e) {
            e.getMessage();
        }
        return null;
    }

    private String exportJSON (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            wi.start(_wurcs);
            GCJSONExporter gcjsonEx = new GCJSONExporter();
            return gcjsonEx.start(wi.getConverter());
        } catch (Exception e) {
            e.getMessage();
        }

        return null;
    }
}
