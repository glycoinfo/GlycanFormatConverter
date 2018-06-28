package org.glycoinfo.GlycanFormatConverter.IUPAC;

import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2018/01/24.
 */
public class WURCSToIUPAC_pickup {

    @Test
    public void WURCSToIUPAC () throws Exception {

    //	String numbers = "G00115ZI";
    	
    String numbers = "G57657JA";//"G64296EN";//"G00048ZA";//"G00301XV";
    	
    	//	String numbers = "G74678YC', 'G95576XZ', 'G67495JG', 'G36327IZ', 'G94304ZN";
    	
    	//String numbers = "G12796TN', 'G49461WX', 'G62437SF', 'G73501HK', 'G74878EF', 'G81642MI', 'G38352BU";
    	
    	//<Q>
    	//String numbers = "G00301XV', 'G00472TL', 'G01819ZN', 'G01954GZ', 'G03015AX', 'G03690AY', 'G04785ZH', 'G06932JZ', 'G07174KI', 'G08553IB', 'G09689DR', 'G10534JP', 'G12851XW', 'G13709MU', 'G13807HJ', 'G15678FJ', 'G16558PB', 'G17738UB', 'G17888GZ', 'G20292CA', 'G22379AK', 'G24566JM', 'G24794OG', 'G24830SA', 'G25220NO', 'G26142PM', 'G27081WU', 'G28428NG', 'G29992EE', 'G30889OL', 'G33949FE', 'G35455XI', 'G36400XY', 'G36647EN', 'G36672DN', 'G37201NW', 'G37238PW', 'G37589TD', 'G37687OM', 'G38925PI', 'G39386NM', 'G40236EN', 'G43203OV', 'G43962PZ', 'G44387PI', 'G46216PH', 'G47076CS', 'G47257BG', 'G47734IW', 'G49291NZ', 'G52226HR', 'G52912WO', 'G53621MK', 'G58756VI', 'G61051JB', 'G61365VY', 'G61390LD', 'G63423BO', 'G64964HW', 'G65131ME', 'G65448KP', 'G65726MC', 'G66745OK', 'G66858QT', 'G66957SK', 'G67009FG', 'G68714YC', 'G70823BY', 'G72456QT', 'G72851VK', 'G73245UM', 'G73785CC', 'G77496LB', 'G78905YB', 'G82955BL', 'G84510DI', 'G85692VJ', 'G86619JX', 'G86638DP', 'G88696LA', 'G91037SI', 'G91192KL', 'G91369NN', 'G92435YW', 'G94434RV', 'G96428KL', 'G96952QM', 'G98115NV', 'G99628DW', 'G99990RL";
    	File file = new File("/Users/e15d5605/Dataset/sampleWURCSforConvertTest");

    	if (file.isFile()) {
    		HashMap<String, String> wurcsMap = openString(file.getAbsolutePath());

    		StringBuilder result = new StringBuilder();
    		WURCSImporter wi = new WURCSImporter();
    		
    		for (String number : numbers.split("', '")) {
    			String wurcs = wurcsMap.get(number);
    			
    			System.out.println(wurcs);
    			
    			try {
    				/* WURCS to IUPAC */	
    				wi.start(wurcs);

    				ExporterEntrance ee = new ExporterEntrance(wi.getGlyContainer());
    				String iupac = ee.toIUPAC(IUPACStyleDescriptor.GREEK);
                    result.append(number + " " + iupac + "\n");
                   
                System.out.println(ee.toIUPAC(IUPACStyleDescriptor.EXTENDED));
                System.out.println(ee.toIUPAC(IUPACStyleDescriptor.CONDENSED));
                System.out.println(ee.toIUPAC(IUPACStyleDescriptor.SHORT));
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
