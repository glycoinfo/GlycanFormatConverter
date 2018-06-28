package org.glycoinfo.GlycanFormatconverter.io.IUPAC;

import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;

public class IUPACExporter {

	IUPACExtendedExporter iee;
	IUPACCondensedExporter ice;
	IUPACShortExporter ise;
	
	public String getExtended () {
		return iee.getIUPACExtended();
	}
	
	public String getExtendedWithGreek () {
		return iee.toGreek();
	}
	
	public String getCondensed () {
		return ice.getIUPACCondensed();
	}
	
	public String getShort () {
		return ise.getIUPACShort();
	}
	
	public void start (GlyContainer _glyCo) throws ConverterExchangeException, GlycanException {

		//TODO : ここで全てのノードをソートしてしまう

		/* Extended */
		iee = new IUPACExtendedExporter();
		iee.start(_glyCo);

		/* Condensed */
		ice = new IUPACCondensedExporter(false);
		ice.start(_glyCo);

		/* Short */
		ise = new IUPACShortExporter();
		ise.start(_glyCo);

	}
}
