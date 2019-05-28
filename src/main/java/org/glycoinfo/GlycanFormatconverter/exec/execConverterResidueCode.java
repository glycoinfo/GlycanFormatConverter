package org.glycoinfo.GlycanFormatconverter.exec;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.CondensedConverter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.ExtendedConverter;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACNotationConverter;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.MonosaccharideIndex;
import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer.WURCSGraphToGlyContainer;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.wurcs.graph.WURCSGraph;

/**
 * Created by e15d5605 on 2019/05/27.
 */
public class execConverterResidueCode {

    public static void main (String[] args) {

        try {
            if (args.length == 1) {
                System.out.println(WURCSToTrivialName(args[0]));
            } else if (args.length > 1) {
                for (int i = 0; i < args.length; i++) {
                    System.out.println(WURCSToTrivialName(args[i]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String WURCSToTrivialName (String _input) throws WURCSException, TrivialNameException, GlycanException {
        if (_input.startsWith("WURCS")) {

        } else {
            if (!_input.startsWith("[") && !_input.equalsIgnoreCase("]")) {
                _input = "[" + _input + "]";
            }
            if (_input.startsWith("[") && !_input.endsWith("]")) {
                _input = "[" + _input;
            }
            if (!_input.startsWith("[") && _input.endsWith("]")) {
                _input = _input + "]";
            }
            _input = "WURCS=2.0/1,1,0/" +_input + "/1/";
        }

        WURCSFactory wf = new WURCSFactory(_input);
        WURCSGraph graph = wf.getGraph();

        WURCSGraphToGlyContainer wg2gc = new WURCSGraphToGlyContainer();
        wg2gc.start(graph);
        GlyContainer gc = wg2gc.getGlycan();

        //TODO : 修飾とか構造情報などの情報を併記する必要があるかも
        CondensedConverter condConv = new CondensedConverter();
        String ret = "";
        for (Node node : gc.getNodes()) {
            ret = condConv.start(node, false);
        }

        return ret;
    }

}
