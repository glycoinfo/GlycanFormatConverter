package org.glycoinfo.GlycanFormatConverter.GWS;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GWS.GWSExporter;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by e15d5605 on 2019/09/01.
 */
public class WURCStoGWS {

    @Test
    public void case1() throws Exception {
        String importStr = "WURCS=2.0/8,12,11/[h2122h_2*N][a2122h-1x_1-5_2*N][Aad1122h-2x_2-6][a11221h-1x_1-5][a2112A-1x_1-5][a11222h-1x_1-5][a2122h-1x_1-5][a211h-1x_1-5_4*N]/1-2-3-4-4-5-6-4-4-7-8-3/a?-b1_b?-c2_c?-d1_c?-k1_c?-l2_d?-e1_d?-j1_e?-f1_e?-i1_f?-g1_g?-h1";
        String exportStr = "redEnd--?D-GlcN,o--??1D-GlcN,p--??2D-Kdo,p((--??1L-L-gro-D-manHep,p(--??1L-L-gro-D-manHep,p(--??1D-GalA,p--??1D-D-gro-D-manHep,p--??1L-L-gro-D-manHep,p)--??1L-L-gro-D-manHep,p)--??1D-Glc,p)--??1L-Ara,p--4?1N)--??2D-Kdo,p$MONO,Und,0,0,redEnd";

        Assert.assertEquals(exportStr, this.commonUtil(importStr));

        return;
    }

    @Test
    public void case2 () throws Exception {
        //Conversion test for O-bond linkage type
        String importStr = "WURCS=2.0/2,2,1/[a1122h-1x_1-5_2-5][a2122A-1b_1-5_2*OSO/3=O/3=O]/1-2/a3-b1";
        String exportStr = "freeEnd--1?1D-Man,p(--2=1,5?2Anhydro)--3b1D-GlcA,p--2?1S$MONO,Und,0,0,freeEnd";

        Assert.assertEquals(exportStr, this.commonUtil(importStr));

        return;
    }

    private String commonUtil (String _importStr) throws Exception {
        String ret;

        WURCSImporter wi = new WURCSImporter();
        wi.start(_importStr);

        GWSExporter gwsExp = new GWSExporter();
        ret = gwsExp.start(wi.getGlyContainer());

        return ret;
    }
}
