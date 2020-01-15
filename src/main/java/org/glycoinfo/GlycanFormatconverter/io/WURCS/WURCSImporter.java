package org.glycoinfo.GlycanFormatconverter.io.WURCS;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.util.GlyContainerOptimizer;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.graph.WURCSGraphNormalizer;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

//import org.glycoinfo.WURCSFramework.util.graph.analysis.SubsumptionLevel;
//import org.glycoinfo.subsumption.SubsumptionConverter;
//import org.glycoinfo.subsumption.SubsumptionException;
//import org.glycoinfo.subsumption.WURCSGraphStateDeterminator;

/**
 * Created by e15d5605 on 2017/10/23.
 */
public class WURCSImporter {

    private GlyContainer glyCo;

    public GlyContainer start (String _wurcs) throws WURCSException, GlycanException {
        WURCSFactory wf = new WURCSFactory(_wurcs);
        WURCSGraph graph = wf.getGraph();
		WURCSGraphNormalizer wgNorm = new WURCSGraphNormalizer();
		wgNorm.start(graph);

		WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();
        wg2gc.start(graph);

        //
        GlyContainerOptimizer gcOpt = new GlyContainerOptimizer();
        return gcOpt.start(wg2gc.getGlycan());
    }
}
