package org.glycoinfo.WURCSFramework.util.oldUtil;

import org.glycoinfo.GlycanFormatconverter.util.TrivialName.TrivialNameException;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.oldUtil.Carbbank.CarbBankNameConverter;
import org.glycoinfo.WURCSFramework.util.oldUtil.Carbbank.ConverterCarbBankException;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainer;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.GRES;

import java.util.ArrayList;

public class GRESToTrivialName {
	
	ResidueContainer a_oRC;
	
	public String getIUPACExtendedNotation() {
		String a_sIUPAC = this.a_oRC.getIUPACExtendedNotation();

		if(a_sIUPAC.contains("5Ac") && a_sIUPAC.contains("Neu")) 
			a_sIUPAC = a_sIUPAC.replace("5Ac", "Ac");
		if(a_sIUPAC.contains("5Gc") && a_sIUPAC.contains("Neu"))
			a_sIUPAC = a_sIUPAC.replace("5Gc", "Gc");
		
		return a_sIUPAC;
	}
	
	public String getIUPACCondensedNotation() {
		String a_sIUPAC = this.a_oRC.getIUPACCondensedNotaiton();

		if(a_sIUPAC.contains("5Ac") && a_sIUPAC.contains("Neu")) 
			a_sIUPAC = a_sIUPAC.replace("5Ac", "Ac");
		if(a_sIUPAC.contains("5Gc") && a_sIUPAC.contains("Neu"))
			a_sIUPAC = a_sIUPAC.replace("5Gc", "Gc");
		
		return a_sIUPAC;
	}
	
	public String getCarbBankNotation() {
		String a_sCarbBank = this.a_oRC.getIUPACExtendedNotation();

		if(a_sCarbBank.contains("alpha")) {
			a_sCarbBank = a_sCarbBank.replace("alpha", "a");
		}else if(a_sCarbBank.contains("beta")) {
			a_sCarbBank = a_sCarbBank.replace("beta", "b");
		}
		
		return a_sCarbBank;
	}
	
	public String getTrivialName() {
		String a_sTrivialName = this.a_oRC.getSugarName();
		
		return a_sTrivialName;
	}
	
	public String getConfiguration() {
		return this.a_oRC.getDLconfiguration().get(0);
	}
	
	public ArrayList<String> getModifications() {
		return this.a_oRC.getModification();
	}
	
	public void start(GRES a_oGRES) throws WURCSFormatException, ConverterExchangeException, TrivialNameException {
		GRESToResidueData a_oGRESToResidueData = new GRESToResidueData();
	
		a_oGRESToResidueData.start(a_oGRES);
		this.a_oRC = a_oGRESToResidueData.getResidueContainer();
			
		CarbBankNameConverter a_oCarbBankConverter = new CarbBankNameConverter();
		
		/** make CarbBank name and set in a ResidueContainer */
		this.a_oRC.setIUPACExtednedNotation(
				a_oCarbBankConverter.makeIUPACExtendedNotation(this.a_oRC));
		
		/** make trivial name and set in a ResidueContainer */
		this.a_oRC.setSugarName(
				a_oCarbBankConverter.makeCommonName(this.a_oRC));
		
		return;
	}
	
	public void start(String a_sResidueCode)
			throws WURCSException, ConverterExchangeException, ConverterCarbBankException, TrivialNameException {
		WURCSSequence2ToResidueContainer a_oWURCSSequence2ToResidueContainer = 
				new WURCSSequence2ToResidueContainer();
		
		String a_sWURCS = "WURCS=2.0/1,1,0/[" + a_sResidueCode + "]/1/";
		this.a_oRC = 
				a_oWURCSSequence2ToResidueContainer.start(a_sWURCS).getFirst();
	
		return;
	}
}
