package Routing;

import Map.CityMap;
import Map.Intersection;
import Map.Road;
import Simulation.EnvironmentSimulator;
import Simulation.SafetyChecker;

import java.util.ArrayList;

public class RouteManager {
    private final Router myRouter;
    private EnvironmentSimulator mySim;
    private final CityMap myMap;

    public RouteManager(CityMap theMap, EnvironmentSimulator theSim) {
        this.myMap = theMap;
        this.myRouter = new Router(theMap);
        this.mySim = theSim;
    }

    public void setSimulation(EnvironmentSimulator theSim) {
        this.mySim = theSim;
    }

    public Route[] getBestRoutes(Intersection theStart, Intersection theEnd, double theRate, int rateLimiter) {
        ArrayList<Route> results = new ArrayList<>();
        // starting with a negative because we'll increment at the start in case threshold goes over 1.0
        double threshold = 0-theRate;
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

    public Route[] getBestRoutes(Intersection theStart, Intersection theEnd, double theRate, int rateLimiter, double minThreshold) {
        ArrayList<Route> results = new ArrayList<>();
        double threshold = 0-theRate;
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


    public double routeLength(Route theRoute) {
        double result = 0;
        int[] routePath = theRoute.getRouteIDs();
        for (int i = 1; i < routePath.length; i++) {
            Road r = CityMap.getRoad(myMap.getIntersection(routePath[i-1]), myMap.getIntersection(routePath[i]));
            if (r == null) {
                return 0;
            }
            double time = r.getDefaultTime();
            result += time;
        }
        return (double) Math.round(result*100)/100;
    }

    public double routeLength(Route theRoute, EnvironmentSimulator theSim) {
        double result = 0;
        int[] routePath = theRoute.getRouteIDs();
        for (int i = 1; i < routePath.length; i++) {
            Road r = CityMap.getRoad(myMap.getIntersection(routePath[i-1]), myMap.getIntersection(routePath[i]));
            double time = SafetyChecker.roadTime(r, theSim);
            result += time;
        }
        return (double) Math.round(result*100)/100;
    }

}
