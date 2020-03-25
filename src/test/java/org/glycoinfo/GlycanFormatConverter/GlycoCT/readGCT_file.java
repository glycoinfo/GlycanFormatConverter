package org.glycoinfo.GlycanFormatConverter.GlycoCT;

import org.glycoinfo.WURCSFramework.io.GlycoCT.GlycoCTListReader;
import org.glycoinfo.WURCSFramework.io.GlycoCT.WURCSExporterGlycoCTList;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Created by e15d5605 on 2019/02/20.
 */
public class readGCT_file {

    //@Test
    //public void GlycoCTToWURCS () throws Exception {
    public static void main (String[] args) throws Exception {

        ArrayList<String> dirs = new ArrayList<>();

        dirs.add("src/test/resources/GlycoCTSample");

        //dirs.add("/Users/e15d5605/Dataset/GlycoCT/Glycoepitope_GlycoCTList_20150224.txt");
        //dirs.add("/Users/e15d5605/Dataset/GlycoCT/glycomedb_structures-02_06_2015-02_57_convert.txt");
        //dirs.add("/Users/e15d5605/Dataset/GlycoCT/glytoucanAccGlycoCTcsvToText_20150717.txt");
        //dirs.add("/Users/e15d5605/Dataset/GlycoCT/20160616_UnicarbKB_Id-glycoct.txt");

        for (String path : dirs) {
            LinkedList<String> names = new LinkedList<>();

            // For usage
            //if ( args[i].equals("-help") ) {
            //    usage();
            //    System.exit(0);
            //}
            // For collect monosaccharides (ResidueCodes)
            //boolean t_bResidueCodeCollection = false;
            //if ( args[i].equals("-MS") )
            //    t_bResidueCodeCollection = true;

            //Check file path
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("The file not exists: " + path);
            }
            names.addLast(path);

            // Make result dir
            String resultDir = file.getAbsolutePath() + "_result";

            // Read GlycoCT list
            System.err.println("Read GlycoCT list from: " + path);
            TreeMap<String, String> gctMap = null;
            try {
                if (file.isFile())
                    gctMap = GlycoCTListReader.readFromFile(path);
                if (file.isDirectory())
                    gctMap = GlycoCTListReader.readFromDir(path);
            } catch (Exception e) {
                System.err.println("The file can not read: " + path);
                e.printStackTrace();
            }

            System.out.println(gctMap);


            // Convert
            WURCSExporterGlycoCTList t_oExport = new WURCSExporterGlycoCTList();
            t_oExport.start(gctMap);

            // Print result
            //WURCSFileWriter.printWURCSList(t_oExport.getMapIDToWURCS(), resultDir, "_moved_WURCSList.txt");
            for (String key : t_oExport.getMapIDToWURCS().keySet()) {
                System.out.println(key + " " + t_oExport.getMapIDToWURCS().get(key));
            }
        }
    }
}