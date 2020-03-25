package org.glycoinfo.GlycanFormatConverter.WURCS;

import org.glycoinfo.GlycanFormatConverter.exchange.GlycoCT.CSVReader;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.wurcs.graph.Backbone;
import org.glycoinfo.WURCSFramework.wurcs.graph.ModificationAlternative;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSEdge;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by e15d5605 on 2019/03/01.
 */
public class WURCS2IUPAC_csv {

    public static void main (String[] args) {
        String path = "./GlyTouCan_sequences_all.csv";

        CSVReader t_reader;
        try {
            t_reader = new CSVReader(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Set<String> id = new HashSet<>();
        Set<String> wurcs = new HashSet<>();
        Set<String> gct = new HashSet<>();
        String[] id_wurcs;
        while ( (id_wurcs = t_reader.readNext() ) != null ) {
            if ( id_wurcs.length < 2 )
                continue;
            String t_strID      = id_wurcs[0];
            String t_strWURCS   = id_wurcs[1];
            String t_strGlycoCT = (id_wurcs.length > 2)? id_wurcs[2] : "";
            if ( !t_strGlycoCT.isEmpty() && !t_strGlycoCT.endsWith("\n") )
                t_strGlycoCT += "\n";

            // Skip if no WURCS
            if ( !t_strWURCS.startsWith("WURCS") )
                continue;

            //if ( this.m_lTargetIDs != null && !this.m_lTargetIDs.contains(t_strID) )
            //    continue;

            id.add(t_strID);

            String t_strIDWURCS = t_strID+":"+t_strWURCS;
            if ( wurcs.contains(t_strIDWURCS) )
                continue;
            wurcs.add(t_strIDWURCS);

            if ( !t_strGlycoCT.isEmpty() && !gct.contains(t_strGlycoCT) ) {
                gct.add(t_strGlycoCT);
               // processGlycoCT( t_strID, t_strGlycoCT );
            }

            //processWURCS( t_strID, t_strWURCS );

            //processAll( t_strID, t_strWURCS, t_strGlycoCT );
        }

        StringBuilder result = new StringBuilder();

        for (String item : wurcs) {
            String[] items = item.split(":");
            try {
                WURCSFactory wf = new WURCSFactory(items[1]);
                WURCSGraph wg = wf.getGraph();

                for (Backbone bb : wg.getBackbones()) {
                    for (WURCSEdge we : bb.getChildEdges()) {
                        if (!(we.getNextComponent() instanceof ModificationAlternative)) continue;
                        ModificationAlternative modAlt = (ModificationAlternative) we.getNextComponent();

                        if (!modAlt.getChildEdges().isEmpty()) {
                            throw new Exception("This fragments could not convert to IUPAC.");
                        }

                    }
                }

                WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();
                wg2gc.start(wg);
                ExporterEntrance ee = new ExporterEntrance(wg2gc.getGlycan());
                result.append(items[0] + "\t" + ee.toIUPAC(IUPACStyleDescriptor.GREEK) + "\n");
            } catch (Exception e) {
                result.append(items[0] + "\te:" + e.getMessage() + "\n");
            }
        }

        /* define file name */
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String fileName = sdf.format(date) + "_IUPACSample_from_WURCS";

        /* write WURCS */
        try {
            writeFile(result.toString(), "", fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFile (String _result, String _error, String _fileName) throws IOException {
        /* file open */
        File file = new File(_fileName);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);

        /* write file */
        PrintWriter pw = new PrintWriter(bw);
        pw.println(_result);
        pw.println(_error);

        pw.close();

        return;
    }
}
