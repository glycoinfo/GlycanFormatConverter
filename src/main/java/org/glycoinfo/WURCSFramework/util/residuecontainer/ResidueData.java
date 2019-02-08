package org.glycoinfo.WURCSFramework.util.residuecontainer;

import java.util.ArrayList;
import java.util.LinkedList;

/*
 * ResidueData is keeping basic information of monosaccharide.
 * a2122h-1b_1-5_2*NCC/3=O is define below data
 * lst_DLconfiguration : D <- define from configuration of SkeletonCode
 * lst_coreName : Glc <- define from a2122h
 * char_ringSize : p <- define from 1-5
 * lst_mode : none <- define a deoxy position, a2122h is not have deoxy.
 * 					  this list should not call as local method.
 * 					  It is use for to define ResidueContainer.
 * str_NativeSubstituent : 2*NCC/3=O 
 * 
 * lst_mod is use to stack for deoxy position in monosaccharide.
 * this list must not include other modificaiton or substituent.
 * @author st
 */

public class ResidueData {
	
	private LinkedList<String> a_aDLConfigurations;
	private LinkedList<String> a_aTrivialNames;
	private char char_ringSize = ' ';
	private String str_superClass = "";
	private ArrayList<String> lst_mod;
	private ArrayList<String> lst_sub;
	private ArrayList<String> lst_NativeSubstituent;
	private char char_anomerSymbol = '?';
	private int int_anomerPos = -1;
	
	private boolean isOnic = false;
	private boolean isAlditol = false;
	private boolean isMotif = false;
	private boolean isAcidicSugar = false;
	private boolean isAldehydo = false;
	private boolean a_bIsAric = false;
	
	public ResidueData() {
		this.a_aDLConfigurations = new LinkedList<String>();
		this.a_aTrivialNames= new LinkedList<String>();
		this.lst_mod = new ArrayList<String>();
		this.lst_sub = new ArrayList<String>();
		this.lst_NativeSubstituent = new ArrayList<String>();
	}
	
	public ResidueData(String _str_DLconfiguration, char _char_ringSize, String _str_coreName) {
		this.a_aDLConfigurations = new LinkedList<String>();
		this.a_aTrivialNames = new LinkedList<String>();
		this.lst_mod = new ArrayList<String>();
		this.lst_sub = new ArrayList<String>();
		this.lst_NativeSubstituent = new ArrayList<String>();
		
		this.a_aDLConfigurations.addLast(_str_DLconfiguration.toUpperCase());
		this.a_aTrivialNames.addLast(_str_coreName);
		this.char_ringSize = _char_ringSize;
	}
	
	
	public LinkedList<String> getDLconfiguration() {
		return this.a_aDLConfigurations;
	}
	
	public LinkedList<String> getCommonName() {
		return this.a_aTrivialNames;
	}
	
	public char getRingSize() {
		return this.char_ringSize;
	}
	
	public String getSuperClass() {
		return this.str_superClass;
	}
	
	public ArrayList<String> getModification() {
		return this.lst_mod;
	}
	
	public ArrayList<String> getSubstituent() {
		return this.lst_sub;
	}
	
	public ArrayList<String> getSubModList() {
		ArrayList<String> ret = new ArrayList<String>();
		
		for(String s : getModification()) ret.add(s);
		for(String s : getSubstituent()) ret.add(s);
		
		return ret;
	}
	
	public ArrayList<String> getNativeSubstituent() {
		return this.lst_NativeSubstituent;
	}
	
	public char getAnomerSymbol() {
		return this.char_anomerSymbol;
	}
	
	public int getAnomerPosition() {
		return this.int_anomerPos;
	}
	
	/***/
	
	public void addCommonName(String _str_core) {
		this.a_aTrivialNames.addLast(_str_core);
	}

	public void addDLconfiguration(String _str_DLconfiguration) {
		this.a_aDLConfigurations.addLast(_str_DLconfiguration.toUpperCase());
	}
	
	public void addModification(String str_mod) {
		this.lst_mod.add(str_mod);
	}
	
	public void addSubstituent(String str_sub) { 
		this.lst_sub.add(str_sub);
	}
	
	/***/

	public boolean isMotif() {
		return this.isMotif;
	}
	
	public boolean isAlditol() {
		return this.isAlditol;
	}

	public boolean isAcidicSugar() {
		return this.isAcidicSugar;
	}
	
	public boolean isAldehydo() {
		return this.isAldehydo;
	}

	public boolean isONIC() {
		return this.isOnic;
	}
	
	public boolean isARIC() {
		return this.a_bIsAric;
	}
	
	/***/
	
	public void setSuperClass(String _str_superClass) {
		this.str_superClass = _str_superClass;
	}
	
	public void addNativeSubstituent(String _str_ano) {
		this.lst_NativeSubstituent.add(_str_ano);
	}
	
	public void setRingSize(char _char_ringSize) {
		this.char_ringSize = _char_ringSize;
	}
	
	public void setMotif(boolean _isMotif) {
		this.isMotif = _isMotif;
	}
	
	public void setAlditol(boolean _isAlditol) {
		this.isAlditol = _isAlditol;
	}
	
	public void setAldehydo(boolean _isAldehyde) {
		this.isAldehydo = _isAldehyde;
	}
	
	public void setAcidicSugar(boolean _isAcidicSugar) {
		this.isAcidicSugar = _isAcidicSugar;
	}
	
	public void setAnomerSymbol(char _char_AnomerSymbol) {
		this.char_anomerSymbol = _char_AnomerSymbol;
	}
	
	public void setAnomerPosition(int _int_AnomerPos) {
		this.int_anomerPos = _int_AnomerPos;
	}
	
	public void setModification(ArrayList<String> _lst_mod) {
		this.lst_mod = _lst_mod;
	}
	
	public void setSubstituent(ArrayList<String> _lst_sub) {
		this.lst_sub = _lst_sub;
	}

	public void setONIC(boolean _isONIC) {
		this.isOnic = _isONIC;
	}
	
	public void setARIC(boolean _a_bIsARIC){
		this.a_bIsAric = _a_bIsARIC;
		if(this.isOnic) this.isOnic = false;
	}
}
