import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// facade for a DWG that automatically switches between a AdjList and an AdjMatrix
public class DWGraph {

    // the active graph being used (list or matrix)
    private Digraph graph;

    // cached node count
    private int size;

    // density at which we switch to a matrix
    private double mtxThreshold;

    // density at which we switch to a list
    private double lstThreshold;

    // updated: current search algorithm, set by search() at runtime (Strategy pattern)
    private Search searcher;

    // default density for switching to the adjacency matrix
    private static final double DEFAULT_MTX_THRESHOLD = 0.5;

    // density for reverting to adjacency list
    private static final double DEFAULT_LST_THRESHOLD = 0.4;

    // string constant used to trigger Floyd-Warshall all-pairs search
    private static final String ALL_PAIRS = "<ALL>";

    // constructs an empty directed weighted graph
    public DWGraph() {
        graph = new AdjList();
        size = 0;
        mtxThreshold = DEFAULT_MTX_THRESHOLD;
        lstThreshold = DEFAULT_LST_THRESHOLD;
        searcher = null; // updated: searcher is not set until search() is called

    } // end of DWGraph

    // constructs a DWG and populates it from JSON
    public DWGraph(String filepath) {
        this();
        loadFromFile(filepath);

    } // end of DWGraph(String filepath)

    // adds node to the graph
    public boolean add(String key) {
        boolean result = graph.add(key);
        size = graph.size();
        convert();

        return result;
    } // end of add(String key)

    // adds a DW erdge
    public boolean add(String src, String dest, Double weight) {
        boolean result = graph.add(src, dest, weight);

        size = graph.size();
        convert();

        return result;
    } // end of add(String src, String dest, Double weight)

    // removes a node and all of its edges
    public String delete(String key) {
        String result = graph.delete(key);
        size = graph.size();
        convert();

        return result;

    } // end of delete(String key)

    // removes a directed edge
    public Double delete(String src, String dest) {
        Double result = graph.delete(src, dest);
        convert();

        return result;
    } // end of delete(String src, String dest)

    // returns a list of all node names
    public ArrayList<String> nodes() {
        return graph.nodes();
    } // end of nodes

    // returns all outbound dest names from a given src node
    public ArrayList<String> edges(String key) {
        return graph.edges(key);
    } // end of edges(String key)

    // returns the weight of a specific edge
    public Double weight(String src, String dest) {
        return graph.weight(src, dest);
    } // end of weight(String src, String dest)

    // returns the unweighted density of the graph
    public double density() {
        return graph.density();
    } // end of density()

    // returns the unweighted outbound density of a node
    public double density(String key) {
        return graph.density(key);
    } // end of density(String key)

    // returns the number of nodes in a graph
    public int size() {
        return graph.size();
    } // end of size

    // returns a readable representation of the graph
    @Override
    public String toString() {
        return graph.toString();
    } // end of toString

    // returns a JSON
    public String toJSON() {
        return graph.toJSON();
    } // end of toJSON

    // creates and returns a new DWG
    public static DWGraph load(String filepath) {
        return new DWGraph(filepath);
    } // end of load

    // find the shortest path between src and dest using the best algorithm
    public Search.Path search(String src, String dest) {
        // select the best algorithm based on query type and graph properties
        if (src.equals(ALL_PAIRS) && dest.equals(ALL_PAIRS)) {
            // all-pairs query: Floyd-Warshall is the only valid choice
            searcher = new FloydWarshall(graph);
        } else if (hasNegativeWeights()) {
            // negative weights present: Dijkstra's is invalid, use Bellman-Ford
            searcher = new BellmanFord(graph, src, dest);
        } else {
            // no negative weights: Dijkstra's is faster and correct
            searcher = new Dijkstras(graph, src, dest);
        } // end of if else

        return searcher.search();
    } // end of search(String src, String dest)

    // scans all edgtes in the graph to check if any are negative
    private boolean hasNegativeWeights() {
        for (String node : graph.nodes()) {
            for (String dest : graph.edges(node)) {
                if (graph.weight(node, dest) < 0) return true;

            } // end of for
        } // end of for

        return false;
    } // end of hasNegativeWeights()

    // checks the current graph density and switches implementations if needed
    private void convert() {
        double d = graph.density();

        if (graph instanceof AdjList && d >= mtxThreshold) {
            // graph has become dense enough to benefit from a matrix
            AdjMatrix matrix = new AdjMatrix();
            transferData(graph, matrix);
            graph = matrix;

        } else if (graph instanceof AdjMatrix && d <= lstThreshold) {
            // graph has become sparse enough that a list is more efficient
            AdjList list = new AdjList();
            transferData(graph, list);
            graph = list;

        } // end of if else
    } // end of convert

    // copies all nodes and all edges from one DIgraph to another
    private void transferData(Digraph from, Digraph to) {
        // add every node first
        for (String node : from.nodes())
            to.add(node);

        // then add every directed edge
        for (String src : from.nodes())
            for (String dest : from.edges(src))
                to.add(src, dest, from.weight(src, dest));

    } // end of transferData(Digraph from, Digraph to)

    // reads JSON
    private void loadFromFile(String filepath) {
        try {
            // Read entire file line by line into a single string
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            parseAndLoad(sb.toString());

        } catch (IOException e) {
            System.err.println("DWGraph: could not read file \"" + filepath + "\"");
        }
    } // end of loadFromFile(String filepath)

    // parser
    private void parseAndLoad(String content) {
        content = content.trim();
        if (content.isEmpty() || !content.startsWith("{")) return;

        // strip the outermost braces to work with the inner content
        content = content.substring(1, content.length() - 1).trim();

        int i = 0;

        while (i < content.length()) {
            // skip whitespace and commas
            i = skipNonToken(content, i);
            if (i >= content.length()) break;

            // read the outer key (node name)
            if (content.charAt(i) != '"') { i++; continue; }
            String[] outerResult = readQuotedString(content, i);
            String nodeName = outerResult[0];
            i = Integer.parseInt(outerResult[1]);

            // ensure this node exists in the graph
            add(nodeName);

            // skip whitespace and the colon separator
            i = skipNonToken(content, i);

            if (i < content.length() && content.charAt(i) == ':') i++;
            i = skipNonToken(content, i);

            if (i >= content.length() || content.charAt(i) != '{') continue;
            i++;

            // parse each edge entry inside the inner object
            while (i < content.length()) {
                i = skipNonToken(content, i);
                if (i >= content.length()) break;
                if (content.charAt(i) == '}') { i++; break; }

                if (content.charAt(i) != '"') { i++; continue; }

                // read the destination node name (inner key)
                String[] innerResult = readQuotedString(content, i);
                String destName = innerResult[0];
                i = Integer.parseInt(innerResult[1]);

                // skip whitespace and colon
                i = skipNonToken(content, i);
                if (i < content.length() && content.charAt(i) == ':') i++;
                i = skipNonToken(content, i);

                // read the numeric weight value
                int valueStart = i;

                while (i < content.length()
                        && !Character.isWhitespace(content.charAt(i))
                        && content.charAt(i) != ','
                        && content.charAt(i) != '}') {
                    i++;
                } // end of while

                String valueStr = content.substring(valueStart, i).trim();

                if (!valueStr.isEmpty()) {
                    try {
                        Double edgeWeight = Double.parseDouble(valueStr);
                        // add() is a no-op for duplicates
                        add(nodeName, destName, edgeWeight);
                    } catch (NumberFormatException ignored) {

                    } // end of try and catch

                } // end of if
            } // end of while

        } // end of while
    } // end of parseAndLoad(String content)

    // advances past any combos of whitespace or commas
    private int skipNonToken(String s, int pos) {
        while (pos < s.length()
                && (Character.isWhitespace(s.charAt(pos)) || s.charAt(pos) == ',')) {
            pos++;
        } // end of while

        return pos;
    } // end of skipNonToken(String s, int pos)

    // reads JSON
    private String[] readQuotedString(String s, int start) {
        int i = start + 1; // skip opening quote
        StringBuilder sb = new StringBuilder();

        while (i < s.length() && s.charAt(i) != '"') {
            if (s.charAt(i) == '\\' && i + 1 < s.length()) {
                i++;
            } // end of if

            sb.append(s.charAt(i));
            i++;
        } // end of while

        i++; // skip closing quote
        return new String[]{sb.toString(), String.valueOf(i)};
    } // end of readQuotedString(String s, int start)

} // end of DWGraph
