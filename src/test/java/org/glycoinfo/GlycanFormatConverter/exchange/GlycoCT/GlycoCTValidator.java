package org.glycoinfo.GlycanFormatConverter.exchange.GlycoCT;

import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.validation.GlycoVisitorValidation;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.glycoinfo.WURCSFramework.io.GlycoCT.GlycoVisitorValidationForWURCS;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation class for GlycoCT
 * @author Masaaki Matsubara
 *
 */
public class GlycoCTValidator {

	private List<String> m_lErrors;
	private List<String> m_lWarnings;
	private StringBuffer m_sbLog;
	private boolean m_bIsNormalized;
	private String m_strNormalizedGlycoCT;

	public GlycoCTValidator() {
		this.m_lErrors   = new ArrayList<>();
		this.m_lWarnings = new ArrayList<>();
		this.m_sbLog = new StringBuffer();
		this.m_bIsNormalized = false;
		this.m_strNormalizedGlycoCT = "";
	}

	public List<String> getErrors() {
		return this.m_lErrors;
	}

	public List<String> getWarnings() {
		return this.m_lWarnings;
	}

	public String getValidationErrorLog() {
		return this.m_sbLog.toString();
	}

	public boolean isNormalized() {
		return this.m_bIsNormalized;
	}

	public String getNormalizedGlycoCT() {
		return this.m_strNormalizedGlycoCT;
	}

	protected void start(String a_strGlycoCT) throws SugarImporterException, GlycoVisitorException {
		SugarImporter t_objImporterGlycoCT = new SugarImporterGlycoCTCondensed();
		Sugar t_oSugar = t_objImporterGlycoCT.parse(a_strGlycoCT);
		// Validate Sugar
		this.validate(t_oSugar);
		// Normalize Sugar
		t_oSugar = this.normalize(t_oSugar);

		// Export normalized GlycoCT
		SugarExporterGlycoCTCondensed t_objExporterGlycoCT = new SugarExporterGlycoCTCondensed();
		t_objExporterGlycoCT.start(t_oSugar);
		String t_strGlycoCT = t_objExporterGlycoCT.getHashCode();

		// Compare normalized GlycoCT
		if ( !a_strGlycoCT.equals(t_strGlycoCT) ) {
			this.m_bIsNormalized = true;
			this.m_strNormalizedGlycoCT = t_strGlycoCT;
		}
	}

	/** Validate sugar */
	private void validate(Sugar a_objSugar) throws GlycoVisitorException {

		// Validate sugar
		GlycoVisitorValidation t_validation = new GlycoVisitorValidation();
		t_validation.start(a_objSugar);
		this.m_lErrors.addAll( t_validation.getErrors() );
		this.m_lWarnings.addAll( t_validation.getWarnings() );

		// Remove error "Sugar has more than one root residue." for hundling compositions
		while( this.m_lErrors.contains("Sugar has more than one root residue.") )
			this.m_lErrors.remove( this.m_lErrors.indexOf("Sugar has more than one root residue.") );

		// Validate for WURCS
		GlycoVisitorValidationForWURCS t_validationWURCS = new GlycoVisitorValidationForWURCS();
		t_validationWURCS.start(a_objSugar);

		// Marge errors and warnings
		this.m_lErrors.addAll( t_validationWURCS.getErrors() );
		this.m_lWarnings.addAll( t_validationWURCS.getWarnings() );
		if ( !this.m_lErrors.isEmpty() )
			this.m_sbLog.append("Errors:\n");
		for ( String err : this.m_lErrors )
			this.m_sbLog.append(err+"\n");

		if ( !this.m_lWarnings.isEmpty() )
			this.m_sbLog.append("Warnings:\n");
		for ( String warn : this.m_lWarnings ) {
			this.m_sbLog.append(warn+"\n");
		}
		if ( !this.m_lErrors.isEmpty() ) {
			throw new GlycoVisitorException("Error in GlycoCT validation.");
		}
	}

	/** Nomarize sugar */
	private Sugar normalize(Sugar a_objSugar) throws GlycoVisitorException {
		// Normalize sugar
		GlycoVisitorToGlycoCT t_objTo
			= new GlycoVisitorToGlycoCT( new MonosaccharideConverter( new Config() ) );
		t_objTo.start(a_objSugar);
		return t_objTo.getNormalizedSugar();
	}

}
