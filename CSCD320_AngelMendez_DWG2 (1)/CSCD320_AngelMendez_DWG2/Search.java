import java.util.List;

/*
 * How did you implement your negative edge weights?
 * What are the benefits and detriments of your design decision?
 *
 * Negative edge weights are stored as ordinary negative double values inside
 * the underlying DWGraph. the data structure does not treat them specially
 * When DWGraph.search() is called it scans all edges to check for any
 * negative weight and uses BellmanFord instead of Dijkstras.
 *
 * Benefits:
 *      - no special encoding, conversion, or flag needed
 *      - The underlying AdjList / AdjMatrix code requires zero changes
 *       - Negative weights can be added or removed at any time
 *
 * Detriments:
 *      - Nothing prevents the user from adding a negative weight to a graph
 *        that was previously Dijkstra safe, which could produce
 *        wrong results if search() is not re called
 *      - Negative cycles are not caught until search() runs
 */

// interface for shortest-path search algorithms
interface Search {

    record Path(String src, String dest, double cost, String[] path) {} // end of Path

    Path search();

} // end of Search
