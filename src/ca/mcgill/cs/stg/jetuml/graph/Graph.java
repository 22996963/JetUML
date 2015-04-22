/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2015 Cay S. Horstmann and the contributors of the 
 * JetUML project.
 *
 * See: https://github.com/prmr/JetUML
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package ca.mcgill.cs.stg.jetuml.graph;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Statement;
import java.util.ArrayList;
import java.util.Collection;

import ca.mcgill.cs.stg.jetuml.framework.GraphModificationListener;
import ca.mcgill.cs.stg.jetuml.framework.Grid;

/**
 *  A graph consisting of selectable nodes and edges.
 */
public abstract class Graph
{
	protected GraphModificationListener aModListener;
	private ArrayList<Node> aNodes;
	private ArrayList<Edge> aEdges;
	private transient ArrayList<Node> aNodesToBeRemoved;
	private transient ArrayList<Edge> aEdgesToBeRemoved;
	private transient boolean aNeedsLayout;
	private transient Rectangle2D aMinBounds;

	/**
	 * Constructs a graph with no nodes or edges.
	 */
	public Graph()
	{
		aNodes = new ArrayList<>();
		aEdges = new ArrayList<>();
		aNodesToBeRemoved = new ArrayList<>();
		aEdgesToBeRemoved = new ArrayList<>();
		aModListener = new GraphModificationListener();
		aNeedsLayout = true;
	}

	/**
	 * Adds the modification listener.
	 * @param pModListener the GraphModificationListener for this Graph.
	 */
	public void addModificationListener(GraphModificationListener pModListener)
	{
		aModListener = pModListener;
	}

	/**
	 * @return The file extension (including the dot) corresponding
	 * to files of this diagram type.
	 */
	public abstract String getFileExtension();

	/**
	 * @return A short description of this diagram, usually
	 * ending in "Diagram", e.g., "State Diagram".
	 */
	public abstract String getDescription();

	/**
	 * Adds an edge to the graph that joins the nodes containing
	 * the given points. If the points aren't both inside nodes,
	 * then no edge is added.
	 * @param pEdge the edge to add
	 * @param pPoint1 a point in the starting node
	 * @param pPoint2 a point in the ending node
	 * @return true if the edge was connected
	 */
	public boolean connect(Edge pEdge, Point2D pPoint1, Point2D pPoint2)
	{
		Node n1 = findNode(pPoint1);
		Node n2 = findNode(pPoint2);
		if(n1 != null)
		{
			if(!noteEdgeCheck(pEdge, n1, n2))
			{
				return false;
			}

			pEdge.connect(n1, n2);
			boolean edgeAdded;
			if (n1 instanceof FieldNode)
			{
				aModListener.startCompoundListening();
				aModListener.trackPropertyChange(this, n1);
			}
			edgeAdded = n1.addEdge(pEdge, pPoint1, pPoint2);
			if (n1 instanceof FieldNode)
			{
				aModListener.finishPropertyChange(this,  n1);
			}
			if(edgeAdded && pEdge.getEnd() != null)
			{
				aEdges.add(pEdge);
				aModListener.edgeAdded(this, pEdge);
				if(!aNodes.contains(pEdge.getEnd()))
				{
					aNodes.add(pEdge.getEnd());
				}
				aNeedsLayout = true;
				if (n1 instanceof FieldNode)
				{
					aModListener.endCompoundListening();
				}
				return true;
			}
			else if (n1 instanceof FieldNode)
			{
				aModListener.endCompoundListening();
			}
		}
		return false;
	}

	/**
	 * Adds a node to the graph so that the top left corner of
	 * the bounding rectangle is at the given point.
	 * @param pNode the node to add
	 * @param pPoint the desired location
	 * @return True if the node was added.
	 */
	public boolean add(Node pNode, Point2D pPoint)
	{
		Rectangle2D bounds = pNode.getBounds();
		pNode.translate(pPoint.getX() - bounds.getX(), pPoint.getY() - bounds.getY()); 

		boolean accepted = false;
		/* A variable commented out during testing. @JoelChev */
		//boolean insideANode = false;
		for(int i = aNodes.size() - 1; i >= 0 && !accepted; i--)
		{
			Node parent = aNodes.get(i);

			if (parent.contains(pPoint) && parent.addNode(pNode, pPoint))
			{
				//insideANode = true;
				aModListener.childAttached(this, parent.getChildren().indexOf(pNode), parent, pNode);
				accepted = true;
			}	
		}
		//		if(insideANode && !accepted)
		//		{
		//			System.out.println("FALSE!");
		//			return false;
		//		}
		aModListener.nodeAdded(this, pNode);
		aNodes.add(pNode);
		aNeedsLayout = true;
		return true;
	}

	/**
      Finds a node containing the given point.
      @param pPoint a point
      @return a node containing p or null if no nodes contain p
	 */
	public Node findNode(Point2D pPoint)
	{
		for(int i = aNodes.size() - 1; i >= 0; i--)
		{
			Node n = aNodes.get(i);
			if(n.contains(pPoint))
			{
				return n;
			}
		}
		return null;
	}

	/**
	 * Finds an edge containing the given point.
	 * @param pPoint a point
	 * @return an edge containing p or null if no edges contain p
	 */
	public Edge findEdge(Point2D pPoint)
	{
		for (int i = aEdges.size() - 1; i >= 0; i--)
		{
			Edge e = aEdges.get(i);
			if(e.contains(pPoint))
			{
				return e;
			}
		}
		return null;
	}

	/**
	 * Returns all edges connected to the given node.
	 * @param pNode The Node to query for Edges.
	 * @return an ArrayList of Edges from the Node pNode.
	 */
	public ArrayList<Edge> getNodeEdges(Node pNode)
	{
		ArrayList<Edge> toRet = new ArrayList<Edge>();
		for(int i = 0; i < aEdges.size(); i++)
		{
			Edge e = aEdges.get(i);
			if((e.getStart() == pNode || e.getEnd() == pNode) && !aEdgesToBeRemoved.contains(e))
			{
				toRet.add(e);
			}
		}
		return toRet;
	}

	/**
	 * Draws the graph.
	 * @param pGraphics2D the graphics context
	 * @param pGrid The grid
	 */
	public void draw(Graphics2D pGraphics2D, Grid pGrid)
	{
		layout(pGraphics2D, pGrid);

		for(int i = 0; i < aNodes.size(); i++)
		{
			Node n = aNodes.get(i);
			n.draw(pGraphics2D);
		}

		for(int i = 0; i < aEdges.size(); i++)
		{
			Edge e = aEdges.get(i);
			e.draw(pGraphics2D);
		}
	}

	/**
	 * Removes a node and all edges that start or end with that node.
	 * @param pNode the node to remove
	 */
	public void removeNode(Node pNode)
	{
		if(aNodesToBeRemoved.contains(pNode))
		{
			return;
		}
		aModListener.startCompoundListening();
		aNodesToBeRemoved.add(pNode);
		// notify nodes of removals
		for(int i = 0; i < aNodes.size(); i++)
		{
			Node n2 = aNodes.get(i);
			if(n2.getParent()!= null && n2.getParent().equals(this))
			{
				aModListener.childDetached(this, pNode.getChildren().indexOf(n2), pNode, n2);
			}
			n2.removeNode(this, pNode);
		}
		for(int i = 0; i < aEdges.size(); i++)
		{
			Edge e = aEdges.get(i);
			if(e.getStart() == pNode || e.getEnd() == pNode)
			{
				removeEdge(e);
			}
		}
		/*Remove the children too @JoelChev*/
		for(Node childNode: pNode.getChildren())
		{
			removeNode(childNode);
		}
		aModListener.nodeRemoved(this, pNode);
		aModListener.endCompoundListening();
		aNeedsLayout = true;
	}

	/**
	 * Removes pElement from the graph.
	 * @param pElement The element to remove.
	 */
	public void removeElement(GraphElement pElement)
	{
		if(pElement instanceof Node)
		{
			removeNode((Node) pElement);
		}
		else if(pElement instanceof Edge)
		{

			removeEdge((Edge) pElement);
		}
	}

	/**
	 * @param pElement The element we want to check is in the grapgh.
	 * @return True if pElement is a node or edge in this graph.
	 */
	public boolean contains( GraphElement pElement )
	{
		if( aNodes.contains(pElement))
		{
			return true;
		}
		if( aEdges.contains( pElement ))
		{
			return true;
		}
		return false;
	}

	/**
	 * Removes an edge from the graph.
	 * @param pEdge the edge to remove
	 */
	public void removeEdge(Edge pEdge)
	{
		if (aEdgesToBeRemoved.contains(pEdge))
		{
			return;
		}
		aEdgesToBeRemoved.add(pEdge);
		aModListener.edgeRemoved(this, pEdge);
		for(int i = aNodes.size() - 1; i >= 0; i--)
		{
			Node n = aNodes.get(i);
			if(n.removeEdge(this, pEdge))
			{
				break;
			}
		}
		aNeedsLayout = true;
	}

	/**
	 * Causes the layout of the graph to be recomputed.
	 */
	public void layout()
	{
		aNeedsLayout = true;
	}

	/**
	 * Computes the layout of the graph.
	 * If you override this method, you must first call 
	 * <code>super.layout</code>.
	 * @param pGraphics2D the graphics context
	 * @param pGrid the grid to snap to
	 */
	protected void layout(Graphics2D pGraphics2D, Grid pGrid)
	{
		if(!aNeedsLayout)
		{
			return;
		}
		aNodes.removeAll(aNodesToBeRemoved);
		aEdges.removeAll(aEdgesToBeRemoved);
		aNodesToBeRemoved.clear();
		aEdgesToBeRemoved.clear();

		for(int i = 0; i < aNodes.size(); i++)
		{
			Node n = aNodes.get(i);
			n.layout(this, pGraphics2D, pGrid);
		}
		aNeedsLayout = false;
	}

	/**
	 * Gets the smallest rectangle enclosing the graph.
	 * @return the bounding rectangle
	 */
	public Rectangle2D getBounds()
	{
		Rectangle2D r = aMinBounds;
		for(int i = 0; i < aNodes.size(); i++)
		{
			Node n = aNodes.get(i);
			Rectangle2D b = n.getBounds();
			if(r == null)
			{
				r = b;
			}
			else
			{
				r.add(b);
			}
		}
		for(int i = 0; i < aEdges.size(); i++)
		{
			Edge e = aEdges.get(i);
			r.add(e.getBounds());
		}
		if(r == null )
		{
			return new Rectangle2D.Double();
		}
		else
		{
			return new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth() + AbstractNode.SHADOW_GAP, r.getHeight() + AbstractNode.SHADOW_GAP);
		}
	}

	/**
	 * @return The minimum bounds property
	 */
	public Rectangle2D getMinBounds() 
	{ return aMinBounds; }

	/**
	 * @param pMinBounds The minimum bounds property
	 */
	public void setMinBounds(Rectangle2D pMinBounds)
	{ aMinBounds = pMinBounds; }

	/**
	 * Gets the node types of a particular graph type.
	 * @return an array of node prototypes
	 */   
	public abstract Node[] getNodePrototypes();

	/**
	 * Gets the edge types of a particular graph type.
	 * @return an array of edge prototypes
	 */   
	public abstract Edge[] getEdgePrototypes();

	/**
	 * Adds a persistence delegate to a given encoder that
	 * encodes the child nodes of this node.
	 * @param pEncoder the encoder to which to add the delegate
	 */
	public static void setPersistenceDelegate(Encoder pEncoder)
	{
		pEncoder.setPersistenceDelegate(Graph.class, new DefaultPersistenceDelegate()
		{
			protected void initialize(Class<?> pType, Object pOldInstance, Object pNewInstance, Encoder pOut) 
			{
				super.initialize(pType, pOldInstance, pNewInstance, pOut);
				Graph g = (Graph)pOldInstance;

				for(int i = 0; i < g.aNodes.size(); i++)
				{
					Node n = g.aNodes.get(i);
					Rectangle2D bounds = n.getBounds();
					Point2D p = new Point2D.Double(bounds.getX(), bounds.getY());
					pOut.writeStatement( new Statement(pOldInstance, "addNode", new Object[]{ n, p }) );
				}
				for(int i = 0; i < g.aEdges.size(); i++)
				{
					Edge e = g.aEdges.get(i);
					pOut.writeStatement( new Statement(pOldInstance, "connect", new Object[]{ e, e.getStart(), e.getEnd() }) );            
				}
			}
		});
	}

	/**
	 * Gets the nodes of this graph.
	 * @return an unmodifiable collection of the nodes
	 */
	public Collection<Node> getNodes()
	{ return aNodes; }

	/**
	 * Gets the edges of this graph.
	 * @return an unmodifiable collection of the edges
	 */
	public Collection<Edge> getEdges() 
	{ return aEdges; }

	/**
	 * Adds a node to this graph. This method should
	 * only be called by a decoder when reading a data file.
	 * @param pNode the node to add
	 * @param pPoint the desired location
	 */
	public void addNode(Node pNode, Point2D pPoint)
	{
		Rectangle2D bounds = pNode.getBounds();
		pNode.translate(pPoint.getX() - bounds.getX(), pPoint.getY() - bounds.getY()); 
		aNodes.add(pNode); 
	}

	/**
	 * Adds an edge to this graph. This method should
	 * only be called by a decoder when reading a data file.
	 * @param pEdge the edge to add
	 * @param pStart the start node of the edge
	 * @param pEnd the end node of the edge
	 */
	public void connect(Edge pEdge, Node pStart, Node pEnd)
	{
		pEdge.connect(pStart, pEnd);
		aEdges.add(pEdge);
	}

	/**
	 * Checks whether edges related to note nodes are acceptable.
	 * @param pEdge The edge to be added
	 * @param pNode1 The first node
	 * @param pNode2 The second node
	 * @return True if the edge is acceptable
	 */
	public boolean noteEdgeCheck(Edge pEdge, Node pNode1, Node pNode2)
	{
		if(pNode1 instanceof NoteNode && !(pEdge instanceof NoteEdge))
		{
			return false;
		}
		if(pNode2!=null && pNode2 instanceof NoteNode && !(pEdge instanceof NoteEdge))
		{
			return false;
		}
		return true;
	}
}


