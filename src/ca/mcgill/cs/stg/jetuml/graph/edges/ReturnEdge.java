/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2016 by the contributors of the JetUML project.
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

package ca.mcgill.cs.stg.jetuml.graph.edges;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import ca.mcgill.cs.stg.jetuml.framework.ArrowHead;
import ca.mcgill.cs.stg.jetuml.framework.LineStyle;
import ca.mcgill.cs.stg.jetuml.framework.SegmentationStyle;
import ca.mcgill.cs.stg.jetuml.geom.Rectangle;
import ca.mcgill.cs.stg.jetuml.graph.Graph;
import ca.mcgill.cs.stg.jetuml.graph.edges.views.SegmentedEdgeView;
import ca.mcgill.cs.stg.jetuml.graph.nodes.Node;
import ca.mcgill.cs.stg.jetuml.graph.nodes.PointNode;

/**
 *  An edge that joins two call nodes.
 */
public class ReturnEdge extends SingleLabelEdge
{
	/**
	 * Constructs a standard return edge.
	 */
	public ReturnEdge()
	{
		aView = new SegmentedEdgeView(this, createSegmentationStyle(), LineStyle.DOTTED,
				() -> ArrowHead.NONE, ()->ArrowHead.V, ()->"", ()->getMiddleLabel(), ()->"");
	}
	
	private SegmentationStyle createSegmentationStyle()
	{
		return new SegmentationStyle()
		{
			@Override
			public boolean isPossible(Edge pEdge)
			{
				assert false; // Should not be called.
				return false;
			}

			@Override
			public Point2D[] getPath(Edge pEdge, Graph pGraph)
			{
				return getPoints(pEdge);
			}

			@Override
			public Side getAttachedSide(Edge pEdge, Node pNode)
			{
				assert false; // Should not be called
				return null;
			}
		};
	}
	
	private static Point2D[] getPoints(Edge pEdge)
	{
		ArrayList<Point2D> lReturn = new ArrayList<>();
		Rectangle start = pEdge.getStart().getBounds();
		Rectangle end = pEdge.getEnd().getBounds();
		if(pEdge.getEnd() instanceof PointNode) // show nicely in tool bar
		{
			lReturn.add(new Point2D.Double(end.getX(), end.getY()));
			lReturn.add(new Point2D.Double(start.getMaxX(), end.getY()));
		}      
		else if(start.getCenter().getX() < end.getCenter().getX())
		{
			lReturn.add(new Point2D.Double(start.getMaxX(), start.getMaxY()));
			lReturn.add(new Point2D.Double(end.getX(), start.getMaxY()));
		}
		else
		{
			lReturn.add(new Point2D.Double(start.getX(), start.getMaxY()));
			lReturn.add(new Point2D.Double(end.getMaxX(), start.getMaxY()));
		}
		return lReturn.toArray(new Point2D[lReturn.size()]);
	}
}
