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

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import ca.mcgill.cs.jetuml.geom.Direction;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.graph.Node;

/**
 * An object to render a PointNode.
 * 
 * @author Martin P. Robillard
 *
 */
public class PointNodeView2 extends AbstractNodeView2
{
	private static final int SELECTION_DISTANCE = 5;
	
	/**
	 * @param pNode The node to wrap.
	 */
	public PointNodeView2(Node pNode)
	{
		super(pNode);
	}
	
	@Override
	public Rectangle getBounds()
	{
		return new Rectangle(node().position().getX(), node().position().getY(), 0, 0);
	}

	@Override
	public boolean contains(Point pPoint)
	{
		return node().position().distance(pPoint) < SELECTION_DISTANCE;
	}

	@Override
	public Point getConnectionPoint(Direction pDirection)
	{
		return node().position();
	}

	@Override
	protected Shape getShape()
	{
		return new Rectangle2D.Double(node().position().getX(), node().position().getY(), 0, 0);
	}

}
