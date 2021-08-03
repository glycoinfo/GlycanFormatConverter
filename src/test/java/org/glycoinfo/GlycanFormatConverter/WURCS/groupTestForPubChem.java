package org.glycoinfo.GlycanFormatConverter.WURCS;

import org.glycoinfo.GlycanFormatConverter.util.fileHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class groupTestForPubChem {

    private String failureDir = "./Error/wurcs_roundrobin_failures.txt";
    private String failureAglycon = "./Error/wurcs_roundrobin_failures_aglycon.txt";
    private String outPath = "./Error/";

    @Test
    public void openFailuresAglycon () {
        fileHandler.writeFile(outPath + "wurcs_roundrobin_failures_aglycon", this.openResultFile(failureAglycon), ".tsv");
    }

    @Test
    public void openFailures () {
        fileHandler.writeFile(outPath + "wurcs_roundrobin_failures", this.openResultFile(failureDir), ".tsv");
    }

    public HashMap<String, ArrayList<String>> openResultFile (String _directory) {
        try {
            return fileHandler.openPubChemResult(_directory);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
