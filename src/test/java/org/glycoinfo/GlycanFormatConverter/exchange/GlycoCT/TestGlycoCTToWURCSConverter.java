package org.glycoinfo.GlycanFormatConverter.exchange.GlycoCT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TestGlycoCTToWURCSConverter {

	public static void main(String[] args) {

		WURCSToGlycoCTConverterFromCSV t_converter = new WURCSToGlycoCTConverterFromCSV();

/*		String t_strTargetIDsFile = "./mismatched_IDs_100818.txt";
		try {
			for ( String t_strID : parseIDs(t_strTargetIDsFile) )
				t_converter.addTargetID(t_strID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
		String t_strCSVFile = "./GlyTouCan_sequences_all.csv";
		t_converter.processCSVFile(t_strCSVFile);
	}

	private static List<String> parseIDs(String filename) throws IOException {
		File f = new File(filename);
		InputStreamReader osr = new InputStreamReader(new FileInputStream(f));
		BufferedReader br = new BufferedReader(osr);

		List<String> t_lIDs = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null) {
			String[] splitted = line.split(", ");
			for ( String t_strID : splitted )
				t_lIDs.add(t_strID.trim());
		}
		br.close();
		return t_lIDs;
	}
}
