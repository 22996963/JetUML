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

/**
 * @author Martin P. Robillard
 */

package ca.mcgill.cs.jetuml.graph.edges;

import ca.mcgill.cs.jetuml.views.ArrowHead;
import ca.mcgill.cs.jetuml.views.LineStyle;
import ca.mcgill.cs.jetuml.views.edges.EdgeView;
import ca.mcgill.cs.jetuml.views.edges.EdgeView2;
import ca.mcgill.cs.jetuml.views.edges.SegmentationStyleFactory;
import ca.mcgill.cs.jetuml.views.edges.SegmentationStyleFactory2;
import ca.mcgill.cs.jetuml.views.edges.SegmentedEdgeView;
import ca.mcgill.cs.jetuml.views.edges.SegmentedEdgeView2;

/**
 *  An edge that that represents a UML dependency
 *  between use cases.
 */
public class UseCaseDependencyEdge extends AbstractEdge
{
	private static final String LABEL_INCLUDE = "\u00ABinclude\u00BB";
	private static final String LABEL_EXTEND = "\u00ABextend\u00BB";
	
	/**
	 * Type of use case dependency.
	 */
	public enum Type 
	{None, Include, Extend}
	
	private Type aType = Type.None;
	
	/**
	 * Creates a general dependency.
	 */
	public UseCaseDependencyEdge()
	{}
	
	/**
	 * Creates a typed dependency.
	 * @param pType The type of dependency.
	 */
	public UseCaseDependencyEdge(Type pType)
	{
		aType = pType;
	}
	
	@Override
	protected void buildProperties()
	{
		super.buildProperties();
		properties().add("dependencyType", () -> aType, pType -> aType = Type.valueOf((String)pType));
	}
	
	@Override
	protected EdgeView generateView()
	{
		return new SegmentedEdgeView(this, SegmentationStyleFactory.createStraightStrategy(),
				() -> LineStyle.DOTTED, () -> ArrowHead.NONE,  () -> ArrowHead.V,
				() -> "", () -> obtainMiddleLabel(), () -> "");
	}
	
	@Override
	protected EdgeView2 generateView2()
	{
		return new SegmentedEdgeView2(this, SegmentationStyleFactory2.createStraightStrategy(),
				() -> LineStyle.DOTTED, () -> ArrowHead.NONE,  () -> ArrowHead.V,
				() -> "", () -> obtainMiddleLabel(), () -> "");
	}
	
	/**
	 * @return The type of dependency relation.
	 */
	public Type getType()
	{
		return aType;
	}
	
	/**
	 * @param pType The type of dependency relation.
	 */
	public void setType(Type pType)
	{
		aType = pType;
	}

	private String obtainMiddleLabel()
	{
		if( aType == Type.Include )
		{
			return LABEL_INCLUDE;
		}
		else if( aType == Type.Extend )
		{
			return LABEL_EXTEND;
		}
		else
		{
			return "";
		}
	}
}
