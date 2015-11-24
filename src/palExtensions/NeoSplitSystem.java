package palExtensions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Vector;

import pal.misc.IdGroup;
import pal.misc.Identifier;

/*
 * Possible todo: subclass PAL's SplitSystem so that this is a SplitSystem, even if it uses none of the original code
 */
public class NeoSplitSystem implements IdGroup, Iterable<Split>, Collection<Split>, List<Split>, RandomAccess {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2394179305014019413L;
	// Data members.
	private IdGroup idGroup; 
	private Vector<Split> splits; // Write-once (at least for now.) IdGroup of splits will always be idGroup.

	public NeoSplitSystem(IdGroup idGroup) {
		this(idGroup,0);
	}
	
	public NeoSplitSystem(IdGroup idGroup, int size)
	{
		if (IdGroupUtils.isOrdered(idGroup)) {
			this.idGroup = idGroup;
		} else {
			this.idGroup = IdGroupUtils.copyOrdered(idGroup);
		}
		if (size>0) {
			splits = new Vector<Split>(size);
		} else {
			splits = new Vector<Split>();
		}
	}
	
	public NeoSplitSystem(Collection<Split> splitCollection) {
		Iterator<Split> iterator = splitCollection.iterator();
		if (!iterator.hasNext()) throw new IllegalArgumentException("Can't make a NeoSplitSystem from no splits");
		Split firstSplit = iterator.next();
		idGroup = firstSplit.idGroup;
		if (!IdGroupUtils.isOrdered(idGroup)) {
			idGroup = IdGroupUtils.copyOrdered(idGroup);
		}
		splits = new Vector<Split>(splitCollection.size());
		splits.add(firstSplit);
		while (iterator.hasNext()) {
			add(iterator.next());
		}
	}
	
	public IdGroup getIdGroup() {
		return idGroup;
	}
	
	public int getSplitCount() {
		return splits.size();
	}
	
	public boolean isCompatible() {
		int n=splits.size();
		for (int i=0; i<n-1; i++) {
			Split split = splits.get(i);
			for (int j=i+1; j<n; j++) {
				if (!split.compatible(splits.get(j))) return false;
			}
		}
		return true;
	}
	
	
	/*
	 * IdGroup interface:
	 */
	@Override
	public void setIdentifier(int i, Identifier id) { 
		throw new UnsupportedOperationException("Can't change the identifiers of a NeoSplitSystem");
	}
	
	/*
	 * Collection interface:
	 */
	@Override
	public boolean add(Split split) { 
		split.setIdGroup(idGroup); // will throw exception if split has wrong set of taxa 
		return splits.add(split); 
	}
	
	@Override
	public boolean addAll(Collection<? extends Split> splitCollection) { 
		for (Split split : splitCollection) {
			split.setIdGroup(idGroup); // will throw exception if split has wrong set of taxa 
		}
		return splits.addAll(splitCollection); 
	}
	
	/*
	 * List interface
	 */
	@Override
	public void add(int index, Split split) {
		split.setIdGroup(idGroup); // will throw exception if split has wrong set of taxa 
		splits.add(index,split);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Split> splitCollection) {
		for (Split split : splitCollection) {
			split.setIdGroup(idGroup); // will throw exception if split has wrong set of taxa 
		}
		return splits.addAll(index, splitCollection);
	}
	
	/* ****************************************************************************************
	 * From here on, we have interface methods which simply pass through unmodified to
	 * idGroup (for IdGroup interface) or splits (for Iterable, Collection, List, RandomAccess interfaces.)
	 * Any which required special code are above this comment.
	 */
	
	
	/*
	 * IdGroup methods - pass through to idGroup
	 */
	@Override
	public int getIdCount() { return idGroup.getIdCount(); }
	@Override
	public Identifier getIdentifier(int i) { return idGroup.getIdentifier(i); }
	@Override
	public int whichIdNumber(String name) { return idGroup.whichIdNumber(name); }

	/*
	 * Iterable methods
	 */
	@Override
	public Iterator<Split> iterator() { return splits.iterator(); }


	/*
	 * Collection methods
	 */
	@Override
	public void clear() { splits.clear(); }
	@Override
	public boolean contains(Object arg0) { return splits.contains(arg0); }
	@Override
	public boolean containsAll(Collection<?> arg0) { return splits.containsAll(arg0); }
	@Override
	public boolean isEmpty() { return splits.isEmpty(); }
	@Override
	public boolean remove(Object arg0) { return splits.remove(arg0); }
	@Override
	public boolean removeAll(Collection<?> arg0) { return splits.removeAll(arg0); }
	@Override
	public boolean retainAll(Collection<?> arg0) { return splits.retainAll(arg0); }
	/**
	 * @return number of splits
	 */
	@Override
	public int size() { return splits.size(); }
	@Override
	public Object[] toArray() { return splits.toArray(); }
	@Override
	public <T> T[] toArray(T[] arg0) { return splits.toArray(arg0); }

	
	/*
	 * List interface
	 */
	@Override
	public Split get(int index) { return splits.get(index); }
	@Override
	public int indexOf(Object split) { return splits.indexOf(split); }
	@Override
	public int lastIndexOf(Object split) { return splits.lastIndexOf(split); }
	@Override
	public ListIterator<Split> listIterator() { return splits.listIterator(); }
	@Override
	public ListIterator<Split> listIterator(int index) { return splits.listIterator(index); }
	@Override
	public Split remove(int index) { return splits.remove(index); }
	@Override
	public Split set(int index, Split split) { return splits.set(index, split); }
	@Override
	public List<Split> subList(int fromIndex, int toIndex) { return splits.subList(fromIndex, toIndex); }
	

}
