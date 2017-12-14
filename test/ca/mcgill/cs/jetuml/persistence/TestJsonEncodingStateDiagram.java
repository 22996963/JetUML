package ca.mcgill.cs.jetuml.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import ca.mcgill.cs.jetuml.diagrams.StateDiagramGraph;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.graph.Edge;
import ca.mcgill.cs.jetuml.graph.Graph;
import ca.mcgill.cs.jetuml.graph.Node;
import ca.mcgill.cs.jetuml.graph.edges.StateTransitionEdge;
import ca.mcgill.cs.jetuml.graph.nodes.CircularStateNode;
import ca.mcgill.cs.jetuml.graph.nodes.NoteNode;
import ca.mcgill.cs.jetuml.graph.nodes.StateNode;

public class TestJsonEncodingStateDiagram
{
	private StateDiagramGraph aGraph;
	
	@Before
	public void setup()
	{
		aGraph = new StateDiagramGraph();
	}
	
	/*
	 * Initializes a simple graph with a start and end node,
	 * two state nodes, and individual transitions between them.
	 */
	private void initiGraph1()
	{
		StateNode node1 = new StateNode();
		node1.getName().setText("Start");
		StateNode node2 = new StateNode();
		node2.getName().setText("End");
		CircularStateNode start = new CircularStateNode();
		start.setFinal(false);
		CircularStateNode end = new CircularStateNode();
		end.setFinal(true);
		StateTransitionEdge edge1 = new StateTransitionEdge();
		edge1.setMiddleLabel("edge1");
		StateTransitionEdge edge2 = new StateTransitionEdge();
		edge2.setMiddleLabel("edge2");
		StateTransitionEdge edge3 = new StateTransitionEdge();
		edge3.setMiddleLabel("edge3");
		aGraph.restoreRootNode(node1);
		aGraph.restoreRootNode(node2);
		aGraph.restoreRootNode(start);
		aGraph.restoreRootNode(end);
		aGraph.restoreEdge(edge1, start, node1);
		aGraph.restoreEdge(edge2, node1, node2);
		aGraph.restoreEdge(edge3, node2, end);
	}
	
	/*
	 * Initializes a graph with a single node at position 10,20
	 */
	private void initiGraph3()
	{
		StateNode node1 = new StateNode();
		node1.getName().setText("Node1");
		StateNode node2 = new StateNode();
		node2.getName().setText("Node2");

		StateTransitionEdge self1 = new StateTransitionEdge();
		self1.setMiddleLabel("self1");
		StateTransitionEdge self2 = new StateTransitionEdge();
		self2.setMiddleLabel("self2");
		StateTransitionEdge edge1 = new StateTransitionEdge();
		edge1.setMiddleLabel("edge1");
		StateTransitionEdge edge2 = new StateTransitionEdge();
		edge2.setMiddleLabel("edge2");
		
		aGraph.restoreRootNode(node1);
		aGraph.restoreRootNode(node2);
		aGraph.restoreEdge(self1, node1, node1);
		aGraph.restoreEdge(self2, node1, node1);
		aGraph.restoreEdge(edge1, node1, node2);
		aGraph.restoreEdge(edge2, node1, node2);
	}
	
	/*
	 * Initializes a graph with a node with two self-edges,
	 * and two transitions to a second node.
	 */
	private void initiGraph2()
	{
		StateNode node1 = new StateNode();
		node1.getName().setText("The Node");
		node1.moveTo(new Point(10,20));
		aGraph.restoreRootNode(node1);
	}
	
	@Test
	public void testEmpty()
	{
		JSONObject object = JsonEncoder.encode(aGraph);
		assertHasKeys(object, "diagram", "nodes", "edges", "version");
		assertEquals("StateDiagramGraph", object.getString("diagram"));
		assertEquals(0, object.getJSONArray("nodes").length());	
		assertEquals(0, object.getJSONArray("edges").length());				
	}
	
	@Test
	public void testSingleNode()
	{
		aGraph.restoreRootNode(new NoteNode());
		
		JSONObject object = JsonEncoder.encode(aGraph);
		assertHasKeys(object, "diagram", "nodes", "edges", "version");
		assertEquals("StateDiagramGraph", object.getString("diagram"));
		assertEquals(1, object.getJSONArray("nodes").length());	
		assertEquals(0, object.getJSONArray("edges").length());	
		JSONObject node = object.getJSONArray("nodes").getJSONObject(0);
		assertHasKeys(node, "type", "id", "x", "y", "name");
		assertEquals(0, node.getInt("x"));
		assertEquals(0, node.getInt("y"));
		assertEquals("", node.getString("name"));
		assertEquals("NoteNode", node.getString("type"));
		assertEquals(0, node.getInt("id"));
	}
	
	@Test
	public void testEncodeGraph1()
	{
		initiGraph1();

		JSONObject object = JsonEncoder.encode(aGraph);
		
		assertHasKeys(object, "diagram", "nodes", "edges", "version");
		assertEquals("StateDiagramGraph", object.getString("diagram"));
		assertEquals(4, object.getJSONArray("nodes").length());	
		assertEquals(3, object.getJSONArray("edges").length());	
		
		JSONArray nodes = object.getJSONArray("nodes");
		JSONObject node1b = find(nodes, build("type", "StateNode", "name", "Start"));
		JSONObject node2b = find(nodes, build("type", "StateNode", "name", "End"));
		JSONObject startb = find(nodes, build("type", "CircularStateNode", "finalState", false));
		JSONObject endb = find(nodes, build("type", "CircularStateNode", "finalState", true));
		
		JSONArray edges = object.getJSONArray("edges");
		JSONObject edge1b = find(edges, build("type", "StateTransitionEdge", "middleLabel", "edge1"));
		JSONObject edge2b = find(edges, build("type", "StateTransitionEdge", "middleLabel", "edge2"));
		JSONObject edge3b = find(edges, build("type", "StateTransitionEdge", "middleLabel", "edge3"));

		assertEquals(edge1b.getInt("start"), startb.getInt("id"));
		assertEquals(edge1b.getInt("end"), node1b.getInt("id"));
		assertEquals(edge2b.getInt("start"), node1b.getInt("id"));
		assertEquals(edge2b.getInt("end"), node2b.getInt("id"));
		assertEquals(edge3b.getInt("start"), node2b.getInt("id"));
		assertEquals(edge3b.getInt("end"), endb.getInt("id"));
	}
	
	@Test
	public void testEncodeDecodeGraph1()
	{
		initiGraph1();
		StateDiagramGraph graph = (StateDiagramGraph) JsonDecoder.decode(JsonEncoder.encode(aGraph));
		
		StateNode node1 = (StateNode) findRootNode(graph, StateNode.class, build("name", "Start"));
		StateNode node2 = (StateNode) findRootNode(graph, StateNode.class, build("name", "End"));
		CircularStateNode start = (CircularStateNode) findRootNode(graph, CircularStateNode.class, build("finalState", false));
		CircularStateNode end = (CircularStateNode) findRootNode(graph, CircularStateNode.class, build("finalState", true));
		StateTransitionEdge edge1 = (StateTransitionEdge) findEdge(graph, StateTransitionEdge.class, build( "middleLabel", "edge1"));
		StateTransitionEdge edge2 = (StateTransitionEdge) findEdge(graph, StateTransitionEdge.class, build( "middleLabel", "edge2"));
		StateTransitionEdge edge3 = (StateTransitionEdge) findEdge(graph, StateTransitionEdge.class, build( "middleLabel", "edge3"));
		
		assertSame(edge1.getStart(), start);
		assertSame(edge1.getEnd(), node1);
		assertSame(edge2.getStart(), node1);
		assertSame(edge2.getEnd(), node2);
		assertSame(edge3.getStart(), node2);
		assertSame(edge3.getEnd(), end);
	}
	
	@Test
	public void testEncodeDecodeGraph2()
	{
		initiGraph2();
		StateDiagramGraph graph = (StateDiagramGraph) JsonDecoder.decode(JsonEncoder.encode(aGraph));
		
		StateNode node1 = (StateNode) findRootNode(graph, StateNode.class, build("name", "The Node"));
		assertEquals(new Point(10,20), node1.position());
		assertEquals("The Node", node1.getName().getText());
	}
	
	@Test
	public void testEncodeDecodeGraph3()
	{
		initiGraph3();
		StateDiagramGraph graph = (StateDiagramGraph) JsonDecoder.decode(JsonEncoder.encode(aGraph));
		
		StateNode node1 = (StateNode) findRootNode(graph, StateNode.class, build("name", "Node1"));
		StateNode node2 = (StateNode) findRootNode(graph, StateNode.class, build("name", "Node2"));
		StateTransitionEdge self1 = (StateTransitionEdge) findEdge(graph, StateTransitionEdge.class, build( "middleLabel", "self1"));
		StateTransitionEdge self2 = (StateTransitionEdge) findEdge(graph, StateTransitionEdge.class, build( "middleLabel", "self2"));
		StateTransitionEdge edge1 = (StateTransitionEdge) findEdge(graph, StateTransitionEdge.class, build( "middleLabel", "edge1"));
		StateTransitionEdge edge2 = (StateTransitionEdge) findEdge(graph, StateTransitionEdge.class, build( "middleLabel", "edge2"));

		assertSame(self1.getStart(), node1);
		assertSame(self1.getEnd(), node1);
		assertSame(self2.getStart(), node1);
		assertSame(self2.getEnd(), node1);
		assertSame(edge1.getStart(), node1);
		assertSame(edge1.getEnd(), node2);
		assertSame(edge2.getStart(), node1);
		assertSame(edge2.getEnd(), node2);

	}
	
	private void assertHasKeys(JSONObject pObject, String... pKeys)
	{
		for( String key : pKeys )
		{
			assertTrue(pObject.has(key));
		}
	}
	
	/*
	 * Finds the object in an array with the specified properties
	 */
	private JSONObject find(JSONArray pArray, Properties pProperties)
	{
		JSONObject found = null;
		for( int i = 0; i < pArray.length(); i++ )
		{
			boolean match = true;
			JSONObject object = pArray.getJSONObject(i);
			for( String key : pProperties )
			{
				if( !object.has(key))
				{
					match = false;
				}
				else
				{
					if(!object.get(key).equals(pProperties.get(key)))
					{
						match = false;
					}
				}
			}
			if( match )
			{
				found = object;
				break;
			}
		}
		assertNotNull(found);
		return found;
	}
	
	private Node findRootNode(Graph pGraph, Class<?> pClass, Properties pProperties)
	{
		for( Node node : pGraph.getRootNodes() )
		{
			if( node.getClass() == pClass )
			{
				boolean match = true;
				Properties nodeProperties = node.properties();
				for( String key : pProperties )
				{
					if( !nodeProperties.get(key).equals(pProperties.get(key)))
					{
						match = false;
						break;
					}
				}
				if( match )
				{
					return node;
				}
			}
		}
		fail("Expected node not found");
		return null;
	}
	
	private Edge findEdge(Graph pGraph, Class<?> pClass, Properties pProperties)
	{
		for( Edge edge : pGraph.getEdges() )
		{
			if( edge.getClass() == pClass )
			{
				boolean match = true;
				Properties edgeProperties = edge.properties();
				for( String key : pProperties )
				{
					if( !edgeProperties.get(key).equals(pProperties.get(key)))
					{
						match = false;
						break;
					}
				}
				if( match )
				{
					return edge;
				}
			}
		}
		fail("Expected edge not found");
		return null;
	}
	
	/*
	 * Creates a properties object with keys as even arguments and values as odd arguments.
	 */
	private Properties build(Object... pInput)
	{
		Properties properties = new Properties();
		for( int i = 0; i < pInput.length; i+=2 )
		{
			properties.put((String)pInput[i], pInput[i+1]);
		}
		return properties;
	}
}
