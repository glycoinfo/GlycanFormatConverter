package org.glycoinfo.GlycanFormatConverter.WURCS;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.extended.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.junit.Assert;
import org.junit.Test;

public class unitTestWURCS2IUPAC2WURCS {

    @Test
    public void monosaccharide () {
        //https://glytoucan.org/Structures/Glycans/G97131OU
        String inWURCS = "WURCS=2.0/1,1,0/[a2122h-1x_1-5_6*OPO/3O/3=O]/1/";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void simple () {
        //https://glytoucan.org/Structures/Glycans/G00027JG
        String inWURCS = "WURCS=2.0/4,6,5/[a2112h-1b_1-?_2*NCC/3=O][a2112h-1b_1-5][a1221m-1a_1-5][a2122h-1b_1-5_2*NCC/3=O]/1-2-3-4-2-3/a3-b1_a6-d1_b2-c1_d4-e1_e2-f1";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void fuzzyLinkage () {
        //https://glytoucan.org/Structures/Glycans/G00140YZ
        String inWURCS = "WURCS=2.0/4,5,4/[h2112h_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a2112h-1b_1-5][a1221m-1a_1-5]/1-2-3-3-4/b4-c1_d2-e1_b1-a3|a6_d1-a3|a6";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void substituent () {
        //https://glytoucan.org/Structures/Glycans/G00020MO
        String inWURCS = "WURCS=2.0/3,4,3/[u2122h_2*NCC/3=O_6*OSO/3=O/3=O][a2112h-1b_1-5][a2122h-1b_1-5_2*NCC/3=O_6*OSO/3=O/3=O]/1-2-3-2/a4-b1_b3-c1_c4-d1";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void repeat () {
        //https://glytoucan.org/Structures/Glycans/G00003VQ
        String inWURCS = "WURCS=2.0/4,5,5/[a2112h-1b_1-5_2*NCC/3=O][a2122A-1b_1-5][a2112h-1a_1-5][a2122h-1b_1-5]/1-2-1-3-4/a3-b1_b4-c1_c4-d1_d3-e1_a1-e4~n";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void bridge () {
        //https://glytoucan.org/Structures/Glycans/G00019BE
        String inWURCS = "WURCS=2.0/5,5,5/[a2211m-1b_1-5][a2112h-1b_1-5][a2211m-1a_1-5][o2h][a2122h-1b_1-5]/1-2-3-4-5/a4-b1_b2-c1_b3-d2*OPO*/3O/3=O_b4-e1_a1-e4~n";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void bridge2 () {
        //https://glytoucan.org/Structures/Glycans/G73750JI
        String inWURCS = "WURCS=2.0/2,8,7/[a1122h-1a_1-5][a2112h-1b_1-5]/1-2-1-2-1-2-1-1/a4-b1_b6-c1*OPO*/3O/3=O_c4-d1_d6-e1*OPO*/3O/3=O_e4-f1_f6n1-g1n2*1OP^X*2/3O/3=O_g2-h1";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void bridgeSecondLinkage () {
        //https://glytoucan.org/Structures/Glycans/G14394SO
        String inWURCS = "WURCS=2.0/4,4,3/[hxh][a2122h-1x_1-5][a1221m-1a_1-5][a2112h-1b_1-5]/1-2-3-4/a3n2-b1n1*1NCCOP^XO*2/6O/6=O_b3-c1_b4-d1";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void bridgeRepeat () {
        //https://glytoucan.org/Structures/Glycans/G42377JL
        String inWURCS = "WURCS=2.0/2,4,4/[hxh][a2112h-1x_1-5_2*NCC/3=O]/1-1-1-2/a1-b1*OPO*/3O/3=O_b3-c1*OPO*/3O/3=O_c2-d1_a3-c3*OPO*/3O/3=O~n";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void cyclic () {
        //https://glytoucan.org/Structures/Glycans/G00048ZA
        String inWURCS = "WURCS=2.0/1,7,7/[a2122h-1a_1-5_2*OC_3*OC_6*N]/1-1-1-1-1-1-1/a1-g4_a4-b1_b4-c1_c4-d1_d4-e1_e4-f1_f4-g1";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void cyclicModification () {
        //https://glytoucan.org/Structures/Glycans/G02833XU
        String inWURCS = "WURCS=2.0/6,8,7/[a2122h-1x_1-5][a2122h-1b_1-5][a2122A-1b_1-5][a21FFA-1a_1-?][a2122h-1b_1-5_4-6*OC^XO*/3CO/6=O/3C][a2112h-1b_1-5_4-6*OC^XO*/3CO/6=O/3C]/1-2-3-4-2-2-5-6/a4-b1_a6-e1_b4-c1_c4-d1_e4-f1_f4-g1_g3-h1";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void compositionHaveLinkages () {
        String inWURCS = "WURCS=2.0/4,15,14/[AUd21122h_5*NCC/3=O][uxxxxh_2*NCC/3=O][uxxxxh][u1221m]/1-2-2-2-2-2-2-3-3-3-4-4-4-4-4/a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void compositionSeparated () {
        String inWURCS = "WURCS=2.0/4,15,0+/[AUd21122h_5*NCC/3=O][uxxxxh_2*NCC/3=O][uxxxxh][u1221m]/1-2-2-2-2-2-2-3-3-3-4-4-4-4-4/";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void modification () {
        //https://glytoucan.org/Structures/Glycans/G00173GD
        String inWURCS = "WURCS=2.0/4,4,3/[a2122h-1a_1-5_2*NSO/3=O/3=O][a2121A-1a_1-5_2*OSO/3=O/3=O][a2122h-1a_1-5_2*NSO/3=O/3=O_6*OSO/3=O/3=O][a21EEA-1a_1-5_2*OSO/3=O/3=O]/1-2-3-4/a4-b1_b4-c1_c4-d1";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void fragment () {
        //https://glytoucan.org/Structures/Glycans/G00025YC
        String inWURCS = "WURCS=2.0/6,11,10/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1b_1-5][a1221m-1a_1-5]/1-2-3-4-2-5-4-2-6-2-5/a4-b1_a6-i1_b4-c1_c3-d1_c6-g1_d2-e1_e4-f1_g2-h1_j4-k1_j1-d4|d6|g4|g6}";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void fragmentSubstituent () {
        //https://glytoucan.org/Structures/Glycans/G00050XR
        String inWURCS = "WURCS=2.0/7,12,14/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][axxxxh-1x_1-?_2*NCC/3=O][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-5-6-7-7-4-5-6-7/a4-b1_b4-c1_e4-f1_f3-g2_g8-h2_j4-k1_k3-l2_c?-d1_c?-i1_d?-e1_i?-j1_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}*OCC/3=O_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}*OCC/3=O_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}*OCC/3=O";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void fragmentSamples () {
        //G82763XK
        String inWURCS = "WURCS=2.0/6,12,11/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-2-5-4-2-5-2-5-6/a4-b1_b4-c1_c3-d1_c6-g1_d2-e1_e4-f1_g2-h1_g6-j1_h4-i1_j4-k1_l1-f3|f6|i3|i6|k3|k6}";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void probability () {
        //https://glytoucan.org/Structures/Glycans/G00367NK
        String inWURCS = "WURCS=2.0/2,8,8/[a2112A-1a_1-5_6%?%*OC][a212h-1a_1-5]/1-1-2-1-1-2-1-2/a4-b1_b3-c1_b4-d1_d4-e1_e3-f1_e4-g1_g3-h1_a1-g4~n";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void probability2 () {
        //https://glytoucan.org/Structures/Glycans/G00393YJ
        String inWURCS = "WURCS=2.0/4,4,4/[a2211m-1b_1-5_2%.5%*OCC/3=O][a2122h-1a_1-5_2*N_3%.5%*OCC/3=O][a2122A-1a_1-5][a2122h-1b_1-5_2*NCC/3=O]/1-2-3-4/a3-b1_b4-c1_b6-d1_a1-d4~n";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void multiFragments () {
        String inWURCS =
                "WURCS=2.0/5,12,11/[a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2122h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-1-2-3-1-4-5-1-4-1-4-3/a4-b1_b4-c1_c3-d1_c6-l1_e4-f1_h4-i1_j4-k1_e1-d2|l2}_h1-d2|l2}_j1-d2|l2}_g2-f3|f6|i3|i6|k3|k6}";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void hloseSubstituent () {
        String inWURCS = "WURCS=2.0/1,1,0/[a26h-1b_1-4_3*CO]/1/";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void anhydro () {
        String inWURCS = "WURCS=2.0/1,1,0/[Ad2dd22h_3-7_1*OC_6*N]/1/";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void deoxySubstituent () {
        String inWURCS = "WURCS=2.0/3,9,0+/[uxxxxh_2*NCC/3=O][axxxxh-1x_1-5_?*][axxxxh-1x_1-5]/1-1-1-1-2-3-3-3-3/";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    @Test
    public void errorCase () {
        String inWURCS = "WURCS=2.0/1,4,3/[a222h-1x_1-4]/1-1-1-1/a1-b1_c1-d1_b?-d?*OPO*/3O/3=O";
        // WURCS2IUPAC
        String iupac = this.toIUPAC(this.inWURCS(inWURCS));

        System.out.println(iupac);

        // IUPAC2WURCS
        String outWURCS = this.toWURCS(this.inIUPAC(iupac));
        Assert.assertEquals(inWURCS, outWURCS);
    }

    private String optimizeWURCS (String _wurcs) {
        try {
            WURCSFactory wf = new WURCSFactory(_wurcs);
            return wf.getWURCS();
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
            e.printStackTrace();
        }

        return null;
    }

    private String toIUPAC (Object _gc) {
        try {
            if (_gc == null) {
                throw new Exception("case1: the error happened in the toIUPAC");
            }
            if (_gc instanceof String) {
                throw new Exception((String) _gc);
            }
            ExporterEntrance ee = new ExporterEntrance((GlyContainer) _gc);
            return ee.toIUPAC(IUPACStyleDescriptor.GREEK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Object inWURCS (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            return wi.start(this.optimizeWURCS(_wurcs));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private Object inIUPAC (String _iupac) {
        try {
            IUPACExtendedImporter iein = new IUPACExtendedImporter();
            return iein.start(_iupac);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
