//   PerformanceTest.java
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

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PerformanceTest
 * 
 * Generates results used for comparing the performance of the Java Spatial
 * Index library against alternative implementations.
 * 
 * @author aled@sourceforge.net
 * @version 1.0b2p1
 */
public class PerformanceTest extends SpatialIndexTest
{

    private static final Logger log = LoggerFactory.getLogger(PerformanceTest.class.getName());
    private static final DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

    public void testPerformance()
    {
	log.debug("testPerformance");

	Date currentDate = new Date();
	intersectPerformanceLog.info(dateFormat.format(currentDate));
	intersectPerformanceLog.info(MessageFormat.format(
							  "IndexType,TestId,MinNodeEntries,MaxNodeEntries,TreeVariant,TreeSize,{0},{1},{2}",
							  "QueryCount", "AverageIntersectCount",
							  "AverageQueryTime"));

	nearestPerformanceLog.info(dateFormat.format(currentDate));
	nearestPerformanceLog.info(MessageFormat.format(
							"IndexType,TestId,MinNodeEntries,MaxNodeEntries,TreeVariant,TreeSize,{0},{1},{2}",
							"QueryCount", "AverageNearestCount",
							"AverageQueryTime"));

	containsPerformanceLog.info(dateFormat.format(currentDate));
	containsPerformanceLog.info(MessageFormat.format(
							 "IndexType,TestId,MinNodeEntries,MaxNodeEntries,TreeVariant,TreeSize,{0},{1},{2}",
							 "QueryCount", "AverageContainsCount",
							 "AverageQueryTime"));

	addPerformanceLog.info(dateFormat.format(currentDate));
	addPerformanceLog.info(MessageFormat.format(
						    "IndexType,TestId,MinNodeEntries,MaxNodeEntries,TreeVariant,TreeSize,{0},{1}",
						    "QueryCount", "AverageAddTime"));

	deletePerformanceLog.info(dateFormat.format(currentDate));
	deletePerformanceLog.info(MessageFormat.format(
						       "IndexType,TestId,MinNodeEntries,MaxNodeEntries,TreeVariant,TreeSize,{0},{1}",
						       "QueryCount", "AverageDeleteTime"));

	Properties p = new Properties();

	// SimpleIndex and NullIndex do not use Min/MaxNodeEntries, so do them
	// first.
	runScript("test.SimpleIndex", p, "allfunctions-100", PERFORMANCE_TEST);
	runScript("test.SimpleIndex", p, "allfunctions-1000", PERFORMANCE_TEST);
	runScript("test.SimpleIndex", p, "allfunctions-10000", PERFORMANCE_TEST);
	// Only go up to 10 000 for simple index, as it takes too long

	p.setProperty("TreeVariant", "null");
	runScript("test.NullIndex", p, "allfunctions-100", PERFORMANCE_TEST);
	runScript("test.NullIndex", p, "allfunctions-1000", PERFORMANCE_TEST);
	runScript("test.NullIndex", p, "allfunctions-10000", PERFORMANCE_TEST);
	runScript("test.NullIndex", p, "allfunctions-100000", PERFORMANCE_TEST);

	// [X]TODO: reasonable values?
	p.setProperty("MinNodeEntries", "5");
	p.setProperty("MaxNodeEntries", "20");

	p.setProperty("TreeVariant", "Linear");
	runScript("test.RTreeWrapper", p, "allfunctions-100", PERFORMANCE_TEST);
	runScript("test.RTreeWrapper", p, "allfunctions-1000", PERFORMANCE_TEST);
	runScript("test.RTreeWrapper", p, "allfunctions-10000", PERFORMANCE_TEST);
	runScript("test.RTreeWrapper", p, "allfunctions-100000", PERFORMANCE_TEST);

	p.setProperty("TreeVariant", "Linear");
	runScript("rtree.RTree", p, "allfunctions-100", PERFORMANCE_TEST);
	runScript("rtree.RTree", p, "allfunctions-1000", PERFORMANCE_TEST);
	runScript("rtree.RTree", p, "allfunctions-10000", PERFORMANCE_TEST);
	runScript("rtree.RTree", p, "allfunctions-100000", PERFORMANCE_TEST);

	p.setProperty("TreeVariant", "Linear");
	runScript("test.SILWrapper", p, "allfunctions-100", PERFORMANCE_TEST);
	runScript("test.SILWrapper", p, "allfunctions-1000", PERFORMANCE_TEST);
	runScript("test.SILWrapper", p, "allfunctions-10000", PERFORMANCE_TEST);
	runScript("test.SILWrapper", p, "allfunctions-100000", PERFORMANCE_TEST);

	p.setProperty("TreeVariant", "Quadratic");
	runScript("test.SILWrapper", p, "allfunctions-100", PERFORMANCE_TEST);
	runScript("test.SILWrapper", p, "allfunctions-1000", PERFORMANCE_TEST);
	runScript("test.SILWrapper", p, "allfunctions-10000", PERFORMANCE_TEST);
	runScript("test.SILWrapper", p, "allfunctions-100000", PERFORMANCE_TEST);

	p.setProperty("TreeVariant", "Rstar");
	runScript("test.SILWrapper", p, "allfunctions-100", PERFORMANCE_TEST);
	runScript("test.SILWrapper", p, "allfunctions-1000", PERFORMANCE_TEST);
	runScript("test.SILWrapper", p, "allfunctions-10000", PERFORMANCE_TEST);
	runScript("test.SILWrapper", p, "allfunctions-100000", PERFORMANCE_TEST);
    }
}
