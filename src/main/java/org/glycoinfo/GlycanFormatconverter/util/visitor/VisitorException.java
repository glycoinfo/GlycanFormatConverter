package org.glycoinfo.GlycanFormatconverter.util.visitor;

public class VisitorException extends Exception {

	protected String message;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VisitorException (String _message) {
		super(_message);
		this.message = _message;
	}
	
	public VisitorException (String _message, Throwable _throwable) {
		super(_message, _throwable);
		this.message = _message;
	}

	public String getMessage () {
		return this.message;
	}
}
