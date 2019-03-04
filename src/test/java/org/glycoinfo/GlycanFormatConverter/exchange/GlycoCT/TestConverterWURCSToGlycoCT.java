package org.glycoinfo.GlycanFormatConverter.exchange.GlycoCT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlycoCT.GlyContainerToSugar;
import org.glycoinfo.GlycanFormatconverter.io.GlycoCT.WURCSExporterGlycoCT;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

public class TestConverterWURCSToGlycoCT {

	public static void main(String[] args) {

		List<String> t_lWURCSs = loadWURCSs();
		if ( t_lWURCSs.isEmpty() )
//			t_lWURCSs = readTXT("./wurcs.txt");
//			t_lWURCSs = readTXT("./wurcs_und.txt");
			t_lWURCSs = readTXT("./wurcs_rep.txt");
		if ( !t_lWURCSs.isEmpty() )
			convertWURCSs(t_lWURCSs);
	}

	private static List<String> loadWURCSs() {
		List<String> lWURCS = new ArrayList<>();
//		lWURCS.add("WURCS=2.0/3,12,16/[a212h-1b_1-5][a2112h-1b_1-5][a211h-1a_1-4]/1-2-2-2-2-2-3-3-2-2-2-2/a4-b1_b3-c1_b6-l1_c3-d1_c6-i1_d3-e1_d6-f1_f6-g1_g3-h1_i3-j1_j3-k1_a1-a4~n_a1-e3~n_b1-b3~n_c1-c3~n_d1-d3~n");
//		lWURCS.add("WURCS=2.0/5,6,6/[a2211m-1a_1-5_3n1%.5%-4n2%.5%*1OC^X*2/3CO/5=O/3C][a2112h-1b_1-5][a2122h-1a_1-5][a2211m-1a_1-5][a2122A-1b_1-5]/1-2-3-4-4-5/a2-b1_b3-c1_c2-d1_d2-e1_e4-f1_a1-f4~n");
//		lWURCS.add("WURCS=2.0/5,5,5/[o222h_5*OPO/3O/3=O][a2112h-1b_1-5_2*NCC/3=O][a2112h-1a_1-5_2*NCC/3=O][a2112m-1a_1-5_2*NCC/3=O_4*N][a2112h-1b_1-5]/1-2-3-4-5/a1-b1_b3-c1_c4-d1_d3-e1_a?-e6~n");
//		lWURCS.add("WURCS=2.0/7,14,13/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1b_1-5][a1221m-1a_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-2-5-2-6-5-4-2-5-6-7/a4-b1_a6-m1_b4-c1_d2-e1_d4-g1_e4-f1_g3-h1_g4-i1_j2-k1_k4-l1_d1-c3|c6_j1-c3|c6_n1-f6|i6|l6}");
//		lWURCS.add("WURCS=2.0/6,17,16/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-1-5-1-5-4-1-5-1-5-6-6-6-6/a4-b1_b4-c1_e4-f1_g4-h1_j4-k1_l4-m1_c?-d1_c?-i1_d?-e1_d?-g1_i?-j1_l1-a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?}_n2-a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?}_o2-a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?}_p2-a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?}_q2-a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?}");
//		lWURCS.add("WURCS=2.0/6,20,19/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1x_1-5][a1221m-1x_1-5]/1-2-3-4-1-5-1-5-1-4-1-5-1-5-6-1-5-1-5-6/a4-b1_a6-o1_b4-c1_c3-d1_c4-i1_c6-j1_d?-e1_d?-g1_e?-f1_g?-h1_j?-k1_j?-m1_k?-l1_m?-n1_p?-q1_r?-s1_p1-a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}_r1-a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}_t1-a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?}");
//		lWURCS.add("WURCS=2.0/2,2,2/[h2h][a2122h-1b_1-5]/1-2/b1-a2%.6%_a1-a3*OPO*/3O/3=O~n");
//		lWURCS.add("WURCS=2.0/7,15,14/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a1221m-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCCO/3=O]/1-2-3-4-2-5-6-7-7-4-2-2-5-6-7/a4-b1_b4-c1_c3-d1_c6-j1_d2-e1_e3-f1_e4-g1_h8-i2_j2-k1_l3-m1_l4-n1_h2-g3|g6_o2-n3|n6_l1-d4|d6|j4|j6}");
//		lWURCS.add("WURCS=2.0/5,6,6/[a2112h-1b_1-4][a2122h-1b_1-5][a2112h-1b_1-4_?%.4%*OCC/3=O][a2112h-1a_1-5][a2112h-1b_1-5]/1-2-3-4-4-5/a5-b1_b3-c1_c3-d1_d2-e1_d3-f1_a1-f3~n");
//		lWURCS.add("WURCS=2.0/5,6,6/[a2211m-1a_1-5_3n1%.5%-4n2%.5%*1OC^X*2/3CO/5=O/3C][a2112h-1b_1-5][a2122h-1a_1-5][a2211m-1a_1-5][a2122A-1b_1-5]/1-2-3-4-4-5/a2-b1_b3-c1_c2-d1_d2-e1_e4-f1_a1-f4~n");
//		lWURCS.add("WURCS=2.0/5,5,5/[h2h][a2122m-1b_1-5_4*NCC/3=O][a2122h-1a_1-5_2*NCC/3=O][a2112h-1b_1-5][a2122h-1b_1-5_2*NCC/3=O]/1-2-3-4-5/a1-b1_b3-c1_c3-d1_d2-e1_a3-d3*OPO*/3O/3=O~n");
//		lWURCS.add("WURCS=2.0/1,1,0/[hxh]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[h1h]/1/");
//		lWURCS.add("WURCS=2.0/2,4,4/[a1122h-1a_1-5][a2112h-1b_1-5]/1-2-1-1/a4-b1_b6n1-c1n2*1OP^X*2/3O/3=O_c2-d1_a1-b6*OPO*/3O/3=O~n");
//		lWURCS.add("WURCS=2.0/3,4,3/[uxxxxm_2*NCC/3=O_4*N][uxxxxh_2*NCC/3=O][uxxxxh]/1-2-2-3/a?|b?|c?|d?}-{a?|b?|c?|d?_a?|b?|c?|d?}-{a?|b?|c?|d?_a?|b?|c?|d?}-{a?|b?|c?|d?");
//		lWURCS.add("WURCS=2.0/3,3,6/[AUd21122h_5*NCC/3=O][u2122h][u2112h]/1-2-3/a?|b?|c?}*OCC/3=O_a?|b?|c?}*OCC/3=O_a?|b?|c?}*OCC/3=O_a?|b?|c?}*OCC/3=O_a?|b?|c?}-{a?|b?|c?_a?|b?|c?}-{a?|b?|c?");
//		lWURCS.add("WURCS=2.0/1,1,0/[a21eEA-1a_1-5]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[a21EEA-1a_1-5]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[mO211h]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[oO211h]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[o121Oh]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[o1212o]/1/");
		// Test monosaccharides with anomer variation
//		lWURCS.add("WURCS=2.0/1,1,0/[a2122h-1x_1-5]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[o2122h]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[u2122h]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[ha122h-2x_2-6]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[hO122h]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[hU122h]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[Aad21122h-2x_2-6_5*NCC/3=O]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[AOd21122h_5*NCC/3=O]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[AUd21122h_5*NCC/3=O]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[oddOOm]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[a21O2m-1x_1-5]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[o21O2m]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[u21O2m]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[ha21Oh-2x_2-6]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[hO21Oh]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[hU21Oh]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[a2122o-1x_1-5]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[o2122o]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[u2122o]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[oa122h-2x_2-6]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[oO122h]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[oU122h]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[h2122o]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[h2122h]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[<Q>]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[<Q>-?a]/1/");
//		lWURCS.add("WURCS=2.0/1,1,0/[<Q>-?b]/1/");
		//
//		lWURCS.add("WURCS=2.0/2,2,2/[a2112h-1a_1-5][a2122h-1b_1-5_2*NCC/3=O_3%.85%*OCC/3=O]/1-2/a3-b1_a1-b4*OPO*/3O/3=O~n");
//		lWURCS.add("WURCS=2.0/1,2,1/[a222h-1b_1-4_1*N]/1-1/a3-b5*OPO*/3O/3=O");
//		lWURCS.add("WURCS=2.0/6,9,8/[AUd1122h][u11221h][u2122h_2*NCC/3=O][u2122h][u2112h][u2122A]/1-2-2-3-3-3-4-5-6/a?|b?|c?|d?|e?|f?|g?|h?|i?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?_a?|b?|c?|d?|e?|f?|g?|h?|i?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?_a?|b?|c?|d?|e?|f?|g?|h?|i?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?_a?|b?|c?|d?|e?|f?|g?|h?|i?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?_a?|b?|c?|d?|e?|f?|g?|h?|i?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?_a?|b?|c?|d?|e?|f?|g?|h?|i?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?_a?|b?|c?|d?|e?|f?|g?|h?|i?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?_a?|b?|c?|d?|e?|f?|g?|h?|i?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?");
//		lWURCS.add("WURCS=2.0/5,7,6/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a1221m-1a_1-5]/1-2-3-4-4-5-4/a4-b1_a6-f1_b4-c1_c3-d1_c6-e1_g1-d?|e?}");
//		lWURCS.add("WURCS=2.0/5,6,6/[a2112h-1a_1-5][a2112h-1b_1-5_2*NCC/3=O][a2112h-1b_1-4][a2112h-1b_1-5][h222h]/1-2-3-4-3-5/a3-b1_b6-c1_c6-d1_d3-e1_e6-f5*OPO*/3O/3=O_a1-f1~n");
//		lWURCS.add("WURCS=2.0/5,5,5/[h2h][a2122m-1b_1-5_4*NCC/3=O][a2122h-1a_1-5_2*NCC/3=O][a2112h-1b_1-5][a2122h-1b_1-5_2*NCC/3=O]/1-2-3-4-5/a1-b1_b3-c1_c3-d1_d2-e1_a3-d3*OPO*/3O/3=O~n");
//		lWURCS.add("WURCS=2.0/2,2,2/[Ad1dd11h_3-7_1*OC][h2h]/1-2/a6-b3*N*/2S(CC^ECC^ZCC$5)/6OC/4=O/4=O_a8-b1");
//		lWURCS.add("WURCS=2.0/3,4,4/[u2112h][a2122h-1b_1-5_2*NCC/3=O_6*OSO/3=O/3=O][a2112h-1b_1-5_6*OSO/3=O/3=O]/1-2-3-2/a3-b1_b4-c1_c3-d1_b1-c3~1-n");
//		lWURCS.add("WURCS=2.0/6,7,7/[a2122m-1b_1-5_3*NCC/3=O][a2112h-1a_1-5_2*NCC/3=O][a2112h-1b_1-5][a2112h-1a_1-5][a222h-1b_1-4][a2122h-1a_1-5]/1-2-3-4-4-5-6/a4-b1_b3-c1_c3-d1_c4-f1_e1-d3%.25%_g1-b4%.55%_a1-f3~n");
//		lWURCS.add("WURCS=2.0/3,4,3/[u2112h_2*NCC/3=O][u2112h][h2h]/1-1-2-3/a?|b?|c?|d?}-{a?|b?|c?|d?_a?|b?|c?|d?}-{a?|b?|c?|d?_a?|b?|c?|d?}-{a?|b?|c?|d?*OPO*/3O/3=O");
//		lWURCS.add("WURCS=2.0/4,4,4/[u2122h_2*NCC/3=O][u2122h_2*NSO/3=O/3=O][u21eEA][u2121A]/1-2-3-4/a?|b?|c?|d?}*OSO/3=O/3=O_a?|b?|c?|d?}-{a?|b?|c?|d?_a?|b?|c?|d?}-{a?|b?|c?|d?_a?|b?|c?|d?}-{a?|b?|c?|d?");

		// Invalide residue.
//		lWURCS.add("WURCS=2.0/7,15,15/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1b_1-5][a1221m-1a_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-2-5-2-5-4-2-5-6-7-7-7/a4-b1_a6-l1_b4-c1_c3-d1_c6-i1_d2-e1_d4-g1_e4-f1_g4-h1_i2-j1_j4-k1_m2-a3|b3|c3|d3|e3|f3|g3|h3|i3|j3|k3|l3}_n2-a3|b3|c3|d3|e3|f3|g3|h3|i3|j3|k3|l3}_o2-a6|b6|c6|d6|e6|f6|g6|h6|i6|j6|k6|l6}_a6|b6|c6|d6|e6|f6|g6|h6|i6|j6|k6|l6}*OSO/3=O/3=O");
		// The child residue has a parent residue.
//		lWURCS.add("WURCS=2.0/6,10,7+/[a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a1221m-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-1-2-3-3-1-4-5-6-1/a4-b1_b4-c1_c3-d1_c6-e1_f3-g1_f4-h1_h6-i2");
		// Phospho-ethanolamine
		lWURCS.add("WURCS=2.0/2,2,1/[hxh][a2122h-1b_1-5]/1-2/a3n2-b1n1*1NCCOP^XO*2/6O/6=O");
		return lWURCS;
	}

	private static List<String> readTXT(String filename) {
		List<String> elems = new ArrayList<>();
		try {
			File f = new File(filename);
			InputStreamReader osr  = new InputStreamReader(new FileInputStream(f));
			BufferedReader br = new BufferedReader(osr);

			String line;
			while ((line = br.readLine()) != null) {
				elems.add(line);
			}
			br.close();

		} catch (IOException e) {
			System.err.println(e);
		}
		return elems;
	}
	private static void convertWURCSs(List<String> a_lWURCSs) {
		int t_nWURCS = 1;
		List<String> t_lErrorWURCS = new ArrayList<>();
		for ( String wurcs : a_lWURCSs ) {
			System.out.println(t_nWURCS++);
			System.out.println("WURCS: "+wurcs);
			String t_strGlycoCT = null;
			try {
				t_strGlycoCT = convertToGlycoCT(wurcs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if ( t_strGlycoCT == null ) {
				t_lErrorWURCS.add((t_nWURCS-1)+":"+wurcs);
				continue;
			}
			System.out.println("GlycoCT:\n"+t_strGlycoCT);
			String t_strConvWURCS = null;
			try {
				t_strConvWURCS = convertGlycoCTToWURCS(t_strGlycoCT);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			if ( !wurcs.equals(t_strConvWURCS) ) {
				System.out.println("Original:\t"+wurcs);
				System.out.println("Converted:\t"+t_strConvWURCS);
			}

		}
		System.out.println();
		System.out.println("Errors:");
		for ( String t_strError : t_lErrorWURCS ) {
			System.out.println(t_strError);
		}

	}

	protected static String convertToGlycoCT(String strWurcs) throws Exception {
		String t_strMessage = "";
		try {
			// Import WURCS string
//			WURCSImporter t_import = new WURCSImporter();
//			t_import.start(strWurcs);
//			GlyContainer t_gc = t_import.getGlyContainer();
			WURCSFactory wf = new WURCSFactory(strWurcs);
			WURCSGraph graph = wf.getGraph();

			WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();
			wg2gc.start(graph);
			GlyContainer t_gc = wg2gc.getGlycan();

			// Exchange GlyConatainer to Sugar
			GlyContainerToSugar t_export = new GlyContainerToSugar();
			t_export.start(t_gc);
			Sugar t_sugar = t_export.getConvertedSugar();
			SugarExporterGlycoCTCondensed t_exportGlycoCT = new SugarExporterGlycoCTCondensed();
			t_exportGlycoCT.start(t_sugar);
			String t_strGlycoCT = t_exportGlycoCT.getHashCode();
			return t_strGlycoCT;
		} catch (WURCSException | GlycanException e) {
			t_strMessage = e.getMessage();
			e.printStackTrace();
		} catch (GlycoconjugateException e) {
			t_strMessage = e.getMessage();
			e.printStackTrace();
		} catch (GlycoVisitorException e) {
			t_strMessage = e.getMessage();
			e.printStackTrace();
		}
		if ( !t_strMessage.isEmpty() )
			throw new Exception(t_strMessage);
		return null;
	}

	protected static String convertGlycoCTToWURCS(String a_strGlycoCT) throws Exception {
		String t_strMessage = "";
		WURCSExporterGlycoCT t_oExporter = new WURCSExporterGlycoCT();
		try {
			t_oExporter.start(a_strGlycoCT);
			String t_strWURCS = t_oExporter.getWURCS();
			return t_strWURCS;

		} catch ( SugarImporterException e ) {
			String message = "There is an error in importer of GlycoCT.\n";
			if ( e.getErrorText() != null ) {
				message += e.getErrorText();
			}
			t_strMessage = message;
			System.out.println(message);
			e.printStackTrace();
		} catch ( GlycoVisitorException e ) {
			String message = "There is an error in GlycoCT validation.\n";
			message += e.getErrorMessage();

			String t_strLog = t_oExporter.getValidationErrorLog().toString();
			if ( !t_strLog.equals("") )
				message += t_strLog;

			t_strMessage = message;
			System.out.println(message);
			e.printStackTrace();
		} catch (WURCSException e) {
			t_strMessage = e.getErrorMessage();
			System.out.println(e.getErrorMessage());
			e.printStackTrace();
		}
		if ( !t_strMessage.isEmpty() ) {
			throw new Exception(t_strMessage);
		}
		return "";
	}

}
