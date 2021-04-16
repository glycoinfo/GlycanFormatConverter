package org.glycoinfo.GlycanFormatConverter.exchange;

import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeExporter;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Created by e15d5605 on 2017/10/06.
 */
public class WURCSToLinearCode {

    public static void main(String[] args) throws Exception{

        String string_list = "src/test/resources/sampleWURCSforConvertTest";

        if(string_list == null || string_list.equals("")) throw new Exception();

        File file = new File(string_list);
        LinearCodeExporter lcExpo = new LinearCodeExporter();

        if(file.isFile()) {
            String input = "";
            LinkedHashMap<String, String> wurcsIndex = openString(string_list);
            for(String key : wurcsIndex.keySet()) {
                input = wurcsIndex.get(key);

                try {
                    WURCSFactory factory = new WURCSFactory(input);
                    WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();

                    wg2gc.start(factory.getGraph());

                    System.out.println(input);
                    lcExpo.start(wg2gc.getGlycan());
                    System.out.println(lcExpo.getLinearCode());
                } catch (Exception e) {
                    e.printStackTrace();
                    //System.out.println(key + "	" + input);
                    //System.out.println(e.getMessage());
                    if(e.getMessage() == null) {
                        System.out.println(key + "	" + input);
                        e.printStackTrace();
                        continue;
                    }
                    /** code error*/
                    if(e.getMessage().contains("This structure can not handled")) {
                        System.out.println(key + "	" + input);
                    }

                    if(e.getMessage().contains("This repeating structure is composed with")) {
                        continue;
                    }
                    if(e.getMessage().contains("This linkage is composed with")) {
                        continue;
                    }
                    if(e.getMessage().contains("Ambiguous")) {
                        continue;
                    }
                    if(e.getMessage().contains("is not handled charactor")) {
                        continue;
                    }
                }
            }
        }
        else if(args.length > 0) {
            WURCSFactory factory = new WURCSFactory(args[0]);
            WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();

            wg2gc.start(factory.getGraph());

            lcExpo.start(wg2gc.getGlycan());
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
            if (line.startsWith("%")) continue;
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
