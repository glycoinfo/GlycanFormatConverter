package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.util.GlyContainerOptimizer;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by e15d5605 on 2017/09/19.
 */
public class GCJSONImporter {

    public GlyContainer start (String _json) throws GlycanException {
        JSONObject glycan = new JSONObject(_json);

        // Monosaccharide to Node
        JSONObject monosaccharides = glycan.getJSONObject("Monosaccharides");
        GCJSONNodeParser gcNodeParser = new GCJSONNodeParser();
        HashMap<String, Node> nodeIndex = gcNodeParser.start(monosaccharides);

        // Parse Edges
        GCJSONEdgeParser gcEdgeParser = new GCJSONEdgeParser(nodeIndex);
        GlyContainer ret = gcEdgeParser.start(glycan.getJSONObject("Edges"), glycan.getJSONObject("Bridge"));

        // Parse repeating unit
        if (!glycan.getJSONObject("Repeat").isEmpty()) {
            GCJSONRepeatParser gcRepParser = new GCJSONRepeatParser(nodeIndex);
            gcRepParser.start(glycan.getJSONObject("Repeat"), glycan.getJSONObject("Bridge"), ret);
        }

        // Parse ambiguous substituents
        if (!glycan.getJSONObject("Fragments").isEmpty()) {
            GCJSONFragmentsParser gcFragParser = new GCJSONFragmentsParser(nodeIndex);
            gcFragParser.start(glycan.getJSONObject("Fragments"), ret);
        }

        // Parse compositions
        if (!glycan.getJSONObject("Composition").isEmpty()) {

        }

        GlyContainerOptimizer gcOpt = new GlyContainerOptimizer();
        ret = gcOpt.start(ret);

        return ret;
    }
}
