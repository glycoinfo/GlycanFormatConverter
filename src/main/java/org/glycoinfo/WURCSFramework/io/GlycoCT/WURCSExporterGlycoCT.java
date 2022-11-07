package org.glycoinfo.WURCSFramework.io.GlycoCT;

import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.validation.GlycoVisitorValidation;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph.SugarToWURCSGraph;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.graph.visitor.WURCSGraphExporterUniqueMonosaccharides;
import org.glycoinfo.WURCSFramework.util.validation.WURCSValidator;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

import java.util.ArrayList;
import java.util.TreeSet;

public class WURCSExporterGlycoCT {

	private String m_strGlycoCT;
	private String m_strWURCS;
	private boolean m_bResidueCodeCollection;
	private TreeSet<String> m_setResidueCodes;
	private StringBuffer m_sbLog;

	public WURCSExporterGlycoCT() {
		this.m_strGlycoCT = "";
		this.m_strWURCS   = "";
		this.m_bResidueCodeCollection = false;
		this.m_setResidueCodes = new TreeSet<>();
		this.m_sbLog = new StringBuffer();
	}

	public void setResidueCodeCollection(boolean a_bCollection) {
		this.m_bResidueCodeCollection = a_bCollection;
	}

	public String getGlycoCT() {
		return this.m_strGlycoCT;
	}

	public String getWURCS() {
		return this.m_strWURCS;
	}

	public boolean doesCollectResidueCodes() {
		return this.m_bResidueCodeCollection;
	}

	public TreeSet<String> getUniqueResidueCodes() {
		return this.m_setResidueCodes;
	}

	public StringBuffer getValidationErrorLog() {
		return this.m_sbLog;
	}

	/**
	 * Convert GlycoCT to WURCS
	 * @param a_strGlycoCT
	 * @throws SugarImporterException
	 * @throws GlycoVisitorException
	 * @throws WURCSException
	 */
	public void start( String a_strGlycoCT ) throws SugarImporterException, GlycoVisitorException, WURCSException {
		this.m_strGlycoCT = a_strGlycoCT;

		// Reset log
		this.m_sbLog = new StringBuffer();

		SugarImporter t_objImporterGlycoCT = new SugarImporterGlycoCTCondensed();
		SugarToWURCSGraph t_objExporterWURCSGraph = new SugarToWURCSGraph();
		Sugar t_oSugar = t_objImporterGlycoCT.parse(this.m_strGlycoCT);

		// Validate Sugar
		this.validate(t_oSugar);
		// Normalize Sugar
		t_oSugar = this.normalize(t_oSugar);
		t_objExporterWURCSGraph.start(t_oSugar);

		// Exchange
		SugarToWURCSGraph t_oS2G = new SugarToWURCSGraph();
		t_oS2G.start(t_oSugar);
		WURCSGraph t_oGraph = t_oS2G.getGraph();

		// Get WURCS string
		WURCSFactory t_oFactory = new WURCSFactory(t_oGraph);
		this.m_strWURCS = t_oFactory.getWURCS();

		// Validate WURCS
		this.m_strWURCS = this.validate(this.m_strWURCS);

		// Do not collect ResidueCodes if the flag is false
		if ( !this.m_bResidueCodeCollection ) return;

		// Collect unique monosaccharides (ResidueCodes)
		WURCSGraphExporterUniqueMonosaccharides t_oExportMS = new WURCSGraphExporterUniqueMonosaccharides();
		t_oExportMS.start( t_oFactory.getGraph() );
		for ( String t_strUniqueMS : t_oExportMS.getMSStrings() ) {
			if ( this.m_setResidueCodes.contains(t_strUniqueMS) ) continue;
			this.m_setResidueCodes.add(t_strUniqueMS);
		}
	}

	/** Validate sugar */
	private void validate(Sugar a_objSugar) throws GlycoVisitorException {

		// Validate sugar
		GlycoVisitorValidation t_validation = new GlycoVisitorValidation();
		t_validation.start(a_objSugar);
		ArrayList<String> t_aErrorStrings   = t_validation.getErrors();
		ArrayList<String> t_aWarningStrings = t_validation.getWarnings();

		// Remove error "Sugar has more than one root residue." for hundling compositions
		while( t_aErrorStrings.contains("Sugar has more than one root residue.") )
			t_aErrorStrings.remove( t_aErrorStrings.indexOf("Sugar has more than one root residue.") );

		// Validate for WURCS
		GlycoVisitorValidationForWURCS t_validationWURCS = new GlycoVisitorValidationForWURCS();
		t_validationWURCS.start(a_objSugar);

		// Marge errors and warnings
		StringBuilder t_sbLog = new StringBuilder();
		t_aErrorStrings.addAll( t_validationWURCS.getErrors() );
		t_aWarningStrings.addAll( t_validationWURCS.getWarnings() );

		if ( !t_aErrorStrings.isEmpty() || !t_aWarningStrings.isEmpty() )
			t_sbLog.append("[GlycoCT validation]\n");

		if ( !t_aErrorStrings.isEmpty() )
			t_sbLog.append("Errors:\n");
		for ( String err : t_aErrorStrings )
			t_sbLog.append(err+"\n");

		if ( !t_aWarningStrings.isEmpty() )
			t_sbLog.append("Warnings:\n");
		for ( String warn : t_aWarningStrings )
			t_sbLog.append(warn+"\n");

		this.m_sbLog.append(t_sbLog);
		if ( !t_aErrorStrings.isEmpty() )
			throw new GlycoVisitorException("Error in GlycoCT validation.");
	}

	/** Nomarize sugar */
	private Sugar normalize(Sugar a_objSugar) throws GlycoVisitorException {
		// Normalize sugar
		GlycoVisitorToGlycoCT t_objTo
			= new GlycoVisitorToGlycoCT( new MonosaccharideConverter( new Config() ) );
		t_objTo.start(a_objSugar);
		return t_objTo.getNormalizedSugar();
	}

	/** Validate WURCS */
	private String validate(String a_strWURCS) throws WURCSException {
		// Validate WURCS string
		WURCSValidator t_validation = new WURCSValidator();
		t_validation.start(a_strWURCS);

		StringBuilder t_sbLog = new StringBuilder();
		if ( t_validation.getReport().hasError() || t_validation.getReport().hasWarning() ) {
			t_sbLog.append("[WURCS validation]\n");
			t_sbLog.append( t_validation.getReport().getResultsSimple() );
		}
		this.m_sbLog.append(t_sbLog);
		if ( t_validation.getReport().hasError() )
			throw new WURCSException("Error in WURCS validation.");

		return t_validation.getReport().getStandardString();
	}
}
