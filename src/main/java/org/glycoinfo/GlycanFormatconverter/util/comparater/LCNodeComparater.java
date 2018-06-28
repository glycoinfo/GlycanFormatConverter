package org.glycoinfo.GlycanFormatconverter.util.comparater;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoExporterException;
import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeNodeConverter;
import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeSUDictionary;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;

import java.util.Comparator;

/**
 * Created by e15d5605 on 2017/10/06.
 */
public class LCNodeComparater implements Comparator<Node> {

    @Override
    public int compare(Node o1, Node o2) {
        LinearCodeNodeConverter lcConv = new LinearCodeNodeConverter();

        LinearCodeSUDictionary lcDict1 = null;
        LinearCodeSUDictionary lcDict2 = null;
        try {
            lcDict1 = lcConv.start(o1);
            lcDict2 = lcConv.start(o2);
        } catch (ConverterExchangeException e) {
            e.printStackTrace();
        } catch (GlyCoExporterException e) {
            e.printStackTrace();
        } catch (GlycanException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (lcDict2.getHierarchy() > lcDict1.getHierarchy()) {
            return 1;
        } else if (lcDict2.getHierarchy() == lcDict1.getHierarchy()) {
            return 0;
        } else {
            return -1;
        }
    }
}
