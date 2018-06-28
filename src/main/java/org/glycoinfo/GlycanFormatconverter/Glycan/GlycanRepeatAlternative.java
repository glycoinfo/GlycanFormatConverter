package org.glycoinfo.GlycanFormatconverter.Glycan;

public class GlycanRepeatAlternative extends GlycanAlternative implements RepeatInterface{

	private GlycanRepeat repeat;	
	public GlycanRepeatAlternative(SubstituentInterface _sub) {
		super(_sub);
		repeat = new GlycanRepeat();
	}

	@Override
	public void setMinRepeatCount(int _min) {
		repeat.setMinRepeatCount(_min);
	}

	@Override
	public void setMaxRepeatCount(int _max) {
		repeat.setMaxRepeatCount(_max);
	}

	@Override
	public int getMinRepeatCount() {
		return repeat.getMinRepeatCount();
	}

	@Override
	public int getMaxRepeatCount() {
		return repeat.getMaxRepeatCount();
	}
	
	public GlycanRepeatAlternative copy() {
		GlycanRepeatAlternative ret = new GlycanRepeatAlternative(getSubstituent());
		ret.repeat = repeat;
		return ret;
	}
}
