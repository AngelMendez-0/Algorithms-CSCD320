import java.util.*;

class FloydWarshall implements Search {

    // graph to search
    private Digraph graph;

    // large sentinel for "no direct edge / unreachable"
    private static final double INF = 1e18;

    // constructs a FloydWarshall for an all pairs shortest path query
    public FloydWarshall(Digraph graph) {
        this.graph = graph;
    } // end of FloydWarshall(Digraph)

    @Override
    public Path search() {
        ArrayList<String> nodes = graph.nodes();
        int n = nodes.size();

        // map each node name to a numeric index for the 2D arrays
        HashMap<String, Integer> index = new HashMap<>();
        for (int i = 0; i < n; i++) {
            index.put(nodes.get(i), i);
        } // end of for

        // dist[i][j] = shortest known distance from node i to node j
        double[][] dist = new double[n][n];

        // next[i][j] = first step on the shortest path from i to j (for reconstruction)
        String[][] next = new String[n][n];

        // initialize: all distances are INF except the diagonal (0) and direct edges
        for (double[] row : dist) Arrays.fill(row, INF);
        for (int i = 0; i < n; i++) dist[i][i] = 0;

        // fill in known direct edges
        for (String u : nodes) {
            int i = index.get(u);
            for (String v : graph.edges(u)) {
                int j = index.get(v);
                dist[i][j] = graph.weight(u, v);
                next[i][j] = v; // first step from u toward v is v itself (direct edge)
            } // end of for
        } // end of for

        // Floyd-Warshall: relax through every possible intermediate vertex k
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    // only relax if both legs of the path are reachable
                    if (dist[i][k] < INF && dist[k][j] < INF) {
                        double candidate = dist[i][k] + dist[k][j];
                        if (candidate < dist[i][j]) {
                            dist[i][j] = candidate;
                            next[i][j] = next[i][k]; // go through k first
                        } // end of if
                    } // end of if
                } // end of for
            } // end of for

        } // end of for

        // build the result: one "A->B=cost" string per finite reachable pair
        List<String> results = new ArrayList<>();
        double totalCost = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // skip self-pairs and unreachable pairs
                if (i != j && dist[i][j] < INF) {
                    results.add(nodes.get(i) + "->" + nodes.get(j) + "=" + dist[i][j]);
                    totalCost += dist[i][j];
                } // end of if
            } // end of for
        } // end of for

        return new Path("<ALL>", "<ALL>", totalCost, results.toArray(new String[0]));
    } // end of search()

} // end of FloydWarshall
