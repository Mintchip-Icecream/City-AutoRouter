package Simulation;

import Map.CityMap;
import Map.Intersection;
import Map.Road;

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
    private static final boolean DEBUG_MODE = true;
    private static final Conditions DEFAULT_CONDITION = new Conditions(0, 0, 0);
    private static final double LIGHT_BLOCKAGE = 0.333;
    private static final double LIGHT_WEATHER = 0.333;
    private static final double LIGHT_TRAFFIC = 0.333;
    private static final double MINIMUM_CONDITION_COVERAGE = 0.1;
    private static final double MAX_BLOCKAGE_COVERAGE = 0.5;
    private static final double MAX_WEATHER_COVERAGE = 0.6;
    private static final double MAX_TRAFFIC_COVERAGE = 0.6;
    private static final double SEVERITY_BOUND = 0.7;
    private static final double MINIMUM_DECAY_MULTIPLIER = 0.001;
    private static final int OBJECTS_PER_LINE_PRINT = 3;
    private static final int DECIMAL_TO_PERCENT = 100;
    private static final int NORTH_DISTANCE_INDEX = 0;
    private static final int SOUTH_DISTANCE_INDEX = 1;
    private static final int EAST_DISTANCE_INDEX = 2;
    private static final int WEST_DISTANCE_INDEX = 3;
    private final CityMap myMap;
    private final HashMap<Intersection, Conditions> myIntersections = new HashMap<>();
    private final HashMap<Road, Conditions> myRoads = new HashMap<>();
    private final Random myRand;

    public EnvironmentSimulator(final CityMap theMap, final long theRNGSeed) {
        this.myMap = theMap;
        this.myRand = new Random(theRNGSeed);
        simulateConditions();
    }

    public final Conditions getCondition(final Intersection theIntersection) {
        if (myIntersections.containsKey(theIntersection)) {
            return myIntersections.get(theIntersection);
        }
        return DEFAULT_CONDITION;
    }

    public final Conditions getCondition(final Road theRoad) {
        if (myRoads.containsKey(theRoad)) {
            return myRoads.get(theRoad);
        }
        return DEFAULT_CONDITION;
    }

    // checks if map is the same as the one this is initialized under
    public final boolean compareMap(final CityMap theOther) {
        return theOther.equals(myMap);
    }

    public final HashMap<Intersection, Conditions> getIntersectionConditions() {
        return new HashMap<>(myIntersections);
    }

    public final HashMap<Road, Conditions> getRoadConditions() {
        return new HashMap<>(myRoads);
    }


    private void applyCondition(final Intersection inter1, final double theWeather,
                                final double theBlockage, final double theTraffic) {
        Conditions newCon = new Conditions(theWeather, theBlockage, theTraffic);
        myIntersections.put(inter1, newCon);
    }

    private void applyCondition(final Road road1, final double theWeather,
                                final double theBlockage, final double theTraffic) {
        Conditions newCon = new Conditions(theWeather, theBlockage, theTraffic);
        myRoads.put(road1, newCon);
    }

    // principle of simulating: we randomly set the radius of effects (so if it's rainy in 1 edge, it should be
    // rainy for a few kilometers more)
    private void simulateConditions() {
        HashMap<Intersection, Double> weatherFactors = new HashMap<>();
        HashMap<Intersection, Double> blockageFactors = new HashMap<>();
        HashMap<Intersection, Double> trafficFactors = new HashMap<>();
        double distance = totalMapDistance(); // we want at last 1/3 of the map to have fairly extreme

        if (DEBUG_MODE) {
            System.out.println("Total Mileage of Map: " + distance);
            System.out.println("Simulating Weather...");
        }

        // set the approximate distance we want to be affected by each condition, then run a simulation on the map for it

        // set weather parameters
        double distanceAffectedByWeather = distance * myRand.nextDouble(MINIMUM_CONDITION_COVERAGE, MAX_WEATHER_COVERAGE);
        simulateSingleCondition(distanceAffectedByWeather, LIGHT_WEATHER, SEVERITY_BOUND, weatherFactors);
        fillOutCondition(weatherFactors, LIGHT_WEATHER);

        if (DEBUG_MODE) {
            System.out.println("Printing Weather Simulation.Conditions for all locations and intersections:");
            printMap(weatherFactors);
            System.out.println("Simulating traffic...");
        }

        // set traffic parameters
        double distanceAffectedByTraffic = distance * myRand.nextDouble(MINIMUM_CONDITION_COVERAGE, MAX_TRAFFIC_COVERAGE);
        simulateSingleCondition(distanceAffectedByTraffic, LIGHT_TRAFFIC, SEVERITY_BOUND, trafficFactors);
        fillOutCondition(trafficFactors, LIGHT_TRAFFIC);

        if (DEBUG_MODE) {
            System.out.println("Printing Traffic Simulation.Conditions for all locations and intersections:");
            printMap(trafficFactors);
            System.out.println("Simulating obstacles...");
        }

        double distanceAffectedByObstacles = distance * myRand.nextDouble(MINIMUM_CONDITION_COVERAGE, MAX_BLOCKAGE_COVERAGE);
        simulateSingleCondition(distanceAffectedByObstacles, LIGHT_BLOCKAGE, SEVERITY_BOUND, blockageFactors);
        fillOutCondition(blockageFactors, LIGHT_BLOCKAGE);

        if (DEBUG_MODE) {
            System.out.println("Printing Obstacle Simulation.Conditions for all locations and intersections:");
            printMap(blockageFactors);
            System.out.println("Simulation Completed!");
        }

        setAllConditions(weatherFactors, blockageFactors, trafficFactors);

    }

    private void printMap(final HashMap<Intersection, Double> theConditionMap) {
        int counter = 0;
        for (Intersection i: myMap.getAllIntersections()) {
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


    private double totalMapDistance() {
        double result = 0;
        for (Road r: myMap.getAllRoads()) {
            result += r.getLength();
        }
        return result;
    }

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
     * idea for process: select random intersection as the epicenter of a condition cluster and a random radius
     * then traverse around the radius until either the limit, which is theDistance, is reached, or we've traversed
     * all of the points using BFS around that road. when traversing to a road, we'll set the condition based on the
     * random factor of the road, and if there's conflicts (we've already set the weather factor), then we'll
     * add to the weather factor
     */
    private void simulateSingleCondition(final double theDistance, final double theConditionOrigin,
                                 final double theConditionBound, final HashMap<Intersection, Double> theConditions) {
        double distanceTraversed = 0;
        Intersection[] interList = myMap.getAllIntersections();
        int problemZonesCreated = 0;

        while (distanceTraversed < theDistance) {
            // set up the epicenter of the condition event, don't set the condition to be too close to 1, it can still get to 1 other ways
            Intersection epicenter = interList[myRand.nextInt(0, interList.length)];
            double epiCondition = myRand.nextDouble(theConditionOrigin, theConditionBound); // mild to pretty severe weather at the center
            // we want to affect everything within this radius
            double radius = myRand.nextDouble(0, theDistance - (distanceTraversed));

            double searchDistance = makeConditionCluster(radius, epicenter, epiCondition, theConditions);
            distanceTraversed += searchDistance;
            problemZonesCreated++;
            if (DEBUG_MODE) {
                System.out.println("Epicenter: " + epicenter.getID() + ", " + epiCondition);
            }
        }
        if (DEBUG_MODE) {
            System.out.println("Problem zones created: " + problemZonesCreated);
            System.out.println("Distance Traveled While Simulating Condition: " + distanceTraversed
                    + ", Distance Threshold: " + theDistance);
        }
    }

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
            theMap.put(inter1, theMap.get(inter1) * (1 + theAmount));
            if (theMap.get(inter1) >= 1) {
                theMap.put(inter1, 1.0);
            }
        } else {
            theMap.put(inter1, theAmount);
        }
    }

    private double makeConditionCluster(final double theRadius, final Intersection theOrigin,
                                        final double theOriginCondition,
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
            if (decay <= 0) {
                decay = MINIMUM_DECAY_MULTIPLIER;
            }
            addToCondition(currEdge, decay * theOriginCondition, theConditionMap);
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
