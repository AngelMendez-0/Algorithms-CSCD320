// custom exception for cycle detection in the DAG
public class CycleException extends Exception {

    // constructs a CycleException with a message identifying the offending edge
    public CycleException(String src, String dest) {
        super("Adding edge " + src + " -> " + dest
                + " would introduce a cycle into the DAG");
    } // end of CycleException(String, String)

} // end of CycleException
