package org.glycoinfo.WURCSFramework.util.residuecontainer;

import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;
import org.glycoinfo.WURCSFramework.util.oldUtil.SubstituentTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class LinkageBlock {
	/**
	 * child: [c,g,k,n]
      ,cAcceptor: [4]
      ,cDonor: [1]
      ,parent: b (string)
      ,bridgeMod: will describe a MAP with string
	 */
	private LinkedList<String> a_aParents;
	private int a_iAcceptorID;
	private int a_iDonorID;
	private SubstituentTemplate a_sBridge;
	private LinkedList<String> a_aChildren;
	private LinkedList<String> a_aAntenna;
	private ArrayList<SubstituentTemplate> a_aAntennaSubs;
	private LinkedList<Integer> a_aDonors;
	private LinkedList<Integer> a_aAcceptors;
	private HashMap<String, RepeatingBlock> a_mRepeating;
	private boolean a_bIdReverse = false;
	private double a_dProbabilityLow = 1.0;
	private double a_dProbabilityHigh = 1.0;
	
	public LinkageBlock(){
		this.a_aChildren = new LinkedList<String>();
		this.a_aAcceptors = new LinkedList<Integer>();
		this.a_aDonors = new LinkedList<Integer>();
		this.a_aAntenna = new LinkedList<String>();
		this.a_aParents = new LinkedList<String>();
		this.a_aAntennaSubs = new ArrayList<SubstituentTemplate>();
		this.a_iAcceptorID = -1;
		this.a_iDonorID = -1;
		this.a_mRepeating = new HashMap<String, RepeatingBlock>();
	}

	public boolean isReverse() {
		return this.a_bIdReverse;
	}
	
	public int getAcceptorID() {
		return this.a_iAcceptorID;
	}
	
	public int getDonorID() {
		return this.a_iDonorID;
	}
	
	public LinkedList<String> getParent() {
		return this.a_aParents;
	}
	
	public LinkedList<String> getChild() {
		return this.a_aChildren;
	}
	
	public LinkedList<Integer> getDonors() {
		return this.a_aDonors;
	}
	
	public LinkedList<Integer> getAcceptors() {
		return this.a_aAcceptors;
	}
	
	public LinkedList<String> getAntenna() {
		return this.a_aAntenna;
	}
	
	public ArrayList<SubstituentTemplate> getAntennaSubs() {
		return this.a_aAntennaSubs;
	}
	
	public HashMap<String, RepeatingBlock> getRepeatingBlock() {
		return this.a_mRepeating;
	}
	
	public SubstituentTemplate getBridgeMAP() {
		return this.a_sBridge;
	}
	
	public void isReverse(boolean _is_Reverse) {
		this.a_bIdReverse = _is_Reverse;
	}
	
	public void setAcceptorID(int int_ID) {
		this.a_iAcceptorID = int_ID;
	}
	
	public void setDonorID(int int_ID) {
		this.a_iDonorID = int_ID;
	}
	
	public void setBridgeMAP(String a_sBridge) throws ConverterExchangeException {
		this.a_sBridge = SubstituentTemplate.forMAP(a_sBridge);
	}
	
	public void addParent(String str_parent) {
		this.a_aParents.add(str_parent);
	}
	
	public void setChild(LinkedList<String> lst_child) {
		this.a_aChildren = lst_child;
	}
	
	public void addAntennaRoot(String str_antenna) {
		this.a_aAntenna.add(str_antenna);
	}
	
	public void addChild(String str_child) {
		this.a_aChildren.add(str_child);
	}
	
	public void addChildDonor(LinkedList<Integer> lst_cDonor) {
		this.a_aDonors = lst_cDonor;
	}
	
	public void addChildAcceptor(LinkedList<Integer> lst_cAcceptor) {
		this.a_aAcceptors = lst_cAcceptor;
	}
	
	public void addRepeatingBlock(String str_repeatPos, RepeatingBlock obj_repBlock) {
		this.a_mRepeating.put(str_repeatPos, obj_repBlock);
	}
	
	public void addAntennaeMAP(String a_sMAP) throws ConverterExchangeException {
		this.a_aAntennaSubs.add(SubstituentTemplate.forMAP(a_sMAP));
	}
	
	public void setProbabilityHigh (double _a_dHigh) {
		this.a_dProbabilityHigh = _a_dHigh;
	}
	
	public void setProbabilityLow (double _a_dLow) {
		this.a_dProbabilityLow = _a_dLow;
	}
	
	public double getProbabilityHigh () {
		return this.a_dProbabilityHigh;
	}
	
	public double getProbabilityLow () {
		return this.a_dProbabilityLow;
	}
}
