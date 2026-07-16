import java.util.ArrayList;

/*
 * Extra Credit Option 1: Node-Based Graph Implementation
 *
 * What problems or dilemmas did you encounter while creating this structure?
 * How did you solve them and why did you choose to do it that way?
 * Is there a situation where this implementation would be better than matrix or list?
 *
 * PROBLEMS ENCOUNTERED:
 *
 * 1. Inbound edge deletion when removing a node.
 *    A node's outbound edges are easy to remove — just discard the node's edge list.
 *    But inbound edges are invisible from the deleted node's perspective. In an AdjList
 *    you can just iterate over all inner maps and remove the key, but in this structure
 *    you have to walk every other node's outbound list and surgically remove any edge
 *    whose dest.key matches the deleted node. I solved this by writing a removeEdgesTo()
 *    helper that traverses each node's edge list and splices out matching edges using a
 *    prev/curr pointer pattern. The tricky part was making sure I didn't skip edges after
 *    a removal, which I handled by only advancing prev when no deletion occurred.
 *
 * 2. O(V) node lookup on every operation.
 *    Without a HashMap, every call to find a node requires a linear scan of the linked
 *    node list via findNode(). This raises the complexity of most operations from O(1)
 *    to O(V). I accepted this tradeoff because adding a HashMap for lookup would violate
 *    the "nodes and edges only" structure constraint, and any other approach I could think
 *    of would have been worse. For graphs of reasonable size this is acceptable, and the
 *    simplicity of the linear scan is easy to reason about and debug compared to
 *    maintaining a parallel structure.
 *
 * 3. Maintaining an accurate edge count without an aggregate structure.
 *    I track edgeCount as a field, incrementing on every successful add() and decrementing
 *    on every successful delete(). This keeps density() at O(1) rather than requiring a
 *    full scan each time. The hardest part was keeping edgeCount correct during node
 *    deletion — I had to subtract two separate counts: the deleted node's own outbound
 *    edges, AND the inbound edges removed from every other node's list. Missing either
 *    one would silently corrupt the count and produce wrong density values. I solved it
 *    by handling both subtractions explicitly in delete(String key) using the return value
 *    of removeEdgesTo() to count inbound removals accurately.
 *
 * WHEN IS NODE-BASED BETTER?
 *    A node-based graph is better when each node needs to carry rich per-node data
 *    directly on the object — things like position, health, or dialogue state in a game.
 *    Rather than maintaining a separate map from node key to node data, the data lives
 *    on the node object itself, which improves locality and makes object-oriented design
 *    more natural. It also avoids key-copying overhead since edges hold direct object
 *    references to their destination nodes rather than storing and looking up string keys.
 *    For purely algorithmic graph work where performance matters most, AdjList or
 *    AdjMatrix will almost always be the better choice.
 */

// Node based DWG
class NodeGraph implements Digraph {

    // represents a vertex in the graph
    private static class Node {

        // unique name
        String key;

        // pointer to next
        Node next;

        // head of list
        Edge outbound;

        // creates a node with a given key and no edges
        Node(String key) {
            this.key = key;
            this.next = null;
            this.outbound = null;

        } // end of Node(String key)

    } // end of Node

    // represents a DW edge
    private static class Edge {

        // reference to the dest node
        Node dest;

        // weight of this edge
        Double weight;

        // pointer to the next outbound edge
        Edge next;

        // creates a new edge
        Edge(Node dest, Double weight) {
            this.dest = dest;
            this.weight = weight;
            this.next = null;

        } // end of Edge(Node dest, Double weight)

    } // end of Edge

    // head of the singly linked list
    private Node head;

    // total number of nodes
    private int nodeCount;

    // total number of edges
    private int edgeCount;

    /**
     * Constructs an empty node-based graph.
     */
    public NodeGraph() {
        head = null;
        nodeCount = 0;
        edgeCount = 0;

    } // end of NodeGraph

    // performs a linear scan of the node list to find the node with a given key
    private Node findNode(String key) {
        Node curr = head;

        while (curr != null) {
            if (curr.key.equals(key))
                return curr;

            curr = curr.next;
        } // end of while

        return null;
    } // end of findNode(String key)

    // creates a new ndoe and appends it to the end
    private Node appendNode(String key) {
        Node newNode = new Node(key);

        if (head == null) {
            head = newNode;
        } else {
            // walk to the tail to keep insertion order
            Node curr = head;

            while (curr.next != null) curr = curr.next;
            curr.next = newNode;
        } // end of if else

        nodeCount++;
        return newNode;
    } // end of appendNode(String key)

    // counts the number of edges from a given node
    private int countOutbound(Node node) {
        int count = 0;
        Edge curr = node.outbound;

        while (curr != null) {
            count++;
            curr = curr.next;
        } // end of while

        return count;
    } // end of countOutbound(Node node)

    // removes every edge from the source node
    private int removeEdgesTo(Node source, String destKey) {
        int removed = 0;
        Edge curr = source.outbound;
        Edge prev = null;

        while (curr != null) {
            if (curr.dest.key.equals(destKey)) {
                // splice this edge out of the linked list
                if (prev == null) {
                    source.outbound = curr.next;
                } else {
                    prev.next = curr.next;
                } // end of if else

                removed++;
                // move curr without moving prev
                curr = (prev == null) ? source.outbound : prev.next;
            } else {
                prev = curr;
                curr = curr.next;
            } // end of if else
        } // end of while

        return removed;
    } // end of removeEdgesTo(Node source, String destKey)

    // formats a double for output
    private String formatWeight(Double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value))
            return String.valueOf(value.longValue());

        return value.toString();
    } // end of formatWeight(Double value)

    @Override
    public boolean add(String key) {
        if (findNode(key) != null)
            return false;

        appendNode(key);
        return true;
    } // end of add(String key)

    @Override
    public boolean add(String src, String dest, Double weight) {
        // create nodes if they do not yet exist
        if (findNode(src) == null) appendNode(src);
        if (findNode(dest) == null) appendNode(dest);

        Node srcNode = findNode(src);
        Node destNode = findNode(dest);

        // scan the list to detect duplicate edges
        Edge curr = srcNode.outbound;

        while (curr != null) {
            if (curr.dest.key.equals(dest))
                return false; // edge already exists

            curr = curr.next;
        } // end of while

        Edge newEdge = new Edge(destNode, weight);
        newEdge.next = srcNode.outbound;
        srcNode.outbound = newEdge;
        edgeCount++;

        return true;
    } // end of add(String src, String dest, Double weight)

    @Override
    public String delete(String key) {
        if (findNode(key) == null) return null;

        // remove all inbound edges from every other node pointing to 'key'
        Node curr = head;

        while (curr != null) {
            if (!curr.key.equals(key))
                edgeCount -= removeEdgesTo(curr, key);

            curr = curr.next;
        } // end of while

        // subtract this node's own outbound edges from the global count
        Node toDelete = findNode(key);
        edgeCount -= countOutbound(toDelete);

        // remove the node itself from the global linked list
        if (head.key.equals(key)) {
            head = head.next;
        } else {
            Node prev = head;
            while (prev.next != null && !prev.next.key.equals(key)) {
                prev = prev.next;
            } // end of while

            if (prev.next != null)
                prev.next = prev.next.next;
        } // end of if else

        nodeCount--;
        return key;
    } // end of delete(String key)

    @Override
    public Double delete(String src, String dest) {
        Node srcNode = findNode(src);
        if (srcNode == null) return null;

        Edge curr = srcNode.outbound;
        Edge prev = null;

        while (curr != null) {
            if (curr.dest.key.equals(dest)) {
                // splice the edge out of the outbound list
                if (prev == null) {
                    srcNode.outbound = curr.next;
                } else {
                    prev.next = curr.next;
                } // end of if else

                edgeCount--;
                return curr.weight;
            } // end of if

            prev = curr;
            curr = curr.next;
        } // end of while

        return null; // edge not found
    } // end of delete(String src, String dest)

    @Override
    public ArrayList<String> nodes() {
        ArrayList<String> result = new ArrayList<>();
        Node curr = head;

        while (curr != null) {
            result.add(curr.key);
            curr = curr.next;
        } // end of while

        return result;
    } // end of nodes()

    @Override
    public ArrayList<String> edges(String key) {
        ArrayList<String> result = new ArrayList<>();
        Node node = findNode(key);

        if (node == null)
            return result;

        Edge curr = node.outbound;

        while (curr != null) {
            result.add(curr.dest.key);
            curr = curr.next;
        } // end of while

        return result;
    } // end of edges(String key)

    @Override
    public Double weight(String src, String dest) {
        Node srcNode = findNode(src);

        if (srcNode == null)
            return null;

        Edge curr = srcNode.outbound;

        while (curr != null) {
            if (curr.dest.key.equals(dest))
                return curr.weight;

            curr = curr.next;
        } // end of while
        return null;
    } // end of weight(String src, String dest)

    @Override
    public double density() {
        if (nodeCount <= 1)
            return 0.0;

        return (double) edgeCount / ((long) nodeCount * (nodeCount - 1));
    } // end of density()

    @Override
    public double density(String key) {
        if (nodeCount <= 1)
            return 0.0;

        Node node = findNode(key);

        if (node == null)
            return 0.0;

        int outbound = 0;
        Edge curr = node.outbound;

        while (curr != null) {
            // exclude self-loops from the density calculation
            if (!curr.dest.key.equals(key)) outbound++;
            curr = curr.next;
        } // end of while

        return (double) outbound / (nodeCount - 1);
    } // end of density(String key)

    @Override
    public int size() {
        return nodeCount;
    } // end of size

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node Graph:\n");

        Node curr = head;

        while (curr != null) {
            sb.append("  ").append(curr.key).append(" ->");
            if (curr.outbound == null) {
                sb.append(" (no edges)");
            } else {
                Edge edge = curr.outbound;

                while (edge != null) {
                    sb.append(" ").append(edge.dest.key).append("(").append(formatWeight(edge.weight)).append(")");
                    edge = edge.next;
                } // end of while
            } // end of if else

            sb.append("\n");
            curr = curr.next;
        } // end of while

        return sb.toString();
    } // end of toString()

    @Override
    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // walk the node list
        Node nodePtr = head;
        int nodeIdx = 0;

        while (nodePtr != null) {
            sb.append("\t\"").append(nodePtr.key).append("\" : {\n");

            // count outbound edges so we know when to suppress the trailing comma
            int total = countOutbound(nodePtr);
            int edgeIdx = 0;
            Edge edgePtr = nodePtr.outbound;

            while (edgePtr != null) {
                sb.append("\t\t\"").append(edgePtr.dest.key).append("\" : ").append(formatWeight(edgePtr.weight));

                if (edgeIdx < total - 1)
                    sb.append(",");

                sb.append("\n");
                edgePtr = edgePtr.next;
                edgeIdx++;
            } // end of while

            sb.append("\t}");

            if (nodePtr.next != null)
                sb.append(",");

            sb.append("\n");
            nodePtr = nodePtr.next;
            nodeIdx++;
        } // end of while

        sb.append("}");
        return sb.toString();
    } // end of toJSON

} // end of NodeGraph