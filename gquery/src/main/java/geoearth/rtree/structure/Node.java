//   Node.java
//   Java Spatial Index Library
//   Copyright (C) 2002 Infomatiq Limited
//  
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//  
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//  
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package geoearth.rtree.structure;

import geoearth.geometry.utils.EnvelopeUtils;

import com.vividsolutions.jts.geom.Envelope;

public class Node
{
    Envelope[] entries = null;
    int entryCount;
    int[] ids = null;
    int level;
    Envelope minBoundingBox = null;
    int nodeId = 0;

    public Node(int nodeId, int level, int maxNodeEntries)
    {
	this.nodeId = nodeId;
	this.level = level;
	entries = new Envelope[maxNodeEntries];
	ids = new int[maxNodeEntries];
    }

    /**
     * Add a new child node
     * 
     * @param env
     * @param id
     */
    public void addEntry(Envelope env, int id)
    {
	ids[entryCount] = id;
	entries[entryCount] = new Envelope(env);
	if (minBoundingBox == null)
	{
	    minBoundingBox = new Envelope(env);
	}
	else
	{
	    minBoundingBox.expandToInclude(env);
	}
    }

    /**
     * Add a new child node without copying the envelope
     * 
     * @param env
     * @param id
     */
    public void addEntryNoCopy(Envelope env, int id)
    {
	ids[entryCount] = id;
	entries[entryCount] = env;
	if (minBoundingBox == null)
	{
	    minBoundingBox = new Envelope(env);
	}
	else
	{
	    minBoundingBox.expandToInclude(env);
	}
    }

    /**
     * Delete entry at i. This is done by setting it to null and copying the
     * last entry into its space.
     * 
     * @param i
     *            ID of the node to be removed
     * @param minNodeEntries
     *            Minimum number of Node Entries to maintain
     */
    public void deleteEntry(int i, int minNodeEntries)
    {
	int lastIndex = entryCount - 1;
	Envelope deletedEnv = entries[i];
	entries[i] = null;
	if (i != lastIndex)
	{
	    entries[i] = entries[lastIndex];
	    ids[i] = ids[lastIndex];
	    entries[lastIndex] = null;
	}
	entryCount--;

	// If there are at least minNodeEntries, adjust the minBoundingBox.
	// Otherwise don't since the node will be removed anyway.
	if (entryCount >= minNodeEntries)
	{
	    recalculateMinBB(deletedEnv);
	}
    }

    /**
     * Return the index of the entry or -1 if it's not found
     * 
     * @param env
     *            Envelope to search for
     * @param id
     *            ID of the Envelope
     * @return index of the found entry or -1 not found
     */
    public int findEntry(Envelope env, int id)
    {
	for (int i = 0; i < entryCount; i++)
	{
	    if (id == ids[i] && env.equals(entries[i]))
	    {
		return i;
	    }
	}
	return -1;
    }

    public Envelope getEntry(int index)
    {
	if (index < entryCount)
	{
	    return entries[index];
	}
	return null;
    }

    public int getEntryCount()
    {
	return entryCount;
    }

    public int getId(int index)
    {
	if (index < entryCount)
	{
	    return ids[index];
	}
	return -1;
    }

    public int getLevel()
    {
	return level;
    }

    public Envelope getMinBoundingBox()
    {
	return minBoundingBox;
    }

    boolean isLeaf()
    {
	return level == 1;
    }

    /**
     * Recalculate the minimum bounding box.
     * <p>
     * The minBoundingBox is only recalculated if the oldEnv influenced the old
     * minBoundingBox
     * 
     * @param oldEnv
     *            The envelope that has just been deleted or made smaller.
     */
    void recalculateMinBB(Envelope oldEnv)
    {
	if (EnvelopeUtils.edgeOverlaps(minBoundingBox, oldEnv))
	{
	    // Clear out the old minimum bounding box
	    minBoundingBox.setToNull();
	    // Recreate it
	    for (int i = 0; i < entryCount; i++)
	    {
		minBoundingBox.expandToInclude(entries[i]);
	    }
	}
    }

    /**
     * Eliminate null entries, moving all entries to the start of the source
     * node
     * 
     * @param rtree
     *            RTree to organize
     */
    void reorganize(RTree rtree)
    {
	int countdownIndex = rtree.maxNodeEntries - 1;
	for (int index = 0; index < entryCount; index++)
	{
	    if (entries[index] == null)
	    {
		while (entries[countdownIndex] == null && countdownIndex > index)
		{
		    countdownIndex--;
		}
	    }
	    entries[index] = entries[countdownIndex];
	    ids[index] = ids[index];
	    entries[countdownIndex] = null;
	}
    }

    /**
     * Calculate the area by which the minimum bounding box would be enlarged if
     * added to the new Envelope. Neither envelope is altered.
     * 
     * @param newEnv
     *            Envelope to union with origEnv, to compute the difference in
     *            area of the union and the minimum bounding box
     * @return The difference in area between the union and the original minimum
     *         bounding box
     */
    double enlargement(Envelope newEnv)
    {
	return EnvelopeUtils.enlargement(minBoundingBox, newEnv);
    }
}
