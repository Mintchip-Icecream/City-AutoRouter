package Routing;

import Map.*;

import java.util.Arrays;

public class Route {
    private final Intersection[] myIntersections;

    Route(Intersection[] theIntersections) {
        this.myIntersections = theIntersections;
    }

    Route(int[] theIDs, CityMap theMap) {
        Intersection[] theIntersections = new Intersection[theIDs.length];
        for (int i = 0; i < theIDs.length; i++) {
            theIntersections[i] = theMap.getIntersection(theIDs[i]);
        }
        myIntersections = theIntersections;
    }

    public Intersection[] getRoute() {
        return myIntersections.clone();
    }

    public int[] getRouteIDs() {
        int[] result = new int[myIntersections.length];
        for (int i = 0; i < myIntersections.length; i++) {
            result[i] = myIntersections[i].getID();
        }
        return result;
    }

    public String toDirections() {
        StringBuilder sb = new StringBuilder();
        Intersection from = myIntersections[0];
        Intersection to = myIntersections[1];
        Road r = CityMap.getRoad(from, to);
        if (r == null) {
            return "Invalid Route";
        }
        CardinalDirection currentDir = r.getDirection(from);
        double accumulator = r.getLength();
        sb.append("Head ");
        sb.append(r.getDirection(from).toString());
        sb.append(" from Location ");
        sb.append(from.getID());
        for (int i = 2; i < myIntersections.length; i++) {
            from = myIntersections[i-1];
            to = myIntersections[i];
            r = CityMap.getRoad(from, to);

            if (r == null) {
                return "Invalid Route";
            }

            CardinalDirection newDir = r.getDirection(from);
            Direction theDirection = CardinalDirection.turnDirection(currentDir, newDir);
            if (theDirection == Direction.FORWARD) {
                accumulator += r.getLength();
            } else {
                sb.append(", then turn ");
                sb.append(theDirection.toString().toLowerCase());
                sb.append(" after ");
                sb.append(accumulator);
                sb.append(" meters");
                accumulator = r.getLength();
            }
            currentDir = newDir;
        }
        sb.append(" onto Location ");
        sb.append(to.getID());
        return sb.toString();
    }

    @Override
    public String toString() {
        return Arrays.toString(getRouteIDs());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Route otherRoute)) {
            return false;
        }
        // compares if the routes have the same ID list
        return Arrays.equals(this.getRouteIDs(), otherRoute.getRouteIDs());
    }
}
