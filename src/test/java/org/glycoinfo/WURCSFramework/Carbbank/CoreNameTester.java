package org.glycoinfo.WURCSFramework.Carbbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.util.exchange.GRESToTrivialName;

public class CoreNameTester {
	public static void main(String[] args) throws Exception {
		String str_file = "src/test/resources/MSlist";
		GRESToTrivialName a_oGRESToTrivialName = new GRESToTrivialName();
		
		File file = new File(str_file);		
		if(file.isFile()) {
			LinkedList<String> a_aCoreNames = new LinkedList<String>();
			
			for(String s : openString(str_file)) {
				try {
					a_oGRESToTrivialName.start(s);
					
					/** carbbank */
					String a_strCarbBank = a_oGRESToTrivialName.getCarbBankNotation();
					/** IUPAC extended notation*/
					String a_strIUPAC = a_oGRESToTrivialName.getIUPACExtendedNotation();
					/** IUPAC condensed notation */
					String a_sIUPACCondensed = a_oGRESToTrivialName.getIUPACCondensedNotation();
					/** core name*/
					String a_strCore = a_oGRESToTrivialName.getTrivialName();
					
					a_aCoreNames.add(s + "\t" + a_strIUPAC + "\t" + a_sIUPACCondensed + "\t" + a_strCarbBank + "\t" + a_strCore);
				} catch (Exception e) {
					a_aCoreNames.add(s);
					//System.err.println(s);
					e.printStackTrace();
				}
			}				
			
			for(String s : a_aCoreNames) System.out.println(s);
		}else if(args.length > 0) {
			a_oGRESToTrivialName.start(args[0]);
			System.out.println(a_oGRESToTrivialName.getTrivialName());
		}else {
			a_oGRESToTrivialName.start(args[0]);
			System.out.println(a_oGRESToTrivialName.getTrivialName());
		}
	}
	
	public static LinkedList<String> openString(String a_strFile) throws Exception {
		try {
			return readMS(new BufferedReader(new FileReader(a_strFile)));
		}catch (IOException e) {
			throw new Exception();
		}
	}
	
	public static LinkedList<String> readMS(BufferedReader a_bfFile) throws IOException {
		String line = "";
		LinkedList<String> ret = new LinkedList<String>();
		
		while((line = a_bfFile.readLine()) != null) {
			line.trim();
			ret.addLast(line);
		}
		a_bfFile.close();

		return ret;
	}
}
