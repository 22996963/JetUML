/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2018 by the contributors of the JetUML project.
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

package ca.mcgill.cs.jetuml.diagram.builder.constraints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.jetuml.JavaFXLoader;
import ca.mcgill.cs.jetuml.diagram.StateDiagram;
import ca.mcgill.cs.jetuml.diagram.edges.NoteEdge;
import ca.mcgill.cs.jetuml.diagram.edges.StateTransitionEdge;
import ca.mcgill.cs.jetuml.diagram.nodes.FinalStateNode;
import ca.mcgill.cs.jetuml.diagram.nodes.InitialStateNode;
import ca.mcgill.cs.jetuml.diagram.nodes.StateNode;

public class TestStateDiagramEdgeConstraints
{
	private StateDiagram aDiagram;
	private StateNode aState;
	private InitialStateNode aInitialNode;
	private FinalStateNode aFinalNode;
	private StateTransitionEdge aEdge;

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
		aDiagram = new StateDiagram();
		aState = new StateNode();
		aInitialNode = new InitialStateNode();
		aFinalNode = new FinalStateNode();
		aEdge = new StateTransitionEdge();
	}
	
	private void createDiagram()
	{
		aDiagram.addRootNode(aState);
		aDiagram.addRootNode(aInitialNode);
		aDiagram.addRootNode(aFinalNode);
	}
	
	@Test
	public void testNoEdgeToInitialNodeFalse()
	{
		createDiagram();
		assertFalse(StateDiagramEdgeConstraints.noEdgeToInitialNode(aInitialNode).satisfied());
	}
	
	@Test
	public void testNoEdgeToInitialNodeTrue()
	{
		createDiagram();
		assertTrue(StateDiagramEdgeConstraints.noEdgeToInitialNode(aState).satisfied());
	}
	
	@Test
	public void testNoEdgeFromFinalNodeInapplicableEdge()
	{
		createDiagram();
		assertTrue(StateDiagramEdgeConstraints.noEdgeFromFinalNode(new NoteEdge(), aFinalNode).satisfied());
	}
	
	@Test
	public void testNoEdgeFromFinalNodeApplicableEdgeFalse()
	{
		createDiagram();
		assertFalse(StateDiagramEdgeConstraints.noEdgeFromFinalNode(aEdge, aFinalNode).satisfied());
	}
	
	@Test
	public void testNoEdgeFromFinalNodeApplicableEdgeTrue()
	{
		createDiagram();
		assertTrue(StateDiagramEdgeConstraints.noEdgeFromFinalNode(aEdge, aState).satisfied());
	}
}