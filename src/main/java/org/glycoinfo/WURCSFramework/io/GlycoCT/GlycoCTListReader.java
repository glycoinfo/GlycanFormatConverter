package org.glycoinfo.WURCSFramework.io.GlycoCT;

import org.glycoinfo.WURCSFramework.util.FileIOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlycoCTListReader {

	private GlycoCTListReader(){};

	public static TreeMap<String, String> readFromFile(String a_strFilePath) throws Exception {
		TreeMap<String, String> t_mapGlycoCTList = new TreeMap<String, String>();

		String line = null;
		BufferedReader br  = FileIOUtils.openTextFileR( a_strFilePath );

		String t_strCode = "";
		String ID ="";
		int count = 0;
		while ( (line = br.readLine())!=null ) {
//			System.out.println(line);
			Matcher mat = Pattern.compile("^(.*)ID:(.*)$").matcher(line);
			if(mat.find()) {
				ID = mat.group(2);
				// Zero fill if ID is integer
/*
				if ( ID.matches("^\\d+$") ) {
					int id = Integer.parseInt( ID );
					ID = String.format("%1$05d", id);
				}
*/
//				System.out.printf("%s\n", ID);
				count++;
				continue;
			}

			if ( line.length() == 0 ) {
				if ( t_strCode == "" ) continue;

				t_mapGlycoCTList.put(ID, t_strCode);
				t_strCode = "";
//				if (count == 100) break;
				continue;
			}
			t_strCode += line + "\n";
		}
		br.close();

		return t_mapGlycoCTList;
	}

	public static TreeMap<String, String> readFromDir(String a_strDirPath) throws Exception {
		TreeMap<String, String> t_mapGlycoCTList = new TreeMap<String, String>();

		File dir = new File(a_strDirPath);
		File[] files = dir.listFiles();
		ArrayList<String> t_aFileNames = new ArrayList<String>();
		for ( File file : files ) {
			t_aFileNames.add( file.getName() );
		}
		int nFile = files.length;

		String line = null;

		Integer id=0;
		int count = 0;
		while ( count < nFile ) {
			id++;
			if (! t_aFileNames.contains( id.toString() + ".txt" ) )
				continue;
//			System.out.println(id);
			count++;

			String t_strCode = "";
			String t_strFileNameR = a_strDirPath +"\\"+ id.toString() + ".txt";
			BufferedReader br = FileIOUtils.openTextFileR( t_strFileNameR );
			while ( (line = br.readLine())!=null ) {
//				System.out.println(line);

				if ( line.length() == 0 ) continue;
				t_strCode += line+"\n";
			}
			br.close();
			t_mapGlycoCTList.put(String.format("%1$05d", id), t_strCode);
		}

		return t_mapGlycoCTList;
	}
}
