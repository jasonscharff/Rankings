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
/******************************************************************************
 * DirectedGraph class
 * 
 * The DirectedGraph class implements a DirectedGraph using an adjacency matrix
 * through HashMaps of HashMaps. In addition to keeping the main adjacency matrix
 * an additional adjacency Matrix is used during TopologicalSort that becomes acyclic
 * using "Super Nodes" (a collection of nodes) so the main adjacency matrix isn't modified.
 * As well, a JGraphT implementation of a graph is stored such that the JGraphT cycle
 * detector can be used (and its acyclic version for topsort).
 * Additionally, a Map of what each super node corresponds to in terms
 * of actual teams so the client is presented with real teams instead of "Mega Node X".
 * Finally, a reference to something of the Interface Ranking is stored so the appropriate tie
 * breaking methods can be called.
 * 
 ******************************************************************************/

public class DirectedGraph {
	HashMap<String, HashMap<String, Boolean>> adjacencyMatrix;
	HashMap<String, HashMap<String, Boolean>> acyclicAdjacencyMatrix;
	HashMap<String, Set<String>> superNodes;
	SimpleDirectedGraph<String, DefaultEdge> jgraph;
	SimpleDirectedGraph<String, DefaultEdge> acyclicJGraph;
	Ranking rankingMethod;

	/**
	 * Constructor method initializes all of the Maps, and the JGraphT graph. As well, takes
	 * parameter of Ranking and stores it so the appropriate tie breaking methods can be called
	 * @param r		An instance of a class implementing Ranking that is used for tie breaking.
	 */
	public DirectedGraph(Ranking r)
	{
		adjacencyMatrix = new HashMap<String, HashMap<String, Boolean>>();
		acyclicAdjacencyMatrix = new HashMap<String, HashMap<String, Boolean>>();
		superNodes = new HashMap<String, Set<String>>();
		jgraph = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		acyclicJGraph= new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		rankingMethod = r;
	}

	/**
	 * Returns a set of all the teams that beat the given team
	 * @param teamName		The team that is being queried for the teams it lost to.
	 * @return		The set of all teams that beat param team
	 */
	public Set<String> getConnections(String teamName)
	{
		return adjacencyMatrix.get(teamName).keySet();
	}

	/**
	 * Returns true
	 * @param v		A string that the client is attemtping to determine if it's in the graph or not.
	 * @return		True or false depending on if the graph contains the given vertex
	 */
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

	/**
	 * Returns a list of all vertices in the graph
	 * @return	All vertices in the graph
	 */
	public Set<String>getVerticies()
	{
		return adjacencyMatrix.keySet();
	}

	/**
	 * Determines if the two vertices are connected via an edge pointing towards the second parameter (to) 
	 * @param from	The vertex that might have "lost" to the secod vertex
	 * @param to	The vertex that might have "beat" the first vetex
	 * @return	True if there is a connection, false if not.
	 */
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

	/**
	 * This method adds a given vertex into the graph
	 * @param v		The vertex to be added into the graph
	 */
	public void addVertex(String v)
	{
		adjacencyMatrix.put(v, new HashMap<String, Boolean>());
		acyclicAdjacencyMatrix.put(v, new HashMap<String, Boolean>());
		jgraph.addVertex(v);
		acyclicJGraph.addVertex(v);
	}

	/**
	 * This method adds an edge connecting the first vertex to the second.
	 * @param from	The origin vertex
	 * @param to	The vertex being that the first vertex connects to
	 * @return		Returns true if the edge was successfully added (if both vertices) are present in the graph
	 * or false if at least one of the given vertices was not in the graph
	 */
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
			acyclicJGraph.addEdge(from, to);
			return true;
		}
	}

	/**
	 * Converts a given set to a LinkedList
	 * @param set	The set to be converted into a LinkedList
	 * @return		The LinkedList form of the given set
	 */
	private LinkedList<String> setToLinkedList(Set<String> set)
	{
		LinkedList<String> asList = new LinkedList<String>();
		for (String x: set)
		{
			asList.add(x);
		}
		return asList;
	}

	/**
	 * This method uses a modified form of Topological Sort to sort the graph.
	 * The first thing it does is adjust any cycles to be larger "super" nodes
	 * that map to a set of nodes in the cycle. From there the typical topological
	 * sort process begins by starting at a node, popping it onto the stack, then attempting
	 * to proceed down the line adding each node to the Stack that it reaches. Once it reaches a dead end
	 * it marks that node as used in the "used" map then pops something off the stack to go back a level.
	 * However, to account for a partially ordered set with multiple end points everything of equal status
	 * (same number of dependencies) is popped to the stack at each stage. While this does reduce efficiently,
	 * it allows for a partially ordered set to be ordered and it solves the issue of two distinct subsets
	 * by considering them together. After, topologically sorting them any ties are resolved and any vertex
	 * in a cycle is re-added and sorted.
	 * @return	A List of the graph in modified topological order.
	 */
	public List<String> topSort()
	{
		//Prep work
		adjustForCycles();
		LinkedList<String> keys = setToLinkedList(acyclicAdjacencyMatrix.keySet());
		List<String> sorted = new LinkedList<String>();
		Stack<String> backTrack = new Stack<String>();
		HashMap<String, Boolean>used = new HashMap<String, Boolean>();
		Iterator<String> it = keys.iterator();
		//Iterate through each vertex
		boolean toContinue = true;
		while (it.hasNext())
		{
			String node = it.next();
			if (used.get(node) != null)
			{
				toContinue = false;
			}
			else
			{
				//Push onto stack
				if(backTrack.isEmpty() == true || backTrack.peek().equals(node) == false)
				{
					backTrack.push(node);
				}
			}
			if (toContinue == true)
			{
				boolean continueLoop = true;
				while(continueLoop == true && backTrack.isEmpty() == false)
				{
					while(continueLoop == true && used.get(node) != null)
					{
						if(backTrack.isEmpty() == true)
						{
							continueLoop = false;
						}
						else
						{
							//Add all vertices of equal status.
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


					}
					if(continueLoop == true)
					{
						//Continue down path if possible
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
								//The break statement is used because there is no convenient way to exit a 
								//for loop and the for each loop makes it much clearer that I'm iterating through a set
								break;
							}
						}
						//Finish with vertex
						if (hasConnection == false)
						{
							sorted.add(node);
							used.put(node, true);
						}
					}

				}
			}

		}
		sorted = moveEntryPoints(sorted);
		resetAcyclicVersions();
		return removeCycleGroupingsAndFixTies(sorted);
	}

	/**
	 * This method fixes a flaw in the initial topological sort algorithm where the
	 * entry points don't necessarily start the List. It goes through and moves them to the front
	 * to fix this flaw
	 * @param sorted	The naturally topologically sorted list.
	 * @return			The sorted list with the entry points first.
	 */
	private List<String> moveEntryPoints(List<String>sorted)
	{
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
		return sorted;
	}

	/**
	 * This method removes any "super node" groupings that used to be cycles
	 * and sorts them based off of the given tie breaking criteria. Even
	 * if there are not any cycles this method still has a purpose in that it fixes the ties
	 * as each group is sorted by the given criteria.
	 * @param list		The topologically sorted list with cycles and ties
	 * @return			The final sorted list without any ties or cycle groupings.
	 */
	private List<String> removeCycleGroupingsAndFixTies(List<String> list)
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

	/**
	 * This method utilizes the JGraphT cycle detector to get all cycles containing a vertex
	 * and group them together. While a break statement shouldn't be necessary to avoid concurrency
	 * cycle exceptions every time a cycle is found it must break out then fix the cycle.
	 */
	public void adjustForCycles()
	{
		boolean continueLoop = true;
		while (continueLoop == true)
		{
			CycleDetector<String, DefaultEdge> detector = new CycleDetector<String, DefaultEdge>(acyclicJGraph);
			if (detector.detectCycles() == false)
			{
				continueLoop = false;
			}
			if (continueLoop == true)
			{
				Set<String> cycleSet = null;
				for (String key : acyclicAdjacencyMatrix.keySet())
				{
					cycleSet = detector.findCyclesContainingVertex(key);
					if (cycleSet.size()  > 0)
					{
						//The break statement is used because there is no convenient way to exit a 
						//for loop and the for each loop makes it much clearer that I'm iterating through a set
						break;
					}
				}
				addSuperNode(cycleSet);
			}
		}

	}

	/**
	 * This method creates a "Super Node" in the adjacency Matrix and in JGraphT graph that encapsulates
	 * everything in the parameter nodes
	 * @param nodes		A Set of all vertices that need to be part of the super node.
	 */
	private void addSuperNode(Set<String> nodes)
	{
		if (nodes == null)
		{
			return;
		}
		else
		{
			String identifier = "Mega Node " + (superNodes.keySet().size() + 1);
			acyclicJGraph.addVertex(identifier);
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
				acyclicJGraph.removeVertex(node);
			}

			for (String megaConnection : acyclicAdjacencyMatrix.get(identifier).keySet())
			{
				acyclicJGraph.addEdge(identifier, megaConnection);
			}
		}

	}

	/**
	 * Resets the Acyclic Versions of each graph so it can be sorted again.
	 */
	private void resetAcyclicVersions()
	{
		acyclicAdjacencyMatrix = (HashMap<String, HashMap<String, Boolean>>) adjacencyMatrix.clone();
		acyclicJGraph = (SimpleDirectedGraph<String, DefaultEdge>) jgraph.clone();

	}

}





