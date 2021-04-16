package org.glycoinfo.GlycanFormatConverter.util;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by e15d5605 on 2019/04/05.
 */
public class fileHandler {

    public static String openTSV (String _path) {
        String fileStrings = "";
        try {
            File file = new File(_path);

            //for (File item : file.listFiles()) {
                if (file.exists()) {
                    Path path = file.toPath();
                    String fileName = path.getFileName().toString();
                    String ext = "";
                    if (fileName.length() > 3) {
                        ext = fileName.substring(fileName.length() - 4);
                    }

                    if (ext.equals(".tsv")) {
                        String filePath = file.toPath().toString();
                        DataFileReader reader = new DataFileReader(filePath);
                        fileStrings = reader.getFileStrings();
                    }
                } else {
                    throw new Exception(file.toPath().toString() + " is not exists.");
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileStrings;
    }

    public static void writeFile (String _filePath, HashMap<String, ArrayList<String>> _resultMap, String _format) {
        try {
            Date date = new Date();
            SimpleDateFormat simpleDate = new SimpleDateFormat("-yyyy-MM-dd");
            String outputStr = simpleDate.format(date);

            FileWriter writeFile = new FileWriter(_filePath + outputStr + _format, true);
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(writeFile));

            for (String key : _resultMap.keySet()) {
                ArrayList<String> results = _resultMap.get(key);
                String content = "";
                for (Iterator<String> iter = results.iterator(); iter.hasNext();) {
                    content += iter.next();
                    if (iter.hasNext()) content += "\t";
                }
                printWriter.print(key + "\t" + content + "\n");
            }

            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        HashMap<String, String> wret = new HashMap<>();
        wret.clear();

        while((line = a_bfFile.readLine()) != null) {
            line.trim();

            if (line.startsWith("%")) continue;

            if (line.contains("#")) {
                line = line.substring(0, line.indexOf("#"));
            }

            if(line.contains("WURCS")) {
                if(line.contains(" ")) line = line.replace(" ", "\t");
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
