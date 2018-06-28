package org.glycoinfo.WURCSFramework.util.residuecontainer;

public enum SuperClass {
	TRI("xh", "Tri", "tri", 3),
	TET("xxh", "Tet", "tet", 4),
	PEN("xxxh", "Pen", "pen", 5),
	HEX("xxxxh", "Hex", "hex", 6),
	HEP("xxxxxh", "Hep", "hep", 7),
	OCT("xxxxxxh", "Oct", "oct", 8),
	NON("xxxxxxxh", "Non", "non", 9),
	DEC("xxxxxxxxh", "Dec", "dec", 10);
	
	String str_base;
	String str_superClass;
	String str_name;
	int int_size;
	
	private SuperClass(String _base, String _str_SuperClass, String _name, int _size) {
		this.str_base = _base;
		this.str_superClass = _str_SuperClass;
		this.str_name = _name;
		this.int_size = _size;
	}
	
	public String getName() {
		return this.str_name;
	}
	
	public String getBasetype() {
		return this.str_base;
	}
	
	public String getSuperClass() {
		return this.str_superClass;
	}
	
	public int getSize() {
		return this.int_size;
	}

	public static SuperClass getBaseType(String _base) {
		SuperClass[] enumArray = SuperClass.values();

		for(SuperClass enumStr : enumArray)
			if (_base.equals(enumStr.str_base.toString())) return enumStr;
		return null;
	}
	
	public static SuperClass getSuperClass(String _shape) {
		SuperClass[] enumArray = SuperClass.values();

		for(SuperClass enumStr : enumArray)
			if (_shape.equals(enumStr.str_superClass.toString())) return enumStr;
		return null;
	}
	
	public static SuperClass getName(String _name) {
		SuperClass[] enumArray = SuperClass.values();

		for(SuperClass enumStr : enumArray)
			if (_name.equals(enumStr.str_name.toString())) return enumStr;
		return null;
	}
	
	public static SuperClass getSize(int _size) {
		SuperClass[] enumArray = SuperClass.values();

		for(SuperClass enumStr : enumArray)
			if (_size == enumStr.int_size) return enumStr;
		return null;
	}
	
}
