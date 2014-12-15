import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public class DirectedGraph {
	HashMap<String, HashMap<String, Boolean>> adjacencyMatrix;
	HashMap<String, HashMap<String, Boolean>> acyclicAdjacencyMatrix;
	HashMap<String, Set<String>> superNodes;
	SimpleDirectedGraph<String, DefaultEdge> jgraph;
	ranking rankingMethod;

	public DirectedGraph(ranking r)
	{
		adjacencyMatrix = new HashMap<String, HashMap<String, Boolean>>();
		acyclicAdjacencyMatrix = new HashMap<String, HashMap<String, Boolean>>();
		superNodes = new HashMap<String, Set<String>>();
		jgraph = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		rankingMethod = r;
	}

	public Set<String> getConnections(String teamName)
	{
		return adjacencyMatrix.get(teamName).keySet();
	}
	public boolean containsVertex (String v)
	{
		if (adjacencyMatrix.get(v) !=null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public Set<String>getVerticies()
	{
		return adjacencyMatrix.keySet();
	}

	public boolean isDependency(String from, String to)
	{
		HashMap<String, Boolean> temp = adjacencyMatrix.get(from);
		if (temp.get(to) == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public void addVertex(String v)
	{
		adjacencyMatrix.put(v, new HashMap<String, Boolean>());
		acyclicAdjacencyMatrix.put(v, new HashMap<String, Boolean>());
		jgraph.addVertex(v);
	}

	public boolean addEdge(String from, String to)
	{
		if (adjacencyMatrix.get(from) == null)
		{
			return false;
		}
		else
		{
			HashMap<String, Boolean> temp = adjacencyMatrix.get(from);
			temp.put(to, true);
			adjacencyMatrix.put(from, temp);
			acyclicAdjacencyMatrix.put(from,temp);
			jgraph.addEdge(from, to);
			return true;
		}
	}

	private LinkedList<String> setToLinkedList(Set<String> set)
	{
		LinkedList<String> asList = new LinkedList<String>();
		for (String x: set)
		{
			asList.add(x);
		}
		return asList;
	}
	public List<String> topSort()
	{
		adjustForCycles();
		LinkedList<String> keys = setToLinkedList(acyclicAdjacencyMatrix.keySet());
		List<String> sorted = new LinkedList<String>();
		Stack<String> backTrack = new Stack<String>();
		HashMap<String, Boolean>used = new HashMap<String, Boolean>();
		Iterator<String> it = keys.iterator();
		while (it.hasNext())
		{
			String node = it.next();
			if (used.get(node) != null)
			{
				continue;
			}
			else
			{
				if(backTrack.isEmpty() == true || backTrack.peek().equals(node) == false)
				{
					backTrack.push(node);
				}
			}
			outerloop:
				while(backTrack.isEmpty() == false)
				{
					while(used.get(node) != null)
					{
						if(backTrack.isEmpty() == true)
						{
							break outerloop;
						}
						int numConnections = acyclicAdjacencyMatrix.get(node).keySet().size(); 
						for (String team : acyclicAdjacencyMatrix.keySet())
						{
							Set<String>keySet = acyclicAdjacencyMatrix.get(team).keySet();
							if (keySet.size() <= numConnections && team.equals(node) == false && used.get(team) == null)
							{
								backTrack.push(team);
							}
						}
						node = backTrack.pop();

					}
					HashMap<String, Boolean> map = acyclicAdjacencyMatrix.get(node);
					boolean hasConnection = false;
					for (String key : map.keySet())
					{
						if (used.get(key) == null)
						{
							hasConnection = true;
							if(backTrack.isEmpty() == true || backTrack.peek().equals(node) == false)
							{
								backTrack.push(node);
							}
							node = key;
							break;
						}
					}

					if (hasConnection == false)
					{
						sorted.add(node);
						used.put(node, true);
					}
				}

		}
		Set<String>starters = new HashSet<String>();
		for (String key: acyclicAdjacencyMatrix.keySet())
		{
			starters.add(key);
		}
		for (String key : acyclicAdjacencyMatrix.keySet())
		{
			HashMap<String, Boolean> temp = acyclicAdjacencyMatrix.get(key);
			starters.removeAll(temp.keySet());
		}
		for (String starter : starters)
		{
			sorted.remove(starter);
		}
		for (String starter : starters)
		{
			sorted.add(starter);
		}
		return removeCycleGroupings(sorted);

	}

	private List<String> removeCycleGroupings(List<String> list)
	{
		rankingMethod.adjustTotalsForWins(this);
		List<String> toReturn = new ArrayList<String>();
		List<String> group = new ArrayList<String>();
		for (String element : list)
		{
			if (element.contains("Mega Node"))
			{
				if (group.size() > 1)
				{
					group = rankingMethod.fixTies(group);
				}
				toReturn.addAll(group);
				group.clear();
				List<String> items = new ArrayList<String>();
				items.addAll(superNodes.get(element));
				items = rankingMethod.fixTies(items);
				for (String item : items)
				{
					toReturn.add(item);
				}
			}
			else
			{
				group.add(element);
			}
		}
		if (group.size() > 1)
		{
			group = rankingMethod.fixTies(group);
		}
		toReturn.addAll(group);
		return toReturn;
	}

	public void adjustForCycles()
	{
		SimpleDirectedGraph <String, DefaultEdge> clonedJgraph = (SimpleDirectedGraph<String, DefaultEdge>) jgraph.clone();
		//Iterate through adjacnecny Matrix keyset
		//Remove all inner portion connections, keep others. Have hashset of contents
		whileLoop:
			while (true)
			{
				CycleDetector<String, DefaultEdge> detector = new CycleDetector<String, DefaultEdge>(jgraph);
				if (detector.detectCycles() == false)
				{
					break whileLoop;
				}
				Set<String> cycleSet = null;
				forLoop:
					for (String key : acyclicAdjacencyMatrix.keySet())
					{
						cycleSet = detector.findCyclesContainingVertex(key);
						if (cycleSet.size()  > 0)
						{
							break forLoop;
						}
					}
				addSuperNode(cycleSet);

			}

	}

	private void addSuperNode(Set<String> nodes)
	{
		if (nodes == null)
		{
			return;
		}
		else
		{
			String identifier = "Mega Node " + (superNodes.keySet().size() + 1);
			jgraph.addVertex(identifier);
			superNodes.put(identifier, nodes);
			for (String node : nodes)
			{
				HashMap<String, Boolean> temp = acyclicAdjacencyMatrix.get(node);
				Iterator <String>connectionIterator = temp.keySet().iterator();
				while (connectionIterator.hasNext())
				{
					String connection = connectionIterator.next();
					if (nodes.contains(connection))
					{
						connectionIterator.remove();
					}
				}
				HashMap<String, Boolean> currentConnections = acyclicAdjacencyMatrix.get(identifier);

				if(currentConnections == null)
				{
					acyclicAdjacencyMatrix.put(identifier, temp);
				}
				else
				{
					for (String connection : temp.keySet())
					{
						currentConnections.put(connection, true);
					}
					acyclicAdjacencyMatrix.put(identifier, currentConnections);
				}

				acyclicAdjacencyMatrix.remove(node);
				jgraph.removeVertex(node);
			}

			for (String megaConnection : acyclicAdjacencyMatrix.get(identifier).keySet())
			{
				jgraph.addEdge(identifier, megaConnection);
			}
		}

	}


}





