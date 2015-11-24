package palExtensions;

import java.util.EnumMap;

import pal.distance.DistanceMatrix;
import pal.misc.IdGroup;
import pal.misc.Identifier;
import palExtensions.Quartet.Topology;

/**
 * A Quadruple is a set of 4 taxa. I.e. it is a Quartet without a known topology.
 * Superclass to Quartet
 * @author woodhams
 *
 */
/*
 * TODO: Think more on whether Quadruple should be and IdGroup, or merely have an IdGroup.
 */
public class Quadruple {
	private IdGroup idGroup;
	private int[] index;
	private EnumMap<Topology,Quartet> quartets;
	
	/**
	 * 
	 * @param idGroup: if you will be using whichQuartet method, this must be the distance matrix.
	 * @param index gets cloned, so need not pass a new array each time.
	 */
	public Quadruple(IdGroup idGroup, int[] index) {
		boolean bad = (index.length!=4);
		int n = idGroup.getIdCount();
		for (int i : index) {
			bad = bad || i>=n || i<0;
		}
		for (int i=1; i<index.length; i++) {
			bad = bad || index[i-1]>=index[i];
		}
		if (bad) throw new IllegalArgumentException("Index list must contain 4 indices within range in increasing order");
		this.index = index.clone();
		this.idGroup = idGroup;
		this.quartets = new EnumMap<>(Topology.class); 
		for (Topology topo : Topology.values()) {
			quartets.put(topo, new Quartet(this,topo));
		}
	}
	
	public Quadruple(IdGroup idGroup, int i, int j, int k, int l) {
		this(idGroup,new int[]{i,j,k,l});
	}
	
	public Identifier getIdentifier(int i) {
		return idGroup.getIdentifier(index[i]);
	}
	
	/**
	 * 
	 * @param i 0..3
	 * @return index (lookup-number into IdGroup) of that member of the quartet.
	 */
	public int getIndex(int i) {
		return index[i];
	}
	
	public IdGroup getIdGroup() {
		return idGroup;
	}
	/**
	 * Returns an array of all Quadruples from an idGroup.
	 * NOTE! Size of array is O(n^4) in size of idGroup.
	 * @param idGroup
	 * @return
	 */
	public static Quadruple[] allQuadruples(IdGroup idGroup) {
		int n=idGroup.getIdCount();
		if (n<4) throw new IllegalArgumentException("IdGroup too small");
		int nQuadruples = n*(n-1)*(n-2)*(n-3)/24; // 24 = 4!
		Quadruple[] all = new  Quadruple[nQuadruples];
		int next = 0;
		for (int i=0; i<n-3; i++)
			for (int j=i+1; j<n-2; j++)
				for (int k=j+1; k<n-1; k++)
					for (int l=k+1; l<n; l++)
						all[next++] = new Quadruple(idGroup,i,j,k,l);
		assert (all[nQuadruples-1]!=null); // check we've filled the array
		return all;
	}
	
	@Override
	public String toString() {
		return "Quad["+getIdentifier(0).toString()+","
			          +getIdentifier(1).toString()+","
			          +getIdentifier(2).toString()+","
			          +getIdentifier(3).toString()+"]";
	}
	
	/**
	 * Uses the four-point-condition distances to determine which quartet describes
	 * this quadruple. This will work with any positive edge weights on the tree.
	 * Weight of 1 on each edge (i.e. count-edges distance) is convenient.
	 * 
	 * With actual distances, it is possible (untested) that a quartet which is actually
	 * unresolved might be considered resolved due to rounding errors.
	 * 
	 * @param dist
	 * @return
	 */
	public Quartet whichQuartet(DistanceMatrix dist) {
		if (dist.getIdCount() != idGroup.getIdCount()) 
			throw new IllegalArgumentException("Distance matrix is the wrong size"); 
		double minDist = Double.MAX_VALUE;
		Topology bestTopo = null;
		for (Topology topo : Quartet.RESOLVED) {
			double thisDist = quartets.get(topo).fourPointDistance(dist);
			if (thisDist == minDist) {
				// we've seen this distance before, so (for now) topology is unresolved.
				bestTopo = Topology.UNRESOLVED;
			} else if (thisDist < minDist) {
				minDist = thisDist;
				bestTopo = topo;
			} 
		}
		return quartets.get(bestTopo);
	}
}
