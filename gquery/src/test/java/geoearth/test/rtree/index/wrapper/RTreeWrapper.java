//   RTreeWrapper.java
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
package geoearth.test.rtree.wrapper;

import geoearth.rtree.IntProcedure;
import geoearth.rtree.SpatialIndex;
import geoearth.rtree.structure.RTree;

import java.util.Properties;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * A completely useless wrapper class for the RTree Class.
 * 
 * Actually the point to introduce the same overhead as the SILWrapper class, so
 * that performance comparisons can be made.
 * 
 * @author aled@sourceforge.net
 * 
 * @version $Revision$
 * 
 */
public class RTreeWrapper implements SpatialIndex
{

    private static final String version = "1.0b2p1";

    private RTree tree;

    class IntProcedure2 implements IntProcedure
    {
	private IntProcedure m_intProcedure = null;

	public IntProcedure2(IntProcedure ip)
	{
	    m_intProcedure = ip;
	}

	public boolean execute(int i)
	{
	    return m_intProcedure.execute(i);
	}
    }

    /**
     * @see geoearth.rtree.SpatialIndex#add(com.vividsolutions.jts.geom.Envelope,
     *      int)
     */
    @Override
    public void add(Envelope env, int id)
    {
	Envelope env2 = new Envelope(env);
	tree.add(env2, id);
    }

    /**
     * @see geoearth.rtree.SpatialIndex#contains(com.vividsolutions.jts.geom.Envelope
     *      , geoearth.rtree.IntProcedure)
     */
    @Override
    public void contains(Envelope env, IntProcedure v)
    {
	// Do nothing
    }

    /**
     * @see geoearth.rtree.SpatialIndex#delete(com.vividsolutions.jts.geom.Envelope,
     *      int)
     */
    @Override
    public boolean delete(Envelope env, int id)
    {
	Envelope env2 = new Envelope(env);
	return tree.delete(env2, id);
    }

    /**
     * @see geoearth.rtree.SpatialIndex#getBounds()
     */
    @Override
    public Envelope getBounds()
    {
	return tree.getBounds();
    }

    /**
     * @see geoearth.rtree.SpatialIndex#getVersion()
     */
    @Override
    public String getVersion()
    {
	return "RTreeWrapper-" + version;
    }

    /**
     * @see geoearth.rtree.SpatialIndex#init(java.util.Properties)
     */
    @Override
    public void init(Properties props)
    {
	// Create a memory-based storage manager
	tree = new RTree();
	tree.init(props);
    }

    /**
     * @see geoearth.rtree.SpatialIndex#intersects(com.vividsolutions.jts.geom.Envelope
     *      , geoearth.rtree.IntProcedure)
     */
    @Override
    public void intersects(Envelope env, IntProcedure ip)
    {
	Envelope env2 = new Envelope(env);
	tree.intersects(env2, new IntProcedure2(ip));
    }

    /**
     * @see geoearth.rtree.SpatialIndex#nearest(com.vividsolutions.jts.geom.Point,
     *      geoearth.rtree.IntProcedure, double)
     */
    @Override
    public void nearest(Point p, IntProcedure v, double distance)
    {
	tree.nearest(new Point(p.getCoordinateSequence(), p.getFactory()), new IntProcedure2(v),
		     distance);
    }

    /**
     * @see geoearth.rtree.SpatialIndex#size()
     */
    @Override
    public int size()
    {
	return tree.size();
    }

}
