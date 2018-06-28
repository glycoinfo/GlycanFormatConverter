package org.glycoinfo.GlycanFormatconverter.exec;

import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.KCF.KCFImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;

/**
 * Created by e15d5605 on 2017/10/04.
 */
public class execConverterWithKCF {
    public static void main(String[] args) throws Exception {

        String t_strVersion = "KCF to IUPAC version ";

        /* importer */
        KCFImporter kcfimp = new KCFImporter();

        /* exporter */
        ExporterEntrance ee;

        try {
            if (args.length == 1) {
                try {
                    if (args[0].equals("-h") || args[0].equals("-H")) {
                        System.out.println(t_strVersion);
                        System.out.println("based on Glycan Format Converter");
                        return;
                    } else {
                        System.out.println(args[0]);
                        ee = new ExporterEntrance(kcfimp.start(args[0]));
                        System.out.println("Short\t" + ee.toIUPAC(IUPACStyleDescriptor.SHORT));
                        System.out.println("Condensed\t" + ee.toIUPAC(IUPACStyleDescriptor.CONDENSED));
                        System.out.println("Extended\t" + ee.toIUPAC(IUPACStyleDescriptor.EXTENDED));
                        System.out.println("Extended\t" + ee.toIUPAC(IUPACStyleDescriptor.GREEK));
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
                            ee = new ExporterEntrance(kcfimp.start(args[0]));
                            System.out.println("Short\t" + ee.toIUPAC(IUPACStyleDescriptor.SHORT));
                            System.out.println("Condensed\t" + ee.toIUPAC(IUPACStyleDescriptor.CONDENSED));
                            System.out.println("Extended\t" + ee.toIUPAC(IUPACStyleDescriptor.EXTENDED));
                            System.out.println("Extended\t" + ee.toIUPAC(IUPACStyleDescriptor.GREEK));
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
