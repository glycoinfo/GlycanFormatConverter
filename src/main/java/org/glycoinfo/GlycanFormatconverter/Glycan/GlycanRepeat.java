package org.glycoinfo.GlycanFormatconverter.Glycan;

public class GlycanRepeat implements RepeatInterface{
	public static final int UNKNOWN = -1;
	
	private int minCount = GlycanRepeat.UNKNOWN;
	private int maxCount = GlycanRepeat.UNKNOWN;
	
	public int getMinRepeatCount() {
		return this.minCount;
	}
	
	public int getMaxRepeatCount() {
		return this.maxCount;
	}
	
	public void setMinRepeatCount(int _count) {
		this.minCount = _count;
	}
	
	public void setMaxRepeatCount(int _count) {
		this.maxCount = _count;
	}
}
