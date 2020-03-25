package org.glycoinfo.GlycanFormatConverter.IUPAC.extended;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoExporterException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.extended.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.junit.Assert;
import org.junit.Test;

public class UnitTestOfIUPACExtendedImporter {

    @Test
    public void test () {
        String input = "";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G02420CD () {
        String input = "WURCS=2.0/5,37,36/[a2122h-1x_1-5_2*NCC/3=O][a1122h-1x_1-5][a2112h-1x_1-5][a1221m-1x_1-5][Aad21122h-2x_2-6_5*NCC/3=O]/1-1-2-2-1-3-1-3-1-4-3-4-4-1-3-1-3-1-3-5-2-1-3-1-3-1-3-5-4-4-1-3-1-3-1-3-4/a?-b1_a?-K1_b?-c1_c?-d1_c?-u1_d?-e1_d?-n1_e?-f1_e?-m1_f?-g1_g?-h1_g?-l1_h?-i1_i?-j1_i?-k1_n?-o1_o?-p1_p?-q1_q?-r1_r?-s1_s?-t2_u?-v1_u?-E1_v?-w1_w?-x1_x?-y1_x?-D1_y?-z1_z?-A1_z?-C1_A?-B2_E?-F1_F?-G1_G?-H1_H?-I1_I?-J1";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G04437UY () {
        String input = "WURCS=2.0/5,12,11/[axxxxh-1b_1-5_2*NCC/3=O][axxxxh-1x_1-5_2*NCC/3=O][axxxxh-1x_1-5][Aad21122h-2a_2-6_5*NCC/3=O][a1221m-1x_1-5]/1-2-3-3-2-3-4-3-2-3-4-5/a?-b1_a?-l1_b?-c1_c?-d1_c?-h1_d?-e1_e?-f1_f?-g2_h?-i1_i?-j1_j?-k2";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G80966KZ () {
        String input = "WURCS=2.0/3,8,7/[a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5]/1-1-2-3-3-3-3-3/a4-b1_b4-c1_c3-d1_c6-f1_d2-e1_f3-g1_f6-h1";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G83385WA () {
        String input = "WURCS=2.0/5,12,11/[AUd21122h_5*NCC/3=O][u1221m][u2122h_2*NCC/3=O][u1122h][u2112h]/1-1-2-3-3-3-3-4-4-4-5-5/a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?";
        //String input = "WURCS=2.0/5,12,0+/[AUd21122h_5*NCC/3=O][u1221m][u2122h_2*NCC/3=O][u1122h][u2112h]/1-1-2-3-3-3-3-4-4-4-5-5/";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G30048DT () {
        String input = "WURCS=2.0/7,12,11/[a2122h-1b_1-5_2*NCC/3=O][a1221m-1x_1-5][a1122h-1b_1-5][a1122h-1a_1-5][a2122h-1x_1-5_2*NCC/3=O][a2112h-1x_1-5][Aad21122h-2x_2-6_5*NCC/3=O]/1-2-1-3-4-5-6-7-4-5-6-7/a4-c1_c4-d1_d3-e1_d6-i1_e?-f1_f?-g1_g?-h2_i?-j1_j?-k1_k?-l2_b1-a?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G59068XW () {
        String input = "WURCS=2.0/3,5,4/[a2112h-1a_1-5_2*NCC/3=O][a2112h-1x_1-5][a2122h-1x_1-5_2*NCC/3=O]/1-2-3-2-3/a3-b1_b?-c1_b?-e1_c?-d1";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G96066HD () {
        String input = "WURCS=2.0/4,5,4/[a2112h-1a_1-5_2*NCC/3=O][a2112h-1b_1-5][a1221m-1a_1-5][a2122h-1b_1-5_2*NCC/3=O]/1-2-3-1-4/a3-b1_a6-e1_b2-c1_b3-d1";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G77199IL () {
        String input = "WURCS=2.0/3,4,3/[a212h-1a_1-5][a2112h-1b_1-5][a2122A-1b_1-5_3*OSO/3=O/3=O]/1-2-2-3/a4-b1_b3-c1_c3-d1";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G24504JY () {
        String input = "WURCS=2.0/4,7,6/[a2122h-1x_1-5][a2112h-1b_1-5][a2122h-1b_1-5_2*NCC/3=O][a1221m-1a_1-5]/1-2-3-2-4-3-2/a4-b1_b3-c1_b6-f1_c3-d1_d2-e1_f4-g1";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G87360NI () {
        String input = "WURCS=2.0/3,9,8/[a2112h-1a_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a2112h-1b_1-5]/1-2-3-2-3-2-3-2-3/a3-b1_a6-f1_b4-c1_c3-d1_d4-e1_f4-g1_g3-h1_h4-i1";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    @Test
    public void G13657KC () {
        String input = "WURCS=2.0/6,14,13/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1b_1-5][a1221m-1a_1-5]/1-2-3-4-2-5-2-5-4-2-5-2-5-6/a4-b1_a6-n1_b4-c1_c3-d1_c6-i1_d2-e1_e4-f1_f3-g1_g4-h1_i2-j1_j4-k1_k3-l1_l4-m1";
        String iupac = toIUPAC(input);
        String wurcs = toWURCS(iupac);
        Assert.assertEquals(input, wurcs);
    }

    private String toWURCS (String _iupac) {
        try {
            IUPACExtendedImporter iei = new IUPACExtendedImporter();
            GlyContainer gc = iei.start(_iupac);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toWURCS();
        } catch (GlycanException | GlyCoImporterException | WURCSException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String toIUPAC (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);

            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toIUPAC(IUPACStyleDescriptor.GREEK);
        } catch (GlyCoExporterException | GlycanException | TrivialNameException | WURCSException e) {
            e.printStackTrace();
        }

        return null;
    }
}