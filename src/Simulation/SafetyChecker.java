package Simulation;

import Map.CityMap;
import Map.Intersection;
import Map.Road;

import Routing.Route;

/**
 * Safety evaluation model for routes. Contains a series of static methods that determine the safety risk associated
 * with an intersection, road, or route given the passed EnvironmentSimulator instance. Different conditions are
 * weighted differently. For example, our current implementation dictates that obstacles and roadblocks provide
 * a greater risk than poor weather. The constants are also tweak-able, we can tweak the weight of certain conditions.
 * Current obstacles weight is high, so it's factored into safety and time evaluation more than weather.
 *
 * @author June Flores
 * @version 11/15/25
 */
public class SafetyChecker {
    /**
     * A scalar that determines the exponential power of the road's simulated time. If this is raised, then
     * the increase to a road's simulated time increases much faster when the condition severity is scaled up.
     */
    private static final double CONDITION_SCALAR = 1.5;
    /**
     * The proportional weight of the weather's contribution to road time increases
     */
    private static final double WEATHER_TIME_WEIGHT = 0.2;
    /**
     * The proportional weight of the traffic's contribution to road time increases
     */
    private static final double TRAFFIC_TIME_WEIGHT = 0.4;
    /**
     * The proportional weight of the roadblock's contribution to road time increases
     */
    private static final double OBSTACLE_TIME_WEIGHT = 0.4;
    /**
     * The proportional weight of the weather's contribution to the safety risk.
     */
    private static final double WEATHER_SAFETY_WEIGHT = 0.2;
    /**
     * The proportional weight of the traffic's contribution to safety risk.
     */
    private static final double TRAFFIC_SAFETY_WEIGHT = 0.3;
    /**
     * The proportional weight of the obstacle's contribution to safety risk.
     */
    private static final double OBSTACLE_SAFETY_WEIGHT = 0.5;
    /**
     * The decimal rounding when we print values.
     */
    private static final int PRINT_DECIMAL_PLACES = 3;
    /**
     * When printing numbers, we use the decimal system, or base-10.
     */
    private static final int NUMBER_BASE = 10;

    /**
     * Default constructor for the SafetyChecker. All methods and fields are static, so this doesn't need to be
     * initialized.
     */
    public SafetyChecker() {}

    /**
     * Calculates the safety risk of a road according to its conditions in the simulator.
     * If the intersection isn't in the simulation it's risk level is 0%
     *
     * @param theIntersection The intersection whose safety is evaluated.
     * @param theSim The simulation where the intersection's condition is held.
     * @return a double between 0-1 representing the safety risk of the intersection.
     */
    public static double safetyRisk(final Intersection theIntersection, final EnvironmentSimulator theSim) {
        Conditions cond = theSim.getCondition(theIntersection);
        return cond.getObstacleSeverity() * OBSTACLE_SAFETY_WEIGHT + cond.getWeatherFactor() * WEATHER_SAFETY_WEIGHT
                + cond.getTrafficDensity() * TRAFFIC_SAFETY_WEIGHT;
    }

    /**
     * Calculates the safety risk of a road according to its conditions in the simulator.
     * If the road isn't in the simulation it's risk level is 0%
     *
     * @param theRoad The road whose safety is evaluated.
     * @param theSim The simulation where the road's condition is held.
     * @return a double between 0-1 representing the safety risk of the road.
     */
    public static double safetyRisk(final Road theRoad, final EnvironmentSimulator theSim) {
        Conditions cond = theSim.getCondition(theRoad);
        return cond.getObstacleSeverity() * OBSTACLE_SAFETY_WEIGHT + cond.getWeatherFactor() * WEATHER_SAFETY_WEIGHT
                + cond.getTrafficDensity() * TRAFFIC_SAFETY_WEIGHT;
    }

    /**
     * Calculates the length of the road with respect to the conditions of it in the simulator.
     * If the road isn't in the simulation it returns the default speed of the road.
     *
     * @param theRoad The road whose time to drive through is evaluated.
     * @param theSim The simulation where the road's condition is held.
     * @return the road's time with the conditions applied to it.
     */
    public static double roadTime(final Road theRoad, final EnvironmentSimulator theSim) {
        Conditions roadCon = theSim.getCondition(theRoad);
        double timeMultiplier = roadCon.getObstacleSeverity() * OBSTACLE_TIME_WEIGHT
                + roadCon.getTrafficDensity() * TRAFFIC_TIME_WEIGHT + roadCon.getWeatherFactor() * WEATHER_TIME_WEIGHT;
        return theRoad.getDefaultTime() * Math.exp(CONDITION_SCALAR * timeMultiplier);
    }

    /**
     * Calculates the safety risk of a route according to the environment simulation it takes place in.
     * The safety of the route is a function of the highest risk intersection or road in the route.
     *
     * @param theRoute The route whose safety will be evaluated
     * @param theSim The simulated environment where the route will be evaluated according to.
     * @return the safety risk of the route, or the risk of the route's least safe area.
     */
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

    /**
     * Gives the string representation of a map's simulation, which is the intersections and their safety risk.
     *
     * @param theSim the simulation who will be printed.
     * @return The string representation of a map and its simulation.
     */
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

    /**
     * Rounds a decimal to the amount of decimal places specified.
     *
     * @param theVal The decimal that will be rounded.
     * @param theDecimalPlaces The decimal place of the rounded number.
     * @return The decimal rounded to the decimal place specified.
     */
    private static double truncateNum(final double theVal, final int theDecimalPlaces) {
        double scale = Math.pow(NUMBER_BASE, theDecimalPlaces);
        return (double) Math.round(theVal *  scale) / scale;
    }
}
