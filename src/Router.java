import java.util.*;

public class Router {
    CityMap myMap;
    private static final double CONDITION_SCALAR = 1.5;
    private static final double WEATHER_TIME_WEIGHT = 0.2;
    private static final double TRAFFIC_TIME_WEIGHT = 0.4;
    private static final double OBSTACLE_TIME_WEIGHT = 0.4;
    private static final double WEATHER_SAFETY_WEIGHT = 0.2;
    private static final double TRAFFIC_SAFETY_WEIGHT = 0.3;
    private static final double OBSTACLE_SAFETY_WEIGHT = 0.5;
    private static final double DOUBLE_EPSILON = 0.0005;



    public Router(CityMap theMap) {
        this.myMap = theMap;
    }

    public double routeLength(int[] theRoute) {
        double result = 0;
        for (int i = 1; i < theRoute.length; i++) {
            Road r = myMap.getRoad(myMap.getIntersection(theRoute[i-1]), myMap.getIntersection(theRoute[i]));
            double time = r.getDefaultTime();
            result += time;
        }
        return (double) Math.round(result*100)/100;
    }

    public String directions(int[] theRoute) {
        StringBuilder sb = new StringBuilder();
        Intersection from = myMap.getIntersection(theRoute[0]);
        Intersection to = myMap.getIntersection(theRoute[1]);
        Road r = myMap.getRoad(from, to);
        CardinalDirection currentDir = r.getDirection(from);
        double accumulator = r.getLength();
        sb.append("From Location ");
        sb.append(from.getID());
        for (int i = 2; i < theRoute.length; i++) {
            from = myMap.getIntersection(theRoute[i-1]);
            to = myMap.getIntersection(theRoute[i]);
            r = myMap.getRoad(from, to);
            CardinalDirection newDir = r.getDirection(from);
            assert currentDir != null;
            currentDir = CardinalDirection.turnDirection(currentDir, newDir);
            if (currentDir == CardinalDirection.FORWARD) {
                accumulator += r.getLength();
            } else {
                sb.append(", then turn ");
                sb.append(currentDir);
                sb.append(" after ");
                sb.append(accumulator);
                sb.append(" meters");
                accumulator = r.getLength();
            }
            currentDir = newDir;
        }
        sb.append(" onto location ");
        sb.append(to.getID());
        return sb.toString();
    }

    // maxRouteSafety
    public double routeSafety(int[] theRoute, EnvironmentSimulator theSim) {
        double maxRouteSafety = 0.0;
        Intersection from, to = myMap.getIntersection(theRoute[1]);
        for (int i = 1; i < theRoute.length; i++) {
            from = myMap.getIntersection(theRoute[i-1]);
            to = myMap.getIntersection(theRoute[i]);
            Road r = myMap.getRoad(from, to);
            maxRouteSafety = Math.max(maxRouteSafety, Math.max(safetyRisk(from, theSim), safetyRisk(r, theSim)));
        }
        maxRouteSafety = Math.max(maxRouteSafety, safetyRisk(to, theSim));
        return maxRouteSafety;
    }

    /**
     *
     * @param theStart
     * @param theEnd
     * @return null if we cannot compute route, route as a series of intersection IDs
     */
    public int[] computeRoute(Intersection theStart, Intersection theEnd) {

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

    public int[] computeRoute(Intersection theStart, Intersection theEnd,
                              double theThreshold, EnvironmentSimulator theSim) {

        PriorityQueue<ComparableIntersection> pq = new PriorityQueue<>(); // for adding new nodes
        HashMap<Intersection, ComparableIntersection> seenNode = new HashMap<>(); // also for adding new nodes
        HashSet<ComparableIntersection> closedNode = new HashSet<>();

        if (compareDouble(safetyRisk(theStart, theSim), theThreshold) == 1) {
            return null;
        } else if (compareDouble(safetyRisk(theEnd, theSim), theThreshold) == 1) {
            return null;
        }
        ComparableIntersection current = new ComparableIntersection(theStart, 0, null);
        pq.add(current);

        while (!pq.isEmpty()) {
            current = pq.poll();
            if (compareDouble(safetyRisk(current.getIntersection(), theSim), theThreshold) == 1) {
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

                if (compareDouble(safetyRisk(r, theSim), theThreshold) == 1) { // skip if road is over the safety threshold
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

    private double safetyRisk(Intersection theIntersection, EnvironmentSimulator theSim) {
        Conditions cond = theSim.getCondition(theIntersection);
        return cond.getObstacleSeverity() * OBSTACLE_SAFETY_WEIGHT + cond.getWeatherFactor() * WEATHER_SAFETY_WEIGHT
                + cond.getTrafficDensity() * TRAFFIC_SAFETY_WEIGHT;
    }

    private double safetyRisk(Road theRoad, EnvironmentSimulator theSim) {
        Conditions cond = theSim.getCondition(theRoad);
        return cond.getObstacleSeverity() * OBSTACLE_SAFETY_WEIGHT + cond.getWeatherFactor() * WEATHER_SAFETY_WEIGHT
                + cond.getTrafficDensity() * TRAFFIC_SAFETY_WEIGHT;
    }

    private double pathWeight(ComparableIntersection thePrevNode, double theRoadTime) {
        return thePrevNode.getPathWeight() + theRoadTime;
    }

    private double pathWeight(ComparableIntersection thePrevNode, Road theRoad, EnvironmentSimulator theSim) {
        Conditions roadCon = theSim.getCondition(theRoad);
        double trafficMultiplier = roadCon.getObstacleSeverity() * OBSTACLE_TIME_WEIGHT
                + roadCon.getTrafficDensity() * TRAFFIC_TIME_WEIGHT + roadCon.getWeatherFactor() * WEATHER_TIME_WEIGHT;
        double roadTime = theRoad.getDefaultTime() * Math.exp(CONDITION_SCALAR * trafficMultiplier);
        return thePrevNode.getPathWeight() + roadTime;
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

    private int[] iterateIntersectionPath(ComparableIntersection theIntersection) {
        ArrayList<Integer> result = new ArrayList<>();
        ComparableIntersection resultIterator = theIntersection;
        while (resultIterator.getPrev() != null) {
            result.addFirst(resultIterator.myIntersection.getID());
            resultIterator = resultIterator.getPrev();
        }
        result.addFirst(resultIterator.myIntersection.getID());
        int[] intArray = new int[result.size()];
        for (int i = 0; i < result.size(); i++) {
            intArray[i] = result.get(i);
        }
        return intArray;
    }

    /**
     * Intersection to represent nodes so that we can store it in a queue for djikstras
     */
    class ComparableIntersection implements Comparable<ComparableIntersection> {
        private Intersection myIntersection;
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
//        public void setIntersection(Intersection theIntersection) {this.myIntersection = theIntersection;}
        public void setPathWeight(double theNewWeight) {this.myPathWeight = theNewWeight;}
        public void setPrev(ComparableIntersection theNewNode) {this.myPrevNode = theNewNode;}

        @Override
        public int compareTo(ComparableIntersection o) {
            return Double.compare(myPathWeight, o.getPathWeight());
        }
    }

}
