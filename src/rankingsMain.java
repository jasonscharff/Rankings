import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.*;
/******************************************************************************
 * 
 * Name:		Jason Scharff
 * Block:		A
 * Date:		12/14/14
 * 
 *  Program #3: Rankings
 *  Description:
 *  	This program ranks a data set given a file using a modified Topological sort that
 *  	can handle four cases: a perfectly ordered set (standard Topological sort), a partially
 *  	ordered set (modifications that allow for two vertices of equal ranking to be placed together
 *  	and tie breaking criteria), two or more distinct subgraphs (handled as part of the partially
 *  	ordered set component), and the cyclic case. To assist with the cyclic case the JGraphT
 *  	cycle detector is used to detect cycles, but then the graph is internally modified under my own
 *  	code. Due to the complex nature and the plethora of cases that could be cyclic it is not guaranteed
 *  	to work in all cyclic cases, but it should work in most. This class is used as the client for
 *  	the DirectedGraph class. The client handles file reading and tie breaking criteria which is
 *  	fed into the DirectedGraph class via the Ranking interface.
 *      
 ******************************************************************************/
public class RankingsMain implements Ranking
{
	//Final variables
	private final static String FILE_NAME = "drawnset.txt";
	private final static int DISTANCE_BETWEEN_TEAMS = 4;
	/*Class scopes are used because the DirectedGraph class
	 * calls the tie breaking methods (which the two Maps below
	 * are used in) because the criteria could change and
	 * I wanted the DirectedGraph class to not be specific to this case
	 * in terms of tie breaking criteria.
	 */
	private static Map<String, Integer> winCount;
	private static Map<String, Integer> totalScore;

	public static void main(String[] args) 
	{
		Map<String, Object>fileResponses = readFile();
		if(fileResponses != null)
		{
			DirectedGraph graph = (DirectedGraph) fileResponses.get("graph");
			totalScore = (HashMap<String, Integer>) fileResponses.get("score");
			winCount = (Map<String, Integer>)fileResponses.get("wins");
			List<String> sortedGraph = graph.topSort();
			printList(sortedGraph);
		}
		
	}
	
	/**
	 * Prints the final list in rank order using numeral rankings.
	 * @param list	The list to be printed
	 */
	public static void printList(List<String> list)
	{
		for (int i = 0; i < list.size(); i++)
		{
			System.out.println((i+1) + ". " + list.get(i));
		}
	}

	/**
	 *  This method adjusts the total score of each team (used as tie breaking
	 * criteria) such that if team A beats team B team A will always have a higher score
	 * even if they come out in the graph as being equal. It is required for the Rankings interface.
	 */
	public void adjustTotalsForWins(DirectedGraph graph)
	{
		for (String team : graph.getVerticies())
		{
			int scoreLoser = totalScore.get(team);
			for (String beat : graph.getConnections(team))
			{
				int scoreWinner = totalScore.get(beat);
				if (scoreLoser > scoreWinner)
				{
					totalScore.put(beat, scoreLoser + 1);
				}
			}
		}
	
	}
	
	/**
	 * This method implements the tie breaking criteria component required by the Rankings interface.
	 * It goes through a topologically sorted list and then determines two things to be of equal ranking
	 * if they are next to each other and they have the same number of wins. If they are of equal ranking
	 * they are sorted in the method inner-rank based off their total scores.
	 */
	public ArrayList<String> fixTies (List<String> presorted)
	{
		ArrayList<String> finalList = new ArrayList<String>();
		Iterator<String> it = presorted.iterator();
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
			finalList.addAll(group);
			
		}
		return finalList;
	
	}
	
	/**
	 * This method given a list of teams of equal rank sorts them using quicksort based off of their
	 * total score adjusted for teams beating teams in adjustForTotalWins. 
	 * @param teams		An ArrayList of teams that are deemed equal and a tie needs to be resolved
	 * @param totalScore	A Map of each teams total score adjusted for certain wins against other teams
	 * @return		A sorted ArrayList that resolves the ties for that "group".
	 */
	public static ArrayList<String> innerRank(ArrayList<String> teams, Map<String, Integer>totalScore)
	{
		ArrayList<TeamNode> toSort = new ArrayList<TeamNode>();
		for (String team : teams)
		{
			TeamNode node = new TeamNode(team, totalScore.get(team));
			toSort.add(node);
		}
		Collections.sort(toSort);
		Collections.reverse(toSort);
		ArrayList<String> toReturn = new ArrayList<String>();
		for (TeamNode node : toSort)
		{
			toReturn.add(node.getTeamName());
		}
		return toReturn;
	}
	
	/**
	 * This method parses the file using the filename specified in the final variable fileName.
	 * @return		A Map containing the DirectedGraph ("graph"), the totalWins ("wins") for each team, 
	 * and the untouched totalScore ("score") for each team
	 */
	public static Map<String, Object> readFile()
	{
		Scanner s;
		DirectedGraph graph = new DirectedGraph(new RankingsMain());
		HashMap <String, Integer> totalScoreMap = new HashMap<String, Integer>();
		HashMap <String, Integer> winCount = new HashMap<String, Integer>();
		try 
		{
			s = new Scanner(new BufferedReader(new FileReader(FILE_NAME)));
			while(s.hasNext())
			{
				String line = s.nextLine();
				String linePairTwo = s.nextLine();
				int indexOfSpace = linePairTwo.indexOf(" ");
				int firstNumber = Integer.parseInt(linePairTwo.substring(0, indexOfSpace));
				int secondNumber = Integer.parseInt(linePairTwo.substring(indexOfSpace + 1));
				indexOfSpace = line.indexOf(" ");
				String teamOne = line.substring(0, indexOfSpace);
				String teamTwo = line.substring(indexOfSpace + DISTANCE_BETWEEN_TEAMS);
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
					//No connection is added for a tie.
				}			
				int currentPointCount = totalScoreMap.get(teamOne);
				totalScoreMap.put(teamOne, currentPointCount + firstNumber);
				currentPointCount = totalScoreMap.get(teamTwo);
				totalScoreMap.put(teamTwo, currentPointCount + secondNumber);
				
			}

			s.close();
			Map<String, Object> toReturn = new HashMap<String, Object>();
			toReturn.put("graph", graph);
			toReturn.put("wins", winCount);
			toReturn.put("score", totalScoreMap);
			return toReturn;
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("File not found");
			return null;
		} 

	}
}