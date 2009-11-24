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
package geoearth.rtree.decorator;

import geoearth.rtree.SpatialIndex;
import gnu.trove.TIntArrayList;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * SortedListDecorator
 * 
 * @author aled@sourceforge.net
 * @version 1.0b2p1
 */
public class SortedListDecorator extends ListDecorator
{
    /**
     * Create a new SortedListDecorator with the provided Spatial Index
     * 
     * @param index
     *            Spatial Index to use during construction
     */
    public SortedListDecorator(SpatialIndex index)
    {
	super(index);
    }

    public TIntArrayList nearest(Point p, double farthestDistance)
    {
	TIntArrayList list = super.nearest(p, farthestDistance);
	list.sort();
	return list;
    }

    public TIntArrayList intersects(Envelope env)
    {
	TIntArrayList list = super.intersects(env);
	list.sort();
	return list;
    }

    public TIntArrayList contains(Envelope env)
    {
	TIntArrayList list = super.contains(env);
	list.sort();
	return list;
    }
}
