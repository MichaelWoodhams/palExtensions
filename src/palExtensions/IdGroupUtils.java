package palExtensions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import pal.misc.IdGroup;
import pal.misc.Identifier;
import pal.misc.SimpleIdGroup;

public class IdGroupUtils {
	// public static boolean equivalent(IdGroup group1, IdGroup group2) // same labels in same order
	/**
	 * Return int[] perm such that reorder[perm[i]]=template[i] (where '=' means identifiers have same label.)
	 * Throws an error if 'template' and 'reorder' do not agree on taxon set.
	 * @param template
	 * @param reorder
	 * @return
	 */
	public static int[] permuteToOrder(IdGroup template, IdGroup reorder) {
		int n=template.getIdCount();
		if (reorder.getIdCount() != n) throw new IllegalArgumentException("IdGroups have different length");
		int[] perm = new int[n];
		for (int i=0; i<n; i++) {
			int x = reorder.whichIdNumber(template.getIdentifier(i).toString());
			if (x==-1) throw new IllegalArgumentException("IdGroups have differing label sets");
			perm[i]=x;
		}
		return perm;
	}
	
	/**
	 * Returns true if the Identifiers are in lexographical order.
	 * @return
	 */
	public static boolean isOrdered(IdGroup group) {
		boolean ordered = true;
		for (int i=1; i<group.getIdCount(); i++) {
			if (group.getIdentifier(i-1).toString().compareTo(group.getIdentifier(i).toString())>0) {
				ordered = false;
				break;
			}
		}
		return ordered;
	}
	
	/**
	 * Return an IdGroup with the same Identifier set as the argument, but sorted into
	 * lexographic order.
	 * 
	 * @param group
	 * @return
	 */
	public static SimpleIdGroup copyOrdered(IdGroup group) {
		Identifier[] ids = new Identifier[group.getIdCount()];
		for (int i=0; i<ids.length; i++) ids[i] = group.getIdentifier(i);
		// Should PAL ever be updated to use Java's Comparable:
		// Arrays.sort(ids);
		Arrays.sort(ids, IdentifierUtils.COMPARATOR);
		return new SimpleIdGroup(ids);
	}
	
	/**
	 * Return 'true' if group1 and group2 contain the same labels in the same order
	 * @param group1
	 * @param group2
	 * @return
	 */
	public static boolean equals(IdGroup group1, IdGroup group2) {
		if (group1 == group2) return true;
		if (group1.getIdCount()!=group2.getIdCount()) return false;
		for (int i=0; i<group1.getIdCount(); i++) {
			if (group1.getIdentifier(i)!=group2.getIdentifier(i) && 
			    ! group1.getIdentifier(i).toString().equals(group2.getIdentifier(i).toString()))
				return false;
		}
		return true;
	}

	
	/**
	 * Return 'true' if group1 and group2 contain the same labels, possibly in different order
	 * @param group1
	 * @param group2
	 * @return
	 */
	public static boolean sameLabels(IdGroup group1, IdGroup group2) {
		if (group1 == group2) return true;
		int nLabels = group1.getIdCount();
		if (group2.getIdCount()!=nLabels) return false;
		Set<String> labels1 = new HashSet<String>(nLabels);
		Set<String> labels2 = new HashSet<String>(nLabels);
		for (int i=0; i<nLabels; i++) {
			labels1.add(group1.getIdentifier(i).getName());
			labels2.add(group2.getIdentifier(i).getName());
		}
		labels1.removeAll(labels2);
		return (labels1.size()==0);
	}
	
	/**
	 * Returns an IdGroup with identifiers sorted alphabetically
	 * @param unsorted
	 * @return
	 */

	public static IdGroup copySorted(IdGroup unsorted) {
		int n = unsorted.getIdCount();
		Identifier[] ids = new Identifier[n];
		for (int i=0; i<n; i++) ids[i] = unsorted.getIdentifier(i);
		java.util.Arrays.sort(ids,IdentifierUtils.COMPARATOR);
		return new SimpleIdGroup(ids);
	}

	public static boolean isSubset(IdGroup subgroup, IdGroup supergroup) {
		boolean subset = true;
		for (int i=0; subset && i<subgroup.getIdCount(); i++) {
			// whichIdNumber returns negative value if name is not found
			subset = (supergroup.whichIdNumber(subgroup.getIdentifier(i).getName())>=0);
		}
		return subset;
	}
	
	public static String[] toStringArray(IdGroup group) {
		int n= group.getIdCount();
		String[] names = new String[n];
		for (int i=0; i<n; i++) {
			names[i] = group.getIdentifier(i).getName();
		}
		return(names);
	}
}
