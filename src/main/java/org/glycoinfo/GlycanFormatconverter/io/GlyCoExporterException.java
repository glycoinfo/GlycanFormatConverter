package org.glycoinfo.GlycanFormatconverter.io;

public class GlyCoExporterException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GlyCoExporterException(String _message) {
		super(_message);
	}
	
	public GlyCoExporterException(String _message, Throwable _throwable) {
		super(_message, _throwable);
	}
}