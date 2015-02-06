package ca.mcgill.cs.stg.jetuml.framework;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import ca.mcgill.cs.stg.jetuml.diagrams.ClassDiagramGraph;
import ca.mcgill.cs.stg.jetuml.diagrams.ObjectDiagramGraph;
import ca.mcgill.cs.stg.jetuml.diagrams.SequenceDiagramGraph;
import ca.mcgill.cs.stg.jetuml.diagrams.StateDiagramGraph;
import ca.mcgill.cs.stg.jetuml.diagrams.UseCaseDiagramGraph;
import ca.mcgill.cs.stg.jetuml.graph.CallEdge;
import ca.mcgill.cs.stg.jetuml.graph.ClassRelationshipEdge;
import ca.mcgill.cs.stg.jetuml.graph.Edge;
import ca.mcgill.cs.stg.jetuml.graph.Graph;
import ca.mcgill.cs.stg.jetuml.graph.GraphElement;
import ca.mcgill.cs.stg.jetuml.graph.Node;
import ca.mcgill.cs.stg.jetuml.graph.NoteEdge;
import ca.mcgill.cs.stg.jetuml.graph.ObjectReferenceEdge;
import ca.mcgill.cs.stg.jetuml.graph.ReturnEdge;
import ca.mcgill.cs.stg.jetuml.graph.StateTransitionEdge;

/**
 * @author JoelChev
 *
 */
public class CutPasteBehavior 
{


	/**
	 * A constructor for a CutPasteBehavior object.
	 */
	public CutPasteBehavior()
	{
	}
	
	/**
	 * @param pSelection The current SelectionList of GraphElements to be cut from the GraphPanel.
	 * @param pClipboard The Clipboard of the EditorFrame that is to receive pSelection.
	 * @param pGraph The Graph to be modified by the cut operation.
	 */
	public void cutBehavior(SelectionList pSelection, Clipboard pClipboard, Graph pGraph)
	{
		SelectionList newSelection = new SelectionList();
   		Map<Node, Node> originalAndClonedNodes = new LinkedHashMap<Node, Node>();
   		for(GraphElement element: pSelection)
   		{
   			if(element instanceof Node)
   			{
   				Node curNode = (Node) element;
   				Node cloneNode = curNode.clone();
   				newSelection.add(cloneNode);
   				originalAndClonedNodes.put(curNode, cloneNode);
   			}
   		}
   		for(GraphElement element: pSelection)
   		{
   			if(element instanceof Edge)
   			{
   				Edge curEdge = (Edge) element;
   				Node start = originalAndClonedNodes.get(curEdge.getStart());
	            Node end = originalAndClonedNodes.get(curEdge.getEnd());  
	            if (start != null && end != null)
	            {
	                Edge cloneEdge = (Edge) curEdge.clone();
	                cloneEdge.connect(start, end);
	                newSelection.add(cloneEdge);
	            }
   			}
   		}
   		if(pSelection.size()>0)
   		{
   			pClipboard.setContents(newSelection);
   		}
   		Iterator<GraphElement> iter = pSelection.iterator();
   		while(iter.hasNext())
   		{
   			GraphElement element = iter.next();
   			if(element instanceof Edge)
   			{
   				pGraph.removeEdge((Edge)element);
   			}
   			else
   			{
   				pGraph.removeNode((Node)element);
   			}
 			iter.remove();
   		}
	}
	
	/**
	 * @param pGraph the Current Graph passed in 
	 * @param pSelection the SelectionList from a previous cut or paste operation
	 * @return a new SelectionList of GraphElements to be selected on the GraphPanel.
	 */
	public SelectionList pasteBehavior(Graph pGraph, SelectionList pSelection)
	{
		Rectangle2D bounds = null;
		Node[]currentProtoTypes = pGraph.getNodePrototypes();
   		Edge[]currentEdgeTypes = pGraph.getEdgePrototypes();
		Iterator<GraphElement> nodeIter = pSelection.iterator();
			ArrayList<Node> copyNodes = new ArrayList<Node>();
			/*
         * Clone all nodes and remember the original-cloned correspondence
         */
        Map<Node, Node> originalAndClonedNodes = new LinkedHashMap<Node, Node>();
        /*
         * First clone all of the nodes and link them with the previous nodes. All the nodes will be iterated over in
         * the pastSelection SelectionList
         */
			while(nodeIter.hasNext())
			{
				GraphElement element = nodeIter.next();
				if(element instanceof Node)
				{
					Node curNode = (Node) element;
					for(Node n: currentProtoTypes)
					{
						if(curNode.getClass() == n.getClass())
						{
							Node newNode = curNode.clone();
							originalAndClonedNodes.put(curNode, newNode);
							copyNodes.add(newNode);
							if(bounds ==null)
							{
		   						bounds = curNode.getBounds();
		   					}
		   					else
		   					{
		   						bounds.add(curNode.getBounds());
		   					}
						}
					}
					
				}
			}
			/*
			 * Now the edges can be cloned as all the nodes have been cloned successfully at this point.
			 * The edges will be iterated over in the pastSelection SelectionList.
			 */
			Iterator<GraphElement>edgeIter = pSelection.iterator();
			ArrayList<Edge> copyEdges = new ArrayList<Edge>();
			while(edgeIter.hasNext())
			{
				GraphElement element = edgeIter.next();
				if(element instanceof Edge)
				{
					Edge curEdge = (Edge) element;
					for(Edge e: currentEdgeTypes)
					{
						if(checkEdgeEquality(curEdge, e, pGraph))
						{ 
			        /*
			         * Clone all edges that join copied nodes
			         */
			            Node start = originalAndClonedNodes.get(curEdge.getStart());
			            Node end = originalAndClonedNodes.get(curEdge.getEnd());  
			            if (start != null && end != null)
			            {
			                Edge e2 = (Edge) e.clone();
			                e2.connect(start, end);
			                copyEdges.add(e2);
			            }
						}
					}
				}	
			}
			/*
			 * updatedSelectionList is the selectionList to return.
			 */
			SelectionList updatedSelectionList = new SelectionList();
			for(Node cloneNode: copyNodes)
			{
				double x = cloneNode.getBounds().getX();
				double y = cloneNode.getBounds().getY();
				/*
				 * This translates all the new nodes and their respective edges over to the top left corner of the 
				 * GraphPanel.
				 */
				pGraph.add(cloneNode, new Point2D.Double(x-bounds.getX(), y-bounds.getY()));
				updatedSelectionList.add(cloneNode);
			}
			for(Edge cloneEdge: copyEdges)
			{
				pGraph.connect(cloneEdge, cloneEdge.getStart(), cloneEdge.getEnd());
			}
			return updatedSelectionList;
	}
	
	/**
	 * @param pEdge1 The copied or cut edge whose actual type needs to be determined.
	 * @param pEdge2 The edge from the list of edge types in the pGraph.
	 * @param pGraph The current graph in the GraphPanel.
	 * @return true if the two edges have the same type and false if not.
	 */
	public boolean checkEdgeEquality(Edge pEdge1, Edge pEdge2, Graph pGraph)
	{
		boolean equal = false;
		if (pEdge1 instanceof NoteEdge && pEdge2 instanceof NoteEdge)
		{
			equal = true;
		}
		if(pGraph instanceof ClassDiagramGraph || pGraph instanceof UseCaseDiagramGraph)
		{
			if(pEdge1 instanceof ClassRelationshipEdge && pEdge2 instanceof ClassRelationshipEdge)
			{
				equal = classDiagramEdgeEqual((ClassRelationshipEdge)pEdge1, (ClassRelationshipEdge)pEdge2, (ClassDiagramGraph)pGraph);
			}
		}
		else if(pGraph instanceof ObjectDiagramGraph)
		{
			if(pEdge1 instanceof ClassRelationshipEdge && pEdge2 instanceof ClassRelationshipEdge || 
					pEdge1 instanceof ObjectReferenceEdge && pEdge2 instanceof ObjectReferenceEdge)
			{
				equal = true;
			}
		}
		else if(pGraph instanceof SequenceDiagramGraph)
		{
			if(pEdge1 instanceof CallEdge && pEdge2 instanceof CallEdge || pEdge1 instanceof ReturnEdge && pEdge2 instanceof ReturnEdge)
			{
				equal = true;
			}
		}
		else if(pGraph instanceof StateDiagramGraph)
		{
			if(pEdge1 instanceof StateTransitionEdge && pEdge2 instanceof StateTransitionEdge)
			{
				equal = true;
			}
		}
		return equal;
	}
	
	/**
	 * @param pEdge1 The copied or cut Edge whose actual type needs to be determined.
	 * @param pEdge2 The edge from the list of ClassRelationshipEdges in the pGraph.
	 * @param pGraph The current ClassDiagramGraph in the GraphPanel
	 * @return true if the two edges have the same type, false otherwise.
	 */
	public boolean classDiagramEdgeEqual(ClassRelationshipEdge pEdge1, ClassRelationshipEdge pEdge2, ClassDiagramGraph pGraph)
	{
		if(pEdge1.getLineStyle() == pEdge2.getLineStyle())
		{
			if(pEdge1.getStartArrowHead() == pEdge2.getStartArrowHead())
			{
				if(pEdge1.getEndArrowHead()== pEdge2.getEndArrowHead())
				{
					if(pEdge1.getBentStyle() == pEdge2.getBentStyle())
					{
						if(pEdge1.getMiddleLabel().equals(pEdge2.getMiddleLabel()))
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
