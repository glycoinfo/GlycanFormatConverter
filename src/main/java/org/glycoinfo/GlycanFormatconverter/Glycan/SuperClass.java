package org.glycoinfo.GlycanFormatconverter.Glycan;

public enum SuperClass {

	SUG(0, "Sug"),
	TRI(3, "Tri"),
	TET(4, "Tet"),
	PEN(5, "Pen"),
	HEX(6, "Hex"),
	HEP(7, "Hep"),
	OCT(8, "Oct"),
	NON(9, "Non"),
	DEC(10, "Dec");
	
	int size;
	String superclass;
	
	private SuperClass(int _size, String _superclass) {
		size = _size;
		superclass = _superclass;
	}
	
	public int getSize() {
		return size;
	}
	
	public String getSuperClass() {
		return superclass;
	}
	
	public static SuperClass forSize(int _int) {
		for(SuperClass enumInt : SuperClass.values()) {
			if(enumInt.size == _int) return enumInt;
		}
		
		return null;
	}
	
	public static SuperClass forSuperClass(String _superClass) {
		for(SuperClass enumInt : SuperClass.values()) {
			if(enumInt.superclass.equals(_superClass)) return enumInt;
		}
		
		return null;
	}

	public static SuperClass forSuperClassWithIgnore (String _superClass) {
		for (SuperClass ind : SuperClass.values()) {
			if (ind.superclass.equalsIgnoreCase(_superClass)) return ind;
		}

		return null;
	}
}
