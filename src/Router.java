import java.util.*;

public class Router {
    CityMap myMap;

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

        for (Road r : current.getIntersection().getRoadList()) { // initialize the priority queue by
            Intersection nonOriginNode;
            if (!r.getSource().equals(current.getIntersection())) { // get whichever intersection in the road isn't our current
                nonOriginNode = r.getSource();
            } else {
                nonOriginNode = r.getDestination();
            }
            ComparableIntersection newNode = new ComparableIntersection(nonOriginNode, current.getPathWeight() + r.getDefaultTime(), current);
            pq.add(newNode);
            seenNode.put(current.getIntersection(), current);
        }
        pq.remove(current);

        closedNode.add(current);

        while (!pq.isEmpty()) {
            current = pq.poll();
            if (current.getIntersection().equals(theEnd)) { // terminating case if we pop off the target
                ArrayList<Integer> result = new ArrayList<>();
                ComparableIntersection resultIterator = current;
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

            // if we didn't land the target node, proceed as usual:

            // compute through the neighbors, calculating distance for unvisited neighbors
            for (Road r : current.getIntersection().getRoadList()) { // get the intersections this is connected to
                Intersection nonOriginNode;

                if (!r.getSource().equals(current.getIntersection())) { // get whichever intersection in the road isn't our current
                    nonOriginNode = r.getSource();
                } else {
                    nonOriginNode = r.getDestination();
                }

                double pathTotal = current.getPathWeight() + r.getDefaultTime(); //compute the weight of path

                if (seenNode.containsKey(nonOriginNode)) { // check if we've already set the weight of this node
                    if (!closedNode.contains(seenNode.get(nonOriginNode))) { // if it's an already closed node, ignore
                        if (pathTotal < seenNode.get(nonOriginNode).myPathWeight) { // check if our path is more optimal
                            seenNode.get(nonOriginNode).setPathWeight(pathTotal); // edit this node with new path
                            seenNode.get(nonOriginNode).setPrev(current);
                            pq.remove(seenNode.get(nonOriginNode));
                            pq.add(seenNode.get(nonOriginNode));
                        }
                    } else {
                        continue; // continue to next iteration if we're computing an already visited node
                    }
                }
                // since this is a never-visited node, we'll add it to the queue along with it's data.
                ComparableIntersection newNode = new ComparableIntersection(nonOriginNode, pathTotal, current);
                seenNode.put(nonOriginNode, newNode);
                pq.add(newNode);
            }
            // mark current node as visited, then after reiteration we'll run through the next shortest node
            closedNode.add(current);
        }
        return null;
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
