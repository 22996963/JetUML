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

import ca.mcgill.cs.jetuml.graph.Graph2;
import ca.mcgill.cs.jetuml.graph.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Basic services for drawing nodes.
 * 
 * @author Martin P. Robillard
 *
 */
public abstract class AbstractNodeView2 implements NodeView2
{
	public static final int SHADOW_GAP = 4;
	private static final Color SHADOW_COLOR = Color.LIGHTGRAY;
	
	private Node aNode;
	
	/**
	 * @param pNode The node to wrap.
	 */
	protected AbstractNodeView2(Node pNode)
	{
		aNode = pNode;
	}
	
	/**
	 * @return The wrapped edge.
	 */
	protected Node node()
	{
		return aNode;
	}
	
	@Override
	public void draw(GraphicsContext pGraphics)
	{
		Shape shape = getShape();
		Paint oldFill = pGraphics.getFill();
		pGraphics.translate(SHADOW_GAP, SHADOW_GAP);      
		pGraphics.setFill(SHADOW_COLOR);
		pGraphics.fill(shape);
		pGraphics.translate(-SHADOW_GAP, -SHADOW_GAP);
		pGraphics.setColor(pGraphics.getBackground());
		pGraphics.fill(shape);      
		pGraphics.setFill(oldFill);
	}
	
	/**
     *  @return the shape to be used for computing the drop shadow
    */
	protected abstract Shape getShape();

	@Override
	public void layout(Graph2 pGraph)
	{}
}
