package palExtensions;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import pal.distance.DistanceMatrix;
import pal.misc.IdGroup;
import pal.tree.AttributeNode;
import pal.tree.Node;
import pal.tree.ReadTree;
import pal.tree.Tree;
import pal.tree.TreeParseException;
import pal.tree.TreeUtils;

public class ExTreeUtils {
	/**
	 * Reorders nodes in the tree in a standard way, such that
	 * topologically equivalent trees will match each other.
	 * @param tree
	 */
	public static void reorderCanonically(Tree tree) {
		ExNodeUtils.reorderCanonically((AttributeNode)tree.getRoot());
	}
	
	/**
	 * Sets all branch lengths to one and reorders nodes canonically.
	 * Applying this process to two topologically equivalent rooted trees
	 * will result in two identical trees.
	 * @param tree
	 */
	public static void topologyOnly(Tree tree) {
		reorderCanonically(tree);
		int n=tree.getExternalNodeCount();
		for (int i=0; i<n; i++) tree.getExternalNode(i).setBranchLength(1);
		n = tree.getInternalNodeCount();
		for (int i=0; i<n; i++) tree.getInternalNode(i).setBranchLength(1);
	}
	
	/**
	 * Returns tree as string without branch lengths (i.e. topology only.)
	 * @param tree
	 * @return
	 */
	public static String toStringSimplified(Tree tree) {
		return ExNodeUtils.toStringSimplified(tree.getRoot());
	}
	
	/**
	 * NOTE: This routine may reorder children in nodes in this tree.
	 * Returns a string which uniquely depends on the topology of the tree.
	 * (I.e. two trees have same toTopologyString iff they have the same topology.)
	 * @param tree
	 * @return
	 */
	public static String toTopologyString(Tree tree) {
		return ExNodeUtils.toTopologyString((AttributeNode)tree.getRoot());
	}

	/**
	 * If root of tree is a binary node, reroot to one of the current root's
	 * children, which will now have 3 children.
	 * (If root has >3 children, do nothing.)
	 * @param tree
	 */
	public static void forceThreeChildRoot(Tree tree) {
		Node root = tree.getRoot();
		if (root.getChildCount()==2) {
			Node newRoot, moved;
			if (root.getChild(0).isLeaf()) {
				newRoot = root.getChild(1);
				moved = root.getChild(0);
				if (newRoot.isLeaf()) throw new IllegalArgumentException("Can't reroot a two taxon tree");
			} else {
				newRoot = root.getChild(0);
				moved = root.getChild(1);
			}
			moved.setBranchLength(moved.getBranchLength()+newRoot.getBranchLength());
			newRoot.addChild(moved);
			newRoot.setParent(null);
			tree.setRoot(newRoot);
		}
	}
	/**
	 * This removes an annoying limitation of PAL's TreeUtils.reroot method, that it
	 * won't work unless the current root has 3+ children.
	 */
	public static void reroot(Tree tree, Node node) {
		forceThreeChildRoot(tree);
		TreeUtils.reroot(tree, node);
	}
	
	/**
	 * Given a tree in New Hampshire format, return the resulting tree.
	 * Returns null if the string is not a legal New Hampshire format tree.
	 * @param inputString
	 * @return
	 */
	public static Tree stringToTree(String inputString) {
		Tree tree = null;
		try {
			tree = robustStringToTree(inputString);
		} catch (TreeParseException e) {
		} catch (IOException e) {
		}
		return tree;
	}

	/**
	 * Given a tree in New Hampshire format, return the resulting tree.
	 * Throws exceptions if the string is not a legal New Hampshire format tree.
	 * @param inputString
	 * @return
	 * @throws TreeParseException
	 * @throws IOException
	 */
	public static Tree robustStringToTree(String inputString) throws TreeParseException, IOException {
		StringReader sr = new StringReader(inputString);
		PushbackReader pbr = new PushbackReader(sr);
		Tree tree = new ReadTree(pbr);
		pbr.close();
		sr.close();
		return tree;
	}
	
	/*
	 * This is a quickly hacked replacement for pal.tree.TreeDistanceMatrix which is buggy,
	 * and I'm trying to avoid introducting any new code (i.e. bugfixing) PAL.
	 * (If tree and supplied IdGroup don't have same order, pal.tree.TreeDistanceMatrix does
	 * not correctly permute the distance matrix to account for it.) 
	 */
	public static DistanceMatrix treeToDistanceMatrix(Tree tree, IdGroup order, boolean countEdges, double epsilon) {
		IdGroup unpermutedIds = TreeUtils.getLeafIdGroup(tree);
		DistanceMatrix unpermutedDist = new DistanceMatrix(computeDistances(tree, countEdges, epsilon), unpermutedIds);
		// but DistanceMatrix's permuting does work
		return new DistanceMatrix(unpermutedDist, order);
	}
	
	/*
	 * Largely copied from pal.tree.TreeDistanceMatrix,
	 * except don't try to permute the indices (PAL got this wrong anyhow.)
	 */
	public static final double[][] computeDistances(Tree tree,  boolean countEdges, double epsilon)
	{
		int numSeqs = tree.getExternalNodeCount();
		double[][] distance = new double[numSeqs][numSeqs];

		double[] dist = new double[tree.getExternalNodeCount()];
		double[] idist = new double[tree.getInternalNodeCount()];

		// fast O(n^2) computation of induced distance matrix
		for (int i = 0; i < tree.getExternalNodeCount(); i++)
		{
			TreeUtils.computeAllDistances(tree, i, dist, idist, countEdges, epsilon);

			for (int j = 0; j < tree.getExternalNodeCount(); j++)
			{
				distance[i][j] = dist[j];
			}
		}
		return distance;
	}
}
