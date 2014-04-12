package de.tinloaf.intervaltree.test;

import de.tinloaf.intervaltree.HasInterval;
import de.tinloaf.intervaltree.IInterval;

public class TestData implements HasInterval<Integer> {
	
	int start;
	int end;
	int id;

	public TestData(int start, int end, int id) {
		this.start = start;
		this.end = end;
		this.id = id;
	}
	
	@Override
	public IInterval<Integer> getInterval() {
		return new TestInterval(this.start, this.end);
	}
	
	@Override
	public String toString() {
		return Integer.toString(this.id);
	}
	
	public int getId() {
		return this.id;
	}
}
