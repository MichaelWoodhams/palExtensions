package junit;

import static org.junit.Assert.*;

import org.junit.Test;

import pal.misc.IdGroup;
import pal.misc.SimpleIdGroup;
import palExtensions.IdGroupUtils;

public class IdGroupUtilsTest {
	private static IdGroup groupABC = new SimpleIdGroup(new String[]{"a","b","c"});
	private static IdGroup groupCAB = new SimpleIdGroup(new String[]{"c","a","b"});
	private static IdGroup groupBAD = new SimpleIdGroup(new String[]{"b","a","d"});

	@Test
	public void testPermuteToOrder() {

		int[] perm = IdGroupUtils.permuteToOrder(groupABC, groupCAB);
		for (int i=0; i<perm.length; i++) {
			assertEquals(groupABC.getIdentifier(i).toString(), groupCAB.getIdentifier(perm[i]).toString());
		}
		try {
			IdGroupUtils.permuteToOrder(groupBAD, groupCAB);
			fail();
		} catch (IllegalArgumentException e) {}	catch (Exception e) { fail(); }
	}
	
	@Test
	public void testIsOrdered() {
		assertTrue(IdGroupUtils.isOrdered(groupABC));
		assertFalse(IdGroupUtils.isOrdered(groupCAB));
	}
	
	@Test
	public void testCopyEquals() {
		assertTrue(IdGroupUtils.equals(groupABC, IdGroupUtils.copyOrdered(groupCAB)));
		assertFalse(IdGroupUtils.equals(groupABC, groupCAB));
		assertFalse(IdGroupUtils.equals(groupABC, groupBAD));
	}
	
	@Test
	public void testSameLabels() {
		assertTrue(IdGroupUtils.sameLabels(groupABC, IdGroupUtils.copyOrdered(groupCAB)));
		assertTrue(IdGroupUtils.sameLabels(groupABC, groupCAB));
		assertFalse(IdGroupUtils.sameLabels(groupABC, groupBAD));
	}

}
