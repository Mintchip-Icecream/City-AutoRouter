import Controller.Controller;
import Map.*;
import Routing.*;
import Simulation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException, SQLException {
//        Controller car = new Controller();
//        car.saveMap("src/simMap.txt");
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