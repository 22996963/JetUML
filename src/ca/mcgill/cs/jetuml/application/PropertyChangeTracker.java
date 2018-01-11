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
package ca.mcgill.cs.jetuml.application;

import java.util.HashMap;

import ca.mcgill.cs.jetuml.commands.ChangePropertyCommand;
import ca.mcgill.cs.jetuml.commands.CompoundCommand;
import ca.mcgill.cs.jetuml.graph.GraphElement;
import ca.mcgill.cs.jetuml.graph.Properties;
import ca.mcgill.cs.jetuml.graph.Property;

/**
 * Tracks modification to the properties of a GraphElement.
 * Should be discarded after a call to stopTracking().
 * 
 * @author Martin P. Robillard
 */
public class PropertyChangeTracker 
{
	private HashMap<String, Object> aOldValues = new HashMap<>();
	private Properties aProperties;
	
	/**
	 * Creates a new tracker for pEdited.
	 *  
	 * @param pEdited The element to track.
	 * @pre pEdited != null;
	 */
	public PropertyChangeTracker(GraphElement pEdited)
	{
		assert pEdited != null;
		aProperties = pEdited.properties();
	}

	/**
	 * Makes a snapshot of the properties values of the tracked element.
	 * 
	 */
	public void startTracking()
	{
		for( Property property : aProperties )
		{
			aOldValues.put(property.getName(), property.get());
		}
	}
	
	/**
	 * Creates and returns a CompoundCommand that represents any change
	 * in properties detected between the time startTracking
	 * and stopTracking were called.
	 * 
	 * @return A CompoundCommand describing the property changes.
	 */
	public CompoundCommand stopTracking()
	{
		CompoundCommand command = new CompoundCommand();
		for( Property property : aProperties )
		{
			if( !aOldValues.get(property.getName()).equals(property.get()))
			{
				command.add(new ChangePropertyCommand(property, aOldValues.get(property.getName()), property.get()));
			}
		}
		return command;
	}
}
