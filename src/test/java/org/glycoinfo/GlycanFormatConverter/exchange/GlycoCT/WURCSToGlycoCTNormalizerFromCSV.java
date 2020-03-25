package org.glycoinfo.GlycanFormatConverter.exchange.GlycoCT;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.glycoinfo.GlycanFormatconverter.io.GlycoCT.WURCSConversionValidatorForGlycoCT;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;

import java.util.*;

public class WURCSToGlycoCTNormalizerFromCSV extends WURCSReaderFromCSV {

	private static String strWrongDelimiter = "Wrong delimiter is used for repeat counts.";
	private static String strWURCSValidationError = "WURCS validation error.";

	private Set<String> m_setIDs;
	private int m_nWURCSs;
	private int m_nGlycoCTs;
	private Map<String, String> m_mapIDToNormalizedWURCS;
	private Map<String, List<String>> m_mapWURCSErrors;
	private Map<String, List<String>> m_mapWURCSValidationErrors;
	private Map<String, List<String>> m_mapWURCSValidationWarnings;
	private Map<String, String> m_mapIDToNormalizedGlycoCT;
	private Map<String, List<String>> m_mapGlycoCTErrors;
	private Map<String, List<String>> m_mapGlycoCTValidationErrors;
	private Map<String, List<String>> m_mapGlycoCTValidationWarnings;

	public WURCSToGlycoCTNormalizerFromCSV() {
		this.m_setIDs = new HashSet<>();

		this.m_nWURCSs = 0;
		this.m_mapIDToNormalizedWURCS = new TreeMap<>();
		this.m_mapWURCSErrors = new TreeMap<>();
		this.m_mapWURCSValidationErrors = new TreeMap<>();
		this.m_mapWURCSValidationWarnings = new TreeMap<>();
		
		this.m_mapIDToNormalizedGlycoCT = new TreeMap<>();
		this.m_mapGlycoCTErrors = new TreeMap<>();
		this.m_mapGlycoCTValidationErrors   = new TreeMap<>();
		this.m_mapGlycoCTValidationWarnings = new TreeMap<>();
	}

	@Override
	protected void processGlycoCT(String a_strID, String a_strGlycoCT) {
		this.m_nGlycoCTs++;

		String t_strNormalizedGlycoCT = normalizeGlycoCT(
				a_strID, a_strGlycoCT,
				this.m_mapGlycoCTErrors,
				this.m_mapGlycoCTValidationErrors,
				this.m_mapGlycoCTValidationWarnings
			);
		if ( !t_strNormalizedGlycoCT.isEmpty() ) {
			System.out.println("GlycoCT normalized: "+a_strID);
			System.out.println("\n"+a_strGlycoCT);
			System.out.println("->\n"+t_strNormalizedGlycoCT);
			this.m_mapIDToNormalizedGlycoCT.put(a_strID, t_strNormalizedGlycoCT);
		}
	}

	@Override
	protected void processWURCS(String a_strID, String a_strWURCS) {
		this.m_setIDs.add(a_strID);

		this.m_nWURCSs++;
		// Fix wrong delimiter for repeating count range
		String t_strNormalizedWURCS = a_strWURCS;
		if ( a_strWURCS.contains(":") ) {
			t_strNormalizedWURCS = a_strWURCS.replaceAll(":", "-");
			if ( !this.m_mapWURCSErrors.containsKey(strWrongDelimiter) )
				this.m_mapWURCSErrors.put(strWrongDelimiter, new ArrayList<String>());
			this.m_mapWURCSErrors.get(strWrongDelimiter).add(a_strID);
			this.m_mapIDToNormalizedWURCS.put(a_strID, t_strNormalizedWURCS);
		}

		t_strNormalizedWURCS = normalizeWURCS(a_strID, t_strNormalizedWURCS,
				this.m_mapWURCSErrors,
				this.m_mapWURCSValidationErrors,
				this.m_mapWURCSValidationWarnings);

		if ( t_strNormalizedWURCS.equals(strWURCSValidationError) ) {
			System.out.println(this.m_nWURCSs);
			System.out.println("WURCS validation error: "+a_strID);
			System.out.println("\t"+a_strWURCS);
			System.out.println("Conversion can not be prosessed due to WURCS validation error.");
			System.out.println();
			this.m_mapIDToNormalizedWURCS.put(a_strID, strWURCSValidationError);
			return;
		}

		if ( !t_strNormalizedWURCS.isEmpty() ) {
			System.out.println("WURCS normalized: "+a_strID);
			System.out.println("\t"+a_strWURCS);
			System.out.println("->\t"+t_strNormalizedWURCS);
			this.m_mapIDToNormalizedWURCS.put(a_strID, t_strNormalizedWURCS);
		}

	}

	@Override
	protected void processAll(String a_strID, String a_strWURCS, String a_strGlycoCT) {

		String t_strWURCS = a_strWURCS;
		if ( this.m_mapIDToNormalizedWURCS.containsKey(a_strID) )
			t_strWURCS = this.m_mapIDToNormalizedWURCS.get(a_strID);

		if ( t_strWURCS.equals(strWURCSValidationError) )
			return;

		String t_strGlycoCT = a_strGlycoCT;
		if ( this.m_mapIDToNormalizedGlycoCT.containsKey(a_strID) )
			t_strGlycoCT = this.m_mapIDToNormalizedGlycoCT.get(a_strID);

		this.processAllNormalized(a_strID, t_strWURCS, t_strGlycoCT);
	}

	protected void processAllNormalized(String a_strID, String a_strWURCS, String a_strGlycoCT) {
		// To be inherited
	}

	@Override
	protected void showResults() {
		System.out.println("Normalized WURCSs:"+ this.m_mapIDToNormalizedWURCS.size()+"/"+this.m_nWURCSs);
		List<String> t_lIDs = new ArrayList<>();
		t_lIDs.addAll(this.m_mapIDToNormalizedWURCS.keySet());
		this.printIDs(t_lIDs, "");
		System.out.println();

		System.out.println("Original WURCS errors:"+ this.countElements(this.m_mapWURCSErrors)+"/"+this.m_nWURCSs);
		this.printIDMap(this.m_mapWURCSErrors, "-", "\t");
		System.out.println();

		System.out.println("Original WURCS validation errors:");
		this.printIDMap(this.m_mapWURCSValidationErrors, "-", "\t");
		System.out.println();

		System.out.println("Original WURCS validation warnings:");
		this.printIDMap(this.m_mapWURCSValidationWarnings, "-", "\t");
		System.out.println();

		System.out.println("Normalized GlycoCTs:"+ this.m_mapIDToNormalizedGlycoCT.size()+"/"+this.m_nGlycoCTs);
		t_lIDs = new ArrayList<>();
		t_lIDs.addAll(this.m_mapIDToNormalizedGlycoCT.keySet());
		this.printIDs(t_lIDs, "");
		System.out.println();

		System.out.println("Original GlycoCT errors:"+ this.countElements(this.m_mapGlycoCTErrors)+"/"+this.m_nGlycoCTs);
		this.printIDMap(this.m_mapGlycoCTErrors, "-", "\t");
		System.out.println();

		System.out.println("Original GlycoCT validation errors:");
		this.printIDMap(this.m_mapGlycoCTValidationErrors, "-", "\t");
		System.out.println();

		System.out.println("Original GlycoCT validation warnings:");
		this.printIDMap(this.m_mapGlycoCTValidationWarnings, "-", "\t");
		System.out.println();

	}

	private String normalizeWURCS(String a_strID, String a_strWURCS,
			Map<String, List<String>> a_mapErrors,
			Map<String, List<String>> a_mapValidationErrors,
			Map<String, List<String>> a_mapValidationWarnigs
		) {
		String t_strNormalized = "";
		try {
			WURCSFactory wf = new WURCSFactory(a_strWURCS);
//			WURCSGraph graph = wf.getGraph();
			String t_strWURCS = wf.getWURCS();

			// Validate WURCS for GlycoCT conversion
			WURCSConversionValidatorForGlycoCT t_wcv4g = new WURCSConversionValidatorForGlycoCT();
			t_wcv4g.start(a_strWURCS);

			// Collect errors
			List<String> t_lAllErrors = new ArrayList<>();
			t_lAllErrors.addAll( t_wcv4g.getErrors() );
			t_lAllErrors.addAll( t_wcv4g.getErrorsForGlycoCT() );
			if ( !t_lAllErrors.isEmpty() ) {
				// Correct error information
				if ( !a_mapErrors.containsKey(strWURCSValidationError) )
					a_mapErrors.put(strWURCSValidationError, new ArrayList<String>());
				a_mapErrors.get(strWURCSValidationError).add(a_strID);

				Set<String> t_lUniques = new HashSet<>();
				for ( String t_strError : t_lAllErrors ) {
					if ( t_lUniques.contains(t_strError) )
						continue;
					System.err.println(t_strError);
					if ( !a_mapValidationErrors.containsKey(t_strError) )
						a_mapValidationErrors.put(t_strError, new ArrayList<String>());
					a_mapValidationErrors.get(t_strError).add(a_strID);
					t_lUniques.add(t_strError);
				}

				// Empty WURCS
				t_strWURCS = strWURCSValidationError;
			}

			// Collect warnings
			List<String> t_lAllWarnings = new ArrayList<>();
			t_lAllWarnings.addAll( t_wcv4g.getWarnings() );
			t_lAllWarnings.addAll( t_wcv4g.getWarningsForGlycoCT() );
			if ( !t_lAllWarnings.isEmpty() ) {
				Set<String> t_lUniques = new HashSet<>();
				for ( String t_strWarning : t_lAllWarnings ) {
					if ( t_lUniques.contains(t_strWarning) )
						continue;
					if ( !a_mapValidationWarnigs.containsKey(t_strWarning) )
						a_mapValidationWarnigs.put(t_strWarning, new ArrayList<String>());
					a_mapValidationWarnigs.get(t_strWarning).add(a_strID);
					t_lUniques.add(t_strWarning);
				}
			}

			if ( !a_strWURCS.equals(t_strWURCS) )
				t_strNormalized = t_strWURCS;

		} catch (WURCSException e) {
			e.printStackTrace();
			// Correct error information
			if ( !a_mapErrors.containsKey(e.getErrorMessage()) )
				a_mapErrors.put(e.getErrorMessage(), new ArrayList<String>());
			a_mapErrors.get(e.getErrorMessage()).add(a_strID);
		}

		return t_strNormalized;
	}

	protected String normalizeGlycoCT(
			String a_strID, String a_strGlycoCT,
			Map<String, List<String>> a_mapErrors,
			Map<String, List<String>> a_mapValidationErrors,
			Map<String, List<String>> a_mapValidationWarnigs
		) {
		GlycoCTValidator t_validator = new GlycoCTValidator();
		try {
			t_validator.start(a_strGlycoCT);
			if ( !t_validator.isNormalized() )
				return "";
			return t_validator.getNormalizedGlycoCT();
		} catch (SugarImporterException e) {
			e.printStackTrace();
			String t_strError = e.getErrorText();
			if ( !a_mapErrors.containsKey(t_strError) )
				a_mapErrors.put(t_strError, new ArrayList<String>());
			a_mapErrors.get(t_strError).add(a_strID);
		} catch (GlycoVisitorException e) {
			e.printStackTrace();
			String t_strError0 = e.getErrorMessage();
			if ( !a_mapErrors.containsKey(t_strError0) )
				a_mapErrors.put(t_strError0, new ArrayList<String>());
			a_mapErrors.get(t_strError0).add(a_strID);

			Set<String> t_lUniques = new HashSet<>();
			for ( String t_strError : t_validator.getErrors() ) {
				if ( t_lUniques.contains(t_strError) )
					continue;
				if ( !a_mapValidationErrors.containsKey(t_strError) )
					a_mapValidationErrors.put(t_strError, new ArrayList<String>());
				a_mapValidationErrors.get(t_strError).add(a_strID);
				t_lUniques.add(t_strError);
			}
			t_lUniques = new HashSet<>();
			for ( String t_strWarning : t_validator.getWarnings() ) {
				if ( t_lUniques.contains(t_strWarning) )
					continue;
				if ( !a_mapValidationWarnigs.containsKey(t_strWarning) )
					a_mapValidationWarnigs.put(t_strWarning, new ArrayList<String>());
				a_mapValidationWarnigs.get(t_strWarning).add(a_strID);
				t_lUniques.add(t_strWarning);
			}
		}
		return "";
	}


}
