package org.glycoinfo.WURCSFramework.util.oldUtil;

import java.util.ArrayList;

public class CompositSaccharideAnalyzer {

	ArrayList<String> a_aConstitutions = new ArrayList<String>();
	ArrayList<String> a_aConfigurations = new ArrayList<String>();

	String a_sIUPACName = "";
	String a_sMotif = "";

	public ArrayList<String> getConstitutions() {
		return this.a_aConstitutions;
	}
	
	public ArrayList<String> getConfigurations() {
		return this.a_aConfigurations;
	}
	
	public String getIUPACName() {
		return this.a_sIUPACName;
	}
	
	public String getMotif() {
		return this.a_sMotif;
	}
	
	public boolean isCompositSugar() {
		if(this.a_aConfigurations.size() == 2 && this.a_aConfigurations.size() == 2) return true;
		return false;
	}
	
	public void start(String a_sIUPACName) {
		this.a_sIUPACName = a_sIUPACName;
		if(a_sIUPACName.contains("-"))
			this.checkComposit(a_sIUPACName);
		if(a_sIUPACName.length() == 6)
			this.checkComposit(a_sIUPACName);

		return;
	}
	
	private void checkComposit(String a_sIUPACName) {
		for(String a_sUnit : a_sIUPACName.split("-")) {
			if(a_sUnit.equals("d") || a_sUnit.equals("l"))
				this.a_aConfigurations.add(a_sUnit);
			if(a_sUnit.endsWith("?"))
				this.a_aConfigurations.add("x");
			if(a_sUnit.length() == 3)
				this.a_aConstitutions.add(a_sUnit);
			if(a_sUnit.length() == 6) 
				this.motifChecker(a_sUnit);
		}
		return;
	}
	
	private void motifChecker(String a_sMonosaccharide) {
		String a_sTrivialName;
		String a_sMotif;
		
		a_sTrivialName = a_sMonosaccharide.substring(0, 3);
		a_sMotif = a_sMonosaccharide.substring(3, 6);
		
		this.a_aConstitutions.add(a_sTrivialName);
		this.a_sMotif = a_sMotif;
	}
}