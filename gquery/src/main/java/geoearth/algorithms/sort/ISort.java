/**
 * 
 */
package geoearth.algorithms.sort;

import java.util.List;

/**
 * @author cjmcmill
 * @param <T>
 *            Type of List to be sorted
 */
public interface ISort<T extends Comparable<T>>
{
    /**
     * Sort the provided list
     * 
     * @param sortList
     *            List to be sorted
     * @return sorted list
     */
    public List<T> sort(List<T> sortList);
}
