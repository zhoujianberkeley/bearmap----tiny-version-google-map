import java.util.ArrayList;
public class Edge {
    String edgeName;
    long edgeID;
    boolean valid;
    String maxSpeed;
    double distance;
    // the node IDs in the edge
    ArrayList<Long> nodeIds;

    public Edge(long id) {
        this.edgeID = id;
        valid = false;
        nodeIds = new ArrayList<>();
    }

    ArrayList<Long> getNodeIds() {
        return nodeIds;
    }

    boolean isValid() {
        return valid;
    }

    void addNode(Long ref) {
        nodeIds.add(ref);
    }

    void setName(String name) {
        this.edgeName = name;
    }

    void setMaxSpeed(String maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
