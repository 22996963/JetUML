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

import static ca.mcgill.cs.jetuml.application.ApplicationResources.RESOURCES;

import java.io.File;

import ca.mcgill.cs.jetuml.application.UserPreferences;
import ca.mcgill.cs.jetuml.diagram.Diagram;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;

/**
 *A tab holding a single diagram.
 */
public class DiagramTab extends Tab
{	
	private DiagramCanvas aDiagramCanvas;
	private final DiagramCanvasController aDiagramCanvasController;
	private File aFile; // The file associated with this diagram
	
	/**
     * Constructs a diagram tab initialized with pDiagram.
     * @param pDiagram The initial diagram
	 */
	public DiagramTab(Diagram pDiagram)
	{
		DiagramTabToolBar sideBar = new DiagramTabToolBar(pDiagram);
		UserPreferences.instance().addBooleanPreferenceChangeHandler(sideBar);
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		aDiagramCanvas = new DiagramCanvas(pDiagram, (int) bounds.getWidth(), (int)bounds.getHeight());
		UserPreferences.instance().addBooleanPreferenceChangeHandler(aDiagramCanvas);
		aDiagramCanvasController = new DiagramCanvasController(aDiagramCanvas, sideBar);
		aDiagramCanvas.setController(aDiagramCanvasController);
		aDiagramCanvas.paintPanel();
		
		BorderPane layout = new BorderPane();
		layout.setRight(sideBar);

		// We put the diagram in a stack pane for the sole
		// purpose of being able to decorate it with CSS
		StackPane pane = new StackPane(aDiagramCanvas);
		final String cssDefault = "-fx-border-color: grey;\n"
				+ "-fx-border-insets: 4;\n"
				+ "-fx-border-width: 1;\n"
				+ "-fx-border-style: solid;\n";
		pane.setStyle(cssDefault);
		ScrollPane scroll = new ScrollPane(pane);
		
		// The call below is necessary to removes the focus highlight around the Canvas
		// See issue #250
		scroll.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;"); 

		// We need to set the max size of the scroll pane so it do not expand to fill 
		// the entire center region of the BorderPane, as dictated by this layout for
		// resizable nodes.
		final int buffer = 12; // (border insets + border width + 1)*2
		scroll.setMaxSize(aDiagramCanvas.getWidth() + buffer, aDiagramCanvas.getHeight() + buffer);
		layout.setCenter(scroll);
		
		setTitle(false);
		setContent(layout);

		setOnCloseRequest(pEvent -> 
		{
			pEvent.consume();
			EditorFrame editorFrame = (EditorFrame) getTabPane().getParent();
			editorFrame.close(this);
		});
	}
	
	/**
	 * This method should be called immediately before closing the tab.
	 */
	public void close()
	{
		UserPreferences.instance().removeBooleanPreferenceChangeHandler(aDiagramCanvas);
		UserPreferences.instance().removeBooleanPreferenceChangeHandler((DiagramTabToolBar)((BorderPane)getContent()).getRight());
	}

	/**
     * @return The diagram being edited within this tab.
	 */
	public Diagram getDiagram()
	{
		return aDiagramCanvas.getDiagram();
	}
	
	/**
	 * Copy the current selection to the clipboard.
	 */
	public void copy()
	{
		aDiagramCanvasController.copy();
	}
	
	/**
	 * Cuts the current selection to the clip board.
	 */
	public void cut()
	{
		aDiagramCanvasController.cut();
	}
	
	/**
	 * Pastes the current clip board content to the diagram.
	 */
	public void paste()
	{
		aDiagramCanvasController.paste();
	}
	
	/**
	 * Open a dialog to edit the properties of the currently selected element.
	 */
	public void editSelected()
	{
		aDiagramCanvasController.editSelected();
	}
	
	/**
	 * Undoes the last command.
	 */
	public void undo()
	{
		aDiagramCanvasController.undo();
	}
	
	/**
	 * Redoes the last undone command.
	 */
	public void redo()
	{
		aDiagramCanvasController.redo();
	}
	
	/**
	 * Copy the current selection to the clipboard.
	 */
	public void removeSelected()
	{
		aDiagramCanvasController.removeSelected();
	}
	
	/**
	 * Selects all elements in the diagram.
	 */
	public void selectAll()
	{
		aDiagramCanvasController.selectAll();
	}
	
	/**
     * Gets the graph panel that is contained in this frame.
     * @return the graph panel
	 */
	public DiagramCanvas getGraphPanel()
   	{
		return aDiagramCanvas;
   	}
	
	/**
	 * Sets the title of the frame as the file name if there
	 * is a file name. 
	 * 
	 * @param pModified If the file is in modified (unsaved) state,
	 * appends an asterisk to the frame title.
	 */
	public void setTitle(boolean pModified)
	{
		if(aFile != null)
		{
			String title = aFile.getName();
			if(pModified)
			{
				if(!getText().endsWith("*"))
				{
					setText(title + "*");
				}
			}
			else
			{
				setText(title);
			}
		}
		else
		{
			setText(RESOURCES.getString(getDiagram().getClass().getSimpleName().toLowerCase() + ".text"));
		}
	}
	
	/**
	 * @return True if the diagram is flagged as modified.
	 */
	public boolean isModified()
	{
		return aDiagramCanvasController.isModified();
	}
	
	/**
	 * Sets whether the corresponding diagram should be flagged as modified.
	 * 
	 * @param pModified True if the diagram should be flagged as modified.
	 */
	public void setModified(boolean pModified)
	{
		aDiagramCanvasController.setModified(pModified);
	}

	/**
     * Gets the file property.
     * @return the file associated with this graph
	 */
	public File getFile()
	{
		return aFile;
	}

	/**
     * Sets the file property.
     * @param pFile The file associated with this graph
	 */
	public void setFile(File pFile)
	{
		aFile = pFile;
		setTitle(false);
	}
}	        
