package org.glycoinfo.GlycanFormatConverter.IUPAC;

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

import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

public class WURCSToWURCS_pickup {

	@Test
	public void WURCSToWURCS () throws Exception {
		String numbers = "";
		//"G21162RS";
		//"G06488MS', 'G80460DD', 'G98391BL', 'G43387DW', 'G84207CL";
		//"G09400JY', 'G69216DQ', 'G96571NU";
		//"G05576BD";
		//"G95576XZ', 'G67495JG', 'G36327IZ";
		//"G74678YC', 'G95576XZ', 'G67495JG', 'G36327IZ', 'G94304ZN";

		File file = new File("/Users/e15d5605/Dataset/sampleWURCSforConvertTest");

		File efile = new File("src/test/resources/Unmatched");
		if (efile.isFile()) {
			numbers = openErrorFile(efile.getAbsolutePath());
		}

		if (file.isFile()) {
			HashMap<String, String> wurcsMap = openString(file.getAbsolutePath());

			StringBuilder result = new StringBuilder();
			WURCSImporter wi = new WURCSImporter();
			int count = 0;	
			
			for (String number : numbers.split("', '")) {
				
				System.out.println(count + " : " + number);
				
				String wurcs = wurcsMap.get(number);

				try {
					/* WURCS to IUPAC */	
					wi.start(wurcs);

					ExporterEntrance ee = new ExporterEntrance(wi.getGlyContainer());
					String iupac = ee.toIUPAC(IUPACStyleDescriptor.GREEK);

					/* IUPAC to WURCS */
					IUPACExtendedImporter iei = new IUPACExtendedImporter();

					ee = new ExporterEntrance(iei.start(iupac));

					/**/
					result.append(number + "\n");
					result.append(iupac + "\n");
					result.append("O : " + wurcs + "\n");
					result.append("C : " + ee.toWURCS() + "\n");
					result.append(wurcs.equals(ee.toWURCS()) + "\n");
					result.append("\n");
				} catch (Exception e) {
					result.append(number + " " + "% " + e.getMessage() + "\n");
					result.append("\n");
					//e.printStackTrace();
				}
				
				count++;
			}

			/* define file name */
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String fileName = sdf.format(date) + "_WURCS_vs_WURCS";

			/* write WURCS */
			writeFile(result.toString(), "", fileName);
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

	private String openErrorFile (String _efile) throws Exception {
		try {
			BufferedReader bf = new BufferedReader(new FileReader(_efile));
			String line = "";
			String ret = "";	

			int count = 0;
			
			while ((line = bf.readLine()) != null) {
				line.trim();
				if (line.startsWith("%")) continue;
				String[] items = line.split("\t");
				if (items.length != 5) continue;
				//if (!items[4].equals("Wrong monosaccharide order")) continue;
				
				ret = ret + items[0] + "', '";
				count++;
			}			
			bf.close();

			return ret;
		} catch (IOException e){
			throw new Exception();
		}
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
