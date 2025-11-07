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
        String fileContents = Files.readString(Path.of(fileName));

        CityMap cm = new CityMap(fileContents);
        System.out.println(cm);
        Router route = new Router(cm);
        System.out.println("Computing Optimal Path from 14 to 36...");
        Route result = route.computeRoute(cm.getIntersection(14), cm.getIntersection(36));
        System.out.println("Routing.Route: " + result + ", Time = " + route.routeLength(result) + " mins");
        System.out.println("Directions: "  + result.toDirections());
        System.out.println();
        EnvironmentSimulator em = new EnvironmentSimulator(cm, 255L, 1);

        System.out.println(SafetyChecker.mapSafety(em));

        System.out.println("Compute 1-3 safest routes from 14 to 36 under simulated condition");
        int counter = 0;
        double thresholdIncrementer = 0.05;
        double threshold = 1.0;
        Route prevRoute = null;
        while (counter < 3 && threshold > 0) {
            Route theResult = route.computeRoute(cm.getIntersection(14),
                    cm.getIntersection(36), threshold, em);
            if (theResult == null) {
                threshold -= thresholdIncrementer;
            } else if (!theResult.equals(prevRoute)) {
                prevRoute = theResult;
                counter++;
                System.out.println("Routing.Route: " + theResult + ", Time = "
                        + route.routeLength(theResult, em) + " mins, risk = "
                        + ((double) Math.round(SafetyChecker.routeSafety(theResult, em)*100)/100));
                threshold -= thresholdIncrementer;
            } else {
                threshold -= thresholdIncrementer;
            }
        }
        System.out.println("Unique Paths Found: " + counter);
    }



    /**
     * the txt file follows this format:
     * for intersections, do "I (isLocation) (ID)" where isLocation is either 0 or 1, and ID which starts from 1 and iterates up in order (ex: "I 0 5" is the 5th intersection)
     * for roads,  do "R (Map.Road ID1) (Map.Road ID2) (Distance) (SpeedLimit)", all of which are numbers, distance is feet, and speed limit is miles/perhour
     * roads must come after the intersections that occure
     */
}