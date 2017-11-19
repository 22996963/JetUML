package ca.mcgill.cs.stg.jetuml.graph.views.nodes;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import ca.mcgill.cs.stg.jetuml.framework.Grid;
import ca.mcgill.cs.stg.jetuml.framework.MultiLineString;
import ca.mcgill.cs.stg.jetuml.geom.Rectangle;
import ca.mcgill.cs.stg.jetuml.graph.Graph;
import ca.mcgill.cs.stg.jetuml.graph.nodes.StateNode;

/**
 * An object to render a StateNode.
 * 
 * @author Martin P. Robillard
 *
 */
public class StateNodeView extends RectangleBoundedNodeView
{
	private static final int DEFAULT_WIDTH = 80;
	private static final int DEFAULT_HEIGHT = 60;
	private static final int ARC_SIZE = 20;
	
	/**
	 * @param pNode The node to wrap.
	 */
	public StateNodeView(StateNode pNode)
	{
		super(pNode, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	private MultiLineString name()
	{
		return ((StateNode)node()).getName();
	}
	
	@Override
	public void draw(Graphics2D pGraphics2D)
	{
		super.draw(pGraphics2D);
		pGraphics2D.draw(getShape());
		name().draw(pGraphics2D, getBounds());
	}
	
	@Override
	protected Shape getShape()
	{       
		return new RoundRectangle2D.Double(getBounds().getX(), getBounds().getY(), 
				getBounds().getWidth(), getBounds().getHeight(), ARC_SIZE, ARC_SIZE);
	}
	
	@Override	
	public void layout(Graph pGraph)
	{
		Rectangle bounds = name().getBounds();
		bounds = new Rectangle(getBounds().getX(), getBounds().getY(), 
				Math.max(bounds.getWidth(), DEFAULT_WIDTH), Math.max(bounds.getHeight(), DEFAULT_HEIGHT));
		setBounds(Grid.snapped(bounds));
	}
}
