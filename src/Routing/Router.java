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
public class Router {
    private static final double DOUBLE_EPSILON = 0.0005;

    public Router() { }

    /**
     *
     * @param theStart
     * @param theEnd
     * @return null if we cannot compute route, route as a series of intersection IDs
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
    private int compareDouble(final double num1, final double num2) {
        if (Math.abs(num1 - num2) < DOUBLE_EPSILON) {
            return 0;
        }
        if (num1 > num2) {
            return 1;
        }
        return -1;
    }

    private double pathWeight(final ComparableIntersection thePrevNode, final double theRoadTime) {
        return thePrevNode.getPathWeight() + theRoadTime;
    }


    private double pathWeight(final ComparableIntersection thePrevNode, final Road theRoad,
                              final EnvironmentSimulator theSim) {
        return thePrevNode.getPathWeight() + SafetyChecker.roadTime(theRoad, theSim);
    }

    private void putNode(final double theWeight, final Intersection theNode, final ComparableIntersection thePrevNode,
                         final PriorityQueue<ComparableIntersection> theQueue,
                         final HashMap<Intersection, ComparableIntersection> theIntersectionList) {
        ComparableIntersection newNode = new ComparableIntersection(theNode, theWeight, thePrevNode);
        theIntersectionList.put(theNode, newNode);
        theQueue.add(newNode);
    }

    private Intersection getNonOriginNode(final Road theRoad, final Intersection theOrigin) {
        if (!theRoad.getSource().equals(theOrigin)) {
            return theRoad.getSource();
        }
        return theRoad.getDestination();
    }

    private void setPathWeight(final double theWeight, final Intersection theCurrent,
                               final ComparableIntersection thePrevNode,
                               final PriorityQueue<ComparableIntersection> theQueue,
                               final HashMap<Intersection, ComparableIntersection> theIntersectionList) {
        if (compareDouble(theWeight,  theIntersectionList.get(theCurrent).myPathWeight) == -1) { // check if our path is more optimal
            theQueue.remove(theIntersectionList.get(theCurrent));
            theIntersectionList.get(theCurrent).setPathWeight(theWeight); // edit this node with new path
            theIntersectionList.get(theCurrent).setPrev(thePrevNode);
            theQueue.add(theIntersectionList.get(theCurrent));
        }
    }

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
     * Specialized intersection to represent nodes so that we can store it in a queue for djikstras.
     */
    private final class ComparableIntersection implements Comparable<ComparableIntersection> {
        private final Intersection myIntersection;
        private double myPathWeight;
        private ComparableIntersection myPrevNode;

        private ComparableIntersection(final Intersection theIntersection, final double thePathWeight,
                                      final ComparableIntersection thePrevNode) {
            this.myIntersection = theIntersection;
            this.myPathWeight = thePathWeight;
            this.myPrevNode = thePrevNode;
        }
        private Intersection getIntersection() {
            return myIntersection;
        }
        private double getPathWeight() {
            return myPathWeight;
        }
        private ComparableIntersection getPrev() {
            return myPrevNode;
        }
        private void setPathWeight(final double theNewWeight) {
            this.myPathWeight = theNewWeight;
        }
        private void setPrev(final ComparableIntersection theNewNode) {
            this.myPrevNode = theNewNode;
        }

        /**
         * This class has a natural ordering inconsistent with "equals", it's ordered based on the intersection's weight.
         * @param theOther the object to be compared.
         * @return
         */
        @Override
        public int compareTo(final ComparableIntersection theOther) {
            return compareDouble(myPathWeight, theOther.getPathWeight());
        }
    }

}
