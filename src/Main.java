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
        int[] result = route.computeRoute(cm.getIntersection(14), cm.getIntersection(36));
        System.out.println("Route: " + Arrays.toString(result) + ", Time = " + route.routeLength(result) + " mins");
        System.out.println("Directions: "  + route.directions(result));
        EnvironmentSimulator em = new EnvironmentSimulator(cm, 255L, 1);

        System.out.println("Compute 1-3 safest routes from 14 to 36 under simulated condition");
        int counter = 0;
        double thresholdIncrementer = 0.05;
        double threshold = 1.0;
        int[] prevRoute = null;
        while (counter < 3 && threshold > 0) {
            int[] resultPath = route.computeRoute(cm.getIntersection(14),
                    cm.getIntersection(36), threshold, em);
            if (resultPath == null) {
                threshold += thresholdIncrementer;
            } else if (Arrays.compare(resultPath, prevRoute) != 0) {
                prevRoute = resultPath;
                counter++;
                System.out.println("Route: " + Arrays.toString(resultPath) + ", Time = "
                        + route.routeLength(resultPath) + " mins, risk = "
                        + ((double) Math.round(route.routeSafety(resultPath, em)*100)/100));
                threshold -= thresholdIncrementer;
            } else {
                threshold -= thresholdIncrementer;
            }
        }
        System.out.println(counter + ", max threshold" + threshold);
    }



    /**
     * the txt file follows this format:
     * for intersections, do "I (isLocation) (ID)" where isLocation is either 0 or 1, and ID which starts from 1 and iterates up in order (ex: "I 0 5" is the 5th intersection)
     * for roads,  do "R (Road ID1) (Road ID2) (Distance) (SpeedLimit)", all of which are numbers, distance is feet, and speed limit is miles/perhour
     * roads must come after the intersections that occure
     */
}