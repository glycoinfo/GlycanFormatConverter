package org.glycoinfo.GlycanFormatconverter.util.TrivialName;

import java.util.ArrayList;

public enum BaseStereoIndex {

	GRO("gro",3),
	ERY("ery",4),
	RIB("rib",5),
	ARA("ara",5),
	ALL("all",6),
	ALT("alt",6),
	GLC("glc",6),
	MAN("man",6),
	THR("thr",4),
	XYL("xyl",5),
	LYX("lyx",5),
	GUL("gul",6),
	IDO("ido",6),
	GAL("gal",6),
	TAL("tal",6);
	
	private String code;
	private int size;
	
	BaseStereoIndex (String _code, int _size) {
		this.code = _code;
		this.size = _size;
	}

	public String getNotation () { return this.code; }

	public int getSize () {
		return this.size;
	}
	
	public static BaseStereoIndex forCode (String _code) {
		for(BaseStereoIndex bsi : BaseStereoIndex.values()) {
			if(bsi.code.equalsIgnoreCase(_code)) return bsi;
		}
		
		return null;
	}

	public static ArrayList<BaseStereoIndex> forSize (int _size) {
		ArrayList<BaseStereoIndex> ret = new ArrayList<>();
		for (BaseStereoIndex bsi : BaseStereoIndex.values()) {
			if (bsi.size == _size) {
				ret.add(bsi);
			}
		}

		return null;
	}
}
