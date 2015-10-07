package ca.mcgill.cs.stg.jetuml.framework;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;

import ca.mcgill.cs.stg.jetuml.commands.AddDeleteNodeCommand;
import ca.mcgill.cs.stg.jetuml.commands.Command;
import ca.mcgill.cs.stg.jetuml.commands.CompoundCommand;
import ca.mcgill.cs.stg.jetuml.diagrams.ClassDiagramGraph;
import ca.mcgill.cs.stg.jetuml.graph.ClassNode;

public class TestUndoManager
{
	private UndoManager aUndoManager;
	private AddDeleteNodeCommand aCommand1;
	private AddDeleteNodeCommand aCommand2;
	private AddDeleteNodeCommand aCommand3;
	private AddDeleteNodeCommand aCommand4;
	private AddDeleteNodeCommand aCommand5;
	private Field aPastCommands;
	private Field aUndoneCommands;
	private Field aTrackingCommands;

	@Before
	public void setup() throws Exception
	{
		aUndoManager = new UndoManager();
		aCommand1 = new AddDeleteNodeCommand(new ClassDiagramGraph(), new ClassNode(), true);
		aCommand2 = new AddDeleteNodeCommand(new ClassDiagramGraph(), new ClassNode(), true);
		aCommand3 = new AddDeleteNodeCommand(new ClassDiagramGraph(), new ClassNode(), true);
		aCommand4 = new AddDeleteNodeCommand(new ClassDiagramGraph(), new ClassNode(), true);
		aCommand5 = new AddDeleteNodeCommand(new ClassDiagramGraph(), new ClassNode(), true);
		aPastCommands = UndoManager.class.getDeclaredField("aPastCommands");
		aPastCommands.setAccessible(true);
		aUndoneCommands = UndoManager.class.getDeclaredField("aUndoneCommands");
		aUndoneCommands.setAccessible(true);
		aTrackingCommands = UndoManager.class.getDeclaredField("aTrackingCommands");
		aTrackingCommands.setAccessible(true);
	}
	
	@Test
	public void testBasicAdd()
	{
		aUndoManager.add(aCommand1);
		assertTrue(getPastCommands().get(0) == aCommand1);
		assertEquals(0, getUndoneCommands().size());
		assertEquals(0, getTrackingCommands().size());
		aUndoManager.add(aCommand2);
		assertEquals(2, getPastCommands().size());
		assertTrue(getPastCommands().pop() == aCommand2);
		assertTrue(getPastCommands().pop() == aCommand1);
		assertEquals(0, getUndoneCommands().size());
		assertEquals(0, getTrackingCommands().size());
	}
	
	@Test
	public void testAddWithTracking()
	{
		aUndoManager.add(aCommand1);
		aUndoManager.startTracking();
		assertTrue(getPastCommands().get(0) == aCommand1);
		assertEquals(0,getUndoneCommands().size());
		assertEquals(1,getTrackingCommands().size());
		aUndoManager.add(aCommand2);
		assertEquals(1, getPastCommands().size());
		assertTrue(getPastCommands().get(0) == aCommand1);
		assertEquals(0,getUndoneCommands().size());
		assertEquals(1,getTrackingCommands().size());
		CompoundCommand cc = (CompoundCommand)getTrackingCommands().peek();
		assertEquals(1, cc.size());
		aUndoManager.add(aCommand3);
		assertEquals(1, getPastCommands().size());
		assertTrue(getPastCommands().get(0) == aCommand1);
		assertEquals(0,getUndoneCommands().size());
		assertEquals(1,getTrackingCommands().size());
		cc = (CompoundCommand)getTrackingCommands().peek();
		assertEquals(2, cc.size());
		aUndoManager.endTracking();
		assertEquals(2, getPastCommands().size());
		cc = (CompoundCommand)getPastCommands().pop();
		assertEquals(2, cc.size());
		assertTrue(getPastCommands().pop() == aCommand1);
		assertEquals(0, getTrackingCommands().size());
		assertEquals(0, getUndoneCommands().size());
	}
	
	@Test
	public void testAddWithTracking3Levels()
	{
		aUndoManager.startTracking();
		aUndoManager.add(aCommand1);
		aUndoManager.add(aCommand2);
		aUndoManager.startTracking();
		aUndoManager.add(aCommand3);
		aUndoManager.add(aCommand4);
		aUndoManager.add(aCommand5);
		aUndoManager.startTracking();
		assertEquals(0, getPastCommands().size());
		assertEquals(0, getUndoneCommands().size());
		assertEquals(3, getTrackingCommands().size());
		aUndoManager.endTracking();
		assertEquals(0, getPastCommands().size());
		assertEquals(0, getUndoneCommands().size());
		assertEquals(2, getTrackingCommands().size());
		CompoundCommand cc = (CompoundCommand)getTrackingCommands().peek();
		assertEquals(3, cc.size());
		aUndoManager.endTracking(); // The command is added to the now current tracking command.
		assertEquals(0, getPastCommands().size());
		assertEquals(0, getUndoneCommands().size());
		assertEquals(1, getTrackingCommands().size());
		cc = (CompoundCommand)getTrackingCommands().peek();
		assertEquals(3, cc.size());
		aUndoManager.endTracking();
		assertEquals(1, getPastCommands().size());
		assertEquals(0, getUndoneCommands().size());
		assertEquals(0, getTrackingCommands().size());
		cc = (CompoundCommand)getPastCommands().peek();
		assertEquals(3, cc.size());
		aUndoManager.endTracking();
		assertEquals(1, getPastCommands().size());
		assertEquals(0, getUndoneCommands().size());
		assertEquals(0, getTrackingCommands().size());
		cc = (CompoundCommand)getPastCommands().peek();
		assertEquals(3, cc.size());
	}
	
	@SuppressWarnings("unchecked")
	private Stack<Command> getPastCommands()
	{
		try
		{
			return (Stack<Command>)aPastCommands.get(aUndoManager);
		}
		catch( IllegalAccessException exception )
		{
			fail();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private Stack<Command> getUndoneCommands()
	{
		try
		{
			return (Stack<Command>)aUndoneCommands.get(aUndoManager);
		}
		catch( IllegalAccessException exception )
		{
			fail();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private Stack<Command> getTrackingCommands()
	{
		try
		{
			return (Stack<Command>)aTrackingCommands.get(aUndoManager);
		}
		catch( IllegalAccessException exception )
		{
			fail();
			return null;
		}
	}
}
