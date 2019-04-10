package org.glycoinfo.GlycanFormatconverter.util;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoExporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.*;
import org.glycoinfo.GlycanFormatconverter.io.JSON.GCJSONExporter;
import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeExporter;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.GlycanFormatconverter.util.exchange.GlyContainerToWURCSGraph.GlyContainerToWURCSGraph;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;

/**
 * Created by e15d5605 on 2017/10/23.
 */
public class ExporterEntrance {

    private GlyContainer glyCo;

    public ExporterEntrance(GlyContainer _glyCo) {
        this.glyCo = _glyCo;
    }

    public String toIUPAC (IUPACStyleDescriptor _style) throws GlycanException, GlyCoExporterException, TrivialNameException {
        if (_style == null) throw new GlyCoExporterException(_style + " is incorrect format.");

        StringBuilder ret = new StringBuilder();
        GlyContainer copyGlyco = glyCo.copy();

        if (_style.equals(IUPACStyleDescriptor.SHORT)) {
            IUPACShortExporter shortExpo = new IUPACShortExporter();
            shortExpo.start(copyGlyco);
            return shortExpo.getIUPACShort();
        }
        if (_style.equals(IUPACStyleDescriptor.CONDENSED)) {
            IUPACCondensedExporter condExpo = new IUPACCondensedExporter(false);
            condExpo.start(copyGlyco);
            return condExpo.getIUPACCondensed();
        }
        if (_style.equals(IUPACStyleDescriptor.EXTENDED)) {
            IUPACExtendedExporter extExpo = new IUPACExtendedExporter();
            extExpo.start(copyGlyco);
            return extExpo.getIUPACExtended();
        }
        if (_style.equals(IUPACStyleDescriptor.GREEK)) {
            IUPACExtendedExporter extExpo = new IUPACExtendedExporter();
            extExpo.start(copyGlyco);
            return extExpo.toGreek();
        }
        if (_style.equals(IUPACStyleDescriptor.GLYCANWEB)) {
            IUPACCondensedExporter condExpo = new IUPACCondensedExporter(true);
            condExpo.start(copyGlyco);
            return condExpo.getIUPACCondensed();
        }

        return ret.toString();
    }

    public String toLinearCode () throws GlyCoExporterException, ConverterExchangeException, GlycanException {
        LinearCodeExporter lcExpo = new LinearCodeExporter();

        return lcExpo.start(glyCo);
    }

    public String toJSON () throws GlycanException, ConverterExchangeException {
        GCJSONExporter gcjsonExpo = new GCJSONExporter();

        return gcjsonExpo.start(glyCo.copy(), false);
    }

    public String toJSONforVisualize () throws ConverterExchangeException, GlycanException {
        GCJSONExporter gcjsonExpo = new GCJSONExporter();

        return gcjsonExpo.start(glyCo.copy(), true);
    }

    public String toWURCS () throws GlycanException, WURCSException {
        GlyContainerToWURCSGraph gc2wg = new GlyContainerToWURCSGraph();
        gc2wg.start(glyCo);

        WURCSFactory wf = new WURCSFactory(gc2wg.getGraph());

        return wf.getWURCS();
    }
}
