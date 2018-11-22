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
		//sets.add("{?-?-Hexp??-(1→}1,{?-D-GlcpNAc-(1→}2,{?-?-Hexp-(1→}6");
//		sets.add("β-D-Glcp-(1-S→4)-β-D-Glcp-(1→");
//		sets.add("?P=1$,1$β-D-Galp-(1→4)-1$β-D-GlcpNAc-(1→2)-1$α-D-Manp-(1→3)[1$α-D-Manp-(1→3)[1$α-D-Manp-(1→6)]-1$α-D-Manp-(1→6)]-1$β-D-Manp-(1→4)-1$β-D-GlcpNAc-(1→4)-1$?-D-GlcpNAc-(1→");
//		sets.add("{?-?-HexNAc-(?→}5,{?-?-Hexp??-(1→}2,{?-?-Hexp-(1→}4");
//		sets.add("α-D-Neup5Ac-(2→8:1→9)-α-D-Neup5Ac-(2→");
//		sets.add("α-D-ManpNAc-(1→4)[β-D-GlcpNAc-(1→30%3)-[4)-β-D-ManpA2NAc-(1→4)-β-D-GlcpNAc-(1→6)]-α-D-GlcpNAc-(1→]n");
//		sets.add("α-D-GlcpNAc-(1→2)-D-Gro-ol-(3-P→1:1-P→3)[α-D-Glcp-(1→2)[α-D-Glcp-(1→2)-[-P-3)-D-Gro-ol-(1-P→3)]-D-Gro-ol-(1-P→3)]-D-Gro-ol");
//		sets.add("[3)-α-D-Glcp-(1→]n");
		//sets.add("[3)-β-D-Galp-(1→3)[α-L-Araf-(1→3)-α-L-Araf-(1→6)-β-D-Galp-(1→6)]-[3)-β-D-Galp-(1→]n:1→3)[β-D-Galp-(1→3)-β-D-Galp-(1→3)-β-D-Galp-(1→6)]-[3)-β-D-Galp-(1→]n:1→3)[β-D-Galp-(1→6)]-[3)-β-D-Galp-(1→]n:1→4)-[4)-β-D-Xylp-(1→]n:1→]n");
		sets.add("α-L-thrHex?A2S4en-(1→3)-?-D-GalpNAc-(1→");
		
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
