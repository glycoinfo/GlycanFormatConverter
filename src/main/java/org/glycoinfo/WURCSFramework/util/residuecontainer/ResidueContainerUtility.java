package org.glycoinfo.WURCSFramework.util.residuecontainer;

import org.glycoinfo.WURCSFramework.util.WURCSDataConverter;
import org.glycoinfo.WURCSFramework.util.oldUtil.IUPAC.ConverterIUPACException;
import org.glycoinfo.WURCSFramework.util.oldUtil.SubstituentTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class ResidueContainerUtility {

	private LinkedList<ResidueContainer> a_aResidueContainers;
	private ArrayList<ResidueContainer> a_aRootOfFragments;
	private ArrayList<SubstituentTemplate> a_aSubstituentOfFragments;
	
	ResidueContainer a_oRoot;
	
	public ResidueContainerUtility () {
		this.a_aResidueContainers = new LinkedList<ResidueContainer>();
		this.a_aRootOfFragments = new ArrayList<ResidueContainer>();
		this.a_aSubstituentOfFragments = new ArrayList<SubstituentTemplate>();
	}
	
	public ArrayList<ResidueContainer> getRootOfFragments() {
		return this.a_aRootOfFragments;
	}
	
	public LinkedList<ResidueContainer> getResidueContainers() {
		return this.a_aResidueContainers;
	}
	
	public ArrayList<SubstituentTemplate> getSubtituentOfFragments() {
		return this.a_aSubstituentOfFragments;
	}
	
	public void addRootOfFragments(ResidueContainer a_oRC) {
		this.a_aRootOfFragments.add(a_oRC);
	}
		
	public ResidueContainer getRoot() throws ConverterIUPACException {
		for(ResidueContainer a_objRC : a_aResidueContainers) {
			if(a_objRC.getRootStatusDescriptor().equals(RootStatusDescriptor.REDEND)) 
				this.a_oRoot = a_objRC;
			if(a_objRC.getRootStatusDescriptor().equals(RootStatusDescriptor.CYCLICSTART))
				this.a_oRoot = a_objRC;
			if(a_objRC.getRootStatusDescriptor().equals(RootStatusDescriptor.KETOTYPE))
				this.a_oRoot = a_objRC;
			if(a_objRC.getRootStatusDescriptor().equals(RootStatusDescriptor.NTYPE))
				this.a_oRoot = a_objRC;
			if(a_objRC.getRootStatusDescriptor().equals(RootStatusDescriptor.OTYPE))
				this.a_oRoot = a_objRC;
			if(a_objRC.getRootStatusDescriptor().equals(RootStatusDescriptor.COMPOSITION)) {
				if(this.a_aResidueContainers.size() > 1) 
					throw new ConverterIUPACException("Composition could not handled");
				else 
					this.a_oRoot = a_objRC;
			}
			if(a_objRC.getRootStatusDescriptor().equals(RootStatusDescriptor.FRAGMENT))
				this.a_aRootOfFragments.add(a_objRC);
		}
		
		return this.a_oRoot;
	}
	
	public ResidueContainer getRoot(boolean a_bIsShowRedEnd) {
		for(ResidueContainer a_oRC : this.a_aResidueContainers) {
			if(!a_oRC.getRootStatusDescriptor().equals(RootStatusDescriptor.NON) &&
				!a_oRC.getRootStatusDescriptor().equals(RootStatusDescriptor.FRAGMENT))
				return a_oRC;
		}
		return null;
	}

	public void extractSubstituent(ResidueContainer a_objRC) {
		if(a_objRC.getNativeSubstituent().size() == 0) return;
		
		for(String s : a_objRC.getNativeSubstituent()) {
			a_objRC.addSubstituent(s);
		}

		/**sort low to high position*/
		Collections.sort(a_objRC.getSubstituent());
		
		return;
	}
	
	public ResidueContainer getIndex(String a_sIndex) throws ResidueContainerException {
		int int_ind = WURCSDataConverter.convertRESIndexToID(a_sIndex) - 1;
		ResidueContainer a_oIndex = null;
		
		if(int_ind > a_aResidueContainers.size())
			throw new ResidueContainerException("This id is exceeded for size of glycan");
		
		for(ResidueContainer a_objRC : this.a_aResidueContainers) {
			if(a_objRC.getNodeIndex().equals(a_sIndex)) {
				a_oIndex = a_objRC;
				break;
			}
		}
		
		if(!a_oIndex.getNodeIndex().equals(a_sIndex))
			throw new ResidueContainerException("This node is not correctly reference for input index : " + a_sIndex + " " + a_oIndex.getNodeIndex());
		
		return a_oIndex;
	}
	
	public ArrayList<ResidueContainer> getParent(ResidueContainer a_objRC) throws ResidueContainerException {
		ArrayList<ResidueContainer> ret = new ArrayList<ResidueContainer>();
		for(String a_sIndex : a_objRC.getLinkage().getParent()) {
			if(!ret.isEmpty()) break;
		
			if(!ret.contains(getIndex(a_sIndex))) 
				ret.add(getIndex(a_sIndex));
		}

		return ret;
	}
	
	public LinkedList<ResidueContainer> getChild(ResidueContainer a_objRC) throws ResidueContainerException {
		LinkedList<ResidueContainer> ret = new LinkedList<ResidueContainer>();
		
		for(String a_sIndex : a_objRC.getLinkage().getChild()) {
			ret.addLast(getIndex(a_sIndex));
		}
		
		return ret;
	}
	
	public int checkNumberOfChildren (ResidueContainer a_objRC) throws ResidueContainerException {
		LinkageBlock a_objLB = a_objRC.getLinkage();
		int ret = a_objLB.getChild().size();
		
		for(String a_sIndex : a_objLB.getChild()) {
			if(a_sIndex.contains("cyclic")) ret--;
			//if(getIndex(a_sIndex).getLinkage().getRepeatingBlock().containsKey("cyclic_start")) ret--;
		}
		
		return ret;
	}
	
	public boolean checkIntoRepeating (ResidueContainer a_objRC) throws ResidueContainerException {
		ResidueContainer a_EndRC = getEndRep(a_objRC);		
		boolean isInRep = false;
		
		if(!a_EndRC.getLinkage().getRepeatingBlock().containsKey("end") &&
				!a_EndRC.getLinkage().getRepeatingBlock().containsKey("start")) return false;
		
		int a_iEndA = a_EndRC.getLinkage().getRepeatingBlock().get("end").getParentAcceptor().getFirst();
		
		for(String a_sIndex : a_EndRC.getLinkage().getChild()) {
			if(a_sIndex.contains("end")) continue;
			ResidueContainer a_oChild = getIndex(a_sIndex);
			
			if(a_oChild.getLinkage().getAcceptors().getFirst() == a_iEndA)
				isInRep = false;
		}
		
		return isInRep;
	}
	
	public boolean isinCyclic () {
		return this.a_aResidueContainers.getFirst().getLinkage().getRepeatingBlock().containsKey("cyclic_start");
	}
	
	public ResidueContainer getEndRep (ResidueContainer a_objRC) throws ResidueContainerException {
		while(!a_objRC.getLinkage().getRepeatingBlock().containsKey("end")) {
			a_objRC = getIndex(a_objRC.getLinkage().getParent().getFirst());
			if(a_objRC.getLinkage().getRepeatingBlock().containsKey("start")) break;
			if(a_objRC.getLinkage().getParent().isEmpty()) break;
		}
		
		if(a_objRC.getLinkage().getRepeatingBlock().containsKey("start") && 
				!a_objRC.getLinkage().getRepeatingBlock().containsKey("end")) {
			a_objRC = getIndex(a_objRC.getLinkage().getRepeatingBlock().get("start").getOppositeNode());
		}
		
		return a_objRC;
	}
	
	public ResidueContainer getCyclicEnd () {
		for(ResidueContainer a_objRC : this.a_aResidueContainers) {
			if(a_objRC.getLinkage().getRepeatingBlock().containsKey("cyclic_end")) return a_objRC;
		}
		
		return null;
	}
	
	public void extractSubstituentOfAntennae() {
		for(ResidueContainer a_oRC : this.a_aResidueContainers) {
			if(a_oRC.getLinkage().getAntennaSubs().isEmpty()) continue;
			if(this.a_aSubstituentOfFragments.containsAll(a_oRC.getLinkage().getAntennaSubs())) continue;
			this.a_aSubstituentOfFragments.addAll(a_oRC.getLinkage().getAntennaSubs());
		}
		
		return;
	}
	
	public void sortChildByPosition(ResidueContainer a_oRC) throws ResidueContainerException {
		if(a_oRC.getLinkage().getChild().size() < 2) return;

		LinkedList<Integer> a_aIndexs = new LinkedList<Integer>();
		int a_iMax = -100000;
		
		for(String a_sIndex : a_oRC.getLinkage().getChild()) {
			int a_iAcceptorPos = this.getIndex(a_sIndex).getLinkage().getAcceptors().getFirst();
			if(a_iAcceptorPos > a_iMax) {
				a_iMax = a_iAcceptorPos;
				a_aIndexs.addLast(a_oRC.getLinkage().getChild().indexOf(a_sIndex));
			}else a_aIndexs.addFirst(a_oRC.getLinkage().getChild().indexOf(a_sIndex));
		}
		
		LinkedList<String> child = new LinkedList<String>();
		
		for(Integer i : a_aIndexs) {
			child.addLast(a_oRC.getLinkage().getChild().get(i));
		}
		
		a_oRC.getLinkage().setChild(child);
		
		return;
	}
	
	private void copy(LinkedList<ResidueContainer> a_tmp) {
		for(ResidueContainer a : a_tmp) {
			this.a_aResidueContainers.addLast(a);
		}
		
		return;
	}

	public void setResidueList(LinkedList<ResidueContainer> _lst_RC) {
		this.copy(_lst_RC);
		
		return;
	}
}
