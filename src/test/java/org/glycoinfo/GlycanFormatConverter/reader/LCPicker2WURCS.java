package org.glycoinfo.GlycanFormatConverter.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

public class LCPicker2WURCS {

	@Test
	public void LC2WURCS () throws Exception {

		String numbers = "G93934MI";

		File file = new File("/Users/e15d5605/Dataset/Atlas_LC_sample1");

		if (file.isFile()) {
			HashMap<String, String> wurcsMap = openString(file.getAbsolutePath());

			StringBuilder result = new StringBuilder();
			LinearCodeImporter lci = new LinearCodeImporter();

			for (String number : numbers.split("', '")) {
				String lc = wurcsMap.get(number);

				System.out.println(lc);

				try {
					/* LC to WURCS */	
					ExporterEntrance ee = new ExporterEntrance(lci.start(lc));
					String iupac = ee.toWURCS();
					result.append(number + " " + iupac + "\n");
				} catch (Exception e) {
					result.append(number + " " + "% " + e.getMessage() + "\n");
					e.printStackTrace();
				}
			}

			System.out.println(result);

		} else {
			throw new Exception("This file could not found.");
		}

	}
	
	public LinkedHashMap<String, String> openString(String _directory) throws Exception {
		try {
			return readKCF(new BufferedReader(new FileReader(_directory)));
		} catch (IOException e) {
			throw new Exception();
		}
	}

	public LinkedHashMap<String, String> readKCF(BufferedReader _bf) throws IOException {
		String line = "";
		LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();

		while ((line = _bf.readLine()) != null) {
			line = line.trim();
			String key = "";

			if (line.equals("")) continue;
			if (line.startsWith("%")) continue;

			if (line.indexOf("\t") != -1) {
				String[] items = line.split("\t");
				key = items[0];
				line = items[1];
			}

			if (key.equals("null") || line.equals("unknown")) {
				//System.out.println(key + " " + line);
				continue;
			}

			ret.put(key, line);
		}

		_bf.close();

		return ret;
	}
}
