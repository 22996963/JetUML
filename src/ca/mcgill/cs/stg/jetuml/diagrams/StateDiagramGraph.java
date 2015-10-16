/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2015 by the contributors of the JetUML project.
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

package ca.mcgill.cs.stg.jetuml.diagrams;

import java.awt.geom.Point2D;
import java.util.ResourceBundle;

import ca.mcgill.cs.stg.jetuml.graph.CircularStateNode;
import ca.mcgill.cs.stg.jetuml.graph.Edge;
import ca.mcgill.cs.stg.jetuml.graph.Graph;
import ca.mcgill.cs.stg.jetuml.graph.Node;
import ca.mcgill.cs.stg.jetuml.graph.NoteEdge;
import ca.mcgill.cs.stg.jetuml.graph.NoteNode;
import ca.mcgill.cs.stg.jetuml.graph.StateNode;
import ca.mcgill.cs.stg.jetuml.graph.StateTransitionEdge;

/**
 * A UML state diagram.
 */
public class StateDiagramGraph extends Graph
{
	private static final Node[] NODE_PROTOTYPES = new Node[]{new StateNode(), new CircularStateNode(), new CircularStateNode(), new NoteNode()};
	private static final Edge[] EDGE_PROTOTYPES = new Edge[]{new StateTransitionEdge(), new NoteEdge()};
	
	static
	{
		((CircularStateNode)NODE_PROTOTYPES[2]).setFinal(true);
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
		return ResourceBundle.getBundle("ca.mcgill.cs.stg.jetuml.UMLEditorStrings").getString("state.extension");
	}

	@Override
	public String getDescription() 
	{
		return ResourceBundle.getBundle("ca.mcgill.cs.stg.jetuml.UMLEditorStrings").getString("state.name");
	}
	
	@Override
	public boolean connect(Edge pEdge, Point2D pPoint1, Point2D pPoint2)
	{	
		Node n1 = findNode(pPoint1);
		Node n2 = findNode(pPoint2);
		if(n1 != null)
		{
			//This checks to see if first node is an end note. 
			// Cannot have edges coming from final node 
			// except for note edges
			if(n1 instanceof CircularStateNode)
			{
				CircularStateNode end = (CircularStateNode) n1;
				if(end.isFinal() && !(pEdge instanceof NoteEdge))
				{
					return false;
				}
			}
			//This checks to see if second node is a beginning node. Cannot return to start state.
			if (n2 instanceof CircularStateNode)
			{
				CircularStateNode begin = (CircularStateNode) n2;
				if(!begin.isFinal() && !(pEdge instanceof NoteEdge))
				{
					return false;
				}
			}
		}
		return super.connect(pEdge, pPoint1, pPoint2);
	}	
}





