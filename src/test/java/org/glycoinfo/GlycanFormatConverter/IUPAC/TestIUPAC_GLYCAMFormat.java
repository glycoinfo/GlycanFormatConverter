package org.glycoinfo.GlycanFormatConverter.IUPAC;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestIUPAC_GLYCAMFormat {

    @Test
    public void Basic() throws Exception {
        String in = "DGlcpb1-4DGlcpb1-OH";
        System.out.println(in);
    }

    @Test
    public void Branches() throws Exception {
        String in = "DManpa1-3DManpa1-3[DGalpb1-4DGalpb1-4]LRhapa1-OH";
        System.out.println(in);
    }

    @Test
    public void RepeatingUnits() throws Exception {
        String in = "DGlcpa1-[4DGlcpa1-]<9> OH";
        System.out.println(in);
    }

    @Test
    public void Cycles() throws Exception {
        String in = "{c[4DGlcpa1-]<7>}";
        System.out.println(in);
    }

    @Test
    public void Derivatives() throws Exception {
        String in = "DGalpNAc[4S,6S]b1-4DGlcpA[2S]b1-3DGlcpNSa1-4DGlcpA[2S]b1-4DGlcpNS[3S]a1-OME";
        System.out.println(in);
    }
}
