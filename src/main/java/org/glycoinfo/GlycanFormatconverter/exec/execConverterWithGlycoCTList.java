package org.glycoinfo.GlycanFormatconverter.exec;

import org.glycoinfo.WURCSFramework.io.GlycoCT.WURCSExporterGlycoCTList;

import java.util.TreeMap;

/**
 * Created by e15d5605 on 2019/02/20.
 */
public class execConverterWithGlycoCTList {
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
                        WURCSExporterGlycoCTList wegct = new WURCSExporterGlycoCTList();
                        wegct.start(openGlycoCT(args));
                    }
                } catch (Exception e) {
                    System.err.println("error");
                    e.printStackTrace();
                }
            } else if (args.length > 1) {
                try {
                    for (int i = 0; i < args.length; i++) {
                        if (i % 2 != 0) {
                            System.out.println(args);
                            WURCSExporterGlycoCTList wegct = new WURCSExporterGlycoCTList();
                            wegct.start(openGlycoCT(args));
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

    public static TreeMap<String, String> openGlycoCT (String[] args) {
        TreeMap<String, String> ret = new TreeMap<>();

        return ret;
    }
}
