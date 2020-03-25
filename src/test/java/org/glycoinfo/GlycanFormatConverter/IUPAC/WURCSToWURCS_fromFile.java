package org.glycoinfo.GlycanFormatConverter.IUPAC;

import org.glycoinfo.GlycanFormatConverter.util.fileHandler;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.extended.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class WURCSToWURCS_fromFile {

	//@Test
	//public void WURCSToWURCS () throws Exception {
	public static void main (String[] args) throws Exception {
		String numbers = "";
		String inputFile =
				//"/Users/e15d5605/Dataset/sampleWURCSforConvertTest";
			"/Users/e15d5605/Dataset/WURCS";

		File file = new File(inputFile);

		if (file.isFile()) {
			HashMap<String, String> wurcsMap = fileHandler.openString(file.getAbsolutePath());

			StringBuilder result = new StringBuilder();
			StringBuilder error = new StringBuilder();
			WURCSImporter wi = new WURCSImporter();
			int count = 0;	

			for (String number : wurcsMap.keySet()) {
			//for (String number : numbers.split("', '")) {

				String wurcs = wurcsMap.get(number);

				try {
					/* WURCS to IUPAC */	
					//ExporterEntrance ee = new ExporterEntrance(wi.getGlyContainer());
					ExporterEntrance ee = new ExporterEntrance(wi.start(wurcs));
					String iupac = ee.toIUPAC(IUPACStyleDescriptor.GREEK);

					/* IUPAC to WURCS */
					IUPACExtendedImporter iei = new IUPACExtendedImporter();

					ee = new ExporterEntrance(iei.start(iupac));

					/**/
					if (!wurcs.equals(ee.toWURCS())) {
						result.append(number + "\n");
						result.append(iupac + "\n");
						result.append("O : " + wurcs + "\n");
						result.append("C : " + ee.toWURCS() + "\n");
						//result.append(wurcs.equals(ee.toWURCS()) + "\n");
						result.append("\n");
					}
				} catch (Exception e) {
					error.append(number + "	" + wurcs + "	#" + e.getMessage() + "\n");
					e.printStackTrace();
				}
				
				count++;
			}

			/* define file name */
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

			/* write WURCS */
			//fileHandler.writeFile(result.toString(), "", sdf.format(date) + "_WURCS_vs_WURCS");

			System.out.println(result);
			System.out.println(error);

		} else {
			throw new Exception("This file could not found.");
		}

	}
}
