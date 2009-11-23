//  SortedListDecoratorIndex.java  
//  Java Spatial Index Library
//  Copyright (C) 2002 Infomatiq Limited
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
package geoearth.rtree.index;

import geoearth.geometry.utils.EnvelopeUtils;
import geoearth.rtree.IntProcedure;
import geoearth.rtree.SpatialIndex;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TIntProcedure;

import java.util.Properties;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * SimpleIndex
 * <p>
 * A very simple (and slow!) spatial index implementation, intended only for
 * generating test results.
 * </p>
 * <p>
 * All of the search methods, i.e. nearest(), contains(), and intersects(), run
 * in linear time, so performance will be very slow with more than 1000 or so
 * entries.
 * </p>
 * <p>
 * On the other hand, the add() and delete() methods are very fast.
 * </p>
 * 
 * @author aled@sourceforge.net
 * @version 1.0b2p1
 * 
 */
public class SimpleIndex implements SpatialIndex
{
    private static final String version = "1.0b2p1";
    TIntObjectHashMap mapping = new TIntObjectHashMap();

    @Override
    public void add(Envelope env, int id)
    {
	mapping.put(id, new Envelope(env));
    }

    @Override
    public void contains(Envelope env, IntProcedure v)
    {
	TIntObjectIterator i = mapping.iterator();
	while (i.hasNext())
	{
	    i.advance();
	    int currID = i.key();
	    Envelope currEnv = (Envelope) i.value();
	    if (env.contains(currEnv))
	    {
		v.execute(currID);
	    }
	}
    }

    @Override
    public boolean delete(Envelope env, int id)
    {
	Envelope val = (Envelope) mapping.get(id);
	if (env.equals(val))
	{
	    mapping.remove(id);
	    return true;
	}
	return false;
    }

    @Override
    public Envelope getBounds()
    {
	Envelope bounds = new Envelope();
	TIntObjectIterator i = mapping.iterator();
	while (i.hasNext())
	{
	    i.advance();
	    Envelope currEnv = (Envelope) i.value();
	    bounds.expandToInclude(currEnv);
	}
	return bounds;
    }

    @Override
    public String getVersion()
    {
	// TODO Auto-generated method stub
	return "SimpleIndex-" + version;
    }

    /**
     * Does nothing, There are no implementation dependent properties for the
     * SimpleIndex spatial index.
     */
    @Override
    public void init(Properties props)
    {
	return;
    }

    @Override
    public void intersects(Envelope env, IntProcedure v)
    {
	TIntObjectIterator i = mapping.iterator();
	while (i.hasNext())
	{
	    i.advance();
	    int currID = i.key();
	    Envelope currEnv = (Envelope) i.value();
	    if (env.intersects(currEnv))
	    {
		v.execute(currID);
	    }
	}
    }

    /**
     * Nearest
     */
    private TIntArrayList nearest(Point p, double farthestDistance)
    {
	TIntArrayList rtn = new TIntArrayList();
	double nearestDistance = farthestDistance;
	TIntObjectIterator i = mapping.iterator();
	while (i.hasNext())
	{
	    i.advance();
	    int currId = i.key();
	    Envelope currentEnv = (Envelope) i.value();
	    double distance = EnvelopeUtils.distance(currentEnv, p);
	    if (distance < nearestDistance)
	    {
		nearestDistance = distance;
		rtn.clear();
	    }
	    if (distance <= nearestDistance)
	    {
		rtn.add(currId);
	    }
	}
	return rtn;
    }

    @Override
    public void nearest(Point p, final IntProcedure v, double distance)
    {
	TIntArrayList nearestList = nearest(p, distance);
	nearestList.forEach(new TIntProcedure()
	{

	    @Override
	    public boolean execute(int id)
	    {
		v.execute(id);
		return true;
	    }
	});
    }

    @Override
    public int size()
    {
	return mapping.size();
    }

}
