package palExtensions;

import pal.distance.DistanceMatrix;


public class Quartet {
	// data members
	private Topology topo;
	private Quadruple quad;
	
	// helper Topology class
	public enum Topology {
		UNRESOLVED, P01V23, P02V13, P03V12;
		
	}
	public static Topology[] RESOLVED = new Topology[]{Topology.P01V23,Topology.P02V13,Topology.P03V12};
	// Would do this if Java allowed it:
	// private static final EnumMap<Topology><int[]> ORDER = new EnumMap<><>();
	public static final int[][] ORDER = new int[Topology.values().length][];
	static {
		ORDER[Topology.UNRESOLVED.ordinal()] = new int[]{3,2,1,0};
		ORDER[Topology.P01V23.ordinal()] = new int[]{0,1,2,3};
		ORDER[Topology.P02V13.ordinal()] = new int[]{0,2,1,3};
		ORDER[Topology.P03V12.ordinal()] = new int[]{0,3,1,2};
	}

	public Quartet(Quadruple quad, Topology topo) {
		if (quad == null || topo == null) throw new IllegalArgumentException("Can't handle nulls");
		this.quad = quad;
		this.topo = topo;
	}
	
	public Topology getTopology() {
		return topo;
	}
	
	public int getTopologyAsInt() {
		return topo.ordinal();
	}
	
	/**
	 * the i=0,1 taxa will be at one end of the quartet, i=2,3 at the other end.
	 * @param i
	 * @return index of taxon (i.e. lookup number into IdGroup)
	 */
	public int getIndex(int i) {
		return quad.getIndex(ORDER[topo.ordinal()][i]);
	}
	
	/**
	 * Returns the sum of distances over this quartet (as used in the four point condition)
	 * given a distance matrix with same taxon order as quadruple's IdGroup.
	 * 
	 * (The correct quartet for this quadruple is the one for which this function
	 * returns the lowest value.)
	 * 
	 * @param dist
	 * @return
	 */
	public double fourPointDistance(DistanceMatrix dist) {
		return dist.getDistance(this.getIndex(0), this.getIndex(1))+
			   dist.getDistance(this.getIndex(2), this.getIndex(3));
	}
	
	public String toString() {
		int[] order = ORDER[topo.ordinal()];
		if (topo!=Topology.UNRESOLVED) {
			return "Quart["+quad.getIdentifier(order[0]).toString()+","
					       +quad.getIdentifier(order[1]).toString()+"|"
					       +quad.getIdentifier(order[2]).toString()+","
					       +quad.getIdentifier(order[3]).toString()+"]";
		} else {
			/* 
			 * want to make unresolved quartets visually more obvious 
			 * that difference between "|" and "," burried deep inside.
			 */
			return "Quart<"+quad.getIdentifier(order[0]).toString()+","
		                   +quad.getIdentifier(order[1]).toString()+","
		                   +quad.getIdentifier(order[2]).toString()+","
		                   +quad.getIdentifier(order[3]).toString()+">";
		}
	}
}
