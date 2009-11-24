//   SpatialIndex.java
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

package geoearth.rtree;

import java.util.Properties;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * Defines methods that must be implemented by all spatial indexes. This
 * includes the RTree and its variants.
 * 
 * @author aled@sourceforge.net
 * @version 1.0b2p1
 */
public interface SpatialIndex
{

    /**
     * Adds a new envelope to the spatial index
     * 
     * @param env
     *            The envelope to add to the spatial index.
     * @param id
     *            The ID of the envelope to add to the spatial index. The result
     *            of adding more than one envelope with the same ID is
     *            undefined.
     */
    public void add(Envelope env, int id);

    /**
     * Finds all rectangles contained by the passed rectangle.
     * 
     * @param env
     *            The envelope for which this method finds contained shapes.
     * 
     * @param v
     *            The visitor whose visit() method is is called for each
     *            contained shape.
     */
    public void contains(Envelope env, IntProcedure v);

    /**
     * Deletes a envelope from the spatial index
     * 
     * @param env
     *            The envelope to delete from the spatial index
     * @param id
     *            The ID of the envelope to delete from the spatial index
     * 
     * @return true if the envelope was deleted false if the envelope was not
     *         found, or the envelope was found but with a different ID
     */
    public boolean delete(Envelope env, int id);

    /**
     * Returns the bounds of all the entries in the spatial index, or null if
     * there are no entries.
     * 
     * @return minimum bounds of all the entries in the spatial index
     */
    public Envelope getBounds();

    /**
     * Returns a string identifying the type of spatial index, and the version
     * number, e.g. "SimpleIndex-0.1"
     * 
     * @return String identifying the type of spatial index
     */
    public String getVersion();

    /**
     * Initializes any implementation dependent properties of the spatial index.
     * For example, RTree implementations will have a NodeSize property.
     * 
     * @param props
     *            The set of properties used to initialize the spatial index.
     */
    public void init(Properties props);

    /**
     * Finds all rectangles that intersect the passed rectangle.
     * 
     * @param env
     *            The envelope for which this method finds intersecting shapes.
     * 
     * @param v
     *            The IntProcedure whose execute() method is is called for each
     *            intersecting shape.
     */
    public void intersects(Envelope env, IntProcedure v);

    /**
     * Finds all rectangles that are nearest to the passed rectangle, and calls
     * execute() on the passed IntProcedure for each one.
     * 
     * @param p
     *            The point for which this method finds the nearest neighbors.
     * 
     * @param v
     *            The IntProcedure whose execute() method is is called for each
     *            nearest neighbor.
     * 
     * @param distance
     *            The farthest distance away from the point to search. Shapes
     *            further than this will not be found. This should be as small
     *            as possible to minimize the search time. Use
     *            Float.POSITIVE_INFINITY to guarantee that the nearest shape is
     *            found, no matter how far away, although this will slow down
     *            the algorithm.
     */
    public void nearest(Point p, IntProcedure v, double distance);

    /**
     * Returns the number of entries in the spatial index
     * 
     * @return number of entries in the spatial index
     */
    public int size();

}
