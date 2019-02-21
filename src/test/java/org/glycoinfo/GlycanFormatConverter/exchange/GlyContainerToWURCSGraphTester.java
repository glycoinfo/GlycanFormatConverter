package org.glycoinfo.GlycanFormatConverter.exchange;

import java.util.ArrayList;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph.GlyContainerToWURCSGraph;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;

public class GlyContainerToWURCSGraphTester {

	public static void main(String[] args) throws WURCSException {

		ArrayList<String> samples = new ArrayList<String>();
		/*
		samples.add("α-D-Neup5Ac-(2→");
		samples.add("β-4-deoxy-D-lyxHexp-(1→");
		samples.add("α-L-QuipNAc-(1→");
		samples.add("α-D-Glcp3,6N24S-(1→");
		samples.add("L-gro-α-D-manHepp6/7P-(1→");
		samples.add("L-gro-α-D-manHepp6,7P-(1→");
		samples.add("aldehyde-D-Gro-(3→");
		samples.add("?-Gro-ol-(1→");
		
		samples.add("α-2,4:3,6-Anhydro-D-Glcp-(1→");
		samples.add("L-gro-α-D-manHepp6,7:3,4:2P3-(1→");
		samples.add("α-3,9-dideoxy-D-koNonp5N7NAc8Ac-(2→");
		samples.add("β-3,4-Anhydro-3,4,7-trideoxy-L-lyxHepp2,6N2-(1→");

		samples.add("α-D-Neup5Ac-(2→8:1-9)-α-D-Neup5Ac-(2→");
		samples.add("α-L-Rhap-(2-Suc-3:1→4)-β-D-Glcp-(1→");

		samples.add("α-D-Manp-(1→3)[α-D-Manp-(1→3)[α-D-Manp-(1→6)]-α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)-?-D-GlcNAc-(?→");
		samples.add("α-D-Neup5Ac-(2→6)-β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)-α-D-Manp-(1→3)[α-D-Manp-(1→3)-α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)[α-L-Fucp-(1→6)]-β-D-GlcpNAc-(1→");
		samples.add("[α-D-Glcp-(1→4)-α-D-Glcp-(1→6)]-4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→");
		
		samples.add("4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→");
		samples.add("4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→");
		
		samples.add("α-D-Neup5Ac-(2→6)=3$,α-D-Neup5Ac-(2→3)=2$,α-D-Neup5Ac-(2→3)=1$,3$|2$|1$β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)[3$|2$|1$β-D-Galp-(1→4)-β-D-GlcpNAc-(1→4)]-α-D-Manp-(1→3)[3$|2$|1$β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)-α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)[α-D-Fucp-(1→6)]-β-D-GlcpNAc-(1→");
		samples.add("?-D-Neup5Gc-(2→3)=1$,1$?-D-Galp-(1→3)[1$?-D-Galp-(1→4)-1$?-D-GlcpNAc-(1→6)]-1$?-D-GalpNAc-(1→");
		samples.add("α-D-Neup5Ac-(2→6)-β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)=1$,1$α-D-Manp-(1→3)[1$α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)[α-D-Fucp-(1→6)]-β-D-GlcpNAc-(1→");
		samples.add("?P=1$,1$β-D-Galp-(1→4)-1$β-D-GlcpNAc-(1→2)-1$α-D-Manp-(1→3)[1$α-D-Manp-(1→3)[1$α-D-Manp-(1→6)]-1$α-D-Manp-(1→6)]-1$β-D-Manp-(1→4)-1$β-D-GlcpNAc-(1→4)-1$?-D-GlcpNAc-(1→");
		*/
		//samples.add("4)-[4)-α-D-Glcp-(1→]n-α-D-Glcp-(1→4)-α-D-GlcpN-(1→");
		//samples.add("[α-D-Glcp-(1→6)-α-D-Glcp-(1→4)-α-D-Glcp-(1→6)]-4)-α-D-Glcp-(1→4)-[4)-α-D-Glcp-(1→]n-α-D-Glcp-(1→");
		//samples.add("[α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→6)]-4)-α-D-Glcp-(1→4)-[4)-α-D-Glcp-(1→]n[α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→6)]-α-D-Glcp-(1→4)-[4)-α-D-Glcp-(1→]n-α-D-Glcp-(1→");
/*
		samples.add("α-D-Glcp-(1→4)-[4)-α-D-GlcpNAc-(1→]n");
		samples.add("[α-D-Glcp-(1→6)]-[4)-α-D-GlcpNAc-(1→]n");
		samples.add("α-D-GlcpN-(1→4)-α-L-IdopA-(1→4)-[4)-α-D-GlcpN-(1→4)-α-L-IdopA-(1→]n-α-D-GlcpN-(1→");
		samples.add("α-L-Rhap-(1→3)-β-D-GlcpNAc-(1→2)[β-D-Glcp-(1→6)]-[-P-3)-β-D-Glcp-(1→1)-aldehyde-D-Gro-(3→]n");
		samples.add("α-L-Glcp-(1→2)[?-Gro-ol-(1-P→3)][[4)-β-D-Glcp-(1→4)]-β-D-Galp-(1→4)-α-D-Glcp-(1→3)-α-L-Rhap-(1→]n");
		samples.add("[4)-α-D-Glcp-(1→]n[α-D-Glcp-(1→6)]-α-D-Glcp-(1→");
		samples.add("α-D-Manp-(1→3)[α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)-?-D-GlcNAc-(?→");
	*/

		//samples.add("α-D-Neup5Ac-(2→3)[β-D-Galp-(1→3)-β-D-GalpNAc-(1→4)]-β-D-Galp-(1→4)-β-D-Glcp-(1→");
		//samples.add("β-D-GlcpNAc-(1→2)-α-D-Manp-(1→3)[β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)-α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)[α-D-Fucp-(1→6)]-α-D-GlcpNAc-(1→");
		//samples.add("β-D-GlcpNAc-(1→4)-β-D-GlcpNAc-(1→3)[β-D-Galp-(1→4)][β-D-Galp-(1→4)[α-D-Fucp-(1→3)]-β-D-GlcpNAc-(1→6)]-β-D-Galp-(1→4)-β-D-Glcp-(1→");
		//samples.add("α-D-Neup5Ac-(2→6)=1$,1$β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)-α-D-Manp-(1→3)[1$β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)-α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-Glcp-(1→");
		//samples.add("[3)-β-D-Galp-(1→3)[α-D-Glcp-(1→6)-α-D-Glcp-(1→4)]-β-D-GlcpA-(1→2)-α-D-Rhap-(1→3)-α-D-Rhap-(1→3)-α-D-Rhap-(1→]n");
		//samples.add("?-D-Fucp-(1→?)=2$,?-D-GalpNAc-(1→?)=1$,?-D-Galp-(1→?)[?-D-Fucp-(1→?)]-?-D-GalpNAc-(1→?)-?-D-Galp-(1→?)-?-D-GlcpNAc-(1→?)[?-D-Fucp-(1→?)-?-D-Galp-(1→?)]-?-D-Rha-(?→");
		//samples.add("?-D-Fucp-(1→?)=2$,?-D-GalpNAc-(1→?)=1$,2$|1$?-D-Galp-(1→?)[2$|1$?-D-Fucp-(1→?)]-2$|1$?-D-GalpNAc-(1→?)-2$|1$?-D-Galp-(1→?)-2$|1$?-D-GlcpNAc-(1→?)[2$|1$?-D-Fucp-(1→?)-2$|1$?-D-Galp-(1→?)]-2$|1$?-D-Rha-(?→");
		samples.add("α-D-Neup5Ac-(2→3)=2$,α-D-Neup5Ac-(2→6)=1$,2$|1$β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)-α-D-Manp-(1→3)[2$|1$β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)-α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-Glcp-(1→");

		StringBuilder results = new StringBuilder();

		for (String iupac : samples) {
			try {
				IUPACExtendedImporter ii = new IUPACExtendedImporter();
				GlyContainer parsed = ii.start(iupac);
				
				IUPACExporter ie = new IUPACExporter();
				ie.start(parsed);
				
				GlyContainerToWURCSGraph gc2wg = new GlyContainerToWURCSGraph();
				gc2wg.start(parsed);		
				WURCSFactory factory = new WURCSFactory(gc2wg.getGraph());

				//results.append(iupac + "\n");
				//results.append(ie.getExtendedWithGreek() + "\n");
				
				if (!iupac.equals(ie.getExtendedWithGreek())) {
					results.append(iupac + "\n");
					results.append(ie.getExtendedWithGreek() + "\n");
				}
				results.append(factory.getWURCS() + "\n\n");
			} catch (ConverterExchangeException e) {
				e.getMessage();
			} catch (GlycanException e) {
				e.getMessage();
			} catch (GlyCoImporterException e) {
				e.getMessage();
			}

		}
		System.out.println(results);
	}

}
