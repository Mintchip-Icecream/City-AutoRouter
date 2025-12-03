package Routing;

import java.util.Arrays;

import Map.CardinalDirection;
import Map.CityMap;
import Map.Direction;
import Map.Intersection;
import Map.Road;

/**
 * Object representing a route or a path between intersections from the starting location to the destination location.
 * Contains the route, along with other methods to view route such as in the form of an array of intersection IDs,
 * and plain language directions.
 *
 * @author June Flores
 * @version 11/15/25
 */
public class Route {
    /**
     * The intersections that comprise the route, from the first location, traversing into the destination location.
     */
    private final Intersection[] myIntersections;

    /**
     * Constructs a route from an ordered set of Intersection instances. Assuming that these intersections
     * are connected to each other, but if they're out of order that's the router's fault.
     *
     * @param theIntersections the ordered list of Intersection objects.
     */
    Route(final Intersection[] theIntersections) {
        this.myIntersections = theIntersections;
    }

    /**
     * Constructs a route from a set of intersection IDs on a map instance. Assumes that the intersections
     * listed can be found on the map through their ID number.
     *
     * @param theIDs a list of ID numbers representing intersections
     * @param theMap
     */
    Route(final int[] theIDs, final CityMap theMap) {
        Intersection[] theIntersections = new Intersection[theIDs.length];
        for (int i = 0; i < theIDs.length; i++) {
            Intersection addedIntersection = theMap.getIntersection(theIDs[i]);
            if (addedIntersection == null) {
                throw new IllegalArgumentException("Intersection " + theIDs[i] + " does not exist in passed map instance");
            }
            theIntersections[i] = theMap.getIntersection(theIDs[i]);
        }
        myIntersections = theIntersections;
    }

    /**
     * Returns the intersections that make up the route, from the first at the 0th index to the last intersection at
     * the nth index.
     *
     * @return an array of intersections representing the route.
     */
    public Intersection[] getRoute() {
        return myIntersections.clone();
    }

    /**
     * Returns an array of the ID numbers of each intersection in the route, ordered from the first to the last
     * intersection in the route.
     *
     * @return an array of intersection ID numbers.
     */
    public int[] getRouteIDs() {
        int[] result = new int[myIntersections.length];
        for (int i = 0; i < myIntersections.length; i++) {
            result[i] = myIntersections[i].getID();
        }
        return result;
    }

    /**
     * Returns the text representation of the route as a series of directions to the user (ex. "From location a,
     * turn right after 500 meters to location b").
     *
     * @return The text representation of the route as human-readable instructions.
     */
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
            from = myIntersections[i - 1];
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

    /**
     * Returns the string representation of the route as a set of intersections (their ID numbers).
     *
     * @return the route string as an array of the ID numbers in the route.
     */
    @Override
    public String toString() {
        return Arrays.toString(getRouteIDs());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object theOther) {
        if (this == theOther) {
            return true;
        }
        if (theOther == null) {
            return false;
        }
        if (!(theOther instanceof Route otherRoute)) {
            return false;
        }
        if (this.hashCode() != otherRoute.hashCode()) {
            return false;
        }
        // compares if the routes have the same ID list
        return Arrays.equals(this.getRouteIDs(), otherRoute.getRouteIDs());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(myIntersections);
    }
}
