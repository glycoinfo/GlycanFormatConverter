package org.glycoinfo.GlycanFormatconverter.Glycan;

import java.util.ArrayList;

public class GlycanAlternative extends Substituent{

	public GlycanAlternative(SubstituentInterface _sub) {
		super(_sub);
	}

	private ArrayList<Edge> inEdges = new ArrayList<Edge>();
	private ArrayList<Edge> outEdges = new ArrayList<Edge>();
	
	public void addInEdge(Edge _inEdge) {
		this.inEdges.add(_inEdge);
	}
	
	public ArrayList<Edge> getInEdges() {
		return this.inEdges;
	}
	
	public void addOutEdge(Edge _outEdge) {
		this.outEdges.add(_outEdge);
	}
	
	public ArrayList<Edge> getOutEdges() {
		return this.outEdges;
	}
	
	public GlycanAlternative copy() {
		return new GlycanAlternative(this.getSubstituent());
	}
}
