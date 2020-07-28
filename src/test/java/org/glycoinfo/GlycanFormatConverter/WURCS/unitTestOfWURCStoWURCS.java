package org.glycoinfo.GlycanFormatConverter.WURCS;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.WURCSValidation;
import org.junit.Assert;
import org.junit.Test;

public class unitTestOfWURCStoWURCS {

    @Test
    public void monosaccharide () {
        //https://glytoucan.org/Structures/Glycans/G97131OU
        String testWURCS = "WURCS=2.0/1,1,0/[a2122h-1x_1-5_6*OPO/3O/3=O]/1/";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void simple () {
        //https://glytoucan.org/Structures/Glycans/G00027JG
        String testWURCS = "WURCS=2.0/4,6,5/[a2112h-1b_1-?_2*NCC/3=O][a2112h-1b_1-5][a1221m-1a_1-5][a2122h-1b_1-5_2*NCC/3=O]/1-2-3-4-2-3/a3-b1_a6-d1_b2-c1_d4-e1_e2-f1";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void fuzzyLinkage () {
        //https://glytoucan.org/Structures/Glycans/G00140YZ
        String testWURCS = "WURCS=2.0/4,5,4/[h2112h_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a2112h-1b_1-5][a1221m-1a_1-5]/1-2-3-3-4/b4-c1_d2-e1_b1-a3|a6_d1-a3|a6";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void substituent () {
        //https://glytoucan.org/Structures/Glycans/G00020MO
        String testWURCS = "WURCS=2.0/3,4,3/[u2122h_2*NCC/3=O_6*OSO/3=O/3=O][a2112h-1b_1-5][a2122h-1b_1-5_2*NCC/3=O_6*OSO/3=O/3=O]/1-2-3-2/a4-b1_b3-c1_c4-d1";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void repeat () {
        //https://glytoucan.org/Structures/Glycans/G00003VQ
        String testWURCS = "WURCS=2.0/4,5,5/[a2112h-1b_1-5_2*NCC/3=O][a2122A-1b_1-5][a2112h-1a_1-5][a2122h-1b_1-5]/1-2-1-3-4/a3-b1_b4-c1_c4-d1_d3-e1_a1-e4~n";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void bridge () {
        //https://glytoucan.org/Structures/Glycans/G00019BE
        String testWURCS = "WURCS=2.0/5,5,5/[a2211m-1b_1-5][a2112h-1b_1-5][a2211m-1a_1-5][o2h][a2122h-1b_1-5]/1-2-3-4-5/a4-b1_b2-c1_b3-d2*OPO*/3O/3=O_b4-e1_a1-e4~n";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void bridgeSecondLinkage () {
        //https://glytoucan.org/Structures/Glycans/G14394SO
        String testWURCS = "WURCS=2.0/4,4,3/[hxh][a2122h-1x_1-5][a1221m-1a_1-5][a2112h-1b_1-5]/1-2-3-4/a3n2-b1n1*1NCCOP^XO*2/6O/6=O_b3-c1_b4-d1";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void bridgeRepeat () {
        //https://glytoucan.org/Structures/Glycans/G42377JL
        String testWURCS = "WURCS=2.0/2,4,4/[hxh][a2112h-1x_1-5_2*NCC/3=O]/1-1-1-2/a1-b1*OPO*/3O/3=O_b3-c1*OPO*/3O/3=O_c2-d1_a3-c3*OPO*/3O/3=O~n";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void cyclic () {
        //https://glytoucan.org/Structures/Glycans/G00048ZA
        String testWURCS = "WURCS=2.0/1,7,7/[a2122h-1a_1-5_2*OC_3*OC_6*N]/1-1-1-1-1-1-1/a1-g4_a4-b1_b4-c1_c4-d1_d4-e1_e4-f1_f4-g1";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void compositionHaveLinkages () {
        String testWURCS = "WURCS=2.0/4,15,14/[AUd21122h_5*NCC/3=O][uxxxxh_2*NCC/3=O][uxxxxh][u1221m]/1-2-2-2-2-2-2-3-3-3-4-4-4-4-4/a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void compositionSeparated () {
        String testWURCS = "WURCS=2.0/4,15,0+/[AUd21122h_5*NCC/3=O][uxxxxh_2*NCC/3=O][uxxxxh][u1221m]/1-2-2-2-2-2-2-3-3-3-4-4-4-4-4/";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void modification () {
        //https://glytoucan.org/Structures/Glycans/G00173GD
        String testWURCS = "WURCS=2.0/4,4,3/[a2122h-1a_1-5_2*NSO/3=O/3=O][a2121A-1a_1-5_2*OSO/3=O/3=O][a2122h-1a_1-5_2*NSO/3=O/3=O_6*OSO/3=O/3=O][a21EEA-1a_1-5_2*OSO/3=O/3=O]/1-2-3-4/a4-b1_b4-c1_c4-d1";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void fragment () {
        //https://glytoucan.org/Structures/Glycans/G00025YC
        String testWURCS = "WURCS=2.0/6,11,10/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1b_1-5][a1221m-1a_1-5]/1-2-3-4-2-5-2-5-4-2-6/a4-b1_a6-k1_b4-c1_c3-d1_c6-i1_d2-g1_e4-f1_g4-h1_i2-j1_e1-d4|d6|i4|i6}";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void fragmentSubstituent () {
        //https://glytoucan.org/Structures/Glycans/G00050XR
        String testWURCS = "WURCS=2.0/7,12,14/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][axxxxh-1x_1-?_2*NCC/3=O][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-5-6-7-7-4-5-6-7/a4-b1_b4-c1_e4-f1_f3-g2_g8-h2_j4-k1_k3-l2_c?-d1_c?-i1_d?-e1_i?-j1_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}*OCC/3=O_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}*OCC/3=O_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}*OCC/3=O";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void fragmentSamples () {
        //G82763XK
        String testWURCS = "WURCS=2.0/6,12,11/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-2-5-4-2-5-2-5-6/a4-b1_b4-c1_c3-d1_c6-g1_d2-e1_e4-f1_g2-h1_g6-j1_h4-i1_j4-k1_l1-f3|f6|i3|i6|k3|k6}";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void probability () {
        //https://glytoucan.org/Structures/Glycans/G00367NK
        String testWURCS = "WURCS=2.0/2,8,8/[a2112A-1a_1-5_6%?%*OC][a212h-1a_1-5]/1-1-2-1-1-2-1-2/a4-b1_b3-c1_b4-d1_d4-e1_e3-f1_e4-g1_g3-h1_a1-g4~n";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void probability2 () {
        //https://glytoucan.org/Structures/Glycans/G00393YJ
        String testWURCS = "WURCS=2.0/4,4,4/[a2211m-1b_1-5_2%.5%*OCC/3=O][a2122h-1a_1-5_2*N_3%.5%*OCC/3=O][a2122A-1a_1-5][a2122h-1b_1-5_2*NCC/3=O]/1-2-3-4/a3-b1_b4-c1_b6-d1_a1-d4~n";
        Assert.assertEquals(testWURCS, toWURCS(testWURCS));
    }

    @Test
    public void multiFragments () {
         String testWURCS =
                "WURCS=2.0/5,12,11/[a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2122h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-1-2-3-1-4-5-1-4-1-4-3/a4-b1_b4-c1_c3-d1_c6-l1_e4-f1_h4-i1_j4-k1_e1-d2|l2}_h1-d2|l2}_j1-d2|l2}_g2-f3|f6|i3|i6|k3|k6}";
        String optWURCS = this.optimizeWURCS(testWURCS);
        Assert.assertEquals(testWURCS, toWURCS(optWURCS));
    }

    @Test
    public void optTest () {
        String testWURCS = "WURCS=2.0/7,20,19/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a1221m-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-4-2-5-6-7-6-2-5-6-6-2-6-6-2-6-6/a4-b1_b4-c1_c3-d1_c6-e1_f3-g1_f4-h1_h3-i2_h4-j1_k3-l1_k4-m1_m4-n1_o4-p1_p4-q1_r4-s1_s4-t1_f1-a?|b?|c?|d?|e?|s?|t?}_k1-a?|b?|c?|d?|e?|s?|t?}_o1-a?|b?|c?|d?|e?|s?|t?}_r1-a?|b?|c?|d?|e?|s?|t?}";

        String result1 = this.optimizeWURCS(testWURCS);
        String result2 = this.optimizeWURCS(result1);
        System.out.println(testWURCS);
        System.out.println(result1);
        System.out.println(result2);
    }

    private String optimizeWURCS (String _wurcs) {
        try {
            WURCSFactory wf = new WURCSFactory(_wurcs);
            return wf.getWURCS();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String toWURCS (String _testWURCS) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_testWURCS);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toWURCS();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
