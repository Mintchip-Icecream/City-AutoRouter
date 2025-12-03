package Routing;

import java.util.ArrayList;

import Map.CityMap;
import Map.Intersection;
import Map.Road;

import Simulation.EnvironmentSimulator;
import Simulation.SafetyChecker;

/**
 * Manager class for creating and processing routes within a certain map and environment simulation.
 *
 * @author June Flores
 * @version 11/30/25
 */
public class RouteManager {
    /**
     * The object that creates routes for the manager.
     */
    private final Router myRouter;
    /**
     * The environmental simulator object that adds environment conditions for roads.
     */
    private EnvironmentSimulator mySim;
    /**
     * The map that contains the intersections in each route.
     */
    private final CityMap myMap;

    /**
     * Constructs a RouteManager to create, measure, load, and apply simulations to routes.
     *
     * @param theMap The map where the routes take place.
     * @param theSim The environmental simulation containing conditions of routes.
     */
    public RouteManager(final CityMap theMap, final EnvironmentSimulator theSim) {
        this.myMap = theMap;
        this.myRouter = new Router();
        this.mySim = theSim;
    }

    /**
     * Reconstructs a route object using an array of intersection IDs, the map object that the intersections of the
     * route are a part of.
     *
     * @param theIntersectionList The integer array of intersection IDs, which represent an intersection on the map.
     * @param theMap The map the route takes place in.
     * @return a route object on the map made up of our intersection list.
     */
    public Route loadRoute(int[] theIntersectionList, final CityMap theMap) {
        return new Route(theIntersectionList, theMap);
    }

    /**
     * Loads a simulation into the route manager. When generating routes, this simulation will contain the conditions
     * applied to each road and intersection in the map. If the simulator doesn't use the map this class uses,
     * some routes may have unreasonably low lengths and risks.
     *
     * @param theSim The environmental simulation object.
     */
    public void setSimulation(final EnvironmentSimulator theSim) {
        this.mySim = theSim;
    }

    /**
     * Generates a list of routes on a map. The safest route is always the first route in the list. The route list
     * will be empty if there is no route found below 100% risk level. While the route manager incrementally generates
     * routes, if the routes are already in the list they will not be added to the result.
     *
     * @param theStart The starting location of each route.
     * @param theEnd The destination of each route, assumes that this intersection is reachable by the start.
     * @param theRate A double between 0-1 that the safety threshold increases by. A small threshold like 0.001
     *                may lead to more routes being found but would take 1000 generations to reach 100% risk.
     * @param rateLimiter The amount of routes returned by the method/
     * @return an array of unique and valid routes from the start to the end. Empty if none is found.
     */
    public Route[] getBestRoutes(final Intersection theStart, final Intersection theEnd,
                                 final double theRate, final int rateLimiter) {
        ArrayList<Route> results = new ArrayList<>();
        // starting with a negative because we'll increment at the start in case threshold goes over 1.0
        double threshold = 0 - theRate;
        Route prevRoute = null;
        if (theStart.equals(theEnd)) {
            throw new IllegalArgumentException("Start and end have to be different");
        }
        while (results.size() < rateLimiter && threshold <= 1.0) {
            threshold += theRate;
            Route theResult = myRouter.computeRoute(theStart, theEnd, threshold, mySim);
            if (theResult != null && !theResult.equals(prevRoute)) {
                prevRoute = theResult;
                results.add(theResult);
            }
        }
        return results.toArray(new Route[0]);
    }

    /**
     * Generates a list of routes on a map below the given minimum threshold. Safest route is always first. The list
     * will be empty if there is no route found below 100% risk level. While the route manager incrementally generates
     * routes, if the routes are already in the list they will not be added to the result.
     *
     * @param theStart The starting location of each route.
     * @param theEnd The destination of each route, assumes that this intersection is reachable by the start.
     * @param theRate A double between 0-1 that the safety threshold increases by. A small threshold like 0.001
     *                may lead to more routes being found but would take 1000 generations to reach 100% risk.
     * @param rateLimiter The amount of routes returned by the method.
     * @param maxThreshold The maximum acceptable risk of a route.
     * @return an array of unique and valid routes from the start to the end that are below the safety threshold.
     */
    public Route[] getBestRoutes(final Intersection theStart, final Intersection theEnd, final double theRate,
                                 final int rateLimiter, final double maxThreshold) {
        ArrayList<Route> results = new ArrayList<>();
        double threshold = 0 - theRate;
        Route prevRoute = null;
        if (theStart.equals(theEnd)) {
            throw new IllegalArgumentException("Start and end have to be different");
        }
        while (results.size() < rateLimiter && threshold <= maxThreshold) {
            threshold += theRate;
            Route theResult = myRouter.computeRoute(theStart, theEnd, threshold, mySim);
            if (theResult != null && !theResult.equals(prevRoute)) {
                prevRoute = theResult;
                results.add(theResult);
            }
        }
        return results.toArray(new Route[0]);
    }

    /**
     * Returns the length of a route, disregarding the environment simulation. Assumes car is driving as fast
     * as it can, and disregards the length of time for a turn.
     *
     * @param theRoute The route that we want the length of. Assumes the route is in the map of the routeManager.
     * @return The length of the route in minutes without any conditions applied to it.
     */
    public double routeLength(final Route theRoute) {
        double result = 0;
        int[] routePath = theRoute.getRouteIDs();
        for (int i = 1; i < routePath.length; i++) {
            Road r = CityMap.getRoad(myMap.getIntersection(routePath[i - 1]), myMap.getIntersection(routePath[i]));
            if (r == null) {
                return 0;
            }
            double time = r.getDefaultTime();
            result += time;
        }
        return result;
    }

    /**
     * Returns the length of a route with the environmental simulation's conditions applied to each road.
     * The conditions of a road slow it down by some percentage, and the route is evaluated with those slow-downs
     * considered.
     *
     * @param theRoute The route we want the length of. Assumes the route is in the map of the routeManager.
     * @param theSim The simulation we want to apply the route to.
     * @return The length of the route in minutes with the environment conditions considered.
     */
    public double routeLength(final Route theRoute, final EnvironmentSimulator theSim) {
        double result = 0;
        int[] routePath = theRoute.getRouteIDs();
        for (int i = 1; i < routePath.length; i++) {
            Road r = CityMap.getRoad(myMap.getIntersection(routePath[i - 1]), myMap.getIntersection(routePath[i]));
            double time = SafetyChecker.roadTime(r, theSim);
            result += time;
        }
        return result;
    }

}
