package org.glycoinfo.WURCSFramework.io.GlycoCT;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.glycoinfo.WURCSFramework.util.WURCSException;

import java.util.TreeMap;
import java.util.TreeSet;

public class WURCSExporterGlycoCTList {

	private TreeMap<String, String> m_mapIDToWURCS;
	private TreeMap<String, String> m_mapIDToMessage;
	private TreeSet<String> m_setResidueCodes;
	private boolean m_bResidueCodeCollection;
	private TreeMap<String, String> m_mapIDToValidationErrorLog;

	public WURCSExporterGlycoCTList() {
		this.m_mapIDToWURCS              = new TreeMap<>();
		this.m_mapIDToMessage            = new TreeMap<>();
		this.m_mapIDToValidationErrorLog = new TreeMap<>();
		this.m_setResidueCodes           = new TreeSet<>();
		this.m_bResidueCodeCollection    = false;
	}

	public void setResidueCodeCollection( boolean a_bDoCollection) {
		this.m_bResidueCodeCollection  =  a_bDoCollection;
	}

	public TreeMap<String, String> getMapIDToWURCS() {
		return this.m_mapIDToWURCS;
	}

	public TreeMap<String, String> getMapIDToMessage() {
		return this.m_mapIDToMessage;
	}

	public TreeMap<String, String> getMapIDToValidationErrorLog() {
		return this.m_mapIDToValidationErrorLog;
	}

	public void start(TreeMap<String, String> a_mapGlycoCTList) {
		WURCSExporterGlycoCT t_oExport = new WURCSExporterGlycoCT();
		t_oExport.setResidueCodeCollection(this.m_bResidueCodeCollection);

		for ( String ID : a_mapGlycoCTList.keySet() ) {
			String t_strGlycoCT = a_mapGlycoCTList.get(ID);
			String t_strWURCS = "";
			String t_strMessage = "";

			System.err.println(ID);
			try {
				t_oExport.start(t_strGlycoCT);
				t_strWURCS = t_oExport.getWURCS();
				if ( this.m_bResidueCodeCollection )
					this.m_setResidueCodes.addAll( t_oExport.getUniqueResidueCodes() );

			} catch ( SugarImporterException e ) {
				t_strMessage = "There is an error in GlycoCT importer";
				if ( e.getErrorText() != null )
					t_strMessage = e.getErrorText();
			} catch ( GlycoVisitorException e ) {
				t_strMessage = e.getErrorMessage();
				this.m_mapIDToValidationErrorLog.put( ID, t_oExport.getValidationErrorLog().toString() );
			} catch (WURCSException e) {
				t_strMessage = e.getErrorMessage();
			}
			if ( !t_strMessage.equals("") )
				this.m_mapIDToMessage.put(ID, t_strMessage);

			// Set result
			if( !t_strWURCS.equals("") )
				this.m_mapIDToWURCS.put(ID, t_strWURCS);
		}

	}
}
