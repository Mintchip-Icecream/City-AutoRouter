package Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class CityMap {
    private static final int INTERSECTION_TYPE_INDEX = 1;
    private static final int INTERSECTION_NUM_INDEX = 2;
    private static final int ROAD_INTER_SOURCE_INDEX = 1;
    private static final int ROAD_INTER_DEST_INDEX = 2;
    private static final int ROAD_LENGTH_INDEX = 3;
    private static final int ROAD_SPEED_LIMIT_INDEX = 4;
    private static final int INTERSECTION_LINE_LENGTH = 2;
    private static final int ROAD_DIRECTION_INDEX = 5;
    private static final int ROAD_LINE_LENGTH = 5;
    private final HashMap<Integer, Intersection> myIntersections = new HashMap<>();
    private final ArrayList<Road> myRoads = new ArrayList<>();

    // default constructor for empty map
    public CityMap() {}

    // constructor for a map with a series of commands for the map
    public CityMap(final String theMapList) {
        int counter = 0;
        String[] myInputLines = theMapList.split("\\s+"); // splits inputs by spaces and line breaks

        while (counter < myInputLines.length - 1) {
            if (myInputLines[counter].equals("I")) {
                int isLocation = Integer.parseInt(myInputLines[counter + INTERSECTION_TYPE_INDEX]);
                int interID = Integer.parseInt(myInputLines[counter + INTERSECTION_NUM_INDEX]);
                addIntersection(isLocation, interID);
                counter += INTERSECTION_LINE_LENGTH;
            } else if (myInputLines[counter].equals("R")) {
                int inter1 = Integer.parseInt(myInputLines[counter + ROAD_INTER_SOURCE_INDEX]);
                int inter2 = Integer.parseInt(myInputLines[counter + ROAD_INTER_DEST_INDEX]);
                double dist = Double.parseDouble(myInputLines[counter + ROAD_LENGTH_INDEX]);
                double speed = Double.parseDouble(myInputLines[counter + ROAD_SPEED_LIMIT_INDEX]);

                CardinalDirection theDirection = getCardinalDirection(myInputLines, counter);

                addRoad(inter1, inter2, dist, speed, theDirection);
                counter += ROAD_LINE_LENGTH;
            } else {
                throw new IllegalArgumentException("No intersection or road found while parsing argument line.");
            }
            counter++;
        }
    }

    public static Road getRoad(final Intersection intersection1, final Intersection intersection2) {
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
        ArrayList<Intersection> resultArray = new ArrayList<>(myIntersections.size());
        for (Map.Entry<Integer, Intersection> e: myIntersections.entrySet()) {
           resultArray.add(e.getValue());
        }
        return resultArray.toArray(new Intersection[0]);
    }

    public Road[] getAllRoads() {
        return myRoads.toArray(new Road[0]);
    }

    public Intersection getIntersection(final int theIntersectionID) {
        if (theIntersectionID <= myIntersections.size()) {
            return myIntersections.get(theIntersectionID);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Intersection i : getAllIntersections()) {
            sb.append(i.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    private void addIntersection(final int isLocation1, final int intersectionID) {
        myIntersections.put(intersectionID, new Intersection(isLocation1 == 1, intersectionID));
    }

    private void addRoad(final int intersection1, final int intersection2, final double theDistance,
                         final double theSpeedLimit, final CardinalDirection theDirection) {
        Road newRoad = myIntersections.get(intersection1).connectIntersection(myIntersections.get(intersection2),
                theDistance, theSpeedLimit, theDirection);
        myRoads.add(newRoad);
    }

    private CardinalDirection getCardinalDirection(final String[] myInputLines, final int counter) {
        CardinalDirection theDirection = switch (myInputLines[counter + ROAD_DIRECTION_INDEX]) {
            case "S" -> CardinalDirection.SOUTH;
            case "W" -> CardinalDirection.WEST;
            case "E" -> CardinalDirection.EAST;
            case "N" -> CardinalDirection.NORTH;
            default -> null;
        };
        if (theDirection == null) {
            throw new IllegalArgumentException("Road does not have valid cardinal direction.");
        }
        return theDirection;
    }

}
