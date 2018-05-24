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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import ca.mcgill.cs.jetuml.UMLEditor;
import ca.mcgill.cs.jetuml.application.FileExtensions;
import ca.mcgill.cs.jetuml.application.RecentFilesQueue;
import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.DiagramType;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.persistence.DeserializationException;
import ca.mcgill.cs.jetuml.persistence.PersistenceService;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * The main frame that contains panes that contain diagrams.
 */
public class EditorFrame extends BorderPane
{
	private static final int MARGIN_IMAGE = 2; // Number of pixels to leave around the diagram when exporting as image
	
	private Stage aMainStage;
	private RecentFilesQueue aRecentFiles = new RecentFilesQueue();
	private Menu aRecentFilesMenu;
	private WelcomeTab aWelcomeTab;

	/**
	 * Constructs a blank frame with a desktop pane but no diagram window.
	 * 
	 * @param pMainStage The main stage used by the UMLEditor
	 */
	public EditorFrame(Stage pMainStage) 
	{
		aMainStage = pMainStage;
		aRecentFiles.deserialize(Preferences.userNodeForPackage(UMLEditor.class).get("recent", "").trim());

		MenuBar menuBar = new MenuBar();
		setTop(menuBar);
		
		TabPane tabPane = new TabPane();
		tabPane.getSelectionModel().selectedItemProperty().addListener((pValue, pOld, pNew) -> setMenuVisibility());
		setCenter( tabPane );
	
		List<NewDiagramHandler> newDiagramHandlers = createNewDiagramHandlers();
		createFileMenu(menuBar, newDiagramHandlers);
		createEditMenu(menuBar);
		createViewMenu(menuBar);
		createHelpMenu(menuBar);
		setMenuVisibility();
		
		aWelcomeTab = new WelcomeTab(newDiagramHandlers);
		showWelcomeTabIfNecessary();
	}
	
	/*
	 * Traverses all menu items up to the second level (top level
	 * menus and their immediate sub-menus), that have "true" in their user data,
	 * indicating that they should only be enabled if there is a diagram 
	 * present. Then, sets their visibility to the boolean value that
	 * indicates whether there is a diagram present.
	 * 
	 * This method assumes that any sub-menu beyond the second level (sub-menus of
	 * top menus) will NOT be diagram-specific.
	 */
	private void setMenuVisibility()
	{
			((MenuBar)getTop()).getMenus().stream() // All top level menus
				.flatMap(menu -> Stream.concat(Stream.of(menu), menu.getItems().stream())) // All menus and immediate sub-menus
				.filter( item -> Boolean.TRUE.equals(item.getUserData())) // Retain only diagram-relevant menu items
				.forEach( item -> item.setDisable(noCurrentGraphFrame()));
	}
	
	// Returns the new menu
	private void createFileMenu(MenuBar pMenuBar, List<NewDiagramHandler> pNewDiagramHandlers) 
	{
		MenuFactory menuFactory = new MenuFactory(RESOURCES);
		
		Menu fileMenu = menuFactory.createMenu("file", false);
		pMenuBar.getMenus().add(fileMenu);

		Menu newMenu = menuFactory.createMenu("file.new", false);
		fileMenu.getItems().add(newMenu);
		for( NewDiagramHandler handler : pNewDiagramHandlers )
		{
			newMenu.getItems().add(menuFactory.createMenuItem(handler.getDiagramType().getName(), handler, false));
		}

		MenuItem fileOpenItem = menuFactory.createMenuItem("file.open", pEvent -> openFile(), false);
		fileMenu.getItems().add(fileOpenItem);

		aRecentFilesMenu = menuFactory.createMenu("file.recent", false);
		buildRecentFilesMenu();
		fileMenu.getItems().add(aRecentFilesMenu);

		MenuItem closeFileItem = menuFactory.createMenuItem("file.close", pEvent -> close(), true);
		fileMenu.getItems().add(closeFileItem);
		closeFileItem.setDisable(noCurrentGraphFrame());

		MenuItem fileSaveItem = menuFactory.createMenuItem("file.save", pEvent -> save(), true);
		fileMenu.getItems().add(fileSaveItem);
		fileSaveItem.setDisable(noCurrentGraphFrame());

		MenuItem fileSaveAsItem = menuFactory.createMenuItem("file.save_as", pEvent -> saveAs(), true);
		fileMenu.getItems().add(fileSaveAsItem);
		fileSaveAsItem.setDisable(noCurrentGraphFrame());

		MenuItem fileExportItem = menuFactory.createMenuItem("file.export_image", pEvent -> exportImage(), true);
		fileMenu.getItems().add(fileExportItem);
		fileExportItem.setDisable(noCurrentGraphFrame());

		MenuItem fileCopyToClipboard = menuFactory.createMenuItem("file.copy_to_clipboard", pEvent -> copyToClipboard(), true);
		fileMenu.getItems().add(fileCopyToClipboard);
		fileCopyToClipboard.setDisable(noCurrentGraphFrame());

		fileMenu.getItems().add(new SeparatorMenuItem());

		MenuItem fileExitItem = menuFactory.createMenuItem("file.exit", pEvent -> exit(), false);
		fileMenu.getItems().add(fileExitItem);
	}
	
	private void createEditMenu(MenuBar pMenuBar) 
	{
		MenuFactory menuFactory = new MenuFactory(RESOURCES);
		
		Menu editMenu = menuFactory.createMenu("edit", true);
		pMenuBar.getMenus().add(editMenu);
		editMenu.setDisable(noCurrentGraphFrame());

		editMenu.getItems().add(menuFactory.createMenuItem("edit.undo", pEvent -> 
		{
			if (noCurrentGraphFrame()) 
			{
				return;
			}
			((GraphFrame) getSelectedTab()).getGraphPanel().undo();
		}, true));

		editMenu.getItems().add(menuFactory.createMenuItem("edit.redo", pEvent ->
		{	
			if (noCurrentGraphFrame()) 
			{
				return;
			}
			((GraphFrame) getSelectedTab()).getGraphPanel().redo();
		}, true));

		editMenu.getItems().add(menuFactory.createMenuItem("edit.selectall", pEvent ->
		{
			if (noCurrentGraphFrame()) 
			{
				return;
			}
			((GraphFrame) getSelectedTab()).getGraphPanel().selectAll();
		}, true));

		editMenu.getItems().add(menuFactory.createMenuItem("edit.properties", pEvent -> 
		{
			if (noCurrentGraphFrame()) 
			{
				return;
			}
			((GraphFrame) getSelectedTab()).getGraphPanel().editSelected();
		}, true));

		editMenu.getItems().add(menuFactory.createMenuItem("edit.cut", pEvent -> cut(), true));
		editMenu.getItems().add(menuFactory.createMenuItem("edit.paste", pEvent -> paste(), true));
		editMenu.getItems().add(menuFactory.createMenuItem("edit.copy", pEvent -> copy(), true));

		editMenu.getItems().add(menuFactory.createMenuItem("edit.delete", pEvent ->  
		{
			if (noCurrentGraphFrame()) 
			{
				return;
			}
			((GraphFrame) getSelectedTab()).getGraphPanel().removeSelected();
		}, true));
	}
	
	private void createViewMenu(MenuBar pMenuBar) 
	{
		MenuFactory menuFactory = new MenuFactory(RESOURCES);
		
		Menu viewMenu = menuFactory.createMenu("view", true);
		pMenuBar.getMenus().add(viewMenu);
		viewMenu.setDisable(noCurrentGraphFrame());

		final CheckMenuItem showGridItem  = (CheckMenuItem) menuFactory.createCheckMenuItem("view.show_grid", pEvent ->
	    {
	    	if( noCurrentGraphFrame() )
	    	{
	    		return;
	    	}
	    	CheckMenuItem menuItem = (CheckMenuItem) pEvent.getSource();  
	    	boolean selected = menuItem.isSelected();
	    	Preferences.userNodeForPackage(UMLEditor.class).put("showGrid", Boolean.toString(selected));
	    	setShowGridForAllFrames(selected);
		}, true);
		showGridItem.setSelected(Boolean.valueOf(Preferences.userNodeForPackage(UMLEditor.class).get("showGrid", "true")));
		viewMenu.getItems().add(showGridItem);
		
		final CheckMenuItem showToolHints  = (CheckMenuItem) menuFactory.createCheckMenuItem("view.show_hints", pEvent ->
	    {
	    	if( noCurrentGraphFrame() )
	    	{
	    		return;
	    	}
	    	CheckMenuItem menuItem = (CheckMenuItem) pEvent.getSource();  
	    	boolean selected = menuItem.isSelected();
	    	Preferences.userNodeForPackage(UMLEditor.class).put("showToolHints", Boolean.toString(selected));
    		setShowToolbarButtonLabelsForAllFrames(selected);
		}, true);
		showToolHints.setSelected(Boolean.valueOf(Preferences.userNodeForPackage(UMLEditor.class).get("showToolHints", "false")));
		viewMenu.getItems().add(showToolHints);
	}
	
	private void setShowToolbarButtonLabelsForAllFrames(boolean pShow)
	{
		for( Tab tab : tabs() )
		{
			if( tab instanceof GraphFrame )
			{
				((GraphFrame)tab).showToolbarButtonLabels(pShow);
			}
		}
	}
	
	private void setShowGridForAllFrames(boolean pShow)
	{
		for( Tab tab : tabs() )
		{
			if( tab instanceof GraphFrame )
			{
				((GraphFrame)tab).getGraphPanel().setShowGrid(pShow);
			}
		}
	}

	private void createHelpMenu(MenuBar pMenuBar) 
	{
		MenuFactory menuFactory = new MenuFactory(RESOURCES);
		
		Menu helpMenu = menuFactory.createMenu("help", false);
		pMenuBar.getMenus().add(helpMenu);
		helpMenu.getItems().add(menuFactory.createMenuItem("help.about", pEvent -> new AboutDialog(aMainStage).show(), false));
	}

	/*
	 * Opens a file with the given name, or switches to the frame if it is already
	 * open.
	 * 
	 * @param pName the file name
	 */
	private void open(String pName) 
	{
		for( Tab tab : tabs() )
		{
			if(tab instanceof GraphFrame)
			{
				if(((GraphFrame) tab).getFileName() != null	&& 
						((GraphFrame) tab).getFileName().getAbsoluteFile().equals(new File(pName).getAbsoluteFile())) 
				{
					tabPane().getSelectionModel().select(tab);
					addRecentFile(new File(pName).getPath());
					return;
				}
			}
		}
		
		try 
		{
			Diagram diagram2 = PersistenceService.read(new File(pName));
			GraphFrame frame2 = new GraphFrame(diagram2, tabPane());
			frame2.setFile(new File(pName).getAbsoluteFile());
			addRecentFile(new File(pName).getPath());
			insertGraphFrameIntoTabbedPane(frame2);
		}
		catch (IOException | DeserializationException exception2) 
		{
			Alert alert = new Alert(AlertType.ERROR, RESOURCES.getString("error.open_file"), ButtonType.OK);
			alert.initOwner(aMainStage);
			alert.showAndWait();
		}
	}
	
	private List<NamedHandler> getOpenFileHandlers()
	{
		List<NamedHandler> result = new ArrayList<>();
		for( File file : aRecentFiles )
   		{
			result.add(new NamedHandler(file.getName(), pEvent -> open(file.getAbsolutePath())));
   		}
		return Collections.unmodifiableList(result);
	}
	
	private List<NewDiagramHandler> createNewDiagramHandlers()
	{
		List<NewDiagramHandler> result = new ArrayList<>();
		for( DiagramType diagramType : DiagramType.values() )
		{
			result.add(new NewDiagramHandler(diagramType, pEvent ->
			{
				insertGraphFrameIntoTabbedPane(new GraphFrame(diagramType.newInstance(), tabPane()));
			}));
		}
		return Collections.unmodifiableList(result);
	}

	/*
	 * Adds a file name to the "recent files" list and rebuilds the "recent files"
	 * menu.
	 * 
	 * @param pNewFile the file name to add
	 */
	private void addRecentFile(String pNewFile) 
	{
		aRecentFiles.add(pNewFile);
		buildRecentFilesMenu();
	}
	
   	/*
   	 * Rebuilds the "recent files" menu. Only works if the number of
   	 * recent files is less than 10. Otherwise, additional logic will need
   	 * to be added to 0-index the mnemonics for files 1-9.
   	 */
   	private void buildRecentFilesMenu()
   	{ 
   		aRecentFilesMenu.getItems().clear();
   		aRecentFilesMenu.setDisable(!(aRecentFiles.size() > 0));
   		int i = 1;
   		for( File file : aRecentFiles )
   		{
   			String name = "_" + i + " " + file.getName();
   			final String fileName = file.getAbsolutePath();
   			MenuItem item = new MenuItem(name);
   			aRecentFilesMenu.getItems().add(item);
   			item.setOnAction(pEvent -> open(fileName));
            i++;
   		}
   }

	/**
	 * Asks the user to open a graph file.
	 */
	public void openFile() 
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(aRecentFiles.getMostRecentDirectory());
		fileChooser.getExtensionFilters().addAll(FileExtensions.getAll());

		File selectedFile = fileChooser.showOpenDialog(aMainStage);
		if (selectedFile != null) 
		{
			open(selectedFile.getAbsolutePath());
		}
	}

	/**
	 * Cuts the current selection of the current panel and puts the content into the
	 * application-specific clipboard.
	 */
	public void cut() 
	{
		if (noCurrentGraphFrame()) 
		{
			return;
		}
		GraphPanel panel = ((GraphFrame) getSelectedTab()).getGraphPanel();
		panel.cut();
		panel.paintPanel();
	}

	/**
	 * Copies the current selection of the current panel and puts the content into
	 * the application-specific clipboard.
	 */
	public void copy() 
	{
		if (noCurrentGraphFrame()) 
		{
			return;
		}
		((GraphFrame) getSelectedTab()).getGraphPanel().copy();
	}

	/**
	 * Pastes a past selection from the application-specific Clipboard into current
	 * panel. All the logic is done in the application-specific CutPasteBehavior.
	 * 
	 */
	public void paste() 
	{
		if (noCurrentGraphFrame()) 
		{
			return;
		}
		GraphPanel panel = ((GraphFrame) getSelectedTab()).getGraphPanel();
		panel.paste();
		panel.paintPanel();
	}

	/**
	 * Copies the current image to the clipboard.
	 */
	public void copyToClipboard() 
	{
		if (noCurrentGraphFrame()) 
		{
			return;
		}
		GraphFrame frame = (GraphFrame) getSelectedTab();
		final Image image = getImage(frame.getGraphPanel());
		final Clipboard clipboard = Clipboard.getSystemClipboard();
	    final ClipboardContent content = new ClipboardContent();
	    content.putImage(image);
	    clipboard.setContent(content);
		Alert alert = new Alert(AlertType.INFORMATION, RESOURCES.getString("dialog.to_clipboard.message"), ButtonType.OK);
		alert.initOwner(aMainStage);
		alert.setHeaderText(RESOURCES.getString("dialog.to_clipboard.title"));
		alert.showAndWait();
	}

	private boolean noCurrentGraphFrame() 
	{
		return getSelectedTab() == null ||
				!(getSelectedTab() instanceof GraphFrame);
	}

	/**
	 * If a user confirms that they want to close their modified graph, this method
	 * will remove it from the current list of tabs.
	 */
	public void close() 
	{
		if (noCurrentGraphFrame()) 
		{
			return;
		}
		Tab currentFrame = getSelectedTab();
		if (currentFrame != null)
		{
			GraphFrame openFrame = (GraphFrame) currentFrame;
			// we only want to check attempts to close a frame
			if (openFrame.getGraphPanel().isModified()) 
			{
				// ask user if it is ok to close
				Alert alert = new Alert(AlertType.CONFIRMATION, RESOURCES.getString("dialog.close.ok"), ButtonType.YES, ButtonType.NO);
				alert.initOwner(aMainStage);
				alert.setTitle(RESOURCES.getString("dialog.close.title"));
				alert.setHeaderText(RESOURCES.getString("dialog.close.title"));
				alert.showAndWait();

				if (alert.getResult() == ButtonType.YES) 
				{
					removeGraphFrameFromTabbedPane(openFrame);
				}
				return;
			} 
			else 
			{
				removeGraphFrameFromTabbedPane(openFrame);
			}
		}
	}

	/**
	 * If a user confirms that they want to close their modified graph, this method
	 * will remove it from the current list of tabs.
	 * 
	 * @param pTab The current Tab that one wishes to close.
	 */
	public void close(Tab pTab) 
	{
		Tab currentFrame = pTab;
		if (currentFrame != null)
		{
			GraphFrame openFrame = (GraphFrame) currentFrame;
			// we only want to check attempts to close a frame
			if (openFrame.getGraphPanel().isModified()) 
			{
				if (openFrame.getGraphPanel().isModified()) 
				{
					// ask user if it is ok to close
					Alert alert = new Alert(AlertType.CONFIRMATION, RESOURCES.getString("dialog.close.ok"), ButtonType.YES, ButtonType.NO);
					alert.initOwner(aMainStage);
					alert.setTitle(RESOURCES.getString("dialog.close.title"));
					alert.setHeaderText(RESOURCES.getString("dialog.close.title"));
					alert.showAndWait();

					if (alert.getResult() == ButtonType.YES) 
					{
						removeGraphFrameFromTabbedPane(openFrame);
					}
				}
				return;
			}
			removeGraphFrameFromTabbedPane(openFrame);
		}
	}

	/**
	 * Save a file. Called by reflection.
	 */
	public void save() 
	{
		if (noCurrentGraphFrame()) 
		{
			return;
		}
		GraphFrame frame = (GraphFrame) getSelectedTab();
		File file = frame.getFileName();
		if (file == null) 
		{
			saveAs();
			return;
		}
		try 
		{
			PersistenceService.save(frame.getGraph(), file);
			frame.getGraphPanel().setModified(false);
		} 
		catch (IOException exception) 
		{
			Alert alert = new Alert(AlertType.ERROR, RESOURCES.getString("error.save_file"), ButtonType.OK);
			alert.initOwner(aMainStage);
			alert.showAndWait();
		}
	}

	/**
	 * Saves the current graph as a new file.
	 */
	public void saveAs() 
	{
		if (noCurrentGraphFrame()) 
		{
			return;
		}
		GraphFrame frame = (GraphFrame) getSelectedTab();
		Diagram graph = frame.getGraph();

		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(FileExtensions.getAll());
		fileChooser.setSelectedExtensionFilter(FileExtensions.get(graph.getDescription()));

		if (frame.getFileName() != null) 
		{
			fileChooser.setInitialDirectory(frame.getFileName().getParentFile());
			fileChooser.setInitialFileName(frame.getFileName().getName());
		} 
		else 
		{
			fileChooser.setInitialDirectory(new File("."));
			fileChooser.setInitialFileName("");
		}

		try 
		{
			File result = fileChooser.showSaveDialog(aMainStage);
			if(fileChooser.getSelectedExtensionFilter() != FileExtensions.get(graph.getDescription()))
			{
				result = new File(result.getPath() + graph.getFileExtension() + RESOURCES.getString("application.file.extension"));
			}
			if (result != null) 
			{
				PersistenceService.save(graph, result);
				addRecentFile(result.getAbsolutePath());
				frame.setFile(result);
				getSelectedTab().setText(frame.getFileName().getName());
				frame.getGraphPanel().setModified(false);
			}
		} 
		catch (IOException exception) 
		{
			Alert alert = new Alert(AlertType.ERROR, RESOURCES.getString("error.save_file"), ButtonType.OK);
			alert.initOwner(aMainStage);
			alert.showAndWait();
		}
	}

	/**
	 * Edits the file path so that the pToBeRemoved extension, if found, is replaced
	 * with pDesired.
	 * 
	 * @param pOriginal
	 *            the file to use as a starting point
	 * @param pToBeRemoved
	 *            the extension that is to be removed before adding the desired
	 *            extension.
	 * @param pDesired
	 *            the desired extension (e.g. ".png")
	 * @return original if it already has the desired extension, or a new file with
	 *         the edited file path
	 */
	static String replaceExtension(String pOriginal, String pToBeRemoved, String pDesired) 
	{
		assert pOriginal != null && pToBeRemoved != null && pDesired != null;

		if (pOriginal.endsWith(pToBeRemoved)) 
		{
			return pOriginal.substring(0, pOriginal.length() - pToBeRemoved.length()) + pDesired;
		}
		else 
		{
			return pOriginal;
		}
	}

	/**
	 * Exports the current graph to an image file.
	 */
	public void exportImage() 
	{
		if (noCurrentGraphFrame()) 
		{
			return;
		}
		
		FileChooser fileChooser = getImageFileChooser();
		File file = fileChooser.showSaveDialog(aMainStage);
		if (file == null) 
		{
			return;
		}

		// Validate the file format
		String fileName = file.getPath();
		String format = fileName.substring(fileName.lastIndexOf(".") + 1);
		if (!ImageIO.getImageWritersByFormatName(format).hasNext())
		{
			Alert alert = new Alert(AlertType.ERROR, RESOURCES.getString("error.unsupported_image"),	ButtonType.OK);
			alert.initOwner(aMainStage);
			alert.setHeaderText(RESOURCES.getString("error.unsupported_image.title"));
			alert.showAndWait();
			return;
		}
		
		GraphFrame frame = (GraphFrame) getSelectedTab();
		try (OutputStream out = new FileOutputStream(file)) 
		{
			BufferedImage image = getBufferedImage(frame.getGraphPanel()); 
			if (format.equals("jpg") || format.equals("jpeg"))	// to correct the display of JPEG/JPG images (removes red hue)
			{
				BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.OPAQUE);
				Graphics2D graphics = imageRGB.createGraphics();
				graphics.drawImage(image, 0,  0, null);
				ImageIO.write(imageRGB, format, out);
				graphics.dispose();
			}
			else if (format.equals("bmp"))	// to correct the BufferedImage type
			{
				BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = imageRGB.createGraphics();
				graphics.drawImage(image, 0, 0, Color.WHITE, null);
				ImageIO.write(imageRGB, format, out);
				graphics.dispose();
			}
			else
			{
				ImageIO.write(image, format, out);
			}
		} 
		catch (IOException exception) 
		{
			Alert alert = new Alert(AlertType.ERROR, RESOURCES.getString("error.save_file"), ButtonType.OK);
			alert.initOwner(aMainStage);
			alert.showAndWait();
		}
	}

	private static String[] getAllSupportedImageWriterFormats() 
	{
		String[] names = ImageIO.getWriterFormatNames();
		HashSet<String> formats = new HashSet<String>();
		for (String name : names) 
		{
			formats.add(name.toLowerCase());
		}
		String[] lReturn = formats.toArray(new String[formats.size()]);
		Arrays.sort(lReturn);
		return lReturn;
	}

	private FileChooser getImageFileChooser() 
	{
		GraphFrame frame = (GraphFrame) getSelectedTab();
		assert frame != null;

		// Initialize the file chooser widget
		FileChooser fileChooser = new FileChooser();
		for (String format : getAllSupportedImageWriterFormats()) 
		{
			if (format.equals("wbmp"))	// no supported conversion available
			{
				continue;
			}
			fileChooser.getExtensionFilters()
				.add(new ExtensionFilter(format.toUpperCase() + " " + RESOURCES.getString("files.image.name"), "*." +format));
		}
		fileChooser.setInitialDirectory(new File("."));

		// If the file was previously saved, use that to suggest a file name root.
		if (frame.getFileName() != null) 
		{
			File file = new File(replaceExtension(frame.getFileName().getAbsolutePath(), RESOURCES.getString("application.file.extension"), ""));
			fileChooser.setInitialDirectory(file.getParentFile());
			fileChooser.setInitialFileName(file.getName());
		}
		return fileChooser;
	}

	/*
	 * Return the image corresponding to the graph.
	 * 
	 * @param pGraph The graph to convert to an image.
	 * 
	 * @return bufferedImage. To convert it into an image, use the syntax :
	 * Toolkit.getDefaultToolkit().createImage(bufferedImage.getSource());
	 */
	private static BufferedImage getBufferedImage(GraphPanel pGraphPanel) 
	{
		BufferedImage image = SwingFXUtils.fromFXImage(getImage(pGraphPanel), null);
		return image;
	}
	
	/*
	 * Return the image corresponding to the graph.
	 * 
	 * @param pGraph The graph to convert to an image.
	 */
	private static Image getImage(GraphPanel pGraphPanel) 
	{
		Rectangle bounds = pGraphPanel.getGraph().getBounds();
		WritableImage image = new WritableImage((int) bounds.getWidth() + MARGIN_IMAGE * 2, (int) bounds.getHeight() + MARGIN_IMAGE * 2);
		boolean oldShowGrid = pGraphPanel.getShowGrid();
		pGraphPanel.setShowGrid(false);
		SnapshotParameters parameter = new SnapshotParameters();
		parameter.setViewport(new Rectangle2D(bounds.getX() - MARGIN_IMAGE, bounds.getY() - MARGIN_IMAGE, 
				bounds.getWidth() + MARGIN_IMAGE * 2, bounds.getHeight() + MARGIN_IMAGE * 2));
		((Node) pGraphPanel).snapshot(parameter, image);
		pGraphPanel.setShowGrid(oldShowGrid);
		return image;
	}
	
	private int getNumberOfDirtyDiagrams()
	{
		return (int) tabs().stream()
			.filter( tab -> tab instanceof GraphFrame ) 
			.filter( frame -> ((GraphFrame) frame).getGraphPanel().isModified())
			.count();
	}

	/**
	 * Exits the program if no graphs have been modified or if the user agrees to
	 * abandon modified graphs.
	 */
	public void exit() 
	{
		final int modcount = getNumberOfDirtyDiagrams();
		if (modcount > 0) 
		{
			Alert alert = new Alert(AlertType.CONFIRMATION, 
					MessageFormat.format(RESOURCES.getString("dialog.exit.ok"), new Object[] { Integer.valueOf(modcount) }),
					ButtonType.YES, 
					ButtonType.NO);
			alert.initOwner(aMainStage);
			alert.setTitle(RESOURCES.getString("dialog.exit.title"));
			alert.setHeaderText(RESOURCES.getString("dialog.exit.title"));
			alert.showAndWait();

			if (alert.getResult() == ButtonType.YES) 
			{
				Preferences.userNodeForPackage(UMLEditor.class).put("recent", aRecentFiles.serialize());
				System.exit(0);
			}
		}
		else 
		{
			Preferences.userNodeForPackage(UMLEditor.class).put("recent", aRecentFiles.serialize());
			System.exit(0);
		}
	}		
	
	// -- TABBING METHODS
	
	private List<Tab> tabs()
	{
		return ((TabPane) getCenter()).getTabs();
	}
	
	private Tab getSelectedTab()
	{
		return ((TabPane) getCenter()).getSelectionModel().getSelectedItem();
	}
	
	private TabPane tabPane()
	{
		return (TabPane) getCenter();
	}
	
	private boolean isWelcomeTabShowing()
	{
		return aWelcomeTab != null && 
				tabs().size() == 1 && 
				tabs().get(0) instanceof WelcomeTab;
	}
	
	/* Insert a graph frame into the tabbedpane */ 
	private void insertGraphFrameIntoTabbedPane(GraphFrame pGraphFrame) 
	{
		if( isWelcomeTabShowing() )
		{
			tabs().remove(0);
		}
		tabs().add(pGraphFrame);
		tabPane().getSelectionModel().selectLast();
	}
	
	/*
	 * Shows the welcome tab if there are no other tabs.
	 */
	private void showWelcomeTabIfNecessary() 
	{
		if( tabs().size() == 0)
		{
			aWelcomeTab.loadRecentFileLinks(getOpenFileHandlers());
			tabs().add(aWelcomeTab);
		}
	}
	
	/*
	 * Removes the graph frame from the tabbed pane
	 */
	private void removeGraphFrameFromTabbedPane(GraphFrame pTab) 
	{
		tabs().remove(pTab);
		showWelcomeTabIfNecessary();
	}
}
