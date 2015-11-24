package palExtensions;

import java.util.HashSet;
import java.util.Set;

import pal.misc.IdGroup;
import pal.misc.Identifier;

public class SmallPartialSplit extends PartialSplit {
	private static final long serialVersionUID = 6135773034714427075L;
	public static final int MAXSIZE = 64;
	private long encoded;
	private long mask; // 1 bits are in the partial split, 0 are excluded
	int[] localToGlobal; // index = bit number within partial split, value = bit number in global set

	public SmallPartialSplit(IdGroup idGroup, boolean[] split, IdGroup universalIdGroup) {
		super(idGroup, universalIdGroup);
		if (universalNTaxa >MAXSIZE) throw new IllegalArgumentException("SmallPartialSplit can only handle up to 64 taxa");
		if (split.length != nTaxa) throw new IllegalArgumentException("Split array has wrong length");
		setMask();
		setSplit(split);
	}
	
	/**
	 * Can be used to ensure multiple splits have the SAME idGroup object, rather than just equivalent ones.
	 * newGroup must be sorted in alphabetic order, and must have same taxon set
	 * as existing idGroup.
	 */
	@Override
	public void setIdGroup(IdGroup newGroup) {
		super.setIdGroup(newGroup);
		setMask();
	}


	private void setMask() {
		mask=0;
		localToGlobal = new int[nTaxa];
		int localIndex = nTaxa-1;
		for (int i=universalNTaxa-1; i>=0; i--) {
			mask >>=1;
			if (universalIdGroup.whichIdNumber(idGroup.getIdentifier(i).getName())>=0) {
				mask++;
				localToGlobal[localIndex--]=i;
			}
		}
	}
	
	/**
	 * Only for use when:
	 * * local taxa and global taxa both ordered and local is subset of global
	 * * mask has been set
	 * * 'split' has order corresponding to local taxa IdGroup.
	 * 
	 * Consider making this public.
	 * @param split
	 */
    private void setSplit(boolean[] split) {
		encoded=0;
		// (t,f) = (0,1) if split[0], (1,0) otherwise. 
		// This ensures Identifier 0 is encoded with a 0 bit, for consistency.
		long t = split[0] ? 0 : 1;
		long f = 1-t;
		// can skip tax=0 in loop as it will always be zero.
		for (int tax=1; tax<nTaxa; tax++) {
			int shift = localToGlobal[tax]-localToGlobal[tax-1];
			encoded = (encoded<<shift) + (split[tax] ? t : f);
		}    	
    }

	
	/**
	 * Set partial split without changing the partial split or universal taxon sets.
	 * idOrder can have different ordering to existing partial split set, but must
	 * have the same taxa.
	 * @param split
	 */
	public void setSplit(IdGroup idOrder, boolean[] split) {
		if (idOrder.equals(idGroup)) {
			setSplit(split);
		} else {
			int[] perm = IdGroupUtils.permuteToOrder(idGroup, idOrder);
			boolean[] permuted = new boolean[nTaxa];
			for (int i=0; i<nTaxa; i++) {
				permuted[i] = split[perm[i]];
			}
			setSplit(split);
		}
	}

	/**
	 * n = index into universal group
	 */
	@Override
	public boolean isInPartial(int n) {
		return (((long)1<<n) & mask)!=0; 
	}

	
	/*
	 * isMember, countSetBits, sizeOfSmaller, smallerSubset are direct
	 * cut and paste from SmallSplit class.
	 */
	/**
	 * i = index into partial split group
	 */
	@Override
	protected boolean isMember(int i) {
		return (((long)1<<localToGlobal[i]) & encoded)!=0; 
	}

	private int countSetBits() {
		long temp = encoded;
		int bitCount = 0;
		do {
			bitCount += temp & 1;
			temp >>>= 1;
		} while (temp!=0);
		return bitCount;
	}
	
	@Override 
	public int sizeOfSmaller() {
		int bitCount = countSetBits();
		return (bitCount*2>nTaxa) ? nTaxa - bitCount : bitCount;
	}

	@Override
	public Set<Identifier> smallerSubset() {
		Set<Identifier> set = new HashSet<Identifier>(nTaxa/2);
		int bitCount = countSetBits();
		if (bitCount*2>nTaxa) {
			// zero bits are the smaller subset
			for (int i=0; i<nTaxa; i++) {
				if (!isMember(i)) {
					set.add(idGroup.getIdentifier(i));
				}
			}
		} else {
			// one bits are the smaller subset
			for (int i=0; i<nTaxa; i++) {
				if (isMember(i)) {
					set.add(idGroup.getIdentifier(i));
				}
			}
		}
		return set;
	}
}
