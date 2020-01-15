package org.glycoinfo.GlycanFormatconverter.io.GlycoCT;

import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class for converting WURCS to GlycoCT. This also outputs messages if there are any errors.
 * @author Masaaki Matsubara
 *
 */
public class WURCSToGlycoCT {

	// Version
	private static final String VERSION = "1.0.190520";

	public static void main(String[] args) {

		String t_strWURCS = "";
		for ( int i = 0; i < args.length; i++ ) {

			// For usage
			if ( args[i].equals("-help") ) {
				usage();
				System.exit(0);
			}

			t_strWURCS = args[i];
		}

		WURCSToGlycoCT converter = new WURCSToGlycoCT();
		converter.start(t_strWURCS);
		if ( !converter.getErrorMessages().isEmpty() )
			System.out.println(converter.getErrorMessages());
		String strGlycoCT = converter.getGlycoCT();
		System.out.println(strGlycoCT);
	}

	private static void usage() {
		System.err.println("Conversion System from WURCS2.0 to GlycoCT");
		System.err.println("\tCurrent version: "+VERSION);
		System.err.println();
		System.err.println("Usage: java (this program).jar [OPTION]... [WURCS]... ");
		System.err.println();
		System.err.println("where OPTION include:");
		System.err.println("\t-help\t\tto print this help message");
		System.err.println();
	}

	private String m_strWURCS;
	private String m_strGlycoCT;
	private String m_strValidationErrors;
	private String m_strConversionErrors;

	public String getWURCS() {
		return this.m_strWURCS;
	}

	public String getGlycoCT() {
		return this.m_strGlycoCT;
	}

	public String getErrorMessages() {
		String strErrorMessages = "";
		if ( this.m_strValidationErrors != null )
			strErrorMessages = this.m_strValidationErrors;
		else if ( this.m_strConversionErrors != null ) {
			strErrorMessages = this.m_strConversionErrors;
		}
		return strErrorMessages;
	}

	public void start(String a_strWURCS) {
		String t_strError = this.validate(a_strWURCS);
		if ( !t_strError.isEmpty() ) {
			this.m_strValidationErrors = t_strError;
			return;
		}

		try {
			WURCSFactory wf = new WURCSFactory(a_strWURCS);
			WURCSGraph graph = wf.getGraph();

			// Exchange WURCSGraph to GlyContainer
			WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();
			wg2gc.start(graph);
			GlyContainer t_gc = wg2gc.getGlycan();

			// Exchange GlyConatainer to Sugar
			GlyContainerToSugar t_export = new GlyContainerToSugar();
			t_export.start(t_gc);
			Sugar t_sugar = t_export.getConvertedSugar();

			// Export GlycoCT from Sugar
			SugarExporterGlycoCTCondensed t_exportGlycoCT = new SugarExporterGlycoCTCondensed();
			t_exportGlycoCT.start(t_sugar);
			String t_strGlycoCT = t_exportGlycoCT.getHashCode();

			this.m_strGlycoCT = t_strGlycoCT;
		} catch (Exception e) {
			this.m_strConversionErrors = e.getMessage();
			e.printStackTrace();
		}
	}

	private String validate(String a_strWURCS) {
		// Validate WURCS for GlycoCT conversion
		WURCSConversionValidatorForGlycoCT t_wcv4g = new WURCSConversionValidatorForGlycoCT();
		t_wcv4g.start(a_strWURCS);

		// Collect errors
		String t_strErrorMessages = "";
		List<String> t_lAllErrors = new ArrayList<>();
		t_lAllErrors.addAll( t_wcv4g.getErrors() );
		t_lAllErrors.addAll( t_wcv4g.getErrorsForGlycoCT() );
		if ( !t_lAllErrors.isEmpty() ) {
			t_strErrorMessages = "Validation errors:\n";
			// Correct error information
			Set<String> t_lUniques = new HashSet<>();
			for ( String t_strError : t_lAllErrors ) {
				if ( t_lUniques.contains(t_strError) )
					continue;
				t_strErrorMessages += "\t"+t_strError+"\n";
				t_lUniques.add(t_strError);
			}
		}

		// Collect warnings
		List<String> t_lAllWarnings = new ArrayList<>();
		t_lAllWarnings.addAll( t_wcv4g.getWarnings() );
		t_lAllWarnings.addAll( t_wcv4g.getWarningsForGlycoCT() );
		if ( !t_lAllWarnings.isEmpty() ) {
			if ( !t_strErrorMessages.isEmpty() )
				t_strErrorMessages += "\n";
			t_strErrorMessages += "Validation warnings:\n";
			Set<String> t_lUniques = new HashSet<>();
			for ( String t_strWarning : t_lAllWarnings ) {
				if ( t_lUniques.contains(t_strWarning) )
					continue;
				t_strErrorMessages += "\t"+t_strWarning+"\n";
				t_lUniques.add(t_strWarning);
			}
		}

		return t_strErrorMessages;
	}
}
