
import Map.CityMap;
import Map.Intersection;
import Map.Road;

import Routing.Route;
import Routing.RouteManager;

import Simulation.EnvironmentSimulator;
import Simulation.SafetyChecker;

import java.util.Random;

/**
 * Controller class for accessing the business logic of the City-AutoRouter system.
 *
 * @author June Flores
 * @version 11/15/25
 */
public class Controller {
    private static final double DEFAULT_THRESHOLD_RATE = 0.05;
    private EnvironmentSimulator mySim;
    private CityMap myMap;
    private RouteManager myRouteManager;
//    private final long RNG_SEED = 445;

    public Controller() { }

    public Controller(final CityMap theMap, final EnvironmentSimulator theSim) {
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

    public boolean isValidRouteParam(final Intersection inter1, final Intersection inter2) {
        return inter1.isLocation() && inter2.isLocation();
    }

    public Route[] computeRoute(final Intersection theStart, final Intersection theEnd,
                                final double theRate, final int rateLimiter) {
        return myRouteManager.getBestRoutes(theStart, theEnd, theRate, rateLimiter);
    }

    public Route[] computeRoute(final Intersection theStart, final Intersection theEnd) {
        if (isValidRouteParam(theStart, theEnd)) {
            return myRouteManager.getBestRoutes(theStart, theEnd, DEFAULT_THRESHOLD_RATE, Integer.MAX_VALUE);
        }
        return null;
    }

    public Route[] computeRoute(final Intersection theStart, final Intersection theEnd, final double theRate) {
        return myRouteManager.getBestRoutes(theStart, theEnd, theRate, Integer.MAX_VALUE);
    }

    public Route[] computeRoute(final Intersection theStart, final Intersection theEnd, final int rateLimiter) {
        return myRouteManager.getBestRoutes(theStart, theEnd, DEFAULT_THRESHOLD_RATE, Integer.MAX_VALUE);
    }

    public Route[] computeRoute(final Intersection theStart, final Intersection theEnd, final double theRate,
                                final int rateLimiter, final double maxThreshold) {
        return myRouteManager.getBestRoutes(theStart, theEnd, theRate, rateLimiter, maxThreshold);
    }

    public double routeSafety(final Route theRoute) {
        return SafetyChecker.routeSafety(theRoute, mySim);
    }

    public double routeTime(final Route theRoute) {
        return myRouteManager.routeLength(theRoute, mySim);
    }

    public void generateRandomSimulation() {
        Random rand = new Random();
        helpLoadSimulation(new EnvironmentSimulator(myMap, rand.nextLong()));
    }

    public boolean loadSimulation(final EnvironmentSimulator theSim) {
        if (theSim.compareMap(myMap)) {
            helpLoadSimulation(theSim);
            return true;
        }
        return false;
    }

    public void loadSimFromSeed(final Long theRNGSeed) {
        helpLoadSimulation(new EnvironmentSimulator(myMap, theRNGSeed));
    }

    // methods for loading/returning map data

    public Intersection[] getIntersectionList() {
        return myMap.getAllIntersections();
    }

    public Road[] getRoadList() {
        return myMap.getAllRoads();
    }

    public void loadMap(final CityMap theMap) {
        if (!mySim.compareMap(theMap)) {
            helpLoadMap(theMap);
        } else {
            helpLoadMap(theMap);
            generateRandomSimulation();
        }
    }

    private void helpLoadMap(final CityMap theMap) {
        myMap = theMap;
        myRouteManager = new RouteManager(myMap, mySim);
    }

    private void helpLoadSimulation(final EnvironmentSimulator theSim) {
        mySim = theSim;
        myRouteManager.setSimulation(theSim);
    }
}
