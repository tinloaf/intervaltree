package de.tinloaf.intervaltree.test;

import de.tinloaf.intervaltree.IInterval;

public class TestInterval implements IInterval<Integer> {

	private int start;
	private int end;
	
	public TestInterval(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	@Override
	public Integer getBegin() {
		return this.start;
	}

	@Override
	public Integer getEnd() {
		return this.end;
	}

}
