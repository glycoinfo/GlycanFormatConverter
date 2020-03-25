package org.glycoinfo.GlycanFormatConverter.KCF;

import org.glycoinfo.GlycanFormatconverter.io.KCF.KCFImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Assert;
import org.junit.Test;

public class UnitTestOfKCF {
    @Test
    public void KCFImporter () {
        String input = "";
        String wurcs = toWURCS(input);
        Assert.assertEquals(wurcs, input);
    }

    public String toWURCS (String _kcf) {
        try {
            KCFImporter kcfi = new KCFImporter();
            ExporterEntrance ee = new ExporterEntrance(kcfi.start(_kcf));
            return ee.toWURCS();
        } catch (Exception e) {
            e.getMessage();
        }
        return "";
    }
}
