package org.glycoinfo.GlycanFormatConverter.LinearCode;

import org.glycoinfo.GlycanFormatconverter.io.LinearCode.LinearCodeImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

/**
 * Created by e15d5605 on 2018/12/10.
 */
public class UnitTestOfLinearCode {

    @Test
    public void Case1 () {
        String input = "Ma3(Ma3(Ma6)Ma6)Mb4GNb4GNb;N";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case2 () {
        String input = "Ab4GNb3Ab3ANa;S/T";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case3 () {
        String input = "Ma2Ma6M[2PE]a4Ua#6Ino(acyl)P";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case4 () {
        String input = "Mb4GNb4GNa#PPDol";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case5 () {
        String input = "A??GN??Ma3(Ma3(Ma6)Ma6)Mb4GNb4GN";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case6 () {
        String input = "A??A??GN??(A??GN??)Ma3(A??GN??(A??GN??)Ma6)Mb4GNb4(Fa6)GN,A??GN,A??GN";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case7 () {
        String input = "A??(Fa3)GN??(A??(Fa3)GN??)Ma3(A??GN??Ma6)Mb4GNb4(Fa6)GN";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case8 () {
        String input = "NN??A??AN*";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case9 () {
        String input = "A??(F??)GN??*";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case10 () {
        String input = "GNb4(NN??A??GN??Ma3)(GN??Ma6)Mb4GNb4GN";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case11 () {
        String input = "GNb4(NN??A??GN??Ma3)(NJ??A??GN??Ma6)Mb4GNb4(Fa6)GN";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case12 () {
        String input = "ZN??Z??Ab3ANb4(NNa3)Ab4G#Cer";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case13 () {
        String input = "NNa3Ab3ANb4(NNa3)Ab4G#Cer";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case14 () {
        String input = "Ab4GNb2(Ab4GNb4)Ma3(Ab4GNb2(Ab4GNb6)Ma6)Mb4GNb4GN,NNa?";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case15 () {
        String input = "GNb4(A??GN??(A??GN??)Ma3)(A??GN??Ma6)Mb4GNb4(Fa6)GN,NN,NN,F";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    @Test
    public void Case16 () {
        String input = "Ma3(Ma6)Mb4GNb4(Fa6)GN,NNa3Ab4GNb2,Ab4(NNa3)Ab4(Fa3)GNb,Ab4(NNa3)Ab4(Fa3)GNb";
        String wurcs = toWURCS(input);
        System.out.println(wurcs);
    }

    public String toWURCS (String _input) {
        try {
            LinearCodeImporter lci = new LinearCodeImporter();
            ExporterEntrance ee = new ExporterEntrance(lci.start(_input));
            return ee.toWURCS();
        } catch (Exception e) {
            e.getMessage();
        }
        return "";
    }
}
