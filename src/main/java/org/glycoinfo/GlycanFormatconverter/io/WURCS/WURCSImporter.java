package org.glycoinfo.GlycanFormatconverter.io.WURCS;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.util.GlyContainerOptimizer;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.graph.WURCSGraphNormalizer;
import org.glycoinfo.WURCSFramework.util.graph.analysis.SubsumptionLevel;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

/**
 * Created by e15d5605 on 2017/10/23.
 */
public class WURCSImporter {

    private WURCSGraphToGlyContainer wg2gc;

    public WURCSGraphToGlyContainer getConverter () {
        return this.wg2gc;
    }

    public GlyContainer start (String _wurcs) throws WURCSException, GlycanException {
        WURCSFactory wf = new WURCSFactory(_wurcs);
        WURCSGraph graph = wf.getGraph();
		WURCSGraphNormalizer wgNorm = new WURCSGraphNormalizer();
		wgNorm.start(graph);

		//TODO: CompositionWithLinkageを弾く

		this.wg2gc = new WURCSGraphToGlyContainer();
        this.wg2gc.start(graph);

        return this.wg2gc.getGlycan();
        //
        //GlyContainerOptimizer gcOpt = new GlyContainerOptimizer();
        //return gcOpt.start(this.wg2gc.getGlycan());
    }
}
