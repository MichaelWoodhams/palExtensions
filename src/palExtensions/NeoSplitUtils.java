package palExtensions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import pal.misc.IdGroup;
import pal.misc.Identifier;
import pal.tree.Node;
import pal.tree.SimpleNode;
import pal.tree.SimpleTree;
import pal.tree.Tree;
import pal.tree.TreeUtils;
/*
 * Similar to PAL's SplitUtils, but using my Split and NeoSplitSystem data types
 */

public class NeoSplitUtils {
	
	/*
	 * Heavily based on PAL SplitUtils.getSplits
	 */
	public static NeoSplitSystem getSplits(IdGroup idGroup, Tree tree)
	{	
		int numInternalNodes = tree.getInternalNodeCount()-1;
		int numLeaves = idGroup.getIdCount();
		boolean[][] rawSplits = new boolean[numInternalNodes][];

		int numGoodSplits = 0;
		for (int i = 0; i < numInternalNodes; i++)
		{
			boolean[] split = new boolean[numLeaves];
			getSplit(idGroup, tree.getInternalNode(i), split);
			
			// Add split to list if:
			// * it isn't a leaf
			// * it isn't already there
			int count = 0;
			for (int j=0; j<split.length; j++) {
				count += (split[j] ? 1 : 0);
			}
			if (count == 1 || count == numLeaves-1) {
				break;
			}
			boolean foundMatch = false; // found a matching split?
			for (int j=0; j<numGoodSplits && !foundMatch; j++) {
				boolean foundMismatch = false; // found a point of disagreement within this particular split?
				for (int k=0; k<numLeaves && !foundMismatch; k++) {
					foundMismatch = (rawSplits[j][k] != split[k]);
				}
				foundMatch = !foundMismatch;
			}
			if (!foundMatch) {
				rawSplits[numGoodSplits++] = split;
			}
		}
		
		NeoSplitSystem splitSystem = new NeoSplitSystem(idGroup,numGoodSplits);
		for (int i = 0; i < numGoodSplits; i++)
		{
			splitSystem.add(Split.toSplit(idGroup, rawSplits[i]));
		}
		return splitSystem;
	}


	public static Tree treeFromSplits(NeoSplitSystem splits) {
		if (!splits.isCompatible()) throw new IllegalArgumentException("Tried to build a tree from incompatible splits");
		/*
		 *  We start off with each identifier belonging to a leaf node.
		 *  We will progressively amalgamate nodes under new parents
		 *  by processing the splits, starting with the 'smallest' splits
		 *  (i.e. those which have fewest members in their smaller half.) 
		 */
		int nTaxa = splits.getIdCount();
		HashMap<Identifier,Node> ancestorNode = new HashMap<Identifier,Node>(nTaxa);
		int n= splits.getIdCount();
		for (int i=0; i<n; i++) {
			Identifier id = splits.getIdentifier(i);
			Node node = new SimpleNode();
			node.setIdentifier(id);
			ancestorNode.put(id, node);
		}
		
		// Cache the split sizes:
		int nSplits = splits.getSplitCount();
		final HashMap<Split,Integer> splitSize = new HashMap<Split,Integer>(nSplits);
		for (Split split : splits) splitSize.put(split, split.sizeOfSmaller());
		
		// Make an array of the splits, then sort it by split size
		Split[] splitArray = new Split[nSplits];
		for (int i=0; i<nSplits; i++) splitArray[i] = splits.get(i);
		Comparator<Split> sizeComp = new Comparator<Split>() {
			public int compare(Split a, Split b) {
				/* Conceptually this is
				 * return a.sizeOfSmaller()-b.sizeOfSmaller();
				 * but that would inefficiently recalculate size many times.
				 */
				return splitSize.get(a)-splitSize.get(b);
			}
		};
		Arrays.sort(splitArray, sizeComp);
		
		// For each split, make a new parent node holding the smaller side of the split
		for (Split split : splitArray) {
			Set<Identifier> idSet = split.smallerSubset();
			Set<Node> children = new HashSet<Node>(idSet.size());
			for (Identifier id : idSet) children.add(ancestorNode.get(id));
			Node parent = new SimpleNode();
			for (Node child : children) parent.addChild(child);
			for (Identifier id : idSet) ancestorNode.put(id, parent);
		}
		
		// Make all remaining ancestor nodes into children of the root
		Node root = new SimpleNode();
		Set<Node> children = new HashSet<Node>(ancestorNode.values());
		for (Node child : children) root.addChild(child);
		return new SimpleTree(root);
	}
	
	public static Tree treeFromSplits(Collection<Split> splits) {
		return treeFromSplits(new NeoSplitSystem(splits));
	}


	/**
	 * creates a split system from a tree
	 * (using tree-induced order of sequences)
	 *
	 * @param tree
	 */
	public static NeoSplitSystem getSplits(Tree tree)
	{
		IdGroup idGroup = TreeUtils.getLeafIdGroup(tree);

		return getSplits(idGroup, tree);
	}

	/*
	 * Gets the split corresponding to the parent edge of 'internalNode'.
	 * Stores the split in boolean array.
	 * Copied directly from PAL SplitUtils. Made 'private' until I think of a reason for it not to be.
	 */
	private static void getSplit(IdGroup idGroup, Node internalNode, boolean[] split) {
		if (internalNode.isLeaf() || internalNode.isRoot())
		{
			throw new IllegalArgumentException("Only internal nodes (and no root) nodes allowed");
		}

		// make sure split is reset
		for (int i = 0; i < split.length; i++)
		{
			split[i] = false;
		}

		// mark all leafs downstream of the node

		for (int i = 0; i < internalNode.getChildCount(); i++)
		{
			markNode(idGroup, internalNode, split);
		}

		// standardize split (i.e. first index is alway true)
		if (split[0] == false)
		{
			for (int i = 0; i < split.length; i++)
			{
				if (split[i] == false)
					split[i] = true;
				else
					split[i] = false;
			}
		}
	}
	
	/*
	 * Direct copy from PAL SplitUtils. Made 'private' until I think of a reason for it not to be.
	 * Marks (in 'split' array) all leaf nodes equal or under 'node'.
	 */
	private static void markNode(IdGroup idGroup, Node node, boolean[] split) {
		if (node.isLeaf())
		{
			String name = node.getIdentifier().getName();
			int index = idGroup.whichIdNumber(name);

			if (index < 0)
			{
				throw new IllegalArgumentException("INCOMPATIBLE IDENTIFIER (" + name + ")");
			}

			split[index] = true;
		}
		else
		{
			for (int i = 0; i < node.getChildCount(); i++)
			{
				markNode(idGroup, node.getChild(i), split);
			}
		}
	}

}
