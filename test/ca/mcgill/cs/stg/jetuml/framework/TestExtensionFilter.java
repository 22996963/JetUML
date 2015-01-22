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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import ca.mcgill.cs.stg.jetuml.framework.ExtensionFilter;

public class TestExtensionFilter
{
	@Test
	public void testBasicConstructor()
	{
		ExtensionFilter filter = new ExtensionFilter("", new String[]{""});
		assertEquals("", filter.getDescription());
		String[] extensions = filter.getExtensions();
		assertEquals(1, extensions.length);
		assertEquals("", extensions[0]);
		
		filter = new ExtensionFilter("Tar files", new String[]{".tar", ".tar.gz"});
		assertEquals("Tar files", filter.getDescription());
		extensions = filter.getExtensions();
		assertEquals(2, extensions.length);
		assertEquals(".tar", extensions[0]);
		assertEquals(".tar.gz", extensions[1]);
	}
	
	@Test 
	public void testCompoundConstructor()
	{
		ExtensionFilter filter = new ExtensionFilter("Tar files", ".tar");
		assertEquals("Tar files", filter.getDescription());
		String[] extensions = filter.getExtensions();
		assertEquals(1, extensions.length);
		assertEquals(".tar", extensions[0]);
		
		filter = new ExtensionFilter("Tar files", ".tar|.tar.gz");
		assertEquals("Tar files", filter.getDescription());
		extensions = filter.getExtensions();
		assertEquals(2, extensions.length);
		assertEquals(".tar", extensions[0]);
		assertEquals(".tar.gz", extensions[1]);
		
		filter = new ExtensionFilter("Tar files", " .tar | .tar.gz ");
		assertEquals("Tar files", filter.getDescription());
		extensions = filter.getExtensions();
		assertEquals(2, extensions.length);
		assertEquals(" .tar ", extensions[0]);
		assertEquals(" .tar.gz ", extensions[1]);
	}
	
	@Test 
	public void testAccept()
	{
		ExtensionFilter filter = new ExtensionFilter("", new String[] {""});
		assertTrue(filter.accept(new File("test")));
		filter = new ExtensionFilter("Test files", new String[] {".txt"});
		assertFalse(filter.accept(new File("README.md")));
		filter = new ExtensionFilter("Readme files", new String[] {".md"});
		assertTrue(filter.accept(new File("README.md")));
	}
}
