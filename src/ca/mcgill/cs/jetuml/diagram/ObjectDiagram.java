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

package ca.mcgill.cs.jetuml.diagram;

import static ca.mcgill.cs.jetuml.application.ApplicationResources.RESOURCES;

import ca.mcgill.cs.jetuml.diagram.builder.ObjectDiagramBuilder;
import ca.mcgill.cs.jetuml.diagram.edges.NoteEdge;
import ca.mcgill.cs.jetuml.diagram.edges.ObjectCollaborationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.ObjectReferenceEdge;
import ca.mcgill.cs.jetuml.diagram.nodes.FieldNode;
import ca.mcgill.cs.jetuml.diagram.nodes.NoteNode;
import ca.mcgill.cs.jetuml.diagram.nodes.ObjectNode;
import ca.mcgill.cs.jetuml.geom.Point;

/**
 *  An UML-style object diagram that shows object references.
 */
public class ObjectDiagram extends Diagram
{
	private static final Node[] NODE_PROTOTYPES = new Node[3];
	private static final Edge[] EDGE_PROTOTYPES = new Edge[3];
	
	static
	{
		NODE_PROTOTYPES[0] = new ObjectNode();
	      
		FieldNode fieldNode = new FieldNode();
	    fieldNode.setName("name");
	    fieldNode.setValue("value");
	    
	    NODE_PROTOTYPES[1] = fieldNode;
	    NODE_PROTOTYPES[2] = new NoteNode();
	    
	    EDGE_PROTOTYPES[0] = new ObjectReferenceEdge();
	    EDGE_PROTOTYPES[1] = new ObjectCollaborationEdge();
	    EDGE_PROTOTYPES[2] = new NoteEdge();
	}
	
	public ObjectDiagram()
	{
		aBuilder = new ObjectDiagramBuilder(this);
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
	
	@Override
	protected void completeEdgeAddition(Node pOrigin, Edge pEdge, Point pPoint1, Point pPoint2)
	{
		if( pOrigin instanceof FieldNode )
		{
			String oldValue = ((FieldNode)pOrigin).getValue();
			((FieldNode)pOrigin).setValue("");
			notifyPropertyChanged(pOrigin, "value", oldValue);
		}
	}
	
	@Override
	public Node[] getNodePrototypes()
	{
		return NODE_PROTOTYPES;
	}

	@Override
	public Edge[] getEdgePrototypes()
	{
		return EDGE_PROTOTYPES;
	}   
	
	@Override
	public String getFileExtension() 
	{
		return RESOURCES.getString("objectdiagram.file.extension");
	}

	@Override
	public String getDescription() 
	{
		return RESOURCES.getString("objectdiagram.file.name");
	}
}
