/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2015-2018 by the contributors of the JetUML project.
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

package ca.mcgill.cs.jetuml.diagram.builder;

import java.util.ArrayList;

import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.Node;
import ca.mcgill.cs.jetuml.diagram.ObjectDiagram;
import ca.mcgill.cs.jetuml.diagram.edges.NoteEdge;
import ca.mcgill.cs.jetuml.diagram.edges.ObjectCollaborationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.ObjectReferenceEdge;
import ca.mcgill.cs.jetuml.diagram.nodes.ChildNode;
import ca.mcgill.cs.jetuml.diagram.nodes.FieldNode;
import ca.mcgill.cs.jetuml.diagram.nodes.NoteNode;
import ca.mcgill.cs.jetuml.diagram.nodes.ObjectNode;
import ca.mcgill.cs.jetuml.geom.Point;

public class ObjectDiagramBuilder extends DiagramBuilder
{
	public ObjectDiagramBuilder( Diagram pDiagram )
	{
		super( pDiagram );
		assert pDiagram instanceof ObjectDiagram;
	}
	
	@Override
	protected void addComplementaryEdgeAdditionOperations(CompoundOperation pOperation, Edge pEdge, Point pPoint1, Point pPoint2)
	{
		assert pEdge.getStart() != null;
		if( pEdge.getStart() instanceof FieldNode )
		{
			final FieldNode node = (FieldNode) pEdge.getStart();
			final String oldValue = node.getValue();
			pOperation.add(new SimpleOperation(()-> node.setValue(""),
					()-> node.setValue(oldValue)));
		}
	}
	
	@Override
	public boolean canAdd(Node pNode, Point pRequestedPosition)
	{
		boolean result = true;
		if( pNode instanceof FieldNode && findObject((FieldNode)pNode, pRequestedPosition) == null )
		{
			result = false;
		}
		return result;
	}
	
	@Override
	public boolean canConnect(Edge pEdge, Node pNode1, Node pNode2, Point pPoint2)
	{
		if( !super.canConnect(pEdge, pNode1, pNode2, pPoint2) )
		{
			return false;
		}
		if( pNode1 instanceof ObjectNode )
		{
			return (pEdge instanceof ObjectCollaborationEdge && pNode2 instanceof ObjectNode) ||
					(pEdge instanceof NoteEdge && pNode2 instanceof NoteNode);
		}
		if( pNode1 instanceof FieldNode )
		{
			return pEdge instanceof ObjectReferenceEdge && pNode2 instanceof ObjectNode;
		}
		return true;
	}
	
	/* Find if the node to be added can be added to an object. Returns null if not. 
	 * If a node is already the parent of the field (from a previously undone operation),
	 * return this node. Otherwise, find if a node is at the point
	 */
	private ObjectNode findObject(FieldNode pNode, Point pPoint)
	{
		ArrayList<ObjectNode> candidates = new ArrayList<>();
		for( Node node : aDiagram.getRootNodes() )
		{
			if( node == pNode )
			{
				continue;
			}
			else if( pNode.getParent() == node )
			{
				return (ObjectNode)node;
			}
			else if( node.view().contains(pPoint) && canAddNodeAsChild(node, pNode))
			{
				candidates.add((ObjectNode)node); // canAddNodeAsChild ensures the downcast is valid
			}
		}
		// Pick the last node in the list as some inexact but simple
		// heuristic for choosing the top node. We'll need a z-coordinate to do better.
		if( candidates.size() > 0 )
		{
			return candidates.get(candidates.size()-1);
		}
		else
		{
			return null;
		}
	}
	
	private static boolean canAddNodeAsChild(Node pParent, Node pPotentialChild)
	{
		if( pParent instanceof ObjectNode )
		{
			return pPotentialChild instanceof FieldNode;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public DiagramOperation createAddNodeOperation(Node pNode, Point pRequestedPosition, int pMaxWidth, int pMaxHeight)
	{
		assert canAdd(pNode, pRequestedPosition);
		DiagramOperation result = null;
		if( pNode instanceof FieldNode )
		{
			ObjectNode object = findObject((FieldNode)pNode, pRequestedPosition);
			
			if( object != null )
			{
				result = new SimpleOperation(()-> object.addChild((ChildNode)pNode),
						()-> object.removeChild((ChildNode)pNode));
			}
		}
		if( result == null )
		{
			result = super.createAddNodeOperation(pNode, pRequestedPosition, pMaxWidth, pMaxHeight);
		}
		return result;
	}

}
