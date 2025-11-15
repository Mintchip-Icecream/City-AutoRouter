package Map;

import java.util.Objects;

public class Road {
    private static final int METERS_IN_KILOMETER = 1000;
    private static final int MINUTES_IN_HOUR = 60;
    private final Intersection[] myConnection;
    private final double myLength;
    private final double mySpeedLimit;
    private final CardinalDirection myDirection;

    public Road(final Intersection theSource, final Intersection theDestination,
                final double theDistance, final double theSpeedLimit, final CardinalDirection theDirection) {
        this.myConnection = new Intersection[]{theSource, theDestination};
        this.myLength = theDistance;
        this.mySpeedLimit = theSpeedLimit;
        this.myDirection = theDirection;
    }

    public double getLength() {
        return myLength;
    } // length in meters

    public double getSpeedLimit() {
        return mySpeedLimit;
    } // speed limit in km/h

    public Intersection getSource() {
        return myConnection[0];
    }

    public Intersection getDestination() {
        return myConnection[1];
    }

    public CardinalDirection getDirection() {
        return myDirection;
    }

    public CardinalDirection getDirection(final Intersection theSource) {
       if (!theSource.equals(myConnection[0])) {
           return CardinalDirection.swapDirection(myDirection);
       }
        return myDirection;
    }

    /**
     *
     * @return the time it takes to traverse road in minutes
     */
    public double getDefaultTime() {
        double kmLength = myLength / METERS_IN_KILOMETER; // compute using (length in km) * (speedLimit) / 60
        return kmLength * mySpeedLimit / MINUTES_IN_HOUR; //rounding just to get a clean decimal
    }

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
       if (!this.getSource().equals(otherRoad.getSource())) { // check if road has same source
           return false;
       }
        return this.getDestination().equals(otherRoad.getDestination()); // check if
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSource(), getDestination(), myLength, mySpeedLimit, myDirection);
    }
}
