package Routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import Map.Intersection;
import Map.Road;

import Simulation.EnvironmentSimulator;
import Simulation.SafetyChecker;

/**
 * Class implementing Djikstra's algorithm to compute the optimal route from the starting intersection
 * to the destination intersection, either with or without an environmental simulation.
 *
 * @author June Flores
 * @version 11/15/25
 */
class Router {
    /**
     * An epsilon, representing a small arbritrary value that is the upper bound of relative error in floating point
     * arithmetic. If the difference between 2 doubles is lower than this epsilon, it is considered functionally
     * equal.
     *
     */
    private static final double DOUBLE_EPSILON = 0.0005;

    /**
     * An empty constructor for the router class.
     *
     */
    public Router() { }

    /**
     * Computes a route without an environment simulation, always returns the fastest possible route, but may be
     * too high risk or even slower than the optimal route when applying conditions to it.
     *
     * @param theStart The first intersection of the route.
     * @param theEnd The final intersection of the route.
     * @return null if we cannot compute route, or the route object representing the fastest route from start to end.
     */
    public Route computeRoute(final Intersection theStart, final Intersection theEnd) {
        // initialize our necessary data structures
        PriorityQueue<ComparableIntersection> pq = new PriorityQueue<>(); // for adding new nodes
        HashMap<Intersection, ComparableIntersection> seenNode = new HashMap<>(); // also for adding new nodes
        HashSet<ComparableIntersection> closedNode = new HashSet<>();

        ComparableIntersection current = new ComparableIntersection(theStart, 0, null);
        pq.add(current);

        while (!pq.isEmpty()) {
            current = pq.poll();
            if (current.getIntersection().equals(theEnd)) { // terminating case if we pop off the target
                return iterateIntersectionPath(current);
            } // if we didn't land the target node, proceed as usual:

            // compute through the neighbors, calculating distance for unvisited neighbors
            for (Road r : current.getIntersection().getRoadList()) { // get the intersections this is connected to
                Intersection nonOriginNode = getNonOriginNode(r, current.getIntersection());


                double pathTotal = pathWeight(current, r.getDefaultTime()); //compute the weight of path

                if (seenNode.containsKey(nonOriginNode)) { // check if we've already set the weight of this node
                    if (!closedNode.contains(seenNode.get(nonOriginNode))) { // if it's an already closed node, ignore
                        setPathWeight(pathTotal, nonOriginNode, current, pq, seenNode);
                    } else {
                        continue; // continue to next iteration if we're computing an already visited node
                    }
                }
                // since this is a never-visited node, we'll add it to the queue along with it's data.
                putNode(pathTotal, nonOriginNode, current, pq, seenNode);
            }
            // mark current node as visited, then after reiteration we'll run through the next shortest node
            closedNode.add(current);
        }
        return null;
    }

    /**
     * Returns the fastest route possible with an environmental simulator, only traversing the roads and intersections
     * that are safe/below the safety risk threshold given. Routes are objects holding an ordered list of intersections.
     *
     * @param theStart The first intersection of the route.
     * @param theEnd The final intersection of the route.
     * @param theThreshold The maximum accepted safety risk of any road or intersection in the route.
     * @param theSim The environmental simulator containing the conditions of the route for these intersections.
     * @return null if no route is found below the threshold, or a route object from the start to the end.
     */
    public Route computeRoute(final Intersection theStart, final Intersection theEnd,
                              final double theThreshold, final EnvironmentSimulator theSim) {

        PriorityQueue<ComparableIntersection> pq = new PriorityQueue<>(); // for adding new nodes
        HashMap<Intersection, ComparableIntersection> seenNode = new HashMap<>(); // also for adding new nodes
        HashSet<ComparableIntersection> closedNode = new HashSet<>();

        if (compareDouble(SafetyChecker.safetyRisk(theStart, theSim), theThreshold) == 1) {
            return null;
        } else if (compareDouble(SafetyChecker.safetyRisk(theEnd, theSim), theThreshold) == 1) {
            return null;
        }
        ComparableIntersection current = new ComparableIntersection(theStart, 0, null);
        pq.add(current);

        while (!pq.isEmpty()) {
            current = pq.poll();
            if (compareDouble(SafetyChecker.safetyRisk(current.getIntersection(), theSim), theThreshold) == 1) {
                closedNode.add(current);
                continue;
            }
            if (current.getIntersection().equals(theEnd)) { // terminating case if we pop off the target
                return iterateIntersectionPath(current);
            } // if we didn't land the target node, proceed as usual

//             random print section for debugging
//            System.out.println("Current: " + current.getIntersection().getID());
//            System.out.print("[");
//            for (ComparableIntersection cn : pq) {
//                System.out.print(cn.getIntersection().getID() + " (" + cn.myPathWeight + "), ");
//            }
//            System.out.print("]\n");

            // compute through the neighbors, calculating distance for unvisited neighbors
            for (Road r : current.getIntersection().getRoadList()) { // get the intersections this is connected to
                Intersection nonOriginNode = getNonOriginNode(r, current.getIntersection());

                double pathTotal = pathWeight(current, r, theSim); //compute the weight of path

                if (compareDouble(SafetyChecker.safetyRisk(r, theSim), theThreshold) == 1) { // skip if road is over the safety threshold
                    continue;
                }

                if (seenNode.containsKey(nonOriginNode)) { // check if we've already set the weight of this node
                    if (!closedNode.contains(seenNode.get(nonOriginNode))) { // if it's an already closed node, ignore
                        setPathWeight(pathTotal, nonOriginNode, current, pq, seenNode);
                    }
                    continue;

                }
                // since this is a never-visited node, we'll add it to the queue along with it's data.
                putNode(pathTotal, nonOriginNode, current, pq, seenNode);
            }
            // mark current node as visited, then after reiteration we'll run through the next shortest node
            closedNode.add(current);
        }

        return null;
    }

    // returns 1 if num1 > num2, -1 if num1 < num2, and 0 if equal

    /**
     * Compares two doubles with an epsilon to approximate equality with slight differences. Returns 0 if the
     * two numbers are equal, 1 if the first number is greater than the second, and -1 if the first number is
     * less than the second.
     *
     * @param num1 The first number for comparison.
     * @param num2 The first number for comparison.
     * @return 0 if num1 ≈ num2, 1 if num1 > num2, -1 if num1 < num2.
     */
    private int compareDouble(final double num1, final double num2) {
        if (Math.abs(num1 - num2) < DOUBLE_EPSILON) {
            return 0;
        }
        if (num1 > num2) {
            return 1;
        }
        return -1;
    }

    /**
     * Gets the path weight (length of traveling to road in minutes) without considering environmental conditions.
     * Path weight is in the context of Dijkstra's algorithm, and it's the time to reach the node from the starting node.
     *
     * @param thePrevNode The node prior to the node that's having their weight evaluated.
     * @param theRoadTime The time to traverse the road between the previous node and the current node.
     * @return The time in minutes it takes to reach the current node from the starting node.
     */
    private double pathWeight(final ComparableIntersection thePrevNode, final double theRoadTime) {
        return thePrevNode.getPathWeight() + theRoadTime;
    }

    /**
     * Gets the path weight (minutes) in the context of Dijkstra's algorithm considering the environmental conditions.
     * Path weight is the distance/time to traverse to the current node from the start.
     *
     * @param thePrevNode The node prior to the node that's having their weight evaluated.
     * @param theRoad The weighted edge/road connecting the current node to the previous node.
     * @param theSim The environmental simulation that has the condition of the node.
     * @return The time in minutes it takes to reach the current node from the start with conditions applied.
     */
    private double pathWeight(final ComparableIntersection thePrevNode, final Road theRoad,
                              final EnvironmentSimulator theSim) {
        return thePrevNode.getPathWeight() + SafetyChecker.roadTime(theRoad, theSim);
    }

    /**
     * Unconditionally adds a node to the priority queue and list of visited nodes. Constructs a ComparableIntersection
     * object from the parameters given, and adds it to the passed queue and hashmap of intersections.
     *
     * @param theWeight The path weight of the node.
     * @param theNode The intersection that we create a ComparableIntersection with.
     * @param thePrevNode The node prior to the intersection we're making a node with. Used for when we need to
     *                    iterate through the path, which is just iterating through previous nodes until we reach null.
     * @param theQueue The priority queue that the created node will be placed into.
     * @param theIntersectionList The hashmap of intersections and corresponding nodes we'll place the node into.
     */
    private void putNode(final double theWeight, final Intersection theNode, final ComparableIntersection thePrevNode,
                         final PriorityQueue<ComparableIntersection> theQueue,
                         final HashMap<Intersection, ComparableIntersection> theIntersectionList) {
        ComparableIntersection newNode = new ComparableIntersection(theNode, theWeight, thePrevNode);
        theIntersectionList.put(theNode, newNode);
        theQueue.add(newNode);
    }

    /**
     * Returns the intersection opposite to the origin intersection. Assumes that the intersection is connected to this
     * road. Otherwise, we'll return the default intersection from calling Road.getDestination. This is needed because
     * we may be traversing to the road from the intersection at its "destination", and we'll just get the
     * intersection we already have by calling Road.getDestination.
     *
     * @param theRoad The weighted edge/road we want to see the other intersection of.
     * @param theOrigin The intersection we're traversing to the road from.
     * @return Either the destination of the road, or the source if we're traversing to the road from the destination.
     */
    private Intersection getNonOriginNode(final Road theRoad, final Intersection theOrigin) {
        if (!theRoad.getSource().equals(theOrigin)) {
            return theRoad.getSource();
        }
        return theRoad.getDestination();
    }

    /**
     * Compares an already traversed node to the calculated path weight of that node according to the current iteration.
     * If the path assigned in the current iteration is more optimal, we change the previous node of that node to the
     * more optimal node to be traversing from, then add it back into the queue to traverse to. Needed in
     * Dijkstra's algorithm because an already visited node may have a more optimal route to reach it. Maintaining
     * the goal of finding the shortest path from the source node to every other node.
     *
     * @param theWeight The path weight of the node in the current iteration. Checks if more optimal than previous weight.
     * @param theCurrent The already traversed intersection that we'll compare our current path to its old path.
     * @param thePrevNode The node that will become the node's new "prevNode" if it's a more optimal path.
     * @param theQueue The queue instance that we'll place the node into if our current path is more optimal.
     * @param theIntersectionList The map of already visited intersections that our node has already been placed in.
     */
    private void setPathWeight(final double theWeight, final Intersection theCurrent,
                               final ComparableIntersection thePrevNode,
                               final PriorityQueue<ComparableIntersection> theQueue,
                               final HashMap<Intersection, ComparableIntersection> theIntersectionList) {
        if (compareDouble(theWeight, theIntersectionList.get(theCurrent).myPathWeight) == -1) { // check if our path is more optimal
            theQueue.remove(theIntersectionList.get(theCurrent));
            theIntersectionList.get(theCurrent).setPathWeight(theWeight); // edit this node with new path
            theIntersectionList.get(theCurrent).setPrev(thePrevNode);
            theQueue.add(theIntersectionList.get(theCurrent));
        }
    }

    /**
     * Constructs a route from the node on a graph to the source intersection, which will have a prevNode value of null.
     * The route is made by placing the Intersection object wrapped in the ComparableIntersection instance into
     * a list in reverse order, then constructing the route.
     *
     * @param theIntersection The final node of the route, or the "destination".
     * @return the route object containing the fastest path of intersections found.
     */
    private Route iterateIntersectionPath(final ComparableIntersection theIntersection) {
        ArrayList<Intersection> path = new ArrayList<>();
        ComparableIntersection resultIterator = theIntersection;
        while (resultIterator.getPrev() != null) {
            path.addFirst(resultIterator.myIntersection);
            resultIterator = resultIterator.getPrev();
        }
        path.addFirst(resultIterator.myIntersection);
        return new Route(path.toArray(new Intersection[0]));
    }

    /**
     * Inner "node" class that represents an intersection, and the path to traverse to it in the context of Dijkstra's
     * algorithm. Implements comparable for the purposes of being added to a priority queue, where the fastest
     * path is prioritized to traverse to.
     */
    private final class ComparableIntersection implements Comparable<ComparableIntersection> {
        /**
         * The intersection this node is representing.
         */
        private final Intersection myIntersection;
        /**
         * The weight to this intersection from some source node.
         */
        private double myPathWeight;
        /**
         * The node that the most optimal path to this intersection is traversing from to reach this node.
         */
        private ComparableIntersection myPrevNode;

        /**
         * Constructs a ComparableIntersection with the intersection it represents, and the initial optimal path weight
         * and previous node, which can be edited if a more optimal path to it is found.
         *
         * @param theIntersection The intersection the node represents.
         * @param thePathWeight The weight of the node as the time to reach it from the source node in minutes.
         * @param thePrevNode The node that traverses to this node in the optimal path.
         */
        private ComparableIntersection(final Intersection theIntersection, final double thePathWeight,
                                      final ComparableIntersection thePrevNode) {
            this.myIntersection = theIntersection;
            this.myPathWeight = thePathWeight;
            this.myPrevNode = thePrevNode;
        }

        /**
         * Returns the intersection that this node represents.
         *
         * @return the intersection represented by the object.
         */
        private Intersection getIntersection() {
            return myIntersection;
        }

        /**
         * Returns the weight of the current optimal path to this node from the source node in minutes.
         *
         * @return the path weight of the node in minutes.
         */
        private double getPathWeight() {
            return myPathWeight;
        }

        /**
         * The node that traverses to this node in the optimal path.
         *
         * @return the previous node to this node in the optimal path.
         */
        private ComparableIntersection getPrev() {
            return myPrevNode;
        }

        /**
         * Sets the path weight in case a more optimal path to this node is found.
         *
         * @param theNewWeight the new path weight of this node.
         */
        private void setPathWeight(final double theNewWeight) {
            this.myPathWeight = theNewWeight;
        }

        /**
         * Sets the new previous node in case a more optimal path to this node is found.
         *
         * @param theNewNode
         */
        private void setPrev(final ComparableIntersection theNewNode) {
            this.myPrevNode = theNewNode;
        }

        /**
         * This class has a natural ordering inconsistent with "equals", it's ordered based on the intersection's weight.
         * @param theOther the ComparableIntersection object to be compared.
         * @return a negative number if theOther is faster, a positive if theOther is slower, and 0 if of equal weight.
         */
        @Override
        public int compareTo(final ComparableIntersection theOther) {
            return compareDouble(myPathWeight, theOther.getPathWeight());
        }
    }

}
