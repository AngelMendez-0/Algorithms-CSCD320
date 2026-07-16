import java.util.*;

class BellmanFord implements Search {

    // graph to search
    private Digraph graph;

    // source and destination node names
    private String src;
    private String dest;

    // sentinel for unreachable nodes
    private static final double INF = Double.POSITIVE_INFINITY;

    // path array entry used to signal a detected negative cycle
    private static final String NEGATIVE_CYCLE_FLAG = "NEGATIVE_CYCLE_DETECTED";

    // constructs a bellmanFord instance for a single pair shortest path query
    public BellmanFord(Digraph graph, String src, String dest) {
        this.graph = graph;
        this.src = src;
        this.dest = dest;
    } // end of BellmanFord(Digraph, String, String)

    @Override
    public Path search() {
        ArrayList<String> nodes = graph.nodes();
        int n = nodes.size();

        // dist maps each node to its shortest known distance from src
        HashMap<String, Double> dist = new HashMap<>();

        // prev maps each node to the node before it on the shortest path
        HashMap<String, String> prev = new HashMap<>();

        // initialize all distances to infinity
        for (String node : nodes) {
            dist.put(node, INF);
            prev.put(node, null);
        } // end of for

        dist.put(src, 0.0);

        // relax all edges V-1 times to guarantee shortest paths
        for (int i = 0; i < n - 1; i++) {
            boolean updated = false;

            for (String u : nodes) {
                // skip nodes that are still unreachable
                if (dist.get(u) == INF) continue;

                for (String v : graph.edges(u)) {
                    double weight = graph.weight(u, v);
                    double newDist = dist.get(u) + weight;

                    if (newDist < dist.getOrDefault(v, INF)) {
                        dist.put(v, newDist);
                        prev.put(v, u);
                        updated = true;
                    } // end of if
                } // end of for
            } // end of for

            // early exit if no relaxation occurred in this iteration
            if (!updated) break;

        } // end of for

        // check for negative cycles with one additional relaxation pass
        for (String u : nodes) {
            if (dist.get(u) == INF) continue;

            for (String v : graph.edges(u)) {
                double weight = graph.weight(u, v);

                if (dist.get(u) + weight < dist.getOrDefault(v, INF)) {
                    // negative cycle detected — return flagged result
                    return new Path(src, dest, Double.NEGATIVE_INFINITY,
                            new String[]{NEGATIVE_CYCLE_FLAG});
                } // end of if
            } // end of for
        } // end of for

        // no path to dest
        if (dist.getOrDefault(dest, INF) == INF) {
            return new Path(src, dest, INF, new String[0]);
        } // end of if

        return new Path(src, dest, dist.get(dest), reconstructPath(prev, dest));
    } // end of search()

    // reconstructs a path from src to dest by walking the prev map
    private String[] reconstructPath(HashMap<String, String> prev, String dest) {
        LinkedList<String> pathList = new LinkedList<>();
        String current = dest;

        // walk backward from dest to src using prev pointers
        while (current != null) {
            pathList.addFirst(current);
            current = prev.get(current);
        } // end of while

        return pathList.toArray(new String[0]);
    } // end of reconstructPath(HashMap, String)

} // end of BellmanFord
