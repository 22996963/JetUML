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
package ca.mcgill.cs.jetuml.persistence;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.jetuml.JavaFXLoader;

public class TestJsonDecoder
{
	
	/**
	 * Load JavaFX toolkit and environment.
	 */
	@BeforeClass
	@SuppressWarnings("unused")
	public static void setupClass()
	{
		JavaFXLoader loader = JavaFXLoader.instance();
	}
	
	/*
	 * Try to decode a valid but empty
	 * JSON object.
	 */
	@Test(expected=DeserializationException.class)
	public void testEmptyJSONObject()
	{
		JSONObject object = new JSONObject();
		JsonDecoder.decode(object);
	}
	
	/*
	 * Try to decode a valid JSON object missing
	 * the nodes and edges.
	 */
	@Test(expected=DeserializationException.class)
	public void testIncompleteJSONObject()
	{
		JSONObject object = new JSONObject();
		object.put("version", "1.2");
		object.put("diagram", "StateDiagramGraph");
		JsonDecoder.decode(object);
	}
}
