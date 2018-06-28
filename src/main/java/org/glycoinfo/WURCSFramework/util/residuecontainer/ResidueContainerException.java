package org.glycoinfo.WURCSFramework.util.residuecontainer;

import org.glycoinfo.WURCSFramework.util.WURCSException;

public class ResidueContainerException extends WURCSException{
	private static final long serialVersionUID = 1L;

	public ResidueContainerException(String a_strMessage) {
		super(a_strMessage);
	}
	
	public ResidueContainerException(String a_strMessage, Throwable a_objThrowable) {
		super(a_strMessage, a_objThrowable);
		// TODO Auto-generated constructor stub
	}
}
