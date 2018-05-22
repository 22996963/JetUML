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

import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.geom.Direction;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import javafx.scene.canvas.GraphicsContext;

/**
 * Services to query the visual properties of a node.
 */
public interface NodeView
{
	/**
     * Gets the smallest rectangle that bounds this node.
     * The bounding rectangle contains all labels.
     * @return the bounding rectangle
   	 */
	Rectangle getBounds();
	
	/**
     *  Draw the node.
     * @param pGraphics the graphics context
     */
	void draw(GraphicsContext pGraphics);

	/**
     * Tests whether the node contains a point.
     * @param pPoint the point to test
     * @return true if this node contains aPoint
     */
	boolean contains(Point pPoint);

	/**
     * Get the best connection point to connect this node 
     * with another node. This should be a point on the boundary
     * of the shape of this node.
     * @param pDirection the direction from the center 
     * of the bounding rectangle towards the boundary 
     * @return the recommended connection point
	 */
	Point getConnectionPoint(Direction pDirection);

	/**
     * Lays out the node and its children.
     * @param pGraph the ambient graph
	 */
	void layout(Diagram pGraph);
}
