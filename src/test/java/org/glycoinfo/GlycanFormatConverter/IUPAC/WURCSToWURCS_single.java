package org.glycoinfo.GlycanFormatConverter.IUPAC;

import java.util.ArrayList;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.junit.Test;

public class WURCSToWURCS_single {

	//@Test
	//public void WURCSToWURCS () throws Exception {
	public static void main (String[] args) throws Exception {

		ArrayList<String> sets = new ArrayList<>();

		sets.add("");
//		sets.add("WURCS=2.0/1,1,0/[a2eE1h-1a_1-4]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2EE1h-1a_1-4]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2fF1h-1a_1-4]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2zZ1h-1a_1-4]/1/");

//		sets.add("WURCS=2.0/1,1,0/[a2122h-1x_1-5_2*CC/2=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2122h-1x_1-5_2*OCC/3=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2122h-1x_1-5_2*NCC/3=O]/1/");

//		sets.add("WURCS=2.0/1,1,0/[a22h-1a_1-4_3*CO]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a26h-1a_1-4_3*CO]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a26h-1a_1-4_3*CO_3*CO]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a25h-1a_1-4_3*F]/1/");

		//sets.add("WURCS=2.0/3,3,2/[a2122h-1a_1-5][ha122h-2b_2-5][u211h]/1-2-3/a1-b2_b4-c2*OPO*/3O/3=O");

//		sets.add("WURCS=2.0/1,1,0/[a1221m-1a_1-5_2*NCC/3=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2122m-1b_1-5_4*NCC/3=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2122h-1x_1-5_2*NCCO/3=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2122h-1x_1-5_2*NSO/3=O/3=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[AUd21122h_5*NCC/3=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[AUd21122h_5*NCCO/3=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[AUd21122h_5*NSO/3=O/3=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2122h-1x_1-5_2*NCC/3=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2122A-1x_1-5_2*NCC/3=O]/1/");
//		sets.add("WURCS=2.0/1,1,0/[a2122m-1x_1-5_2*NCC/3=O]/1/");

		sets.add("WURCS=2.0/5,5,5/[a2122h-1a_1-5_2*NCC/3=O][a1221m-1a_1-5_2*NCC/3=O][a2112h-1a_1-5][a2122h-1a_1-5][a2122m-1b_1-5_4*NCC/3=O]/1-2-3-4-5/a3-b1_b3-c1_c6-d1*OPO*/3O/3=O_d6-e1_a1-e3~n");
//		sets.add("WURCS=2.0/5,12,11/[hxh][a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5]/1-2-3-4-5-5-5-5-5-5-5-5/a3n2-b1n1*1NCCOP^XO*2/6O/6=O_b4-c1_c4-d1_d3-e1_d6-h1_e2-f1_f2-g1_h3-i1_h6-k1_i2-j1_k2-l1");
//   	sets.add("WURCS=2.0/2,2,1/[a222h-1x_1-4_1*N][a211h-1b_1-4]/1-2/a5-b1*OP^XOP^XO*/5O/5=O/3O/3=O");

		StringBuilder result = new StringBuilder();

		for (String input : sets) {
			try {
				result.append(input + "\n");

				// WURCS to IUPAC
				WURCSImporter wi = new WURCSImporter();
				wi.start(input);
				ExporterEntrance ee = new ExporterEntrance(wi.getGlyContainer());

				result.append(ee.toIUPAC(IUPACStyleDescriptor.GREEK) + "\n");

				// IUPAC to WURCS

				String iupac = ee.toIUPAC(IUPACStyleDescriptor.GREEK);
				IUPACExtendedImporter iee = new IUPACExtendedImporter();
				ExporterEntrance ee2 = new ExporterEntrance(iee.start(iupac));
				String wurcs = ee2.toWURCS();

				result.append(wurcs + "\n");
				result.append(input.equals(wurcs) + "\n");
				result.append("\n");
			} catch (Exception e) {
				e.printStackTrace();
				result.append("\n");
			}
		}
		
		System.out.println(result);
	}
}
