import Map.*;
import Routing.*;
import Simulation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    private static String fileName = "src/simMap.txt";

    public static void main(String[] args) throws IOException {
        CityMap newCM = new CityMap(Files.readString(Path.of(fileName)));
        EnvironmentSimulator em = new EnvironmentSimulator(newCM, 333L);
        Controller car = new Controller(newCM, em);
        int startID = 1;
        int endID = 35;
        System.out.println("Computing route from location " + startID + " to location " + endID);
        Intersection i1 = car.getMap().getIntersection(startID);
        Intersection i2 = car.getMap().getIntersection(endID);
        Route[] routes = car.computeRoute(i1, i2, 0.05, 5);
        System.out.println("Routes computed: " + routes.length);
        for (Route r: routes)  {
            System.out.println("Route Length: " + truncateNum(car.routeTime(r), 2) + " mins, Safety Risk: "
                    + truncateNum(car.routeSafety(r), 4));
            System.out.println(Arrays.toString(r.getRouteIDs()));
            System.out.println(r.toDirections());
        }
    }

    public static double truncateNum(double val, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return (double) Math.round(val *  scale) / scale;
    }


    /**
     * the txt file follows this format:
     * for intersections, do "I (isLocation) (ID)" where isLocation is either 0 or 1, and ID which starts from 1 and iterates up in order (ex: "I 0 5" is the 5th intersection)
     * for roads,  do "R (Map.Road ID1) (Map.Road ID2) (Distance) (SpeedLimit)", all of which are numbers, distance is feet, and speed limit is miles/perhour
     * roads must come after the intersections that occure
     */
}