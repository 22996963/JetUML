/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2015 Cay S. Horstmann and the contributors of the 
 * JetUML project.
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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import ca.mcgill.cs.stg.jetuml.framework.Grid;
import ca.mcgill.cs.stg.jetuml.framework.MultiLineString;

/**
 * An interface node in a class diagram.
 */
public class InterfaceNode extends RectangularNode implements ParentChildNode
{
	private static final int DEFAULT_COMPARTMENT_HEIGHT = 20;
	private static final int DEFAULT_WIDTH = 100;
	private static final int DEFAULT_HEIGHT = 60;
	
	private double aMidHeight;
	private double aBotHeight;
	private MultiLineString aName;
	private MultiLineString aMethods;   
	private ParentChildNode aContainer;
	
	/**
     * Construct an interface node with a default size and
     * the text <<interface>>.
	 */
	public InterfaceNode()
	{
		aName = new MultiLineString(true);
		aName.setText("\u00ABinterface\u00BB");
		aMethods = new MultiLineString();
		aMethods.setJustification(MultiLineString.LEFT);
		setBounds(new Rectangle2D.Double(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT));
		aMidHeight = DEFAULT_COMPARTMENT_HEIGHT;
		aBotHeight = DEFAULT_COMPARTMENT_HEIGHT;
	}

	@Override
	public void draw(Graphics2D pGraphics2D)
	{
		super.draw(pGraphics2D);
		Rectangle2D top = new Rectangle2D.Double(getBounds().getX(), getBounds().getY(), 
    		  getBounds().getWidth(), getBounds().getHeight() - aMidHeight - aBotHeight);
		pGraphics2D.draw(top);
		aName.draw(pGraphics2D, top);
		Rectangle2D mid = new Rectangle2D.Double(top.getX(), top.getMaxY(), top.getWidth(), aMidHeight);
		pGraphics2D.draw(mid);
		Rectangle2D bot = new Rectangle2D.Double(top.getX(), mid.getMaxY(), top.getWidth(), aBotHeight);
		pGraphics2D.draw(bot);
		aMethods.draw(pGraphics2D, bot);
	}

	@Override
	public void layout(Graph pGraph, Graphics2D pGraphics2D, Grid pGrid)
	{
		Rectangle2D min = new Rectangle2D.Double(0, 0, DEFAULT_WIDTH, DEFAULT_COMPARTMENT_HEIGHT);
		Rectangle2D top = aName.getBounds(pGraphics2D); 
		top.add(min);
		Rectangle2D bot = aMethods.getBounds(pGraphics2D);

		aBotHeight = bot.getHeight();
		if(aBotHeight == 0)
		{
			top.add(new Rectangle2D.Double(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT));
			aMidHeight = 0;
		}
		else
		{
			bot.add(min);
			aMidHeight = DEFAULT_COMPARTMENT_HEIGHT;
			aBotHeight = bot.getHeight();
		}

		Rectangle2D b = new Rectangle2D.Double(getBounds().getX(), getBounds().getY(), 
				Math.max(top.getWidth(), bot.getWidth()), top.getHeight() + aMidHeight + aBotHeight);
		pGrid.snap(b);
		setBounds(b);
	}

	/**
     * Sets the name property value.
     * @param pName the interface name
	 */
	public void setName(MultiLineString pName)
	{
		aName = pName;
	}

	/**
     * Gets the name property value.
     * @return the interface name
	 */
	public MultiLineString getName()
	{
		return aName;
	}

	/**
     * Sets the methods property value.
     * @param pMethods the methods of this interface
	 */
	public void setMethods(MultiLineString pMethods)
	{
		aMethods = pMethods;
	}
	
	/**
     * Gets the methods property value.
     * @return the methods of this interface
	 */
	public MultiLineString getMethods()
	{
		return aMethods;
	}

	@Override
	public InterfaceNode clone()
	{
		InterfaceNode cloned = (InterfaceNode)super.clone();
		cloned.aName = (MultiLineString)aName.clone();
		cloned.aMethods = (MultiLineString)aMethods.clone();
		return cloned;
	}
	
	@Override
	public ParentChildNode getParent()
	{
		return aContainer;
	}

	@Override
	public void setParent(ParentChildNode pNode)
	{
		assert pNode instanceof PackageNode;
		aContainer = (PackageNode) pNode;
	}

	@Override
	public List<ParentChildNode> getChildren()
	{
		return new ArrayList<ParentChildNode>();
	}

	@Override
	public void addChild(int pIndex, ParentChildNode pNode)
	{
		assert false;
	}

	@Override
	public void addChild(ParentChildNode pNode)
	{
		assert false;
	}

	@Override
	public void removeChild(ParentChildNode pNode)
	{
		assert false;		
	}
}
