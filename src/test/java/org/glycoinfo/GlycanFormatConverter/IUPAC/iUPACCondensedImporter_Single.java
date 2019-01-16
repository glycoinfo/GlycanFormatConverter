package org.glycoinfo.GlycanFormatConverter.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACCondensedImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by e15d5605 on 2018/08/21.
 */
public class iUPACCondensedImporter_Single {

    @Test
    public void IUPACCondensedImporter () throws GlyCoImporterException, GlycanException, WURCSException {
        ArrayList<String> sets = new ArrayList<>();

        //sets.add("KDNa2-3Galb1-4(Fuca1-3)GlcNAc-");
        //sets.add("Neu5,9Ac2a2-6Galb1-4GlcNAcb-");
        //sets.add("(3S)Galb1-4(Fuca1-3)(6S)Glc-");
        //sets.add("(6S)(4S)Galb1-4GlcNAcb-");

        //sets.add("GlcN(Gc)b-");
        //sets.add("Mana1-6(Mana1-3)Manb1-4GlcNAcb1-4GlcNAcb-");
        //sets.add("Galb1-4GlcNAcb1-2 Mana1-6(Galb1-4GlcNAcb1-4)(Galb1-4GlcNAcb1-2Mana1-3)Manb1-4GlcNAcb1-4(Fuca1-6)GlcNAc-");
        sets.add("Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-6(Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAb1-2)Mana1-6(Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-2Mana1-3)Manb1-4GlcNAcb1-4(Fuca1-6)GlcNAcb-");
        //sets.add("Neu5Aca2-3Galb1-3(6S)GlcNAc-");
        //sets.add("Gala1-3(Fuca1-2)Galb1-4GlcNAcb1-2Mana1-6(Gala1-3(Fuca1-2)Galb1-4GlcNAcb1-2Mana1-3)Manb1-4GlcNAcb1-4GlcNAcb-");

        //sets.add("GlcNAc(b1-2)Man(a1-6)[Gal(b1-4)GlcNAc(b1-2)[Gal(b1-4)GlcNAc(b1-4)]Man(a1-3)]Man(b1-4)GlcNAc(b1-4)[Fuc(a1-6)]GlcNAc");
        //sets.add("GlcNAc b1-2 Man a1-6 (Gal b1-4 GlcNAc b1-2 (Gal b1-4 GlcNAc b1-4)Man a1-3)Man b1-4 GlcNAc b1-4(Fuc a1-6)GlcNAc");
        //sets.add("GlcNAc b1-? Man a1-3/6 (Gal b1-4 GlcNAc b1-2 (Gal b1-4 GlcNAc b1-4)Man a1-3/6)Man b1-4 GlcNAc b1-4(Fuc a1-6)GlcNAc");

        //sets.add("4)Glc(a1-4)[4)Glc(a1-]n:a1-4)Glc(a1-4)[4)Glc(a1-]n:(a1-");

        //sets.add("GlcN(a1-4)L-IdoA(a1-4)[4)GlcN(a1-4)L-IdoA(a1-]n:a1-4)GlcN(a1-");
        //sets.add("4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-");
        //sets.add("Glc(a1-4)6)Glc(a1-4)Glc(a1-4)Glc(a1-");
        //sets.add("[4)Glc(b1-3)Gal(a1-4)GalNAc(b1-4)GlcA(b1-3)GalNAc(b1-]n");

        for (String input : sets) {
            IUPACCondensedImporter ici = new IUPACCondensedImporter();
            ici.start(input);

            GlyContainer gc = ici.getGlyContainer();

            ExporterEntrance ee = new ExporterEntrance(gc);
            System.out.println(ee.toWURCS());
        }
    }
}
