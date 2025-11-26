import Map.*;
import Routing.*;
import Simulation.*;
import UI.Controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException, SQLException {
        Controller car = new Controller();
        System.out.println("Loading Simulation 1");
        car.loadSim(1);
        int startID = 1;
        int endID = 56;
        System.out.println("Computing route from location " + startID + " to location " + endID);
        Intersection i1 = car.getMap().getIntersection(startID);
        Intersection i2 = car.getMap().getIntersection(endID);
        Route[] routes = car.computeRoute(i1, i2, 0.05, 5);
        System.out.println(SafetyChecker.mapSafety(car.getEnvironment()));
        System.out.println("Routes computed: " + routes.length);
        for (Route r: routes)  {
            System.out.println("Route Length: " + truncateNum(car.routeTime(r), 2) + " mins, Safety Risk: "
                    + truncateNum(car.routeSafety(r), 4));
            System.out.println(Arrays.toString(r.getRouteIDs()));
            System.out.println(r.toDirections());
        }
        System.out.println("Now Saving Safest Route");
        car.saveRoute(routes[1]);
        System.out.println("Now Generating New Simulation");
        car.generateRandomSimulation();
        Route[] newRoutes = car.computeRoute(i1, i2, 0.05, 5);
        System.out.println("Routes computed: " + newRoutes.length);
        for (Route r: newRoutes)  {
            System.out.println("Route Length: " + truncateNum(car.routeTime(r), 2) + " mins, Safety Risk: "
                    + truncateNum(car.routeSafety(r), 4));
            System.out.println(Arrays.toString(r.getRouteIDs()));
            System.out.println(r.toDirections());
        }
        Route oldRoute = car.loadRoute(1);
        System.out.println("is old route faster on new sim?");
        System.out.println(car.routeTime(oldRoute) < car.routeTime(newRoutes[0]));
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