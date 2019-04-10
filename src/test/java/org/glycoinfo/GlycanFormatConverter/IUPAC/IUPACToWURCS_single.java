package org.glycoinfo.GlycanFormatConverter.IUPAC;

import java.util.ArrayList;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoExporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.junit.Test;

public class IUPACToWURCS_single {

	//@Test
	//public void IUPACToWURCS () throws GlycanException, WURCSException, GlyCoExporterException {
	public static void main (String[] args) {

		ArrayList<String> sets = new ArrayList<>();
		//sets.add("?-D-Neup5Gc-(2→?)-?-D-Galp-(1→?)-?-D-GlcpNAc-(1→?)-?-D-Manp-(1→?)[?-D-Neup5Ac-(2→?)-?-D-Galp-(1→?)-?-D-GlcpNAc-(1→?)-?-D-Manp-(1→?)]-?-D-Manp-(1→?)-?-D-GlcpNAc-(1→?)-?-D-GlcpNAc-(1→");
		//sets.add("(?-D-Neu5Ac-(?→)3,(?-D-Neu5Gc-(?→)1,(?-D-GlcNAc-(?→)6,(?-D-Man-(?→)3,(?-D-Gal-(?→)4");
		//sets.add("(6-deoxy-?-HexNAc-ol)1,(?-?-HexA2,3NAc2-(?->)1,(?-?-HexA2NAc3NAm-(?->)1");
		//sets.add("{?-?-Hexp??-(1→}1,{?-D-GlcpNAc-(1→}2,{?-?-Hexp-(1→}6");
		//sets.add("β-D-Galp-(1→3)-β-D-GalpNAc-(1→4)-?-D-GalNAc-(3-P→1)-D-Gro-ol");
		//sets.add("α-2,3-Anhydro-D-Eryf2,2CMeOH2-(1→");
		//sets.add("?-D-GlcpN2Gc-(1→");
		//sets.add("?-D-GlcpN2S-(1→");
		//sets.add("?-D-Neu5Ac-(?→");
		//sets.add("?-D-Neu5Gc-(?→");
		//sets.add("?-D-GlcpNAc-(1→");
		sets.add("α-D-Apif-(1→");

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
