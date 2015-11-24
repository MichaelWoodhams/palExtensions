package junit;

/**
 * Tests Split and SmallSplit classes
 */

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import pal.misc.IdGroup;
import pal.misc.SimpleIdGroup;
import palExtensions.SmallPartialSplit;
import palExtensions.SmallSplit;
import palExtensions.Split;

public class SplitTest {
	private static IdGroup STANDARD  = new SimpleIdGroup(new String[]{"bat","cat","fat","hat","mat"});
	private static IdGroup REORDERED = new SimpleIdGroup(new String[]{"mat","bat","cat","fat","hat"});
	private static IdGroup DIFFERENT = new SimpleIdGroup(new String[]{"rat","cat","fat","hat","mat"});
	private static IdGroup DIFF_SIZE = new SimpleIdGroup(new String[]{"bat","cat","fat","hat","mat","rat"});
	private static IdGroup UNIVERSE_1= new SimpleIdGroup(new String[]{"at","bat","cat","fat","hat","mat","rat","sat"});
	
	private static boolean[] B11000 = new boolean[]{ true, true,false,false,false};
	private static boolean[] B11100 = new boolean[]{ true, true, true,false,false};
	private static boolean[] B01100 = new boolean[]{false, true, true,false,false};
	private static boolean[] B00011 = new boolean[]{false,false,false, true, true};
	private static boolean[] B10001 = new boolean[]{ true,false,false,false, true};
	private static boolean[] B01110 = new boolean[]{false, true, true, true,false};
	private static boolean[] B00110 = new boolean[]{false,false, true, true,false};
	private static boolean[] B110000= new boolean[]{ true, true,false,false,false,false};
	private static SmallSplit SP_STD_BC  = new SmallSplit(STANDARD, B11000); // bat cat
	private static SmallSplit SP_STD_BCF = new SmallSplit(STANDARD, B11100); // bat cat fat
	private static SmallSplit SP_STD_HM  = new SmallSplit(STANDARD, B00011); // hat mat
	private static SmallSplit SP_STD_CF  = new SmallSplit(STANDARD, B01100); // cat fat
	private static SmallSplit SP_ROD_BC  = new SmallSplit(REORDERED,B01100); // bat cat
	private static SmallSplit SP_ROD_BCF = new SmallSplit(REORDERED,B01110); // bat cat fat
	private static SmallSplit SP_ROD_HM  = new SmallSplit(REORDERED,B10001); // hat mat
	private static SmallSplit SP_ROD_CF  = new SmallSplit(REORDERED,B00110); // cat fat
	private static SmallSplit SP_DIF_CF  = new SmallSplit(DIFFERENT,B01100); 
	private static SmallSplit SP_SIZ_CF  = new SmallSplit(DIFF_SIZE,B110000); 
	
	private static SmallPartialSplit P1_STD_BC  = new SmallPartialSplit(STANDARD, B11000, UNIVERSE_1); // bat cat
	private static SmallPartialSplit P1_STD_BCF = new SmallPartialSplit(STANDARD, B11100, UNIVERSE_1); // bat cat fat
	private static SmallPartialSplit P1_STD_HM  = new SmallPartialSplit(STANDARD, B00011, UNIVERSE_1); // hat mat
	private static SmallPartialSplit P1_STD_CF  = new SmallPartialSplit(STANDARD, B01100, UNIVERSE_1); // cat fat
	private static SmallPartialSplit P1_ROD_BC  = new SmallPartialSplit(REORDERED,B01100, UNIVERSE_1); // bat cat
	private static SmallPartialSplit P1_ROD_BCF = new SmallPartialSplit(REORDERED,B01110, UNIVERSE_1); // bat cat fat
	private static SmallPartialSplit P1_ROD_HM  = new SmallPartialSplit(REORDERED,B10001, UNIVERSE_1); // hat mat
	private static SmallPartialSplit P1_ROD_CF  = new SmallPartialSplit(REORDERED,B00110, UNIVERSE_1); // cat fat
	private static SmallPartialSplit P1_DIF_CF  = new SmallPartialSplit(DIFFERENT,B01100, UNIVERSE_1); 
	private static SmallPartialSplit P1_SIZ_CF  = new SmallPartialSplit(DIFF_SIZE,B110000,UNIVERSE_1); 
	
	
	@Test
	public void testCompatibleSmallSplit() {
		// Tests where splits have the same ID group
		assertTrue(SP_STD_BC.compatible(SP_STD_BC));  // bat cat VS self
		assertTrue(SP_STD_BC.compatible(SP_STD_BCF)); // bat cat VS bat cat fat
		assertTrue(SP_STD_BCF.compatible(SP_STD_BC)); // bat cat fat VS bat cat
		assertTrue(SP_STD_BC.compatible(SP_STD_HM));  // bat cat VS hat mat
		assertFalse(SP_STD_BC.compatible(SP_STD_CF)); // bat cat VS cat fat		
		// Tests where ID groups have same members, but in different order
		assertTrue(SP_STD_BCF.compatible(SP_ROD_BCF)); // bat cat fat VS bat cat fat
		assertTrue(SP_STD_BCF.compatible(SP_ROD_HM));  // bat cat fat VS hat mat (same split differently expressed)
		assertTrue(SP_STD_BC.compatible(SP_ROD_BCF));  // bat cat VS bat cat fat
		assertTrue(SP_STD_BCF.compatible(SP_ROD_BC));  // bat cat fat VS bat cat
		assertTrue(SP_STD_BC.compatible(SP_ROD_HM));   // bat cat VS hat mat
		assertFalse(SP_STD_BC.compatible(SP_ROD_CF));  // bat cat VS cat fat
		// Tests with the generalized compatibility comparator
		assertTrue(Split.compatible(SP_STD_BC, SP_STD_BC));
		assertTrue(Split.compatible(SP_STD_BC,SP_ROD_BCF));
		assertTrue(Split.compatible(SP_STD_BC,SP_ROD_HM));
		assertFalse(Split.compatible(SP_STD_BC,SP_ROD_CF));
		// Tests of incompatible ID groups
		try {
			SP_STD_BC.compatible(SP_DIF_CF);
			fail();
		} catch (IllegalArgumentException e) {}	catch (Exception e) { fail(); }
		try {
			SP_STD_BC.compatible(SP_SIZ_CF);
			fail();
		} catch (IllegalArgumentException e) {} catch (Exception e) { fail(); }
		try {
			Split.compatible(SP_STD_BC, SP_DIF_CF);
			fail();
		} catch (IllegalArgumentException e) {} catch (Exception e) { fail(); }
		try {
			Split.compatible(SP_STD_BC, SP_SIZ_CF);
			fail();
		} catch (IllegalArgumentException e) {} catch (Exception e) { fail(); }
	}
	
	@Test
	public void testCompatiblePartialSplit() {
		// Tests where splits have the same ID group
		assertTrue(P1_STD_BC.compatible(P1_STD_BC));  // bat cat VS self
		assertTrue(P1_STD_BC.compatible(P1_STD_BCF)); // bat cat VS bat cat fat
		assertTrue(P1_STD_BCF.compatible(P1_STD_BC)); // bat cat fat VS bat cat
		assertTrue(P1_STD_BC.compatible(P1_STD_HM));  // bat cat VS hat mat
		assertFalse(P1_STD_BC.compatible(P1_STD_CF)); // bat cat VS cat fat		
		// Tests where ID groups have same members, but in different order
		assertTrue(P1_STD_BCF.compatible(P1_ROD_BCF)); // bat cat fat VS bat cat fat
		assertTrue(P1_STD_BCF.compatible(P1_ROD_HM));  // bat cat fat VS hat mat (same split differently expressed)
		assertTrue(P1_STD_BC.compatible(P1_ROD_BCF));  // bat cat VS bat cat fat
		assertTrue(P1_STD_BCF.compatible(P1_ROD_BC));  // bat cat fat VS bat cat
		assertTrue(P1_STD_BC.compatible(P1_ROD_HM));   // bat cat VS hat mat
		assertFalse(P1_STD_BC.compatible(P1_ROD_CF));  // bat cat VS cat fat
		// Tests with the generalized compatibility comparator
		assertTrue(Split.compatible(P1_STD_BC, P1_STD_BC));
		assertTrue(Split.compatible(P1_STD_BC,P1_ROD_BCF));
		assertTrue(Split.compatible(P1_STD_BC,P1_ROD_HM));
		assertFalse(Split.compatible(P1_STD_BC,P1_ROD_CF));
		// Tests of incompatible ID groups
		try {
			P1_STD_BC.compatible(P1_DIF_CF);
			fail();
		} catch (IllegalArgumentException e) {}	catch (Exception e) { fail(); }
		try {
			P1_STD_BC.compatible(P1_SIZ_CF);
			fail();
		} catch (IllegalArgumentException e) {} catch (Exception e) { fail(); }
		try {
			Split.compatible(P1_STD_BC, P1_DIF_CF);
			fail();
		} catch (IllegalArgumentException e) {} catch (Exception e) { fail(); }
		try {
			Split.compatible(P1_STD_BC, P1_SIZ_CF);
			fail();
		} catch (IllegalArgumentException e) {} catch (Exception e) { fail(); }
	}

	
	@Test
	public void testEquals() {
		assertTrue(SP_STD_BCF.equals(SP_STD_HM));     // Same split differently expressed
		assertTrue(SP_STD_BCF.equals(SP_ROD_BCF));
		assertTrue(SP_STD_BCF.equals(SP_ROD_HM));
		assertFalse(SP_STD_BCF.equals(SP_ROD_BC));
		assertTrue(Split.equals(SP_STD_BC, SP_STD_BC));
		assertTrue(Split.equals(SP_STD_BCF, SP_ROD_HM));
		assertFalse(Split.equals(SP_ROD_BCF, SP_STD_CF));
		try {
			Split.equals(SP_STD_BC, SP_DIF_CF);
			fail();
		} catch (IllegalArgumentException e) {} catch (Exception e) { fail(); }
		try {
			Split.equals(SP_STD_BC, SP_SIZ_CF);
			fail();
		} catch (IllegalArgumentException e) {} catch (Exception e) { fail(); }
	}
	
	@Test
	public void testHashcode() {
		assertTrue(SP_STD_BCF.hashCode()==SP_STD_BCF.hashCode());
		assertTrue(SP_STD_BCF.hashCode()==SP_STD_HM.hashCode()); // Same split differently expressed
		// It would be nice if this were true, but currently equivalent but not same IdGroup objects yield different hashcodes 
		assertFalse(SP_STD_BCF.hashCode()==SP_ROD_HM.hashCode()); 
		assertFalse(SP_STD_BCF.hashCode()==SP_ROD_BC.hashCode());
		assertFalse(SP_STD_BC.hashCode()==SP_DIF_CF.hashCode());
	}
	
	@Test
	public void testSetIdGroup() {
		// Create local version of SP_ROD_BCF as this test changes it
		SmallSplit SP_ROD_BCF = new SmallSplit(REORDERED,B01110); // bat cat fat
		// Id groups are equivalent, but different objects:
		assertFalse(SP_ROD_BCF.getIdGroup() == SP_STD_BCF.getIdGroup());
		SP_ROD_BCF.setIdGroup(SP_STD_BCF.getIdGroup());
		assertTrue(SP_ROD_BCF.getIdGroup() == SP_STD_BCF.getIdGroup());
		try {
			SP_ROD_BCF.setIdGroup(SP_ROD_BCF);
			fail();
		} catch (IllegalArgumentException e) {} catch (Exception e) { fail(); }
	}
	
	@Test
	public void testGetBooleanArray() {
		assertTrue(Arrays.equals(SP_STD_HM.getBooleanArray(),B00011));
		assertFalse(Arrays.equals(SP_ROD_BCF.getBooleanArray(),B01110));
	}
	
	@Test
	public void testHexString() {
		assertTrue(SP_STD_BC.toHexString().equals("07")); // In alphabetic order, 11000. Invert to set MSB to zero -> 00111 -> 0x07
		assertTrue(SP_STD_BCF.toHexString().equals("03")); // 11100->00011->0x03
		assertTrue(SP_STD_HM.toHexString().equals("03")); // 00011->00011->0x03
		assertTrue(SP_STD_CF.toHexString().equals("0C")); // 01100->01100->0x0C
	}
}
