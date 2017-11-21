package ca.mcgill.cs.stg.jetuml.graph.views.nodes;

import java.awt.Graphics2D;

import ca.mcgill.cs.stg.jetuml.framework.MultiLineString;
import ca.mcgill.cs.stg.jetuml.geom.Rectangle;
import ca.mcgill.cs.stg.jetuml.graph.nodes.ClassNode;

/**
 * An object to render an interface in a class diagram.
 * 
 * @author Martin P. Robillard
 *
 */
public class ClassNodeView extends InterfaceNodeView
{
	/**
	 * @param pNode The node to wrap.
	 */
	public ClassNodeView(ClassNode pNode)
	{
		super(pNode);
	}
	
	private MultiLineString attributes()
	{
		return ((ClassNode)node()).getAttributes();
	}
	
	@Override
	public void draw(Graphics2D pGraphics2D)
	{
		super.draw(pGraphics2D); 
		int bottomHeight = computeBottom().getHeight();
		Rectangle top = new Rectangle(getBounds().getX(), getBounds().getY(), 
				getBounds().getWidth(), (int) Math.round(getBounds().getHeight() - middleHeight() - bottomHeight));
		Rectangle mid = new Rectangle(top.getX(), top.getMaxY(), top.getWidth(), (int) Math.round(middleHeight()));
		attributes().draw(pGraphics2D, mid);
	}
	
	/**
	 * @return True if the node requires a bottom compartment.
	 */
	@Override
	protected boolean needsMiddleCompartment()
	{
		return !attributes().getText().isEmpty();
	}
	
	@Override
	protected int middleWidth()
	{
		if( !needsMiddleCompartment() )
		{
			return 0;
		}
		else
		{
			return Math.max(attributes().getBounds().getWidth(), DEFAULT_WIDTH);
		}
	}
	
	@Override
	protected int middleHeight()
	{
		if( !needsMiddleCompartment() )
		{
			return 0;
		}
		else
		{
			return Math.max(attributes().getBounds().getHeight(), DEFAULT_COMPARTMENT_HEIGHT);
		}
	}
}
