import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.io.*;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
public class rankingsMain 
{
	private final static String fileName = "rankings.txt";
	private final static int distanceBetweenTeams = 4;

	public static void main(String[] args) 
	{
		ArrayList<Object>fileResponses = readFile();
		DirectedGraph graph = (DirectedGraph) fileResponses.get(0);
		List<String> sortedGraph = graph.topSort();
		System.out.println(sortedGraph);
		sortedGraph = fixTies(sortedGraph, (HashMap<String, Integer>)fileResponses.get(1), (HashMap<String, Integer>)fileResponses.get(2));
		System.out.println(sortedGraph);
		
//		if (graph != null)
//		{
//			HashMap<String, Integer> winCount = (HashMap<String, Integer>) fileResponses.get(1);
//			HashMap<String, Integer> totalScore = (HashMap<String, Integer>) fileResponses.get(2);
//			ArrayList<String>rankings = fixTies(graph, winCount, totalScore);
//			System.out.println(rankings);
//		}
		
		
	}

	
	public static ArrayList<String> fixTies (List<String> topSorted, HashMap<String, Integer> winCount, HashMap<String, Integer> totalScore)
	{
		ArrayList<String> finalList = new ArrayList<String>();
		Iterator<String> it = topSorted.iterator();
		String next = it.next();
		while (it.hasNext())
		{
			String firstRank = next;
			int numWins = winCount.get(firstRank);
			ArrayList<String> group = new ArrayList<String>();
			group.add(firstRank);
			next = it.next();
			while (winCount.get(next) == numWins)
			{
				group.add(next);
				if(it.hasNext())
				{
					next = it.next();
				}
				else
				{
					break;
				}
				
			}
			if (group.size() > 1)
			{
				group = innerRank(group, totalScore);
			}
			finalList = addArrayContents(finalList, group);
			
		}
		return finalList;
	
	}
	public static ArrayList<String> innerRank(ArrayList<String> teams, HashMap<String, Integer>totalScore)
	{
		ArrayList<teamNode> toSort = new ArrayList<teamNode>();
		for (String team : teams)
		{
			teamNode node = new teamNode(team, totalScore.get(team));
			toSort.add(node);
		}
		Collections.sort(toSort);
		Collections.reverse(toSort);
		ArrayList<String> toReturn = new ArrayList<String>();
		for (teamNode node : toSort)
		{
			toReturn.add(node.getTeamName());
		}
		return toReturn;
	}
	
	public static ArrayList<String> addArrayContents(ArrayList<String> firstArray, ArrayList<String> secondArray)
	{
		for (String x: secondArray)
		{
			firstArray.add(x);
		}
		return firstArray;
	}
	
	public static ArrayList<Object> readFile()
	{
		Scanner s;
		DirectedGraph graph = new DirectedGraph();
		HashMap <String, Integer> totalScoreMap = new HashMap<String, Integer>();
		HashMap <String, Integer> winCount = new HashMap<String, Integer>();
		try 
		{
			s = new Scanner(new BufferedReader(new FileReader(fileName)));
			while(s.hasNext())
			{
				String line = s.nextLine();
				String linePairTwo = s.nextLine();
				int indexOfSpace = linePairTwo.indexOf(" ");
				int firstNumber = Integer.parseInt(linePairTwo.substring(0, indexOfSpace));
				int secondNumber = Integer.parseInt(linePairTwo.substring(indexOfSpace + 1));
				indexOfSpace = line.indexOf(" ");
				String teamOne = line.substring(0, indexOfSpace);
				String teamTwo = line.substring(indexOfSpace + distanceBetweenTeams);
				if (graph.containsVertex(teamOne) == false)
				{
					totalScoreMap.put(teamOne, 0);
					winCount.put(teamOne, 0);
					graph.addVertex(teamOne);
				}
				if (graph.containsVertex(teamTwo) == false)
				{
					totalScoreMap.put(teamTwo, 0);
					winCount.put(teamTwo, 0);
					graph.addVertex(teamTwo);
				}

				if(firstNumber > secondNumber)
				{
					int currentCount = winCount.get(teamOne);
					winCount.put(teamOne, currentCount + 1);
					
					graph.addEdge(teamTwo, teamOne);
	

					
				}
				else if (firstNumber < secondNumber)
				{
					int currentCount = winCount.get(teamTwo);
					winCount.put(teamTwo, currentCount + 1);
					graph.addEdge(teamOne, teamTwo);


				}
				else
				{
					//Special Case are equal
					System.out.println("Special Case Activated. Tie game.");
				}			
				int currentPointCount = totalScoreMap.get(teamOne);
				totalScoreMap.put(teamOne, currentPointCount + firstNumber);
				currentPointCount = totalScoreMap.get(teamTwo);
				totalScoreMap.put(teamTwo, currentPointCount + secondNumber);
				
			}

			s.close();
			ArrayList<Object> toReturn = new ArrayList<Object>(3);
			toReturn.add(graph);
			toReturn.add(winCount);
			toReturn.add(totalScoreMap);
			return toReturn;
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("File not found");
			return null;
		} 




	}
}

class teamNode implements Comparable
{
	String teamName;
	int totalScore;
	
	public teamNode(String name, int totalScore)
	{
		this.teamName = name;
		this.totalScore = totalScore;
	}
	
	public String getTeamName()
	{
		return teamName;
	}
	
	public int getTotalScore()
	{
		return totalScore;
	}

	public int compareTo(Object o) 
	{
		return this.totalScore - ((teamNode) o).getTotalScore();
	}
}




