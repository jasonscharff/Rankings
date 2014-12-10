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


//Before popping, just check through x number excluding used.


public class DirectedGraph {
	HashMap<String, HashMap<String, Boolean>> adjacencyMatrix;
	HashMap<String, Set<String>> superNodes;
	public DirectedGraph()
	{
		adjacencyMatrix = new HashMap<String, HashMap<String, Boolean>>();
		superNodes = new HashMap<String, Set<String>>();
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
		LinkedList<String> keys = setToLinkedList(adjacencyMatrix.keySet());
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
						int numConnections = adjacencyMatrix.get(node).keySet().size(); 
						for (String team : adjacencyMatrix.keySet())
						{
							Set<String>keySet = adjacencyMatrix.get(team).keySet();
							if (keySet.size() <= numConnections && team.equals(node) == false && used.get(team) == null)
							{
								backTrack.push(team);
							}
							//							if (keySet.size() ==0 && team.equals(node) == false && used.get(team) == null)
							//							{
							//								backTrack.push(team);
							//							}
						}
						node = backTrack.pop();

					}
					HashMap<String, Boolean> map = adjacencyMatrix.get(node);
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
		for (String key: adjacencyMatrix.keySet())
		{
			starters.add(key);
		}
		for (String key : adjacencyMatrix.keySet())
		{
			HashMap<String, Boolean> temp = adjacencyMatrix.get(key);
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
	
		public void adjustForCycles(CycleDetector detector)
		{
			for (String key : adjacencyMatrix.keySet())
			{
				Set<String> cycleSet = detector.findCyclesContainingVertex(key);
				if (cycleSet.size() !=0)
				{
					String identifier = "Mega Node " + superNodes.keySet().size() + 1;
					superNodes.put(identifier, cycleSet);
					//Go through each and set everything that connects to contents of mega node as connecting to 
					//the mega node instead of the individual. Go through and find everything it connects to besides a cyclical member
					//and make the mega node connect to it. 
					//Inner rank
				}
			}
		}
	
}





