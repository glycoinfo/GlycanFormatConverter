package org.glycoinfo.GlycanFormatconverter.util;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;

/**
 * Created by e15d5605 on 2017/10/23.
 */
public class ImporterEntrance {

    private String input;

    //TODO : 読み込まれたStringを検定し、なんの形式なのかを判別する。判別ごそれぞれのimporterに投げ込み、GlyContainerを返す。
    public ImporterEntrance (String _input) {
        this.input = _input;
    }

    public GlyContainer start () {

        switch (chekcFormat()) {
            case "KCF" :
                break;

            case "LinearCode" :
                break;

            case "WURCS" :
                break;

            case "IUPACExtended" :
                break;

            case "IUPACCondensed" :
                break;

            case "JSON" :
                break;
        }

        return null;
    }

    private String chekcFormat () {
        String ret = "";

        return ret;
    }
}
