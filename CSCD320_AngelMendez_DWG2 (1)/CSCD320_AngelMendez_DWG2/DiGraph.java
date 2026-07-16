import java.util.ArrayList;

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
