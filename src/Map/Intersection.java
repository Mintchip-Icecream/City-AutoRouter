package Map;

import java.util.ArrayList;

public class Intersection {
    private final int myId;
    private final ArrayList<Road> myRoads = new ArrayList<>(4); // we assume that intersections are connected to at most 4 roads
    private final boolean myAccessibility;

    public Intersection(boolean isLocation1, int theID) {
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

    Road connectIntersection(Intersection theOther, double theDistance, double theSpeedLimit, CardinalDirection theDirection) {
        Road newRoad = new Road(this, theOther, theDistance, theSpeedLimit, theDirection);
        theOther.addRoad(newRoad);
        this.addRoad(newRoad);
        return newRoad;
    }

    private void addRoad(Road theRoad) {
        myRoads.add(theRoad);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { // compare memory location
            return true;
        }
        if (obj == null) { // check if null
            return false;
        }
        if (!(obj instanceof Intersection)) { // check if an Map.Intersection obj
            return false;
        }
        Intersection otherIntersection = (Intersection) obj;
        if (otherIntersection.getID() != myId) { // check if it has same ID
            return false;
        }
        if (otherIntersection.isLocation() != myAccessibility) { // check if it's also a location or not
            return false;
        }
        return myRoads.equals(otherIntersection.getRoadList()); // check if roads are equivalent
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
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        sb.append("]");
        return sb.toString();
    }
}
