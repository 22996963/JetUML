/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2018 by the contributors of the JetUML project.
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
package ca.mcgill.cs.jetuml.views.nodes;

import java.util.ArrayList;
import java.util.List;

import ca.mcgill.cs.jetuml.diagrams.SequenceDiagramGraph2;
import ca.mcgill.cs.jetuml.geom.Conversions;
import ca.mcgill.cs.jetuml.geom.Direction;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.graph.Edge;
import ca.mcgill.cs.jetuml.graph.Graph2;
import ca.mcgill.cs.jetuml.graph.Node;
import ca.mcgill.cs.jetuml.graph.edges.CallEdge;
import ca.mcgill.cs.jetuml.graph.nodes.CallNode;
import ca.mcgill.cs.jetuml.graph.nodes.ImplicitParameterNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Line;

/**
 * An object to render a call in a Sequence diagram.
 * 
 * @author Martin P. Robillard
 *
 */
public class CallNodeView2 extends RectangleBoundedNodeView2
{
	private static final int DEFAULT_WIDTH = 16;
	private static final int DEFAULT_HEIGHT = 30;
//	private static final Stroke STROKE = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 5, 5 }, 0);
	
	/**
	 * @param pNode The node to wrap.
	 */
	public CallNodeView2(CallNode pNode)
	{
		super(pNode, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	private boolean openBottom()
	{
		return ((CallNode)node()).isOpenBottom();
	}
	
	private ImplicitParameterNode implicitParameter()
	{
		return (ImplicitParameterNode)((CallNode)node()).getParent();
	}
	
	@Override
	public void setBounds(Rectangle pNewBounds)
	{
		super.setBounds(pNewBounds);
	}
	
	@Override
	public void draw(GraphicsContext pGraphics)
	{
		System.out.println("draw call node called");
//		super.draw(pGraphics);
//		Color oldColor = pGraphics.getColor();
//		pGraphics.setColor(Color.WHITE);
//		pGraphics.fill(Conversions.toRectangle2D(getBounds()));
//		pGraphics.setColor(oldColor);
//		if(openBottom())
//		{
//			final Rectangle bounds = getBounds();
//			int x1 = bounds.getX();
//			int x2 = bounds.getMaxX();
//			int y1 = bounds.getY();
//			int y3 = bounds.getMaxY();
//			int y2 = y3 - CallNode.CALL_YGAP;
//			pGraphics.draw(new Line(x1, y1, x2, y1));
//			pGraphics.draw(new Line(x1, y1, x1, y2));
//			pGraphics.draw(new Line(x2, y1, x2, y2));
//			Stroke oldStroke = pGraphics.getStroke();
//			pGraphics.setStroke(STROKE);
//			pGraphics.draw(new Line2D.Double(x1, y2, x1, y3));
//			pGraphics.draw(new Line2D.Double(x2, y2, x2, y3));
//			pGraphics.setStroke(oldStroke);
//		}
//		else
//		{
//			pGraphics.draw(Conversions.toRectangle2D(getBounds()));
//		}
	}
	
	@Override
	public void layout(Graph2 pGraph)
	{
		assert implicitParameter() != null;
		assert pGraph instanceof SequenceDiagramGraph2;
		SequenceDiagramGraph2 graph = (SequenceDiagramGraph2) pGraph;

		// Shift the node to its proper place on the X axis.
		node().translate(computeMidX(pGraph) - getBounds().getCenter().getX(), 0);

		// Compute the Y coordinate of the bottom of the node
		int bottomY = computeBottomY(graph);

		final Rectangle bounds = getBounds();

		int minHeight = DEFAULT_HEIGHT;
		Edge returnEdge = graph.findEdge(node(), graph.getCaller(node()));
		if(returnEdge != null)
		{
			minHeight = Math.max(minHeight, returnEdge.view2().getBounds().getHeight());         
		}
		setBounds(new Rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), Math.max(minHeight, bottomY - bounds.getY())));
	}
	
	/*
	 * @return The X coordinate that should be the middle
	 * of this call node. Takes into account nested calls.
	 */
	private int computeMidX(Graph2 pGraph)
	{
		int xmid = implicitParameter().view2().getBounds().getCenter().getX();

		// Calculate a shift for each caller with the same implicit parameter
		for(CallNode node = ((SequenceDiagramGraph2)pGraph).getCaller(node()); node != null && node != node(); 
				node = ((SequenceDiagramGraph2)pGraph).getCaller(node))
		{
			if(((CallNode)node).getParent() == implicitParameter())
			{
				xmid += getBounds().getWidth() / 2;
			}
		}
		return xmid;
	}

	/*
	 * Compute the Y coordinate of the bottom of the CallNode. This 
	 * triggers the layout of all callee nodes.
	 */
	private int computeBottomY(SequenceDiagramGraph2 pGraph)
	{
		// Compute the Y coordinate of the bottom of the node
		int bottomY = getBounds().getY() + CallNode.CALL_YGAP;

		for(Node node : getCallees(pGraph))
		{
			if(node instanceof ImplicitParameterNode) // <<create>>
			{
				node.translate(0, bottomY - ((ImplicitParameterNode) node).getTopRectangle().getCenter().getY());
				bottomY += ((ImplicitParameterNode)node).getTopRectangle().getHeight() / 2 + CallNode.CALL_YGAP;
			}
			else if(node instanceof CallNode)
			{  
				Edge callEdge = pGraph.findEdge(node(), node);
				// compute height of call edge
				if(callEdge != null)
				{
					bottomY += callEdge.view2().getBounds().getHeight() - CallNode.CALL_YGAP;
				}

				node.translate(0, bottomY - node.view2().getBounds().getY());
				node.view2().layout(pGraph);
				if(((CallNode) node).isSignaled(pGraph))
				{
					bottomY += CallNode.CALL_YGAP;
				}
				else
				{
					bottomY += node.view2().getBounds().getHeight() + CallNode.CALL_YGAP;
				}
			}
		}
		if(openBottom())
		{
			bottomY += 2 * CallNode.CALL_YGAP;
		}
		return bottomY;
	}
	
	/*
	 * @param pGraph
	 * @return All the nodes (CallNodes or ImplicitParameterNodes) that have a calledge
	 * originating at this CallNode. If an ImplicitParameterNode is in the list, it's always
	 * returned first.
	 */
	private List<Node> getCallees(Graph2 pGraph)
	{
		List<Node> callees = new ArrayList<>();
		for( Edge edge : pGraph.getEdges())
		{
			if( edge.getStart() == node() && edge instanceof CallEdge )
			{
				if( edge.getEnd() instanceof ImplicitParameterNode )
				{
					callees.add(0, edge.getEnd());
				}
				else
				{
					callees.add(edge.getEnd());
				}
			}
		}
		return callees;
	}

	@Override
	public Point getConnectionPoint(Direction pDirection)
	{
		if(pDirection.getX() > 0)
		{
			return new Point(getBounds().getMaxX(), getBounds().getY());
		}
		else
		{
			return new Point(getBounds().getX(), getBounds().getY());
		}
	}
}
