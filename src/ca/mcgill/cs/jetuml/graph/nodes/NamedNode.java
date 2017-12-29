/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2016 by the contributors of the JetUML project.
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

package ca.mcgill.cs.jetuml.graph.nodes;

import ca.mcgill.cs.jetuml.application.MultiLineString;
import ca.mcgill.cs.jetuml.graph.Properties;
import ca.mcgill.cs.jetuml.graph.ValueExtractor;
import ca.mcgill.cs.jetuml.graph.ValueExtractor.Type;

/**
   A node with a name.
*/
public abstract class NamedNode extends AbstractNode
{
	private MultiLineString aName = new MultiLineString();

	/**
     * Sets the name property value.
     * @param pName the new state name
	 */
	public void setName(MultiLineString pName)
	{
		aName = pName;
	}

	/**
     * Gets the name property value.
     * @return the state name
	 */
	public MultiLineString getName()
	{
		return aName;
	}
	
	@Override
	public Properties properties()
	{
		Properties properties = super.properties();
		properties.put("name", aName.getText());
		return properties;
	}
	
	@Override
	public void initialize(ValueExtractor pExtractor)
	{
		super.initialize(pExtractor);
		aName.setText((String)pExtractor.get("name", Type.STRING));
	}

	@Override
	public NamedNode clone()
	{
		NamedNode clone = (NamedNode)super.clone();
		clone.aName = aName.clone();
		return clone;
	}
}
