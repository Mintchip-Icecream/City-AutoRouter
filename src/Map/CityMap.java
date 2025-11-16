package Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/**
 * Representation of a map of a given area using a weighted graph data structure.
 * City maps are represented using nodes (using the Intersection data type) and weighted edges
 * (using the Road data type). This class provides a wrapper for an associated collection of nodes and edges,
 * along with methods for accessing the items within a map.
 *
 * @author June Flores
 * @version 11/15/25
 */
public final class CityMap {
    /**
     * When processing our txt file for initializing an intersection. Each line has a word array of size 3,
     * because there are three elements: ["I" (IntersectionType) (IDNumber)].
     */
    private static final int INTERSECTION_LINE_LENGTH = 2;
    /**
     * When processing our txt file for intializing an intersection. Each line is 6 elements long,
     * ["R" (SourceID) (DestinationID) (LengthInMeters) (SpeedLimitInKM/H) (CardinalDirection)].
     */
    private static final int ROAD_LINE_LENGTH = 5;
    /**
     * When initializing an intersection from a line, the 2nd string is either 0 or 1 representing if it's a location.
     * If line[1] is a 0, it's a plain intersection, if it's a 1, then it's a location and may be selected for routing.
     */
    private static final int INTERSECTION_TYPE_INDEX = 1;
    /**
     * The index in the initialization of an intersection line, line[2] is the number ID of the intersection.
     */
    private static final int INTERSECTION_NUM_INDEX = 2;
    /**
     * When initializing a road, the 2nd string in the processed line is the source intersection's ID number.
     */
    private static final int ROAD_INTER_SOURCE_INDEX = 1;
    /**
     * When initializing a road, the 3rd string in the processed line is the destination intersection's ID number.
     */
    private static final int ROAD_INTER_DEST_INDEX = 2;
    /**
     * The index of a road line representing the distance of the road in meters.
     */
    private static final int ROAD_LENGTH_INDEX = 3;
    /**
     * The index of a road line representing the speed limit in Kilometers / per hour.
     */
    private static final int ROAD_SPEED_LIMIT_INDEX = 4;
    /**
     * The index of a road line representing the cardinal direction from the source intersection to the destination
     * intersection. If source intersection 1 is South of destination 2, then our direction is South.
     */
    private static final int ROAD_DIRECTION_INDEX = 5;
    /**
     * HashMap containing the intersections. We access the intersections using their ID number.
     */
    private final HashMap<Integer, Intersection> myIntersections = new HashMap<>();
    /**
     * List containing the roads in no particular order.
     */
    private final ArrayList<Road> myRoads = new ArrayList<>();

    /**
     * Default constructor for a CityMap that's empty with no intersections or roads.
     */
    public CityMap() { }

    /**
     * Initializes a CityMap according to a string of text containing the map data. For correct initialization,
     * the text must follow this order: Each intersection is initialized using a 3 item line,
     * [I (IsLocation, 0 if false, and 1 if true) (Intersection ID Number, must be unique)], but don't include
     * brackets. Each road is initialized using a 6 item line,
     * [R (Source ID Number) (Destination ID Number) (Distance in Meters) (Speed Limit in KM/H) (Cardinal Direction)]
     * An example of intializing roads and intersections would be:
     * " I 1 1
     * I 0 2
     * R 1 2 320 50 S "
     *
     * @param theMapList String representation of the map following the format explained in Javadoc comment.
     */
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

    /**
     * If 2 intersections are connected by a road, return the road connecting them.
     *
     * @param intersection1 An intersection.
     * @param intersection2 An intersection presumed to be connected to the first intersection.
     * @return The road object that connects intersection1 to intersection 2, or null if none is found.
     */
    public static Road getRoad(final Intersection intersection1, final Intersection intersection2) {
        if (intersection1 == null || intersection2 == null) {
            return null;
        }
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

    /**
     * Returns an object array view of every intersection stored in the map.
     * @return An intersection array of the intersections contained in the map object.
     */
    public Intersection[] getAllIntersections() {
        ArrayList<Intersection> resultArray = new ArrayList<>(myIntersections.size());
        for (java.util.Map.Entry<Integer, Intersection> e: myIntersections.entrySet()) {
           resultArray.add(e.getValue());
        }
        return resultArray.toArray(new Intersection[0]);
    }

    /**
     * Returns an object array view of every road stored in the map.
     * @return A road array view of the roads contained in the map object.
     */
    public Road[] getAllRoads() {
        return myRoads.toArray(new Road[0]);
    }

    /**
     * Gets an intersection from the map from the ID number.
     * @param theIntersectionID The ID number of the Intersection object.
     * @return null if the ID number does not exist in our map, otherwise return the intersection with the ID number.
     */
    public Intersection getIntersection(final int theIntersectionID) {
        if (theIntersectionID <= myIntersections.size()) {
            return myIntersections.get(theIntersectionID);
        }
        return null;
    }

    /**
     * Returns a string of all the intersections in the map. We call the toString of every intersection object,
     * which contains an adjacency list of all the intersections that are connected to it.
     * Each intersection is represented using their ID number.
     *
     * @return A string representation of the Map graph.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Intersection i : getAllIntersections()) {
            sb.append(i.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object theOther) {
        if (theOther == null || getClass() != theOther.getClass()) {
            return false;
        }

        CityMap cityMap = (CityMap) theOther;
        if (this.hashCode() != theOther.hashCode()) {
            return false;
        }
        return Arrays.equals(this.getAllIntersections(), cityMap.getAllIntersections())
                && Arrays.equals(this.getAllRoads(), cityMap.getAllRoads());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(getAllIntersections()), Arrays.hashCode(getAllRoads()));
    }

    /**
     * Instantiates an intersection to go into our map. The intersection is inserted into our map using an
     * Integer-Intersection entry pair.
     *
     * @param isLocation1 The number representing if the intersection is a location, 0 = false, 1 = true.
     * @param intersectionID The unique ID number of the intersection,
     *                       if the ID is already existing, it will override the previous intersection.
     */
    private void addIntersection(final int isLocation1, final int intersectionID) {
        myIntersections.put(intersectionID, new Intersection(isLocation1 == 1, intersectionID));
    }

    /**
     * Instantiates a road to go into our map, the intersections must already be existing for the road to be valid.
     *
     * @param intersection1 The source intersection.
     * @param intersection2 The destination intersection.
     * @param theDistance The distance of the road in meters.
     * @param theSpeedLimit The speed limit of the road in kilometers per hour.
     * @param theDirection The cardinal direction (NSEW) of the road from the source to the destination.
     */
    private void addRoad(final int intersection1, final int intersection2, final double theDistance,
                         final double theSpeedLimit, final CardinalDirection theDirection) {
        Road newRoad = myIntersections.get(intersection1).connectIntersection(myIntersections.get(intersection2),
                theDistance, theSpeedLimit, theDirection);
        myRoads.add(newRoad);
    }

    /**
     * Determines the cardinal direction of the road when initializing using a string text.
     *
     * @param myInputLines The string array containing all the elements in the map.
     * @param counter The current position index, which should be pointing to "R".
     * @return null if there is no valid cardinal direction at the direction index of a line. Otherwise,
     * return the cardinaal direction of the road from the source to the destination.
     */
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
