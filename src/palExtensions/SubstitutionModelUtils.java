package palExtensions;

import pal.substmodel.SubstitutionModel;

public class SubstitutionModelUtils {
	public static void resetToDefault(SubstitutionModel model) {
		int n=model.getNumParameters();
		for (int i=0; i<n; i++) {
			model.setParameter(model.getDefaultValue(i),i);
		}
	}

}
