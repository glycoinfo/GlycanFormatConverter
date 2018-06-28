package org.glycoinfo.WURCSFramework.util.exchange.Carbbank;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSSequence2ToResidueContainer;
import org.glycoinfo.WURCSFramework.util.residuecontainer.LinkageBlock;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainer;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainerException;
import org.glycoinfo.WURCSFramework.util.residuecontainer.ResidueContainerUtility;
import org.glycoinfo.WURCSFramework.util.residuecontainer.RootStatusDescriptor;

public class WURCSToCarbBank {

	private StringBuilder str_CarbBank;
	private LinkedHashMap<String, String> map_node;
	
	private ResidueContainerUtility a_uRCU;
	
	public String getCarbBank() {
		return this.str_CarbBank.toString();
	}
	
	public void start(String str_WURCS) throws Exception {
		this.init();
		
		LinkedList<ResidueContainer> lst_RC = 
			new WURCSSequence2ToResidueContainer().start(str_WURCS);
		this.a_uRCU.setResidueList(lst_RC);
		
		this.extractMonosaccharideName();
		this.addNode(this.a_uRCU.getRoot());
		return;
	}
	
	private void extractMonosaccharideName() throws WURCSFormatException {
		for(ResidueContainer a_objRC : this.a_uRCU.getResidueContainers()) {
			StringBuilder str_tmp = new StringBuilder();
			LinkageBlock a_objLB = a_objRC.getLinkage();

			String str_IUPAC = a_objRC.getIUPACExtendedNotation();
			if(str_IUPAC.contains("beta-")) 
				str_IUPAC = str_IUPAC.replace("beta", "b");
			if(a_objRC.getIUPACExtendedNotation().contains("alpha-"))			
				str_IUPAC = str_IUPAC.replace("alpha", "a");

			str_tmp.append(str_IUPAC);
			
			/** append linkage position */
			if(a_objLB.getAcceptors().size() > 0) {
				str_tmp.append("-(");
				
				if(a_objRC.getAnomerPosition() != -1) str_tmp.append(a_objRC.getAnomerPosition());
				else str_tmp.append("?");

				str_tmp.append("-");
				
				if(a_objLB.getAcceptors().size() == 1) {
					if(a_objLB.getAcceptors().getLast() == -1) str_tmp.append("?");
					else str_tmp.append(a_objLB.getAcceptors().getLast());
				}
				if(a_objLB.getAcceptors().size() > 1) {
					for(Iterator<Integer> i = a_objLB.getAcceptors().iterator(); i.hasNext(); ) {
						str_tmp.append(i.next());
						if(i.hasNext()) str_tmp.append("/");
					}
				}	
				str_tmp.append(")");
			}			
			this.map_node.put(a_objRC.getNodeIndex(), str_tmp.toString());
		}
	}

	private void addNode(ResidueContainer a_objRC) throws ResidueContainerException {
		if(a_objRC == null) return;

		String cID = a_objRC.getNodeIndex();
		LinkageBlock a_objLB = a_objRC.getLinkage();

		if(!this.map_node.containsKey(a_objRC.getNodeIndex())) return;

		if(this.str_CarbBank.indexOf(")") == 0 || cID.equals("a")) {
			this.str_CarbBank.insert(0, this.map_node.get(cID));
		}else this.str_CarbBank.insert(0, this.map_node.get(cID) + "-");
		
		this.map_node.remove(cID);		
				
		/** check number of child */
		if(a_objLB.getChild().size() == 0)
			if(isYoungestChild(a_objRC)) this.str_CarbBank.insert(0, "(");
		if(a_objLB.getChild().size() == 1)
			addNode(this.a_uRCU.getIndex(a_objLB.getChild().getLast()));
		if(a_objLB.getChild().size() > 1) {
			for(int i = a_objLB.getChild().size() - 1; i>=0; i--) { 
				String childID = a_objLB.getChild().get(i);
				if(i != 0) {
					this.str_CarbBank.insert(0, ")-");
				}
				addNode(this.a_uRCU.getIndex(childID));
			}
		}
		
		return;
	}
	
	private boolean isYoungestChild(ResidueContainer a_objRC) throws ResidueContainerException {
		if(!a_objRC.getRootStatusDescriptor().equals(RootStatusDescriptor.NON))
			return false;
		
		String str_parent = a_objRC.getLinkage().getParent().getLast();
		String str_child = a_objRC.getNodeIndex();
		ResidueContainer Node = this.a_uRCU.getIndex(str_parent);
		
		while(Node.getRootStatusDescriptor().equals(RootStatusDescriptor.NON) && 
				Node.getLinkage().getChild().size() == 1) {
			str_parent = Node.getLinkage().getParent().getLast();
			str_child = Node.getNodeIndex();
			Node = this.a_uRCU.getIndex(str_parent);
		}
		
		LinkedList<String> lst_child = Node.getLinkage().getChild();
		if(lst_child.size() <= 1) return false;
		if(lst_child.indexOf(str_child) != 0) return true;
		if(Node.getRootStatusDescriptor().equals(RootStatusDescriptor.NON) && 
				lst_child.indexOf(str_child) == 0 && 
				this.a_uRCU.getIndex(str_parent).getLinkage().getChild().size() > 1) {
			return isYoungestChild(Node);
		}
		
		return false;
	}
	
	private void init() {
		this.a_uRCU = new ResidueContainerUtility();
		this.str_CarbBank = new StringBuilder();
		this.map_node = new LinkedHashMap<String, String>();
	}	
}
