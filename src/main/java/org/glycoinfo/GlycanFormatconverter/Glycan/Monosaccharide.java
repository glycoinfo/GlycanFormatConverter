package org.glycoinfo.GlycanFormatconverter.Glycan;

import org.glycoinfo.GlycanFormatconverter.util.visitor.ContainerVisitor;
import org.glycoinfo.GlycanFormatconverter.util.visitor.VisitorException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class Monosaccharide extends Node {
	private AnomericStateDescriptor enumAnomer;
	private int anomerPosition;
	private LinkedList<String> stereos = new LinkedList<String>();
	private SuperClass enumSuperClass;

	public static final int UNKNOWN_RING = -1;
	public static final int OPEN_CHAIN = 0;
	
	private int ringStart;
	private int ringEnd;
	private ArrayList<GlyCoModification> modifications = new ArrayList<GlyCoModification>();
	
	public Monosaccharide() {
		super();
		this.ringStart = -1;
		this.ringEnd = -1;
		this.modifications.clear();
		this.stereos.clear();
	}
	
	public Monosaccharide(AnomericStateDescriptor _anomer, SuperClass _superclass) throws GlycanException {
		if (_anomer == null) {
			throw new GlycanException ("Invalid nomeric state");
		}
		this.enumAnomer = _anomer;
		if(_superclass == null) {
			throw new GlycanException ("Invalid superclass");
		}
		this.enumSuperClass = _superclass;
		this.ringStart = -1;
		this.ringEnd = -1;
		this.modifications.clear();
		this.stereos.clear();
	}
	
	public void setAnomer(AnomericStateDescriptor _anomer) throws GlycanException {
		if(_anomer == null) {
			throw new GlycanException ("Invalid anomeric state");
		}
		this.enumAnomer = _anomer;
	}

	public void setSuperClass(SuperClass _superclass) throws GlycanException {
		if(_superclass == null) {
			throw new GlycanException ("Invalid super class");
		}
		this.enumSuperClass = _superclass;
	}
	
	public void setAnomericPosition(int _anomericPosition) {
		this.anomerPosition = _anomericPosition;
	}
	
	public SuperClass getSuperClass() {
		return this.enumSuperClass;
	}
	
	public AnomericStateDescriptor getAnomer() {
		return this.enumAnomer;
	}
	
	public int getAnomericPosition() {
		return this.anomerPosition;
	}
	
	public void setRing(int _start, int _end) throws GlycanException {
		if( (_start == 0 || _end == 0) && ((_start + _end) > 0)) {
			
		}
		if(_start > _end && _end > Monosaccharide.UNKNOWN_RING) {
			throw new GlycanException("start point bigger than end point");
		}
		if(_start < Monosaccharide.UNKNOWN_RING) {
			throw new GlycanException("start point should not -1");
		}
		if(_end < Monosaccharide.UNKNOWN_RING) {
			throw new GlycanException("end point should not -1");
		}
		this.ringStart = _start;
		this.ringEnd = _end;
	}
	
	public void setRingStart(int _start) throws GlycanException {
		if(this.ringStart > _start) {
			throw new GlycanException("Invalid start ring position");
		}
		if(_start < Monosaccharide.UNKNOWN_RING) {
			throw new GlycanException("Invalid start ring position");
		}
		this.ringStart = _start;
	}
	
	public void setRingEnd(int _end) throws GlycanException {
		if(this.ringEnd > _end) {
			throw new GlycanException("Invalid end ring position");
		}
		if(_end < Monosaccharide.UNKNOWN_RING) {
			throw new GlycanException("Invalid end ring position");
		}
		this.ringEnd = _end;
	}
	
	public int getRingStart() {
		return this.ringStart;
	}
	
	public int getRingEnd() {
		return this.ringEnd;
	}
	
	public ArrayList<GlyCoModification> getModifications() {
		ArrayList<GlyCoModification> ret = new ArrayList<>();
		ret.addAll(this.modifications);
		return ret;
	}
	
	//public ArrayList<Substituent> getSubstituent() {
	//	return this.substituents;
	//}
	
	public boolean addModification(GlyCoModification _modification) {
		if (_modification == null) return false;
		if (!this.modifications.contains(_modification)) {
			return this.modifications.add(_modification);
		}
		return false;
	}
	
	public boolean removeModification(GlyCoModification _modificaiton) {
		return this.modifications.remove(_modificaiton);
	}
	
	public void setModification(ArrayList<GlyCoModification> _modifications) throws GlycanException {
		if(_modifications == null) {
			throw new GlycanException("Modificaition list is Null");
		}
		this.modifications.clear();
		for(Iterator<GlyCoModification> iterMod = _modifications.iterator(); iterMod.hasNext();) {
			this.modifications.add(iterMod.next());
		}
	}
	
	public void setStereos(LinkedList<String> _stereo) throws GlycanException {
		if (_stereo == null) { 
			throw new GlycanException("null is not a valide set of basetypes");
		}
		this.stereos.clear();
		for (Iterator<String> iterStereo = _stereo.iterator(); iterStereo.hasNext();) {
			this.addStereo(iterStereo.next());
		}
		this.stereos = _stereo;
	}
	
	public LinkedList<String> getStereos() {
		LinkedList<String> ret = new LinkedList<>();
		ret.addAll(stereos);
		
		return ret;
	}
	
	public boolean addStereo(String _stereo) throws GlycanException {
		 if (_stereo == null) { 
			 throw new GlycanException("Basetype can not be null");
		 }
		 return this.stereos.add(_stereo);
	}
	
	public boolean removeStereo(String _stereo) {
		return this.stereos.remove(_stereo);
	}
	
	public boolean hasModification(GlyCoModification _modification) {
		return this.modifications.contains(_modification);
	}
	
	public boolean hasModification(GlyCoModification _modification, Integer _positionOne) {
		for(GlyCoModification m : this.modifications) {
			if(m.getModificationTemplate().equals(_modification.getModificationTemplate()) &&
					m.getPositionOne() == _positionOne) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasModification(GlyCoModification _modification, Integer _positionOne, Integer _positionTwo) {
		for(GlyCoModification m : this.modifications) {
			if(m.getModificationTemplate().equals(_modification.getModificationTemplate()) && 
					m.getPositionOne() == _positionOne && 
					m.getPositionTwo() == _positionTwo) {
				return true;
			}
		}
		return false;
	}
	
	public void accept (ContainerVisitor _visitor) throws VisitorException {
		_visitor.visit(this);
	}
	
	public Monosaccharide copy() throws GlycanException {
		Monosaccharide ret = null;
		ret = new Monosaccharide(this.enumAnomer, this.enumSuperClass);
		ret.setAnomericPosition(this.getAnomericPosition());
		
		ret.setRingStart(this.ringStart);
		ret.setRingEnd(this.ringEnd);
		//ret.setRing(this.ringStart, this.ringEnd);
		
		for (Iterator<String> iterStereo = this.stereos.iterator(); iterStereo.hasNext();) {
			ret.addStereo(iterStereo.next());
		}
		
		for(Iterator<GlyCoModification> iterMod = this.modifications.iterator(); iterMod.hasNext();) {
			ret.addModification(iterMod.next().copy());
		}

		for(Iterator<Edge> iterEdge = this.getChildEdges().iterator(); iterEdge.hasNext();) {
			Edge childEdge = iterEdge.next();
			Substituent sub = (Substituent) childEdge.getSubstituent();
			if (sub == null) continue;
			if (sub instanceof GlycanRepeatModification) continue;
			if (childEdge.getChild() != null) continue;
			//if (sub.getSubstituent() instanceof SubstituentTemplate) {
			Edge copyEdge = childEdge.copy();
			Substituent copySub = sub.copy();
			copyEdge.setSubstituent(copySub);
			copyEdge.setParent(ret);
			ret.addChildEdge(copyEdge);
			//}
		}

		ret.setStereos(stereos);

		return ret;
	}
}
