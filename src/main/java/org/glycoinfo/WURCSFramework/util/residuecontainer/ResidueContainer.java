package org.glycoinfo.WURCSFramework.util.residuecontainer;

public class ResidueContainer extends ResidueData{
	/** WURCSJson element is decribes as below
	 *  {
        SugarName: GlcNAc (string)
        ,anomerPosition: 1 (number)
        ,Index: a (string)
        ,root: redEnd (string)
        ,ID: 1 (number)
        ,Linkage: {
        	this block handle in WURCSJsonLinkageBlock object
        }
        ,anomerSymbol: b (string)
        ,Basetype: u2122h_2*NCC/3=O (string)
        ,MS: 12122h-1b_1-5_2*NCC/3=O (string)
    }
	 */
	private String str_sugarName;
	private String str_index;
	private RootStatusDescriptor a_enumRootStatus;
	private int int_ID;
	private int int_frgNum;
	private String str_name;
	private String str_MS;
	private int int_BackBoneSize;
	private LinkageBlock a_objLB;
	private String a_sCondensedNotation = "";
	
	public ResidueContainer(String _strDLconfiguration, char _char_ringSize, String _str_coreName){
		super(_strDLconfiguration, _char_ringSize, _str_coreName);
		
		this.str_sugarName = "";
		this.int_ID = 0;
		this.int_frgNum = 0;
		this.str_name = "";
		this.str_MS = "";
		this.a_objLB = new LinkageBlock();
		this.int_BackBoneSize = 0;
	}
	
	public ResidueContainer(){
		super();
		
		this.str_sugarName = "";
		this.int_ID = 0;
		this.int_frgNum = 0;
		this.str_name = "";
		this.str_MS = "";
		this.a_objLB = new LinkageBlock();
		this.int_BackBoneSize = 0;
	}
	
	public void setSugarName(String str_sugarName) {
		this.str_sugarName = str_sugarName;
	}
	
	public void setNodeIndex(String str_index) {
		this.str_index = str_index;
	}
	
	public void setNodeID(int int_nodeID) {
		this.int_ID = int_nodeID;
	}

	public void setRootStatus(RootStatusDescriptor a_enumRootStatus) {
		this.a_enumRootStatus = a_enumRootStatus;
	}
	
	public void setIUPACExtednedNotation(String a_sIUPACExt) {
		this.str_name = a_sIUPACExt;
	}
	
	public void setBackBoneSize(int _int_BackBone) {
		this.int_BackBoneSize = _int_BackBone;
	}
	
	public void setMS(String str_MS) {
		this.str_MS = str_MS;
	}
	
	public String getNodeIndex() {
		return this.str_index;
	}
	
	public String getMS() {
		return this.str_MS;
	}
	
	public String getIUPACExtendedNotation() {
		return this.str_name;
	}
	
	public RootStatusDescriptor getRootStatusDescriptor() {
		return this.a_enumRootStatus;
	}

	public int getFrgNum() {
		return this.int_frgNum;
	}
	
	public int getNodeID() {
		return this.int_ID;
	}
	
	public LinkageBlock getLinkage() {
		return this.a_objLB;
	}
	
	public int getBackBoneSize() {
		return this.int_BackBoneSize;
	}
	
	public void addLinkage(LinkageBlock obj_linkage) {
		this.a_objLB = obj_linkage;
	}
	
	public String getSugarName() {
		return this.str_sugarName;
	}
	
	public void addFrgNum(int _int_frgNum) {
		this.int_frgNum = this.int_frgNum + 1;
	}
	
	public void setIUPACCondensedNotation (String _a_sCondensedNotation) {
		this.a_sCondensedNotation = _a_sCondensedNotation;
	}
	
	public String getIUPACCondensedNotaiton () {
		return this.a_sCondensedNotation;
	}
}
