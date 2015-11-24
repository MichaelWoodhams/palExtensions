package palExtensions;

import pal.misc.IdGroup;

public abstract class PartialSplit extends Split {
	private static final long serialVersionUID = 6225936107332390948L;
	protected IdGroup universalIdGroup;
	protected int universalNTaxa;

	protected PartialSplit(IdGroup idGroup, IdGroup universalIdGroup) {
		super(idGroup);
		if (!IdGroupUtils.isSubset(idGroup, universalIdGroup)) {
			throw new IllegalArgumentException("Partial split IdGroup must be subset of universal IdGroup");
		}
		if (IdGroupUtils.isOrdered(universalIdGroup)) {
			this.universalIdGroup = universalIdGroup;
		} else {
			this.universalIdGroup = IdGroupUtils.copyOrdered(universalIdGroup);
		}
		universalNTaxa=universalIdGroup.getIdCount();
	}
	
	/*
	 * Will return the appropriate subclass of Split for the size of the data array
	 */
	public static Split toPartialSplit(IdGroup idGroup, boolean[] rawSplit, IdGroup universalIdGroup) {
		if (universalIdGroup.getIdCount()>SmallPartialSplit.MAXSIZE) {
			throw new RuntimeException("Splits over 64 length not yet implemented");
		} else {
			return new SmallPartialSplit(idGroup, rawSplit, universalIdGroup);
		}
	}
	
	// True if nth identifier in universal group is a member of the partial split
	public abstract boolean isInPartial(int n);
	
	public IdGroup getUniversalIdGroup() {
		return universalIdGroup;
	}
	
	/**
	 * Can be used to ensure multiple splits have the SAME idGroup object, rather than just equivalent ones.
	 * newGroup must be sorted in alphabetic order, and must have same taxon set
	 * as existing idGroup.
	 */
	@Override
	public void setIdGroup(IdGroup newGroup) {
		if (!IdGroupUtils.isSubset(idGroup, universalIdGroup)) {
			throw new IllegalArgumentException("Partial split IdGroup must be subset of universal IdGroup");
		}
		super.setIdGroup(newGroup);
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
	public void setUniversalIdGroup(IdGroup newGroup) {
		if (!IdGroupUtils.equals(universalIdGroup, newGroup)) 
			throw new IllegalArgumentException("Tried to change split IdGroup to one which does not match");
		// If someone wants infinite regress, at least make them work a little bit for it:
		if (this == newGroup) throw new IllegalArgumentException("Tried to cause infinite regress");
		universalIdGroup = newGroup;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer("(");
		String comma = "";
		for (int i=0; i<universalNTaxa; i++) {
			buf.append(comma);
			buf.append(isInPartial(i) ? (isMember(i) ? '+' : '-') : '?');
			buf.append(idGroup.getIdentifier(i).toString());
			comma = ",";
		}
		buf.append(')');
		return buf.toString();
	}

	/*
	 * For the following, Split has a default implementation which I don't want to use, 
	 * and expect subclasses to override, so throw an error to avoid accidental use
	 * of Split default implementation.  These should never get called.
	 */
	@Override
	public int hashCode() {
		throw new RuntimeException("Abstract class PartialSplit should have hashcode overridden in all subclasses");
	}
	@Override
	public boolean compatible(Split other) {
		throw new RuntimeException("Abstract class PartialSplit should have compatible overridden in all subclasses");
	}
	@Override
	public boolean equals(Split other) {
		throw new RuntimeException("Abstract class PartialSplit should have equals overridden in all subclasses");
	}		
}
