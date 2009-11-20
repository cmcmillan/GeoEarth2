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

package com.infomatiq.jsi.test;

import java.util.Properties;

import org.apache.log4j.Logger;
import sil.rtree.RTree;
import sil.spatialindex.IData;
import sil.spatialindex.INode;
import sil.spatialindex.ISpatialIndex;
import sil.spatialindex.IVisitor;
import sil.spatialindex.Region;
import sil.storagemanager.IStorageManager;
import sil.storagemanager.MemoryStorageManager;
import sil.storagemanager.PropertySet;

import com.infomatiq.jsi.IntProcedure;
import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;

/**
 * Wrapper class for the Spatial Index Library (v0.43b) written by
 * Marios Hadjieleftheriou (marioh@cs.ucr.edu), with minor modifications.
 * 
 * Used to generate test results and performance comparisons.
 * 
 * @author aled@sourceforge.net
 * @version $Revision$
 */
public class SILWrapper implements SpatialIndex {
  
  private static final Logger log = 
    Logger.getLogger(SILWrapper.class.getName());
    
  private static final String version = "1.0b2p1";
  
  private IStorageManager storageManager = null; 
  private ISpatialIndex tree = null;
  private int size = 0;
  
  class IntProcedureVisitor implements IVisitor {
    private IntProcedure m_intProcedure = null;
    
    public IntProcedureVisitor(IntProcedure ip) {
      m_intProcedure = ip;
    }
    
    public void visitNode(final INode n) {
      return;
    }
    
    public void visitData(final IData d) {
      m_intProcedure.execute(d.getIdentifier());
    }
  }
  
  /**
   * @see com.infomatiq.jsi.SpatialIndex#init(Properties)
   */
  public void init(Properties props) {
    int minNodeEntries = Integer.parseInt(props.getProperty("MinNodeEntries", "0"));
    int maxNodeEntries = Integer.parseInt(props.getProperty("MaxNodeEntries", "0"));
    
    float fillFactor = (float) minNodeEntries / (float) maxNodeEntries;
    
    // create a memory-based storage manager
    storageManager = new MemoryStorageManager();
    PropertySet propertySet = new PropertySet();
    propertySet.setProperty("FillFactor", new Double(fillFactor));
    propertySet.setProperty("IndexCapacity", new Integer(maxNodeEntries));
    propertySet.setProperty("LeafCapacity", new Integer(maxNodeEntries));
    propertySet.setProperty("Dimension", new Integer(2));
    
    String strTreeVariant = props.getProperty("TreeVariant");
    Integer intTreeVariant = null;
    if (strTreeVariant.equalsIgnoreCase("Linear")) {
      intTreeVariant = new Integer(sil.spatialindex.SpatialIndex.RtreeVariantLinear);
    } else if (strTreeVariant.equalsIgnoreCase("Quadratic")) {
      intTreeVariant = new Integer(sil.spatialindex.SpatialIndex.RtreeVariantQuadratic); 
    } else {
      // default
      if (!strTreeVariant.equalsIgnoreCase("Rstar")) {
        log.error("Property key TreeVariant: invalid value " + strTreeVariant + ", defaulting to Rstar");
      }
      intTreeVariant = new Integer(sil.spatialindex.SpatialIndex.RtreeVariantRstar); 
    }
    propertySet.setProperty("TreeVariant", intTreeVariant);
    
    tree = new RTree(propertySet, storageManager);
  }

  /**
   * @see com.infomatiq.jsi.SpatialIndex#nearest(Point p, IntProcedure ip, float)
   */
  public void nearest(Point p , IntProcedure ip, float furthestDistance) {
    tree.nearestNeighborQuery(1, 
                              new sil.spatialindex.Point(p.coordinates),  
                              new IntProcedureVisitor(ip));
  }

  /**
   * @see com.infomatiq.jsi.SpatialIndex#intersects(Rectangle, IntProcedure)
   */
  public void intersects(Rectangle r, IntProcedure ip) {
    Region region = new Region(r.min, r.max);  
    tree.intersectionQuery(region, new IntProcedureVisitor(ip));
  }

  /**
   * @see com.infomatiq.jsi.SpatialIndex#contains(Rectangle, IntProcedure)
   */
  public void contains(Rectangle r, IntProcedure ip) {
    Region region = new Region(r.min, r.max);
    tree.containmentQuery(region, new IntProcedureVisitor(ip));
  }

  /**
   * @see com.infomatiq.jsi.SpatialIndex#add(Rectangle, int)
   */
  public void add(Rectangle r, int id) {
    Region region = new Region(r.min, r.max);
    tree.insertData(null, region, id);
    size++;
  }

  /**
   * @see com.infomatiq.jsi.SpatialIndex#delete(Rectangle, int)
   */
  public boolean delete(Rectangle r, int id) {
    Region region = new Region(r.min, r.max);
    if (tree.deleteData(region, id)) {
      size--;
      return true;
    }
    return false;
  }
  
  /**
   * @see com.infomatiq.jsi.SpatialIndex#size()
   */
  public int size() {
    return size;
  }

  /**
   * @see com.infomatiq.jsi.SpatialIndex#getBounds()
   */
  public Rectangle getBounds() {
    return null; // operation not supported in Spatial Index Library
  }

  /**
   * @see com.infomatiq.jsi.SpatialIndex#getVersion()
   */
  public String getVersion() {
    return "SILWrapper-" + version;
  }

}
