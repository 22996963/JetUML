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
package ca.mcgill.cs.stg.jetuml.commands;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import ca.mcgill.cs.stg.jetuml.commands.DeleteNodeCommand;
import ca.mcgill.cs.stg.jetuml.diagrams.ClassDiagramGraph;
import ca.mcgill.cs.stg.jetuml.graph.Graph;
import ca.mcgill.cs.stg.jetuml.graph.nodes.ClassNode;
import ca.mcgill.cs.stg.jetuml.graph.nodes.Node;

public class TestDeleteNodeCommand 
{
    private Graph aGraph;
    private Field aNodesToBeRemoved;
    private Node aNode;
    private DeleteNodeCommand aDeleteNodeCommand;

    @Before
    public void setUp() throws Exception 
    {
        aGraph = new ClassDiagramGraph();
        aNode = new ClassNode();
        aNodesToBeRemoved = aGraph.getClass().getSuperclass().getDeclaredField("aNodesToBeRemoved");
        aNodesToBeRemoved.setAccessible(true);
        aDeleteNodeCommand = new DeleteNodeCommand(aGraph, aNode);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecute() 
    {
        aDeleteNodeCommand.execute();
        ArrayList<Node> aListNodesToBeRemoved;
        try 
        {
            aListNodesToBeRemoved = (ArrayList<Node>) (aNodesToBeRemoved.get(aGraph));
            assertTrue(aListNodesToBeRemoved.contains(aNode));
        } 
        catch (IllegalArgumentException | IllegalAccessException e1) 
        {
            fail();
        }
    }

    @Test
    public void testUndo() 
    {
        aDeleteNodeCommand.execute();
        assertEquals(0, aGraph.getRootNodes().size());
        aDeleteNodeCommand.undo();
        assertEquals(1, aGraph.getRootNodes().size());
    }
}
