package org.glycoinfo.GlycanFormatConverter.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by e15d5605 on 2017/06/21.
 */
public class IUPACImporterFromFile {

	@Test
	public void IUPACImporterTester () throws Exception {
		
		//String string_list = "/Users/e15d5605/Dataset/test";
		//String string_list = "/Users/e15d5605/Dataset/20180202_IUPACSample_from_WURCS";
        //String string_list = "src/test/resources/20180917_IUPACSample_from_WURCS";
        String string_list = "src/test/resources/sampleIUPAC";

		 if(string_list == null || string_list.equals("")) throw new Exception();
		 
		 File file = new File(string_list);
		 
		 if(file.isFile()) {
			 String input = "";
			 LinkedHashMap<String, String> iupacIndex = openString(string_list);

			 StringBuilder results = new StringBuilder();
             StringBuilder error = new StringBuilder();
			 for(String key : iupacIndex.keySet()) {
			  	 input = iupacIndex.get(key);

				 try {
					 /* Import IUPAC */
					 IUPACExtendedImporter ii = new IUPACExtendedImporter();
					 GlyContainer glyCo = ii.start(input);
					 ExporterEntrance ee = new ExporterEntrance(glyCo);

					 /* to WURCS */
					 results.append(ee.toWURCS() + "\n");
				 } catch (Exception e) {
					 results.append("\n");
				     //results.append(key + "\t" + "% " + e.getMessage() + "\n");
					 error.append(key + "\t" + input + "\t" + e.getMessage() + "\n");
					 e.printStackTrace();
				 }
			 }

			 /* define file name */
			 Date date = new Date();
			 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			 String fileName = sdf.format(date) + "_WURCSSample_from_IUPAC";

			 /* write WURCS */
			 writeFile(results.toString(), "", fileName);

			 System.out.println(error);
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
