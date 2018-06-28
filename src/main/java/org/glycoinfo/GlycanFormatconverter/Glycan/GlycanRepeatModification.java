package org.glycoinfo.GlycanFormatconverter.Glycan;

public class GlycanRepeatModification extends Substituent implements RepeatInterface{

	private GlycanRepeat repeat;
	
	public GlycanRepeatModification(SubstituentInterface _subface) throws GlycanException { 
		super(_subface);
		this.repeat = new GlycanRepeat();
	}

	public int getMinRepeatCount() {
		return this.repeat.getMinRepeatCount();
	}
	
	public int getMaxRepeatCount() {
		return this.repeat.getMaxRepeatCount();
	}
	
	public void setMinRepeatCount(int _count) {
		this.repeat.setMinRepeatCount(_count);;
	}
	
	public void setMaxRepeatCount(int _count) {
		this.repeat.setMaxRepeatCount(_count);
	}

	public GlycanRepeatModification copy() {
		CrossLinkedTemplate subT = (CrossLinkedTemplate) this.getSubstituent();
		GlycanRepeatModification copy;
		try {
			copy = new GlycanRepeatModification(subT);
			copy.repeat = this.repeat;
			return copy;
		} catch (GlycanException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}

