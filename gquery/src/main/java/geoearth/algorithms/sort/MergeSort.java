package geoearth.algorithms.sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic MergeSort Algorithm
 * 
 * @param <T>
 *            Type of object being sorted
 */
public class MergeSort<T extends Comparable<T>> implements ISort<T>
{
    /**
     * Sort the provided list
     * 
     * @param sortList
     *            List to be sorted
     * @return sorted list
     */
    public List<T> sort(List<T> sortList)
    {
	if (sortList.size() <= 1)
	{
	    return sortList;
	}
	List<T> left = new ArrayList<T>();
	List<T> right = new ArrayList<T>();
	int mid = sortList.size() / 2;
	// Get the left side
	for (int l = 0; l < mid; l++)
	{
	    left.add(sortList.get(l));
	}
	// Get the right side
	for (int r = mid; r < sortList.size(); r++)
	{
	    right.add(sortList.get(r));
	}
	left = sort(left);
	right = sort(right);

	List<T> results;
	if (left.get(left.size() - 1).compareTo(right.get(0)) > 0)
	{
	    results = merge(left, right);
	}
	else
	{
	    results = new ArrayList<T>();
	    results.addAll(left);
	    results.addAll(right);
	}
	return results;
    }

    private List<T> merge(List<T> left, List<T> right)
    {
	List<T> results = new ArrayList<T>();
	while (left.size() > 0 && right.size() > 0)
	{
	    if (left.get(0).compareTo(right.get(0)) <= 0)
	    {
		results.add(left.get(0));
		left.remove(0);
	    }
	    else
	    {
		results.add(right.get(0));
		right.remove(0);
	    }
	}
	if (left.size() > 0)
	{
	    results.addAll(left);
	}
	else if (right.size() > 0)
	{
	    results.addAll(right);
	}
	return results;
    }
}