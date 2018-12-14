package org.glycoinfo.GlycanFormatConverter.reader;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

/**
 * Created by e15d5605 on 2018/12/10.
 */
public class LCSingleToWURCS {

    @Test
    public void LCToWURCS () {

        String lc =
        //"Ma3(Ma3(Ma6)Ma6)Mb4GNb4GNb;N";
        //"Ab4GNb3Ab3ANa;S/T";
        //"Ma2Ma6M[2PE]a4Ua#6Ino(acyl)P";
        "Mb4GNb4GNa#PPDol";

        //        "A??GN??Ma3(Ma3(Ma6)Ma6)Mb4GNb4GN";
        //        "A??A??GN??(A??GN??)Ma3(A??GN??(A??GN??)Ma6)Mb4GNb4(Fa6)GN,A??GN,A??GN";
        //      "A??(Fa3)GN??(A??(Fa3)GN??)Ma3(A??GN??Ma6)Mb4GNb4(Fa6)GN";
        //"NN??A??AN*";
        //"A??(F??)GN??*";
        //"GNb4(NN??A??GN??Ma3)(GN??Ma6)Mb4GNb4GN";
        //"GNb4(NN??A??GN??Ma3)(NJ??A??GN??Ma6)Mb4GNb4(Fa6)GN";
        //"ZN??Z??Ab3ANb4(NNa3)Ab4G#Cer";
        //"NNa3Ab3ANb4(NNa3)Ab4G#Cer";
        //"Ab4GNb2(Ab4GNb4)Ma3(Ab4GNb2(Ab4GNb6)Ma6)Mb4GNb4GN,NNa?";
        //"GNb4(A??GN??(A??GN??)Ma3)(A??GN??Ma6)Mb4GNb4(Fa6)GN,NN,NN,F";
        //"Ma3(Ma6)Mb4GNb4(Fa6)GN,NNa3Ab4GNb2,Ab4(NNa3)Ab4(Fa3)GNb,Ab4(NNa3)Ab4(Fa3)GNb";

        try {
            LinearCodeImporter lci = new LinearCodeImporter();
            GlyContainer gc = lci.start(lc);
            ExporterEntrance ee = new ExporterEntrance(gc);
            System.out.println(ee.toWURCS());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
