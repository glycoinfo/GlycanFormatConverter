package org.glycoinfo.GlycanFormatConverter.exchange;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.JSON.GCJSONExporter;
import org.glycoinfo.GlycanFormatconverter.io.JSON.GCJSONImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Created by e15d5605 on 2017/10/18.
 */
public class WURCS2WURCSviaJSON {

    public static void main(String[] args) throws Exception{

        String string_list = "src/test/resources/sampleWURCSforConvertTest";

        if(string_list == null || string_list.equals("")) throw new Exception();

        File file = new File(string_list);

        if(file.isFile()) {
            String input = "";
            LinkedHashMap<String, String> wurcsIndex = openString(string_list);

            StringBuilder errors = new StringBuilder();

            for(String key : wurcsIndex.keySet()) {
                input = wurcsIndex.get(key);

                try {
                    WURCSFactory wfin = new WURCSFactory(input);

                    /* WURCSGraph to GlyContainer */
                    WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();
                    wg2gc.start(wfin.getGraph());

                    GlyContainer g1 = wg2gc.getGlycan();

                    ExporterEntrance ee = new ExporterEntrance(g1);

                    /* GC to JSON */
                    String json = ee.toJSON();
                    //System.out.println(json);

                    /* JSON to GC */
                    GCJSONImporter gcjImporter = new GCJSONImporter();
                    GlyContainer g2 = gcjImporter.start(json);

                    /* GC to WURCS */
                    GlyContainerToWURCSGraph gc2wg = new GlyContainerToWURCSGraph();
                    gc2wg.start(g2);

                    /* graph json */
                    String gcjson1 = ee.toJSONforVisualize();
                    System.out.println(gcjson1);

                    WURCSFactory wfoff = new WURCSFactory(gc2wg.getGraph());

                    System.out.println(input);
                    System.out.println(wfoff.getWURCS());
                    System.out.println("");

                } catch (Exception e) {
                    errors.append(key + "\t" + input + "\n");
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }

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
        int count = 1;

        while((line = a_bfFile.readLine()) != null) {
            line.trim();
            if(line.startsWith("%")) continue;
            if(line.indexOf("WURCS") != -1) {
                if(line.indexOf(" ") != -1) line = line.replace(" ", "\t");
                String[] IDandWURCS = line.split("\t");

                if (IDandWURCS.length == 2) {
                    if (IDandWURCS[0].equals("")) {
                        wret.put(String.valueOf(count), IDandWURCS[1]);
                        count++;
                    } else {
                        wret.put(IDandWURCS[0].trim(), IDandWURCS[1]);
                    }
                }
            }
        }
        a_bfFile.close();

        return wret;
    }
}
