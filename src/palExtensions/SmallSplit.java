package palExtensions;

import java.util.HashSet;
import java.util.Set;

import pal.misc.IdGroup;
import pal.misc.Identifier;

/**
 * Stores a split on a taxon set of up to size 64.
 * 
 * 
 * @author woodhams
 *
 */

// TODO: Could override getBooleanArray with a less general more efficient version. 
public class SmallSplit extends Split {
	private static final long serialVersionUID = -818119290208793838L;
	/*
	 * Split is binary encoded with most significant bit corresponding to 
	 * Identifier 0.
	 */
	private long encoded;
	
	public SmallSplit(IdGroup idGroup, boolean[] split) {
		super(idGroup);
		if (nTaxa >64) throw new IllegalArgumentException("SmallSplit can only handle up to 64 taxa");
		if (split.length != nTaxa) throw new IllegalArgumentException("Split array has wrong length");
		setSplit(split,idGroup);
	}

	/**
	 * Only for use when we know the boolean array is in the right order for
	 * this split's IdGroup.
	 * 
	 * Consider making this public.
	 * @param split
	 */
    private void setSplit(boolean[] split) {
		// (t,f) = (0,1) if split[0], (1,0) otherwise. 
		// This ensures Identifier 0 is encoded with a 0 bit, for consistency.
		long t = split[0] ? 0 : 1;
		long f = 1-t;
		// can skip tax=0 in loop as it will always be zero.
		for (int tax=1; tax<nTaxa; tax++) {
			encoded = (encoded<<1) + (split[tax] ? t : f);
		}    	
    }
    
    /**
     * Set the split from a boolean array in the case where the order of the taxa in the array
     * might not be orthographic, hence some reordering is necessary.
     */
    public void setSplit(boolean[] split, IdGroup idOrder) {
    	if (IdGroupUtils.isOrdered(idOrder)) {
    		setSplit(split);
    	} else {
			int[] perm = IdGroupUtils.permuteToOrder(idGroup, idOrder);
			// (t,f) = (0,1) if split[0], (1,0) otherwise. 
			// This ensures Identifier 0 is encoded with a 0 bit, for consistency.
			long t = split[perm[0]] ? 0 : 1;
			long f = 1-t;
			// can skip tax=0 in loop as it will always be zero.
			for (int tax=1; tax<nTaxa; tax++) {
				encoded = (encoded<<1) + (split[perm[tax]] ? t : f);
			}    	
    	}
    }
    
    public boolean equals(SmallSplit other) {
    	return IdGroupUtils.equals(this, other) && this.encoded == other.encoded; 
    }

	/**
	 * Can throw IllegalArgumentExceptions if IdGroups of the splits do not have matching labels.
	 */
	public boolean compatible(SmallSplit other) {
		if (other instanceof SmallSplit) {
			if (!IdGroupUtils.equals(this.idGroup, other.idGroup))
				throw new IllegalArgumentException("Attempting to compare splits on different taxa sets");
			long otherEncoded = ((SmallSplit)other).encoded;
			return ((encoded & otherEncoded) == encoded || (encoded & otherEncoded) == otherEncoded);
		} else {
			// Can throw IllegalArgumentException
			return Split.compatible(this, other);
		}
	}
	
	@Override
	protected boolean isMember(int i) {
		if (i<0 || i>=nTaxa) throw new IllegalArgumentException("Illegal taxon index");
		return (encoded & (1 << (nTaxa-i-1)))!=0;
	}
	
	@Override
	public String toHexString() {
		int hexDigits = (nTaxa+3)/4;
		String format = "%0"+Integer.toString(hexDigits)+"X";
		return String.format(format, encoded); 
	}
	
	
	// Found from Mathematica by: RandomPrime[{2000000000, 2100000000}]
	//private static final long LARGE_PRIME = 2020209151;
	// Found from Mathematica by: RandomPrime[{10^18, 9 10^18}]
	private static final long LARGE_PRIME = 5941310150097163001L;
	/**
	 * Has a drawback: If two identical splits have equivalent
	 * IdGroup (contain equal strings in the same order) but
	 * are not the same object, hashCodes will not be equal.
	 * Good practice in creating Splits will not do this, however.
	 */
	@Override
	public int hashCode() {
		/*
		 *  It would be simpler to hashcode
		 *  the 64 bit 'encoded' by: (int)((encoded)^(encoded>>>32)
		 *  but I think unsafe as correlations between high and low 32 bits are likely.
		 */
		//return idGroup.hashCode() ^ (int)((encoded*LARGE_PRIME)>>>32);
		int temp1 =  idGroup.hashCode();
		long temp2 = encoded*LARGE_PRIME;
		long temp3 = temp2>>>32;
		int temp4 = (int)temp3;
		int temp5 = temp1 & temp4;
		return temp5;
		// temp variables to aid debugging trace
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
