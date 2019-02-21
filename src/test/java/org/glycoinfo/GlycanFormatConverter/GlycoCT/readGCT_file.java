package org.glycoinfo.GlycanFormatConverter.GlycoCT;

import org.glycoinfo.GlycanFormatconverter.io.GlycoCT.WURCSExporterGlycoCTList;
import org.junit.Test;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * Created by e15d5605 on 2019/02/20.
 */
public class readGCT_file {

    //@Test
    //public void GlycoCTToWURCS () throws Exception {
    public static void main (String[] args) throws Exception {
        String directory = "src/test/resources/GlycoCTSample";

        if(directory == null || directory.equals("")) throw new Exception();

        File file = new File(directory);

        if(file.isFile()) {

            WURCSExporterGlycoCTList wegctList = new WURCSExporterGlycoCTList();

            try {
                TreeMap<String, String> gctMAP = openString(directory);

                //Import GlycoCT
                wegctList.start(gctMAP);

            } catch (Exception e) {
                e.printStackTrace();
            }

            TreeMap<String, String> wurcsList = wegctList.getMapIDToWURCS();

            for (String key : wurcsList.keySet()) {
                System.out.println(key + " " + wurcsList.get(key));
            }

            /* define file name */
            //Date date = new Date();
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            //String fileName = sdf.format(date) + "_WURCSSample_from_IUPAC";

			 /* write WURCS */
            //writeFile(results.toString(), "", fileName);
        }
    }

    private void writeFile (String _result, String _error, String _fileName) throws IOException {
        /* file open */
        File file = new File(_fileName);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);

        /* write file */
        PrintWriter pw = new PrintWriter(bw);
        pw.println(_result);
        pw.println(_error);

        pw.close();

        return;
    }

    /**
     *
     * @param a_strFile
     * @return
     * @throws Exception
     */
    public static TreeMap<String, String> openString (String a_strFile) throws Exception {
        try {
            return readGCT(new BufferedReader(new FileReader(a_strFile)));
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
    public static TreeMap<String, String> readGCT (BufferedReader a_bfFile) throws IOException {
        String line;
        TreeMap<String, String> ret = new TreeMap<>();
        ret.clear();

        boolean isRES = false;
        String gct = "";
        int count = 1;

        while((line = a_bfFile.readLine()) != null) {
            line.trim();
            if(line.startsWith("%")) continue;
            if(line.indexOf(" ") != -1) line = line.replace(" ", "\t");

            if (line.equals("RES")) {
                if (isRES) {
                    ret.put(String.valueOf(count), gct);
                    count++;

                    gct = "";
                } else {
                    isRES = true;
                }
            }

            gct += line + "\n";
        }

        ret.put(String.valueOf(count), gct);

        a_bfFile.close();

        return ret;
    }
}
