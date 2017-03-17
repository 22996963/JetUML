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

package ca.mcgill.cs.stg.jetuml.graph;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;

import ca.mcgill.cs.stg.jetuml.framework.ArrowHead;
import ca.mcgill.cs.stg.jetuml.framework.Direction;

/**
 *  A curved edge for a state transition in a state diagram. The
 *  edge has two natures, either a self-edge, or a inter-node 
 *  edge.
 *  
 *  @author Martin P. Robillard New layout algorithms.
 */
public class StateTransitionEdge extends AbstractEdge
{
	private static final int DEGREES_5 = 5;
	private static final int DEGREES_10 = 10;
	private static final int DEGREES_60 = 60;
	private static final int DEGREES_270 = 270;
	private static final int SELF_EDGE_OFFSET = 15;
	private static JLabel label = new JLabel();
	private String aLabelText = "";
	
	/**
     * Sets the label property value.
     * @param pNewValue the new value
	 */
	public void setLabel(String pNewValue)
	{
		aLabelText = pNewValue;
	}

	/**
     * Gets the label property value.
     * @return the current value
	 */
	public String getLabel()
	{
		return aLabelText;
	}

	@Override
	public void draw(Graphics2D pGraphics2D)
	{
//		pGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		pGraphics2D.draw(getShape());
		drawLabel(pGraphics2D);
		if( isSelfEdge() )
		{
			Point2D connectionPoint2 = getSelfEdgeConnectionPoints().getP2();
			ArrowHead.V.draw(pGraphics2D, new Point2D.Double(connectionPoint2.getX()+SELF_EDGE_OFFSET, connectionPoint2.getY()-SELF_EDGE_OFFSET/4), 
					getConnectionPoints().getP2());
		}
		else
		{
			ArrowHead.V.draw(pGraphics2D, getControlPoint(), getConnectionPoints().getP2());
		}
	}
	
	@Override
	public Rectangle2D getBounds()
	{
		Rectangle2D bounds = super.getBounds();
		bounds.add(getLabelBounds());
		return bounds;
	}
	
	@Override
	public Line2D getConnectionPoints()
	{
		if(isSelfEdge())
		{
			return getSelfEdgeConnectionPoints();
		}
		else
		{
			return getNormalEdgeConnectionsPoints();
		}
	}
	
	@Override
	protected Shape getShape()
	{
		if( isSelfEdge() )
		{
			return getSelfEdgeShape();
		}
		else
		{
			return getNormalEdgeShape();
		}
	}
	

	/*
	 *  Draws the label.
	 *  @param pGraphics2D the graphics context
	 */
	private void drawLabel(Graphics2D pGraphics2D)
	{
		Rectangle2D labelBounds = getLabelBounds();
		double x = labelBounds.getX();
		double y = labelBounds.getY();
		pGraphics2D.translate(x, y);
		label.paint(pGraphics2D);
		pGraphics2D.translate(-x, -y);        
	}
	
	private Rectangle2D getLabelBounds()
	{
		if( isSelfEdge() )
		{
			return getSelfEdgeLabelBounds();
		}
		else
		{
			return getNormalEdgeLabelBounds();
		}
	}

	/*
     *  Gets the bounds of the label text .
     * @return the bounds of the label text
     */
	private Rectangle2D getNormalEdgeLabelBounds()
	{
		Line2D line = getConnectionPoints();
		Point2D control = getControlPoint();
		double x = control.getX() / 2 + line.getX1() / 4 + line.getX2() / 4;
		double y = control.getY() / 2 + line.getY1() / 4 + line.getY2() / 4;

		label.setText(toHtml(aLabelText));
		Dimension dimension = label.getPreferredSize();
		label.setBounds(0, 0, dimension.width, dimension.height);
   
		final int gap = 3;
		if( line.getY1() == line.getY2() )
		{
			x -= dimension.getWidth() / 2;
		}
		else if( line.getY1() <= line.getY2() )
		{
			x += gap;
		}
		else
		{
			x -= dimension.getWidth() + gap;
		}
		
		if( line.getX1() == line.getX2() )
		{
			y += dimension.getHeight() / 2;
		}
		else if( line.getX1() <= line.getX2() )
		{
			y -= dimension.getHeight() + gap;
		}
		else
		{
			y += gap;
		}
		return new Rectangle2D.Double(x, y, dimension.width, dimension.height);
   }   
	
	/*
     * Positions the label above the self edge, centered
     * in the middle of it.
     * @return the bounds of the label text
     */
	private Rectangle2D getSelfEdgeLabelBounds()
	{
		Line2D line = getConnectionPoints();
		label.setText(toHtml(aLabelText));
		Dimension dimension = label.getPreferredSize();
		label.setBounds(0, 0, dimension.width, dimension.height);
		return new Rectangle2D.Double(line.getX1() + SELF_EDGE_OFFSET - dimension.width/2,	
				line.getY1() - SELF_EDGE_OFFSET*2, dimension.width, dimension.height);
   }   

	/**
     *  Gets the control point for the quadratic spline.
     * @return the control point
     */
	private Point2D getControlPoint()
	{
		Line2D line = getConnectionPoints();
		double t = Math.tan(Math.toRadians(getAngle()));
		double dx = (line.getX2() - line.getX1()) / 2;
		double dy = (line.getY2() - line.getY1()) / 2;
		return new Point2D.Double((line.getX1() + line.getX2()) / 2 + t * dy, (line.getY1() + line.getY2()) / 2 - t * dx);         
	}
	
	private Shape getSelfEdgeShape()
	{
		Line2D line = getSelfEdgeConnectionPoints();
		return new Arc2D.Double(line.getX1(), line.getY1()-SELF_EDGE_OFFSET, SELF_EDGE_OFFSET*2, SELF_EDGE_OFFSET*2, 
				DEGREES_270, DEGREES_270, Arc2D.OPEN);
	}
	
	private Shape getNormalEdgeShape()
	{
		Line2D line = getConnectionPoints();
		Point2D control = getControlPoint();
		GeneralPath p = new GeneralPath();
		p.moveTo((float)line.getX1(), (float)line.getY1());
		p.quadTo((float)control.getX(), (float)control.getY(), (float)line.getX2(), (float)line.getY2());      
		return p;
	}
	
	private double getAngle()
	{
		if(getStart() == getEnd())
		{
			return DEGREES_60; 
		}
		else
		{
			return DEGREES_10;
		}
	}
	
	/*
	 * The connection points for the self-edge are an offset from the top-right
	 * corner.
	 */
	private Line2D getSelfEdgeConnectionPoints()
	{
		Point2D.Double point1 = new Point2D.Double(getStart().getBounds().getMaxX() - SELF_EDGE_OFFSET, 
				getStart().getBounds().getMinY());
		Point2D.Double point2 = new Point2D.Double(getStart().getBounds().getMaxX(), 
				getStart().getBounds().getMinY() + SELF_EDGE_OFFSET);
		return new Line2D.Double(point1, point2);
	}
	
	/*
	 * The connection points are a slight offset from the center.
	 * @return
	 */
	private Line2D getNormalEdgeConnectionsPoints()
	{
		Rectangle2D start = getStart().getBounds();
		Rectangle2D end = getEnd().getBounds();
		Point2D startCenter = new Point2D.Double(start.getCenterX(), start.getCenterY());
		Point2D endCenter = new Point2D.Double(end.getCenterX(), end.getCenterY());
		Direction d1 = new Direction(startCenter, endCenter).turn(-DEGREES_5);
		Direction d2 = new Direction(endCenter, startCenter).turn(DEGREES_5);
		return new Line2D.Double(getStart().getConnectionPoint(d1), getEnd().getConnectionPoint(d2));
	}
	
	private boolean isSelfEdge()
	{
		return getStart() == getEnd();
	}
}
