package org.glycoinfo.GlycanFormatConverter.reader;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.io.KCF.KCFImporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Created by e15d5605 on 2017/07/31.
 */
public class KCFToGlyContainer {

    public static void main(String[] args) throws Exception {
        String directory =
                "src/test/resources/KCFSample";
                //"src/test/resources/sampleKCF";

        if (directory == null || directory.equals("")) throw new Exception("File could not found!");

        File file = new File(directory);

        if (file.isFile()) {
            String input = "";
            LinkedHashMap<String, String> kcfIndex = openString(directory);
            StringBuilder results = new StringBuilder();
            StringBuilder error = new StringBuilder();

            for (String key : kcfIndex.keySet()) {
                try {
                    KCFImporter kcfI = new KCFImporter();
                    GlyContainer glyco = kcfI.start(kcfIndex.get(key));

                    IUPACExporter ie = new IUPACExporter();
                    ie.start(glyco);

                    String kcfi = ie.getExtendedWithGreek();

                    IUPACExtendedImporter iei = new IUPACExtendedImporter();

                    glyco = iei.start(kcfi);

                    ie.start(glyco);

                    String iupaci = ie.getExtendedWithGreek();

                    if (kcfi.equals(iupaci)) {
                        System.out.println(kcfi + "\n");
                    } else {
                        results.append(kcfi + "\n");
                        results.append(iupaci + "\n");
                    }

                    System.out.println(results);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static LinkedHashMap<String, String> openString(String _directory) throws Exception {
        try {
            return readKCF(new BufferedReader(new FileReader(_directory)));
        } catch (IOException e) {
            throw new Exception();
        }
    }

    public static LinkedHashMap<String, String> readKCF(BufferedReader _bf) throws IOException {
        String line = "";
        LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
        int count = 0;

        StringBuilder kcfUnit = new StringBuilder();
        boolean isSkip = false;

        while ((line = _bf.readLine()) != null) {
            line.trim();
            if (line.equals("")) continue;
            if (line.startsWith("%")) isSkip = true;
            if (line.startsWith("ENTRY")) count++;

            kcfUnit.append(line + "\n");

            if (line.equals("///")) {
                if (!isSkip) ret.put(String.valueOf(count), kcfUnit.toString());
                kcfUnit = new StringBuilder();
                isSkip = false;
            }
        }

        _bf.close();

        return ret;
    }
}