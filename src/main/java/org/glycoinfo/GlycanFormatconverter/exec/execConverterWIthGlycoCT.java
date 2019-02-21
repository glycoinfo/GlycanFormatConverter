package org.glycoinfo.GlycanFormatconverter.exec;


import org.glycoinfo.GlycanFormatconverter.io.GlycoCT.WURCSExporterGlycoCT;

/**
 * Created by e15d5605 on 2019/02/20.
 */
public class execConverterWIthGlycoCT {
    public static void main (String[] args) throws Exception {

        String t_strVersion = "GlycoCT to WURCS version ";

        try {
            if (args.length == 1) {
                try {
                    if (args[0].equals("-h") || args[0].equals("-H")) {
                        System.out.println(t_strVersion);
                        System.out.println("based on Glycan Format Converter");
                        return;
                    } else {
                        System.out.println(args[0]);
                        WURCSExporterGlycoCT wegct = new WURCSExporterGlycoCT();
                        wegct.start(args[0]);
                    }
                } catch (Exception e) {
                    System.err.println("error");
                    e.printStackTrace();
                }
            } else if (args.length > 1) {
                try {
                    for (int i = 0; i < args.length; i++) {
                        if (i % 2 != 0) {
                            System.out.println(args[0]);
                            WURCSExporterGlycoCT wegct = new WURCSExporterGlycoCT();
                            wegct.start(args[0]);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("error");
                    e.printStackTrace();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
