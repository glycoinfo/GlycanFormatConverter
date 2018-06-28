package org.glycoinfo.GlycanFormatconverter.exec;

import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;

/**
 * Created by e15d5605 on 2017/10/23.
 */
public class execConverterWithWURCS {

    public static void main(String[] args) throws Exception {

        String t_strVersion = "WURCS to IUPAC version ";

        /* importer */
        WURCSImporter wi = new WURCSImporter();

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
                        wi.start(args[0]);
                        ee = new ExporterEntrance(wi.getGlyContainer());
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
                            wi.start(args[1]);
                            ee = new ExporterEntrance(wi.getGlyContainer());
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
