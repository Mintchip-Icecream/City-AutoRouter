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
 * @version 11/15/25
 */
public class RouteManager {
    private final Router myRouter;
    private EnvironmentSimulator mySim;
    private final CityMap myMap;

    public RouteManager(final CityMap theMap, final EnvironmentSimulator theSim) {
        this.myMap = theMap;
        this.myRouter = new Router();
        this.mySim = theSim;
    }

    public void setSimulation(final EnvironmentSimulator theSim) {
        this.mySim = theSim;
    }

    public Route[] getBestRoutes(final Intersection theStart, final Intersection theEnd,
                                 final double theRate, final int rateLimiter) {
        ArrayList<Route> results = new ArrayList<>();
        // starting with a negative because we'll increment at the start in case threshold goes over 1.0
        double threshold = 0 - theRate;
        Route prevRoute = null;
        while (results.size() <= rateLimiter && threshold < 1.0) {
            threshold += theRate;
            Route theResult = myRouter.computeRoute(theStart, theEnd, threshold, mySim);
            if (theResult != null && !theResult.equals(prevRoute)) {
                prevRoute = theResult;
                results.add(theResult);
            }
        }
        return results.toArray(new Route[0]);
    }

    public Route[] getBestRoutes(final Intersection theStart, final Intersection theEnd, final double theRate,
                                 final int rateLimiter, final double minThreshold) {
        ArrayList<Route> results = new ArrayList<>();
        double threshold = 0 - theRate;
        Route prevRoute = null;
        while (results.size() <= rateLimiter && threshold < minThreshold) {
            threshold += theRate;
            Route theResult = myRouter.computeRoute(theStart, theEnd, threshold, mySim);
            if (theResult != null && !theResult.equals(prevRoute)) {
                prevRoute = theResult;
                results.add(theResult);
            }
        }
        if (results.isEmpty()) {
            return null;
        }
        return results.toArray(new Route[0]);
    }


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
