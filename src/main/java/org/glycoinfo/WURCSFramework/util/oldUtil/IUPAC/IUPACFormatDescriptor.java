package org.glycoinfo.WURCSFramework.util.oldUtil.IUPAC;

public enum IUPACFormatDescriptor {

	CONDENSED("condensed"),
	SHORT("short"),
	EXTENDED("extended");
	
	private String a_sForm;
	
	private IUPACFormatDescriptor(String _a_sForm) {
		this.a_sForm = _a_sForm;
	}
	
	public String getFormat() {
		return this.a_sForm;
	}
	
	public static IUPACFormatDescriptor forFormat(String _a_sFormat) {
		if(_a_sFormat.equals(IUPACFormatDescriptor.CONDENSED))
			return IUPACFormatDescriptor.CONDENSED;
		if(_a_sFormat.equals(IUPACFormatDescriptor.SHORT))
			return IUPACFormatDescriptor.SHORT;
		if(_a_sFormat.equals(IUPACFormatDescriptor.EXTENDED))
			return IUPACFormatDescriptor.EXTENDED;

		return null;
	}
}
