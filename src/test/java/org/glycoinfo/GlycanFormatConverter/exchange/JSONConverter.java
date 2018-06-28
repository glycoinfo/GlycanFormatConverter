package org.glycoinfo.GlycanFormatConverter.exchange;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.junit.Test;

public class JSONConverter {

	@Test
	public void WURCSToJSON () throws Exception { 
		
		File file = new File("/Users/e15d5605/Dataset/sampleWURCSforConvertTest");

		if (file.isFile()) {
			HashMap<String, String> wurcsMap = openString(file.getAbsolutePath());

			StringBuilder results = new StringBuilder();
			StringBuilder error = new StringBuilder();
			
			for (String number : wurcsMap.keySet()) {

				String input = wurcsMap.get(number);

				try {
					WURCSFactory wfin = new WURCSFactory(input);

					/* WURCSGraph to GlyContainer */
					WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();
					wg2gc.start(wfin.getGraph());

					GlyContainer g1 = wg2gc.getGlycan();

					ExporterEntrance ee = new ExporterEntrance(g1);

					/* GC to JSON */
					//String json = ee.toJSON();
					//System.out.println(json);

					/* graph json */
					String gcjson1 = ee.toJSONforVisualize();
					System.out.println(gcjson1);

					results.append(gcjson1 + "\n");

					//	                    System.out.println(input);
					//	                    System.out.println("");

				} catch (Exception e) {
					error.append(number + "\t" + input + "\n");
					//System.out.println(e.getMessage());
					//e.printStackTrace();
				}
			}
	
			 /* define file name */
			 Date date = new Date();
			 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			 String fileName = sdf.format(date) + "_JSON";

			 /* write WURCS */
			 writeFile(results.toString(), "", fileName);	
			
		} else {
			throw new Exception("This file could not found.");
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
    private HashMap<String, String> openString(String a_strFile) throws Exception {
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
    private HashMap<String, String> readWURCS(BufferedReader a_bfFile) throws IOException {
        String line = "";
        HashMap<String, String> wret = new HashMap<String, String>();
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
	
