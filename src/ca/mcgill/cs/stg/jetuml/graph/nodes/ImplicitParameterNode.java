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

package ca.mcgill.cs.stg.jetuml.graph.nodes;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Statement;
import java.util.ArrayList;
import java.util.List;

import ca.mcgill.cs.stg.jetuml.geom.Point;
import ca.mcgill.cs.stg.jetuml.geom.Rectangle;
import ca.mcgill.cs.stg.jetuml.graph.views.nodes.ImplicitParameterNodeView;
import ca.mcgill.cs.stg.jetuml.graph.views.nodes.NodeView;

/**
 * An implicit parameter node in a sequence diagram. The 
 * visual portion of this node includes the top rectangle (object) and
 * its vertical life line. The ImplicitParamterNode's creator is the
 * CallNode that is the source of a <<creates>> edge that leads to 
 * this node, or null if this node is node created as part of the 
 * sequence.
 */
public class ImplicitParameterNode extends NamedNode implements ParentNode
{
	private List<ChildNode> aCallNodes = new ArrayList<>();

	/**
     * Construct an object node with a default size.
	 */
	public ImplicitParameterNode()
	{
		getName().setUnderlined(true);
	}
	
	@Override
	protected NodeView generateView()
	{
		return new ImplicitParameterNodeView(this);
	}

	@Override
	public ImplicitParameterNode clone()
	{
		ImplicitParameterNode cloned = (ImplicitParameterNode) super.clone();
		cloned.aCallNodes = new ArrayList<>();
		for( ChildNode child : aCallNodes )
		{
			// We can't use addChild(...) here because of the interaction with the original parent.
			ChildNode clonedChild = (ChildNode) child.clone();
			clonedChild.setParent(cloned);
			cloned.aCallNodes.add(clonedChild);
		}
		return cloned;
	}
	
	@Override
	public List<ChildNode> getChildren()
	{
		return aCallNodes;
	}

	@Override
	public void addChild(int pIndex, ChildNode pNode)
	{
		ParentNode oldParent = pNode.getParent();
		if (oldParent != null)
		{
			oldParent.removeChild(pNode);
		}
		aCallNodes.add(pIndex, pNode);
		pNode.setParent(this);
	}
	
	/**
	 * Adds a child in the right sequence in the list of calls.
	 * @param pChild The child to add
	 * @param pPoint The point selected.
	 */
	public void addChild(ChildNode pChild, Point pPoint)
	{
		int i = 0;
		while(i < aCallNodes.size() && aCallNodes.get(i).getBounds().getY() <= pPoint.getY())
		{
			i++;
		}
		addChild(i, pChild);
	}

	@Override
	public void addChild(ChildNode pNode)
	{
		addChild(aCallNodes.size(), pNode);
	}

	@Override
	public void removeChild(ChildNode pNode)
	{
		if (pNode.getParent() != this)
		{
			return;
		}
		aCallNodes.remove(pNode);
		pNode.setParent(null);
	}
	
	/**
	 * @return The bounds of the top rectangle.
	 */
	public Rectangle getTopRectangle()
	{
		return ((ImplicitParameterNodeView)view()).getTopRectangle();
	}
	
	/**
	 * @param pNewBounds The new bounds.
	 */
	public void setBounds(Rectangle pNewBounds)
	{
		((ImplicitParameterNodeView)view()).setBounds(pNewBounds);
	}
	
	/**
	 *  Adds a persistence delegate to a given encoder that
	 * encodes the child nodes of this node.
	 * @param pEncoder the encoder to which to add the delegate
	 */
	public static void setPersistenceDelegate(Encoder pEncoder)
	{
		pEncoder.setPersistenceDelegate(ImplicitParameterNode.class, new DefaultPersistenceDelegate()
		{
			protected void initialize(Class<?> pType, Object pOldInstance, Object pNewInstance, Encoder pOut) 
			{
				super.initialize(pType, pOldInstance, pNewInstance, pOut);
				for(ChildNode node : ((ParentNode) pOldInstance).getChildren())
				{
					pOut.writeStatement( new Statement(pOldInstance, "addChild", new Object[]{ node }) );            
				}
			}
		});
	}
}
