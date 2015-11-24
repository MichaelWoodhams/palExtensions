package palExtensions;

import java.util.Comparator;
import pal.misc.Identifier;

public class IdentifierUtils {
	public static final Comparator<Identifier> COMPARATOR = new Comparator<Identifier>() {
		public int compare(Identifier id1, Identifier id2) {
			return id1.toString().compareTo(id2.toString());
		}
	};
}
