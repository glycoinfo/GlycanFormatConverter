package org.glycoinfo.WURCSFramework.Carbbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.glycoinfo.WURCSFramework.util.exchange.Carbbank.WURCSToCarbBank;

public class WURCSToCarbBankTester {

	public static void main(String[] args) throws Exception{

		String string_list = 
				"/Users/st/git/glycanformatconverter/GlycanFormatConverter/src/test/resources/sampleWURCSforConvertTest";
				
		if(string_list == null || string_list.equals("")) throw new Exception();
		
		File file = new File(string_list);
		WURCSToCarbBank a_objW2C = new WURCSToCarbBank();
		
		if(file.isFile()) {
			LinkedHashMap<String, String> wurcsIndex = new LinkedHashMap<String, String>();	
			
			String input = "";
			try {
				wurcsIndex = openString(string_list);
				for(String key : wurcsIndex.keySet()) {
					input = wurcsIndex.get(key);					
					a_objW2C.start(wurcsIndex.get(key));
					System.out.println(key + "\t" + a_objW2C.getCarbBank());
				} 
			} catch (Exception e) {
				System.err.println(input);
				e.printStackTrace();
			}
		}else if(args.length > 0) {
			a_objW2C.start(args[0]);
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
