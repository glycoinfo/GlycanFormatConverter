package org.glycoinfo.GlycanFormatconverter.io.WURCS;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.graph.WURCSGraphNormalizer;
//import org.glycoinfo.WURCSFramework.util.graph.analysis.SubsumptionLevel;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;
//import org.glycoinfo.subsumption.SubsumptionConverter;
//import org.glycoinfo.subsumption.SubsumptionException;
//import org.glycoinfo.subsumption.WURCSGraphStateDeterminator;

/**
 * Created by e15d5605 on 2017/10/23.
 */
public class WURCSImporter {

    private GlyContainer glyCo;

    public GlyContainer getGlyContainer () {
        return glyCo;
    }

    public void start (String _wurcs) throws WURCSException, GlycanException {
        WURCSFactory wf = new WURCSFactory(_wurcs);
        WURCSGraph graph = wf.getGraph();
		WURCSGraphNormalizer wgNorm = new WURCSGraphNormalizer();
		wgNorm.start(graph);

        /* Check WURCS level
        * If subsumption level is 4B, convert to level 5.
        * */
/*        WURCSGraphStateDeterminator wgsd = new WURCSGraphStateDeterminator();
        SubsumptionLevel subLevel = wgsd.getSubsumptionLevel(graph);

        if (subLevel.equals(SubsumptionLevel.LV3) || subLevel.equals(SubsumptionLevel.LV4B)) {
        	SubsumptionConverter subConv = new SubsumptionConverter();
        	subConv.setWURCSseq(_wurcs);
        	subConv.convertDefined2Ambiguous();
        	wf = new WURCSFactory(subConv.getAmbiguousWURCSseq());
        	graph = wf.getGraph();
        }
*/
		WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();
        wg2gc.start(graph);

        glyCo = wg2gc.getGlycan();
    }
}
