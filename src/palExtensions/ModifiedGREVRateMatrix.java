package palExtensions;

import pal.substmodel.GeneralREVRateMatrix;
/**
 * Just a wrapper around Pal's GeneralREVRateMatrix, but with a non-zero lower bound on
 * parameters to ensure it plays nicely with ConjugateDirectionSearch.
 * @author woodhams
 *
 */

public class ModifiedGREVRateMatrix extends GeneralREVRateMatrix {
	private static final long serialVersionUID = -1665232672437209655L;
	// The entire point of this class:
	public double getRateParameterLowerBound(int parameter) { return 0.00001; }
	
	public ModifiedGREVRateMatrix(int dimension) {
		super(dimension);
	}
	 public ModifiedGREVRateMatrix(int dimension, int[] constraints, double[] specifiedDefaultParameters, int fixedConstraintValue) {
		 super(dimension,constraints,specifiedDefaultParameters,fixedConstraintValue);
	 }
	
	public static final ModifiedGREVRateMatrix modCreateHKY() {
		return new ModifiedGREVRateMatrix(4,new int[] {0, 1, 0, 0, 1, 0},new double[] { 2.0 },0);
	}
	public static final ModifiedGREVRateMatrix modCreateGTR() { return new ModifiedGREVRateMatrix(4); }
}
