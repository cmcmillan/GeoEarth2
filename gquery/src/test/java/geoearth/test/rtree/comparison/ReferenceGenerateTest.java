//   ReferenceGenerateTest
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
package geoearth.test.rtree.comparison;

import java.util.Properties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReferenceTest
 * 
 * Generates reference results used for testing the Java Spatial Index.
 * Reference results are generated using alternative spatial index
 * implementations, specifically SimpleIndex and Spatial Index Library.
 * 
 * @author aled@sourceforge.net
 * @version 1.0b2p1
 */
public class ReferenceGenerateTest extends SpatialIndexTest
{
    private static final Logger log =
	    LoggerFactory.getLogger(ReferenceGenerateTest.class.getName());

    @Test
    public void testReferenceGenerateAllFunctions()
    {
	log.debug("testReferenceGenerateAllFunctions()");

	Properties p = new Properties();

	log.info("Creating reference test results (all functions) for 100 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "allfunctions-100", REFERENCE_GENERATE);

	log.info("Creating reference test results (all functions) for 1000 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "allfunctions-1000", REFERENCE_GENERATE);

	log.info("Creating reference test results (all functions) for 10,000 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "allfunctions-10000", REFERENCE_GENERATE);

	p.setProperty("MinNodeEntries", "1");
	p.setProperty("MaxNodeEntries", "13"); // different to other tests
	p.setProperty("TreeVariant", "Rstar");

	log.info("Creating reference test results (all functions) for 100,000 entries.");
	runScript("test.rtree.index.wrapper.SILWrapper", p, "allfunctions-100000",
		  REFERENCE_GENERATE);
    }

    @Test
    public void testReferenceGenerateDelete()
    {
	log.debug("testReferenceGenerateDelete()");

	Properties p = new Properties();

	log.info("Creating reference testDelete results for 100 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "delete-100", REFERENCE_GENERATE);

	log.info("Creating reference testDelete results for 1000 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "delete-1000", REFERENCE_GENERATE);

	log.info("Creating reference testDelete results for 10,000 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "delete-10000", REFERENCE_GENERATE);

	// log.info("Creating reference testDelete results for 100,000 entries.");
	// runScript("test.rtree.index.wrapper.SILWrapper", p, "delete-100000",
	// REFERENCE_GENERATE);
    }

    @Test
    public void testReferenceGenerateIntersect()
    {
	log.debug("testReferenceGenerateIntersect()");

	Properties p = new Properties();

	log.info("Creating reference testIntersect results for 100 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "intersect-100", REFERENCE_GENERATE);

	log.info("Creating reference testIntersect results for 1000 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "intersect-1000", REFERENCE_GENERATE);

	log.info("Creating reference testIntersect results for 10,000 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "intersect-10000", REFERENCE_GENERATE);

	p.setProperty("MinNodeEntries", "1");
	p.setProperty("MaxNodeEntries", "13"); // different to other tests
	p.setProperty("TreeVariant", "Rstar");

	log.info("Creating reference testIntersect results for 100,000 entries.");
	runScript("test.rtree.index.wrapper.SILWrapper", p, "intersect-100000", REFERENCE_GENERATE);
    }

    @Test
    public void testReferenceGenerateNearest()
    {
	log.debug("testReferenceGenerateNearest()");

	Properties p = new Properties();

	log.info("Creating reference testIntersect results for 100 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "nearest-100", REFERENCE_GENERATE);

	log.info("Creating reference testIntersect results for 1000 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "nearest-1000", REFERENCE_GENERATE);

	log.info("Creating reference testIntersect results for 10,000 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "nearest-10000", REFERENCE_GENERATE);

	p.setProperty("MinNodeEntries", "1");
	p.setProperty("MaxNodeEntries", "13"); // different to other tests
	p.setProperty("TreeVariant", "Rstar");

	log.info("Creating reference testIntersect results for 100,000 entries.");
	runScript("test.rtree.index.wrapper.SILWrapper", p, "nearest-100000", REFERENCE_GENERATE);
    }

    @Test
    public void testReferenceGenerateContains()
    {
	log.debug("testReferenceGenerateContains()");

	Properties p = new Properties();

	log.info("Creating reference testIntersect results for 100 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "contains-100", REFERENCE_GENERATE);

	log.info("Creating reference testIntersect results for 1000 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "contains-1000", REFERENCE_GENERATE);

	log.info("Creating reference testIntersect results for 10,000 entries.");
	runScript("test.rtree.index.SimpleIndex", p, "contains-10000", REFERENCE_GENERATE);

	p.setProperty("MinNodeEntries", "6");
	p.setProperty("MaxNodeEntries", "13"); // different to other tests
	p.setProperty("TreeVariant", "Rstar");

	log.info("Creating reference testIntersect results for 100,000 entries.");
	runScript("test.rtree.index.wrapper.SILWrapper", p, "contains-100000", REFERENCE_GENERATE);
    }
}
