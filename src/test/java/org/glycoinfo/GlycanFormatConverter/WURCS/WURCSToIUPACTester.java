package org.glycoinfo.GlycanFormatConverter.WURCS;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

//TODO : will implement exception utility

public class WURCSToIUPACTester {

	@Test
	public void WURCSToIUPACtester () throws Exception {
		
		String string_list = "/Users/e15d5605/Dataset/sampleWURCSforConvertTest";
		
		if(string_list == null || string_list.equals("")) throw new Exception();
		
		File file = new File(string_list);
		
		if(file.isFile()) {
			String input = "";
			LinkedHashMap<String, String> wurcsIndex = openString(string_list);
			
			StringBuilder result = new StringBuilder();
			
			for(String key : wurcsIndex.keySet()) {
				input = wurcsIndex.get(key);					

				try {
					/* WURCS to IUPAC */
					WURCSImporter wi = new WURCSImporter();
					GlyContainer glycan = wi.start(input);//wi.getGlyContainer();
					ExporterEntrance ee = new ExporterEntrance(glycan);
					
					result.append(key + " " + ee.toIUPAC(IUPACStyleDescriptor.GREEK) + "\n");
					
				} catch (Exception e) {
					//System.out.println(key + "	" + input);
					result.append(key + " " + "% " + e.getMessage() + "\n");
					//System.out.println(key + " " + e.getMessage());
				}
			} 
			
			/* define file name */
	        Date date = new Date();
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	        String fileName = sdf.format(date) + "_IUPACSample_from_WURCS";

	       	/* write WURCS */
	        writeFile(result.toString(), "", fileName);	
	        
		} else {
			throw new Exception("This file is not found !");
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
	private LinkedHashMap<String, String> openString(String a_strFile) throws Exception {
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
	private LinkedHashMap<String, String> readWURCS(BufferedReader a_bfFile) throws IOException {
		String line = "";
		LinkedHashMap<String, String> wret = new LinkedHashMap<String, String>();
		wret.clear();

		while((line = a_bfFile.readLine()) != null) {
			line.trim();
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
