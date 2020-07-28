package org.glycoinfo.GlycanFormatConverter.WURCS;

import org.glycoinfo.GlycanFormatConverter.util.fileHandler;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed.IUPACCondensedImporter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.extended.IUPACExtendedImporter;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.graph.WURCSGraphNormalizer;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class groupTestForWURCStoIUPAC {

    private String filePath = "src/test/resources/";
    private String outPath = "src/test/resources/result/";
    private String error = "";

    @Test
    public void simple () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_simple.tsv");
        fileHandler.writeFile(outPath + "WURCS_simpleIUPAC", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void fragments () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_fragments.tsv");
        fileHandler.writeFile(outPath + "WURCS_fragmentsIUPAC", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void repeats () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_repeats.tsv");
        fileHandler.writeFile(outPath + "WURCS_repeatsIUPAC", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void bridge () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_bridgeMod.tsv");
        fileHandler.writeFile(outPath + "WURCS_bridgeModIUPAC", this.makeMap(fileData), ".tsv");
    }

    //WURCS=2.0/3,4,3/[u212h_2|3*OPO/3O/3=O][a2112h-1b_1-5][a21EEA-1a_1-4]/1-2-2-3/a4-b1_b3-c1_c3-d1
    //WURCS=2.0/3,4,3/[u212h_2|3*OPO/3O/3=O][a2112h-1b_1-5][a21EEA-1a_1-5]/1-2-2-3/a4-b1_b3-c1_c3-d1

    @Test
    public void errorCase1_simple () {
        String wurcs = "WURCS=2.0/4,7,7/[hxh][h2h][a2122h-1a_1-5_2*NCC/3=O][a2122h-1a_1-5]/1-2-3-2-2-2-4/a1-b1*OPO*/3O/3=O_b2-c1_b3-d1*OPO*/3O/3=O_d3-e1*OPO*/3O/3=O_e3-f1*OPO*/3O/3=O_f2-g1_a3-f3*OPO*/3O/3=O~n";
        String condensed = this.toCondensed(wurcs);
        String extended = this.toExtended(wurcs);

        //System.out.println(condensed);
        System.out.println(extended);
        //System.out.println(this.wurcsExporterFromCondensed(condensed));
        System.out.println(this.wurcsExporterFromExtended(extended));
    }

    @Test
    public void errorCase2_simple () {
        String wurcs = "WURCS=2.0/3,4,3/[u2122h][a2112h-1x_1-5][a2122h-1x_1-5]/1-2-3-2/a?-b1_a?-d1_b?-c1";
        String condensed = this.toCondensed(wurcs);
        String extended = this.toExtended(wurcs);

        System.out.println(condensed);
        System.out.println(extended);
        System.out.println(this.wurcsExporterFromCondensed(condensed));
        System.out.println(this.wurcsExporterFromExtended(extended));
    }

    @Test
    public void errorCase9_simple () {
        String wurcs = "WURCS=2.0/3,3,2/[a2122h-1x_1-5][ha122h-2x_2-5][u211h]/1-2-3/a1-b2_b?-c?*OPO*/3O/3=O";
        String condensed = this.toCondensed(wurcs);
        String extended = this.toExtended(wurcs);

        System.out.println(condensed);
        //System.out.println(extended);
        System.out.println(this.wurcsExporterFromCondensed(condensed));
        //System.out.println(this.wurcsExporterFromExtended(extended));
    }

    @Test
    public void errorCase10_simple () {
        String wurcs = "WURCS=2.0/3,3,2/[ha122h-2b_2-5][a2122h-1a_1-5][a2122h-1x_1-5]/1-2-3/a2-b1_b2-c2*OPO*/3O/3=O";
        String condensed = this.toCondensed(wurcs);
        String extended = this.toExtended(wurcs);

        System.out.println(condensed);
        System.out.println(extended);
        System.out.println(this.wurcsExporterFromCondensed(condensed));
        System.out.println(this.wurcsExporterFromExtended(extended));
    }

    @Test
    public void errorCase14_simple () {
        String wurcs = "WURCS=2.0/1,1,0/[hU122h]/1/";
        String condensed = this.toCondensed(wurcs);
        String extended = this.toExtended(wurcs);

        System.out.println(condensed);
        System.out.println(extended);
        System.out.println(this.wurcsExporterFromCondensed(condensed));
        System.out.println(this.wurcsExporterFromExtended(extended));
    }

    @Test
    public void errorCase15_simple () {
        String wurcs = "WURCS=2.0/1,1,0/[c1122h_2-5]/1/";
        String condensed = this.toCondensed(wurcs);
        String extended = this.toExtended(wurcs);

        System.out.println(condensed);
        System.out.println(extended);
        System.out.println(this.wurcsExporterFromCondensed(condensed));
        System.out.println(this.wurcsExporterFromExtended(extended));
    }

    @Test
    public void errorCase16_simple () {
        String wurcs = "WURCS=2.0/1,1,0/[hO122h]/1/";
        String condensed = this.toCondensed(wurcs);
        String extended = this.toExtended(wurcs);

        System.out.println(condensed);
        System.out.println(extended);
        System.out.println(this.wurcsExporterFromCondensed(condensed));
        System.out.println(this.wurcsExporterFromExtended(extended));
    }

    @Test
    public void errorCase17_simple () {
        String wurcs = "WURCS=2.0/1,1,1/[<Q>-?a_5*OCC/3=O_8*OCC/3=O_9*OCC/3=O]/1/a1-a7~n";
        String extended = this.toExtended(wurcs);

        System.out.println(extended);
        System.out.println(this.wurcsExporterFromExtended(extended));
    }

    private String wurcsNormalize (String _wurcs) {
        try {
            WURCSFactory wf = new WURCSFactory(_wurcs);
            WURCSGraphNormalizer norm = new WURCSGraphNormalizer();
            WURCSGraph graph = wf.getGraph();
            norm.start(graph);

            wf = new WURCSFactory(graph);
            return wf.getWURCS();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String toShort (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toIUPAC(IUPACStyleDescriptor.SHORT);
        } catch (Exception e) {
            e.printStackTrace();
            this.error += e.getMessage();
            return e.getMessage();
        }
    }

    private String toCondensed (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toIUPAC(IUPACStyleDescriptor.CONDENSED);
        } catch (Exception e) {
            e.printStackTrace();
            this.error += e.getMessage();
            return e.getMessage();
        }
    }

    private String toExtended (String _wurcs) {
        try {
            WURCSImporter wi = new WURCSImporter();
            GlyContainer gc = wi.start(_wurcs);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toIUPAC(IUPACStyleDescriptor.GREEK);
        } catch (Exception e) {
            e.printStackTrace();
            this.error += e.getMessage();
            return e.getMessage();
        }
    }

    private String toCondensedFromCondensed (String _condensed) {
        try {
            IUPACCondensedImporter ici = new IUPACCondensedImporter();
            GlyContainer gc = ici.start(_condensed);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toIUPAC(IUPACStyleDescriptor.CONDENSED);
        } catch (Exception e) {
            e.printStackTrace();
            this.error += e.getMessage();
            return e.getMessage();
        }
    }

    private String toExtendedFromExtended (String _extended) {
        try {
            IUPACExtendedImporter iei = new IUPACExtendedImporter();
            GlyContainer gc = iei.start(_extended);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toIUPAC(IUPACStyleDescriptor.GREEK);
        } catch (Exception e) {
            e.printStackTrace();
            this.error += e.getMessage();
            return e.getMessage();
        }
    }

    private String wurcsExporterFromCondensed (String _condensed) {
        try {
            IUPACCondensedImporter ici = new IUPACCondensedImporter();
            ExporterEntrance ee = new ExporterEntrance(ici.start(_condensed));
            return ee.toWURCS();
        } catch (Exception e) {
            this.error += e.getMessage();
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private String wurcsExporterFromExtended (String _extended) {
        try {
            IUPACExtendedImporter iei = new IUPACExtendedImporter();
            ExporterEntrance ee = new ExporterEntrance(iei.start(_extended));
            return ee.toWURCS();
        } catch (Exception e) {
            this.error += e.getMessage();
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private HashMap<String, ArrayList<String>> makeMap (String _fileData) {
        HashMap<String, ArrayList<String>> resultMap = new HashMap<>();
        int count = 0;
        for (String item : _fileData.split("\\n")) {
            if (item.startsWith("?")) continue;
            String[] columns = item.split("\\t");
            ArrayList<String> values = new ArrayList<>();

            values.add(columns[1]);

            //to condensed
            //String condensed = this.toCondensed(columns[1]);
            //values.add(condensed);

            //to extended
            String extended = this.toExtended(columns[1]);
            values.add(extended);

            // to wurcs exporter from condensed
            //values.add(this.wurcsExporterFromCondensed(condensed));

            // to wurcs exporter from extended
            values.add(this.wurcsExporterFromExtended(extended));

            //#
            values.add(this.error);

            if (columns.length == 2) {
                resultMap.put(columns[0], values);
            } else {
                resultMap.put(String.valueOf(count), values);
            }

            count++;
            this.error = "";
        }

        return resultMap;
    }
}
