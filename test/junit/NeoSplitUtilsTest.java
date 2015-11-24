package junit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pal.math.MersenneTwisterFast;
import pal.tree.Tree;
import pal.tree.TreeGenerator;
import pal.tree.TreeUtils;
import palExtensions.ExTreeUtils;
import palExtensions.ExtRandom;
import palExtensions.NeoSplitSystem;
import palExtensions.NeoSplitUtils;
import palExtensions.YuleTree;

public class NeoSplitUtilsTest {
	/*
	 * Create random binary trees. Possibly compress them to non-binary trees.
	 * Decompose to splits, then recreate tree from splits. Ensure tree
	 * topology was not changed by this process. 
	 */
	@Test
	public void testTreeToFromSplits() {
		final int N_TREES = 10;
		MersenneTwisterFast rng = new ExtRandom();
		String[] leafNames = new String[]{"A","B","C","D","E","F","G","H","I"};
		TreeGenerator generator = new YuleTree(leafNames,1.0,4); // 4 = RNG seed
		for (int i=0; i<N_TREES; i++) {
			Tree tree = generator.getNextTree(null);
			// Progressively remove internal nodes, testing each time.
			boolean firstTime = true;
			do {
				if (firstTime) {
					firstTime = false;
				} else {
					TestUtils.randomNodeMerge(tree,rng);
				}
				// To ensure the trees are rooted the same, always reroot to the parent node of leaf "A"
				ExTreeUtils.reroot(tree, TreeUtils.getNodeByName(tree, "A").getParent());
				String startTopology = ExTreeUtils.toTopologyString(tree);
				NeoSplitSystem splits = NeoSplitUtils.getSplits(tree);
				Tree newTree = NeoSplitUtils.treeFromSplits(splits);
				ExTreeUtils.reroot(newTree, TreeUtils.getNodeByName(newTree, "A").getParent());
				String finalTopology = ExTreeUtils.toTopologyString(tree);
				assertTrue(startTopology.equals(finalTopology));
			} while (tree.getInternalNodeCount()>1);
		}
	}
}
