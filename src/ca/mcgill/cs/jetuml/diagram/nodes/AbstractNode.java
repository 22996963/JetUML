/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2018, 2019 by the contributors of the JetUML project.
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
package ca.mcgill.cs.jetuml.diagram.nodes;

import java.util.Optional;

import ca.mcgill.cs.jetuml.diagram.AbstractDiagramElement;
import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.Node;
import ca.mcgill.cs.jetuml.geom.Point;

/**
 * Common elements for the Node hierarchy.
 */
public abstract class AbstractNode extends AbstractDiagramElement implements Node
{
	private Point aPosition = new Point(0, 0);
	private Optional<Diagram> aDiagram = Optional.empty();
	
	@Override
	public void translate(int pDeltaX, int pDeltaY)
	{
		aPosition = new Point( aPosition.getX() + pDeltaX, aPosition.getY() + pDeltaY );
	}
	
	@Override
	public final Point position()
	{
		return aPosition;
	}
	
	@Override
	public final void moveTo(Point pPoint)
	{
		aPosition = pPoint;
	}

	@Override
	public AbstractNode clone()
	{
		AbstractNode clone = (AbstractNode) super.clone();
		return clone;
	}
	
	@Override
	public final String toString()
	{
		return getClass().getSimpleName() + " at " + position();
	}
	
	@Override
	protected void buildProperties()
	{
		super.buildProperties();
		properties().addInvisible("x", () -> aPosition.getX(), pX -> aPosition.setX((int)pX)); 
		properties().addInvisible("y", () -> aPosition.getY(), pY -> aPosition.setY((int)pY));
	}
	
	@Override
	public final void attach(Diagram pDiagram)
	{
		assert pDiagram != null;
		aDiagram = Optional.of(pDiagram);
	}

	@Override
	public final void detach()
	{
		aDiagram = Optional.empty();
	}

	@Override
	public final Optional<Diagram> getDiagram()
	{
		return aDiagram;
	}
}
