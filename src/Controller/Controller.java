package Controller;

import Map.CityMap;
import Map.Intersection;
import Map.Road;

import Routing.Route;
import Routing.RouteManager;

import Simulation.EnvironmentSimulator;
import Simulation.SafetyChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

/**
 * Controller class for accessing the business logic of the City-AutoRouter system.
 *
 * @author June Flores
 * @version 11/15/25
 */
public class Controller {
    /**
     * The default rate that the safety threshold of routes is computed. When calling the function, we compute
     * the optimal route 20 times with different thresholds up to 100%
     */
    private static final double DEFAULT_THRESHOLD_RATE = 0.05;
    /**
     * The current simulation of the CAR system.
     */
    private EnvironmentSimulator mySim;
    /**
     * The current loaded map of the CAR system.
     */
    private CityMap myMap;
    /**
     * The current route manager of the CAR system.
     */
    private RouteManager myRouteManager;
    /**
     * The database manager and operations class instance of the CAR system.
     */
    private DBOps myDB;
    /**
     * The id of the currently loaded map, used for saving routes and simulations.
     */
    private int myMapID;

    /**
     * Initializes the CAR backend with a default map, and a random simulation.
     *
     * @throws SQLException If connection to the database fails
     */
    public Controller() throws SQLException {
        Random rand = new Random();
        myDB = DBOps.getInstance();
        myMapID = CityMap.getDefaultMapID();
        myMap = new CityMap();
        mySim = new EnvironmentSimulator(myMap, rand.nextLong());
        myRouteManager = new RouteManager(myMap, mySim);
    }

    /**
     * Initializes a determined controller, with a map corresponding to a map from the database, and a simulation
     * from the database too.
     *
     * @param theMapID The ID of the map instance in the database.
     * @param theSimID The ID of the simulation in the database.
     * @throws SQLException If SQL connection fails while loading the map or simulation.
     */
    public Controller(final int theMapID, final int theSimID) throws SQLException{
        this.myMap = new CityMap(theMapID);
        this.mySim = new EnvironmentSimulator(theSimID, myMap);
        myRouteManager = new RouteManager(myMap, mySim);
        DBOps myDB = DBOps.getInstance();
        System.out.println("CAR System Initialized");

//        this.myUI = new RouterGUI();
//        this.myUI.setCityMap(theMap);
//        this.myUI.setVisible(true);
    }

    /**
     * Processes and saves a map in the txt file from the path specified.
     *
     * @param theFileName the file path string of the map we want to save.
     * @throws IOException if the file path is invalid.
     */
    public void saveMap(String theFileName) throws IOException {
        String readFile = Files.readString(Path.of(theFileName));
        CityMap cm = myDB.saveMap(readFile, theFileName);
        if (cm != null) {
            loadMap(cm);
        }
    }

    /**
     * Loads a map from the database according to the passed ID
     *
     * @param theMapID the row ID of the map in the database.
     */
    public void loadMap(int theMapID) {
        CityMap newMap = new CityMap(theMapID);
        if (!(newMap.getAllIntersections().length == 0)) {
            myMapID = theMapID;
            loadMap(newMap);
        }
    }

    /**
     * Returns all the saved routes in the database as their integer IDs. Only returns the list of their IDs and
     * an array of the first and last locationIDs, in the order of when they were last used.
     *
     * @return an ordered map of routeIDs and the first and last intersection in them.
     */
    public Map<Integer, int[]> getRoutes() {
        return myDB.getRoutes();
    }

    /**
     * Returns all the simulations in the database as their integer IDs and the time they were last used. Ordered by
     * when they were last used.
     *
     * @return an ordered map of the simIDs and the time they were used.
     */
    public Map<Integer, String> getSimulations() {
        return myDB.getSimulations();
    }

    /**
     * Return all of the maps saved in the database as their integer IDs and the name of the map. Ordered by
     * when they were saved (recently saved maps first).
     *
     * @return an ordered map of the simIDs and the time they were used.
     */
    public Map<Integer, String> getMaps() {
        return myDB.getMaps();
    }

    /**
     * Saves the current simulation of the system into the database.
     */
    public void saveSim() {
        myDB.saveSim(mySim, myMapID);
    }

    /**
     * Loads a simulation from the database into the system.
     * @param theSimID the simID of the simulation we want to load.
     */
    public void loadSim(final int theSimID) {
        loadSimulation(new EnvironmentSimulator(theSimID, myMap));
    }

    /**
     * Saves a route into the system.
     * @param theRoute the route that will be saved.
     */
    public void saveRoute(final Route theRoute) {
        myDB.saveRoute(theRoute, myMapID);
    }

    /**
     * Loads a route from the database into the system.
     *
     * @param theRouteID the routeID of the route we want to load.
     * @return the route instance of the loaded route.
     */
    public Route loadRoute(int theRouteID) {
        int[] interIDList = myDB.loadRoute(theRouteID);
        return myRouteManager.loadRoute(interIDList, myMap);
    }

    /**
     * Returns the current map of the CAR system.
     *
     * @return the current CityMap of the CAR system.
     */
    public CityMap getMap() {
        return myMap;
    }

    /**
     * Returns the current simulation of the CAR system.
     *
     * @return the current simulation of the CAR system.
     */
    public EnvironmentSimulator getEnvironment() {
        return mySim;
    }

    /**
     * Checks if the intersections are locations so that it can compute the routes.
     *
     * @param inter1 the first intersection of the route.
     * @param inter2 the last intersection of the route.
     * @return true if the intersections are locations, otherwise false.
     */
    public boolean isValidRouteParam(final Intersection inter1, final Intersection inter2) {
        return inter1.isLocation() && inter2.isLocation();
    }

    /**
     * Computes the optimal routes from the start location to the end location.
     *
     * @param theStart The location of the start of the map.
     * @param theEnd The location of the end of the map.
     * @param theRate The rate of increasing the safety threshold of the routes.
     * @param theLimiter The amount of routes that will be computed. If we reach the limit we'll stop computing routes.
     * @return An array of the optimal routes from the start to the end location. Null if start and end not locations.
     */
    public Route[] computeRoute(final Intersection theStart, final Intersection theEnd,
                                final double theRate, final int theLimiter) {
        if (isValidRouteParam(theStart, theEnd)) {
            return myRouteManager.getBestRoutes(theStart, theEnd, theRate, theLimiter);
        }
        return null;
    }

    /**
     * Computes the optimal routes from the start location to the end location. No limit on the amount of routes
     * computed, and the route manager will always calculate 20 times to get the routes.
     *
     * @param theStart The location of the start of the map.
     * @param theEnd The location of the end of the map.
     * @return An array of the optimal routes from the start to the end location. Null if start and end not locations.
     */
    public Route[] computeRoute(final Intersection theStart, final Intersection theEnd) {
        if (isValidRouteParam(theStart, theEnd)) {
            return myRouteManager.getBestRoutes(theStart, theEnd, DEFAULT_THRESHOLD_RATE, Integer.MAX_VALUE);
        }
        return null;
    }

    /**
     * Checks the safety of the route passed according to our current simulation.
     *
     * @param theRoute The route whose safety will be evaluated.
     * @return the safety risk of the route passed according to our current simulation.
     */
    public double routeSafety(final Route theRoute) {
        return SafetyChecker.routeSafety(theRoute, mySim);
    }

    /**
     * Returns the safety risk of an intersection according to the current simulation.
     *
     * @param theIntersection the intersection whose safety we want to check
     * @return the safety risk of the intersection.
     */
    public double getSafety(final Intersection theIntersection) {
        return SafetyChecker.safetyRisk(theIntersection, mySim);
    }

    /**
     * Returns the safety risk of a road according to the current simulation.
     *
     * @param theRoad the road whose safety we want to check
     * @return the safety risk of the road.
     */
    public double getSafety(final Road theRoad) {
        return SafetyChecker.safetyRisk(theRoad, mySim);
    }

    /**
     * Calculates the time of the route passed according to our current simulation.
     *
     * @param theRoute The route whose time will be evaluated.
     * @return the time of the route passed according to our current simulation.
     */
    public double routeTime(final Route theRoute) {
        return myRouteManager.routeLength(theRoute, mySim);
    }

    /**
     * Generates and loads a new environment simulation into the system.
     */
    public void generateRandomSimulation() {
        Random rand = new Random();
        helpLoadSimulation(new EnvironmentSimulator(myMap, rand.nextLong()));
    }

    /**
     * Generates and loads a new environment simulation into the system using the seed for random number generation.
     */
    public void generateSimulationFromSeed(long theLong) {
        helpLoadSimulation(new EnvironmentSimulator(myMap, theLong));
    }

    /**
     * Loads the simulation into the system while checking if the simulation and the map are compatible.
     *
     * @param theSim the simulation that will be loaded.
     */
    public void loadSimulation(final EnvironmentSimulator theSim) {
        if (theSim.compareMap(myMap)) {
            helpLoadSimulation(theSim);
        }
    }

    // methods for loading/returning map data

    /**
     * Gets the intersections of the current map.
     *
     * @return the intersection list of the current map.
     */
    public Intersection[] getIntersectionList() {
        return myMap.getAllIntersections();
    }

    /**
     * Gets the roads of the current map.
     *
     * @return the road list of the current map.
     */
    public Road[] getRoadList() {
        return myMap.getAllRoads();
    }

    /**
     * Loads a map into the system, if the map is incompatible with the simulator, a new one is generated.
     *
     * @param theMap The map that will be loaded into the system.
     */
    public void loadMap(final CityMap theMap) {
        if (!mySim.compareMap(theMap)) {
            helpLoadMap(theMap);
        } else {
            helpLoadMap(theMap);
            generateRandomSimulation();
        }
    }

    /**
     * Handles setting the current map to the map passed, also changes the route manager to our current map.
     *
     * @param theMap the map we'll load into the system.
     */
    private void helpLoadMap(final CityMap theMap) {
        myMap = theMap;
        myRouteManager = new RouteManager(myMap, mySim);
    }

    /**
     * Handles loading the simulation into the system, sets the simulation to the route manager too.
     *
     * @param theSim the simulation we'll load into the system.
     */
    private void helpLoadSimulation(final EnvironmentSimulator theSim) {
        mySim = theSim;
        myRouteManager.setSimulation(theSim);
    }
}
