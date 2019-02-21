package org.glycoinfo.GlycanFormatConverter.exchange;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph.GlyContainerToWURCSGraph;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSSequence2ToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;

public class GCToWURCSGraphFromFile {


	public static void main(String[] args) throws Exception{

		String string_list = "src/test/resources/sampleWURCSforConvertTest";
		
		if(string_list == null || string_list.equals("")) throw new Exception();
		
		File file = new File(string_list);
	
		if(file.isFile()) {
			String input = "";
			LinkedHashMap<String, String> wurcsIndex = openString(string_list);
			
			StringBuilder results = new StringBuilder();
			StringBuilder errors = new StringBuilder();
			
			for(String key : wurcsIndex.keySet()) {
				input = wurcsIndex.get(key);					

				try {
					WURCSFactory wfin = new WURCSFactory(input);
					
					/** WURCSSequence2 to GlyContainer */
					WURCSSequence2ToGlyContainer w2gc = new WURCSSequence2ToGlyContainer();
					w2gc.start(wfin.getSequence());
					
					GlyContainer glycan = w2gc.getGlycan();
					
					/** Convert to IUPAC */
					IUPACExporter ie = new IUPACExporter();
					ie.start(glycan);
										
					/** Import IUPAC */
					IUPACExtendedImporter ii = new IUPACExtendedImporter();
					
					/** GlyContainerToWURCSGraph */
					GlyContainerToWURCSGraph gc2wg = new GlyContainerToWURCSGraph();
					gc2wg.start(ii.start(ie.getExtendedWithGreek()));

					/** Convert WURCS */
					WURCSFactory wfout = new WURCSFactory(gc2wg.getGraph());

					if (!input.equals(wfout.getWURCS())) {
						results.append(input + "\n");
						results.append(wfout.getWURCS() + "\n");
						results.append(ie.getExtendedWithGreek() + "\n\n");
					}
				} catch (Exception e) {
					errors.append(key + "\t" + input + "\n");
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			} 
			
			System.out.println(results);
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
