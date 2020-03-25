package org.glycoinfo.GlycanFormatConverter.LinearCode;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by e15d5605 on 2017/08/28.
 */
public class LinearCodeReader {

	@Test
	public void readLinearCode () throws Exception {
		//String directory = "src/test/resources/Atlas_LC_sample1";

		//if (directory == null || directory.equals("")) throw new Exception("File could not found!");

		String dir =
				"/Users/e15d5605/Dataset/LCToWURCS/20181210_Atlas_LC_samples";

		File file = new File(dir);

		if (file.isFile()) {
			LinkedHashMap<String, String> lncIndex = openString(file.getAbsolutePath());
			StringBuilder results = new StringBuilder();
			StringBuilder error = new StringBuilder();

			for (String key : lncIndex.keySet()) {
				try {
					LinearCodeImporter lci = new LinearCodeImporter();
					GlyContainer glyco = lci.start(lncIndex.get(key));

					ExporterEntrance ee = new ExporterEntrance(glyco);

					/* for IUPAC */
					//results.append(key + "\t" + ee.toIUPAC(IUPACStyleDescriptor.GREEK) + "\n");

					/* for WURCS */
					System.out.println(key + " " + ee.toWURCS());
					results.append(ee.toWURCS() + "\n");
				} catch (Exception e) {
					error.append(key + "\t" + lncIndex.get(key) + "\n");
					e.printStackTrace();
				}
			}

			/* define file name */
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String fileName = sdf.format(date) + "_WURCSSample_from_LC_group2";

			/* write WURCS */
			writeFile(results.toString(), "", fileName);	

			String errorFile = sdf.format(date) + "_error_LC_sample";
			writeFile(error.toString(), "", errorFile);

//			System.out.println(results);
//			System.out.println(error);
		}
	}

	public void writeFile (String _result, String _error, String _fileName) throws IOException {
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
		int count = 0;

		while ((line = _bf.readLine()) != null) {
			line = line.trim();
			String key;

			System.out.println(count + " " + line);

			//if (line.equals("")) continue;
			//if (line.startsWith("%")) continue;

			//if (line.indexOf("\t") != -1) {
			//	String[] items = line.split("\t");
			//	key = items[0];
			//	line = items[1];
			//}

			//if (key.equals("null") || line.equals("unknown")) {
			//	continue;
			//}
			//if (key.equals("")) {
				key = String.valueOf(count);
				count++;
			//}

			ret.put(key, line);
		}

		_bf.close();

		return ret;
	}
}
