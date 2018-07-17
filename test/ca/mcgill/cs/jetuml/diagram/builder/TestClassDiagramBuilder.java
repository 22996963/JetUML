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

package ca.mcgill.cs.jetuml.diagram.builder;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.jetuml.JavaFXLoader;
import ca.mcgill.cs.jetuml.diagram.ClassDiagram;
import ca.mcgill.cs.jetuml.diagram.DiagramElement;
import ca.mcgill.cs.jetuml.diagram.edges.DependencyEdge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.NoteEdge;
import ca.mcgill.cs.jetuml.diagram.nodes.ClassNode;
import ca.mcgill.cs.jetuml.diagram.nodes.InterfaceNode;
import ca.mcgill.cs.jetuml.diagram.nodes.NoteNode;
import ca.mcgill.cs.jetuml.diagram.nodes.PackageNode;
import ca.mcgill.cs.jetuml.geom.Dimension;
import ca.mcgill.cs.jetuml.geom.Point;

public class TestClassDiagramBuilder
{
	private ClassDiagram aDiagram;
	private ClassDiagramBuilder aBuilder;
	
	/**
	 * Load JavaFX toolkit and environment.
	 */
	@BeforeClass
	@SuppressWarnings("unused")
	public static void setupClass()
	{
		JavaFXLoader loader = JavaFXLoader.instance();
	}
	
	@Before
	public void setUp()
	{
		aDiagram = new ClassDiagram();
		aBuilder = new ClassDiagramBuilder(aDiagram);
	}
	
	@Test
	public void testcreateAddNodeOperationSimple()
	{
		ClassNode node = new ClassNode();
		DiagramOperation operation = aBuilder.createAddNodeOperation(node, new Point(10,10));
		assertEquals(0, aDiagram.getRootNodes().size());
		operation.execute();
		assertEquals(1, aDiagram.getRootNodes().size());
		assertTrue(aDiagram.getRootNodes().contains(node));
		assertEquals(new Point(10,10), node.position());
		operation.undo();
		assertEquals(0, aDiagram.getRootNodes().size());
	}
	
	@Test
	public void testcreateAddNodeOperationReposition()
	{
		ClassNode node = new ClassNode();
		aBuilder.setCanvasDimension(new Dimension(500,500));
		DiagramOperation operation = aBuilder.createAddNodeOperation(node, new Point(450,450));
		assertEquals(0, aDiagram.getRootNodes().size());
		operation.execute();
		assertEquals(1, aDiagram.getRootNodes().size());
		assertTrue(aDiagram.getRootNodes().contains(node));
		assertEquals(new Point(400,440), node.position());
		operation.undo();
		assertEquals(0, aDiagram.getRootNodes().size());
		operation.execute();
		assertEquals(1, aDiagram.getRootNodes().size());
		assertTrue(aDiagram.getRootNodes().contains(node));
		assertEquals(new Point(400,440), node.position());
		operation.undo();
		assertEquals(0, aDiagram.getRootNodes().size());
	}
	
	/*
	 * Adding a node that can't be a child to the root
	 * of the diagram, so, no over any other node.
	 */
	@Test
	public void testCreateAddNodeOperationInvalidChildNotOverNode()
	{
		NoteNode node = new NoteNode();
		DiagramOperation operation = aBuilder.createAddNodeOperation(node, new Point(50,50));
		operation.execute();
		assertEquals(1, aDiagram.getRootNodes().size());
		assertTrue(aDiagram.getRootNodes().contains(node));
		assertEquals(new Point(50,50), node.position());
	}
	
	/*
	 * Adding a node that can't be a child over another node that
	 * can be a parent.
	 */
	@Test
	public void testCreateAddNodeOperationInvalidChildOverNode()
	{
		PackageNode node = new PackageNode();
		aDiagram.addRootNode(node);
		NoteNode node2 = new NoteNode();
		aBuilder.createAddNodeOperation(node2, new Point(20,20)).execute();
		assertEquals(2, aDiagram.getRootNodes().size());
		assertTrue(aDiagram.getRootNodes().contains(node2));
		assertEquals(new Point(20,20), node2.position());
	}
	
	@Test
	public void testCreateAddNodeOperationValidChildAddition()
	{
		PackageNode node = new PackageNode();
		aDiagram.addRootNode(node);
		InterfaceNode node2 = new InterfaceNode();
		DiagramOperation operation = aBuilder.createAddNodeOperation(node2, new Point(10,10));
		operation.execute();
		assertEquals(1, aDiagram.getRootNodes().size());
		assertTrue(aDiagram.getRootNodes().contains(node));
		assertEquals(new Point(0,0), node.position());
		
		assertEquals(1, node.getChildren().size());
		assertSame(node2, node.getChildren().get(0));
		assertEquals(new Point(10,10), node2.position());
		
		operation.undo();
		assertEquals(0, node.getChildren().size());
	}
	
	@Test
	public void testCreateAddNodeOperationValidSubChildAddition()
	{
		PackageNode bottom = new PackageNode();
		aDiagram.addRootNode(bottom);
		
		PackageNode middle = new PackageNode();
		aBuilder.createAddNodeOperation(middle, new Point(10,10)).execute();
		
		assertEquals(1, aDiagram.getRootNodes().size());
		assertSame(bottom, aDiagram.getRootNodes().toArray()[0]);
		assertEquals(1, bottom.getChildren().size());
		assertSame(middle, bottom.getChildren().get(0));
		
		InterfaceNode top = new InterfaceNode();
		aBuilder.createAddNodeOperation(top, new Point(20,20)).execute();
		assertEquals(1, aDiagram.getRootNodes().size());
		assertSame(bottom, aDiagram.getRootNodes().toArray()[0]);
		assertEquals(1, bottom.getChildren().size());
		assertSame(middle, bottom.getChildren().get(0));
		assertEquals(1, middle.getChildren().size());
		assertSame(top, middle.getChildren().get(0));
	}
	
	@Test
	public void testCanAddNode()
	{
		ClassNode node = new ClassNode();
		assertTrue(aBuilder.canAdd(node, new Point(1000,1000)));
	}
	
	@Test
	public void testCanAddEdgeNoFirstNode()
	{
		assertFalse(aBuilder.canAdd(new DependencyEdge(), new Point(10,10), new Point(20,20)));
	}
	
	@Test
	public void testCanAddEdgeFromNoteNode()
	{
		NoteNode node = new NoteNode();
		aDiagram.addRootNode(node);
		node.translate(10, 10);
		assertFalse(aBuilder.canAdd(new DependencyEdge(), new Point(15,15), new Point(100, 100)));
		assertTrue(aBuilder.canAdd(new NoteEdge(), new Point(15,15), new Point(100, 100)));
	}
	
	@Test
	public void testCanAddEdgeNoSecondNode()
	{
		ClassNode node1 = new ClassNode();
		node1.translate(10, 10);
		aDiagram.addRootNode(node1);
		assertFalse(aBuilder.canAdd(new DependencyEdge(), new Point(15,15), new Point(150, 150)));
	}
	
	@Test
	public void testCanAddEdgeAlreadyExists()
	{
		ClassNode node1 = new ClassNode();
		node1.translate(10, 10);
		ClassNode node2 = new ClassNode();
		node2.translate(200, 200);
		aDiagram.addRootNode(node1);
		aDiagram.addRootNode(node2);
		DependencyEdge edge = new DependencyEdge();
		edge.connect(node1, node2, aDiagram);
		aDiagram.addEdge(edge);
		assertFalse(aBuilder.canAdd(new DependencyEdge(), new Point(15,15), new Point(205, 205)));
	}
	
	@Test
	public void testCanAddEdgeFromNoteNodeNotNoteEdge()
	{
		NoteNode node = new NoteNode();
		NoteNode end = new NoteNode();
		end.moveTo(new Point(100,100));
		aDiagram.addRootNode(node);
		aDiagram.addRootNode(end);
		assertFalse(aBuilder.canAdd(new DependencyEdge(), new Point(15,15), new Point(105, 105)));
	}
	
	@Test
	public void testCanAddEdgeFromNoteNodeNotNoteEdge2()
	{
		NoteNode node = new NoteNode();
		ClassNode end = new ClassNode();
		end.moveTo(new Point(100,100));
		aDiagram.addRootNode(node);
		aDiagram.addRootNode(end);
		assertFalse(aBuilder.canAdd(new DependencyEdge(), new Point(15,15), new Point(105, 105)));
	}
	
	@Test
	public void testCanAddEdgeFromNoteNodeNotNoteEdge3()
	{
		ClassNode node = new ClassNode();
		NoteNode end = new NoteNode();
		end.moveTo(new Point(100,100));
		aDiagram.addRootNode(node);
		aDiagram.addRootNode(end);
		assertFalse(aBuilder.canAdd(new DependencyEdge(), new Point(15,15), new Point(105, 105)));
	}
	
	@Test
	public void testCanAddEdgeToNoteNodeNotNodeEdge()
	{
		NoteNode node = new NoteNode();
		aDiagram.addRootNode(node);
		assertFalse(aBuilder.canAdd(new DependencyEdge(), new Point(205, 205), new Point(15,15)));
	}
	
	@Test
	public void testCanAddEdgeSelfGeneralization()
	{
		ClassNode node = new ClassNode();
		aDiagram.addRootNode(node);
		assertFalse(aBuilder.canAdd(new GeneralizationEdge(), new Point(15, 15), new Point(15,15)));
	}
	
	@Test
	public void testCanAddEdgeNonSelfDependency()
	{
		ClassNode node1 = new ClassNode();
		aDiagram.addRootNode(node1);
		ClassNode node2 = new ClassNode();
		node2.translate(200, 200);
		aDiagram.addRootNode(node2);
		assertTrue(aBuilder.canAdd(new DependencyEdge(), new Point(15,15), new Point(205,205)));
	}
	
	@Test
	public void testCanAddEdgeSelfDependency()
	{
		ClassNode node1 = new ClassNode();
		aDiagram.addRootNode(node1);
		assertTrue(aBuilder.canAdd(new DependencyEdge(), new Point(15,15), new Point(15,15)));
	}
	
	@Test
	public void testCanAddNoteEdgeNoNoteNode()
	{
		ClassNode start = new ClassNode();
		ClassNode end = new ClassNode();
		end.moveTo(new Point(150, 150));
		aDiagram.addRootNode(start);
		aDiagram.addRootNode(end);
		assertFalse(aBuilder.canAdd(new NoteEdge(), new Point(5,5), new Point(155,155)));
	}
	
	@Test
	public void testCreateAddElementsOperationNothing()
	{
		DiagramOperation operation = aBuilder.createAddElementsOperation(new ArrayList<>());
		operation.execute();
		assertTrue(aDiagram.getRootNodes().isEmpty());
		assertTrue(aDiagram.getEdges().isEmpty());
		operation.undo();
		assertTrue(aDiagram.getRootNodes().isEmpty());
		assertTrue(aDiagram.getEdges().isEmpty());
	}
	
	@Test
	public void testCreateAddElementsOperationNodesAndEdges()
	{
		ArrayList<DiagramElement> elements = new ArrayList<>();
		ClassNode node1 = new ClassNode();
		node1.moveTo(new Point(10,10));
		ClassNode node2 = new ClassNode();
		node2.moveTo(new Point(100,100));
		DependencyEdge edge = new DependencyEdge();
		edge.connect(node1, node2, aDiagram);
		elements.addAll(Arrays.asList(new DiagramElement[]{edge, node1, node2}));
		
		DiagramOperation operation = aBuilder.createAddElementsOperation(elements);
		operation.execute();
		assertEquals(2, aDiagram.getRootNodes().size());
		assertEquals(1, aDiagram.getEdges().size());
		assertSame(node1, aDiagram.getRootNodes().toArray()[0]);
		assertSame(node2, aDiagram.getRootNodes().toArray()[1]);
		assertSame(edge, aDiagram.getEdges().toArray()[0]);
		
		operation.undo();
		assertEquals(0, aDiagram.getRootNodes().size());
		assertEquals(0, aDiagram.getEdges().size());
	}
}
