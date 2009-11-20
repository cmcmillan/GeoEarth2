//   ReferenceCompareTest.java
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

/**
 * ReferenceCompareTest
 *  
 * Generates results used for comparing the performance of the Java Spatial 
 * Index library against alternative implementations.
 * 
 * @author  aled@sourceforge.net
 * @version 1.0b2p1
 */
public class ReferenceCompareTest extends SpatialIndexTest {

  private static final Logger log = Logger.getLogger(ReferenceCompareTest.class.getName());
  
  public ReferenceCompareTest(String s) {
    super(s);
  }
  
  public void testReferenceCompareAllFunctions() {
    log.debug("testReferenceCompareDelete()");
       
    Properties p = new Properties();
    p.setProperty("MinNodeEntries", "3");
    p.setProperty("MaxNodeEntries", "6"); // keep small to maximise the number of node splits...
    p.setProperty("TreeVariant", "Linear");

    log.info("Performing full reference comparison (all functions) for 100 entries, SIL library");
    runScript("test.SILWrapper", p, "allfunctions-100", REFERENCE_COMPARISON_TEST);  

    log.info("Performing full reference comparison (all functions) for 1000 entries, SIL library");
    runScript("test.SILWrapper", p, "allfunctions-1000", REFERENCE_COMPARISON_TEST);
    
    log.info("Performing full reference comparison (all functions) for 10,000 entries, SIL library");
    runScript("test.SILWrapper", p, "allfunctions-10000", REFERENCE_COMPARISON_TEST); 

    log.info("Performing full reference comparison (all functions) for 100 entries, JSI library");
    runScript("rtree.RTree", p, "allfunctions-100", REFERENCE_COMPARISON_TEST);  

    log.info("Performing full reference comparison (all functions) for 1000 entries, JSI library");
    runScript("rtree.RTree", p, "allfunctions-1000", REFERENCE_COMPARISON_TEST);
    
    log.info("Performing full reference comparison (all functions) for 10,000 entries, JSI library");
    runScript("rtree.RTree", p, "allfunctions-10000", REFERENCE_COMPARISON_TEST);  
    
    log.info("Performing full reference comparison (all functions) for 100,000 entries, JSI library");
    runScript("rtree.RTree", p, "allfunctions-100000", REFERENCE_COMPARISON_TEST);   
  }
  
  public void testReferenceCompareDelete() {
    log.debug("testReferenceCompareDelete()");
       
    Properties p = new Properties();
    p.setProperty("MinNodeEntries", "3");
    p.setProperty("MaxNodeEntries", "6"); // keep small to maximise the number of node splits...
    p.setProperty("TreeVariant", "Linear");
    
    log.info("Performing delete reference comparison for 100 entries, SIL library");
    runScript("test.SILWrapper", p, "delete-100", REFERENCE_COMPARISON_TEST);  

    log.info("Performing delete reference comparison for 1000 entries, SIL library");
    runScript("test.SILWrapper", p, "delete-1000", REFERENCE_COMPARISON_TEST);  

 //   log.info("Performing delete reference comparison for 10,000 entries, SIL library");
 //   runScript("test.SILWrapper", p, "delete-10000", REFERENCE_COMPARISON_TEST);
 
    log.info("Performing delete reference comparison for 100 entries, JSI library");
    runScript("rtree.RTree", p, "delete-100", REFERENCE_COMPARISON_TEST);
    
    log.info("Performing delete reference comparison for 1000 entries, JSI library");
    runScript("rtree.RTree", p, "delete-1000", REFERENCE_COMPARISON_TEST);
    
    log.info("Performing delete reference comparison for 10,000 entries, JSI library");
    runScript("rtree.RTree", p, "delete-10000", REFERENCE_COMPARISON_TEST);
    
//    log.info("Performing delete reference comparison for 100,000 entries, JSI library");
//    runScript("rtree.RTree", p, "delete-100000", REFERENCE_COMPARISON_TEST);
  }
//
//  public void testReferenceCompareIntersect() {
//    log.debug("testReferenceCompareIntersect()");
//       
//    Properties p = new Properties();
//    p.setProperty("MinNodeEntries", "5");
//    p.setProperty("MaxNodeEntries", "10");
//    p.setProperty("TreeVariant", "Linear");
//    
//    log.info("Performing intersect reference comparison for 100 entries, SIL library");    
//    runScript("test.SILWrapper", p, "intersect-100", REFERENCE_COMPARISON_TEST);  
//    
//    log.info("Performing intersect reference comparison for 1000 entries, SIL library");
//    runScript("test.SILWrapper", p, "intersect-1000", REFERENCE_COMPARISON_TEST);  
//    
//    log.info("Performing intersect reference comparison for 10,000 entries, SIL library");
//    runScript("test.SILWrapper", p, "intersect-10000", REFERENCE_COMPARISON_TEST);
// 
//    log.info("Performing intersect reference comparison for 100 entries, JSI library");
//    runScript("rtree.RTree", p, "intersect-100", REFERENCE_COMPARISON_TEST);  
//    
//    log.info("Performing intersect reference comparison for 1000 entries, JSI library");
//    runScript("rtree.RTree", p, "intersect-1000", REFERENCE_COMPARISON_TEST);  
//    
//    log.info("Performing intersect reference comparison for 10,000 entries, JSI library");
//    runScript("rtree.RTree", p, "intersect-10000", REFERENCE_COMPARISON_TEST);
//    
//    log.info("Performing intersect reference comparison for 100,000 entries, JSI library");
//    runScript("rtree.RTree", p, "intersect-100,000", REFERENCE_COMPARISON_TEST);
//  }
//  
//  public void testReferenceCompareNearest() {
//    log.debug("testReferenceCompareNearest()");
//       
//    log.info("Performing nearest reference comparison");
//    
//    Properties p = new Properties();
//    p.setProperty("MinNodeEntries", "5");
//    p.setProperty("MaxNodeEntries", "10");
//    p.setProperty("TreeVariant", "Linear");
//      
//    log.info("Performing nearest reference comparison for 100 entries, SIL library");
//    runScript("test.SILWrapper", p, "nearest-100", REFERENCE_COMPARISON_TEST);  
//       
//    log.info("Performing nearest reference comparison for 1000 entries, SIL library");
//    runScript("test.SILWrapper", p, "nearest-1000", REFERENCE_COMPARISON_TEST);  
//     
//    log.info("Performing nearest reference comparison for 10000 entries, SIL library");
//    runScript("test.SILWrapper", p, "nearest-10000", REFERENCE_COMPARISON_TEST); 
//        
//    log.info("Performing nearest reference comparison for 100 entries, JSI library");
//    runScript("rtree.RTree", p, "nearest-100", REFERENCE_COMPARISON_TEST);  
//       
//    log.info("Performing nearest reference comparison for 1000 entries, JSI library");
//    runScript("rtree.RTree", p, "nearest-1000", REFERENCE_COMPARISON_TEST);  
//       
//    log.info("Performing nearest reference comparison for 10,000 entries, JSI library");
//    runScript("rtree.RTree", p, "nearest-10000", REFERENCE_COMPARISON_TEST);
//       
//    log.info("Performing nearest reference comparison for 100,000 entries, JSI library");
//    runScript("rtree.RTree", p, "nearest-100000", REFERENCE_COMPARISON_TEST);
//  }
//
//  public void testReferenceCompareContains() {
//    log.debug("testReferenceCompareContains()");
//           
//    Properties p = new Properties();
//    p.setProperty("MinNodeEntries", "5");
//    p.setProperty("MaxNodeEntries", "10");
//    p.setProperty("TreeVariant", "Linear");
//    
//    log.info("Performing contains reference comparison for 100 entries, SIL library");
//    runScript("test.SILWrapper", p, "contains-100", REFERENCE_COMPARISON_TEST);  
//    
//    log.info("Performing contains reference comparison for 1000 entries, SIL library");
//    runScript("test.SILWrapper", p, "contains-1000", REFERENCE_COMPARISON_TEST);  
//    
//    log.info("Performing contains reference comparison for 10,000 entries, SIL library");
//    runScript("test.SILWrapper", p, "contains-10000", REFERENCE_COMPARISON_TEST);
//      
//    log.info("Performing contains reference comparison for 100 entries, JSI library");
//    runScript("rtree.RTree", p, "contains-100", REFERENCE_COMPARISON_TEST);  
//    
//    log.info("Performing contains reference comparison for 1000 entries, JSI library");
//    runScript("rtree.RTree", p, "contains-1000", REFERENCE_COMPARISON_TEST);  
//    
//    log.info("Performing contains reference comparison for 10,000 entries, JSI library");
//    runScript("rtree.RTree", p, "contains-10000", REFERENCE_COMPARISON_TEST);
//    
//    log.info("Performing contains reference comparison for 100,000 entries, JSI library");
//    runScript("rtree.RTree", p, "contains-100000", REFERENCE_COMPARISON_TEST);
//  }
}
