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
    }

    public double getSpeedLimit() {
        return mySpeedLimit;
    }

    public int getSourceID() {
        return myConnection[0].getID();
    }

    public int getDestinationID() {
        return myConnection[1].getID();
    }
}
