package org.glycoinfo.GlycanFormatConverter.exchange;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.GlycanFormatconverter.util.similarity.NodeSimilarity;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Created by e13m5610 on 2017/05/24.
 */
public class IUPACToIUPAC {

    public static void main (String[] args) throws Exception {
        {

            String string_list = "src/test/resources/sampleWURCSforConvertTest";

            if(string_list == null || string_list.equals("")) throw new Exception();

            File file = new File(string_list);

            if(file.isFile()) {
                String input = "";
                LinkedHashMap<String, String> wurcsIndex = openString(string_list);

                StringBuilder results = new StringBuilder();
                StringBuilder errors = new StringBuilder();

                for(String key : wurcsIndex.keySet()) {
                    input = wurcsIndex.get(key);

                    try {
                        String ret1 = "";
                        String ret2 = "";

                        WURCSFactory wfin = new WURCSFactory(input);

                        /** WURCSSequence2 to GlyContainer */
                        WURCSGraphToGlyContainer wg2g = new WURCSGraphToGlyContainer();
                        wg2g.start(wfin.getGraph());
                        GlyContainer glycan = wg2g.getGlycan();

                        /** Convert to IUPAC */
                        ExporterEntrance ee1 = new ExporterEntrance(glycan);
                        ret1 = ee1.toIUPAC(IUPACStyleDescriptor.GREEK);

                        /** Import IUPAC */
                        IUPACExtendedImporter ii = new IUPACExtendedImporter();
                        GlyContainer iglycan = ii.start(ret1);

                        /** GlyContainerToIUPAC */
                        ExporterEntrance ee2 = new ExporterEntrance(iglycan);
                        ret2 = ee2.toIUPAC(IUPACStyleDescriptor.GREEK);

                        if (!ret1.equals(ret2)) {
                            results.append(input + "\n");
                            results.append(ret1 + "\n");
                            results.append(ret2 + "\n\n");
                        }
                    } catch (Exception e) {
                        errors.append(key + "\t" + input + "\n");
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                }

                System.out.println(results);
                System.out.println(errors);
            }
            else if(args.length > 0) {
            }else {
                throw new Exception("This file is not found !");
            }
        }
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
                for(Edge edge : current.getParentEdges()) {
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

    /**
     *
     * @param a_strFile
     * @return
     * @throws Exception
     */
    public static LinkedHashMap<String, String> openString(String a_strFile) throws Exception {
        try {
            return readWURCS(new BufferedReader(new FileReader(a_strFile)));
        }catch (IOException e) {
            throw new Exception();
        }
    }

    /**
     *
     * @param a_bfFile
     * @return
     * @throws IOException
     */
    public static LinkedHashMap<String, String> readWURCS(BufferedReader a_bfFile) throws IOException {
        String line = "";
        LinkedHashMap<String, String> wret = new LinkedHashMap<String, String>();
        wret.clear();

        while((line = a_bfFile.readLine()) != null) {
            line.trim();
            if(line.startsWith("%")) continue;
            if(line.indexOf("WURCS") != -1) {
                if(line.indexOf(" ") != -1) line = line.replace(" ", "\t");
                String[] IDandWURCS = line.split("\t");
                if (IDandWURCS.length == 2) {
                    wret.put(IDandWURCS[0].trim(), IDandWURCS[1]);
                }
            }
        }
        a_bfFile.close();

        return wret;
    }
}
