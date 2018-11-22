package org.glycoinfo.GlycanFormatConverter.IUPAC;

import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACCondensedImporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Created by e15d5605 on 2017/10/04.
 */
public class IUPACCondensedImporterFromFile {

    public static void main (String[] args) throws Exception {

        String string_list = "src/test/resources/sampleIUPACCondensed";

        if(string_list == null || string_list.equals("")) throw new Exception();

        File file = new File(string_list);

        if(file.isFile()) {
            String input = "";
            LinkedHashMap<String, String> iupacIndex = openString(string_list);

            StringBuilder results = new StringBuilder();
            StringBuilder errors = new StringBuilder();

            for(String key : iupacIndex.keySet()) {
                input = iupacIndex.get(key);

                try {
                    /* Import IUPAC */
                    IUPACCondensedImporter ii = new IUPACCondensedImporter();

                    /* GlyContainerToIUPAC */
                    IUPACExporter ie = new IUPACExporter();

                    ii.start(iupacIndex.get(key));

                    ie.start(ii.getGlyContainer());

                    if (!iupacIndex.get(key).equals(ie.getExtendedWithGreek())) {
                        results.append(input + "\n");
                        results.append(ie.getExtendedWithGreek() + "\n\n");
                    }
                } catch (Exception e) {
                    errors.append(key + "\t" + input + "\n");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println(results);
            System.out.println(errors);
        }
        else if(args.length > 0) {
        }else {
            throw new Exception("This file is not found !");
        }
    }

    /**
     *
     * @param a_strFile
     * @return
     * @throws Exception
     */
    public static LinkedHashMap<String, String> openString(String a_strFile) throws Exception {
        try {
            return readWURCS(new BufferedReader(new FileReader(a_strFile)));
        }catch (IOException e) {
            throw new Exception();
        }
    }

    /**
     *
     * @param a_bfFile
     * @return
     * @throws IOException
     */
    public static LinkedHashMap<String, String> readWURCS(BufferedReader a_bfFile) throws IOException {
        String line = "";
        LinkedHashMap<String, String> wret = new LinkedHashMap<String, String>();
        wret.clear();

        while((line = a_bfFile.readLine()) != null) {
            line.trim();
            if(line.startsWith("%")) continue;
            if(line.indexOf(" ") != -1) line = line.replace(" ", "\t");
            String[] IDandWURCS = line.split("\t");
            if (IDandWURCS.length == 2) {
                wret.put(IDandWURCS[0].trim(), IDandWURCS[1]);
            }
        }
        a_bfFile.close();

        return wret;
    }
}
