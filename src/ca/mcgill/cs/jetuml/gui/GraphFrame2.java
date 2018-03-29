/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2015-2017 by the contributors of the JetUML project.
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

import java.io.File;

import ca.mcgill.cs.jetuml.diagrams.ClassDiagramGraph2;
import ca.mcgill.cs.jetuml.diagrams.ObjectDiagramGraph2;
import ca.mcgill.cs.jetuml.diagrams.SequenceDiagramGraph2;
import ca.mcgill.cs.jetuml.diagrams.StateDiagramGraph2;
import ca.mcgill.cs.jetuml.diagrams.UseCaseDiagramGraph2;
import ca.mcgill.cs.jetuml.graph.Graph2;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;

/**
 *A frame for showing a graphical editor.
 *
 *@author Kaylee I. Kutschera - Migration to JavaFX
 */
public class GraphFrame2 extends Tab
{	
	private TabPane aTabbedPane;
	private GraphPanel2 aPanel;
	private File aFile; // The file associated with this graph
	
	/**
     * Constructs a graph frame with an empty tool bar.
     * @param pGraph the initial graph
     * @param pTabbedPane the TabPane associated with this GraphFrame.
	 */
	public GraphFrame2(Graph2 pGraph, TabPane pTabbedPane)
	{
		aTabbedPane = pTabbedPane;
		ToolBar2 sideBar = new ToolBar2(pGraph);
		aPanel = new GraphPanel2(pGraph, sideBar, Screen.getPrimary().getVisualBounds());
		aPanel.paintPanel();
		
		BorderPane layout = new BorderPane();
		layout.setRight(sideBar);
		ScrollPane scroll = new ScrollPane(aPanel);
		scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		scroll.setVbarPolicy(ScrollBarPolicy.NEVER);
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
     * Gets the graph that is being edited in this frame.
     * @return the graph
	 */
	public Graph2 getGraph()
	{
		return aPanel.getGraph();
	}

	/**
     * Gets the graph panel that is contained in this frame.
     * @return the graph panel
	 */
	public GraphPanel2 getGraphPanel()
   	{
		return aPanel;
   	}
	
	/**
	 * This association and getter method are needed to display messages using the copy to clipboard
	 * functionality of the Optional ToolBar.
	 * @return aTabbedPane the TabPane associated with this GraphFrame.
	 */
	public TabPane getTabbedPane()
	{
		return aTabbedPane;
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
			Graph2 graphType = getGraph();
			if (graphType instanceof UseCaseDiagramGraph2)
			{
				setText("Use Case Diagram 2");
			} 
			else if (graphType instanceof StateDiagramGraph2)
			{
				setText("State Diagram 2");
			} 
			else if (graphType instanceof ClassDiagramGraph2)
			{
				setText("Class Diagram 2");
			} 
			else if (graphType instanceof SequenceDiagramGraph2)
			{
				setText("Sequence Diagram 2");
			} 
			else if (graphType instanceof ObjectDiagramGraph2)
			{
				setText("Object Diagram 2");
			} 
			else 
			{
				setText("Not supported in JavaFX");
			}
		}
	}

	/**
     * Gets the file property.
     * @return the file associated with this graph
	 */
	public File getFileName()
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
