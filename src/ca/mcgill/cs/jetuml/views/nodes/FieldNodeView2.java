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

import ca.mcgill.cs.jetuml.geom.Direction;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.graph.Graph2;
import ca.mcgill.cs.jetuml.graph.nodes.FieldNode;
import ca.mcgill.cs.jetuml.views.StringViewer;
import javafx.scene.canvas.GraphicsContext;


//TODO: TO BE COMPLETED


/**
 * An object to render a FieldNode.
 * 
 * @author Martin P. Robillard
 *
 */
public class FieldNodeView2 extends RectangleBoundedNodeView2
{
	private static final String EQUALS = " = ";
	private static final int DEFAULT_WIDTH = 60;
	private static final int DEFAULT_HEIGHT = 20;
	private static final StringViewer VALUE_VIEWER = new StringViewer(StringViewer.Align.RIGHT, false, false);
	private static final StringViewer NAME_VIEWER = new StringViewer(StringViewer.Align.RIGHT, false, false);
	private static final StringViewer EQUALS_VIEWER = new StringViewer(StringViewer.Align.RIGHT, false, false);
	
	/**
	 * @param pNode The node to wrap.
	 */
	public FieldNodeView2(FieldNode pNode)
	{
		super(pNode, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	private String name()
	{
		return ((FieldNode)node()).getName();
	}
	
	private String value()
	{
		return ((FieldNode)node()).getValue();
	}
	
	/**
	 * @param pNewBounds The new bounds for this node.
	 */
	public void setBounds(Rectangle pNewBounds)
	{
		super.setBounds(pNewBounds);
	}
	
	@Override
	public void draw(GraphicsContext pGraphics) {}
	
	private int leftWidth()
	{
		return NAME_VIEWER.getBounds(name()).getWidth();
	}
	
	private int midWidth()
	{
		return EQUALS_VIEWER.getBounds(EQUALS).getWidth();
	}
	
	private int rightWidth()
	{
		int rightWidth = VALUE_VIEWER.getBounds(value()).getWidth();
		if(rightWidth == 0)
		{
			rightWidth = DEFAULT_WIDTH / 2;
		}
		return rightWidth;
	}
	
	@Override
	public void layout(Graph2 pGraph)
	{
		final int width = leftWidth() + midWidth() + rightWidth();
		final int height = Math.max(NAME_VIEWER.getBounds(name()).getHeight(), 
				Math.max(VALUE_VIEWER.getBounds(value()).getHeight(), EQUALS_VIEWER.getBounds(EQUALS).getHeight()));
		final Rectangle bounds = getBounds();
		setBounds(new Rectangle(bounds.getX(), bounds.getY(), width, height));
	}
	
	@Override
	public Point getConnectionPoint(Direction pDirection)
	{
		Rectangle bounds = getBounds();
		return new Point((bounds.getMaxX() + bounds.getX() + getAxis()) / 2, bounds.getCenter().getY());
	}
	
	/**
	 * @return The axis.
	 */
	public int getAxis()
	{
		return leftWidth() + midWidth() / 2;
	}
}
