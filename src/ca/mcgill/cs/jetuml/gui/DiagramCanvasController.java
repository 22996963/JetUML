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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ca.mcgill.cs.jetuml.application.Clipboard;
import ca.mcgill.cs.jetuml.application.MoveTracker;
import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.DiagramElement;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.Node;
import ca.mcgill.cs.jetuml.diagram.builder.CompoundOperation;
import ca.mcgill.cs.jetuml.diagram.builder.DiagramOperationProcessor;
import ca.mcgill.cs.jetuml.diagram.nodes.ChildNode;
import ca.mcgill.cs.jetuml.diagram.nodes.ParentNode;
import ca.mcgill.cs.jetuml.geom.Line;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * An instance of this class is responsible to handle the user
 * interface events on a diagram canvas.
 */
public class DiagramCanvasController
{
	private enum DragMode 
	{ DRAG_NONE, DRAG_MOVE, DRAG_RUBBERBAND, DRAG_LASSO }
	
	private static final int CONNECT_THRESHOLD = 8;
	
	private final SelectionModel aSelectionModel;
	private final MoveTracker aMoveTracker = new MoveTracker();
	private final DiagramCanvas aCanvas;
	private final DiagramTabToolBar aToolBar;
	private DragMode aDragMode;
	private Point aLastMousePoint;
	private Point aMouseDownPoint;  
//	private UndoManager aUndoManager = new UndoManager();	
	private DiagramOperationProcessor aProcessor = new DiagramOperationProcessor();
	private boolean aModified = false;
	private MouseDraggedGestureHandler aHandler;

	
	/**
	 * Creates a new controller.
	 * @param pCanvas The canvas being controlled
	 * @param pToolBar The toolbar.
	 * @param pHandler A handler for when the mouse is dragged
	 */
	public DiagramCanvasController(DiagramCanvas pCanvas, DiagramTabToolBar pToolBar, MouseDraggedGestureHandler pHandler)
	{
		aCanvas = pCanvas;
		aSelectionModel = new SelectionModel(aCanvas);
		aToolBar = pToolBar;
		aCanvas.setOnMousePressed(e -> mousePressed(e));
		aCanvas.setOnMouseReleased(e -> mouseReleased(e));
		aCanvas.setOnMouseDragged( e -> mouseDragged(e));
		aHandler = pHandler;
	}
	
	/**
	 * Removes any element in the selection model that is not in the diagram.
	 * TODO a hack which will hopefully be factored out.
	 */
	public void synchronizeSelectionModel()
	{
		Set<DiagramElement> toBeRemoved = new HashSet<>();
		for(DiagramElement selected : aSelectionModel )
		{
			if(!aCanvas.getDiagram().contains(selected)) 
			{
				toBeRemoved.add(selected);
			}
		}

		toBeRemoved.forEach( element -> aSelectionModel.removeFromSelection(element));            
	}
	
	/**
	 * @return The selection model associated with this controller
	 */
	public SelectionModel getSelectionModel()
	{
		return aSelectionModel;
	}
	
	/**
	 * @return The diagram associated with this controller.
	 */
	public Diagram getDiagram()
	{
		return aCanvas.getDiagram();
	}
	
	/**
	 * Checks whether this graph has been modified since it was last saved.
	 * @return true if the graph has been modified
	 */
	public boolean isModified()
	{	
		return aModified;
	}

	/**
	 * Sets or resets the modified flag for this graph.
	 * @param pModified true to indicate that the graph has been modified
	 */
	public void setModified(boolean pModified)
	{
		aModified = pModified;
	}
	
	/**
	 * Edits the properties of the selected graph element.
	 */
	public void editSelected()
	{
		Optional<DiagramElement> edited = aSelectionModel.getLastSelected();
		if( edited.isPresent() )
		{
			PropertyEditorDialog dialog = new PropertyEditorDialog((Stage)aCanvas.getScene().getWindow(), 
					edited.get(), ()-> {aCanvas.getDiagram().requestLayout(); aCanvas.paintPanel(); });
			
			CompoundOperation operation = dialog.show();
			if(!operation.isEmpty())
			{
				aProcessor.storeAlreadyExecutedOperation(operation);
				setModified(true);
			}
		}
	}
	
	/**
	 * Pastes the content of the clip board into the graph managed by this panel.
	 */
	public void paste()
	{
		Iterable<DiagramElement> newElements = Clipboard.instance().getElements();
		aProcessor.executeNewOperation(aCanvas.getDiagram().builder().createAddElementsOperation(newElements));
		List<DiagramElement> newElementList = new ArrayList<>();
		for( DiagramElement element : newElementList )
		{
			newElementList.add(element);
		}
		aSelectionModel.setSelectionTo(newElementList);
		aCanvas.paintPanel();
	}
	
	/**
	 * Undoes the most recent command.
	 * If the UndoManager performs a command, the method 
	 * it calls will repaint on its own
	 */
	public void undo()
	{
		if( aProcessor.canUndo() )
		{
			aProcessor.undoLastExecutedOperation();
			aCanvas.paintPanel();
		}
	}
	
	/**
	 * Removes the last undone action and performs it.
	 * If the UndoManager performs a command, the method 
	 * it calls will repaint on its own
	 */
	public void redo()
	{
		if( aProcessor.canRedo() )
		{
			aProcessor.redoLastUndoneOperation();
			aCanvas.paintPanel();
		}
	}
	
	/**
	 * Copy the currently selected elements to the clip board.
	 */
	public void copy()
	{
		Clipboard.instance().copy(aSelectionModel);
	}
	
	/**
	 * Removes the selected graph elements.
	 */
	public void removeSelected()
	{
		aProcessor.executeNewOperation(aCanvas.getDiagram().builder().createDeleteElementsOperation(aSelectionModel));
		aSelectionModel.clearSelection();
		aCanvas.paintPanel();
	}
	
	/**
	 * Copy the currently selected elements to the clip board and removes them
	 * from the graph managed by this panel.
	 */
	public void cut()
	{
		Clipboard.instance().copy(aSelectionModel);
		removeSelected();
	}
	
	private Line computeRubberband()
	{
		return new Line(new Point(aMouseDownPoint.getX(), aMouseDownPoint.getY()), 
				new Point(aLastMousePoint.getX(), aLastMousePoint.getY()));
	}
	
	private Rectangle computeLasso()
	{
		return new Rectangle((int) Math.min(aMouseDownPoint.getX(), aLastMousePoint.getX()), 
						     (int) Math.min(aMouseDownPoint.getY(), aLastMousePoint.getY()), 
						     (int) Math.abs(aMouseDownPoint.getX() - aLastMousePoint.getX()) , 
						     (int) Math.abs(aMouseDownPoint.getY() - aLastMousePoint.getY()));
	}
	
	private Point getMousePoint(MouseEvent pEvent)
	{
		return new Point((int)pEvent.getX(), (int)pEvent.getY());
	}

	/*
	 * Will return null if nothing is selected.
	 */
	private DiagramElement getSelectedElement(MouseEvent pEvent)
	{
		Point mousePoint = getMousePoint(pEvent);
		DiagramElement element = aCanvas.getDiagram().findEdge(mousePoint);
		if (element == null)
		{
			element = aCanvas.getDiagram().findNode(new Point(mousePoint.getX(), mousePoint.getY())); 
		}
		return element;
	}

	private void handleSelection(MouseEvent pEvent)
	{
		DiagramElement element = getSelectedElement(pEvent);
		if (element != null) // Something is selected
		{
			if (pEvent.isControlDown())
			{
				if (!aSelectionModel.contains(element))
				{
					aSelectionModel.addToSelection(element);
				}
				else
				{
					aSelectionModel.removeFromSelection(element);
				}
			}
			else if (!aSelectionModel.contains(element))
			{
				// The test is necessary to ensure we don't undo multiple selections
				aSelectionModel.set(element);
			}
			aDragMode = DragMode.DRAG_MOVE;
			aMoveTracker.startTrackingMove(aSelectionModel);
		}
		else // Nothing is selected
		{
			if (!pEvent.isControlDown()) 
			{
				aSelectionModel.clearSelection();
			}
			aDragMode = DragMode.DRAG_LASSO;
		}
	}

	private void handleSingleClick(MouseEvent pEvent)
	{
		Optional<DiagramElement> tool = getTool(pEvent);
		if(!tool.isPresent())
		{
			handleSelection(pEvent);
		}
		else if(tool.get() instanceof Node)
		{
			handleNodeCreation(pEvent);
		}
		else if(tool.get() instanceof Edge)
		{
			handleEdgeStart(pEvent);
		}
	}
	
	private void handleNodeCreation(MouseEvent pEvent)
	{
		assert aToolBar.getCreationPrototype().isPresent();
		Node newNode = ((Node) aToolBar.getCreationPrototype().get()).clone();
		Point point = getMousePoint(pEvent);
		if(aCanvas.getDiagram().builder().canAdd(newNode, point))
		{
			aProcessor.executeNewOperation(aCanvas.getDiagram().builder().createAddNodeOperation(newNode, 
					new Point(point.getX(), point.getY()), (int) aCanvas.getWidth(), (int) aCanvas.getHeight()));
			setModified(true);
			aSelectionModel.set(newNode);
			aCanvas.paintPanel();
		}
		else // Special behavior, if we can't add a node, we select any element at the point
		{
			handleSelection(pEvent);
		}
	}

	private void handleEdgeStart(MouseEvent pEvent)
	{
		DiagramElement element = getSelectedElement(pEvent);
		if(element != null && element instanceof Node) 
		{
			aDragMode = DragMode.DRAG_RUBBERBAND;
		}
	}

	/*
	 * Implements a convenience feature. Normally returns 
	 * aSideBar.getSelectedTool(), except if the mouse points
	 * to an existing node, in which case defaults to select
	 * mode because it's likely the user wanted to select the element
	 * and forgot to switch tool. The only exception is when adding
	 * children nodes, where the parent node obviously has to be selected.
	 */
	private Optional<DiagramElement> getTool(MouseEvent pEvent)
	{
		Optional<DiagramElement> tool = aToolBar.getCreationPrototype();
		DiagramElement selected = getSelectedElement(pEvent);

		if( tool.isPresent() && tool.get() instanceof Node)
		{
			if(selected != null && selected instanceof Node)
			{
				if(!(tool.get() instanceof ChildNode && selected instanceof ParentNode))
				{
					aToolBar.setToolToBeSelect();
					tool = Optional.empty();
				}
			}
		}	
		return tool;
	}
	
	/**
	 * Select all elements in the diagram.
	 */
	public void selectAll()
	{
		aToolBar.setToolToBeSelect();
		aSelectionModel.selectAll(aCanvas.getDiagram());
	}

	private void mousePressed(MouseEvent pEvent)
	{
		if( pEvent.isSecondaryButtonDown() )
		{
			aToolBar.showPopup(pEvent.getScreenX(), pEvent.getScreenY());
		}
		else if( pEvent.getClickCount() > 1 )
		{
			editSelected();
		}
		else
		{
			handleSingleClick(pEvent);
		}
		Point point = getMousePoint(pEvent);
		aLastMousePoint = new Point(point.getX(), point.getY()); 
		aMouseDownPoint = aLastMousePoint;
		aCanvas.paintPanel();
	}

	private void mouseReleased(MouseEvent pEvent)
	{
		if (aDragMode == DragMode.DRAG_RUBBERBAND)
		{
			releaseRubberband(getMousePoint(pEvent));
		}
		else if(aDragMode == DragMode.DRAG_MOVE)
		{
			releaseMove();
		}
		else if( aDragMode == DragMode.DRAG_LASSO )
		{
			aSelectionModel.deactivateLasso();
		}
		aDragMode = DragMode.DRAG_NONE;
	}
	
	private void releaseRubberband(Point pMousePoint)
	{
		assert aToolBar.getCreationPrototype().isPresent();
		Edge newEdge = (Edge) ((Edge) aToolBar.getCreationPrototype().get()).clone();
		if(pMousePoint.distance(aMouseDownPoint) > CONNECT_THRESHOLD )
		{
			if( aCanvas.getDiagram().builder().canAdd(newEdge, aMouseDownPoint, pMousePoint))
			{
				aProcessor.executeNewOperation(aCanvas.getDiagram().builder().createAddEdgeOperation(newEdge, 
						aMouseDownPoint, pMousePoint));
				setModified(true);
				aSelectionModel.set(newEdge);
				aCanvas.paintPanel();
			}
		}
		aSelectionModel.deactivateRubberband();
	}
	
	private void releaseMove()
	{
		// For optimization purposes, some of the layouts are not done on every move event.
		aCanvas.getDiagram().requestLayout();
		setModified(true);
		CompoundOperation operation = aMoveTracker.endTrackingMove(aCanvas.getDiagram());
		if(!operation.isEmpty())
		{
			aProcessor.storeAlreadyExecutedOperation(operation);
		}
		aCanvas.paintPanel();
	}

	private void mouseDragged(MouseEvent pEvent)
	{
		Point mousePoint = getMousePoint(pEvent);
		Point pointToReveal = mousePoint;
		if(aDragMode == DragMode.DRAG_MOVE ) 
		{
			pointToReveal = computePointToReveal(mousePoint);
			moveSelection(mousePoint);
		}
		else if(aDragMode == DragMode.DRAG_LASSO)
		{
			aLastMousePoint = mousePoint;
			if( !pEvent.isControlDown() )
			{
				aSelectionModel.clearSelection();
			}
			aSelectionModel.activateLasso(computeLasso(), aCanvas.getDiagram());
		}
		else if(aDragMode == DragMode.DRAG_RUBBERBAND)
		{
			aLastMousePoint = mousePoint;
			aSelectionModel.activateRubberband(computeRubberband());
		}
		aHandler.interactionTo(pointToReveal);
	}
	
	// finds the point to reveal based on the entire selection
	private Point computePointToReveal(Point pMousePoint)
	{
		Rectangle bounds = aSelectionModel.getSelectionBounds();
		int x = bounds.getMaxX();
		int y = bounds.getMaxY();
		
		if( pMousePoint.getX() < aLastMousePoint.getX()) 	 // Going left, reverse coordinate
		{
			x = bounds.getX(); 
		}
		if( pMousePoint.getY() < aLastMousePoint.getY())	// Going up, reverse coordinate
		{
			y = bounds.getY(); 
		}
		return new Point(x, y);
	}
	
	// TODO, include edges between selected nodes in the bounds check.
	// This will be doable by collecting all edges connected to a transitively selected node.
	private void moveSelection(Point pMousePoint)
	{
		int dx = (int)(pMousePoint.getX() - aLastMousePoint.getX());
		int dy = (int)(pMousePoint.getY() - aLastMousePoint.getY());

		// Ensure the selection does not exceed the canvas bounds
		Rectangle bounds = aSelectionModel.getSelectionBounds();
		dx = Math.max(dx, -bounds.getX());
		dy = Math.max(dy, -bounds.getY());
		dx = Math.min(dx, (int) aCanvas.getWidth() - bounds.getMaxX());
		dy = Math.min(dy, (int) aCanvas.getHeight() - bounds.getMaxY());

		for(Node selected : aSelectionModel.getSelectedNodes())
		{
			selected.translate(dx, dy);
		}
		aLastMousePoint = pMousePoint; 
		aCanvas.paintPanel();
	}
}