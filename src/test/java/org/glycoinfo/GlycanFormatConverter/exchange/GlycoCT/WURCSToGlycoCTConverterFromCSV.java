package org.glycoinfo.GlycanFormatConverter.exchange.GlycoCT;

import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlycoCT.GlyContainerToSugar;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.io.GlycoCT.WURCSExporterGlycoCT;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WURCSToGlycoCTConverterFromCSV extends WURCSToGlycoCTNormalizerFromCSV {

	private int m_nWURCSs;
	private Map<String, String> m_mapIDToWURCS;
	private Map<String, Map<String, List<String>>> m_mapW2GExceptionToErrorToIDs;
	private Map<String, Map<String, List<String>>> m_mapG2WExceptionToErrorToIDs;

	private Map<String, List<String>> m_mapGlycoCTErrors;
	private Map<String, List<String>> m_mapGlycoCTValidationErrors;
	private Map<String, List<String>> m_mapGlycoCTValidationWarnings;

	private int m_nSuccess;
	private int m_nErrors;
	private int m_nComparedGlycoCT;
	private List<String> m_lNormalizedGlycoCT;
	private List<String> m_lMismatchedGlycoCT;
	private List<String> m_lMismatchedWURCS;
	private Map<String, String> m_mapIDToMismatchedWURCS;
	private Map<String, List<String>> m_mapMismatchedDetails;


	public WURCSToGlycoCTConverterFromCSV() {
		this.m_nWURCSs = 0;
		this.m_mapIDToWURCS = new TreeMap<>();

		this.m_mapW2GExceptionToErrorToIDs = new TreeMap<>();
		this.m_mapG2WExceptionToErrorToIDs = new TreeMap<>();

		this.m_mapGlycoCTErrors = new TreeMap<>();
		this.m_mapGlycoCTValidationErrors   = new TreeMap<>();
		this.m_mapGlycoCTValidationWarnings = new TreeMap<>();

		this.m_nSuccess = 0;
		this.m_nErrors = 0;
		this.m_nComparedGlycoCT = 0;

		this.m_lNormalizedGlycoCT = new ArrayList<>();
		this.m_lMismatchedGlycoCT = new ArrayList<>();
		this.m_lMismatchedWURCS   = new ArrayList<>();
		this.m_mapIDToMismatchedWURCS = new TreeMap<>();
		this.m_mapMismatchedDetails = new TreeMap<>();
	}

	@Override
	protected void processAllNormalized(String a_strID, String a_strWURCS, String a_strGlycoCT) {
		this.m_nWURCSs++;
		System.out.println(this.m_nWURCSs+": "+a_strID+":"+a_strWURCS);
		this.m_mapIDToWURCS.put(a_strID, a_strWURCS);

		// Convert WURCS to GlycoCT
		String t_strConvertedGlycoCT = this.convertW2G(a_strID, a_strWURCS);
		if ( t_strConvertedGlycoCT == null ) {
			this.m_nErrors++;

			System.out.println("Couldn't be converted to GlycoCT.");
			return;
		}
		// Normalize GlycoCT
		String t_strNormalizedGlycoCT = this.normalizeGlycoCT(
				a_strID, t_strConvertedGlycoCT,
				this.m_mapGlycoCTErrors,
				this.m_mapGlycoCTValidationErrors,
				this.m_mapGlycoCTValidationWarnings
			);
		if ( !t_strNormalizedGlycoCT.isEmpty() ) {
			this.m_lNormalizedGlycoCT.add(a_strID);
			t_strConvertedGlycoCT = t_strNormalizedGlycoCT;

			System.out.println("Converted GlycoCT is normalized.");
		}
		System.out.println("Converted GlycoCT:\n"+t_strConvertedGlycoCT);

		// Compare original GlycoCT if any
		List<Integer> t_lMismachedLines = new ArrayList<>();
		if ( !a_strGlycoCT.isEmpty() ) {
			this.m_nComparedGlycoCT++;
			if ( !matchLines(a_strGlycoCT, t_strConvertedGlycoCT, t_lMismachedLines) ) {
				this.m_lMismatchedGlycoCT.add(a_strID);

				System.out.println("Original and converted GlcoCTs are mismatched.");
				System.out.println("MismatchedLines: "+t_lMismachedLines);
				System.out.println("Original GlycoCT:\n"+a_strGlycoCT);
			}
		}

		// Reconvert GlycoCT to WURCS
		String t_strConvertedWURCS = this.convertG2W(a_strID, t_strConvertedGlycoCT);
		if ( t_strConvertedWURCS == null ) {
			this.m_nErrors++;

			System.out.println("Couldn't be reconverted to WURCS.");
			return;
		}
		// Compare original WURCS
		if ( !a_strWURCS.equals(t_strConvertedWURCS) ) {
			this.m_lMismatchedWURCS.add(a_strID);
			this.m_mapIDToMismatchedWURCS.put(a_strID, t_strConvertedWURCS);
			String t_strMessage = "Something mismatched.";
			/*
			if ( t_strConvertedGlycoCT.contains("phospho-ethanolamine") )
				t_strMessage = "Linkage position of phospho-ethanoleamine is swaped.";
			if ( a_strWURCS.contains("-1x_1-?") && t_strConvertedWURCS.contains("/[u") )
				t_strMessage = "Ring state of root residue is lost.";
			if ( a_strWURCS.contains("-2x_2-?") && t_strConvertedWURCS.contains("/[AU") )
				t_strMessage = "Ring state of root residue is lost.";
			*/
			if ( a_strWURCS.contains("+/") )
				t_strMessage = "Missing linkages";
			if ( !this.m_mapMismatchedDetails.containsKey(t_strMessage) )
				this.m_mapMismatchedDetails.put(t_strMessage, new ArrayList<String>());
			this.m_mapMismatchedDetails.get(t_strMessage).add(a_strID);

			System.out.println("Original and reconverted WURCSs are mismatched.");
			System.out.println("Reconverted WURCS:\n"+t_strConvertedWURCS);
			return;
		}
		this.m_nSuccess++;
	}

	@Override
	protected void showResults() {

		System.out.println("Success: "+this.m_nSuccess+"/"+this.m_nWURCSs);

		System.out.println("Mismatched WURCS: "+ this.m_lMismatchedWURCS.size()+"/"+this.m_nWURCSs);
		if ( !this.m_lMismatchedWURCS.isEmpty() )
			this.printIDMap(this.m_mapMismatchedDetails, "", "\t");
		System.out.println();

		if ( !this.m_mapMismatchedDetails.isEmpty() ) {
			System.out.println("--Details for something mismatched:");
			for ( String t_strID : this.m_mapMismatchedDetails.get("Something mismatched.") ) {
				System.out.println("-"+t_strID);
				System.out.println("\t"+this.m_mapIDToWURCS.get(t_strID));
				System.out.println("\t"+this.m_mapIDToMismatchedWURCS.get(t_strID));
			}
			System.out.println();
		}

		System.out.println("Normalized converted GlycoCT: "+ this.m_lNormalizedGlycoCT.size()+"/"+this.m_nWURCSs);
		if ( !this.m_lNormalizedGlycoCT.isEmpty() )
			this.printIDs(this.m_lNormalizedGlycoCT, "");
		System.out.println();

		System.out.println("Errors: "+this.m_nErrors+"/"+this.m_nWURCSs);
		System.out.println();

		int t_nW2GError = this.countElementsOfElements(this.m_mapW2GExceptionToErrorToIDs);
		if ( t_nW2GError != 0 ) {
			System.out.println("-W2G errors: "+t_nW2GError+"/"+this.m_nErrors);
			this.printIDMapOfMap(this.m_mapW2GExceptionToErrorToIDs, "-", "\t");
			System.out.println();
		}

		int t_nG2WError = this.countElementsOfElements(this.m_mapG2WExceptionToErrorToIDs);
		if ( t_nG2WError != 0 ) {
			System.out.println("-G2W errors: "+t_nG2WError+"/"+this.m_nErrors);
			this.printIDMapOfMap(this.m_mapG2WExceptionToErrorToIDs, "-", "\t");
			System.out.println();
		}

		int t_nGErrors = this.countElements(this.m_mapGlycoCTErrors);
		System.out.println("Converted GlycoCT errors: "+ t_nGErrors+"/"+t_nG2WError);
		this.printIDMap(this.m_mapGlycoCTErrors, "", "\t");
		System.out.println();

		System.out.println("Converted GlycoCT validation errors:");
		this.printIDMap(this.m_mapGlycoCTValidationErrors, "", "\t");
		System.out.println();

		System.out.println("Converted GlycoCT validation warnings:");
		this.printIDMap(this.m_mapGlycoCTValidationWarnings, "", "\t");
		System.out.println();

		System.out.println("Compared GlycoCTs: "+this.m_nComparedGlycoCT);
		System.out.println("Mismatched GlycoCTs: "+this.m_lMismatchedGlycoCT.size()+"/"+this.m_nComparedGlycoCT);
		if ( !this.m_lMismatchedGlycoCT.isEmpty() )
			this.printIDs(this.m_lMismatchedGlycoCT, "");
		System.out.println();

		System.out.println("--Validation results for original WURCS and GlycoCTs--");
		super.showResults();
	}

	protected boolean matchLines(String str1, String str2, List<Integer> a_lMismatchedLines) {
		String[] strSplit1 = str1.split("\n");
		String[] strSplit2 = str2.split("\n");

		int t_nLower = strSplit1.length;
		int t_nUpper = strSplit2.length;
		if ( t_nLower > t_nUpper ) {
			t_nLower = strSplit2.length;
			t_nUpper = strSplit1.length;
		}
		boolean t_bMatched = true;
		for ( int i=0; i<t_nLower; i++ ) {
			String line1 = strSplit1[i];
			String line2 = strSplit2[i];
			if ( !line1.equals(line2) ) {
				t_bMatched = false;
				a_lMismatchedLines.add(i+1);
			}
		}
		if ( t_nLower != t_nUpper ) {
			t_bMatched = false;
			for ( int i=t_nLower; i<=t_nUpper; i++ )
				a_lMismatchedLines.add(i);
		}
		return t_bMatched;
	}

	protected String convertW2G(String a_strID, String a_strWURCS) {
		String t_strException = "";
		String t_strMessage = "";
		try {
			WURCSFactory wf = new WURCSFactory(a_strWURCS);
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
		} catch (WURCSException e) {
			t_strException = "WURCSException";
			t_strMessage = e.getMessage();
			// Organize error messages with structure information
			if ( t_strMessage.contains(" could not found")
			  || t_strMessage.contains(" could not support") )
				t_strMessage = "Complex substituent.";
			if ( t_strMessage.contains(" could not handled") )
				t_strMessage = "Complex backbone.";
			e.printStackTrace();
		} catch (GlycanException e) {
			t_strException = "GlycanException";
			t_strMessage = e.getMessage();
			// Organize error messages with structure information
			if ( t_strMessage.contains(" could not found !") )
				t_strMessage = "Complex substituent.";
			if ( t_strMessage.contains(" have more than two anchors") )
				t_strMessage = "Complex substituent with multiple linkages.";
			e.printStackTrace();
		} catch (GlycoconjugateException e) {
			t_strException = "GlycoconjugateException";
			t_strMessage = e.getMessage();
			e.printStackTrace();
		} catch (GlycoVisitorException e) {
			t_strException = "GlycoVisitorException";
			t_strMessage = e.getMessage();
			e.printStackTrace();
		}
		if ( !t_strMessage.isEmpty() )
			this.putExceptionToErrorToID(this.m_mapW2GExceptionToErrorToIDs, t_strException, t_strMessage, a_strID);
		return null;
	}

	protected String convertG2W(String a_strID, String a_strGlycoCT) {
		String t_strException = "";
		String t_strMessage = "";
		WURCSExporterGlycoCT t_oExporter = new WURCSExporterGlycoCT();
		try {
			t_oExporter.start(a_strGlycoCT);
			String t_strWURCS = t_oExporter.getWURCS();
			return t_strWURCS;
		} catch ( SugarImporterException e ) {
			t_strException = "SugarImporterException";
			t_strMessage = e.getErrorText();
			e.printStackTrace();
		} catch ( GlycoVisitorException e ) {
			t_strException = "GlycoVisitorException";
			t_strMessage = e.getErrorMessage();
			e.printStackTrace();
		} catch (WURCSException e) {
			t_strException = "WURCSException";
			t_strMessage = e.getErrorMessage();
			e.printStackTrace();
		}
		if ( !t_strMessage.isEmpty() )
			this.putExceptionToErrorToID(this.m_mapG2WExceptionToErrorToIDs, t_strException, t_strMessage, a_strID);
		return null;
	}

	private void putExceptionToErrorToID(
			Map<String, Map<String, List<String>>> a_mapExToErrToID,
			String a_strException, String a_strError, String a_strID
		) {
		if ( !a_mapExToErrToID.containsKey(a_strException) )
			a_mapExToErrToID.put(a_strException, new TreeMap<String, List<String>>());
		Map<String, List<String>> t_mapErrorToIDs = a_mapExToErrToID.get(a_strException);
		if ( !t_mapErrorToIDs.containsKey(a_strError) )
			t_mapErrorToIDs.put(a_strError, new ArrayList<String>());
		t_mapErrorToIDs.get(a_strError).add(a_strID);

	}
}
