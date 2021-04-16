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

    private final String filePath = "src/test/resources/";
    private final String outPath = "src/test/resources/result/";
    private String error = "";

    @Test
    public void simple () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_simple.tsv");
        fileHandler.writeFile(outPath + "WI_simpleIUPAC", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void fragments () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_fragments.tsv");
        fileHandler.writeFile(outPath + "WI_fragmentsIUPAC", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void repeats () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_repeats.tsv");
        fileHandler.writeFile(outPath + "WI_repeatsIUPAC", this.makeMap(fileData), ".tsv");
    }

    @Test
    public void bridge () {
        String fileData = fileHandler.openTSV(filePath + "WURCS_bridgeMod.tsv");
        fileHandler.writeFile(outPath + "WI_bridgeModIUPAC", this.makeMap(fileData), ".tsv");
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

            String optWURCS = this.wurcsNormalize(columns[1]);
            values.add(optWURCS);

            //to condensed
            //String condensed = this.toCondensed(columns[1]);
            //values.add(condensed);

            //to extended
            String extended = this.toExtended(optWURCS);
            values.add(extended);

            // to wurcs exporter from condensed
            //values.add(this.wurcsExporterFromCondensed(condensed));

            // to wurcs exporter from extended
            String reconWURCSEx = this.wurcsExporterFromExtended(extended);
            values.add(reconWURCSEx);

            //#
            values.add(optWURCS.equals(reconWURCSEx) ? "1" : "0");

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
