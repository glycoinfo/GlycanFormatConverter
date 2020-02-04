package org.glycoinfo.GlycanFormatconverter.io.GLYCAM;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;

import java.util.HashMap;

public class GLYCANImporter {

    private HashMap<Node, String> notationMap;

    GLYCANImporter() {
        notationMap = new HashMap<>();
    }

    public GlyContainer start (String _seq) {
        GlyContainer ret = new GlyContainer();

        //TODO: 単糖の抽出
        //TODO: 単糖成分の分解
        //TODO: 修飾の抽出
        //TODO: 親子関係の定義
        //TODO: 分岐関係の定義
        //TODO: 結合位置の抽出

        return ret;
    }
}
