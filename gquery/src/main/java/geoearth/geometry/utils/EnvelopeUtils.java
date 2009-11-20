package geoearth.geometry.utils;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Utility class of methods working with Envelopes
 * 
 * @author cjmcmill
 * 
 */
public final class EnvelopeUtils
{
    /**
     * Return the minimum distance between the envelope and the geometry
     * 
     * @param env
     *            Envelope
     * @param geom
     *            Geometry
     * @return The minimum distance between the envelope and the geometry
     */
    public static final double distance(Envelope env, Geometry geom)
    {
	GeometryFactory geomFact = new GeometryFactory();
	Geometry envGeom = geomFact.toGeometry(env);
	return envGeom.distance(geom);
    }

    /**
     * Calculate if the edges of the two envelopes overlap
     * 
     * @param a
     *            Primary Envelope
     * @param b
     *            Secondary Envelope
     * @return true if one or more of the boundaries of the two envelopes
     *         overlap
     */
    public static final boolean edgeOverlaps(Envelope a, Envelope b)
    {
	if (a == null || a.isNull() || b == null || b.isNull())
	{
	    return false;
	}

	double aMinX = a.getMinX();
	double aMinY = a.getMinY();
	double bMinX = b.getMinX();
	double bMinY = b.getMinY();

	double aMaxX = a.getMaxX();
	double aMaxY = a.getMaxY();
	double bMaxX = b.getMaxX();
	double bMaxY = b.getMaxY();

	if ((bMinX <= aMaxX && aMinX <= bMaxX) || (bMinY <= aMaxY && aMinY <= bMaxY))
	{
	    return true;
	}
	return false;
    }

    /**
     * Calculate the area by which origEnv would be enlarged if added to the
     * newEnv. Neither envelope is altered.
     * 
     * @param origEnv
     *            Original Envelope
     * @param newEnv
     *            Envelope to union with origEnv, to compute the difference in
     *            area of the union and the original envelope
     * @return The difference in area of the union and the original envelope
     */
    public static final double enlargement(Envelope origEnv, Envelope newEnv)
    {
	double enlargedArea =
		(Math.max(origEnv.getMaxX(), newEnv.getMaxX()) - Math.min(origEnv.getMinX(),
									  newEnv.getMinX()))
			* (Math.max(origEnv.getMaxY(), newEnv.getMaxY()) - Math.min(
										    origEnv.getMinY(),
										    newEnv.getMinY()));

	return enlargedArea - origEnv.getArea();
    }
}
