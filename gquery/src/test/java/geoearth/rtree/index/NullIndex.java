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

import geoearth.rtree.IntProcedure;
import geoearth.rtree.SpatialIndex;

import java.util.Properties;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * An implementation of SpatialIndex that does absolutely nothing. The purpose
 * of this class is to measure the overhead of the testing framework,
 * 
 * @author aled@sourceforge.net
 * @version 1.0b2p1
 * 
 */
public class NullIndex implements SpatialIndex
{
    private static final String version = "1.0b2p1";

    @Override
    public void add(Envelope r, int id)
    {
    }

    @Override
    public void contains(Envelope env, IntProcedure v)
    {
    }

    @Override
    public boolean delete(Envelope env, int id)
    {
	return false;
    }

    @Override
    public Envelope getBounds()
    {
	return null;
    }

    @Override
    public String getVersion()
    {
	return "NullIndex-" + version;
    }

    @Override
    public void init(Properties props)
    {
    }

    @Override
    public void intersects(Envelope env, IntProcedure ip)
    {
    }

    @Override
    public void nearest(Point p, IntProcedure v, double distance)
    {
    }

    @Override
    public int size()
    {
	return 0;
    }
}
