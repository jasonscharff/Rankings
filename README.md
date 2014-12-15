#Rankings
###Summary
This program ranks a data set through a modified form of Topological Sort. It handles four cases or a combination of thereof. The first case it handles is the simplest: a directly ordered graph. For the directly ordered graph case the standard topological sort algorithim is used. The next case, a partially ordered set, is when there is a subjective ranking (not purely topological ordering). This often comes in the form of two or more starting and/or endpoints. To address this issue, the topological sort algorithim was modified so anything of equal ranking is placed together in the Stack. For example when it's at the end of a path the algorithim pushes anything that has the same number of dependicies as the visited node onto the stack. That way everything is ranked evenly. At the end using a tie breaking criteria a decision is made (detailed below) between evenly ranked cases. The next case, a disconnected subset is also solved through this system because it iterates through the entire graph and eveything of equal rank is compared together. The final case, a cyclic graph, is also supported, but not tested for the most extreme cases simply due to the sheer quantity of complexities. The way this roblem is solved is by finding all cycles using the JGraphT library's cycle Detector which finds cycles in a SimpleDirectedGraph (also part of JGraphT). Each cycle in the graph class written by me is then combined into one "super node" containing all vertices. This removes all cycles from the graph so it can be topologically sorted. After it is topologically sorted the super nodes are collapsed and using the tie breaking criteria sorted within the cycle. Two nodes are considered to be tied if they are topologically displayed next to each other and have the same number of wins.
###Classes and Interfaces
#####Client: RankingsMain
The client class has two main functionalities: reading the file (specifities described below) and handling the tie breaking criteria. In order to utilize the DirectedGraph class it must impelment the Ranking interface which specifies the tie breaking criteria. In this case the tie breaking criteria is the total number of points gained by each team. The rationale behind this is that if you lost with lots of points you still did pretty well against the other team and if you only won with a few points you probably didn't do that well even though you won–both teams are probably bad in that case. For example if team A won 5 times, but it only won 1 to 0 each time, but team B won 5 times also and won 100 to 99 each time then team B is better because it has the ability to score more points.
#####Impelmentation of a Directed Graph: DirectedGraph
The DirectedGraph class impelments a DirectedGraph using an adjacency matrix which is implemented via HashMaps of HashMaps. In the main HashMap the key is the team name which returns a HashMap of all its connections. In addition to just hosting a typical adjacency matrix this class also holds a duplicate of the adjacency matrix that is modified during the topological sort operation to remove cycles. At the end of the topological sort it is reset to be identical to the original so it can be top sorted again. Besides the two adjacency matricies, two JGraphT Simple Directed Graphs are stored (one to be modified for cycles) so the JGraphT CycleDetector can be used to find the vertices in a particular cycle. Finally, a reference to something implementing the Rankings interface (the client) is also held so the tie breaking functions can be passed in as a parameter and not be dependent on the DirectedGraph class. The details of the top sort algorithim are depicted in the section summary.
#####Rankings Interface
The Rankings interface specifies the existence of two methods, fixTies which returns an ArrayList and adjustForTotalWins which returns nothing. Adjust for total wins updates the total scores so that if A and B are deemed equal, but A beat B then A will always be ranked higher. fixTies provides the tie breaking criteria, in this case total scores.
#####TeamNode
The final class impelmented in this program is TeamNode. TeamNode encapsulates a team name and total score together and impelments comparable so the teams can be sorted using Colletions.sort to break ties.

###External Libraries
The only external libraries used are the JGraphT <a href="http://jgrapht.org/javadoc/org/jgrapht/graph/SimpleDirectedGraph.html">simple directed graph</a> and the JGraphT <a href="http://jgrapht.org/javadoc/org/jgrapht/alg/CycleDetector.html">cycle detector</a>. The JGraphT CycleDetector was written by John Sichi and published on September 16, 2004 and the DirectedGraph Interface and by assumption (no author is listed) the SimpleDirectedGraph (utilized in this program) was written by Bark Naveh and published on July 14, 2003.

###File Input
File input is handled in the client (RankingsMain) and must follow the following format. Each match takes up two lines in a .txt file. The first line is always in the form A v. B which states what teams played each other. For example, if the 49ers played the Cowboys then the line would be 49ers v. Cowboys. There must be exactly 4 characters between the team names (that is a final variable that can be changed if necessary). The second line after declaring what teams played each other is the scores. The scores are always written X Y where x is the score of the first team (in the example 49ers) and Y is the score of the second team (n the example Cowboys). So if the Cowboys beat the 49ers 14 to 7 the file would look as such:
<br/>
49ers v. Cowboys <br/>
7 14 <br/>
This system allows for an easily readable file by both the computer and the person. In the submitted program 4 example files are present. The file to be read can be changed by changing the final variable "FILE_NAME". 
Due to the flexibility of the DirectedGraph class the file input can easily be changed without affecting the remainder of the program.

#####Jason Scharff







