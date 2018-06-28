package org.glycoinfo.GlycanFormatconverter.Glycan;

import org.glycoinfo.GlycanFormatconverter.util.visitor.ContainerVisitor;
import org.glycoinfo.GlycanFormatconverter.util.visitor.Visitable;
import org.glycoinfo.GlycanFormatconverter.util.visitor.VisitorException;

public class GlyCoModification implements Visitable {
	
	private Integer positionOne = GlyCoModification.UNKNOWN_POSITION;
	private Integer positionTwo = null;
	public static final int UNKNOWN_POSITION = 0;
	
	private ModificationTemplate enumModificaiton;
	
	public GlyCoModification(ModificationTemplate symbol, int position) throws GlycanException {
		this.enumModificaiton = symbol;
		this.setPositionOne(position);
	}
	
	public GlyCoModification(ModificationTemplate symbol, int positionOne, int positionTwo) throws GlycanException {
		this.enumModificaiton = symbol;
		this.setPositionOne(positionOne);
		this.setPositionTwo(positionTwo);
	}
	
	public GlyCoModification(char symbol, int positionOne) throws GlycanException {
		this.enumModificaiton = ModificationTemplate.forCarbon(symbol);
		this.setPositionOne(positionOne);
	}
	
	public GlyCoModification(char symbol, int positionOne, int positionTwo) throws GlycanException {
		this.enumModificaiton = ModificationTemplate.forCarbon(symbol);
		this.setPositionOne(positionOne);
		this.setPositionTwo(positionTwo);
	}
	
	public void setPositionOne(Integer _position) throws GlycanException {
		if(_position == null) {
			throw new GlycanException ("Invalid value for attach position");
		}
		if(_position < GlyCoModification.UNKNOWN_POSITION) {
			throw new GlycanException ("Invalid value for attach position");
		}
		this.positionOne = _position;
	}
	
	public Integer getPositionOne () {
		return this.positionOne;
	}
	
	public void setPositionTwo(Integer _position) throws GlycanException {
		if(_position == null) {
			this.positionTwo = null;
		}else {
			if(_position < GlyCoModification.UNKNOWN_POSITION) {
				throw new GlycanException ("Invalid value for attach position");
			}
			this.positionTwo = _position;			
		}
	}
	
	public Integer getPositionTwo() {
		return this.positionTwo;
	}
	
	public ModificationTemplate getModificationTemplate() {
		return this.enumModificaiton;
	}
	
	public boolean hasPositionOne() {
		if(this.positionOne == null) return false;
		else return true;
	}
	
	public boolean hasPositionTwo() {
		if(this.positionTwo == null) return false;
		else return true;
	}
	
	public GlyCoModification copy() throws GlycanException {
		GlyCoModification ret = new GlyCoModification(this.enumModificaiton, this.positionOne);
		
		if (!hasPositionTwo()) {
			ret.setPositionTwo(this.positionTwo);
		}
		
		return ret;
		/*if(!hasPositionTwo()) {
			return new GlyCoModification(this.enumModificaiton, this.positionOne);
		}else {
			return new GlyCoModification(this.enumModificaiton, this.positionOne, this.positionTwo);
		}*/
	}

	@Override
	public void accept(ContainerVisitor _visitor) throws VisitorException {
		_visitor.visit(this);
	}
}