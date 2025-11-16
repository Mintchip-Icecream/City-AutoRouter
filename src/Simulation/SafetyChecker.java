package Simulation;

import Map.CityMap;
import Map.Intersection;
import Map.Road;

import Routing.Route;

/**
 * Safety evaluation model for routes. Contains a series of static methods that determine the safety risk associated
 * with an intersection, road, or route given the passed EnvironmentSimulator instance. Different conditions are
 * weighted differently. For example, our current implementation dictates that obstacles and roadblocks provide
 * a greater risk than poor weather.
 *
 * @author June Flores
 * @version 11/15/25
 */
public class SafetyChecker {
    private static final double CONDITION_SCALAR = 1.5;
    private static final double WEATHER_TIME_WEIGHT = 0.2;
    private static final double TRAFFIC_TIME_WEIGHT = 0.4;
    private static final double OBSTACLE_TIME_WEIGHT = 0.4;
    private static final double WEATHER_SAFETY_WEIGHT = 0.2;
    private static final double TRAFFIC_SAFETY_WEIGHT = 0.3;
    private static final double OBSTACLE_SAFETY_WEIGHT = 0.5;
    private static final int PRINT_DECIMAL_PLACES = 3;
    private static final int NUMBER_BASE = 10;

    public SafetyChecker() {
    }

    public static double safetyRisk(final Intersection theIntersection, final EnvironmentSimulator theSim) {
        Conditions cond = theSim.getCondition(theIntersection);
        return cond.getObstacleSeverity() * OBSTACLE_SAFETY_WEIGHT + cond.getWeatherFactor() * WEATHER_SAFETY_WEIGHT
                + cond.getTrafficDensity() * TRAFFIC_SAFETY_WEIGHT;
    }

    public static double safetyRisk(final Road theRoad, final EnvironmentSimulator theSim) {
        Conditions cond = theSim.getCondition(theRoad);
        return cond.getObstacleSeverity() * OBSTACLE_SAFETY_WEIGHT + cond.getWeatherFactor() * WEATHER_SAFETY_WEIGHT
                + cond.getTrafficDensity() * TRAFFIC_SAFETY_WEIGHT;
    }

    public static double roadTime(final Road theRoad, final EnvironmentSimulator theSim) {
        Conditions roadCon = theSim.getCondition(theRoad);
        double trafficMultiplier = roadCon.getObstacleSeverity() * OBSTACLE_TIME_WEIGHT
                + roadCon.getTrafficDensity() * TRAFFIC_TIME_WEIGHT + roadCon.getWeatherFactor() * WEATHER_TIME_WEIGHT;
        return theRoad.getDefaultTime() * Math.exp(CONDITION_SCALAR * trafficMultiplier);
    }

    // maxRouteSafety
    public static double routeSafety(final Route theRoute, final EnvironmentSimulator theSim) {
        double maxRouteSafety = 0.0;
        Intersection[] routePath = theRoute.getRoute();
        Intersection from;
        Intersection to = routePath[1];
        for (int i = 1; i < routePath.length; i++) {
            from = routePath[i - 1];
            to = routePath[i];
            Road r = CityMap.getRoad(from, to);
            double fromRisk = safetyRisk(from, theSim);
            double roadRisk = safetyRisk(r, theSim);
            maxRouteSafety = Math.max(maxRouteSafety, Math.max(fromRisk, roadRisk));
        }
        maxRouteSafety = Math.max(maxRouteSafety, SafetyChecker.safetyRisk(to, theSim));
        return maxRouteSafety;
    }

    public static String mapSafety(final EnvironmentSimulator theSim) {
        StringBuilder sb = new StringBuilder();
        for (Intersection i : theSim.getIntersectionConditions().keySet()) {
            sb.append("[");
            sb.append(i.getID());
            sb.append(": ");
            sb.append(truncateNum(safetyRisk(i, theSim), PRINT_DECIMAL_PLACES));
            sb.append("] ");
        }
        return sb.toString();
    }

    private static double truncateNum(final double val, final int decimalPlaces) {
        double scale = Math.pow(NUMBER_BASE, decimalPlaces);
        return (double) Math.round(val *  scale) / scale;
    }

    // MAY IMPLEMENT HAZARD LIST LATER

//    public static String[] hazardList(Route theRoute, EnvironmentSimulator theSim) {
//        double distance = 0.0;
//        double weatherRisk = 0.0;
//        double trafficRisk = 0.0;
//        double obstacleRisk = 0.0;
//        ArrayList<String> result = new ArrayList<>();
//        Intersection[] routePath = theRoute.getRoute();
//        Intersection from = routePath[0];
//        Intersection to = routePath[1];
//        Road r = CityMap.getRoad(from, to);
//        if (r == null) {
//            return new String[]{"Invalid Route"};
//        }
//        CardinalDirection currentDir = r.getDirection(from);
//        for (int i = 1; i < routePath.length; i++) {
//            from = routePath[i-1];
//            to = routePath[i];
//            r = CityMap.getRoad(from, to);
//            if (r == null) {
//                return new String[]{"Invalid Route"};
//            }
//            CardinalDirection newDir = r.getDirection(from);
//            Direction theDirection = CardinalDirection.turnDirection(currentDir, newDir);
//
//        }
//    }
//
//    private static String severity(double theDouble) {
//        if (theDouble < .33) {
//            return "Low";
//        }
//        if (theDouble < .66) {
//            return "Mild";
//        }
//        return "Severe";
//    }
}
