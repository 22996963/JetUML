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
package ca.mcgill.cs.stg.jetuml.framework;

import java.awt.geom.Point2D;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import ca.mcgill.cs.stg.jetuml.diagrams.ClassDiagramGraph;
import ca.mcgill.cs.stg.jetuml.graph.ClassNode;
import ca.mcgill.cs.stg.jetuml.graph.DependencyEdge;
import ca.mcgill.cs.stg.jetuml.graph.Edge;
import ca.mcgill.cs.stg.jetuml.graph.PackageNode;

public class TestSegmentationStrategies 
{
	private PackageNode aNode1;
	private PackageNode aNode2;
	private ClassNode aNode3;
	private ClassNode aNode4;
	private PackageNode aNode5;
	private ClassDiagramGraph aGraph;
	
	@Before
	public void setup()
	{
		// Default-sized node rooted at (30,30)
		aNode1 = new PackageNode();
		aNode1.translate(30, 30);
		aNode2 = new PackageNode();
		aNode2.translate(200, 100);
		aNode3 = new ClassNode();
		aNode4 = new ClassNode();
		aNode3.translate(20, 20);
		aNode4.translate(110, 20);
		aNode5 = new PackageNode();
		aNode5.translate(200, 250);
		aGraph = new ClassDiagramGraph();
		aGraph.insertNode(aNode1);
		aGraph.insertNode(aNode2);
		aGraph.insertNode(aNode3);
		aGraph.insertNode(aNode4);
		aGraph.insertNode(aNode5);
	}
	
	@Test
	public void testSelfEdge1()
	{
		Point2D[] points = SegmentationStyleFactory.createStraightStrategy().getPath(aNode1, aNode1, aGraph);
		assertEquals( 5, points.length );
		assertEquals( new Point2D.Double(110,50), points[0]);
		assertEquals( new Point2D.Double(110,30), points[1]);
		assertEquals( new Point2D.Double(150,30), points[2]);
		assertEquals( new Point2D.Double(150,70), points[3]);
		assertEquals( new Point2D.Double(130,70), points[4]);
	}
	
	@Test
	public void testSelfEdge2()
	{
		Point2D[] points = SegmentationStyleFactory.createStraightStrategy().getPath(aNode3, aNode3, aGraph);
		assertEquals( 5, points.length );
		assertEquals( new Point2D.Double(100,20), points[0]);
		assertEquals( new Point2D.Double(100,0), points[1]);
		assertEquals( new Point2D.Double(140,0), points[2]);
		assertEquals( new Point2D.Double(140,40), points[3]);
		assertEquals( new Point2D.Double(120,40), points[4]);
	}
	
	@Test
	public void testStraight1()
	{
		// Note that path should only work if there's an edge between the two nodes.
		Edge edge = new DependencyEdge();
		edge.connect(aNode1, aNode2, aGraph);
		aGraph.insertEdge(edge);
		
		edge = new DependencyEdge();
		edge.connect(aNode2, aNode1, aGraph);
		aGraph.insertEdge(edge);
		
		Point2D[] points = SegmentationStyleFactory.createStraightStrategy().getPath(aNode1, aNode2, aGraph);
		assertEquals( 2, points.length );
		assertEquals( new Point2D.Double(130,66), points[0]);
		assertEquals( new Point2D.Double(200,136), points[1]);
		
		points = SegmentationStyleFactory.createStraightStrategy().getPath(aNode2, aNode1, aGraph);
		assertEquals( 2, points.length );
		assertEquals( new Point2D.Double(130,74), points[1]);
		assertEquals( new Point2D.Double(200,144), points[0]);
	}
	
	@Test
	public void testStraight2()
	{
		Edge edge = new DependencyEdge();
		edge.connect(aNode3, aNode4, aGraph);
		aGraph.insertEdge(edge);
		Point2D[] points = SegmentationStyleFactory.createStraightStrategy().getPath(aNode3, aNode4, aGraph);
		assertEquals( 2, points.length );
		assertEquals( new Point2D.Double(120,50), points[0]);
		assertEquals( new Point2D.Double(110,50), points[1]);
	}
	
	@Test
	public void testHVH1()
	{
		Point2D[] points = SegmentationStyleFactory.createHVHStrategy().getPath(aNode1, aNode2, aGraph);
		assertEquals( 4, points.length );
		assertEquals( new Point2D.Double(130,70), points[0]);
		assertEquals( new Point2D.Double(165,70), points[1]);
		assertEquals( new Point2D.Double(165,140), points[2]);
		assertEquals( new Point2D.Double(200,140), points[3]);
		
		points = SegmentationStyleFactory.createHVHStrategy().getPath(aNode2, aNode1, aGraph);
		assertEquals( 4, points.length );
		assertEquals( new Point2D.Double(130,70), points[3]);
		assertEquals( new Point2D.Double(165,70), points[2]);
		assertEquals( new Point2D.Double(165,140), points[1]);
		assertEquals( new Point2D.Double(200,140), points[0]);
	}
	
	@Test
	public void testHVH2()
	{
		Edge edge = new DependencyEdge();
		edge.connect(aNode3, aNode4, aGraph);
		aGraph.insertEdge(edge);
		Point2D[] points = SegmentationStyleFactory.createHVHStrategy().getPath(aNode3, aNode4, aGraph);
		assertEquals( 2, points.length );
		assertEquals( new Point2D.Double(120,50), points[0]);
		assertEquals( new Point2D.Double(110,50), points[1]);
	}
	
	@Test
	public void testHVH3()
	{
		ClassNode node = aNode3.clone();
		node.translate(150, 5);
		Point2D[] points = SegmentationStyleFactory.createHVHStrategy().getPath(aNode3, node, aGraph);
		assertEquals( 2, points.length );
		assertEquals( new Point2D.Double(120,55), points[0]);
		assertEquals( new Point2D.Double(170,55), points[1]);
	}
	
	@Test
	public void testVHV1()
	{
		Point2D[] points = SegmentationStyleFactory.createVHVStrategy().getPath(aNode1, aNode2, aGraph);
		assertEquals( 4, points.length );
		assertEquals( new Point2D.Double(130,70), points[0]);
		assertEquals( new Point2D.Double(165,70), points[1]);
		assertEquals( new Point2D.Double(165,140), points[2]);
		assertEquals( new Point2D.Double(200,140), points[3]);
		
		points = SegmentationStyleFactory.createVHVStrategy().getPath(aNode2, aNode1, aGraph);
		assertEquals( 4, points.length );
		assertEquals( new Point2D.Double(130,70), points[3]);
		assertEquals( new Point2D.Double(165,70), points[2]);
		assertEquals( new Point2D.Double(165,140), points[1]);
		assertEquals( new Point2D.Double(200,140), points[0]);
	}
	
	@Test
	public void testVHV2()
	{
		Edge edge = new DependencyEdge();
		edge.connect(aNode3, aNode4, aGraph);
		aGraph.insertEdge(edge);
		Point2D[] points = SegmentationStyleFactory.createVHVStrategy().getPath(aNode3, aNode4, aGraph);
		assertEquals( 2, points.length );
		assertEquals( new Point2D.Double(120,50), points[0]);
		assertEquals( new Point2D.Double(110,50), points[1]);
	}
	
	@Test
	public void testVHV3()
	{
		Point2D[] points = SegmentationStyleFactory.createVHVStrategy().getPath(aNode2, aNode5, aGraph);
		assertEquals( 2, points.length );
		assertEquals( new Point2D.Double(250,180), points[0]);
		assertEquals( new Point2D.Double(250,250), points[1]);
		
		aNode5.translate(100, 0);
		points = SegmentationStyleFactory.createVHVStrategy().getPath(aNode2, aNode5, aGraph);
		assertEquals( 4, points.length );
		assertEquals( new Point2D.Double(250,180), points[0]);
		assertEquals( new Point2D.Double(250,215), points[1]);
		assertEquals( new Point2D.Double(350,215), points[2]);
		assertEquals( new Point2D.Double(350,250), points[3]);
		
		points = SegmentationStyleFactory.createVHVStrategy().getPath(aNode5, aNode2, aGraph);
		assertEquals( 4, points.length );
		assertEquals( new Point2D.Double(250,180), points[3]);
		assertEquals( new Point2D.Double(250,215), points[2]);
		assertEquals( new Point2D.Double(350,215), points[1]);
		assertEquals( new Point2D.Double(350,250), points[0]);
	}
}
