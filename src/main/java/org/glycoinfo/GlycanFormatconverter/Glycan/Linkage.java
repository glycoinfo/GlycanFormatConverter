package org.glycoinfo.GlycanFormatconverter.Glycan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class Linkage {

	private ArrayList<Integer> childLinkages;
	private ArrayList<Integer> parentLinkages;
	
	private LinkageType parentType = LinkageType.UNVALIDATED;
	private LinkageType childType = LinkageType.UNVALIDATED;

	private double parentProbabilityLower = 1.0D;
	private double parentProbabilityUpper = 1.0D;

	private double childProbabilityLower = 1.0D;
	private double childProbabilityUpper = 1.0D;
	
	public static final int UNKNOWN_POSITION = -1;
	
	public Linkage() {
		this.childLinkages = new ArrayList<>();
		this.parentLinkages = new ArrayList<>();
	}
	
	public Linkage (LinkedList<Integer> _child) {
		this.setChildLinkages(_child);
	}
	
	public Linkage (LinkedList<Integer> _child, LinkedList<Integer> _parent) {
		this.setChildLinkages(_child);
		this.setParentLinkages(_parent);
	}
	
	public void addChildLinkage(int _position) {
		if(!this.childLinkages.contains(_position)) {
			this.childLinkages.add(_position);
		}
	}
	
	public void addParentLinkage(int _position) {
		if(!this.parentLinkages.contains(_position)) {
			this.parentLinkages.add(_position);
		}
	}
	
	public ArrayList<Integer> getChildLinkages() {
		return this.childLinkages;
	}
	
	public ArrayList<Integer> getParentLinkages() {
		return this.parentLinkages;
	}
	
	public void setChildLinkages(Collection<Integer> linkedList) {
		this.childLinkages.clear();
		for (Integer integer : linkedList) {
			this.addChildLinkage(integer);
		}
	}
	
	public void setParentLinkages(Collection<Integer> linkedList) {
		this.parentLinkages.clear();
		for (Integer integer : linkedList) {
			this.addParentLinkage(integer);
		}
	}
	
	public void setProbabilityLower(double _lower) {
		this.parentProbabilityLower = _lower;
	}
	
	public void setProbabilityUpper(double _upper) {
		this.parentProbabilityUpper = _upper;
	}

	public void setChildProbabilityLower (double _childLow) { this.childProbabilityLower = _childLow; }

	public void setChildProbabilityUpper (double _childUp) { this.childProbabilityUpper = _childUp; }

	public double getParentProbabilityLower() { return this.parentProbabilityLower; }
	
	public double getParentProbabilityUpper() {
		return this.parentProbabilityUpper;
	}

	public double getChildProbabilityLower () { return this.childProbabilityLower; }

	public double getChildProbabilityUpper () { return this.childProbabilityUpper; }

	public void setParentLinkageType(LinkageType _parentType) throws GlycanException { 
		if(_parentType == null) {
			throw new GlycanException("Invalid parent linkage type.");
		}
		this.parentType = _parentType;
	}
	
	public void setChildLinkageType(LinkageType _childType) throws GlycanException {
		if(_childType == null) {
			throw new GlycanException("Invalid parent linkage type.");
		}
		this.childType = _childType;
	}
	
	public LinkageType getParentLinkageType() {
		return this.parentType;
	}
	
	public LinkageType getChildLinkageType() {
		return this.childType;
	}
	
	public Linkage copy() throws GlycanException {
		Linkage ret = new Linkage();
		ret.setChildLinkages(getChildLinkages());
		ret.setParentLinkages(getParentLinkages());

		/**/
		ret.setParentLinkageType(getParentLinkageType());
		ret.setChildLinkageType(getChildLinkageType());

		/**/
		ret.setProbabilityLower(getParentProbabilityLower());
		ret.setProbabilityUpper(getParentProbabilityUpper());
		ret.setChildProbabilityLower(getChildProbabilityLower());
		ret.setChildProbabilityUpper(getChildProbabilityUpper());

		return ret;
	}
}
