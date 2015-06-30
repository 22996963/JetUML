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

package ca.mcgill.cs.stg.jetuml.diagrams;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.ResourceBundle;

import ca.mcgill.cs.stg.jetuml.graph.ChildNode;
import ca.mcgill.cs.stg.jetuml.graph.ClassNode;
import ca.mcgill.cs.stg.jetuml.graph.ClassRelationshipEdge;
import ca.mcgill.cs.stg.jetuml.graph.Edge;
import ca.mcgill.cs.stg.jetuml.graph.Graph;
import ca.mcgill.cs.stg.jetuml.graph.InterfaceNode;
import ca.mcgill.cs.stg.jetuml.graph.Node;
import ca.mcgill.cs.stg.jetuml.graph.NoteEdge;
import ca.mcgill.cs.stg.jetuml.graph.NoteNode;
import ca.mcgill.cs.stg.jetuml.graph.PackageNode;

/**
 *  A UML class diagram.
 */
public class ClassDiagramGraph extends Graph
{
	//CSOFF:
	private static final Node[] NODE_PROTOTYPES = new Node[] {new ClassNode(), new InterfaceNode(), new PackageNode(), new NoteNode()};
	private static final Edge[] EDGE_PROTOTYPES = new Edge[7];

	static
	{
		EDGE_PROTOTYPES[0] = ClassRelationshipEdge.createDependencyEdge();
		EDGE_PROTOTYPES[1] = ClassRelationshipEdge.createInheritanceEdge();
		EDGE_PROTOTYPES[2] = ClassRelationshipEdge.createInterfaceInheritanceEdge();
		EDGE_PROTOTYPES[3] = ClassRelationshipEdge.createAssociationEdge();
		EDGE_PROTOTYPES[4] = ClassRelationshipEdge.createAggregationEdge();
		EDGE_PROTOTYPES[5] = ClassRelationshipEdge.createCompositionEdge();
		EDGE_PROTOTYPES[6] = new NoteEdge();	
	}
	//CSON:

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
		return ResourceBundle.getBundle("ca.mcgill.cs.stg.jetuml.UMLEditorStrings").getString("class.extension");
	}

	@Override
	public String getDescription() 
	{
		return ResourceBundle.getBundle("ca.mcgill.cs.stg.jetuml.UMLEditorStrings").getString("class.name");
	}

	private static boolean canAddNodeAsChild(Node pParent, Node pPotentialChild)
	{
		if( pParent instanceof PackageNode )
		{
			return pPotentialChild instanceof ClassNode || pPotentialChild instanceof InterfaceNode || 
					pPotentialChild instanceof PackageNode ;
		}
		else
		{
			return false;
		}
	}
	
	/* Find if the node to be added should be added to a package. Returns null if not */
	private PackageNode findContainer(Node pNode, Point2D pPoint)
	{
		ArrayList<PackageNode> candidates = new ArrayList<>();
		for( Node node : aNodes )
		{
			if( node == pNode )
			{
				continue;
			}
			else if( node.contains(pPoint) && canAddNodeAsChild(node, pNode))
			{
				candidates.add((PackageNode)node); // canAddNodeAsChild ensures the downcast is valid
			}
		}
		// There should be only one node without a child (the top package)
		for( PackageNode node : candidates )
		{
			if(!hasChild(candidates, node))
			{
				return node;
			}
		}
		return null;
	}

	/*
	 * returns true if pNode has a child among pPackages
	 */
	private static boolean hasChild(ArrayList<PackageNode> pPackages, PackageNode pNode)
	{
		for( PackageNode node : pPackages )
		{
			if( pNode.getChildren().contains(node) )
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean add(Node pNode, Point2D pPoint)
	{
		aModListener.startCompoundListening();
		super.add(pNode, pPoint);
		PackageNode container = findContainer(pNode, pPoint);
		if( container != null )
		{
			container.addChild((ChildNode)pNode);
		}
		aModListener.endCompoundListening();
		return true;
	}
}





