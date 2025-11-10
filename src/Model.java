import Simulation.*;
import Routing.*;
import Map.*;

import java.util.Random;

public class Model {
    private EnvironmentSimulator mySim;
    private CityMap myMap;
    private RouteManager myRouteManager;
//    private final long RNG_SEED = 445;

    public Model() {}

    public Model(CityMap theMap, EnvironmentSimulator theSim) {
        this.myMap = theMap;
        this.mySim = theSim;
        myRouteManager = new RouteManager(myMap, mySim);
        System.out.println("CAR System Initialized");
    }

    public CityMap getMap() {
        return myMap;
    }

    public EnvironmentSimulator getEnvironment() {
        return mySim;
    }

    // methods for generating routes
    // methods for loading/creating environments

    public boolean isValidRouteParam(Intersection inter1, Intersection inter2) {
        return inter1.isLocation() && inter2.isLocation();
    }

    public Route[] computeRoute(Intersection theStart, Intersection theEnd, double theRate, int rateLimiter) {
        return myRouteManager.getBestRoutes(theStart, theEnd, theRate,rateLimiter);
    }

    public Route[] computeRoute(Intersection theStart, Intersection theEnd) {
        if (isValidRouteParam(theStart, theEnd)) {
            return myRouteManager.getBestRoutes(theStart, theEnd, 0.05, Integer.MAX_VALUE);
        }
        return null;
    }

    public Route[] computeRoute(Intersection theStart, Intersection theEnd, double theRate) {
        return myRouteManager.getBestRoutes(theStart, theEnd, theRate, Integer.MAX_VALUE);
    }

    public Route[] computeRoute(Intersection theStart, Intersection theEnd, int rateLimiter) {
        return myRouteManager.getBestRoutes(theStart, theEnd, 0.05, Integer.MAX_VALUE);
    }

    public Route[] computeRoute(Intersection theStart, Intersection theEnd, double theRate, int rateLimiter, double maxThreshold) {
        return myRouteManager.getBestRoutes(theStart, theEnd, theRate, rateLimiter, maxThreshold);
    }

    public double routeSafety(Route theRoute) {
        return SafetyChecker.routeSafety(theRoute, mySim);
    }

    public double routeTime(Route theRoute) {
        return myRouteManager.routeLength(theRoute, mySim);
    }

    public void generateRandomSimulation() {
        Random rand = new Random();
        helpLoadSimulation(new EnvironmentSimulator(myMap, rand.nextLong()));
    }

    public boolean loadSimulation(EnvironmentSimulator theSim) {
        if (theSim.compareMap(myMap)) {
            helpLoadSimulation(theSim);
            return true;
        }
        return false;
    }

    public void loadSimFromSeed(Long theRNGSeed) {
        helpLoadSimulation(new EnvironmentSimulator(myMap, theRNGSeed));
    }

    // methods for loading/returning map data

    public Intersection[] getIntersectionList() {
        return myMap.getAllIntersections();
    }

    public Road[] getRoadList() {
        return myMap.getAllRoads();
    }

    public void loadMap(CityMap theMap) {
        if (!mySim.compareMap(theMap)) {
            helpLoadMap(theMap);
        } else {
            helpLoadMap(theMap);
            generateRandomSimulation();
        }
    }

    private void helpLoadMap(CityMap theMap) {
        myMap = theMap;
        myRouteManager = new RouteManager(myMap, mySim);
    }

    private void helpLoadSimulation(EnvironmentSimulator theSim) {
        mySim = theSim;
        myRouteManager.setSimulation(theSim);
    }
}
