package Map;

import java.util.ArrayList;

public class CityMap {
    private final ArrayList<Intersection> myIntersections = new ArrayList<>();
    private final ArrayList<Road> myRoads = new ArrayList<>();

    // default constructor for empty map
    public CityMap() {
    }

    // constructor for a map with a series of commands for the map
    public CityMap(String theMapList) {
        int counter = 0;
        String[] myInputLines = theMapList.split("\\s+"); // splits inputs by spaces and line breaks

        while (counter < myInputLines.length - 1) {
            if (myInputLines[counter].equals("I")){
                int isLocation = Integer.parseInt(myInputLines[counter+1]);
                int ID = Integer.parseInt(myInputLines[counter+2]);
                addIntersection(isLocation, ID);
                counter += 2;
            }
            if (myInputLines[counter].equals("R")){
                int inter1 = Integer.parseInt(myInputLines[counter+1]);
                int inter2 = Integer.parseInt(myInputLines[counter+2]);
                double dist = Double.parseDouble(myInputLines[counter+3]);
                double speed = Double.parseDouble(myInputLines[counter+4]);
                CardinalDirection theDirection;
                switch (myInputLines[counter+5]) {
                    case "S": theDirection = CardinalDirection.SOUTH;
                        break;
                    case "W": theDirection = CardinalDirection.WEST;
                        break;
                    case "E": theDirection = CardinalDirection.EAST;
                        break;
                    case "N": theDirection = CardinalDirection.NORTH;
                        break;
                    default: theDirection = CardinalDirection.NORTH;
                }
                addRoad(inter1, inter2, dist, speed, theDirection);
                counter += 5;
            }
            counter++;
        }
    }

    public static Road getRoad(Intersection intersection1, Intersection intersection2) {
        for (Road r: intersection1.getRoadList()) {
            if (r.getDestination().equals(intersection2)) {
                return r;
            }
            if (r.getSource().equals(intersection2)) {
                return r;
            }
        }
        return null;
    }

    public Intersection[] getAllIntersections() {
        return myIntersections.toArray(new Intersection[0]);
    }

    public Road[] getAllRoads() {
        return myRoads.toArray(new Road[0]);
    }

    public Intersection getIntersection(int theIntersectionID) {
        if (theIntersectionID <= myIntersections.size()) {
            return myIntersections.get(theIntersectionID-1);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Intersection i : myIntersections) {
            sb.append(i.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    private void addIntersection(int isLocation1, int intersectionID) {
        myIntersections.add(new Intersection(isLocation1 == 1, intersectionID));
    }

    private void addRoad(int intersection1, int intersection2, double theDistance, double theSpeedLimit, CardinalDirection theDirection){
        Road newRoad = myIntersections.get(intersection1-1).connectIntersection(myIntersections.get(intersection2-1), theDistance, theSpeedLimit, theDirection);
        myRoads.add(newRoad);
    }

}
