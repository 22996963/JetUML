/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2016, 2017 by the contributors of the JetUML project.
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

import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * A tab that allow users to open new diagrams of the different types
 * or open recently saved diagrams.
 * 
 * @author JoelChev - Original Code
 * @author Kaylee I. Kutschera - Migration to JavaFX
 * @author Martin P. Robillard - Refactoring and new layout
 */
public class WelcomeTab extends Tab
{
	private static final ResourceBundle WELCOME_RESOURCES = ResourceBundle.getBundle("ca.mcgill.cs.jetuml.gui.EditorStrings");
    private VBox aLeftPanel;
    private VBox aRightPanel;
    
	/**
	 * @param pNewDiagramMap a map containing the name and handler corresponding to the creation of a new diagram.
	 * @param pRecentFilesMap a map containing the name and handler corresponding to opening a recent file.
	 */
	public WelcomeTab(Map<String, EventHandler<ActionEvent>> pNewDiagramMap, Map<String, EventHandler<ActionEvent>> pRecentFilesMap)
	{
		super(WELCOME_RESOURCES.getString("welcome.title"));
		setClosable(false);
		
		BorderPane layout = new BorderPane();
		layout.getStyleClass().add("welcome-tab");
		HBox shortcutPanel = new HBox();
		shortcutPanel.getStyleClass().add("panel");
		shortcutPanel.getChildren().addAll(getLeftPanel(pNewDiagramMap), getRightPanel(pRecentFilesMap));
		layout.setCenter(shortcutPanel);
		layout.setBottom(getFootTextPanel());
	    
	    setContent(layout);
	}
		
	private VBox getLeftPanel(Map<String, EventHandler<ActionEvent>> pNewDiagramMap)
	{
		if(aLeftPanel == null)
		{
			Label title = new Label(WELCOME_RESOURCES.getString("welcome.create.text"));

			HBox leftTitlePanel = new HBox();
			leftTitlePanel.getStyleClass().add("panel-title");
			leftTitlePanel.getChildren().addAll(title);

			aLeftPanel = new VBox();
			aLeftPanel.getStyleClass().add("panel-content");
			aLeftPanel.getChildren().add(leftTitlePanel);
			for(Map.Entry<String, EventHandler<ActionEvent>> entry : pNewDiagramMap.entrySet())
			{
				String label = entry.getKey();
				Button newDiagramShortcut = new Button(label.toLowerCase());
				newDiagramShortcut.setOnAction(entry.getValue());
				aLeftPanel.getChildren().add(newDiagramShortcut);
			}
		}
		return aLeftPanel;
	}
	
	private VBox getRightPanel(Map<String, EventHandler<ActionEvent>> pRecentFilesMap)
	{
		if(aRightPanel == null)
		{
			Label title = new Label(WELCOME_RESOURCES.getString("welcome.open.text"));

			HBox rightTitlePanel = new HBox();
			rightTitlePanel.getStyleClass().add("panel-title");
			rightTitlePanel.getChildren().add(title);

			aRightPanel = new VBox();
			aRightPanel.getStyleClass().add("panel-content");
			aRightPanel.getChildren().add(rightTitlePanel);
			
			for(Map.Entry<String, EventHandler<ActionEvent>> entry : pRecentFilesMap.entrySet())
			{
				String label = entry.getKey();
				Button fileShortcut = new Button(label.toLowerCase());
				fileShortcut.setOnAction(entry.getValue());
				aRightPanel.getChildren().add(fileShortcut);
			}
		}
		return this.aRightPanel;
	}

	private HBox getFootTextPanel()
	{
		HBox footTextPanel = new HBox();
		footTextPanel.getStyleClass().add("footer");
		footTextPanel.getChildren().add(new Label(WELCOME_RESOURCES.getString("welcome.copyright")));
		return footTextPanel;
	}
}	
