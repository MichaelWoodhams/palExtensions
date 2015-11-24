package junit;

import pal.math.MersenneTwisterFast;
import pal.tree.Node;
import pal.tree.Tree;

/**
 * Methods shared by several tests
 * @author woodhams
 *
 */
public class TestUtils {
	/*
	 * Randomly reorder the children in a tree
	 */
	public static void shuffleTree(Tree tree, MersenneTwisterFast rng) {
		for (int i=0; i<tree.getInternalNodeCount(); i++) {
			Node node = tree.getExternalNode(i);
			int nChild = node.getChildCount();
			Node[] children = new Node[nChild];
			for (int j=0; j<nChild; j++) children[j]=node.getChild(j);
			int[] shuffle = rng.shuffled(nChild);
			for (int j=0; j<nChild; j++) node.setChild(j, children[shuffle[j]]);
		}
	}
	
	/*
	 * Randomly merge a node with its parent, thus converting a binary tree into
	 * a non-binary tree
	 */
	public static void randomNodeMerge(Tree tree, MersenneTwisterFast rng) {
		if (tree.getInternalNodeCount()==1) throw new RuntimeException ("No nodes to merge");
		Node randNode = null;
		do {
			randNode = tree.getInternalNode(rng.nextInt(tree.getInternalNodeCount()));
		} while (randNode == tree.getRoot());
		Node parent = randNode.getParent();
		int i = 0;
		for (i=0; i<parent.getChildCount() && parent.getChild(i)!=randNode; i++) { }
		if (i==parent.getChildCount()) throw new RuntimeException("randNode was not child of its parent!");
		parent.removeChild(i);
		for (i=0; i<randNode.getChildCount(); i++) {
			parent.addChild(randNode.getChild(i));
		}
	}
}
