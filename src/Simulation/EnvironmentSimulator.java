package Simulation;

import DB.DBOps;
import Map.CardinalDirection;
import Map.CityMap;
import Map.Intersection;
import Map.Road;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Class that applies conditions and risk levels to the intersections and roads of a map. Works by simulating
 * "problem areas" of certain conditions such as weather, obstacles, and traffic, and assigns a set of conditions to
 * each intersection and road on the map.
 *
 * @author June Flores
 * @version 11/15/25
 */
public class EnvironmentSimulator {
    /**
     * A toggleable switch that prints to console the individual conditions of each intersection, along with the
     * epicenters of "problem zones" when constructing the environmental simulation, does not do anything when loading
     * previously saved simulations.
     */
    private static final boolean DEBUG_MODE = false;
    /**
     * The default condition of a road or intersection, with 0 risk in all conditions.
     */
    private static final Conditions DEFAULT_CONDITION = new Conditions(0, 0, 0);
    /**
     * The bound of what's considered "light blockage", a light version of this condition is between 0 and this number.
     */
    private static final double LIGHT_BLOCKAGE = 0.333;
    /**
     * The bound of what's considered "light weather", a light version of this condition is between 0 and this number.
     */
    private static final double LIGHT_WEATHER = 0.333;
    /**
     * The bound of what's considered "light traffic", a light version of this condition is between 0 and this number.
     */
    private static final double LIGHT_TRAFFIC = 0.333;
    /**
     * The minimum percentage of the map that will be hit with problem zones of each condition. No condition
     * will be light for less than 10% of the map.
     */
    private static final double MINIMUM_CONDITION_COVERAGE = 0.1;
    /**
     * The maximum percentage of the map that will be affected with the problems zones of the blockage condition.
     * This is only an approximate percentage, the actual amount affected may be slightly less or more than this.
     */
    private static final double MAX_BLOCKAGE_COVERAGE = 0.5;
    /**
     * The maximum percentage of the map that will be hit with the problems zones of the weather condition.
     * This is only an approximate percentage, the actual amount affected may be slightly less or more than this.
     */
    private static final double MAX_WEATHER_COVERAGE = 0.6;
    /**
     * The maximum percentage of the map that will be hit with the problems zones of the traffic condition.
     * This is only an approximate percentage, the actual amount affected may be slightly less or more than this.
     */
    private static final double MAX_TRAFFIC_COVERAGE = 0.6;
    /**
     * The maximum severity of the epicenter of problem zone. An epicenter's condition will be some number larger than
     * what's considered a "light" severity level, and the severity bound. Even if below 1.0, the severity may increase
     * to 1.0 if problem zones are overlapping.
     */
    private static final double SEVERITY_BOUND = 0.7;
    /**
     * The minimum decay multiplier that's acceptable. A node could be within the radius but have such a low
     * decay multiplier it's almost 0 which is strange behavior, or it could possibly be traversed while being out
     * of radius, which breaks the decay multiplier function by becoming a negative number. So we compare the decay
     * with this minimum and disregard the intersection if below this.
     */
    private static final double MINIMUM_DECAY_MULTIPLIER = 0.005;
    /**
     * When in debug mode, and printing the intersections and their simulated condition, we only print this amount
     * of intersections so that it's more easily readable in the console.
     */
    private static final int OBJECTS_PER_LINE_PRINT = 3;
    /**
     * The value 100, which converts decimals between 0 and 1 to a percent. For example, 0.2 would become 20 percent.
     */
    private static final int DECIMAL_TO_PERCENT = 100;
    /**
     * In our array of distances from the epicenter, the amount that this intersection is North to the epicenter is
     * the first, or 0th index of the array.
     */
    private static final int NORTH_DISTANCE_INDEX = 0;
    /**
     * In our array of distances from the epicenter, the amount that this intersection is South to the epicenter is
     * the second, or 1th index of the array.
     */
    private static final int SOUTH_DISTANCE_INDEX = 1;
    /**
     * In our array of distances from the epicenter, the amount that this intersection is East to the epicenter is
     * the second, or 2th index of the array.
     */
    private static final int EAST_DISTANCE_INDEX = 2;
    /**
     * In our array of distances from the epicenter, the amount that this intersection is West to the epicenter is
     * the second, or 3th index of the array.
     */
    private static final int WEST_DISTANCE_INDEX = 3;
    /**
     * The map object we're simulating the intersections and roads of.
     */
    private final CityMap myMap;
    /**
     * A map containing the conditions corresponding to each intersection after completing the simulation.
     */
    private final HashMap<Intersection, Conditions> myIntersections = new HashMap<>();
    /**
     * A map containing the conditions corresponding to each intersection after completing the simulation.
     */
    private final HashMap<Road, Conditions> myRoads = new HashMap<>();
    /**
     * The random object that's created on simulator instantiation and gives random numbers for simulations.
     */
    private Random myRand;
    /**
     * The seed for the random number generator. When using the same seed, you will get the same procedurally
     * generated environment.
     */
    private long myRNGSeed;

    /**
     * Constructs a new EnvironmentalSimulator using the RNG seed given, and applies conditions to the map.
     *
     * @param theMap The map we'll simulate conditions on.
     * @param theRNGSeed The seed for the simulation's random number generators.
     */
    public EnvironmentSimulator(final CityMap theMap, final long theRNGSeed) {
        this.myMap = theMap;
        this.myRand = new Random(theRNGSeed);
        this.myRNGSeed = theRNGSeed;
        simulateConditions();
    }

    /**
     * Constructs an EnvironmentalSimulator using the simulationID that's an index to a simulation in the database.
     * If the map is larger than the saved sim's mapID, the simulator will use the RNG seed to simulate conditions for
     * the intersections and roads whose conditions aren't saved in the database.
     *
     * @param theSimID The index in the database for the environmental simulator.
     * @param theMap The map we're setting our environmental simulation to.
     */
    public EnvironmentSimulator(final int theSimID, CityMap theMap) {
        this.myMap = theMap;
        DBOps db = DBOps.getInstance();
        ResultSet simTuple;
        ResultSet conditions;
        try {
            simTuple = db.loadSimTuple(theSimID);
            myRNGSeed = simTuple.getInt(2);
            myRand = new Random(myRNGSeed);
            conditions = db.loadSim(theSimID);
            HashMap<Intersection, Double> weatherFactors = new HashMap<>();
            HashMap<Intersection, Double> blockageFactors = new HashMap<>();
            HashMap<Intersection, Double> trafficFactors = new HashMap<>();

            while (conditions.next()) {
                Intersection i = myMap.getIntersection(conditions.getInt(1));
                weatherFactors.put(i, conditions.getDouble(2));
                blockageFactors.put(i, conditions.getDouble(3));
                trafficFactors.put(i, conditions.getDouble(4));
            }
            // randomizes the conditions on remaining intersections in case there's some missing from the DB
            fillOutCondition(weatherFactors, LIGHT_WEATHER);
            fillOutCondition(trafficFactors, LIGHT_TRAFFIC);
            fillOutCondition(blockageFactors, LIGHT_BLOCKAGE);
            setAllConditions(weatherFactors, blockageFactors, trafficFactors);
            System.out.println("Simulation " + theSimID + " Successfully Loaded");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the condition object that corresponds to the given intersection.
     * @param theIntersection the intersection we want the simulated condition of.
     * @return the default condition (0, 0, 0) if not in the simulation, or the condition of the intersection.
     */
    public final Conditions getCondition(final Intersection theIntersection) {
        if (myIntersections.containsKey(theIntersection)) {
            return myIntersections.get(theIntersection);
        }
        return DEFAULT_CONDITION;
    }

    /**
     * Gets the condition object that corresponds to the given road.
     * @param theRoad the road we want the simulated condition of.
     * @return the default condition (0, 0, 0) if not in the simulation, or the condition of the road.
     */
    public final Conditions getCondition(final Road theRoad) {
        if (myRoads.containsKey(theRoad)) {
            return myRoads.get(theRoad);
        }
        return DEFAULT_CONDITION;
    }

    /**
     * Gets the RNG seed of the environmental simulator.
     *
     * @return the RNG seed of the simulator.
     */
    public final long getSeed() {
        return myRNGSeed;
    }

    /**
     * Checks if the map is equal to the map the EnvironmentalSimulator object is initialized other.
     * If a map isn't equal, a lot of intersections and roads will return default conditions.
     *
     * @param theOther The other map object to be compared.
     * @return true if the other map object and the simulator's map are the same, false if they're different.
     */
    public final boolean compareMap(final CityMap theOther) {
        return theOther.equals(myMap);
    }

    /**
     * Returns the whole list of intersections and their conditions in the simulation.
     *
     * @return the map of intersections and their conditions in the simulation.
     */
    public final HashMap<Intersection, Conditions> getIntersectionConditions() {
        return new HashMap<>(myIntersections);
    }

    /**
     * Returns the whole list of roads and their conditions in the simulation.
     *
     * @return the map of roads and their conditions in the simulation.
     */
    public final HashMap<Road, Conditions> getRoadConditions() {
        return new HashMap<>(myRoads);
    }

    /**
     * Creates a condition object and puts it into the object's intersection map with the associated intersection.
     *
     * @param inter1 the intersection we're creating the condition for.
     * @param theWeather the weather risk of the intersection.
     * @param theBlockage the obstacle/blockage risk of the intersection.
     * @param theTraffic the traffic risk of the intersection.
     */
    private void applyCondition(final Intersection inter1, final double theWeather,
                                final double theBlockage, final double theTraffic) {
        Conditions newCon = new Conditions(theWeather, theBlockage, theTraffic);
        myIntersections.put(inter1, newCon);
    }

    /**
     * Creates a condition object and puts it into the object's road condition map with the associated road.
     *
     * @param road1 the road we're creating the condition for.
     * @param theWeather the weather risk of the road.
     * @param theBlockage the obstacle/blockage risk of the road.
     * @param theTraffic the traffic risk of the intersection.
     */
    private void applyCondition(final Road road1, final double theWeather,
                                final double theBlockage, final double theTraffic) {
        Conditions newCon = new Conditions(theWeather, theBlockage, theTraffic);
        myRoads.put(road1, newCon);
    }

    // principle of simulating: we randomly set the radius of effects (so if it's rainy in 1 edge, it should be
    // rainy for a few kilometers more)

    /**
     * Instantiates the conditions of the map. This creates an environment simulation for the whole map.
     * Prints the process of the simulation, the epicenters of problem zones, and the individual conditions of each
     * intersection if DEBUG_MODE is set to true.
     */
    private void simulateConditions() {
        HashMap<Intersection, Double> weatherFactors = new HashMap<>();
        HashMap<Intersection, Double> blockageFactors = new HashMap<>();
        HashMap<Intersection, Double> trafficFactors = new HashMap<>();
        double distance = totalMapDistance(); // we want at last 1/3 of the map to have fairly extreme

        if (DEBUG_MODE) {
            System.out.println("Total Mileage of Map: " + distance);
            System.out.println("\nSimulating Weather...");
        }

        // set the approximate distance we want to be affected by each condition, then run a simulation on the map for it

        // set weather parameters
        double distanceAffectedByWeather = distance * myRand.nextDouble(MINIMUM_CONDITION_COVERAGE, MAX_WEATHER_COVERAGE);
        simulateSingleCondition(distanceAffectedByWeather, LIGHT_WEATHER, SEVERITY_BOUND, weatherFactors);

        if (DEBUG_MODE) {
            System.out.println("Before filling out weather");
            printMap(weatherFactors);
        }
        fillOutCondition(weatherFactors, LIGHT_WEATHER);

        if (DEBUG_MODE) {
            System.out.println("Printing Weather Simulation.Conditions for all locations and intersections:");
            printMap(weatherFactors);
            System.out.println("\nSimulating traffic...");
        }

        // set traffic parameters
        double distanceAffectedByTraffic = distance * myRand.nextDouble(MINIMUM_CONDITION_COVERAGE, MAX_TRAFFIC_COVERAGE);
        simulateSingleCondition(distanceAffectedByTraffic, LIGHT_TRAFFIC, SEVERITY_BOUND, trafficFactors);

        if (DEBUG_MODE) {
            System.out.println("Before filling out traffic");
            printMap(trafficFactors);
        }
        fillOutCondition(trafficFactors, LIGHT_TRAFFIC);

        if (DEBUG_MODE) {
            System.out.println("Printing Traffic Simulation.Conditions for all locations and intersections:");
            printMap(trafficFactors);
            System.out.println("\nSimulating obstacles...");
        }

        double distanceAffectedByObstacles = distance * myRand.nextDouble(MINIMUM_CONDITION_COVERAGE, MAX_BLOCKAGE_COVERAGE);
        simulateSingleCondition(distanceAffectedByObstacles, LIGHT_BLOCKAGE, SEVERITY_BOUND, blockageFactors);
        if (DEBUG_MODE) {
            System.out.println("Before filling out obstacles");
            printMap(blockageFactors);
        }

        fillOutCondition(blockageFactors, LIGHT_BLOCKAGE);

        if (DEBUG_MODE) {
            System.out.println("Printing Obstacle Simulation.Conditions for all locations and intersections:");
            printMap(blockageFactors);
            System.out.println("Simulation Completed!");
        }

        setAllConditions(weatherFactors, blockageFactors, trafficFactors);

    }

    /**
     * Prints to the console the hashmap of a single condition for debugging purposes.
     *
     * @param theConditionMap the hashmap of intersections and their condition risk we want to print.
     */
    private void printMap(final HashMap<Intersection, Double> theConditionMap) {
        int counter = 0;
        for (Intersection i: myMap.getAllIntersections()) {
            if (theConditionMap.get(i) == null) {
                continue;
            }
            if ((counter % OBJECTS_PER_LINE_PRINT) == 0) {
                System.out.println();
            } else {
                System.out.print(" ");
            }
            System.out.print("(" + i.getID() + ", " + (Math.round(theConditionMap.get(i) * DECIMAL_TO_PERCENT)) + "%)");
            counter++;
        }
        System.out.println("\n");
    }


    /**
     * Calculates the total distance of the map as a function of adding up the distances of all the roads in the map.
     *
     * @return the total distance of the map by all the roads in it.
     */
    private double totalMapDistance() {
        double result = 0;
        for (Road r: myMap.getAllRoads()) {
            result += r.getLength();
        }
        return result;
    }

    /**
     * After simulating the risks of each intersection for each condition, takes the hashmaps of the intersections
     * and their condition risk, and turns it into a condition object. Also simulates the road conditions, by
     * taking their source and destination intersections, and averaging the conditions of both of them to get the
     * conditions of the road.
     *
     * @param theWeather The map of the intersections on the map and their weather risk.
     * @param theObstacles The map of the intersections on the map and their obstacle risk.
     * @param theTraffic The map of the intersections on the map and their traffic risk.
     */
    private void setAllConditions(final HashMap<Intersection, Double> theWeather,
                                  final HashMap<Intersection, Double> theObstacles,
                                  final HashMap<Intersection, Double> theTraffic) {
        for (Intersection i: myMap.getAllIntersections()) {
            applyCondition(i, theWeather.get(i), theObstacles.get(i), theTraffic.get(i));
        }
        for (Road r: myMap.getAllRoads()) {
            Intersection source = r.getSource();
            Intersection dest = r.getDestination();
            double weatherFactor = (theWeather.get(source) + theWeather.get(dest)) / 2;
            double blockageFactor = (theObstacles.get(source) + theObstacles.get(dest)) / 2;
            double trafficFactor = (theTraffic.get(source) + theTraffic.get(dest)) / 2;
            applyCondition(r, weatherFactor, blockageFactor, trafficFactor);
        }
    }

    /**
     * Simulates the risk of all the intersections in the map for one specific condition. Adds all of the intersections
     * and their risk level for the condition in the HashMap of the conditions.
     * (Idea of implementation: select random intersection as the epicenter of a condition cluster and a random radius
     * then traverse around the radius until either the limit, which is theDistance, is reached, or we've traversed
     * all  the points using BFS around that road. when traversing to a road, we'll set the condition based on the
     * random factor of the road, and if there's conflicts (we've already set the weather factor), then we'll
     * add to the weather factor).
     *
     * @param theDistance The amount of distance (percentage of the map) we want to cover with this condition.
     * @param theConditionOrigin The lowest risk level of the epicenter of a problem zone, higher number = more extreme.
     * @param theConditionBound The highest risk level of the epicenter of a problem zone.
     * @param theConditions The map the risk levels and the intersections will be placed into.
     */
    private void simulateSingleCondition(final double theDistance, final double theConditionOrigin,
                                 final double theConditionBound, final HashMap<Intersection, Double> theConditions) {
        double distanceTraversed = 0;
        Intersection[] interList = myMap.getAllIntersections();
        int problemZonesCreated = 0;

        while (distanceTraversed < theDistance) {
            // set up the epicenter of the condition event, don't set the condition to be too close to 1, it can still get to 1 other ways
            Intersection epicenter = interList[myRand.nextInt(0, interList.length)];
//            double epiCondition = myRand.nextDouble(theConditionOrigin, theConditionBound); // mild to pretty severe weather at the center
            // we want to affect everything within this radius
            double radius = myRand.nextDouble(0, theDistance - (distanceTraversed));
            // If we have a lower radius, we want to reduce the epicenter's condition slightly
            double radiusRatio  = 1 - radius / theDistance; // low when radius is close to distance, high when it isn't
            double radiusMultiplier = theConditionBound * radiusRatio; // if radius is low, this number will be closer to 100% of condition bound
            double conditionMax = theConditionBound - (radiusMultiplier);
            if (conditionMax < theConditionOrigin) {
                conditionMax = conditionMax + theConditionOrigin;
            }
            double epiCondition = myRand.nextDouble(theConditionOrigin, conditionMax); // mild to pretty severe weather at the center


            double searchDistance = makeConditionCluster(radius, epicenter, epiCondition, theConditionOrigin, theConditions);
            distanceTraversed += searchDistance;
            problemZonesCreated++;
            if (DEBUG_MODE) {
                System.out.println("Epicenter: " + epicenter.getID() + ", " + epiCondition + ", radius=" + radius + "m");
            }
        }
        if (DEBUG_MODE) {
            System.out.println("Problem zones created: " + problemZonesCreated);
            System.out.println("Distance Traveled While Simulating Condition: " + distanceTraversed
                    + ", Distance Threshold: " + theDistance);
        }
    }

    /**
     * Takes the hashmap of a specific intersection, and simulates the conditions for all the intersections not in
     * the map. Basically applies light conditions to all intersections not in a problem zone or not simulated yet.
     *
     * @param theMap The hashmap of intersections and conditions we want to place the condition into.
     * @param theBound The maximum risk level of an intersection we'll place into the map, none to light severity.
     */
    private void fillOutCondition(final HashMap<Intersection, Double> theMap, final double theBound) {
        for (Intersection i : myMap.getAllIntersections()) {
            if (!theMap.containsKey(i)) {
                theMap.put(i, myRand.nextDouble(theBound));
            }
        }
    }

    private void addToCondition(final Intersection inter1, final double theAmount,
                                final HashMap<Intersection, Double> theMap) {
        if (theMap.containsKey(inter1)) {
            theMap.put(inter1, theMap.get(inter1) * (1 + (theAmount/4)));
        } else {
            theMap.put(inter1, theAmount);
        }
        if (theMap.get(inter1) >= 1) {
            theMap.put(inter1, 1.0);
        }
    }

    private double makeConditionCluster(final double theRadius, final Intersection theOrigin,
                                        final double theOriginCondition, final double theConditionMin,
                                        final HashMap<Intersection, Double> theConditionMap) {
        // set up our bfs from the origin
        Queue<Intersection> bfsQueue =  new LinkedList<>();
        HashMap<Intersection, double[]> distances = new HashMap<>();
        HashMap<Intersection, Intersection> visited = new HashMap<>(); // <inter1, theprevious>
        double searchDistance = 0;
        distances.put(theOrigin, new double[] {0, 0, 0, 0});
        bfsQueue.add(theOrigin);
        visited.put(theOrigin, null);

        while (!bfsQueue.isEmpty() && searchDistance < theRadius) {
            Intersection currEdge = bfsQueue.poll();
            double totalDistance = distanceFromOrigin(distances.get(currEdge));
            // the line of code below could lead to a negative number, so set it to the absolute value or 0
            double decay  = 1.0 - distanceFromOrigin(distances.get(currEdge)) / theRadius;
            if (decay <= MINIMUM_DECAY_MULTIPLIER) {
                decay = MINIMUM_DECAY_MULTIPLIER;
            }
            double intersectionCondition = decay * theOriginCondition;
            if (intersectionCondition < theConditionMin) {
                // if condition less than min, then set it to min + (how much the origin is decayed)
                intersectionCondition = theConditionMin;
            }
            addToCondition(currEdge, intersectionCondition, theConditionMap);
            if (visited.get(currEdge) != null) {
                searchDistance += CityMap.getRoad(currEdge, visited.get(currEdge)).getLength();
            }

            if (withinDistance(totalDistance, theRadius)) {
                // mark the visited nodes
                for (Road r : currEdge.getRoadList()) { // get the intersections this is connected to
                    Intersection nonOriginNode;
                    if (!r.getSource().equals(currEdge)) { // get whichever intersection in the road isn't our current
                        nonOriginNode = r.getSource();
                    } else {
                        nonOriginNode = r.getDestination();
                    }
                    visited.put(nonOriginNode, currEdge);
                    distances.put(nonOriginNode, addCartesianDistances(currEdge, nonOriginNode, distances.get(currEdge)));
                    bfsQueue.add(nonOriginNode);
                }
            } // if we're out of bounds, don't add the adjacent nodes
        }
        return searchDistance;
    }


    // get the distance matrix [North, South, East, West] of an intersection using previous distance matrix
    private double[] addCartesianDistances(final Intersection theFrom, final Intersection theTo,
                                           final double[] distances) {
        Road r = CityMap.getRoad(theFrom, theTo);
        double[] result = distances.clone();
        if (r != null) {
            switch (r.getDirection(theFrom)) {
                case NORTH: result[NORTH_DISTANCE_INDEX] += r.getLength();
                    break;
                case SOUTH: result[SOUTH_DISTANCE_INDEX] += r.getLength();
                    break;
                case EAST: result[EAST_DISTANCE_INDEX] += r.getLength();
                    break;
                case WEST: result[WEST_DISTANCE_INDEX] += r.getLength();
                    break;
                default: break;
            }
        }
        return result;
    }

    private double distanceFromOrigin(final double[] theDistances) {
        double theX = Math.abs(theDistances[NORTH_DISTANCE_INDEX] - theDistances[SOUTH_DISTANCE_INDEX]);
        double theY = Math.abs(theDistances[EAST_DISTANCE_INDEX] - theDistances[WEST_DISTANCE_INDEX]);
        return Math.sqrt((theX * theX) + (theY * theY));
    }

    private boolean withinDistance(final double theDistance, final double theRadius) {
        return theDistance <= theRadius;
    }
}
