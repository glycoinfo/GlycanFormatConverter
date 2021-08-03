package org.glycoinfo.GlycanFormatConverter.util;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

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

            FileWriter writeFile = new FileWriter(_filePath + outputStr + _format, false);
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
     * @param _fileName
     * @param _format
     * @return
     * @throws Exception
     */
    public static Object openString(String _fileName, String _format) throws Exception {
        try {
            File file = new File(_fileName);
            if (!file.isFile()) {
                throw new Exception(file.getAbsolutePath() + " is not found.");
            }

            if (_format.equals("WURCS")) {
                return readWURCS(new BufferedReader(new FileReader(_fileName)));
            }
            if (_format.equals("KCF")) {
                return readKCF(new BufferedReader(new FileReader(_fileName)));
            }

            throw new Exception(_format + " can not support.");
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    /**
     *
     * @param _bf
     * @return
     * @throws IOException
     */
    public static HashMap<String, String> readWURCS(BufferedReader _bf) throws IOException {
        String line = "";
        HashMap<String, String> wret = new HashMap<>();

        while((line = _bf.readLine()) != null) {
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
        _bf.close();

        return wret;
    }

    public static HashMap<String, String> readKCF (BufferedReader _bf) throws IOException {
        String line;
        HashMap<String, String> ret = new HashMap<>();

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

            kcfUnit.append(line)
                    .append("\n");

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

    public static HashMap<String, ArrayList<String>> openPubChemResult (String _directory) throws IOException {
        String line;
        HashMap<String, ArrayList<String>> ret = new HashMap<>();

        BufferedReader bf = new BufferedReader(new FileReader(_directory));

        while ((line = bf.readLine()) != null) {
            line.trim();
            if (line.startsWith("Reading")) continue;
            String id = Arrays.asList(line.split(" ")).get(0);

            ArrayList<String> wurcs = new ArrayList();
            wurcs.add(Arrays.asList(line.split(" ")).get(6).replaceAll("\"", ""));

            ret.put(id, wurcs);
        }

        bf.close();

        return ret;
    }
}
