import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class KDTree {
    private KDNode root = null;
    ArrayList<KDNode> constructList = new ArrayList<>();
    public boolean isEmpty() {
        return root == null;
    }

    class KDNode {
        KDNode left;
        KDNode right;
        double xAxis;
        double yAxis;
        long id;

        KDNode(double x, double y, long givenID) {
            xAxis = x;
            yAxis = y;
            id = givenID;
        }

        public double getXAxis() {
            return xAxis;
        }

        public double getYAxis() {
            return yAxis;
        }
    }

    public void getConstructList(ArrayList<Node> givenList) {
        for (Node n: givenList) {
            double x = GraphDB.projectToX(n.nodeLon, n.nodeLat);
            double y = GraphDB.projectToY(n.nodeLon, n.nodeLat);
            long xID = n.nodeID;
            constructList.add(new KDNode(x, y, xID));
        }

    }

    public void constructTree(ArrayList<Node> givenList) {
        getConstructList(givenList);
        root = constructHelper(constructList, 0);

    }

    public KDNode constructHelper(List<KDNode> listOfKDNodes, int depth) {
        if (depth % 2 == 0) {
            //listOfKDNodes.sort(Comparator.comparing(KDNode::getXAxis));
            Collections.sort(listOfKDNodes, new Comparator<KDNode>() {
                @Override
                public int compare(KDNode o1, KDNode o2) {
                    double result =  o1.xAxis - o2.xAxis;
                    if (result < 0) {
                        return -1;
                    } else if (result > 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        } else {
            //listOfKDNodes.sort(Comparator.comparing(KDNode::getYAxis));
            Collections.sort(listOfKDNodes, new Comparator<KDNode>() {
                @Override
                public int compare(KDNode o1, KDNode o2) {
                    double result =  o1.yAxis - o2.yAxis;
                    if (result < 0) {
                        return -1;
                    } else if (result > 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        }
        if (listOfKDNodes.size() > 1) {
            int middle = listOfKDNodes.size() / 2;
            KDNode rootX = listOfKDNodes.get(middle);
            List<KDNode> leftList = new ArrayList<>();
            for (int i = 0; i < middle; i++) {
                leftList.add(listOfKDNodes.get(i));
            }
            List<KDNode> rightList = new ArrayList<>();
            for (int i = middle + 1; i < listOfKDNodes.size(); i++) {
                rightList.add(listOfKDNodes.get(i));
            }

            if (leftList.size() > 0) {
                rootX.left = constructHelper(leftList, depth + 1);
            }
            if (rightList.size() > 0) {
                rootX.right = constructHelper(rightList, depth + 1);
            }
            return rootX;
        } else if (listOfKDNodes.size() == 1) {
            return listOfKDNodes.get(0);
        } else {
            return null;
        }
    }


    public long nearest(double xCor, double yCor) {
        if (root == null) {
            return 0;
        }
        KDNode temp = root;
        double bestDis = Double.MAX_VALUE;
        long bestID = root.id;
        Champion champion = new Champion(bestDis, bestID);
        return nearestHelper(xCor, yCor, temp, champion, 1).bestID;
    }

    public Champion nearestHelper(double targetX, double targetY,
                                  KDNode node, Champion champion, int level) {
        if (node == null) {
            return champion;
        }
        double dist = euclidean(targetX, targetY, node.xAxis, node.yAxis);
        int newLevel = level + 1;
        if (dist < champion.bestDistant) {
            champion.bestID = node.id;
            champion.bestDistant = dist;
        }

        boolean goLeftOrBottom = false;
        if (level % 2 == 0) {
            if (targetY < node.yAxis) {
                goLeftOrBottom = true;
            }
        } else {
            if (targetX < node.xAxis) {
                goLeftOrBottom = true;
            }
        }
        if (goLeftOrBottom) {
            nearestHelper(targetX, targetY, node.left, champion, newLevel);
            double orientationX;
            double orientationY;
            if (level % 2 == 0) {
                orientationX = targetX;
                orientationY = node.yAxis;
            } else {
                orientationX = node.xAxis;
                orientationY = targetY;
            }
            double orientationDis = euclidean(orientationX, orientationY, targetX, targetY);
            if (orientationDis < champion.bestDistant) {
                nearestHelper(targetX, targetY, node.right, champion, newLevel);
            }

            //nearestHelper(targetX, targetY, node.right, newLevel);
        } else {
            nearestHelper(targetX, targetY, node.right, champion, newLevel);
            double orientationX;
            double orientationY;
            if (level % 2 == 0) {
                orientationX = targetX;
                orientationY = node.yAxis;
            } else {
                orientationX = node.xAxis;
                orientationY = targetY;
            }
            double orientationDis = euclidean(orientationX, orientationY, targetX, targetY);
            if (orientationDis < champion.bestDistant) {
                nearestHelper(targetX, targetY, node.left, champion, newLevel);
            }
            //nearestHelper(targetX, targetY, node.left, newLevel);
        }
        return champion;
    }

    public class Champion {
        double bestDistant;
        long bestID;

        public Champion(double dis, long id) {
            bestDistant = dis;
            bestID = id;
        }
    }

    public static double euclidean(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
