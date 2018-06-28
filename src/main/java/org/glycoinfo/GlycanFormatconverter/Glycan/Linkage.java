package org.glycoinfo.GlycanFormatconverter.Glycan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class Linkage {

	private ArrayList<Integer> childLinkages = new ArrayList<Integer>();
	private ArrayList<Integer> parentLinkages = new ArrayList<Integer>();
	
	private LinkageType parentType = LinkageType.UNVALIDATED;
	private LinkageType childType = LinkageType.UNVALIDATED;


	private double parentProbabilityLower = 1.0D;
	private double parentProbabilityUpper = 1.0D;

	private double childProbabilityLower = 1.0D;
	private double childProbabilityUpper = 1.0D;
	
	public static final int UNKNOWN_POSITION = -1;
	
	public Linkage() {
		this.clear();
	}
	
	public Linkage (LinkedList<Integer> _child) {
		this.clear();
		this.setChildLinkages(_child);
	}
	
	public Linkage (LinkedList<Integer> _child, LinkedList<Integer> _parent) {
		this.clear();
		this.setChildLinkages(_child);
		this.setParentLinkages(_parent);
	}
	
	public void clear() {
		this.childLinkages.clear();
		this.parentLinkages.clear();
	}
	
	public boolean addChildLinkage(int _position) {
		if(!this.childLinkages.contains(_position)) {
			return this.childLinkages.add(_position);
		}
		return false;
	}
	
	public boolean addParentLinkage(int _position) {
		if(!this.parentLinkages.contains(_position)) {
			return this.parentLinkages.add(_position);
		}
		return false;
	}
	
	public ArrayList<Integer> getChildLinkages() {
		return this.childLinkages;
	}
	
	public ArrayList<Integer> getParentLinkages() {
		return this.parentLinkages;
	}
	
	public void setChildLinkages(Collection<Integer> linkedList) {
		if(linkedList == null) {
			
		}
		this.childLinkages.clear();
		for(Iterator<Integer> iterPosition = linkedList.iterator(); iterPosition.hasNext();) {
			this.addChildLinkage(iterPosition.next());
		}
	}
	
	public void setParentLinkages(Collection<Integer> linkedList) {
		if(linkedList == null) {
			
		}
		this.parentLinkages.clear();
		for(Iterator<Integer> iterPosition = linkedList.iterator(); iterPosition.hasNext();) {
			this.addParentLinkage(iterPosition.next());
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
