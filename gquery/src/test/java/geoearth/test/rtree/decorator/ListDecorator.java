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
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

package geoearth.rtree.decorator;

import geoearth.rtree.IntProcedure;
import geoearth.rtree.SpatialIndex;
import gnu.trove.TIntArrayList;

import org.junit.Before;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * ListDecorator
 * 
 * @author aled@sourceforge.net
 * @version 1.0b2p1
 */
public class ListDecorator
{

    SpatialIndex spatialIndex = null;

    /**
     * ListDecorator Constructor
     * 
     * @param index
     *            SpatialIndex used in searching
     */
    public ListDecorator(SpatialIndex index)
    {
	spatialIndex = index;
    }

    class AddToListProcedure implements IntProcedure
    {
	private TIntArrayList list = new TIntArrayList();

	public boolean execute(int id)
	{
	    list.add(id);
	    return true;
	}

	public TIntArrayList getList()
	{
	    return list;
	}
    }

    /**
     * Finds all shapes that are nearest to the passed point
     * 
     * @param p
     *            The point which this method uses to find the nearest
     *            neighbors.
     * @param farthestDistance
     *            The farthest distance away from the point to search. Shapes
     *            further than this will not be found. This should be as small
     *            as possible to minimize the search time. Use
     *            Float.POSITIVE_INFINITY to guarantee that the nearest
     *            rectangle is found, no matter how far away, although this will
     *            slow down the algorithm.
     * @return List of IDs of shapes that are nearest to the passed point,
     *         ordered by distance (nearest --> farthest)
     */
    public TIntArrayList nearest(Point p, double farthestDistance)
    {
	AddToListProcedure v = new AddToListProcedure();
	spatialIndex.nearest(p, v, farthestDistance);
	return v.getList();
    }

    /**
     * Finds all shapes that intersect the passed envelope.
     * 
     * @param env
     *            The envelope for which this method finds intersecting
     *            envelopes
     * @return List of IDs of shapes that intersect the provided envelope
     *         ordered by distance (nearest --> farthest)
     */
    public TIntArrayList intersects(Envelope env)
    {
	AddToListProcedure v = new AddToListProcedure();
	spatialIndex.intersects(env, v);
	return v.getList();
    }

    /**
     * Finds all shapes contained by the passed envelope
     * 
     * @param env
     *            The envelope for which this method finds contained shapes.
     * @return List of IDs of shapes that are contained by the provided envelope
     *         ordered by distance (nearest --> farthest)
     */
    public TIntArrayList contains(Envelope env)
    {
	AddToListProcedure v = new AddToListProcedure();
	spatialIndex.contains(env, v);
	return v.getList();
    }

    @Before
    public void setUp() throws Exception
    {
    }

}
