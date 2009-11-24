//   SILWrapper.java
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

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sil.rtree.RTree;
import sil.spatialindex.IData;
import sil.spatialindex.INode;
import sil.spatialindex.ISpatialIndex;
import sil.spatialindex.IVisitor;
import sil.spatialindex.Region;
import sil.storagemanager.IStorageManager;
import sil.storagemanager.MemoryStorageManager;
import sil.storagemanager.PropertySet;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * Wrapper class for the Spatial Index Library (v0.43b) written by Marios
 * Hadjieleftherio (marioh@cs.ucr.edu), with minor modifications
 * 
 * @author aled@sourceforge.net
 * @version $Revision$
 * 
 */
public class SILWrapper implements SpatialIndex
{
    private static final Logger log = LoggerFactory.getLogger(SILWrapper.class.getName());

    private static final String version = "1.0b2p1";

    private IStorageManager storageManager = null;
    private ISpatialIndex tree = null;
    private int size = 0;

    class IntProcedureVisitor implements IVisitor
    {
	private IntProcedure m_intProcedure = null;

	public IntProcedureVisitor(IntProcedure ip)
	{
	    m_intProcedure = ip;
	}

	public void visitNode(final INode n)
	{
	    return;
	}

	public void visitData(final IData d)
	{
	    m_intProcedure.execute(d.getIdentifier());
	}
    }

    /**
     * @see geoearth.rtree.SpatialIndex#add(com.vividsolutions.jts.geom.Envelope,
     *      int)
     */
    @Override
    public void add(Envelope env, int id)
    {
	Region region =
		new Region(new double[] { env.getMinX(), env.getMinY() }, new double[] {
			env.getMaxX(), env.getMaxY() });
	tree.insertData(null, region, id);
	size++;
    }

    /**
     * @see geoearth.rtree.SpatialIndex#contains(com.vividsolutions.jts.geom.Envelope
     *      , geoearth.rtree.IntProcedure)
     */
    @Override
    public void contains(Envelope env, IntProcedure v)
    {
	Region region =
		new Region(new double[] { env.getMinX(), env.getMinY() }, new double[] {
			env.getMaxX(), env.getMaxY() });
	tree.containmentQuery(region, new IntProcedureVisitor(v));
    }

    /**
     * @see geoearth.rtree.SpatialIndex#delete(com.vividsolutions.jts.geom.Envelope,
     *      int)
     */
    @Override
    public boolean delete(Envelope env, int id)
    {
	Region region =
		new Region(new double[] { env.getMinX(), env.getMinY() }, new double[] {
			env.getMaxX(), env.getMaxY() });
	if (tree.deleteData(region, id))
	{
	    size--;
	    return true;
	}
	return false;
    }

    /**
     * @see geoearth.rtree.SpatialIndex#getBounds()
     */
    @Override
    public Envelope getBounds()
    {
	// Operation not supported in Spatial Index Library
	return null;
    }

    /**
     * @see geoearth.rtree.SpatialIndex#getVersion()
     */
    @Override
    public String getVersion()
    {
	return "SILWrapper-" + version;
    }

    /**
     * @see geoearth.rtree.SpatialIndex#init(java.util.Properties)
     */
    @Override
    public void init(Properties props)
    {
	int minNodeEntries = Integer.parseInt(props.getProperty("MinNodeEntries", "0"));
	int maxNodeEntries = Integer.parseInt(props.getProperty("MaxNodeEntries", "0"));

	double fillFactor = (double) minNodeEntries / (double) maxNodeEntries;

	// Create a memory=based storage manage
	storageManager = new MemoryStorageManager();
	PropertySet propSet = new PropertySet();
	propSet.setProperty("FillFactor", new Double(fillFactor));
	propSet.setProperty("IndexCapacity", new Integer(maxNodeEntries));
	propSet.setProperty("LeafCapacity", new Integer(maxNodeEntries));
	propSet.setProperty("Dimension", new Integer(2));

	String treeVariant = props.getProperty("TreeVariant");
	Integer intTreeVariant = null;
	if (treeVariant.equalsIgnoreCase("Linear"))
	{
	    intTreeVariant = new Integer(sil.spatialindex.SpatialIndex.RtreeVariantLinear);
	}
	else if (treeVariant.equalsIgnoreCase("Quadratic"))
	{
	    intTreeVariant = new Integer(sil.spatialindex.SpatialIndex.RtreeVariantQuadratic);
	}
	else
	{
	    // Default
	    if (!treeVariant.equalsIgnoreCase("RStar"))
	    {
		log.error("Property key TreeVariant: invalid value {} , defaulting to Rstar",
			  treeVariant);
	    }
	    intTreeVariant = new Integer(sil.spatialindex.SpatialIndex.RtreeVariantRstar);
	}
	propSet.setProperty("TreeVariant", intTreeVariant);

	tree = new RTree(propSet, storageManager);
    }

    /**
     * @see geoearth.rtree.SpatialIndex#intersects(com.vividsolutions.jts.geom.Envelope
     *      , geoearth.rtree.IntProcedure)
     */
    @Override
    public void intersects(Envelope env, IntProcedure v)
    {
	Region region =
		new Region(new double[] { env.getMinX(), env.getMinY() }, new double[] {
			env.getMaxX(), env.getMaxY() });
	tree.intersectionQuery(region, new IntProcedureVisitor(v));
    }

    /**
     * @see geoearth.rtree.SpatialIndex#nearest(com.vividsolutions.jts.geom.Point,
     *      geoearth.rtree.IntProcedure, double)
     */
    @Override
    public void nearest(Point p, IntProcedure v, double distance)
    {
	tree.nearestNeighborQuery(1,
				  new sil.spatialindex.Point(new double[] { p.getX(), p.getY() }),
				  new IntProcedureVisitor(v));
    }

    /**
     * @see geoearth.rtree.SpatialIndex#size()
     */
    @Override
    public int size()
    {
	return size;
    }

}
