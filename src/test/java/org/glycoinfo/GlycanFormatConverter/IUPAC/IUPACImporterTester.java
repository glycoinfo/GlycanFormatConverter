package org.glycoinfo.GlycanFormatConverter.IUPAC;

import java.util.ArrayList;

import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanRepeatModification;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanUndefinedUnit;
import org.glycoinfo.GlycanFormatconverter.Glycan.Linkage;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.util.similarity.NodeSimilarity;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;

public class IUPACImporterTester {

	public static void main(String[] args) throws GlycanException, GlyCoImporterException, ConverterExchangeException {
		
		ArrayList<String> samples = new ArrayList<String>();

		//samples.add("α-D-Neup5Ac-(2→");
		//samples.add("β-4-deoxy-D-lyxHexp-(1→");
		//samples.add("α-L-QuipNAc-(1→");
		//samples.add("α-D-Glcp3,6N24S-(1→");
		//samples.add("L-gro-α-D-manHepp6/7P-(1→");
		//samples.add("L-gro-α-D-manHepp6,7P-(1→");
		//samples.add("aldehyde-D-Gro-(3→");
		//samples.add("?-Gro-ol-(1→");
		//samples.add("α-2,4:3,6-Anhydro-D-Glcp-(1→");
		//samples.add("L-gro-α-D-manHepp6,7:3,4:2P3-(1→");
		//samples.add("α-3,9-dideoxy-D-koNonp5N7NAc8Ac-(2→");
		//samples.add("β-3,4-Anhydro-3,4,7-trideoxy-L-lyxHepp2,6N2-(1→");

		//samples.add("α-D-Neup5Ac-(2→8:1-9)-α-D-Neup5Ac-(2→");
		//samples.add("α-L-Rhap-(2-Suc-3:1→4)-β-D-Glcp-(1→");

		//samples.add("α-D-Manp-(1→3)[α-D-Manp-(1→3)[α-D-Manp-(1→6)]-α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)-?-D-GlcNAc-(?→");
		//samples.add("α-D-Neup5Ac-(2→6)-β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)-α-D-Manp-(1→3)[α-D-Manp-(1→3)-α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)[α-L-Fucp-(1→6)]-β-D-GlcpNAc-(1→");
		//samples.add("[α-D-Glcp-(1→4)-α-D-Glcp-(1→6)]-4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→");

		//samples.add("4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→4)-α-D-Glcp2,3Me26N-(1→");
		//samples.add("4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→4)-α-D-Glcp2,6Me2-(1→");

		//samples.add("α-D-Neup5Ac-(2→6)=3$,α-D-Neup5Ac-(2→3)=2$,α-D-Neup5Ac-(2→3)=1$,3$|2$|1$β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)[3$|2$|1$β-D-Galp-(1→4)-β-D-GlcpNAc-(1→4)]-α-D-Manp-(1→3)[3$|2$|1$β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)-α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)[α-D-Fucp-(1→6)]-β-D-GlcpNAc-(1→");
		//samples.add("?-D-Neup5Gc-(2→3)=1$,1$?-D-Galp-(1→3)[1$?-D-Galp-(1→4)-1$?-D-GlcpNAc-(1→6)]-1$?-D-GalpNAc-(1→");
		//samples.add("α-D-Neup5Ac-(2→6)-β-D-Galp-(1→4)-β-D-GlcpNAc-(1→2)=1$,1$α-D-Manp-(1→3)[1$α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)[α-D-Fucp-(1→6)]-β-D-GlcpNAc-(1→");
		//samples.add("?P=1$,1$β-D-Galp-(1→4)-1$β-D-GlcpNAc-(1→2)-1$α-D-Manp-(1→3)[1$α-D-Manp-(1→3)[1$α-D-Manp-(1→6)]-1$α-D-Manp-(1→6)]-1$β-D-Manp-(1→4)-1$β-D-GlcpNAc-(1→4)-1$?-D-GlcpNAc-(1→");

		//samples.add("4)-[4)-α-D-Glcp-(1→]n-α-D-Glcp-(1→4)-α-D-GlcpN-(1→");
		//samples.add("[α-D-Glcp-(1→6)-α-D-Glcp-(1→4)-α-D-Glcp-(1→6)]-4)-α-D-Glcp-(1→4)-[4)-α-D-Glcp-(1→]n-α-D-Glcp-(1→");
		//samples.add("[α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→6)]-4)-α-D-Glcp-(1→4)-[4)-α-D-Glcp-(1→]n[α-D-Glcp-(1→4)-α-D-Glcp-(1→4)-α-D-Glcp-(1→6)]-α-D-Glcp-(1→4)-[4)-α-D-Glcp-(1→]n-α-D-Glcp-(1→");

		//samples.add("α-D-Glcp-(1→4)-[4)-α-D-GlcpNAc-(1→]n");
		//samples.add("[α-D-Glcp-(1→6)]-[4)-α-D-GlcpNAc-(1→]n");
		//samples.add("α-D-GlcpN-(1→4)-α-L-IdopA-(1→4)-[4)-α-D-GlcpN-(1→4)-α-L-IdopA-(1→]n-α-D-GlcpN-(1→");
		//samples.add("α-L-Rhap-(1→3)-β-D-GlcpNAc-(1→2)[β-D-Glcp-(1→6)]-[-P-3)-β-D-Glcp-(1→1)-aldehyde-D-Gro-(3→]n");
		//samples.add("α-L-Glcp-(1→2)[?-Gro-ol-(1-P→3)][[4)-β-D-Glcp-(1→4)]-β-D-Galp-(1→4)-α-D-Glcp-(1→3)-α-L-Rhap-(1→]n");
		//samples.add("[4)-α-D-Glcp-(1→]n[α-D-Glcp-(1→6)]-α-D-Glcp-(1→");
		//samples.add("α-D-Manp-(1→3)[α-D-Manp-(1→6)]-β-D-Manp-(1→4)-β-D-GlcpNAc-(1→4)-?-D-GlcNAc-(?→");
		samples.add("?-D-Neup5Gc-(2→?)-?-D-Galp-(1→?)-?-D-GlcpNAc-(1→?)-?-D-Manp-(1→?)[?-D-Neup5Ac-(2→?)-?-D-Galp-(1→?)-?-D-GlcpNAc-(1→?)-?-D-Manp-(1→?)]-?-D-Manp-(1→?)-?-D-GlcpNAc-(1→?)-?-D-GlcpNAc-(1→");
		
		StringBuilder results = new StringBuilder();
		
		for (String iupac : samples) {
			IUPACExtendedImporter ii = new IUPACExtendedImporter();
			
			GlyContainer parsed = ii.start(iupac);
			
			openStatus(parsed);
			
			IUPACExporter ie = new IUPACExporter();
			ie.start(parsed);
			results.append(iupac + "\n");
			results.append(ie.getExtendedWithGreek() + "\n\n");
		}

		System.out.println(results);
	}

	public static void openStatus (GlyContainer _glyCo) throws GlycanException {
		System.out.println("count " + _glyCo.getAllNodes().size());

		NodeSimilarity gu = new NodeSimilarity();
		
		for(Node current : gu.sortAllNode(_glyCo.getRootNodes().get(0))) {
			System.out.println("current " + ((Monosaccharide) current).getStereos());
			
			System.out.println("child side");	
			for(Edge edge : current.getChildEdges()) {
				for(Linkage lin : edge.getGlycosidicLinkages()) {
					System.out.println(lin.getParentLinkages() + " " + lin.getChildLinkages());
				}

				if(edge.getChild() != null && edge.getParent() != null) {
					Monosaccharide c = (Monosaccharide) edge.getChild();
					Monosaccharide p = (Monosaccharide) edge.getParent();
					System.out.println("child : " + c.getStereos() + " / parent : " + p.getStereos());
				}
				if(edge.getSubstituent() != null) {
					if(edge.getSubstituent() instanceof GlycanRepeatModification) {
						System.out.println("end rep");
						GlycanRepeatModification g = (GlycanRepeatModification) edge.getSubstituent();
						System.out.println(g.getMaxRepeatCount() + " " + g.getMinRepeatCount());
					}
					if(edge.getSubstituent() instanceof Substituent) {
						Substituent s = (Substituent) edge.getSubstituent();
						StringBuilder subste = new StringBuilder();
						if(s.getSubstituent() != null) subste.append(s.getSubstituent());
						if(s.getFirstPosition() != null) subste.append(" " + s.getFirstPosition().getParentLinkages());
						if(s.getSecondPosition() != null) subste.append(" " + s.getSecondPosition().getParentLinkages());
						System.out.println(subste);
					}
				}
			}
			
			System.out.println("++++++++");
			
			System.out.println("parent side");
			if(current.getParentEdge() != null) {
				Edge edge = current.getParentEdge();
				for(Linkage lin : edge.getGlycosidicLinkages()) {
					System.out.println(lin.getParentLinkages() + " " + lin.getChildLinkages());
				}
				if(edge.getChild() != null && edge.getParent() != null) {
					Monosaccharide c = (Monosaccharide) current.getParentEdge().getChild();
					Monosaccharide p = (Monosaccharide) current.getParentEdge().getParent();
					System.out.println("child : " + c.getStereos() + " / parent : " + p.getStereos());
				}
				if(edge.getSubstituent() != null) {
					if(edge.getSubstituent() instanceof GlycanRepeatModification) {
						System.out.println("start rep");
						GlycanRepeatModification g = (GlycanRepeatModification) edge.getSubstituent();
						System.out.println(g.getMaxRepeatCount() + " " + g.getMinRepeatCount());
					}
					if(edge.getSubstituent() instanceof Substituent) {
						Substituent s = (Substituent) edge.getSubstituent();
						StringBuilder subste = new StringBuilder();
						if(s.getSubstituent() != null) subste.append(s.getSubstituent());
						if(s.getFirstPosition() != null) subste.append(" " + s.getFirstPosition().getParentLinkages());
						if(s.getSecondPosition() != null) subste.append(" " + s.getSecondPosition().getParentLinkages());
						System.out.println(subste);
					}
				}
			}			
			
			System.out.println("--------\n");
		}
							
		for(GlycanUndefinedUnit und : _glyCo.getUndefinedUnit()) {
			for(Node child : gu.sortAllNode(und.getRootNodes().get(0))) {
				if (child instanceof Substituent) {
					System.out.println(((Substituent) child).getSubstituent());
				}else {
					System.out.println(((Monosaccharide) child).getStereos());

					for(Node node : child.getChildNodes()) {
						System.out.println("child " + ((Monosaccharide) node).getStereos());
					}
					//for(Linkage lin : child.getParentEdge().getGlycosidicLinkages()) {
					//	System.out.println("parent linkage site : " + lin.getChildLinkages() + " " + lin.getParentLinkages());
					//}
				}	
			}		

			System.out.println(und.getConnection().getGlycosidicLinkages().get(0).getChildLinkages() +" " + und.getConnection().getGlycosidicLinkages().get(0).getParentLinkages());

			for(Node parent : und.getParents()) {
				System.out.println(((Monosaccharide) parent).getStereos());
			}

			System.out.println("");
		}
	}
}
