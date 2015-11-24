package junit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import pal.misc.IdGroup;
import pal.misc.SimpleIdGroup;
import pal.tree.Node;
import pal.tree.SimpleNode;
import pal.tree.SimpleTree;
import pal.tree.Tree;
import palExtensions.NeoSplitSystem;
import palExtensions.NeoSplitUtils;
import palExtensions.SmallSplit;

public class NeoSplitSystemTest {
	/*
	 * For taxon set {Bat, Cat, Fat, Hat, Mat}, list the hex forms of various splits
	 */
	private static final String xCF = "0C"; // BCFHM->01100
	private static final String xCFM = "0D"; // 01101
	private static final String xBC = "07"; // 11000->00111
	//private static final String xBF = "0B"; // 10100->01011
	// Set up test data
	private static final Tree BIN_TREE;
	private static final Tree NONBIN_TREE;
	private static final IdGroup IDGROUP;
	static {
		Node BAT = new SimpleNode("Bat",1.0);
		Node CAT = new SimpleNode("Cat",1.0);
		Node FAT = new SimpleNode("Fat",1.0);
		Node HAT = new SimpleNode("Hat",1.0);
		Node MAT = new SimpleNode("Mat",1.0);
		Node BH = new SimpleNode();
		Node FC = new SimpleNode();
		Node FCM = new SimpleNode();
		BH.addChild(BAT);
		BH.addChild(HAT);
		FC.addChild(FAT);
		FC.addChild(CAT);
		FCM.addChild(FC);
		FCM.addChild(MAT);
		Node root = new SimpleNode();
		root.addChild(BH);
		root.addChild(FCM);
		BIN_TREE = new SimpleTree(root);
		BAT = new SimpleNode("Bat",1.0);
		CAT = new SimpleNode("Cat",1.0);
		FAT = new SimpleNode("Fat",1.0);
		HAT = new SimpleNode("Hat",1.0);
		MAT = new SimpleNode("Mat",1.0);
		BH = new SimpleNode();
		BH.addChild(BAT);
		BH.addChild(HAT);
		FCM = new SimpleNode();
		FCM.addChild(FAT);
		FCM.addChild(CAT);
		FCM.addChild(MAT);
		root = new SimpleNode();
		root.addChild(BH);
		root.addChild(FCM);
		NONBIN_TREE = new SimpleTree(root);
		IDGROUP = new SimpleIdGroup(5);
		IDGROUP.setIdentifier(0, BAT.getIdentifier());
		IDGROUP.setIdentifier(1, CAT.getIdentifier());
		IDGROUP.setIdentifier(2, FAT.getIdentifier());
		IDGROUP.setIdentifier(3, HAT.getIdentifier());
		IDGROUP.setIdentifier(4, MAT.getIdentifier());
	}
	private static boolean[] B11000 = new boolean[]{ true, true,false,false,false};
	//private static boolean[] B11100 = new boolean[]{ true, true, true,false,false};
	private static boolean[] B01100 = new boolean[]{false, true, true,false,false};
	//private static boolean[] B00011 = new boolean[]{false,false,false, true, true};
	//private static boolean[] B10001 = new boolean[]{ true,false,false,false, true};
	//private static boolean[] B01110 = new boolean[]{false, true, true, true,false};
	//private static boolean[] B00110 = new boolean[]{false,false, true, true,false};
	private static SmallSplit SP_BC  = new SmallSplit(IDGROUP, B11000); // bat cat
	//private static SmallSplit SP_BCF = new SmallSplit(IDGROUP, B11100); // bat cat fat
	//private static SmallSplit SP_HM  = new SmallSplit(IDGROUP, B00011); // hat mat
	private static SmallSplit SP_CF  = new SmallSplit(IDGROUP, B01100); // cat fat
	

	private String[] splitSysToHexArray(NeoSplitSystem splitSys) {
		String[] array = new String[splitSys.getSplitCount()];
		for (int i=0; i<array.length; i++) {
			array[i]=splitSys.get(i).toHexString();
		}
		Arrays.sort(array);
		return array;
	}
	
	@Test
	public void test() {
		NeoSplitSystem splitSys = NeoSplitUtils.getSplits(BIN_TREE);
		String[] expected = new String[]{xCF,xCFM};
		Arrays.sort(expected);
		String[] observed = splitSysToHexArray(splitSys);
		assertTrue(Arrays.equals(observed,expected));
		
		splitSys = NeoSplitUtils.getSplits(NONBIN_TREE);
		expected = new String[]{xCFM};
		Arrays.sort(expected);
		observed = splitSysToHexArray(splitSys);
		assertTrue(Arrays.equals(observed,expected));
		
		splitSys = new NeoSplitSystem(IDGROUP, 0); // initial size zero, will grow dynamically
		splitSys.add(SP_CF);
		expected = new String[]{xCF};
		assertTrue(Arrays.equals(splitSysToHexArray(splitSys), expected));
		splitSys.add(SP_BC);
		expected = new String[]{xCF, xBC};
		Arrays.sort(expected);
		assertTrue(Arrays.equals(splitSysToHexArray(splitSys), expected));
		splitSys.remove(0); // SP_BC
		expected = new String[]{xBC};
		assertTrue(Arrays.equals(splitSysToHexArray(splitSys), expected));
	}

}
