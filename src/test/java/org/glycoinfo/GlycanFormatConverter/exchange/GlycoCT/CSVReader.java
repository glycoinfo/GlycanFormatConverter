package org.glycoinfo.GlycanFormatConverter.exchange.GlycoCT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class CSVReader {

	private BufferedReader m_br;

	public CSVReader(String filename) throws FileNotFoundException {
		File f = new File(filename);
		InputStreamReader osr = new InputStreamReader(new FileInputStream(f));
		this.m_br = new BufferedReader(osr);
	}

	public String[] readNext() {
		try {
			String line = "";
			String line0;
			while ((line0 = this.m_br.readLine()) != null) {
				if ( !line.isEmpty() )
					line += "\n";
				line += line0;
				if ( !line0.endsWith("\"") && !line0.endsWith(",") )
					continue;
				String[] t_strSplited = line.trim().split("\",");
				// Trim first '"'
				for ( int i=0; i<t_strSplited.length; i++ )
					t_strSplited[i] = t_strSplited[i].substring(1);
				// Trim last '"' for last element
				String t_strLast = t_strSplited[t_strSplited.length-1];
				if ( t_strLast.endsWith("\"") )
					t_strSplited[t_strSplited.length-1]
							= t_strLast.substring(0, t_strLast.length()-1);
				line = "";
				return t_strSplited;
			}
			this.m_br.close();
		} catch (IOException e) {
			System.err.println(e);
		}
		return null;
	}

}
