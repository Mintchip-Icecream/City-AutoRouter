import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    private static String fileName = "src/simMap.txt";
    public static void main(String[] args) throws IOException {
        String fileContents = Files.readString(Path.of(fileName));

        CityMap cm = new CityMap(fileContents);
        System.out.println(cm);
    }



    /**
     * the txt file follows this format:
     * for intersections, do "I (isLocation) (ID)" where isLocation is either 0 or 1, and ID which starts from 1 and iterates up in order (ex: "I 0 5" is the 5th intersection)
     * for roads,  do "R (Road ID1) (Road ID2) (Distance) (SpeedLimit)", all of which are numbers, distance is feet, and speed limit is miles/perhour
     * roads must come after the intersections that occure
     */
}