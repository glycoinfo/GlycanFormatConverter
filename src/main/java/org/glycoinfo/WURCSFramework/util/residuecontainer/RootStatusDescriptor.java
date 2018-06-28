package org.glycoinfo.WURCSFramework.util.residuecontainer;

public enum RootStatusDescriptor {

	REDEND("redEnd"),
	OTYPE("O-type"),
	NTYPE("N-type"),
	KETOTYPE("=O-type"),
	COMPOSITION("cmpRoot"),
	FRAGMENT("frgRoot"),
	CYCLICSTART("cyclic_start"),
	NON("");
	
	private String a_sRootType;
	
	RootStatusDescriptor (String _a_sRootType) {
		this.a_sRootType = _a_sRootType;
	}
	
	public static RootStatusDescriptor forRootStatus(String _a_sRootType) {
		RootStatusDescriptor[] enumArray = RootStatusDescriptor.values();
		
		for(RootStatusDescriptor enumStr : enumArray) {
			if(_a_sRootType.equals(enumStr.a_sRootType.toString())) return enumStr;
		}
		
		return null;
	}
	
	public String toString() {
		return this.a_sRootType;
	}
}
