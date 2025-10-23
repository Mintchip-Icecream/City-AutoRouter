public class Road {
    Intersection[] myConnection;
    double myLength;
    double mySpeedLimit;

    public Road(Intersection theSource, Intersection theDestination, double theDistance, double theSpeedLimit) {
        this.myConnection = new Intersection[]{theSource, theDestination};
        this.myLength = theDistance;
        this.mySpeedLimit = theSpeedLimit;
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

    /**
     *
     * @return the time it takes to traverse road in minutes
     */
    public double getDefaultTime() {
        double kmLength = myLength / 1000; // compute using (length in km) * (speedLimit) / 60
        return (double) Math.round(((kmLength*mySpeedLimit)/60)*1000)/1000; //rounding just to get a clean decimal
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { // compare memory location
            return true;
        }
        if (obj == null) { // check if null
            return false;
        }
        if (!(obj instanceof Road)) { // check if an Intersection obj
            return false;
        }
        Road otherRoad = (Road) obj;

        if (myLength != otherRoad.getLength()) { // check if road isn't same length
            return false;
        }
        if (mySpeedLimit!= otherRoad.getSpeedLimit()) { // check if road isn't same speed limit
            return false;
        }
       if (!this.getSource().equals(otherRoad.getSource())) { // check if road has same source
           return false;
       }
        return this.getDestination().equals(otherRoad.getDestination()); // check if
    }
}
