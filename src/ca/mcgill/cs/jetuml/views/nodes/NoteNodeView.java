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
import ca.mcgill.cs.jetuml.diagram.nodes.NoteNode;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.views.Grid;
import ca.mcgill.cs.jetuml.views.StringViewer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * An object to render a NoteNode.
 */
public class NoteNodeView extends RectangleBoundedNodeView
{
	private static final int DEFAULT_WIDTH = 60;
	private static final int DEFAULT_HEIGHT = 40;
	private static final int FOLD_X = 8;
	private static final int FOLD_Y = 8;
	private static final Color DEFAULT_COLOR = Color.color(0.9f, 0.9f, 0.6f); // Pale yellow
	private static final StringViewer NOTE_VIEWER = new StringViewer(StringViewer.Align.LEFT, false, false);
	
	/**
	 * @param pNode The node to wrap.
	 */
	public NoteNodeView(NoteNode pNode)
	{
		super(pNode, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	private String name()
	{
		return ((NoteNode)node()).getName();
	}
	
	@Override
	public void draw(GraphicsContext pGraphics)
	{
		super.draw(pGraphics);
		double oldLineWidth = pGraphics.getLineWidth();
		pGraphics.setLineWidth(LINE_WIDTH);
		fillFold(pGraphics);   
		pGraphics.setLineWidth(oldLineWidth);
      
		NOTE_VIEWER.draw(name(), pGraphics, getDefaultBounds());
	}
	
	@Override
	public void fillShape(GraphicsContext pGraphics) 
	{
		Rectangle bounds = getBounds();		
		pGraphics.beginPath();
		pGraphics.setFill(DEFAULT_COLOR);
		pGraphics.moveTo((float)bounds.getX(), (float)bounds.getY());
		pGraphics.lineTo((float)(bounds.getMaxX() - FOLD_X), (float)bounds.getY());
		pGraphics.lineTo((float)bounds.getMaxX(), (float)(bounds.getY() + FOLD_Y));
		pGraphics.lineTo((float)bounds.getMaxX(), (float)bounds.getMaxY());
		pGraphics.lineTo((float)bounds.getX(), (float)bounds.getMaxY());
		pGraphics.closePath();
		pGraphics.fill();
		pGraphics.stroke();
	}
	
	/**
	 * Fills in note fold.
	 * @param pGraphics GraphicsContext in which to fill the fold
	 */
	public void fillFold(GraphicsContext pGraphics)
	{
		final Rectangle bounds = getBounds();
		pGraphics.beginPath();
		pGraphics.setFill(Color.WHITE);
		pGraphics.moveTo((float)(bounds.getMaxX() - FOLD_X), (float)bounds.getY());
		pGraphics.lineTo((float)bounds.getMaxX() - FOLD_X, (float)bounds.getY() + FOLD_X);
		pGraphics.lineTo((float)bounds.getMaxX(), (float)(bounds.getY() + FOLD_Y));
		pGraphics.closePath();
		pGraphics.fill();
		pGraphics.stroke();
	}
	
	/**
	 * Gets the smallest rectangle that bounds this node when not containing text.
	 * @return the bounding rectangle
	 */
	public Rectangle getDefaultBounds()
	{
		return new Rectangle(node().position().getX(), node().position().getY(), DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	@Override
	public Rectangle getBounds()
	{
		Rectangle textBounds = NOTE_VIEWER.getBounds(name()); 
		return new Rectangle(node().position().getX(), node().position().getY(), 
				Math.max(textBounds.getWidth(), DEFAULT_WIDTH), Math.max(textBounds.getHeight(), DEFAULT_HEIGHT));
	}
	
	@Override
	public void layout(Diagram pGraph)
	{
		setBounds(Grid.snapped(getBounds()));
	}
}
