package org.glycoinfo.GlycanFormatconverter.io.LinearCode;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by e15d5605 on 2017/08/29.
 */
public class LinearCodeStacker {

    private String baseUnit;
    private String linearCodeSU;
    private String annotation;
    private String anomericStatus;
    private String parentLinkage;
    private String substituents;

    public LinearCodeStacker (String _linearCodeSU) {

        baseUnit = _linearCodeSU;

        /* parse SU
         * group 1 : Monosacchride LinearCode notation
         * group 2 : composition annotation (~, ^, or ')
         *   non symbol -> configuration : D, ring size : p
         *   '          -> configuration : L, ring size : p
         *   ^          -> configuration : D, ring size : f
         *   ~          -> configuration : L, ring size : f
         * group 3 : substituent group
         *   group 4 : substituents
         * */
        Matcher monoMat = Pattern.compile("([A-Z]+)+(~|\\^|')?(\\[(.+)+])?").matcher(_linearCodeSU);
        if (monoMat.find()) {
            linearCodeSU = monoMat.group(1);
            annotation = monoMat.group(2);
            substituents = monoMat.group(4);
        }

        /* parse linkage
         * group 1 : anomeric status
         * group 2 : parent side linkage
         */
        Matcher linMat = Pattern.compile("((?![A-Z])[ab?])([\\d?/]+)?").matcher(_linearCodeSU);
        if (linMat.find()) {
            anomericStatus = linMat.group(1);
            parentLinkage = linMat.group(2);
        }
    }

    public String getBaseUnit () {
        return baseUnit;
    }

    public String getLinearCodeSU () {
        return linearCodeSU;
    }

    public String getAnnotation () {
        return annotation;
    }

    public String getAnomericStatus () { return anomericStatus; }

    public String getParentLinkage () {
        return parentLinkage;
    }

    public String getSubstituent () {
        return substituents;
    }
}
