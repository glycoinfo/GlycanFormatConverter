package org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph;


import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;

/**
 * Class of relative configuartion BaseType string
 * @author MasaakiMatsubara
 *
 */
public enum BaseTypeForRelativeConfiguration
{
	// basetype jokers
	// "3" and "4" in relative configuration are replace from "1" and "2" in "D" absolute configuration, respectively
	XGRO("xgro","x"), // "xgro" has no relative configuration
	XTHR("xthr","34"),
	XERY("xery","44"),
	XARA("xara","344"),
	XRIB("xrib","444"),
	XLYX("xlyx","334"),
	XXYL("xxyl","434"),
	XALL("xall","4444"),
	XALT("xalt","3444"),
	XMAN("xman","3344"),
	XGLC("xglc","4344"),
	XGUL("xgul","4434"),
	XIDO("xido","3434"),
	XTAL("xtal","3334"),
	XGAL("xgal","4334");

	private String m_strName;
	private String m_strStereo;

	private BaseTypeForRelativeConfiguration( String a_strName, String a_strStereo )
	{
		this.m_strName = a_strName;
		this.m_strStereo = a_strStereo;
	}

	public String getName()
	{
		return this.m_strName;
	}

	public String getStereoCode()
	{
		return this.m_strStereo;
	}

	public static BaseTypeForRelativeConfiguration forName( String a_strName ) throws WURCSExchangeException
	{
		String t_strName = a_strName.toUpperCase();
		for ( BaseTypeForRelativeConfiguration t_objBasetype : BaseTypeForRelativeConfiguration.values() )
		{
			if ( t_objBasetype.m_strName.equalsIgnoreCase(t_strName) )
			{
				return t_objBasetype;
			}
		}
		throw new WURCSExchangeException("Invalid value for basetype");
	}

	public static BaseTypeForRelativeConfiguration forStereoCode( String a_strCode ) throws WURCSExchangeException
	{
		String t_strName = a_strCode.toUpperCase();
		for ( BaseTypeForRelativeConfiguration t_objBasetype : BaseTypeForRelativeConfiguration.values() )
		{
			if ( t_objBasetype.m_strStereo.equalsIgnoreCase(t_strName) )
			{
				return t_objBasetype;
			}
		}
		throw new WURCSExchangeException("Invalid value for basetype stereo code");
	}

}
