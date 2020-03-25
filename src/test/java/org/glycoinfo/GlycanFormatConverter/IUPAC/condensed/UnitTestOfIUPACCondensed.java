package org.glycoinfo.GlycanFormatConverter.IUPAC.condensed;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoImporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.condensed.IUPACCondensedImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.junit.Test;

/**
 * Created by e15d5605 on 2018/08/21.
 * Modified by e15d5605 (S.TSUCHIYA) on 2020/02/06.
 */
public class UnitTestOfIUPACCondensed {

    @Test
    public void Fragments () {
        String input = "Fuc(?1-?)=1$,1$Neu5Ac(?2-?)1$Gal(?1-?)1$GlcNAc(?1-?)1$Man(a1-3)[1$Neu5Ac(?2-?)1$Gal(?1-?)1$GlcNAc(?1-?)1$Man(a1-6)]1$Man(b1-4)1$GlcNAc(b1-4)1$GlcNAc(b1-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void UNKNOWN () {
        String input = "GlcA3OS(b1-3)Gal(b1-3)Gal(b1-4)Xyl(??-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void WrongCase () {
        String input = "Gal(b1-4)GlcNAc(b1-6)[Fuc(a1-2)Gal(b1-3)GlcNAc(b1-3)]Gal(b1-4)Glc(b1-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void NonBracket () {
        String input = "Galb1-4GlcNAcb1-6[Fuca1-2Galb1-3GlcNAcb1-3]Galb1-4Glcb1-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void FuzzyLinkage () {
        String input = "GlcNAc(b1-2)Man(a1-6)[Gal(b1-4)GlcNAc(b1-2)[Gal(b1-3/4/6)GlcNAc(b1-4)]Man(a1-3)]Man(b1-4)GlcNAc(b1-4)[Fuc(a1-6)]GlcNAc(?1-";
        System.out.println(toWURCS(input));
    }
    @Test
    public void IUPACCondensedImporter () {
        //sets.add("KDNa2-3Galb1-4(Fuca1-3)GlcNAc-");

        //sets.add("GlcN(Gc)b-");
        //sets.add("Mana1-6(Mana1-3)Manb1-4GlcNAcb1-4GlcNAcb-");
        //sets.add("Galb1-4GlcNAcb1-2 Mana1-6(Galb1-4GlcNAcb1-4)(Galb1-4GlcNAcb1-2Mana1-3)Manb1-4GlcNAcb1-4(Fuca1-6)GlcNAc-");
        //sets.add("Neu5Aca2-3Galb1-3(6S)GlcNAc-");
        //sets.add("Gala1-3(Fuca1-2)Galb1-4GlcNAcb1-2Mana1-6(Gala1-3(Fuca1-2)Galb1-4GlcNAcb1-2Mana1-3)Manb1-4GlcNAcb1-4GlcNAcb-");

        //sets.add("GlcNAc(b1-2)Man(a1-6)[Gal(b1-4)GlcNAc(b1-2)[Gal(b1-4)GlcNAc(b1-4)]Man(a1-3)]Man(b1-4)GlcNAc(b1-4)[Fuc(a1-6)]GlcNAc(?1-");
        //sets.add("GlcNAc b1-2 Man a1-6 (Gal b1-4 GlcNAc b1-2 (Gal b1-4 GlcNAc b1-4)Man a1-3)Man b1-4 GlcNAc b1-4(Fuc a1-6)GlcNAc");
        //sets.add("GlcNAc b1-? Man a1-3/6 (Gal b1-4 GlcNAc b1-2 (Gal b1-4 GlcNAc b1-4)Man a1-3/6)Man b1-4 GlcNAc b1-4(Fuc a1-6)GlcNAc");

        //sets.add("Glc(a1-4)6)Glc(a1-4)Glc(a1-4)Glc(a1-");

        //System.out.println(toWURCS(input));
    }

    @Test
    public void Bisecting () {
        String input = "Gal(b1-4)GlcNAc(b1-6)[Gal(b1-3)][GlcNAc(b1-2)]Gal(b1-4)Glc(b1-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void BranchesWithBoxBracket () {
        String input = "Gal(b1-4)GlcNAc(b1-6)[Fuc(a1-2)Gal(b1-3)GlcNAc(b1-3)]Gal(b1-4)Glc(b1-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void BranchesWithCurlyBracket () {
        String input = "Galb1-4GlcNAcb1-6(Fuca1-2Galb1-3GlcNAcb1-3)Galb1-4Glcb1-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void MultipleBranches () {
        String input = "Mana1-3(Gulb1-4)GlcNAcb1-6(Fuca1-2(Galb1-3)GlcNAcb1-3)Galb1-4Glcb1-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void Branches () {
        String input = "Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-6(Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAb1-2)Mana1-6(Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-3Galb1-4GlcNAcb1-2Mana1-3)Manb1-4GlcNAcb1-4(Fuca1-6)GlcNAcb-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void SubstituentInMonosaccharide () {
        String input = "Neu5,9Ac2a2-6Galb1-4GlcNAcb-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void SubstituentIsDonor () {
        String input = "(6S)(4S)Galb1-4GlcNAcb-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void SubstituentIsBranch () {
        String input = "(3S)Galb1-4(Fuca1-3)(6S)Glc-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void Cyclic () {
        String input = "4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-4)Glc2,3OMe26N(a1-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void CyclicInRepeat () {
        String input = "4)Glc(a1-4)[4)Glc(a1-]n:a1-4)Glc(a1-4)[4)Glc(a1-]n:(a1-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void BranchedRepeat () {
        String input = "GlcN(a1-4)L-IdoA(a1-4)[4)GlcN(a1-4)L-IdoA(a1-]n:a1-4)GlcN(a1-";
        System.out.println(toWURCS(input));
    }

    @Test
    public void Repeat () {
        String input = "[4)Glc(b1-3)Gal(a1-4)GalNAc(b1-4)GlcA(b1-3)GalNAc(b1-]n";
        System.out.println(toWURCS(input));
    }

    public String toWURCS(String _iupac) {
        try {
            IUPACCondensedImporter ici = new IUPACCondensedImporter();
            GlyContainer gc = ici.start(_iupac);
            ExporterEntrance ee = new ExporterEntrance(gc);
            return ee.toWURCS();
        } catch (GlyCoImporterException e) {
            e.printStackTrace();
//            System.out.println(e.getMessage());
        } catch (GlycanException e) {
            e.printStackTrace();
//            System.out.println(e.getMessage());
        } catch (WURCSException e) {
            e.printStackTrace();
//            System.out.println(e.getMessage());
        }

        return null;
    }

}
