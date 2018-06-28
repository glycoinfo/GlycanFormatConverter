package org.glycoinfo.GlycanFormatconverter.Glycan;

import org.glycoinfo.GlycanFormatconverter.util.visitor.ContainerVisitor;
import org.glycoinfo.GlycanFormatconverter.util.visitor.VisitorException;

public class Substituent extends Node{
	private Linkage firstPosition = null;
	private Linkage secondPosition = null;
	
	private SubstituentInterface subInterface;
	
	public Substituent(SubstituentInterface enumSub) {
		this.setTemplate(enumSub);
	}
	
	public Substituent(SubstituentInterface _enumTemplate, Linkage _firstPosition) {
		this.setTemplate(_enumTemplate);
		this.firstPosition = _firstPosition;
	}
	
	public Substituent(SubstituentInterface _enumTemplate, Linkage _firstPosition, Linkage _secondPosition) {
		this.setTemplate(_enumTemplate);
		this.firstPosition = _firstPosition;
		this.secondPosition = _secondPosition;
	}
	
	public SubstituentInterface getSubstituent() {
		return this.subInterface;
	}
	
	public void setTemplate(SubstituentInterface _template) {
		this.subInterface = _template;
	}
	
	public void setFirstPosition(Linkage _firstPosition) throws GlycanException {
		if(_firstPosition == null) {
			throw new GlycanException("Invalid value for attach position");
		}
		this.firstPosition = _firstPosition;
	}
	
	public void setSecondPosition(Linkage _secondPosition) throws GlycanException {
		if(_secondPosition == null) {
			this.secondPosition = null;
		}else {
			this.secondPosition = _secondPosition;			
		}
	}
	
	public String getNameWithIUPAC() {
		return subInterface.getIUPACnotation();
	}
	
	public Linkage getFirstPosition() {
		return this.firstPosition;
	}
	
	public Linkage getSecondPosition() {
		return this.secondPosition;
	}
	
	public Substituent copy() throws GlycanException {
		Substituent ret = new Substituent(this.subInterface);
		
		if (firstPosition != null) ret.setFirstPosition(firstPosition.copy());
		if (secondPosition != null) ret.setSecondPosition(secondPosition.copy());

		return ret;
	}

	@Override
	public void accept(ContainerVisitor _visitor) throws VisitorException {
		_visitor.visit(this);
	}
}