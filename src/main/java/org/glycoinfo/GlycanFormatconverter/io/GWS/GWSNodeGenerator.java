package org.glycoinfo.GlycanFormatconverter.io.GWS;

import org.glycoinfo.GlycanFormatconverter.Glycan.AnomericStateDescriptor;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;

/**
 * Created by e15d5605 on 2019/08/28.
 */
public class GWSNodeGenerator {

    private String gwsNode;

    GWSNodeGenerator () {
        this.gwsNode = "";
    }

    public String getGWSNode () {
        return this.gwsNode;
    }

    public void start (Node _node) throws GlycanException {
        if (!(_node instanceof Monosaccharide)) return;

        //make iupac notation
        IUPACNotationConverter nodeConv_IUPAC = new IUPACNotationConverter();
        nodeConv_IUPAC.makeTrivialName(_node);
        String notation = nodeConv_IUPAC.getCoreCode();

        //make ring size
        String ringSize;
        ringSize = nodeConv_IUPAC.defineRingSize(_node);
        if (ringSize.equals("")) ringSize = this.optimizeRingSize(_node);

        //make anomeric state
        char anomericState;
        String anomericPos;
        anomericState = this.optimizeAnomericState(_node);
        anomericPos = this.optimizeAnomericPosition(_node);

        //make configuration
        String configuration;
        configuration = nodeConv_IUPAC.extractDLconfiguration(((Monosaccharide) _node).getStereos().getFirst());

        //integrate each parts
        //b anomeric state
        //1 anomeric position
        //D configuration
        //- hyphen
        //GlcNAc notation
        //, comma
        //p ring size
        this.gwsNode += anomericState;
        this.gwsNode += anomericPos;
        this.gwsNode += configuration.toUpperCase();
        this.gwsNode += "-";
        this.gwsNode += notation;
        this.gwsNode += ",";
        this.gwsNode += ringSize;

        //remove core substituent


        return;
    }

    private String optimizeAnomericPosition (Node _node) {
        String ret = "?";
        int anomericPos = ((Monosaccharide) _node).getAnomericPosition();

        if (anomericPos > 0) {
            ret = String.valueOf(anomericPos);
        }
        if (anomericPos == 0) {
            ret = "";
        }

        return ret;
    }

    private char optimizeAnomericState (Node _node) {
        char ret = '?';

        AnomericStateDescriptor anomDesc = ((Monosaccharide) _node).getAnomer();

        if (!anomDesc.equals(AnomericStateDescriptor.UNKNOWN_STATE) && !anomDesc.equals(AnomericStateDescriptor.OPEN)) {
            ret = anomDesc.getAnomericState();
        }

        return ret;
    }

    private String optimizeRingSize (Node _node) {
        String ret = "?";

        int anomericPos = ((Monosaccharide) _node).getAnomericPosition();
        AnomericStateDescriptor anomDesc = ((Monosaccharide) _node).getAnomer();

        if (anomericPos == 0 && anomDesc.equals(AnomericStateDescriptor.OPEN)) {
            ret = "o";
        }

        return ret;
    }
}
