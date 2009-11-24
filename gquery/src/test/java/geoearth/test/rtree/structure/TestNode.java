package geoearth.rtree.structure;

import static org.junit.Assert.assertTrue;

import geoearth.geometry.utils.EnvelopeUtils;

import org.junit.BeforeClass;
import org.junit.Test;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class TestNode
{
    private static GeometryFactory geomFactory;
    private static WKTReader reader;
    private static Polygon a;
    private static Polygon b;

    /**
     * Setup the initial mock objects for testing the Node class
     * 
     * @throws ParseException
     *             Invalid WKT
     */
    @BeforeClass
    public static void init() throws ParseException
    {
	geomFactory = new GeometryFactory();
	reader = new WKTReader(geomFactory);

	// Square
	a = (Polygon) reader.read("POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))");
	// Triangle inside the square
	b = (Polygon) reader.read("POLYGON((0 0, 0 10, 5 5, 0 0))");
    }

    /**
     * 
     */
    @Test
    public void testEdgeOverlaps()
    {
	System.out.println("A Envelope: "
		+ geomFactory.toGeometry(a.getEnvelopeInternal()).toText());
	System.out.println("B Envelope: "
		+ geomFactory.toGeometry(b.getEnvelopeInternal()).toText());

	System.out.println("A Intersect B: " + a.intersection(b).toText());

	assertTrue("Boundary intersects", a.relate(b, "*T*******"));
	assertTrue("Edge Overlaps", EnvelopeUtils.edgeOverlaps(a.getEnvelopeInternal(),
							       b.getEnvelopeInternal()));
	assertTrue("two geometries have at least one point in common", a.intersects(b));
    }
}
