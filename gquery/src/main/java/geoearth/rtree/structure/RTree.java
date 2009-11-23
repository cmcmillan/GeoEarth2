//   RTree.java
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
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
package geoearth.rtree.structure;

import geoearth.geometry.utils.EnvelopeUtils;
import geoearth.rtree.IntProcedure;
import geoearth.rtree.SpatialIndex;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import gnu.trove.TIntStack;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * This is a lightweight RTree implementation, specifically designed for the
 * following features (in order of importance):
 * <ul>
 * <li>Fast intersection query performance. To achieve this, the RTree uses only
 * main memory to store entries. Obviously this will only improve performance if
 * there is enough physical memory to avoid paging.</li>
 * <li>Low memory requirements.</li>
 * <li>Fast add performance.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The main reason for the high speed of this RTree implementation is the
 * avoidance of the creation of unnecessary objects, mainly achieved by using
 * primitive collections from the trove4j library.
 * </p>
 * 
 * @author aled@sourceforge.net
 * @version 1.0b2p1
 */
public class RTree implements SpatialIndex
{
    private static final Logger log = LoggerFactory.getLogger(RTree.class.getName());
    private static final Logger deleteLog =
	    LoggerFactory.getLogger(RTree.class.getName() + "-delete");

    private static final String version = "1.0b2p1";

    // parameters of the tree
    private static final int DEFAULT_MAX_NODE_ENTRIES = 10;
    int maxNodeEntries = 10;
    int minNodeEntries = 10;

    // Map of NodeID -> Node object
    // [X] TODO eliminate the this map - it should not be needed
    // Nodes can be found by traversing the tree.
    private TIntObjectHashMap nodeMap = new TIntObjectHashMap();

    // internal consistency checking - set to true if debugging tree corruption
    private static final boolean INTERNAL_CONSISTENCY_CHECKING = false;

    // used to mark the status of entries during a node split
    private static final int ENTRY_STATUS_ASSIGNED = 0;
    private static final int ENTRY_STATUS_UNASSIGNED = 1;
    private byte[] entryStatus = null;
    private byte[] initialEntryStatus = null;

    // Stacks used to store nodeId and entry index of each node
    // from the root down to the leaf. Enables fast lookup
    // of nodes when is a split is propagated up the tree.
    private TIntStack parents = new TIntStack();
    private TIntStack parentsEntry = new TIntStack();

    // Initialization
    private int treeHeight = 1; // leaves are always level 1
    private int rootNodeId = 0;
    private int size = 0;

    // Enables creation of new nodes
    private int highestUsedNodeId = rootNodeId;

    // Deleted node objects are retained in the nodeMap,
    // so that they can be reused. Store the IDs of nodes
    // which can be reused
    private TIntStack deletedNodeIds = new TIntStack();

    // List of nearest rectangles. Use a member variable to
    // avoid recreating the object each time nearest() is called.
    private TIntArrayList nearestIds = new TIntArrayList();

    // Inner class used as a bridge between the trove4j TIntProcedure
    // and the SpatialIndex IntProcedure. This is used because
    // the nearest rectangles must be stored as they are found, in
    // case a closer one is found later.
    // 
    // A single instance of this class is used to avoid creating a new
    // one every time nearest() is called.
    private class TIntProcedureVisit implements TIntProcedure
    {
	public IntProcedure m_intProcedure = null;

	public TIntProcedureVisit()
	{
	}

	public void setProcedure(IntProcedure ip)
	{
	    m_intProcedure = ip;
	}

	public boolean execute(int i)
	{
	    m_intProcedure.execute(i);
	    return true;
	}
    };

    private TIntProcedureVisit visitProc = new TIntProcedureVisit();

    /**
     * Constructor. Use init() method to initialize the parameters of the RTree
     */
    public RTree()
    {
	return; // NOP
    }

    // -------------------------------------------------------------------------
    // public implementation of SpatialIndex interface:
    // init(Properties)
    // add(Rectangle, int)
    // delete(Rectangle, int)
    // nearest(Point, IntProcedure, float)
    // intersects(Rectangle, IntProcedure)
    // contains(Rectangle, IntProcedure)
    // size()
    // -------------------------------------------------------------------------

    /**
     * <p>
     * Initialize implementation dependent properties of the RTree. Currently
     * implemented properties are:
     * <ul>
     * <li>MaxNodeEntries</li> This specifies the maximum number of entries in a
     * node. The default value is 10, which is used if the property is not
     * specified, or is less than 2.
     * <li>MinNodeEntries</li> This specifies the minimum number of entries in a
     * node. The default value is half of the MaxNodeEntries value (rounded
     * down), which is used if the property is not specified or is less than 1.
     * </ul>
     * </p>
     * 
     * @see SpatialIndex#init(Properties)
     */
    public void init(Properties props)
    {
	maxNodeEntries = Integer.parseInt(props.getProperty("MaxNodeEntries", "0"));
	minNodeEntries = Integer.parseInt(props.getProperty("MinNodeEntries", "0"));

	// Obviously a node with less than 2 entries cannot be split
	// The node splitting algorithm will work with only 2 entries
	// per node, but will be inefficient
	if (maxNodeEntries < 2)
	{
	    log.warn("Invalid Max Node Entries = {} Resetting to default value of {}",
		     maxNodeEntries, DEFAULT_MAX_NODE_ENTRIES);
	    maxNodeEntries = DEFAULT_MAX_NODE_ENTRIES;
	}

	// The MinNodeEntries must be less than or equal to (int) MaxNodeEntries
	// / 2
	if (minNodeEntries < 1 || minNodeEntries > maxNodeEntries / 2)
	{
	    log.warn("MinNodeEntries must be between 1 and MaxNodeEntries / 2");
	    minNodeEntries = maxNodeEntries / 2;
	}

	entryStatus = new byte[maxNodeEntries];
	initialEntryStatus = new byte[maxNodeEntries];

	for (int i = 0; i < maxNodeEntries; i++)
	{
	    initialEntryStatus[i] = ENTRY_STATUS_UNASSIGNED;
	}

	Node root = new Node(rootNodeId, 1, maxNodeEntries);
	nodeMap.put(rootNodeId, root);

	log.info("init() MaxNodeEntries = {}, MinNodeEntries = {}", maxNodeEntries, minNodeEntries);
    }

    @Override
    public void add(Envelope env, int id)
    {
	log.debug("Adding envelope {}, id {}", env, id);
	add(new Envelope(env), id, 1);

	size++;
    }

    /**
     * Adds a new entry at a specified level in the tree
     * 
     * @param env
     *            Envelope to add
     * @param id
     *            ID of the Envelope
     * @param level
     *            Level at which the entry should be added to the tree
     */
    private void add(Envelope env, int id, int level)
    {
	// Step 1: Find position for new record]
	// Invoke ChooseLeaf to select a leaf node L
	// in which to place env
	Node n = chooseNode(env, level);
	Node newLeaf = null;

	// Step 2: Add record to leaf node
	// If n has room for another entry, install Entry
	// Otherwise invoke SplitNode to obtain newLeaf
	// containing Entry and all the old entries of n
	if (n.entryCount < maxNodeEntries)
	{
	    n.addEntryNoCopy(env, id);
	}
	else
	{
	    newLeaf = splitNode(n, env, id);
	}

	// Step 3: Propagate changes up the tree
	// Invoke AdjustTree on n, passing newLeaf
	// if a split was performed
	Node newNode = adjustTree(n, newLeaf);

	// Step 4: Grow tree taller
	// If node split propagation cause the root to split,
	// create a new root whose children are the two resulting nodes.
	if (newNode != null)
	{
	    int oldRootNodeId = rootNodeId;
	    Node oldRoot = getNode(oldRootNodeId);

	    rootNodeId = getNextNodeId();
	    treeHeight++;
	    Node root = new Node(rootNodeId, treeHeight, maxNodeEntries);
	    root.addEntry(newNode.minBoundingBox, newNode.nodeId);
	    root.addEntry(oldRoot.minBoundingBox, oldRoot.nodeId);
	    nodeMap.put(rootNodeId, root);
	}

	if (INTERNAL_CONSISTENCY_CHECKING)
	{
	    checkConsistency(rootNodeId, treeHeight, null);
	}
    }

    @Override
    public boolean delete(Envelope en, int id)
    {
	// FindLeaf algorithm inlined here. Note the "official" algorithm
	// searches all overlapping entries. This seemed inefficient,
	// as an entry is only worth searching if it contains (NOT overlaps)
	// the envelope we are searching for.
	// 
	// Also the algorithm has be changed so it is not recursive

	// Algorithm: Search subtrees
	// If root is not a leaf, check each entry
	// to determine if it contains env.
	// For each entry found invoke findLeaf
	// on the node pointed to by the entry,
	// until r is found or all entries have been checked.
	parents.clear();
	parents.push(rootNodeId);

	parentsEntry.clear();
	parentsEntry.push(-1);
	Node n = null;
	// index of entry to be deleted in leaf
	int foundIndex = -1;

	while (foundIndex == -1 && parents.size() > 0)
	{
	    n = getNode(parents.peek());
	    int startIndex = parentsEntry.peek() + 1;

	    if (!n.isLeaf())
	    {
		deleteLog.debug("Searching node {} , from index {}", n.nodeId, startIndex);
		boolean contains = false;
		for (int i = startIndex; i < n.entryCount; i++)
		{
		    if (n.entries[i].contains(en))
		    {
			parents.push(n.ids[i]);
			parentsEntry.pop();
			// This becomes the start index when the child has been
			// searched
			parentsEntry.push(i);
			parentsEntry.push(-1);
			contains = true;
			break; // goto the next iteration of while
		    }
		}
		if (contains)
		{
		    continue;
		}
		foundIndex = n.findEntry(en, id);
		parents.pop();
		parentsEntry.pop();
	    }
	}

	if (foundIndex != -1)
	{
	    n.deleteEntry(foundIndex, minNodeEntries);
	    condenseTree(n);
	    size--;
	}

	// Shrink the tree if possible (i.e. if root node has exactly one entry,
	// and that entry is not a leaf node, delete the root and its entry
	// becomes the new root
	Node root = getNode(rootNodeId);
	while (root.entryCount == 1 && treeHeight > 1)
	{
	    root.entryCount = 0;
	    rootNodeId = root.ids[0];
	    treeHeight--;
	    root = getNode(rootNodeId);
	}

	return (foundIndex != -1);
    }

    @Override
    public void contains(Envelope env, IntProcedure v)
    {
	// Find all envelopes in the tree that are contained by the passed
	// envelope.
	// <p>
	// Written to be non-recursive (should model other searches on this?)
	parents.clear();
	parents.push(rootNodeId);

	parentsEntry.clear();
	parentsEntry.push(-1);

	// Test to see if the envelopes intersect before proceeding
	// if no intersection return immediately
	Node rootNode = getNode(rootNodeId);
	if (!rootNode.minBoundingBox.intersects(env))
	{
	    return;
	}

	while (parents.size() > 0)
	{
	    Node n = getNode(parents.peek());
	    int startIndex = parentsEntry.peek() + 1;

	    if (!n.isLeaf())
	    {
		// go through every entry in the index node to check
		// if it intersects the passed rectangle.
		// If so, it could contain entries that are contained
		boolean intersects = false;
		for (int i = startIndex; i < n.entryCount; i++)
		{
		    if (env.intersects(n.entries[i]))
		    {
			parents.push(n.ids[i]);
			parentsEntry.pop();
			// This becomes the start index when the child has been
			// searched
			parentsEntry.push(i);
			parentsEntry.push(-1);
			intersects = true;
			break; // goto the next iteration of while
		    }
		}
		if (intersects)
		{
		    continue;
		}
	    }
	    else
	    {
		// go through every entry in the leaf to check
		// if it is contained by the passed envelope
		for (int i = 0; i < n.entryCount; i++)
		{
		    if (env.contains(n.entries[i]))
		    {
			v.execute(n.ids[i]);
		    }
		}
	    }
	    parents.pop();
	    parentsEntry.pop();
	}
    }

    @Override
    public Envelope getBounds()
    {
	Envelope bounds = null;

	Node n = getNode(rootNodeId);
	if (n != null && n.minBoundingBox != null)
	{
	    bounds = new Envelope(n.minBoundingBox);
	}
	return bounds;
    }

    @Override
    public String getVersion()
    {
	return "RTree-" + version;
    }

    @Override
    public void intersects(Envelope r, IntProcedure ip)
    {
	Node rootNode = getNode(rootNodeId);
	intersects(r, ip, rootNode);
    }

    @Override
    public void nearest(Point p, IntProcedure v, double distance)
    {
	Node rootNode = getNode(rootNodeId);
	nearest(p, rootNode, distance);

	visitProc.setProcedure(v);
	nearestIds.forEach(visitProc);
	nearestIds.clear();
    }

    @Override
    public int size()
    {
	return size;
    }

    // --------------------------------------------------------------------
    // end of SpatialIndex Methods
    // --------------------------------------------------------------------

    /**
     * Get the next available node ID. Reuse deleted node IDs if possible.
     * 
     * @return The next available node ID
     */
    private int getNextNodeId()
    {
	int nextNodeId = 0;
	if (deletedNodeIds.size() > 0)
	{
	    nextNodeId = deletedNodeIds.pop();
	}
	else
	{
	    nextNodeId = highestUsedNodeId + 1;
	    highestUsedNodeId++;
	}
	return nextNodeId;
    }

    /**
     * Get a node object, given the ID of the node
     * 
     * @param index
     *            ID of the node
     * @return Node with ID corresponding to index
     */
    public Node getNode(int index)
    {
	return (Node) nodeMap.get(index);
    }

    /**
     * Get the root node ID
     * 
     * @return The root node ID
     */
    public int getRootNodeId()
    {
	return rootNodeId;
    }

    /**
     * Split a node. Algorithm is taken pretty much verbatim from Guttman's
     * original paper.
     * 
     * @param n
     * @param newEnv
     * @param newId
     * @return
     */
    private Node splitNode(Node n, Envelope newEnv, int newId)
    {
	// Step 1: Pick first entry for each group
	// Apply algorithm pickSeeds to choose two entries
	// to be the first elements of the groups.
	// Assign each to a group.
	System.arraycopy(initialEntryStatus, 0, entryStatus, 0, minNodeEntries);

	Node newNode = new Node(getNextNodeId(), n.level, maxNodeEntries);
	nodeMap.put(newNode.nodeId, newNode);

	// This also sets the entryCount to 1
	pickSeeds(n, newEnv, newId, newNode);

	// Step 2: Check if done
	// If all entries have been assigned, stop. If one group has so few
	// entries that all the rest must be assigned to it in order
	// for it to have the minimum number m, assign them and stop.
	while (n.entryCount + newNode.entryCount < maxNodeEntries + 1)
	{
	    if (maxNodeEntries - newNode.entryCount + 1 == minNodeEntries)
	    {
		// assign all remaining entries to the original node
		for (int i = 0; i < maxNodeEntries; i++)
		{
		    if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED)
		    {
			entryStatus[i] = ENTRY_STATUS_ASSIGNED;
			n.minBoundingBox.expandToInclude(n.entries[i]);
			n.entryCount++;
		    }
		}
		break;
	    }
	    if (maxNodeEntries - newNode.entryCount + 1 == minNodeEntries)
	    {
		// assign all remaining entries to the new node
		for (int i = 0; i < maxNodeEntries; i++)
		{
		    if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED)
		    {
			entryStatus[i] = ENTRY_STATUS_ASSIGNED;
			newNode.addEntryNoCopy(n.entries[i], n.ids[i]);
			n.entries[i] = null;
			n.entryCount++;
		    }
		}
		break;
	    }

	    // Step 3: Select entry to assign
	    // Invoke algorithm pickNext to choose the next entry to assign.
	    // Add it to the the group whose covering envelope will have to be
	    // enlarged least to accommodate it.Resolve ties by adding the entry
	    // to the group with smaller area, then to the one with fewer
	    // entries,
	    // then to either. Repeat from Step 2
	    pickNext(n, newNode);
	}

	n.reorganize(this);

	// check that the minBoundingBox for each node is correct
	if (INTERNAL_CONSISTENCY_CHECKING)
	{
	    if (!n.minBoundingBox.equals(calculateMinBB(n)))
	    {
		log.error("Error: splitNode old node minimum bounding box wrong");
	    }
	    if (!newNode.minBoundingBox.equals(calculateMinBB(newNode)))
	    {
		log.error("Error: splitNode new node minimum bounding box wrong");
	    }
	}
	return newNode;
    }

    /**
     * Pick the seeds used to split a node. Select two entries to be the first
     * elements of the groups
     * 
     * @param n
     * @param newEnv
     * @param newId
     * @param newNode
     */
    private void pickSeeds(Node n, Envelope newEnv, int newId, Node newNode)
    {
	// Find extreme rectangles along all dimensions.
	// Along each dimension, find the entry whose rectangle
	// has the highest low side, and the one with the lowest high side.
	// Record the separation
	double maxNormalizedSeparation = 0;
	int highestLowIndex = 0;
	int lowestHighIndex = 0;

	// for the purposes of picking seeds, take the minimim bound box of the
	// node to include the new envelope as well
	n.minBoundingBox.expandToInclude(newEnv);

	log.debug("pickSeeds(): NodeId = {}, newEnv = {}", n.nodeId, newEnv);

	double tempHighestLowX = newEnv.getMinX();
	// -1 indicates the new rectangle is the seed
	int tempHighestLowIndexX = -1;

	double tempLowestHighX = newEnv.getMaxX();
	// -1 indicates the new rectangle is the seed
	int tempLowestHighIndexX = -1;

	double tempHighestLowY = newEnv.getMinY();
	// -1 indicates the new rectangle is the seed
	int tempHighestLowIndexY = -1;

	double tempLowestHighY = newEnv.getMaxY();
	// -1 indicates the new rectangle is the seed
	int tempLowestHighIndexY = -1;

	for (int i = 0; i < n.entryCount; i++)
	{
	    double tempLowX = n.entries[i].getMinX();
	    double tempLowY = n.entries[i].getMinY();
	    if (tempLowX >= tempHighestLowX)
	    {
		tempHighestLowX = tempLowX;
		tempHighestLowIndexX = i;
	    }
	    else
	    {
		// Ensure that the same index cannot be both
		// lowestHighX and highestLowX
		double tempHighX = n.entries[i].getMaxX();
		if (tempHighX <= tempLowestHighX)
		{
		    tempLowestHighX = tempHighX;
		    tempLowestHighIndexX = i;
		}
	    }
	    if (tempLowY >= tempHighestLowY)
	    {
		tempHighestLowY = tempLowY;
		tempHighestLowIndexY = i;
	    }
	    else
	    {
		// Ensure that the same index cannot be both
		// lowestHighY and highestLowY
		double tempHighY = n.entries[i].getMaxY();
		if (tempHighY <= tempLowestHighY)
		{
		    tempLowestHighY = tempHighY;
		    tempLowestHighIndexY = i;
		}
	    }

	    // Step 2: Adjust for shape of the rectangle cluster
	    // Normalize the separations by dividing by the widths
	    // of the entire set along the corresponding dimension
	    // NOTE: This ensures that it doesn't matter what
	    // dimension is being
	    double normalizedSeparationX =
		    (tempHighestLowX - tempLowestHighX)
			    / (n.minBoundingBox.getMaxX() - n.minBoundingBox.getMinX());
	    double normalizedSeparationY =
		    (tempHighestLowY - tempLowestHighY)
			    / (n.minBoundingBox.getMaxY() - n.minBoundingBox.getMinY());

	    if (normalizedSeparationX > 1 || normalizedSeparationX < -1)
	    {
		log.error("Invalid normalized separation");
	    }
	    if (normalizedSeparationY > 1 || normalizedSeparationY < -1)
	    {
		log.error("Invalid normalized separation");
	    }

	    log.debug(
		      "Entry {}, X dimension: HighestLow = {} (index {}), LowestHigh = {} (index {}), NormalizedSeparation = {}",
		      new Object[] { i, tempHighestLowX, tempHighestLowIndexX, tempLowestHighX,
			      tempLowestHighIndexX, normalizedSeparationX });
	    log.debug(
		      "Entry {}, Y dimension: HighestLow = {} (index {}), LowestHigh = {} (index {}), NormalizedSeparation = {}",
		      new Object[] { i, tempHighestLowY, tempHighestLowIndexY, tempLowestHighY,
			      tempLowestHighIndexY, normalizedSeparationY });

	    // Step 3: Select the most extreme pair
	    // Choose the pair with the greatest normalized
	    // separation along any dimension
	    if (normalizedSeparationX > maxNormalizedSeparation)
	    {
		maxNormalizedSeparation = normalizedSeparationX;
		highestLowIndex = tempHighestLowIndexX;
		lowestHighIndex = tempLowestHighIndexX;
	    }
	    if (normalizedSeparationY > maxNormalizedSeparation)
	    {
		maxNormalizedSeparation = normalizedSeparationY;
		highestLowIndex = tempHighestLowIndexY;
		lowestHighIndex = tempLowestHighIndexY;
	    }
	}

	// highestLowIndex is the seed for the new node
	if (highestLowIndex == -1)
	{
	    newNode.addEntry(newEnv, newId);
	}
	else
	{
	    newNode.addEntryNoCopy(n.entries[highestLowIndex], n.ids[highestLowIndex]);
	    n.entries[highestLowIndex] = null;

	    // move the new envelope into the space vacted by the seed for the
	    // new node
	    n.entries[highestLowIndex] = newEnv;
	    n.ids[highestLowIndex] = newId;
	}

	// lowestHighIndex is the seed for the original node
	if (lowestHighIndex == -1)
	{
	    lowestHighIndex = highestLowIndex;
	}

	entryStatus[lowestHighIndex] = ENTRY_STATUS_ASSIGNED;
	n.entryCount = 1;

	n.minBoundingBox.init(n.entries[lowestHighIndex].getMinX(),
			      n.entries[lowestHighIndex].getMinY(),
			      n.entries[lowestHighIndex].getMaxX(),
			      n.entries[lowestHighIndex].getMaxY());
    }

    /**
     * Pick the next entry to be assigned to a group during a node split.
     * 
     * Determine the cost of putting each entry in each group. For each entry
     * not yet in a group, calculate the area increase required in the covering
     * rectangles of each group
     * 
     * @param n
     * @param newNode
     * @return
     */
    private int pickNext(Node n, Node newNode)
    {
	double maxDifference = Double.NEGATIVE_INFINITY;
	int next = 0;
	int nextGroup = 0;

	log.debug("pickNext()");

	for (int i = 0; i < maxNodeEntries; i++)
	{
	    if (entryStatus[i] == ENTRY_STATUS_UNASSIGNED)
	    {
		if (n.entries[i] == null)
		{
		    log.error("Error: Node {}, entry {} is null", n.nodeId, i);
		}

		double nIncrease = n.enlargement(n.entries[i]);
		double newNodeIncrease = newNode.enlargement(n.entries[i]);
		double difference = Math.abs(nIncrease - newNodeIncrease);

		if (difference > maxDifference)
		{
		    next = i;
		    if (nIncrease < newNodeIncrease)
		    {
			nextGroup = 0;
		    }
		    else if (newNodeIncrease < nIncrease)
		    {
			nextGroup = 1;
		    }
		    else if (n.minBoundingBox.getArea() < newNode.minBoundingBox.getArea())
		    {
			nextGroup = 0;
		    }
		    else if (newNode.minBoundingBox.getArea() < n.minBoundingBox.getArea())
		    {
			nextGroup = 1;
		    }
		    else if (newNode.entryCount < maxNodeEntries / 2)
		    {
			nextGroup = 0;
		    }
		    else
		    {
			nextGroup = 1;
		    }
		    maxDifference = difference;
		}
		log.debug(
			  "Entry {} group0 increase = {}, group1 increase = {}, diff = {}, MaxDiff = {} (entry {})",
			  new Object[] { i, nIncrease, newNodeIncrease, difference, maxDifference,
				  next });
	    }
	}

	entryStatus[next] = ENTRY_STATUS_ASSIGNED;

	if (nextGroup == 0)
	{
	    // Don't add the entry since it already exists in n.
	    // We are just putting it back
	    n.minBoundingBox.expandToInclude(n.entries[next]);
	    n.entryCount++;
	}
	else
	{
	    // move to new node
	    newNode.addEntryNoCopy(n.entries[next], n.ids[next]);
	    n.entries[next] = null;
	}
	return next;
    }

    /**
     * Recursively searches the tree for the nearest entry. Other queries call
     * execute on an IntProcedure when a matching entry is found; however
     * nearest() must store the entry Ids as it searches the tree, in case a
     * nearer entry is found. <br>
     * Uses the member variable nearestIds to store the nearest entry Ids.
     * 
     * TODO rewrite to be non-recursive?
     * 
     * @param p
     * @param n
     * @param nearestDistance
     * @return
     */
    private double nearest(Point p, Node n, double nearestDistance)
    {
	for (int i = 0; i < n.entryCount; i++)
	{
	    double tempDistance = EnvelopeUtils.distance(n.entries[i], p);
	    if (n.isLeaf())
	    {
		// For leaves, the distance is an actual nearest distance
		if (tempDistance < nearestDistance)
		{
		    nearestDistance = tempDistance;
		    nearestIds.clear();
		}
		if (tempDistance <= nearestDistance)
		{
		    nearestIds.add(n.ids[i]);
		}
	    }
	    else
	    {
		// for index nodes, only go into them if they potentially could
		// have an envelope closer than actualNearest
		if (tempDistance <= nearestDistance)
		{
		    // search the child node
		    nearestDistance = nearest(p, getNode(n.ids[i]), nearestDistance);
		}
	    }
	}
	return nearestDistance;
    }

    /**
     * Recursively searches the tree for all intersecting entries. Immediately
     * calls execute() on the passed IntProcedure when a matching entry is
     * found.
     * 
     * TODO rewrite this to be non-recursive? Make sure it doesn't slow it down.
     * 
     * @param env
     * @param v
     * @param n
     */
    private void intersects(Envelope env, IntProcedure v, Node n)
    {
	for (int i = 0; i < n.entryCount; i++)
	{
	    if (env.intersects(n.entries[i]))
	    {
		if (n.isLeaf())
		{
		    v.execute(n.ids[i]);
		}
		else
		{
		    Node childNode = getNode(n.ids[i]);
		    intersects(env, v, childNode);
		}
	    }
	}
    }

    /**
     * Used by delete(). Ensures that all nodes from the passed node up to the
     * root have the minimum number of entries.
     * 
     * Note that parent and parentEntryStacks are expected to contain the
     * nodeIds of all parents up to the root.
     */
    private Envelope oldEnvelope = new Envelope();

    private void condenseTree(Node l)
    {
	// Step 1: Initialize
	// Set n = l. Set the list of eliminated nodes to be empty.
	Node n = l;
	Node parent = null;
	int parentEntry = 0;

	TIntStack eliminatedNodeIds = new TIntStack();

	// Step 2: Find parent entry
	// If N is the root, go to Step 6.
	// Otherwise let P be the parent of N,
	// and En be N's entry in P
	while (n.level != treeHeight)
	{
	    parent = getNode(parents.pop());
	    parentEntry = parentsEntry.pop();

	    // Step 3: Eliminate under-full node
	    // If N has too few entries, delete En from P
	    // and add N to the list of eliminated nodes
	    if (n.entryCount < minNodeEntries)
	    {
		parent.deleteEntry(parentEntry, minNodeEntries);
		eliminatedNodeIds.push(n.nodeId);
	    }
	    else
	    {
		// Step 4: Adjust covering rectangle
		// If N has not been eliminated, adjust EnI to tightly contain
		// all entries in N
		if (!n.minBoundingBox.equals(parent.entries[parentEntry]))
		{
		    // Set the old Envelope to the current parent envelope
		    oldEnvelope.init(parent.entries[parentEntry]);
		    // Update the parent entry to be the minimum bounding box of
		    // N
		    parent.entries[parentEntry].init(n.minBoundingBox);
		    // Recalculate the minimum bounding box of the parent
		    parent.recalculateMinBB(oldEnvelope);
		}
	    }
	    // Step 5: Move up one level in tree
	    // Set N = P and repeat from Step 2
	    n = parent;
	}

	// Step 6: Reinsert orphaned entries
	// Reinsert all entries of nodes in set Q.
	// Entries from eliminated leaf nodes are
	// reinserted in tree leaves as an Insert(),
	// but entries from higher level nodes must
	// be placed higher in the tree, so that leaves
	// of their dependent subtrees will be on the same
	// level as leaves of the main tree
	while (eliminatedNodeIds.size() > 0)
	{
	    Node e = getNode(eliminatedNodeIds.pop());
	    for (int j = 0; j < e.entryCount; j++)
	    {
		add(e.entries[j], e.ids[j], e.level);
		e.entries[j] = null;
	    }
	    e.entryCount = 0;
	    deletedNodeIds.push(e.nodeId);
	}
    }

    /**
     * Chooses a leaf to add the envelope to.
     * <p>
     * Used by add().
     * 
     * @param env
     * @param level
     * @return Node to add the envelope to
     */
    private Node chooseNode(Envelope env, int level)
    {
	// Step 1: Initialization
	// Set N to be the root node;
	Node n = getNode(rootNodeId);
	parents.clear();
	parentsEntry.clear();

	while (true)
	{
	    if (n == null)
	    {
		log.error("Could not get root node ({})", rootNodeId);
	    }
	    // Step 2: Leaf Check
	    // If N is a leaf, return N
	    if (n.level == level)
	    {
		return n;
	    }

	    // Step 3: Choose subtree
	    // If N is not at the desired level, let F be the entry in N
	    // whose envelope FI needs least enlargement to include EI.
	    // Resolve ties by choosing the entry with the envelope with
	    // the smallest area
	    double leastEnlargement = EnvelopeUtils.enlargement(n.entries[0], env);
	    // Index of envelope in subtree
	    int index = 0;
	    for (int i = 1; i < n.entryCount; i++)
	    {
		Envelope tempEnv = n.entries[i];
		double tempEnlargment = EnvelopeUtils.enlargement(tempEnv, env);
		if ((tempEnlargment < leastEnlargement)
			|| ((tempEnlargment == leastEnlargement && tempEnv.getArea() < n.entries[index].getArea())))
		{
		    index = i;
		    leastEnlargement = tempEnlargment;
		}
	    }

	    parents.push(n.nodeId);
	    parentsEntry.push(index);

	    // Step 4: Descend until a leaf is reached.
	    // Set N to be the child node pointed to by Fp
	    // and repeat from Step 2
	    n = getNode(n.ids[index]);
	}
    }

    /**
     * Ascend from a leaf node L to the root, adjusting covering envelopes and
     * propagating node splits as necessary.
     * 
     * @param n
     * @param nn
     * @return
     */
    private Node adjustTree(Node n, Node nn)
    {
	// Step 1: Initialization
	// Set N = L. If L was split previously,
	// set NN to be the resulting second node.

	// Step 2: Check if done
	// If N is the root, stop.
	while (n.level != treeHeight)
	{
	    // Step 3: Adjust covering envelope in parent entry
	    // Let P be the parent node of N, let En be N's entry in P.
	    // Adjust EnI so that it tightly encloses all entry envelopes in N.
	    Node parent = getNode(parents.pop());
	    int entry = parentsEntry.pop();

	    if (parent.ids[entry] != n.nodeId)
	    {
		log.error(
			  "Error: entry {} in node {} should point to node {}; actually points to node {}",
			  new Object[] { entry, parent.nodeId, n.nodeId, parent.ids[entry] });
	    }

	    if (!parent.entries[entry].equals(n.minBoundingBox))
	    {
		parent.entries[entry].init(n.minBoundingBox);
		parent.minBoundingBox.init(parent.entries[0]);
		for (int i = 1; i < parent.entryCount; i++)
		{
		    parent.minBoundingBox.expandToInclude(parent.entries[i]);
		}
	    }

	    // Step 4: Propagate node split upward.
	    // If N has a partner NN resulting from an earlier split,
	    // create a new entry Enn with Ennp point to NN and
	    // Enni enclosing all envelopes in NN. Add Enn to P if there is
	    // room. Otherwise, invoke splitNode to produce P and PP containing
	    // Enn and all P's old entries.
	    Node newNode = null;
	    if (nn != null)
	    {
		if (parent.entryCount < maxNodeEntries)
		{
		    parent.addEntry(nn.minBoundingBox, nn.nodeId);
		}
		else
		{
		    newNode = splitNode(parent, new Envelope(nn.minBoundingBox), nn.nodeId);
		}
	    }

	    // Step 5: Move up to next level
	    // Set N = P and set NN = PP if a split occurred. Repeat from Step
	    // 2.
	    n = parent;
	    nn = newNode;

	    parent = null;
	    newNode = null;
	}
	return nn;
    }

    /**
     * Check the consistency of the tree.
     * 
     * @param nodeId
     * @param expectedLevel
     * @param expectedMinBB
     */
    private void checkConsistency(int nodeId, int expectedLevel, Envelope expectedMinBB)
    {
	// Go through the tree, and check that the internal data structures of
	// the tree are not corrupted.
	Node n = getNode(nodeId);

	if (n == null)
	{
	    log.error("Error: Could not read node {}", nodeId);
	}

	if (n.level != expectedLevel)
	{
	    log.error("Error: Node {}, expected level {}, actual level {}", new Object[] { nodeId,
		    expectedLevel, n.level });
	}

	Envelope calculatedMinBB = calculateMinBB(n);
	if (!n.minBoundingBox.equals(calculatedMinBB))
	{
	    log.error(
		      "Error: Node {}, calculated min bounding box does not equal stored min bounding box",
		      nodeId);
	}

	if (expectedMinBB != null && !n.minBoundingBox.equals(expectedMinBB))
	{
	    log.error(
		      "Error: Node {}, expected min bounding box (from parent) does not equal stored min bounding box",
		      nodeId);
	}

	for (int i = 0; i < n.entryCount; i++)
	{
	    if (n.entries[i] == null)
	    {
		log.error("Error: Node {}, Entry {} is null", new Object[] { nodeId, i });
	    }

	    if (n.level > 1)
	    {
		// If not a leaf node
		checkConsistency(n.ids[i], n.level - 1, n.entries[i]);
	    }
	}
    }

    /**
     * Calculate the node's minimum bounding box from its entries.
     * 
     * @param n
     * @return
     */
    private Envelope calculateMinBB(Node n)
    {
	Envelope minBB = new Envelope(n.entries[0]);

	for (int i = 1; i < n.entryCount; i++)
	{
	    minBB.expandToInclude(n.entries[i]);
	}
	return minBB;
    }
}
