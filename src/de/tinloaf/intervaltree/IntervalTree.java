package de.tinloaf.intervaltree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An Interval Tree implementation based on a Red-Black-Tree. Supports insertion, deletion, lookups and multiple entries
 * per interval.
 * 
 * Please note that this data structure is not static and not concurrency-safe.
 * 
 * @author Lukas Barth
 *
 * @param <E> Data type of the interval borders. This can be anything that is comparable to itself.
 * @param <I> Entry data type. This must implement HasInterval<E>, and the methods implemented must return the interval of the entry
 */
public class IntervalTree <E extends Comparable<E>, I extends HasInterval<E>> {
	private static final boolean VERIFY = false;

	private static final float ALLOCATION_FACTOR = 0.5f;
	
	private TreeNode root;
	private int count;
	
	private enum Color {
		RED, BLACK
	}
		
	private class TreeNode {
		IInterval<E> interval;
		E max;
		
		HashSet<I> values;
		
		TreeNode left;
		TreeNode right;
		TreeNode parent;
		
		Color color;
				
		TreeNode(IInterval<E> interval, Color nodeColor, TreeNode left, TreeNode right) {
			this.interval = interval;
			this.color = nodeColor;
			
			this.left = left;
			this.right = right;
			
			this.updateMax();
			
			this.parent = null;
			
			this.values = new HashSet<I>();
		}
		
		void updateMax() {
			this.max = interval.getEnd();
			
			if (left != null) {
				left.parent = this;
				this.max = getMax(this.max, left.max);
			}
			
			if (right != null) {
				right.parent = this;
				this.max = getMax(this.max, right.max);
			}			
		}
		
		TreeNode grandparent() {
			assert this.parent != null;
			assert this.parent.parent != null;
			
			return this.parent.parent;
		}
		
		TreeNode sibling() {
			assert this.parent != null;
			
			if (this == this.parent.right) {
				return this.parent.left;
			} else {
				return this.parent.right;
			}
		}
		
		TreeNode uncle() {
			assert this.parent != null;
			assert this.parent.parent != null;
			
			return this.parent.sibling();
		}
	}

	/* ======================
	 * Little Helpers
	 * ======================
	 */
	private E getMax(E a, E b) {
		int compareResult = a.compareTo(b);
		if (compareResult < 0) {
			return b;
		} else {
			return a;
		}
	}
	
	private Color nodeColor(TreeNode n) {
		return (n == null) ? Color.BLACK : n.color; // leaves are black
	}
	
	/* ======================
	 * Sanity Checks
	 * ======================
	 */
	private void verifyRedOrBlack(TreeNode root) {
		assert nodeColor(root) == Color.RED || nodeColor(root) == Color.BLACK;
		
		if (root == null)
			return;
		
		verifyRedOrBlack(root.left);
		verifyRedOrBlack(root.right);
	}
	
	private void verifySearchTree(TreeNode root) {
		if (root == null)
			return;
		
		if (root.left != null) {
			assert (
					(root.left.interval.getBegin().compareTo(root.interval.getBegin()) < 0) ||
					((root.left.interval.getBegin().compareTo(root.interval.getBegin()) == 0) &&
							root.left.interval.getEnd().compareTo(root.interval.getEnd()) < 0)					
					);			
			
			verifySearchTree(root.right);
		}
		
		if (root.right != null) {
			assert (
					(root.right.interval.getBegin().compareTo(root.interval.getBegin()) > 0) ||
					((root.right.interval.getBegin().compareTo(root.interval.getBegin()) == 0) &&
							root.right.interval.getEnd().compareTo(root.interval.getEnd()) > 0)					
					);			
			
			verifySearchTree(root.right);
		}
	}
	
	private E verifyMaxValues(TreeNode root) {
		if (root == null)
			return null;
		
		E computedMax = root.interval.getEnd();
		
		if (root.right != null) {
			E rightMax = verifyMaxValues(root.right);
			if (rightMax.compareTo(computedMax) > 0) {
				computedMax = rightMax;
			}
		}
		
		if (root.left != null) {
			E leftMax = verifyMaxValues(root.left);
			if (leftMax.compareTo(computedMax) > 0) {
				computedMax = leftMax;
			}
		}
		
		assert(computedMax.equals(root.max));
		
		return root.max;
	}

	private void verifyNoConsecutiveRed(TreeNode root) {
		if (nodeColor(root) == Color.RED) {
			assert nodeColor(root.parent) == Color.BLACK;
			assert nodeColor(root.left) == Color.BLACK;
			assert nodeColor(root.right) == Color.BLACK;
		}
		
		if (root == null)
			return;
		
		verifyNoConsecutiveRed(root.right);
		verifyNoConsecutiveRed(root.left);
	}
	
	private int verifyBlackLength(TreeNode n, int blackCount, int pathBlackCount) {
	    if (nodeColor(n) == Color.BLACK) {
	        blackCount++;
	    }
	    if (n == null) {
	        if (pathBlackCount == -1) {
	            pathBlackCount = blackCount;
	        } else {
	            assert blackCount == pathBlackCount;
	        }
	        return pathBlackCount;
	    }
	    pathBlackCount = verifyBlackLength(n.left,  blackCount, pathBlackCount);
	    pathBlackCount = verifyBlackLength(n.right, blackCount, pathBlackCount);
	    return pathBlackCount;
	}
		
	private void verify() {
		if (VERIFY) {
			verifyRedOrBlack(root);
			assert (nodeColor(this.root) == Color.BLACK);
			verifyNoConsecutiveRed(root);
			verifyBlackLength(this.root, 0, -1);
			verifyMaxValues(this.root);
			verifySearchTree(this.root);
		}
	}

	/* ==================================
	 * Search Functions
	 * ==================================
	 */
	private TreeNode lookupNode(IInterval<E> key) {
		TreeNode node = this.root;
		while (node != null) {
			int compResult = key.getBegin().compareTo(node.interval.getBegin());
			if (compResult < 0) {
				node = node.left;
			} else if (compResult > 0) {
				node = node.right;
			} else {
				// See if the ends also match
				int endCompResult = key.getEnd().compareTo(node.interval.getEnd());
				
				if (endCompResult == 0) {
					return node;
				} else if (endCompResult > 0){
					node = node.right;
				} else {
					node = node.left;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Lookup all entries that have a given interval. This will only return exact matches for the given interval.
	 * 
	 * @param key The interval of the entry to look up
	 * @return A set of possibly multiple entries that have the interval given in key
	 */
	public Set<I> lookup(IInterval<E> key) {
		TreeNode node = lookupNode(key);
		if (node == null) 
			return new HashSet<I>();
		
		return node.values;
 	}
		
	private boolean areIntervalsOverlapping(IInterval<E> a, IInterval<E> b) {
		if (a.getBegin().compareTo(b.getEnd()) > 0) {
			return false;
		}
		
		if (a.getEnd().compareTo(b.getBegin()) < 0) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * This finds all entries whose intervals overlap with a given query interval.
	 * 
	 * @param interval The query interval
	 * @return All entries that overlap the given interval
	 */
	public List<I> findOverlapping(IInterval<E> interval) {
		ArrayList<I> collector = new ArrayList<I>(Math.round(this.count * IntervalTree.ALLOCATION_FACTOR));
		
		findOverlapping(interval, this.root, collector);
		
		return collector;
	}
	
	private void findOverlapping(IInterval<E> interval, TreeNode root, Collection<I> collector) {
		if (root == null)
			return;
		
		// check if we overlap ourselves
		if (areIntervalsOverlapping(interval, root.interval)) {
			collector.addAll(root.values);
		}
		
		// check if we must descent to the left
		if ((root.left != null) && (root.left.max.compareTo(interval.getBegin()) >= 0) ) { 
			findOverlapping(interval, root.left, collector);
		}
		
		// Check if we must descent to the right
		if ((root.right != null) && (root.interval.getBegin().compareTo(interval.getEnd()) <= 0)) { 
			findOverlapping(interval, root.right, collector);
		}
	}
	
	private void accumulateAll(TreeNode n, Set<I> accumulator) {
		accumulator.addAll(n.values);
		
		if (n.left != null) {
			accumulateAll(n.left, accumulator);
		}
		
		if (n.right != null) {
			accumulateAll(n.right, accumulator);
		}
	}
	
	/**
	 * Returns all entries in the interval tree
	 * 
	 * @return All entries in the interval tree
	 */
	public Set<I> getAll() {
		Set<I> result = new HashSet<I>();
		
		if (this.root != null)
			this.accumulateAll(this.root, result);
		
		return result;
	}

	
	/* ==================================
	 *        ROTATIONS
	 * ==================================
	 */
	private void replaceNode(TreeNode oldn, TreeNode newn) {
	    if (oldn.parent == null) {
	        root = newn;
	    } else {
	        if (oldn == oldn.parent.left)
	            oldn.parent.left = newn;
	        else
	            oldn.parent.right = newn;
	    }
	    if (newn != null) {
	        newn.parent = oldn.parent;
	    }
	}

	private void rotateLeft(TreeNode n) {
	    TreeNode r = n.right;
	    replaceNode(n, r);
	    n.right = r.left;
	    if (r.left != null) {
	        r.left.parent = n;
	    }
	    r.left = n;
	    n.parent = r;
	    
	    // Update max towards root
	    n.updateMax();
	    r.updateMax();
	    if (r.parent != null)
	    	r.parent.updateMax();
	}

	private void rotateRight(TreeNode n) {
	    TreeNode l = n.left;
	    replaceNode(n, l);
	    n.left = l.right;
	    if (l.right != null) {
	        l.right.parent = n;
	    }
	    l.right = n;
	    n.parent = l;
	    
	    // Update max towards root
	    n.updateMax();
	    l.updateMax();
	    if (l.parent != null)
	    	l.parent.updateMax();
	}
	
	/* =========================
	 *  Insertion
	 * =========================
	 */
	/**
	 * This inserts a new entry into the Interval Tree
	 * 
	 * @param entry The entry to be added
	 */
	public void insert(I entry) {
		this.insert(entry.getInterval(), entry);
		this.count++;
	}
	
	private void insert(IInterval<E> key, I value) {
		TreeNode insertedNode = new TreeNode(key, Color.RED, null, null);
		insertedNode.values.add(value);
		
	    if (root == null) {
	        root = insertedNode;
	    } else {
	        TreeNode n = root;
	        while (true) {
	            int compResult = key.getBegin().compareTo(n.interval.getBegin());
				n.max = getMax(n.max, key.getEnd());
	            
	            if (compResult == 0) {
	            	int endCompResult = key.getEnd().compareTo(n.interval.getEnd());
	            	// If intervals match perfectly - nice.
	            	if (endCompResult == 0) {
	            		n.values.add(value);
	            		return; // No tree fixup needed
	            	} else {
	            		if (endCompResult > 0) {
	            			if (n.right == null) {
	            				n.right = insertedNode;
	            				break;
	            			} else {
	            				n = n.right;
	            			}
	            		} else {
	            			if (n.left == null) {
	            				n.left = insertedNode;
	            				break;
	            			} else {
	            				n = n.left;
	            			}	            			
	            		}
	            	}	            	
	            } else if (compResult < 0) {
	                if (n.left == null) {
	                    n.left = insertedNode;
	                    break;
	                } else {
	                    n = n.left;
	                }
	            } else {
	                assert compResult > 0;
	                if (n.right == null) {
	                    n.right = insertedNode;
	                    break;
	                } else {
	                    n = n.right;
	                }
	            }
	        }
	        insertedNode.parent = n;
	    }
	    
	    fixTreeAt(insertedNode);

	    verify();
	}
	
	private void fixTreeAt(TreeNode n) {
		/*
		 * Case 1: We are the root. We are black, everything else is still OK
		 */
	    if (n.parent == null) {
	        n.color = Color.BLACK;
	        return;
	    }

	    /*
	     * Case 2: Our parent is black. Two consecutive blacks are allowed, all is well.
	     */
	    if (nodeColor(n.parent) == Color.BLACK)
	        return; // Tree is still valid
	    
	    /*
	     * Case 3: We violate red-red, but our uncle is red, too. We switch the colors of (uncle/parent) and grandparent
	     * and restart fixing at our grandparent.
	     */
	    if (nodeColor(n.uncle()) == Color.RED) {
	        n.parent.color = Color.BLACK;
	        n.uncle().color = Color.BLACK;
	        n.grandparent().color = Color.RED;
	        fixTreeAt(n.grandparent());
	        return;
	    }
	    
	    /*
	     * Case 4: We violate red-red, and our uncle is black. We need to rotate!
	     */
	    
	    /*
	     * Sub-Case 4.1: We are "inside" our grandparents subtree, i.e. we are the left child and our parent is a
	     * right child (or vice versa). We need to rotate ourselves to the outside (by rotating about our parent)
	     * to apply the fix for case 4.
	     */
	    if (n == n.parent.right && n.parent == n.grandparent().left) {
	        rotateLeft(n.parent);
	        n = n.left;
	    } else if (n == n.parent.left && n.parent == n.grandparent().right) {
	        rotateRight(n.parent);
	        n = n.right;
	    }
	    
	    /*
	     * Now, finally fix case 4 by rotating about our parent.
	     */
	    n.parent.color = Color.BLACK;
	    n.grandparent().color = Color.RED;
	    if (n == n.parent.left && n.parent == n.grandparent().left) {
	        rotateRight(n.grandparent());
	    } else {
	        assert n == n.parent.right && n.parent == n.grandparent().right;
	        rotateLeft(n.grandparent());
	    }
	}
	
	/* ================================
	 * Deletion
	 * ================================
	 */
	/**
	 * Deletes a given entry from the Interval Tree
	 * 
	 * @param entry The entry to be deleted
	 */
	public void delete(I entry) {
		this.delete(entry.getInterval(), entry);
		this.count--;
	}
	
	private void delete(IInterval<E> key, I value) {
		TreeNode n = lookupNode(key);
	    if (n == null)
	        return;  // Key not found, do nothing
	    
	    if (n.values.size() > 1) {
	    	// we retain the node since it contains multiple values
	    	n.values.remove(value);
	    	return;
	    } else {
	    	assert(n.values.contains(value));
	    }
	    
	    /*
	     * We actually have to delete the node.	    
	     */
	    
	    if (n.left != null && n.right != null) {
	        // Copy key/value from predecessor and then delete it instead
	        TreeNode pred = maximumNode(n.left);
	        n.interval = pred.interval;
	        n.values = pred.values;
	        
	        n = pred;
	    }

	    assert n.left == null || n.right == null;
	    TreeNode child = (n.right == null) ? n.left : n.right;
	    
	    boolean wasBlack = ((nodeColor(n) == Color.BLACK));
	    
	    if (wasBlack) {
	        n.color = nodeColor(child);
	        fixTreeForDeletion(n);
	    }
	    replaceNode(n, child);

	    if ((child != null) && (child.parent == null)) {
	    	child.color = Color.BLACK;
	    }
	    
        // Fix all the max values from what we just deleted up to the root
        TreeNode cur = n.parent;
        while (cur != null) {
        	cur.updateMax();
        	
        	cur = cur.parent;
        }
        
	    verify();
	}
	
	private void fixTreeForDeletion(TreeNode n) {
		/*
		 * Case 1: We deleted the root. All is well since we deleted one black node from
		 * every path.
		 */
		if (n.parent == null) {
			n.color = Color.BLACK;
			return;
		}
		
		/*
		 * Case 2: We have a red sibling. Rotate about the parent to prepare for remaining cases.
		 */
		if (nodeColor(n.sibling()) == Color.RED) {
	        n.parent.color = Color.RED;
	        n.sibling().color = Color.BLACK;
	        if (n == n.parent.left)
	            rotateLeft(n.parent);
	        else
	            rotateRight(n.parent);
	    }
		
		/*
		 * Case 3: We can recolor and recurse
		 */
		if (nodeColor(n.parent) == Color.BLACK &&
		        nodeColor(n.sibling()) == Color.BLACK &&
		        nodeColor(n.sibling().left) == Color.BLACK &&
		        nodeColor(n.sibling().right) == Color.BLACK)
		    {
		        n.sibling().color = Color.RED;
		        fixTreeForDeletion(n.parent);
		        return;
		    }

		/*
		 * Case 4
		 */
	    if (nodeColor(n.parent) == Color.RED &&
	            nodeColor(n.sibling()) == Color.BLACK &&
	            nodeColor(n.sibling().left) == Color.BLACK &&
	            nodeColor(n.sibling().right) == Color.BLACK)
	        {
	            n.sibling().color = Color.RED;
	            n.parent.color = Color.BLACK;
	            return;
	        }
	    
	    /*
	     * Case 5: Prepare for case 6
	     */
	    if (n == n.parent.left &&
	            nodeColor(n.sibling()) == Color.BLACK &&
	            nodeColor(n.sibling().left) == Color.RED &&
	            nodeColor(n.sibling().right) == Color.BLACK)
	        {
	            n.sibling().color = Color.RED;
	            n.sibling().left.color = Color.BLACK;
	            rotateRight(n.sibling());
	        }
	        else if (n == n.parent.right &&
	                 nodeColor(n.sibling()) == Color.BLACK &&
	                 nodeColor(n.sibling().right) == Color.RED &&
	                 nodeColor(n.sibling().left) == Color.BLACK)
	        {
	            n.sibling().color = Color.RED;
	            n.sibling().right.color = Color.BLACK;
	            rotateLeft(n.sibling());
	        }
	    
	    /*
	     * Case 6
	     */
	    n.sibling().color = nodeColor(n.parent);
	    n.parent.color = Color.BLACK;
	    if (n == n.parent.left) {
	        assert nodeColor(n.sibling().right) == Color.RED;
	        n.sibling().right.color = Color.BLACK;
	        rotateLeft(n.parent);
	    }
	    else
	    {
	        assert nodeColor(n.sibling().left) == Color.RED;
	        n.sibling().left.color = Color.BLACK;
	        rotateRight(n.parent);
	    }
	}
	
	private TreeNode maximumNode(TreeNode n) {
	    assert n != null;
	    while (n.right != null) {
	        n = n.right;
	    }
	    return n;
	}
	
	/* =================
	 *  Misc
	 * =================
	 */
	
	/**
	 * Creates a new, empty Interval Tree
	 */
	public IntervalTree() {
		this.root = null;
		this.count = 0;
		verify();
	}
	
	/**
	 * Returns the maximum interval end in the tree
	 * 
	 * @return The maximum interval end in the tree
	 */
	public E getMaximum() {
		if (this.root == null)
			return null;
		
		return this.root.max;
	}
	
	private void printHelper(TreeNode n, int indent) {
	    if (n == null) {
	        System.out.print("<empty tree>");
	        return;
	    }
	    if (n.right != null) {
	        printHelper(n.right, indent + 2);
	    }
	    for (int i = 0; i < indent; i++)
	        System.out.print(" ");
	    
    	String payloads = "";
    	for (I payload : n.values) {
    		payloads += payload.toString() + ",";
    	}
    	
	    if (n.color == Color.BLACK) {
	        System.out.println(n.interval.getBegin() + " / " + n.interval.getEnd() + " / " + n.max + ": " + payloads);
	    }
	    else
	        System.out.println("<" + n.interval.getBegin() + " / " + n.interval.getEnd() + " / " + n.max + ">" + ": " + payloads);
	    if (n.left != null) {
	        printHelper(n.left, indent + 2);
	    }
	}
	
	/**
	 * Debug method that will print the whole tree to System.out
	 */
	public void printTree() {
		printHelper(this.root, 0);		
	}
}
 