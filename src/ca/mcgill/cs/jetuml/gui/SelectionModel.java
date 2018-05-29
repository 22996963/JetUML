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
package ca.mcgill.cs.jetuml.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import ca.mcgill.cs.jetuml.diagram.DiagramData;
import ca.mcgill.cs.jetuml.diagram.DiagramElement;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.Node;
import ca.mcgill.cs.jetuml.diagram.nodes.ChildNode;
import ca.mcgill.cs.jetuml.diagram.nodes.ParentNode;
import ca.mcgill.cs.jetuml.geom.Line;
import ca.mcgill.cs.jetuml.geom.Rectangle;

/**
 * Encapsulates all state related to the selection feature of a diagram canvas.
 * Conceptually, a selection model comprises three components:
 * 
 * 1. A list of selected elements.
 * 2. Optionally, a rubberband tool used to select two nodes for drawing an edge
 * 3. Optionally, a lasso tool used to select any element within a region on a canvas.
 */
public class SelectionModel implements Iterable<DiagramElement>
{
	private final SelectionObserver aObserver;
	
	private Stack<DiagramElement> aSelected = new Stack<>();
	private Optional<Line> aRubberband = Optional.empty();
	private Optional<Rectangle> aLasso = Optional.empty();
	
	/**
	 * Creates a new selection model with a single observer.
	 * 
	 * @param pObserver The observer for this model.
	 */
	public SelectionModel(SelectionObserver pObserver)
	{
		aObserver = pObserver;
	}
	
	/**
	 * Clears the selection model and selects all root nodes and 
	 * edges in the diagram. Triggers a notification.
	 * 
	 * @param pDiagramData Provides the data for the selection.
	 * @pre pDiagramData != null
	 */
	public void selectAll(DiagramData pDiagramData)
	{
		assert pDiagramData != null;
		clearSelection();
		pDiagramData.allElements().forEach(this::addToSelection);
		aObserver.selectionModelChanged();
	}
	
	/**
	 * @return A rectangle that represents the bounding
	 * box of the entire selection.
	 */
	public Rectangle getSelectionBounds()
	{
		Optional<DiagramElement> lastSelected = getLastSelected();
		assert lastSelected.isPresent();
		Rectangle bounds = lastSelected.get().view().getBounds();
		for(DiagramElement selected : aSelected )
		{
			bounds = bounds.add(selected.view().getBounds());
		}
		return bounds;
	}
	
	/**
	 * @return An iterable of all selected nodes. This 
	 * corresponds to the entire selection, except the edge.
	 */
	public Iterable<Node> getSelectedNodes()
	{
		List<Node> result = new ArrayList<>();
		for( DiagramElement element : aSelected )
		{
			if( element instanceof Node )
			{
				result.add((Node) element);
			}
		}
		return result;
	}
	
	/**
	 * Records information about an active lasso selection tool, select all elements
	 * in the lasso, and triggers a notification.
	 * 
	 * @param pLasso The bounds of the current lasso.
	 * @param pDiagramData Data about the diagram whose elements are being selected with the lasso.
	 * @param pAddMode If true, elements in the lasso are added to any existing selection. Otherwise, 
	 * only the elements in the lasso are selected.
	 * @pre pLasso != null;
	 * @pre pDiagramData != null;
	 */
	public void activateLasso(Rectangle pLasso, DiagramData pDiagramData, boolean pAddMode)
	{
		assert pLasso != null;
		aLasso = Optional.of(pLasso);
		pDiagramData.rootNodes().forEach( node -> selectNode(pAddMode, node, pLasso));
		pDiagramData.edges().forEach( edge -> selectEdge(pAddMode, edge, pLasso));
		aObserver.selectionModelChanged();
	}
	
	private void selectNode(boolean pCtrl, Node pNode, Rectangle pLasso)
	{
		if(!pCtrl && !pLasso.contains(pNode.view().getBounds())) 
		{
			removeFromSelection(pNode);
		}
		else if(pLasso.contains(pNode.view().getBounds())) 
		{
			addToSelection(pNode);
		}
		if(pNode instanceof ParentNode)
		{
			for (ChildNode child : ((ParentNode) pNode).getChildren())
			{
				selectNode(pCtrl, child, pLasso);
			}
		}
	}
	
	/*
	 * Edges need to be added too when highlighted, but only if both their endpoints have been highlighted.
	 */
	private void selectEdge( boolean pAddMode, Edge pEdge, Rectangle pLasso )
	{
		if(!pAddMode && !pLasso.contains(pEdge.view().getBounds()))
		{
			removeFromSelection(pEdge);
		}
		else if(pLasso.contains(pEdge.view().getBounds()))
		{
			if(transitivelyContains(pEdge.getStart()) && transitivelyContains(pEdge.getEnd()))
			{
				addToSelection(pEdge);
			}
		}		
	}
	
	/**
	 * @return The active lasso, if available.
	 */
	public Optional<Rectangle> getLasso()
	{
		return aLasso;
	}
	
	/**
	 * Removes the active lasso from the model and triggers a notification.
	 */
	public void deactivateLasso()
	{
		aLasso = Optional.empty();
		aObserver.selectionModelChanged();
	}
	
	/**
	 * Records information about an active rubberband selection tool and triggers a notification.
	 * @param pLine The line that represents the rubberband.
	 * @pre pLine != null;
	 */
	public void activateRubberband(Line pLine)
	{
		assert pLine != null;
		aRubberband = Optional.of(pLine);
		aObserver.selectionModelChanged();
	}
	
	
	/**
	 * @return The active rubberband, if available.
	 */
	public Optional<Line> getRubberband()
	{
		return aRubberband;
	}
	
	/**
	 * Removes the active rubberband from the model and triggers a notification.
	 */
	public void deactivateRubberband()
	{
		aRubberband = Optional.empty();
		aObserver.selectionModelChanged();
	}
	
	/**
	 * Clears any existing selection and initializes it with pNewSelection.
	 * Triggers a notification.
	 * 
	 * @param pNewSelection A list of elements to select.
	 * @pre pNewSelection != null;
	 */
	public void setSelectionTo(List<DiagramElement> pNewSelection)
	{
		assert pNewSelection != null;
		clearSelection();
		pNewSelection.forEach(aSelected::add);
		aObserver.selectionModelChanged();
	}
	
	/**
	 * Adds an element to the selection set and sets
	 * it as the last selected element. If the element 
	 * is already in the list, it is added to the end 
	 * of the list. If the node is transitively a child of 
	 * any node in the list, it is not added.
	 * 
	 * @param pElement The element to add to the list.
	 * Cannot be null.
	 */
	public void addToSelection(DiagramElement pElement)
	{
		assert pElement != null;
		if( !containsParent( pElement ))
		{
			aSelected.remove(pElement);
			aSelected.push(pElement);
			
			// Remove children in case a parent was added.
			ArrayList<DiagramElement> toRemove = new ArrayList<>();
			for( DiagramElement element : aSelected )
			{
				if( containsParent(element) )
				{
					toRemove.add(element);
				}
			}
			for( DiagramElement element : toRemove )
			{
				removeFromSelection(element);
			}
		}
	}
	
	/*
	 * Returns true if any of the parents of pElement is contained
	 * (transitively).
	 * @param pElement The element to test
	 * @return true if any of the parents of pElement are included in the 
	 * selection.
	 */
	private boolean containsParent(DiagramElement pElement)
	{
		if( pElement instanceof ChildNode )
		{
			ParentNode parent = ((ChildNode) pElement).getParent();
			if( parent == null )
			{
				return false;
			}
			else if( aSelected.contains(parent))
			{
				return true;
			}
			else
			{
				return containsParent(parent);
			}
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * @param pElement The element to test.
	 * @return True if either this element or one of its parent is contained.
	 */
	private boolean transitivelyContains(DiagramElement pElement)
	{
		return contains(pElement) || containsParent(pElement);
	}
	
	/**
	 * Removes all selections.
	 */
	public void clearSelection()
	{
		aSelected.clear();
	}
	
	/**
	 * @return The last element that was selected, if present.
	 */
	public Optional<DiagramElement> getLastSelected()
	{
		if( aSelected.isEmpty() )
		{
			return Optional.empty();
		}
		else
		{
			return Optional.of(aSelected.peek());
		}
	}
	
	/**
	 * Include in the selection list any edge in pEdges whose start and end nodes
	 * are already in the selection list, and replaces the previously last element
	 * in the selection to last place.
	 * @param pEdges The edges to consider adding.
	 */
	public void addEdgesIfContained(Iterable<Edge> pEdges)
	{
		if( aSelected.isEmpty() )
		{
			return;
		}
		DiagramElement last = aSelected.pop();
		for( Edge edge : pEdges )
		{
			if( capturesEdge(edge))
			{
				addToSelection(edge);
			}
		}
		aSelected.push(last);
	}
	
	/**
	 * @param pElement The element to test.
	 * @return True if pElement is in the list of selected elements.
	 */
	public boolean contains(DiagramElement pElement)
	{
		return aSelected.contains(pElement);
	}
	
	/**
	 * @param pEdge The edge to test.
	 * @return true iif the selection contains both end-points of pEdge, or their parent.
	 */
	public boolean capturesEdge(Edge pEdge)
	{
		return (contains(pEdge.getStart()) || containsParent(pEdge.getStart())) &&
				(contains(pEdge.getEnd()) || containsParent(pEdge.getEnd()));
	}
	
	/**
	 * Removes pElement from the list of selected elements,
	 * or does nothing if pElement is not selected.
	 * @param pElement The element to remove.
	 * @pre pElement != null;
	 */
	public void removeFromSelection(DiagramElement pElement)
	{
		assert pElement != null;
		aSelected.remove(pElement);
	}
	
	/**
	 * Sets pElement as the single selected element.
	 * @param pElement The element to set as selected. Cannot
	 * be null.
	 */
	public void set(DiagramElement pElement)
	{
		assert pElement != null;
		aSelected.clear();
		aSelected.add(pElement);
	}

	@Override
	public Iterator<DiagramElement> iterator()
	{
		return aSelected.iterator();
	}
	
	/**
	 * @return True if there is not element in the selection list.
	 */
	public boolean isEmpty()
	{
		return aSelected.isEmpty();
	}
}
