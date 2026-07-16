import java.util.*;

class Dijkstras implements Search {

    // graph to search
    private Digraph graph;

    // source and destination node names
    private String src;
    private String dest;

    // sentinel value for "unreachable"
    private static final double INF = Double.POSITIVE_INFINITY;

    // constructs a Dijkestras instance for a single pair shortest path query
    public Dijkstras(Digraph graph, String src, String dest) {
        this.graph = graph;
        this.src = src;
        this.dest = dest;

    } // end of Dijkstras(Digraph, String, String)

    @Override
    public Path search() {
        ArrayList<String> nodes = graph.nodes();

        // dist maps each node to its shortest known distance from src
        HashMap<String, Double> dist = new HashMap<>();

        // prev maps each node to the node that precedes it on the shortest path
        HashMap<String, String> prev = new HashMap<>();

        // initialize all distances to infinity
        for (String node : nodes) {
            dist.put(node, INF);
            prev.put(node, null);

        } // end of for

        dist.put(src, 0.0);

        // min heap of (distance, nodeName) pairs ordered by distance
        PriorityQueue<AbstractMap.SimpleEntry<Double, String>> pq =
                new PriorityQueue<>(Comparator.comparingDouble(AbstractMap.SimpleEntry::getKey));
        pq.offer(new AbstractMap.SimpleEntry<>(0.0, src));

        // tracks nodes whose shortest path has been finalized
        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            AbstractMap.SimpleEntry<Double, String> entry = pq.poll();
            double d = entry.getKey();
            String curr = entry.getValue();

            // skip stale queue entries where a shorter path was already found
            if (d > dist.getOrDefault(curr, INF)) continue;
            if (visited.contains(curr)) continue;
            visited.add(curr);

            // stop early once the destination is settled
            if (curr.equals(dest)) break;

            // relax all outbound edges from curr
            for (String neighbor : graph.edges(curr)) {
                if (visited.contains(neighbor)) continue;

                double weight = graph.weight(curr, neighbor);
                double newDist = d + weight;

                if (newDist < dist.getOrDefault(neighbor, INF)) {
                    dist.put(neighbor, newDist);
                    prev.put(neighbor, curr);
                    pq.offer(new AbstractMap.SimpleEntry<>(newDist, neighbor));
                } // end of if
            } // end of for

        } // end of while

        // no path to dest found
        if (dist.getOrDefault(dest, INF) == INF) {
            return new Path(src, dest, INF, new String[0]);

        } // end of if

        return new Path(src, dest, dist.get(dest), reconstructPath(prev, dest));
    } // end of search()

    // reconstructs the path from src to dst
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

} // end of Dijkstras
