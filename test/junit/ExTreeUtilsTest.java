package junit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pal.math.MersenneTwisterFast;
import pal.tree.Tree;
import pal.tree.TreeGenerator;
import palExtensions.ExTreeUtils;
import palExtensions.ExtRandom;
import palExtensions.YuleTree;

public class ExTreeUtilsTest {
	private static final int N_TREES = 4;
	private static final int N_TRIALS_PER_TREE = 10;
	
	@Test
	public void testToStringSimplified() {
		MersenneTwisterFast rng = new ExtRandom();
		String[] leafNames = new String[]{"A","B","C","D","E","F","G","H","I"};
		TreeGenerator generator = new YuleTree(leafNames,1.0,4); // 4 = RNG seed
		for (int i=0; i<N_TREES; i++) {
			Tree tree = generator.getNextTree(null);
			// Test: should get same simplified tree string, no matter how the tree gets shuffled
			String simplified = ExTreeUtils.toStringSimplified(tree);
			for (int j=0; j<N_TRIALS_PER_TREE; j++) {
				TestUtils.shuffleTree(tree,rng);
				assertTrue(simplified.equals(ExTreeUtils.toStringSimplified(tree)));
			}			
			// And test for non-binary trees also
			TestUtils.randomNodeMerge(tree,rng);
			TestUtils.randomNodeMerge(tree,rng);
			simplified = ExTreeUtils.toStringSimplified(tree);
			for (int j=0; j<N_TRIALS_PER_TREE; j++) {
				TestUtils.shuffleTree(tree,rng);
				assertTrue(simplified.equals(ExTreeUtils.toStringSimplified(tree)));
			}
		}
	}
}
