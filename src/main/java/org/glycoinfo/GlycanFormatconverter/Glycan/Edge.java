package org.glycoinfo.GlycanFormatconverter.Glycan;

import java.util.ArrayList;
import java.util.Iterator;

import org.glycoinfo.GlycanFormatconverter.util.visitor.ContainerVisitor;
import org.glycoinfo.GlycanFormatconverter.util.visitor.Visitable;
import org.glycoinfo.GlycanFormatconverter.util.visitor.VisitorException;

public class Edge implements Visitable {
	private Node parent = null;
	private Node child = null;
	private ArrayList<Linkage> linkages = new ArrayList<Linkage>();
	private Node substituent = null;
	
	public void setParent(Node _parent) {
		this.parent = _parent;
	}
	
	public void setChild(Node _child) {
		this.child = _child;
	}
	
	public void setSubstituent(Node _substituent) {
		this.substituent = _substituent;
	}
	
	public Node getChild() {
		return this.child;
	}
	
	public Node getParent() {
		return this.parent;
	}
	
	public Node getSubstituent() {
		return this.substituent;
	}
	
	public void setGlycosidicLinkages(ArrayList<Linkage> _linkages) throws GlycanException {
		if(_linkages == null) {
			throw new GlycanException ("linkage is Null");
		}
		this.linkages.clear();
		for(Iterator<Linkage> iterLinkage = _linkages.iterator(); iterLinkage.hasNext();) {
			this.addGlycosidicLinkage(iterLinkage.next());
		}
		this.linkages = _linkages;
	}
	
	public ArrayList<Linkage> getGlycosidicLinkages() {
		ArrayList<Linkage> ret = new ArrayList<>();
		Iterator<Linkage> iterLIN = this.linkages.iterator();

		while (iterLIN.hasNext()) {
			Linkage lin = iterLIN.next();
			ret.add(lin);
		}

		return ret;
	}
	
	public boolean addGlycosidicLinkage(Linkage _linkage) throws GlycanException {
		if(_linkage == null) {
			throw new GlycanException ("Linkage is Null");
		}
		if(!this.linkages.contains(_linkage)) {
			return this.linkages.add(_linkage);
		}
		return false;
	}
	
	public boolean removeGlycosidicLinkage(Linkage _linkage) {
		if(this.linkages.contains(_linkage)) {
			return this.linkages.remove(_linkage);
		}
		return false;
	}
	
	public boolean isRepeat() {
		if(substituent == null) return false;
		if(!(substituent instanceof GlycanRepeatModification)) return false;
		GlycanRepeatModification cyclic = (GlycanRepeatModification) substituent;
		return (cyclic.getMaxRepeatCount() != 0 && cyclic.getMinRepeatCount() != 0);
	}
	
	public boolean isCyclic() {
		if(substituent == null) return false;
		if(!(substituent instanceof GlycanRepeatModification)) return false;
		GlycanRepeatModification cyclic = (GlycanRepeatModification) substituent;
		return (cyclic.getMaxRepeatCount() == 0 && cyclic.getMinRepeatCount() == 0);
	}
	
	public boolean isReverseEdge () {
		if(substituent != null) {
			if(substituent instanceof GlycanRepeatModification) return false;
			if(child != null) return true;
		}
		if(linkages.size() > 1) return false;
		if(child == null) return false;
				
		Monosaccharide childMono = (Monosaccharide) child;
		int childPos = linkages.get(0).getChildLinkages().get(0);
		int anomericPos = childMono.getAnomericPosition();

		if (!childMono.getAnomer().equals(AnomericStateDescriptor.OPEN)) {
			if(childPos != -1) return true;
			//if (anomericPos != -1 && childPos == -1) return true;
		} else {
			if(anomericPos == childPos) return true;
			if (anomericPos == 0 && !linkages.get(0).getChildLinkages().contains(anomericPos)) return true;
		}

		return false;
	}
	
	public Edge copy() throws GlycanException {
		Edge ret = new Edge();
		for (Iterator<Linkage> iterEdges = this.linkages.iterator(); iterEdges.hasNext();) {
			ret.addGlycosidicLinkage(iterEdges.next().copy());
		}

		//ret.setChild(child);
		//ret.setParent(parent);

		/* copy of repeating unit */
		if(substituent != null)
			ret.setSubstituent(((Substituent) substituent).copy());
		
		return ret;
	}

	@Override
	public void accept(ContainerVisitor _visitor) throws VisitorException {
		_visitor.visit(this);
	}
}
