package org.glycoinfo.GlycanFormatconverter.io.JSON;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Linkage;
import org.glycoinfo.GlycanFormatconverter.Glycan.LinkageType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by e15d5605 on 2017/10/24.
 */
public class GCJSONLinkageParser {

    public Linkage parsePosition (JSONObject _position) throws GlycanException {
        if (_position.length() == 0) return null;
        Linkage ret = new Linkage();

        for (String key : _position.keySet()) {
            switch (key) {
                case "Position" :
                    ret = extractPosition(_position.getJSONObject(key), ret);
                    break;

                case "Probability" :
                    ret = extractProbability(_position.getJSONObject(key), ret);
                    break;

                case "LinkageType" :
                    ret = extractLinkageType(_position.getJSONObject(key), ret);
                    break;
            }
        }

        return ret;
    }

    public Linkage extractPosition (JSONObject _position, Linkage _lin) {
        for (String key : _position.keySet()) {
            switch (key) {
                case "ChildSide" :
                    _lin.setChildLinkages(openPositions(_position.getJSONArray(key)));
                    break;

                case "ParentSide" :
                    _lin.setParentLinkages(openPositions(_position.getJSONArray(key)));
                    break;
            }
        }

        return _lin;
    }

    public Linkage extractLinkageType (JSONObject _linkageType, Linkage _lin) throws GlycanException {
        for (String key : _linkageType.keySet()) {
            switch (key) {
                case "ChildSide" :
                    _lin.setChildLinkageType(parseLinkageType(_linkageType.getString(key)));
                    break;

                case "ParentSide" :
                    _lin.setParentLinkageType(parseLinkageType(_linkageType.getString(key)));
                    break;
            }
        }

        return _lin;
    }

    public Linkage extractProbability (JSONObject _probability, Linkage _lin) {
        for (String key : _probability.keySet()) {
            JSONObject probability = _probability.getJSONObject(key);
            switch (key) {
                case "ChildSide" :
                    _lin.setChildProbabilityLower(probability.getDouble("High"));
                    _lin.setChildProbabilityUpper(probability.getDouble("Low"));
                    break;

                case "ParentSide" :
                    _lin.setProbabilityLower(probability.getDouble("High"));
                    _lin.setProbabilityUpper(probability.getDouble("Low"));
                    break;
            }
        }

        return _lin;
    }

    private Collection<Integer> openPositions (JSONArray _positions) {
        ArrayList<Integer> ret = new ArrayList<>();
        for (Object pos : _positions) {
            ret.add((Integer) pos);
        }

        return ret;
    }

    private LinkageType parseLinkageType (String _type) {
        for (LinkageType value : LinkageType.values()) {
            if (_type.equals(value.toString())) return value;
        }
        return null;
    }
}
