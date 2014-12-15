/******************************************************************************
 * TeamNode class
 * 
 * The following class encapsulates a team name and score together so the teams
 * can be sorted by total score in the tie breaking method. So it can be sorted
 * it implements Comparable.
 ******************************************************************************/
public class TeamNode implements Comparable
{
	String teamName;
	int totalScore;
	
	/**
	 * Constructor method initializes teamName and totalScore with given parameters.
	 * @param name	The name of the team
	 * @param totalScore	The total number of points the team has earned
	 */
	public TeamNode(String name, int totalScore)
	{
		this.teamName = name;
		this.totalScore = totalScore;
	}
	
	/**
	 * Getter method that returns the name of the team
	 * @return	The team name
	 */
	public String getTeamName()
	{
		return teamName;
	}
	
	/**
	 * Getter method that returns the total number of points that team has earned
	 * @return	The total number of points earned by the team.
	 */
	public int getTotalScore()
	{
		return totalScore;
	}
	
	/**
	 * Standard compareTo method required by the Comparable Interface. It simply compares the scores
	 * of the two teams.
	 */
	public int compareTo(Object o) 
	{
		return this.totalScore - ((TeamNode) o).getTotalScore();
	}
}