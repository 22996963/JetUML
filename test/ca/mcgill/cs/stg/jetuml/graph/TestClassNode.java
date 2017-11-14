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
package ca.mcgill.cs.stg.jetuml.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.mcgill.cs.stg.jetuml.diagrams.ClassDiagramGraph;
import ca.mcgill.cs.stg.jetuml.framework.MultiLineString;
import ca.mcgill.cs.stg.jetuml.geom.Rectangle;

public class TestClassNode
{
	private ClassNode aNode1;
	private Graphics2D aGraphics;
	private ClassDiagramGraph aGraph;
	
	@Before
	public void setup()
	{
		aNode1 = new ClassNode();
		aGraphics = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB).createGraphics();
		aGraph= new ClassDiagramGraph();
	}
	
	@After
	public void teardown()
	{
		aGraphics.dispose();
	}
	
	@Test
	public void testDefault()
	{
		MultiLineString name = aNode1.getName();
		assertEquals( MultiLineString.CENTER, name.getJustification() );
		assertTrue(name.isBold());
		assertFalse(name.isUnderlined());
		assertEquals("", name.getText());
		MultiLineString methods = aNode1.getMethods();
		assertEquals( MultiLineString.LEFT, methods.getJustification() );
		assertFalse(methods.isBold());
		assertFalse(methods.isUnderlined());
		assertEquals("", methods.getText());
		MultiLineString attributes = aNode1.getAttributes();
		assertEquals( MultiLineString.LEFT, attributes.getJustification() );
		assertFalse(attributes.isBold());
		assertFalse(attributes.isUnderlined());
		assertEquals("", attributes.getText());
		assertEquals(new Rectangle(0,0,100,60), aNode1.getBounds());
		assertNull(aNode1.getParent());
	}
	
	@Test
	public void testNeedsMiddle()
	{
		assertFalse(aNode1.needsMiddleCompartment());
		MultiLineString attributes = new MultiLineString();
		attributes.setText("Foo");
		aNode1.setAttributes(attributes);
		assertTrue(aNode1.needsMiddleCompartment());
	}
	
	@Test
	public void testNeedsBottom()
	{
		assertFalse(aNode1.needsBottomCompartment());
		MultiLineString methods = new MultiLineString();
		methods.setText("Foo");
		aNode1.setMethods(methods);
		assertTrue(aNode1.needsBottomCompartment());
	}
	
	@Test
	public void testSetName()
	{
		MultiLineString name = new MultiLineString();
		name.setText("Foo");
		aNode1.setName(name);
		assertEquals("Foo", aNode1.getName().getText());
	}
	
	@Test
	public void testSetParent()
	{
		PackageNode package1 = new PackageNode();
		PackageNode package2 = new PackageNode();
		aNode1.setParent(package1);
		assertTrue( aNode1.getParent() == package1 );
		aNode1.setParent(package2);
		assertTrue( aNode1.getParent() == package2 );
		aNode1.setParent(null);
		assertNull( aNode1.getParent() );
	}
	
	@Test
	public void testClone()
	{
		PackageNode package1 = new PackageNode();
		aNode1.setParent(package1);
		ClassNode clone = aNode1.clone();
		MultiLineString name = clone.getName();
		assertEquals( MultiLineString.CENTER, name.getJustification() );
		assertTrue(name.isBold());
		assertFalse(name.isUnderlined());
		assertEquals("", name.getText());
		assertFalse(name == aNode1.getName() );
		MultiLineString methods = clone.getMethods();
		assertEquals( MultiLineString.LEFT, methods.getJustification() );
		assertFalse(methods.isBold());
		assertFalse(methods.isUnderlined());
		assertEquals("", methods.getText());
		assertFalse(methods == aNode1.getMethods() );
		MultiLineString attributes = clone.getAttributes();
		assertEquals( MultiLineString.LEFT, attributes.getJustification() );
		assertFalse(attributes.isBold());
		assertFalse(attributes.isUnderlined());
		assertEquals("", attributes.getText());
		assertFalse(attributes == aNode1.getAttributes() );
		assertEquals(new Rectangle(0,0,100,60), clone.getBounds());
		assertTrue(clone.getBounds() == aNode1.getBounds());
		assertTrue(clone.getParent() == aNode1.getParent());
	}
	
	@Test
	public void testComputeMiddle()
	{
		assertEquals(new Rectangle(0,0,0,0), aNode1.computeMiddle());
		MultiLineString attributes = new MultiLineString();
		attributes.setText("Foo");
		aNode1.setAttributes(attributes);
		assertEquals(new Rectangle(0,0,100,20), aNode1.computeMiddle());
		attributes.setText("Foo\nFoo");
		assertEquals(new Rectangle(0,0,100,32), aNode1.computeMiddle());
		attributes.setText("Foo");
		assertEquals(new Rectangle(0,0,100,20), aNode1.computeMiddle());
		attributes.setText("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		assertEquals(new Rectangle(0,0,350,20), aNode1.computeMiddle());
	}

	@Test
	public void testComputeBottom()
	{
		assertEquals(new Rectangle(0,0,0,0), aNode1.computeBottom());
		MultiLineString methods = new MultiLineString();
		methods.setText("Foo");
		aNode1.setMethods(methods);
		assertEquals(new Rectangle(0,0,100,20), aNode1.computeBottom());
		methods.setText("Foo\nFoo");
		assertEquals(new Rectangle(0,0,100,32), aNode1.computeBottom());
		methods.setText("Foo");
		assertEquals(new Rectangle(0,0,100,20), aNode1.computeBottom());
		methods.setText("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		assertEquals(new Rectangle(0,0,350,20), aNode1.computeBottom());
	}

	@Test
	public void testComputeTop()
	{
		assertEquals(new Rectangle(0,0,100,60), aNode1.computeTop());
		MultiLineString name = new MultiLineString();
		name.setText("X\nX\nX\nX");
		aNode1.setName(name);
		assertEquals(new Rectangle(0,0,100,64), aNode1.computeTop());
		
		name.setText("");
		assertEquals(new Rectangle(0,0,100,60), aNode1.computeTop());
		
		MultiLineString methods = new MultiLineString();
		methods.setText("X");
		aNode1.setMethods(methods);
		assertEquals(new Rectangle(0,0,100,40), aNode1.computeTop());
		methods.setText("X\nX\nX");
		assertEquals(new Rectangle(0,0,100,40), aNode1.computeTop());
		
		name.setText("X\nX\nX");
		assertEquals(new Rectangle(0,0,100,48), aNode1.computeTop());
		name.setText("X\nX\nX\nX");
		assertEquals(new Rectangle(0,0,100,64), aNode1.computeTop());
		
		name.setText("X");
		methods.setText("X");
		MultiLineString attributes = new MultiLineString();
		attributes.setText("X");
		aNode1.setAttributes(attributes);
		assertEquals(new Rectangle(0,0,100,20), aNode1.computeTop());
		
		methods.setText("");
		assertEquals(new Rectangle(0,0,100,40), aNode1.computeTop());
	}
	
	@Test
	public void testLayout()
	{
		// Test layout with no snapping (grid size is 10)
		aNode1.translate(10, 10);
		aNode1.layout(aGraph, aGraphics);
		assertEquals(new Rectangle(10,10,100,60), aNode1.getBounds());
		
		MultiLineString name = new MultiLineString();
		name.setText("X\nX\nX\nX");
		aNode1.setName(name);
		aNode1.layout(aGraph, aGraphics);
		assertEquals(new Rectangle(10,10,100,80), aNode1.getBounds());
		
		MultiLineString methods = new MultiLineString();
		methods.setText("X\nX");
		aNode1.setMethods(methods);
		aNode1.layout(aGraph, aGraphics);
		assertEquals(new Rectangle(10,10,100,100), aNode1.getBounds());
		
		name.setText("X");
		methods.setText("X");
		MultiLineString attributes = new MultiLineString();
		attributes.setText("X");
		aNode1.setMethods(attributes);
		aNode1.layout(aGraph, aGraphics);
		assertEquals(new Rectangle(10,10,100,60), aNode1.getBounds());
		
		// Test layout with snapping
		aNode1.translate(-4, -4);
		aNode1.layout(aGraph, aGraphics);
		assertEquals(new Rectangle(10,10,100,60), aNode1.getBounds());
	}
}
