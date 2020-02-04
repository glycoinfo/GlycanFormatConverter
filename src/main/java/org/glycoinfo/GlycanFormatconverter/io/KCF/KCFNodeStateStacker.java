package org.glycoinfo.GlycanFormatconverter.io.KCF;

import org.glycoinfo.GlycanFormatconverter.Glycan.SuperClass;

import java.util.ArrayList;

public class KCFNodeStateStacker {

    private String ulosonic;
    private String ringSize;
    private String tailStatus;
    private String firstConfig;
    private String secondConfig;

    private SuperClass superClass;
    private KCFMonosaccharideDescriptor firstUnit;
    private KCFMonosaccharideDescriptor secondUnit;

    private ArrayList<String> subs;
    private ArrayList<String> mods;

    KCFNodeStateStacker() {
        this.firstConfig = "";
        this.secondConfig = "";
        this.ringSize = "";
        this.ulosonic = "";
        this.tailStatus = "";
        this.firstUnit = null;
        this.secondUnit = null;
        this.superClass = null;

        this.mods = new ArrayList<>();
        this.subs = new ArrayList<>();
    }

    public void setFisrtConfig (String _firstConfig) {
        this.firstConfig = _firstConfig;
    }

    public void setSecondConfig (String _secondConfig) {
        this.secondConfig = _secondConfig;
    }

    public void setRingSize (String _ringSize) {
        this.ringSize = _ringSize;
    }

    public void setUlosonic (String _ulosonic) {
        this.ulosonic = _ulosonic;
    }

    public void setTailStatus (String _tailStatus) {
        this.tailStatus = _tailStatus;
    }

    public void setSuperClass (SuperClass _superClass) {
        this.superClass = _superClass;
    }

    public void setFisrtUnit (KCFMonosaccharideDescriptor _firstUnit) {
        this.firstUnit = _firstUnit;
    }

    public void setSecondUnit (KCFMonosaccharideDescriptor _secondUnit) {
        this.secondUnit = _secondUnit;
    }

    public void setSubstituents (ArrayList<String> _subs) {
        this.subs.addAll(_subs);
    }

    public void setModifications (ArrayList<String> _mods) {
        this.mods.addAll(_mods);
    }

    public String getFisrtConfig () {
        return this.firstConfig;
    }

    public String getSecondConfig () {
        return this.secondConfig;
    }

    public String getRingSize () {
        return this.ringSize;
    }

    public String getUlosonic () {
        return this.ulosonic;
    }

    public String getTailStatus () {
        return this.tailStatus;
    }

    public KCFMonosaccharideDescriptor getFirstUnit () {
        return this.firstUnit;
    }

    public KCFMonosaccharideDescriptor getSecondUnit () {
        return this.secondUnit;
    }

    public SuperClass getSuperClass () {
        return this.superClass;
    }

    public ArrayList<String> getModifications () {
        return this.mods;
    }

    public ArrayList<String> getSubstituents () {
        return this.subs;
    }
}
