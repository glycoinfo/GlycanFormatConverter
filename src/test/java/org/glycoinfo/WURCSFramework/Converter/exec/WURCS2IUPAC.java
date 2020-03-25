package org.glycoinfo.WURCSFramework.Converter.exec;

import org.glycoinfo.WURCSFramework.util.oldUtil.IUPAC.WURCSToIUPAC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

public class WURCS2IUPAC {

	public static void main(String[] args) throws Exception{

		String t_strVersion = "WURCS to IUPAC version 23 December 2016"; 
		String string_list = "src/test/resources/sampleWURCSforConvertTest";
		string_list = "src";
		
		if(string_list == null || string_list.equals("")) throw new Exception();
		
		File file = new File(string_list);
		WURCSToIUPAC a_objW2I = new WURCSToIUPAC();
		
		if(file.isFile()) {
			
			if (file.exists()){
			
				TreeMap<String, String> wurcsIndex = new TreeMap<String, String>();	
				
				String input = "";
				try {
					wurcsIndex = openString(string_list);
					for(String key : wurcsIndex.keySet()) {
						input = wurcsIndex.get(key);
						
						System.out.println(wurcsIndex.get(key));
						a_objW2I.start(wurcsIndex.get(key));
						System.out.println("Condensed\t" + a_objW2I.getCondensedIUPAC());
						System.out.println("Short\t" + a_objW2I.getShortIUPAC());
						System.out.println("Extend\t" + a_objW2I.getExtendedIUPAC());
					} 
				} catch (Exception e) {
					System.err.println(input);
					e.printStackTrace();
				}
			}
			else {
				System.out.println("file not found");
			}
			
			
			
		}else if(!file.isFile()) {						
			try{				
				if (args.length == 1 ) {
					try {
						if (args[0].equals("-h") || args[0].equals("-H")){
							System.out.println(t_strVersion);
							System.out.println("based on Glycan Format Converter");
							return;
						}
						else {
							System.out.println(args[0]);
							a_objW2I.start(args[0]);
							System.out.println("Condensed\t" + a_objW2I.getCondensedIUPAC());
							System.out.println("Short\t" + a_objW2I.getShortIUPAC());
							System.out.println("Extend\t" + a_objW2I.getExtendedIUPAC());
						}
					} catch (Exception e) {
						System.err.println("error");
						e.printStackTrace();
					}				
				}
				else if (args.length > 1 ) {
					try {
						for(int i=0; i< args.length; i++) {				
							if (i % 2 != 0){
								System.out.println(args[0]);
								a_objW2I.start(args[1]);
								System.out.println("Condensed\t" + a_objW2I.getCondensedIUPAC());
								System.out.println("Short\t" + a_objW2I.getShortIUPAC());
								System.out.println("Extend\t" + a_objW2I.getExtendedIUPAC());
							}
						}
					} catch (Exception e) {
						System.err.println("error");
						e.printStackTrace();
					}
					
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
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
	public static TreeMap<String, String> openString(String a_strFile) throws Exception {
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
	public static TreeMap<String, String> readWURCS(BufferedReader a_bfFile) throws IOException {
		String line = "";
		TreeMap<String, String> wret = new TreeMap<String, String>();
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