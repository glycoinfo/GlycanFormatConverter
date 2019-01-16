package org.glycoinfo.GlycanFormatConverter.exchange;

import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;

/**
 * Created by e15d5605 on 2019/01/07.
 */
public class ToJSON_single {
    public static void main (String[] args) throws Exception {
        String input = "GNa4I[2S]a4G[2NS3S6S]a4Ub3Ab3Ab4Xb;S";

        StringBuilder results = new StringBuilder();
        StringBuilder errors = new StringBuilder();

        try {
            LinearCodeImporter lcImp = new LinearCodeImporter();
            ExporterEntrance ee = new ExporterEntrance(lcImp.start(input));
            results.append(ee.toJSONforVisualize() + "\n");

        } catch (Exception e) {
            errors.append(input + "\n");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println(results);
        System.out.println(errors);
    }
}
