import java.util.ArrayList;
public class Node implements Comparable<Node> {
    long nodeID;
    double nodeLat;
    double nodeLon;
    String nodeName;
    // the adjacent vertices of this vertex
    ArrayList<Long> adjacent;

    Node prev;
    double priority;

    //nodes for building the graph
    public Node(long id, double lat, double lon) {
        this.nodeID = id;
        this.nodeLat = lat;
        this.nodeLon = lon;
        adjacent = new ArrayList<>();
    }

    // Create the nodes this way to run the shortest path
    public Node(long id, Node prev, double priority) {
        this.nodeID = id;
        this.prev = prev;
        this.priority = priority;
    }

    long getId() {
        return nodeID;
    }

    double getLat() {
        return nodeLat;
    }

    double getLon() {
        return nodeLon;
    }

    void setName(String name) {
        this.nodeName = name;
    }

    void addAdj(Long a) {
        adjacent.add(a);
    }

    //compare nodes in PQ for running shortest paths
    public int compareTo(Node o) {
        double diff = this.priority - o.priority;
        if (diff < 0) {
            return -1;
        } else if (diff == 0) {
            return 0;
        } else {
            return 1;
        }
    }
}
