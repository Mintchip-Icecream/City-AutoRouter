package Map;

import java.util.Objects;

/**
 * Data type representing a weighted edge on the CityMap graph, or a road between two intersections in plain terms.
 * Roads contain the intersections they're connected to, along with the attributes of the road, such as speed limit,
 * distance, and direction.
 *
 * @author June Flores
 * @version 11/30/25
 */
public class Road {
    /**
     * Used to convert between meters and kilometers. Kilometers = Meter * 1000, Meter = Kilometer/1000.
     */
    private static final int METERS_IN_KILOMETER = 1000;
    /**
     * Used to convert between minutes and hours. Minutes = Hour / 60, Hour = Minutes * 60.
     */
    private static final int MINUTES_IN_HOUR = 60;
    /**
     * An array containing the intersections connected to this road. For now only 2 intersections are stored in a road.
     */
    private final Intersection[] myConnection;
    /**
     * The distance of the road in meters.
     */
    private final double myLength;
    /**
     * The speed limit of the road in kilometers per hour.
     */
    private final double mySpeedLimit;
    /**
     * The cardinal direction of the road (North, South, East, West) from the source to the destination.
     */
    private final CardinalDirection myDirection;

    /**
     * Creates a new road instance from the source intersection to the destination intersection.
     * Package private because we want to encapsulate map creation in the CityMap class.
     *
     * @param theSource The source intersection.
     * @param theDestination The destination intersection.
     * @param theDistance The length of road in meters.
     * @param theSpeedLimit The speed limit in KM/H.
     * @param theDirection The cardinal direction of the road.
     */
    public Road(final Intersection theSource, final Intersection theDestination,
                final double theDistance, final double theSpeedLimit, final CardinalDirection theDirection) {
        if (theSource == null || theDestination == null) {
            throw new IllegalArgumentException("Road cannot be initialized with a null intersection.");
        }
        this.myConnection = new Intersection[]{theSource, theDestination};
        this.myLength = theDistance;
        this.mySpeedLimit = theSpeedLimit;
        this.myDirection = theDirection;
    }

    /**
     * Returns the length of the road in meters.
     *
     * @return the length of road in meters.
     */
    public final double getLength() {
        return myLength;
    }

    /**
     * Returns the speed limit of the road in kilometers per hour.
     *
     * @return the speed limit of the road in KM/h.
     */
    public final double getSpeedLimit() {
        return mySpeedLimit;
    }

    /**
     * Returns the source intersection, or the first intersection in the connection list.
     *
     * @return the source intersection.
     */
    public final Intersection getSource() {
        return myConnection[0];
    }

    /**
     * Returns the destination intersection, or the final intersection in the connection list.
     *
     * @return the destination intersection.
     */
    public Intersection getDestination() {
        return myConnection[myConnection.length - 1];
    }

    /**
     * Returns the cardinal direction of the road from the source intersection to the destination.
     *
     * @return the cardinal direction of the road.
     */
    public final CardinalDirection getDirection() {
        return myDirection;
    }

    /**
     * Returns the cardinal direction of the road when incoming through the passed intersection. If the intersection
     * the caller is incoming from is the destination, then we return the opposite direction. If the source or
     * an intersection that the road doesn't connect to, returns the default direction.
     *
     * @param theSource the incoming intersection.
     * @return the direction of the road, swapped if the incoming intersection is the destination.
     */
    public final CardinalDirection getDirection(final Intersection theSource) {
       if (!theSource.equals(myConnection[0])) {
           return CardinalDirection.swapDirection(myDirection);
       }
        return myDirection;
    }

    /**
     * Returns the calculated time in minutes to travel the road as a function of the length / speed limit.
     * Assumes the user is traveling exactly at the speed limit.
     *
     * @return the time it takes to traverse road in minutes
     */
    public double getDefaultTime() {
        double kmLength = myLength / METERS_IN_KILOMETER; // compute using (length in km) * (speedLimit) / 60
        return kmLength * mySpeedLimit / MINUTES_IN_HOUR; //rounding just to get a clean decimal
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
        if (!(theObj instanceof Road otherRoad)) { // check if an Map.Intersection obj
            return false;
        }
        if (this.hashCode() != otherRoad.hashCode()) {
            return false;
        }
        if (myLength != otherRoad.getLength()) { // check if road isn't same length
            return false;
        }
        if (mySpeedLimit != otherRoad.getSpeedLimit()) { // check if road isn't same speed limit
            return false;
        }
       if (this.getSource().getID() != otherRoad.getSource().getID()) { // check if road has same source
           return false;
       }
        return this.getDestination().getID() == otherRoad.getDestination().getID(); // check if same destination
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getSource().getID(), getDestination().getID(), myLength, mySpeedLimit, myDirection);
    }
}
