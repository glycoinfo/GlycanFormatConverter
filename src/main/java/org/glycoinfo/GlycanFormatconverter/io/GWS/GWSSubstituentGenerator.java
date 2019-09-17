package org.glycoinfo.GlycanFormatconverter.io.GWS;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.SubstituentIUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.ModifiedMonosaccharideDescriptor;

/**
 * Created by e15d5605 on 2019/08/30.
 */
public class GWSSubstituentGenerator {

    private String gwsSubNotation;

    public GWSSubstituentGenerator () {
        this.gwsSubNotation = "";
    }

    public String getGWSSubNotation () {
        return this.gwsSubNotation;
    }

    public void start (Node _node) {
        if (!(_node instanceof Substituent)) return;

        Substituent sub = ((Substituent) _node);

        SubstituentInterface subTemp = sub.getSubstituent();

        this.gwsSubNotation = subTemp.getIUPACnotation();

        return;
    }

    private boolean isCoreSubstituent (Node _node) throws GlycanException {
        boolean isCore = false;

        Substituent sub = ((Substituent) _node);

        if (sub.getSubstituent() instanceof CrossLinkedTemplate) return false;

        Node donor = _node.getParentNode();

        IUPACNotationConverter nodeConv_IUPAC = new IUPACNotationConverter();

        nodeConv_IUPAC.makeTrivialName(donor);

        ModifiedMonosaccharideDescriptor modMonoDesc =
                ModifiedMonosaccharideDescriptor.forTrivialName(nodeConv_IUPAC.getCoreCode());

        SubstituentIUPACNotationConverter subConv_IUPAC = nodeConv_IUPAC.getSubConv();

        return isCore;
    }
}
