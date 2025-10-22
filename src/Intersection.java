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
            if (r.getSourceID() != myId) {
                sb.append(r.getSourceID());
            } else {
                sb.append(r.getDestinationID());

            }
            sb.append("(");
            sb.append(Math.round((r.getLength()/100)/r.getSpeedLimit()*60));
            sb.append(" mins), ");

        }
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        sb.append("]");
        return sb.toString();
    }
}
