import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Reflection: When should you use a matrix to represent a directed weighted graph?
 * Why? Does this reasoning hold true for other types of graphs?
 *
 * Use an adjacency matrix when the graph is DENSE (when the number of edges E
 * approaches the maximum).
 *
 * O(1) edge lookup: weights[srcIdx][destIdx] is a constant-time array access
 * which matters when your algorithm constantly checks if edges exist
 *
 * Predictable space: O(V^2) is fixed regardless of edge count. For
 * systems that need deterministic memory usage this program makes it far easier
 * to know how much memory and or time will be needed
 *
 * The downside is that sparse graphs waste most of the V^2 slots with null entries
 * that consume memory but represent no real data. Node insertion/deletion is also
 * expensive: O(V^2) to allocate a new row/column or rebuild the matrix after removal.
 *
 * Does this hold for other graph types?
 *
 * Undirected: only the upper or lower triangle is needed — V*(V-1)/2 entries
 * instead of V*(V-1) but it is still O(V^2). Dense undirected graphs
 * still benefit from the O(1) lookup and cache locality.
 *
 * Unweighted: a boolean[][] is enough instead of Double[][] halving per-entry
 * storage, but the density argument is very similar.
 *
 * Frequently changed graphs: matrix rebuild on node deletion becomes a bottleneck,
 * making the list better even with high density when nodes change a lot.The dense
 * graph advantages are more universal while the mutation varies by graph type.
 */

// Adjacency matrix implementation of a DWG
// edge weights are stored in a 2D double array
class AdjMatrix implements Digraph {

    // starting number of rows/columns allocated in the weight matrix
    private static final int INITIAL_CAPACITY = 10;

    // multiplier applied to capacity during each resize operation
    private static final double GROWTH_FACTOR = 2.0;

    // 2D weight array with weights[i][j] that holds the weight of the directed edges
    private Double[][] weights;

    // maps each node name to its current index
    private LinkedHashMap<String, Integer> keyMap;

    // number of nodes in graph
    private int count;

    // current side lengths
    private int capacity;

    // constructs an empty matrix
    public AdjMatrix() {
        capacity = INITIAL_CAPACITY;
        weights = new Double[capacity][capacity];
        keyMap = new LinkedHashMap<>();
        count = 0;

    } // end of AdjMatrix

    @Override
    public boolean add(String key) {
        if (keyMap.containsKey(key))
            return false;

        // grow the backing array before we run out of index slots
        if (count >= capacity)
            resize();

        keyMap.put(key, count);
        count++;
        return true;
    } // end of add(String key)

    // doubles the matrix capacity by allocating new array
    private void resize() {
        int newCapacity = (int) (capacity * GROWTH_FACTOR);
        Double[][] newWeights = new Double[newCapacity][newCapacity];

        // copy every existing weight to the same row/column in the larger array
        for (int row = 0; row < capacity; row++) {
            for (int col = 0; col < capacity; col++) {
                newWeights[row][col] = weights[row][col];
            } // end of for
        } // end of for

        weights = newWeights;
        capacity = newCapacity;
    } // end of resize()

    @Override
    public boolean add(String src, String dest, Double weight) {
        // ensure both endpoints exist before accessing their indices
        add(src);
        add(dest);

        int srcIdx = keyMap.get(src);
        int destIdx = keyMap.get(dest);

        // return false without overwriting if the edge already exists
        if (weights[srcIdx][destIdx] != null) return false;

        weights[srcIdx][destIdx] = weight;
        return true;
    } // end of add(String src, String dest, Double weight)

    @Override
    public String delete(String key) {
        if (!keyMap.containsKey(key))
            return null;

        // build a new key-to-index map that excludes the deleted node
        LinkedHashMap<String, Integer> newKeyMap = new LinkedHashMap<>();
        int newIdx = 0;

        for (String k : keyMap.keySet())
            if (!k.equals(key)) newKeyMap.put(k, newIdx++);


        // copy weights for all surviving (src, dest) pairs into a fresh matrix
        Double[][] newWeights = new Double[capacity][capacity];

        for (String src : newKeyMap.keySet()) {
            int oldSrc = keyMap.get(src);
            int newSrc = newKeyMap.get(src);

            for (String dest : newKeyMap.keySet()) {
                int oldDest = keyMap.get(dest);
                int newDest = newKeyMap.get(dest);
                newWeights[newSrc][newDest] = weights[oldSrc][oldDest];
            } // end of for
        } // end of for

        keyMap = newKeyMap;
        weights = newWeights;
        count--;
        return key;
    } // end of delete(String key)

    @Override
    public Double delete(String src, String dest) {
        if (!keyMap.containsKey(src) || !keyMap.containsKey(dest))
            return null;

        int srcIdx = keyMap.get(src);
        int destIdx = keyMap.get(dest);

        if (weights[srcIdx][destIdx] == null)
            return null;

        Double removed = weights[srcIdx][destIdx];
        weights[srcIdx][destIdx] = null;
        return removed;
    } // end of delete(String src, String dest)

    @Override // updated: added missing @Override annotation
    public ArrayList<String> nodes() {
        return new ArrayList<>(keyMap.keySet());

    } // end of nodes()

    @Override
    public ArrayList<String> edges(String key) {
        ArrayList<String> result = new ArrayList<>();

        if (!keyMap.containsKey(key))
            return result;

        int srcIdx = keyMap.get(key);

        // each column with a non-null weight is an outbound edge destination
        for (Map.Entry<String, Integer> entry : keyMap.entrySet()) {
            if (weights[srcIdx][entry.getValue()] != null) {
                result.add(entry.getKey());
            } // end of if
        } // end of for

        return result;
    }  // end of edges(String key)

    @Override
    public Double weight(String src, String dest) {
        if (!keyMap.containsKey(src) || !keyMap.containsKey(dest))
            return null;

        // returns null automatically when no edge is present (null stored in array)
        return weights[keyMap.get(src)][keyMap.get(dest)];

    } // end of weight(String src, String dest)

    @Override
    public double density() {
        if (count <= 1)
            return 0.0;

        int edgeCount = 0;

        for (Map.Entry<String, Integer> srcEntry : keyMap.entrySet()) {
            int srcIdx = srcEntry.getValue();

            for (Map.Entry<String, Integer> destEntry : keyMap.entrySet()) {
                // skip the diagonal (self-loops)
                if (!srcEntry.getKey().equals(destEntry.getKey())
                        && weights[srcIdx][destEntry.getValue()] != null) {
                    edgeCount++;
                } // end of if
            } // end of for
        } // end of for

        return (double) edgeCount / ((long) count * (count - 1));
    } // end of density()

    @Override
    public double density(String key) {
        if (count <= 1 || !keyMap.containsKey(key)) return 0.0;

        int srcIdx = keyMap.get(key);
        int edgeCount = 0;

        for (Map.Entry<String, Integer> entry : keyMap.entrySet()) {
            // exclude the self-loop (diagonal) entry
            if (!entry.getKey().equals(key) && weights[srcIdx][entry.getValue()] != null) {
                edgeCount++;
            } // end of if
        } // end of for

        return (double) edgeCount / (count - 1);
    } // end of density(String)

    @Override
    public int size() {
        return count;
    } // end of size

    @Override
    public String toString() {
        // width allocated to each cell, including the row-header column
        final int COL_WIDTH = 12;
        StringBuilder sb = new StringBuilder();
        sb.append("Adjacency Matrix:\n");

        ArrayList<String> nodeList = new ArrayList<>(keyMap.keySet());

        // header row: blank label cell + one column label per node
        sb.append(String.format("%-" + COL_WIDTH + "s", ""));

        for (String node : nodeList)
            sb.append(String.format("%-" + COL_WIDTH + "s", truncate(node, COL_WIDTH - 1)));

        sb.append("\n");

        // data rows: row label + weight cells
        for (String src : nodeList) {
            sb.append(String.format("%-" + COL_WIDTH + "s", truncate(src, COL_WIDTH - 1)));
            int srcIdx = keyMap.get(src);

            for (String dest : nodeList) {
                Double w = weights[srcIdx][keyMap.get(dest)];
                String cell = (w == null) ? "-" : formatWeight(w);
                sb.append(String.format("%-" + COL_WIDTH + "s", cell));
            } // end of for

            sb.append("\n");
        } // end of for

        return sb.toString();
    } // end of toString

    @Override
    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        ArrayList<String> nodeList = new ArrayList<>(keyMap.keySet());

        for (int i = 0; i < nodeList.size(); i++) {
            String src = nodeList.get(i);
            int srcIdx = keyMap.get(src);

            sb.append("\t\"").append(src).append("\" : {\n");

            // collect all non-null outbound edges in keyMap iteration order
            ArrayList<String> destList = new ArrayList<>();
            for (String dest : nodeList) {
                if (weights[srcIdx][keyMap.get(dest)] != null) destList.add(dest);
            } // end of for

            // write each edge with a trailing comma on all but the last entry
            for (int j = 0; j < destList.size(); j++) {
                String dest = destList.get(j);
                sb.append("\t\t\"").append(dest).append("\" : ").append(formatWeight(weights[srcIdx][keyMap.get(dest)]));

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

    // truncates a string to the max length
    private String truncate(String s, int maxLen) {
        final int ELLIPSIS_LEN = 3;

        if (s.length() <= maxLen)
            return s;

        return s.substring(0, maxLen - ELLIPSIS_LEN) + "...";
    } // end of truncate(String s, int maxLen)

    // formats a double for JSON
    private String formatWeight(Double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf(value.longValue());
        } // end of if

        return value.toString();
    } // end of formatWeight

} // end of AdjMatrix