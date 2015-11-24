package palExtensions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import pal.misc.IdGroup;
import pal.misc.Identifier;

/*
 * To be subclassed into SmallSplit (up to 64 taxa) and LongSplit (more than 64 taxa).
 * 
 * Splits are always stored in lexographical order of the Identifiers. This means that two
 * splits on the same set of taxa are always directly comparable.
 */

@SuppressWarnings("serial")
public abstract class Split implements IdGroup {
	protected IdGroup idGroup;
	protected int nTaxa;
	
	protected Split(IdGroup idGroup) {
		if (IdGroupUtils.isOrdered(idGroup)) {
			this.idGroup = idGroup;
		} else {
			this.idGroup = IdGroupUtils.copyOrdered(idGroup);
		}
		nTaxa=idGroup.getIdCount();
	}
	
	/*
	 * Will return the appropriate subclass of Split for the size of the data array
	 */
	public static Split toSplit(IdGroup idGroup, boolean[] rawSplit) {
		if (rawSplit.length>64) {
			throw new RuntimeException("Splits over 64 length not yet implemented");
		} else {
			return new SmallSplit(idGroup, rawSplit);
		}
	}
	
	/**
	 * If two splits have the same IdGroup and same split hex-encoding,
	 * they are the same split so we want to ensure they have the same 
	 * hashcode. Expect this to be overridden in subclasses
	 */
	@Override
	public int hashCode() {
		return idGroup.hashCode()+this.toHexString().hashCode();
	}
	
	public IdGroup getIdGroup() {
		return idGroup;
	}
	
	/**
	 * Useful for ensuring a collection of splits have the same idGroup object
	 * 
	 * IMPORTANT: if you want Split 'new' to match IdGroup of 'old' you must do
	 *   new.setIdGroup(old.getIdGroup());
	 * NOT:
	 *   new.setIdGroup(old);
	 * which will not generate an error, but is unlikely to be what you want.
	 */
	public void setIdGroup(IdGroup newGroup) {
		if (!IdGroupUtils.isOrdered(idGroup)) {
			throw new IllegalArgumentException("newGroup must be ordered, for the sake of consistency");
		}
		if (!IdGroupUtils.equals(idGroup, newGroup)) 
			throw new IllegalArgumentException("Tried to change split IdGroup to one which does not match");
		// If someone wants infinite regress, at least make them work a little bit for it:
		if (this == newGroup) throw new IllegalArgumentException("Tried to cause infinite regress");
		idGroup = newGroup;
	}
	
	/**
	 * Note that this output must be interpreted in the context (i.e. order) of the split's IdGroup.
	 * @return Split as a boolean array
	 */
	public boolean[] getBooleanArray() {
		boolean[] array = new boolean[nTaxa];
		for (int i=0; i<nTaxa; i++) array[i] = isMember(i);
		return array;
	}
	
	/**
	 * Note that this output must be interpreted in the context (i.e. order) of the split's IdGroup.
	 * @return Split as a boolean array
	 */		
	public String toHexString() {
		return mdwUtils.Strings.booleanArrayToHexString(getBooleanArray());
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer("(");
		String comma = "";
		for (int i=0; i<nTaxa; i++) {
			buf.append(comma).append(isMember(i) ? '+' : '-').append(idGroup.getIdentifier(i).toString());
			comma = ",";
		}
		buf.append(')');
		return buf.toString();
	}
	
	/**
	 * Allows different subclasses of Split to be compared for compatibility.
	 * 
	 * Each subclass should contain a "compatible(SplitSubclass other)" method which is more efficient. 
	 * @param other
	 * @param perm
	 * @return
	 */
	public boolean compatible(Split other) {
		return compatible(this, other);
	}
	
	/**
	 * returns true if and only if split is compatible with every split in splitSet
	 */
	public boolean compatible(Iterable<Split> splitSet) {
		Iterator<Split> iterator = splitSet.iterator();
		while (iterator.hasNext()) {
			if (!compatible(iterator.next())) return false;
		}
		return true;
	}
	
	/**
	 * A very general compatibility test which allows comparing splits from different subclasses.
	 * 
	 * This is implemented as a public static method to allow unit testing to force the use of
	 * the general method. For normal use, use the instance 'compatible(Split)' method.
	 * 
	 * @param other
	 * @return
	 */
	public static boolean compatible(Split a, Split b) {
		if (!IdGroupUtils.equals(a,b)) throw new IllegalArgumentException("Attempting to compare splits on different taxa sets");
		boolean qAb = false; // there exists a label in 'a' but not in 'b'.
		boolean qaB = false; // there exists a label in 'b' but not in 'a'.
		boolean qAB = false; // there exists a label in both 'a' and 'b'.
		boolean qab = false; // there exists a label in neither 'a' nor 'b'.
		for (int i=0; i<a.nTaxa; i++) {
			boolean t = a.isMember(i);
			boolean o = b.isMember(i);
			qAb = qAb || (t&&!o);
			qaB = qaB || (o&&!t);
			qAB = qAB || (t&&o);
			qab = qab || !(t||o);
		}
		return !(qAB && qab && qAb && qaB);
	}
	
	/**
	 * Allows different subclasses of Split to be compared for compatibility.
	 * 
	 * Each subclass should contain an "equals(SplitSubclass other)" method which is more efficient. 
	 * @param other
	 * @return
	 */
	public boolean equals(Split other) {
		return equals(this,other);
	}
	
	/**
	 * A very general compatibility test which allows comparing splits from different subclasses.
	 * 
	 * This is implemented as a public static method to allow unit testing to force the use of
	 * the general method. For normal use, use the instance 'compatible(Split)' method.
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equals(Split a, Split b) {
		if (!IdGroupUtils.equals(a,b)) throw new IllegalArgumentException("Attempting to compare splits on different taxa sets");
		return Arrays.equals(a.getBooleanArray(),b.getBooleanArray());
	}
	
	protected abstract boolean isMember(int i);
	
	/**
	 * 
	 * @return the size of the smaller subset of the split
	 */
	public abstract int sizeOfSmaller();
	/**
	 * 
	 * @return the set of identifiers in the smaller side of the split
	 */
	public abstract Set<Identifier> smallerSubset();
	
	/*
	 * Methods to implement IdGroup, which just pass through to 'idg' member
	 */
	public int getIdCount()                  { return idGroup.getIdCount(); }
	public Identifier getIdentifier(int i)    {return idGroup.getIdentifier(i); }
	public void setIdentifier(int i, Identifier id) { idGroup.setIdentifier(i, id); };
	public int whichIdNumber(String name)     {return idGroup.whichIdNumber(name); }
}
