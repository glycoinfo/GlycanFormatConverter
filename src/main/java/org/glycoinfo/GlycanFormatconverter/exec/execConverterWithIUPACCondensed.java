package org.glycoinfo.GlycanFormatconverter.exec;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed.IUPACCondensedImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;

/**
 * Created by e15d5605 on 2017/10/23.
 */
public class execConverterWithIUPACCondensed {

    public static void main (String[] args) throws GlyCoImporterException, GlycanException {
        IUPACCondensedImporter ici = new IUPACCondensedImporter();
        ExporterEntrance ee;

        if (args.length == 0)
            throw new GlyCoImporterException("This converter is need IUPAC-Condensed.");

        if (args.length == 1) {
            try {
                System.out.println(args[0]);
                ee = new ExporterEntrance(ici.start(args[0]));
                System.out.println(ee.toWURCS());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args.length > 1) {
            try {
                for (int i = 0; i < args.length; i++) {
                    if (i % 2 != 0) {
                        System.out.println(args[i]);
                        //ici.start(args[i]);
                        ee = new ExporterEntrance(ici.start(args[i]));
                        System.out.println(ee.toWURCS());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
