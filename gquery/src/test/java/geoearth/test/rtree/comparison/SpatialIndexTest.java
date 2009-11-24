//   SpatialIndexTest.java
//   Java Spatial Index Library
//   Copyright (C) 2002 Infomatiq Limited
//   Copyright (C) 2008 Aled Morris <aled@sourceforge.net>
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

import static org.junit.Assert.assertTrue;

import geoearth.rtree.SpatialIndex;
import geoearth.test.rtree.decorator.ListDecorator;
import geoearth.test.rtree.decorator.SortedListDecorator;
import geoearth.test.rtree.index.SpatialIndexFactory;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class SpatialIndexTest
{
    private static final Logger log = LoggerFactory.getLogger(SpatialIndexTest.class.getName());

    protected static final Logger addPerformanceLog = LoggerFactory.getLogger("addPerformance");

    protected static final Logger containsPerformanceLog =
	    LoggerFactory.getLogger("containsPerformance");

    protected static final Logger deletePerformanceLog =
	    LoggerFactory.getLogger("deletePerformance");

    protected static final Logger intersectPerformanceLog =
	    LoggerFactory.getLogger("intersectPerformance");

    protected static final Logger nearestPerformanceLog =
	    LoggerFactory.getLogger("nearestPerformance");

    protected static final int PERFORMANCE_TEST = 0;
    protected static final int REFERENCE_COMPARISON_TEST = 1;
    protected static final int REFERENCE_GENERATE = 2;

    protected static final GeometryFactory geomFactory = new GeometryFactory();

    private void writeOutput(String outputLine, PrintWriter outputFile,
	    LineNumberReader referenceFile)
    {
	try
	{
	    outputFile.println(outputLine);
	    outputFile.flush();
	    if (referenceFile != null)
	    {
		String refLine = referenceFile.readLine();
		if (!outputLine.equals(refLine))
		{
		    log.error("Output does not match reference on line {}",
			      referenceFile.getLineNumber());
		    log.error(" Reference result: {}", refLine);
		    log.error(" Test result:      {}", outputLine);
		    assertTrue("Output does not match reference on line "
			    + referenceFile.getLineNumber(), false);
		}
	    }
	}
	catch (IOException e)
	{
	    log.error("IOException while writing test results");
	}
    }

    private Envelope getRandomEnvelope(Random r, double scale)
    {
	double x1 = r.nextGaussian() * scale;
	double y1 = r.nextGaussian() * scale;
	double x2 = x1 + r.nextGaussian() * scale;
	double y2 = y1 + r.nextGaussian() * scale;

	return new Envelope(x1, x2, y1, y2);
    }

    private Point getRandomPoint(Random r, double scale)
    {
	double x = r.nextGaussian() * scale;
	double y = r.nextGaussian() * scale;

	return geomFactory.createPoint(new Coordinate(x, y));
    }

    private Polygon getRandomRectangle(Random r, double scale)
    {
	GeometricShapeFactory gsf = new GeometricShapeFactory(geomFactory);
	double x = r.nextGaussian() * scale;
	double y = r.nextGaussian() * scale;
	double w = r.nextGaussian() * scale;
	double h = r.nextGaussian() * scale;

	gsf.setWidth(w);
	gsf.setHeight(h);
	gsf.setNumPoints(4);
	gsf.setBase(new Coordinate(x, y));
	return gsf.createRectangle();
    }

    private Polygon getRandomCircle(Random r, double scale)
    {
	GeometricShapeFactory gsf = new GeometricShapeFactory(geomFactory);
	double x = r.nextGaussian() * scale;
	double y = r.nextGaussian() * scale;
	double w = r.nextGaussian() * scale;
	double h = r.nextGaussian() * scale;

	gsf.setWidth(w);
	gsf.setHeight(h);

	gsf.setBase(new Coordinate(x, y));
	return gsf.createCircle();
    }

    private Polygon getRandomPolygon(Random r, double scale, int maxPoints)
    {
	if (maxPoints == 0)
	{
	    // Create an empty polygon
	    return geomFactory.createPolygon(null, null);
	}
	// We need 0 or 4 points at a minimum to create a polygon
	// since the first and last point must equal
	if (maxPoints < 3)
	{
	    maxPoints = 3;
	}
	int points = 0;
	while (points < 3)
	{
	    points = r.nextInt(maxPoints);
	}
	// Add one because we will need to close the list
	Coordinate[] coords = new Coordinate[points + 1];

	double x = r.nextGaussian() * scale;
	double y = r.nextGaussian() * scale;
	Coordinate newCoord = new Coordinate(x, y);
	Coordinate prevCoord = newCoord;
	coords[0] = newCoord;
	// Generate Coordinates
	for (int i = 1; i < points; i++)
	{
	    while (prevCoord.equals(newCoord))
	    {
		x = r.nextGaussian() * scale;
		y = r.nextGaussian() * scale;
		// Consecutive points cannot match
		newCoord = new Coordinate(x, y);
	    }
	    coords[i] = newCoord;
	    prevCoord = newCoord;
	}
	// Close the Coordinate array
	coords[points] = new Coordinate(coords[0]);
	LinearRing shell = geomFactory.createLinearRing(coords);
	// Create the polygon
	return geomFactory.createPolygon(shell, null);
    }

    private LineString getRandomPolyline(Random r, double scale, int maxPoints)
    {
	if (maxPoints == 0)
	{
	    // Create an empty polygon
	    return geomFactory.createLineString(new Coordinate[] {});
	}
	// We need 0 or 2 points at a minimum to create a LineString
	// Consecutive points can be the same unless there are only two points.
	if (maxPoints < 1)
	{
	    maxPoints = 1;
	}
	int points = 0;
	while (points < 2)
	{
	    points = r.nextInt(maxPoints) + 1;
	}
	// Add one because we will need to close the list
	Coordinate[] coords = new Coordinate[points];

	double x = r.nextGaussian() * scale;
	double y = r.nextGaussian() * scale;

	// Generate Coordinates
	for (int i = 0; i < points; i++)
	{
	    coords[i] = new Coordinate(x, y);
	    if (points == 2 && i == 1 && coords[0].equals(coords[1]))
	    {
		// Cause the loop to run again
		i--;
	    }
	    x = r.nextGaussian() * scale;
	    y = r.nextGaussian() * scale;
	}
	// Create the LineString
	return geomFactory.createLineString(coords);
    }

    /**
     * 
     * @param indexType
     * @param indexProperties
     * @param testId
     * @param testType
     * @return Time take to execute method in milliseconds
     */
    protected long runScript(String indexType, Properties indexProperties, String testId,
	    int testType)
    {
	log.debug("runScript: {}, testId={}, minEntries={}, maxEntries={}, treeVariant={}",
		  new Object[] { indexType, testId, indexProperties.getProperty("MinNodeEntries"),
			  indexProperties.getProperty("MaxNodeEntries"),
			  indexProperties.getProperty("TreeVariant") });

	SpatialIndex spatialIndex = SpatialIndexFactory.newInstance(indexType, indexProperties);
	ListDecorator ld = null;

	// Don't sort the results if we are testing the performance
	if (testType == PERFORMANCE_TEST)
	{
	    ld = new ListDecorator(spatialIndex);
	}
	else
	{
	    ld = new SortedListDecorator(spatialIndex);
	}

	Random r = new Random();
	DecimalFormat decimalFormat = new DecimalFormat();
	decimalFormat.setMinimumFractionDigits(4);
	decimalFormat.setMaximumFractionDigits(4);
	decimalFormat.setPositivePrefix(" ");
	decimalFormat.setGroupingUsed(false);

	String testInputRoot = MessageFormat.format("tests{0}test-{1}", File.separator, testId);
	String testResultsRoot =
		MessageFormat.format("tests-results{0}test-{1}", File.separator, testId);

	// Open test input file for read-only access
	// Filename is of form: test-testId-in
	LineNumberReader inputFile = null;
	String inputFileName = MessageFormat.format("{0}-{1}-{2}", testInputRoot, "in");
	try
	{
	    inputFile =
		    new LineNumberReader(new InputStreamReader(new FileInputStream(inputFileName)));
	}
	catch (FileNotFoundException e)
	{
	    log.error("Unable to open test input file {}", inputFileName);
	    assertTrue("Unable to open test input file " + inputFileName, false);
	    return -1;
	}

	// Open reference results file for read-only access.
	// Filename is of form: test-testId-reference
	LineNumberReader referenceFile = null;
	String referenceFileName = MessageFormat.format("{0}-{1}-{2}", testInputRoot, "reference");
	try
	{
	    referenceFile =
		    new LineNumberReader(
					 new InputStreamReader(
							       new FileInputStream(
										   referenceFileName)));
	}
	catch (FileNotFoundException e)
	{
	    log.error("Unable to open reference test results file {}", referenceFileName);
	    assertTrue("Unable to open reference test results file " + referenceFileName, false);
	    return -1;
	}

	// Open actual results file for writing.
	// Filename is of form: test-testId-indexType-revision-datetime, unless
	// generating reference results.
	PrintWriter outputFile = null;
	if (testType == REFERENCE_COMPARISON_TEST || testType == REFERENCE_GENERATE)
	{
	    String outputFilename = null;
	    if (testType == REFERENCE_COMPARISON_TEST)
	    {
		outputFilename =
			MessageFormat.format(
					     "{0}-{1}-{2}",
					     testResultsRoot,
					     spatialIndex.getVersion(),
					     new SimpleDateFormat("yyMMddHHmmss").format(new Date()));
	    }
	    else
	    {
		outputFilename = MessageFormat.format("{0}-{1}", testResultsRoot, "reference");
	    }
	    try
	    {
		outputFile = new PrintWriter(new FileOutputStream(outputFilename));
	    }
	    catch (FileNotFoundException e)
	    {
		log.error("Unable to open test output results file {}", outputFilename);
		assertTrue("Unable to open test output results file " + outputFilename, false);
		return -1;
	    }
	}

	long scriptStartTime = System.currentTimeMillis();
	try
	{
	    while (inputFile.ready())
	    {
		String inputLine = inputFile.readLine();
		if (inputLine.startsWith("#"))
		{
		    continue;
		}

		StringBuffer outputBuffer = null;

		if (testType == REFERENCE_COMPARISON_TEST || testType == REFERENCE_GENERATE)
		{
		    outputBuffer = new StringBuffer(inputLine);
		}

		StringTokenizer st = new StringTokenizer(inputLine);
		while (st.hasMoreTokens())
		{
		    String op = st.nextToken().toUpperCase();
		    if (op.equals("RANDOMIZE"))
		    {
			r.setSeed(Long.parseLong(st.nextToken()));
			if (testType == REFERENCE_COMPARISON_TEST || testType == REFERENCE_GENERATE)
			{
			    writeOutput(MessageFormat.format("{0} : OK", outputBuffer), outputFile,
					referenceFile);
			}
		    }
		    else if (op.equals("ADDRANDOM"))
		    {
			int count = Integer.parseInt(st.nextToken());
			int startId = Integer.parseInt(st.nextToken());
			double scale = Double.parseDouble(st.nextToken());

			if (testType == REFERENCE_COMPARISON_TEST || testType == REFERENCE_GENERATE)
			{
			    writeOutput(outputBuffer.toString(), outputFile, referenceFile);
			}

			long startTime = System.currentTimeMillis();
			for (int id = startId; id < startId + count; id++)
			{
			    Envelope env = getRandomEnvelope(r, scale);
			    spatialIndex.add(env, id);
			    if (testType == REFERENCE_COMPARISON_TEST
				    || testType == REFERENCE_GENERATE)
			    {
				writeOutput(
					    MessageFormat.format(" {0} {1} : OK", id, r.toString()),
					    outputFile, referenceFile);
			    }
			}
			long time = System.currentTimeMillis() - startTime;
			log.debug("Added {} entries in {}ms ({} ms per add)", new Object[] { count,
				time, time / (double) count });
			if (testType == PERFORMANCE_TEST)
			{
			    addPerformanceLog.info("{}, {}, {}, {}, {}, {}, {}, {}", new Object[] {
				    indexType, testId,
				    indexProperties.getProperty("MinNodeEntries"),
				    indexProperties.getProperty("MaxNodeEntries"),
				    indexProperties.getProperty("TreeVariant"),
				    spatialIndex.size(), count, (double) time / (double) count });
			}
		    }
		    else if (op.equals("DELETERANDOM"))
		    {
			int count = Integer.parseInt(st.nextToken());
			int startId = Integer.parseInt(st.nextToken());
			double scale = Double.parseDouble(st.nextToken());

			if (testType == REFERENCE_COMPARISON_TEST || testType == REFERENCE_GENERATE)
			{
			    writeOutput(outputBuffer.toString(), outputFile, referenceFile);
			}

			long startTime = System.currentTimeMillis();
			int successfulDeleteCount = 0;
			for (int id = startId; id < startId + count; id++)
			{
			    Envelope env = getRandomEnvelope(r, scale);
			    boolean deleted = spatialIndex.delete(env, id);

			    if (deleted)
			    {
				successfulDeleteCount++;
			    }

			    if (testType == REFERENCE_COMPARISON_TEST
				    || testType == REFERENCE_GENERATE)
			    {
				writeOutput(MessageFormat.format(" {0} {1} : {2}", id,
								 r.toString(), deleted),
					    outputFile, referenceFile);
			    }
			}
			long time = System.currentTimeMillis() - startTime;

			log.debug(
				  "Attempted to delete {} entries ({} successful) in {}ms ({} ms per delete)",
				  new Object[] { count, successfulDeleteCount, time,
					  time / (double) count });
		    }
		    else if (op.equals("NEARESTRANDOM"))
		    {
			int queryCount = Integer.parseInt(st.nextToken());
			double scale = Double.parseDouble(st.nextToken());

			if (testType == REFERENCE_COMPARISON_TEST || testType == REFERENCE_GENERATE)
			{
			    writeOutput(outputBuffer.toString(), outputFile, referenceFile);
			}

			long startTime = System.currentTimeMillis();
			int totalEntriesReturned = 0;
			for (int id = 0; id < queryCount; id++)
			{
			    Point p = getRandomPoint(r, scale);
			    TIntArrayList idList = ld.nearest(p, Double.POSITIVE_INFINITY);
			    totalEntriesReturned += idList.size();
			    if (testType == REFERENCE_COMPARISON_TEST
				    || testType == REFERENCE_GENERATE)
			    {
				final StringBuffer tempBuffer =
					new StringBuffer(
							 MessageFormat.format(
									      " {} {} {} : OK",
									      id,
									      decimalFormat.format(p.getX()),
									      decimalFormat.format(p.getY())));
				idList.forEach(new TIntProcedure()
				{

				    @Override
				    public boolean execute(int id)
				    {
					tempBuffer.append(' ');
					tempBuffer.append(id);
					return true;
				    }
				});
				writeOutput(tempBuffer.toString(), outputFile, referenceFile);
			    }
			}
			long time = System.currentTimeMillis() - startTime;
			log.debug("NearestQueried {} times in {} ms. Per query: {} ms, {} entries",
				  new Object[] { queryCount, time, time / (double) queryCount,
					  (double) totalEntriesReturned / (double) queryCount });
			if (testType == PERFORMANCE_TEST)
			{
			    nearestPerformanceLog.info(
						       "{}, {}, {}, {}, {}, {}, {}, {}, {}",
						       new Object[] {
							       indexType,
							       testId,
							       indexProperties.getProperty("MinNodeEntries"),
							       indexProperties.getProperty("MaxNodeEntries"),
							       indexProperties.getProperty("TreeVariant"),
							       spatialIndex.size(),
							       queryCount,
							       (double) totalEntriesReturned
								       / (double) queryCount,
							       (double) time / (double) queryCount });
			}
		    }
		    else if (op.equals("INTERSECTRANDOM"))
		    {
			int queryCount = Integer.parseInt(st.nextToken());
			double scale = Double.parseDouble(st.nextToken());

			if (testType == REFERENCE_COMPARISON_TEST || testType == REFERENCE_GENERATE)
			{
			    writeOutput(outputBuffer.toString(), outputFile, referenceFile);
			}

			long startTime = System.currentTimeMillis();
			int totalEntriesReturned = 0;
			for (int id = 0; id < queryCount; id++)
			{
			    Envelope env = getRandomEnvelope(r, scale);
			    TIntArrayList idList = ld.intersects(env);
			    totalEntriesReturned += idList.size();
			    if (testType == REFERENCE_COMPARISON_TEST
				    || testType == REFERENCE_GENERATE)
			    {
				final StringBuffer tempBuffer =
					new StringBuffer(MessageFormat.format(" {} {} : OK", id,
									      env.toString()));
				idList.forEach(new TIntProcedure()
				{

				    @Override
				    public boolean execute(int id)
				    {
					tempBuffer.append(' ');
					tempBuffer.append(id);
					return true;
				    }
				});
				writeOutput(tempBuffer.toString(), outputFile, referenceFile);
			    }
			}
			long time = System.currentTimeMillis() - startTime;
			log.debug(
				  "IntersectQueried {} times in {} ms. Per query: {} ms, {} entries",
				  new Object[] { queryCount, time, time / (double) queryCount,
					  (double) totalEntriesReturned / (double) queryCount });
			if (testType == PERFORMANCE_TEST)
			{
			    intersectPerformanceLog.info(
							 "{}, {}, {}, {}, {}, {}, {}, {}, {}",
							 new Object[] {
								 indexType,
								 testId,
								 indexProperties.getProperty("MinNodeEntries"),
								 indexProperties.getProperty("MaxNodeEntries"),
								 indexProperties.getProperty("TreeVariant"),
								 spatialIndex.size(),
								 queryCount,
								 (double) totalEntriesReturned
									 / (double) queryCount,
								 (double) time
									 / (double) queryCount });
			}
		    }
		    else if (op.equals("CONTAINSRANDOM"))
		    {
			int queryCount = Integer.parseInt(st.nextToken());
			double scale = Double.parseDouble(st.nextToken());

			if (testType == REFERENCE_COMPARISON_TEST || testType == REFERENCE_GENERATE)
			{
			    writeOutput(outputBuffer.toString(), outputFile, referenceFile);
			}

			long startTime = System.currentTimeMillis();
			int totalEntriesReturned = 0;
			for (int id = 0; id < queryCount; id++)
			{
			    Envelope env = getRandomEnvelope(r, scale);
			    TIntArrayList idList = ld.intersects(env);
			    totalEntriesReturned += idList.size();
			    if (testType == REFERENCE_COMPARISON_TEST
				    || testType == REFERENCE_GENERATE)
			    {
				final StringBuffer tempBuffer =
					new StringBuffer(MessageFormat.format(" {} {} : OK", id,
									      env.toString()));
				idList.forEach(new TIntProcedure()
				{

				    @Override
				    public boolean execute(int id)
				    {
					tempBuffer.append(' ');
					tempBuffer.append(id);
					return true;
				    }
				});
				writeOutput(tempBuffer.toString(), outputFile, referenceFile);
			    }
			}
			long time = System.currentTimeMillis() - startTime;
			log.debug(
				  "IntersectQueried {} times in {} ms. Per query: {} ms, {} entries",
				  new Object[] { queryCount, time, time / (double) queryCount,
					  (double) totalEntriesReturned / (double) queryCount });
			if (testType == PERFORMANCE_TEST)
			{
			    intersectPerformanceLog.info(
							 "{}, {}, {}, {}, {}, {}, {}, {}, {}",
							 new Object[] {
								 indexType,
								 testId,
								 indexProperties.getProperty("MinNodeEntries"),
								 indexProperties.getProperty("MaxNodeEntries"),
								 indexProperties.getProperty("TreeVariant"),
								 spatialIndex.size(),
								 queryCount,
								 (double) totalEntriesReturned
									 / (double) queryCount,
								 (double) time
									 / (double) queryCount });
			}
		    }
		}
		}
	    }
	}
	catch (IOException e)
	{
	    log.error("An error occurred while running {}", testId, e);
	}
    }
}
