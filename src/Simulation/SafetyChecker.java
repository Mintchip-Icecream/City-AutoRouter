package Simulation;

import Map.*;
import Routing.Route;

import java.util.ArrayList;

public class SafetyChecker {
    private static final double CONDITION_SCALAR = 1.5;
    private static final double WEATHER_TIME_WEIGHT = 0.2;
    private static final double TRAFFIC_TIME_WEIGHT = 0.4;
    private static final double OBSTACLE_TIME_WEIGHT = 0.4;
    private static final double WEATHER_SAFETY_WEIGHT = 0.2;
    private static final double TRAFFIC_SAFETY_WEIGHT = 0.3;
    private static final double OBSTACLE_SAFETY_WEIGHT = 0.5;

    public SafetyChecker() {
    }

    public static double safetyRisk(Intersection theIntersection, EnvironmentSimulator theSim) {
        Conditions cond = theSim.getCondition(theIntersection);
        return cond.getObstacleSeverity() * OBSTACLE_SAFETY_WEIGHT + cond.getWeatherFactor() * WEATHER_SAFETY_WEIGHT
                + cond.getTrafficDensity() * TRAFFIC_SAFETY_WEIGHT;
    }

    public static double safetyRisk(Road theRoad, EnvironmentSimulator theSim) {
        Conditions cond = theSim.getCondition(theRoad);
        return cond.getObstacleSeverity() * OBSTACLE_SAFETY_WEIGHT + cond.getWeatherFactor() * WEATHER_SAFETY_WEIGHT
                + cond.getTrafficDensity() * TRAFFIC_SAFETY_WEIGHT;
    }

    public static double roadTime(Road theRoad, EnvironmentSimulator theSim) {
        Conditions roadCon = theSim.getCondition(theRoad);
        double trafficMultiplier = roadCon.getObstacleSeverity() * OBSTACLE_TIME_WEIGHT
                + roadCon.getTrafficDensity() * TRAFFIC_TIME_WEIGHT + roadCon.getWeatherFactor() * WEATHER_TIME_WEIGHT;
        return theRoad.getDefaultTime() * Math.exp(CONDITION_SCALAR * trafficMultiplier);
    }

    // maxRouteSafety
    public static double routeSafety(Route theRoute, EnvironmentSimulator theSim) {
        double maxRouteSafety = 0.0;
        Intersection[] routePath = theRoute.getRoute();
        Intersection from;
        Intersection to = routePath[1];
        for (int i = 1; i < routePath.length; i++) {
            from = routePath[i-1];
            to = routePath[i];
            Road r = CityMap.getRoad(from, to);
            double fromRisk = safetyRisk(from, theSim);
            double roadRisk = safetyRisk(r, theSim);
            maxRouteSafety = Math.max(maxRouteSafety, Math.max(fromRisk, roadRisk));
        }
        maxRouteSafety = Math.max(maxRouteSafety, SafetyChecker.safetyRisk(to, theSim));
        return maxRouteSafety;
    }

    public static String mapSafety(EnvironmentSimulator theSim) {
        StringBuilder sb = new StringBuilder();
        for (Intersection i : theSim.getIntersectionConditions().keySet()) {
            sb.append("[");
            sb.append(i.getID());
            sb.append(": ");
            sb.append((double) Math.round(safetyRisk(i, theSim)*1000) / 1000);
            sb.append("] ");
        }
        return sb.toString();
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
