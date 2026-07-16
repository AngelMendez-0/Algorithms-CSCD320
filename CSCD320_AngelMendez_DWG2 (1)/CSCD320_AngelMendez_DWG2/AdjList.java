import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Reflection: When should you use a list to represent a directed weighted graph?
 * Why? Does this reasoning hold true for other types of graphs?
 *
 * Use an adjacency list when the graph is SPARSE (when the edges E is much smaller than the maximum
 * In those cases the list O(v+e) space costs way outperforms a matrix's fixed polynomial allocation
 *
 * Time wise, listing all neighbors of a node takes O(degree) since you must visit every neighbor.
 * You have to check whether edge (u,v) exists. which is slow compared to a matrix's O(1) but in
 * sparse graphs, average degree is low so O(degree) is low, so O(degree) is acceptable
 *
 * The list also handles dynamic graphs well: adding a node is O(1) (just insert
 * a new key), and removing a node is O(V+E) (scan all adjacency lists). Both are
 * better than or equal to the O(V^2) rebuild cost of a matrix.
 *
 * Does this hold for other graph types?
 * Undirected graphs: same logic, each edge is stored twice (once per endpoint)
 * but space is still O(V+E), half the matrix's O(V^2) for sparse cases.
 *
 * Unweighted graphs: the inner HashMap reduces to a HashSet same tradeoffs.
 *
 * Multigraphs: the inner structure grows to support multiple edges per pair,
 * but sparse still favors a list over a matrix.
 *
 */

// Adjacency list of a DWG
// Uses a double nested linked hashmap
class AdjList implements Digraph {

    // outer key = source node name; inner key = destination name; value = edge weight
    private HashMap<String, HashMap<String, Double>> connections;

    // constructs empty listy
    public AdjList() {
        connections = new LinkedHashMap<>();
    } // end of AdjList

    @Override
    public boolean add(String key) {
        if (connections.containsKey(key)) return false;
        connections.put(key, new LinkedHashMap<>());

        return true;
    } // end of add

    @Override
    public boolean add(String src, String dest, Double weight) {
        // ensures both endpoints exist in the graph before adding the edge
        add(src);
        add(dest);

        // return false without changing the graph if the edge already exists
        if (connections.get(src).containsKey(dest)) return false;

        connections.get(src).put(dest, weight);
        return true;

    } // end of add(String src, String dest, Double weight)

    @Override
    public String delete(String key) {
        if (!connections.containsKey(key)) return null;

        // remove all inbound edges to 'key' from every other node's adjacent map
        for (HashMap<String, Double> adjacency : connections.values()) {
            adjacency.remove(key);
        } // end of for

        // remove the node itself and all of its correlated edges
        connections.remove(key);
        return key;
    } // end of delete(String key)

    @Override
    public Double delete(String src, String dest) {
        if (!connections.containsKey(src))
            return null;

        if (!connections.get(src).containsKey(dest))
            return null;

        return connections.get(src).remove(dest);
    } // end of delete(String src, String dest)

    @Override
    public ArrayList<String> nodes() {
        return new ArrayList<>(connections.keySet());
    } // end of nodes()

    @Override
    public ArrayList<String> edges(String key) {
        if (!connections.containsKey(key))
            return new ArrayList<>();

        return new ArrayList<>(connections.get(key).keySet());
    } // end of edges(String key)

    @Override
    public Double weight(String src, String dest) {
        if (!connections.containsKey(src))
            return null;

        return connections.get(src).get(dest); // null if dest not in inner map
    } // end of weight(String src, String dest)

    @Override
    public double density() {
        int n = connections.size();

        if (n <= 1)
            return 0.0;

        // count directed edges, excluding self-loops that don't count
        int edgeCount = 0;

        for (Map.Entry<String, HashMap<String, Double>> entry : connections.entrySet()) {
            for (String dest : entry.getValue().keySet()) {
                if (!dest.equals(entry.getKey())) edgeCount++;
            } // end of if
        } // end of for

        // cast n to long before multiplying to avoid int overflow for large graphs
        return (double) edgeCount / ((long) n * (n - 1));
    } // end of density()

    @Override
    public double density(String key) {
        int n = connections.size();

        if (n <= 1 || !connections.containsKey(key))
            return 0.0;

        // count outbound edges excluding the self-loop, if one exists
        int outboundCount = 0;

        for (String dest : connections.get(key).keySet()) {
            if (!dest.equals(key)) outboundCount++;
        } // end of for

        return (double) outboundCount / (n - 1);
    } // end of density(String key)

    @Override
    public int size() {
        return connections.size();
    } // end of size()

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Adjacency List:\n");

        for (Map.Entry<String, HashMap<String, Double>> entry : connections.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" ->");

            if (entry.getValue().isEmpty()) {
                sb.append(" (no edges)");
            } else {
                for (Map.Entry<String, Double> edge : entry.getValue().entrySet()) {
                    sb.append(" ").append(edge.getKey()).append("(").append(formatWeight(edge.getValue())).append(")");
                } // end of for
            } // end of else

            sb.append("\n");
        } // end of for

        return sb.toString();
    } // end of toString

    @Override
    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        ArrayList<String> nodeList = new ArrayList<>(connections.keySet());

        for (int i = 0; i < nodeList.size(); i++) {
            String node = nodeList.get(i);
            HashMap<String, Double> edgeMap = connections.get(node);
            ArrayList<String> destList = new ArrayList<>(edgeMap.keySet());

            sb.append("\t\"").append(node).append("\" : {\n");

            // write each edge, suppressing the trailing comma on the last entry
            for (int j = 0; j < destList.size(); j++) {
                String dest = destList.get(j);

                sb.append("\t\t\"").append(dest).append("\" : ").append(formatWeight(edgeMap.get(dest)));

                if (j < destList.size() - 1) sb.append(",");
                sb.append("\n");

            } // end of for

            sb.append("\t}");
            if (i < nodeList.size() - 1) sb.append(",");
            sb.append("\n");

        } // end of for

        sb.append("}");
        return sb.toString();
    } // end of toJSON

    // formats a do[uble value for JSON
    private String formatWeight(Double value) {
        // check if the value has no fractional component
        if (value == Math.floor(value) && !Double.isInfinite(value))
            return String.valueOf(value.longValue());

        return value.toString();
    } // end of formatWeight

} // end of AdjList