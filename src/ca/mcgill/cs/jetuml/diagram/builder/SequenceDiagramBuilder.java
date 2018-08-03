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

import java.util.List;
import java.util.Optional;

import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.DiagramElement;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.Node;
import ca.mcgill.cs.jetuml.diagram.SequenceDiagram;
import ca.mcgill.cs.jetuml.diagram.edges.CallEdge;
import ca.mcgill.cs.jetuml.diagram.edges.ReturnEdge;
import ca.mcgill.cs.jetuml.diagram.nodes.CallNode;
import ca.mcgill.cs.jetuml.diagram.nodes.ChildNode;
import ca.mcgill.cs.jetuml.diagram.nodes.ImplicitParameterNode;
import ca.mcgill.cs.jetuml.geom.Point;

/**
 * A builder for sequence diagrams.
 */
public class SequenceDiagramBuilder extends DiagramBuilder
{
	private static final int CALL_NODE_YGAP = 5;
	
	/**
	 * Creates a new builder for sequence diagrams.
	 * 
	 * @param pDiagram The diagram to wrap around.
	 * @pre pDiagram != null;
	 */
	public SequenceDiagramBuilder( Diagram pDiagram )
	{
		super( pDiagram );
		assert pDiagram instanceof SequenceDiagram;
	}
	
	/*
	 * Returns true if pCallee is in the control-flow of pPotentialCaller
	 */
	private boolean isCallDominator(CallNode pPotentialCaller, CallNode pCallee)
	{
		for( Optional<CallNode> caller = ((SequenceDiagram)aDiagram).getCaller(pCallee); 
				caller.isPresent(); caller = ((SequenceDiagram)aDiagram).getCaller(caller.get()))
		{
			if( caller.get() == pPotentialCaller )
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected List<DiagramElement> getCoRemovals(DiagramElement pElement)
	{
		List<DiagramElement> result = super.getCoRemovals(pElement);
		if( pElement instanceof CallEdge && hasNoCallees(((Edge)pElement).getEnd()))
		{
			result.add(((Edge)pElement).getEnd());
		}
		if( pElement instanceof CallEdge )
		{
			Edge returnEdge = null;
			Edge input = (CallEdge) pElement;
			for( Edge edge : aDiagram.edges() )
			{
				if( edge instanceof ReturnEdge && edge.getStart() == input.getEnd() && edge.getEnd() == input.getStart())
				{
					returnEdge = edge;
					break;
				}
			}
			if( returnEdge != null )
			{
				final Edge target = returnEdge;
				result.add(target);
			}
		}
		return result;
	}
	
	@Override
	public DiagramOperation createRemoveEdgeOperation(Edge pEdge)
	{
		CompoundOperation result = new CompoundOperation();
		result.add(super.createRemoveEdgeOperation(pEdge));
		
		if(pEdge instanceof CallEdge && hasNoCallees(pEdge.getEnd())) 
		{
			result.add(createRemoveNodeOperation(pEdge.getEnd()));
		}
		
		// Also delete the return edge, if it exists
		if( pEdge instanceof CallEdge )
		{
			Edge returnEdge = null;
			for( Edge edge : aDiagram.edges() )
			{
				if( edge instanceof ReturnEdge && edge.getStart() == pEdge.getEnd() && edge.getEnd() == pEdge.getStart())
				{
					returnEdge = edge;
					break;
				}
			}
			if( returnEdge != null )
			{
				final Edge target = returnEdge;
				result.add(new SimpleOperation( ()-> aDiagram.removeEdge(target),
						()-> aDiagram.addEdge(target)));
			}
		}
		return result;
	}
	
	/**
	 * @param pNode The node to check
	 * @return True if pNode is a call node that does not have any outgoing
	 * call edge.
	 */
	private boolean hasNoCallees(Node pNode)
	{
		if( !(pNode instanceof CallNode ))
		{
			return false;
		}
		assert pNode instanceof CallNode;
		for( Edge edge : aDiagram.edges() )
		{
			if( edge.getStart() == pNode )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canAdd(Node pNode, Point pRequestedPosition)
	{
		boolean result = true;
		if(pNode instanceof CallNode && insideTargetArea(pRequestedPosition) == null)
		{
			result = false;
		}
		return result;
	}
	
	@Override
	protected boolean canConnect(Edge pEdge, Node pNode1, Optional<Node> pNode2, Point pPoint2)
	{
		boolean lReturn = true;
		if( !super.canConnect(pEdge, pNode1, pNode2, pPoint2) )
		{
			lReturn = false;
		}
		else if(pNode1 instanceof CallNode && pEdge instanceof ReturnEdge && pNode2.get() instanceof CallNode)
		{
			// The end node has to be the caller, and adding a return edge on the same object is not allowed.
			lReturn = pNode2.get() == ((SequenceDiagram)aDiagram).getCaller(pNode1).get() && 
					!(((CallNode)pNode1).getParent() == ((CallNode)pNode2.get()).getParent());
		}
		else if(pNode1 instanceof CallNode && !(pEdge instanceof CallEdge))
		{
			lReturn = false;
		}
		else if(pNode1 instanceof CallNode && !(pNode2.get() instanceof CallNode) && !(pNode2.get() instanceof ImplicitParameterNode ))
		{
			lReturn = false;
		}
		else if(pNode1 instanceof ImplicitParameterNode )
		{
			lReturn = false;
		}
		else if( pNode1 instanceof CallNode && pEdge instanceof CallEdge && 
				pNode2.get() instanceof ImplicitParameterNode && ((SequenceDiagram)aDiagram).getCaller(pNode2.get()) != null)
		{
			lReturn = !((ImplicitParameterNode)pNode2.get()).getTopRectangle().contains(pPoint2);
		}
		return lReturn;
	}
	
	@Override
	protected void addComplementaryEdgeAdditionOperations(CompoundOperation pOperation, Edge pEdge, Point pPoint1, Point pPoint2)
	{
		if( !(pEdge.getStart() instanceof CallNode) )
		{
			return;
		}
		final CallNode origin = (CallNode) pEdge.getStart();
		if( pEdge instanceof ReturnEdge )
		{
			return;
		}
		final Node end = pEdge.getEnd();
		
		// Case 1 End is on the same implicit parameter -> create a self call
		// Case 2 End is on an existing call node on a different implicit parameter -> connect
		// Case 3 End is on a different implicit parameter -> create a new call
		// Case 4 End is on an implicit parameter top node -> creates node
		// Case 5 End is on an implicit parameter node in the same call graph -> new callnode.
		if( end instanceof CallNode )
		{
			CallNode endAsCallNode = (CallNode) end;
			if( endAsCallNode.getParent() == origin.getParent() ) // Case 1
			{
				CallNode newCallNode = new CallNode();
				pEdge.connect(origin, newCallNode, aDiagram);
				final ImplicitParameterNode parent = (ImplicitParameterNode)origin.getParent();
				pOperation.add(new SimpleOperation(()-> parent.addChild(newCallNode, pPoint1),
						()-> parent.removeChild(newCallNode)));
						
			}
			else // Case 2
			{
				if( isCallDominator(endAsCallNode, origin))
				{
					CallNode newCallNode = new CallNode();
					pEdge.connect(origin, newCallNode, aDiagram);
					final ImplicitParameterNode parent = (ImplicitParameterNode)origin.getParent();
					pOperation.add(new SimpleOperation(()-> parent.addChild(newCallNode, pPoint1),
							()-> parent.removeChild(newCallNode)));
				}
				// Simple connect
			}
		}
		else if( end instanceof ImplicitParameterNode )
		{
			final ImplicitParameterNode endAsImplicitParameterNode = (ImplicitParameterNode) end;
			if(endAsImplicitParameterNode.getTopRectangle().contains(pPoint2)) // Case 4
			{
				final CallEdge edge = (CallEdge)pEdge;
				final String label = edge.getMiddleLabel();
				pOperation.add(new SimpleOperation(()-> edge.setMiddleLabel("\u00ABcreate\u00BB"),
						()-> edge.setMiddleLabel(label)));
			}
			else // Case 3
			{
				CallNode newCallNode = new CallNode();
				pEdge.connect(pEdge.getStart(), newCallNode, aDiagram);
				pOperation.add(new SimpleOperation(()-> endAsImplicitParameterNode.addChild(newCallNode, pPoint1),
						()-> endAsImplicitParameterNode.removeChild(newCallNode)));
			}
		}
	}
	
	@Override
	public DiagramOperation createAddNodeOperation(Node pNode, Point pRequestedPosition)
	{
		DiagramOperation result = null;
		if(pNode instanceof CallNode) 
		{
			ImplicitParameterNode target = insideTargetArea(pRequestedPosition);
			if( target != null )
			{
				result = new SimpleOperation(()-> target.addChild((ChildNode)pNode),
						()-> target.removeChild((ChildNode)pNode));
			}
		}
		if( result == null )
		{
			result = super.createAddNodeOperation(pNode, pRequestedPosition);
		}
		return result;
	}
	
	/*
	 * If pPoint is inside an ImplicitParameterNode but below its top
	 * rectangle, returns that node. Otherwise, returns null.
	 */
	private ImplicitParameterNode insideTargetArea(Point pPoint)
	{
		for( Node node : aDiagram.rootNodes() )
		{
			if(node instanceof ImplicitParameterNode && node.view().contains(pPoint))
			{
				if( !(pPoint.getY() < ((ImplicitParameterNode)node).getTopRectangle().getMaxY() + CALL_NODE_YGAP))
				{
					return (ImplicitParameterNode) node;
				}
			}
		}
		return null;
	}
}
