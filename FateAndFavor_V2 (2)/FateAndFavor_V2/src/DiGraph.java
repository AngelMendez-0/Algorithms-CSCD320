import java.util.ArrayList;

/*
 * Reflection: When did you choose to swap between the matrix and list?
 * Explain and defend your threshold selection process.
 *
 * I swap to an AdjMatrix when graph density reaches 0.5 (50%), and swap back to
 * AdjList when density drops to 0.4 (40%). The reasoning has to do with the time
 * vs the space it takes to use each
 *
 * AdjMatrix: O(1) edge lookup, O(V^2) space regardless of edge count.
 * AdjList:   O(degree) edge lookup, O(V+E) which is more space efficient when E < V^2
 *
 * At 50% density lookup costs ~O(V/2). The matrix's O(1) advantage becomes significant enough
 * to justify the fixed V^2 space cost. Below 50%, the list wastes far less memory and degree-based
 * lookups are fast enough to prefer it
 */

// Interface defining the full API for a DWG
interface Digraph {

    // adds note to graph
    boolean add(String key);

    // adds a directed weighted edge from src to dest
    boolean add(String src, String dest, Double weight);

    // removes a node and all of its inbound and outbound edges
    String delete(String key);

    // removes a directed edge from src to dest
    Double delete(String src, String dest);

    // returns a list of all node names currently in the graph
    ArrayList<String> nodes();

    // returns a list of all outbound destination names from the source node
    ArrayList<String> edges(String key);

    // returns the weight of a specific edge
    Double weight(String src, String dest);

    // calculates the unweighted density of the entire graph
    double density();

    // calculates the unweighted outbound density of a specific node
    double density(String key);

    // returns the current number of nodes
    int size();

    // returns a readable string representation of the graph
    String toString();

    // creates a JSON format
    String toJSON();

} // end of Digraph
