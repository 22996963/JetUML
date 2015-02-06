package ca.mcgill.cs.stg.jetuml.framework;

import java.util.Stack;

public class CompoundCommand implements Command{
	private Stack<Command> aCommands;
	private int aSize;
	
	public CompoundCommand()
	{
		aCommands = new Stack<Command>();
	}
	
	public void add(Command pCommand)
	{
		aCommands.push(pCommand);
		aSize++;
	}
	
	public int size()
	{
		return aSize;
	}
	
	public void undo()
	{
		while(!aCommands.empty())
		{
			Command c = aCommands.pop();
			c.undo();
		}
	}
	
	public void execute()
	{
		while(!aCommands.empty())
		{
			Command c = aCommands.pop();
			c.execute();
		}
	}
}
