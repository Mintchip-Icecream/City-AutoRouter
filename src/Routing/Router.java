package Routing;

import java.util.*;
import Map.*;
import Simulation.*;

public class Router {
    CityMap myMap;
    private static final double DOUBLE_EPSILON = 0.0005;

    public Router(CityMap theMap) {
        this.myMap = theMap;
    }

    public double routeLength(Route theRoute) {
        double result = 0;
        int[] routePath = theRoute.getRouteIDs();
        for (int i = 1; i < routePath.length; i++) {
            Road r = CityMap.getRoad(myMap.getIntersection(routePath[i-1]), myMap.getIntersection(routePath[i]));
            if (r == null) {
                return 0;
            }
            double time = r.getDefaultTime();
            result += time;
        }
        return (double) Math.round(result*100)/100;
    }

    public double routeLength(Route theRoute, EnvironmentSimulator theSim) {
        double result = 0;
        int[] routePath = theRoute.getRouteIDs();
        for (int i = 1; i < routePath.length; i++) {
            Road r = CityMap.getRoad(myMap.getIntersection(routePath[i-1]), myMap.getIntersection(routePath[i]));
            double time = SafetyChecker.roadTime(r, theSim);
            result += time;
        }
        return (double) Math.round(result*100)/100;
    }

    /**
     *
     * @param theStart
     * @param theEnd
     * @return null if we cannot compute route, route as a series of intersection IDs
     */
    public Route computeRoute(Intersection theStart, Intersection theEnd) {

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

    public Route computeRoute(Intersection theStart, Intersection theEnd,
                              double theThreshold, EnvironmentSimulator theSim) {

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
            } // if we didn't land the target node, proceed as usual:

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

    // returns 1 if num1 > num2, -1 if num1 < num2, and 0 if equal
    private int compareDouble(double num1, double num2) {
        if (Math.abs(num1 - num2) < DOUBLE_EPSILON) {
            return 0;
        }
        if (num1 > num2) {
            return 1;
        }
        return -1;
    }

    private double pathWeight(ComparableIntersection thePrevNode, double theRoadTime) {
        return thePrevNode.getPathWeight() + theRoadTime;
    }


    private double pathWeight(ComparableIntersection thePrevNode, Road theRoad, EnvironmentSimulator theSim) {
        return thePrevNode.getPathWeight() + SafetyChecker.roadTime(theRoad, theSim);
    }

    private void putNode(double theWeight, Intersection theNode, ComparableIntersection thePrevNode,
                         PriorityQueue<ComparableIntersection> theQueue,
                         HashMap<Intersection, ComparableIntersection> theIntersectionList) {
        ComparableIntersection newNode = new ComparableIntersection(theNode, theWeight, thePrevNode);
        theIntersectionList.put(theNode, newNode);
        theQueue.add(newNode);
    }

    private Intersection getNonOriginNode(Road theRoad, Intersection theOrigin) {
        if (!theRoad.getSource().equals(theOrigin)) {
            return theRoad.getSource();
        }
        return theRoad.getDestination();
    }

    private void setPathWeight(double currentWeight, Intersection node, ComparableIntersection prevNode,
                               PriorityQueue<ComparableIntersection> theQueue,
                               HashMap<Intersection, ComparableIntersection> theIntersectionList) {
        if (compareDouble(currentWeight,  theIntersectionList.get(node).myPathWeight) == -1) { // check if our path is more optimal
            theIntersectionList.get(node).setPathWeight(currentWeight); // edit this node with new path
            theIntersectionList.get(node).setPrev(prevNode);
            theQueue.remove(theIntersectionList.get(node));
            theQueue.add(theIntersectionList.get(node));
        }
    }

    private Route iterateIntersectionPath(ComparableIntersection theIntersection) {
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
     * Specialized intersection to represent nodes so that we can store it in a queue for djikstras
     */
    private class ComparableIntersection implements Comparable<ComparableIntersection> {
        private final Intersection myIntersection;
        private double myPathWeight;
        private ComparableIntersection myPrevNode;

        public ComparableIntersection(Intersection theIntersection, double thePathWeight, ComparableIntersection thePrevNode) {
            this.myIntersection = theIntersection;
            this.myPathWeight = thePathWeight;
            this.myPrevNode = thePrevNode;
        }

        public Intersection getIntersection() { return myIntersection;}
        public double getPathWeight() {return myPathWeight;}
        public ComparableIntersection getPrev() {return myPrevNode;}
//        public void setIntersection(Map.Intersection theIntersection) {this.myIntersection = theIntersection;}
        public void setPathWeight(double theNewWeight) {this.myPathWeight = theNewWeight;}
        public void setPrev(ComparableIntersection theNewNode) {this.myPrevNode = theNewNode;}

        @Override
        public int compareTo(ComparableIntersection o) {
            return Double.compare(myPathWeight, o.getPathWeight());
        }
    }

}
