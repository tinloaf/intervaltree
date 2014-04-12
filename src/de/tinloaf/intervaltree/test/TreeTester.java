package de.tinloaf.intervaltree.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.tinloaf.intervaltree.IntervalTree;

public class TreeTester {

	private final static int TEST_RANGE_HIGH = Integer.MAX_VALUE;

	private final static int COUNT_RANDOM = 5000;
	private final static int COUNT_EQUAL = 100;
	private final static int COUNT_EQUALSTART = 100;
	
	private int id;
	
	private Random r;
	
	private ArrayList<TestData> randomData(IntervalTree<Integer, TestData> testTree) {	
		ArrayList<TestData> dataList = new ArrayList<TestData>(10000);
		
		for (int i = 0; i < COUNT_RANDOM; i++) {
			int a = Math.round(r.nextInt(TEST_RANGE_HIGH));
			int b = Math.round(r.nextInt(TEST_RANGE_HIGH));
			
			int low = Math.min(a,b);
			int high = Math.max(a, b);
			
			TestData data = new TestData(low, high, id++);
			testTree.insert(data);
			dataList.add(data);
		}
		
		for (TestData data : dataList) {
			assert(testTree.findOverlapping(data.getInterval()).contains(data));
		}		
		
		return dataList;
	}
	
	private ArrayList<TestData> equalIntervals(IntervalTree<Integer, TestData> testTree) {
		ArrayList<TestData> dataList = new ArrayList<TestData>(10000);
		
		for (int i = 0; i < COUNT_EQUAL; i++) {
			int a = Math.round(r.nextInt(TEST_RANGE_HIGH));
			int b = Math.round(r.nextInt(TEST_RANGE_HIGH));
			
			int low = Math.min(a,b);
			int high = Math.max(a, b);
			
			for (long j = 0; j < (Math.round(r.nextInt(COUNT_EQUAL)) + 2); j++) {
				TestData data = new TestData(low, high, id++);
				testTree.insert(data);
				dataList.add(data);
			}
			testTree.delete(dataList.get(dataList.size() - 1));
			dataList.remove(dataList.get(dataList.size() - 1));
		}
		
		for (TestData data : dataList) {
			assert(testTree.findOverlapping(data.getInterval()).contains(data));
		}
		
		return dataList;
	}
	
	private ArrayList<TestData> equalStarts(IntervalTree<Integer, TestData> testTree) {
		ArrayList<TestData> dataList = new ArrayList<TestData>(10000);
		
		for (int i = 0; i < COUNT_EQUALSTART; i++) {
			int low = (int)Math.round(r.nextInt(TEST_RANGE_HIGH) * 0.75);
			
			for (long j = 0; j < (Math.round(r.nextInt(50))); j++) {
				int high = low + (Math.round(r.nextInt(TEST_RANGE_HIGH - low)));
				
				TestData data = new TestData(low, high, id++);
				testTree.insert(data);
				dataList.add(data);
			}			
		}
		
		for (TestData data : dataList) {
			assert(testTree.findOverlapping(data.getInterval()).contains(data));
		}		
		
		return dataList;		
	}
	
	private void testOverlapping(IntervalTree<Integer, TestData> testTree, ArrayList<TestData> testData) {
		for (int i = 0; i < 1000; i++) {
			ArrayList<TestData> selectedData = new ArrayList<TestData>(20);
			
			int low = Integer.MAX_VALUE;
			int high = Integer.MIN_VALUE;
			
			//testTree.printTree();
			//System.out.println("=========");
			
			for (int j = 0; j < 20; j++) {
				TestData selected = testData.get(r.nextInt(testData.size()));
				selectedData.add(selected);
				
				low = Math.min(low, selected.start);
				high = Math.max(high, selected.end);
			}
			
			TestInterval joinedInterval = new TestInterval(low, high);
			
			List<TestData> overlapping = testTree.findOverlapping(joinedInterval);
			
			for (TestData data : overlapping) {
				assert(	(! (data.end < low))
						&& (! (data.start > high)));
			}
			
			for (TestData data : selectedData) {
				//System.out.println("Looking for: " + data.getId());
				assert(overlapping.contains(data));
			}
		}
	}
	
	private ArrayList<TestData> deleteSome(IntervalTree<Integer, TestData> testTree, ArrayList<TestData> testData) {
		ArrayList<TestData> selectedData = new ArrayList<TestData>(20);
	
		int count = r.nextInt(testData.size());
		
		for (int j = 0; j < count; j++) {
			TestData selected = testData.get(r.nextInt(testData.size()));
			if (!selectedData.contains(selected))
				selectedData.add(selected);
		}
		
		for (TestData data : selectedData) {
			//System.out.println("DELETING: " + data.id);
			testTree.delete(data);
		}
		
		return selectedData;
	}
	
	private void deleteAll(IntervalTree<Integer, TestData> testTree, ArrayList<TestData> testData) {
		for (TestData data : testData) {
			assert (testTree.findOverlapping(data.getInterval()).contains(data));
			
			testTree.delete(data);

			//testTree.printTree();
			//System.out.println("======================");
			
			assert (! (testTree.findOverlapping(data.getInterval()).contains(data)));
		}
	}
	
	public void run() {
		IntervalTree<Integer, TestData> testTree = new IntervalTree<Integer, TestData>();

		ArrayList<TestData> dataInTree = new ArrayList<TestData>();

		int seed = new Random().nextInt(Integer.MAX_VALUE);
		System.out.println("Random Seed: " + seed);
		this.r = new Random(seed);
		
		for (int i = 0 ; i < 5; i++) {
		
			System.out.println("Inserting random data...");
			dataInTree.addAll(this.randomData(testTree));
			System.out.println("Inserting intervals with equal starts...");
			dataInTree.addAll(this.equalStarts(testTree));
			System.out.println("Inserting equal intervals...");
			dataInTree.addAll(this.equalIntervals(testTree));
	
			System.out.println("Testing overlap-search...");
			testOverlapping(testTree, dataInTree);
			
			System.out.println("Testing Deletion...");
			dataInTree.removeAll(this.deleteSome(testTree, dataInTree));		
		}
		
		System.out.println("Deleting all...");
		deleteAll(testTree, dataInTree);
		System.out.println("Finished!");
	}
	
	TreeTester() {
		this.id = 0;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TreeTester tester = new TreeTester();
		tester.run();
	}

}
