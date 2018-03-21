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
package ca.mcgill.cs.jetuml.views.edges;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import ca.mcgill.cs.jetuml.geom.Conversions2;
import ca.mcgill.cs.jetuml.geom.Direction;
import ca.mcgill.cs.jetuml.geom.Line;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.graph.Edge;
import ca.mcgill.cs.jetuml.graph.edges.StateTransitionEdge;
import ca.mcgill.cs.jetuml.views.ArrowHead;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * An edge view specialized for state transitions.
 * 
 * @author Martin P. Robillard
 * @author Kaylee I. Kutschera - Migration to JavaFX
 */
public class StateTransitionEdgeView2 extends AbstractEdgeView2
{
	private static final int SELF_EDGE_OFFSET = 15;
	private static final int DEGREES_5 = 5;
	private static final int DEGREES_10 = 10;
	private static final int DEGREES_20 = 20;
	private static final int DEGREES_270 = 270;
	
	private static final int RADIANS_TO_PIXELS = 10;
	private static final double HEIGHT_RATIO = 3.5;
	private static final int MAX_LENGTH_FOR_NORMAL_FONT = 15;
	private static final int MIN_FONT_SIZE = 9;
	
	// The amount of vertical difference in connection points to tolerate
	// before centering the edge label on one side instead of in the center.
	private static final int VERTICAL_TOLERANCE = 20; 

	private Font aFont = Font.getDefault();
	private String aLabel;
	
	/**
	 * @param pEdge The edge to wrap.
	 */
	public StateTransitionEdgeView2(StateTransitionEdge pEdge)
	{
		super(pEdge);
		aLabel = ((StateTransitionEdge) edge()).getMiddleLabel();
	}
	
	@Override
	public void draw(GraphicsContext pGraphics)
	{
		Paint oldStroke = pGraphics.getStroke();
		StrokeLineCap oldCap = pGraphics.getLineCap();
		StrokeLineJoin oldJoin = pGraphics.getLineJoin();
		double oldMiter = pGraphics.getMiterLimit();
		double[] oldDashes = pGraphics.getLineDashes();
		if (isSelfEdge())
		{
			pGraphics.setStroke(Color.BLACK);
			drawSelfEdge(pGraphics);
		}
		else 
		{
			pGraphics.beginPath();
			pGraphics.setStroke(Color.BLACK);
			completeDrawPath(pGraphics, (Path) getShape());
		}
		drawLabel(pGraphics);
		drawArrowHead(pGraphics);
		
		pGraphics.setStroke(oldStroke);
		pGraphics.setLineCap(oldCap);
		pGraphics.setLineJoin(oldJoin);
		pGraphics.setMiterLimit(oldMiter);
		pGraphics.setLineDashes(oldDashes);
	}
	
	private void drawArrowHead(GraphicsContext pGraphics)
	{
		if( isSelfEdge() )
		{
			Point connectionPoint2 = getSelfEdgeConnectionPoints().getPoint2();
			if( getPosition() == 1 )
			{
				ArrowHead.V.view2().draw(pGraphics, new Point2D(connectionPoint2.getX()+SELF_EDGE_OFFSET, 
						connectionPoint2.getY()-SELF_EDGE_OFFSET/4), Conversions2.toPoint2D(getConnectionPoints().getPoint2()));
			}
			else
			{
				ArrowHead.V.view2().draw(pGraphics, new Point2D(connectionPoint2.getX()-SELF_EDGE_OFFSET/4, 
						connectionPoint2.getY()-SELF_EDGE_OFFSET), Conversions2.toPoint2D(getConnectionPoints().getPoint2()));
			}
		}
		else
		{
			ArrowHead.V.view2().draw(pGraphics, getControlPoint(), Conversions2.toPoint2D(getConnectionPoints().getPoint2()));
		}
	}
	
	/*
	 *  Draws the label.
	 *  @param pGraphics2D the graphics context
	 */
	private void drawLabel(GraphicsContext pGraphics)
	{
		aLabel = ((StateTransitionEdge) edge()).getMiddleLabel();
		adjustLabelFont();
		Rectangle2D labelBounds = getLabelBounds();
		double x = labelBounds.getMinX();
		double y = labelBounds.getMinY();
		
		Paint oldFill = pGraphics.getFill();
		Font oldFont = pGraphics.getFont();
		pGraphics.translate(x, y);
		pGraphics.setFill(Color.BLACK);
		pGraphics.setFont(aFont);
		pGraphics.setTextAlign(TextAlignment.CENTER);
		pGraphics.fillText(aLabel, labelBounds.getWidth()/2, 0);
		pGraphics.setFill(oldFill);
		pGraphics.setFont(oldFont);
		pGraphics.translate(-x, -y);        
	}
	
	private void drawSelfEdge(GraphicsContext pGraphics)
	{
		Arc arc = (Arc) getShape();
			pGraphics.strokeArc(arc.getCenterX(), arc.getCenterY(), arc.getRadiusX(), arc.getRadiusY(), arc.getStartAngle(), 
					arc.getLength(), arc.getType());
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
		Line line = getConnectionPoints();
		Point2D control = getControlPoint();
		double x = control.getX() / 2 + line.getX1() / 4 + line.getX2() / 4;
		double y = control.getY() / 2 + line.getY1() / 4 + line.getY2() / 4;

		adjustLabelFont();
		Rectangle dimension = getLabelBounds(aLabel);

		int gap = 3;
		if( line.getY1() >= line.getY2() - VERTICAL_TOLERANCE && 
				line.getY1() <= line.getY2() + VERTICAL_TOLERANCE ) 
		{
			// The label is centered if the edge is (mostly) horizontal
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
		
		if( line.getX1() <= line.getX2() )
		{
			y -= dimension.getHeight() + gap;
		}
		else
		{
			y += gap;
		}
		
		// Additional gap to make sure the labels don't overlap
		if( edge().getGraph() != null && getPosition() > 1 )
		{
			double delta = Math.abs(Math.atan2(line.getX2()-line.getX1(), line.getY2()-line.getY1()));
			delta = dimension.getHeight() - delta*RADIANS_TO_PIXELS;
			if( line.getX1() <= line.getX2() )
			{
				y -= delta;
			}
			else
			{
				y += delta;
			}
		}
		return new Rectangle2D(x, y, dimension.getWidth(), dimension.getHeight());
}   
	
	/*
  * Positions the label above the self edge, centered
  * in the middle of it.
  * @return the bounds of the label text
  */
	private Rectangle2D getSelfEdgeLabelBounds()
	{
		Line line = getConnectionPoints();
		adjustLabelFont();
		Rectangle dimension = getLabelBounds(aLabel);
		if( getPosition() == 1 )
		{
			return new Rectangle2D(line.getX1() + SELF_EDGE_OFFSET - dimension.getWidth()/2,	
					line.getY1() - SELF_EDGE_OFFSET*2, dimension.getWidth(), dimension.getHeight());
		}
		else
		{
			return new Rectangle2D(line.getX1() - dimension.getWidth()/2,	
					line.getY1() - SELF_EDGE_OFFSET * HEIGHT_RATIO, dimension.getWidth(), dimension.getHeight());
		}
	}   
	
	/**
     * Gets the bounding rectangle for pString.
     * @param pString The input string. Cannot be null.
     * @return the bounding rectangle (with top left corner (0,0))
	 */
	public Rectangle getLabelBounds(String pString)
	{
		if(pString == null || pString.length() == 0) 
		{
			return new Rectangle(0, 0, 0, 0);
		}
		FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
		FontMetrics fontMetrics = fontLoader.getFontMetrics(aFont);
		int width = (int) Math.round(fontLoader.computeStringWidth(pString, aFont));
		int height = (int) Math.round(fontMetrics.getLineHeight());
		return new Rectangle(0, 0, width, height);
	}
	
	private void adjustLabelFont()
	{
		if(((StateTransitionEdge) edge()).getMiddleLabel().length() > MAX_LENGTH_FOR_NORMAL_FONT)
		{
			float difference = ((StateTransitionEdge) edge()).getMiddleLabel().length() - MAX_LENGTH_FOR_NORMAL_FONT;
			difference = difference / (2*((StateTransitionEdge) edge()).getMiddleLabel().length()); // damping
			double newFontSize = Math.max(MIN_FONT_SIZE, (1-difference) * aFont.getSize());
			aFont = new Font(aFont.getName(), newFontSize);
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
	
	private boolean isSelfEdge()
	{
		return edge().getStart() == edge().getEnd();
	}
	
	private Shape getSelfEdgeShape()
	{
		Line line = getSelfEdgeConnectionPoints();
		Arc arc = new Arc();
		if( getPosition() == 1 )
		{
			arc.setCenterX(line.getX1());
			arc.setCenterY(line.getY1()-SELF_EDGE_OFFSET);
			arc.setRadiusX(SELF_EDGE_OFFSET*2);
			arc.setRadiusY(SELF_EDGE_OFFSET*2);
			arc.setStartAngle(DEGREES_270);
			arc.setLength(DEGREES_270);
			arc.setType(ArcType.OPEN);
		}
		else
		{		
			arc.setCenterX(line.getX1()-SELF_EDGE_OFFSET);
			arc.setCenterY(line.getY1()-SELF_EDGE_OFFSET*2);
			arc.setRadiusX(SELF_EDGE_OFFSET*2);
			arc.setRadiusY(SELF_EDGE_OFFSET*2);
			arc.setStartAngle(1);
			arc.setLength(DEGREES_270);
			arc.setType(ArcType.OPEN);
		}
		return arc;
	}
	
	@Override
	public boolean contains(Point pPoint)
	{
		boolean result = super.contains(pPoint);
		if (getShape() instanceof Arc)
		{
			Arc arc = (Arc) getShape();
			arc.setRadiusX(arc.getRadiusX() + 2 * MAX_DISTANCE);
			arc.setRadiusY(arc.getRadiusY() + 2 * MAX_DISTANCE);
			result = arc.contains(pPoint.getX(), pPoint.getY());
		}
		return result;
	}
	
	/** 
	 * @return An index that represents the position in the list of
	 * edges between the same start and end nodes. 
	 * @pre getGraph() != null
	 */
	private int getPosition()
	{
		assert edge().getGraph() != null;
		int lReturn = 0;
		for( Edge edge : edge().getGraph2().getEdges(edge().getStart()))
		{
			if( edge.getStart() == edge().getStart() && edge.getEnd() == edge().getEnd())
			{
				lReturn++;
			}
			if( edge == edge() )
			{
				return lReturn;
			}
		}
		assert lReturn > 0;
		return lReturn;
	}
	
	/*
	 * The connection points for the self-edge are an offset from the top-right
	 * corner.
	 */
	private Line getSelfEdgeConnectionPoints()
	{
		if( getPosition() == 1 )
		{
			Point2D point1 = new Point2D(edge().getStart().view2().getBounds().getMaxX() - SELF_EDGE_OFFSET, 
					edge().getStart().view2().getBounds().getY());
			Point2D point2 = new Point2D(edge().getStart().view2().getBounds().getMaxX(), 
					edge().getStart().view2().getBounds().getY() + SELF_EDGE_OFFSET);
			return new Line(Conversions2.toPoint(point1), Conversions2.toPoint(point2));
		}
		else
		{
			Point2D point1 = new Point2D(edge().getStart().view2().getBounds().getX(), 
					edge().getStart().view2().getBounds().getY() + SELF_EDGE_OFFSET);
			Point2D point2 = new Point2D(edge().getStart().view2().getBounds().getX() + SELF_EDGE_OFFSET, 
					edge().getStart().view2().getBounds().getY());
			return new Line(Conversions2.toPoint(point1), Conversions2.toPoint(point2));
		}
	}
	
	private Shape getNormalEdgeShape()
	{
		Line line = getConnectionPoints();
		Path path = new Path();
		MoveTo moveTo = new MoveTo(line.getPoint1().getX(), line.getPoint1().getY());
		QuadCurveTo curveTo = new QuadCurveTo(getControlPoint().getX(), getControlPoint().getY(), line.getPoint2().getX(), line.getPoint2().getY());
		path.getElements().addAll(moveTo, curveTo);
		return path;
	}
	
	
	/**
     *  Gets the control point for the quadratic spline.
     * @return the control point
     */
	private Point2D getControlPoint()
	{
		Line line = getConnectionPoints();
		double tangent = Math.tan(Math.toRadians(DEGREES_10));
		if( edge().getGraph() != null && getPosition() > 1 )
		{
			tangent = Math.tan(Math.toRadians(DEGREES_20));
		}
		double dx = (line.getX2() - line.getX1()) / 2;
		double dy = (line.getY2() - line.getY1()) / 2;
		return new Point2D((line.getX1() + line.getX2()) / 2 + tangent * dy, (line.getY1() + line.getY2()) / 2 - tangent * dx);         
	}
	
	@Override
	public Rectangle getBounds()
	{
		return super.getBounds().add(Conversions2.toRectangle(getLabelBounds()));
	}
	
	@Override
	public Line getConnectionPoints()
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
	
	/*
	 * The connection points are a slight offset from the center.
	 * @return
	 */
	private Line getNormalEdgeConnectionsPoints()
	{
		Rectangle start = edge().getStart().view2().getBounds();
		Rectangle end = edge().getEnd().view2().getBounds();
		Point startCenter = start.getCenter();
		Point endCenter = end.getCenter();
		int turn = DEGREES_5;
		if( edge().getGraph() != null && getPosition() > 1 )
		{
			turn = DEGREES_20;
		}
		Direction d1 = new Direction(startCenter, endCenter).turn(-turn);
		Direction d2 = new Direction(endCenter, startCenter).turn(turn);
		return new Line(edge().getStart().view2().getConnectionPoint(d1), edge().getEnd().view2().getConnectionPoint(d2));
	}
	
}
