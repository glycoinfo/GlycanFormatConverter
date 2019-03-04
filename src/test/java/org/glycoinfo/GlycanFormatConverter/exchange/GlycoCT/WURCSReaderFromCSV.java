package org.glycoinfo.GlycanFormatConverter.exchange.GlycoCT;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class WURCSReaderFromCSV {

	private List<String> m_lTargetIDs;

	public void addTargetID(String a_strTargetID) {
		if ( this.m_lTargetIDs == null )
			this.m_lTargetIDs = new ArrayList<>();
		if ( !this.m_lTargetIDs.contains(a_strTargetID) )
			this.m_lTargetIDs.add(a_strTargetID);
	}

	public void processCSVFile(String _strFile) {
		CSVReader t_reader;
		try {
			t_reader = new CSVReader(_strFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		Set<String> t_setUniqueIDs = new HashSet<>();
		Set<String> t_setUniqueWURCS = new HashSet<>();
		Set<String> t_setUniqueGlycoCT = new HashSet<>();
		String[] wurcs;
		while ( (wurcs = t_reader.readNext() ) != null ) {
			if ( wurcs.length < 2 )
				continue;
			String t_strID      = wurcs[0];
			String t_strWURCS   = wurcs[1];
			String t_strGlycoCT = (wurcs.length > 2)? wurcs[2] : "";
			if ( !t_strGlycoCT.isEmpty() && !t_strGlycoCT.endsWith("\n") )
				t_strGlycoCT += "\n";

			// Skip if no WURCS
			if ( !t_strWURCS.startsWith("WURCS") )
				continue;

			if ( this.m_lTargetIDs != null && !this.m_lTargetIDs.contains(t_strID) )
				continue;

			t_setUniqueIDs.add(t_strID);

			String t_strIDWURCS = t_strID+":"+t_strWURCS;
			if ( t_setUniqueWURCS.contains(t_strIDWURCS) )
				continue;
			t_setUniqueWURCS.add(t_strIDWURCS);

			if ( !t_strGlycoCT.isEmpty() && !t_setUniqueGlycoCT.contains(t_strGlycoCT) ) {
				t_setUniqueGlycoCT.add(t_strGlycoCT);
				processGlycoCT( t_strID, t_strGlycoCT );
			}

			processWURCS( t_strID, t_strWURCS );

			processAll( t_strID, t_strWURCS, t_strGlycoCT );
		}

		System.out.println("Total # of entries:"+t_setUniqueIDs.size() );
		System.out.println("Total # of unique WURCSs:"+t_setUniqueWURCS.size() );
		System.out.println("Total # of unique GlycoCTs:"+t_setUniqueGlycoCT.size() );
		System.out.println();

		showResults();
	}

	protected void printIDs(List<String> a_lIDs, String t_strHeader) {
		String t_strLine = "";
		for ( String t_strID : a_lIDs ) {
			if ( !t_strLine.isEmpty() )
				t_strLine += ", ";
			t_strLine += t_strID;
			if ( t_strLine.length() < 80 )
				continue;
			System.out.println(t_strHeader+t_strLine);
			t_strLine = "";
		}
		System.out.println(t_strHeader+t_strLine);
	}

	protected void printIDMap(Map<String, List<String>> a_mapIndexToIDs, String t_strHeader, String t_strIDHeader) {
		for ( String t_str : a_mapIndexToIDs.keySet() ) {
			List<String> t_lIDs = a_mapIndexToIDs.get(t_str);
			System.out.println(t_strHeader+"-"+t_str+": "+t_lIDs.size());
			this.printIDs(t_lIDs, t_strIDHeader);
		}

	}

	protected void printIDMapOfMap(Map<String, Map<String, List<String>>> t_mapOfMap, String t_strHeader, String t_strIDHeader) {
		for ( String t_str1 : t_mapOfMap.keySet() ) {
			Map<String, List<String>> t_mapStr2ToID = t_mapOfMap.get(t_str1);
			System.out.println(t_strHeader+"-"+t_str1+": "+this.countElements(t_mapStr2ToID));
			this.printIDMap(t_mapStr2ToID, t_strHeader+"-", t_strIDHeader);
		}

	}

	protected int countElements(Map<String, List<String>> a_map) {
		int t_nErrors = 0;
		for ( String t_strError : a_map.keySet() )
			t_nErrors += a_map.get(t_strError).size();
		return t_nErrors;

	}

	protected int countElementsOfElements(Map<String, Map<String, List<String>>> a_map) {
		int t_nCount = 0;
		for ( String t_str : a_map.keySet() )
			t_nCount += this.countElements(a_map.get(t_str));
		return t_nCount;
	}

	abstract protected void processGlycoCT(String t_strID, String t_strGlycoCT);
	abstract protected void processWURCS(String a_strID, String a_strWURCS);
	abstract protected void processAll(String a_strID, String a_strWURCS, String a_strGlycoCT);
	abstract protected void showResults();

}
