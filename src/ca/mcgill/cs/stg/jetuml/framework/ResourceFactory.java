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

package ca.mcgill.cs.stg.jetuml.framework;

import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * A class for creating menus from strings in a 
 * resource bundle.
 */
public class ResourceFactory
{
	private ResourceBundle aBundle;

	/**
	 * @param pBundle The bundle to use to fetch
	 * resources.
	 */
	public ResourceFactory(ResourceBundle pBundle)
	{
		aBundle = pBundle;
	}

	/**
	 * Creates a menu item that calls a method in response to the action event.
	 * @param pPrefix A string such as "file.open" that indicates the menu->submenu path
	 * @param pTarget The object on which pMethodName will be invoked when the menu is selected.
	 * @param pMethodName The method to invoke when the menu is selected.
	 * @return A menu item for the action described.
	 */
	public JMenuItem createMenuItem(String pPrefix, Object pTarget, String pMethodName)
	{
		return createMenuItem(pPrefix, (ActionListener) EventHandler.create(ActionListener.class, pTarget, pMethodName));
	}

	/**
	 * Creates a menu item where pListener is triggered when the menu item is selected.
	 * @param pPrefix A string such as "file.open" that indicates the menu->submenu path
	 * @param pListener The callback to execute when the menu item is selected.
	 * @return A menu item for the action described.
	 */
	public JMenuItem createMenuItem(String pPrefix, ActionListener pListener)
	{
		String text = aBundle.getString(pPrefix + ".text");
		JMenuItem menuItem = new JMenuItem(text);
		return configure(menuItem, pPrefix, pListener);
	}

	/**
	 * Create a checkbox menu.
	 * @param pPrefix A string such as "file.open" that indicates the menu->submenu path
	 * @param pListener The callback to execute when the menu item is selected.
	 * @return A menu item for the action described.
	 */
	public JMenuItem createCheckBoxMenuItem(String pPrefix, ActionListener pListener)
	{
		String text = aBundle.getString(pPrefix + ".text");
		JMenuItem menuItem = new JCheckBoxMenuItem(text);
		return configure(menuItem, pPrefix, pListener);
	}	

	/*
	 * Configures the menu with text, mnemonic, accelerator, etc
	 */
	private JMenuItem configure(JMenuItem pMenuItem, String pPrefix, ActionListener pListener)
	{      
		pMenuItem.addActionListener(pListener);
		try
		{
			String mnemonic = aBundle.getString(pPrefix + ".mnemonic");
			pMenuItem.setMnemonic(mnemonic.charAt(0));
		}
		catch(MissingResourceException exception) // TODO use containsKey instead
		{} // ok not to set mnemonic
      
		try
		{
			String accelerator = aBundle.getString(pPrefix + ".accelerator");
			pMenuItem.setAccelerator(KeyStroke.getKeyStroke(accelerator));
		}
		catch (MissingResourceException exception)
		{
			// ok not to set accelerator
		}

		try
		{
			String tooltip = aBundle.getString(pPrefix + ".tooltip");
			pMenuItem.setToolTipText(tooltip);         
		}
		catch (MissingResourceException exception)
		{
			// ok not to set tooltip
		}
		return pMenuItem;
	}
	
	/**
	 * Create a menu that corresponds to the resource for key pPrefix.
	 * @param pPrefix A string such as "file" that indicates the menu->submenu path
	 * @return A configured menu
	 */
	public JMenu createMenu(String pPrefix)
	{
		String text = aBundle.getString(pPrefix + ".text");
		JMenu menu = new JMenu(text);
		try
		{
			String mnemonic = aBundle.getString(pPrefix + ".mnemonic");
			menu.setMnemonic(mnemonic.charAt(0));
		}
		catch(MissingResourceException exception)
		{
			// ok not to set mnemonic
		}

      	try
      	{
      		String tooltip = aBundle.getString(pPrefix + ".tooltip");
      		menu.setToolTipText(tooltip);         
      	}
      	catch(MissingResourceException exception)
      	{
      		// ok not to set tooltip
      	}
      	return menu;
	}
}
