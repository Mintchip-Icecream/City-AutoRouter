import java.util.ArrayList;

public class CityMap {
    private ArrayList<Intersection> myIntersections = new ArrayList();
    private ArrayList<Road> myRoads = new ArrayList();

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
                addRoad(inter1, inter2, dist, speed);
                counter += 4;
            }
            counter++;
        }
    }

    private void addIntersection(int isLocation1, int intersectionID) {
        myIntersections.add(new Intersection(isLocation1 == 1, intersectionID));
    }

    private void addRoad(int intersection1, int intersection2, double theDistance, double theSpeedLimit){
        Road newRoad = myIntersections.get(intersection1-1).connectIntersection(myIntersections.get(intersection2-1), theDistance, theSpeedLimit);
        myRoads.add(newRoad);
    }

    public Intersection getIntersection(int theIntersectionID) {
        if (theIntersectionID < myIntersections.size()) {
            return myIntersections.get(theIntersectionID);
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
}
