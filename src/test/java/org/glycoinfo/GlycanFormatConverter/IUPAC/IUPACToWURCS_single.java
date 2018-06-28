package org.glycoinfo.GlycanFormatConverter.IUPAC;

import java.util.ArrayList;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoExporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.junit.Test;

public class IUPACToWURCS_single {

	@Test
	public void IUPACToWURCS () throws GlycanException, WURCSException, GlyCoExporterException {//, SubsumptionException {

		ArrayList<String> sets = new ArrayList<>();
		//sets.add("?-D-Neup5Gc-(2→?)-?-D-Galp-(1→?)-?-D-GlcpNAc-(1→?)-?-D-Manp-(1→?)[?-D-Neup5Ac-(2→?)-?-D-Galp-(1→?)-?-D-GlcpNAc-(1→?)-?-D-Manp-(1→?)]-?-D-Manp-(1→?)-?-D-GlcpNAc-(1→?)-?-D-GlcpNAc-(1→");
		//sets.add("(?-D-Neu5Ac-(?→)3,(?-D-Neu5Gc-(?→)1,(?-D-GlcNAc-(?→)6,(?-D-Man-(?→)3,(?-D-Gal-(?→)4");
		//sets.add("(6-deoxy-?-HexNAc-ol)1,(?-?-HexA2,3NAc2-(?->)1,(?-?-HexA2NAc3NAm-(?->)1");
		sets.add("{?-?-Hexp??-(1→}1,{?-D-GlcpNAc-(1→}2,{?-?-Hexp-(1→}6");
		
		IUPACExtendedImporter iei = new IUPACExtendedImporter();
		
		for (String input : sets) {
			try {				
				ExporterEntrance ee = new ExporterEntrance(iei.start(input));  		
				String wurcs = ee.toWURCS();
				System.out.println(wurcs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
