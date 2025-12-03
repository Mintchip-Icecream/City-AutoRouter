package Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Data type representing a node on the CityMap graph, or an intersection on a road in real life terms.
 * Intersections store their node ID, an adjacency list consisting of the roads connecting them to other roads,
 * and a bool of whether they're a location and can be set as the start of a route.
 *
 * @author June Flores
 * @version 11/30/25
 */
public class Intersection {
    /**
     * Integer identifier for the intersection. We assume that in a map object, no two intersections share an ID.
     */
    private final int myId;
    /**
     * A list of road (edge) objects. We can determine which intersections we're connected to by checking the roads.
     * Lists are initialized with a capacity of 4 because most intersections have just 4 roads or less.
     */
    private final ArrayList<Road> myRoads = new ArrayList<>(4);
    /**
     * Boolean determining if an intersection is a location or not. Locations are functionally the same as
     * intersections, but can be set as the starts and ends of routes.
     */
    private final boolean myAccessibility;

    /**
     * Constructs an empty intersection with an ID number and is either a location or not.
     * This is package-private, we want the creation of intersections to be encapsulated within the CityMap class.
     *
     * @param isLocation1 True if node is an intersection.
     * @param theID The integer identifier of the intersection.
     */
    Intersection(final boolean isLocation1, final int theID) {
        this.myId = theID;
        this.myAccessibility = isLocation1;
    }

    /**
     * Returns the ID number of this intersection.
     *
     * @return the ID number of the intersection.
     */
    public final int getID() {
        return myId;
    }

    /**
     * Returns true if the intersection is a location.
     *
     * @return true if this intersection is a location.
     */
    public final boolean isLocation() {
        return myAccessibility;
    }

    /**
     * Returns an array of references to the Road objects connected to this intersection.
     *
     * @return an array of Road objects associated with the intersection.
     */
    public final Road[] getRoadList() {
        return myRoads.toArray(new Road[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object theObj) {
        if (this == theObj) { // compare memory location
            return true;
        }
        if (theObj == null) { // check if null
            return false;
        }
        if (!(theObj instanceof Intersection otherIntersection)) { // check if same class
            return false;
        }
        if (this.hashCode() != theObj.hashCode()) {
            return false;
        }
        if (otherIntersection.getID() != myId) { // check if it has same ID
            return false;
        }
        if (otherIntersection.isLocation() != myAccessibility) { // check if it's also a location or not
            return false;
        }
        return Arrays.equals(getRoadList(), otherIntersection.getRoadList());
    }

    /**
     * Returns the string representation of the adjacency list of the intersection. The string consists of the
     * ID number of the location/intersection and the ID numbers of the intersections it is connected to.
     *
     * @return the string representation of the intersection.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (myAccessibility) {
            sb.append("Location ");
        } else {
            sb.append("Intersection ");
        }
        sb.append(myId);
        sb.append(": [");
        for (Road r : myRoads) {
            if (r.getSource().getID() != myId) {
                sb.append(r.getSource().getID());
            } else {
                sb.append(r.getDestination().getID());
            }
            sb.append(", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(myId, myRoads.size(), myAccessibility);
    }

    /**
     * Generates a road connecting this intersection to another intersection.
     *
     * @param theOther The other intersection this intersection will be connected to.
     * @param theDistance The distance of the road.
     * @param theSpeedLimit The speed limit of the road.
     * @param theDirection The cardinal direction of the road.
     * @return The road object created
     */
    Road connectIntersection(final Intersection theOther, final double theDistance,
                             final double theSpeedLimit, final CardinalDirection theDirection) {
        Road newRoad = new Road(this, theOther, theDistance, theSpeedLimit, theDirection);
        theOther.addRoad(newRoad);
        this.addRoad(newRoad);
        return newRoad;
    }

    private void addRoad(final Road theRoad) {
        myRoads.add(theRoad);
    }
}
