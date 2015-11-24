package palExtensions;

import java.util.List;

import pal.math.MersenneTwisterFast;

/**
 * A subclass replacement for PAL's MersenneTwisterFast with few extra 
 * capabilities.
 * 
 * Attempts to allows us to demand that all RNGs be seeded.
 * Alas, due to many methods in pal.math.MersenneTwisterFast being 'final', 
 * the check is only on nextInt(int) and shuffled(int) methods. 
 * Fortunately some other methods use nextInt(int) and so will also be caught.
 * I'm really getting to hate 'final' declarations.
 * 
 * @author woodhams
 *
 */

public class ExtRandom extends MersenneTwisterFast {
	private static final long serialVersionUID = -6506899262232124633L;
	private static boolean ALLOW_UNSEEDED = true; 
	private boolean seeded; // Has this instance been seeded? (else
	
	public ExtRandom() {
		super();
		seeded = false;
	}
	
	public ExtRandom(long seed) {
		super(seed);
		seeded = true;
	}
	

	/**
	 * If ALLOW_UNSEEDED is false, causes use of unseeded RNG to throw an exception.
	 */
	private void seedTest() {
		if (!ALLOW_UNSEEDED & !seeded) { throw new RuntimeException("Attempted to use an unseeded random number generator"); }
	}
	/**
	 *  Throw an exception on any future attempt to get a random number from an unseeded generator
	 */
	public static void forbidUnseeded() {
		ALLOW_UNSEEDED = false;
	}
	/**
	 *  Undoes forbitUnseeded() and resumes normal service.
	 */
	public static void allowUnseeded() {
		ALLOW_UNSEEDED = true;
	}
	
	@Override 
	public int nextInt(int n) {
		seedTest();
		return super.nextInt(n);
	}
	
	/**
	 * Returns random value from an exponential distribution of mean mu.
	 * @param mu
	 * @return
	 */
    public final double nextExponential(double mu) {
    	seedTest();
    	return -Math.log(nextDouble())*mu;
    }
    
	/**
	 * Shuffles a subrange of a list
	 * @param <T>
	 * @param list The list to shuffle
	 * @param startIndex the starting index of the portion of the array to shuffle
	 * @param length the length of the portion of the array to shuffle
	 */
    /*
     * Modified from MersenneTwisterFast.shuffleSubset(int,int,Object[])
     */
	public final <T> void shuffleSubset(int startIndex, int length, List<T> list) {
		for (int i = 0; i < length; i++) {
			final int index = nextInt(length-i) + i;
			final int first = startIndex+index;
			final int second = startIndex+i;
			final T temp = list.get(first);
			list.set(first, list.get(second));
			list.set(second, temp);
		}
	}
	/**
	 * Shuffles a list 
	 * @param list The list to shuffle
	 */
	public final <T> void shuffle(List<T> list) {
		shuffleSubset(0,list.size(), list);
	}

}
