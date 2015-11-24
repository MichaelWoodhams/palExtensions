package palExtensions;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;

import pal.tree.AttributeNode;
import pal.tree.Node;
import pal.tree.NodeUtils;

public class ExNodeUtils {
	private static final String ORDER_ATTR = "ExNodeUtils.ORDER";
	private static final String NULL = "If you ever see this, there is a bug in ExNodeUtils";
	
	/**
	 * (Possibly) changes order of children in this node and its descendents
	 * to put them into canonical order: I.e. child subtrees are ordered by
	 * the lexographically smallest leaf name they contain.
	 * @param node
	 */
	public static void reorderCanonically(AttributeNode node) {
		nullifyAttributeRecursively((AttributeNode) node,ORDER_ATTR);
		privateReorderCanonically(node);
	}
	
	// must have attribute ORDER_ATTR cleared before top level call to this method
	private static void privateReorderCanonically(AttributeNode node) {
		Comparator<AttributeNode> canonicalNodeComparator = new Comparator<AttributeNode>() {
			public int compare(AttributeNode node0, AttributeNode node1) {
				return getSetSmallestLeaf(node0).compareTo(getSetSmallestLeaf(node1));
			}
		};
		
		int nChild = node.getChildCount();
		if (nChild<=1) return;
		AttributeNode[] children = new AttributeNode[nChild];
		for (int i=nChild-1; i>=0; i--) {
			children[i] = (AttributeNode)node.getChild(i);
			node.removeChild(i);
			privateReorderCanonically(children[i]);
		}
		Arrays.sort(children, canonicalNodeComparator);
		for (int i=0; i<nChild; i++) {
			node.addChild(children[i]);
		}
	}
	
	/**
	 * AttributeNode has no 'remove attribute' ability, and 'null' cannot be
	 * stored as an attribute value, so use a private object to represent
	 * absent value.
	 * 
	 * Assumes tree nodes: will loop until stack exhaustion on a cyclic graph.
	 */
	public static void nullifyAttributeRecursively(AttributeNode node, String attribute) {
		node.setAttribute(attribute, NULL);
		for (int i=0; i<node.getChildCount(); i++) {
			nullifyAttributeRecursively((AttributeNode)node.getChild(i),attribute);
		}
	}
	
	/**
	 * Returns the (lexographically) smallest leaf name which is a descendent 
	 * of this node. Caches this information in an attribute (and uses
	 * cached value instead of recalculating, if cached values is present.)
	 * 
	 * Acts recursively, so will cache values in all descendent nodes (if
	 * not already done.)
	 * 
	 * @param node
	 */
	private static String getSetSmallestLeaf(AttributeNode node) {
		String smallest = (String)node.getAttribute(ORDER_ATTR);
		if (smallest==NULL) {
			// Not cached, so need to recalculate
			if (node.isLeaf()) {
				smallest=node.getIdentifier().toString();
			} else {
				smallest=getSetSmallestLeaf((AttributeNode)node.getChild(0));
				int n = node.getChildCount();
				for (int i=1; i<n; i++) {
					String temp = getSetSmallestLeaf((AttributeNode)node.getChild(i));
					if (temp.compareTo(smallest)<0) smallest = temp; 
				}
			}
			node.setAttribute(ORDER_ATTR, smallest);
		}
		return smallest;
	}
	
	/**
	 * returns NH format string, without branch lengths etc.
	 * @param node
	 * @return
	 */
	public static String toStringSimplified(Node node) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		NodeUtils.printNH(pw, node, false, false);
		pw.close();
		try {
			sw.close();
		} catch (IOException e) {
			System.err.println("Can't happen");
			e.printStackTrace();
		}
		return sw.toString();
	}
	
	/**
	 * NOTE: This routine may reorder childen in this node or its descendents.
	 * @param node
	 * @return
	 */
	public static String toTopologyString(AttributeNode node) {
		reorderCanonically(node);
		return toStringSimplified(node);
	}
}
