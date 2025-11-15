package Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Intersection {
    private final int myId;
    private final ArrayList<Road> myRoads = new ArrayList<>(4); // we assume that intersections are connected to at most 4 roads
    private final boolean myAccessibility;

    public Intersection(final boolean isLocation1, final int theID) {
        this.myId = theID;
        this.myAccessibility = isLocation1;
    }

    public int getID() {
        return myId;
    }

    public boolean isLocation() {
        return myAccessibility;
    }

    public Road[] getRoadList() {
        return myRoads.toArray(new Road[0]);
    }

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (myAccessibility) {
            sb.append("Location ");
        } else {
            sb.append("Map.Intersection ");
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

    @Override
    public int hashCode() {
        return Objects.hash(myId, myRoads.size(), myAccessibility);
    }

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
