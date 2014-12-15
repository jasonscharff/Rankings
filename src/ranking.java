import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ranking {
	public ArrayList<String> fixTies(List<String> presorted);
	public void adjustTotalsForWins(DirectedGraph graph);
}
