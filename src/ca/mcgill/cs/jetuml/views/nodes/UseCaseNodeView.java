package ca.mcgill.cs.jetuml.views.nodes;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.graph.Graph;
import ca.mcgill.cs.jetuml.graph.nodes.UseCaseNode;
import ca.mcgill.cs.jetuml.views.Grid;
import ca.mcgill.cs.jetuml.views.StringViewer;

/**
 * An object to render a UseCaseNode.
 * 
 * @author Martin P. Robillard
 *
 */
public class UseCaseNodeView extends RectangleBoundedNodeView
{
	private static final int DEFAULT_WIDTH = 110;
	private static final int DEFAULT_HEIGHT = 40;
	private static final StringViewer NAME_VIEWER = new StringViewer(StringViewer.Align.CENTER, false, false);
	
	/**
	 * @param pNode The node to wrap.
	 */
	public UseCaseNodeView(UseCaseNode pNode)
	{
		super(pNode, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	@Override
	public void draw(Graphics2D pGraphics2D)
	{
		super.draw(pGraphics2D);      
		pGraphics2D.draw(getShape());
		NAME_VIEWER.draw(name(), pGraphics2D, getBounds());
	}
	
	@Override
	public Shape getShape()
	{
		return new Ellipse2D.Double(node().position().getX(), node().position().getY(), 
				getBounds().getWidth(), getBounds().getHeight());
	}
	
	private String name()
	{
		return ((UseCaseNode)node()).getName();
	}
	
	@Override	
	public void layout(Graph pGraph)
	{
		Rectangle bounds = NAME_VIEWER.getBounds(name());
		bounds = new Rectangle(getBounds().getX(), getBounds().getY(), 
				Math.max(bounds.getWidth(), DEFAULT_WIDTH), Math.max(bounds.getHeight(), DEFAULT_HEIGHT));
		setBounds(Grid.snapped(bounds));
	}
}
