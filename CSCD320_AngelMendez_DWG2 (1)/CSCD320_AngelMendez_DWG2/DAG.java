import java.util.*;

/*
 * What was your process to create the DAG?
 * How does it differ from the original DWGraph?
 *
 * The DAG is a Facade layered on top of DWGraph. DWGraph already handles
 * all storage and density matrix/list switching. DAG adds two
 * new versions on top of that which use acyclicity on every insertion
 * and hiding edge weights
 *
 * Differences from DWGraph:
 *      - add(src, dest) takes no weight all edges are stored with a
 *       unit weight of 1.0 internally
 *      - weight() is removed from the API.
 *      - Every add(src, dest) call first checks if the edge would create
 *       a cycle if so it throws CycleException instead of adding the edge
 *       - A topoSort() method is provided which is only meaningful for DAGs
 *
 * How did you handle verifying that the graph is acyclic?
 * Defend your design choices.
 *
 * Before adding edge (src, dest) I run a DFS from dest to check whether
 * src is reachable from dest in the current graph. If it is adding
 * src to dest would complete a cycle.I think its correct because a new edge (u, v)
 * creates a cycle only if v can already reach u. Also because self loops are caught
 * before the DFS runs
 *
 * Why DFS over alternatives:
 *      - Maintaining a topological order is more complex to update
 *      - Union find detects undirected cycles, not directed ones
 */

// Facade for a Directed Acyclic Graph built on top of DWGraph
public class DAG {

    // internal unit weight — DAG edges are unweighted from the client's view
    private static final Double UNIT_WEIGHT = 1.0;

    // underlying directed weighted graph used for storage and traversal
    private DWGraph graph;

    // constructs an empty DAG.
    public DAG() {
        graph = new DWGraph();
    } // end of DAG()

    // adds an isolated node to the DAG
    public boolean add(String key) {
        return graph.add(key);
    } // end of add(String key)

    // adds a directed unweighted edge from src to dest
    public boolean add(String src, String dest) throws CycleException {
        // self-loop always creates a cycle
        if (src.equals(dest)) {
            throw new CycleException(src, dest);
        } // end of if

        // if dest can already reach src, adding src->dest would close a cycle
        if (isReachable(dest, src)) {
            throw new CycleException(src, dest);
        } // end of if

        return graph.add(src, dest, UNIT_WEIGHT);
    } // end of add(String src, String dest)

    // removes a node and all its edges
    public String delete(String key) {
        return graph.delete(key);
    } // end of delete(String key)

    // removes a directed edge from the src to dest
    public boolean delete(String src, String dest) {
        return graph.delete(src, dest) != null;
    } // end of delete(String src, String dest)

    // returns all node names in the DAG
    public ArrayList<String> nodes() {
        return graph.nodes();
    } // end of nodes()

    // returns all outbound edge dest from the given node
    public ArrayList<String> edges(String key) {
        return graph.edges(key);
    } // end of edges(String key)

    // returns the number of nodes in the DAG
    public int size() {
        return graph.size();
    } // end of size()

    // returns a readable string for the DAG
    @Override
    public String toString() {
        return "DAG:\n" + graph.toString();
    } // end of toString()

    // returns a JSON
    public String toJSON() {
        return graph.toJSON();
    } // end of toJSON()

    // returns the nodes of the DAG in order using Kahn's alogrithm
    public List<String> topoSort() {
        ArrayList<String> nodeList = graph.nodes();

        // count inbound edges for every node
        HashMap<String, Integer> inDegree = new HashMap<>();
        for (String node : nodeList) {
            inDegree.put(node, 0);
        } // end of for

        for (String node : nodeList) {
            for (String dest : graph.edges(node)) {
                inDegree.put(dest, inDegree.getOrDefault(dest, 0) + 1);
            } // end of for
        } // end of for

        // seed the queue with all nodes that have no inbound edges
        Queue<String> queue = new LinkedList<>();
        for (String node : nodeList) {
            if (inDegree.get(node) == 0) queue.offer(node);
        } // end of for

        List<String> result = new ArrayList<>();

        // repeatedly remove a zero in degree node and decrement its neighbors
        while (!queue.isEmpty()) {
            String node = queue.poll();
            result.add(node);

            for (String neighbor : graph.edges(node)) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);

                // neighbor now has no remaining inbound edges add to queue
                if (newDegree == 0) queue.offer(neighbor);
            } // end of for
        } // end of while

        return result;
    } // end of topoSort()

    // returns true if there is a directed path from A to B
    private boolean isReachable(String from, String to) {
        if (from.equals(to)) return true;

        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(from);

        while (!stack.isEmpty()) {
            String curr = stack.pop();

            if (curr.equals(to)) return true;
            if (visited.contains(curr)) continue;
            visited.add(curr);

            for (String neighbor : graph.edges(curr)) {
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                } // end of if
            } // end of for
        } // end of while

        return false;
    } // end of isReachable(String, String)

} // end of DAG
