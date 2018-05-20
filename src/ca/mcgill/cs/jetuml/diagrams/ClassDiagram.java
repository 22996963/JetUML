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

package ca.mcgill.cs.jetuml.diagrams;

import static ca.mcgill.cs.jetuml.application.ApplicationResources.RESOURCES;

import java.util.ArrayList;
import java.util.List;

import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.graph.Edge;
import ca.mcgill.cs.jetuml.graph.Graph;
import ca.mcgill.cs.jetuml.graph.Node;
import ca.mcgill.cs.jetuml.graph.edges.AggregationEdge;
import ca.mcgill.cs.jetuml.graph.edges.AssociationEdge;
import ca.mcgill.cs.jetuml.graph.edges.DependencyEdge;
import ca.mcgill.cs.jetuml.graph.edges.GeneralizationEdge;
import ca.mcgill.cs.jetuml.graph.edges.NoteEdge;
import ca.mcgill.cs.jetuml.graph.nodes.ChildNode;
import ca.mcgill.cs.jetuml.graph.nodes.ClassNode;
import ca.mcgill.cs.jetuml.graph.nodes.InterfaceNode;
import ca.mcgill.cs.jetuml.graph.nodes.NoteNode;
import ca.mcgill.cs.jetuml.graph.nodes.PackageNode;

/**
 *  A UML class diagram.
 */
public class ClassDiagram extends Graph
{
	private static final Node[] NODE_PROTOTYPES = new Node[] {new ClassNode(), 
															  new InterfaceNode(), 
															  new PackageNode(), 
															  new NoteNode()};
	
	private static final Edge[] EDGE_PROTOTYPES = new Edge[] {new DependencyEdge(), 
															  new GeneralizationEdge(), 
															  new GeneralizationEdge(GeneralizationEdge.Type.Implementation),
															  new AssociationEdge(),
															  new AggregationEdge(),
															  new AggregationEdge(AggregationEdge.Type.Composition),
															  new NoteEdge()};

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
		return RESOURCES.getString("classdiagram.file.extension");
	}

	@Override
	public String getDescription() 
	{
		return RESOURCES.getString("classdiagram.file.name");
	}

	private static boolean canAddNodeAsChild(Node pPotentialChild)
	{
		return pPotentialChild instanceof ClassNode || pPotentialChild instanceof InterfaceNode || 
					pPotentialChild instanceof PackageNode ;
	}
	
	@Override
	public boolean canConnect(Edge pEdge, Node pNode1, Node pNode2, Point pPoint2)
	{
		if( !super.canConnect(pEdge, pNode1, pNode2, pPoint2) )
		{
			return false;
		}
		if( pEdge instanceof GeneralizationEdge && pNode1 == pNode2 )
		{
			return false;
		}
		
		return true;
	}
	
	/* Find if the node to be added should be added to a package. Returns null if not. 
	 * If packages overlap, select the last one added, which by default should be on
	 * top. This could be fixed if we ever add a z coordinate to the graph.
	 */
	private PackageNode findContainer( List<Node> pNodes, Point pPoint)
	{
		PackageNode container = null;
		for( Node node : pNodes )
		{
			if( node instanceof PackageNode && node.view().contains(pPoint) )
			{
				container = (PackageNode) node;
			}
		}
		if( container == null )
		{
			return null;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Node> children = new ArrayList(container.getChildren());
		if( children.size() == 0 )
		{
			return container;
		}
		else
		{
			PackageNode deeperContainer = findContainer( children, pPoint );
			if( deeperContainer == null )
			{
				return container;
			}
			else
			{
				return deeperContainer;
			}
		}
	}
	
	/* (non-Javadoc)
	 * See if, given its position, the node should be added as a child of
	 * a PackageNode.
	 * 
	 * @see ca.mcgill.cs.jetuml.graph.Graph#addNode(ca.mcgill.cs.jetuml.graph.Node, java.awt.geom.Point2D)
	 */
	@Override
	public boolean addNode(Node pNode, Point pPoint, int pMaxWidth, int pMaxHeight)
	{
		if( canAddNodeAsChild(pNode))
		{
			PackageNode container = findContainer(aRootNodes, pPoint);
			if( container != null )
			{
				container.addChild((ChildNode)pNode);
			}
		}
		super.addNode(pNode, pPoint, pMaxWidth, pMaxHeight);
		return true;
	}
}





