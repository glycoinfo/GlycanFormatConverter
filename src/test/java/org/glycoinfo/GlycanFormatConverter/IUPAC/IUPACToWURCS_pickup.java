package org.glycoinfo.GlycanFormatConverter.IUPAC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

public class IUPACToWURCS_pickup {

	@Test
	public void IUPACToWURCS () throws Exception {
		
		//String numbers = "G09335SZ', 'G12760VJ', 'G34208PJ', 'G34497XL', 'G39177BZ', 'G41586PD', 'G65132JN', 'G67867AY', 'G69666WN', 'G75940FW', 'G83325QY', 'G84356OA', 'G85696MX', 'G87946UV', 'G96377XW', 'G97803IP', 'G98329NH";
		String numbers = "G14654BW', 'G62897PE";
		
		File file = new File("/Users/e15d5605/Dataset/20180202_IUPACSample_from_WURCS");
		
		if (file.isFile()) {
			HashMap<String, String> iupacMap = openString(file.getAbsolutePath());
			
			StringBuilder results = new StringBuilder();
			IUPACExtendedImporter iei = new IUPACExtendedImporter();
			
			for (String number : numbers.split("', '")) {
				String iupac = iupacMap.get(number);
				
				System.out.println(iupac);
				
				try {
					ExporterEntrance ee = new ExporterEntrance(iei.start(iupac));
					String wurcs = ee.toWURCS();
					results.append(number + " " + wurcs + "\n");
				} catch (Exception e) {
					results.append(number + " " + "% " + e.getMessage() + "\n");
					e.printStackTrace();
				}
			}
			
			System.out.println(results);
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
            if(line.startsWith("%")) continue;
            if(line.indexOf(" ") != -1) line = line.replace(" ", "\t");
            String[] IDandWURCS = line.split("\t");
            if (IDandWURCS.length == 2) {
                wret.put(IDandWURCS[0].trim(), IDandWURCS[1]);
            }
        }
        a_bfFile.close();

        return wret;
    }
}