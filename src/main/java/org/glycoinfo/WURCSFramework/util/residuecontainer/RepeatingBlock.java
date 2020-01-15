package org.glycoinfo.WURCSFramework.util.residuecontainer;

import org.glycoinfo.WURCSFramework.util.oldUtil.ConverterExchangeException;
import org.glycoinfo.WURCSFramework.util.oldUtil.SubstituentTemplate;

import java.util.LinkedList;

public class RepeatingBlock {
	/**
	 * repeating block is describe as below
	 * this block is contained Linkage
	 * 
	 * repeat: {
       	start: {
          cAcceptor: [-1]
         ,cDonor: [1]
         ,bridge:  (string)
         ,end: x1122h-1x_1-5 (string) -> will change the Index
        }
        ,end: {
          min: -1 (number)
         ,max: -1 (number)
         ,start: x1122h-1x_1-5 (string) -> will change the Index
         ,pDonor: [1]
         ,bridge:  (string)
         ,pAcceptor: [-1]
        }
       }
	 */
	private int int_max;
	private int int_min;
	private String str_start_end;
	private SubstituentTemplate a_enumTemplate;
	private LinkedList<Integer> lst_cAcceptor;
	private LinkedList<Integer> lst_cDonor;
	private LinkedList<Integer> lst_pAcceptor;
	private LinkedList<Integer> lst_pDonor;
	
	private boolean isCyclic;
	private boolean isNonRedEnd;
	
	public RepeatingBlock() {
		this.int_max = 0;
		this.int_min = 0;
		this.lst_cAcceptor = new LinkedList<Integer>();
		this.lst_cDonor = new LinkedList<Integer>();;
		this.lst_pAcceptor = new LinkedList<Integer>();
		this.lst_pDonor = new LinkedList<Integer>();
		this.str_start_end = "";
		this.isCyclic = false;
		this.isNonRedEnd = true;
	}

	public void setMax(int int_max) {
		this.int_max = int_max;
	}
	
	public void setMin(int int_min) {
		this.int_min = int_min;
	}
	
	public void setBridge(String str_bridge) throws ConverterExchangeException {
		this.a_enumTemplate = SubstituentTemplate.forMAP(str_bridge);
	}
	
	public void setChildAcceptor(LinkedList<Integer> lst_cAcceptor) {
		this.lst_cAcceptor = lst_cAcceptor;
	}
	
	public void addParentAcceptor(LinkedList<Integer> lst_pAcceptor) {
		this.lst_pAcceptor = lst_pAcceptor;
	}
	
	public void setChildDonor(LinkedList<Integer> lst_cDonor) {
		this.lst_cDonor = lst_cDonor;
	}
	
	public void addParentDonor(LinkedList<Integer> lst_pDonor) {
		this.lst_pDonor = lst_pDonor;
	}
	
	public void setOppositdeNode(String str_Index) {
		this.str_start_end = str_Index;
	}
	
	public void isCyclic(boolean _isCyclic) {
		this.isCyclic = _isCyclic;
	}

	public void isNonRedEnd(boolean _isNonRedEnd) {
		this.isNonRedEnd = _isNonRedEnd;
	}
	
	public int getMax() {
		return this.int_max;
	}
	
	public int getMin() {
		return this.int_min;
	}
	
	public SubstituentTemplate getBridge() {
		return this.a_enumTemplate;
	}
	
	public String getOppositeNode() {
		return this.str_start_end;
	}
	
	public LinkedList<Integer> getParentAcceptor() {
		return this.lst_pAcceptor;
	}
	
	public LinkedList<Integer> getParentDonor() {
		return this.lst_pDonor;
	}
		
	public LinkedList<Integer> getChildAcceptor() {
		return this.lst_cAcceptor;
	}

	public LinkedList<Integer> getChildDonor() {
		return this.lst_cDonor;
	}
	
	public boolean isCyclic() {
		return this.isCyclic;
	}
	
	public boolean isNonRedEnd() {
		return this.isNonRedEnd;
	}
}
