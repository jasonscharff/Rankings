import java.util.ArrayList;
import java.util.List;
/******************************************************************************
 * Ranking Interface
 * 
 * Any client that uses the DirectedGraph class must implement the Ranking
 * Interface. Because the tie breaking criteria varies on a case by case scenario
 * and should be able to changed without changing the DirectedGraph class
 * one of the parameters in the DirectedGraph class must be an Instance of
 * something implementing Ranking so it can call the particular adjustTotalForWins
 * and fixTies method during the topological sort process.
 ******************************************************************************/
public interface Ranking {
	public ArrayList<String> fixTies(List<String> presorted);
	public void adjustTotalsForWins(DirectedGraph graph);
}
