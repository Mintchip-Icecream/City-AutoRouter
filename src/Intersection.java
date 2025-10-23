import java.util.ArrayList;

public class Intersection {
    private int myId;
    private ArrayList<Road> myRoads = new ArrayList<>(4); // we assume that intersections are connected to at most 4 roads
    private boolean myAccessibility;

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

    public ArrayList<Road> getRoadList() {
        return myRoads;
    }

    public Road connectIntersection(Intersection theOther, double theDistance, double theSpeedLimit) {
        Road newRoad = new Road(this, theOther, theDistance, theSpeedLimit);
        theOther.addRoad(newRoad);
        this.addRoad(newRoad);
        return newRoad;
    }

    public void addRoad(Road theRoad) {
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
        if (!(obj instanceof Intersection)) { // check if an Intersection obj
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
//            sb.append("(");
//            sb.append(r.getDefaultTime());
//            sb.append(" mins), ");
            sb.append(", ");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        sb.append("]");
        return sb.toString();
    }
}
