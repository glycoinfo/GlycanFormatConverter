package org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph;


import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;

/**
 * Class for MAP code conversion from Substituent name in GlycoCT
 * @author MasaakiMatsubara
 *
 */
public enum SubstituentTypeToMAP {
	// For single
	ANHYDRO				("anhydro",	"",	"", null, "O", "O"),
	EPOXY				("epoxy",	"",	"", null, "O", "O"),	// same as anhydro
	LACTONE				("lactone",	"",	"", null, "O", "O"),	// same as anhydro
	ACYL 				("acyl",		"CR/2=O", null, null, "C", null),
	ACETYL				("acetyl",				"CC/2=O",	null, null, "C", null),
	BROMO				("bromo",				"Br",		null, null, "Br", null),
	CHLORO				("chloro",				"Cl",		null, null, "Cl", null),
	ETHYL				("ethyl",				"CC",		null, null, "C", null),
	FLOURO				("fluoro",				"F",		null, null, "F", null),
	FORMYL				("formyl",				"C=O",		null, null, "C", null),
	GLYCOLYL			("glycolyl",			"CCO/2=O",	null, null, "C", null),
	HYDROXYMETHYL		("hydroxymethyl",		"CO",		null, null, "C", null),
	IODO				("iodo",				"I",		null, null, "I", null),
	METHYL				("methyl",				"C",		null, null, "C", null),
	N_ACETYL			("n-acetyl",			"NCC/3=O",	null, null, "N", null),
	N_ALANINE			("n-alanine",			"NCC^XC/4N/3=O",	null, null, "N", null),
	N_DIMETHYL			("n-dimethyl",			"NC/2C",	null, null, "N", null),
	N_FORMYL			("n-formyl",			"NC=O",		null, null, "N", null),
	N_GLYCOLYL			("n-glycolyl",			"NCCO/3=O",	null, null, "N", null),
	N_METHYL			("n-methyl",			"NC",		null, null, "N", null),
	N_SUCCINATE			("n-succinate",			"NCCCCO/6=O/3=O",	null, null, "N", null),
	N_TRIFLOUROACETYL	("n-triflouroacetyl",	"NCCF/4F/4F/3=O",	null, null, "N", null),
	NITRATE				("nitrate",				"C=O/2=O",	null, null, "C", null),
	R_LACTATE			("(r)-lactate",			"CC^RC/3O/2=O",		null, null, "C", null),
	S_LACTATE			("(s)-lactate",			"CC^SC/3O/2=O",		null, null, "C", null),
	THIO				("thio",				"S",		null, null, "S", null),
	AMIDINO				("amidino",				"CN/2=N",	null, null, "C", null),
	N_AMIDINO			("n-amidino",			"NCN/3=N",	null, null, "N", null),
	CARBOXYMETHYL		("carboxymethyl",		"CCO/3=O",	null, null, "C", null),
	R_CARBOXYMETHYL		("(r)-carboxymethyl",	"?*",		null, null, "?", null), // no chirality
	S_CARBOXYMETHYL		("(s)-carboxymethyl",	"?*",		null, null, "?", null), // no chirality
	R_CARBOXYETHYL		("(r)-carboxyethyl",	"C^RCO/3=O/2C",		null, null, "C", null),
	S_CARBOXYETHYL		("(s)-carboxyethyl",	"C^SCO/3=O/2C",		null, null, "C", null),
	N_METHYLCARBAMOYL	("n-methyl-carbamoyl",	"CNC/2=O",	null, null, "C", null),
	PHOSPHO_CHOLINE		("phospho-choline",		"P^XOCCNC/6C/6C/2O/2=O",	null, null, "P", null),
	X_LACTATE			("(x)-lactate",			"CC^XC/3O/2=O",		null, null, "C", null),
	R_1_HYDROXYMETHYL	("(r)-1-hydroxymethyl",	"?*",	null, null, "?", null), // no chirality
	S_1_HYDROXYMETHYL	("(s)-1-hydroxymethyl",	"?*",	null, null, "?", null), // no chirality
	// For double
	PYRUVATE			("pyruvate",		null,	"C^X*/2CO/4=O/2C",	null,  "C", "C"),	// it can be no chirality
	R_PYRUVATE			("(r)-pyruvate",	null,	"C^R*/2CO/4=O/2C",	false, "C", "C"),
	S_PYRUVATE			("(s)-pyruvate",	null,	"C^R*/2CO/4=O/2C",	true,  "C", "C"),
	// For both of single or double
	AMINO				("amino",			"N",				"N*",	null,  "N", "N"),
	ETHANOLAMINE		("ethanolamine",	"NCCO",				"NCC*",	false, "N", "C"),
	IMINO				("imino",			"=N",				"=N*",	false, "N", "N"),
	SUCCINATE			("succinate",		"CCCCO/5=O/2=O",	"CCCC*/5=O/2=O",	null, "C", "C"),
	N_SULFATE			("n-sulfate",		"NSO/3=O/3=O",		"NS*/3=O/3=O",		true, "N", "S"),
	PHOSPHATE			("phosphate",		"PO/2O/2=O",		"P^X*/2O/2=O",		null, "P", "P"),
	PYROPHOSPHATE		("pyrophosphate",	"P^XOPO/4O/4=O/2O/2=O",				"P^XOP^X*/4O/4=O/2O/2=O",				null, "P", "P"),
	TRIPHOSPHATE		("triphosphate",	"P^XOP^XOPO/6O/6=O/4O/4=O/2O/2=O",	"P^XOP^XOP^X*/6O/6=O/4O/4=O/2O/2=O",	null, "P", "P"),
	SULFATE				("sulfate",			"SO/2=O/2=O",		"S*/2=O/2=O",		null, "S", "S"),
	PHOSPHO_ETHANOLAMINE	("phospho-ethanolamine",	"P^XOCCN/2O/2=O",				"NCCOP^X*/6O/6=O",				true, "P", "N"),
	DIPHOSPHO_ETHANOLAMINE	("diphospho-ethanolamine",	"P^XOP^XOCCN/4O/4=O/2O/2=O",	"NCCOP^XOP^X*/8O/8=O/6O/6=O",	true, "P", "N");
	//	X_PYRUVATE			("(x)-pyruvate","--"),

	private String m_strName;
	private String m_strMAPSingle;
	private String m_strMAPDouble;
	private Boolean m_bIsSwapCarbonPositions;
	private String m_strHeadAtom;
	private String m_strTailAtom;

	/**
	 * private constructor
	 * @param a_strName				substituent name
	 * @param a_strMAPSingle		MAP code in MOD
	 * @param a_strMAPDouble		MAP code in LIN
	 * @param a_bIsSwapCarbonPosition	MAP code for MLU is swap carbon position
	 */
	private SubstituentTypeToMAP(
			String a_strName,
			String a_strMAPSingle,
			String a_strMAPDouble,
			Boolean a_bIsSwapCarbonPosition,
			String a_cHeadAtom,
			String a_cTailAtom )
	{
		this.m_strName = a_strName;
		this.m_strMAPSingle = a_strMAPSingle;
		this.m_strMAPDouble = a_strMAPDouble;
		this.m_bIsSwapCarbonPositions = a_bIsSwapCarbonPosition;
		this.m_strHeadAtom = a_cHeadAtom;
		this.m_strTailAtom = a_cTailAtom;
	}

	public static SubstituentTypeToMAP forName( String a_strName ) throws WURCSExchangeException
	{
		String t_strName = a_strName.toUpperCase();
		for ( SubstituentTypeToMAP t_objType : SubstituentTypeToMAP.values() )
		{
			if ( t_objType.m_strName.equalsIgnoreCase(t_strName) )
			{
				return t_objType;
			}
		}
		throw new WURCSExchangeException("\""+t_strName+"\" is not found.");
	}

	public String getName()
	{
		return this.m_strName;
	}

	public String getMAPSingle()
	{
		return this.m_strMAPSingle;
	}

	public String getMAPDouble()
	{
		return this.m_strMAPDouble;
	}

	public Boolean isSwapCarbonPositions() {
		return this.m_bIsSwapCarbonPositions;
	}

	public String getHeadAtom()
	{
		return this.m_strHeadAtom;
	}

	public String getTailAtom()
	{
		return this.m_strTailAtom;
	}

}
