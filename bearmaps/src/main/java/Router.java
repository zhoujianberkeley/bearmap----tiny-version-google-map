import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a <code>shortestPath</code> method and <code>routeDirections</code> for
 * finding routes between two points on the map.
 */
public class Router {
    /**
     * Return a <code>List</code> of vertex IDs corresponding to the shortest path from a given
     * starting coordinate and destination coordinate.
     * @param g <code>GraphDB</code> data source.
     * @param stlon The longitude of the starting coordinate.
     * @param stlat The latitude of the starting coordinate.
     * @param destlon The longitude of the destination coordinate.
     * @param destlat The latitude of the destination coordinate.
     * @return The <code>List</code> of vertex IDs corresponding to the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g,
                                          double stlon, double stlat,
                                          double destlon, double destlat) {
        // the list to contain the vertices in the route
        ArrayList<Long> recordVertices = new ArrayList<>();
        // The priority queue to use
        PriorityQueue<Node> fringe = new PriorityQueue<>();
        // the mashMap to record the distances from starting point to this vertex
        HashMap<Long, Double> best = new HashMap<>();
        // the hashMap to record parent Nodes of vertices
        HashMap<Long, Node> parentNode = new HashMap<>();
        // a hashSet to mark the visited Nodes
        HashSet<Long> marked = new HashSet<>();

        // Get the starting Node and the end Node
        Node startingNode = g.vertexMap.get(g.closest(stlon, stlat));
        Node endingNode = g.vertexMap.get(g.closest(destlon, destlat));

        // add the starting Node to the fringe
        Node sourceNode = new Node(startingNode.nodeID, null,
                heuristic(g, startingNode.nodeID, endingNode.nodeID));
        parentNode.put(startingNode.nodeID, null);
        best.put(startingNode.nodeID, 0.0);
        fringe.add(sourceNode);

        while (!fringe.isEmpty()) {
            // dequeue the vertex with the closest distance
            Node pop = fringe.poll();
            // if this vertex is the destination, exit
            if (pop.nodeID == endingNode.nodeID) {
                break;
            }
            if (marked.contains(pop.nodeID)) {
                continue;
            }
            marked.add(pop.nodeID);
            Node tempNode = g.vertexMap.get(pop.nodeID);
            //System.out.println(tempNode);
            //System.out.println(tempNode.adjacent.size());
            for (long adjNodeId: tempNode.adjacent) {
                if (!marked.contains(adjNodeId)) {
                    // relax the edges
                    double dis = best.get(tempNode.nodeID) + g.distance(tempNode.nodeID, adjNodeId);
                    if (!best.containsKey(adjNodeId) || dis < best.get(adjNodeId)) {
                        // Change the best distance of this Node
                        best.put(adjNodeId, dis);
                        // Change the parent Node of this Node
                        parentNode.put(adjNodeId, pop);
                        // add the this Node to the fringe
                        Node toAdd = new Node(adjNodeId,
                                pop, dis + heuristic(g, adjNodeId, endingNode.nodeID));
                        fringe.add(toAdd);
                    }
                }
            }
        }
        while (endingNode != null) {
            recordVertices.add(0, endingNode.nodeID);
            endingNode = parentNode.get(endingNode.nodeID);
        }
        return recordVertices;
    }

    private static double heuristic(GraphDB g, long n, long goal) {
        return g.distance(n, goal);
    }

    /**
     * Given a <code>route</code> of vertex IDs, return a <code>List</code> of
     * <code>NavigationDirection</code> objects representing the travel directions in order.
     * @param g <code>GraphDB</code> data source.
     * @param route The shortest-path route of vertex IDs.
     * @return A new <code>List</code> of <code>NavigationDirection</code> objects.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        NavigationDirection n = new NavigationDirection();
        n.way = g.vertexMap.get(route.get(0)).nodeName;
        List<NavigationDirection> directions = new ArrayList<>();
        for (int i = 1; i < route.size(); i += 1) {
            long tempId = route.get(i);
            if (g.vertexMap.get(tempId).nodeName.equals(n.way)) {
                n.distance += g.distance(tempId, route.get(i - 1));
            } else {
                directions.add(n);
                n = new NavigationDirection();
                n.way = g.vertexMap.get(tempId).nodeName;
            }
        }
        System.out.println(directions);
        return directions;
    }

    private static String bearingToAction(double bearingInDegress) {
        if (bearingInDegress < -100.0) {
            return NavigationDirection.DIRECTIONS[NavigationDirection.SHARP_LEFT];
        } else if (bearingInDegress < -30.0) {
            return NavigationDirection.DIRECTIONS[NavigationDirection.LEFT];
        } else if (bearingInDegress < -15.0) {
            return NavigationDirection.DIRECTIONS[NavigationDirection.SLIGHT_LEFT];
        } else if (bearingInDegress <= 15.0) {
            return NavigationDirection.DIRECTIONS[NavigationDirection.STRAIGHT];
        } else if (bearingInDegress < 30.0) {
            return NavigationDirection.DIRECTIONS[NavigationDirection.SLIGHT_RIGHT];
        } else if (bearingInDegress < 100.0) {
            return NavigationDirection.DIRECTIONS[NavigationDirection.RIGHT];
        } else {
            return NavigationDirection.DIRECTIONS[NavigationDirection.SHARP_RIGHT];
        }
    }

    private static NavigationDirection create(String action, String way, double miles) {
        String direction = String.format("%s on %s and continue for %f miles.", action, way, miles);
        return Objects.requireNonNull(NavigationDirection.fromString(direction));
    }

    static int directionPointer(Double bearing) {
        if (-15.0 < bearing && bearing < 15.0) {
            return 1;
        } else if (-30.0 < bearing && bearing <= -15.0) {
            return 2;
        } else if (15.0 <= bearing && bearing < 30.0) {
            return 3;
        } else if (30.0 <= bearing && bearing < 100.0) {
            return 4;
        } else if (-100.0 < bearing && bearing <= -30.0) {
            return 5;
        } else if (bearing <= -100) {
            return 6;
        } else if (100.0 <= bearing) {
            return 7;
        }
        return 0;
    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0, STRAIGHT = 1, SLIGHT_LEFT = 2, SLIGHT_RIGHT = 3,
                RIGHT = 4, LEFT = 5, SHARP_LEFT = 6, SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        public static final String UNKNOWN_ROAD = "unknown road";

        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction represented.*/
        int direction;
        /** The name of this way. */
        String way;
        /** The distance along this way. */
        double distance;

        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Returns a new <code>NavigationDirection</code> from a string representation.
         * @param dirAsString <code>String</code> instructions for a navigation direction.
         * @return A new <code>NavigationDirection</code> based on the string, or <code>null</code>
         * if unable to parse.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // Not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
