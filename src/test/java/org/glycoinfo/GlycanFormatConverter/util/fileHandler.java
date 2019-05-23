package org.glycoinfo.GlycanFormatConverter.util;

import java.io.*;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2019/04/05.
 */
public class fileHandler {


    public static void writeFile (String _result, String _error, String _fileName) throws IOException {
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

    public static String openErrorFile (String _efile) throws Exception {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(_efile));
            String line = "";
            String ret = "";

            int count = 0;

            while ((line = bf.readLine()) != null) {
                line.trim();
                if (line.startsWith("%")) continue;
                String[] items = line.split("\t");
                if (items.length != 5) continue;
                //if (!items[4].equals("Wrong monosaccharide order")) continue;

                ret = ret + items[0] + "', '";
                count++;
            }
            bf.close();

            return ret;
        } catch (IOException e){
            throw new Exception();
        }
    }

    /**
     *
     * @param a_strFile
     * @return
     * @throws Exception
     */
    public static HashMap<String, String> openString(String a_strFile) throws Exception {
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
    public static HashMap<String, String> readWURCS(BufferedReader a_bfFile) throws IOException {
        String line = "";
        HashMap<String, String> wret = new HashMap<String, String>();
        wret.clear();

        while((line = a_bfFile.readLine()) != null) {
            line.trim();

            if (line.startsWith("%")) continue;

            if (line.indexOf("#") != -1) {
                line = line.substring(0, line.indexOf("#"));
            }

            if(line.indexOf("WURCS") != -1) {
                if(line.indexOf(" ") != -1) line = line.replace(" ", "\t");
                String[] IDandWURCS = line.split("\t");
                if (IDandWURCS.length == 2) {
                    wret.put(IDandWURCS[0].trim(), IDandWURCS[1]);
                }
            }
        }
        a_bfFile.close();

        return wret;
    }
}
