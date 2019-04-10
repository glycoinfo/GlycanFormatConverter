package org.glycoinfo.GlycanFormatConverter.GlycoCT;

import org.glycoinfo.WURCSFramework.io.GlycoCT.WURCSExporterGlycoCT;

/**
 * Created by e15d5605 on 2019/02/20.
 */
public class readGCT_single {

    //@Test
    //public void GlycoCTToWURCS () {
    public static void main (String[] args) {
        String input =
                "RES\n" +
                "1b:x-dglc-HEX-x:x\n" +
                "2s:n-acetyl\n" +
                "3b:b-dglc-HEX-1:5\n" +
                "4s:n-acetyl\n" +
                "5b:b-dman-HEX-1:5\n" +
                "6b:a-dman-HEX-1:5\n" +
                "7b:a-dman-HEX-1:5\n" +
                "LIN\n" +
                "1:1d(2+1)2n\n" +
                "2:1o(4+1)3d\n" +
                "3:3d(2+1)4n\n" +
                "4:3o(4+1)5d\n" +
                "5:5o(3+1)6d\n" +
                "6:5o(6+1)7d";

                /*
                "RES\n" +
                        "1r:r1\n" +
                        "REP\n" +
                        "REP1:3o(1+1)2n=-1--1\n" +
                        "RES\n" +
                        "2s:phosphate\n" +
                        "3b:o-dman-HEX-0:0|1:aldi\n" +
                        "4s:(s)-pyruvate\n" +
                        "LIN\n" +
                        "1:2n(1+6)3o\n" +
                        "2:3o(4+1)4n\n" +
                        "3:3o(5+1)4n";
                 */

        try {
            WURCSExporterGlycoCT wegct = new WURCSExporterGlycoCT();
            wegct.start(input);
            String wurcs = wegct.getWURCS();
            System.out.println(wurcs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
