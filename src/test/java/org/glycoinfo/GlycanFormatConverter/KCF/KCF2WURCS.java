package org.glycoinfo.GlycanFormatConverter.KCF;

import org.glycoinfo.GlycanFormatConverter.util.fileHandler;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.KCF.KCFImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.junit.Test;

import java.util.*;

/**
 * Created by e15d5605 on 2017/12/12.
 */
public class KCF2WURCS {

    private final String outPath = "src/test/resources/result/";

    @Test
    public void kcf2wurcs_case1 (){
        String inputFile = "src/test/resources/KCF/" + "";

        try {
            Object kcfMap = fileHandler.openString(inputFile, "KCF");
            if (kcfMap instanceof String) throw new Exception((String) kcfMap);
            else {
                HashMap<String, String> template = (HashMap<String, String>) kcfMap;
                fileHandler.writeFile(outPath + "K2W", this.makeMap(template), ".tsv");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void kcf2wurcs_case2 (){
        String inputFile = "src/test/resources/KCF/" + "20171212_GlyTouCan_KCFsamples";

        try {
            Object kcfMap = fileHandler.openString(inputFile, "KCF");
            if (kcfMap instanceof String) throw new Exception((String) kcfMap);
            else {
                HashMap<String, String> template = (HashMap<String, String>) kcfMap;
                fileHandler.writeFile(outPath + "K2W", this.makeMap(template), ".tsv");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void kcf2wurcs_case3 (){
        String inputFile = "src/test/resources/KCF/" + "KCFSample";

        try {
            Object kcfMap = fileHandler.openString(inputFile, "KCF");
            if (kcfMap instanceof String) throw new Exception((String) kcfMap);
            else {
                HashMap<String, String> template = (HashMap<String, String>) kcfMap;
                fileHandler.writeFile(outPath + "K2W", this.makeMap(template), ".tsv");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void kcf2wurcs_UnitCase1 () {
        String inputKCF =
                "ENTRY       G04902                      Glycan\n" +
                        "NODE        10\n" +
                        "            1   GalNAc     24     1\n" +
                        "            2   S          18     5\n" +
                        "            3   GalA       15     1\n" +
                        "            4   S          10    -4\n" +
                        "            5   GalNAc      6     1\n" +
                        "            6   S           0     5\n" +
                        "            7   GalA       -3     1\n" +
                        "            8   GalNAc    -12     1\n" +
                        "            9   S         -18     5\n" +
                        "            10  L4-en-thrHexA   -24     1\n" +
                        "EDGE        9\n" +
                        "            1     2       1:4  \n" +
                        "            2     3:b1    1:3  \n" +
                        "            3     4       3:2  \n" +
                        "            4     5:b1    3:4  \n" +
                        "            5     6       5:6  \n" +
                        "            6     7:b1    5:3  \n" +
                        "            7     8:b1    7:4  \n" +
                        "            8     9       8:6  \n" +
                        "            9    10:a1    8:3  \n" +
                        "///";
        System.out.println(this.toWURCS(inputKCF));
   }

    @Test
    public void kcf2wurcs_UnitCase2 () {
        String inputKCF =
                "ENTRY       G04866                      Glycan\n" +
                        "NODE        10\n" +
                        "            1   Glc         0     0\n" +
                        "            2   Glc       -10     5\n" +
                        "            3   Glc       -10    -5\n" +
                        "            4   Glc       -20     5\n" +
                        "            5   GlcA      -20    -5\n" +
                        "            6   Glc       -30     5\n" +
                        "            7   GlcA      -30    -5\n" +
                        "            8   GlcA      -40     5\n" +
                        "            9   Gal       -50     5\n" +
                        "            10  Glc4,6Py   -60     5\n" +
                        "EDGE        9\n" +
                        "            1     2:b1    1:6  \n" +
                        "            2     3:b1    1:4  \n" +
                        "            3     4:b1    2:4  \n" +
                        "            4     5:b1    3:4  \n" +
                        "            5     6:b1    4:4  \n" +
                        "            6     7:b1    5:4  \n" +
                        "            7     8:b1    6:4  \n" +
                        "            8     9:a1    8:4  \n" +
                        "            9    10:b1    9:6  \n" +
                        "///";
        System.out.println(this.toWURCS(inputKCF));
   }

    @Test
    public void kcf2wurcs_UnitCase3 () {
        String inputKCF =
                        "ENTRY       G09728                      Glycan\n" +
                        "NODE        3\n" +
                        "            1   2dGlc     8.1  -0.1\n" +
                        "            2   Glc      -0.9  -0.1\n" +
                        "            3   Glc      -8.9  -0.1\n" +
                        "EDGE        2\n" +
                        "            1     2:a1    1:4  \n" +
                        "            2     3:a1    2:4  \n" +
                        "///";
        System.out.println(this.toWURCS(inputKCF));
   }

    @Test
    public void kcf2wurcs_UnitCase4 () {
        String inputKCF =
                "ENTRY       G09840                      Glycan\n" +
                        "NODE        2\n" +
                        "            1   Glc       8.2   0.5\n" +
                        "            2   L2,6d3-C-methyl-lyxHex3N  -8.8   0.5\n" +
                        "EDGE        1\n" +
                        "            1     2:a1    1:2  \n" +
                        "///";
        System.out.println(this.toWURCS(inputKCF));
   }

    private HashMap<String, ArrayList<String>> makeMap (HashMap<String, String> _kcfMap) {
        HashMap<String, ArrayList<String>> resultMap = new HashMap<>();
        for (String key : _kcfMap.keySet()) {
            // kcf to wurcs & modify model wurcs
            String model = this.optimizeWURCS(this.toWURCS(_kcfMap.get(key)));

            // wurcs to iupac-extended
            //iupac = this.toIUPAC(this.inWURCS(model));

            // iupac-extended to wurcs
            //wurcs = this.toWURCS(this.inIUPAC(iupac));

            ArrayList<String> values = new ArrayList<>();

            //values[0] : wurcs

            //#0
            values.add(model.replaceAll("\n", " "));

            //#1
            // 0: error in some protocol
            // 1: complete
            // 2: error in GFC
            // 3: error in WFW (validation error)
            String code = "0";
            if (model.startsWith("WURCS=")) code = "1";
            if (model.startsWith("The (C)-type linkage of substituent") ||
                    model.startsWith("IUPAC importer can not support")) code = "2";
            if (model.startsWith("SkeletonCode ") || model.startsWith("Only one modification") ||
                    model.startsWith("The number of unique modifications")) code = "3";
            values.add(code);

            resultMap.put(key, values);
        }

        return resultMap;
    }

    private String toWURCS (String _kcf) {
        try {
            KCFImporter kcfImporter = new KCFImporter();
            GlyContainer gc = kcfImporter.start(_kcf);

            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toWURCS();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private String optimizeWURCS (String _wurcs) {
        try {
            if (_wurcs == null) {
                throw new Exception("WURCS string can not generated.");
            }
            if (!_wurcs.startsWith("WURCS")) {
                throw new Exception(_wurcs);
            }

            WURCSFactory wf = new WURCSFactory(_wurcs);
            return wf.getWURCS();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
