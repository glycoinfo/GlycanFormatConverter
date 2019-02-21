package org.glycoinfo.GlycanFormatConverter.WURCS;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.KCF.KCFImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by e15d5605 on 2017/12/12.
 */
public class KCF2WURCS {

    public static void main(String[] args) throws Exception {
        String input =
               //"src/test/resources/20171212_GlyTouCan_KCFsamples";
               "src/test/resources/KCFSample";

        File file = new File(input);

        if (file.isFile()) {
            LinkedHashMap<String, String> kcfDictionary = openString(input);

            StringBuilder ret = new StringBuilder();
            StringBuilder errorList = new StringBuilder();

            int done = 0;

            for (String key : kcfDictionary.keySet()) {

                try {
                    KCFImporter kcfImporter = new KCFImporter();
                    GlyContainer gc = kcfImporter.start(kcfDictionary.get(key));

                    ExporterEntrance ee = new ExporterEntrance(gc);
                    String wurcs = ee.toWURCS();

                    ret.append(key + "\t" + wurcs);

                    System.out.println(wurcs);
                    
                    done++;
                } catch (Exception e) {
                    ret.append(key + "\t" + "%");
                    errorList.append(kcfDictionary.get(key) + "\n");
                    System.out.println(key + " " + e.getMessage());
                    //e.printStackTrace();
                }

                ret.append("\n");
            }

            /* define file name */
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String fileName = sdf.format(date) + "_WURCSSample_from_KCF";

            String errorName = sdf.format(date) + "_errorKCF";

            /* write WURCS */
            //writeFile(ret.toString(), fileName);

            /* write error */
            //writeFile(errorList.toString(), errorName);

            System.out.println(done + "/" + kcfDictionary.size());
            //System.out.println(ret);
        }
    }

    public static void writeFile (String _result, String _fileName) throws IOException {
        /* file open */
        File file = new File(_fileName);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);

        /* write file */
        PrintWriter pw = new PrintWriter(bw);
        pw.println(_result);

        pw.close();

        return;
    }

    public static LinkedHashMap<String, String> openString(String _directory) throws Exception {
        try {
            return readKCF(new BufferedReader(new FileReader(_directory)));
        } catch (IOException e) {
            throw new Exception();
        }
    }

    public static LinkedHashMap<String, String> readKCF(BufferedReader _bf) throws IOException {
        String line;
        LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();

        StringBuilder kcfUnit = new StringBuilder();
        boolean isSkip = false;
        String key = "";

        while ((line = _bf.readLine()) != null) {
            line.trim();

            if (line.equals("")) continue;
            if (line.startsWith("%")) isSkip = true;
            if (line.startsWith("ENTRY")) {
                key = trimID(line).get(1);
            }

            kcfUnit.append(line + "\n");

            if (line.equals("///")) {
                if (!isSkip) ret.put(key, kcfUnit.toString());
                kcfUnit = new StringBuilder();
                isSkip = false;
            }
        }

        _bf.close();

        return ret;
    }

    public static ArrayList<String> trimID (String _id) {
        ArrayList<String> indexes = new ArrayList<>();

        for (String unit : _id.split("\\s")) {
            if (unit.equals("")) continue;
            indexes.add(unit);
        }

        return indexes;
    }


}
